package com.flashcard.repository;

import com.flashcard.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Deck entity operations
 */
@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {

    /**
     * Find deck by name (case-insensitive)
     */
    Optional<Deck> findByNameIgnoreCase(String name);

    /**
     * Find all decks ordered by creation date (newest first)
     */
    List<Deck> findAllByOrderByCreatedAtDesc();

    /**
     * Check if deck exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find decks with at least one card
     */
    @Query("SELECT d FROM Deck d WHERE SIZE(d.cards) > 0")
    List<Deck> findDecksWithCards();
}
