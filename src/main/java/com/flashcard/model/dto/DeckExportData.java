package com.flashcard.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flashcard.model.Card;
import java.util.List;

public class DeckExportData {

    private final String name;
    private final List<CardExportData> cards;

    @JsonCreator
    public DeckExportData(@JsonProperty("name") String name,
                          @JsonProperty("cards") List<CardExportData> cards) {
        this.name = name;
        this.cards = cards;
    }

    /**
     * Static factory method that converts Card entities to CardExportData
     */
    public static DeckExportData fromCardEntities(String name, List<Card> cardEntities) {
        List<CardExportData> cardExportData = cardEntities.stream()
                .map(CardExportData::new)
                .toList();
        return new DeckExportData(name, cardExportData);
    }

    public String getName() {
        return name;
    }

    public List<CardExportData> getCards() {
        return cards;
    }

    @Override
    public String toString() {
        return String.format("DeckExportData{name='%s', cardCount=%d}", name, cards.size());
    }
}
