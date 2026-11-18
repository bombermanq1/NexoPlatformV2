package nexoplatform.services;

import nexoplatform.config.ConexionBD;
import nexoplatform.domain.Aplicacion;
import nexoplatform.domain.Entorno;
import nexoplatform.domain.PlanRecursos;
import nexoplatform.domain.TipoEntorno;
import nexoplatform.domain.Usuario;
import nexoplatform.repository.CatalogoRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class GestionPlataformaServiceImpl implements GestionPlataformaService {
    private final CatalogoRepository catalogo;

    public GestionPlataformaServiceImpl(CatalogoRepository catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public int registrarUsuario(Usuario usuario) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int id = catalogo.guardarUsuario(conexion, usuario);
                conexion.commit();
                return id;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    @Override
    public int registrarAplicacion(Aplicacion aplicacion) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                if (!catalogo.desarrolladorActivo(conexion, aplicacion.getDesarrolladorId())) {
                    throw new IllegalArgumentException("Selecciona un desarrollador activo.");
                }
                int id = catalogo.guardarAplicacion(conexion, aplicacion);
                conexion.commit();
                return id;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    @Override
    public int configurarEntorno(int aplicacionId, TipoEntorno tipo, int planId) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                if (!catalogo.aplicacionActiva(conexion, aplicacionId)) {
                    throw new IllegalArgumentException("Selecciona una aplicación activa.");
                }
                Entorno entorno = new Entorno(0, tipo, aplicacionId);
                if (planId != 0) {
                    PlanRecursos plan = catalogo.buscarPlan(conexion, planId);
                    if (plan == null) {
                        throw new IllegalArgumentException("El plan de recursos no existe.");
                    }
                    entorno.setPlan(plan);
                }
                int id = catalogo.guardarEntorno(conexion, entorno);
                conexion.commit();
                return id;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    @Override
    public List<Usuario> listarUsuarios() throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            return catalogo.listarUsuarios(conexion);
        }
    }

    @Override
    public List<Aplicacion> listarAplicaciones() throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            return catalogo.listarAplicaciones(conexion);
        }
    }

    @Override
    public List<Entorno> listarEntornos() throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            return catalogo.listarEntornos(conexion);
        }
    }

    @Override
    public List<PlanRecursos> listarPlanes() throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            return catalogo.listarPlanes(conexion);
        }
    }

    @Override
    public boolean desactivarUsuario(int id) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            return catalogo.desactivarUsuario(conexion, id);
        }
    }

    @Override
    public boolean desactivarAplicacion(int id) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            return catalogo.desactivarAplicacion(conexion, id);
        }
    }
}
