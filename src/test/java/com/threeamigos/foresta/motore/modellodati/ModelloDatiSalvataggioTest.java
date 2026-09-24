package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        senzaNome.setClasse(ClassePersonaggio.LADRO);
        senzaNome.setVivo(true);
        ArtefattoMD spada = ArtefattoMDTest.creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "");
        spada.setSlotEquipaggiamento(SlotArtefatto.MANO_PRINCIPALE);
        spada.setNomeProprio("Diavolina");
        senzaNome.getArtefatti().add(spada);
        PersonaggioMD morto = new PersonaggioMD();
        morto.setClasse(ClassePersonaggio.MAGA);
        morto.setNome("Pippa");
        morto.setVivo(false);
        modello.getGruppoGiocatoreMD().addPersonaggioMD(senzaNome);
        modello.getGruppoGiocatoreMD().addPersonaggioMD(morto);
        modello.getGruppoGiocatoreMD().getArtefatti().add(
                ArtefattoMDTest.creaArtefatto(TipoArtefatto.INCANTAMENTO, "la pergamena minore", "che trasmette un effetto"));
        String primo = salva(modello);
        // When
        ModelloDati riletto = new ModelloDati();
        BufferedReader reader = new BufferedReader(new StringReader(primo));
        for (Serializzabile parte : parti(riletto)) {
            parte.leggi(reader);
        }
        // Then
        assertEquals(primo, salva(riletto));
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
