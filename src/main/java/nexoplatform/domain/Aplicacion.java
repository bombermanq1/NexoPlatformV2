package nexoplatform.domain;

public class Aplicacion {
    private final int id;
    private String nombre;
    private final String descripcion;
    private final String tecnologia;
    private boolean activa;
    private final int desarrolladorId;

    public Aplicacion(int id, String nombre, String descripcion, String tecnologia, boolean activa, int desarrolladorId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tecnologia = tecnologia;
        this.activa = activa;
        this.desarrolladorId = desarrolladorId;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTecnologia() {
        return tecnologia;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public int getDesarrolladorId() {
        return desarrolladorId;
    }

    @Override
    public String toString() {
        return id + " | " + nombre + " | " + tecnologia + " | " + descripcion
                + " | desarrollador " + desarrolladorId + (activa ? "" : " [inactiva]");
    }
}
