package com.flashcards.service;

import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import com.flashcard.repository.CardRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CardService
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private Validator validator;

    @InjectMocks
    private CardService cardService;

    private Deck testDeck;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testDeck = new Deck("Test Deck");
        testDeck.setId(1L);

        testCard = new Card("Test Question", "Test Answer", testDeck);
        testCard.setId(1L);
    }

    @Test
    void createCard_ValidData_ReturnsCreatedCard() {
        // Given
        String question = "What is Java?";
        String answer = "A programming language";
        when(validator.validate(any(Card.class))).thenReturn(Set.of());
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        Card result = cardService.createCard(question, answer, testDeck);

        // Then
        assertNotNull(result);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_EmptyQuestion_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                cardService.createCard("", "Answer", testDeck));
        assertThrows(IllegalArgumentException.class, () ->
                cardService.createCard(null, "Answer", testDeck));
    }

    @Test
    void createCard_EmptyAnswer_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                cardService.createCard("Question", "", testDeck));
        assertThrows(IllegalArgumentException.class, () ->
                cardService.createCard("Question", null, testDeck));
    }

    @Test
    void createCard_NullDeck_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                cardService.createCard("Question", "Answer", null));
    }

    @Test
    void getCardsByDeck_ReturnsListOfCards() {
        // Given
        List<Card> expectedCards = Arrays.asList(testCard, new Card("Q2", "A2", testDeck));
        when(cardRepository.findByDeckOrderByCreatedAtDesc(testDeck)).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.getCardsByDeck(testDeck);

        // Then
        assertEquals(expectedCards, result);
        verify(cardRepository).findByDeckOrderByCreatedAtDesc(testDeck);
    }

    @Test
    void findCardById_ExistingId_ReturnsOptionalWithCard() {
        // Given
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

        // When
        Optional<Card> result = cardService.findCardById(cardId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCard, result.get());
    }

    @Test
    void updateCard_ValidData_ReturnsUpdatedCard() {
        // Given
        Long cardId = 1L;
        String newQuestion = "Updated Question";
        String newAnswer = "Updated Answer";
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
        when(validator.validate(any(Card.class))).thenReturn(Set.of());
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        Card result = cardService.updateCard(cardId, newQuestion, newAnswer);

        // Then
        assertNotNull(result);
        verify(cardRepository).save(testCard);
    }

    @Test
    void deleteCard_ExistingId_ReturnsTrue() {
        // Given
        Long cardId = 1L;
        when(cardRepository.existsById(cardId)).thenReturn(true);

        // When
        boolean result = cardService.deleteCard(cardId);

        // Then
        assertTrue(result);
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void recordCorrectAnswer_UpdatesCardStatistics() {
        // Given
        Long cardId = 1L;
        int initialCorrectCount = testCard.getCorrectCount();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        Card result = cardService.recordCorrectAnswer(cardId);

        // Then
        assertNotNull(result);
        verify(cardRepository).save(testCard);
        // Note: The actual increment is tested in the Card entity test
    }

    @Test
    void recordIncorrectAnswer_UpdatesCardStatistics() {
        // Given
        Long cardId = 1L;
        int initialIncorrectCount = testCard.getIncorrectCount();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        Card result = cardService.recordIncorrectAnswer(cardId);

        // Then
        assertNotNull(result);
        verify(cardRepository).save(testCard);
    }

    @Test
    void searchCards_WithKeyword_ReturnsFilteredCards() {
        // Given
        String keyword = "Java";
        List<Card> expectedCards = Arrays.asList(testCard);
        when(cardRepository.findByDeckAndKeyword(testDeck, keyword)).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCards(testDeck, keyword);

        // Then
        assertEquals(expectedCards, result);
        verify(cardRepository).findByDeckAndKeyword(testDeck, keyword);
    }

    @Test
    void searchCards_EmptyKeyword_ReturnsAllCards() {
        // Given
        List<Card> expectedCards = Arrays.asList(testCard);
        when(cardRepository.findByDeckOrderByCreatedAtDesc(testDeck)).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCards(testDeck, "");

        // Then
        assertEquals(expectedCards, result);
        verify(cardRepository).findByDeckOrderByCreatedAtDesc(testDeck);
    }
}