-- seed contests, problems and a simple testcase
INSERT INTO contest (id, contest_id, name) VALUES (1, 'sample-101', 'Sample Contest');

INSERT INTO problem (id, slug, title, statement, contest_id)
VALUES (1, 'add-two', 'Add Two Numbers', 'Read two integers from input and print their sum.', 1);

INSERT INTO testcase (id, input_data, expected_output, hidden) VALUES (1, '1 2', '3', false);
