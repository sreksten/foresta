package com.threeamigos.foresta.tipi;

/**
 * I valori di bilanciamento dell'economia: contrattazione, magazzini dei negozi, prezzi (vedi economia.md).
 *
 * @author Stefano Reksten
 */
public final class CostantiEconomia {

    // Con cosa comincia una partita: le monete bastano a un'arma nuova per tutti dopo i primi pasti (vedi economia.md)
    public static final int MONETE_INIZIALI = 150;
    public static final int PREZIOSI_INIZIALI = 5;

    // Contrattazione (vedi RegoleContrattazione): lo sconto sugli acquisti e il ricavo delle vendite crescono con
    // bonus = contrattazione / (contrattazione + saturazione), fino a un limite che nessun modificatore supera.
    // Con la vendita al massimo al 75% e l'acquisto almeno all'80% non si guadagna comprando e rivendendo.
    public static final int CONTRATTAZIONE_SATURAZIONE = 4;
    public static final double CONTRATTAZIONE_COEFFICIENTE_SCONTO = 0.24;
    public static final double CONTRATTAZIONE_SCONTO_MASSIMO = 0.20;
    public static final double CONTRATTAZIONE_QUOTA_VENDITA_BASE = 0.50;
    public static final double CONTRATTAZIONE_COEFFICIENTE_VENDITA = 0.30;
    public static final double CONTRATTAZIONE_QUOTA_VENDITA_MASSIMA = 0.75;

    // Magazzini dei negozi di città, riforniti alla creazione del mondo e a ogni aumento del suo livello:
    // quanti artefatti per negozio, con livelli a rotazione fra livello del mondo - MAGAZZINO_DIVARIO_LIVELLO
    // e livello del mondo + MAGAZZINO_DIVARIO_LIVELLO (mai sotto 1)
    // A ogni aumento del livello del mondo l'armaiolo scarta gli artefatti di livello inferiore al livello
    // del mondo meno questo scarto
    public static final int MAGAZZINO_ARTEFATTI_ARMAIOLO = 6;
    public static final int MAGAZZINO_PERGAMENE = 6;
    public static final int MAGAZZINO_DIVARIO_LIVELLO = 1;
    public static final int MAGAZZINO_SCARTO_SOTTO_LIVELLO = 2;

    // Dall'alchimista
    public static final int COSTO_POZIONE_SALUTE = 5;
    public static final int COSTO_POZIONE_SALUTE_GRANDE = 15;
    public static final int COSTO_POZIONE_MAGIA = 10;
    public static final int COSTO_POZIONE_MAGIA_GRANDE = 30;
    public static final int COSTO_POZIONE_PANACEA = 5;
    public static final int COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO = 10;

    // In locanda: il pasto (e quanto fa recuperare) e il pernottamento
    public static final int COSTO_PASTO = 5;
    public static final int COSTO_PERNOTTAMENTO = 5;
    public static final int RECUPERO_SALUTE_DA_PASTO = 100;

    // Le mappe
    public static final int COSTO_MAPPA_DELLA_ZONA = 10;
    public static final int COSTO_MAPPA_DELLA_FORESTA = 20;

    private CostantiEconomia() {
    }
}
