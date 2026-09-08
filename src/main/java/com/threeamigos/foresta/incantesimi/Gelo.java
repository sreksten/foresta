package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

/**
 *
 * @author Stefano Reksten
 */
public class Gelo extends IncantesimoMaleficoImpl implements Incantesimo {

    public Gelo(int livello) {
        super(livello);
    }

    public ClasseIncantesimo getClasse() {
        return ClasseIncantesimo.GELO;
    }

    public int getCostoLancio() {
        return Costanti.INCANTESIMO_GELO_COSTO_LANCIO;
    }

    @Override
    public TipoDanno getTipoDanno() {
        return TipoDanno.GELO;
    }

    public int getDanni() {
        return Costanti.INCANTESIMO_GELO_DANNI;
    }
}
