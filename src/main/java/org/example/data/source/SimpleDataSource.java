package org.example.data.source;

import lombok.Getter;
import lombok.Setter;
import org.example.Main;
import org.example.config.Configurer;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Getter
@Setter
public class SimpleDataSource {
    private Connection connection;
    private Configurer configurer;

    private static SimpleDataSource instance;

    private SimpleDataSource(Configurer configurer) throws SQLException {
        PGSimpleDataSource ds = new PGSimpleDataSource();

        ds.setDatabaseName(configurer.getDatabaseName());
        ds.setUser(configurer.getUser());
        ds.setPassword(configurer.getPassword());
        ds.setServerNames(new String[]{configurer.getHost()});
        ds.setPortNumbers(new int[]{configurer.getPort()});
        ds.setCurrentSchema(configurer.getSchema());

        connection = ds.getConnection();
    }

    public void initSchema() throws URISyntaxException, IOException {
        String sql = Files.readString(Paths
                .get(Objects
                        .requireNonNull(Main.class
                                .getResource("../../db/migration/schema_init.sql")).toURI()));

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void dropSchema() throws URISyntaxException, IOException {
        String sql = Files.readString(Paths
                .get(Objects
                        .requireNonNull(Main.class
                                .getResource("../../db/migration/schema_drop.sql")).toURI()));

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static SimpleDataSource getInstance(Configurer configurer) throws SQLException {
        if (instance == null)
            instance = new SimpleDataSource(configurer);

        return instance;
    }
}
