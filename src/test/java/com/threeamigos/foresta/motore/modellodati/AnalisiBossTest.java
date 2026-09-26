package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.incantesimi.Fuoco;
import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Quanto sono difficili Lich e Strega, che compaiono al livello del gioco: per ogni livello e classe giocante (con
 * la sua dotazione tipica) la probabilità di colpire il boss di fisico e con un Fuoco, il danno per colpo e i turni
 * che servono in media a buttarlo giù, contro i turni in cui il boss butta giù il personaggio. Poi il Fuoco del Mago
 * contro i mostri comuni, per confronto. Sono medie su molti personaggi, perché le caratteristiche si tirano a caso.
 * I turni medi sono i colpi che servono (salute / danno, per eccesso) divisi per la probabilità di colpire.
 */
class AnalisiBossTest {

    private static final int CAMPIONI = 300;
    private static final int[] LIVELLI = {1, 3, 5, 8, 10, 15, 20};
    private static final ClassePersonaggio[] BOSS = {ClassePersonaggio.LICH, ClassePersonaggio.STREGA};
    private static final ClassePersonaggio[] PG = {ClassePersonaggio.GUERRIERO, ClassePersonaggio.LADRO,
            ClassePersonaggio.ELFO, ClassePersonaggio.BARDO, ClassePersonaggio.MAGO};
    private static final ClassePersonaggio[] MOSTRI_COMUNI = {ClassePersonaggio.GOBLIN, ClassePersonaggio.HOBGOBLIN,
            ClassePersonaggio.SCHELETRO, ClassePersonaggio.ARPIA, ClassePersonaggio.CENTAURO, ClassePersonaggio.TROLL,
            ClassePersonaggio.MINOTAURO, ClassePersonaggio.GARGOYLE};

    @Disabled("Da eseguire manualmente per misurare la difficoltà dei boss")
    @Test
    void stampaDifficoltaDeiBoss() {
        stampa(System.out);
    }

    static void stampa(PrintStream out) {
        // Il Logger scrive su System.out: durante i calcoli si zittisce
        PrintStream originale = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
        try {
            ModelloDati.setIstanza(new ModelloDati());
            for (ClassePersonaggio classeBoss : BOSS) {
                stampaBoss(out, classeBoss);
            }
            stampaFuocoControMostriComuni(out, 5);
        } finally {
            System.setOut(originale);
        }
    }

    private static void stampaBoss(PrintStream out, ClassePersonaggio classeBoss) {
        out.println();
        out.println("=== " + classeBoss);
        for (int livello : LIVELLI) {
            Personaggio boss = classeBoss.getIstanza(livello);
            out.printf("Liv %2d: salute %d, difesa fisica %.0f (VEL %d + DES %d + PAR/2 %d), difesa magica %d (RM %d + SAG %d)%s%n",
                    livello, boss.getSaluteMassima(),
                    boss.getVelocita() + boss.getDestrezza() + boss.getParata() * 0.5d,
                    boss.getVelocita(), boss.getDestrezza(), boss.getParata() / 2,
                    boss.getResistenzaMagica() + boss.getSaggezza(), boss.getResistenzaMagica(), boss.getSaggezza(),
                    boss.isImmuneAIncantesimo(ClasseIncantesimo.FUOCO) ? ", immune al Fuoco" : "");
            out.printf("   %-9s %-22s %7s %8s %6s %7s | %7s %8s %6s %7s | %s%n",
                    "Classe", "Dotazione", "Att.fis", "Colpisce", "Danno", "Turni",
                    "Att.mag", "Fuoco", "Danno", "Turni", "Il boss lo butta giù in");
            for (ClassePersonaggio classePg : PG) {
                stampaRiga(out, classeBoss, classePg, livello, boss.getSaluteMassima(),
                        boss.isImmuneAIncantesimo(ClasseIncantesimo.FUOCO));
            }
        }
    }

    private static void stampaRiga(PrintStream out, ClassePersonaggio classeBoss, ClassePersonaggio classePg, int livello,
                                   int saluteBoss, boolean immuneAlFuoco) {
        Equipaggiamento equipaggiamento = Equipaggiamento.tipiciPer(classePg).get(0);
        double attaccoFisico = 0, attaccoMagico = 0, colpisceFisico = 0, colpisceMagico = 0, dannoArma = 0,
                dannoFuoco = 0, colpisceBoss = 0, dannoBoss = 0, salutePg = 0;
        for (int i = 0; i < CAMPIONI; i++) {
            Personaggio pg = classePg.getIstanza(livello);
            equipaggiamento.equipaggia(pg);
            Personaggio boss = classeBoss.getIstanza(livello);
            attaccoFisico += pg.getPrecisione() + pg.getDestrezza();
            attaccoMagico += pg.getPrecisione() + pg.getIntelligenza();
            colpisceFisico += CalcolatoreCombattimento.calcolaProbabilitaDiColpire(pg, boss, SupertipoDanno.FISICO);
            colpisceMagico += CalcolatoreCombattimento.calcolaProbabilitaDiColpire(pg, boss, SupertipoDanno.MAGICO);
            dannoArma += CalcolatoreCombattimento.calcolaDannoRisultante(pg, boss, pg.getArmaEquipaggiata()).getDanno();
            dannoFuoco += CalcolatoreCombattimento.calcolaDannoRisultante(pg, boss, new Fuoco(livello)).getDanno();
            colpisceBoss += CalcolatoreCombattimento.calcolaProbabilitaDiColpire(boss, pg,
                    boss.getArmaEquipaggiata().getTipoDanno().getSuperTipo());
            dannoBoss += CalcolatoreCombattimento.calcolaDannoRisultante(boss, pg, boss.getArmaEquipaggiata()).getDanno();
            salutePg += pg.getSaluteMassima();
        }
        attaccoFisico /= CAMPIONI;
        attaccoMagico /= CAMPIONI;
        colpisceFisico /= CAMPIONI;
        colpisceMagico /= CAMPIONI;
        dannoArma /= CAMPIONI;
        dannoFuoco = immuneAlFuoco ? 0 : dannoFuoco / CAMPIONI;
        colpisceBoss /= CAMPIONI;
        dannoBoss /= CAMPIONI;
        salutePg /= CAMPIONI;
        out.printf("   %-9s %-22s %7.0f %7.0f%% %6.0f %7s | %7.0f %7.0f%% %6s %7s | %s turni (%.0f%% x %.0f su %.0f)%n",
                classePg, equipaggiamento.getNome(),
                attaccoFisico, colpisceFisico, dannoArma, turni(saluteBoss, dannoArma, colpisceFisico),
                attaccoMagico, colpisceMagico, immuneAlFuoco ? "immune" : String.format("%.0f", dannoFuoco),
                turni(saluteBoss, dannoFuoco, colpisceMagico),
                turni(salutePg, dannoBoss, colpisceBoss), colpisceBoss, dannoBoss, salutePg);
    }

    private static void stampaFuocoControMostriComuni(PrintStream out, int livello) {
        out.println();
        out.println("=== Fuoco del Mago (" + Equipaggiamento.tipiciPer(ClassePersonaggio.MAGO).get(0).getNome()
                + ") contro i mostri comuni, livello " + livello);
        out.printf("   %-10s %7s %8s %6s %7s%n", "Mostro", "Salute", "Colpisce", "Danno", "Turni");
        Equipaggiamento equipaggiamento = Equipaggiamento.tipiciPer(ClassePersonaggio.MAGO).get(0);
        for (ClassePersonaggio classeMostro : MOSTRI_COMUNI) {
            double salute = 0, colpisce = 0, danno = 0;
            boolean immune = false;
            for (int i = 0; i < CAMPIONI; i++) {
                Personaggio mago = ClassePersonaggio.MAGO.getIstanza(livello);
                equipaggiamento.equipaggia(mago);
                Personaggio mostro = classeMostro.getIstanza(livello);
                immune = mostro.isImmuneAIncantesimo(ClasseIncantesimo.FUOCO);
                salute += mostro.getSaluteMassima();
                colpisce += CalcolatoreCombattimento.calcolaProbabilitaDiColpire(mago, mostro, SupertipoDanno.MAGICO);
                danno += CalcolatoreCombattimento.calcolaDannoRisultante(mago, mostro, new Fuoco(livello)).getDanno();
            }
            salute /= CAMPIONI;
            colpisce /= CAMPIONI;
            danno = immune ? 0 : danno / CAMPIONI;
            out.printf("   %-10s %7.0f %7.0f%% %6s %7s%n", classeMostro, salute, colpisce,
                    immune ? "immune" : String.format("%.0f", danno), turni(salute, danno, colpisce));
        }
    }

    /**
     * I turni medi per buttare giù un bersaglio: i colpi che servono, divisi per la probabilità di colpire.
     */
    private static String turni(double salute, double danno, double probabilitaPercentuale) {
        if (danno <= 0 || probabilitaPercentuale <= 0) {
            return "mai";
        }
        double colpi = Math.ceil(salute / danno);
        return String.format("%.1f", colpi / (probabilitaPercentuale / 100.0d));
    }
}
