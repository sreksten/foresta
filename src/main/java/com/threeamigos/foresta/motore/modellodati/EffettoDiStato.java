package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public class EffettoDiStato {

    private final TipoEffettoDiStato tipoEffettoDiStato;
    private int valore;

    public EffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int valore) {
        this.tipoEffettoDiStato = tipoEffettoDiStato;
        this.valore = valore;
    }

    public TipoEffettoDiStato getTipoEffettoDiStato() {
        return tipoEffettoDiStato;
    }

    public int getValore() {
        return valore;
    }

    public void setValore(int valore) {
    	this.valore = valore;
    }
}
