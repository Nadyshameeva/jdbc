package org.example.data.repo;

import org.example.data.core.JdbcRepository;
import org.example.model.Visitor;

import java.sql.Connection;
import java.util.UUID;

public class VisitorRepository extends JdbcRepository<Visitor, UUID> {
    public VisitorRepository(Connection connection) {
        super(connection);
    }
}
