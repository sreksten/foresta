package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.PersonaggioMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

/**
 *
 * @author Stefano Reksten
 */
public class LanciatoreDeiDadi {

    //Percentuali sulle quali redistribuire i punti iniziali. Nell'ordine:
    // FORZA, DESTREZZA, COSTITUZIONE, INTELLIGENZA, SAGGEZZA, CARISMA, FORTUNA

    // Razze e Classi Giocabili (Budget PG [] = {70 Punti a Livello 1)

    // Focus [] = {Carisma e Bilanciamento Mentale
    private static final int[] BARDO = { 10, 15, 10, 15, 15, 25, 10 };
    // Focus [] = {Destrezza e Sensi
    private static final int[] ELFO = { 10, 25, 10, 15, 15, 15, 10 };
    //Focus [] = {Forza e Costituzione
    private static final int[] GUERRIERO = { 25, 15, 25, 8, 10, 12, 5 };
    // Focus [] = {Destrezza e Fortuna
    private static final int[] LADRO = { 10, 25, 10, 10, 10, 10, 25 };
    // Focus [] = {Intelligenza e Saggezza
    private static final int[] MAGO = { 5, 10, 10, 30, 25, 15, 5 };
    // Equilibrato ed intelligente
    private static final int[] OMBRAFIAMMA = { 24, 12, 22, 16, 14, 18, 4 };

    // Umanoidi e Piccole Creature (Budget Mostro Comune [] = {50 Punti a Livello 1)

    private static final int[] ARPIA = { 10, 25, 10, 10, 15, 10, 10 };
    private static final int[] EREMITA = { 8, 8, 14, 20, 25, 15, 10 };
    private static final int[] FOLLETTO = { 5, 30, 5, 15, 10, 10, 25 };
    private static final int[] GOBLIN = { 10, 25, 10, 10, 8, 7, 25 };
    private static final int[] HOBGOBLIN = { 22, 15, 20, 12, 11, 15, 5 };
    // (Alta Costituzione/Ossa, zero Mente)
    private static final int[] SCHELETRO = { 20, 20, 25, 5, 5, 5, 20 };

    // Creature Spettrali ed Eteree (Budget Mostro Comune [] = {50 Punti a Livello 1)

    //Forza azzerata nativamente
    private static final int[] FANTASMA = { 0, 20, 15, 15, 15, 25, 10 };
    private static final int[] SPETTRO = { 5, 25, 15, 15, 10, 25, 5 };
    private static final int[] SPIRITO = { 0, 25, 10, 20, 20, 20, 5 };

    // I Bruti e i Grandi Mostri (Budget Elite/Boss [] = {105 Punti a Livello 1)

    private static final int[] CENTAURO = { 22, 18, 20, 8, 12, 10, 10 };
    private static final int[] CHIMERA = { 25, 20, 25, 8, 10, 7, 5 };
    private static final int[] GARGOYLE = { 25, 10, 30, 8, 12, 10, 5 };
    private static final int[] MINOTAURO = { 30, 12, 25, 5, 8, 15, 5 };
    // Focus estremo su Costituzione/Rigenerazione
    private static final int[] TROLL = { 28, 12, 35, 5, 5, 5, 10 };
    private static final int[] VIVERNA = { 25, 22, 23, 5, 10, 10, 5 };

    // I Colossi e i Boss Leggendari (Budget Elite/Boss [] = {105 Punti a Livello 1)

    private static final int[] CHIMERADRAGO = { 28, 15, 25, 10, 10, 7, 5 };
    // Molto equilibrato e intelligente
    private static final int[] GIGANTE = { 35, 8, 30, 5, 8, 9, 5 };
    private static final int[] OMBRANERA = { 15, 25, 15, 10, 10, 20, 5 };
    private static final int[] TITANO = { 30, 10, 25, 12, 13, 15, 5 };

    // Boss leggendari

    private static final int[] MINOTAUROGIGANTE = { 35, 8, 30, 5, 5, 12, 5 };
    private static final int[] IDRA = { 26, 14, 35, 5, 10, 5, 5 };

    // Il Mago Supremo dei non-morti
    private static final int[] LICH = { 5, 10, 15, 30, 20, 15, 5 };
    private static final int[] STREGA = { 7, 13, 10, 25, 20, 20, 5 };

    private static final int[] DRAGO = { 24, 12, 22, 16, 14, 18, 4 };

    private static int[] getPercentualiPer(ClassePersonaggio classePersonaggio) {
        switch (classePersonaggio) {
            case ARPIA:
                return ARPIA;
            case BARDO:
            case CANTASTORIE:
                return BARDO;
            case CENTAURO:
                return CENTAURO;
            case CHIMERA:
                return CHIMERA;
            case CHIMERA_DRAGO:
                return CHIMERADRAGO;
            case DRAGO:
                return DRAGO;
            case ELFA:
            case ELFO:
                return ELFO;
            case EREMITA:
                return EREMITA;
            case FANTASMA:
                return FANTASMA;
            case FOLLETTO:
                return FOLLETTO;
            case GARGOYLE:
                return GARGOYLE;
            case GIGANTE:
                return GIGANTE;
            case GOBLIN:
                return GOBLIN;
            case GUERRIERA:
            case GUERRIERO:
                return GUERRIERO;
            case HOBGOBLIN:
                return HOBGOBLIN;
            case IDRA:
                return IDRA;
            case LADRA:
            case LADRO:
                return LADRO;
            case LICH:
                return LICH;
            case MAGA:
            case MAGO:
                return MAGO;
            case MINOTAURO:
                return MINOTAURO;
            case MINOTAURO_GIGANTE:
                return MINOTAUROGIGANTE;
            case OMBRAFIAMMA:
                return OMBRAFIAMMA;
            case OMBRA_NERA:
                return OMBRANERA;
            case SCHELETRO:
                return SCHELETRO;
            case SPETTRO:
                return SPETTRO;
            case SPIRITO:
                return SPIRITO;
            case STREGA:
                return STREGA;
            case TITANO:
                return TITANO;
            case TROLL:
                return TROLL;
            case VIVERNA:
                return VIVERNA;
            default:
                throw new IllegalArgumentException("Tipo di personaggio non valido");
        }
    }

    private static int getPuntiDaDistribuire(ClassePersonaggio classePersonaggio, int livello) {
        switch (classePersonaggio) {
            // Boss finale:
            case DRAGO:
                return 200 + (livello - 1) * 6;
            case LICH:
            case STREGA:
                return 155 + (livello - 1) * 5;
            case IDRA:
            case MINOTAURO_GIGANTE:
                return 135 + (livello - 1) * 4;
            case BARDO:
            case CANTASTORIE:
            case ELFA:
            case ELFO:
            case GUERRIERA:
            case GUERRIERO:
            case LADRA:
            case LADRO:
                return 35 * (livello - 1) * 3;
            default:
            return 15 + (livello - 1) * 3;
        }
    }

    public static void tiraDadiPer(ClassePersonaggio classePersonaggio, int livello, PersonaggioMD md) {

        int puntiDaDistribuire = getPuntiDaDistribuire(classePersonaggio, livello);
        int[] percentuali = getPercentualiPer(classePersonaggio);

        // 1. Calcolo iniziale deterministico (la linea di base stabile)
        double forza = 5.0d + (puntiDaDistribuire * percentuali[0]) / 100.0d;
        double destrezza = 5.0d + (puntiDaDistribuire * percentuali[1]) / 100.0d;
        double costituzione = 5.0d + (puntiDaDistribuire * percentuali[2]) / 100.0d;
        double intelligenza = 5.0d + (puntiDaDistribuire * percentuali[3]) / 100.0d;
        double saggezza = 5.0d + (puntiDaDistribuire * percentuali[4]) / 100.0d;
        double carisma = 5.0d + (puntiDaDistribuire * percentuali[5]) / 100.0d;
        double fortuna = 5.0d + (puntiDaDistribuire * percentuali[6]) / 100.0d;

        // 2. INTRODUZIONE DELLA TOLLERANZA (Scostamento del 5%)
        java.util.Random random = new java.util.Random();
        double percentualeScostamento = 0.05; // 5%
        double poolMutazione = 0.0d;

        // Calcoliamo quanti punti togliere temporaneamente a ogni statistica per rimescolarli
        double tolFor = forza * percentualeScostamento;
        forza -= tolFor;
        int forzaInt = (int)forza;
        poolMutazione += tolFor + (forza - forzaInt);

        double tolDes = destrezza * percentualeScostamento;
        destrezza -= tolDes;
        int destrezzaInt = (int)destrezza;
        poolMutazione += tolDes + (destrezza - destrezzaInt);

        double tolCos = costituzione * percentualeScostamento;
        costituzione -= tolCos;
        int costituzioneInt = (int)costituzione;
        poolMutazione += tolCos + (costituzione - costituzioneInt);

        double tolInt = intelligenza * percentualeScostamento;
        intelligenza -= tolInt;
        int intelligenzaInt = (int)intelligenza;
        poolMutazione += tolInt + (intelligenza - intelligenzaInt);

        double tolSag = saggezza * percentualeScostamento;
        saggezza -= tolSag;
        int saggezzaInt = (int)saggezza;
        poolMutazione += tolSag + (saggezza - saggezzaInt);

        double tolCar = carisma * percentualeScostamento;
        carisma -= tolCar;
        int carismaInt = (int)carisma;
        poolMutazione += tolCar + (carisma - carismaInt);

        double tolFot = fortuna * percentualeScostamento;
        fortuna -= tolFot;
        int fortunaInt = (int)fortuna;
        poolMutazione += tolFot + (fortuna - fortunaInt);

        // Ridistribuiamo il pool accumulato in modo totalmente casuale 1 punto alla volta
        // Questo garantisce che nessun punto vada perso e il budget resti matematicamente intatto!
        while (poolMutazione > 0) {
            int statisticaCasuale = random.nextInt(7); // Genera un numero da 0 a 6
            if (statisticaCasuale == 0) {
                forzaInt++;
            } else if (statisticaCasuale == 1) {
                destrezzaInt++;
            } else if (statisticaCasuale == 2) {
                costituzioneInt++;
            } else if (statisticaCasuale == 3) {
                intelligenzaInt++;
            } else if (statisticaCasuale == 4) {
                saggezzaInt++;
            } else if (statisticaCasuale == 5) {
                carismaInt++;
            } else if (statisticaCasuale == 6) {
                fortunaInt++;
            }
            poolMutazione--;
        }

        md.setForza(forzaInt);
        md.setDestrezza(destrezzaInt);
        md.setCostituzione(costituzioneInt);
        md.setIntelligenza(intelligenzaInt);
        md.setSaggezza(saggezzaInt);
        md.setCarisma(carismaInt);
        md.setFortuna(fortunaInt);
    }
}
