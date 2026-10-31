package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.RegistroTattiche;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Analisi manuale (si esegue con {@code -Danalisi=true}) delle classi giocabili a confronto, da cui nasce {@code personaggi_giocabili.md}.
 * <ul>
 * <li>{@link #schede()}: le risorse e gli attributi medi di ogni classe (equipaggiata come nel gioco) ai livelli 1, 3, 5 e 7.</li>
 * <li>{@link #scontri()}: ogni classe da sola contro gli stessi avversari, e come quarto del gruppo Guerriero, Ladro e Mago (con la riga del gruppo
 * senza di lei per confronto). Si agisce con la tattica predefinita di ogni classe, nel round a coppie di {@link RoundASimulato}, senza pozioni
 * né pergamene, a salute e magia piene. Per ogni avversario: vittorie, salute residua dei miei e round medi delle vittorie.</li>
 * </ul>
 * Parametri: {@code -Diterazioni=<n>} (default 100).
 */
class AnalisiDeiPersonaggiGiocabiliTest {

    private static final int TURNI_MASSIMI = 200;

    private static final int[] LIVELLI = {1, 3, 5, 7};

    /**
     * Le classi giocabili, una per ruolo (la forma maschile: le femminili hanno gli stessi valori tranne il carico)
     */
    private static final List<TipoPersonaggio> CLASSI = Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO,
            TipoPersonaggio.ELFO, TipoPersonaggio.BARDO, TipoPersonaggio.SACERDOTE, TipoPersonaggio.CHIERICO, TipoPersonaggio.DRUIDO);

    private static final List<TipoPersonaggio> AVVERSARI = Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.SCHELETRO, TipoPersonaggio.TROLL,
            TipoPersonaggio.VIVERNA, TipoPersonaggio.STREGA, TipoPersonaggio.IDRA, TipoPersonaggio.LICH);

    /**
     * Gli avversari del gruppo di quattro: tre alla volta (branco) o un boss
     */
    private static final List<TipoPersonaggio> AVVERSARI_DEL_GRUPPO = Arrays.asList(TipoPersonaggio.TROLL, TipoPersonaggio.VIVERNA,
            TipoPersonaggio.SCHELETRO, TipoPersonaggio.STREGA, TipoPersonaggio.MINOTAURO_GIGANTE, TipoPersonaggio.IDRA, TipoPersonaggio.DRAGO,
            TipoPersonaggio.LICH);

    private static final List<TipoPersonaggio> BRANCHI = Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.SCHELETRO);

    private static final List<TipoPersonaggio> BRANCHI_DEL_GRUPPO = Arrays.asList(TipoPersonaggio.TROLL, TipoPersonaggio.VIVERNA,
            TipoPersonaggio.SCHELETRO);

    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    static final class Esito {
        boolean vittoria;
        int round;
        double saluteResidua;
    }

    private static final class Colonna {
        final String nome;
        final ToDoubleFunction<Personaggio> valore;

        Colonna(String nome, ToDoubleFunction<Personaggio> valore) {
            this.nome = nome;
            this.valore = valore;
        }

        Colonna(String nome, TipoAttributo attributo) {
            this(nome, p -> p.get(attributo));
        }
    }

    private static final List<Colonna> RISORSE = Arrays.asList(
            new Colonna("salute", Personaggio::getSaluteMassima), new Colonna("rig.sal", Personaggio::getRigenerazioneSalute),
            new Colonna("magia", Personaggio::getMagiaMassima), new Colonna("rig.mag", Personaggio::getRigenerazioneMagia),
            new Colonna("carico", Personaggio::getCaricoMassimo), new Colonna("bersagli", Personaggio::getBersagli),
            new Colonna("pot.mag", Personaggio::getPotereMagico));

    private static final List<Colonna> ATTRIBUTI = Arrays.asList(
            new Colonna("FOR", TipoAttributo.FORZA), new Colonna("DES", TipoAttributo.DESTREZZA),
            new Colonna("COS", TipoAttributo.COSTITUZIONE), new Colonna("INT", TipoAttributo.INTELLIGENZA),
            new Colonna("SAG", TipoAttributo.SAGGEZZA), new Colonna("CAR", TipoAttributo.CARISMA),
            new Colonna("FORT", TipoAttributo.FORTUNA), new Colonna("CRIT", TipoAttributo.CRITICO),
            new Colonna("PREC", TipoAttributo.PRECISIONE), new Colonna("VEL", TipoAttributo.VELOCITA),
            new Colonna("FURT", TipoAttributo.FURTIVITA), new Colonna("PAR", TipoAttributo.PARATA),
            new Colonna("RES.M", TipoAttributo.RESISTENZA_MAGICA), new Colonna("PERC", TipoAttributo.PERCEZIONE),
            new Colonna("SOGG", TipoAttributo.SOGGEZIONE), new Colonna("FURIA", TipoAttributo.FURIA),
            new Colonna("CORAG", TipoAttributo.CORAGGIO), new Colonna("VAL", TipoAttributo.VALORE),
            new Colonna("CONTR", TipoAttributo.CONTRATTAZIONE));

    @Test
    @EnabledIfSystemProperty(named = "analisi", matches = "true")
    void schede() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            int prove = Integer.getInteger("iterazioni", 100);
            for (List<Colonna> colonne : Arrays.asList(RISORSE, ATTRIBUTI)) {
                for (int livello : LIVELLI) {
                    out.printf("%n== %s, livello %d (media su %d, equipaggiati come nel gioco)%n", colonne == RISORSE ? "RISORSE" : "ATTRIBUTI", livello, prove);
                    StringBuilder testata = new StringBuilder(String.format("%-10s", "classe"));
                    for (Colonna c : colonne) {
                        testata.append(String.format(" %8s", c.nome));
                    }
                    out.println(testata);
                    for (TipoPersonaggio classe : CLASSI) {
                        double[] somme = new double[colonne.size()];
                        for (int i = 0; i < prove; i++) {
                            Personaggio pg = nuovo(classe, livello);
                            for (int c = 0; c < somme.length; c++) {
                                somme[c] += colonne.get(c).valore.applyAsDouble(pg);
                            }
                        }
                        StringBuilder riga = new StringBuilder(String.format("%-10s", classe));
                        for (double somma : somme) {
                            riga.append(String.format(" %8.1f", somma / prove));
                        }
                        out.println(riga);
                    }
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "analisi", matches = "true")
    void scontri() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            int iterazioni = Integer.getInteger("iterazioni", 100);
            for (boolean aQuattro : new boolean[]{false, true}) {
                for (int livello : new int[]{3, 5, 7}) {
                    out.printf("%n== SCONTRI livello %d, %s (%d scontri per cella): vittorie %% / salute residua %% / round medi%n", livello,
                            aQuattro ? "come quarto del gruppo GUE+LAD+MAG" : "da soli", iterazioni);
                    StringBuilder testata = new StringBuilder(String.format("%-16s", "classe"));
                    List<TipoPersonaggio> avversari = aQuattro ? AVVERSARI_DEL_GRUPPO : AVVERSARI;
                    for (TipoPersonaggio avversario : avversari) {
                        testata.append(String.format(" | %-17s", avversario));
                    }
                    out.println(testata);
                    List<List<TipoPersonaggio>> righe = new ArrayList<>();
                    List<String> nomi = new ArrayList<>();
                    if (aQuattro) {
                        righe.add(Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO));
                        nomi.add("(nessuno)");
                    }
                    for (TipoPersonaggio classe : CLASSI) {
                        righe.add(aQuattro ? Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, classe)
                                : Arrays.asList(classe));
                        nomi.add(classe.name());
                        if (classe == TipoPersonaggio.ELFO && INCANTATO) {
                            righe.add(aQuattro ? Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, classe)
                                    : Arrays.asList(classe));
                            nomi.add("ELFO+LIBRO");
                        }
                    }
                    for (int r = 0; r < righe.size(); r++) {
                        elfoConLibro = nomi.get(r).equals("ELFO+LIBRO");
                        List<TipoPersonaggio> gruppo = righe.get(r);
                        StringBuilder riga = new StringBuilder(String.format("%-16s", nomi.get(r)));
                        for (TipoPersonaggio avversario : avversari) {
                            boolean branco = (aQuattro ? BRANCHI_DEL_GRUPPO : BRANCHI).contains(avversario);
                            int quantita = !branco ? 1 : aQuattro ? 3 : Math.min(LocazioneBase.numeroMassimoDiMostri(livello, gruppo.size()), 3);
                            int vittorie = 0;
                            double salute = 0, round = 0;
                            for (int i = 0; i < iterazioni; i++) {
                                Esito e = scontro(gruppo, avversario, quantita, livello);
                                if (e.vittoria) {
                                    vittorie++;
                                    salute += e.saluteResidua;
                                    round += e.round;
                                }
                            }
                            riga.append(String.format(" | %5.1f %4.0f %5.1f  ", 100.0 * vittorie / iterazioni,
                                    vittorie == 0 ? 0 : 100.0 * salute / vittorie, vittorie == 0 ? 0 : round / vittorie));
                        }
                        out.println(riga);
                    }
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    /**
     * Che cosa fa ogni classe da sola contro un avversario: quante volte per scontro compie ogni azione e quanta magia spende
     */
    @Test
    @EnabledIfSystemProperty(named = "analisi", matches = "true")
    void azioni() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            int iterazioni = Integer.getInteger("iterazioni", 100);
            for (TipoPersonaggio avversario : Arrays.asList(TipoPersonaggio.VIVERNA, TipoPersonaggio.TROLL)) {
                out.printf("%n== AZIONI per scontro, livello 7, da soli contro 1 %s (%d scontri)%n", avversario, iterazioni);
                for (TipoPersonaggio classe : CLASSI) {
                    java.util.Map<com.threeamigos.foresta.tipi.TipoAzione, Integer> azioni = new java.util.EnumMap<>(com.threeamigos.foresta.tipi.TipoAzione.class);
                    double[] magia = new double[1];
                    for (int i = 0; i < iterazioni; i++) {
                        ModelloDati.installa();
                        ModelloDati.setIstanza(new ModelloDati());
                        Personaggio pg = FabbricaPersonaggi.crea(classe, 7);
                        equipaggia(pg, classe);
                        List<Personaggio> gruppo = Arrays.asList(pg);
                        List<Personaggio> nemici = Arrays.asList(FabbricaPersonaggi.crea(avversario, 7));
                        RoundASimulato round = new RoundASimulato(gruppo, nemici);
                        RoundASimulato.Decisore decisore = (chi, r) -> {
                            int prima = chi.getMagia();
                            com.threeamigos.foresta.tattiche.Decisione d = r.agisciConLaTattica(chi, RegistroTattiche.predefinitaDelGiocabile(chi.getClasse()));
                            if (d != null) {
                                azioni.merge(d.getTipoEseguito(), 1, Integer::sum);
                                magia[0] += Math.max(0, prima - chi.getMagia());
                            }
                        };
                        for (int turno = 1; turno <= TURNI_MASSIMI; turno++) {
                            RoundASimulato.Esito fine = round.eseguiRound(decisore);
                            if (fine != RoundASimulato.Esito.IN_CORSO) {
                                break;
                            }
                        }
                    }
                    StringBuilder riga = new StringBuilder(String.format("%-10s magia spesa %5.1f |", classe, magia[0] / iterazioni));
                    for (java.util.Map.Entry<com.threeamigos.foresta.tipi.TipoAzione, Integer> e : azioni.entrySet()) {
                        riga.append(String.format(" %s %.2f,", e.getKey(), (double) e.getValue() / iterazioni));
                    }
                    out.println(riga);
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    private static Personaggio nuovo(TipoPersonaggio classe, int livello) {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        Personaggio pg = FabbricaPersonaggi.crea(classe, livello);
        equipaggia(pg, classe);
        return pg;
    }

    private Esito scontro(List<TipoPersonaggio> classi, TipoPersonaggio classeAvversari, int quantita, int livello) {
        ModelloDati.installa();
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
        RoundASimulato round = new RoundASimulato(gruppo, avversari);
        RoundASimulato.Decisore decisore = (chi, r) -> r.agisciConLaTattica(chi, RegistroTattiche.predefinitaDelGiocabile(chi.getClasse()));
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
            if (p.isVivo()) {
                salute += p.getSalute();
            }
            massima += p.getSaluteMassima();
        }
        esito.saluteResidua = salute / massima;
        return esito;
    }

    private static final Equipaggiamento MAZZA_E_ARMATURA = Equipaggiamento.di("MAZZA_E_ARMATURA",
            Equipaggiamento.Pezzo.di(com.threeamigos.foresta.tipi.TipoArtefatto.MAZZA), Equipaggiamento.Pezzo.di(com.threeamigos.foresta.tipi.TipoArtefatto.SCUDO),
            Equipaggiamento.Pezzo.di(com.threeamigos.foresta.tipi.TipoArtefatto.ELMO), Equipaggiamento.Pezzo.di(com.threeamigos.foresta.tipi.TipoArtefatto.ARMATURA));

    /**
     * La dotazione tipica della classe, come nelle altre analisi (il Chierico mazza e armatura, il Druido bastone, libro e veste, il Sacerdote
     * bastone, libro e veste: l'armatura non la porta)
     */
    private static void equipaggia(Personaggio pg, TipoPersonaggio classe) {
        if (INCANTATO) {
            equipaggiaConArmiIncantate(pg, classe);
            return;
        }
        switch (classe.getRuolo()) {
            case CHIERICO:
                MAZZA_E_ARMATURA.equipaggia(pg);
                break;
            case DRUIDO:
            case SACERDOTE:
                Equipaggiamento.BASTONE_LIBRO_E_VESTE.equipaggia(pg);
                break;
            default:
                Equipaggiamento.tipiciPer(classe).get(0).equipaggia(pg);
        }
    }

    /**
     * Con {@code -Dequip=incantato} le armi di tutti hanno un incantamento di FULMINE del grado adatto al livello (nessun mostro vi è debole o
     * immune); l'Elfo può portare il libro magico al posto della seconda spada (Elfo+Libro: un libro occupa la mano secondaria)
     */
    private static final boolean INCANTATO = "incantato".equals(System.getProperty("equip"));

    private static boolean elfoConLibro;

    private static Equipaggiamento.Pezzo incantato(com.threeamigos.foresta.tipi.TipoArtefatto tipo) {
        return Equipaggiamento.Pezzo.di(tipo).incantato(com.threeamigos.foresta.tipi.TipoDanno.FULMINE);
    }

    private static void equipaggiaConArmiIncantate(Personaggio pg, TipoPersonaggio classe) {
        com.threeamigos.foresta.tipi.TipoArtefatto spada = com.threeamigos.foresta.tipi.TipoArtefatto.SPADA;
        Equipaggiamento.Pezzo scudo = Equipaggiamento.Pezzo.di(com.threeamigos.foresta.tipi.TipoArtefatto.SCUDO);
        Equipaggiamento.Pezzo elmo = Equipaggiamento.Pezzo.di(com.threeamigos.foresta.tipi.TipoArtefatto.ELMO);
        Equipaggiamento.Pezzo armatura = Equipaggiamento.Pezzo.di(com.threeamigos.foresta.tipi.TipoArtefatto.ARMATURA);
        Equipaggiamento.Pezzo veste = Equipaggiamento.Pezzo.di(com.threeamigos.foresta.tipi.TipoArtefatto.VESTE);
        Equipaggiamento.Pezzo libro = Equipaggiamento.Pezzo.di(com.threeamigos.foresta.tipi.TipoArtefatto.LIBRO_MAGICO);
        switch (classe.getRuolo()) {
            case GUERRIERO:
                Equipaggiamento.di("CAVALIERE_INCANTATO", incantato(spada), scudo, elmo, armatura).equipaggia(pg);
                break;
            case LADRO:
                // Il ladro non porta l'elmo (vedi RegoleEquipaggiamento.puoUsare)
                Equipaggiamento.di("DUE_SPADE_INCANTATE_SENZA_ELMO", incantato(spada), incantato(spada), veste).equipaggia(pg);
                break;
            case ELFO:
                (elfoConLibro ? Equipaggiamento.di("SPADA_INCANTATA_E_LIBRO", incantato(spada), libro, elmo, veste)
                        : Equipaggiamento.di("DUE_SPADE_INCANTATE", incantato(spada), incantato(spada), elmo, veste)).equipaggia(pg);
                break;
            case BARDO:
                Equipaggiamento.di("SPADA_INCANTATA_SCUDO_E_VESTE", incantato(spada), scudo, elmo, veste).equipaggia(pg);
                break;
            case CHIERICO:
                Equipaggiamento.di("MAZZA_INCANTATA_E_ARMATURA", incantato(com.threeamigos.foresta.tipi.TipoArtefatto.MAZZA), scudo, elmo,
                        armatura).equipaggia(pg);
                break;
            default:
                Equipaggiamento.BASTONE_LIBRO_E_VESTE.equipaggia(pg);
        }
    }
}
