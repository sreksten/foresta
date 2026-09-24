package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.incantesimi.PortataIncantesimo;
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
 * <p>
 * Il PG può avere anche una {@link ScortaDiPergamene}: finché ne ha una e abbastanza MAGIA, a ogni turno lancia
 * l'incantesimo invece di attaccare con le armi. Mago ed Elfo, senza pergamene, lanciano il dardo arcano se promette
 * più danno delle armi (probabilità di colpire per danno, come fanno i mostri quando scelgono fra magia e armi).
 * I mostri combattono solo con le armi.
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
        return simulaScontroGruppo(classePg, equipaggiamento, ScortaDiPergamene.NESSUNA, classeMostro, quantitaMostri,
                livello, iterazioni);
    }

    /**
     * @param pergamene gli incantesimi che il PG può lanciare nello scontro
     */
    public static RisultatoMatrice simulaScontroGruppo(ClassePersonaggio classePg, Equipaggiamento equipaggiamento,
                                                       ScortaDiPergamene pergamene, ClassePersonaggio classeMostro,
                                                       int quantitaMostri, int livello, int iterazioni) {
        return simulaScontroGruppo(classePg, equipaggiamento, pergamene, classeMostro, quantitaMostri, livello, livello,
                iterazioni);
    }

    /**
     * @param livello        il livello del PG (e del suo equipaggiamento)
     * @param livelloMostri  il livello dei mostri, per scontri più duri di quelli alla pari
     */
    public static RisultatoMatrice simulaScontroGruppo(ClassePersonaggio classePg, Equipaggiamento equipaggiamento,
                                                       ScortaDiPergamene pergamene, ClassePersonaggio classeMostro,
                                                       int quantitaMostri, int livello, int livelloMostri,
                                                       int iterazioni) {
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
                mostri.add(classeMostro.getIstanza(livelloMostri));
            }

            boolean vittoria = false;
            boolean sconfitta = false;
            int turni = 0;
            int pergameneRimaste = pergamene.getQuantita();

            while (turni < TURNI_MASSIMI) {
                turni++;

                // FIX CRITICO: Forza il valore minimo di bersagli a 1 per evitare che
                // il turno del giocatore venga saltato a causa di statistiche non caricate.
                int bersagli = Math.min(Math.max(1, pg.getBersagli()), mostri.size());
                IncantesimoMalefico incantesimo = pergameneRimaste > 0
                        ? (IncantesimoMalefico) pergamene.getIncantesimo().getIstanza(pg.getLivello())
                        : null;
                Personaggio primoVivo = bersaglioVivo(mostri, 0);
                if (incantesimo != null && pg.getMagia() >= incantesimo.getCostoLancio()) {
                    pergameneRimaste--;
                    lancia(pg, incantesimo, mostri, statistichePg);
                } else if (primoVivo != null && conviene(pg, primoVivo)) {
                    DardoArcano dardo = new DardoArcano(pg);
                    attacca(pg, primoVivo, new FaseDiAttacco(dardo, 1.0d), statistichePg);
                    pg.subMagia(dardo.getCostoLancio());
                } else {
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
     * Come nel gioco: un incantesimo di portata GRUPPO colpisce tutti i mostri vivi, gli altri il primo vivo.
     * Ogni bersaglio si colpisce o si manca per conto suo.
     */
    private static void lancia(Personaggio pg, IncantesimoMalefico incantesimo, List<Personaggio> mostri,
                               StatisticheAttacco statistiche) {
        List<Personaggio> bersagli = new ArrayList<>();
        for (Personaggio mostro : mostri) {
            if (mostro.isVivo()) {
                bersagli.add(mostro);
                if (incantesimo.getClasse().getPortata() != PortataIncantesimo.GRUPPO) {
                    break;
                }
            }
        }
        for (Personaggio bersaglio : bersagli) {
            attacca(pg, bersaglio, new FaseDiAttacco(incantesimo, 1.0d), statistiche);
        }
        pg.subMagia(incantesimo.getCostoLancio());
    }

    /**
     * Se al PG conviene il dardo arcano invece delle armi: deve poterlo lanciare, e la probabilità di colpire per
     * il danno deve essere maggiore di quella delle sue fasi di attacco con le armi.
     */
    private static boolean conviene(Personaggio pg, Personaggio bersaglio) {
        if (!DardoArcano.puoLanciarlo(pg)) {
            return false;
        }
        DardoArcano dardo = new DardoArcano(pg);
        double conDardo = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(pg, bersaglio, dardo.getTipoDanno().getSuperTipo())
                * CalcolatoreCombattimento.calcolaDannoRisultante(pg, bersaglio, dardo).getDanno();
        double conArmi = 0.0d;
        for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(pg)) {
            conArmi += CalcolatoreCombattimento.calcolaProbabilitaDiColpire(pg, bersaglio, fase.getArma().getTipoDanno().getSuperTipo())
                    * CalcolatoreCombattimento.calcolaDannoRisultante(pg, bersaglio, fase.getArma(), fase.getFattore()).getDanno();
        }
        return conDardo > conArmi;
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
