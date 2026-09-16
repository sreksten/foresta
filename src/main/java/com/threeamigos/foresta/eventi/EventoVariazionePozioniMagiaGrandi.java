package com.threeamigos.foresta.eventi;

/**
 * Notifica un cambiamento nel totale delle pozioni di magia grandi disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class EventoVariazionePozioniMagiaGrandi extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public EventoVariazionePozioniMagiaGrandi(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.VARIAZIONE_POZIONI_MAGIA_GRANDI);
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
