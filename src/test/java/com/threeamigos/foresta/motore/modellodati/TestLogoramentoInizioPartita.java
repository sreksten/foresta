package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.FaseDiAttacco;
import com.threeamigos.foresta.motore.GestoreProgressione;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Il logoramento a inizio partita: un protagonista di livello 1 con la dotazione di base (EquipaggiamentoIniziale),
 * da solo, con le pozioni iniziali del gruppo (Costanti.POZIONI_SALUTE_INIZIALI) ma senza usare le pergamene, che attraversa una locazione del bosco dopo l'altra. Gli incontri seguono le regole
 * di LocazioneBase.crea (incontriPossibili e numeroMassimoDiMostri, mostri al livello del protagonista, un incontro il
 * 90% delle volte, sempre alla prima locazione); a fine locazione la stanchezza cresce di 1 e la salute recupera
 * getRigenerazioneSalute(), come in Automa; l'esperienza dei mostri uccisi fa salire di livello.
 * <p>
 * Non simula gli effetti di stato (che nel gioco rendono gli scontri più duri), la fuga, la corruzione e l'amicizia.
 * Vedi risorse_e_documenti_vari/analisi_logoramento.md.
 */
public class TestLogoramentoInizioPartita {

    private static final List<TipoPersonaggio> CLASSI = Arrays.asList(TipoPersonaggio.GUERRIERO,
            TipoPersonaggio.LADRO, TipoPersonaggio.ELFO, TipoPersonaggio.BARDO, TipoPersonaggio.MAGO);

    // Quelli di Bosco e Radura
    private static final TipoPersonaggio[] MOSTRI_DEL_BOSCO = {TipoPersonaggio.ARPIA,
            TipoPersonaggio.CENTAURO, TipoPersonaggio.CHIMERA, TipoPersonaggio.CHIMERA_DRAGO,
            TipoPersonaggio.EREMITA, TipoPersonaggio.FOLLETTO, TipoPersonaggio.GIGANTE, TipoPersonaggio.GOBLIN,
            TipoPersonaggio.HOBGOBLIN, TipoPersonaggio.MINOTAURO, TipoPersonaggio.SCHELETRO,
            TipoPersonaggio.TITANO, TipoPersonaggio.TROLL, TipoPersonaggio.VIVERNA};

    private static final int LOCAZIONI_MASSIME = 40;

    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    /**
     * Le regole del gioco, con qualcosa che si può togliere o aggiungere per confronto
     */
    static class Variante {
        final String nome;
        boolean inizioMorbido = true;
        boolean recuperoAFineLocazione = true;
        int pozioni = 0;

        Variante(String nome) {
            this.nome = nome;
        }
    }

    static class EsitoCatena {
        int locazioni;
        int livelloFinale;
    }

    private final Random random = new Random(20261005L);

    @Disabled("Da eseguire manualmente: analisi del logoramento a inizio partita")
    @Test
    void analisi() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            scontriSingoli(out);
            catene(out);
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    /**
     * Per ogni classe e mostro del bosco, uno e due esemplari, a pieno di salute: quante volte vince e quanta salute
     * gli resta quando vince
     */
    private void scontriSingoli(PrintStream out) {
        int iterazioni = 1_000;
        out.println("== Scontri singoli a livello 1, dotazione di base: vittorie% / salute residua% quando vince ==");
        StringBuilder testata = new StringBuilder(String.format("%-16s", "mostro"));
        for (TipoPersonaggio classe : CLASSI) {
            testata.append(String.format(" | %-17s", classe));
        }
        out.println(testata);
        for (TipoPersonaggio mostro : MOSTRI_DEL_BOSCO) {
            for (int quantita = 1; quantita <= Math.min(2, quantitaMassima(mostro)); quantita++) {
                StringBuilder riga = new StringBuilder(String.format("%-13s x%d", mostro, quantita));
                for (TipoPersonaggio classe : CLASSI) {
                    int vittorie = 0;
                    double saluteResidua = 0;
                    for (int i = 0; i < iterazioni; i++) {
                        Personaggio pg = nuovoProtagonista(classe);
                        if (scontro(pg, generaMostri(mostro, quantita, 1), new int[]{0})) {
                            vittorie++;
                            saluteResidua += pg.getSalute() / (double) pg.getSaluteMassima();
                        }
                    }
                    riga.append(String.format(" | %5.1f%% / %5.1f%%", 100.0d * vittorie / iterazioni,
                            vittorie == 0 ? 0.0d : 100.0d * saluteResidua / vittorie));
                }
                out.println(riga);
            }
        }
        out.println();
    }

    /**
     * Una locazione dopo l'altra, finché il protagonista muore: per ogni classe e variante, quante locazioni regge
     */
    private void catene(PrintStream out) {
        int iterazioni = 2_000;
        List<Variante> varianti = new ArrayList<>();
        Variante attuale = new Variante("gioco attuale (" + Costanti.POZIONI_SALUTE_INIZIALI + " pozioni iniziali)");
        attuale.pozioni = Costanti.POZIONI_SALUTE_INIZIALI;
        varianti.add(attuale);
        varianti.add(new Variante("senza pozioni iniziali"));
        Variante senzaRecupero = new Variante("senza recupero a fine locazione");
        senzaRecupero.recuperoAFineLocazione = false;
        varianti.add(senzaRecupero);
        Variante senzaInizioMorbido = new Variante("senza inizio morbido");
        senzaInizioMorbido.inizioMorbido = false;
        varianti.add(senzaInizioMorbido);
        Variante senzaNessuno = new Variante("senza recupero e senza inizio morbido");
        senzaNessuno.recuperoAFineLocazione = false;
        senzaNessuno.inizioMorbido = false;
        varianti.add(senzaNessuno);
        Variante tre = new Variante("gioco attuale + 3 pozioni di salute");
        tre.pozioni = 3;
        varianti.add(tre);

        out.println("== Locazioni attraversate prima di morire (media / mediana / % che arriva a 5 / a 10 / livello medio alla fine) ==");
        for (Variante variante : varianti) {
            out.println("-- " + variante.nome);
            for (TipoPersonaggio classe : CLASSI) {
                List<Integer> locazioni = new ArrayList<>();
                double livelli = 0;
                int arrivaA5 = 0;
                int arrivaA10 = 0;
                for (int i = 0; i < iterazioni; i++) {
                    EsitoCatena esito = catena(classe, variante);
                    locazioni.add(esito.locazioni);
                    livelli += esito.livelloFinale;
                    if (esito.locazioni >= 5) {
                        arrivaA5++;
                    }
                    if (esito.locazioni >= 10) {
                        arrivaA10++;
                    }
                }
                Collections.sort(locazioni);
                double media = locazioni.stream().mapToInt(Integer::intValue).average().orElse(0);
                out.printf("   %-10s media %5.2f  mediana %2d  a 5: %5.1f%%  a 10: %5.1f%%  livello %4.2f%n", classe, media,
                        locazioni.get(locazioni.size() / 2), 100.0d * arrivaA5 / iterazioni,
                        100.0d * arrivaA10 / iterazioni, livelli / iterazioni);
            }
        }
    }

    private EsitoCatena catena(TipoPersonaggio classe, Variante variante) {
        Personaggio pg = nuovoProtagonista(classe);
        int[] pozioni = {variante.pozioni};
        EsitoCatena esito = new EsitoCatena();
        for (int locazione = 1; locazione <= LOCAZIONI_MASSIME; locazione++) {
            boolean incontro = locazione == 1 || random.nextInt(100) < 90;
            if (incontro) {
                int livello = pg.getLivello();
                TipoPersonaggio[] possibili = variante.inizioMorbido
                        ? LocazioneBase.incontriPossibili(MOSTRI_DEL_BOSCO, livello) : MOSTRI_DEL_BOSCO;
                TipoPersonaggio mostro = scegliMostro(possibili, locazione);
                // Senza inizio morbido, le regole di prima: fino a 2 mostri fino al livello 5
                int cap = variante.inizioMorbido ? LocazioneBase.numeroMassimoDiMostri(livello, 1) : (livello <= 5 ? 2 : 3);
                int quantita = 1 + random.nextInt(Math.min(cap, quantitaMassima(mostro)));
                if (!scontro(pg, generaMostri(mostro, quantita, livello), pozioni)) {
                    esito.locazioni = locazione - 1;
                    esito.livelloFinale = pg.getLivello();
                    return esito;
                }
            }
            pg.addStanchezza(1);
            if (variante.recuperoAFineLocazione) {
                pg.addSalute(pg.getRigenerazioneSalute());
                pg.addMagia(pg.getRigenerazioneMagia());
            }
        }
        esito.locazioni = LOCAZIONI_MASSIME;
        esito.livelloFinale = pg.getLivello();
        return esito;
    }

    private TipoPersonaggio scegliMostro(TipoPersonaggio[] possibili, int locazione) {
        while (true) {
            TipoPersonaggio mostro = possibili[random.nextInt(possibili.length)];
            // Come in LocazioneBase.crea: niente Eremita al primo turno
            if (locazione != 1 || mostro != TipoPersonaggio.EREMITA) {
                return mostro;
            }
        }
    }

    @Disabled("Da eseguire manualmente: la dotazione di base contro mostri di livello crescente")
    @Test
    void saltoDiLivello() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO,
                    TipoPersonaggio.MAGO)) {
                for (TipoPersonaggio mostro : Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.TROLL)) {
                    for (int livello : new int[]{1, 2, 3, 5}) {
                        int vittorie = 0;
                        double residua = 0;
                        int iterazioni = 1_000;
                        for (int i = 0; i < iterazioni; i++) {
                            Personaggio pg = nuovoProtagonista(classe);
                            if (livello > 1) {
                                pg.addPuntiEsperienza(GestoreProgressione.getXpNecessariPerLivello(livello));
                                pg.addSalute(pg.getSaluteMassima());
                            }
                            if (scontro(pg, generaMostri(mostro, 1, livello), new int[]{0})) {
                                vittorie++;
                                residua += pg.getSalute() / (double) pg.getSaluteMassima();
                            }
                        }
                        out.printf("%-9s livello %d (dotazione di livello 1, salute piena) vs %-6s di livello %d: vince %5.1f%%, salute residua %5.1f%%%n",
                                classe, livello, mostro, livello, 100.0d * vittorie / iterazioni,
                                vittorie == 0 ? 0 : 100.0d * residua / vittorie);
                    }
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    /**
     * Ai livelli alti i personaggi giocanti crescono linearmente e i mostri con la radice quadrata del livello: ogni
     * classe con la sua prima dotazione tipica, del suo livello e senza pergamene, contro mostri e boss del suo livello
     */
    @Disabled("Da eseguire manualmente: le classi ai livelli alti contro mostri e boss")
    @Test
    void livelliAlti() {
        PrintStream out = System.out;
        System.setOut(NULL_STREAM);
        try {
            List<TipoPersonaggio> classi = Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO,
                    TipoPersonaggio.ELFO, TipoPersonaggio.BARDO, TipoPersonaggio.MAGO);
            Object[][] avversari = {{TipoPersonaggio.TROLL, 1}, {TipoPersonaggio.TROLL, 3},
                    {TipoPersonaggio.GIGANTE, 2}, {TipoPersonaggio.MINOTAURO_GIGANTE, 1}, {TipoPersonaggio.IDRA, 1},
                    {TipoPersonaggio.LICH, 1}, {TipoPersonaggio.STREGA, 1}, {TipoPersonaggio.DRAGO, 1}};
            for (int livello : new int[]{5, 10, 15}) {
                StringBuilder testata = new StringBuilder(String.format("livello %-2d          ", livello));
                for (TipoPersonaggio classe : classi) {
                    testata.append(String.format(" | %-15s", classe));
                }
                out.println(testata);
                for (Object[] avversario : avversari) {
                    TipoPersonaggio mostro = (TipoPersonaggio) avversario[0];
                    int quantita = (Integer) avversario[1];
                    StringBuilder riga = new StringBuilder(String.format("%-17s x%d", mostro, quantita));
                    for (TipoPersonaggio classe : classi) {
                        RisultatoMatrice r = CombatSimulatorMatrix.simulaScontroGruppo(classe,
                                Equipaggiamento.tipiciPer(classe).get(0), ScortaDiPergamene.NESSUNA, mostro, quantita,
                                livello, livello, 500);
                        riga.append(String.format(" | %5.1f%% %5.1f t", r.winRatePg, r.mediaTurni));
                    }
                    out.println(riga);
                }
            }
        } finally {
            System.setOut(out);
        }
    }

    /**
     * I boss al livello del mondo: com'è fatto ciascuno e come va un Guerriero con la sua dotazione tipica (CAVALIERE),
     * uno contro uno
     */
    @Disabled("Da eseguire manualmente: i boss colpo per colpo")
    @Test
    void boss() {
        PrintStream out = System.out;
        System.setOut(NULL_STREAM);
        try {
            List<TipoPersonaggio> boss = Arrays.asList(TipoPersonaggio.IDRA, TipoPersonaggio.MINOTAURO_GIGANTE,
                    TipoPersonaggio.LICH, TipoPersonaggio.STREGA, TipoPersonaggio.DRAGO);
            for (int livello : new int[]{1, 3, 5, 8, 10}) {
                Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, livello);
                Equipaggiamento.tipiciPer(TipoPersonaggio.GUERRIERO).get(0).equipaggia(guerriero);
                out.printf("== livello %d: Guerriero CAVALIERE salute %d, forza %d, parata %d, res. magica %d%n", livello,
                        guerriero.getSaluteMassima(), guerriero.getForza(), guerriero.getParata(), guerriero.getResistenzaMagica());
                for (TipoPersonaggio classe : boss) {
                    Personaggio b = FabbricaPersonaggi.crea(classe, livello);
                    StringBuilder armi = new StringBuilder();
                    for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(b)) {
                        armi.append(fase.getArma().getTipoDanno()).append(' ').append(fase.getArma().getDanni()).append(' ');
                    }
                    RisultatoMatrice r = CombatSimulatorMatrix.simulaScontroGruppo(TipoPersonaggio.GUERRIERO,
                            Equipaggiamento.tipiciPer(TipoPersonaggio.GUERRIERO).get(0), ScortaDiPergamene.NESSUNA,
                            classe, 1, livello, livello, 500);
                    out.printf("%-17s salute %4d for %3d int %3d par %3d resM %3d bersagli %d armi [%s] | colpisce %4.1f%% danno %5.1f "
                                    + "| subisce: colpito %4.1f%% danno %5.1f | vince %5.1f%% in %4.1f turni%n",
                            classe, b.getSaluteMassima(), b.getForza(), b.getIntelligenza(), b.getParata(), b.getResistenzaMagica(),
                            b.getBersagli(), armi.toString().trim(), r.tassoColpireMostro, r.dannoMedioMostro,
                            r.tassoColpirePg, r.dannoMedioPg, r.winRatePg, r.mediaTurni);
                }
            }
        } finally {
            System.setOut(out);
        }
    }

    @Disabled("Da eseguire manualmente: gli attributi che contano per colpire e difendersi, Guerriero contro Lich")
    @Test
    void attributiGuerrieroLich() {
        PrintStream out = System.out;
        System.setOut(NULL_STREAM);
        try {
            for (int livello : new int[]{1, 3, 5, 10}) {
                Personaggio g = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, livello);
                Equipaggiamento.tipiciPer(TipoPersonaggio.GUERRIERO).get(0).equipaggia(g);
                Personaggio l = FabbricaPersonaggi.crea(TipoPersonaggio.LICH, livello);
                out.printf("L%-2d GUERRIERO prec %d des %d vel %d par %d resM %d sag %d int %d salute %d | LICH prec %d des %d vel %d par %d resM %d sag %d int %d salute %d tipo %s%n",
                        livello, g.getPrecisione(), g.getDestrezza(), g.getVelocita(), g.getParata(), g.getResistenzaMagica(),
                        g.getSaggezza(), g.getIntelligenza(), g.getSaluteMassima(),
                        l.getPrecisione(), l.getDestrezza(), l.getVelocita(), l.getParata(), l.getResistenzaMagica(),
                        l.getSaggezza(), l.getIntelligenza(), l.getSaluteMassima(), l.getArmaEquipaggiata().getTipoDanno().getSuperTipo());
            }
        } finally {
            System.setOut(out);
        }
    }

    @Disabled("Da eseguire manualmente: gli incantesimi dei mostri magici contro un Guerriero di livello 1")
    @Test
    void incantesimiDeiMostri() {
        PrintStream out = System.out;
        System.setOut(NULL_STREAM);
        try {
            Personaggio g = nuovoProtagonista(TipoPersonaggio.GUERRIERO);
            out.printf("Guerriero: salute %d, res. magica %d, saggezza %d%n", g.getSaluteMassima(), g.getResistenzaMagica(), g.getSaggezza());
            for (TipoPersonaggio classe : MOSTRI_DEL_BOSCO) {
                Personaggio m = FabbricaPersonaggi.crea(classe, 1);
                if (!((com.threeamigos.foresta.personaggi.PersonaggioBase) m).isMagico()) {
                    continue;
                }
                StringBuilder sb = new StringBuilder(String.format("%-13s magia %3d int %2d:", classe, m.getMagia(), m.getIntelligenza()));
                for (com.threeamigos.foresta.tipi.ClasseIncantesimo ci : com.threeamigos.foresta.tipi.ClasseIncantesimo.values()) {
                    if (ci.getTipo() != com.threeamigos.foresta.tipi.TipoIncantesimo.MALEFICO || FabbricaIncantesimi.costoLancio(ci) > m.getMagia()) {
                        continue;
                    }
                    com.threeamigos.foresta.incantesimi.IncantesimoMalefico inc =
                            (com.threeamigos.foresta.incantesimi.IncantesimoMalefico) FabbricaIncantesimi.crea(ci, 1);
                    sb.append(String.format(" %s %d%%x%d", ci, CalcolatoreCombattimento.calcolaProbabilitaDiColpire(m, g,
                            inc.getTipoDanno().getSuperTipo()), CalcolatoreCombattimento.calcolaDannoRisultante(m, g, inc).getDanno()));
                }
                out.println(sb);
            }
        } finally {
            System.setOut(out);
        }
    }

    @Disabled("Da eseguire manualmente: controlla la dotazione di base e i numeri di un colpo")
    @Test
    void controlloDotazione() {
        PrintStream out = System.out;
        System.setOut(NULL_STREAM);
        try {
            for (TipoPersonaggio classe : CLASSI) {
                Personaggio pg = nuovoProtagonista(classe);
                StringBuilder sb = new StringBuilder(classe + ": arma " + pg.getArmaEquipaggiata().getClass().getSimpleName()
                        + " danno " + pg.getArmaEquipaggiata().getDanni() + " liv " + pg.getArmaEquipaggiata().getLivello()
                        + ", salute " + pg.getSaluteMassima() + ", rigenerazione " + pg.getRigenerazioneSalute() + ", fasi");
                for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(pg)) {
                    sb.append(' ').append(fase.getArma().getClass().getSimpleName()).append(fase.getArma().getDanni())
                            .append('x').append(fase.getFattore());
                }
                out.println(sb);
            }
            Object[][] coppie = {{TipoPersonaggio.GUERRIERO, Equipaggiamento.SPADA_E_SCUDO},
                    {TipoPersonaggio.LADRO, Equipaggiamento.SPADA}, {TipoPersonaggio.MAGO, Equipaggiamento.NESSUNO}};
            for (Object[] coppia : coppie) {
                for (TipoPersonaggio mostro : Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.SCHELETRO,
                        TipoPersonaggio.TROLL, TipoPersonaggio.GIGANTE)) {
                    RisultatoMatrice r = CombatSimulatorMatrix.simulaScontroGruppo((TipoPersonaggio) coppia[0],
                            (Equipaggiamento) coppia[1], mostro, 1, 1, 2_000);
                    out.printf("%-9s %-14s vs %-10s win %5.1f%% turni %5.2f colpisce %4.1f%%/%4.1f%% danno %5.1f/%5.1f salute %d/%d%n",
                            coppia[0], coppia[1], mostro, r.winRatePg, r.mediaTurni, r.tassoColpirePg, r.tassoColpireMostro,
                            r.dannoMedioPg, r.dannoMedioMostro, FabbricaPersonaggi.crea(((TipoPersonaggio) coppia[0]), 1).getSaluteMassima(),
                            FabbricaPersonaggi.crea(mostro, 1).getSaluteMassima());
                }
            }
        } finally {
            System.setOut(out);
        }
    }

    private static Personaggio nuovoProtagonista(TipoPersonaggio classe) {
        ModelloDati.setIstanza(new ModelloDati());
        Personaggio pg = conNome(classe);
        EquipaggiamentoIniziale.equipaggia(pg);
        return pg;
    }

    /**
     * Con un nome, come il protagonista del gioco: un personaggio senza nome è un PNG e non accumula esperienza
     */
    private static Personaggio conNome(TipoPersonaggio classe) {
        switch (classe) {
            case GUERRIERO:
                return new Guerriero("Eroe", 1);
            case LADRO:
                return new Ladro("Eroe", 1);
            case ELFO:
                return new Elfo("Eroe", 1);
            case BARDO:
                return new Bardo("Eroe", 1);
            case MAGO:
                return new Mago("Eroe", 1);
            default:
                throw new IllegalArgumentException(classe.toString());
        }
    }

    private static List<Personaggio> generaMostri(TipoPersonaggio classe, int quantita, int livello) {
        List<Personaggio> mostri = new ArrayList<>();
        for (int i = 0; i < quantita; i++) {
            mostri.add(FabbricaPersonaggi.crea(classe, livello));
        }
        return mostri;
    }

    /**
     * Come in CombatSimulatorMatrix: il protagonista attacca il primo mostro vivo (con il dardo arcano se gli
     * conviene, o beve una pozione se è sotto il 35% della salute e ne ha), poi ogni mostro vivo attacca lui.
     * L'esperienza dei mostri uccisi va al protagonista, come in LocazioneBase. Vero se vince.
     */
    private static boolean scontro(Personaggio pg, List<Personaggio> mostri, int[] pozioni) {
        for (int turno = 0; turno < CombatSimulatorMatrix.TURNI_MASSIMI; turno++) {
            Personaggio bersaglio = mostri.get(0);
            if (pozioni[0] > 0 && pg.getSalute() * 100 < pg.getSaluteMassima() * 35) {
                // Bere costa il turno, come nel gioco
                pozioni[0]--;
                pg.addSalute(Costanti.RECUPERO_DA_POZIONE_SALUTE);
            } else if (CombatSimulatorMatrix.conviene(pg, bersaglio)) {
                DardoArcano dardo = new DardoArcano(pg);
                colpisci(pg, bersaglio, dardo, 1.0d);
                pg.subMagia(dardo.getCostoLancio());
            } else {
                for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(pg)) {
                    Personaggio vivo = primoVivo(mostri);
                    if (vivo == null) {
                        break;
                    }
                    colpisci(pg, vivo, fase.getArma(), fase.getFattore());
                }
            }
            for (Personaggio mostro : new ArrayList<>(mostri)) {
                if (!mostro.isVivo()) {
                    mostri.remove(mostro);
                    pg.addPuntiEsperienza(mostro.getPuntiEsperienza());
                }
            }
            if (mostri.isEmpty()) {
                return true;
            }
            for (Personaggio mostro : mostri) {
                if (pg.isVivo()) {
                    // Come nel gioco: con le armi o, se sa la magia, con un incantesimo
                    mostro.rispondiInMischia(pg);
                }
            }
            // A fine round passa un turno di effetti di stato per tutti (LocazioneBase.dopoIlRound)
            if (pg.isVivo()) {
                pg.applicaDanniDaEffettiDiStato();
                pg.riduciEffettiDiStato();
            }
            for (Personaggio mostro : mostri) {
                if (mostro.isVivo()) {
                    mostro.applicaDanniDaEffettiDiStato();
                    mostro.riduciEffettiDiStato();
                }
            }
            if (!pg.isVivo()) {
                return false;
            }
        }
        return false;
    }

    /**
     * Un colpo come nel gioco: se va a segno il risultato (danno ed effetti di stato) si applica al difensore
     */
    private static void colpisci(Personaggio attaccante, Personaggio difensore, Arma arma, double fattore) {
        if (CalcolatoreCombattimento.colpisce(attaccante, difensore, arma.getTipoDanno().getSuperTipo())) {
            difensore.applicaRisultatoCombattimento(CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma, fattore));
        }
    }

    private static Personaggio primoVivo(List<Personaggio> mostri) {
        for (Personaggio mostro : mostri) {
            if (mostro.isVivo()) {
                return mostro;
            }
        }
        return null;
    }

    private static int quantitaMassima(TipoPersonaggio classe) {
        FabbricaPersonaggi.crea(classe, 1);
        return FabbricaPersonaggi.quantitaMassima(classe);
    }
}
