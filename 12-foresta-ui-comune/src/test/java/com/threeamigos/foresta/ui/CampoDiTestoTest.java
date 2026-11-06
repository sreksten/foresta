package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il campo di testo comune, con un campo finto al posto di quello del toolkit.
 */
class CampoDiTestoTest {

    /** Il campo del toolkit finto: ricorda se si vede e che cosa c'è scritto */
    private static final class CampoFinto implements CampoDiTesto.Campo {
        boolean visibile;
        String testo = "";

        @Override
        public void mostra(String testo) {
            visibile = true;
            this.testo = testo;
        }

        @Override
        public void nascondi() {
            visibile = false;
        }

        @Override
        public String getTestoScritto() {
            return testo;
        }
    }

    private final CampoFinto finto = new CampoFinto();
    private final CampoDiTesto campo = new CampoDiTesto();

    CampoDiTestoTest() {
        campo.setCampo(finto);
    }

    @Test
    void annullaChiudeSoloLaRichiestaGiusta() {
        Consumer<String> mia = testo -> { };
        Consumer<String> altra = testo -> { };
        campo.mostra("nome", mia);
        assertTrue(campo.isVisibile());
        assertTrue(finto.visibile);

        campo.annulla(altra);
        assertTrue(campo.isVisibile(), "un'altra richiesta non chiude questa");

        campo.annulla(mia);
        assertFalse(campo.isVisibile());
        assertFalse(finto.visibile);
    }

    @Test
    void lInvioVaAChiHaChiestoIlTestoSenzaICaratteriCheIlFontNonHa() {
        List<String> ricevuti = new ArrayList<>();
        campo.mostra("", ricevuti::add);
        finto.testo = "Bob_";
        campo.invio();
        assertEquals(1, ricevuti.size());
        assertEquals("Bob", ricevuti.get(0));
        assertFalse(campo.isVisibile());
    }

    @Test
    void escRinunciaSoloAUnTestoChiestoDallaUi() {
        campo.mostra("Aldo");
        campo.esc();
        assertTrue(campo.isVisibile(), "il testo che aspetta il motore non si rinuncia");

        campo.mostra("Aldo", testo -> { });
        campo.esc();
        assertFalse(campo.isVisibile());
    }

    @Test
    void iCaratteriDiControlloSonoAmmessiIlSeparatoreNo() {
        assertTrue(CampoDiTesto.ammesso('\b'));
        assertTrue(CampoDiTesto.ammesso('a'));
        assertFalse(CampoDiTesto.ammesso('|'));
    }
}
