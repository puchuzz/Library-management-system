-- Optional sample data.  Usage:  mysql -u root -p library_db < sql/seed.sql
-- (The default admin account is created automatically by the app on first run.)
USE library_db;

INSERT IGNORE INTO books (isbn, title, author, category, total_copies, available_copies) VALUES
('9780132350884', 'Clean Code',                         'Robert C. Martin',   'Software Engineering', 3, 3),
('9780134685991', 'Effective Java',                     'Joshua Bloch',       'Programming',          2, 2),
('9780262033848', 'Introduction to Algorithms',         'Thomas H. Cormen',   'Algorithms',           4, 4),
('9780136042594', 'Artificial Intelligence: A Modern Approach', 'Stuart Russell', 'Computer Science',  2, 2),
('9780070636774', 'Digital Electronics',                'R. P. Jain',         'Electronics',          3, 3),
('9780073380377', 'Communication Systems',              'Simon Haykin',       'Electronics',          2, 2),
('9780596517748', 'JavaScript: The Good Parts',         'Douglas Crockford',  'Programming',          1, 1);

INSERT IGNORE INTO members (full_name, email, phone, member_type, joined_on, active) VALUES
('Riya Sen',        'riya.sen@example.com',     '9000000001', 'STUDENT', CURDATE(), TRUE),
('Arjun Das',       'arjun.das@example.com',    '9000000002', 'STUDENT', CURDATE(), TRUE),
('Dr. Meera Ghosh', 'meera.ghosh@example.com',  '9000000003', 'FACULTY', CURDATE(), TRUE);
