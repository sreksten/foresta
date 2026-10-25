package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I dati che le enum del modulo portano con sé: quali missioni esistono dall'inizio, chi sono i personaggi noti e dove
 * vivono, perché un personaggio è nel gruppo, quanto rende un riposo, gli oggetti, gli intermezzi e i tipi di missione.
 */
class DatiDeiTipiTest {

    @Test
    void leMissioniPredefiniteSiRiconosconoDalLoroIdentificativo() {
        assertTrue(MissionePredefinita.SCONFIGGI_IL_DRAGO.isPredefinita());
        assertFalse(MissionePredefinita.SCONFIGGI_IL_MINOTAURO_GIGANTE.isPredefinita(), "nasce durante la partita");
        for (MissionePredefinita classe : MissionePredefinita.values()) {
            assertEquals(classe.isPredefinita(), MissionePredefinita.isIdDiMissionePredefinita(classe.name()), classe.name());
            assertSame(classe.isPredefinita() ? classe : null, MissionePredefinita.predefinitaConId(classe.name()), classe.name());
        }
        assertFalse(MissionePredefinita.isIdDiMissionePredefinita("a9dc89fb-3931-4da7-af35-5cb1c2113450"), "un uuid non è una predefinita");
        assertNull(MissionePredefinita.predefinitaConId("NON_ESISTE"));
    }




    @Test
    void gliOggettiGenerabiliSonoTuttiTranneArtefattiEOggettiDiMissione() {
        for (TipoBottino oggetto : TipoBottino.values()) {
            boolean generabile = oggetto != TipoBottino.ARTEFATTO && oggetto != TipoBottino.OGGETTO_MISSIONE;
            assertEquals(generabile, oggetto.isGenerabile(), oggetto.name());
            assertTrue(oggetto.getQuantitaMassima() >= 1, oggetto.name());
            assertTrue(oggetto.getValore() > 0, oggetto.name());
        }
        assertEquals(150, TipoBottino.COFANO.getValore());
        assertEquals(2, TipoBottino.MONETA.getQuantitaMassima());
    }

    @Test
    void ogniTipoDiMissioneHaLaSuaCategoria() {
        for (TipoMissione tipo : TipoMissione.values()) {
            assertNotNull(tipo.getCategoriaMissione(), tipo.name());
        }
        assertSame(CategoriaMissione.ACQUISIZIONE, TipoMissione.RACCOLTA_INGREDIENTI.getCategoriaMissione());
        assertSame(CategoriaMissione.SPIRITUALE, TipoMissione.RITUALE.getCategoriaMissione());
    }
}
