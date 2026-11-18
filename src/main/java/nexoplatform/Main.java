package nexoplatform;

import nexoplatform.config.ConexionBD;
import nexoplatform.repository.CatalogoRepository;
import nexoplatform.repository.DespliegueRepository;
import nexoplatform.services.GestionPlataformaService;
import nexoplatform.services.GestionPlataformaServiceImpl;
import nexoplatform.services.DespliegueService;
import nexoplatform.services.DespliegueServiceImpl;
import nexoplatform.userinterface.MenuApp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if (!comprobarConexion()) {
            return;
        }

        CatalogoRepository catalogo = new CatalogoRepository();
        GestionPlataformaService catalogoService = new GestionPlataformaServiceImpl(catalogo);
        DespliegueService despliegueService = new DespliegueServiceImpl(catalogo, new DespliegueRepository());

        try (Scanner scanner = new Scanner(System.in)) {
            MenuApp menu = new MenuApp(scanner, catalogoService, despliegueService);
            menu.mostrarMenu();
        }
    }

    private static boolean comprobarConexion() {
        try (Connection conexion = ConexionBD.conectar()) {
            return true;
        } catch (SQLException error) {
            String clave = System.getenv("DB_PASSWORD");
            if (clave == null || clave.isEmpty()) {
                System.out.println("Define DB_PASSWORD antes de iniciar el programa.");
            } else {
                System.out.println("No se pudo conectar a nexoplatform_v2 (SQLSTATE " + error.getSQLState() + ").");
            }
            return false;
        }
    }
}
