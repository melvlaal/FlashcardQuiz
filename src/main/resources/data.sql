INSERT INTO decks (name, created_at) VALUES
('Java Programming', CURRENT_TIMESTAMP),
('Spanish Vocabulary', CURRENT_TIMESTAMP),
('Math Formulas', CURRENT_TIMESTAMP);

INSERT INTO cards (question, answer, deck_id, created_at, correct_count, incorrect_count) VALUES
-- Java Programming cards
('What is polymorphism in Java?', 'The ability of objects to take on many forms through inheritance and method overriding', 1, CURRENT_TIMESTAMP, 0, 0),
('What is the difference between == and .equals() in Java?', '== compares references, .equals() compares object content', 1, CURRENT_TIMESTAMP, 0, 0),
('What is a constructor?', 'A special method used to initialize objects when they are created', 1, CURRENT_TIMESTAMP, 0, 0),

-- Spanish Vocabulary cards
('How do you say "hello" in Spanish?', 'Hola', 2, CURRENT_TIMESTAMP, 0, 0),
('How do you say "goodbye" in Spanish?', 'Adiós', 2, CURRENT_TIMESTAMP, 0, 0),
('How do you say "thank you" in Spanish?', 'Gracias', 2, CURRENT_TIMESTAMP, 0, 0),

-- Math Formulas cards
('What is the quadratic formula?', 'x = (-b ± √(b²-4ac)) / 2a', 3, CURRENT_TIMESTAMP, 0, 0),
('What is the area of a circle?', 'A = πr²', 3, CURRENT_TIMESTAMP, 0, 0),
('What is the Pythagorean theorem?', 'a² + b² = c²', 3, CURRENT_TIMESTAMP, 0, 0);