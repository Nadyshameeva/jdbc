package org.example.data.repo;

import org.example.data.core.JdbcRepository;
import org.example.model.Book;

import java.sql.Connection;
import java.util.UUID;

public class BookRepository extends JdbcRepository<Book, UUID> {
    public BookRepository(Connection connection) {
        super(connection);
    }
}
