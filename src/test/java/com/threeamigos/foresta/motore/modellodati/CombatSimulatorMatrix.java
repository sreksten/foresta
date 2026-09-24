package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.motore.DannoRisultante;
import com.threeamigos.foresta.motore.FaseDiAttacco;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Personaggio.NotificaMorte;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Simulatore di combattimento isolato (Monte Carlo Matrix Test), non collegato allo stato
 * di gioco reale ({@code Gruppo}/{@code BusEventi}). Vedi piano_montecarlo_matrix.md.
 * <p>
 * Il PG può avere un {@link Equipaggiamento} (armi, scudo, elmo, armatura, incantamenti): ogni attacco
 * passa dalle fasi di {@link CalcolatoreCombattimento#fasiDiAttacco}, come nel gioco, quindi conta anche
 * la seconda arma. Senza equipaggiamento il PG combatte con l'arma naturale, come nella prima versione.
 */
public class CombatSimulatorMatrix {

    static final int TURNI_MASSIMI = 100;

    public static RisultatoMatrice simulaScontroGruppo(ClassePersonaggio classePg, ClassePersonaggio classeMostro,
                                                       int quantitaMostri, int livello, int iterazioni) {
        return simulaScontroGruppo(classePg, Equipaggiamento.NESSUNO, classeMostro, quantitaMostri, livello, iterazioni);
    }

    /**
     * @param equipaggiamento quel che porta il PG, costruito al suo livello
     * @throws IllegalArgumentException se la classe non può portare l'equipaggiamento
     *                                  (vedi {@link Equipaggiamento#motivoRifiuto})
     */
    public static RisultatoMatrice simulaScontroGruppo(ClassePersonaggio classePg, Equipaggiamento equipaggiamento,
                                                       ClassePersonaggio classeMostro, int quantitaMostri,
                                                       int livello, int iterazioni) {
        int vittoriePg = 0;
        int sconfittePg = 0;
        int stalli = 0;
        long turniTotali = 0;
        double stanchezzaTotaleFinale = 0;
        StatisticheAttacco statistichePg = new StatisticheAttacco();
        StatisticheAttacco statisticheMostro = new StatisticheAttacco();

        for (int i = 0; i < iterazioni; i++) {
            Personaggio pg = classePg.getIstanza(livello);
            Optional<String> rifiuto = equipaggiamento.equipaggia(pg);
            if (rifiuto.isPresent()) {
                throw new IllegalArgumentException(classePg + " non può portare " + equipaggiamento + " (" + rifiuto.get() + ")");
            }

            List<Personaggio> mostri = new ArrayList<>();
            for (int m = 0; m < quantitaMostri; m++) {
                mostri.add(classeMostro.getIstanza(livello));
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
                    // Come nel gioco, la seconda arma colpisce lo stesso bersaglio della prima,
                    // o il prossimo vivo se la prima l'ha ucciso
                    for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(pg)) {
                        Personaggio bersaglio = bersaglioVivo(mostri, b);
                        if (bersaglio == null) {
                            break;
                        }
                        attacca(pg, bersaglio, fase, statistichePg);
                    }
                }

                // Pulizia dei mostri sconfitti
                mostri.removeIf(mostro -> !mostro.isVivo());

                if (mostri.isEmpty()) {
                    vittoria = true;
                    break;
                }

                // Turno dei mostri superstiti
                for (int m = 0; m < mostri.size() && pg.isVivo(); m++) {
                    for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(mostri.get(m))) {
                        if (pg.isVivo()) {
                            attacca(mostri.get(m), pg, fase, statisticheMostro);
                        }
                    }
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

    /**
     * Il mostro in posizione preferita se è vivo, altrimenti il primo vivo dopo di lui (o prima), o null
     */
    private static Personaggio bersaglioVivo(List<Personaggio> mostri, int preferito) {
        for (int i = 0; i < mostri.size(); i++) {
            Personaggio mostro = mostri.get((preferito + i) % mostri.size());
            if (mostro.isVivo()) {
                return mostro;
            }
        }
        return null;
    }

    private static void attacca(Personaggio attaccante, Personaggio difensore, FaseDiAttacco fase, StatisticheAttacco statistiche) {
        statistiche.tentativi++;
        SupertipoDanno superTipoDanno = fase.getArma().getTipoDanno().getSuperTipo();

        // Controllo e gestione corretta dell'ordine dei parametri (Attaccante -> Difensore)
        if (CalcolatoreCombattimento.colpisce(attaccante, difensore, superTipoDanno)) {
            statistiche.colpiti++;

            DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore,
                    fase.getArma(), fase.getFattore());

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
