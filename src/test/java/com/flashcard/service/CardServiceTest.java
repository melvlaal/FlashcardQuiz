package com.flashcard.service;

import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import com.flashcard.repository.CardRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        testDeck.setCreatedAt(LocalDateTime.now());

        testCard = new Card("Test Question", "Test Answer", testDeck);
        testCard.setId(1L);
        testCard.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createCard_WithValidInputs_ShouldCreateCard() {
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
        verify(validator).validate(any(Card.class));
    }

    @Test
    void createCard_WithEmptyQuestion_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.createCard("", "Test Answer", testDeck)
        );

        assertEquals("Question cannot be empty", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_WithNullQuestion_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.createCard(null, "Test Answer", testDeck)
        );

        assertEquals("Question cannot be empty", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_WithEmptyAnswer_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.createCard("Test Question", "", testDeck)
        );

        assertEquals("Answer cannot be empty", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_WithNullAnswer_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.createCard("Test Question", null, testDeck)
        );

        assertEquals("Answer cannot be empty", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_WithNullDeck_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.createCard("Test Question", "Test Answer", null)
        );

        assertEquals("Deck cannot be null", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_WithValidationErrors_ShouldThrowException() {
        // Given
        ConstraintViolation<Card> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Validation error");
        when(validator.validate(any(Card.class))).thenReturn(Set.of(violation));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.createCard("Question", "Answer", testDeck)
        );

        assertTrue(exception.getMessage().contains("Validation errors"));
        assertTrue(exception.getMessage().contains("Validation error"));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCardsByDeck_ShouldReturnCardsList() {
        // Given
        List<Card> expectedCards = List.of(testCard);
        when(cardRepository.findByDeckOrderByCreatedAtDesc(testDeck)).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.getCardsByDeck(testDeck);

        // Then
        assertEquals(expectedCards, result);
        verify(cardRepository).findByDeckOrderByCreatedAtDesc(testDeck);
    }

    @Test
    void updateCard_WithValidInputs_ShouldUpdateCard() {
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
        verify(cardRepository).findById(cardId);
        verify(cardRepository).save(any(Card.class));
        verify(validator).validate(any(Card.class));
    }

    @Test
    void updateCard_WithNonExistingCard_ShouldThrowException() {
        // Given
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.updateCard(cardId, "Question", "Answer")
        );

        assertEquals("Card not found with ID: 999", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void updateCard_WithEmptyQuestion_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.updateCard(1L, "", "Answer")
        );

        assertEquals("Question cannot be empty", exception.getMessage());
        verify(cardRepository, never()).findById(any());
    }

    @Test
    void updateCard_WithEmptyAnswer_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.updateCard(1L, "Question", "")
        );

        assertEquals("Answer cannot be empty", exception.getMessage());
        verify(cardRepository, never()).findById(any());
    }

    @Test
    void deleteCard_WithExistingId_ShouldReturnTrue() {
        // Given
        Long cardId = 1L;
        when(cardRepository.existsById(cardId)).thenReturn(true);

        // When
        boolean result = cardService.deleteCard(cardId);

        // Then
        assertTrue(result);
        verify(cardRepository).existsById(cardId);
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void deleteCard_WithNonExistingId_ShouldReturnFalse() {
        // Given
        Long cardId = 999L;
        when(cardRepository.existsById(cardId)).thenReturn(false);

        // When
        boolean result = cardService.deleteCard(cardId);

        // Then
        assertFalse(result);
        verify(cardRepository).existsById(cardId);
        verify(cardRepository, never()).deleteById(cardId);
    }

    @Test
    void searchCards_WithKeyword_ShouldReturnFilteredCards() {
        // Given
        String keyword = "java";
        List<Card> expectedCards = List.of(testCard);
        when(cardRepository.findByDeckAndKeyword(testDeck, keyword)).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCards(testDeck, keyword);

        // Then
        assertEquals(expectedCards, result);
        verify(cardRepository).findByDeckAndKeyword(testDeck, keyword);
    }

    @Test
    void searchCards_WithEmptyKeyword_ShouldReturnAllCards() {
        // Given
        List<Card> expectedCards = List.of(testCard);
        when(cardRepository.findByDeckOrderByCreatedAtDesc(testDeck)).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCards(testDeck, "");

        // Then
        assertEquals(expectedCards, result);
        verify(cardRepository).findByDeckOrderByCreatedAtDesc(testDeck);
        verify(cardRepository, never()).findByDeckAndKeyword(any(), any());
    }

    @Test
    void searchCards_WithNullKeyword_ShouldReturnAllCards() {
        // Given
        List<Card> expectedCards = List.of(testCard);
        when(cardRepository.findByDeckOrderByCreatedAtDesc(testDeck)).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCards(testDeck, null);

        // Then
        assertEquals(expectedCards, result);
        verify(cardRepository).findByDeckOrderByCreatedAtDesc(testDeck);
        verify(cardRepository, never()).findByDeckAndKeyword(any(), any());
    }

    @Test
    void getCardCount_ShouldReturnCount() {
        // Given
        long expectedCount = 5L;
        when(cardRepository.countByDeck(testDeck)).thenReturn(expectedCount);

        // When
        long result = cardService.getCardCount(testDeck);

        // Then
        assertEquals(expectedCount, result);
        verify(cardRepository).countByDeck(testDeck);
    }
}
