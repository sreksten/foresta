package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoSlotArtefatto;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                ArtefattoMDTest.creaArtefatto(TipoArtefatto.ELMO, "l'elmo di Gorgor", ""), new CoordinateMD(3, 1));
        modello.getGruppoGiocatoreMD().setCoordinate(new CoordinateMD(1, 2));
        PersonaggioMD senzaNome = new PersonaggioMD();
        senzaNome.setClasse(TipoPersonaggio.LADRO);
        senzaNome.setVivo(true);
        ArtefattoMD spada = ArtefattoMDTest.creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "");
        spada.setSlotEquipaggiamento(TipoSlotArtefatto.MANO_PRINCIPALE);
        spada.setNomeProprio("Diavolina");
        senzaNome.getArtefatti().add(spada);
        PersonaggioMD morto = new PersonaggioMD();
        morto.setClasse(TipoPersonaggio.MAGA);
        morto.setNome("Pippa");
        morto.setVivo(false);
        modello.getGruppoGiocatoreMD().addPersonaggioMD(senzaNome);
        modello.getGruppoGiocatoreMD().addPersonaggioMD(morto);
        modello.getGruppoGiocatoreMD().getArtefatti().add(
                ArtefattoMDTest.creaArtefatto(TipoArtefatto.PERGAMENA, "la pergamena minore", "che trasmette un effetto"));
        modello.getGruppoGiocatoreMD().setIncantesimi(ClasseIncantesimo.FUOCO, 4);
        modello.getLineaTemporaleMD().setGiocoFinito(true);
        modello.getLineaTemporaleMD().setEvento("Gwendolyn vede levarsi una colonna di fumo a nord");
        modello.getLineaTemporaleMD().addCittaDistrutta(TipoLocazione.CITTA_RUUNA);
        String primo = salva(modello);
        // When
        ModelloDati riletto = new ModelloDati();
        BufferedReader reader = new BufferedReader(new StringReader(primo));
        for (Serializzabile parte : parti(riletto)) {
            parte.leggi(reader);
        }
        // Then
        assertEquals(primo, salva(riletto));
        assertEquals(4, riletto.getGruppoGiocatoreMD().getIncantesimi(ClasseIncantesimo.FUOCO));
        assertTrue(riletto.getLineaTemporaleMD().isGiocoFinito());
        assertEquals("Gwendolyn vede levarsi una colonna di fumo a nord", riletto.getLineaTemporaleMD().getEvento());
        assertEquals(Collections.singletonList(TipoLocazione.CITTA_RUUNA), riletto.getLineaTemporaleMD().getCittaDistrutte());
    }

    @Test
    void lAiutoSpentoSiSalvaESiRilegge() throws IOException {
        // Given
        ModelloDati modello = new ModelloDati();
        modello.reimposta(4, 3);
        modello.getGruppoGiocatoreMD().setCoordinate(new CoordinateMD(1, 2));
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
}
