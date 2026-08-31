package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public class EffettoDiStato {

    private final TipoEffettoDiStato tipoEffettoDiStato;
    private final int valore;

    public EffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int valore) {
        this.tipoEffettoDiStato = tipoEffettoDiStato;
        this.valore = valore;
    }

    public TipoEffettoDiStato getTipoModificatoreAttributo() {
        return tipoEffettoDiStato;
    }

    public int getValore() {
        return valore;
    }
}
