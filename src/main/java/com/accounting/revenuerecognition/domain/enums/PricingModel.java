package com.accounting.revenuerecognition.domain.enums;

/**
 * Defines the contract pricing model governing revenue recognition calculations.
 */
public enum PricingModel {
    /**
     * Fixed total contract price recognized upon delivery of agreed milestones
     * or percentage-of-completion schedules.
     */
    FIXED_PRICE,

    /**
     * Revenue recognized based on billable hours logged at agreed rates
     * plus pass-through materials and expenses.
     */
    TIME_AND_MATERIALS,

    /**
     * Revenue recognized based on all actual incurred costs (labor + materials)
     * plus a predefined contract markup percentage.
     */
    COST_PLUS
}
