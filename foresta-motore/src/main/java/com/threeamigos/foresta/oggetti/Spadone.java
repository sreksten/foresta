package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Uno spadone del bottino: un oggetto che diventa un artefatto di tipo spadone, un'arma a due mani (vedi {@link OggettoArtefatto}).
 *
 * @author Stefano Reksten
 */
public class Spadone extends OggettoArtefatto {

    public Spadone() {
        super(TipoArtefatto.SPADONE);
    }

    public String getAIS() {
        return Articoli.UNO;
    }

    public String getAIP() {
        return Articoli.ALCUNI;
    }

    public String getADS() {
        return Articoli.LO;
    }

    public String getADP() {
        return Articoli.GLI;
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
