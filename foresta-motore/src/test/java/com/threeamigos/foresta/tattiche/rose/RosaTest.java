package com.threeamigos.foresta.tattiche.rose;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.tattiche.ContestoTattico;

/**
 * Le rose da cui il selettore di una tattica pesca i candidati (tattiche_di_combattimento_automatiche.md §11.3).
 */
class RosaTest {

    private Personaggio guerriero;
    private Personaggio mago;
    private Personaggio ladro;
    private Personaggio bardo;
    private Personaggio goblin1;
    private Personaggio goblin2;
    private Personaggio ostaggio;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        mago = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1);
        ladro = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 1);
        bardo = FabbricaPersonaggi.crea(TipoPersonaggio.BARDO, 1);
        goblin1 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        goblin2 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        ostaggio = FabbricaPersonaggi.crea(TipoPersonaggio.VIANDANTE, 1);
    }

    private ContestoTattico perIMiei(Personaggio attore) {
        return new ContestoTattico(attore, true, Arrays.asList(guerriero, mago, ladro, bardo), Arrays.asList(goblin1, goblin2),
                Collections.singletonList(ostaggio));
    }

    private ContestoTattico perIMostri(Personaggio attore) {
        return new ContestoTattico(attore, false, Arrays.asList(goblin1, goblin2), Arrays.asList(guerriero, mago, ladro, bardo),
                Collections.singletonList(ostaggio));
    }

    private static List<Personaggio> candidati(TipoRosa tipo, ContestoTattico contesto) {
        return Rose.di(tipo).candidati(contesto);
    }

    private static void uccidi(Personaggio personaggio, Personaggio assassino) {
        personaggio.subSalute(1_000_000, assassino, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(personaggio.isVivo());
    }

    @Test
    void ogniTipoDiRosaHaIlSuoRosa() {
        for (TipoRosa tipo : TipoRosa.values()) {
            assertNotNull(Rose.di(tipo), tipo.name());
            assertEquals(tipo, Rose.di(tipo).getTipo());
        }
    }

    @Test
    void seStessoEIlSoloAttore() {
        assertEquals(Collections.singletonList(mago), candidati(TipoRosa.SE_STESSO, perIMiei(mago)));
        assertEquals(Collections.singletonList(goblin2), candidati(TipoRosa.SE_STESSO, perIMostri(goblin2)));
    }

    @Test
    void alleatiEAvversariDipendonoDalLatoDellAttore() {
        assertEquals(Arrays.asList(guerriero, mago, ladro, bardo), candidati(TipoRosa.ALLEATI, perIMiei(mago)));
        assertEquals(Arrays.asList(goblin1, goblin2), candidati(TipoRosa.AVVERSARI, perIMiei(mago)));
        assertEquals(Arrays.asList(goblin1, goblin2), candidati(TipoRosa.ALLEATI, perIMostri(goblin1)));
        assertEquals(Arrays.asList(guerriero, mago, ladro, bardo), candidati(TipoRosa.AVVERSARI, perIMostri(goblin1)));
    }

    @Test
    void ICadutiEChiEInPanchinaNonSonoCandidati() {
        uccidi(ladro, goblin1);
        bardo.setInPanchina(true);
        assertEquals(Arrays.asList(guerriero, mago), candidati(TipoRosa.ALLEATI, perIMiei(mago)));
        assertEquals(Arrays.asList(guerriero, mago), candidati(TipoRosa.AVVERSARI, perIMostri(goblin1)));
    }

    @Test
    void alleatiCadutiSonoSoloIMortiENonChiEInPanchina() {
        uccidi(ladro, goblin1);
        bardo.setInPanchina(true);
        assertEquals(Collections.singletonList(ladro), candidati(TipoRosa.ALLEATI_CADUTI, perIMiei(mago)));
    }

    @Test
    void gliOspitiSonoGliStessiPerTutti() {
        assertEquals(Collections.singletonList(ostaggio), candidati(TipoRosa.OSPITI, perIMiei(mago)));
        assertEquals(Collections.singletonList(ostaggio), candidati(TipoRosa.OSPITI, perIMostri(goblin1)));
    }

    @Test
    void alleatiEOspitiHannoPrimaIPersonaggiESoloPerIMiei() {
        assertEquals(Arrays.asList(guerriero, mago, ladro, bardo, ostaggio), candidati(TipoRosa.ALLEATI_E_OSPITI, perIMiei(mago)));
        assertThrows(IllegalStateException.class, () -> candidati(TipoRosa.ALLEATI_E_OSPITI, perIMostri(goblin1)));
    }

    @Test
    void avversariEOspitiHannoPrimaIPersonaggiESoloPerIMostri() {
        assertEquals(Arrays.asList(guerriero, mago, ladro, bardo, ostaggio), candidati(TipoRosa.AVVERSARI_E_OSPITI, perIMostri(goblin1)));
        assertThrows(IllegalStateException.class, () -> candidati(TipoRosa.AVVERSARI_E_OSPITI, perIMiei(mago)));
    }

    @Test
    void unOspiteCadutoNonECandidato() {
        uccidi(ostaggio, goblin1);
        assertTrue(candidati(TipoRosa.OSPITI, perIMiei(mago)).isEmpty());
        assertEquals(Arrays.asList(guerriero, mago, ladro, bardo), candidati(TipoRosa.ALLEATI_E_OSPITI, perIMiei(mago)));
    }

    @Test
    void ilRisultatoNonSiPuoModificare() {
        assertThrows(UnsupportedOperationException.class, () -> candidati(TipoRosa.ALLEATI, perIMiei(mago)).clear());
        assertThrows(UnsupportedOperationException.class, () -> candidati(TipoRosa.SE_STESSO, perIMiei(mago)).clear());
    }

    @Test
    void ILatiPerCuiUnRosaValeSonoDichiaratiNelTipo() {
        assertTrue(TipoRosa.ALLEATI_E_OSPITI.valePer(true));
        assertFalse(TipoRosa.ALLEATI_E_OSPITI.valePer(false));
        assertTrue(TipoRosa.AVVERSARI_E_OSPITI.valePer(false));
        assertFalse(TipoRosa.AVVERSARI_E_OSPITI.valePer(true));
        for (TipoRosa tipo : new TipoRosa[]{TipoRosa.SE_STESSO, TipoRosa.ALLEATI, TipoRosa.ALLEATI_CADUTI, TipoRosa.AVVERSARI, TipoRosa.OSPITI}) {
            assertTrue(tipo.valePer(true) && tipo.valePer(false), tipo.name());
        }
    }
}
