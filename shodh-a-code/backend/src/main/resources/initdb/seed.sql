-- ========================
-- Enable pgcrypto for UUID generation if needed
-- ========================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================
-- Create tables
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
    contest_id BIGINT NOT NULL REFERENCES contest(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS testcase (
    id BIGINT PRIMARY KEY,
    problem_id BIGINT NOT NULL REFERENCES problem(id) ON DELETE CASCADE,
    input_data TEXT,
    expected_output TEXT,
    hidden BOOLEAN
);

-- ========================
-- Seed contest
-- ========================
INSERT INTO contest (id, name, description, start_time, end_time)
VALUES (1, 'Sample Contest', 'First seeded contest', NOW(), NOW() + interval '1 hour')
ON CONFLICT (id) DO NOTHING;

-- ========================
-- Seed problem
-- ========================
-- Make sure the contest exists before inserting
INSERT INTO problem (id, contest_id, title, description)
SELECT 1, id, 'Add Two Numbers', 'Read two integers from input and print their sum.'
FROM contest
WHERE id = 1
ON CONFLICT (id) DO NOTHING;

-- ========================
-- Seed testcases
-- ========================
-- Make sure the problem exists before inserting
INSERT INTO testcase (id, problem_id, input_data, expected_output, hidden)
SELECT 1, id, '1 2', '3', false FROM problem WHERE id = 1
UNION ALL
SELECT 2, id, '5 7', '12', false FROM problem WHERE id = 1
ON CONFLICT (id) DO NOTHING;
