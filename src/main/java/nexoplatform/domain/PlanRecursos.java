package nexoplatform.domain;

public class PlanRecursos {
    private final int id;
    private final String nombre;
    private final int vcpu;
    private final int ramGB;
    private final int almacenamientoGB;
    private boolean sellado;

    public PlanRecursos(int id, String nombre, int vcpu, int ramGB, int almacenamientoGB) {
        this.id = id;
        this.nombre = nombre;
        this.vcpu = vcpu;
        this.ramGB = ramGB;
        this.almacenamientoGB = almacenamientoGB;
    }

    public int getId() {
        return id;
    }

    public boolean isSellado() {
        return sellado;
    }

    public void sellar() {
        if (nombre == null || nombre.isBlank() || vcpu <= 0 || ramGB <= 0 || almacenamientoGB <= 0) {
            throw new IllegalArgumentException("El plan de recursos debe tener nombre y capacidades positivas.");
        }
        sellado = true;
    }

    @Override
    public String toString() {
        return id + " | " + nombre + " | " + vcpu + " vCPU | " + ramGB + " GB RAM | "
                + almacenamientoGB + " GB de almacenamiento";
    }
}
