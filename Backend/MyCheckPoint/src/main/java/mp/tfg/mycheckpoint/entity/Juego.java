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
@ToString(exclude = {"generos", "motores", "modosJuego", "companias", "keywords", "plataformasIgdb", "temas", "perspectivas", "imagenes", "videos", "webs", "idiomasSoporte", "franquicias", "dlcs", "expansiones", "similares", "juegosUsuario", "listasJuego", "tierListsJuego", "ranking", "duracion"}) // Evitar recursión
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

    // Relación con PlataformaIGDB (no con nuestra entidad Plataforma)
    //@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    //@JoinTable(name = "juegoplataformaigdb",
    //        joinColumns = @JoinColumn(name = "juego_id"),
    //        inverseJoinColumns = @JoinColumn(name = "plataforma_igdb_id"))
    // Podríamos necesitar una entidad intermedia si queremos guardar fechaLanzamiento específica
    // por plataforma IGDB, como está en el SQL. Vamos a crearla abajo.
    // private Set<PlataformaIGDB> plataformasIgdb = new HashSet<>(); //<- Cambio abajo
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

    // --- Relaciones 1:N (Hacia entidades relacionadas directamente con Juego) ---

    // Juego 1 <--> * Artwork
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Artwork> imagenes = new HashSet<>();

    // Juego 1 <--> * Video
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Video> videos = new HashSet<>();

    // Juego 1 <--> * Web
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Web> webs = new HashSet<>();

    // Juego 1 <--> * JuegoIdiomaSoporte (Tabla de unión con atributos extra)
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoIdiomaSoporte> idiomasSoporte = new HashSet<>();


    // --- Relaciones N:M con la propia entidad Juego (a través de JuegoRelacion) ---
    // Usamos la entidad JuegoRelacion para manejar esto

    // Juegos que son DLCs/Expansiones/Similares DE este juego (Este juego es el ORIGEN)
    @OneToMany(mappedBy = "juegoOrigen", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoRelacion> relacionesOrigen = new HashSet<>();

    // Juegos de los cuales ESTE juego es un DLC/Expansion/Similar (Este juego es el DESTINO)
    @OneToMany(mappedBy = "juegoDestino", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoRelacion> relacionesDestino = new HashSet<>();


    // --- Relaciones inversas (desde otras entidades hacia Juego) ---

    // Juego 1 <--> * JuegoUsuario
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoUsuario> juegosUsuario = new HashSet<>();

    // Juego 1 <--> * ListaJuego (Tabla de unión)
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ListaJuego> listasJuego = new HashSet<>();

    // Juego 1 <--> * TierListJuego (Tabla de unión)
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TierListJuego> tierListsJuego = new HashSet<>();

    // Juego 1 <--> 1 Ranking (Ranking tiene la FK)
    @OneToOne(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Ranking ranking;

    // Juego 1 <--> 1 DuracionJuego (DuracionJuego tiene la FK)
    @OneToOne(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private DuracionJuego duracion;


    // --- Métodos Helper  ---
    // Helpers para ManyToMany (implica actualizar ambos lados)
    public void addGenero(Genero genero) {
        this.generos.add(genero);
        genero.getJuegos().add(this); // Asumiendo que Genero tiene Set<Juego> juegos con mappedBy="generos"
    }

    public void removeGenero(Genero genero) {
        this.generos.remove(genero);
        genero.getJuegos().remove(this); // Asumiendo que Genero tiene Set<Juego> juegos con mappedBy="generos"
    }

    public void addMotorGrafico(MotorGrafico motorGrafico) {
        this.motores.add(motorGrafico);
        motorGrafico.getJuegos().add(this); // Asumiendo MotorGrafico tiene Set<Juego> juegos con mappedBy="motores"
    }

    public void removeMotorGrafico(MotorGrafico motorGrafico) {
        this.motores.remove(motorGrafico);
        motorGrafico.getJuegos().remove(this);
    }

    public void addCompania(Compania compania) {
        this.companias.add(compania);
        compania.getJuegos().add(this); // Asumiendo Compania tiene Set<Juego> juegos con mappedBy="companias"
    }

    public void removeCompania(Compania compania) {
        this.companias.remove(compania);
        compania.getJuegos().remove(this);
    }

    public void addKeyword(Keyword keyword) {
        this.keywords.add(keyword);
        keyword.getJuegos().add(this); // Asumiendo Keyword tiene Set<Juego> juegos con mappedBy="keywords"
    }

    public void removeKeyword(Keyword keyword) {
        this.keywords.remove(keyword);
        keyword.getJuegos().remove(this);
    }

    public void addModoJuego(ModoJuego modoJuego) {
        this.modosJuego.add(modoJuego);
        modoJuego.getJuegos().add(this); // Asumiendo ModoJuego tiene Set<Juego> juegos con mappedBy="modosJuego"
    }

    public void removeModoJuego(ModoJuego modoJuego) {
        this.modosJuego.remove(modoJuego);
        modoJuego.getJuegos().remove(this);
    }

    public void addPerspectivaJugador(PerspectivaJugador perspectivaJugador) {
        this.perspectivas.add(perspectivaJugador);
        perspectivaJugador.getJuegos().add(this); // Asumiendo PerspectivaJugador tiene Set<Juego> juegos con mappedBy="perspectivas"
    }

    public void removePerspectivaJugador(PerspectivaJugador perspectivaJugador) {
        this.perspectivas.remove(perspectivaJugador);
        perspectivaJugador.getJuegos().remove(this);
    }

    public void addTema(Tema tema) {
        this.temas.add(tema);
        tema.getJuegos().add(this); // Asumiendo Tema tiene Set<Juego> juegos con mappedBy="temas"
    }

    public void removeTema(Tema tema) {
        this.temas.remove(tema);
        tema.getJuegos().remove(this);
    }

    public void addFranquicia(Franquicia franquicia) {
        this.franquicias.add(franquicia);
        franquicia.getJuegos().add(this); // Asumiendo Franquicia tiene Set<Juego> juegos con mappedBy="franquicias"
    }

    public void removeFranquicia(Franquicia franquicia) {
        this.franquicias.remove(franquicia);
        franquicia.getJuegos().remove(this);
    }

    // Helpers para OneToMany directas (Artwork, Video, Web)
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

    // Helpers para OneToMany hacia entidades de unión con atributos (JuegoIdiomaSoporte, JuegoPlataformaIGDB)
    public void addJuegoIdiomaSoporte(JuegoIdiomaSoporte juegoIdiomaSoporte) {
        this.idiomasSoporte.add(juegoIdiomaSoporte);
        juegoIdiomaSoporte.setJuego(this);
        // Al crear el objeto de unión, su ID compuesto debe estar completo
        // juegoIdiomaSoporte.setId(new JuegoIdiomaSoporteId(this.id, juegoIdiomaSoporte.getIdioma().getId(), juegoIdiomaSoporte.getTipoSoporte()));
    }

    public void removeJuegoIdiomaSoporte(JuegoIdiomaSoporte juegoIdiomaSoporte) {
        this.idiomasSoporte.remove(juegoIdiomaSoporte);
        juegoIdiomaSoporte.setJuego(null);
    }

    public void addJuegoPlataformaIGDB(JuegoPlataformaIGDB juegoPlataformaIGDB) {
        this.plataformasIgdb.add(juegoPlataformaIGDB);
        juegoPlataformaIGDB.setJuego(this);
        // Al crear el objeto de unión, su ID compuesto debe estar completo
        // juegoPlataformaIGDB.setId(new JuegoPlataformaIGDBId(this.id, juegoPlataformaIGDB.getPlataformaIgdb().getId()));
    }

    public void removeJuegoPlataformaIGDB(JuegoPlataformaIGDB juegoPlataformaIGDB) {
        this.plataformasIgdb.remove(juegoPlataformaIGDB);
        juegoPlataformaIGDB.setJuego(null);
    }


    // Helpers para relaciones autoreferenciales (JuegoRelacion)
    public void addRelacionOrigen(JuegoRelacion relacion) {
        this.relacionesOrigen.add(relacion);
        relacion.setJuegoOrigen(this);
        // Es importante que al crear la relación, el ID compuesto y el juego destino estén completos
        // relacion.setId(new JuegoRelacionId(this.id, relacion.getJuegoDestino().getId(), relacion.getId().getTipoRelacion()));
        // relacion.setJuegoDestino(...)
    }

    public void removeRelacionOrigen(JuegoRelacion relacion) {
        this.relacionesOrigen.remove(relacion);
        relacion.setJuegoOrigen(null);
    }

    // Helpers para relaciones autoreferenciales (lado inverso)
    public void addRelacionDestino(JuegoRelacion relacion) {
        this.relacionesDestino.add(relacion);
        relacion.setJuegoDestino(this);
        // Es importante que al crear la relación, el ID compuesto y el juego origen estén completos
        // relacion.setId(new JuegoRelacionId(relacion.getJuegoOrigen().getId(), this.id, relacion.getId().getTipoRelacion()));
        // relacion.setJuegoOrigen(...)
    }

    public void removeRelacionDestino(JuegoRelacion relacion) {
        this.relacionesDestino.remove(relacion);
        relacion.setJuegoDestino(null);
    }


    // Helpers para OneToMany inversas (desde Juego hacia tablas de unión con usuarios)
    public void addJuegoUsuario(JuegoUsuario juegoUsuario) {
        this.juegosUsuario.add(juegoUsuario);
        juegoUsuario.setJuego(this);
        // Es importante que al crear JuegoUsuario, el usuario también esté establecido
        // juegoUsuario.setUsuario(...)
    }

    public void removeJuegoUsuario(JuegoUsuario juegoUsuario) {
        this.juegosUsuario.remove(juegoUsuario);
        juegoUsuario.setJuego(null);
    }

    public void addListaJuego(ListaJuego listaJuego) {
        this.listasJuego.add(listaJuego);
        listaJuego.setJuego(this);
        // Es importante que al crear ListaJuego, la lista y el ID compuesto estén completos
        // listaJuego.setId(new ListaJuegoId(listaJuego.getLista().getId(), this.id));
        // listaJuego.setLista(...)
    }

    public void removeListaJuego(ListaJuego listaJuego) {
        this.listasJuego.remove(listaJuego);
        listaJuego.setJuego(null);
    }

    public void addTierListJuego(TierListJuego tierListJuego) {
        this.tierListsJuego.add(tierListJuego);
        tierListJuego.setJuego(this);
        // Es importante que al crear TierListJuego, la tierlist, el nivel y el ID compuesto estén completos
        // tierListJuego.setId(new TierListJuegoId(tierListJuego.getTierList().getId(), this.id));
        // tierListJuego.setTierList(...)
        // tierListJuego.setNivelTier(...)
    }

    public void removeTierListJuego(TierListJuego tierListJuego) {
        this.tierListsJuego.remove(tierListJuego);
        tierListJuego.setJuego(null);
    }

    // No solemos crear helpers para relaciones OneToOne mappedBy (Ranking, DuracionJuego)
    // ya que la relación se establece en el lado dueño (donde está la FK).
    // Ranking.setJuego(juego) o DuracionJuego.setJuego(juego) es lo común.
}