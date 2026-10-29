package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.MissionePredefinita;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I dati di una missione: i suoi campi, le proprietà, l'albero delle missioni secondarie e come si salvano.
 */
class MissioneMDTest {

    private static String salva(MissioneMD missione) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            missione.salva(stream);
        }
        return scritto.toString();
    }

    private static MissioneMD rileggi(MissioneMD destinazione, String salvato) throws IOException {
        destinazione.leggi(new BufferedReader(new StringReader(salvato)));
        return destinazione;
    }

    private static MissioneMD missione(String id) {
        MissioneMD missione = new MissioneMD();
        missione.setId(id);
        missione.setClasse(MissionePredefinita.values()[0]);
        return missione;
    }

    @Test
    void iCampiSiLeggonoESiCambiano() {
        MissioneMD missione = new MissioneMD();
        assertNotNull(missione.getId());
        assertNotEquals(missione.getId(), new MissioneMD().getId(), "ogni missione ha il suo identificativo");
        assertTrue(missione.isDescrizioneVisibile());

        missione.setId("ID");
        missione.setClasse(MissionePredefinita.values()[1]);
        missione.setNome("Il ponte|dei troll");
        missione.setDescrizione("Liberate il ponte\ndai troll");
        missione.setDescrizioneVisibile(false);

        assertEquals("ID", missione.getId());
        assertEquals(MissionePredefinita.values()[1], missione.getClasse());
        assertEquals("Il pontedei troll", missione.getNome(), "il | si toglie");
        assertEquals("Liberate il ponte dai troll", missione.getDescrizione(), "l'a capo diventa uno spazio");
        assertFalse(missione.isDescrizioneVisibile());
    }

    @Test
    void leProprietaSiAggiungonoSiSostituisconoESiTolgono() {
        MissioneMD missione = missione("M");
        assertNull(missione.ottieniProprieta("PASSO"));
        missione.aggiungiProprieta("PASSO", "INCARICO");
        missione.aggiungiProprieta("PASSO", "ACCETTAZIONE|con il separatore");
        assertEquals("ACCETTAZIONEcon il separatore", missione.ottieniProprieta("PASSO"));
        missione.rimuoviProprieta("PASSO");
        assertNull(missione.ottieniProprieta("PASSO"));
    }

    @Test
    void leMissioniSecondarieNonSiRipetonoENonFannoCicli() {
        MissioneMD principale = missione("PRINCIPALE");
        MissioneMD prima = missione("PRIMA");
        MissioneMD seconda = missione("SECONDA");

        assertTrue(principale.aggiungiMissioneMD(prima));
        assertTrue(principale.aggiungiMissioneMD(seconda));
        assertFalse(principale.aggiungiMissioneMD(missione("PRIMA")), "lo stesso identificativo c'è già");
        assertFalse(principale.aggiungiMissioneMD(principale), "una missione secondaria di sé stessa sarebbe un ciclo");
        assertEquals(listaDi(prima, seconda), new ArrayList<>(principale.getMissioniMD()));
        assertThrows(UnsupportedOperationException.class, () -> principale.getMissioniMD().add(missione("ALTRA")));

        // Si toglie per identificativo, anche passando un'altra istanza
        principale.rimuoviMissioneMD(missione("PRIMA"));
        assertEquals(listaDi(seconda), new ArrayList<>(principale.getMissioniMD()));
    }

    @Test
    void reimpostareTogliePropietaEMissioniSecondarieMaNonIlResto() {
        MissioneMD missione = missione("M");
        missione.setNome("Il nome");
        missione.aggiungiProprieta("PASSO", "INCARICO");
        missione.aggiungiMissioneMD(missione("SECONDARIA"));

        missione.reimposta();

        assertNull(missione.ottieniProprieta("PASSO"));
        assertTrue(missione.getMissioniMD().isEmpty());
        assertEquals("M", missione.getId());
        assertEquals("Il nome", missione.getNome());
    }

    @Test
    void lAlberoDelleMissioniSiSalvaESiRilegge() throws IOException {
        MissioneMD principale = missione("PRINCIPALE");
        principale.setNome("Sconfiggi il drago");
        principale.setDescrizione("Il drago minaccia le città");
        principale.setDescrizioneVisibile(false);
        principale.aggiungiProprieta("PASSO", "CACCIA");
        MissioneMD secondaria = missione("SECONDARIA");
        secondaria.setNome("La strega");
        MissioneMD nipote = missione("NIPOTE");
        nipote.aggiungiProprieta("FATTA", "S");
        secondaria.aggiungiMissioneMD(nipote);
        principale.aggiungiMissioneMD(secondaria);
        String salvato = salva(principale);

        MissioneMD destinazione = missione("DA_SOSTITUIRE");
        destinazione.aggiungiProprieta("VECCHIA", "S");
        destinazione.aggiungiMissioneMD(missione("VECCHIA_SECONDARIA"));
        MissioneMD riletta = rileggi(destinazione, salvato);

        assertEquals("PRINCIPALE", riletta.getId());
        assertEquals("Sconfiggi il drago", riletta.getNome());
        assertEquals("Il drago minaccia le città", riletta.getDescrizione());
        assertFalse(riletta.isDescrizioneVisibile());
        assertEquals("CACCIA", riletta.ottieniProprieta("PASSO"));
        assertNull(riletta.ottieniProprieta("VECCHIA"), "le proprietà di prima si sostituiscono");
        assertEquals(1, riletta.getMissioniMD().size(), "le missioni secondarie di prima si sostituiscono");
        MissioneMD secondariaRiletta = riletta.getMissioniMD().iterator().next();
        assertEquals("La strega", secondariaRiletta.getNome());
        MissioneMD nipoteRiletta = secondariaRiletta.getMissioniMD().iterator().next();
        assertEquals("NIPOTE", nipoteRiletta.getId());
        assertEquals("S", nipoteRiletta.ottieniProprieta("FATTA"));
        assertEquals(salvato, salva(riletta));
    }

    @Test
    void nomeEDescrizioneCheMancanoRestanoMancanti() throws IOException {
        MissioneMD missione = missione("M");
        MissioneMD riletta = rileggi(new MissioneMD(), salva(missione));
        assertNull(riletta.getNome());
        assertNull(riletta.getDescrizione());
    }

    private static List<MissioneMD> listaDi(MissioneMD... missioni) {
        List<MissioneMD> lista = new ArrayList<>();
        Collections.addAll(lista, missioni);
        return lista;
    }
}
