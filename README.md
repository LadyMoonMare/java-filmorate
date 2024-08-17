<h3><span style="color:orange">Приложение Filmorate</span></h3>
___
*Диаграмма приложения Filmorate:*<br>
![Диаграмма приложения Filmorate](/diagramFilmorate.svg)

*Пояснения к диаграмме:*<br>

*- <span style="color:yellow">reviews</span> - таблица с отзывами, имеет первичный ключ - идентификатор (<span style="color:orange">id</span>), текст отзыва (<span style="color:orange">content</span>), идентификатор*<br>
*фильма (<span style="color:orange">film_id</span>), тип отзыва (<span style="color:orange">is_positive</span>), рейтинг полезности (<span style="color:orange">useful</span>) и идентификатор пользователя (<span style="color:orange">user_id</span>);*<br>
*- <span style="color:yellow">like_reviews</span> - таблица, определяющая лайки поставленные отзывам;*<br>
*- <span style="color:yellow">feed</span> - таблица c лентой событий, имеет первичный ключ - идентификатор (<span style="color:orange">id</span>), идентификатор сущности над которой*<br>
*было совершено действие (<span style="color:orange">entity_id</span>), время совершения действия (<span style="color:orange">timestamp</span>), идентификатор пользователя (<span style="color:orange">user_id</span>);*<br>
*- <span style="color:yellow">feed_event_type</span> - таблица, связующая идентификатор события с идентификатором типа события;*<br>
*- <span style="color:yellow">feed_operation</span> - таблица, связующая идентификатор события с идентификатором операции;*<br>
*- <span style="color:yellow">event_type</span> - таблица, определяющая тип события;*<br>
*- <span style="color:yellow">event_type</span> - таблица, определяющая тип операции;*<br>
*- <span style="color:yellow">films</span> - таблица с фильмами, имеет первичный ключ - идентификатор (<span style="color:orange">id</span>), описание (<span style="color:orange">description</span>), продолжительность (<span style="color:orange">duration</span>),*<br>
*идентификатор рейтинга по классификации Ассоциации кинокомпаний (<span style="color:orange">mpa_id</span>), дату релиза (<span style="color:orange">release_date</span>) и название (<span style="color:orange">title</span>);*<br>
*- <span style="color:yellow">likes</span> - таблица, связующая идентификатор фильмов с идентификаторами пользователей, что соответствует лайкам,*<br>
*поставленным пользователями, к каждому фильму;*<br>
*- <span style="color:yellow">film_genre</span> - таблица, связующая идентификатор фильмов с жанрами фильмов;*<br>
*- <span style="color:yellow">film_director</span> - таблица, связующая идентификатор фильмов с идентификаторами режиссёров фильмов;*<br>
*- <span style="color:yellow">rating_mpa</span> - таблица, определяющая рейтинг по классификации Ассоциации кинокомпаний для каждого фильма;*<br>
*- <span style="color:yellow">directors</span> - таблица, определяющая режиссёра для каждого фильма;*<br>
*- <span style="color:yellow">genre</span> - таблица, содержащая виды жанров фильмов;*<br>
*- <span style="color:yellow">app_user</span> - таблица с пользователями, имеет первичный ключ - идентификатор (<span style="color:orange">id</span>), дату рождения (<span style="color:orange">birthday</span>),*<br>
*электронную почту (<span style="color:orange">email</span>), логин (<span style="color:orange">login</span>) и имя (<span style="color:orange">name</span>);*<br>
*- <span style="color:yellow">friends</span> - таблица, связующая идентификатор пользователя c идентификатором друга (пользователя) и определяющая*<br>
*статус их дружбы*<br>
