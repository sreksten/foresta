package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoDanno;
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

    public int getCostoAcquisto() {
        return Costanti.INCANTESIMO_VELENO_COSTO_ACQUISTO;
    }

    public int getCostoLancio() {
        return Costanti.INCANTESIMO_VELENO_COSTO_LANCIO;
    }

    @Override
    public TipoDanno getTipoDanno() {
        return TipoDanno.VELENO;
    }

    public int getDanni() {
        return Costanti.INCANTESIMO_VELENO_DANNI;
    }
}
