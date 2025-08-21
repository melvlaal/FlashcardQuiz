package com.flashcard.model.dto;

import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardExportDataTest {

    private Deck testDeck;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testDeck = new Deck("Test Deck");
        testDeck.setId(1L);

        testCard = new Card("What is Java?", "A programming language", testDeck);
        testCard.setId(1L);
    }

    @Test
    void jsonConstructor_ShouldSetFields() {
        // Given
        String question = "Test Question";
        String answer = "Test Answer";

        // When
        CardExportData exportData = new CardExportData(question, answer);

        // Then
        assertEquals(question, exportData.getQuestion());
        assertEquals(answer, exportData.getAnswer());
    }

    @Test
    void cardConstructor_ShouldExtractFieldsFromCard() {
        // When
        CardExportData exportData = new CardExportData(testCard);

        // Then
        assertEquals("What is Java?", exportData.getQuestion());
        assertEquals("A programming language", exportData.getAnswer());
    }

    @Test
    void cardConstructor_WithNullCard_ShouldHandleGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new CardExportData(null));
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        // Given
        String question = "Sample Question";
        String answer = "Sample Answer";
        CardExportData exportData = new CardExportData(question, answer);

        // When & Then
        assertEquals(question, exportData.getQuestion());
        assertEquals(answer, exportData.getAnswer());
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        // Given
        String question = "What is Spring?";
        String answer = "A Java framework";
        CardExportData exportData = new CardExportData(question, answer);

        // When
        String result = exportData.toString();

        // Then
        assertEquals("CardExportData{question='What is Spring?', answer='A Java framework'}", result);
    }

    @Test
    void fieldsAreFinal_ShouldNotAllowModification() {
        // Given
        CardExportData exportData = new CardExportData("Question", "Answer");

        // When & Then - fields should be final, so no setters should exist
        assertThrows(NoSuchMethodException.class, () ->
                CardExportData.class.getMethod("setQuestion", String.class));
        assertThrows(NoSuchMethodException.class, () ->
                CardExportData.class.getMethod("setAnswer", String.class));
    }
}
