package com.threeamigos.foresta.eventi;

/**
 * Notifica un cambiamento nel totale delle pozioni di magia disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class EventoVariazionePozioniMagia extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public EventoVariazionePozioniMagia(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.VARIAZIONE_POZIONI_MAGIA);
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
