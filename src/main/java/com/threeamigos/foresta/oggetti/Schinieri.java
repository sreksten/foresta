package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Loot a sé, come spada e scudo.
 * (vedi TipoOggetto.ELMO e l'elenco degli oggetti del Bosco).
 *
 */
public class Schinieri extends OggettoArtefatto {

    public Schinieri() {
        super(TipoArtefatto.SCHINIERI);
    }

    public String getAIS() {
        return Misc.UN_PAIO_DI;
    }

    public String getAIP() {
        return Misc.ALCUNE_PAIA_DI;
    }

    public String getADS() {
        return Misc.GLI;
    }

    public String getADP() {
        return Misc.GLI;
    }

    public String getNomeSingolare() {
        return "schinieri";
    }

    public String getNomePlurale() {
        return "schinieri";
    }

    public TipoOggetto getClasse() {
        return TipoOggetto.SCHINIERI;
    }
}
