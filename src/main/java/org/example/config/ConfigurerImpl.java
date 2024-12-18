package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurerImpl implements Configurer {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String schema;

    private static Configurer instance;

    private ConfigurerImpl() throws IOException {
        Properties props = new Properties();
        InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("appdata/application.properties");
        props.load(input);

        host = props.getProperty("database.host");
        port = Integer.parseInt(props.getProperty("database.port"));
        database = props.getProperty("database.name");
        username = props.getProperty("database.user");
        schema = props.getProperty("database.schema");
        password = props.getProperty("database.password");
    }

    @Override
    public String getUser() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDatabaseName() {
        return database;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    public static Configurer getInstance() throws IOException {
        if (instance == null)
            instance = new ConfigurerImpl();

        return instance;
    }
}
