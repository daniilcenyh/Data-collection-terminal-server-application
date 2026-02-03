--liquibase formatted sql

--changeset YourName:create-barcodes-table
--comment create table barcodes for storing product barcodes synchronized from external API
create table barcodes
(
    id              bigserial primary key,
    ware_1c_code    varchar(100) not null,
    barcode         varchar(255) not null unique,
    last_updated    timestamp without time zone,
    created_at      timestamp without time zone default current_timestamp,
    updated_at      timestamp without time zone default current_timestamp,
    constraint barcodes__wares__fk
        foreign key (ware_1c_code) references wares (code_1c)
            on delete cascade
            on update cascade
);
--rollback drop table barcodes;

--changeset YourName:create-barcodes-indexes
--comment create indexes for barcodes table to optimize queries
create index idx_barcodes_ware_1c_code on barcodes(ware_1c_code);
create index idx_barcodes_barcode on barcodes(barcode);
create index idx_barcodes_last_updated on barcodes(last_updated);
--rollback drop index idx_barcodes_ware_1c_code;
--rollback drop index idx_barcodes_barcode;
--rollback drop index idx_barcodes_last_updated;

--changeset YourName:add-barcodes-table-comments
--comment add comments to barcodes table and columns for better documentation
comment on table barcodes is 'Таблица штрихкодов товаров, синхронизируемых с внешним API';
comment on column barcodes.id is 'Уникальный идентификатор штрихкода';
comment on column barcodes.ware_1c_code is 'Код товара из 1C (связь с wares.code_1c)';
comment on column barcodes.barcode is 'Значение штрихкода';
comment on column barcodes.last_updated is 'Время последнего обновления данных из внешнего API';
comment on column barcodes.created_at is 'Время создания записи в БД';
comment on column barcodes.updated_at is 'Время последнего изменения записи в БД';
--rollback comment on table barcodes is null;
--rollback comment on column barcodes.id is null;
--rollback comment on column barcodes.ware_1c_code is null;
--rollback comment on column barcodes.barcode is null;
--rollback comment on column barcodes.last_updated is null;
--rollback comment on column barcodes.created_at is null;
--rollback comment on column barcodes.updated_at is null;

--changeset YourName:create-barcodes-update-trigger
--comment create trigger to call update_updated_at_column function on row update for barcodes
create trigger update_barcodes_updated_at
    before update on barcodes
    for each row
    execute function update_updated_at_column();
--rollback drop trigger if exists update_barcodes_updated_at on barcodes;