CREATE TABLE address (
    id BINARY(16) NOT NULL,
    street VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    person_id BINARY(16) NOT NULL,
    created_at TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_address_person FOREIGN KEY (person_id) REFERENCES person(id)
);
