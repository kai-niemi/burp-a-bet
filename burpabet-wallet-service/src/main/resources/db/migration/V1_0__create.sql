-- drop table if exists transaction_item cascade;
-- drop table if exists transaction cascade;
-- drop table if exists account cascade;
-- drop table if exists outbox cascade;

-- drop type if exists account_type;
-- drop type if exists account_class;

create type if not exists account_type as enum ('A', 'L', 'E', 'R', 'C');
create type if not exists account_class as enum ('operator', 'customer');

-- Using inheritance structure in ORM with account_class as discriminator
create table if not exists account
(
    id               uuid           not null default gen_random_uuid(),
    account_class    account_class  not null,
    jurisdiction     varchar(3)     not null,
    foreign_id       uuid           null,
    operator_id      uuid           null,
    balance          decimal(19, 2) not null,
    currency         varchar(3)     not null,
    balance_unit     string as (concat(balance::string, ' ', currency)) virtual,
    name             varchar(512)   not null,
    description      varchar(1024)  null,
    account_type     account_type   not null,
    closed           boolean        not null default false,
    allow_negative   integer        not null default 0,
    inserted_at      timestamptz    not null default clock_timestamp(),
    last_modified_at timestamptz    null,

    primary key (id)
);

comment on column account.foreign_id is 'Foreign entity ID relevant only for customer accounts';
comment on column account.operator_id is 'Foreign entity ID relevant only for customer accounts';

create index on account (foreign_id);

create index on account (jurisdiction, account_class)
    storing (balance, currency, name, description, account_type, closed, allow_negative, inserted_at, last_modified_at);

create table if not exists transaction
(
    id               uuid        not null default gen_random_uuid(),
    booking_date     date        not null default current_date(),
    transfer_date    date        not null default current_date(),
    transaction_type varchar(32) not null,
    jurisdiction     varchar(3)  not null,

    primary key (id)
);

create table if not exists transaction_item
(
    transaction_id       uuid           not null,
    account_id           uuid           not null,
    amount               decimal(19, 2) not null,
    currency             varchar(3)     not null,
    note                 varchar(512),
    running_balance      decimal(19, 2) not null,
    amount_unit          string as (concat(amount::string, ' ', currency)) virtual,
    running_balance_unit string as (concat(running_balance::string, ' ', currency)) virtual,
    jurisdiction         varchar(3)     not null,

    primary key (transaction_id, account_id)
);

------------------------------------------------
-- Constraints on account
------------------------------------------------

alter table if exists account
    add constraint if not exists check_account_allow_negative check (allow_negative between 0 and 1);
alter table if exists account
    add constraint if not exists check_account_positive_balance check (balance * abs(allow_negative - 1) >= 0);

alter table if exists account
    add constraint if not exists fk_account_ref_account
    foreign key (operator_id) references account (id);

------------------------------------------------
-- Constraints on transaction_item
------------------------------------------------

alter table if exists transaction_item
    add constraint if not exists fk_region_ref_transaction
    foreign key (transaction_id) references transaction (id);

alter table if exists transaction_item
    add constraint if not exists fk_region_ref_account
    foreign key (account_id) references account (id);

