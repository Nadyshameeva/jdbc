package org.example.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.data.core.annotation.Column;
import org.example.data.core.annotation.Id;
import org.example.data.core.annotation.Model;

import java.util.UUID;

@Data
@Model("books")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {
    @Id
    @Expose(deserialize = false)
    private UUID id;

    @SerializedName("name")
    private String title;

    private String author;

    @Column("publication_year")
    @SerializedName("publishingYear")
    private int publicationYear;

    private String isbn;

    private String publisher;
}
