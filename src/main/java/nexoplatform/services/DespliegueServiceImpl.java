package nexoplatform.services;

import nexoplatform.config.ConexionBD;
import nexoplatform.domain.Entorno;
import nexoplatform.domain.EstadoEtapa;
import nexoplatform.domain.EstadoSimulacion;
import nexoplatform.domain.EstadoSolicitud;
import nexoplatform.domain.EtapaDespliegue;
import nexoplatform.domain.EventoDespliegue;
import nexoplatform.domain.HistorialDespliegue;
import nexoplatform.domain.SimulacionDespliegue;
import nexoplatform.domain.SolicitudDespliegue;
import nexoplatform.repository.CatalogoRepository;
import nexoplatform.repository.DespliegueRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DespliegueServiceImpl implements DespliegueService {
    private final CatalogoRepository catalogo;
    private final DespliegueRepository despliegues;

    public DespliegueServiceImpl(CatalogoRepository catalogo, DespliegueRepository despliegues) {
        this.catalogo = catalogo;
        this.despliegues = despliegues;
    }

    @Override
    public int crearSolicitud(int desarrolladorId, int entornoId, String artefacto) throws SQLException {
        if (artefacto == null || artefacto.isBlank()) {
            throw new IllegalArgumentException("Escribe la referencia del artefacto.");
        }

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                if (!catalogo.desarrolladorActivo(conexion, desarrolladorId)) {
                    throw new IllegalArgumentException("Selecciona un desarrollador activo.");
                }
                Entorno entorno = validarEntorno(conexion, entornoId);
                SolicitudDespliegue solicitud = new SolicitudDespliegue(0, LocalDateTime.now(), artefacto,
                        EstadoSolicitud.PENDIENTE, null, desarrolladorId, entorno);
                int id = despliegues.guardarSolicitud(conexion, solicitud);
                HistorialDespliegue historial = despliegues.crearHistorial(conexion, id);
                registrarEvento(conexion, historial, solicitud, "Solicitud registrada.");
                conexion.commit();
                return id;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    @Override
    public int ejecutarSimulacion(int solicitudId, int responsableId) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                SolicitudDespliegue solicitud = despliegues.buscarSolicitud(conexion, solicitudId);
                if (solicitud == null) {
                    throw new IllegalArgumentException("La solicitud no existe.");
                }
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE
                        || despliegues.existeSimulacion(conexion, solicitudId)) {
                    throw new IllegalArgumentException("Se requiere una solicitud pendiente sin simulación.");
                }
                if (!catalogo.responsableActivo(conexion, responsableId)) {
                    throw new IllegalArgumentException("Selecciona un responsable de plataforma activo.");
                }
                Entorno entorno = validarEntorno(conexion, solicitud.getEntorno().getId());
                if (!entorno.isConfigurado()) {
                    throw new IllegalArgumentException("El entorno necesita un plan de recursos válido.");
                }
                HistorialDespliegue historial = despliegues.buscarHistorial(conexion, solicitudId);
                if (historial == null) {
                    throw new IllegalArgumentException("La solicitud no tiene historial.");
                }

                int simulacionId = simular(conexion, solicitud, responsableId, entorno, historial);
                conexion.commit();
                return simulacionId;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    private int simular(Connection conexion, SolicitudDespliegue solicitud, int responsableId,
                        Entorno entorno, HistorialDespliegue historial) throws SQLException {
        List<EtapaDespliegue> etapas = List.of(
                new EtapaDespliegue(1, "Validación", EstadoEtapa.PENDIENTE, null),
                new EtapaDespliegue(2, "Ejecución simulada", EstadoEtapa.PENDIENTE, null),
                new EtapaDespliegue(3, "Verificación", EstadoEtapa.PENDIENTE, null));
        SimulacionDespliegue simulacion = new SimulacionDespliegue(0, LocalDateTime.now(), null,
                EstadoSimulacion.EN_EJECUCION, entorno.getPlan(), etapas);
        int simulacionId = despliegues.guardarSimulacion(conexion, simulacion, solicitud.getId(), responsableId);
        despliegues.guardarEtapas(conexion, simulacionId, simulacion.getEtapas());
        solicitud.setEstado(EstadoSolicitud.EN_EJECUCION);
        despliegues.actualizarSolicitud(conexion, solicitud);
        registrarEvento(conexion, historial, solicitud, "Simulación iniciada.");

        for (EtapaDespliegue etapa : simulacion.getEtapas()) {
            etapa.setEstado(EstadoEtapa.EN_EJECUCION);
            despliegues.actualizarEtapa(conexion, simulacionId, etapa);
            etapa.setEstado(EstadoEtapa.EXITOSA);
            despliegues.actualizarEtapa(conexion, simulacionId, etapa);
            registrarEvento(conexion, historial, solicitud, etapa.getNombre() + " completada.");
        }

        simulacion.setEstado(EstadoSimulacion.EXITOSA);
        simulacion.setFechaFin(LocalDateTime.now());
        despliegues.finalizarSimulacion(conexion, simulacionId, simulacion);
        solicitud.setEstado(EstadoSolicitud.EXITOSA);
        despliegues.actualizarSolicitud(conexion, solicitud);
        registrarEvento(conexion, historial, solicitud, "Simulación finalizada.");
        return simulacionId;
    }

    private Entorno validarEntorno(Connection conexion, int entornoId) throws SQLException {
        Entorno entorno = catalogo.buscarEntorno(conexion, entornoId);
        if (entorno == null || !catalogo.aplicacionActiva(conexion, entorno.getAplicacionId())) {
            throw new IllegalArgumentException("Selecciona un entorno de una aplicación activa.");
        }
        return entorno;
    }

    private void registrarEvento(Connection conexion, HistorialDespliegue historial,
                                 SolicitudDespliegue solicitud, String descripcion) throws SQLException {
        EventoDespliegue evento = new EventoDespliegue(0, LocalDateTime.now(), descripcion, solicitud.getEstado());
        despliegues.registrarEvento(conexion, historial.getId(), evento);
    }

    @Override
    public List<SolicitudDespliegue> listarSolicitudes() throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            return despliegues.listarSolicitudes(conexion);
        }
    }

    @Override
    public SimulacionDespliegue buscarSimulacion(int solicitudId) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            return despliegues.buscarSimulacion(conexion, solicitudId);
        }
    }

    @Override
    public HistorialDespliegue buscarHistorial(int solicitudId) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            return despliegues.buscarHistorial(conexion, solicitudId);
        }
    }
}
