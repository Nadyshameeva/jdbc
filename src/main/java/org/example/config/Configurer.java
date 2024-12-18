package org.example.config;

public interface Configurer {
    String getUser();
    String getPassword();
    String getDatabaseName();
    String getSchema();
    String getHost();
    int getPort();
}
