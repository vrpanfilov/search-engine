package main.model;

import com.sun.istack.NotNull;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "field")
@Data
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column(nullable = false)
    String name;
    @Column(nullable = false)
    String selector;
    @NotNull
    float weight;

    public Field() {

    }
}
