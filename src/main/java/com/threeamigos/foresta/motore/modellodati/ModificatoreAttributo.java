package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public class ModificatoreAttributo {

    private final TipoModificatoreAttributo tipoModificatore;
    private final int valore;

    public ModificatoreAttributo(TipoModificatoreAttributo tipoModificatore, int valore) {
        this.tipoModificatore = tipoModificatore;
        this.valore = valore;
    }

    public TipoModificatoreAttributo getTipoModificatoreAttributo() {
        return tipoModificatore;
    }

    public int getValore() {
        return valore;
    }

}
