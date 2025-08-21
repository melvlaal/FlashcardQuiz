package com.flashcard.service;

import com.flashcard.model.Deck;
import com.flashcard.repository.DeckRepository;
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
class DeckServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private Validator validator;

    @InjectMocks
    private DeckService deckService;

    private Deck testDeck;

    @BeforeEach
    void setUp() {
        testDeck = new Deck("Test Deck");
        testDeck.setId(1L);
        testDeck.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createDeck_WithValidName_ShouldCreateDeck() {
        // Given
        String deckName = "New Deck";
        when(deckRepository.existsByNameIgnoreCase(deckName)).thenReturn(false);
        when(validator.validate(any(Deck.class))).thenReturn(Set.of());
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

        // When
        Deck result = deckService.createDeck(deckName);

        // Then
        assertNotNull(result);
        verify(deckRepository).existsByNameIgnoreCase(deckName);
        verify(deckRepository).save(any(Deck.class));
        verify(validator).validate(any(Deck.class));
    }

    @Test
    void createDeck_WithEmptyName_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deckService.createDeck("")
        );

        assertEquals("Deck name cannot be empty", exception.getMessage());
        verify(deckRepository, never()).save(any());
    }

    @Test
    void createDeck_WithNullName_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deckService.createDeck(null)
        );

        assertEquals("Deck name cannot be empty", exception.getMessage());
        verify(deckRepository, never()).save(any());
    }

    @Test
    void createDeck_WithExistingName_ShouldThrowException() {
        // Given
        String deckName = "Existing Deck";
        when(deckRepository.existsByNameIgnoreCase(deckName)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deckService.createDeck(deckName)
        );

        assertEquals("Deck with name 'Existing Deck' already exists", exception.getMessage());
        verify(deckRepository, never()).save(any());
    }

    @Test
    void createDeck_WithValidationErrors_ShouldThrowException() {
        // Given
        String deckName = "Valid Name";
        ConstraintViolation<Deck> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Validation error");

        when(deckRepository.existsByNameIgnoreCase(deckName)).thenReturn(false);
        when(validator.validate(any(Deck.class))).thenReturn(Set.of(violation));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deckService.createDeck(deckName)
        );

        assertTrue(exception.getMessage().contains("Validation errors"));
        assertTrue(exception.getMessage().contains("Validation error"));
        verify(deckRepository, never()).save(any());
    }

    @Test
    void getAllDecks_ShouldReturnAllDecks() {
        // Given
        List<Deck> expectedDecks = List.of(testDeck);
        when(deckRepository.findAllByOrderByCreatedAtDesc()).thenReturn(expectedDecks);

        // When
        List<Deck> result = deckService.getAllDecks();

        // Then
        assertEquals(expectedDecks, result);
        verify(deckRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getDecksWithCards_ShouldReturnDecksWithCards() {
        // Given
        List<Deck> expectedDecks = List.of(testDeck);
        when(deckRepository.findDecksWithCards()).thenReturn(expectedDecks);

        // When
        List<Deck> result = deckService.getDecksWithCards();

        // Then
        assertEquals(expectedDecks, result);
        verify(deckRepository).findDecksWithCards();
    }

    @Test
    void findDeckByName_WithValidName_ShouldReturnDeck() {
        // Given
        String deckName = "Test Deck";
        when(deckRepository.findByNameIgnoreCase(deckName)).thenReturn(Optional.of(testDeck));

        // When
        Optional<Deck> result = deckService.findDeckByName(deckName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testDeck, result.get());
        verify(deckRepository).findByNameIgnoreCase(deckName);
    }

    @Test
    void findDeckByName_WithEmptyName_ShouldReturnEmpty() {
        // When
        Optional<Deck> result = deckService.findDeckByName("");

        // Then
        assertTrue(result.isEmpty());
        verify(deckRepository, never()).findByNameIgnoreCase(any());
    }

    @Test
    void findDeckByName_WithNullName_ShouldReturnEmpty() {
        // When
        Optional<Deck> result = deckService.findDeckByName(null);

        // Then
        assertTrue(result.isEmpty());
        verify(deckRepository, never()).findByNameIgnoreCase(any());
    }

    @Test
    void deleteDeck_WithExistingId_ShouldReturnTrue() {
        // Given
        Long deckId = 1L;
        when(deckRepository.existsById(deckId)).thenReturn(true);

        // When
        boolean result = deckService.deleteDeck(deckId);

        // Then
        assertTrue(result);
        verify(deckRepository).existsById(deckId);
        verify(deckRepository).deleteById(deckId);
    }

    @Test
    void deleteDeck_WithNonExistingId_ShouldReturnFalse() {
        // Given
        Long deckId = 999L;
        when(deckRepository.existsById(deckId)).thenReturn(false);

        // When
        boolean result = deckService.deleteDeck(deckId);

        // Then
        assertFalse(result);
        verify(deckRepository).existsById(deckId);
        verify(deckRepository, never()).deleteById(deckId);
    }
}
