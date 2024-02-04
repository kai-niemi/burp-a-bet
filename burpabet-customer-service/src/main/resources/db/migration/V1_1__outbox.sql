create table if not exists outbox
(
    id             uuid as ((payload ->> 'eventId')::UUID) stored,
    aggregate_id   uuid as ((payload ->> 'entityId')::UUID) stored,
    aggregate_type varchar(32) not null,
    payload        jsonb       not null,

    primary key (id)
);

alter table outbox set (ttl_expire_after = '1 hour');

-- CANCEL JOBS (SELECT job_id FROM [SHOW JOBS] where job_type='CHANGEFEED' and status='running');

create changefeed into '${cdc-sink-url}?topic_name=registration'
with diff
         as select id           as event_id,
                   aggregate_id as aggregate_id,
                   event_op()   as event_type,
                   payload
            from outbox
            where event_op() != 'delete'
              and aggregate_type = 'registration';

create changefeed into '${cdc-sink-url}?topic_name=customer-placement'
with diff
         as select id           as event_id,
                   aggregate_id as aggregate_id,
                   event_op()   as event_type,
                   payload
            from outbox
            where event_op() != 'delete'
              and aggregate_type = 'placement';

create changefeed into '${cdc-sink-url}?topic_name=customer-settlement'
with diff
         as select id           as event_id,
                   aggregate_id as aggregate_id,
                   event_op()   as event_type,
                   payload
            from outbox
            where event_op() != 'delete'
              and aggregate_type = 'settlement';
