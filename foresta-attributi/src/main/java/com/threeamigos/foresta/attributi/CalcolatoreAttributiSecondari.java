package com.threeamigos.foresta.attributi;

/**
 * Le formule degli attributi secondari: ognuno si ricava da due attributi primari, uno che pesa di più e uno di meno, e dal
 * moltiplicatore della classe del personaggio.
 * <p>
 * Non sa niente di chi possiede gli attributi: riceve i valori <b>attuali</b> dei primari (già modificati da artefatti e
 * incantamenti, e troncati a interi come fa il gioco) e il moltiplicatore, e restituisce il valore secondario. Sta a chi lo
 * chiama (il personaggio) leggere i primari, scegliere il moltiplicatore della sua classe e scrivere il risultato.
 * <p>
 * Le formule sommano le <b>radici quadrate</b> dei due primari, ciascuna per il suo peso: raddoppiare un primario non
 * raddoppia il secondario (rendimenti decrescenti). Il valore grezzo si moltiplica poi per il moltiplicatore della classe
 * e si tronca a intero. I secondari che dipendono dai primari come
 * <i>(principale, secondario)</i> si chiamano con i parametri in quell'ordine.
 *
 * @author Stefano Reksten
 */
public final class CalcolatoreAttributiSecondari {

    /** Il critico non supera mai questa percentuale. */
    public static final int CRITICO_MASSIMO = 100;

    private CalcolatoreAttributiSecondari() {
    }

    /**
     * Una somma pesata delle radici dei due attributi di partenza: la base di tutte le formule tranne il carico.
     */
    private static double grezzo(double pesoPrincipale, int principale, double pesoSecondario, int secondario) {
        return pesoPrincipale * Math.sqrt(principale) + pesoSecondario * Math.sqrt(secondario);
    }

    /**
     * Il critico (0-100): la precisione nel colpire i punti scoperti, alimentata da DESTREZZA (peso 70%, la mira
     * chirurgica sulle fessure dell'armatura) e da FORTUNA (peso 30%, il fato che fa deviare il colpo nel punto giusto).
     * Con Destrezza 25 e Fortuna 25 un Ladro ha circa il 15% di critico.
     *
     * @return la percentuale di critico, mai sopra {@link #CRITICO_MASSIMO}
     */
    public static int critico(int destrezza, int fortuna, double moltiplicatore) {
        final double COEFFICIENTE_DESTREZZA = 2.0;
        final double COEFFICIENTE_FORTUNA = 0.8;
        double critico = grezzo(COEFFICIENTE_DESTREZZA, destrezza, COEFFICIENTE_FORTUNA, fortuna) * moltiplicatore;
        return (int) Math.min(critico, CRITICO_MASSIMO);
    }

    /**
     * La precisione (occhio, mano ferma, coordinazione): DESTREZZA (80%, la coordinazione motoria fine) e INTELLIGENZA
     * (20%, il calcolo della traiettoria e dei movimenti del bersaglio).
     */
    public static int precisione(int destrezza, int intelligenza, double moltiplicatore) {
        return (int) (grezzo(0.8, destrezza, 0.2, intelligenza) * moltiplicatore);
    }

    /**
     * La velocità: DESTREZZA (85%, la rapidità di gambe e braccia) e FORTUNA (15%, il tempismo che fa agire per primi).
     */
    public static int velocita(int destrezza, int fortuna, double moltiplicatore) {
        return (int) (grezzo(0.85, destrezza, 0.15, fortuna) * moltiplicatore);
    }

    /**
     * La furtività (muoversi senza farsi notare): DESTREZZA (75%, la grazia e il controllo del corpo) e FORTUNA (25%, il
     * tempismo con cui un tuono o il vento coprono i passi).
     */
    public static int furtivita(int destrezza, int fortuna, double moltiplicatore) {
        return (int) (grezzo(0.75, destrezza, 0.25, fortuna) * moltiplicatore);
    }

    /**
     * La parata (frapporre arma o scudo al colpo): FORZA (70%, reggere l'impatto senza farsi spezzare la guardia) e
     * DESTREZZA (30%, i riflessi per mettere lo scudo nell'angolo giusto).
     */
    public static int parata(int forza, int destrezza, double moltiplicatore) {
        return (int) (grezzo(0.70, forza, 0.30, destrezza) * moltiplicatore);
    }

    /**
     * La resistenza magica (controllo dei flussi e fermezza d'animo): SAGGEZZA (70%, la connessione spirituale che fa
     * da scudo contro gli anatemi) e INTELLIGENZA (30%, la comprensione della trama dell'incantesimo per deviarla).
     */
    public static int resistenzaMagica(int saggezza, int intelligenza, double moltiplicatore) {
        return (int) (grezzo(0.70, saggezza, 0.30, intelligenza) * moltiplicatore);
    }

    /**
     * La percezione (vista e udito acuti): SAGGEZZA (75%, l'intuito e il "sesto senso") e DESTREZZA (25%, la prontezza
     * nel volgere lo sguardo verso uno stimolo improvviso).
     */
    public static int percezione(int saggezza, int destrezza, double moltiplicatore) {
        return (int) (grezzo(0.75, saggezza, 0.25, destrezza) * moltiplicatore);
    }

    /**
     * La soggezione (la forza della personalità unita all'aura di terrore): CARISMA (70%, il magnetismo) e FORZA (30%,
     * la stazza che intimidisce).
     */
    public static int soggezione(int carisma, int forza, double moltiplicatore) {
        return (int) (grezzo(0.70, carisma, 0.30, forza) * moltiplicatore);
    }

    /**
     * La furia (impulso distruttivo e resistenza al dolore): FORZA (75%, la potenza dei colpi nella rabbia) e FORTUNA
     * (25%, il fato che premia l'audacia di chi attacca alla cieca).
     */
    public static int furia(int forza, int fortuna, double moltiplicatore) {
        return (int) (grezzo(0.75, forza, 0.25, fortuna) * moltiplicatore);
    }

    /**
     * Il coraggio (forza di volontà): CARISMA (75%, la stabilità della personalità che non si fa intimidire) e
     * COSTITUZIONE (25%, il controllo del battito e dell'adrenalina che evita il panico fisico).
     */
    public static int coraggio(int carisma, int costituzione, double moltiplicatore) {
        return (int) (grezzo(0.75, carisma, 0.25, costituzione) * moltiplicatore);
    }

    /**
     * La contrattazione (strappare un prezzo migliore): CARISMA (60%, la parlantina) e FORTUNA (40%, trovare il
     * mercante nella giornata buona). Con moltiplicatore 0 (chi non tratta coi mercanti) vale 0.
     */
    public static int contrattazione(int carisma, int fortuna, double moltiplicatore) {
        return (int) (grezzo(0.60, carisma, 0.40, fortuna) * moltiplicatore);
    }

    /**
     * Il valore (lo spirito di sacrificio): SAGGEZZA (70%, la rettitudine che spinge a difendere i deboli) e
     * COSTITUZIONE (30%, la tempra per reggere i colpi intercettati per gli altri).
     */
    public static int valore(int saggezza, int costituzione, double moltiplicatore) {
        return (int) (grezzo(0.70, saggezza, 0.30, costituzione) * moltiplicatore);
    }

    /**
     * Quanti bersagli un personaggio riesce a minacciare insieme: 70% la sua forza trainante e 30% quella di appoggio. Quali
     * siano i due primari dipende dalla classe (i maghi usano INTELLIGENZA e SAGGEZZA, i bruti FORZA e COSTITUZIONE...) e
     * lo sceglie il chiamante.
     *
     * @return almeno 1
     */
    public static int numeroBersagli(int principale, int secondario, double moltiplicatore) {
        return (int) Math.max(1, Math.floor(grezzo(0.70, principale, 0.30, secondario) * moltiplicatore));
    }

    /**
     * Il carico massimo trasportabile prima dei modificatori di CARICO_MASSIMO: FORZA pesa 12 per radice (la potenza
     * muscolare per sollevare) e COSTITUZIONE 6 (la tempra per camminare sotto sforzo). Il totale si moltiplica per il
     * moltiplicatore di classe (un Guerriero porta più di una Maga, un Drago più di un Goblin); con moltiplicatore 0
     * (i fantasmi) non porta niente.
     *
     * @return il carico, che il chiamante tronca a intero dove serve
     */
    public static double potenzialeDiCarico(int forza, int costituzione, double moltiplicatore) {
        if (moltiplicatore == 0.0) {
            return 0.0;
        }
        final double PESO_PER_RADICE_FORZA = 12.0;
        final double PESO_PER_RADICE_COSTITUZIONE = 6.0;
        return (PESO_PER_RADICE_FORZA * Math.sqrt(forza) + PESO_PER_RADICE_COSTITUZIONE * Math.sqrt(costituzione)) * moltiplicatore;
    }
}
