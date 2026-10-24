package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

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
        return Articoli.UN_PAIO_DI;
    }

    public String getAIP() {
        return Articoli.ALCUNE_PAIA_DI;
    }

    public String getADS() {
        return Articoli.GLI;
    }

    public String getADP() {
        return Articoli.GLI;
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
