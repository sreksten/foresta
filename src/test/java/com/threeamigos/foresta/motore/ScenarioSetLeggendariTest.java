package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.tipi.TipoDanno;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I set leggendari: i pezzi di un set sono le righe di leggendari.txt che lo nominano, e chi li indossa tutti ha i
 * bonus di ognuno moltiplicati, finché li indossa.
 */
class ScenarioSetLeggendariTest {

    private static final String GIACCA = "la Giacca di Pelle del Ribelle";
    private static final String OCCHIALI = "gli Occhiali da Sole del Ribelle";

    @Test
    void iPezziDiUnSetSonoLeRigheCheLoNominano() {
        assertEquals(new HashSet<>(Arrays.asList("ROMYJONA", "GIOVENTU_RIBELLE", "CAVALIERI_SVOGLIATI", "RECUPERO_CREDITI")),
                CatalogoLeggendari.getTuttiISet().keySet());
        assertEquals(new HashSet<>(Arrays.asList(GIACCA, OCCHIALI)), CatalogoLeggendari.getPezzi("GIOVENTU_RIBELLE"));
        assertEquals(4, CatalogoLeggendari.getPezzi("ROMYJONA").size());
        assertEquals(1.3d, CatalogoLeggendari.getSet("GIOVENTU_RIBELLE").get().getMoltiplicatore());
        assertTrue(CatalogoLeggendari.getPezzi("NON_ESISTE").isEmpty());

        ArtefattoMD giacca = Leggendari.con(GIACCA).costruisci().getModelloDati();
        assertEquals("GIOVENTU_RIBELLE", giacca.getSetLeggendario());
        assertEquals(GIACCA, giacca.getPezzoLeggendario());
        assertEquals(Optional.of("Pezzo della Gioventù Ribelle (2 pezzi, bonus x1,3)"), RegoleSetLeggendari.descrizioneSet(giacca));
        assertEquals(Optional.empty(), RegoleSetLeggendari.descrizioneSet(Leggendari.con("la Gemma del Tramonto Eterno").costruisci().getModelloDati()));
    }

    @Test
    void ilSetDiUnArtefattoSopravviveAlSalvataggio() throws Exception {
        ArtefattoMD occhiali = Leggendari.con(OCCHIALI).costruisci().getModelloDati();
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            occhiali.salva(stream);
        }
        ArtefattoMD riletto = new ArtefattoMD();
        riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));
        assertEquals("GIOVENTU_RIBELLE", riletto.getSetLeggendario());
        assertEquals(OCCHIALI, riletto.getPezzoLeggendario());
        assertEquals(occhiali.getModificatori(), riletto.getModificatori());

        ArtefattoMD comune = new ArtefattoMD();
        comune.setTipo(Leggendari.con(OCCHIALI).getTipo());
        comune.setNome("la maschera di latta");
        StringWriter scrittoComune = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scrittoComune)) {
            comune.salva(stream);
        }
        ArtefattoMD rilettoComune = new ArtefattoMD();
        rilettoComune.leggi(new BufferedReader(new StringReader(scrittoComune.toString())));
        assertNull(rilettoComune.getSetLeggendario());
        assertNull(rilettoComune.getPezzoLeggendario());
    }

    @Test
    void conTuttiIPezziIBonusSiMoltiplicanoMaIMalusNo() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(201)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio capo = senzaArtefatti(partita.gruppo().getCapo());
            int carisma = capo.getCarisma();
            int percezione = capo.getPercezione();

            // La giacca da sola: +3 di Carisma, come scritto
            capo.addArtefatto(Leggendari.con(GIACCA).costruisci());
            assertEquals(carisma + 3, capo.getCarisma());
            assertFalse(RegoleSetLeggendari.isCompleto(capo.getModelloDati().getArtefatti(), "GIOVENTU_RIBELLE"));

            // Con gli occhiali il set è completo: (3 + 4) x 1,3 = 9,1 di Carisma; il -1 di Percezione resta -1
            Artefatto occhiali = Leggendari.con(OCCHIALI).costruisci();
            capo.addArtefatto(occhiali);
            assertTrue(RegoleSetLeggendari.isCompleto(capo.getModelloDati().getArtefatti(), "GIOVENTU_RIBELLE"));
            assertEquals(carisma + 9, capo.getCarisma());
            assertEquals(percezione - 1, capo.getPercezione());

            // Tolti gli occhiali, la giacca torna quella di prima
            capo.removeArtefatto(occhiali);
            assertEquals(carisma + 3, capo.getCarisma());
        }
    }

    @Test
    void conTuttiIPezziAncheLeResistenzeDegliIncantamentiSiMoltiplicano() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(202)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio capo = senzaArtefatti(partita.gruppo().getCapo());
            OggettoLeggendario scudo = Leggendari.con("lo Scudo Fiscale");
            capo.addArtefatto(scudo.costruisci());
            double soloScudo = CalcolatoreCombattimento.difesaContro(capo, TipoDanno.GELO);
            capo.addArtefatto(Leggendari.con("la Mazza dell'Oste Furioso").costruisci());
            assertTrue(RegoleSetLeggendari.isCompleto(capo.getModelloDati().getArtefatti(), "RECUPERO_CREDITI"));
            assertTrue(CalcolatoreCombattimento.difesaContro(capo, TipoDanno.GELO) > soloScudo);
        }
    }

    /**
     * Il personaggio senza niente addosso: nessun altro artefatto deve cambiare gli attributi che si misurano (per
     * esempio portando il Carisma a un valore assoluto).
     */
    private static Personaggio senzaArtefatti(Personaggio personaggio) {
        personaggio.getInventario().forEach(personaggio::removeArtefatto);
        assertTrue(personaggio.getModelloDati().getArtefatti().isEmpty());
        return personaggio;
    }
}
