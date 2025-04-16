CREATE TABLE IF NOT EXISTS public.flusers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO public.flusers (name)
VALUES
    ('Alice'),
    ('Diana');


CREATE TABLE visa_users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    country VARCHAR(100),
    visa_type VARCHAR(50)
);

INSERT INTO visa_users (name, country, visa_type) VALUES
('John Doe', 'USA', 'Tourist'),
('Alice Smith', 'Canada', 'Work');
