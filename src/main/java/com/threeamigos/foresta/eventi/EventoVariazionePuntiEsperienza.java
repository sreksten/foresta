package com.threeamigos.foresta.eventi;

/**
 * Notifica un cambiamento nel totale dei punti esperienza del personaggio principale.
 * Agisce anche sul livello del mondo.
 *
 * @author Stefano Reksten
 */
public class EventoVariazionePuntiEsperienza extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public EventoVariazionePuntiEsperienza(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.VARIAZIONE_PUNTI_ESPERIENZA);
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
