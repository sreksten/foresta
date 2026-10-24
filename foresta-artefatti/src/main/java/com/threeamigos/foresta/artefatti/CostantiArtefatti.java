package com.threeamigos.foresta.artefatti;

/**
 * I valori di bilanciamento degli artefatti: come nascono (probabilità di essere incantati, di avere un nome proprio o di venire
 * dalla grammatica, danni delle armi, effetti degli ingredienti), da che livello una pergamena è di grado medio o maggiore, e quanto
 * valgono e costano le pergamene. Quelli degli altri oggetti del gioco (cofani, anelli...) restano in {@code Costanti}.
 *
 * @author Stefano Reksten
 */
public final class CostantiArtefatti {

    // ---- Come nasce un artefatto (vedi GeneratoreArtefattiTabelle)

    /**
     * Probabilità che un artefatto generato abbia un nome proprio.
     */
    public static final double ARTEFATTO_PROBABILITA_NOME_PROPRIO = 0.05;

    /**
     * Probabilità che un artefatto incantabile generato sia raro (i leggendari non si generano).
     */
    public static final double ARTEFATTO_PROBABILITA_RARO = 0.1;

    /**
     * Probabilità, per ogni livello oltre il primo, che un artefatto incantabile generato nasca già incantato: il 5%, fino al
     * massimo {@link #ARTEFATTO_PROBABILITA_INCANTATO_MASSIMA}.
     */
    public static final double ARTEFATTO_PROBABILITA_INCANTATO_PER_LIVELLO = 0.05;

    /**
     * Il tetto della probabilità che un artefatto incantabile nasca già incantato.
     */
    public static final double ARTEFATTO_PROBABILITA_INCANTATO_MASSIMA = 0.6;

    /**
     * Quota degli artefatti che prendono nome ed effetti dalla grammatica (artefatti2.txt), per i tipi che conosce. Metà per provarla
     * accanto alle tabelle: da rivedere quando sarà bilanciata.
     */
    public static final double ARTEFATTO_PROBABILITA_DA_GRAMMATICA = 0.5;

    /**
     * Gli incantamenti al massimo con cui un artefatto nasce; ne resta sempre almeno un posto libero per la fusione.
     */
    public static final int ARTEFATTO_MASSIMO_INCANTAMENTI_IN_NASCITA = 3;

    // ---- Le armi

    /**
     * Il danno medio di un'arma generata è {@code ARMA_DANNI_BASE + ARMA_DANNI_PER_LIVELLO × livello}, ma almeno
     * {@code ARMA_DANNI_MINIMI_BASE + ARMA_DANNI_MINIMI_PER_LIVELLO × livello}, così ai livelli bassi un'arma fa ben più delle mani
     * nude di un personaggio (4 + 1,5 × √FORZA, cioè 9-10). Le due curve si incontrano al livello 12.
     */
    public static final int ARMA_DANNI_BASE = 4;

    /**
     * I danni medi in più, per livello, di un'arma generata (vedi {@link #ARMA_DANNI_BASE}).
     */
    public static final int ARMA_DANNI_PER_LIVELLO = 2;

    /**
     * I danni medi minimi di un'arma di livello zero (vedi {@link #ARMA_DANNI_BASE}).
     */
    public static final int ARMA_DANNI_MINIMI_BASE = 16;

    /**
     * I danni medi minimi in più, per livello, di un'arma generata (vedi {@link #ARMA_DANNI_BASE}).
     */
    public static final int ARMA_DANNI_MINIMI_PER_LIVELLO = 1;

    /**
     * Di quanto si moltiplicano danni e prezzo di un'arma a due mani rispetto a una a una mano.
     */
    public static final double ARTEFATTO_MOLTIPLICATORE_DUE_MANI = 1.5;

    // ---- I gradi degli incantamenti (vedi GradoIncantamento)

    /**
     * Il livello da cui un incantamento è di grado medio: fino al livello 3 è minore.
     */
    public static final int GRADO_INCANTAMENTO_MEDIO_DAL_LIVELLO = 4;

    /**
     * Il livello da cui un incantamento è di grado maggiore: da 4 a 7 è medio.
     */
    public static final int GRADO_INCANTAMENTO_MAGGIORE_DAL_LIVELLO = 8;

    // ---- Gli ingredienti magici

    /**
     * Il livello da cui gli effetti di un ingrediente magico sono di grado medio: minori fino al livello 2.
     */
    public static final int INGREDIENTE_GRADO_MEDIO_DAL_LIVELLO = 3;

    /**
     * Il livello da cui gli effetti di un ingrediente magico sono di grado maggiore.
     */
    public static final int INGREDIENTE_GRADO_MAGGIORE_DAL_LIVELLO = 5;

    /**
     * Un ingrediente magico ha tanti effetti quanto il suo livello, fino a questo numero.
     */
    public static final int INGREDIENTE_EFFETTI_MASSIMI = 3;

    // ---- I prezzi (vedi ListinoPergamene)

    /**
     * Il prezzo di un incantamento di un ingrediente magico, per punto di danno grezzo che aggiunge.
     */
    public static final double PERGAMENA_PREZZO_PER_PUNTO_DI_DANNO = 0.5;

    /**
     * Il prezzo di un modificatore di un ingrediente magico, per ogni 1% di cui aumenta il valore tipico dell'attributo.
     */
    public static final double PERGAMENA_PREZZO_PER_PERCENTO_DI_ATTRIBUTO = 0.5;

    /**
     * Il prezzo di un modificatore che porta l'attributo a una quantità fissa, per punto di quella quantità.
     */
    public static final int PERGAMENA_PREZZO_PER_PUNTO_ASSOLUTO = 5;

    /**
     * "Porta a 0" (quantità assoluta con 0) non vale 5 × 0: costa questo, fisso (prima del moltiplicatore del tempo).
     */
    public static final double PERGAMENA_PREZZO_PORTA_A_ZERO = 50;

    /**
     * Un ingrediente non è mai gratis: una gemma minore con la sola parte percentuale vale meno di una moneta, e costa questo.
     */
    public static final int PERGAMENA_PREZZO_MINIMO = 1;

    /**
     * La maggiorazione del prezzo di un ingrediente che dà un effetto di stato.
     */
    public static final double PERGAMENA_MAGGIORAZIONE_EFFETTI_DI_STATO = 0.25;

    /**
     * Il prezzo di un modificatore a tempo è quello del modificatore permanente per questo coefficiente per ogni turno di durata,
     * fino a {@link #PERGAMENA_MOLTIPLICATORE_PREZZO_TEMPO_MASSIMO}.
     */
    public static final double PERGAMENA_COEFFICIENTE_PREZZO_PER_TURNO = 0.5;

    /**
     * Il tetto per cui si moltiplica il prezzo di un modificatore a tempo rispetto al permanente.
     */
    public static final double PERGAMENA_MOLTIPLICATORE_PREZZO_TEMPO_MASSIMO = 2.0;

    /**
     * Quanti turni vale una locazione, per il prezzo di un modificatore che dura per locazioni.
     */
    public static final int PERGAMENA_TURNI_PER_LOCAZIONE = 5;

    // ---- Gli oggetti speciali dei cofani

    /**
     * Quanto vale, per livello, un oggetto che fa salire di livello un artefatto (non si compra né si vende).
     */
    public static final int AGGIORNAMENTO_VALORE_PER_LIVELLO = 100;

    /**
     * Quanto vale, per livello, il libro che riprogramma un artefatto (non si compra né si vende).
     */
    public static final int RIPROGRAMMAZIONE_VALORE_PER_LIVELLO = 200;

    private CostantiArtefatti() {
    }
}
