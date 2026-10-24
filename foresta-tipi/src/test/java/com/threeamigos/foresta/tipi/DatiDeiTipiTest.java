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
    void iPersonaggiNotiHannoUnAmbitoEUnaChiave() {
        assertSame(TipoPersonaggio.BARDO_DELLE_LOCANDE, PersonaggioNoto.BARDO_DELLE_LOCANDE.getTipo());
        assertSame(PersonaggioNoto.Ambito.GLOBALE, PersonaggioNoto.BARDO_DELLE_LOCANDE.getAmbito());
        assertEquals("NOME_MASCHILE", PersonaggioNoto.BARDO_DELLE_LOCANDE.getProduzioneNomi());
        assertFalse(PersonaggioNoto.BARDO_DELLE_LOCANDE.isConProvenienza());
        assertTrue(PersonaggioNoto.ARMAIOLO.isConProvenienza());
        assertNull(PersonaggioNoto.CAMPIONE.getTipo(), "nome e classe del campione li dà la sfida");
        assertNull(PersonaggioNoto.LOCANDIERE.getProduzioneNomi(), "il nome del locandiere sta nella locanda");

        assertEquals("BARDO_DELLE_LOCANDE", PersonaggioNoto.BARDO_DELLE_LOCANDE.chiave(null));
        assertEquals("ARMAIOLO:CITTA_NYENA", PersonaggioNoto.ARMAIOLO.chiave("CITTA_NYENA"));
        for (PersonaggioNoto noto : PersonaggioNoto.values()) {
            assertNotNull(noto.getAmbito(), noto.name());
        }
    }

    @Test
    void soloChiEPermanenteResta() {
        assertFalse(TipoMotivoNelGruppo.PERMANENTE.isATempo());
        assertTrue(TipoMotivoNelGruppo.AIUTO_GRATUITO.isATempo());
        assertTrue(TipoMotivoNelGruppo.AIUTO_MERCENARIO.isATempo());
        assertTrue(TipoMotivoNelGruppo.MIETITURA.isATempo());
        // Lo scheletro della mietitura torna polvere quando cade, e non è stato interpellato
        for (TipoMotivoNelGruppo motivo : TipoMotivoNelGruppo.values()) {
            assertEquals(motivo == TipoMotivoNelGruppo.MIETITURA, motivo.isLasciaSeMuore(), motivo.name());
            assertEquals(motivo != TipoMotivoNelGruppo.MIETITURA, motivo.isEntraVolentieri(), motivo.name());
        }
    }

    @Test
    void piuSiStaRiparatiPiuIlRiposoRende() {
        assertTrue(TipoRiposo.ALL_APERTO_SENZA_FUOCO.getMoltiplicatore() < TipoRiposo.ALL_APERTO_CON_FUOCO.getMoltiplicatore());
        assertTrue(TipoRiposo.ALL_APERTO_CON_FUOCO.getMoltiplicatore() < TipoRiposo.AL_COPERTO.getMoltiplicatore());
        assertEquals(1.0d, TipoRiposo.ALL_APERTO_CON_FUOCO.getMoltiplicatore());
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
    void nessunIntermezzoFissoEDiProva() {
        for (TipoIntermezzo intermezzo : TipoIntermezzo.values()) {
            assertFalse(intermezzo.isDiProva(), intermezzo.name());
        }
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
