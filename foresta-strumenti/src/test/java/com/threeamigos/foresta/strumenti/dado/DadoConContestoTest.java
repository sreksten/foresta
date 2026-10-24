package com.threeamigos.foresta.strumenti.dado;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I lanci del dado oltre ai casi base: i valori limite e gli errori (che si scrivono nel registro), i lanci con contesto che un
 * truccatore può decidere, e le scelte fra elementi.
 */
class DadoConContestoTest {

    private boolean contestoUsato;

    @AfterEach
    void ripristina() {
        Dado.impostaTruccatore(null);
        Dado.ripristina();
        Dado.impostaSeme(System.nanoTime());
    }

    private static TruccatoreDiDadi truccatore(OptionalInt lancio, Optional<?> scelta) {
        return new TruccatoreDiDadi() {
            @Override
            public OptionalInt lancio(ContestoLancio contesto, int min, int max) {
                return lancio;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi) {
                return (Optional<T>) scelta;
            }
        };
    }

    private static void assertErrore(String atteso, Runnable lancio) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, lancio::run);
        assertTrue(e.getMessage().contains(atteso), e.getMessage());
    }

    @Test
    void conUnaFacciaSolaIlRisultatoENoto() {
        assertEquals(1, Dado.tiraAncheAUnaFaccia(1));
        assertEquals(1, Dado.tiraAncheAUnaFaccia(ContestoLancio.NUMERO_MOSTRI, 1));
        assertEquals(5, Dado.tiraAncheSenzaRange(5, 5));
        int risultato = Dado.tiraAncheAUnaFaccia(6);
        assertTrue(risultato >= 1 && risultato <= 6);
        int senzaRange = Dado.tiraAncheSenzaRange(3, 4);
        assertTrue(senzaRange == 3 || senzaRange == 4);
        int conContesto = Dado.tiraAncheAUnaFaccia(ContestoLancio.NUMERO_MOSTRI, 6);
        assertTrue(conContesto >= 1 && conContesto <= 6);
    }

    @Test
    void unNumeroDiFacceNonValidoEUnErroreCheDiceMetodoEParametri() {
        assertErrore("Dado.tiraAncheAUnaFaccia(0): il numero di facce deve essere maggiore di zero", () -> Dado.tiraAncheAUnaFaccia(0));
        assertErrore("Dado.tira(1): il numero di facce deve essere maggiore di 1", () -> Dado.tira(1));
        assertErrore("Dado.tiraAncheSenzaRange(4, 3): il valore minimo deve essere minore o uguale", () -> Dado.tiraAncheSenzaRange(4, 3));
        assertErrore("Dado.tira(5, 5): il valore minimo deve essere minore del valore massimo", () -> Dado.tira(5, 5));
        assertErrore("Dado.tira(NUMERO_MOSTRI, 5, 5): il valore minimo", () -> Dado.tira(ContestoLancio.NUMERO_MOSTRI, 5, 5));
        assertErrore("Dado.tira(NUMERO_MOSTRI, 1): il numero di facce deve essere maggiore di 1", () -> Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1));
        assertErrore("Dado.tiraAncheAUnaFaccia(NUMERO_MOSTRI, 0): il numero di facce", () -> Dado.tiraAncheAUnaFaccia(ContestoLancio.NUMERO_MOSTRI, 0));
    }

    @Test
    void iTrucchiNonValidiSonoErrori() {
        Dado.trucca(1.5);
        assertThrows(IllegalStateException.class, Dado::probabilita);
        Dado.trucca(-0.1);
        assertThrows(IllegalStateException.class, Dado::probabilita);
        Dado.trucca(0.5);
        assertThrows(IllegalStateException.class, () -> Dado.tira(1, 6), "una probabilità non è un lancio");
        assertThrows(IllegalArgumentException.class, () -> Dado.trucca(5L));
        assertThrows(IllegalArgumentException.class, () -> Dado.trucca((Number) null));
    }

    @Test
    void unTruccoSottoOSopraLIntervalloEUnErrore() {
        Dado.trucca(0);
        assertThrows(IllegalStateException.class, () -> Dado.tira(1, 6));
        Dado.trucca(7);
        assertThrows(IllegalStateException.class, () -> Dado.tira(1, 6));
    }

    @Test
    void ilTruccatoreDecideIlLancioDiUnContesto() {
        Dado.impostaTruccatore(truccatore(OptionalInt.of(4), Optional.empty()));
        assertEquals(4, Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1, 6));
        assertEquals(4, Dado.tira(ContestoLancio.NUMERO_MOSTRI, 6));
        assertEquals(4, Dado.tiraAncheAUnaFaccia(ContestoLancio.NUMERO_MOSTRI, 6));
    }

    @Test
    void unLancioFuoriIntervalloOSenzaParereDelTruccatoreRestaAlCaso() {
        Dado.impostaTruccatore(truccatore(OptionalInt.of(99), Optional.empty()));
        int fuori = Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1, 6);
        assertTrue(fuori >= 1 && fuori <= 6);
        Dado.impostaTruccatore(truccatore(OptionalInt.of(0), Optional.empty()));
        int sotto = Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1, 6);
        assertTrue(sotto >= 1 && sotto <= 6);
        Dado.impostaTruccatore(truccatore(OptionalInt.empty(), Optional.empty()));
        int senza = Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1, 6);
        assertTrue(senza >= 1 && senza <= 6);
        Dado.impostaTruccatore(null);
        int nessuno = Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1, 6);
        assertTrue(nessuno >= 1 && nessuno <= 6);
    }

    @Test
    void laCodaDeiTrucchiHaLaPrecedenzaSulTruccatore() {
        Dado.impostaTruccatore(new TruccatoreDiDadi() {
            @Override
            public OptionalInt lancio(ContestoLancio contesto, int min, int max) {
                contestoUsato = true;
                return OptionalInt.of(4);
            }

            @Override
            public <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi) {
                contestoUsato = true;
                return Optional.of(ammessi.get(0));
            }
        });
        Dado.trucca(2, 3);

        assertEquals(2, Dado.tira(ContestoLancio.NUMERO_MOSTRI, 1, 6));
        assertFalse(contestoUsato);
        assertFalse(Dado.sceltaVoluta(ContestoLancio.TIPO_MOSTRO, Arrays.asList("a", "b")).isPresent());
        assertFalse(contestoUsato, "con un trucco in coda il truccatore non si interpella");
    }

    @Test
    void sceglierFraGliElementiRispettaIlTruccatoreSoloSeLElementoEAmmesso() {
        List<String> ammessi = Arrays.asList("goblin", "orco", "troll");
        Dado.impostaTruccatore(truccatore(OptionalInt.empty(), Optional.of("orco")));
        assertEquals("orco", Dado.scegli(ContestoLancio.TIPO_MOSTRO, ammessi));
        assertEquals(Optional.of("orco"), Dado.sceltaVoluta(ContestoLancio.TIPO_MOSTRO, ammessi));

        Dado.impostaTruccatore(truccatore(OptionalInt.empty(), Optional.of("drago")));
        assertFalse(Dado.sceltaVoluta(ContestoLancio.TIPO_MOSTRO, ammessi).isPresent(), "un elemento non ammesso si ignora");
        assertTrue(ammessi.contains(Dado.scegli(ContestoLancio.TIPO_MOSTRO, ammessi)));

        Dado.impostaTruccatore(null);
        assertFalse(Dado.sceltaVoluta(ContestoLancio.TIPO_MOSTRO, ammessi).isPresent());
        assertTrue(ammessi.contains(Dado.scegli(ContestoLancio.TIPO_MOSTRO, ammessi)));
    }

    @Test
    void nonSiSceglieFraNiente() {
        assertErrore("Dado.scegli(TIPO_MOSTRO): non c'è niente da scegliere", () -> Dado.scegli(ContestoLancio.TIPO_MOSTRO, Collections.<String>emptyList()));
    }
}
