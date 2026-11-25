package com.eventhub.venue;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "venues")
public class Venue
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 60)
    private String city;

    @Column(nullable = false, length = 150)
    private String address;

    @Column(nullable = false)
    private int capacity;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
