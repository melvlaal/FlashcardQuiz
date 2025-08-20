package com.flashcards.service;

import com.flashcard.model.Deck;
import com.flashcard.repository.DeckRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeckService
 */
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
    }

    @Test
    void createDeck_ValidName_ReturnsCreatedDeck() {
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
    }

    @Test
    void createDeck_EmptyName_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                deckService.createDeck(""));
        assertThrows(IllegalArgumentException.class, () ->
                deckService.createDeck(null));
    }

    @Test
    void createDeck_DuplicateName_ThrowsException() {
        // Given
        String deckName = "Existing Deck";
        when(deckRepository.existsByNameIgnoreCase(deckName)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                deckService.createDeck(deckName));
    }

    @Test
    void getAllDecks_ReturnsListOfDecks() {
        // Given
        List<Deck> expectedDecks = Arrays.asList(testDeck, new Deck("Another Deck"));
        when(deckRepository.findAllByOrderByCreatedAtDesc()).thenReturn(expectedDecks);

        // When
        List<Deck> result = deckService.getAllDecks();

        // Then
        assertEquals(expectedDecks, result);
        verify(deckRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void findDeckById_ExistingId_ReturnsOptionalWithDeck() {
        // Given
        Long deckId = 1L;
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));

        // When
        Optional<Deck> result = deckService.findDeckById(deckId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testDeck, result.get());
    }

    @Test
    void findDeckById_NonExistingId_ReturnsEmptyOptional() {
        // Given
        Long deckId = 999L;
        when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

        // When
        Optional<Deck> result = deckService.findDeckById(deckId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void deleteDeck_ExistingId_ReturnsTrue() {
        // Given
        Long deckId = 1L;
        when(deckRepository.existsById(deckId)).thenReturn(true);

        // When
        boolean result = deckService.deleteDeck(deckId);

        // Then
        assertTrue(result);
        verify(deckRepository).deleteById(deckId);
    }

    @Test
    void deleteDeck_NonExistingId_ReturnsFalse() {
        // Given
        Long deckId = 999L;
        when(deckRepository.existsById(deckId)).thenReturn(false);

        // When
        boolean result = deckService.deleteDeck(deckId);

        // Then
        assertFalse(result);
        verify(deckRepository, never()).deleteById(deckId);
    }

    @Test
    void updateDeckName_ValidName_ReturnsUpdatedDeck() {
        // Given
        Long deckId = 1L;
        String newName = "Updated Name";
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
        when(deckRepository.findByNameIgnoreCase(newName)).thenReturn(Optional.empty());
        when(validator.validate(any(Deck.class))).thenReturn(Set.of());
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

        // When
        Deck result = deckService.updateDeckName(deckId, newName);

        // Then
        assertNotNull(result);
        verify(deckRepository).save(testDeck);
    }
}