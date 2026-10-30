package com.threeamigos.foresta.eventi.richieste;

/**
 * Quanto manca a riavere un'abilità innata o un canto appena usato: i turni che restano e quelli che la ricarica dura in tutto. La UI ci disegna
 * l'icona che "si riempie" e non cliccabile.
 *
 * @author Stefano Reksten
 */
public final class RicaricaDelComando {

    private final int turniRimasti;
    private final int durata;

    public RicaricaDelComando(int turniRimasti, int durata) {
        this.turniRimasti = turniRimasti;
        this.durata = durata;
    }

    /**
     * I turni che mancano a riavere il comando (almeno uno: a zero è pronto, e non è più in ricarica)
     */
    public int getTurniRimasti() {
        return turniRimasti;
    }

    /**
     * I turni che dura in tutto la ricarica di quel comando
     */
    public int getDurata() {
        return durata;
    }
}
