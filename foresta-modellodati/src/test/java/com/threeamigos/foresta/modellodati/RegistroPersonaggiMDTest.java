package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I personaggi del mondo fuori dal gruppo: quelli da distribuire, quelli nelle locazioni e i residenti (bardo, borgomastri,
 * mastri di bottega...), e come si salvano.
 */
class RegistroPersonaggiMDTest {

    private static PersonaggioMD personaggio(TipoPersonaggio classe) {
        PersonaggioMD personaggio = new PersonaggioMD();
        personaggio.setClasse(classe);
        personaggio.setVivo(true);
        return personaggio;
    }

    private static RegistroPersonaggiMD salvaERileggi(RegistroPersonaggiMD registro, RegistroPersonaggiMD destinazione) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            registro.salva(stream);
        }
        destinazione.leggi(new BufferedReader(new StringReader(scritto.toString())));
        return destinazione;
    }

    @Test
    void iPersonaggiDaDistribuireSiPrendonoUnoAllaVolta() {
        RegistroPersonaggiMD registro = new RegistroPersonaggiMD();
        PersonaggioMD goblin = personaggio(TipoPersonaggio.GOBLIN);
        PersonaggioMD troll = personaggio(TipoPersonaggio.TROLL);
        registro.aggiungiPersonaggio(goblin);
        registro.aggiungiPersonaggio(troll);
        assertEquals(2, registro.getNumeroDisponibili());
        assertSame(goblin, registro.rimuoviDisponibile(0));
        assertEquals(1, registro.getNumeroDisponibili());
    }

    @Test
    void ogniLocazioneHaIlSuoPersonaggio() {
        RegistroPersonaggiMD registro = new RegistroPersonaggiMD();
        CoordinateMD grotta = new CoordinateMD(4, 2);
        PersonaggioMD troll = personaggio(TipoPersonaggio.TROLL);
        registro.addPersonaggioInLocazione(troll, grotta);
        assertSame(troll, registro.getPersonaggioInLocazione(grotta));
        assertEquals(Collections.singleton(grotta), registro.getUbicazioniPersonaggi());
        assertThrows(UnsupportedOperationException.class, () -> registro.getUbicazioniPersonaggi().clear());
        registro.rimuoviPersonaggioInLocazione(grotta);
        assertNull(registro.getPersonaggioInLocazione(grotta));
    }

    @Test
    void laChiaveDiUnResidenteSiPulisceEdETroveraAnchePrimaDellaPulizia() throws IOException {
        RegistroPersonaggiMD registro = new RegistroPersonaggiMD();
        ResidenteMD campione = new ResidenteMD("Ro|sa", null, TipoPersonaggio.GUERRIERA);
        registro.aggiungiResidente("CAMPIONE:Ro|sa", campione);

        assertSame(campione, registro.getResidente("CAMPIONE:Ro|sa"));
        assertSame(campione, registro.getResidente("CAMPIONE:Rosa"));
        assertEquals(Collections.singleton("CAMPIONE:Rosa"), registro.getResidenti().keySet());

        // E il salvataggio si rilegge: il | nella chiave spezzerebbe la riga
        RegistroPersonaggiMD riletto = salvaERileggi(registro, new RegistroPersonaggiMD());
        assertEquals("Rosa", riletto.getResidente("CAMPIONE:Ro|sa").getNome());
        assertEquals(TipoPersonaggio.GUERRIERA, riletto.getResidente("CAMPIONE:Rosa").getClasse());
    }

    @Test
    void iResidentiSiRitrovanoDallaLoroChiave() {
        RegistroPersonaggiMD registro = new RegistroPersonaggiMD();
        ResidenteMD bardo = new ResidenteMD("Lino|il Bardo", null, null);
        registro.aggiungiResidente("BARDO_DELLE_LOCANDE", bardo);
        assertSame(bardo, registro.getResidente("BARDO_DELLE_LOCANDE"));
        assertNull(registro.getResidente("NESSUNO"));
        assertEquals("Linoil Bardo", bardo.getNome(), "il | si toglie");
        assertThrows(UnsupportedOperationException.class, () -> registro.getResidenti().clear());

        ResidenteMD armaiolo = new ResidenteMD("Gerardo", "da Nyena", TipoPersonaggio.ARMAIOLO);
        assertEquals("da Nyena", armaiolo.getProvenienza());
        assertSame(TipoPersonaggio.ARMAIOLO, armaiolo.getClasse());
        assertFalse(armaiolo.isSconfitto());
        armaiolo.setSconfitto(true);
        assertTrue(armaiolo.isSconfitto());
    }

    @Test
    void personaggiNelleLocazioniEResidentiSiSalvanoESiRileggono() throws IOException {
        RegistroPersonaggiMD registro = new RegistroPersonaggiMD();
        CoordinateMD grotta = new CoordinateMD(4, 2);
        PersonaggioMD troll = personaggio(TipoPersonaggio.TROLL);
        troll.setNome("Brutto");
        registro.addPersonaggioInLocazione(troll, grotta);
        ResidenteMD campione = new ResidenteMD("Ugo", "da Ruuna", TipoPersonaggio.GUERRIERO);
        campione.setSconfitto(true);
        registro.aggiungiResidente("CAMPIONE:1", campione);
        registro.aggiungiResidente("BARDO_DELLE_LOCANDE", new ResidenteMD("Lino", null, null));
        registro.aggiungiPersonaggio(personaggio(TipoPersonaggio.GOBLIN));

        RegistroPersonaggiMD destinazione = new RegistroPersonaggiMD();
        destinazione.aggiungiResidente("DA_DIMENTICARE", new ResidenteMD("Nessuno", null, null));
        RegistroPersonaggiMD riletto = salvaERileggi(registro, destinazione);

        assertEquals("Brutto", riletto.getPersonaggioInLocazione(grotta).getNome());
        ResidenteMD campioneRiletto = riletto.getResidente("CAMPIONE:1");
        assertEquals("Ugo", campioneRiletto.getNome());
        assertEquals("da Ruuna", campioneRiletto.getProvenienza());
        assertSame(TipoPersonaggio.GUERRIERO, campioneRiletto.getClasse());
        assertTrue(campioneRiletto.isSconfitto());
        ResidenteMD bardoRiletto = riletto.getResidente("BARDO_DELLE_LOCANDE");
        assertNull(bardoRiletto.getProvenienza());
        assertNull(bardoRiletto.getClasse());
        assertNull(riletto.getResidente("DA_DIMENTICARE"), "i residenti di prima si sostituiscono");
        assertEquals(0, riletto.getNumeroDisponibili(), "quelli da distribuire non si salvano");
    }

    @Test
    void reimpostareDimenticaTutto() {
        RegistroPersonaggiMD registro = new RegistroPersonaggiMD();
        registro.aggiungiPersonaggio(personaggio(TipoPersonaggio.GOBLIN));
        registro.addPersonaggioInLocazione(personaggio(TipoPersonaggio.TROLL), new CoordinateMD(1, 1));
        registro.aggiungiResidente("BARDO_DELLE_LOCANDE", new ResidenteMD("Lino", null, null));
        registro.reimposta();
        assertEquals(0, registro.getNumeroDisponibili());
        assertTrue(registro.getUbicazioniPersonaggi().isEmpty());
        assertTrue(registro.getResidenti().isEmpty());
    }
}
