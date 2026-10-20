package co.edu.autonoma.mesa_ayuda_api.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import co.edu.autonoma.mesa_ayuda_api.dto.CheckResponse;

@Service
public class CheckService {

    private final DataSource dataSource;

    public CheckService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CheckResponse verificarBaseDatos() {
        try (Connection conexion = dataSource.getConnection()) {
            DatabaseMetaData metadatos = conexion.getMetaData();
            return new CheckResponse(
                    "conectada",
                    metadatos.getDatabaseProductName() + " " + metadatos.getDatabaseProductVersion(),
                    conexion.getCatalog(),
                    null);
        } catch (SQLException e) {
            return new CheckResponse(
                    "sin conexion",
                    null,
                    null,
                    e.getMessage());
        }
    }

}
