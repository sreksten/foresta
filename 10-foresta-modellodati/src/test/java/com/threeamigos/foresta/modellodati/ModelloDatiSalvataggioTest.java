package com.threeamigos.foresta.modellodati;


import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoSlotArtefatto;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Salva e rilegge in fila, sullo stesso flusso, le parti del modello dati lette con LettoreCampi
 * (la foresta resta fuori perché va generata): tutti i lettori devono restare allineati fra loro
 * anche con testi vuoti e campi facoltativi assenti.
 */
class ModelloDatiSalvataggioTest {

    @Test
    void salvaERileggiDanLoStessoSalvataggio() throws IOException {
        // Given
        ModelloDati modello = new ModelloDati();
        modello.reimposta(4, 3);
        modello.getRegistroArtefattiMD().addArtefattoInLocazione(
                ArtefattiDiTest.creaArtefatto(TipoArtefatto.ELMO, "l'elmo di Gorgor", ""), new CoordinateMD(3, 1));
        PersonaggioMD senzaNome = new PersonaggioMD();
        senzaNome.setClasse(TipoPersonaggio.LADRO);
        senzaNome.setVivo(true);
        ArtefattoMD spada = ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "");
        spada.setSlotEquipaggiamento(TipoSlotArtefatto.MANO_PRINCIPALE);
        spada.setNomeProprio("Diavolina");
        senzaNome.aggiungiArtefatto(spada);
        PersonaggioMD morto = new PersonaggioMD();
        morto.setClasse(TipoPersonaggio.MAGA);
        morto.setNome("Pippa");
        morto.setVivo(false);
        modello.getGruppoGiocatoreMD().addPersonaggioMD(senzaNome);
        modello.getGruppoGiocatoreMD().addPersonaggioMD(morto);
        modello.getGruppoGiocatoreMD().aggiungiArtefatto(
                ArtefattiDiTest.creaArtefatto(TipoArtefatto.PERGAMENA, "la pergamena minore", "che trasmette un effetto"));
        modello.getGruppoGiocatoreMD().setIncantesimi(TipoIncantesimo.FUOCO, 4);
        modello.getLineaTemporaleMD().setEvento("Gwendolyn vede levarsi una colonna di fumo a nord");
        String primo = salva(modello);
        // When
        ModelloDati riletto = new ModelloDati();
        BufferedReader reader = new BufferedReader(new StringReader(primo));
        for (Serializzabile parte : parti(riletto)) {
            parte.leggi(reader);
        }
        // Then
        assertEquals(primo, salva(riletto));
        assertEquals(4, riletto.getGruppoGiocatoreMD().getIncantesimi(TipoIncantesimo.FUOCO));
        assertFalse(riletto.getLineaTemporaleMD().isGiocoFinito(), "una partita finita non si salva: alla rilettura non lo è");
        assertEquals("Gwendolyn vede levarsi una colonna di fumo a nord", riletto.getLineaTemporaleMD().getEvento());
    }

    @Test
    void lAiutoSpentoSiSalvaESiRilegge() throws IOException {
        // Given
        ModelloDati modello = new ModelloDati();
        modello.reimposta(4, 3);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 3; y++) {
                modello.getForestaMD().impostaLocazione(new CoordinateMD(x, y), TipoLocazione.BOSCO);
            }
        }
        assertTrue(modello.isAiutoAbilitato(), "in una partita nuova l'aiuto è acceso");
        modello.setAiutoAbilitato(false);
        StringWriter scritto = new StringWriter();
        PrintWriter writer = new PrintWriter(scritto);
        modello.salva(writer);
        writer.flush();
        // When
        ModelloDati riletto = new ModelloDati();
        riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));
        // Then
        assertFalse(riletto.isAiutoAbilitato());
    }

    private static Serializzabile[] parti(ModelloDati modello) {
        return new Serializzabile[]{
                modello.getGruppoGiocatoreMD(), modello.getStatisticheMD(), modello.getLineaTemporaleMD(),
                modello.getRegistroPersonaggiMD(), modello.getRegistroArtefattiMD(), modello.getNotizieMD()
        };
    }

    private static String salva(ModelloDati modello) throws IOException {
        StringWriter scritto = new StringWriter();
        PrintWriter writer = new PrintWriter(scritto);
        for (Serializzabile parte : parti(modello)) {
            parte.salva(writer);
        }
        writer.flush();
        return scritto.toString();
    }

    @Test
    void laMappaPrendeIDatiDalModelloDatiInCorso() {
        ModelloDati.installa();
        ModelloDati uno = new ModelloDati();
        uno.reimposta(6, 6);
        ModelloDati.setIstanza(uno);
        assertEquals(6, com.threeamigos.foresta.mappa.Foresta.getDimensioneX());
        ModelloDati due = new ModelloDati();
        due.reimposta(9, 9);
        ModelloDati.setIstanza(due);
        assertEquals(9, com.threeamigos.foresta.mappa.Foresta.getDimensioneX(), "il modello dati cambiato si vede subito");
    }
}
