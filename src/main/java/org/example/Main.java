package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.data.repo.BookRepository;
import org.example.data.repo.MusicRepository;
import org.example.config.Configurer;
import org.example.config.ConfigurerImpl;
import org.example.data.repo.VisitorRepository;
import org.example.data.source.SimpleDataSource;
import org.example.model.Book;
import org.example.model.Music;
import org.example.model.Visitor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            Configurer configurer = ConfigurerImpl.getInstance();
            SimpleDataSource dataSource = SimpleDataSource.getInstance(configurer);
            dataSource.dropSchema();
            dataSource.initSchema();

            MusicRepository musicRepository = new MusicRepository(dataSource.getConnection());
            VisitorRepository visitorRepository = new VisitorRepository(dataSource.getConnection());
            BookRepository bookRepository = new BookRepository(dataSource.getConnection());

            System.out.println("\n--- Task 1 ---");
            musicRepository.findAll().forEach(System.out::println);

            System.out.println("\n--- Task 2 ---");
            musicRepository.findAllByTitleMatchingRegex("[mt]").forEach(System.out::println);

            System.out.println("\n--- Task 3 ---");
            int insertedCount = musicRepository.save(new Music(777, "Never gonna give you up"));
            System.out.println((insertedCount == 1) ? "Music added" : "Music not added");

            System.out.println("\n--- Task 4 ---");
            Gson gson = new Gson();
            TypeToken<List<Visitor>> visitorTypeToken = new TypeToken<>() {
            };

            InputStreamReader inputStreamReader =
                    new InputStreamReader(Main.class.getResourceAsStream("books.json"), StandardCharsets.UTF_8);
            List<Visitor> visitors = gson.fromJson(inputStreamReader, visitorTypeToken);
            System.out.println("Visitors list:");
            visitors.forEach(System.out::println);

            visitors.forEach(visitor -> {
                try {
                    visitor.setId(UUID.randomUUID());
                    visitorRepository.save(visitor);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            Set<Book> books = visitors.stream()
                    .flatMap(visitor -> visitor.getFavoriteBooks().stream())
                    .collect(Collectors.toSet());

            books.forEach(book -> {
                book.setId(UUID.randomUUID());
                try {
                    bookRepository.save(book);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}