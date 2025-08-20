package com.flashcard.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flashcard.model.Card;

/**
 * Data transfer object for card export/import operations
 */
public class CardExportData {

    private final String question;
    private final String answer;

    @JsonCreator
    public CardExportData(@JsonProperty("question") String question,
                          @JsonProperty("answer") String answer) {
        this.question = question;
        this.answer = answer;
    }

    /**
     * Constructor that converts Card entity to CardExportData
     */
    public CardExportData(Card card) {
        this.question = card.getQuestion();
        this.answer = card.getAnswer();
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public String toString() {
        return String.format("CardExportData{question='%s', answer='%s'}",
                question, answer);
    }
}
