package nexoplatform.domain;

import java.util.LinkedList;
import java.util.List;

public class HistorialDespliegue {
    private final int id;
    private final List<EventoDespliegue> eventos = new LinkedList<>();

    public HistorialDespliegue(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void registrarEvento(EventoDespliegue evento) {
        evento.sellar();
        eventos.add(evento);
    }

    public List<EventoDespliegue> getEventos() {
        return List.copyOf(eventos);
    }

    public void setEventos(List<EventoDespliegue> eventos) {
        for (EventoDespliegue evento : eventos) {
            evento.sellar();
        }
        this.eventos.clear();
        this.eventos.addAll(eventos);
    }
}
