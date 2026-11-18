package nexoplatform.repository;

import nexoplatform.domain.Aplicacion;
import nexoplatform.domain.Desarrollador;
import nexoplatform.domain.Entorno;
import nexoplatform.domain.PlanRecursos;
import nexoplatform.domain.ResponsablePlataforma;
import nexoplatform.domain.TipoEntorno;
import nexoplatform.domain.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CatalogoRepository {

    public int guardarUsuario(Connection conexion, Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, email, activo) VALUES (?, ?, ?) RETURNING id";
        int id;
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, usuario.getNombre());
            consulta.setString(2, usuario.getEmail());
            consulta.setBoolean(3, usuario.isActivo());
            id = obtenerId(consulta);
        }

        if (usuario instanceof Desarrollador desarrollador) {
            guardarPerfil(conexion, "INSERT INTO desarrolladores (usuario_id, equipo) VALUES (?, ?)",
                    id, desarrollador.getEquipo());
        } else if (usuario instanceof ResponsablePlataforma responsable) {
            guardarPerfil(conexion, "INSERT INTO responsables_plataforma (usuario_id, area) VALUES (?, ?)",
                    id, responsable.getArea());
        } else {
            throw new IllegalArgumentException("Tipo de usuario no válido.");
        }
        return id;
    }

    private void guardarPerfil(Connection conexion, String sql, int id, String dato) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            consulta.setString(2, dato);
            consulta.executeUpdate();
        }
    }

    public List<Usuario> listarUsuarios(Connection conexion) throws SQLException {
        String sql = """
                SELECT u.id, u.nombre, u.email, u.activo, d.equipo, r.area
                FROM usuarios u
                LEFT JOIN desarrolladores d ON d.usuario_id = u.id
                LEFT JOIN responsables_plataforma r ON r.usuario_id = u.id
                ORDER BY u.id
                """;
        List<Usuario> usuarios = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet datos = consulta.executeQuery()) {
            while (datos.next()) {
                int id = datos.getInt("id");
                String nombre = datos.getString("nombre");
                String email = datos.getString("email");
                boolean activo = datos.getBoolean("activo");
                String equipo = datos.getString("equipo");
                String area = datos.getString("area");
                if (equipo != null) {
                    usuarios.add(new Desarrollador(id, nombre, email, activo, equipo));
                }
                if (area != null) {
                    usuarios.add(new ResponsablePlataforma(id, nombre, email, activo, area));
                }
            }
        }
        return usuarios;
    }

    public boolean desarrolladorActivo(Connection conexion, int id) throws SQLException {
        String sql = """
                SELECT 1 FROM usuarios u JOIN desarrolladores d ON d.usuario_id = u.id
                WHERE u.id = ? AND u.activo FOR SHARE OF u
                """;
        return existe(conexion, sql, id);
    }

    public boolean responsableActivo(Connection conexion, int id) throws SQLException {
        String sql = """
                SELECT 1 FROM usuarios u JOIN responsables_plataforma r ON r.usuario_id = u.id
                WHERE u.id = ? AND u.activo FOR SHARE OF u
                """;
        return existe(conexion, sql, id);
    }

    public boolean aplicacionActiva(Connection conexion, int id) throws SQLException {
        return existe(conexion, "SELECT 1 FROM aplicaciones WHERE id = ? AND activa FOR SHARE", id);
    }

    private boolean existe(Connection conexion, String sql, int id) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            try (ResultSet datos = consulta.executeQuery()) {
                return datos.next();
            }
        }
    }

    public int guardarAplicacion(Connection conexion, Aplicacion aplicacion) throws SQLException {
        String sql = """
                INSERT INTO aplicaciones (nombre, descripcion, tecnologia, activa, desarrollador_id)
                VALUES (?, ?, ?, ?, ?) RETURNING id
                """;
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, aplicacion.getNombre());
            consulta.setString(2, aplicacion.getDescripcion());
            consulta.setString(3, aplicacion.getTecnologia());
            consulta.setBoolean(4, aplicacion.isActiva());
            consulta.setInt(5, aplicacion.getDesarrolladorId());
            return obtenerId(consulta);
        }
    }

    public List<Aplicacion> listarAplicaciones(Connection conexion) throws SQLException {
        String sql = "SELECT id, nombre, descripcion, tecnologia, activa, desarrollador_id FROM aplicaciones ORDER BY id";
        List<Aplicacion> aplicaciones = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet datos = consulta.executeQuery()) {
            while (datos.next()) {
                aplicaciones.add(new Aplicacion(datos.getInt("id"), datos.getString("nombre"),
                        datos.getString("descripcion"), datos.getString("tecnologia"),
                        datos.getBoolean("activa"), datos.getInt("desarrollador_id")));
            }
        }
        return aplicaciones;
    }

    public int guardarEntorno(Connection conexion, Entorno entorno) throws SQLException {
        String sql = """
                INSERT INTO entornos (aplicacion_id, tipo, plan_id) VALUES (?, ?, ?)
                ON CONFLICT (aplicacion_id, tipo) DO UPDATE SET plan_id = EXCLUDED.plan_id
                RETURNING id
                """;
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, entorno.getAplicacionId());
            consulta.setString(2, entorno.getTipo().name());
            if (entorno.isConfigurado()) {
                consulta.setInt(3, entorno.getPlan().getId());
            } else {
                consulta.setNull(3, Types.INTEGER);
            }
            return obtenerId(consulta);
        }
    }

    private static final String ENTORNOS_CON_PLAN = """
            SELECT e.id, e.tipo, e.aplicacion_id, p.id AS plan_id, p.nombre AS plan_nombre,
                   p.vcpu, p.ram_gb, p.almacenamiento_gb
            FROM entornos e LEFT JOIN planes_recursos p ON p.id = e.plan_id
            """;

    public Entorno buscarEntorno(Connection conexion, int id) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(ENTORNOS_CON_PLAN + "WHERE e.id = ? FOR SHARE OF e")) {
            consulta.setInt(1, id);
            try (ResultSet datos = consulta.executeQuery()) {
                return datos.next() ? leerEntorno(datos) : null;
            }
        }
    }

    public List<Entorno> listarEntornos(Connection conexion) throws SQLException {
        List<Entorno> entornos = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(ENTORNOS_CON_PLAN + "ORDER BY e.id");
             ResultSet datos = consulta.executeQuery()) {
            while (datos.next()) {
                entornos.add(leerEntorno(datos));
            }
        }
        return entornos;
    }

    private Entorno leerEntorno(ResultSet datos) throws SQLException {
        Entorno entorno = new Entorno(datos.getInt("id"), TipoEntorno.valueOf(datos.getString("tipo")),
                datos.getInt("aplicacion_id"));
        if (datos.getObject("plan_id") != null) {
            entorno.setPlan(leerPlan(datos));
        }
        return entorno;
    }

    private static final String PLANES = """
            SELECT id AS plan_id, nombre AS plan_nombre, vcpu, ram_gb, almacenamiento_gb
            FROM planes_recursos
            """;

    public PlanRecursos buscarPlan(Connection conexion, int id) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(PLANES + "WHERE id = ?")) {
            consulta.setInt(1, id);
            try (ResultSet datos = consulta.executeQuery()) {
                return datos.next() ? leerPlan(datos) : null;
            }
        }
    }

    public List<PlanRecursos> listarPlanes(Connection conexion) throws SQLException {
        List<PlanRecursos> planes = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(PLANES + "ORDER BY id");
             ResultSet datos = consulta.executeQuery()) {
            while (datos.next()) {
                planes.add(leerPlan(datos));
            }
        }
        return planes;
    }

    static PlanRecursos leerPlan(ResultSet datos) throws SQLException {
        PlanRecursos plan = new PlanRecursos(datos.getInt("plan_id"), datos.getString("plan_nombre"),
                datos.getInt("vcpu"), datos.getInt("ram_gb"), datos.getInt("almacenamiento_gb"));
        plan.sellar();
        return plan;
    }

    static int obtenerId(PreparedStatement consulta) throws SQLException {
        try (ResultSet datos = consulta.executeQuery()) {
            datos.next();
            return datos.getInt("id");
        }
    }

    public boolean desactivarUsuario(Connection conexion, int id) throws SQLException {
        return desactivar(conexion, "UPDATE usuarios SET activo = false WHERE id = ? AND activo", id);
    }

    public boolean desactivarAplicacion(Connection conexion, int id) throws SQLException {
        return desactivar(conexion, "UPDATE aplicaciones SET activa = false WHERE id = ? AND activa", id);
    }

    private boolean desactivar(Connection conexion, String sql, int id) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() == 1;
        }
    }
}
