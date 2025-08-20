package com.flashcard.service;

import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import com.flashcard.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for card management operations
 */
@Service
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final Validator validator;

    @Autowired
    public CardService(CardRepository cardRepository, Validator validator) {
        this.cardRepository = cardRepository;
        this.validator = validator;
    }

    /**
     * Create a new card in the specified deck
     */
    public Card createCard(String question, String answer, Deck deck) {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }
        if (answer == null || answer.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer cannot be empty");
        }
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }

        Card card = new Card(question.trim(), answer.trim(), deck);
        validateCard(card);

        return cardRepository.save(card);
    }

    /**
     * Get all cards in a deck
     */
    @Transactional(readOnly = true)
    public List<Card> getCardsByDeck(Deck deck) {
        return cardRepository.findByDeckOrderByCreatedAtDesc(deck);
    }

    /**
     * Find card by ID
     */
    @Transactional(readOnly = true)
    public Optional<Card> findCardById(Long id) {
        return cardRepository.findById(id);
    }

    /**
     * Update card question and answer
     */
    public Card updateCard(Long cardId, String question, String answer) {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }
        if (answer == null || answer.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer cannot be empty");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));

        card.setQuestion(question.trim());
        card.setAnswer(answer.trim());
        validateCard(card);

        return cardRepository.save(card);
    }

    /**
     * Delete card by ID
     */
    public boolean deleteCard(Long cardId) {
        if (cardRepository.existsById(cardId)) {
            cardRepository.deleteById(cardId);
            return true;
        }
        return false;
    }

    /**
     * Search cards by keyword in question or answer
     */
    @Transactional(readOnly = true)
    public List<Card> searchCards(Deck deck, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getCardsByDeck(deck);
        }
        return cardRepository.findByDeckAndKeyword(deck, keyword.trim());
    }

    /**
     * Get cards that have never been reviewed
     */
    @Transactional(readOnly = true)
    public List<Card> getUnreviewedCards(Deck deck) {
        return cardRepository.findUnreviewedCardsByDeck(deck);
    }

    /**
     * Get cards with low accuracy rate
     */
    @Transactional(readOnly = true)
    public List<Card> getCardsWithLowAccuracy(Deck deck, double threshold) {
        return cardRepository.findCardsWithLowAccuracy(deck, threshold);
    }

    /**
     * Record correct answer for a card
     */
    public Card recordCorrectAnswer(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));

        card.incrementCorrectCount();
        return cardRepository.save(card);
    }

    /**
     * Record incorrect answer for a card
     */
    public Card recordIncorrectAnswer(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));

        card.incrementIncorrectCount();
        return cardRepository.save(card);
    }

    /**
     * Get total number of cards in a deck
     */
    @Transactional(readOnly = true)
    public long getCardCount(Deck deck) {
        return cardRepository.countByDeck(deck);
    }

    /**
     * Check if card exists
     */
    @Transactional(readOnly = true)
    public boolean cardExists(Long cardId) {
        return cardRepository.existsById(cardId);
    }

    /**
     * Validate card entity
     */
    private void validateCard(Card card) {
        Set<ConstraintViolation<Card>> violations = validator.validate(card);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Validation errors: ");
            for (ConstraintViolation<Card> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }
}