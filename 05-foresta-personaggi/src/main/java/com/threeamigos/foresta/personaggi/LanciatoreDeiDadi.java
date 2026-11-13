package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Distribuisce a un personaggio i punti degli attributi di base alla creazione: un budget di punti, che cresce con il livello, ripartito in
 * percentuali diverse per ogni classe (il guerriero punta su forza e costituzione, il mago su intelligenza e saggezza...). Lo chiama
 * PersonaggioBase quando nasce un personaggio, giocante o mostro.
 * <p>
 * Ogni attributo parte da 5 (i “35 punti di base”, 5 per 7 attributi); a questi si aggiungono i punti del budget, ripartiti con le
 * percentuali della classe. Il budget a livello 1 è di 35 punti per i personaggi giocanti, i non combattenti e il Necromante (totale 70),
 * di 15 per i mostri comuni (totale 50) e di più per i boss (vedi {@link #getPuntiDaDistribuire}). Un po' di caso: a ogni attributo si
 * toglie circa il 5% e i punti tolti si ridistribuiscono a caso fra i sette; il totale resta sempre quello del budget.
 *
 * @author Stefano Reksten
 */
public class LanciatoreDeiDadi {

    // Le percentuali con cui ogni classe ripartisce il suo budget (la somma fa sempre 100: lo controlla LanciatoreDeiDadiTest). Nell'ordine:
    // FORZA, DESTREZZA, COSTITUZIONE, INTELLIGENZA, SAGGEZZA, CARISMA, FORTUNA
    // I titoletti raggruppano le classi per genere; il budget di ciascuna lo dice getPuntiDaDistribuire, non il gruppo in cui sta qui.

    // Le classi giocabili (budget di 70 punti a livello 1, compresi i 35 di base)

    // Carisma, e la mente in equilibrio
    private static final int[] BARDO = { 10, 15, 10, 15, 15, 25, 10 };
    // Destrezza, e un po' di tutto il resto
    private static final int[] ELFO = { 10, 25, 10, 15, 15, 15, 10 };
    // Forza e costituzione
    private static final int[] GUERRIERO = { 25, 15, 25, 8, 10, 12, 5 };
    // Destrezza e fortuna
    private static final int[] LADRO = { 10, 25, 10, 10, 10, 10, 25 };
    // Intelligenza e saggezza
    private static final int[] MAGO = { 5, 10, 10, 30, 25, 15, 5 };
    // Il sacerdote punta sulla Saggezza, con più costituzione del mago
    private static final int[] SACERDOTE = { 5, 10, 15, 20, 30, 15, 5 };
    // Il chierico: forza e costituzione del guerriero, con la saggezza del sacerdote
    private static final int[] CHIERICO = { 20, 8, 22, 10, 22, 13, 5 };
    // Il druido: saggezza e intelligenza, con destrezza e un po' di fortuna
    private static final int[] DRUIDO = { 5, 15, 12, 20, 28, 10, 10 };
    // L'Ombrafiamma, personaggio segreto: forza e costituzione, con il resto in equilibrio (come il Drago); ha il budget dei giocanti
    private static final int[] OMBRAFIAMMA = { 22, 11, 20, 14, 13, 16, 4 };

    // Umanoidi e piccole creature (budget dei mostri comuni: 50 punti a livello 1)

    private static final int[] ARPIA = { 12, 28, 12, 10, 15, 10, 13 };
    private static final int[] EREMITA = { 8, 8, 14, 20, 25, 15, 10 };
    private static final int[] FOLLETTO = { 5, 30, 5, 15, 10, 10, 25 };
    private static final int[] GOBLIN = { 10, 25, 12, 10, 10, 8, 25 };
    private static final int[] HOBGOBLIN = { 22, 15, 20, 12, 11, 15, 5 };
    // Costituzione alta (le ossa) e la mente al minimo
    private static final int[] SCHELETRO = { 20, 20, 25, 5, 5, 5, 20 };

    // Creature spettrali ed eteree (budget dei mostri comuni: 50 punti a livello 1)

    // Nessun punto alla forza, che resta vicina ai 5 di base (non a zero: vedi tiraDadiPer); così anche lo Spirito
    private static final int[] FANTASMA = { 0, 20, 15, 15, 15, 25, 10 };
    private static final int[] SPETTRO = { 5, 25, 15, 15, 10, 25, 5 };
    private static final int[] SPIRITO = { 0, 25, 10, 20, 20, 20, 5 };

    // I bruti e i grandi mostri (anche loro con il budget dei mostri comuni: 50 punti a livello 1)

    private static final int[] CENTAURO = { 22, 18, 20, 8, 12, 10, 10 };
    private static final int[] CHIMERA = { 25, 20, 25, 8, 10, 7, 5 };
    private static final int[] GARGOYLE = { 25, 10, 30, 8, 12, 10, 5 };
    private static final int[] MINOTAURO = { 30, 12, 25, 5, 8, 15, 5 };
    // Focus estremo su Costituzione/Rigenerazione
    private static final int[] TROLL = { 28, 12, 35, 5, 5, 5, 10 };
    private static final int[] VIVERNA = { 25, 22, 23, 5, 10, 10, 5 };

    // I colossi (anche loro con il budget dei mostri comuni: 50 punti a livello 1)

    private static final int[] CHIMERADRAGO = { 28, 15, 25, 10, 10, 7, 5 };
    // Forza e costituzione, la mente al minimo
    private static final int[] GIGANTE = { 35, 8, 30, 5, 8, 9, 5 };
    // L'Ombra nera, l'altro mago oscuro con il Necromante: come lui ha il budget dei giocanti (70)
    private static final int[] OMBRANERA = { 15, 25, 15, 10, 10, 20, 5 };
    private static final int[] TITANO = { 27, 9, 23, 11, 12, 13, 5 };

    // I boss (budget più alto, vedi getPuntiDaDistribuire) e il Necromante (budget dei giocanti)

    private static final int[] MINOTAUROGIGANTE = { 35, 8, 30, 5, 5, 12, 5 };
    private static final int[] IDRA = { 26, 14, 35, 5, 10, 5, 5 };

    // Il Mago Supremo dei non-morti
    private static final int[] LICH = { 5, 10, 15, 30, 20, 15, 5 };
    // Il Necromante: un mago che punta su Intelligenza e Saggezza, con il carisma al minimo
    private static final int[] NECROMANTE = { 5, 10, 15, 35, 25, 5, 5 };
    private static final int[] STREGA = { 7, 13, 10, 25, 20, 20, 5 };
    private static final int[] DRAGO = { 22, 11, 20, 14, 13, 16, 4 };

    /**
     * Le percentuali della classe, nell'ordine degli attributi scritto sopra. Le classi senza un profilo proprio (i non combattenti)
     * hanno quello del bardo: è il ramo {@code default}, che sta in mezzo all'elenco dei bardi e vale anche per ogni classe nuova.
     */
    static int[] getPercentualiPer(TipoPersonaggio classePersonaggio) {
        // Si guarda il ruolo (TipoPersonaggio.getRuolo): la forma femminile ha le percentuali della maschile
        switch (classePersonaggio.getRuolo()) {
            case ARPIA: return ARPIA;
            case BARDO:
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
            case ELFO: return ELFO;
            case EREMITA: return EREMITA;
            case FANTASMA: return FANTASMA;
            case FOLLETTO: return FOLLETTO;
            case GARGOYLE: return GARGOYLE;
            case GIGANTE: return GIGANTE;
            case GOBLIN: return GOBLIN;
            case GUERRIERO:
            case CAPITANO_DELLE_GUARDIE:
            case VICHINGO: return GUERRIERO;
            case HOBGOBLIN: return HOBGOBLIN;
            case IDRA: return IDRA;
            case LADRO: return LADRO;
            case LICH: return LICH;
            case NECROMANTE: return NECROMANTE;
            case MAGO: return MAGO;
            case SACERDOTE: return SACERDOTE;
            case CHIERICO: return CHIERICO;
            case DRUIDO: return DRUIDO;
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

    /**
     * Il budget della classe a quel livello, oltre ai 35 punti di base (5 per attributo): a livello 1 35 per i giocanti, i non combattenti
     * (Viandante compreso), l'Ombrafiamma e i due maghi oscuri, Necromante e Ombra nera; 15 per i mostri comuni (tutte le classi non elencate); di più per i boss. Ogni livello oltre il primo
     * aggiunge qualche punto (da 3 a 6, secondo la classe). Un livello sotto 1 conta come 1.
     */
    private static int getPuntiDaDistribuire(TipoPersonaggio classePersonaggio, int livello) {
        if (livello < 1) livello = 1;
        int livelloModificato = livello - 1;

        // Si guarda il ruolo (TipoPersonaggio.getRuolo): la forma femminile ha il budget della maschile
        switch (classePersonaggio.getRuolo()) {
            // I boss: con i 35 punti di base, a livello 1 il doppio di un personaggio giocante (70) per il Drago (140) e da 1,6 a 1,8
            // volte per gli altri (115 e 125), perché si affrontano con il gruppo intero; crescono un po' più in fretta
            case DRAGO:
                return 105 + (livelloModificato * 6);
            case LICH:
            case STREGA:
                return 90 + (livelloModificato * 5);
            case IDRA:
            case MINOTAURO_GIGANTE:
                return 80 + (livelloModificato * 4);
            case BARDO:
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
            case ELFO:
            case GUERRIERO:
            case CAPITANO_DELLE_GUARDIE:
            case VICHINGO:
            case LADRO:
            case MAGO:
            case NECROMANTE:
            case OMBRA_NERA:
            case OMBRAFIAMMA:
            case SACERDOTE:
            case CHIERICO:
            case DRUIDO:
                // Giocanti, non combattenti, Ombrafiamma e i due maghi oscuri: 35 punti a livello 1 (70 con quelli di base), +4 per ogni livello successivo
                return 35 + (livelloModificato * 4);
            default:
                // Mostri comuni: 15 punti a livello 1 (50 con quelli di base), +3 per ogni livello successivo
                return 15 + (livelloModificato * 3);
        }
    }

    /**
     * Distribuisce i punti da assegnare a un personaggio di quella classe e di quel livello, scrivendo i sette attributi primari nel suo
     * modello dati. Il risultato cambia a ogni chiamata (c'è un po' di caso, preso da {@link Dado#sorgente()}), ma il totale no: è sempre
     * il budget più i 35 di base.
     */
    public static void tiraDadiPer(TipoPersonaggio classePersonaggio, int livello, PersonaggioMD md) {
        int puntiDaDistribuire = getPuntiDaDistribuire(classePersonaggio, livello);
        int[] percentuali = getPercentualiPer(classePersonaggio);

        // 1. Il valore teorico di ogni attributo, con i decimali: 5 di base più la sua percentuale del budget
        double fGrezzo = 5.0d + (puntiDaDistribuire * percentuali[0]) / 100.0d;
        double dGrezzo = 5.0d + (puntiDaDistribuire * percentuali[1]) / 100.0d;
        double cGrezzo = 5.0d + (puntiDaDistribuire * percentuali[2]) / 100.0d;
        double iGrezzo = 5.0d + (puntiDaDistribuire * percentuali[3]) / 100.0d;
        double sGrezzo = 5.0d + (puntiDaDistribuire * percentuali[4]) / 100.0d;
        double caGrezzo = 5.0d + (puntiDaDistribuire * percentuali[5]) / 100.0d;
        double foGrezzo = 5.0d + (puntiDaDistribuire * percentuali[6]) / 100.0d;

        // 2. A ogni attributo si toglie il 5% (troncando a intero) e si mettono da parte i punti tolti, interi, in poolMutazione.
        // Le frazioni che restano fuori da entrambi tornano al passo 4
        double percentualeScostamento = 0.05d;
        int poolMutazione = 0;

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

        // 3. I punti messi da parte si ridistribuiscono uno alla volta, ciascuno a un attributo pescato a caso fra i sette
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

        // 4. Le frazioni perse troncando: il totale deve tornare esattamente al budget (le percentuali fanno 100, quindi la somma dei
        // valori teorici è proprio questa). Il resto, da 0 a 6 punti (meno di uno per attributo), va all'attributo che conta di più per la classe
        int budgetTotaleAtteso = puntiDaDistribuire + 35; // i 35 punti di base: 5 per ognuno dei 7 attributi
        int sommaAttuale = forzaInt + destrezzaInt + costituzioneInt + intelligenzaInt + saggezzaInt + carismaInt + fortunaInt;
        int resto = budgetTotaleAtteso - sommaAttuale;

        // L'intelligenza ai maghi, la saggezza a sacerdoti, chierici e druidi, la forza a tutti gli altri (anche ai fantasmi, che non ne hanno)
        // Il ruolo (TipoPersonaggio.getRuolo) mette insieme la forma maschile e la femminile: la Maga come il Mago
        TipoPersonaggio ruolo = classePersonaggio.getRuolo();
        if (ruolo == TipoPersonaggio.MAGO || ruolo == TipoPersonaggio.NECROMANTE || ruolo == TipoPersonaggio.LICH || ruolo == TipoPersonaggio.STREGA) {
            intelligenzaInt += resto;
        } else if (ruolo == TipoPersonaggio.SACERDOTE || ruolo == TipoPersonaggio.CHIERICO || ruolo == TipoPersonaggio.DRUIDO) {
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
