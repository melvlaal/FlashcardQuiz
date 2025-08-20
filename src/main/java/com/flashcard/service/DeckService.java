package com.flashcard.service;

import com.flashcard.model.Deck;
import com.flashcard.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for deck management operations
 */
@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final Validator validator;

    /**
     * Create a new deck with validation
     */
    public Deck createDeck(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name cannot be empty");
        }

        String trimmedName = name.trim();

        if (deckRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Deck with name '" + trimmedName + "' already exists");
        }

        Deck deck = new Deck(trimmedName);
        validateDeck(deck);

        return deckRepository.save(deck);
    }

    /**
     * Get all decks ordered by creation date
     */
    public List<Deck> getAllDecks() {
        return deckRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get decks that contain at least one card
     */
    public List<Deck> getDecksWithCards() {
        return deckRepository.findDecksWithCards();
    }

    /**
     * Find deck by name (case-insensitive)
     */
    public Optional<Deck> findDeckByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return deckRepository.findByNameIgnoreCase(name.trim());
    }

    /**
     * Delete deck by ID
     */
    public boolean deleteDeck(Long deckId) {
        if (deckRepository.existsById(deckId)) {
            deckRepository.deleteById(deckId);
            return true;
        }
        return false;
    }

    /**
     * Validate deck entity
     */
    private void validateDeck(Deck deck) {
        Set<ConstraintViolation<Deck>> violations = validator.validate(deck);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Validation errors: ");
            for (ConstraintViolation<Deck> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }
}
