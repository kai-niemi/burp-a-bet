truncate table account cascade ;
truncate table transaction cascade ;
truncate table transaction_item cascade ;

-- CANCEL JOBS (SELECT job_id FROM [SHOW JOBS] where job_type='CHANGEFEED' and status='running')
