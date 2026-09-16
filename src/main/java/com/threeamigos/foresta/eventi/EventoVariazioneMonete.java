package com.threeamigos.foresta.eventi;

/**
 * Notifica un cambiamento nel totale delle monete disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneMonete extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public EventoVariazioneMonete(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.VARIAZIONE_MONETE);
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
