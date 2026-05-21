CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS word_books (
    id BIGINT PRIMARY KEY,
    book_name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    cover_url VARCHAR(512),
    word_count INTEGER NOT NULL DEFAULT 0,
    difficulty_level INTEGER NOT NULL DEFAULT 1,
    status INTEGER NOT NULL DEFAULT 1,
    create_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    update_time TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_word_books_status ON word_books(status);
CREATE INDEX IF NOT EXISTS idx_word_books_difficulty ON word_books(difficulty_level);
CREATE INDEX IF NOT EXISTS idx_word_books_status_difficulty_id ON word_books(status, difficulty_level, id DESC);
CREATE INDEX IF NOT EXISTS idx_word_books_book_name_trgm ON word_books USING gin (book_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_word_books_description_trgm ON word_books USING gin (description gin_trgm_ops);

CREATE TABLE IF NOT EXISTS word_book_words (
    id BIGINT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    word VARCHAR(128) NOT NULL,
    phonetic VARCHAR(128),
    pronunciation_url VARCHAR(512),
    translation VARCHAR(512),
    definition TEXT,
    example_sentence TEXT,
    example_translation TEXT,
    sort_no INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    update_time TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_word_book_words_book_id ON word_book_words(book_id);
CREATE INDEX IF NOT EXISTS idx_word_book_words_book_sort ON word_book_words(book_id, sort_no ASC, id ASC);
CREATE INDEX IF NOT EXISTS idx_word_book_words_book_word ON word_book_words(book_id, word);
CREATE INDEX IF NOT EXISTS idx_word_book_words_word_trgm ON word_book_words USING gin (word gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_word_book_words_translation_trgm ON word_book_words USING gin (translation gin_trgm_ops);
