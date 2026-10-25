package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * L'incantesimo di Gelo: un incantesimo malefico di danno di Gelo (vedi {@link IncantesimoMaleficoImpl}). Costo e danni stanno nelle costanti {@code INCANTESIMO_GELO_*}.
 *
 * @author Stefano Reksten
 */
public class Gelo extends IncantesimoMaleficoImpl implements Incantesimo {

    public Gelo(int livello) {
        super(livello);
    }

    public TipoIncantesimo getClasse() {
        return TipoIncantesimo.GELO;
    }

    @Override
    public TipoDanno getTipoDanno() {
        return TipoDanno.GELO;
    }

    public int getDanni() {
        return CostantiIncantesimi.INCANTESIMO_GELO_DANNI;
    }
}
