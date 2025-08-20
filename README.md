# Flashcard Quiz

A simple console-based flashcard learning application (similar to Anki).

## Features
- Create and delete decks
- Add, edit, and remove cards
- Quiz mode to practice flashcards
- (Planned) Save/load decks to JSON

## Technologies
- Java 17
- Maven
- Gson (for JSON storage)
- JUnit 5

## How to Run
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.example.flashcards.FlashcardQuizApplication"
```
