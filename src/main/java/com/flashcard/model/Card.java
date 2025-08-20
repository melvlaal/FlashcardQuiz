package com.flashcard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entity representing a flashcard with question and answer
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question cannot be empty")
    @Size(max = 500, message = "Question cannot exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String question;

    @NotBlank(message = "Answer cannot be empty")
    @Size(max = 500, message = "Answer cannot exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String answer;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

    @Column(name = "correct_count")
    private int correctCount = 0;

    @Column(name = "incorrect_count")
    private int incorrectCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    // Constructors
    public Card() {
        this.createdAt = LocalDateTime.now();
    }

    public Card(String question, String answer) {
        this();
        this.question = question;
        this.answer = answer;
    }

    public Card(String question, String answer, Deck deck) {
        this(question, answer);
        this.deck = deck;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastReviewed() { return lastReviewed; }
    public void setLastReviewed(LocalDateTime lastReviewed) { this.lastReviewed = lastReviewed; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getIncorrectCount() { return incorrectCount; }
    public void setIncorrectCount(int incorrectCount) { this.incorrectCount = incorrectCount; }

    public Deck getDeck() { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }

    // Helper methods
    public void incrementCorrectCount() {
        this.correctCount++;
        this.lastReviewed = LocalDateTime.now();
    }

    public void incrementIncorrectCount() {
        this.incorrectCount++;
        this.lastReviewed = LocalDateTime.now();
    }

    public double getAccuracyRate() {
        int total = correctCount + incorrectCount;
        return total > 0 ? (double) correctCount / total : 0.0;
    }

    @Override
    public String toString() {
        return String.format("Card{id=%d, question='%s', answer='%s'}", id, question, answer);
    }
}