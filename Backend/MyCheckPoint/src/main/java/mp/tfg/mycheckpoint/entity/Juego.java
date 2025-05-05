package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.GameTypeEnum;
import mp.tfg.mycheckpoint.entity.enums.StatusEnum;
import mp.tfg.mycheckpoint.entity.junction.*;
import mp.tfg.mycheckpoint.entity.junction.ListaJuego;
import mp.tfg.mycheckpoint.entity.junction.TierListJuego;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "juego", indexes = {
        @Index(name = "idx_juego_idigdb", columnList = "idigdb"),
        @Index(name = "idx_juego_slug", columnList = "slug"),
        @Index(name = "idx_juego_nombre", columnList = "nombre"),
        @Index(name = "idx_juego_fechalanzamiento", columnList = "fechaLanzamiento"),
        @Index(name = "idx_juego_gametype", columnList = "gameType"),
        @Index(name = "idx_juego_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// ***** CORRECCIÓN: Añadido 'similares' a la exclusión de toString *****
@ToString(exclude = {"generos", "motores", "modosJuego", "companias", "keywords",
        "plataformasIgdb", "temas", "perspectivas", "imagenes",
        "videos", "webs", "idiomasSoporte", "franquicias",
        "relacionesOrigen", "relacionesDestino", // Mantenemos estos para DLCs/Expansiones
        "similares", // Añadido aquí
        "juegosUsuario", "listasJuego", "tierListsJuego",
        "ranking", "duracion"})
@SQLDelete(sql = "UPDATE juego SET fechaEliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "fechaEliminacion IS NULL")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "idigdb", unique = true, nullable = false)
    private Long idigdb; // ID de IGDB

    @Column(name = "nombre", length = 500, nullable = false)
    private String nombre;

    @Column(name = "slug", length = 500, unique = true, nullable = false)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "gameType", nullable = false)
    private GameTypeEnum gameType;

    @Column(name = "resumen", columnDefinition = "TEXT")
    private String resumen;

    @Column(name = "historia", columnDefinition = "TEXT")
    private String historia;

    @Column(name = "totalRating") // REAL CHECK (totalRating >= 0 AND totalRating <= 100)
    private Float totalRating; // Puntuación IGDB 0-100

    @Column(name = "totalRatingCount")
    private Integer totalRatingCount;

    @Column(name = "fechaLanzamiento")
    private LocalDate fechaLanzamiento;

    @Column(name = "coverUrl", columnDefinition = "TEXT")
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEnum status;

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaModificacion;

    @Column(name = "fechaEliminacion", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime fechaEliminacion;


    // --- Relaciones N:M (con JoinTable) ---

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "juegogenero",
            joinColumns = @JoinColumn(name = "juego_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id"))
    private Set<Genero> generos = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "juegomotorgrafico",
            joinColumns = @JoinColumn(name = "juego_id"),
            inverseJoinColumns = @JoinColumn(name = "motorgrafico_id"))
    private Set<MotorGrafico> motores = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "juegocompania",
            joinColumns = @JoinColumn(name = "juego_id"),
            inverseJoinColumns = @JoinColumn(name = "compania_id"))
    private Set<Compania> companias = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "juegokeyword",
            joinColumns = @JoinColumn(name = "juego_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id"))
    private Set<Keyword> keywords = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "juegomodojuego",
            joinColumns = @JoinColumn(name = "juego_id"),
            inverseJoinColumns = @JoinColumn(name = "modojuego_id"))
    private Set<ModoJuego> modosJuego = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "juegoperspectivajugador",
            joinColumns = @JoinColumn(name = "juego_id"),
            inverseJoinColumns = @JoinColumn(name = "perspectivajugador_id"))
    private Set<PerspectivaJugador> perspectivas = new HashSet<>();

    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoPlataformaIGDB> plataformasIgdb = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "juegotema",
            joinColumns = @JoinColumn(name = "juego_id"),
            inverseJoinColumns = @JoinColumn(name = "tema_id"))
    private Set<Tema> temas = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "juegofranquicia",
            joinColumns = @JoinColumn(name = "juego_id"),
            inverseJoinColumns = @JoinColumn(name = "franquicia_id"))
    private Set<Franquicia> franquicias = new HashSet<>();

    // ***** CORRECCIÓN: Añadida relación ManyToMany para juegos similares *****
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "juego_similar", // Nombre de la tabla de unión para juegos similares
            joinColumns = @JoinColumn(name = "juego_id"), // FK a este juego
            inverseJoinColumns = @JoinColumn(name = "similar_juego_id") // FK al juego similar
    )
    private Set<Juego> similares = new HashSet<>();

    // ***** CORRECCIÓN: Relación inversa para juegos similares (necesaria si quieres navegar en ambos sentidos) *****
    @ManyToMany(mappedBy = "similares", fetch = FetchType.LAZY)
    private Set<Juego> similarDe = new HashSet<>();


    // --- Relaciones 1:N (Hacia entidades relacionadas directamente con Juego) ---

    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Artwork> imagenes = new HashSet<>();

    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Video> videos = new HashSet<>();

    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Web> webs = new HashSet<>();

    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoIdiomaSoporte> idiomasSoporte = new HashSet<>();


    // --- Relaciones N:M con la propia entidad Juego (a través de JuegoRelacion para DLC/Expansion) ---
    // Usamos la entidad JuegoRelacion para manejar esto
    // ***** NO TOCAMOS ESTO, sigue siendo válido para DLCs y Expansiones *****
    @OneToMany(mappedBy = "juegoOrigen", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoRelacion> relacionesOrigen = new HashSet<>();

    @OneToMany(mappedBy = "juegoDestino", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoRelacion> relacionesDestino = new HashSet<>();


    // --- Relaciones inversas (desde otras entidades hacia Juego) ---

    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoUsuario> juegosUsuario = new HashSet<>();

    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ListaJuego> listasJuego = new HashSet<>();

    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TierListJuego> tierListsJuego = new HashSet<>();

    @OneToOne(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Ranking ranking;

    @OneToOne(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private DuracionJuego duracion;


    // --- Métodos Helper ---
    // (Se mantienen los helpers existentes para otras relaciones)
    // ... (Helpers para Genero, MotorGrafico, Compania, etc.) ...
    public void addGenero(Genero genero) {
        this.generos.add(genero);
        genero.getJuegos().add(this);
    }
    public void removeGenero(Genero genero) {
        this.generos.remove(genero);
        genero.getJuegos().remove(this);
    }
    public void addMotorGrafico(MotorGrafico motorGrafico) {
        this.motores.add(motorGrafico);
        motorGrafico.getJuegos().add(this);
    }
    public void removeMotorGrafico(MotorGrafico motorGrafico) {
        this.motores.remove(motorGrafico);
        motorGrafico.getJuegos().remove(this);
    }
    public void addCompania(Compania compania) {
        this.companias.add(compania);
        compania.getJuegos().add(this);
    }
    public void removeCompania(Compania compania) {
        this.companias.remove(compania);
        compania.getJuegos().remove(this);
    }
    public void addKeyword(Keyword keyword) {
        this.keywords.add(keyword);
        keyword.getJuegos().add(this);
    }
    public void removeKeyword(Keyword keyword) {
        this.keywords.remove(keyword);
        keyword.getJuegos().remove(this);
    }
    public void addModoJuego(ModoJuego modoJuego) {
        this.modosJuego.add(modoJuego);
        modoJuego.getJuegos().add(this);
    }
    public void removeModoJuego(ModoJuego modoJuego) {
        this.modosJuego.remove(modoJuego);
        modoJuego.getJuegos().remove(this);
    }
    public void addPerspectivaJugador(PerspectivaJugador perspectivaJugador) {
        this.perspectivas.add(perspectivaJugador);
        perspectivaJugador.getJuegos().add(this);
    }
    public void removePerspectivaJugador(PerspectivaJugador perspectivaJugador) {
        this.perspectivas.remove(perspectivaJugador);
        perspectivaJugador.getJuegos().remove(this);
    }
    public void addTema(Tema tema) {
        this.temas.add(tema);
        tema.getJuegos().add(this);
    }
    public void removeTema(Tema tema) {
        this.temas.remove(tema);
        tema.getJuegos().remove(this);
    }
    public void addFranquicia(Franquicia franquicia) {
        this.franquicias.add(franquicia);
        franquicia.getJuegos().add(this);
    }
    public void removeFranquicia(Franquicia franquicia) {
        this.franquicias.remove(franquicia);
        franquicia.getJuegos().remove(this);
    }
    public void addArtwork(Artwork artwork) {
        this.imagenes.add(artwork);
        artwork.setJuego(this);
    }
    public void removeArtwork(Artwork artwork) {
        this.imagenes.remove(artwork);
        artwork.setJuego(null);
    }
    public void addVideo(Video video) {
        this.videos.add(video);
        video.setJuego(this);
    }
    public void removeVideo(Video video) {
        this.videos.remove(video);
        video.setJuego(null);
    }
    public void addWeb(Web web) {
        this.webs.add(web);
        web.setJuego(this);
    }
    public void removeWeb(Web web) {
        this.webs.remove(web);
        web.setJuego(null);
    }
    public void addJuegoIdiomaSoporte(JuegoIdiomaSoporte juegoIdiomaSoporte) {
        this.idiomasSoporte.add(juegoIdiomaSoporte);
        juegoIdiomaSoporte.setJuego(this);
    }
    public void removeJuegoIdiomaSoporte(JuegoIdiomaSoporte juegoIdiomaSoporte) {
        this.idiomasSoporte.remove(juegoIdiomaSoporte);
        juegoIdiomaSoporte.setJuego(null);
    }
    public void addJuegoPlataformaIGDB(JuegoPlataformaIGDB juegoPlataformaIGDB) {
        this.plataformasIgdb.add(juegoPlataformaIGDB);
        juegoPlataformaIGDB.setJuego(this);
    }
    public void removeJuegoPlataformaIGDB(JuegoPlataformaIGDB juegoPlataformaIGDB) {
        this.plataformasIgdb.remove(juegoPlataformaIGDB);
        juegoPlataformaIGDB.setJuego(null);
    }
    public void addRelacionOrigen(JuegoRelacion relacion) {
        this.relacionesOrigen.add(relacion);
        relacion.setJuegoOrigen(this);
    }
    public void removeRelacionOrigen(JuegoRelacion relacion) {
        this.relacionesOrigen.remove(relacion);
        relacion.setJuegoOrigen(null);
    }
    public void addRelacionDestino(JuegoRelacion relacion) {
        this.relacionesDestino.add(relacion);
        relacion.setJuegoDestino(this);
    }
    public void removeRelacionDestino(JuegoRelacion relacion) {
        this.relacionesDestino.remove(relacion);
        relacion.setJuegoDestino(null);
    }
    public void addJuegoUsuario(JuegoUsuario juegoUsuario) {
        this.juegosUsuario.add(juegoUsuario);
        juegoUsuario.setJuego(this);
    }
    public void removeJuegoUsuario(JuegoUsuario juegoUsuario) {
        this.juegosUsuario.remove(juegoUsuario);
        juegoUsuario.setJuego(null);
    }
    public void addListaJuego(ListaJuego listaJuego) {
        this.listasJuego.add(listaJuego);
        listaJuego.setJuego(this);
    }
    public void removeListaJuego(ListaJuego listaJuego) {
        this.listasJuego.remove(listaJuego);
        listaJuego.setJuego(null);
    }
    public void addTierListJuego(TierListJuego tierListJuego) {
        this.tierListsJuego.add(tierListJuego);
        tierListJuego.setJuego(this);
    }
    public void removeTierListJuego(TierListJuego tierListJuego) {
        this.tierListsJuego.remove(tierListJuego);
        tierListJuego.setJuego(null);
    }

    // ***** CORRECCIÓN: Añadidos helpers para la nueva relación 'similares' *****
    public void addSimilar(Juego juegoSimilar) {
        this.similares.add(juegoSimilar);
        juegoSimilar.getSimilarDe().add(this); // Actualiza el lado inverso
    }

    public void removeSimilar(Juego juegoSimilar) {
        this.similares.remove(juegoSimilar);
        juegoSimilar.getSimilarDe().remove(this); // Actualiza el lado inverso
    }

}