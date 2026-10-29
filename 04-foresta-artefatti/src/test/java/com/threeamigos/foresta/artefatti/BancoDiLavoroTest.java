package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il banco di lavoro e l'interfaccia di chi scambia artefatti.
 */
class BancoDiLavoroTest {

    private static Artefatto artefatto() {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(TipoArtefatto.SPADA);
        md.setNome("la prova");
        return Artefatto.di(md);
    }

    @Test
    void ilBancoTieneGliArtefattiAggiunti() {
        BancoDiLavoro banco = new BancoDiLavoro();
        Artefatto uno = artefatto();
        banco.addArtefatto(uno);
        assertEquals(1, banco.getInventario().size());
        assertTrue(banco.getInventario().contains(uno));
    }

    @Test
    void l_inventarioRestituitoENonModificaIlBanco() {
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(artefatto());
        banco.getInventario().clear();
        assertEquals(1, banco.getInventario().size());
    }

    @Test
    void siToglieConIlModelloDatiNonConLIdentitaDellArtefatto() {
        BancoDiLavoro banco = new BancoDiLavoro();
        Artefatto uno = artefatto();
        banco.addArtefatto(uno);
        banco.addArtefatto(artefatto());
        // Un Artefatto ricreato dallo stesso modello dati è lo stesso artefatto
        banco.removeArtefatto(Artefatto.di(uno.getModelloDati()));
        assertEquals(1, banco.getInventario().size());
        assertFalse(banco.getInventario().contains(uno));
    }

    @Test
    void loScambiatoreTrattaTuttoSalvoCheLoDicaDiversamente() {
        assertTrue(new BancoDiLavoro().tratta(artefatto()));
    }
}
