package com.flashcard.model.dto;

import com.flashcard.model.Card;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DeckExportData {

    private final String name;
    private final List<CardExportData> cards;

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
