-- Set primary region to where kafka runs
ALTER DATABASE customer PRIMARY REGION "aws-eu-north-1";
ALTER DATABASE customer ADD REGION "aws-eu-central-1";
ALTER DATABASE customer ADD REGION "aws-eu-west-1";

SET enable_multiregion_placement_policy=on;
ALTER DATABASE customer PLACEMENT RESTRICTED;

ALTER TABLE customer ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN jurisdiction IN ('BE','IE','NL') THEN 'aws-eu-west-1'
        WHEN jurisdiction IN ('BG','CZ','DE','EL','ES','FR','HR','IT','CY','LU','HU','MT','AT','PL','PT','RO','SI','SK') THEN 'aws-eu-central-1'
        WHEN jurisdiction IN ('DK','EE','LV','LT','FI','SE') THEN 'aws-eu-north-1'
        ELSE 'aws-eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE customer SET LOCALITY REGIONAL BY ROW AS region;
