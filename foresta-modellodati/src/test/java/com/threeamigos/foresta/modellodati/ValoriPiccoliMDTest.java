package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.artefatti.IncantamentoMD;
import com.threeamigos.foresta.effettidistato.EffettoDiStatoMD;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I valori piccoli del modello dati: un effetto di stato che scala col tempo, una notizia, un incantamento senza nome, e il
 * modello dati della partita in corso.
 */
class ValoriPiccoliMDTest {

    @Test
    void unaNotiziaTieneIdentificativoECorpoSenzaSeparatore() {
        NotiziaMD notizia = new NotiziaMD();
        notizia.setId("N|1");
        notizia.setCorpo("Il drago|vola");
        assertEquals("N1", notizia.getId());
        assertEquals("Il dragovola", notizia.getCorpo());
    }

    @Test
    void unIncantamentoSenzaNomeSiChiamaNessunNome() {
        assertEquals(ArtefattoMD.NESSUN_NOME, new IncantamentoMD("  ", TipoDanno.FUOCO, 1, 0.1d).getNomeIncantamento());
        assertEquals(ArtefattoMD.NESSUN_NOME, new IncantamentoMD(null, TipoDanno.FUOCO, 1, 0.1d).getNomeIncantamento());
    }

    @Test
    void ilModelloDatiDellaPartitaSiSostituisce() {
        ModelloDati prima = ModelloDati.getIstanza();
        try {
            ModelloDati nuovo = new ModelloDati();
            ModelloDati.setIstanza(nuovo);
            assertSame(nuovo, ModelloDati.getIstanza());
            assertNotNull(nuovo.getRegistroMissioniMD());
            assertNotNull(nuovo.getIntermezziMD());
        } finally {
            ModelloDati.setIstanza(prima);
        }
    }
}
