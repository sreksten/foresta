package com.threeamigos.foresta.eventi;

/**
 * Notifica un cambiamento nel totale delle pozioni di salute disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class EventoVariazionePozioniSalute extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public EventoVariazionePozioniSalute(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.VARIAZIONE_POZIONI_SALUTE);
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
