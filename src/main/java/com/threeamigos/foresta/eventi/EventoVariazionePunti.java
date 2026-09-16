package com.threeamigos.foresta.eventi;

/**
 * Notifica un cambiamento nel totale dei punti fatti durante il corso del gioco. Molto old school.
 *
 * @author Stefano Reksten
 */
public class EventoVariazionePunti extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public EventoVariazionePunti(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.VARIAZIONE_PUNTI);
        this.valorePrecedente = valorePrecedente;
        this.nuovoValore = nuovoValore;
    }

    public int getValorePrecedente() {
        return valorePrecedente;
    }

    public int getNuovoValore() {
        return nuovoValore;
    }
}
