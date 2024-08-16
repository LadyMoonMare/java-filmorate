INSERT INTO mpa (rating)
VALUES ('G'),
       ('PG'),
       ('PG-13'),
       ('R'),
       ('NC-17');

INSERT INTO genres (id, name)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');

INSERT INTO event_type (event_name)
VALUES ('LIKE'),
       ('REVIEW'),
       ('FRIEND');

INSERT INTO operation (operation_name)
VALUES ('REMOVE'),
       ('ADD'),
       ('UPDATE');