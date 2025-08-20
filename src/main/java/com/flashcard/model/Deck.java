package com.flashcard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a deck (collection) of flashcards
 */
@Entity
@Table(name = "decks")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Deck name cannot be empty")
    @Size(max = 100, message = "Deck name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Card> cards = new ArrayList<>();

    // Constructors
    public Deck() {
        this.createdAt = LocalDateTime.now();
    }

    public Deck(String name) {
        this();
        this.name = name;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }

    // Helper methods
    public void addCard(Card card) {
        cards.add(card);
        card.setDeck(this);
    }

    public void removeCard(Card card) {
        cards.remove(card);
        card.setDeck(null);
    }

    public int getCardCount() {
        return cards.size();
    }

    @Override
    public String toString() {
        return String.format("Deck{id=%d, name='%s', cardCount=%d}", id, name, getCardCount());
    }
}
