package nexoplatform.domain;

public class ResponsablePlataforma extends Usuario {
    private String area;

    public ResponsablePlataforma(int id, String nombre, String email, boolean activo, String area) {
        super(id, nombre, email, activo);
        this.area = area;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
