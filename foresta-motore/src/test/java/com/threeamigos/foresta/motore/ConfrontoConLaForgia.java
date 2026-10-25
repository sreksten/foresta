package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.missioni.richieste.ForgiaRichiesta;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Quanto cambia uno scontro se il gruppo ha l'arma forgiata (vedi LaForgia), con gli stessi gruppi, livelli e avversari di
 * TestScontriDiGruppo: senza (con l'arma tipica di ogni classe), solo il capo con un'arma forgiata del suo stesso tipo, tutti con
 * un'arma forgiata. Serve a tarare i valori di Costanti.FORGIA_* e quelli di ForgiaRichiesta.
 * <p>
 * Si lancia a mano: {@code mvn test -pl foresta-motore -am -Dtest=ConfrontoConLaForgia -Dforgia.confronto=true -Dsurefire.failIfNoSpecifiedTests=false}
 */
class ConfrontoConLaForgia {

    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    private static final int NESSUNO = 0;
    private static final int SOLO_IL_CAPO = 1;
    private static final int TUTTI = 2;

    @EnabledIfSystemProperty(named = "forgia.confronto", matches = "true")
    @Test
    void confronto() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            List<List<TipoPersonaggio>> gruppi = Arrays.asList(
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.ELFO, TipoPersonaggio.MAGO));
            List<TipoPersonaggio> boss = Arrays.asList(TipoPersonaggio.IDRA, TipoPersonaggio.MINOTAURO_GIGANTE,
                    TipoPersonaggio.STREGA, TipoPersonaggio.LICH, TipoPersonaggio.DRAGO);
            int iterazioni = Integer.getInteger("forgia.iterazioni", 300);
            for (int livello : new int[]{3, 5, 8}) {
                out.printf("%n== livello %d: vittorie (percentuale) senza arma forgiata / solo il capo / tutti (round medi in parentesi)%n", livello);
                List<Object[]> avversari = new ArrayList<>();
                for (TipoPersonaggio b : boss) {
                    avversari.add(new Object[]{b, 1});
                }
                avversari.add(new Object[]{TipoPersonaggio.TROLL, -1});
                avversari.add(new Object[]{TipoPersonaggio.GOBLIN, -1});
                for (Object[] avversario : avversari) {
                    TipoPersonaggio classe = (TipoPersonaggio) avversario[0];
                    int fissa = (Integer) avversario[1];
                    StringBuilder riga = new StringBuilder(String.format("%-14s", classe + (fissa > 0 ? "" : " (max)")));
                    for (List<TipoPersonaggio> gruppo : gruppi) {
                        int quantita = fissa > 0 ? fissa : Math.min(LocazioneBase.numeroMassimoDiMostri(livello, gruppo.size()), 4);
                        riga.append(" | ").append(gruppo.size()).append("pg x").append(quantita).append(": ");
                        for (int modo : new int[]{NESSUNO, SOLO_IL_CAPO, TUTTI}) {
                            TestScontriDiGruppo simulatore = new TestScontriDiGruppo();
                            simulatore.ritocco = (personaggio, posto) -> forgia(personaggio, posto, modo);
                            int vittorie = 0;
                            double turni = 0;
                            for (int i = 0; i < iterazioni; i++) {
                                TestScontriDiGruppo.Esito esito = simulatore.scontro(gruppo, classe, quantita, livello, 0, 0);
                                if (esito.vittoria) {
                                    vittorie++;
                                }
                                turni += esito.turni;
                            }
                            riga.append(String.format("%5.1f%% (%4.1f)%s", 100.0d * vittorie / iterazioni, turni / iterazioni,
                                    modo == TUTTI ? "" : "  /  "));
                        }
                    }
                    out.println(riga);
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    /**
     * Sostituisce l'arma principale del personaggio con una forgiata dello stesso tipo, se il modo lo vuole per lui.
     */
    private static void forgia(Personaggio personaggio, int posto, int modo) {
        if (modo == NESSUNO || modo == SOLO_IL_CAPO && posto != 0) {
            return;
        }
        Optional<Artefatto> arma = personaggio.getInventario().stream()
                .filter(a -> a.getTipo().getCategoria() == CategoriaArtefatto.ARMA).findFirst();
        if (!arma.isPresent()) {
            return;
        }
        ForgiaRichiesta riga;
        do {
            riga = ForgiaRichiesta.da(ProduttoreDiMissioniCasuale.rigaDiMissioni("FORGIA"));
        } while (riga.getTipo() != arma.get().getTipo());
        personaggio.removeArtefatto(arma.get());
        personaggio.addArtefatto(riga.leggendario(personaggio.getLivello()).costruisci(Statistiche.getLivello()));
    }
}
