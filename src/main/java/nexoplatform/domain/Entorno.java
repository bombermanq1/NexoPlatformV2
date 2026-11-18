package nexoplatform.domain;

public class Entorno {
    private final int id;
    private final TipoEntorno tipo;
    private final int aplicacionId;
    private PlanRecursos plan;

    public Entorno(int id, TipoEntorno tipo, int aplicacionId) {
        this.id = id;
        this.tipo = tipo;
        this.aplicacionId = aplicacionId;
    }

    public int getId() {
        return id;
    }

    public TipoEntorno getTipo() {
        return tipo;
    }

    public int getAplicacionId() {
        return aplicacionId;
    }

    public PlanRecursos getPlan() {
        return plan;
    }

    public boolean isConfigurado() {
        return plan != null && plan.isSellado();
    }

    public void setPlan(PlanRecursos plan) {
        if (plan != null && !plan.isSellado()) {
            throw new IllegalArgumentException("El plan de recursos debe estar sellado.");
        }
        this.plan = plan;
    }

    @Override
    public String toString() {
        return id + " | " + tipo + " | aplicación " + aplicacionId
                + (isConfigurado() ? " | plan " + plan.getId() : " | sin plan");
    }
}
