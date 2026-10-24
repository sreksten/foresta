package com.threeamigos.foresta.serializzazione;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class LettoreCampiTest {

    private enum Slot {
        TESTA, MANO_SECONDARIA
    }

    @Test
    void conservaICampiVuoti() throws IOException {
        LettoreCampi campi = new LettoreCampi("a||3|");
        assertEquals("a", campi.testo());
        assertEquals("", campi.testo());
        assertEquals(3, campi.intero());
        assertEquals("", campi.testo());
        assertFalse(campi.haAltriCampi());
    }

    @Test
    void unaRigaVuotaNonHaCampi() throws IOException {
        assertFalse(new LettoreCampi("").haAltriCampi());
    }

    @Test
    void iCampiFacoltativiVuotiSonoNull() throws IOException {
        LettoreCampi campi = new LettoreCampi("|MANO_SECONDARIA|");
        assertNull(campi.testoFacoltativo());
        assertEquals(Slot.MANO_SECONDARIA, campi.enumeratoFacoltativo(Slot.class));
        assertNull(campi.enumeratoFacoltativo(Slot.class));
        assertEquals("", Serializzabile.facoltativo(null));
        assertEquals("TESTA", Serializzabile.facoltativo(Slot.TESTA));
    }

    @Test
    void campiMancantiOFineDelFileSonoErrori() throws IOException {
        LettoreCampi campi = new LettoreCampi("uno");
        campi.testo();
        assertThrows(IOException.class, campi::testo);
        assertThrows(IOException.class, () -> new LettoreCampi(null));
    }

    @Test
    void unNomeDiEnumeratoSconosciutoEUnErroreDiLettura() throws IOException {
        IOException obbligatorio = assertThrows(IOException.class,
                () -> new LettoreCampi("NON_ESISTE").enumerato(Slot.class));
        assertTrue(obbligatorio.getMessage().contains("NON_ESISTE"), obbligatorio.getMessage());
        assertTrue(obbligatorio.getMessage().contains("Slot"), obbligatorio.getMessage());
        assertThrows(IOException.class, () -> new LettoreCampi("NON_ESISTE").enumeratoFacoltativo(Slot.class));
        assertThrows(IOException.class, () -> new LettoreCampi("").enumerato(Slot.class));
    }

    @Test
    void unDecimaleRovinatoDaUnIOExceptionCheDiceDove() throws IOException {
        LettoreCampi campi = new LettoreCampi("1.5|uno e mezzo");
        assertEquals(1.5d, campi.decimale());
        IOException errore = assertThrows(IOException.class, campi::decimale);
        assertTrue(errore.getMessage().contains("uno e mezzo"), errore.getMessage());
    }

    private static BufferedReader righe(String testo) {
        return new BufferedReader(new StringReader(testo));
    }

    @Test
    void unaRigaConUnSoloValoreSiLeggeDalFlusso() throws IOException {
        assertEquals(42, LettoreCampi.leggiIntero(righe("42\n")));
        assertEquals("abc-123", LettoreCampi.leggiTesto(righe("abc-123\n")));
        assertTrue(LettoreCampi.leggiBooleano(righe("true\n")));
        assertFalse(LettoreCampi.leggiBooleano(righe("false\n")));
    }

    @Test
    void unFlussoFinitoPrimaEUnErroreDiLettura() {
        assertThrows(IOException.class, () -> LettoreCampi.leggiIntero(righe("")));
        assertThrows(IOException.class, () -> LettoreCampi.leggiTesto(righe("")));
        assertThrows(IOException.class, () -> LettoreCampi.leggiBooleano(righe("")));
        assertThrows(IOException.class, () -> LettoreCampi.leggiIntero(righe("dodici\n")));
    }

    @Test
    void unCampoFacoltativoPienoSiLeggeComeE() throws IOException {
        LettoreCampi campi = new LettoreCampi("testo|");
        assertEquals("testo", campi.testoFacoltativo());
        assertNull(campi.testoFacoltativo());
    }

    @Test
    void unNumeroInteroSiLeggeEUnTestoNoDaUnErroreCheDiceIlCampo() throws IOException {
        LettoreCampi campi = new LettoreCampi("-7|sette");
        assertEquals(-7, campi.intero());
        IOException errore = assertThrows(IOException.class, campi::intero);
        assertTrue(errore.getMessage().contains("sette"), errore.getMessage());
        assertTrue(errore.getCause() instanceof NumberFormatException);
    }

    @Test
    void unBooleanoSiLegge() throws IOException {
        LettoreCampi campi = new LettoreCampi("true|false|boh");
        assertTrue(campi.booleano());
        assertFalse(campi.booleano());
        assertFalse(campi.booleano(), "ciò che non è true vale falso");
    }

    @Test
    void unEnumeratoSiLeggeDalNome() throws IOException {
        assertEquals(Slot.TESTA, new LettoreCampi("TESTA").enumerato(Slot.class));
    }

    @Test
    void iSottocampiSonoUnaRigaAParte() throws IOException {
        LettoreCampi campi = new LettoreCampi("FORZA§12§nota § con separatore|dopo");
        LettoreCampi sottocampi = campi.sottocampi(2, 3);
        assertEquals("FORZA", sottocampi.testo());
        assertEquals(12, sottocampi.intero());
        assertEquals("nota § con separatore", sottocampi.testo(), "l'ultimo tiene tutto il resto");
        assertFalse(sottocampi.haAltriCampi());
        assertEquals("dopo", campi.testo(), "il campo seguente resta al suo posto");
    }

    @Test
    void conMenoSottocampiDelMinimoELaRigaERovinata() throws IOException {
        LettoreCampi campi = new LettoreCampi("FORZA§12");
        IOException errore = assertThrows(IOException.class, () -> campi.sottocampi(3, 3));
        assertTrue(errore.getMessage().contains("FORZA§12"), errore.getMessage());
        LettoreCampi finito = new LettoreCampi("");
        assertThrows(IOException.class, () -> finito.sottocampi(1, 1));
    }

    @Test
    void ilRestoDellaRigaTieneIlPipeEFinisceICampi() throws IOException {
        LettoreCampi campi = new LettoreCampi("uno|due|tre|quattro");
        assertEquals("uno", campi.testo());
        assertEquals("due|tre|quattro", campi.resto());
        assertFalse(campi.haAltriCampi());
        assertEquals("", campi.resto(), "finiti i campi il resto è vuoto");
    }
}
