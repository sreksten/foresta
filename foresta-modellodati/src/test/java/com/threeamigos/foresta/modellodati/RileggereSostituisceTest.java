package com.threeamigos.foresta.modellodati;


import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.tipi.TipoNegozio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rileggere un salvataggio in un oggetto che ha già dei dati li sostituisce, non li somma (come fanno già i trofei, le notizie e gli
 * intermezzi).
 */
class RileggereSostituisceTest {

    private static PersonaggioMD personaggio(TipoPersonaggio classe, String nome) {
        PersonaggioMD personaggio = new PersonaggioMD();
        personaggio.setClasse(classe);
        personaggio.setNome(nome);
        personaggio.setVivo(true);
        return personaggio;
    }

    private static BufferedReader scritto(Serializzabile oggetto) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            oggetto.salva(stream);
        }
        return new BufferedReader(new StringReader(scritto.toString()));
    }

    @Test
    void ilGruppoRiletto() throws IOException {
        GruppoGiocatoreMD salvato = new GruppoGiocatoreMD();
        salvato.addPersonaggioMD(personaggio(TipoPersonaggio.GUERRIERO, "Arsenio"));
        GruppoGiocatoreMD destinazione = new GruppoGiocatoreMD();
        destinazione.addPersonaggioMD(personaggio(TipoPersonaggio.MAGO, "Merlino"));
        destinazione.addPersonaggioMD(personaggio(TipoPersonaggio.LADRO, "Gualtiero"));

        destinazione.leggi(scritto(salvato));

        assertEquals(1, destinazione.getPersonaggiMD().size());
        assertEquals("Arsenio", destinazione.getPersonaggiMD().get(0).getNome());
    }

    @Test
    void ilRegistroDeiPersonaggiRiletto() throws IOException {
        RegistroPersonaggiMD salvato = new RegistroPersonaggiMD();
        salvato.addPersonaggioInLocazione(personaggio(TipoPersonaggio.TROLL, "Gruk"), new CoordinateMD(1, 2));
        RegistroPersonaggiMD destinazione = new RegistroPersonaggiMD();
        destinazione.addPersonaggioInLocazione(personaggio(TipoPersonaggio.GOBLIN, "Snaga"), new CoordinateMD(5, 5));
        destinazione.aggiungiResidente("CAMPIONE:Vecchio", new ResidenteMD("Vecchio", null, TipoPersonaggio.GUERRIERO));

        destinazione.leggi(scritto(salvato));

        assertEquals(1, destinazione.getUbicazioniPersonaggi().size());
        assertNull(destinazione.getPersonaggioInLocazione(new CoordinateMD(5, 5)));
        assertEquals("Gruk", destinazione.getPersonaggioInLocazione(new CoordinateMD(1, 2)).getNome());
        assertTrue(destinazione.getResidenti().isEmpty());
    }

    @Test
    void ilRegistroDegliArtefattiRiletto() throws IOException {
        RegistroArtefattiMD salvato = new RegistroArtefattiMD();
        salvato.segnaLocalizzazioneConosciuta(new CoordinateMD(2, 2));
        RegistroArtefattiMD destinazione = new RegistroArtefattiMD();
        destinazione.addArtefattoInLocazione(ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "la spada vecchia", "che arrugginisce"),
                new CoordinateMD(9, 9));
        destinazione.aggiungiAMagazzino(new CoordinateMD(7, 1), TipoNegozio.ARMAIOLO,
                ArtefattiDiTest.creaArtefatto(TipoArtefatto.SCUDO, "lo scudo vecchio", "che cigola"));
        destinazione.segnaLocalizzazioneConosciuta(new CoordinateMD(8, 8));

        destinazione.leggi(scritto(salvato));

        assertTrue(destinazione.getUbicazioniArtefattiSmarriti().isEmpty());
        assertTrue(destinazione.getMagazzino(new CoordinateMD(7, 1), TipoNegozio.ARMAIOLO).isEmpty());
        assertEquals(1, destinazione.getLocalizzazioniConosciute().size());
        assertTrue(destinazione.isLocalizzazioneConosciuta(new CoordinateMD(2, 2)));
    }
}
