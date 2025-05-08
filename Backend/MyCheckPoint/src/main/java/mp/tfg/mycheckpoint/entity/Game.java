package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString; // Para evitar problemas con relaciones circulares
import mp.tfg.mycheckpoint.dto.enums.GameTypeEnum;
import mp.tfg.mycheckpoint.dto.enums.StatusEnum;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "juego") // Nombre tabla BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_igdb", unique = true, nullable = false)
    private Long idigdb;

    @Column(name = "nombre", nullable = false, length = 500)
    private String nombre;

    @Column(name = "slug", unique = true, nullable = false, length = 500)
    private String slug;

    @Enumerated(EnumType.STRING) // Asumiendo que 'game_type' es VARCHAR en BBDD
    @Column(name = "game_type", nullable = false, length = 50)
    private GameTypeEnum gameType;

    @Column(name = "resumen", columnDefinition = "TEXT")
    private String resumen;

    @Column(name = "historia", columnDefinition = "TEXT")
    private String historia;

    @Column(name = "total_rating")
    private Float totalRating;

    @Column(name = "total_rating_count")
    private Integer totalRatingCount;

    @Column(name = "fecha_lanzamiento")
    private LocalDate fechaLanzamiento; // Tipo Date de SQL

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(name = "motor_grafico", columnDefinition = "TEXT")
    private String motorGrafico;

    @Column(name = "franquicia", columnDefinition = "TEXT")
    private String franquicia;

    @Enumerated(EnumType.STRING) // Asumiendo que 'status' es VARCHAR en BBDD
    @Column(name = "status", nullable = false, length = 50)
    private StatusEnum status;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    @UpdateTimestamp
    private OffsetDateTime fechaModificacion;

    @Column(name = "fecha_eliminacion")
    private OffsetDateTime fechaEliminacion;

    // --- RELACIONES ManyToMany ---

    @ManyToMany(fetch = FetchType.LAZY) // LAZY para no cargar todo siempre
    @JoinTable(
            name = "juego_genero", // Tabla de unión
            joinColumns = @JoinColumn(name = "juego_id"), // FK en tabla unión a esta entidad (Juego)
            inverseJoinColumns = @JoinColumn(name = "genero_id") // FK en tabla unión a la otra entidad (Genero)
    )
    @ToString.Exclude // Excluir de toString para evitar recursión infinita
    @EqualsAndHashCode.Exclude // Excluir de equals/hashCode si usas @Data y quieres evitar problemas con colecciones lazy
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "juego_plataforma",
            joinColumns = @JoinColumn(name = "juego_id"),
            inverseJoinColumns = @JoinColumn(name = "plataforma_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Platform> platforms = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable( name = "juego_compania", joinColumns = @JoinColumn(name = "juego_id"), inverseJoinColumns = @JoinColumn(name = "compania_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Company> companies = new HashSet<>(); // Nombre campo Java: companies

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable( name = "juego_keyword", joinColumns = @JoinColumn(name = "juego_id"), inverseJoinColumns = @JoinColumn(name = "keyword_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Keyword> keywords = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable( name = "juego_modo_juego", joinColumns = @JoinColumn(name = "juego_id"), inverseJoinColumns = @JoinColumn(name = "modo_juego_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<GameMode> gameModes = new HashSet<>(); // Nombre campo Java: gameModes

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable( name = "juego_tema", joinColumns = @JoinColumn(name = "juego_id"), inverseJoinColumns = @JoinColumn(name = "tema_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Theme> themes = new HashSet<>();

    // --- RELACIÓN OneToMany para Idiomas con tipo de soporte ---
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<GameLanguageSupport> languageSupports = new HashSet<>(); // Nombre campo Java

    // --- RELACIONES OneToMany para Artwork, Video, Web ---
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Artwork> artworks = new HashSet<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Video> videos = new HashSet<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Web> webs = new HashSet<>();


}
