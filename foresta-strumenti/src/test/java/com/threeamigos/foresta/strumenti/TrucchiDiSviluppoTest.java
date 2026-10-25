package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrucchiDiSviluppoTest {

    private final TrucchiDiSviluppo trucchi = TrucchiDiSviluppo.istanza();

    @AfterEach
    void ripristina() {
        trucchi.spegniTutto();
        Dado.impostaTruccatore(null);
        Dado.ripristina();
    }

    @Test
    void senzaTruccatoreIlLancioConContestoVaAlCaso() {
        for (int i = 0; i < 50; i++) {
            int lancio = Dado.tira(ContestoLancio.CARTA_FORBICI_SASSO, 3);
            assertTrue(lancio >= 1 && lancio <= 3);
        }
    }

    @Test
    void unTruccoAccesoDaSempreLoStessoRisultatoFinoAlloSpegnimento() {
        trucchi.installa();
        trucchi.accendiNumero(ContestoLancio.NUMERO_MOSTRI, 4);
        for (int i = 0; i < 20; i++) {
            assertEquals(4, Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1, 6));
        }
        assertTrue(trucchi.isAcceso(ContestoLancio.NUMERO_MOSTRI));
        trucchi.spegni(ContestoLancio.NUMERO_MOSTRI);
        assertFalse(trucchi.isAcceso(ContestoLancio.NUMERO_MOSTRI));
    }

    @Test
    void unTruccoNonTocaIAltriContesti() {
        trucchi.installa();
        trucchi.accendiNumero(ContestoLancio.NUMERO_MOSTRI, 6);
        boolean tuttiSei = true;
        for (int i = 0; i < 100 && tuttiSei; i++) {
            tuttiSei = Dado.tira(ContestoLancio.COFANO_ESITO, 0, 7) == 6;
        }
        assertFalse(tuttiSei);
    }

    @Test
    void unValoreFuoriDalLancioVieneIgnorato() {
        trucchi.installa();
        trucchi.accendiNumero(ContestoLancio.CARTA_FORBICI_SASSO, 9);
        for (int i = 0; i < 20; i++) {
            int lancio = Dado.tira(ContestoLancio.CARTA_FORBICI_SASSO, 3);
            assertTrue(lancio >= 1 && lancio <= 3);
        }
    }

    @Test
    void unaFacciaSolaNonConsultaIlTruccatore() {
        trucchi.installa();
        trucchi.accendiNumero(ContestoLancio.NUMERO_MOSTRI, 1);
        assertEquals(1, Dado.tiraAncheAUnaFaccia(ContestoLancio.NUMERO_MOSTRI, 1));
    }

    @Test
    void laCodaDeiTestHaLaPrecedenza() {
        trucchi.installa();
        trucchi.accendiNumero(ContestoLancio.NUMERO_MOSTRI, 4);
        Dado.trucca(2);
        assertEquals(2, Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1, 6));
        assertEquals(4, Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1, 6));
    }

    @Test
    void laScelteVoluteSiApplicanoSoloSeAmmesse() {
        trucchi.installa();
        List<String> ammessi = Arrays.asList("GOBLIN", "DRAGO", "LICH");
        trucchi.accendiElemento(ContestoLancio.TIPO_MOSTRO, "DRAGO");
        assertEquals("DRAGO", Dado.scegli(ContestoLancio.TIPO_MOSTRO, ammessi));
        assertEquals("DRAGO", Dado.sceltaVoluta(ContestoLancio.TIPO_MOSTRO, ammessi).get());
        // Non ammesso in questa locazione: la scelta va al caso
        List<String> senzaDrago = Arrays.asList("GOBLIN", "LICH");
        assertTrue(senzaDrago.contains(Dado.scegli(ContestoLancio.TIPO_MOSTRO, senzaDrago)));
        assertFalse(Dado.sceltaVoluta(ContestoLancio.TIPO_MOSTRO, senzaDrago).isPresent());
    }

    @Test
    void senzaTruccoNessunaSceltaVoluta() {
        trucchi.installa();
        assertFalse(Dado.sceltaVoluta(ContestoLancio.COFANO_OGGETTO, Arrays.asList("A", "B")).isPresent());
    }

    @Test
    void ilTipoDelContestoDeveCorrispondere() {
        assertThrows(IllegalArgumentException.class, () -> trucchi.accendiNumero(ContestoLancio.TIPO_MOSTRO, 1));
        assertThrows(IllegalArgumentException.class, () -> trucchi.accendiElemento(ContestoLancio.NUMERO_MOSTRI, "x"));
    }

    @Test
    void ilNumeroEL_ElementoVolutiSiLeggono() {
        trucchi.accendiNumero(ContestoLancio.NUMERO_MOSTRI, 4);
        assertEquals(4, trucchi.getNumero(ContestoLancio.NUMERO_MOSTRI).getAsInt());
        assertFalse(trucchi.getNumero(ContestoLancio.COFANO_ESITO).isPresent());
        assertFalse(trucchi.getElemento(ContestoLancio.TIPO_MOSTRO).isPresent());

        trucchi.accendiElemento(ContestoLancio.TIPO_MOSTRO, "goblin");
        assertEquals("goblin", trucchi.getElemento(ContestoLancio.TIPO_MOSTRO).get());
        assertTrue(trucchi.isAcceso(ContestoLancio.TIPO_MOSTRO), "acceso anche con il solo elemento");

        trucchi.spegni(ContestoLancio.TIPO_MOSTRO);
        assertFalse(trucchi.getElemento(ContestoLancio.TIPO_MOSTRO).isPresent());
    }

    @Test
    void unTruccoFuoriDalLancioNonSiApplicaNeSopraNeSotto() {
        trucchi.accendiNumero(ContestoLancio.NUMERO_MOSTRI, 4);
        assertTrue(trucchi.lancio(ContestoLancio.NUMERO_MOSTRI, 1, 6).isPresent());
        assertFalse(trucchi.lancio(ContestoLancio.NUMERO_MOSTRI, 5, 9).isPresent(), "sotto il minimo");
        assertFalse(trucchi.lancio(ContestoLancio.NUMERO_MOSTRI, 1, 3).isPresent(), "sopra il massimo");
        assertFalse(trucchi.lancio(ContestoLancio.COFANO_ESITO, 1, 6).isPresent(), "nessun trucco su quel contesto");
    }
}
