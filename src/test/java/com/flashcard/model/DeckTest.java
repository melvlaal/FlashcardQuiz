package com.flashcard.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void defaultConstructor_ShouldSetCreatedAtAndEmptyCards() {
        // When
        Deck deck = new Deck();

        // Then
        assertNotNull(deck.getCreatedAt());
        assertTrue(deck.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(deck.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
        assertNotNull(deck.getCards());
        assertTrue(deck.getCards().isEmpty());
    }

    @Test
    void oneParameterConstructor_ShouldSetNameAndDefaults() {
        // Given
        String deckName = "Test Deck";

        // When
        Deck deck = new Deck(deckName);

        // Then
        assertEquals(deckName, deck.getName());
        assertNotNull(deck.getCreatedAt());
        assertNotNull(deck.getCards());
        assertTrue(deck.getCards().isEmpty());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        Deck deck = new Deck();
        Long id = 1L;
        String name = "Test Deck";
        LocalDateTime now = LocalDateTime.now();
        List<Card> cards = new ArrayList<>();

        // When
        deck.setId(id);
        deck.setName(name);
        deck.setCreatedAt(now);
        deck.setCards(cards);

        // Then
        assertEquals(id, deck.getId());
        assertEquals(name, deck.getName());
        assertEquals(now, deck.getCreatedAt());
        assertEquals(cards, deck.getCards());
    }

    @Test
    void getCardCount_WithEmptyCards_ShouldReturnZero() {
        // Given
        Deck deck = new Deck("Test Deck");

        // When
        int count = deck.getCardCount();

        // Then
        assertEquals(0, count);
    }

    @Test
    void getCardCount_WithCards_ShouldReturnCorrectCount() {
        // Given
        Deck deck = new Deck("Test Deck");
        List<Card> cards = List.of(
                new Card("Q1", "A1", deck),
                new Card("Q2", "A2", deck),
                new Card("Q3", "A3", deck)
        );
        deck.setCards(cards);

        // When
        int count = deck.getCardCount();

        // Then
        assertEquals(3, count);
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        // Given
        Deck deck = new Deck("Java Programming");
        deck.setId(1L);
        List<Card> cards = List.of(
                new Card("Q1", "A1", deck),
                new Card("Q2", "A2", deck)
        );
        deck.setCards(cards);

        // When
        String result = deck.toString();

        // Then
        assertEquals("Deck{id=1, name='Java Programming', cardCount=2}", result);
    }

    @Test
    void toString_WithNullId_ShouldHandleGracefully() {
        // Given
        Deck deck = new Deck("Test Deck");

        // When
        String result = deck.toString();

        // Then
        assertEquals("Deck{id=null, name='Test Deck', cardCount=0}", result);
    }
}
