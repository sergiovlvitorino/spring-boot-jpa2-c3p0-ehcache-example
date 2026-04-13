CREATE TABLE person (
    id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    job VARCHAR(100) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
);
