INSERT INTO mpa (rating)
VALUES ('G'),
       ('PG'),
       ('PG-13'),
       ('R'),
       ('NC-17');

INSERT INTO genres (name)
VALUES ('Комедия'),
       ('Драма'),
       ('Мультфильм'),
       ('Триллер'),
       ('Документальный'),
       ('Боевик');

INSERT INTO event_type (event_name)
VALUES ('LIKE'),
       ('REVIEW'),
       ('FRIEND');

INSERT INTO operation (operation_name)
VALUES ('REMOVE'),
       ('ADD'),
       ('UPDATE');