package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Guerriera;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladra;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il participio di una notizia si accorda con il suo soggetto: il capo (EROE) se parla di lui, il ladro o il bardo se parla di loro.
 */
class ConcordanzaDelleNotizieTest {

    @BeforeEach
    @AfterEach
    void gruppoPulito() {
        BusEventi.azzera();
        ModelloDati.setIstanza(new ModelloDati());
        GruppoGiocatore.azzeraIstanza();
    }

    @Test
    void ilParticipioSegueIlLadroENonIlCapo() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(new Guerriero("Arsenio", 1));
        gruppo.aggiungiPersonaggio(new Ladra("Gualtiera", 1));

        assertEquals("Gualtiera è stata vista contare monete.",
                NotizieDelleLocande.validaNotizia("LADRO è statLETTERA_FINALE_LADRO vistLETTERA_FINALE_LADRO contare monete."));
        assertEquals("Arsenio è stato visto contare monete.",
                NotizieDelleLocande.validaNotizia("EROE è statLETTERA_FINALE vistLETTERA_FINALE contare monete."));
    }

    @Test
    void ilParticipioSegueIlBardoENonIlCapo() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(new Guerriera("Brunilde", 1));
        gruppo.aggiungiPersonaggio(new Bardo("Dante", 1));

        assertEquals("Dante viene cacciato a calci.",
                NotizieDelleLocande.validaNotizia("BARDO viene cacciatLETTERA_FINALE_BARDO a calci."));
        assertEquals("Brunilde viene cacciata a calci.",
                NotizieDelleLocande.validaNotizia("EROE viene cacciatLETTERA_FINALE a calci."));
    }

    @Test
    void senzaLadroLaNotiziaNonSiApplica() {
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(new Guerriero("Arsenio", 1));

        assertNull(NotizieDelleLocande.validaNotizia("LADRO è statLETTERA_FINALE_LADRO vistLETTERA_FINALE_LADRO."));
    }

    @Test
    void nelleGrammaticheLetteraFinaleSenzaClasseVaConEroe() throws IOException {
        try (BufferedReader lettore = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/com/threeamigos/foresta/testi/locande.txt"), StandardCharsets.UTF_8))) {
            String riga;
            while ((riga = lettore.readLine()) != null) {
                if (Pattern.compile("LETTERA_FINALE(?!_)").matcher(riga).find()) {
                    assertTrue(riga.contains("EROE"), "LETTERA_FINALE si accorda con il capo: il soggetto dev'essere EROE, altrimenti "
                            + "LETTERA_FINALE_<CLASSE>: " + riga);
                }
            }
        }
    }
}
