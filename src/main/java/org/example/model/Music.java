package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.data.core.annotation.Column;
import org.example.data.core.annotation.Id;
import org.example.data.core.annotation.Model;

@Data
@Model
@AllArgsConstructor
@NoArgsConstructor
public class Music {
    @Id
    private int id;

    @Column("name")
    private String title;
}
