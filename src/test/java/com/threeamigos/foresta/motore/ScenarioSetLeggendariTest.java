package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.missioni.SetLeggendario;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.SupertipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoModificatore;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I set leggendari: i pezzi di un set sono le righe di leggendari.txt che lo nominano, e chi li indossa tutti ha i
 * bonus di ognuno moltiplicati, finché li indossa. I test non nominano né set né pezzi: li prendono dal catalogo, e i
 * valori attesi li calcolano dalla grammatica, così nomi, descrizioni, valori e composizione dei set si possono
 * cambiare senza toccarli.
 */
class ScenarioSetLeggendariTest {

    /**
     * Gli attributi primari, che nessun altro attributo cambia, con il modo di leggerli.
     */
    private static final Map<TipoAttributo, ToIntFunction<Personaggio>> PRIMARI = new EnumMap<>(TipoAttributo.class);

    static {
        PRIMARI.put(TipoAttributo.FORZA, Personaggio::getForza);
        PRIMARI.put(TipoAttributo.DESTREZZA, Personaggio::getDestrezza);
        PRIMARI.put(TipoAttributo.COSTITUZIONE, Personaggio::getCostituzione);
        PRIMARI.put(TipoAttributo.INTELLIGENZA, Personaggio::getIntelligenza);
        PRIMARI.put(TipoAttributo.SAGGEZZA, Personaggio::getSaggezza);
        PRIMARI.put(TipoAttributo.CARISMA, Personaggio::getCarisma);
    }

    @Test
    void ogniSetHaIPezziCheLoNominanoESiDescriveConIDatiDellaGrammatica() {
        assertFalse(CatalogoLeggendari.getTuttiISet().isEmpty());
        for (SetLeggendario set : CatalogoLeggendari.getTuttiISet().values()) {
            List<String> pezzi = pezziDi(set);
            assertTrue(pezzi.size() >= 2, set.getChiave());
            // I pezzi sono proprio i leggendari che nominano il set
            assertEquals(CatalogoLeggendari.getTuttiILeggendari().values().stream()
                            .filter(leggendario -> set.getChiave().equals(leggendario.getSet()))
                            .map(OggettoLeggendario::getChiave).sorted().collect(Collectors.toList()),
                    pezzi.stream().sorted().collect(Collectors.toList()));
            for (String pezzo : pezzi) {
                ArtefattoMD md = Leggendari.con(pezzo).costruisci().getModelloDati();
                assertEquals(set.getChiave(), md.getSetLeggendario());
                assertEquals(pezzo, md.getPezzoLeggendario());
                String descrizione = RegoleSetLeggendari.descrizioneSet(md).orElseThrow(AssertionError::new);
                assertTrue(descrizione.startsWith("Pezzo ") && descrizione.endsWith("(bonus x" + italiano(set.getMoltiplicatore()) + ")"),
                        descrizione);
                // I tipi dei pezzi, nell'ordine dei tipi
                assertEquals(pezzi.stream().map(p -> Leggendari.con(p).getTipo().getDescrizione()).collect(Collectors.joining(", ")),
                        RegoleSetLeggendari.tipiDelSet(md).orElseThrow(AssertionError::new));
            }
            for (int i = 1; i < pezzi.size(); i++) {
                assertTrue(Leggendari.con(pezzi.get(i - 1)).getTipo().ordinal() <= Leggendari.con(pezzi.get(i)).getTipo().ordinal());
            }
        }
        // Un leggendario che non è di un set non ha niente da dire
        CatalogoLeggendari.getTuttiILeggendari().values().stream().filter(leggendario -> leggendario.getSet() == null).findFirst()
                .ifPresent(leggendario -> assertEquals(Optional.empty(), RegoleSetLeggendari.descrizioneSet(leggendario.costruisci().getModelloDati())));
        assertTrue(CatalogoLeggendari.getPezzi("NON_ESISTE").isEmpty());
    }

    @Test
    void ilSetDiUnArtefattoSopravviveAlSalvataggio() throws Exception {
        ArtefattoMD pezzo = Leggendari.con(pezziDi(unSet()).get(0)).costruisci().getModelloDati();
        ArtefattoMD riletto = salvaERileggi(pezzo);
        assertEquals(pezzo.getSetLeggendario(), riletto.getSetLeggendario());
        assertEquals(pezzo.getPezzoLeggendario(), riletto.getPezzoLeggendario());
        assertEquals(pezzo.getModificatori(), riletto.getModificatori());

        ArtefattoMD comune = new ArtefattoMD();
        comune.setTipo(pezzo.getTipo());
        comune.setNome("un artefatto qualsiasi");
        ArtefattoMD rilettoComune = salvaERileggi(comune);
        assertNull(rilettoComune.getSetLeggendario());
        assertNull(rilettoComune.getPezzoLeggendario());
    }

    @Test
    void siMoltiplicanoSoloIBonusFissiEPercentuali() {
        ModificatoreAttributo fisso = new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 4);
        ModificatoreAttributo percentuale = new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 20);
        ModificatoreAttributo malus = new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, -2);
        ModificatoreAttributo assoluto = new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.QUANTITA_ASSOLUTA, 10);
        assertEquals(6.0d, RegoleSetLeggendari.applica(fisso, 1.5d).getQuantita());
        assertEquals(30.0d, RegoleSetLeggendari.applica(percentuale, 1.5d).getQuantita());
        assertSame(malus, RegoleSetLeggendari.applica(malus, 1.5d));
        assertSame(assoluto, RegoleSetLeggendari.applica(assoluto, 1.5d));
        assertSame(fisso, RegoleSetLeggendari.applica(fisso, 1.0d));
    }

    @Test
    void conTuttiIPezziIBonusSiMoltiplicanoETogliendoneUnoTornanoQuelliDiPrima() {
        // Un set e un attributo primario su cui i suoi pezzi hanno solo modificatori fissi, almeno uno positivo
        SetLeggendario set = null;
        TipoAttributo attributo = null;
        for (SetLeggendario candidato : setInOrdine()) {
            for (TipoAttributo primario : PRIMARI.keySet()) {
                List<ModificatoreAttributo> modificatori = modificatoriSu(pezziDi(candidato), primario);
                if (!modificatori.isEmpty() && modificatori.stream().allMatch(m -> m.getTipoModificatoreAttributo() == TipoModificatore.AUMENTO_FISSO)
                        && modificatori.stream().anyMatch(m -> m.getQuantita() > 0)) {
                    set = candidato;
                    attributo = primario;
                    break;
                }
            }
            if (set != null) {
                break;
            }
        }
        assertNotNull(set, "serve un set con un bonus fisso su un attributo primario");
        ToIntFunction<Personaggio> valore = PRIMARI.get(attributo);
        List<String> pezzi = pezziDi(set);
        List<String> tranneLUltimo = pezzi.subList(0, pezzi.size() - 1);

        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(201)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio capo = senzaArtefatti(partita.gruppo().getCapo());
            int base = valore.applyAsInt(capo);

            // Tutti i pezzi tranne l'ultimo: i bonus sono quelli scritti
            for (String pezzo : tranneLUltimo) {
                capo.addArtefatto(Leggendari.con(pezzo).costruisci());
            }
            int incompleto = (int) (base + somma(modificatoriSu(tranneLUltimo, attributo), 1.0d));
            assertEquals(incompleto, valore.applyAsInt(capo));
            assertFalse(RegoleSetLeggendari.isCompleto(capo.getModelloDati().getArtefatti(), set.getChiave()));

            // Con l'ultimo il set è completo: i bonus si moltiplicano, i malus no
            Artefatto ultimo = Leggendari.con(pezzi.get(pezzi.size() - 1)).costruisci();
            capo.addArtefatto(ultimo);
            assertTrue(RegoleSetLeggendari.isCompleto(capo.getModelloDati().getArtefatti(), set.getChiave()));
            assertEquals((int) (base + somma(modificatoriSu(pezzi, attributo), set.getMoltiplicatore())), valore.applyAsInt(capo),
                    attributo + " con il set " + set.getChiave());

            // Tolto l'ultimo, si torna a prima
            capo.removeArtefatto(ultimo);
            assertEquals(incompleto, valore.applyAsInt(capo));
        }
    }

    @Test
    void conTuttiIPezziAncheLeResistenzeDegliIncantamentiSiMoltiplicano() {
        // Un set con un pezzo difensivo incantato, e il tipo di danno da cui protegge
        SetLeggendario set = null;
        TipoDanno tipoDanno = null;
        for (SetLeggendario candidato : setInOrdine()) {
            for (String pezzo : pezziDi(candidato)) {
                Artefatto artefatto = Leggendari.con(pezzo).costruisci();
                if (isPezzoDifensivo(artefatto) && !artefatto.getIncantamenti().isEmpty()) {
                    set = candidato;
                    tipoDanno = artefatto.getIncantamenti().iterator().next().getTipoDannoElementale();
                    break;
                }
            }
            if (set != null) {
                break;
            }
        }
        assertNotNull(set, "serve un set con un pezzo difensivo incantato");

        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(202)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio capo = senzaArtefatti(partita.gruppo().getCapo());
            // Gli stessi pezzi, ma senza set: l'unica differenza è il moltiplicatore
            List<Artefatto> copie = new ArrayList<>();
            for (String pezzo : pezziDi(set)) {
                Artefatto copia = Leggendari.con(pezzo).costruisci();
                copia.getModelloDati().setPezzoDiSetLeggendario(null, null);
                capo.addArtefatto(copia);
                copie.add(copia);
            }
            double senzaSet = CalcolatoreCombattimento.difesaContro(capo, tipoDanno);
            copie.forEach(capo::removeArtefatto);
            for (String pezzo : pezziDi(set)) {
                capo.addArtefatto(Leggendari.con(pezzo).costruisci());
            }
            assertTrue(CalcolatoreCombattimento.difesaContro(capo, tipoDanno) > senzaSet, set.getChiave() + " contro " + tipoDanno);
        }
    }

    @Test
    void diOgniPezzoSiSaSeLoIndossaChiIndossaQuestoSeCeLHaIlGruppoOSeVaTrovato() {
        List<String> pezzi = pezziDi(unSet());
        ArtefattoMD primo = Leggendari.con(pezzi.get(0)).costruisci().getModelloDati();
        ArtefattoMD secondo = Leggendari.con(pezzi.get(1)).costruisci().getModelloDati();
        // Uno indossa il primo pezzo, un altro il secondo; gli altri non li ha nessuno
        List<Collection<ArtefattoMD>> equipaggiamenti = Arrays.asList(
                new ArrayList<>(Collections.singletonList(primo)), new ArrayList<>(Collections.singletonList(secondo)));

        List<RegoleSetLeggendari.Pezzo> dalPrimo = RegoleSetLeggendari.pezzi(primo, equipaggiamenti, Collections.emptyList());
        assertEquals(pezzi, dalPrimo.stream().map(RegoleSetLeggendari.Pezzo::getChiave).collect(Collectors.toList()));
        assertEquals(RegoleSetLeggendari.StatoPezzo.INDOSSATO, dalPrimo.get(0).getStato());
        assertEquals(RegoleSetLeggendari.StatoPezzo.DEL_GRUPPO, dalPrimo.get(1).getStato());
        dalPrimo.subList(2, dalPrimo.size()).forEach(pezzo -> assertEquals(RegoleSetLeggendari.StatoPezzo.DA_TROVARE, pezzo.getStato()));

        List<RegoleSetLeggendari.Pezzo> dalSecondo = RegoleSetLeggendari.pezzi(secondo, equipaggiamenti, Collections.emptyList());
        assertEquals(RegoleSetLeggendari.StatoPezzo.DEL_GRUPPO, dalSecondo.get(0).getStato());
        assertEquals(RegoleSetLeggendari.StatoPezzo.INDOSSATO, dalSecondo.get(1).getStato());

        // Un pezzo nell'inventario del gruppo, che nessuno indossa, è del gruppo
        List<RegoleSetLeggendari.Pezzo> conInventario = RegoleSetLeggendari.pezzi(primo, Collections.singletonList(
                new ArrayList<>(Collections.singletonList(primo))), Collections.singletonList(secondo));
        assertEquals(RegoleSetLeggendari.StatoPezzo.DEL_GRUPPO, conInventario.get(1).getStato());
    }

    @Test
    void unSetCompletoSiDiceConIlSuoBonus() {
        SetLeggendario set = unSet();
        List<ArtefattoMD> tutti = pezziDi(set).stream().map(pezzo -> Leggendari.con(pezzo).costruisci().getModelloDati())
                .collect(Collectors.toList());
        assertTrue(RegoleSetLeggendari.setCompleti(tutti.subList(1, tutti.size())).isEmpty());
        List<SetLeggendario> completi = RegoleSetLeggendari.setCompleti(tutti);
        assertEquals(1, completi.size());
        assertEquals(set.getChiave(), completi.get(0).getChiave());
        String descrizione = RegoleSetLeggendari.descrizioneSetCompleto(set);
        assertTrue(descrizione.startsWith("Set completo: ") && descrizione.endsWith("(bonus x" + italiano(set.getMoltiplicatore()) + ")"),
                descrizione);
    }

    // --- Aiuti

    private static List<SetLeggendario> setInOrdine() {
        return CatalogoLeggendari.getTuttiISet().values().stream()
                .sorted((a, b) -> a.getChiave().compareTo(b.getChiave()))
                .collect(Collectors.toList());
    }

    private static SetLeggendario unSet() {
        return setInOrdine().get(0);
    }

    private static List<String> pezziDi(SetLeggendario set) {
        return new ArrayList<>(CatalogoLeggendari.getPezzi(set.getChiave()));
    }

    private static List<ModificatoreAttributo> modificatoriSu(List<String> pezzi, TipoAttributo attributo) {
        return pezzi.stream()
                .flatMap(pezzo -> Leggendari.con(pezzo).costruisci().getModificatori().stream())
                .filter(modificatore -> modificatore.getTipoAttributo() == attributo)
                .collect(Collectors.toList());
    }

    /**
     * La somma dei modificatori fissi, con i bonus moltiplicati e i malus no.
     */
    private static double somma(List<ModificatoreAttributo> modificatori, double moltiplicatore) {
        return modificatori.stream()
                .mapToDouble(m -> m.getQuantita() > 0 ? m.getQuantita() * moltiplicatore : m.getQuantita())
                .sum();
    }

    private static boolean isPezzoDifensivo(Artefatto artefatto) {
        SupertipoArtefatto supertipo = artefatto.getTipo().getSupertipo();
        return supertipo == SupertipoArtefatto.SCUDO || supertipo == SupertipoArtefatto.ELMO
                || supertipo == SupertipoArtefatto.ARMATURA || supertipo == SupertipoArtefatto.SCHINIERI;
    }

    private static String italiano(double numero) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ITALIAN)).format(numero);
    }

    private static ArtefattoMD salvaERileggi(ArtefattoMD artefatto) throws Exception {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            artefatto.salva(stream);
        }
        ArtefattoMD riletto = new ArtefattoMD();
        riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));
        return riletto;
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
