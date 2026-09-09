package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.motore.Arma;
import com.threeamigos.foresta.motore.ArmaNaturale;
import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.motore.DannoRisultante;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Personaggio.NotificaMorte;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulatore di combattimento isolato (Monte Carlo Matrix Test), non collegato allo stato
 * di gioco reale ({@code Gruppo}/{@code BusEventi}). Vedi piano_montecarlo_matrix.md.
 */
public class CombatSimulatorMatrix {

    static final int TURNI_MASSIMI = 100;

    public static RisultatoMatrice simulaScontroGruppo(ClassePersonaggio classePg, ClassePersonaggio classeMostro,
                                                       int quantitaMostri, int livello, int iterazioni) {
        int vittoriePg = 0;
        int sconfittePg = 0;
        int stalli = 0;
        long turniTotali = 0;
        double stanchezzaTotaleFinale = 0;
        StatisticheAttacco statistichePg = new StatisticheAttacco();
        StatisticheAttacco statisticheMostro = new StatisticheAttacco();

        for (int i = 0; i < iterazioni; i++) {
            Personaggio pg = classePg.getIstanza(livello);
            Arma armaPg = new ArmaNaturale(pg);

            List<Personaggio> mostri = new ArrayList<>();
            List<Arma> armiMostri = new ArrayList<>();
            for (int m = 0; m < quantitaMostri; m++) {
                Personaggio mostro = classeMostro.getIstanza(livello);
                mostri.add(mostro);
                armiMostri.add(new ArmaNaturale(mostro));
            }

            boolean vittoria = false;
            boolean sconfitta = false;
            int turni = 0;

            while (turni < TURNI_MASSIMI) {
                turni++;

                // FIX CRITICO: Forza il valore minimo di bersagli a 1 per evitare che
                // il turno del giocatore venga saltato a causa di statistiche non caricate.
                int bersagli = Math.min(Math.max(1, pg.getBersagli()), mostri.size());
                for (int b = 0; b < bersagli; b++) {
                    attacca(pg, mostri.get(b), armaPg, statistichePg);
                }

                // Pulizia dei mostri sconfitti
                for (int m = mostri.size() - 1; m >= 0; m--) {
                    if (!mostri.get(m).isVivo()) {
                        mostri.remove(m);
                        armiMostri.remove(m);
                    }
                }

                if (mostri.isEmpty()) {
                    vittoria = true;
                    break;
                }

                // Turno dei mostri superstiti
                for (int m = 0; m < mostri.size() && pg.isVivo(); m++) {
                    attacca(mostri.get(m), pg, armiMostri.get(m), statisticheMostro);
                }

                if (!pg.isVivo()) {
                    sconfitta = true;
                    break;
                }
            }

            if (vittoria) {
                vittoriePg++;
            } else if (sconfitta) {
                sconfittePg++;
            } else {
                stalli++;
            }
            turniTotali += turni;
            stanchezzaTotaleFinale += pg.getStanchezza();
        }

        return new RisultatoMatrice(
                (vittoriePg / (double) iterazioni) * 100,
                (sconfittePg / (double) iterazioni) * 100,
                (stalli / (double) iterazioni) * 100,
                turniTotali / (double) iterazioni,
                stanchezzaTotaleFinale / iterazioni,
                statistichePg.tassoColpire(),
                statisticheMostro.tassoColpire(),
                statistichePg.dannoMedio(),
                statisticheMostro.dannoMedio()
        );
    }

    private static void attacca(Personaggio attaccante, Personaggio difensore, Arma arma, StatisticheAttacco statistiche) {
        statistiche.tentativi++;
        SupertipoDanno superTipoDanno = arma.getTipoDanno().getSuperTipo();

        // Controllo e gestione corretta dell'ordine dei parametri (Attaccante -> Difensore)
        if (CalcolatoreCombattimento.colpisce(attaccante, difensore, superTipoDanno)) {
            statistiche.colpiti++;

            DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma);

            statistiche.dannoTotale += risultato.getDanno();
            difensore.subSalute(risultato.getDanno(), attaccante, Personaggio.NotificaFerite.NO, NotificaMorte.NO);
        }
    }

    private static class StatisticheAttacco {
        long tentativi;
        long colpiti;
        long dannoTotale;

        double tassoColpire() {
            return tentativi == 0 ? 0.0d : ((double) colpiti / (double) tentativi) * 100.0d;
        }

        double dannoMedio() {
            return colpiti == 0 ? 0.0d : (double) dannoTotale / (double) colpiti;
        }
    }
}
