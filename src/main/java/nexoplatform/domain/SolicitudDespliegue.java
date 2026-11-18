package nexoplatform.domain;

import java.time.LocalDateTime;

public class SolicitudDespliegue {
    private final int id;
    private final LocalDateTime fechaCreacion;
    private final String artefactoReferencia;
    private EstadoSolicitud estado;
    private String motivoError;
    private final int desarrolladorId;
    private final Entorno entorno;

    public SolicitudDespliegue(int id, LocalDateTime fechaCreacion, String artefactoReferencia, EstadoSolicitud estado, String motivoError, int desarrolladorId, Entorno entorno) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.artefactoReferencia = artefactoReferencia;
        this.estado = estado;
        this.motivoError = motivoError;
        this.desarrolladorId = desarrolladorId;
        this.entorno = entorno;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public String getArtefactoReferencia() {
        return artefactoReferencia;
    }

    public String getMotivoError() {
        return motivoError;
    }

    public void setMotivoError(String motivoError) {
        this.motivoError = motivoError;
    }

    public int getDesarrolladorId() {
        return desarrolladorId;
    }

    public Entorno getEntorno() {
        return entorno;
    }

    @Override
    public String toString() {
        return id + " | " + artefactoReferencia + " | " + estado + " | " + fechaCreacion
                + " | entorno " + entorno.getId() + " (" + entorno.getTipo() + ")"
                + " | aplicación " + entorno.getAplicacionId()
                + (motivoError == null ? "" : " | " + motivoError);
    }
}
