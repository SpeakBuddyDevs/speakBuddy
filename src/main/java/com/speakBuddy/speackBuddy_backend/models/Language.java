package com.speakBuddy.speackBuddy_backend.models;




import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "languages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String isoCode; // es, en, fr, etc. -> iconos de banderas en flutter
}
