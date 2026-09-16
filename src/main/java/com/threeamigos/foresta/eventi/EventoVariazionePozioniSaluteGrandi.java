package com.threeamigos.foresta.eventi;

/**
 * Notifica un cambiamento nel totale delle pozioni di salute grandi disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class EventoVariazionePozioniSaluteGrandi extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public EventoVariazionePozioniSaluteGrandi(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.VARIAZIONE_POZIONI_SALUTE_GRANDI);
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
