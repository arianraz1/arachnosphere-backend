package com.tarantulacommunity.tarantulasocial.features.collection_entry.entity;

import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tarantula_collections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "species_id"}))
public class CollectionEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private SpeciesEntity species;

    @Min(0)
    @Column(nullable = false)
    private int slingMale = 0;
    @Min(0)
    @Column(nullable = false)
    private int slingFemale = 0;
    @Min(0)
    @Column(nullable = false)
    private int slingUnsexed = 0;

    @Min(0)
    @Column(nullable = false)
    private int juvenileMale = 0;
    @Min(0)
    @Column(nullable = false)
    private int juvenileFemale = 0;
    @Min(0)
    @Column(nullable = false)
    private int juvenileUnsexed = 0;

    @Min(0)
    @Column(nullable = false)
    private int subadultMale = 0;
    @Min(0)
    @Column(nullable = false)
    private int subadultFemale = 0;
    @Min(0)
    @Column(nullable = false)
    private int subadultUnsexed = 0;

    @Min(0)
    @Column(nullable = false)
    private int adultMale = 0;
    @Min(0)
    @Column(nullable = false)
    private int adultFemale = 0;
    @Min(0)
    @Column(nullable = false)
    private int adultUnsexed = 0;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionImageEntity> images = new ArrayList<>();

    @Size(max = 1000)
    @Column(length = 1000)
    private String customInfo;

    @Version
    private Long version;
}