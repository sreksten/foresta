package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.abilita.DardoArcano;
import com.threeamigos.foresta.abilita.LuceDivina;
import com.threeamigos.foresta.abilita.MartelloSacro;
import com.threeamigos.foresta.abilita.Radici;
import com.threeamigos.foresta.abilita.Sciame;
import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.FaseDiAttacco;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Analisi manuale (si esegue con {@code -Danalisi=true}) dei danni delle abilità innate che colpiscono: la luce divina del Sacerdote, le
 * radici e lo sciame del Druido, il dardo arcano di Mago ed Elfo, il martello sacro del Chierico e l'attacco alle spalle del Ladro (che è un
 * colpo con le armi, sempre un critico: qui si misura con il critico garantito, quindi il doppio di un colpo normale, e non costa Magia). Per ogni livello (1, 3, 5, 7) e per ogni bersaglio (viverna,
 * chimera-drago e i boss, tutti allo stesso livello di chi attacca) dice il danno medio di un colpo andato a segno sul primo bersaglio
 * (quello che subisce tutto il danno), in assoluto e in percentuale della salute massima media del bersaglio. Il colpo critico è
 * neutralizzato e i personaggi, che hanno attributi a caso, si ripetono {@link #PROVE} volte per farne la media.
 * <p>
 * Il libro magico è simulato come un +20% sul danno finale (non si guarda quello vero, che dipende dal suo livello).
 */
class AnalisiDanniAbilitaInnateTest {

    private static final int PROVE = 200;

    private static final double LIBRO_MAGICO = 1.2d;

    private static final int[] LIVELLI = {1, 3, 5, 7};

    private static final List<TipoPersonaggio> BERSAGLI = Arrays.asList(TipoPersonaggio.VIVERNA, TipoPersonaggio.CHIMERA_DRAGO,
            TipoPersonaggio.DRAGO, TipoPersonaggio.IDRA, TipoPersonaggio.LICH, TipoPersonaggio.MINOTAURO_GIGANTE);

    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    /**
     * Un'abilità da misurare: chi la usa e come la si costruisce
     */
    private static final class Candidata {
        final String nome;
        final TipoPersonaggio classe;
        final Function<Personaggio, Arma> costruttore;
        // Un colpo con le armi di chi la usa (l'attacco alle spalle), non un attacco magico: niente costruttore e niente costo di Magia
        final boolean conLeArmi;

        Candidata(String nome, TipoPersonaggio classe, Function<Personaggio, Arma> costruttore) {
            this(nome, classe, costruttore, false);
        }

        Candidata(String nome, TipoPersonaggio classe, Function<Personaggio, Arma> costruttore, boolean conLeArmi) {
            this.nome = nome;
            this.classe = classe;
            this.costruttore = costruttore;
            this.conLeArmi = conLeArmi;
        }
    }

    private static final List<Candidata> CANDIDATE = Arrays.asList(
            new Candidata("Luce divina (Sacerdote)", TipoPersonaggio.SACERDOTE, LuceDivina::new),
            new Candidata("Radici (Druido)", TipoPersonaggio.DRUIDO, Radici::new),
            new Candidata("Sciame (Druido)", TipoPersonaggio.DRUIDO, Sciame::new),
            new Candidata("Dardo arcano (Mago)", TipoPersonaggio.MAGO, DardoArcano::new),
            new Candidata("Dardo arcano (Elfo)", TipoPersonaggio.ELFO, DardoArcano::new),
            new Candidata("Martello sacro (Chierico)", TipoPersonaggio.CHIERICO, MartelloSacro::new),
            new Candidata("Attacco alle spalle (Ladro)", TipoPersonaggio.LADRO, null, true),
            new Candidata("Fulmine pergamena (Mago)", TipoPersonaggio.MAGO, pg -> new com.threeamigos.foresta.incantesimi.Fulmine(pg.getLivello())),
            new Candidata("Fulmine pergamena (Sacerd.)", TipoPersonaggio.SACERDOTE, pg -> new com.threeamigos.foresta.incantesimi.Fulmine(pg.getLivello())));

    @Test
    @EnabledIfSystemProperty(named = "analisi", matches = "true")
    void analisi() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            tabellaDiBase(out);
            tabellaDellaMagia(out);
            tabellaDeiBersagli(out);
            tabellaDellaSalute(out);
            for (boolean equipaggiati : new boolean[]{false, true}) {
                for (int livello : LIVELLI) {
                    tabellaDelLivello(out, livello, equipaggiati);
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    private void tabellaDiBase(PrintStream out) {
        out.printf("%n== DANNI DI BASE (prima di livello, INTELLIGENZA, moltiplicatore di classe e difese) e costo di lancio%n");
        out.printf("%-26s | %6s | %6s%n", "abilità", "danni", "costo");
        out.printf("%-26s | %6d | %6d%n", "Luce divina (Sacerdote)", CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_DANNI,
                CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_COSTO_LANCIO);
        out.printf("%-26s | %6d | %6d%n", "Radici (Druido)", CostantiAbilitaInnate.ABILITA_RADICI_DANNI,
                CostantiAbilitaInnate.ABILITA_RADICI_COSTO_LANCIO);
        out.printf("%-26s | %6d | %6d%n", "Sciame (Druido)", CostantiAbilitaInnate.ABILITA_SCIAME_DANNI,
                CostantiAbilitaInnate.ABILITA_SCIAME_COSTO_LANCIO);
        out.printf("%-26s | %6d | %6d%n", "Dardo arcano (Mago)", CostantiAbilitaInnate.DARDO_ARCANO_DANNI_MAGO,
                CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO);
        out.printf("%-26s | %6d | %6d%n", "Dardo arcano (Elfo)", CostantiAbilitaInnate.DARDO_ARCANO_DANNI_ELFO,
                CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_ELFO);
        out.printf("%-26s | %6d | %6d%n", "Martello sacro (Chierico)", CostantiAbilitaInnate.ABILITA_MARTELLO_SACRO_DANNI,
                CostantiAbilitaInnate.ABILITA_MARTELLO_SACRO_COSTO_LANCIO);
        out.printf("%-26s | %6s | %6d%n", "Attacco alle spalle (Ladro)", "armi x2", CostantiAbilitaInnate.ABILITA_ATTACCO_ALLE_SPALLE_COSTO_LANCIO);
    }

    private void tabellaDellaMagia(PrintStream out) {
        out.printf("%n== Magia massima media (e recupero per turno di rigenerazione) di chi usa le abilità, e quanti lanci reggono a magia piena%n");
        out.printf("%-10s |", "classe");
        for (int livello : LIVELLI) {
            out.printf(" liv %d: magia (recupero) |", livello);
        }
        out.printf(" lanci a magia piena (liv 7)%n");
        for (Candidata candidata : CANDIDATE) {
            if (candidata.nome.contains("pergamena") || CANDIDATE.stream().filter(c -> !c.nome.contains("pergamena"))
                    .filter(c -> c.classe == candidata.classe).findFirst().get() != candidata) {
                continue;
            }
            out.printf("%-10s |", candidata.classe);
            double magiaA7 = 0;
            for (int livello : LIVELLI) {
                double magia = 0, recupero = 0;
                for (int prova = 0; prova < PROVE; prova++) {
                    ModelloDati.installa();
                    ModelloDati.setIstanza(new ModelloDati());
                    Personaggio pg = FabbricaPersonaggi.crea(candidata.classe, livello);
                    magia += pg.getMagiaMassima();
                    recupero += pg.getRigenerazioneMagia();
                }
                magia /= PROVE;
                recupero /= PROVE;
                magiaA7 = magia;
                out.printf(" %9.0f (%6.1f)       |", magia, recupero);
            }
            StringBuilder lanci = new StringBuilder();
            for (Candidata c : CANDIDATE) {
                if (c.classe == candidata.classe && !c.nome.contains("pergamena")) {
                    if (c.conLeArmi) {
                        lanci.append(String.format(java.util.Locale.ROOT, " %s: senza costo di Magia", c.nome.replaceAll(" \\(.*", "")));
                        continue;
                    }
                    Personaggio pg = FabbricaPersonaggi.crea(c.classe, 7);
                    lanci.append(String.format(java.util.Locale.ROOT, " %s: %d", c.nome.replaceAll(" \\(.*", ""),
                            (int) (magiaA7 / ((com.threeamigos.foresta.abilita.Abilita) c.costruttore.apply(pg)).getCostoLancio())));
                }
            }
            out.printf("%s%n", lanci);
        }
    }

    private void tabellaDeiBersagli(PrintStream out) {
        out.printf("%n== QUANTI AVVERSARI COLPISCONO insieme gli attacchi a più bersagli (getBersagli medio)%n");
        out.printf("%-10s |", "classe");
        for (int livello : LIVELLI) {
            out.printf(" liv %d |", livello);
        }
        out.println();
        for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.SACERDOTE, TipoPersonaggio.DRUIDO, TipoPersonaggio.MAGO,
                TipoPersonaggio.ELFO, TipoPersonaggio.CHIERICO, TipoPersonaggio.GUERRIERO, TipoPersonaggio.BARDO)) {
            out.printf("%-10s |", classe);
            for (int livello : LIVELLI) {
                double somma = 0;
                for (int prova = 0; prova < PROVE; prova++) {
                    ModelloDati.installa();
                    ModelloDati.setIstanza(new ModelloDati());
                    somma += FabbricaPersonaggi.crea(classe, livello).getBersagli();
                }
                out.printf(" %5.2f |", somma / PROVE);
            }
            out.println();
        }
    }

    private void tabellaDellaSalute(PrintStream out) {
        out.printf("%n== SALUTE MASSIMA MEDIA dei bersagli, per livello%n");
        out.printf("%-18s |", "classe");
        for (int livello : LIVELLI) {
            out.printf(" liv %d |", livello);
        }
        out.println();
        List<TipoPersonaggio> classi = new java.util.ArrayList<>(BERSAGLI);
        classi.add(TipoPersonaggio.STREGA);
        for (TipoPersonaggio classe : classi) {
            out.printf("%-18s |", classe);
            for (int livello : LIVELLI) {
                double somma = 0;
                for (int prova = 0; prova < PROVE; prova++) {
                    ModelloDati.installa();
                    ModelloDati.setIstanza(new ModelloDati());
                    somma += FabbricaPersonaggi.crea(classe, livello).getSaluteMassima();
                }
                out.printf(" %5.0f |", somma / PROVE);
            }
            out.println();
        }
    }

    /**
     * Il bastone e il libro per chi li porta (Mago, Sacerdote, Druido); il Ladro porta due spade e la veste, senza elmo (vedi
     * RegoleEquipaggiamento.puoUsare). Il Chierico e l'Elfo non hanno niente: le loro abilità non dipendono dall'equipaggiamento.
     */
    private static void equipaggia(Personaggio pg, TipoPersonaggio classe) {
        switch (classe) {
            case LADRO:
                Equipaggiamento.DUE_SPADE_E_VESTE_SENZA_ELMO.equipaggia(pg);
                break;
            case ELFO:
            case CHIERICO:
                break;
            default:
                Equipaggiamento.BASTONE_LIBRO_E_VESTE.equipaggia(pg);
        }
    }

    private void tabellaDelLivello(PrintStream out, int livello, boolean equipaggiati) {
        out.printf("%n== %s, LIVELLO %d: danno medio di un colpo a segno sul primo bersaglio, e in %% della sua salute massima media "
                + "(libro magico = +%d%%, media su %d prove)%n", equipaggiati ? "CON BASTONE E LIBRO VERI (Mago, Sacerdote, Druido)" : "SENZA EQUIPAGGIAMENTO", livello, Math.round((LIBRO_MAGICO - 1.0d) * 100), PROVE);
        out.printf("%-26s %-6s |", "abilità", "libro");
        for (TipoPersonaggio bersaglio : BERSAGLI) {
            out.printf(" %-17s|", bersaglio);
        }
        out.printf(" %s%n", "magia, danno/magia (vs viverna)");
        for (Candidata candidata : CANDIDATE) {
            double[] danni = new double[BERSAGLI.size()];
            double[] salute = new double[BERSAGLI.size()];
            double costo = 0;
            for (int b = 0; b < BERSAGLI.size(); b++) {
                for (int prova = 0; prova < PROVE; prova++) {
                    ModelloDati.installa();
                    ModelloDati.setIstanza(new ModelloDati());
                    Personaggio attaccante = FabbricaPersonaggi.crea(candidata.classe, livello);
                    if (equipaggiati) {
                        equipaggia(attaccante, candidata.classe);
                    }
                    attaccante.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
                    Personaggio bersaglio = FabbricaPersonaggi.crea(BERSAGLI.get(b), livello);
                    bersaglio.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
                    if (candidata.conLeArmi) {
                        // Ogni arma del turno (la seconda spada al 40%) con il critico garantito: il colpo è sempre a segno
                        for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(attaccante, bersaglio)) {
                            danni[b] += CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, bersaglio, fase.getArma(), fase.getFattore(), true)
                                    .getDanno();
                        }
                        salute[b] += bersaglio.getSaluteMassima();
                        continue;
                    }
                    Arma abilita = candidata.costruttore.apply(attaccante);
                    danni[b] += CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, bersaglio, abilita).getDanno();
                    salute[b] += bersaglio.getSaluteMassima();
                    if (b == 0) {
                        costo += abilita instanceof com.threeamigos.foresta.abilita.Abilita ? ((com.threeamigos.foresta.abilita.Abilita) abilita).getCostoLancio() : 0;
                    }
                }
            }
            costo /= PROVE;
            for (boolean libro : new boolean[]{false, true}) {
                double fattore = libro ? LIBRO_MAGICO : 1.0d;
                out.printf("%-26s %-6s |", candidata.nome, libro ? "con" : "senza");
                for (int b = 0; b < BERSAGLI.size(); b++) {
                    double media = danni[b] / PROVE * fattore;
                    double mediaSalute = salute[b] / PROVE;
                    out.printf(" %6.0f (%5.0f%%)  |", media, 100.0d * media / mediaSalute);
                }
                if (costo == 0) {
                    out.printf(" %4.0f, senza costo%n", costo);
                } else {
                    out.printf(" %4.0f, %5.1f%n", costo, danni[0] / PROVE * fattore / costo);
                }
            }
        }
    }
}
