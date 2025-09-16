-- Enable pgcrypto for UUID generation if needed
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================
-- Create tables if they don't exist
-- ========================

CREATE TABLE IF NOT EXISTS contest (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS problem (
    id BIGINT PRIMARY KEY,
    contest_id BIGINT REFERENCES contest(id),
    title TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS testcase (
    id BIGINT PRIMARY KEY,
    problem_id BIGINT REFERENCES problem(id),
    input_data TEXT,
    expected_output TEXT,
    hidden BOOLEAN
);

-- ========================
-- Seed contest
-- ========================
INSERT INTO contest (id, name, description, start_time, end_time)
VALUES (1, 'Sample Contest', 'First seeded contest',
        NOW(), NOW() + interval '1 hour')
ON CONFLICT (id) DO NOTHING;

-- ========================
-- Seed problem
-- ========================
INSERT INTO problem (id, contest_id, title, description)
VALUES (
    1,
    (SELECT id FROM contest WHERE name='Sample Contest'),
    'Add Two Numbers',
    'Read two integers from input and print their sum.'
)
ON CONFLICT (id) DO NOTHING;

-- ========================
-- Seed testcases
-- ========================
INSERT INTO testcase (id, problem_id, input_data, expected_output, hidden)
VALUES 
  (1, (SELECT id FROM problem WHERE title='Add Two Numbers'), '1 2', '3', false),
  (2, (SELECT id FROM problem WHERE title='Add Two Numbers'), '5 7', '12', false)
ON CONFLICT (id) DO NOTHING;
