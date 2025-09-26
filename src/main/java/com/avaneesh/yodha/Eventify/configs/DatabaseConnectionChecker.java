package com.avaneesh.yodha.Eventify.configs; // Make sure this package name matches yours

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {

    private final DataSourceProperties dataSourceProperties;

    // Spring will automatically provide the DataSourceProperties bean
    public DatabaseConnectionChecker(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println();
        System.out.println("=================================================");
        System.out.println("   VERIFYING DATASOURCE CONFIGURATION AT STARTUP   ");
        System.out.println("=================================================");
        System.out.println("URL:      " + dataSourceProperties.getUrl());
        System.out.println("Username: " + dataSourceProperties.getUsername());

        // For security, we never print the actual password.
        // We just check if it was loaded.
        String password = dataSourceProperties.getPassword();
        System.out.println("Password Loaded: " + (password != null && !password.isEmpty()));
        System.out.println("=================================================");
        System.out.println();
    }
}