-- Set primary region to where kafka runs
ALTER DATABASE burp_wallet PRIMARY REGION "aws-eu-north-1";
ALTER DATABASE burp_wallet ADD REGION "aws-eu-central-1";
ALTER DATABASE burp_wallet ADD REGION "aws-eu-west-1";

SET enable_multiregion_placement_policy=on;
ALTER DATABASE burp_wallet PLACEMENT RESTRICTED;

-- Belgium	(BE)	Greece	(EL)	Lithuania	(LT)	Portugal	(PT)
-- Bulgaria	(BG)	Spain	(ES)	Luxembourg	(LU)	Romania	    (RO)
-- Czechia	(CZ)	France	(FR)	Hungary	    (HU)	Slovenia	(SI)
-- Denmark	(DK)	Croatia	(HR)	Malta   	(MT)	Slovakia	(SK)
-- Germany	(DE)	Italy	(IT)	Netherlands	(NL)	Finland 	(FI)
-- Estonia	(EE)	Cyprus	(CY)	Austria 	(AT)	Sweden  	(SE)
-- Ireland	(IE)	Latvia	(LV)	Poland  	(PL)

-- show create table account;
-- alter table account set locality regional;
-- set sql_safe_updates = false;
-- alter table account drop column crdb_region;
-- select crdb_internal.locality_value('region');
-- select id,region from account limit 1;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN jurisdiction IN ('BE','IE','NL') THEN 'aws-eu-west-1'
        WHEN jurisdiction IN ('BG','CZ','DE','EL','ES','FR','HR','IT','CY','LU','HU','MT','AT','PL','PT','RO','SI','SK') THEN 'aws-eu-central-1'
        WHEN jurisdiction IN ('DK','EE','LV','LT','FI','SE') THEN 'aws-eu-north-1'
        ELSE 'aws-eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN jurisdiction IN ('BE','IE','NL') THEN 'aws-eu-west-1'
        WHEN jurisdiction IN ('BG','CZ','DE','EL','ES','FR','HR','IT','CY','LU','HU','MT','AT','PL','PT','RO','SI','SK') THEN 'aws-eu-central-1'
        WHEN jurisdiction IN ('DK','EE','LV','LT','FI','SE') THEN 'aws-eu-north-1'
        ELSE 'aws-eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN jurisdiction IN ('BE','IE','NL') THEN 'aws-eu-west-1'
        WHEN jurisdiction IN ('BG','CZ','DE','EL','ES','FR','HR','IT','CY','LU','HU','MT','AT','PL','PT','RO','SI','SK') THEN 'aws-eu-central-1'
        WHEN jurisdiction IN ('DK','EE','LV','LT','FI','SE') THEN 'aws-eu-north-1'
        ELSE 'aws-eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;
