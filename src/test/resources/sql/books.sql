-- test data for the integration tests (and the extra of exercise 4)
-- clear the child tables first (the DataLoader seeds reviews and book_categories)
DELETE FROM reviews;
DELETE FROM book_categories;
DELETE FROM books;

INSERT INTO books (id, title, author, published_year, available)
VALUES (1, 'Clean Code', 'Robert C. Martin', 2008, true),
       (2, 'The Clean Coder', 'Robert C. Martin', 2011, true),
       (3, 'Effective Java', 'Joshua Bloch', 2018, true),
       (4, 'Head First Java', 'Kathy Sierra', 2005, false);

-- books created by the tests get id 100, 101... (no clash with the ids above)
ALTER TABLE books ALTER COLUMN id RESTART WITH 100;
