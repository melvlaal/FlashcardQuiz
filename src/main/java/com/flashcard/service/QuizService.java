package com.flashcard.service;

import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Service class for quiz functionality and spaced repetition logic
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    private final CardService cardService;
    private final Random random;

    /**
     * Start a quiz session with the specified deck
     * Returns shuffled list of cards for the quiz
     */
    public List<Card> startQuizSession(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }

        List<Card> cards = cardService.getCardsByDeck(deck);
        if (cards.isEmpty()) {
            throw new IllegalArgumentException("Deck '" + deck.getName() + "' contains no cards");
        }

        // Shuffle cards for random order
        Collections.shuffle(cards, random);
        return cards;
    }

    /**
     * Check if the user's answer matches the correct answer
     * Uses case-insensitive comparison and handles multiple acceptable answers
     */
    public QuizResult checkAnswer(Card card, String userAnswer) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        if (userAnswer == null) {
            userAnswer = "";
        }

        String correctAnswer = card.getAnswer().trim();
        String providedAnswer = userAnswer.trim();

        boolean isCorrect = isAnswerCorrect(correctAnswer, providedAnswer);

        return new QuizResult(isCorrect, correctAnswer, providedAnswer);
    }

    /**
     * Check if provided answer is correct using various matching strategies
     */
    private boolean isAnswerCorrect(String correctAnswer, String userAnswer) {
        if (correctAnswer.equalsIgnoreCase(userAnswer)) {
            return true;
        }

        // Handle multiple acceptable answers separated by semicolons or commas
        String[] acceptableAnswers = correctAnswer.split("[;,]");
        for (String answer : acceptableAnswers) {
            if (answer.trim().equalsIgnoreCase(userAnswer)) {
                return true;
            }
        }

        // Handle partial matches for longer answers (> 10 characters)
        if (correctAnswer.length() > 10 && userAnswer.length() > 5) {
            return correctAnswer.toLowerCase().contains(userAnswer.toLowerCase()) ||
                    userAnswer.toLowerCase().contains(correctAnswer.toLowerCase());
        }

        return false;
    }

    /**
     * Inner class to hold quiz result
     */
    @Getter
    public static class QuizResult {
        private final boolean correct;
        private final String correctAnswer;
        private final String userAnswer;

        public QuizResult(boolean correct, String correctAnswer, String userAnswer) {
            this.correct = correct;
            this.correctAnswer = correctAnswer;
            this.userAnswer = userAnswer;
        }

        public boolean isCorrect() { return correct; }
    }
}
