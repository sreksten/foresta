package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;

/**
 * L'incantesimo di Gelo: un incantesimo malefico di danno di Gelo (vedi {@link IncantesimoMaleficoImpl}). Costo e danni stanno nelle costanti {@code INCANTESIMO_GELO_*}.
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
