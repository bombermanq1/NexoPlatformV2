package nexoplatform.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    public static Connection conectar() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/nexoplatform_v2";
        String clave = System.getenv("DB_PASSWORD");

        if (clave == null || clave.isEmpty()) {
            throw new SQLException("Define DB_PASSWORD antes de iniciar el programa.", "08001");
        }

        return DriverManager.getConnection(url, "postgres", clave);
    }
}
