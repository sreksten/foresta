package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;


/**
 * La curva dell'esperienza: quanti punti servono per raggiungere un livello. Cresce con il quadrato del livello meno uno, per una
 * costante di bilanciamento: toccandola si rallenta o si velocizza il gioco. Sono solo calcoli: quanta esperienza dà un fatto del gioco lo dice
 * {@link EsperienzaDelGruppo}.
 *
 * @author Stefano Reksten
 */
public class GestoreProgressione {

    // Costante di bilanciamento. Si tocca questa per rallentare o velocizzare il gioco.
    private static final int COSTANTE_XP = 100;

    /**
     * Calcola gli XP TOTALI CUMULATIVI necessari per raggiungere un determinato livello.
     * Es: per il livello 1 restituisce 0, per il livello 2 restituisce 100, ecc.
     */
    public static int getXpNecessariPerLivello(int livello) {
        if (livello <= 1) return 0;
        return (int) (COSTANTE_XP * Math.pow(livello - 1, 2));
    }

    /**
     * Calcola gli XP richiesti SPECIFICATAMENTE per superare il livello attuale
     * e passare a quello successivo (la classica "barra degli XP" che si riempie).
     */
    public static int getXpRichiestiPerProssimoLivello(int livelloAttuale) {
        int xpLivelloCorrente = getXpNecessariPerLivello(livelloAttuale);
        int xpProssimoLivello = getXpNecessariPerLivello(livelloAttuale + 1);
        return xpProssimoLivello - xpLivelloCorrente;
    }

    /**
     * Gli XP totali cumulativi con cui si raggiunge il livello successivo a quello attuale.
     */
    public static int getXpPerProssimoLivello(int livelloAttuale) {
        return getXpNecessariPerLivello(livelloAttuale + 1);
    }

    /**
     * Gli XP da aggiungere per far salire di un livello chi ha quell'esperienza: quelli che mancano al livello successivo, più
     * quelli che servono perché per il livello dopo ancora gli manchi la stessa percentuale (arrotondata per eccesso, così il
     * salto è sempre di un solo livello). Se il livello successivo è già il massimo, bastano quelli che mancano a raggiungerlo.
     * Zero se è già al livello massimo.
     */
    public static int getXpPerAumentoDiLivello(int livello, int xpAttuali) {
        if (livello >= CostantiPersonaggi.MASSIMO_LIVELLO) return 0;
        long mancanti = Math.max(0, getXpPerProssimoLivello(livello) - xpAttuali);
        if (livello + 1 >= CostantiPersonaggi.MASSIMO_LIVELLO) return (int) mancanti;
        long ampiezzaLivello = getXpRichiestiPerProssimoLivello(livello);
        long ampiezzaProssimo = getXpRichiestiPerProssimoLivello(livello + 1);
        long mancantiDopo = (mancanti * ampiezzaProssimo + ampiezzaLivello - 1) / ampiezzaLivello;
        return (int) (mancanti + ampiezzaProssimo - mancantiDopo);
    }

    /**
     * Calcola matematicamente il livello attuale del personaggio basandosi
     * unicamente sugli XP totali accumulati nella sua carriera.
     */
    public static int calcolaLivelloDaXp(int xpTotali) {
        if (xpTotali <= 0) return 1;

        // Invertiamo la formula quadratica usando la radice quadrata
        int livelloCalcolato = (int) Math.floor(Math.sqrt((double) xpTotali / COSTANTE_XP)) + 1;

        // Mettiamo un tetto massimo per evitare che i livelli scalino all'infinito
        return Math.min(livelloCalcolato, CostantiPersonaggi.MASSIMO_LIVELLO);
    }
}
