package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * L'incantesimo di Veleno: un incantesimo malefico di danno di Veleno (vedi {@link IncantesimoMaleficoImpl}). Costo e danni stanno nelle costanti {@code INCANTESIMO_VELENO_*}.
 *
 * @author Stefano Reksten
 */
public class Veleno extends IncantesimoMaleficoImpl implements Incantesimo {

    public Veleno(int livello) {
        super(livello);
    }

    public TipoIncantesimo getClasse() {
        return TipoIncantesimo.VELENO;
    }

    @Override
    public TipoDanno getTipoDanno() {
        return TipoDanno.VELENO;
    }

    public int getDanni() {
        return CostantiIncantesimi.INCANTESIMO_VELENO_DANNI;
    }
}
