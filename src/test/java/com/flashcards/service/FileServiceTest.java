package com.flashcards.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashcard.model.Card;
import com.flashcard.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FileService
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DeckService deckService;

    @Mock
    private CardService cardService;

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    private Deck testDeck;
    private List<Card> testCards;

    @BeforeEach
    void setUp() {
        testDeck = new Deck("Test Deck");
        testDeck.setId(1L);

        Card card1 = new Card("Question 1", "Answer 1", testDeck);
        Card card2 = new Card("Question 2", "Answer 2", testDeck);
        testCards = Arrays.asList(card1, card2);
    }

    @Test
    void exportDeckToJson_ValidDeck_CreatesFile() throws IOException {
        // Given
        Path jsonFile = tempDir.resolve("test-deck.json");
        when(cardService.getCardsByDeck(testDeck)).thenReturn(testCards);

        // When
        fileService.exportDeckToJson(testDeck, jsonFile.toString());

        // Then
        verify(cardService).getCardsByDeck(testDeck);
        verify(objectMapper).writeValue(eq(jsonFile.toFile()), any(FileService.DeckExportData.class));
    }

    @Test
    void exportDeckToJson_NullDeck_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                fileService.exportDeckToJson(null, "test.json"));
    }

    @Test
    void exportDeckToJson_EmptyFilePath_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                fileService.exportDeckToJson(testDeck, ""));
    }

    @Test
    void importDeckFromJson_ValidFile_ReturnsDeck() throws IOException {
        // Given
        Path jsonFile = tempDir.resolve("import-test.json");
        Files.createFile(jsonFile);

        FileService.DeckExportData exportData = new FileService.DeckExportData();
        exportData.setName("Imported Deck");
        exportData.setCards(Arrays.asList(
                createCardExportData("Q1", "A1"),
                createCardExportData("Q2", "A2")
        ));

        when(objectMapper.readValue(eq(jsonFile.toFile()), eq(FileService.DeckExportData.class)))
                .thenReturn(exportData);
        when(deckService.findDeckByName("Imported Deck")).thenReturn(Optional.empty());
        when(deckService.createDeck("Imported Deck")).thenReturn(testDeck);
        when(cardService.createCard(anyString(), anyString(), eq(testDeck)))
                .thenReturn(new Card("Q", "A", testDeck));

        // When
        Deck result = fileService.importDeckFromJson(jsonFile.toString());

        // Then
        assertNotNull(result);
        verify(deckService).createDeck("Imported Deck");
        verify(cardService, times(2)).createCard(anyString(), anyString(), eq(testDeck));
    }

    @Test
    void exportDeckToCsv_ValidDeck_CreatesFile() throws IOException {
        // Given
        Path csvFile = tempDir.resolve("test-deck.csv");
        when(cardService.getCardsByDeck(testDeck)).thenReturn(testCards);

        // When
        fileService.exportDeckToCsv(testDeck, csvFile.toString());

        // Then
        assertTrue(Files.exists(csvFile));
        List<String> lines = Files.readAllLines(csvFile);
        assertTrue(lines.size() >= 3); // Header + 2 cards
        assertTrue(lines.get(0).contains("Question,Answer,Correct Count,Incorrect Count,Accuracy Rate"));
        verify(cardService).getCardsByDeck(testDeck);
    }

    @Test
    void importDeckFromCsv_ValidFile_ReturnsDeck() throws IOException {
        // Given
        Path csvFile = tempDir.resolve("import-test.csv");
        List<String> csvContent = Arrays.asList(
                "Question,Answer,Correct Count,Incorrect Count,Accuracy Rate",
                "What is Java?,A programming language,0,0,0.00",
                "What is Spring?,A framework,0,0,0.00"
        );
        Files.write(csvFile, csvContent);

        when(deckService.findDeckByName("Test Import")).thenReturn(Optional.empty());
        when(deckService.createDeck("Test Import")).thenReturn(testDeck);
        when(cardService.createCard(anyString(), anyString(), eq(testDeck)))
                .thenReturn(new Card("Q", "A", testDeck));

        // When
        Deck result = fileService.importDeckFromCsv(csvFile.toString(), "Test Import");

        // Then
        assertNotNull(result);
        verify(deckService).createDeck("Test Import");
        verify(cardService, times(2)).createCard(anyString(), anyString(), eq(testDeck));
    }

    private FileService.CardExportData createCardExportData(String question, String answer) {
        FileService.CardExportData cardData = new FileService.CardExportData();
        cardData.setQuestion(question);
        cardData.setAnswer(answer);
        cardData.setCorrectCount(0);
        cardData.setIncorrectCount(0);
        return cardData;
    }
}
