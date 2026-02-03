--liquibase formatted sql

--changeset YourName:create-wares-table
--comment create table wares for storing products synchronized from external API
create table wares
(
    id           bigserial primary key,
    title        varchar(500) not null,
    code_1c      varchar(100) not null unique,
    is_weighty   boolean default false,
    price        integer,
    last_updated timestamp without time zone,
    created_at   timestamp without time zone default current_timestamp,
    updated_at   timestamp without time zone default current_timestamp
);
--rollback drop table wares;

--changeset YourName:create-wares-indexes
--comment create indexes for wares table to optimize queries
create index idx_wares_code_1c on wares(code_1c);
create index idx_wares_last_updated on wares(last_updated);
create index idx_wares_price on wares(price);
create index idx_wares_is_weighty on wares(is_weighty);
--rollback drop index idx_wares_code_1c;
--rollback drop index idx_wares_last_updated;
--rollback drop index idx_wares_price;
--rollback drop index idx_wares_is_weighty;

--changeset YourName:add-wares-table-comments
--comment add comments to wares table and columns for better documentation
comment on table wares is 'Таблица товаров, синхронизируемых с внешним API';
comment on column wares.id is 'Уникальный идентификатор товара';
comment on column wares.title is 'Название товара';
comment on column wares.code_1c is 'Уникальный код товара из 1C';
comment on column wares.is_weighty is 'Флаг весового товара';
comment on column wares.price is 'Цена товара в копейках';
comment on column wares.last_updated is 'Время последнего обновления данных из внешнего API';
comment on column wares.created_at is 'Время создания записи в БД';
comment on column wares.updated_at is 'Время последнего изменения записи в БД';
--rollback comment on table wares is null;
--rollback comment on column wares.id is null;
--rollback comment on column wares.title is null;
--rollback comment on column wares.code_1c is null;
--rollback comment on column wares.is_weighty is null;
--rollback comment on column wares.price is null;
--rollback comment on column wares.last_updated is null;
--rollback comment on column wares.created_at is null;
--rollback comment on column wares.updated_at is null;

--changeset YourName:create-update-timestamp-function splitStatements:false
--comment create function to automatically update updated_at timestamp
create or replace function update_updated_at_column()
returns trigger as $$
begin
    new.updated_at = current_timestamp;
    return new;
end;
$$ language plpgsql;
--rollback drop function if exists update_updated_at_column();

--changeset YourName:create-wares-update-trigger
--comment create trigger to call update_updated_at_column function on row update
create trigger update_wares_updated_at
    before update on wares
    for each row
    execute function update_updated_at_column();
--rollback drop trigger if exists update_wares_updated_at on wares;