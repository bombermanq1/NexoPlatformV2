package nexoplatform.domain;

import java.time.LocalDateTime;
import java.util.List;

public class SimulacionDespliegue {
    private final int id;
    private final LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoSimulacion estado;
    private final PlanRecursos plan;
    private List<EtapaDespliegue> etapas;

    public SimulacionDespliegue(int id, LocalDateTime fechaInicio, LocalDateTime fechaFin, EstadoSimulacion estado,
            PlanRecursos plan, List<EtapaDespliegue> etapas) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.plan = plan;
        setEtapas(etapas);
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public PlanRecursos getPlan() {
        return plan;
    }

    public EstadoSimulacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSimulacion estado) {
        this.estado = estado;
    }

    public List<EtapaDespliegue> getEtapas() {
        return etapas;
    }

    public void setEtapas(List<EtapaDespliegue> etapas) {
        if (etapas.size() != 3) {
            throw new IllegalArgumentException("La simulación debe tener tres etapas.");
        }
        for (int i = 0; i < etapas.size(); i++) {
            if (etapas.get(i).getOrden() != i + 1) {
                throw new IllegalArgumentException("Las etapas deben estar ordenadas del 1 al 3.");
            }
        }
        this.etapas = List.copyOf(etapas);
    }

    @Override
    public String toString() {
        return id + " | " + estado + " | plan " + plan.getId()
                + " | inicio " + (fechaInicio == null ? "pendiente" : fechaInicio)
                + " | fin " + (fechaFin == null ? "pendiente" : fechaFin);
    }
}
