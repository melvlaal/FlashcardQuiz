package com.flashcard.service;

import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Service class for quiz functionality and spaced repetition logic
 */
@Service
@Transactional
public class QuizService {

    private final CardService cardService;
    private final Random random;

    @Autowired
    public QuizService(CardService cardService) {
        this.cardService = cardService;
        this.random = new Random();
    }

    /**
     * Start a quiz session with the specified deck
     * Returns shuffled list of cards for the quiz
     */
    @Transactional(readOnly = true)
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

        // Update card statistics
        if (isCorrect) {
            cardService.recordCorrectAnswer(card.getId());
        } else {
            cardService.recordIncorrectAnswer(card.getId());
        }

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
     * Get quiz statistics for a deck
     */
    @Transactional(readOnly = true)
    public QuizStatistics getQuizStatistics(Deck deck) {
        List<Card> cards = cardService.getCardsByDeck(deck);

        int totalCards = cards.size();
        int reviewedCards = 0;
        int correctAnswers = 0;
        int totalAnswers = 0;

        for (Card card : cards) {
            if (card.getLastReviewed() != null) {
                reviewedCards++;
            }
            correctAnswers += card.getCorrectCount();
            totalAnswers += (card.getCorrectCount() + card.getIncorrectCount());
        }

        double overallAccuracy = totalAnswers > 0 ? (double) correctAnswers / totalAnswers : 0.0;

        return new QuizStatistics(totalCards, reviewedCards, totalAnswers, correctAnswers, overallAccuracy);
    }

    /**
     * Get recommended cards for review based on performance
     */
    @Transactional(readOnly = true)
    public List<Card> getRecommendedCardsForReview(Deck deck, int maxCards) {
        // Priority: unreviewed cards first, then cards with low accuracy
        List<Card> unreviewedCards = cardService.getUnreviewedCards(deck);
        List<Card> lowAccuracyCards = cardService.getCardsWithLowAccuracy(deck, 0.6);

        // Combine and limit the results
        unreviewedCards.addAll(lowAccuracyCards);
        Collections.shuffle(unreviewedCards, random);

        return unreviewedCards.size() > maxCards ?
                unreviewedCards.subList(0, maxCards) : unreviewedCards;
    }

    /**
     * Inner class to hold quiz result
     */
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
        public String getCorrectAnswer() { return correctAnswer; }
        public String getUserAnswer() { return userAnswer; }
    }

    /**
     * Inner class to hold quiz statistics
     */
    public static class QuizStatistics {
        private final int totalCards;
        private final int reviewedCards;
        private final int totalAnswers;
        private final int correctAnswers;
        private final double overallAccuracy;

        public QuizStatistics(int totalCards, int reviewedCards, int totalAnswers,
                              int correctAnswers, double overallAccuracy) {
            this.totalCards = totalCards;
            this.reviewedCards = reviewedCards;
            this.totalAnswers = totalAnswers;
            this.correctAnswers = correctAnswers;
            this.overallAccuracy = overallAccuracy;
        }

        public int getTotalCards() { return totalCards; }
        public int getReviewedCards() { return reviewedCards; }
        public int getUnreviewedCards() { return totalCards - reviewedCards; }
        public int getTotalAnswers() { return totalAnswers; }
        public int getCorrectAnswers() { return correctAnswers; }
        public int getIncorrectAnswers() { return totalAnswers - correctAnswers; }
        public double getOverallAccuracy() { return overallAccuracy; }
        public double getReviewProgress() { return totalCards > 0 ? (double) reviewedCards / totalCards : 0.0; }
    }
}