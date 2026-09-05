package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.TipoRiposo;

/**
 *
 * @author Stefano Reksten
 */
public class RisultatoRiposo {

    private final int tempoRiposo;
    private final TipoRiposo tipoRiposo;
    private final int ripristinoSalute;
    private final int ripristinoMagia;
    private final int abbassamentoStanchezza;

    public RisultatoRiposo(int tempoRiposo, TipoRiposo tipoRiposo, int ripristinoSalute, int ripristinoMagia, int abbassamentoStanchezza) {
        this.tempoRiposo = tempoRiposo;
        this.tipoRiposo = tipoRiposo;
        this.ripristinoSalute = ripristinoSalute;
        this.ripristinoMagia = ripristinoMagia;
        this.abbassamentoStanchezza = abbassamentoStanchezza;
    }

    public int getTempoRiposo() {
        return tempoRiposo;
    }

    public TipoRiposo getTipoRiposo() {
        return tipoRiposo;
    }

    public int getRipristinoSalute() {
        return ripristinoSalute;
    }

    public int getRipristinoMagia() {
        return ripristinoMagia;
    }

    public int getAbbassamentoStanchezza() {
        return abbassamentoStanchezza;
    }
}
