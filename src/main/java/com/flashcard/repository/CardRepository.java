package com.flashcard.repository;

import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Card entity operations
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Find all cards in a specific deck
     */
    List<Card> findByDeckOrderByCreatedAtDesc(Deck deck);

    /**
     * Count cards in a specific deck
     */
    long countByDeck(Deck deck);

    /**
     * Find cards in deck that contain keyword in question or answer
     */
    @Query("SELECT c FROM Card c WHERE c.deck = :deck AND " +
            "(LOWER(c.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.answer) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Card> findByDeckAndKeyword(@Param("deck") Deck deck, @Param("keyword") String keyword);

    /**
     * Find cards that have never been reviewed
     */
    @Query("SELECT c FROM Card c WHERE c.deck = :deck AND c.lastReviewed IS NULL")
    List<Card> findUnreviewedCardsByDeck(@Param("deck") Deck deck);

    /**
     * Find cards with low accuracy rate (less than specified threshold)
     */
    @Query("SELECT c FROM Card c WHERE c.deck = :deck AND " +
            "(c.correctCount + c.incorrectCount) > 0 AND " +
            "(CAST(c.correctCount AS double) / (c.correctCount + c.incorrectCount)) < :threshold")
    List<Card> findCardsWithLowAccuracy(@Param("deck") Deck deck, @Param("threshold") double threshold);
}