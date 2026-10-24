package com.threeamigos.foresta.artefatti.leggendari;

import com.threeamigos.foresta.artefatti.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
    void unBottinoNonPuoEssereDiUnSet() {
        rifiuta(l(SET), l(pezzo("A", "SCUDO", "S"), pezzo("B", "SPADA", "S")), l(pezzo("C", "ELMO", "S")), "non può far parte di un set");
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
