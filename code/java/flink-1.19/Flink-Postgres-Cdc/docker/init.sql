CREATE SCHEMA IF NOT EXISTS labschema;

CREATE TABLE labschema.stream_note (
  id SERIAL PRIMARY KEY,
  added_date TIMESTAMP,
  content TEXT,
  is_live BOOLEAN,
  title TEXT
);

INSERT INTO labschema.stream_note (added_date, content, is_live, title)
VALUES
  (NOW(), 'Initial note 1', false, 'Note A'),
  (NOW(), 'Initial note 2', false, 'Note B');
