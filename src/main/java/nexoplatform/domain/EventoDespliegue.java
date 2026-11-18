package nexoplatform.domain;

import java.time.LocalDateTime;

public class EventoDespliegue {
    private final int id;
    private final LocalDateTime fecha;
    private final String descripcion;
    private final EstadoSolicitud estadoResultante;

    public EventoDespliegue(int id, LocalDateTime fecha, String descripcion, EstadoSolicitud estadoResultante) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.estadoResultante = estadoResultante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public EstadoSolicitud getEstadoResultante() {
        return estadoResultante;
    }

    public void sellar() {
        if (fecha == null || descripcion == null || descripcion.isBlank() || estadoResultante == null) {
            throw new IllegalArgumentException("El evento requiere fecha, descripción y estado.");
        }
    }

    @Override
    public String toString() {
        return id + " | " + fecha + " | " + estadoResultante + " | " + descripcion;
    }
}
