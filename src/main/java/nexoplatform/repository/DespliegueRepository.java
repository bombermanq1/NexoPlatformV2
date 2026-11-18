package nexoplatform.repository;

import nexoplatform.domain.Entorno;
import nexoplatform.domain.EstadoEtapa;
import nexoplatform.domain.EstadoSimulacion;
import nexoplatform.domain.EstadoSolicitud;
import nexoplatform.domain.EtapaDespliegue;
import nexoplatform.domain.EventoDespliegue;
import nexoplatform.domain.HistorialDespliegue;
import nexoplatform.domain.SimulacionDespliegue;
import nexoplatform.domain.SolicitudDespliegue;
import nexoplatform.domain.TipoEntorno;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DespliegueRepository {

    public int guardarSolicitud(Connection conexion, SolicitudDespliegue solicitud) throws SQLException {
        String sql = """
                INSERT INTO solicitudes_despliegue
                    (fecha_creacion, artefacto_referencia, estado, desarrollador_id, entorno_id)
                VALUES (?, ?, ?, ?, ?) RETURNING id
                """;
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setObject(1, solicitud.getFechaCreacion());
            consulta.setString(2, solicitud.getArtefactoReferencia());
            consulta.setString(3, solicitud.getEstado().name());
            consulta.setInt(4, solicitud.getDesarrolladorId());
            consulta.setInt(5, solicitud.getEntorno().getId());
            return CatalogoRepository.obtenerId(consulta);
        }
    }

    public HistorialDespliegue crearHistorial(Connection conexion, int solicitudId) throws SQLException {
        String sql = "INSERT INTO historiales_despliegue (solicitud_id) VALUES (?) RETURNING id";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, solicitudId);
            return new HistorialDespliegue(CatalogoRepository.obtenerId(consulta));
        }
    }

    public HistorialDespliegue buscarHistorial(Connection conexion, int solicitudId) throws SQLException {
        String sql = """
                SELECT h.id AS historial_id, e.id AS evento_id, e.fecha, e.descripcion, e.estado_resultante
                FROM historiales_despliegue h
                LEFT JOIN eventos_despliegue e ON e.historial_id = h.id
                WHERE h.solicitud_id = ? ORDER BY e.id
                """;
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, solicitudId);
            try (ResultSet datos = consulta.executeQuery()) {
                if (!datos.next()) {
                    return null;
                }
                HistorialDespliegue historial = new HistorialDespliegue(datos.getInt("historial_id"));
                do {
                    if (datos.getObject("evento_id") != null) {
                        historial.registrarEvento(new EventoDespliegue(datos.getInt("evento_id"),
                                datos.getObject("fecha", LocalDateTime.class), datos.getString("descripcion"),
                                EstadoSolicitud.valueOf(datos.getString("estado_resultante"))));
                    }
                } while (datos.next());
                return historial;
            }
        }
    }

    public void registrarEvento(Connection conexion, int historialId, EventoDespliegue evento) throws SQLException {
        evento.sellar();
        String sql = """
                INSERT INTO eventos_despliegue (historial_id, fecha, descripcion, estado_resultante)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, historialId);
            consulta.setObject(2, evento.getFecha());
            consulta.setString(3, evento.getDescripcion());
            consulta.setString(4, evento.getEstadoResultante().name());
            consulta.executeUpdate();
        }
    }

    private static final String SOLICITUDES = """
            SELECT s.id, s.fecha_creacion, s.artefacto_referencia, s.estado, s.motivo_error,
                   s.desarrollador_id, s.entorno_id, e.tipo, e.aplicacion_id
            FROM solicitudes_despliegue s JOIN entornos e ON e.id = s.entorno_id
            """;

    public SolicitudDespliegue buscarSolicitud(Connection conexion, int id) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(SOLICITUDES + "WHERE s.id = ? FOR UPDATE OF s")) {
            consulta.setInt(1, id);
            try (ResultSet datos = consulta.executeQuery()) {
                return datos.next() ? leerSolicitud(datos) : null;
            }
        }
    }

    public List<SolicitudDespliegue> listarSolicitudes(Connection conexion) throws SQLException {
        List<SolicitudDespliegue> solicitudes = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(SOLICITUDES + "ORDER BY s.id");
             ResultSet datos = consulta.executeQuery()) {
            while (datos.next()) {
                solicitudes.add(leerSolicitud(datos));
            }
        }
        return solicitudes;
    }

    private SolicitudDespliegue leerSolicitud(ResultSet datos) throws SQLException {
        Entorno entorno = new Entorno(datos.getInt("entorno_id"), TipoEntorno.valueOf(datos.getString("tipo")),
                datos.getInt("aplicacion_id"));
        return new SolicitudDespliegue(datos.getInt("id"), datos.getObject("fecha_creacion", LocalDateTime.class),
                datos.getString("artefacto_referencia"), EstadoSolicitud.valueOf(datos.getString("estado")),
                datos.getString("motivo_error"), datos.getInt("desarrollador_id"), entorno);
    }

    public void actualizarSolicitud(Connection conexion, SolicitudDespliegue solicitud) throws SQLException {
        String sql = "UPDATE solicitudes_despliegue SET estado = ?, motivo_error = ? WHERE id = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, solicitud.getEstado().name());
            consulta.setString(2, solicitud.getMotivoError());
            consulta.setInt(3, solicitud.getId());
            consulta.executeUpdate();
        }
    }

    public int guardarSimulacion(Connection conexion, SimulacionDespliegue simulacion, int solicitudId,
                                 int responsableId) throws SQLException {
        String sql = """
                INSERT INTO simulaciones_despliegue (fecha_inicio, estado, solicitud_id, plan_id, responsable_id)
                VALUES (?, ?, ?, ?, ?) RETURNING id
                """;
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setObject(1, simulacion.getFechaInicio());
            consulta.setString(2, simulacion.getEstado().name());
            consulta.setInt(3, solicitudId);
            consulta.setInt(4, simulacion.getPlan().getId());
            consulta.setInt(5, responsableId);
            return CatalogoRepository.obtenerId(consulta);
        }
    }

    public boolean existeSimulacion(Connection conexion, int solicitudId) throws SQLException {
        String sql = "SELECT 1 FROM simulaciones_despliegue WHERE solicitud_id = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, solicitudId);
            try (ResultSet datos = consulta.executeQuery()) {
                return datos.next();
            }
        }
    }

    public SimulacionDespliegue buscarSimulacion(Connection conexion, int solicitudId) throws SQLException {
        String sql = """
                SELECT s.id, s.fecha_inicio, s.fecha_fin, s.estado, p.id AS plan_id,
                       p.nombre AS plan_nombre, p.vcpu, p.ram_gb, p.almacenamiento_gb
                FROM simulaciones_despliegue s JOIN planes_recursos p ON p.id = s.plan_id
                WHERE s.solicitud_id = ?
                """;
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, solicitudId);
            try (ResultSet datos = consulta.executeQuery()) {
                if (!datos.next()) {
                    return null;
                }
                int id = datos.getInt("id");
                return new SimulacionDespliegue(id, datos.getObject("fecha_inicio", LocalDateTime.class),
                        datos.getObject("fecha_fin", LocalDateTime.class),
                        EstadoSimulacion.valueOf(datos.getString("estado")),
                        CatalogoRepository.leerPlan(datos), listarEtapas(conexion, id));
            }
        }
    }

    private List<EtapaDespliegue> listarEtapas(Connection conexion, int simulacionId) throws SQLException {
        String sql = """
                SELECT orden, nombre, estado, detalle_error FROM etapas_despliegue
                WHERE simulacion_id = ? ORDER BY orden
                """;
        List<EtapaDespliegue> etapas = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, simulacionId);
            try (ResultSet datos = consulta.executeQuery()) {
                while (datos.next()) {
                    etapas.add(new EtapaDespliegue(datos.getInt("orden"), datos.getString("nombre"),
                            EstadoEtapa.valueOf(datos.getString("estado")), datos.getString("detalle_error")));
                }
            }
        }
        return etapas;
    }

    public void guardarEtapas(Connection conexion, int simulacionId, List<EtapaDespliegue> etapas) throws SQLException {
        String sql = "INSERT INTO etapas_despliegue (simulacion_id, orden, nombre, estado) VALUES (?, ?, ?, ?)";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            for (EtapaDespliegue etapa : etapas) {
                consulta.setInt(1, simulacionId);
                consulta.setInt(2, etapa.getOrden());
                consulta.setString(3, etapa.getNombre());
                consulta.setString(4, etapa.getEstado().name());
                consulta.executeUpdate();
            }
        }
    }

    public void actualizarEtapa(Connection conexion, int simulacionId, EtapaDespliegue etapa) throws SQLException {
        String sql = """
                UPDATE etapas_despliegue SET estado = ?, detalle_error = ?
                WHERE simulacion_id = ? AND orden = ?
                """;
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, etapa.getEstado().name());
            consulta.setString(2, etapa.getDetalleError());
            consulta.setInt(3, simulacionId);
            consulta.setInt(4, etapa.getOrden());
            consulta.executeUpdate();
        }
    }

    public void finalizarSimulacion(Connection conexion, int id, SimulacionDespliegue simulacion) throws SQLException {
        String sql = "UPDATE simulaciones_despliegue SET estado = ?, fecha_fin = ? WHERE id = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, simulacion.getEstado().name());
            consulta.setObject(2, simulacion.getFechaFin());
            consulta.setInt(3, id);
            consulta.executeUpdate();
        }
    }
}
