package com.threeamigos.foresta.artefatti.leggendari;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OggettoLeggendarioTest {

    static final String SCUDO = "TIPO=SCUDO;NOME=lo Scudo Fiscale;CHIAVE=SCUDO_X;BREVE=lo Scudo Fiscale;DESCRIZIONE=che para;"
            + "LIVELLO=5;COSTO=60;PESO=3;LEGGENDA=Conoscete lo scudo fiscale?;LEGGENDA=Era di un esattore.";
    static final String SPADA = "TIPO=SPADA;NOME=la Spada Lunga;CHIAVE=SPADA_X;BREVE=la Spada;DESCRIZIONE=che taglia;DANNI=16;"
            + "LIVELLO=3;COSTO=40;PESO=2;MOD=FORZA +100%;MOD=VALORE +2;INCANTAMENTO=NECROTICO 12 0.15 Incantesimo di Prova;"
            + "LEGGENDA=Una lama.;LEGGENDA=Una lama.;SET=UN_SET";

    private static void rifiuta(String riga, String messaggio) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> OggettoLeggendario.da(riga));
        assertTrue(e.getMessage().contains(messaggio), e.getMessage());
    }

    @Test
    void leggeUnaRigaSenzaDanniNeSet() {
        OggettoLeggendario scudo = OggettoLeggendario.da(SCUDO + ";");
        assertEquals(SCUDO + ";", scudo.getRiga());
        assertEquals("SCUDO_X", scudo.getChiave());
        assertEquals(TipoArtefatto.SCUDO, scudo.getTipo());
        assertEquals("lo Scudo Fiscale", scudo.getNome());
        assertEquals("lo Scudo Fiscale", scudo.getNomeBreve());
        assertEquals(2, scudo.getLeggenda().size());
        assertNull(scudo.getSet());
        assertTrue(scudo.laLeggendaNominaLOggetto());
    }

    @Test
    void laLeggendaPuoNonNominareLOggetto() {
        OggettoLeggendario spada = OggettoLeggendario.da(SPADA);
        assertFalse(spada.laLeggendaNominaLOggetto());
        assertEquals("UN_SET", spada.getSet());
    }

    @Test
    void unaRigaMalFattaSiRifiuta() {
        rifiuta(SCUDO + ";;senzauguale", "Un campo è CHIAVE=valore");
        rifiuta(SCUDO + ";BOH=1", "Campo sconosciuto: BOH");
        rifiuta(SCUDO + ";PESO=4", "Campo ripetuto: PESO");
        rifiuta(SCUDO.replace("CHIAVE=SCUDO_X;", ""), "Manca il campo CHIAVE");
        rifiuta(SCUDO.replace("NOME=lo Scudo Fiscale;", "NOME=;"), "Manca il campo NOME");
        rifiuta(SCUDO + ";DANNI=3", "I danni ci sono per le armi");
        rifiuta(SPADA.replace("DANNI=16;", ""), "I danni ci sono per le armi");
        rifiuta(SCUDO.replace(";LEGGENDA=Era di un esattore.", ""), "La leggenda ha almeno 2 battute");
    }

    @Test
    void unaRigaNullaSiRifiuta() {
        assertThrows(NullPointerException.class, () -> OggettoLeggendario.da(null));
    }

    @Test
    void unIncantamentoSbagliatoSiScopreSubito() {
        assertThrows(IllegalArgumentException.class, () -> OggettoLeggendario.da(SPADA.replace("NECROTICO 12 0.15 Incantesimo di Prova", "NECROTICO 12")));
    }

    @Test
    void costruisceUnaSpadaConModificatoriIncantamentiESet() {
        Artefatto spada = OggettoLeggendario.da(SPADA).costruisci(1);
        ArtefattoMD md = spada.getModelloDati();
        assertEquals(TipoRaritaArtefatto.LEGGENDARIO, md.getRarita());
        assertEquals(3, md.getLivello());
        assertEquals(2, md.getModificatori().size());
        assertEquals(1, md.getIncantamenti().size());
        assertEquals("UN_SET", md.getSetLeggendario());
        assertEquals("SPADA_X", md.getPezzoLeggendario());
    }

    @Test
    void costruisceUnScudoSenzaIncantamentiNeSet() {
        ArtefattoMD md = OggettoLeggendario.da(SCUDO).costruisci(1).getModelloDati();
        assertEquals(TipoRaritaArtefatto.LEGGENDARIO, md.getRarita());
        assertTrue(md.getIncantamenti().isEmpty());
        assertNull(md.getSetLeggendario());
    }

    @Test
    void nasceAlLivelloDiRiferimentoSeEPiuAlto() {
        OggettoLeggendario scudo = OggettoLeggendario.da(SCUDO);
        assertEquals(5, scudo.costruisci(2).getLivello());
        assertEquals(9, scudo.costruisci(9).getLivello());
    }

    @Test
    void soloUnLeggendarioSiRiallineaAlLivello() {
        Artefatto scudo = OggettoLeggendario.da(SCUDO).costruisci(1);
        OggettoLeggendario.alLivello(scudo, 3);
        assertEquals(5, scudo.getLivello());
        OggettoLeggendario.alLivello(scudo, 8);
        assertEquals(8, scudo.getLivello());
        scudo.getModelloDati().setRarita(TipoRaritaArtefatto.RARO);
        OggettoLeggendario.alLivello(scudo, 12);
        assertEquals(8, scudo.getLivello());
    }
}
