package com.flashcard.model.dto;

import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckExportDataTest {

    private Deck testDeck;
    private Card testCard1;
    private Card testCard2;

    @BeforeEach
    void setUp() {
        testDeck = new Deck("Test Deck");
        testDeck.setId(1L);

        testCard1 = new Card("What is Java?", "A programming language", testDeck);
        testCard1.setId(1L);

        testCard2 = new Card("What is Spring?", "A Java framework", testDeck);
        testCard2.setId(2L);
    }

    @Test
    void constructor_ShouldSetFields() {
        // Given
        String name = "Test Deck";
        List<CardExportData> cards = List.of(
                new CardExportData("Q1", "A1"),
                new CardExportData("Q2", "A2")
        );

        // When
        DeckExportData exportData = new DeckExportData(name, cards);

        // Then
        assertEquals(name, exportData.getName());
        assertEquals(cards, exportData.getCards());
        assertEquals(2, exportData.getCards().size());
    }

    @Test
    void fromCardEntities_ShouldConvertCardsToDeckExportData() {
        // Given
        String deckName = "Java Programming";
        List<Card> cardEntities = List.of(testCard1, testCard2);

        // When
        DeckExportData exportData = DeckExportData.fromCardEntities(deckName, cardEntities);

        // Then
        assertEquals(deckName, exportData.getName());
        assertEquals(2, exportData.getCards().size());

        CardExportData firstCard = exportData.getCards().get(0);
        assertEquals("What is Java?", firstCard.getQuestion());
        assertEquals("A programming language", firstCard.getAnswer());

        CardExportData secondCard = exportData.getCards().get(1);
        assertEquals("What is Spring?", secondCard.getQuestion());
        assertEquals("A Java framework", secondCard.getAnswer());
    }

    @Test
    void fromCardEntities_WithEmptyList_ShouldCreateEmptyDeck() {
        // Given
        String deckName = "Empty Deck";
        List<Card> cardEntities = List.of();

        // When
        DeckExportData exportData = DeckExportData.fromCardEntities(deckName, cardEntities);

        // Then
        assertEquals(deckName, exportData.getName());
        assertTrue(exportData.getCards().isEmpty());
    }

    @Test
    void fromCardEntities_WithNullList_ShouldThrowException() {
        // Given
        String deckName = "Test Deck";

        // When & Then
        assertThrows(NullPointerException.class, () ->
                DeckExportData.fromCardEntities(deckName, null));
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        // Given
        String name = "Sample Deck";
        List<CardExportData> cards = List.of(new CardExportData("Q", "A"));
        DeckExportData exportData = new DeckExportData(name, cards);

        // When & Then
        assertEquals(name, exportData.getName());
        assertEquals(cards, exportData.getCards());
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        // Given
        String name = "Programming Basics";
        List<CardExportData> cards = List.of(
                new CardExportData("Q1", "A1"),
                new CardExportData("Q2", "A2"),
                new CardExportData("Q3", "A3")
        );
        DeckExportData exportData = new DeckExportData(name, cards);

        // When
        String result = exportData.toString();

        // Then
        assertEquals("DeckExportData{name='Programming Basics', cardCount=3}", result);
    }

    @Test
    void toString_WithEmptyCards_ShouldShowZeroCount() {
        // Given
        DeckExportData exportData = new DeckExportData("Empty Deck", List.of());

        // When
        String result = exportData.toString();

        // Then
        assertEquals("DeckExportData{name='Empty Deck', cardCount=0}", result);
    }

    @Test
    void fieldsAreFinal_ShouldNotAllowModification() {
        // Given
        DeckExportData exportData = new DeckExportData("Test", List.of());

        // When & Then - fields should be final, so no setters should exist
        assertThrows(NoSuchMethodException.class, () ->
                DeckExportData.class.getMethod("setName", String.class));
        assertThrows(NoSuchMethodException.class, () ->
                DeckExportData.class.getMethod("setCards", List.class));
    }
}