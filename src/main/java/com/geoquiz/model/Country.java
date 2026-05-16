package com.geoquiz.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "countries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "iso_alpha_2", length = 10)
    private String isoAlpha2;

    @Column(name = "iso_alpha_3", length = 10)
    private String isoAlpha3;

    private Double latitude;
    private Double longitude;
    
    private String continent;

    @Column(name = "geometry_json", columnDefinition = "TEXT")
    private String geometryJson;
}
