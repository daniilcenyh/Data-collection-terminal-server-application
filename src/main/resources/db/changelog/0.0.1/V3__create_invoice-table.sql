--liquibase formatted sql

--changeset YourName:create-invoices-table
--comment create table invoices for storing invoice documents
create table invoices
(
    id              uuid primary key default gen_random_uuid(),
    invoice_number  bigserial unique not null,
    status          varchar(50) not null default 'DRAFT',
    total_amount    integer default 0,
    items_count     integer default 0,
    sent_at         timestamp without time zone,
    last_updated    timestamp without time zone,
    created_at      timestamp without time zone default current_timestamp,
    updated_at      timestamp without time zone default current_timestamp
);
--rollback drop table invoices;

--changeset YourName:create-invoices-indexes
--comment create indexes for invoices table
create index idx_invoices_invoice_number on invoices(invoice_number);
create index idx_invoices_status on invoices(status);
create index idx_invoices_created_at on invoices(created_at);
--rollback drop index idx_invoices_invoice_number;
--rollback drop index idx_invoices_status;
--rollback drop index idx_invoices_created_at;

--changeset YourName:create-product-items-table
--comment create table product_items for storing invoice line items
create table product_items
(
    id              uuid primary key default gen_random_uuid(),
    invoice_id      uuid not null,
    ware_id         bigint not null,
    barcode_id      bigint not null,
    quantity        integer not null default 1,
    unit_price      integer not null,
    total_price     integer not null,
    last_updated    timestamp without time zone,
    created_at      timestamp without time zone default current_timestamp,
    updated_at      timestamp without time zone default current_timestamp,
    constraint product_items__invoices__fk
        foreign key (invoice_id) references invoices (id)
            on delete cascade,
    constraint product_items__wares__fk
        foreign key (ware_id) references wares (id)
            on delete restrict,
    constraint product_items__barcodes__fk
        foreign key (barcode_id) references barcodes (id)
            on delete restrict,
    constraint product_items_quantity_positive
        check (quantity > 0),
    constraint product_items_unit_price_positive
        check (unit_price >= 0)
);
--rollback drop table product_items;

--changeset YourName:create-product-items-indexes
--comment create indexes for product_items table
create index idx_product_items_invoice_id on product_items(invoice_id);
create index idx_product_items_ware_id on product_items(ware_id);
create index idx_product_items_barcode_id on product_items(barcode_id);
--rollback drop index idx_product_items_invoice_id;
--rollback drop index idx_product_items_ware_id;
--rollback drop index idx_product_items_barcode_id;

--changeset YourName:add-invoices-table-comments
--comment add comments to invoices and product_items tables
comment on table invoices is 'Таблица накладных';
comment on column invoices.id is 'Уникальный идентификатор накладной (UUID)';
comment on column invoices.invoice_number is 'Номер накладной (автоинкремент)';
comment on column invoices.status is 'Статус накладной (DRAFT, SENT, ERROR)';
comment on column invoices.total_amount is 'Общая сумма накладной';
comment on column invoices.items_count is 'Количество позиций в накладной';
comment on column invoices.sent_at is 'Время отправки в 1С';

comment on table product_items is 'Таблица позиций накладной';
comment on column product_items.id is 'Уникальный идентификатор позиции';
comment on column product_items.invoice_id is 'ID накладной';
comment on column product_items.ware_id is 'ID товара';
comment on column product_items.barcode_id is 'ID штрихкода';
comment on column product_items.quantity is 'Количество';
comment on column product_items.unit_price is 'Цена за единицу';
comment on column product_items.total_price is 'Общая цена (quantity * unit_price)';
--rollback comment on table invoices is null;
--rollback comment on table product_items is null;

--changeset YourName:create-invoices-update-trigger
--comment create trigger for invoices table
create trigger update_invoices_updated_at
    before update on invoices
    for each row
    execute function update_updated_at_column();
--rollback drop trigger if exists update_invoices_updated_at on invoices;

--changeset YourName:create-product-items-update-trigger
--comment create trigger for product_items table
create trigger update_product_items_updated_at
    before update on product_items
    for each row
    execute function update_updated_at_column();
--rollback drop trigger if exists update_product_items_updated_at on product_items;

--changeset YourName:create-update-invoice-totals-function splitStatements:false
--comment create function to automatically update invoice totals
create or replace function update_invoice_totals()
returns trigger as $$
begin
    update invoices
    set total_amount = (
        select coalesce(sum(total_price), 0)
        from product_items
        where invoice_id = coalesce(new.invoice_id, old.invoice_id)
    ),
    items_count = (
        select count(*)
        from product_items
        where invoice_id = coalesce(new.invoice_id, old.invoice_id)
    )
    where id = coalesce(new.invoice_id, old.invoice_id);
    return coalesce(new, old);
end;
$$ language plpgsql;
--rollback drop function if exists update_invoice_totals();

--changeset YourName:create-product-items-update-totals-trigger
--comment create trigger to update invoice totals when product items change
create trigger update_invoice_totals_on_product_item_change
    after insert or update or delete on product_items
    for each row
    execute function update_invoice_totals();
--rollback drop trigger if exists update_invoice_totals_on_product_item_change on product_items;