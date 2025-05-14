package mp.tfg.mycheckpoint.entity.games;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.GameType;
import mp.tfg.mycheckpoint.dto.enums.ReleaseStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    @Column(unique = true, nullable = false)
    private Long igdbId;

    private String name;
    private String slug;

    @Embedded
    private Cover cover;

    @Column(name = "total_rating")
    private Double totalRating;
    @Column(name = "total_rating_count")
    private Integer totalRatingCount;
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    @Column(name = "storyline", columnDefinition = "TEXT")
    private String storyline;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "game_type")
    private GameType gameType;

    @Column(name = "first_release_date")
    private Instant firstReleaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "first_release_status")
    private ReleaseStatus firstReleaseStatus;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "parent_game_internal_id")
    private Game parentGame;

    @OneToMany(mappedBy = "parentGame", fetch = FetchType.LAZY)
    private Set<Game> childGames = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "version_parent_game_internal_id")
    private Game versionParentGame;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_remake_versions_assoc", joinColumns = @JoinColumn(name = "original_game_id"), inverseJoinColumns = @JoinColumn(name = "remake_game_id"))
    private Set<Game> remakeVersions = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_remaster_versions_assoc", joinColumns = @JoinColumn(name = "original_game_id"), inverseJoinColumns = @JoinColumn(name = "remaster_game_id"))
    private Set<Game> remasterVersions = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "game_artworks", joinColumns = @JoinColumn(name = "game_internal_id"))
    private List<Artwork> artworks = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "game_screenshots", joinColumns = @JoinColumn(name = "game_internal_id"))
    private List<Screenshot> screenshots = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "game_websites", joinColumns = @JoinColumn(name = "game_internal_id"))
    private List<Website> websites = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "game_videos", joinColumns = @JoinColumn(name = "game_internal_id"))
    private List<Video> videos = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_game_modes", joinColumns = @JoinColumn(name = "game_internal_id"), inverseJoinColumns = @JoinColumn(name = "game_mode_internal_id"))
    private Set<GameMode> gameModes = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_genres", joinColumns = @JoinColumn(name = "game_internal_id"), inverseJoinColumns = @JoinColumn(name = "genre_internal_id"))
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_franchises", joinColumns = @JoinColumn(name = "game_internal_id"), inverseJoinColumns = @JoinColumn(name = "franchise_internal_id"))
    private Set<Franchise> franchises = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_game_engines_assoc", joinColumns = @JoinColumn(name = "game_internal_id"), inverseJoinColumns = @JoinColumn(name = "game_engine_internal_id"))
    private Set<GameEngine> gameEngines = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_keywords_assoc", joinColumns = @JoinColumn(name = "game_internal_id"), inverseJoinColumns = @JoinColumn(name = "keyword_internal_id"))
    private Set<Keyword> keywords = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_platforms_assoc", joinColumns = @JoinColumn(name = "game_internal_id"), inverseJoinColumns = @JoinColumn(name = "platform_internal_id"))
    private Set<Platform> platforms = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_themes_assoc", joinColumns = @JoinColumn(name = "game_internal_id"), inverseJoinColumns = @JoinColumn(name = "theme_internal_id"))
    private Set<Theme> themes = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "game_similar_games_assoc", joinColumns = @JoinColumn(name = "game_id"), inverseJoinColumns = @JoinColumn(name = "similar_game_id"))
    private Set<Game> similarGames = new HashSet<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<GameCompanyInvolvement> involvedCompanies = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(igdbId, game.igdbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId);
    }
}