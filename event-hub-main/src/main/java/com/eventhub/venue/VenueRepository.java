package com.eventhub.venue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID>
{
    List<Venue> findAllByActiveTrueOrderByCreatedAtDesc();

    Optional<Venue> findByIdAndActiveTrue(UUID id);

    boolean existsByNameIgnoreCaseAndCityIgnoreCaseAndActiveTrue(String name, String city);

    boolean existsByNameIgnoreCaseAndCityIgnoreCaseAndActiveTrueAndIdNot(String name, String city, UUID id);
}
