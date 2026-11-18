package nexoplatform.userinterface;

import nexoplatform.domain.Aplicacion;
import nexoplatform.domain.Desarrollador;
import nexoplatform.domain.Entorno;
import nexoplatform.domain.EtapaDespliegue;
import nexoplatform.domain.EventoDespliegue;
import nexoplatform.domain.HistorialDespliegue;
import nexoplatform.domain.PlanRecursos;
import nexoplatform.domain.ResponsablePlataforma;
import nexoplatform.domain.SimulacionDespliegue;
import nexoplatform.domain.SolicitudDespliegue;
import nexoplatform.domain.TipoEntorno;
import nexoplatform.domain.Usuario;
import nexoplatform.services.GestionPlataformaService;
import nexoplatform.services.DespliegueService;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class MenuApp {
    private final Scanner scanner;
    private final GestionPlataformaService catalogo;
    private final DespliegueService despliegues;

    public MenuApp(Scanner scanner, GestionPlataformaService catalogo, DespliegueService despliegues) {
        this.scanner = scanner;
        this.catalogo = catalogo;
        this.despliegues = despliegues;
    }

    public void mostrarMenu() {
        while (true) {
            System.out.println("""
                    
                    ===================================
                             NEXOPLATFORM V2
                    ===================================
                    1. Registrar usuario
                    2. Registrar aplicación
                    3. Consultar aplicaciones y entornos
                    4. Configurar entorno
                    5. Consultar planes de recursos
                    6. Crear solicitud de despliegue
                    7. Ejecutar simulación
                    8. Consultar solicitudes y simulaciones
                    9. Consultar historial
                    10. Dar de baja usuario o aplicación
                    0. Salir
                    """);
            try {
                int opcion = leerNumero("Selecciona una opción");
                switch (opcion) {
                    case 1 -> registrarUsuario();
                    case 2 -> registrarAplicacion();
                    case 3 -> consultarAplicaciones();
                    case 4 -> configurarEntorno();
                    case 5 -> listarPlanes();
                    case 6 -> crearSolicitud();
                    case 7 -> ejecutarSimulacion();
                    case 8 -> consultarSolicitudes();
                    case 9 -> consultarHistorial();
                    case 10 -> darDeBaja();
                    case 0 -> { return; }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (NoSuchElementException finDeEntrada) {
                return;
            } catch (IllegalArgumentException error) {
                System.out.println(error.getMessage());
            } catch (SQLException error) {
                mostrarErrorSQL(error);
            }
        }
    }

    private void registrarUsuario() throws SQLException {
        int tipo = leerNumero("1. Desarrollador  2. Responsable de plataforma");
        if (tipo != 1 && tipo != 2) {
            throw new IllegalArgumentException("Tipo de usuario no válido.");
        }
        String nombre = leerTexto("Nombre");
        String email = leerTexto("Correo electrónico");
        Usuario usuario;
        if (tipo == 1) {
            usuario = new Desarrollador(0, nombre, email, true, leerTexto("Equipo"));
        } else {
            usuario = new ResponsablePlataforma(0, nombre, email, true, leerTexto("Área"));
        }
        int id = catalogo.registrarUsuario(usuario);
        System.out.println("Usuario registrado con ID " + id + ".");
    }

    private void registrarAplicacion() throws SQLException {
        listarUsuariosActivos(1);
        int desarrolladorId = leerNumero("ID del desarrollador responsable");
        String nombre = leerTexto("Nombre");
        String descripcion = leerTexto("Descripción");
        String tecnologia = leerTexto("Tecnología");
        int id = catalogo.registrarAplicacion(new Aplicacion(0, nombre, descripcion, tecnologia, true, desarrolladorId));
        System.out.println("Aplicación registrada con ID " + id + ".");
    }

    private void consultarAplicaciones() throws SQLException {
        listarAplicaciones();
        listarEntornos();
    }

    private void configurarEntorno() throws SQLException {
        listarAplicaciones();
        int aplicacionId = leerNumero("ID de la aplicación");
        int opcion = leerNumero("1. DESARROLLO  2. PRUEBAS");
        TipoEntorno tipo = switch (opcion) {
            case 1 -> TipoEntorno.DESARROLLO;
            case 2 -> TipoEntorno.PRUEBAS;
            default -> throw new IllegalArgumentException("Tipo de entorno no válido.");
        };
        listarPlanes();
        int planId = leerNumero("ID del plan de recursos (0 para dejarlo sin plan)");
        int id = catalogo.configurarEntorno(aplicacionId, tipo, planId);
        System.out.println("Entorno guardado con ID " + id + ".");
    }

    private void crearSolicitud() throws SQLException {
        listarUsuariosActivos(1);
        int desarrolladorId = leerNumero("ID del desarrollador solicitante");
        listarEntornos();
        int entornoId = leerNumero("ID del entorno de destino");
        String artefacto = leerTexto("Referencia del artefacto");
        int id = despliegues.crearSolicitud(desarrolladorId, entornoId, artefacto);
        System.out.println("Solicitud registrada con ID " + id + " y estado PENDIENTE.");
    }

    private void ejecutarSimulacion() throws SQLException {
        listarSolicitudes();
        int solicitudId = leerNumero("ID de la solicitud pendiente");
        listarUsuariosActivos(2);
        int responsableId = leerNumero("ID del responsable de plataforma");
        int id = despliegues.ejecutarSimulacion(solicitudId, responsableId);
        System.out.println("Simulación " + id + " finalizada con estado EXITOSA.");
    }

    private void consultarSolicitudes() throws SQLException {
        if (!listarSolicitudes()) {
            return;
        }
        int id = leerNumero("ID de la solicitud para ver su simulación (0 para volver)");
        if (id == 0) {
            return;
        }
        SimulacionDespliegue simulacion = despliegues.buscarSimulacion(id);
        if (simulacion == null) {
            System.out.println("No hay simulación para esa solicitud.");
            return;
        }
        System.out.println("Simulación " + simulacion);
        for (EtapaDespliegue etapa : simulacion.getEtapas()) {
            System.out.println("  " + etapa);
        }
    }

    private void consultarHistorial() throws SQLException {
        if (!listarSolicitudes()) {
            return;
        }
        int solicitudId = leerNumero("ID de la solicitud");
        HistorialDespliegue historial = despliegues.buscarHistorial(solicitudId);
        if (historial == null) {
            System.out.println("No hay historial para esa solicitud.");
            return;
        }
        System.out.println("Historial " + historial.getId());
        for (EventoDespliegue evento : historial.getEventos()) {
            System.out.println("  " + evento);
        }
    }

    private void darDeBaja() throws SQLException {
        int tipo = leerNumero("1. Usuario  2. Aplicación");
        boolean resultado;
        switch (tipo) {
            case 1 -> {
                for (Usuario usuario : catalogo.listarUsuarios()) {
                    System.out.println(usuario);
                }
                resultado = catalogo.desactivarUsuario(leerNumero("ID del usuario"));
            }
            case 2 -> {
                listarAplicaciones();
                resultado = catalogo.desactivarAplicacion(leerNumero("ID de la aplicación"));
            }
            default -> throw new IllegalArgumentException("Tipo de registro no válido.");
        }
        System.out.println(resultado ? "Baja registrada." : "No se encontró un registro activo con ese ID.");
    }

    private void listarUsuariosActivos(int tipo) throws SQLException {
        System.out.println(tipo == 1 ? "Desarrolladores activos" : "Responsables de plataforma activos");
        for (Usuario usuario : catalogo.listarUsuarios()) {
            if (usuario.isActivo() && ((tipo == 1 && usuario instanceof Desarrollador)
                    || (tipo == 2 && usuario instanceof ResponsablePlataforma))) {
                System.out.println("  " + usuario);
            }
        }
    }

    private void listarAplicaciones() throws SQLException {
        System.out.println("Aplicaciones");
        for (Aplicacion aplicacion : catalogo.listarAplicaciones()) {
            System.out.println("  " + aplicacion);
        }
    }

    private void listarEntornos() throws SQLException {
        System.out.println("Entornos");
        for (Entorno entorno : catalogo.listarEntornos()) {
            System.out.println("  " + entorno);
        }
    }

    private void listarPlanes() throws SQLException {
        System.out.println("Planes de recursos");
        for (PlanRecursos plan : catalogo.listarPlanes()) {
            System.out.println("  " + plan);
        }
    }

    private boolean listarSolicitudes() throws SQLException {
        List<SolicitudDespliegue> solicitudes = despliegues.listarSolicitudes();
        if (solicitudes.isEmpty()) {
            System.out.println("No hay solicitudes registradas.");
            return false;
        }
        System.out.println("Solicitudes de despliegue");
        for (SolicitudDespliegue solicitud : solicitudes) {
            System.out.println("  " + solicitud);
        }
        return true;
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje + ": ");
        String valor = scanner.nextLine().strip();
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("El dato es obligatorio.");
        }
        return valor;
    }

    private int leerNumero(String mensaje) {
        String valor = leerTexto(mensaje);
        try {
            int numero = Integer.parseInt(valor);
            if (numero >= 0) {
                return numero;
            }
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Escribe un número entero no negativo.");
        }
        throw new IllegalArgumentException("Escribe un número entero no negativo.");
    }

    private void mostrarErrorSQL(SQLException error) {
        String estado = error.getSQLState();
        if ("23505".equals(estado)) {
            System.out.println("Ya existe un registro con ese valor único.");
        } else {
            System.out.println("No se pudo completar la operación en PostgreSQL (SQLSTATE " + estado + ").");
        }
    }
}
