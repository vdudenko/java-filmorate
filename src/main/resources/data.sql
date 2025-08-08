MERGE INTO genres (name) KEY (name) VALUES ('Комедия');
MERGE INTO genres (name) KEY (name) VALUES ('Драма');
MERGE INTO genres (name) KEY (name) VALUES ('Мультфильм');
MERGE INTO genres (name) KEY (name) VALUES ('Триллер');
MERGE INTO genres (name) KEY (name) VALUES ('Документальный');
MERGE INTO genres (name) KEY (name) VALUES ('Боевик');

MERGE INTO ratings (name, description) KEY (name) VALUES ('G', 'Нет возрастных ограничений.');
MERGE INTO ratings (name, description) KEY (name) VALUES ('PG', 'Детям рекомендуется смотреть фильм с родителями.');
MERGE INTO ratings (name, description) KEY (name) VALUES ('PG-13', 'Детям до 13 лет просмотр не желателен.');
MERGE INTO ratings (name, description) KEY (name) VALUES ('R', 'Лицам до 17 лет просматривать фильм можно только в присутствии взрослого.');
MERGE INTO ratings (name, description) KEY (name) VALUES ('NC-17', 'Лицам до 18 лет просмотр запрещён.');