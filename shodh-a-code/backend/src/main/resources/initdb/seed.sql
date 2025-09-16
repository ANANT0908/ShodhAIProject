-- Enable pgcrypto for UUID generation if needed
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Seed contest (id is Long/int, so use 1)
INSERT INTO contest (id, name, description, start_time, end_time)
VALUES (1, 'Sample Contest', 'First seeded contest',
        NOW(), NOW() + interval '1 hour')
ON CONFLICT (id) DO NOTHING;

-- Seed problem (id is Long/int, so use 1)
INSERT INTO problem (id, contest_id, title, description)
VALUES (
    1,
    (SELECT id FROM contest WHERE name='Sample Contest'),
    'Add Two Numbers',
    'Read two integers from input and print their sum.'
)
ON CONFLICT (id) DO NOTHING;

-- Seed testcases (ids are Long/int, so use 1 and 2)
INSERT INTO testcase (id, problem_id, input_data, expected_output, hidden)
VALUES 
  (1, (SELECT id FROM problem WHERE title='Add Two Numbers'), '1 2', '3', false),
  (2, (SELECT id FROM problem WHERE title='Add Two Numbers'), '5 7', '12', false)
ON CONFLICT (id) DO NOTHING;
