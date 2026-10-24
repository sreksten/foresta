package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

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
        return Articoli.UNA;
    }

    public String getAIP() {
        return Articoli.ALCUNE;
    }

    public String getADS() {
        return Articoli.LA;
    }

    public String getADP() {
        return Articoli.LE;
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
