package com.threeamigos.foresta.tipi;

/**
 * I valori di bilanciamento delle tattiche di combattimento.
 *
 * @author Stefano Reksten
 */
public final class CostantiTattiche {

    /**
     * L'astuzia di un personaggio per le tattiche (tattiche_di_combattimento_automatiche.md §4): l'intelligenza massima della sua classe per
     * una rampa sul livello, che parte da questa frazione al livello 1 e arriva al pieno al livello 9...
     */
    public static final double ASTUZIA_PARTENZA = 0.4d;
    /**
     * ...salendo di tanto a ogni livello
     */
    public static final double ASTUZIA_PER_LIVELLO = 0.075d;

    /**
     * Dopo quanti tentativi falliti per caso (un Silenzio mancato) una tattica smette di provare la stessa azione su quel bersaglio
     * (tattiche_di_combattimento_automatiche.md §11.7)
     */
    public static final int MAX_TENTATIVI_FALLITI_PER_AZIONE = 2;

    private CostantiTattiche() {
    }
}
