create table if not exists visitors (
    id uuid primary key ,
    name text,
    surname text,
    is_subscribed boolean
);

create table if not exists books (
    id uuid primary key ,
    title text,
    author text,
    publication_year int,
    isbn text,
    publisher text
);

create table if not exists visitors_books (
    visitor_id uuid,
    book_id uuid,

    constraint pk_visitor_book primary key (visitor_id, book_id)
);