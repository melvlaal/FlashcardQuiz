package com.flashcard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Service class for file operations (JSON and CSV import/export)
 */
@Service
public class FileService {

    private final ObjectMapper objectMapper;
    private final DeckService deckService;
    private final CardService cardService;
    private final Scanner scanner;

    @Autowired
    public FileService(ObjectMapper objectMapper, DeckService deckService, CardService cardService) {
        this.scanner = new Scanner(System.in);
        this.objectMapper = objectMapper;
        this.deckService = deckService;
        this.cardService = cardService;
    }

    /**
     * Export deck to JSON file
     */
    public void exportDeckToJson(Deck deck, String filePath) throws IOException {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        List<Card> cards = cardService.getCardsByDeck(deck);
        DeckExportData exportData = new DeckExportData(deck.getName(), cards);

        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        objectMapper.writeValue(path.toFile(), exportData);
    }

    /**
     * Import deck from JSON file
     */
    public Deck importDeckFromJson(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        DeckExportData importData = objectMapper.readValue(path.toFile(), DeckExportData.class);

        // Create deck with unique name if necessary
        String deckName = getUniqueDeckName(importData.getName());
        Deck deck = deckService.createDeck(deckName);

        // Import cards
        for (CardExportData cardData : importData.getCards()) {
            cardService.createCard(cardData.getQuestion(), cardData.getAnswer(), deck);
        }

        return deck;
    }

    /**
     * Export deck to CSV file
     */
    public void exportDeckToCsv(Deck deck, String filePath) throws IOException {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        List<Card> cards = cardService.getCardsByDeck(deck);

        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
            // Write CSV header
            writer.println("Question,Answer,Correct Count,Incorrect Count,Accuracy Rate");

            // Write card data
            for (Card card : cards) {
                String question = escapeCsvField(card.getQuestion());
                String answer = escapeCsvField(card.getAnswer());
                double accuracy = card.getAccuracyRate();

                writer.printf("%s,%s,%d,%d,%.2f%n",
                        question, answer, card.getCorrectCount(),
                        card.getIncorrectCount(), accuracy);
            }
        }
    }

    /**
     * Import deck from CSV file
     */
    public Deck importDeckFromCsv(String filePath, String deckName) {
        Deck importedDeck = fileService.importDeckFromCsv(filePath, deckName);
        System.out.println("Deck '" + importedDeck.getName() + "' imported successfully!");
        System.out.println("Cards imported: " + cardService.getCardCount(importedDeck));
        return importedDeck;
    }
}
