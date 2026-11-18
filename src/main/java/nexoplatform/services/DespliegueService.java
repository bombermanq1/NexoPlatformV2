package nexoplatform.services;

import nexoplatform.domain.HistorialDespliegue;
import nexoplatform.domain.SimulacionDespliegue;
import nexoplatform.domain.SolicitudDespliegue;

import java.sql.SQLException;
import java.util.List;

public interface DespliegueService {

    int crearSolicitud(int desarrolladorId, int entornoId, String artefacto) throws SQLException;

    int ejecutarSimulacion(int solicitudId, int responsableId) throws SQLException;

    List<SolicitudDespliegue> listarSolicitudes() throws SQLException;

    SimulacionDespliegue buscarSimulacion(int solicitudId) throws SQLException;

    HistorialDespliegue buscarHistorial(int solicitudId) throws SQLException;
}
