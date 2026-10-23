package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.abilita.AbilitaInnate;
import com.threeamigos.foresta.abilita.ImposizioneDelleMani;
import com.threeamigos.foresta.abilita.Rigenerazione;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.modellodati.tattiche.VoceMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.tattiche.Decisione;
import com.threeamigos.foresta.tattiche.RegistroTattiche;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Analisi manuale (si esegue con {@code -Danalisi=true}) di Chierico e Druido: quanto pesano le loro abilità innate sul
 * combattimento, a confronto con gruppi senza di loro o con un Sacerdote o un Bardo al loro posto.
 * <ul>
 * <li>Tabella dei numeri: salute, magia, quante volte si può usare un'abilità con la magia piena e quanto vale in salute.</li>
 * <li>Scontri: i miei agiscono con la loro tattica predefinita (come il gioco), con il round a coppie di {@link RoundASimulato}, senza
 * pozioni né pergamene, a salute e magia piene a inizio scontro. Per ogni gruppo: vittorie, round, caduti, salute residua del gruppo, e per le
 * abilità quante volte le si usa a scontro e quanta magia si spende.</li>
 * </ul>
 */
class AnalisiAbilitaInnateTest {

    private static final int TURNI_MASSIMI = 200;

    /**
     * La tattica con cui agisce un mio personaggio: di norma quella predefinita della sua classe
     */
    private java.util.function.Function<Personaggio, TatticaMD> tattica = pg -> RegistroTattiche.predefinitaDelGiocabile(pg.getClasse());
    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    private static final Equipaggiamento MAZZA_E_ARMATURA = Equipaggiamento.di("MAZZA_E_ARMATURA",
            Equipaggiamento.Pezzo.di(TipoArtefatto.MAZZA), Equipaggiamento.Pezzo.di(TipoArtefatto.SCUDO),
            Equipaggiamento.Pezzo.di(TipoArtefatto.ELMO), Equipaggiamento.Pezzo.di(TipoArtefatto.ARMATURA));

    /**
     * Cosa succede in uno scontro, per le abilità
     */
    static final class Esito {
        boolean vittoria;
        int round;
        int caduti;
        double saluteResidua;
        final Map<TipoAzione, Integer> azioni = new EnumMap<>(TipoAzione.class);
        final Map<TipoAzione, Integer> magiaSpesa = new EnumMap<>(TipoAzione.class);
        double magiaSpesaTotale;
        double magiaDisponibile;
    }

    @Test
    @EnabledIfSystemProperty(named = "analisi", matches = "true")
    void analisi() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            tabellaDeiNumeri(out);
            scontri(out);
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    /**
     * Una sequenza di scontri di seguito, con il solo recupero di fine locazione (quello del gioco, LocazioneBase: salute e magia
     * rigenerate, senza riposo né pozioni né pergamene): la magia regge o finisce? Quanto si usano le abilità quando la magia è
     * contata?
     */
    @Test
    @EnabledIfSystemProperty(named = "analisi", matches = "true")
    void catene() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            int iterazioni = Integer.getInteger("iterazioni", 200);
            int scontriInCatena = Integer.getInteger("catena", 12);
            List<TipoPersonaggio> nemici = Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.HOBGOBLIN, TipoPersonaggio.TROLL,
                    TipoPersonaggio.MINOTAURO, TipoPersonaggio.STREGA, TipoPersonaggio.GARGOYLE, TipoPersonaggio.SCHELETRO,
                    TipoPersonaggio.IDRA);
            List<List<TipoPersonaggio>> gruppi = Arrays.asList(
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.CHIERICO),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.DRUIDO),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.SACERDOTE),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.BARDO),
                    Arrays.asList(TipoPersonaggio.CHIERICO, TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO),
                    Arrays.asList(TipoPersonaggio.DRUIDO, TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO));
            for (int livello : new int[]{3, 5, 8}) {
                out.printf("%n== CATENE livello %d: %d scontri di seguito con il solo recupero di fine locazione (%d catene)%n",
                        livello, scontriInCatena, iterazioni);
                out.printf("%-22s | %8s %8s %8s | %s%n", "gruppo", "catena", "scontri", "salute", "per lanciatore: magia a inizio scontro, lanci/scontro, magia spesa/scontro (recupero/scontro)");
                for (List<TipoPersonaggio> gruppo : gruppi) {
                    double completate = 0, scontriFatti = 0, saluteFinale = 0;
                    Map<TipoPersonaggio, double[]> magia = new LinkedHashMap<>();
                    Map<TipoPersonaggio, Map<TipoAzione, double[]>> lanci = new LinkedHashMap<>();
                    for (int i = 0; i < iterazioni; i++) {
                        ModelloDati.setIstanza(new ModelloDati());
                        List<Personaggio> pgs = new ArrayList<>();
                        for (TipoPersonaggio classe : gruppo) {
                            Personaggio pg = FabbricaPersonaggi.crea(classe, livello);
                            equipaggia(pg, classe);
                            pgs.add(pg);
                        }
                        int fatti = 0;
                        boolean finita = true;
                        for (int k = 0; k < scontriInCatena; k++) {
                            TipoPersonaggio classe = nemici.get(Dado.tira(nemici.size()) - 1);
                            int quantita = classe == TipoPersonaggio.IDRA ? 1
                                    : Math.min(Dado.tira(LocazioneBase.numeroMassimoDiMostri(livello, gruppo.size())), 3);
                            List<Personaggio> avversari = new ArrayList<>();
                            for (int q = 0; q < quantita; q++) {
                                avversari.add(FabbricaPersonaggi.crea(classe, livello));
                            }
                            for (Personaggio pg : pgs) {
                                if (AbilitaInnate.conosciutaDa(pg.getClasse()) || pg.getClasse().getRuolo() == TipoPersonaggio.MAGO.getRuolo()) {
                                    double[] m = magia.computeIfAbsent(pg.getClasse(), c -> new double[3]);
                                    m[0] += (double) pg.getMagia() / pg.getMagiaMassima();
                                    m[1]++;
                                    m[2] += pg.getRigenerazioneMagia();
                                }
                            }
                            Esito e = new Esito();
                            // I lanci vanno ricondotti al lanciatore: per le abilità c'è un solo chierico o druido nel gruppo
                            giocaScontro(pgs, avversari, e);
                            for (Personaggio pg : pgs) {
                                if (AbilitaInnate.conosciutaDa(pg.getClasse())) {
                                    Map<TipoAzione, double[]> mm = lanci.computeIfAbsent(pg.getClasse(), c -> new EnumMap<>(TipoAzione.class));
                                    for (Map.Entry<TipoAzione, Integer> a : e.azioni.entrySet()) {
                                        if (interessante(a.getKey()) && a.getKey() != TipoAzione.DARDO_ARCANO && a.getKey() != TipoAzione.CANTO_CURATIVO) {
                                            double[] v = mm.computeIfAbsent(a.getKey(), x -> new double[2]);
                                            v[0] += a.getValue();
                                            v[1] += e.magiaSpesa.getOrDefault(a.getKey(), 0);
                                        }
                                    }
                                }
                            }
                            fatti++;
                            if (!e.vittoria) {
                                finita = false;
                                break;
                            }
                            for (Personaggio pg : pgs) {
                                if (pg.isVivo()) {
                                    pg.addSalute(pg.getRigenerazioneSalute());
                                    pg.addMagia(pg.getRigenerazioneMagia());
                                }
                            }
                        }
                        scontriFatti += fatti;
                        if (finita) {
                            completate++;
                            double salute = 0, massima = 0;
                            for (Personaggio pg : pgs) {
                                salute += pg.isVivo() ? pg.getSalute() : 0;
                                massima += pg.getSaluteMassima();
                            }
                            saluteFinale += salute / massima;
                        }
                    }
                    StringBuilder riga = new StringBuilder(String.format("%-22s | %7.1f%% %8.1f %7.0f%% |", sigla(gruppo),
                            100.0 * completate / iterazioni, scontriFatti / iterazioni, completate == 0 ? 0 : 100.0 * saluteFinale / completate));
                    for (Map.Entry<TipoPersonaggio, double[]> m : magia.entrySet()) {
                        riga.append(String.format("  %s: magia %.0f%% inizio", m.getKey().name().substring(0, 3), 100.0 * m.getValue()[0] / m.getValue()[1]));
                        Map<TipoAzione, double[]> mm = lanci.get(m.getKey());
                        if (mm != null) {
                            double scontriTotali = m.getValue()[1];
                            double spesa = 0;
                            for (Map.Entry<TipoAzione, double[]> a : mm.entrySet()) {
                                riga.append(String.format(" %s %.2f", a.getKey().name().substring(0, 4), a.getValue()[0] / scontriTotali));
                                spesa += a.getValue()[1];
                            }
                            riga.append(String.format(" (spende %.1f, recupera %.1f)", spesa / scontriTotali, m.getValue()[2] / scontriTotali));
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
     * Quanto pesa ogni abilità: lo stesso gruppo, con la tattica predefinita del chierico o del druido a cui si disattivano le voci di
     * un'abilità (o di tutte). Nella tabella, vittorie e salute residua del gruppo contro gli stessi avversari.
     */
    @Test
    @EnabledIfSystemProperty(named = "analisi", matches = "true")
    void contributo() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            int iterazioni = Integer.getInteger("iterazioni", 200);
            List<TipoPersonaggio> avversari = Arrays.asList(TipoPersonaggio.IDRA, TipoPersonaggio.MINOTAURO_GIGANTE, TipoPersonaggio.STREGA,
                    TipoPersonaggio.LICH, TipoPersonaggio.DRAGO, TipoPersonaggio.TROLL, TipoPersonaggio.GOBLIN);
            // {classe dell'incantatore, nomi (parti) delle voci da togliere}: il primo di ogni gruppo è la variante piena
            Object[][] varianti = {
                    {TipoPersonaggio.CHIERICO, "piene", new String[0]},
                    {TipoPersonaggio.CHIERICO, "senza imposizione", new String[]{"Imposizione"}},
                    {TipoPersonaggio.CHIERICO, "senza abilità", new String[]{"Imposizione", "Scacciare"}},
                    {TipoPersonaggio.DRUIDO, "piene", new String[0]},
                    {TipoPersonaggio.DRUIDO, "senza pelle", new String[]{"Pelle"}},
                    {TipoPersonaggio.DRUIDO, "senza rigenerazione", new String[]{"Rigenerazione"}},
                    {TipoPersonaggio.DRUIDO, "senza radici/sciame", new String[]{"Radici"}},
                    {TipoPersonaggio.DRUIDO, "senza abilità", new String[]{"Pelle", "Rigenerazione", "Radici"}}};
            for (boolean aQuattro : new boolean[]{true, false}) {
                for (int livello : new int[]{3, 5, 8}) {
                    out.printf("%n== CONTRIBUTO livello %d, gruppo %s (%d scontri): vittorie / salute residua%n", livello,
                            aQuattro ? "GUE+LAD+MAG+X" : "X+GUE+LAD", iterazioni);
                    StringBuilder testata = new StringBuilder(String.format("%-20s", "avversari"));
                    for (Object[] v : varianti) {
                        testata.append(String.format(" | %-7s %-19s", v[0].toString().substring(0, 3), v[1]));
                    }
                    out.println(testata);
                    for (TipoPersonaggio classe : avversari) {
                        boolean boss = classe != TipoPersonaggio.TROLL && classe != TipoPersonaggio.GOBLIN;
                        StringBuilder riga = new StringBuilder(String.format("%-20s", classe + (boss ? "" : " (max)")));
                        for (Object[] v : varianti) {
                            TipoPersonaggio x = (TipoPersonaggio) v[0];
                            String[] togli = (String[]) v[2];
                            tattica = pg -> {
                                TatticaMD base = RegistroTattiche.predefinitaDelGiocabile(pg.getClasse());
                                if (pg.getClasse() != x || togli.length == 0) {
                                    return base;
                                }
                                List<VoceMD> voci = new ArrayList<>();
                                for (VoceMD voce : base.getVoci()) {
                                    boolean daTogliere = false;
                                    for (String parte : togli) {
                                        daTogliere |= voce.getNome() != null && voce.getNome().contains(parte);
                                    }
                                    voci.add(daTogliere ? voce.disattivata() : voce);
                                }
                                return new TatticaMD(voci);
                            };
                            List<TipoPersonaggio> gruppo = aQuattro
                                    ? Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, x)
                                    : Arrays.asList(x, TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO);
                            int quantita = boss ? 1 : Math.min(LocazioneBase.numeroMassimoDiMostri(livello, gruppo.size()), 3);
                            int vittorie = 0;
                            double salute = 0;
                            for (int i = 0; i < iterazioni; i++) {
                                Esito e = scontro(gruppo, classe, quantita, livello);
                                if (e.vittoria) {
                                    vittorie++;
                                    salute += e.saluteResidua;
                                }
                            }
                            riga.append(String.format(" | %5.1f%% %4.0f%%%12s", 100.0 * vittorie / iterazioni,
                                    vittorie == 0 ? 0 : 100.0 * salute / vittorie, ""));
                        }
                        out.println(riga);
                    }
                }
            }
        } finally {
            tattica = pg -> RegistroTattiche.predefinitaDelGiocabile(pg.getClasse());
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    /**
     * Una prova stretta, da ripetere a ogni variante dei numeri delle abilità: il Chierico e il Druido come capo di un gruppo di tre
     * (con Guerriero e Ladro) contro pochi avversari che li mettono alla prova. Una riga per livello: vittorie, salute residua e lanci
     * medi per scontro delle abilità.
     */
    @Test
    @EnabledIfSystemProperty(named = "analisi", matches = "true")
    void sensibilita() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            int iterazioni = Integer.getInteger("iterazioni", 300);
            List<TipoPersonaggio> avversari = Arrays.asList(TipoPersonaggio.IDRA, TipoPersonaggio.MINOTAURO_GIGANTE, TipoPersonaggio.TROLL,
                    TipoPersonaggio.STREGA, TipoPersonaggio.DRAGO);
            List<TipoPersonaggio> classi = new ArrayList<>();
            for (String nome : System.getProperty("classi", "CHIERICO,DRUIDO").split(",")) {
                classi.add(TipoPersonaggio.valueOf(nome));
            }
            for (TipoPersonaggio x : classi) {
                for (int livello : new int[]{3, 5, 8}) {
                    StringBuilder riga = new StringBuilder(String.format("SENS %-9s L%d |", x, livello));
                    double sommaVittorie = 0;
                    Map<TipoAzione, Double> lanci = new EnumMap<>(TipoAzione.class);
                    for (TipoPersonaggio classe : avversari) {
                        boolean boss = classe == TipoPersonaggio.IDRA || classe == TipoPersonaggio.MINOTAURO_GIGANTE || classe == TipoPersonaggio.DRAGO
                                || classe == TipoPersonaggio.STREGA;
                        List<TipoPersonaggio> gruppo = Arrays.asList(x, TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO);
                        int quantita = boss ? 1 : Math.min(LocazioneBase.numeroMassimoDiMostri(livello, gruppo.size()), 3);
                        int vittorie = 0;
                        double salute = 0;
                        for (int i = 0; i < iterazioni; i++) {
                            Esito e = scontro(gruppo, classe, quantita, livello);
                            if (e.vittoria) {
                                vittorie++;
                                salute += e.saluteResidua;
                            }
                            for (Map.Entry<TipoAzione, Integer> a : e.azioni.entrySet()) {
                                if (interessante(a.getKey()) && a.getKey() != TipoAzione.DARDO_ARCANO) {
                                    lanci.merge(a.getKey(), (double) a.getValue(), Double::sum);
                                }
                            }
                        }
                        sommaVittorie += vittorie;
                        riga.append(String.format(" %s %5.1f%%/%2.0f%%", classe.name().substring(0, 3), 100.0 * vittorie / iterazioni,
                                vittorie == 0 ? 0 : 100.0 * salute / vittorie));
                    }
                    riga.append(String.format(" | media %5.1f%% | lanci/scontro", 100.0 * sommaVittorie / (iterazioni * avversari.size())));
                    for (Map.Entry<TipoAzione, Double> l : lanci.entrySet()) {
                        riga.append(String.format(" %s %.2f", l.getKey().name().substring(0, 4), l.getValue() / (iterazioni * avversari.size())));
                    }
                    out.println(riga);
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    private void tabellaDeiNumeri(PrintStream out) {
        out.printf("%n== Numeri di partenza (personaggio creato dalla fabbrica, senza equipaggiamento)%n");
        out.printf("%-10s %4s %7s %7s | %-30s%n", "classe", "liv", "salute", "magia", "note");
        for (int livello : new int[]{1, 3, 5, 8, 10}) {
            for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.CHIERICO, TipoPersonaggio.DRUIDO, TipoPersonaggio.SACERDOTE,
                    TipoPersonaggio.BARDO, TipoPersonaggio.MAGO, TipoPersonaggio.GUERRIERO)) {
                Personaggio pg = FabbricaPersonaggi.crea(classe, livello);
                String note = "";
                if (classe == TipoPersonaggio.CHIERICO) {
                    ImposizioneDelleMani im = new ImposizioneDelleMani(pg);
                    int lanci = pg.getMagiaMassima() / im.getCostoLancio();
                    note = String.format("imposizione: cura %d (=%d%% della salute) x %d lanci a magia piena = %d salute (%d volte la salute)",
                            im.getCura(), 100 * im.getCura() / pg.getSaluteMassima(), lanci, lanci * im.getCura(),
                            lanci * im.getCura() / pg.getSaluteMassima());
                } else if (classe == TipoPersonaggio.DRUIDO) {
                    Rigenerazione ri = new Rigenerazione(pg);
                    int lanci = pg.getMagiaMassima() / ri.getCostoLancio();
                    int perTurno = ri.getCuraPerTurno(pg);
                    note = String.format("rigenerazione su se stesso: %d/turno x4 = %d, %d lanci a magia piena; pelle (5): %d lanci",
                            perTurno, 4 * perTurno, lanci, pg.getMagiaMassima() / 5);
                }
                out.printf("%-10s %4d %7d %7d | %s%n", classe, livello, pg.getSaluteMassima(), pg.getMagiaMassima(), note);
            }
        }
    }

    private void scontri(PrintStream out) {
        List<List<TipoPersonaggio>> gruppi = Arrays.asList(
                Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO),
                Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.CHIERICO),
                Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.DRUIDO),
                Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.SACERDOTE),
                Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.BARDO),
                Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.CHIERICO, TipoPersonaggio.DRUIDO),
                Arrays.asList(TipoPersonaggio.CHIERICO, TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO),
                Arrays.asList(TipoPersonaggio.DRUIDO, TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO));
        List<TipoPersonaggio> avversari = Arrays.asList(TipoPersonaggio.IDRA, TipoPersonaggio.MINOTAURO_GIGANTE, TipoPersonaggio.STREGA,
                TipoPersonaggio.LICH, TipoPersonaggio.DRAGO, TipoPersonaggio.TROLL, TipoPersonaggio.GOBLIN);
        int iterazioni = Integer.getInteger("iterazioni", 200);
        for (int livello : new int[]{3, 5, 8}) {
            out.printf("%n== livello %d: vittorie / round / caduti / salute residua del gruppo (%d scontri)%n", livello, iterazioni);
            StringBuilder testata = new StringBuilder(String.format("%-22s", "avversari"));
            for (List<TipoPersonaggio> gruppo : gruppi) {
                testata.append(String.format(" | %-24s", sigla(gruppo)));
            }
            out.println(testata);
            Map<List<TipoPersonaggio>, Map<TipoAzione, double[]>> abilita = new LinkedHashMap<>();
            for (TipoPersonaggio classe : avversari) {
                boolean boss = classe != TipoPersonaggio.TROLL && classe != TipoPersonaggio.GOBLIN;
                StringBuilder riga = new StringBuilder(String.format("%-22s", classe + (boss ? "" : " (max)")));
                for (List<TipoPersonaggio> gruppo : gruppi) {
                    int quantita = boss ? 1 : Math.min(LocazioneBase.numeroMassimoDiMostri(livello, gruppo.size()), 3);
                    int vittorie = 0;
                    double round = 0, caduti = 0, salute = 0;
                    Map<TipoAzione, double[]> somma = abilita.computeIfAbsent(gruppo, g -> new EnumMap<>(TipoAzione.class));
                    double spesaTotale = 0, disponibile = 0;
                    for (int i = 0; i < iterazioni; i++) {
                        Esito e = scontro(gruppo, classe, quantita, livello);
                        if (e.vittoria) {
                            vittorie++;
                            salute += e.saluteResidua;
                        }
                        round += e.round;
                        caduti += e.caduti;
                        for (Map.Entry<TipoAzione, Integer> a : e.azioni.entrySet()) {
                            double[] v = somma.computeIfAbsent(a.getKey(), k -> new double[2]);
                            v[0] += a.getValue();
                            v[1] += e.magiaSpesa.getOrDefault(a.getKey(), 0);
                        }
                        spesaTotale += e.magiaSpesaTotale;
                        disponibile += e.magiaDisponibile;
                    }
                    riga.append(String.format(" | %5.1f%% %5.1fr %3.1fc %4.0f%%", 100.0 * vittorie / iterazioni, round / iterazioni,
                            caduti / iterazioni, vittorie == 0 ? 0 : 100.0 * salute / vittorie));
                }
                out.println(riga);
            }
            out.printf("-- abilità usate per scontro (media su tutti gli avversari), e magia spesa (media per scontro)%n");
            for (Map.Entry<List<TipoPersonaggio>, Map<TipoAzione, double[]>> g : abilita.entrySet()) {
                double scontriTotali = (double) iterazioni * avversari.size();
                StringBuilder sb = new StringBuilder(String.format("%-24s", sigla(g.getKey())));
                for (Map.Entry<TipoAzione, double[]> a : g.getValue().entrySet()) {
                    if (!interessante(a.getKey())) {
                        continue;
                    }
                    sb.append(String.format("  %s %.2f (mana %.1f)", a.getKey(), a.getValue()[0] / scontriTotali, a.getValue()[1] / scontriTotali));
                }
                out.println(sb);
            }
        }
    }

    private static boolean interessante(TipoAzione azione) {
        switch (azione) {
            case IMPOSIZIONE_DELLE_MANI:
            case SCACCIARE_I_NON_MORTI:
            case RADICI:
            case SCIAME:
            case PELLE_DI_CORTECCIA:
            case RIGENERAZIONE:
            case CANTO_CURATIVO:
            case DARDO_ARCANO:
                return true;
            default:
                return false;
        }
    }

    private static String sigla(List<TipoPersonaggio> gruppo) {
        StringBuilder sb = new StringBuilder();
        for (TipoPersonaggio classe : gruppo) {
            if (sb.length() > 0) {
                sb.append('+');
            }
            sb.append(classe.name(), 0, 3);
        }
        return sb.toString();
    }

    private Esito scontro(List<TipoPersonaggio> classi, TipoPersonaggio classeAvversari, int quantita, int livello) {
        ModelloDati.setIstanza(new ModelloDati());
        List<Personaggio> gruppo = new ArrayList<>();
        for (TipoPersonaggio classe : classi) {
            Personaggio pg = FabbricaPersonaggi.crea(classe, livello);
            equipaggia(pg, classe);
            gruppo.add(pg);
        }
        List<Personaggio> avversari = new ArrayList<>();
        for (int i = 0; i < quantita; i++) {
            avversari.add(FabbricaPersonaggi.crea(classeAvversari, livello));
        }
        Esito esito = new Esito();
        giocaScontro(gruppo, avversari, esito);
        return esito;
    }

    /**
     * Uno scontro fra i personaggi e gli avversari dati, in cui i miei agiscono con la loro tattica predefinita; riempie l'esito
     */
    private void giocaScontro(List<Personaggio> gruppo, List<Personaggio> avversari, Esito esito) {
        for (Personaggio p : gruppo) {
            esito.magiaDisponibile += p.getMagia();
        }
        RoundASimulato round = new RoundASimulato(gruppo, avversari);
        RoundASimulato.Decisore decisore = (chi, r) -> {
            int magiaPrima = chi.getMagia();
            Decisione d = r.agisciConLaTattica(chi, tattica.apply(chi));
            if (d != null) {
                TipoAzione tipo = d.getTipoEseguito();
                esito.azioni.merge(tipo, 1, Integer::sum);
                int spesa = Math.max(0, magiaPrima - chi.getMagia());
                esito.magiaSpesa.merge(tipo, spesa, Integer::sum);
                esito.magiaSpesaTotale += spesa;
            }
        };
        for (int turno = 1; turno <= TURNI_MASSIMI; turno++) {
            esito.round = turno;
            RoundASimulato.Esito fine = round.eseguiRound(decisore);
            if (fine == RoundASimulato.Esito.VITTORIA) {
                esito.vittoria = true;
                break;
            }
            if (fine == RoundASimulato.Esito.SCONFITTA) {
                break;
            }
        }
        double salute = 0, massima = 0;
        for (Personaggio p : gruppo) {
            if (!p.isVivo()) {
                esito.caduti++;
            } else {
                salute += p.getSalute();
            }
            massima += p.getSaluteMassima();
        }
        esito.saluteResidua = salute / massima;
    }

    private static void equipaggia(Personaggio pg, TipoPersonaggio classe) {
        switch (classe.getRuolo()) {
            case CHIERICO:
                MAZZA_E_ARMATURA.equipaggia(pg);
                break;
            case DRUIDO:
                Equipaggiamento.BASTONE_LIBRO_E_VESTE.equipaggia(pg);
                break;
            case SACERDOTE:
                if (MAZZA_E_ARMATURA.equipaggia(pg).isPresent()) {
                    Equipaggiamento.BASTONE_LIBRO_E_VESTE.equipaggia(pg);
                }
                break;
            default:
                Equipaggiamento.tipiciPer(classe).get(0).equipaggia(pg);
        }
    }
}
