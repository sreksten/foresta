package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.MissionePredefinita;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le missioni della partita, per identificativo: come si aggiungono, si dimenticano e si salvano.
 */
class RegistroMissioniMDTest {

    private static String salva(RegistroMissioniMD registro) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            registro.salva(stream);
        }
        return scritto.toString();
    }

    private static MissioneMD missione(String id) {
        MissioneMD missione = new MissioneMD();
        missione.setId(id);
        missione.setClasse(MissionePredefinita.SCONFIGGI_IL_DRAGO);
        return missione;
    }

    @Test
    void unaMissioneSiRitrovaDalSuoIdentificativo() {
        RegistroMissioniMD registro = new RegistroMissioniMD();
        MissioneMD drago = missione("SCONFIGGI_IL_DRAGO");
        registro.aggiungiMissione("SCONFIGGI_IL_DRAGO", drago);
        assertSame(drago, registro.getMissione("SCONFIGGI_IL_DRAGO"));
        assertNull(registro.getMissione("NON_C_E"));
        assertEquals(1, registro.getMissioni().size());
    }

    @Test
    void reimpostareDimenticaTutteLeMissioni() {
        RegistroMissioniMD registro = new RegistroMissioniMD();
        registro.aggiungiMissione("SCONFIGGI_IL_DRAGO", missione("SCONFIGGI_IL_DRAGO"));
        registro.reimposta();
        assertTrue(registro.getMissioni().isEmpty());
        assertNull(registro.getMissione("SCONFIGGI_IL_DRAGO"));
    }

    @Test
    void leMissioniSiSalvanoESiRileggonoConIlLoroAlbero() throws IOException {
        RegistroMissioniMD registro = new RegistroMissioniMD();
        MissioneMD drago = missione("SCONFIGGI_IL_DRAGO");
        drago.aggiungiProprieta("PASSO", "CACCIA");
        MissioneMD secondaria = missione("SECONDARIA");
        secondaria.setNome("La strega");
        drago.aggiungiMissioneMD(secondaria);
        registro.aggiungiMissione("SCONFIGGI_IL_DRAGO", drago);
        registro.aggiungiMissione("LA_LEALTA", missione("LA_LEALTA"));
        String salvato = salva(registro);

        RegistroMissioniMD riletto = new RegistroMissioniMD();
        riletto.aggiungiMissione("DA_DIMENTICARE", missione("DA_DIMENTICARE"));
        riletto.leggi(new BufferedReader(new StringReader(salvato)));

        assertNull(riletto.getMissione("DA_DIMENTICARE"), "le missioni di prima si sostituiscono");
        assertEquals(2, riletto.getMissioni().size());
        MissioneMD dragoRiletto = riletto.getMissione("SCONFIGGI_IL_DRAGO");
        assertEquals("CACCIA", dragoRiletto.ottieniProprieta("PASSO"));
        assertEquals("La strega", dragoRiletto.getMissioniMD().iterator().next().getNome());
        assertNotNull(riletto.getMissione("LA_LEALTA"));
        assertEquals(salvato, salva(riletto));
    }

    @Test
    void unRegistroTroncatoDaUnErroreChiaro() {
        assertThrows(IOException.class, () -> new RegistroMissioniMD().leggi(new BufferedReader(new StringReader(""))));
        assertThrows(IOException.class, () -> new RegistroMissioniMD().leggi(new BufferedReader(new StringReader("1\n"))),
                "annuncia una missione che non c'è");
    }
}
