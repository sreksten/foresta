package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

/**
 *
 * @author Stefano Reksten
 */
public class Veleno extends IncantesimoMaleficoImpl implements Incantesimo {

    public Veleno(int livello) {
        super(livello);
    }

    public ClasseIncantesimo getClasse() {
        return ClasseIncantesimo.VELENO;
    }

    public PortataIncantesimo getPortata() {
        return PortataIncantesimo.GRUPPO;
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
