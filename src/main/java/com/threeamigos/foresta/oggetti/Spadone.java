package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tools.Misc;

/**
 *
 * @author Stefano Reksten
 */
public class Spadone extends OggettoArtefatto {

    public Spadone() {
        super(TipoArtefatto.SPADONE);
    }

    public String getAIS() {
        return Misc.UNO;
    }

    public String getAIP() {
        return Misc.ALCUNI;
    }

    public String getADS() {
        return Misc.LO;
    }

    public String getADP() {
        return Misc.GLI;
    }

    public String getNomeSingolare() {
        return "spadone";
    }

    public String getNomePlurale() {
        return "spadoni";
    }

    public TipoOggetto getClasse() {
        return TipoOggetto.SPADONE;
    }
}
