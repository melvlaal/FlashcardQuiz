package com.flashcard;

import com.flashcard.ui.ConsoleInterface;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FlashcardQuizApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashcardQuizApplication.class, args);
    }

    /**
     * Creates a CommandLineRunner bean to start the console interface
     * when the Spring Boot application launches
     */
    @Bean
    public CommandLineRunner commandLineRunner(ConsoleInterface consoleInterface) {
        return args -> consoleInterface.start();
    }
}