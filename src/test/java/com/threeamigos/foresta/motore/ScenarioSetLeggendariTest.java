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
        assertEquals(Optional.of("Pezzo della Gioventù Ribelle (bonus x1,3)"), RegoleSetLeggendari.descrizioneSet(giacca));
        // I tipi dei pezzi, in ordine di tipo
        assertEquals(Optional.of("Veste, Maschera"), RegoleSetLeggendari.tipiDelSet(giacca));
        assertEquals(Optional.of("Spada, Elmo, Maschera, Schinieri"),
                RegoleSetLeggendari.tipiDelSet(Leggendari.con("la Spada della Morte").costruisci().getModelloDati()));
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

    @Test
    void diOgniPezzoSiSaSeELoIndossaChiIndossaQuestoSeCeLHaIlGruppoOSeVaTrovato() {
        ArtefattoMD spada = Leggendari.con("la Spada della Morte").costruisci().getModelloDati();
        ArtefattoMD cervice = Leggendari.con("la Cervice Imperturbabile di RomyJona").costruisci().getModelloDati();
        ArtefattoMD maschera = Leggendari.con("la Maschera da Guerra di RomyJona").costruisci().getModelloDati();
        // Uno indossa la spada e la cervice, un altro la maschera; gli scarponi non li ha nessuno
        java.util.List<java.util.Collection<ArtefattoMD>> equipaggiamenti = Arrays.asList(
                new java.util.ArrayList<>(Arrays.asList(spada, cervice)), new java.util.ArrayList<>(Arrays.asList(maschera)));
        java.util.List<RegoleSetLeggendari.Pezzo> pezzi = RegoleSetLeggendari.pezzi(spada, equipaggiamenti, java.util.Collections.emptyList());
        assertEquals(4, pezzi.size());
        assertEquals(RegoleSetLeggendari.StatoPezzo.INDOSSATO, stato(pezzi, "la Spada della Morte"));
        assertEquals(RegoleSetLeggendari.StatoPezzo.INDOSSATO, stato(pezzi, "la Cervice Imperturbabile di RomyJona"));
        assertEquals(RegoleSetLeggendari.StatoPezzo.DEL_GRUPPO, stato(pezzi, "la Maschera da Guerra di RomyJona"));
        assertEquals(RegoleSetLeggendari.StatoPezzo.DA_TROVARE, stato(pezzi, "gli Scarponi Chiodati di RomyJona"));
        // Dal punto di vista della maschera, spada e cervice sono del gruppo
        assertEquals(RegoleSetLeggendari.StatoPezzo.DEL_GRUPPO,
                stato(RegoleSetLeggendari.pezzi(maschera, equipaggiamenti, java.util.Collections.emptyList()), "la Spada della Morte"));
    }

    @Test
    void unSetCompletoSiDiceConIlSuoBonus() {
        ArtefattoMD giacca = Leggendari.con(GIACCA).costruisci().getModelloDati();
        ArtefattoMD occhiali = Leggendari.con(OCCHIALI).costruisci().getModelloDati();
        assertTrue(RegoleSetLeggendari.setCompleti(Arrays.asList(giacca)).isEmpty());
        java.util.List<com.threeamigos.foresta.missioni.SetLeggendario> completi = RegoleSetLeggendari.setCompleti(Arrays.asList(giacca, occhiali));
        assertEquals(1, completi.size());
        assertEquals("Set completo: Gioventù Ribelle (bonus x1,3)", RegoleSetLeggendari.descrizioneSetCompleto(completi.get(0)));
    }

    private static RegoleSetLeggendari.StatoPezzo stato(java.util.List<RegoleSetLeggendari.Pezzo> pezzi, String nome) {
        return pezzi.stream().filter(p -> p.getNome().equals(nome)).findFirst().orElseThrow(AssertionError::new).getStato();
    }
}
