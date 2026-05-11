package co.empresa.vivaeventos.events.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Column(name = "venue_id")
    private UUID venueId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "event_end_date")
    private LocalDateTime eventEndDate;

    @Column(name = "event_date_time", nullable = false)
    private LocalDateTime eventDateTime;

    @Column(length = 500)
    private String location;

    @Column(length = 100)
    private String city;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(length = 50)
    private String status = "DRAFT";

    @Column(nullable = false)
    private String category;

    @Column(name = "age_restriction")
    private Integer ageRestriction = 0;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "venue_name", length = 255)
    private String venueName;

    @Column(length = 255)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "maps_embed_url", columnDefinition = "TEXT")
    private String mapsEmbedUrl;

    @Column(name = "maps_link_url", columnDefinition = "TEXT")
    private String mapsLinkUrl;

    @Column(name = "artist_name", length = 255)
    private String artistName;

    @Column(name = "spotify_url", columnDefinition = "TEXT")
    private String spotifyUrl;

    @Column(name = "instagram_url", columnDefinition = "TEXT")
    private String instagramUrl;

    @Column(name = "twitter_url", columnDefinition = "TEXT")
    private String twitterUrl;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (eventDate == null && eventDateTime != null) {
            eventDate = eventDateTime;
        }
        if (status == null) {
            status = "DRAFT";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (eventDateTime != null && eventDate == null) {
            eventDate = eventDateTime;
        }
    }
}