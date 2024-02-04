ALTER DATABASE burp_customer PRIMARY REGION "eu-central-1";
ALTER DATABASE burp_customer ADD REGION "eu-west-1";
ALTER DATABASE burp_customer ADD REGION "eu-north-1";

SET enable_multiregion_placement_policy=on;
ALTER DATABASE burp_customer PLACEMENT RESTRICTED;

ALTER TABLE customer ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN jurisdiction IN ('BE','IE','NL') THEN 'eu-west-1'
        WHEN jurisdiction IN ('BG','CZ','DE','EL','ES','FR','HR','IT','CY','LU','HU','MT','AT','PL','PT','RO','SI','SK') THEN 'eu-central-1'
        WHEN jurisdiction IN ('DK','EE','LV','LT','FI','SE') THEN 'eu-north-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE customer SET LOCALITY REGIONAL BY ROW AS region;
