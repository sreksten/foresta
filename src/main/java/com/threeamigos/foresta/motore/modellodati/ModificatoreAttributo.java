package com.threeamigos.foresta.motore.modellodati;

import java.util.Objects;

/**
 * Modifica di un certo scostamento (positivo o negativo) il valore base di un attributo di un personaggio
 * @author Stefano Reksten
 */
public class ModificatoreAttributo {

    private final TipoAttributo tipoAttributo;
    private final TipoModificatore tipoModificatore;
    private final double quantita;
    private final String note;

    public ModificatoreAttributo(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
                                 double quantita, String note) {
        this.tipoAttributo = tipoAttributo;
        this.tipoModificatore = tipoModificatore;
        this.quantita = quantita;
        this.note = Serializzabile.senzaPipe(note);
    }

    public ModificatoreAttributo(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
                                 double quantita) {
        this(tipoAttributo, tipoModificatore, quantita, "");
    }

    public TipoAttributo getTipoAttributo() {
        return tipoAttributo;
    }

    public TipoModificatore getTipoModificatoreAttributo() {
        return tipoModificatore;
    }

    public double getQuantita() {
        return quantita;
    }

    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ModificatoreAttributo)) {
            return false;
        }
        ModificatoreAttributo other = (ModificatoreAttributo) obj;
        return tipoAttributo == other.tipoAttributo && tipoModificatore == other.tipoModificatore
                && quantita == other.quantita && Objects.equals(note, other.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoAttributo, tipoModificatore, quantita, note);
    }
}
