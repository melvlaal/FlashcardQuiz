package com.flashcard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import com.flashcard.model.dto.CardExportData;
import com.flashcard.model.dto.DeckExportData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Service class for file operations (JSON and CSV import/export)
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final ObjectMapper objectMapper;
    private final DeckService deckService;
    private final CardService cardService;

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
        DeckExportData exportData = DeckExportData.fromCardEntities(deck.getName(), cards);

        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

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
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
            // Write CSV header
            writer.println("Question,Answer");

            // Write card data
            for (Card card : cards) {
                String question = escapeCsvField(card.getQuestion());
                String answer = escapeCsvField(card.getAnswer());

                writer.printf("%s,%s%n",
                        question, answer);
            }
        }
    }

    /**
     * Import deck from CSV file
     */
    public Deck importDeckFromCsv(String filePath, String deckName) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        if (deckName == null || deckName.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name cannot be empty");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        // Create deck with unique name if necessary
        String uniqueDeckName = getUniqueDeckName(deckName);
        Deck deck = deckService.createDeck(uniqueDeckName);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Parse CSV line
                String[] parts = parseCsvLine(line);
                if (parts.length >= 2) {
                    String question = unescapeCsvField(parts[0]);
                    String answer = unescapeCsvField(parts[1]);

                    if (!question.trim().isEmpty() && !answer.trim().isEmpty()) {
                        cardService.createCard(question, answer, deck);
                    }
                }
            }
        }

        return deck;
    }

    /**
     * Get unique deck name by appending number if necessary
     */
    private String getUniqueDeckName(String baseName) {
        String trimmedName = baseName.trim();
        Optional<Deck> existingDeck = deckService.findDeckByName(trimmedName);

        if (!existingDeck.isPresent()) {
            return trimmedName;
        }

        int counter = 1;
        String uniqueName;
        do {
            uniqueName = trimmedName + " (" + counter + ")";
            existingDeck = deckService.findDeckByName(uniqueName);
            counter++;
        } while (existingDeck.isPresent());

        return uniqueName;
    }

    /**
     * Escape CSV field by wrapping in quotes and escaping internal quotes
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        // If field contains comma, newline, or quote, wrap in quotes
        if (field.contains(",") || field.contains("\n") || field.contains("\"")) {
            // Escape existing quotes by doubling them
            String escaped = field.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }

        return field;
    }

    /**
     * Unescape CSV field by removing quotes and unescaping internal quotes
     */
    private String unescapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        String trimmed = field.trim();

        // If field is wrapped in quotes, remove them and unescape internal quotes
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            String content = trimmed.substring(1, trimmed.length() - 1);
            return content.replace("\"\"", "\"");
        }

        return trimmed;
    }

    /**
     * Parse CSV line handling quoted fields
     */
    private String[] parseCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return new String[0];
        }

        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Check if it's an escaped quote
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++; // Skip the next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Add the last field
        result.add(currentField.toString());

        return result.toArray(new String[0]);
    }
}
