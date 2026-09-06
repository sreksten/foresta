package com.threeamigos.foresta.motore;

/**
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
     * Calcola gli XP richiesti SPECIFICATAMENTE per superare il livello attuale
     * e passare a quello successivo (la classica "barra degli XP" che si riempie).
     */
    public static int getXpPerProssimoLivello(int livelloAttuale) {
        return getXpNecessariPerLivello(livelloAttuale + 1);
    }

    /**
     * Calcola matematicamente il livello attuale del personaggio basandosi
     * unicamente sugli XP totali accumulati nella sua carriera.
     */
    public static int calcolaLivelloDaXp(int xpTotali) {
        if (xpTotali <= 0) return 1;

        // Invertiamo la formula quadratica usando la radice quadrata
        int livelloCalcolato = (int) Math.floor(Math.sqrt((double) xpTotali / COSTANTE_XP)) + 1;

        // Mettiamo un tetto massimo (CAP) per evitare che i livelli scalino all'infinito
        int capMassimoLivello = 50;
        return Math.min(livelloCalcolato, capMassimoLivello);
    }

    /**
     * Un artefatto dà il 5% dei punti necessari per arrivare al livello successivo
     */
    public static void acquisisciArtefattoMinore() {
        aggiungiPercentuale(5);
    }

    /**
     * Una missione secondaria dà il 20% dei punti necessari per arrivare al livello successivo
     */
    public static void completaMissioneSecondaria() {
        aggiungiPercentuale(20);
    }

    /**
     * Una missione principale dà il 50% dei punti necessari per arrivare al livello successivo
     */
    public static void completaMissionePrincipale() {
        aggiungiPercentuale(50);
    }

    private static void aggiungiPercentuale(int percentuale) {
        GruppoGiocatore.getIstanza().addPuntiEsperienza(getXpPerProssimoLivello(Statistiche.getLivello() * percentuale / 100));
    }
}