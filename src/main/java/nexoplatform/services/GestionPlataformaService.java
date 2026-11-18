package nexoplatform.services;

import nexoplatform.domain.Aplicacion;
import nexoplatform.domain.Entorno;
import nexoplatform.domain.PlanRecursos;
import nexoplatform.domain.TipoEntorno;
import nexoplatform.domain.Usuario;

import java.sql.SQLException;
import java.util.List;

public interface GestionPlataformaService {

    int registrarUsuario(Usuario usuario) throws SQLException;

    int registrarAplicacion(Aplicacion aplicacion) throws SQLException;

    int configurarEntorno(int aplicacionId, TipoEntorno tipo, int planId) throws SQLException;

    List<Usuario> listarUsuarios() throws SQLException;

    List<Aplicacion> listarAplicaciones() throws SQLException;

    List<Entorno> listarEntornos() throws SQLException;

    List<PlanRecursos> listarPlanes() throws SQLException;

    boolean desactivarUsuario(int id) throws SQLException;

    boolean desactivarAplicacion(int id) throws SQLException;
}
