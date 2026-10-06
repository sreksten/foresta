package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.intermezzi.ElementoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.intermezzi.VersoDiDefault;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Una scena in città può avere un ospite che la missione ha portato fin lì (un bardo da riportare a casa, un ostaggio
 * liberato, un colpevole catturato): entra subito dopo il capo, prima degli altri, e guarda il mandante.
 */
class ScenaInCittaConOspiteTest {

    private static final double FINE_INGRESSO = 10;

    private static PartitaDiTest conDueCompagni() {
        PartitaDiTest partita = PartitaDiTest.nuova(7);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
        partita.gruppo().aggiungiPersonaggio(new Guerriero("Primo", 1));
        partita.gruppo().aggiungiPersonaggio(new Guerriero("Secondo", 1));
        return partita;
    }

    private static Viandante conOspite(PartitaDiTest partita) {
        Viandante ospite = new Viandante("Ostaggio", 1);
        partita.gruppo().aggiungiOspite(ospite, true);
        return ospite;
    }

    private static List<String> id(PaginaIntermezzo pagina) {
        return pagina.getElementi().stream().map(ElementoIntermezzo::getId).collect(Collectors.toList());
    }

    @Test
    void senzaOspiteLaScenaEComeSempre() {
        try (PartitaDiTest partita = conDueCompagni()) {
            PaginaIntermezzo pagina = ScenaInCitta.conMoglieDelBardo().parlaIlMandante("Ciao").getPagine().get(0);
            assertEquals(java.util.Arrays.asList("mandante", "personaggio0", "personaggio1", "personaggio2"), id(pagina));
            assertEquals(0.5, pagina.getElemento("personaggio0").getStatoAl(FINE_INGRESSO).getX(), 1e-9);
            assertEquals(0.4, pagina.getElemento("personaggio2").getStatoAl(FINE_INGRESSO).getX(), 1e-9);
        }
    }

    @Test
    void l_ospiteEntraSubitoDopoIlCapoEPrimaDegliAltri() {
        try (PartitaDiTest partita = conDueCompagni()) {
            conOspite(partita);
            PaginaIntermezzo pagina = ScenaInCitta.conMoglieDelBardo()
                    .parlaIlMandante("Di nuovo in queste condizioni!").getPagine().get(0);
            assertEquals(java.util.Arrays.asList("mandante", "personaggio0", "ospite", "personaggio1", "personaggio2"), id(pagina));

            ElementoIntermezzo capo = pagina.getElemento("personaggio0");
            ElementoIntermezzo ospite = pagina.getElemento("ospite");
            ElementoIntermezzo secondo = pagina.getElemento("personaggio1");
            // Si fermano uno dietro l'altro: il capo, poi l'ospite, poi gli altri che sono scalati di un posto
            assertEquals(0.5, capo.getStatoAl(FINE_INGRESSO).getX(), 1e-9);
            assertEquals(0.45, ospite.getStatoAl(FINE_INGRESSO).getX(), 1e-9);
            assertEquals(0.4, secondo.getStatoAl(FINE_INGRESSO).getX(), 1e-9);
            assertEquals(0.35, pagina.getElemento("personaggio2").getStatoAl(FINE_INGRESSO).getX(), 1e-9);
            // E partono nello stesso ordine: all'istante in cui parte l'ospite il capo è già in cammino, e il secondo ancora fermo
            double xPartenza = ospite.getStatoAl(0).getX();
            assertTrue(capo.getStatoAl(0.5).getX() > xPartenza);
            assertEquals(xPartenza, ospite.getStatoAl(0.5).getX(), 1e-9);
            assertEquals(xPartenza, secondo.getStatoAl(1.0).getX(), 1e-9);
        }
    }

    @Test
    void l_ospiteGuardaIlMandante() {
        try (PartitaDiTest partita = conDueCompagni()) {
            conOspite(partita);
            PaginaIntermezzo pagina = ScenaInCitta.conMoglieDelBardo()
                    .parlaIlMandante("Ciao").getPagine().get(0);
            // Il mandante sta a destra e il gruppo arriva da sinistra: chi cammina verso destra guarda a destra, e il
            // bardo (la cui immagine guarda a sinistra) deve essere rovesciato, come il capo
            assertTrue(pagina.getElemento("mandante").getStatoAl(FINE_INGRESSO).getX() > pagina.getElemento("ospite").getStatoAl(FINE_INGRESSO).getX());
            assertEquals(pagina.getElemento("personaggio0").getStatoAl(FINE_INGRESSO).isSpecchiato(),
                    pagina.getElemento("ospite").getStatoAl(FINE_INGRESSO).isSpecchiato());
            assertTrue(pagina.getElemento("ospite").getStatoAl(FINE_INGRESSO).isSpecchiato());
        }
    }

    @Test
    void ilViandanteChePiuMissioniScortanoHaUnVersoEPuoEntrareInScena() {
        try (PartitaDiTest partita = conDueCompagni()) {
            conOspite(partita);
            assertNotNull(VersoDiDefault.di(TipoPersonaggio.VIANDANTE));
            PaginaIntermezzo pagina = ScenaInCitta.conMandante()
                    .parlaIlMandante("Mio marito!").parlaLOspite("Siete stati voi?").getPagine().get(0);
            assertNotNull(pagina.getElemento("ospite"));
            assertEquals(2, pagina.getBattuteProgrammate().size());
        }
    }

    @Test
    void unOspiteMortoNonCompare() {
        try (PartitaDiTest partita = conDueCompagni()) {
            Viandante ospite = conOspite(partita);
            ospite.getModelloDati().setVivo(false);
            assertFalse(ospite.isVivo());
            PaginaIntermezzo pagina = ScenaInCitta.conMandante().parlaIlMandante("Ciao").getPagine().get(0);
            assertNull(pagina.getElemento("ospite"));
            assertThrows(IllegalStateException.class, () -> ScenaInCitta.conMandante().parlaLOspite("Io?"),
                    "senza ospite non c'è chi parla");
        }
    }

    @Test
    void piuOspitiEntranoInFilaDopoIlCapo() {
        try (PartitaDiTest partita = conDueCompagni()) {
            conOspite(partita);
            partita.gruppo().aggiungiOspite(new Viandante("Altro", 1), false);
            PaginaIntermezzo pagina = ScenaInCitta.conMandante().parlaIlMandante("Ciao").getPagine().get(0);
            assertEquals(java.util.Arrays.asList("mandante", "personaggio0", "ospite", "ospite1", "personaggio1", "personaggio2"), id(pagina));
            assertEquals(0.4, pagina.getElemento("ospite1").getStatoAl(FINE_INGRESSO).getX(), 1e-9);
        }
    }
}
