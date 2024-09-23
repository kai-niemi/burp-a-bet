-- From a performance standpoint there should be one outbox table per aggregate that
-- will reduce the number of change feeds per range. In this demo however, there's
-- only one outbox table with a discriminator column for the aggregate type.

create table if not exists outbox
(
    id             uuid as ((payload ->> 'eventId')::UUID) stored,
    aggregate_id   uuid as ((payload ->> 'entityId')::UUID) stored,
    aggregate_type varchar(32) not null,
    payload        jsonb       not null,

    primary key (id)
);

alter table outbox set (ttl_expire_after = '1 hour');

-- Request topic for placement journey
create changefeed into '${cdc-sink-url}?topic_name=placement'
with diff
         as select id           as event_id,
                   aggregate_id as aggregate_id,
                   event_op()   as event_type,
                   payload
            from outbox
            where event_op() != 'delete'
              and aggregate_type = 'placement';

-- Request topic for settlement journey
create changefeed into '${cdc-sink-url}?topic_name=settlement'
with diff
         as select id           as event_id,
                   aggregate_id as aggregate_id,
                   event_op()   as event_type,
                   payload
            from outbox
            where event_op() != 'delete'
              and aggregate_type = 'settlement';

-- Response topic for registration journey
create changefeed into '${cdc-sink-url}?topic_name=betting-registration'
with diff
         as select id           as event_id,
                   aggregate_id as aggregate_id,
                   event_op()   as event_type,
                   payload
            from outbox
            where event_op() != 'delete'
              and aggregate_type = 'registration';

