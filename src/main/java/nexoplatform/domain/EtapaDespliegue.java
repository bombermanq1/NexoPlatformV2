package nexoplatform.domain;

public class EtapaDespliegue {
    private final int orden;
    private final String nombre;
    private EstadoEtapa estado;
    private String detalleError;

    public EtapaDespliegue(int orden, String nombre, EstadoEtapa estado, String detalleError) {
        this.orden = orden;
        this.nombre = nombre;
        this.estado = estado;
        this.detalleError = detalleError;
    }

    public int getOrden() {
        return orden;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoEtapa getEstado() {
        return estado;
    }

    public void setEstado(EstadoEtapa estado) {
        this.estado = estado;
    }

    public String getDetalleError() {
        return detalleError;
    }

    public void setDetalleError(String detalleError) {
        this.detalleError = detalleError;
    }

    @Override
    public String toString() {
        return orden + ". " + nombre + " | " + estado + (detalleError == null ? "" : " | " + detalleError);
    }
}
