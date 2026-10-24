package com.threeamigos.foresta.artefatti.leggendari;

import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.artefatti.tipi.StatoPezzoDelSet;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RegoleSetLeggendariTest {

    private static final String SET = "ROMY_JONA";

    private static List<ArtefattoMD> pezziDelSet() {
        return CatalogoLeggendari.getPezzi(SET).stream()
                .map(chiave -> CatalogoLeggendari.getLeggendario(chiave).orElseThrow(AssertionError::new).costruisci(1).getModelloDati())
                .collect(Collectors.toList());
    }

    private static ArtefattoMD senzaSet() {
        return new GeneratoreArtefattiTabelle(new Random(1)).generaArtefatto(TipoArtefatto.SPADA, 2).getModelloDati();
    }

    private static ModificatoreAttributoMD modificatore(TipoModificatore tipo, double quantita, BersaglioModificatore bersaglio) {
        return new ModificatoreAttributoMD(TipoAttributo.FORZA, tipo, quantita, "", UnitaDurataModificatore.PERMANENTE, 0, bersaglio);
    }

    @Test
    void contaIPezziIndossati() {
        List<ArtefattoMD> pezzi = pezziDelSet();
        assertEquals(0, RegoleSetLeggendari.pezziIndossati(Collections.emptyList(), SET));
        assertEquals(2, RegoleSetLeggendari.pezziIndossati(pezzi.subList(0, 2), SET));
        assertEquals(0, RegoleSetLeggendari.pezziIndossati(Collections.singletonList(senzaSet()), SET));
    }

    @Test
    void ilSetECompletoSoloConTuttiIPezzi() {
        List<ArtefattoMD> pezzi = pezziDelSet();
        assertTrue(RegoleSetLeggendari.isCompleto(pezzi, SET));
        assertFalse(RegoleSetLeggendari.isCompleto(pezzi.subList(1, pezzi.size()), SET));
        assertFalse(RegoleSetLeggendari.isCompleto(pezzi, "NON_C_E"));
    }

    @Test
    void ilMoltiplicatoreVaSoloAiSetCompleti() {
        List<ArtefattoMD> pezzi = pezziDelSet();
        assertEquals(1.5, RegoleSetLeggendari.moltiplicatore(pezzi, pezzi.get(0)), 0.0001);
        assertEquals(1.0, RegoleSetLeggendari.moltiplicatore(pezzi.subList(0, 2), pezzi.get(0)), 0.0001);
        assertEquals(1.0, RegoleSetLeggendari.moltiplicatore(pezzi, senzaSet()), 0.0001);
    }

    @Test
    void siMoltiplicanoSoloIBonusDelPortatore() {
        ModificatoreAttributoMD fisso = modificatore(TipoModificatore.VARIAZIONE_FISSA, 4, BersaglioModificatore.PORTATORE);
        ModificatoreAttributoMD percentuale = modificatore(TipoModificatore.VARIAZIONE_PERCENTUALE, 10, BersaglioModificatore.PORTATORE);
        assertEquals(6, RegoleSetLeggendari.applica(fisso, 1.5).getQuantita(), 0.0001);
        assertEquals(15, RegoleSetLeggendari.applica(percentuale, 1.5).getQuantita(), 0.0001);
        assertSame(fisso, RegoleSetLeggendari.applica(fisso, 1.0));
        ModificatoreAttributoMD malus = modificatore(TipoModificatore.VARIAZIONE_FISSA, -4, BersaglioModificatore.PORTATORE);
        assertSame(malus, RegoleSetLeggendari.applica(malus, 1.5));
        ModificatoreAttributoMD sulBersaglio = modificatore(TipoModificatore.VARIAZIONE_FISSA, 4, BersaglioModificatore.BERSAGLIO);
        assertSame(sulBersaglio, RegoleSetLeggendari.applica(sulBersaglio, 1.5));
        ModificatoreAttributoMD assoluto = modificatore(TipoModificatore.QUANTITA_ASSOLUTA, 4, BersaglioModificatore.PORTATORE);
        assertSame(assoluto, RegoleSetLeggendari.applica(assoluto, 1.5));
    }

    @Test
    void descriveIlSetEISuoiTipi() {
        ArtefattoMD pezzo = pezziDelSet().get(0);
        assertEquals("Pezzo del Corredo di RomyJona (bonus x1,5)", RegoleSetLeggendari.descrizioneSet(pezzo).orElseThrow(AssertionError::new));
        assertTrue(RegoleSetLeggendari.tipiDelSet(pezzo).orElseThrow(AssertionError::new).startsWith(TipoArtefatto.SPADA.getDescrizione()));
        assertFalse(RegoleSetLeggendari.descrizioneSet(senzaSet()).isPresent());
        assertFalse(RegoleSetLeggendari.tipiDelSet(senzaSet()).isPresent());
    }

    @Test
    void dicePerOgniPezzoDoveSta() {
        List<ArtefattoMD> pezzi = pezziDelSet();
        ArtefattoMD indossato = pezzi.get(0);
        ArtefattoMD delGruppo = pezzi.get(1);
        ArtefattoMD dellAltro = pezzi.get(2);
        Collection<Collection<ArtefattoMD>> equipaggiamenti = Arrays.asList(
                Collections.singletonList(indossato), Collections.singletonList(dellAltro));
        List<RegoleSetLeggendari.Pezzo> stati = RegoleSetLeggendari.pezzi(indossato, equipaggiamenti, Collections.singletonList(delGruppo));
        assertEquals(pezzi.size(), stati.size());
        assertEquals(StatoPezzoDelSet.INDOSSATO, stati.get(0).getStato());
        assertEquals(StatoPezzoDelSet.DEL_GRUPPO, stati.get(1).getStato());
        assertEquals(StatoPezzoDelSet.DEL_GRUPPO, stati.get(2).getStato());
        assertEquals(StatoPezzoDelSet.DA_TROVARE, stati.get(3).getStato());
        RegoleSetLeggendari.Pezzo primo = stati.get(0);
        assertEquals(indossato.getPezzoLeggendario(), primo.getChiave());
        assertEquals(TipoArtefatto.SPADA, primo.getTipo());
        assertTrue(primo.getNome().startsWith("la Spada"));
    }

    @Test
    void unPezzoCheNessunoIndossaEDelGruppoODaTrovare() {
        List<ArtefattoMD> pezzi = pezziDelSet();
        List<RegoleSetLeggendari.Pezzo> stati = RegoleSetLeggendari.pezzi(pezzi.get(0), Collections.emptyList(), Collections.singletonList(pezzi.get(0)));
        assertEquals(StatoPezzoDelSet.DEL_GRUPPO, stati.get(0).getStato());
        assertEquals(StatoPezzoDelSet.DA_TROVARE, stati.get(1).getStato());
        assertTrue(RegoleSetLeggendari.pezzi(senzaSet(), Collections.emptyList(), Collections.emptyList()).isEmpty());
    }

    @Test
    void trovaISetCompleti() {
        List<ArtefattoMD> pezzi = new ArrayList<>(pezziDelSet());
        pezzi.add(senzaSet());
        assertEquals(1, RegoleSetLeggendari.setCompleti(pezzi).size());
        assertEquals(SET, RegoleSetLeggendari.setCompleti(pezzi).get(0).getChiave());
        assertTrue(RegoleSetLeggendari.setCompleti(pezzi.subList(0, 2)).isEmpty());
        assertTrue(RegoleSetLeggendari.setCompleti(Collections.emptyList()).isEmpty());
    }

    @Test
    void descriveUnSetCompletoSenzaArticolo() {
        assertEquals("Set completo: Corredo di RomyJona (bonus x1,5)",
                RegoleSetLeggendari.descrizioneSetCompleto(CatalogoLeggendari.getSet(SET).orElseThrow(AssertionError::new)));
        for (String articolo : new String[]{"il ", "lo ", "la ", "i ", "gli ", "le ", "l'", ""}) {
            SetLeggendario set = SetLeggendario.da("CHIAVE=A;NOME=" + articolo + "banda;MOLTIPLICATORE=2");
            assertEquals("Set completo: Banda (bonus x2)", RegoleSetLeggendari.descrizioneSetCompleto(set), articolo);
        }
    }
}
