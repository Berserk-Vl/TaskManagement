INSERT INTO users(email, password) VALUES ('admin@sb.ru', 'admin');
INSERT INTO users(email, password) VALUES ('user@mail.ru', '123');

INSERT INTO tasks(title, description, status, priority, author, performer) VALUES ('Buy juice.', 'Go to store and by orange juice.', 'PENDING', 'HIGH', 'user@mail.ru', null);
INSERT INTO tasks(title, description, status, priority, author, performer) VALUES ('Task Management.', 'Create simple task management system.', 'IN_PROCESS', 'HIGH', 'admin@sb.ru', 'admin@sb.ru');
INSERT INTO tasks(title, description, status, priority, author, performer) VALUES ('Simple task.', 'Be happy.', 'DONE', 'LOW', 'admin@sb.ru', 'user@mail.ru');

INSERT INTO comments(task_id, author, text, timestamp) VALUES (1, 'admin@sb.ru', 'I love orange juice.', '2024-08-10 01:09:02.846821+03');
INSERT INTO comments(task_id, author, text, timestamp) VALUES (1, 'user@mail.ru', 'Tomato juice is good too.', '2024-08-11 00:35:32.837361+03');
INSERT INTO comments(task_id, author, text, timestamp) VALUES (3, 'user@mail.ru', 'It was not an easy task.', '2024-08-10 20:24:38.479498+03');