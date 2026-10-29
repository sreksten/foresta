package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I dati che le enum delle missioni portano con sé: quali missioni esistono dall'inizio i tipi di missione e chi batte chi a carta, forbici e sasso.
 */
class TipiDelleMissioniTest {

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
    void ogniTipoDiMissioneHaLaSuaCategoria() {
        for (TipoMissione tipo : TipoMissione.values()) {
            assertNotNull(tipo.getCategoriaMissione(), tipo.name());
        }
        assertSame(CategoriaMissione.ACQUISIZIONE, TipoMissione.RACCOLTA_INGREDIENTI.getCategoriaMissione());
        assertSame(CategoriaMissione.SPIRITUALE, TipoMissione.RITUALE.getCategoriaMissione());
    }

    @Test
    void cartaBatteSassoSassoBatteForbiceForbiceBatteCarta() {
        for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
            for (MossaCartaForbiciSasso altra : MossaCartaForbiciSasso.values()) {
                boolean attesa = (mossa == MossaCartaForbiciSasso.CARTA && altra == MossaCartaForbiciSasso.SASSO)
                        || (mossa == MossaCartaForbiciSasso.SASSO && altra == MossaCartaForbiciSasso.FORBICE)
                        || (mossa == MossaCartaForbiciSasso.FORBICE && altra == MossaCartaForbiciSasso.CARTA);
                assertEquals(attesa, mossa.batte(altra), mossa + " contro " + altra);
            }
        }
    }
}
