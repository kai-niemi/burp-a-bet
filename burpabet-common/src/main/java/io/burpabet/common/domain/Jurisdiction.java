package io.burpabet.common.domain;

/**
 * Belgium	(BE)	Greece	(EL)	Lithuania	(LT)	Portugal	(PT)
 * Bulgaria	(BG)	Spain	(ES)	Luxembourg	(LU)	Romania	    (RO)
 * Czechia	(CZ)	France	(FR)	Hungary	    (HU)	Slovenia	(SI)
 * Denmark	(DK)	Croatia	(HR)	Malta   	(MT)	Slovakia	(SK)
 * Germany	(DE)	Italy	(IT)	Netherlands	(NL)	Finland 	(FI)
 * Estonia	(EE)	Cyprus	(CY)	Austria 	(AT)	Sweden  	(SE)
 * Ireland	(IE)	Latvia	(LV)	Poland  	(PL)
 */
public enum Jurisdiction {
    BE("west-1", "Belgium"),
    IE("west-1", "Ireland"),
    NL("west-1", "Netherlands"),

    BG("central-1", "Bulgaria"),
    CZ("central-1", "Czechia"),
    DE("central-1", "Denmark"),
    EL("central-1", "Greece"),
    ES("central-1", "Spain"),
    FR("central-1", "France"),
    HR("central-1", "Hungary"),
    IT("central-1", "Italia"),
    CY("central-1", "Cyprus"),
    LU("central-1", "Luxembourg"),
    HU("central-1", "Hungary"),
    MT("central-1", "Malta"),
    AT("central-1", "Austria"),
    PL("central-1", "Poland"),
    PT("central-1", "Portugal"),
    RO("central-1", "Romania"),
    SI("central-1", "Slovenia"),
    SK("central-1", "Slovakia"),

    DK("eu-north", "Denmark"),
    EE("eu-north", "Estonia"),
    LV("eu-north", "Latvia"),
    LT("eu-north", "Lithuania"),
    FI("eu-north", "Finland"),
    SE("eu-north", "Sweden");

    private final String region;

    private final String country;

    Jurisdiction(String region, String country) {
        this.region = region;
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }
}
