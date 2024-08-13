INSERT INTO users(id, email, password) VALUES (1, 'admin@sb.ru', 'admin') ON CONFLICT (id) DO NOTHING;
INSERT INTO users(id, email, password) VALUES (2, 'user@mail.ru', '123') ON CONFLICT (id) DO NOTHING;

INSERT INTO tasks(id, title, description, status, priority, author, performer) VALUES (1, 'Buy juice.', 'Go to store and by orange juice.', 'PENDING', 'HIGH', 'admin@sb.ru', null) ON CONFLICT (id) DO NOTHING;
INSERT INTO tasks(id, title, description, status, priority, author, performer) VALUES (2, 'Task Management.', 'Create simple task management system.', 'IN_PROCESS', 'HIGH', 'admin@sb.ru', 'admin@sb.ru') ON CONFLICT (id) DO NOTHING;
INSERT INTO tasks(id, title, description, status, priority, author, performer) VALUES (3, 'Simple task.', 'Be happy.', 'DONE', 'LOW', 'admin@sb.ru', 'user@mail.ru') ON CONFLICT (id) DO NOTHING;

INSERT INTO comments(id, task_id, author, text, timestamp) VALUES (1, 1, 'admin@sb.ru', 'I love orange juice.', '2024-08-10 01:09:02.846821+03') ON CONFLICT (id) DO NOTHING;
INSERT INTO comments(id, task_id, author, text, timestamp) VALUES (2, 1, 'user@mail.ru', 'Tomato juice is good too.', '2024-08-11 00:35:32.837361+03') ON CONFLICT (id) DO NOTHING;
INSERT INTO comments(id, task_id, author, text, timestamp) VALUES (3, 3, 'user@mail.ru', 'It was not an easy task.', '2024-08-10 20:24:38.479498+03') ON CONFLICT (id) DO NOTHING;