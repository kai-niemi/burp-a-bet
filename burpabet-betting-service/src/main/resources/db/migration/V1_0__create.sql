-- drop type if exists bet_type;
-- drop type if exists race_outcome;

create type if not exists bet_type as enum ('win', 'each_way');
create type if not exists race_outcome as enum ('win', 'lose');

-- drop table if exists race cascade;

create table if not exists race
(
    id         uuid primary key not null default gen_random_uuid(),
    event_date date             not null default current_date(),
    track      varchar(64)      not null,
    horse      varchar(64)      not null,
    odds       float            not null,
    outcome    race_outcome     null
);

-- drop table if exists bet cascade;

create table if not exists bet
(
    id                uuid primary key not null default gen_random_uuid(),
    race_id           uuid             not null,
    customer_id       uuid             not null,
    customer_name     varchar(128)     null,
    jurisdiction      varchar(3)       null,
    stake             decimal(19, 2)   not null,
    stake_currency    varchar(3)       not null,
    bet_type          bet_type         not null,
    placement_status  varchar(32)      not null,
    settlement_status varchar(32)      null,
    settled           bool             null     default false,
    payout            decimal(19, 2)   null,
    payout_currency   varchar(3)       null,
    inserted_at       timestamptz      not null default clock_timestamp(),
    last_modified_at  timestamptz      null
);

alter table if exists bet
    add constraint if not exists fk_bet_ref_race
    foreign key (race_id) references race (id);

create table if not exists key_log
(
    id            uuid primary key not null,
    time_received timestamptz      not null
);

alter table key_log set (ttl_expire_after = '1 hour');
