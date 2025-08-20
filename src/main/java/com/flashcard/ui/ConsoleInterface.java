package com.flashcard.ui;

import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import com.flashcard.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based user interface for the flashcard application
 */
@Component
public class ConsoleInterface {

    private final Scanner scanner;
    private final DeckService deckService;
    private final CardService cardService;
    private final QuizService quizService;
    private final FileService fileService;

    @Autowired
    public ConsoleInterface(DeckService deckService, CardService cardService,
                            QuizService quizService, FileService fileService) {
        this.scanner = new Scanner(System.in);
        this.deckService = deckService;
        this.cardService = cardService;
        this.quizService = quizService;
        this.fileService = fileService;
    }

    /**
     * Start the console interface main loop
     */
    public void start() {
        printWelcome();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = getUserInput("Enter your choice: ");

            try {
                switch (choice) {
                    case "1" -> startQuiz();
                    case "2" -> manageDeckMenu();
                    case "3" -> fileOperationsMenu();
                    case "4" -> {
                        System.out.println("Thank you for using Flashcard Quiz! Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            if (running) {
                pressEnterToContinue();
            }
        }

        scanner.close();
    }

    /**
     * Print welcome message
     */
    private void printWelcome() {
        System.out.println("========================================");
        System.out.println("    Welcome to Flashcard Quiz App!");
        System.out.println("========================================");
        System.out.println();
    }

    /**
     * Print main menu options
     */
    private void printMainMenu() {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("1. Start Quiz");
        System.out.println("2. Manage Decks");
        System.out.println("3. File Operations");
        System.out.println("4. Exit");
        System.out.println();
    }

    /**
     * Start quiz mode
     */
    private void startQuiz() {
        List<Deck> decks = deckService.getDecksWithCards();
        if (decks.isEmpty()) {
            System.out.println("No decks with cards available. Please create a deck and add cards first.");
            return;
        }

        System.out.println("\n=== START QUIZ ===");
        Deck selectedDeck = selectDeck(decks, "Select deck for quiz:");
        if (selectedDeck == null) return;

        runQuiz(selectedDeck);
    }

    /**
     * Run quiz session for selected deck
     */
    private void runQuiz(Deck deck) {
        List<Card> cards = quizService.startQuizSession(deck);
        int correct = 0;
        int total = cards.size();

        System.out.println("\nStarting quiz with " + total + " cards from deck: " + deck.getName());
        System.out.println("Type 'quit' at any time to exit the quiz.\n");

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            System.out.printf("Question %d/%d: %s%n", i + 1, total, card.getQuestion());

            String userAnswer = getUserInput("Your answer: ");
            if ("quit".equalsIgnoreCase(userAnswer)) {
                System.out.println("Quiz ended early.");
                break;
            }

            QuizService.QuizResult result = quizService.checkAnswer(card, userAnswer);
            if (result.isCorrect()) {
                System.out.println("✓ Correct!");
                correct++;
            } else {
                System.out.println("✗ Incorrect. The correct answer is: " + result.getCorrectAnswer());
            }
            System.out.println();
        }

        // Show quiz results
        double percentage = total > 0 ? (double) correct / total * 100 : 0;
        System.out.println("=== QUIZ RESULTS ===");
        System.out.printf("Score: %d/%d (%.1f%%)%n", correct, total, percentage);

        // Show deck statistics
        QuizService.QuizStatistics stats = quizService.getQuizStatistics(deck);
        System.out.printf("Deck Progress: %d/%d cards reviewed (%.1f%%)%n",
                stats.getReviewedCards(), stats.getTotalCards(),
                stats.getReviewProgress() * 100);
        System.out.printf("Overall Accuracy: %.1f%% (%d/%d)%n",
                stats.getOverallAccuracy() * 100,
                stats.getCorrectAnswers(), stats.getTotalAnswers());
    }

    /**
     * Deck management menu
     */
    private void manageDeckMenu() {
        boolean backToMain = false;

        while (!backToMain) {
            System.out.println("\n=== DECK MANAGEMENT ===");
            System.out.println("1. Create New Deck");
            System.out.println("2. View All Decks");
            System.out.println("3. Select Deck for Card Management");
            System.out.println("4. Delete Deck");
            System.out.println("5. Back to Main Menu");

            String choice = getUserInput("Enter your choice: ");

            try {
                switch (choice) {
                    case "1" -> createNewDeck();
                    case "2" -> viewAllDecks();
                    case "3" -> selectDeckForCardManagement();
                    case "4" -> deleteDeck();
                    case "5" -> backToMain = true;
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            if (!backToMain && !choice.equals("5")) {
                pressEnterToContinue();
            }
        }
    }

    /**
     * Create new deck
     */
    private void createNewDeck() {
        System.out.println("\n=== CREATE NEW DECK ===");
        String name = getUserInput("Enter deck name: ");

        try {
            Deck deck = deckService.createDeck(name);
            System.out.println("Deck '" + deck.getName() + "' created successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating deck: " + e.getMessage());
        }
    }

    /**
     * View all decks with statistics
     */
    private void viewAllDecks() {
        List<Deck> decks = deckService.getAllDecks();

        System.out.println("\n=== ALL DECKS ===");
        if (decks.isEmpty()) {
            System.out.println("No decks found. Create a deck first.");
            return;
        }

        System.out.printf("%-5s %-30s %-10s %-15s%n", "ID", "Name", "Cards", "Created");
        System.out.println("-".repeat(65));

        for (Deck deck : decks) {
            long cardCount = cardService.getCardCount(deck);
            System.out.printf("%-5d %-30s %-10d %-15s%n",
                    deck.getId(),
                    truncateString(deck.getName(), 30),
                    cardCount,
                    deck.getCreatedAt().toLocalDate());
        }
    }

    /**
     * Select deck for card management
     */
    private void selectDeckForCardManagement() {
        List<Deck> decks = deckService.getAllDecks();
        if (decks.isEmpty()) {
            System.out.println("No decks available. Create a deck first.");
            return;
        }

        Deck selectedDeck = selectDeck(decks, "Select deck for card management:");
        if (selectedDeck != null) {
            manageCardsInDeck(selectedDeck);
        }
    }

    /**
     * Card management for selected deck
     */
    private void manageCardsInDeck(Deck deck) {
        boolean backToDeckMenu = false;

        while (!backToDeckMenu) {
            System.out.println("\n=== CARD MANAGEMENT - " + deck.getName() + " ===");
            System.out.println("1. Add New Card");
            System.out.println("2. View All Cards");
            System.out.println("3. Edit Card");
            System.out.println("4. Delete Card");
            System.out.println("5. Search Cards");
            System.out.println("6. Back to Deck Menu");

            String choice = getUserInput("Enter your choice: ");

            try {
                switch (choice) {
                    case "1" -> addNewCard(deck);
                    case "2" -> viewAllCards(deck);
                    case "3" -> editCard(deck);
                    case "4" -> deleteCard(deck);
                    case "5" -> searchCards(deck);
                    case "6" -> backToDeckMenu = true;
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            if (!backToDeckMenu && !choice.equals("6")) {
                pressEnterToContinue();
            }
        }
    }

    /**
     * Add new card to deck
     */
    private void addNewCard(Deck deck) {
        System.out.println("\n=== ADD NEW CARD ===");
        String question = getUserInput("Enter question: ");
        String answer = getUserInput("Enter answer: ");

        try {
            Card card = cardService.createCard(question, answer, deck);
            System.out.println("Card added successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error adding card: " + e.getMessage());
        }
    }

    /**
     * View all cards in deck
     */
    private void viewAllCards(Deck deck) {
        List<Card> cards = cardService.getCardsByDeck(deck);

        System.out.println("\n=== ALL CARDS - " + deck.getName() + " ===");
        if (cards.isEmpty()) {
            System.out.println("No cards found in this deck.");
            return;
        }

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            System.out.printf("\n[%d] ID: %d%n", i + 1, card.getId());
            System.out.println("Question: " + card.getQuestion());
            System.out.println("Answer: " + card.getAnswer());
            System.out.printf("Statistics: %d correct, %d incorrect (%.1f%% accuracy)%n",
                    card.getCorrectCount(), card.getIncorrectCount(),
                    card.getAccuracyRate() * 100);
        }
    }

    /**
     * Edit existing card
     */
    private void editCard(Deck deck) {
        List<Card> cards = cardService.getCardsByDeck(deck);
        if (cards.isEmpty()) {
            System.out.println("No cards available to edit.");
            return;
        }

        System.out.println("\n=== EDIT CARD ===");
        Card selectedCard = selectCard(cards, "Select card to edit:");
        if (selectedCard == null) return;

        System.out.println("Current question: " + selectedCard.getQuestion());
        String newQuestion = getUserInput("Enter new question (or press Enter to keep current): ");
        if (newQuestion.trim().isEmpty()) {
            newQuestion = selectedCard.getQuestion();
        }

        System.out.println("Current answer: " + selectedCard.getAnswer());
        String newAnswer = getUserInput("Enter new answer (or press Enter to keep current): ");
        if (newAnswer.trim().isEmpty()) {
            newAnswer = selectedCard.getAnswer();
        }

        try {
            cardService.updateCard(selectedCard.getId(), newQuestion, newAnswer);
            System.out.println("Card updated successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating card: " + e.getMessage());
        }
    }

    /**
     * Delete card from deck
     */
    private void deleteCard(Deck deck) {
        List<Card> cards = cardService.getCardsByDeck(deck);
        if (cards.isEmpty()) {
            System.out.println("No cards available to delete.");
            return;
        }

        System.out.println("\n=== DELETE CARD ===");
        Card selectedCard = selectCard(cards, "Select card to delete:");
        if (selectedCard == null) return;

        System.out.println("Question: " + selectedCard.getQuestion());
        System.out.println("Answer: " + selectedCard.getAnswer());

        String confirmation = getUserInput("Are you sure you want to delete this card? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirmation.trim())) {
            if (cardService.deleteCard(selectedCard.getId())) {
                System.out.println("Card deleted successfully!");
            } else {
                System.out.println("Failed to delete card.");
            }
        } else {
            System.out.println("Card deletion cancelled.");
        }
    }

    /**
     * Search cards by keyword
     */
    private void searchCards(Deck deck) {
        System.out.println("\n=== SEARCH CARDS ===");
        String keyword = getUserInput("Enter search keyword: ");

        List<Card> foundCards = cardService.searchCards(deck, keyword);

        System.out.println("\nSearch results for '" + keyword + "':");
        if (foundCards.isEmpty()) {
            System.out.println("No cards found matching the keyword.");
            return;
        }

        for (int i = 0; i < foundCards.size(); i++) {
            Card card = foundCards.get(i);
            System.out.printf("\n[%d] ID: %d%n", i + 1, card.getId());
            System.out.println("Question: " + card.getQuestion());
            System.out.println("Answer: " + card.getAnswer());
        }
    }

    /**
     * Delete deck with confirmation
     */
    private void deleteDeck() {
        List<Deck> decks = deckService.getAllDecks();
        if (decks.isEmpty()) {
            System.out.println("No decks available to delete.");
            return;
        }

        System.out.println("\n=== DELETE DECK ===");
        Deck selectedDeck = selectDeck(decks, "Select deck to delete:");
        if (selectedDeck == null) return;

        long cardCount = cardService.getCardCount(selectedDeck);
        System.out.println("Deck: " + selectedDeck.getName());
        System.out.println("Cards: " + cardCount);
        System.out.println("WARNING: This will delete the deck and all its cards!");

        String confirmation = getUserInput("Are you sure you want to delete this deck? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirmation.trim())) {
            if (deckService.deleteDeck(selectedDeck.getId())) {
                System.out.println("Deck deleted successfully!");
            } else {
                System.out.println("Failed to delete deck.");
            }
        } else {
            System.out.println("Deck deletion cancelled.");
        }
    }

    /**
     * File operations menu
     */
    private void fileOperationsMenu() {
        boolean backToMain = false;

        while (!backToMain) {
            System.out.println("\n=== FILE OPERATIONS ===");
            System.out.println("1. Export Deck to JSON");
            System.out.println("2. Import Deck from JSON");
            System.out.println("3. Export Deck to CSV");
            System.out.println("4. Import Deck from CSV");
            System.out.println("5. Back to Main Menu");

            String choice = getUserInput("Enter your choice: ");

            try {
                switch (choice) {
                    case "1" -> exportDeckToJson();
                    case "2" -> importDeckFromJson();
                    case "3" -> exportDeckToCsv();
                    case "4" -> importDeckFromCsv();
                    case "5" -> backToMain = true;
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            if (!backToMain && !choice.equals("5")) {
                pressEnterToContinue();
            }
        }
    }

    /**
     * Export deck to JSON file
     */
    private void exportDeckToJson() {
        List<Deck> decks = deckService.getAllDecks();
        if (decks.isEmpty()) {
            System.out.println("No decks available to export.");
            return;
        }

        System.out.println("\n=== EXPORT DECK TO JSON ===");
        Deck selectedDeck = selectDeck(decks, "Select deck to export:");
        if (selectedDeck == null) return;

        String fileName = getUserInput("Enter file name (without extension): ");
        String filePath = fileName + ".json";

        try {
            fileService.exportDeckToJson(selectedDeck, filePath);
            System.out.println("Deck exported successfully to: " + filePath);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    /**
     * Import deck from JSON file
     */
    private void importDeckFromJson() {
        System.out.println("\n=== IMPORT DECK FROM JSON ===");
        String filePath = getUserInput("Enter JSON file path: ");

        try {
            Deck importedDeck = fileService.importDeckFromJson(filePath);
            System.out.println("Deck '" + importedDeck.getName() + "' imported successfully!");
            System.out.println("Cards imported: " + cardService.getCardCount(importedDeck));
        } catch (IOException e) {
            System.out.println("Import failed: " + e.getMessage());
        }
    }

    /**
     * Export deck to CSV file
     */
    private void exportDeckToCsv() {
        List<Deck> decks = deckService.getAllDecks();
        if (decks.isEmpty()) {
            System.out.println("No decks available to export.");
            return;
        }

        System.out.println("\n=== EXPORT DECK TO CSV ===");
        Deck selectedDeck = selectDeck(decks, "Select deck to export:");
        if (selectedDeck == null) return;

        String fileName = getUserInput("Enter file name (without extension): ");
        String filePath = fileName + ".csv";

        try {
            fileService.exportDeckToCsv(selectedDeck, filePath);
            System.out.println("Deck exported successfully to: " + filePath);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    /**
     * Import deck from CSV file
     */
    private void importDeckFromCsv() {
        System.out.println("\n=== IMPORT DECK FROM CSV ===");
        String filePath = getUserInput("Enter CSV file path: ");
        String deckName = getUserInput("Enter name for the new deck: ");

        Deck importedDeck = fileService.importDeckFromCsv(filePath, deckName);
        System.out.println("Deck '" + importedDeck.getName() + "' imported successfully!");
        System.out.println("Cards imported: " + cardService.getCardCount(importedDeck));
    }

    // Helper methods

    /**
     * Select deck from list with user input
     */
    private Deck selectDeck(List<Deck> decks, String prompt) {
        System.out.println("\n" + prompt);
        System.out.printf("%-5s %-30s %-10s%n", "ID", "Name", "Cards");
        System.out.println("-".repeat(50));

        for (int i = 0; i < decks.size(); i++) {
            Deck deck = decks.get(i);
            long cardCount = cardService.getCardCount(deck);
            System.out.printf("%-5d %-30s %-10d%n",
                    i + 1,
                    truncateString(deck.getName(), 30),
                    cardCount);
        }

        try {
            String input = getUserInput("Enter deck number (or 0 to cancel): ");
            int choice = Integer.parseInt(input);

            if (choice == 0) {
                return null;
            } else if (choice > 0 && choice <= decks.size()) {
                return decks.get(choice - 1);
            } else {
                System.out.println("Invalid selection.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return null;
        }
    }

    /**
     * Select card from list with user input
     */
    private Card selectCard(List<Card> cards, String prompt) {
        System.out.println("\n" + prompt);

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            System.out.printf("[%d] Q: %s | A: %s%n",
                    i + 1,
                    truncateString(card.getQuestion(), 40),
                    truncateString(card.getAnswer(), 40));
        }

        try {
            String input = getUserInput("Enter card number (or 0 to cancel): ");
            int choice = Integer.parseInt(input);

            if (choice == 0) {
                return null;
            } else if (choice > 0 && choice <= cards.size()) {
                return cards.get(choice - 1);
            } else {
                System.out.println("Invalid selection.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return null;
        }
    }

    /**
     * Get user input with prompt
     */
    private String getUserInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Wait for user to press Enter
     */
    private void pressEnterToContinue() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Truncate string to specified length with ellipsis
     */
    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}