package nexoplatform.domain;

public abstract class Usuario {
    private final int id;
    private final String nombre;
    private final String email;
    private boolean activo;

    public Usuario(int id, String nombre, String email, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return id + " | " + nombre + (activo ? "" : " [inactivo]");
    }
}
