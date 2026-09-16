package com.threeamigos.foresta.eventi;

/**
 * Notifica un cambiamento nel totale delle gemme presenti nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneGemme extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public EventoVariazioneGemme(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.VARIAZIONE_GEMME);
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
