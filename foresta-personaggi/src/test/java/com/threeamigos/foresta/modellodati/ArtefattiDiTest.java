package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;

/**
 * Gli artefatti che servono ai test del modello dati.
 */
final class ArtefattiDiTest {

    static ArtefattoMD creaArtefatto(TipoArtefatto tipo, String nome, String descrizione) {
        ArtefattoMD artefatto = new ArtefattoMD();
        artefatto.setTipo(tipo);
        artefatto.setNome(nome);
        artefatto.setDescrizione(descrizione);
        artefatto.setLivello(3);
        artefatto.setDanni(7);
        artefatto.setCostoAcquisto(20);
        artefatto.setPeso(1.5);
        return artefatto;
    }

    private ArtefattiDiTest() {
    }
}
