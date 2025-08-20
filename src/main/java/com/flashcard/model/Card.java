package com.flashcard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a flashcard with question and answer
 */
@Entity
@Table(name = "cards")
@Getter
@Setter
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

    @Override
    public String toString() {
        return String.format("Card{id=%d, question='%s', answer='%s'}", id, question, answer);
    }
}
