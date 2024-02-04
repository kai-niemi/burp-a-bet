package io.burpabet.common.domain;

public abstract class TopicNames {
    /**
     * Topic name for customer registration events
     */
    public static final String REGISTRATION = "registration";

    /**
     * Topic name for customer bet placement events
     */
    public static final String PLACEMENT = "placement";

    /**
     * Topic name for customer bet settlement events
     */
    public static final String SETTLEMENT = "settlement";

    public static final String WALLET_REGISTRATION = "wallet-" + REGISTRATION;

    public static final String WALLET_PLACEMENT = "wallet-" + PLACEMENT;

    public static final String WALLET_SETTLEMENT = "wallet-" + SETTLEMENT;

    public static final String CUSTOMER_PLACEMENT = "customer-" + PLACEMENT;

    public static final String CUSTOMER_SETTLEMENT = "customer-" + SETTLEMENT;

    public static final String BETTING_REGISTRATION = "betting-" + REGISTRATION;

    private TopicNames() {
    }
}
