package com.threeamigos.foresta.artefatti.leggendari;

import com.threeamigos.foresta.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoLeggendariTest {

    private static final String SET = "CHIAVE=S;NOME=il Set;MOLTIPLICATORE=1.5";

    @AfterEach
    void ripristina() {
        CatalogoLeggendari.reimposta();
    }

    private static String pezzo(String chiave, String tipo, String set) {
        return "TIPO=" + tipo + ";NOME=un pezzo;CHIAVE=" + chiave + ";BREVE=un pezzo;DESCRIZIONE=che c'è;LIVELLO=2;COSTO=10;PESO=1;"
                + (TipoArtefatto.valueOf(tipo).getCategoria() == CategoriaArtefatto.ARMA ? "DANNI=5;" : "") + (set == null ? "" : "SET=" + set + ";")
                + "LEGGENDA=Una.;LEGGENDA=Due.";
    }

    private static void rifiuta(List<String> set, List<String> leggendari, List<String> bottini, String messaggio) {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> CatalogoLeggendari.caricaDa(set, leggendari, bottini));
        assertTrue(e.getMessage().contains(messaggio), e.getMessage());
    }

    private static com.threeamigos.foresta.modellodati.ArtefattoMD costruito(String chiave) {
        return CatalogoLeggendari.getLeggendario(chiave).orElseThrow(AssertionError::new).costruisci(1).getModelloDati();
    }

    private static List<String> l(String... righe) {
        return Arrays.asList(righe);
    }

    @Test
    void ilCatalogoDelGiocoSiLegge() {
        assertFalse(CatalogoLeggendari.getTuttiILeggendari().isEmpty());
        assertFalse(CatalogoLeggendari.getTuttiIBottini().isEmpty());
        assertFalse(CatalogoLeggendari.getTuttiISet().isEmpty());
        assertTrue(CatalogoLeggendari.getLeggendario("SPADA_DI_ROMY_JONA").isPresent());
        assertTrue(CatalogoLeggendari.getSet("ROMY_JONA").isPresent());
        assertTrue(CatalogoLeggendari.getPezzi("ROMY_JONA").contains("SPADA_DI_ROMY_JONA"));
        assertTrue(CatalogoLeggendari.getPezzi("NON_C_E").isEmpty());
        assertFalse(CatalogoLeggendari.getLeggendario("NON_C_E").isPresent());
        assertFalse(CatalogoLeggendari.getSet("NON_C_E").isPresent());
    }

    @Test
    void leggereIlCatalogoNonConsumaITiriDelDado() {
        // Il catalogo si legge una volta per esecuzione: se consumasse tiri, due partite con lo stesso seme sarebbero diverse a seconda
        // che fosse già stato letto o no (vedi ProduttoreDiArtefattiCasuale.tutteLeRigheLeggendarie)
        com.threeamigos.foresta.strumenti.dado.Dado.impostaSeme(1);
        int senzaLeggere = com.threeamigos.foresta.strumenti.dado.Dado.tira(1_000_000);
        com.threeamigos.foresta.strumenti.dado.Dado.impostaSeme(1);
        CatalogoLeggendari.reimposta();
        assertFalse(CatalogoLeggendari.getTuttiILeggendari().isEmpty());
        assertEquals(senzaLeggere, com.threeamigos.foresta.strumenti.dado.Dado.tira(1_000_000));
    }

    @Test
    void ileggendariDiBottinoSiTrovanoPerChiave() {
        String chiave = CatalogoLeggendari.getTuttiIBottini().keySet().iterator().next();
        assertTrue(CatalogoLeggendari.getLeggendario(chiave).isPresent());
        assertFalse(CatalogoLeggendari.getTuttiILeggendari().containsKey(chiave));
    }

    @Test
    void ipezziDiUnSetStannoInOrdineDiTipo() {
        CatalogoLeggendari.caricaDa(l(SET), l(pezzo("B_SCUDO", "SCUDO", "S"), pezzo("A_SPADA", "SPADA", "S"), pezzo("C_ELMO", "ELMO", "S"),
                pezzo("SENZA_SET", "SCUDO", null)), l(pezzo("BOTTINO", "SPADA", null)));
        assertEquals(Arrays.asList("A_SPADA", "B_SCUDO", "C_ELMO"), Arrays.asList(CatalogoLeggendari.getPezzi("S").toArray()));
        assertTrue(CatalogoLeggendari.getLeggendario("BOTTINO").isPresent());
    }

    @Test
    void gliAccessoriDiUnSetSonoQuantiSiVuole() {
        CatalogoLeggendari.caricaDa(l(SET), l(pezzo("A", "ANELLO", "S"), pezzo("B", "ANELLO", "S")), Collections.emptyList());
        assertEquals(2, CatalogoLeggendari.getPezzi("S").size());
    }

    @Test
    void unaChiaveRipetutaSiRifiuta() {
        rifiuta(l(SET, SET), l(), l(), "Set leggendario ripetuto: S");
        rifiuta(l(SET), l(pezzo("A", "SCUDO", "S"), pezzo("A", "SPADA", "S")), l(), "Leggendario ripetuto: A");
        rifiuta(l(SET), l(pezzo("A", "SCUDO", "S"), pezzo("B", "SPADA", "S")), l(pezzo("A", "ELMO", null)), "Leggendario ripetuto: A");
        rifiuta(l(SET), l(pezzo("A", "SCUDO", "S"), pezzo("B", "SPADA", "S")), l(pezzo("C", "ELMO", null), pezzo("C", "ELMO", null)),
                "Leggendario ripetuto: C");
    }

    @Test
    void unSetCheNonCeSiRifiuta() {
        rifiuta(l(SET), l(pezzo("A", "SCUDO", "ALTRO")), l(), "è di un set che non c'è: ALTRO");
    }

    @Test
    void unBottinoPuoEssereDiUnSet() {
        // Un pezzo di bottino lo dà una missione invece di una leggenda, ma conta come gli altri
        CatalogoLeggendari.caricaDa(l(SET), l(pezzo("A", "SCUDO", "S"), pezzo("B", "SPADA", "S")), l(pezzo("C", "ELMO", "S")));
        assertEquals(new HashSet<>(Arrays.asList("A", "B", "C")), CatalogoLeggendari.getPezzi("S"));
        assertFalse(CatalogoLeggendari.getTuttiILeggendari().containsKey("C"));
    }

    @Test
    void unSetConPezziSiCompletaConQuelliChePortaENonServeIndossarliTutti() {
        String parziale = "CHIAVE=P;NOME=il Set Parziale;MOLTIPLICATORE=1.4;PEZZI=2";
        // Tre armi che non si impugnano insieme e degli schinieri di bottino: con PEZZI=2 va bene
        CatalogoLeggendari.caricaDa(l(parziale), l(pezzo("A", "SPADONE", "P"), pezzo("B", "SPADA", "P"), pezzo("C", "BASTONE_MAGICO", "P")),
                l(pezzo("D", "SCHINIERI", "P")));
        assertEquals(4, CatalogoLeggendari.getPezzi("P").size());
        assertEquals(2, CatalogoLeggendari.getPezziRichiesti("P"));
        assertTrue(CatalogoLeggendari.getSet("P").orElseThrow(AssertionError::new).isCompletabileInParte());
        // Uno spadone e gli schinieri completano il set; lo spadone da solo no
        List<com.threeamigos.foresta.modellodati.ArtefattoMD> spadoneESchinieri = Arrays.asList(costruito("A"), costruito("D"));
        assertTrue(RegoleSetLeggendari.isCompleto(spadoneESchinieri, "P"));
        assertFalse(RegoleSetLeggendari.isCompleto(spadoneESchinieri.subList(0, 1), "P"));
        assertEquals(1.4, RegoleSetLeggendari.moltiplicatore(spadoneESchinieri, spadoneESchinieri.get(0)), 0.0001);
        // Senza PEZZI= servono tutti
        CatalogoLeggendari.caricaDa(l(SET), l(pezzo("A", "SCUDO", "S"), pezzo("B", "SPADA", "S")), l());
        assertEquals(2, CatalogoLeggendari.getPezziRichiesti("S"));
        assertFalse(CatalogoLeggendari.getSet("S").orElseThrow(AssertionError::new).isCompletabileInParte());
        assertEquals(0, CatalogoLeggendari.getPezziRichiesti("NON_C_E"));
    }

    @Test
    void unSetNonChiedePiuPezziDiQuantiNeHaNeMenoDiDue() {
        rifiuta(l("CHIAVE=P;NOME=il Set;MOLTIPLICATORE=1.4;PEZZI=3"), l(pezzo("A", "SPADA", "P"), pezzo("B", "ELMO", "P")), l(),
                "chiede più pezzi di quanti ne ha");
        assertThrows(IllegalArgumentException.class, () -> SetLeggendario.da("CHIAVE=P;NOME=il Set;MOLTIPLICATORE=1.4;PEZZI=1"));
        // Anche in un set parziale i pezzi si devono poter indossare
        rifiuta(l("CHIAVE=P;NOME=il Set;MOLTIPLICATORE=1.4;PEZZI=2"), l(pezzo("A", "PERGAMENA", "P"), pezzo("B", "SPADA", "P")), l(),
                "non si indossa");
    }

    @Test
    void unSetHaAlmenoDuePezzi() {
        rifiuta(l(SET), l(pezzo("A", "SCUDO", "S")), l(), "ha meno di due pezzi");
        rifiuta(l(SET), l(), l(), "ha meno di due pezzi");
    }

    @Test
    void ipezziDiUnSetSiDevonoPoterIndossareInsieme() {
        rifiuta(l(SET), l(pezzo("A", "PERGAMENA", "S"), pezzo("B", "SPADA", "S")), l(), "non si indossa");
        rifiuta(l(SET), l(pezzo("A", "SCUDO", "S"), pezzo("B", "SCUDO", "S")), l(), "non si può indossare con gli altri");
        rifiuta(l(SET), l(pezzo("A", "SPADONE", "S"), pezzo("B", "SPADA", "S")), l(), "non si può indossare con gli altri");
        rifiuta(l(SET), l(pezzo("A", "SPADA", "S"), pezzo("B", "SPADONE", "S")), l(), "non si può indossare con gli altri");
        rifiuta(l(SET), l(pezzo("A", "SPADONE", "S"), pezzo("B", "SCUDO", "S")), l(), "non si può indossare con gli altri");
        rifiuta(l(SET), l(pezzo("A", "SCUDO", "S"), pezzo("B", "SPADONE", "S")), l(), "non si può indossare con gli altri");
        rifiuta(l(SET), l(pezzo("A", "SPADONE", "S"), pezzo("B", "SPADONE", "S")), l(), "non si può indossare con gli altri");
    }
}
