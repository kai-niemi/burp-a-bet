ALTER DATABASE burp_betting PRIMARY REGION "eu-central-1";
ALTER DATABASE burp_betting ADD REGION "eu-west-1";
ALTER DATABASE burp_betting ADD REGION "eu-north-1";

SET enable_multiregion_placement_policy=on;
ALTER DATABASE burp_betting PLACEMENT RESTRICTED;

ALTER TABLE bet ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN jurisdiction IN ('BE','IE','NL') THEN 'eu-west-1'
        WHEN jurisdiction IN ('BG','CZ','DE','EL','ES','FR','HR','IT','CY','LU','HU','MT','AT','PL','PT','RO','SI','SK') THEN 'eu-central-1'
        WHEN jurisdiction IN ('DK','EE','LV','LT','FI','SE') THEN 'eu-north-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE bet SET LOCALITY REGIONAL BY ROW AS region;
