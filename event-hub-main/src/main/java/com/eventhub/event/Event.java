package com.eventhub.event;

import com.eventhub.user.User;
import com.eventhub.venue.Venue;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events")
public class Event
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean published;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
}
