package com.threeamigos.foresta.motore;

/**
 * Controls how {@link GrammarBean} picks an alternative among a production's children.
 * @author Stefano Reksten
 */
public enum ProductionModeEnum {

    /**
     * Always pick the first alternative.
     */
    FIRST,
    /**
     * Always pick the last alternative.
     */
    LAST,
    /**
     * Pick a random alternative.
     */
    RANDOM

}
