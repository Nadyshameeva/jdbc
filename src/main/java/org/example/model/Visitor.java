package org.example.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.data.core.annotation.Column;
import org.example.data.core.annotation.Id;
import org.example.data.core.annotation.IgnoreColumn;
import org.example.data.core.annotation.Model;

import java.util.List;
import java.util.UUID;

@Data
@Model("visitors")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Visitor {
    @Id
    @Expose(deserialize = false)
    private UUID id;

    private String name;

    private String surname;

    @Column("is_subscribed")
    @SerializedName("subscribed")
    private boolean isSubscribed;

    @IgnoreColumn
    private List<Book> favoriteBooks;
}
