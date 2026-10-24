package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.Costanti;
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
