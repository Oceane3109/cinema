package com.cinema.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "films")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(length = 1000)
    private String description;

    private Integer dureeMinutes;

    private LocalDate dateSortie;

    private String genre;

    private String realisateur;

    @Column(length = 1000)
    private String acteurs;

    private Double note;

    private String langue;

    private String pays;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
