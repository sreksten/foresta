package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Distribuisce a un personaggio i punti degli attributi di base alla creazione: un budget di punti, che cresce con il livello, ripartito in
 * percentuali diverse per ogni classe (il guerriero punta su forza e costituzione, il mago su intelligenza e saggezza...).
 *
 * @author Stefano Reksten
 */
public class LanciatoreDeiDadi {

    // Percentuali sulle quali redistribuire i punti iniziali. Nell'ordine:
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
    // Il sacerdote punta sulla Saggezza, con più costituzione del mago
    private static final int[] SACERDOTE = { 5, 10, 15, 20, 30, 15, 5 };
    // Il chierico: forza e costituzione del guerriero, con la saggezza del sacerdote
    private static final int[] CHIERICO = { 20, 8, 22, 10, 22, 13, 5 };
    // Il druido: saggezza e intelligenza, con destrezza e un po' di fortuna
    private static final int[] DRUIDO = { 5, 15, 12, 20, 28, 10, 10 };
    // Equilibrato ed intelligente
    private static final int[] OMBRAFIAMMA = { 22, 11, 20, 14, 13, 16, 4 };

    // Umanoidi e Piccole Creature (Budget Mostro Comune [] = {50 Punti a Livello 1)

    private static final int[] ARPIA = { 12, 28, 12, 10, 15, 10, 13 };
    private static final int[] EREMITA = { 8, 8, 14, 20, 25, 15, 10 };
    private static final int[] FOLLETTO = { 5, 30, 5, 15, 10, 10, 25 };
    private static final int[] GOBLIN = { 10, 25, 12, 10, 10, 8, 25 };
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
    private static final int[] TITANO = { 27, 9, 23, 11, 12, 13, 5 };

    // Boss leggendari

    private static final int[] MINOTAUROGIGANTE = { 35, 8, 30, 5, 5, 12, 5 };
    private static final int[] IDRA = { 26, 14, 35, 5, 10, 5, 5 };

    // Il Mago Supremo dei non-morti
    private static final int[] LICH = { 5, 10, 15, 30, 20, 15, 5 };
    // Il Necromante: un mago che punta su Intelligenza e Saggezza, con un po' di Carisma per spaventare
    private static final int[] NECROMANTE = { 5, 10, 15, 35, 25, 5, 5 };
    private static final int[] STREGA = { 7, 13, 10, 25, 20, 20, 5 };
    private static final int[] DRAGO = { 22, 11, 20, 14, 13, 16, 4 };

    static int[] getPercentualiPer(TipoPersonaggio classePersonaggio) {
        switch (classePersonaggio) {
            case ARPIA: return ARPIA;
            case BARDO:
            case CANTASTORIE:
            case VIANDANTE:
            case BARDO_DELLE_LOCANDE:
            case LOCANDIERE:
            case ARMAIOLO:
            case ALCHIMISTA:
            case VENDITORE_DI_PERGAMENE:
            case INCANTATORE:
            case MOGLIE_DEL_BARDO:
            case UOMO:
            case BORGOMASTRO:
            case DONNA:
            // Chi non ha un profilo suo (un nuovo non combattente) ha quello dei bardi: carisma e un po' di tutto
            default:
            return BARDO;
            case CENTAURO: return CENTAURO;
            case CHIMERA: return CHIMERA;
            case CHIMERA_DRAGO: return CHIMERADRAGO;
            case DRAGO: return DRAGO;
            case ELFA:
            case ELFO: return ELFO;
            case EREMITA: return EREMITA;
            case FANTASMA: return FANTASMA;
            case FOLLETTO: return FOLLETTO;
            case GARGOYLE: return GARGOYLE;
            case GIGANTE: return GIGANTE;
            case GOBLIN: return GOBLIN;
            case GUERRIERA:
            case GUERRIERO:
            case CAPITANO_DELLE_GUARDIE: return GUERRIERO;
            case HOBGOBLIN: return HOBGOBLIN;
            case IDRA: return IDRA;
            case LADRA:
            case LADRO: return LADRO;
            case LICH: return LICH;
            case NECROMANTE: return NECROMANTE;
            case MAGA:
            case MAGO: return MAGO;
            case SACERDOTE:
            case SACERDOTESSA: return SACERDOTE;
            case CHIERICO:
            case CHIERICA: return CHIERICO;
            case DRUIDO:
            case DRUIDA: return DRUIDO;
            case MINOTAURO: return MINOTAURO;
            case MINOTAURO_GIGANTE: return MINOTAUROGIGANTE;
            case OMBRAFIAMMA: return OMBRAFIAMMA;
            case OMBRA_NERA: return OMBRANERA;
            case SCHELETRO: return SCHELETRO;
            case SPETTRO: return SPETTRO;
            case SPIRITO: return SPIRITO;
            case STREGA: return STREGA;
            case TITANO: return TITANO;
            case TROLL: return TROLL;
            case VIVERNA: return VIVERNA;
        }
    }

    private static int getPuntiDaDistribuire(TipoPersonaggio classePersonaggio, int livello) {
        if (livello < 1) livello = 1;
        int livelloModificato = livello - 1;

        switch (classePersonaggio) {
            // I boss: con i 35 punti di base, circa il doppio di un personaggio giocante (70) per il Drago e una volta e
            // mezza-1,8 per gli altri a livello 1, perché si affrontano con il gruppo intero; crescono un po' più in fretta
            case DRAGO:
                return 105 + (livelloModificato * 6);
            case LICH:
            case STREGA:
                return 90 + (livelloModificato * 5);
            case IDRA:
            case MINOTAURO_GIGANTE:
                return 80 + (livelloModificato * 4);
            case BARDO:
            case CANTASTORIE:
            case VIANDANTE:
            case BARDO_DELLE_LOCANDE:
            case LOCANDIERE:
            case ARMAIOLO:
            case ALCHIMISTA:
            case VENDITORE_DI_PERGAMENE:
            case INCANTATORE:
            case MOGLIE_DEL_BARDO:
            case UOMO:
            case BORGOMASTRO:
            case DONNA:
            case ELFA:
            case ELFO:
            case GUERRIERA:
            case GUERRIERO:
            case CAPITANO_DELLE_GUARDIE:
            case LADRA:
            case LADRO:
            case MAGA:
            case MAGO:
            case NECROMANTE:
            case SACERDOTE:
            case SACERDOTESSA:
            case CHIERICO:
            case CHIERICA:
            case DRUIDO:
            case DRUIDA:
                // CORRETTO: Budget PG a livello 1 = 35 punti extra (+4 punti per ogni livello successivo)
                return 35 + (livelloModificato * 4);
            default:
                // Budget Mostro Comune a livello 1 = 15 punti extra (+3 punti per ogni livello successivo)
                return 15 + (livelloModificato * 3);
        }
    }

    /**
     * Distribuisce i punti da assegnare a un personaggio di quella classe e di quel livello, scrivendo gli attributi di base nel suo modello dati.
     */
    public static void tiraDadiPer(TipoPersonaggio classePersonaggio, int livello, PersonaggioMD md) {
        int puntiDaDistribuire = getPuntiDaDistribuire(classePersonaggio, livello);
        int[] percentuali = getPercentualiPer(classePersonaggio);

        // 1. Calcolo iniziale teorico preciso (double)
        double fGrezzo = 5.0d + (puntiDaDistribuire * percentuali[0]) / 100.0d;
        double dGrezzo = 5.0d + (puntiDaDistribuire * percentuali[1]) / 100.0d;
        double cGrezzo = 5.0d + (puntiDaDistribuire * percentuali[2]) / 100.0d;
        double iGrezzo = 5.0d + (puntiDaDistribuire * percentuali[3]) / 100.0d;
        double sGrezzo = 5.0d + (puntiDaDistribuire * percentuali[4]) / 100.0d;
        double caGrezzo = 5.0d + (puntiDaDistribuire * percentuali[5]) / 100.0d;
        double foGrezzo = 5.0d + (puntiDaDistribuire * percentuali[6]) / 100.0d;

        // 2. Applicazione della tolleranza (Scostamento del 5%)
        double percentualeScostamento = 0.05d;
        int poolMutazione = 0;

        // Estraiamo la parte intera e accumuliamo le frazioni rimosse nel pool
        int forzaInt = (int) (fGrezzo * (1.0d - percentualeScostamento));
        poolMutazione += (int) (fGrezzo - forzaInt);

        int destrezzaInt = (int) (dGrezzo * (1.0d - percentualeScostamento));
        poolMutazione += (int) (dGrezzo - destrezzaInt);

        int costituzioneInt = (int) (cGrezzo * (1.0d - percentualeScostamento));
        poolMutazione += (int) (cGrezzo - costituzioneInt);

        int intelligenzaInt = (int) (iGrezzo * (1.0d - percentualeScostamento));
        poolMutazione += (int) (iGrezzo - intelligenzaInt);

        int saggezzaInt = (int) (sGrezzo * (1.0d - percentualeScostamento));
        poolMutazione += (int) (sGrezzo - saggezzaInt);

        int carismaInt = (int) (caGrezzo * (1.0d - percentualeScostamento));
        poolMutazione += (int) (caGrezzo - carismaInt);

        int fortunaInt = (int) (foGrezzo * (1.0d - percentualeScostamento));
        poolMutazione += (int) (foGrezzo - fortunaInt);

        // 3. Ridistribuzione del pool casuale (Gestito interamente a numeri INTERI)
        java.util.Random random = Dado.sorgente();
        int[] aggiunte = new int[7];
        while (poolMutazione > 0) {
            aggiunte[random.nextInt(7)]++;
            poolMutazione--;
        }
        forzaInt += aggiunte[0];
        destrezzaInt += aggiunte[1];
        costituzioneInt += aggiunte[2];
        intelligenzaInt += aggiunte[3];
        saggezzaInt += aggiunte[4];
        carismaInt += aggiunte[5];
        fortunaInt += aggiunte[6];

        // 4. Controllo finale del resto per blindare il budget totale complessivo
        int budgetTotaleAtteso = puntiDaDistribuire + 35; // 35 punti sono il minimo biologico (5 * 7)
        int sommaAttuale = forzaInt + destrezzaInt + costituzioneInt + intelligenzaInt + saggezzaInt + carismaInt + fortunaInt;
        int resto = budgetTotaleAtteso - sommaAttuale;

        // Assegniamo il resto degli arrotondamenti alla statistica chiave dell'archetipo
        if (classePersonaggio == TipoPersonaggio.MAGO || classePersonaggio == TipoPersonaggio.MAGA || classePersonaggio == TipoPersonaggio.NECROMANTE ||
                classePersonaggio == TipoPersonaggio.LICH || classePersonaggio == TipoPersonaggio.STREGA) {
            intelligenzaInt += resto;
        } else if (classePersonaggio == TipoPersonaggio.SACERDOTE || classePersonaggio == TipoPersonaggio.SACERDOTESSA
                || classePersonaggio == TipoPersonaggio.CHIERICO || classePersonaggio == TipoPersonaggio.CHIERICA
                || classePersonaggio == TipoPersonaggio.DRUIDO || classePersonaggio == TipoPersonaggio.DRUIDA) {
            saggezzaInt += resto;
        } else {
            forzaInt += resto;
        }

        // 5. Scrittura finale nel modello dati
        md.setForza(forzaInt);
        md.setDestrezza(destrezzaInt);
        md.setCostituzione(costituzioneInt);
        md.setIntelligenza(intelligenzaInt);
        md.setSaggezza(saggezzaInt);
        md.setCarisma(carismaInt);
        md.setFortuna(fortunaInt);
    }
}
