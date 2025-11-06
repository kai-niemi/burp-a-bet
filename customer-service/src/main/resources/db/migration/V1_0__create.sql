-- drop table if exists customer cascade;

create table if not exists customer
(
    id                 uuid                not null default gen_random_uuid(),
    operator_id        uuid                null,
    email              varchar(256) unique not null,
    name               varchar(128)        not null,
    jurisdiction       varchar(3)          not null,
    status             varchar(32)         not null,
    spending_budget_pm decimal(19, 2)      null,

    primary key (id)
);
