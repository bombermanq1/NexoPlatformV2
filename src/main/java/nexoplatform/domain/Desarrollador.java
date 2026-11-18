package nexoplatform.domain;

public class Desarrollador extends Usuario {
    private String equipo;

    public Desarrollador(int id, String nombre, String email, boolean activo, String equipo) {
        super(id, nombre, email, activo);
        this.equipo = equipo;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }
}
