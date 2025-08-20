package com.flashcard.service;

import com.flashcard.model.Deck;
import com.flashcard.repository.DeckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for deck management operations
 */
@Service
@Transactional
public class DeckService {

    private final DeckRepository deckRepository;
    private final Validator validator;

    @Autowired
    public DeckService(DeckRepository deckRepository, Validator validator) {
        this.deckRepository = deckRepository;
        this.validator = validator;
    }

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
    @Transactional(readOnly = true)
    public List<Deck> getAllDecks() {
        return deckRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get decks that contain at least one card
     */
    @Transactional(readOnly = true)
    public List<Deck> getDecksWithCards() {
        return deckRepository.findDecksWithCards();
    }

    /**
     * Find deck by ID
     */
    @Transactional(readOnly = true)
    public Optional<Deck> findDeckById(Long id) {
        return deckRepository.findById(id);
    }

    /**
     * Find deck by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public Optional<Deck> findDeckByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return deckRepository.findByNameIgnoreCase(name.trim());
    }

    /**
     * Update deck name
     */
    public Deck updateDeckName(Long deckId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name cannot be empty");
        }

        String trimmedName = newName.trim();
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found with ID: " + deckId));

        // Check if another deck already has this name
        Optional<Deck> existingDeck = deckRepository.findByNameIgnoreCase(trimmedName);
        if (existingDeck.isPresent() && !existingDeck.get().getId().equals(deckId)) {
            throw new IllegalArgumentException("Deck with name '" + trimmedName + "' already exists");
        }

        deck.setName(trimmedName);
        validateDeck(deck);

        return deckRepository.save(deck);
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
     * Check if deck exists
     */
    @Transactional(readOnly = true)
    public boolean deckExists(Long deckId) {
        return deckRepository.existsById(deckId);
    }

    /**
     * Get total number of decks
     */
    @Transactional(readOnly = true)
    public long getTotalDeckCount() {
        return deckRepository.count();
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