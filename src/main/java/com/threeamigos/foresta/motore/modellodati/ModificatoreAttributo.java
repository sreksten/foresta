package com.threeamigos.foresta.motore.modellodati;

/**
 * Modifica di un certo scostamento (positivo o negativo) il valore base di un attributo di un personaggio
 * @author Stefano Reksten
 */
public class ModificatoreAttributo {

    private final TipoAttributo tipoModificatore;
    private final int valore;

    public ModificatoreAttributo(TipoAttributo tipoModificatore, int valore) {
        this.tipoModificatore = tipoModificatore;
        this.valore = valore;
    }

    public TipoAttributo getTipoModificatoreAttributo() {
        return tipoModificatore;
    }

    public int getValore() {
        return valore;
    }

}
