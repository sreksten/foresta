package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * I dati dei tipi del personaggio: i personaggi noti, il motivo per cui si è nel gruppo e quanto rende il riposo.
 */
class DatiDeiPersonaggiTest {

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
}
