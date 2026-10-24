package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Una falce del bottino: un oggetto che diventa un artefatto di tipo falce, un'arma a due mani (vedi {@link OggettoArtefatto}).
 *
 * @author Stefano Reksten
 */
public class Falce extends OggettoArtefatto {

    public Falce() {
        super(TipoArtefatto.FALCE);
    }

    public String getAIS() {
        return Misc.UNA;
    }

    public String getAIP() {
        return Misc.ALCUNE;
    }

    public String getADS() {
        return Misc.LA;
    }

    public String getADP() {
        return Misc.LE;
    }

    public String getNomeSingolare() {
        return "falce";
    }

    public String getNomePlurale() {
        return "falci";
    }

    public TipoOggetto getClasse() {
        return TipoOggetto.FALCE;
    }
}
