package com.threeamigos.foresta.strumenti.dado;

/**
 * Per che cosa si sta lanciando il dado: chi sviluppa può truccare il lancio di un contesto solo (vedi
 * {@link TruccatoreDiDadi}), senza toccare gli altri. I lanci senza contesto ({@code Dado.tira(...)}) non si possono
 * truccare così: restano al caso, o alla coda di {@link Dado#trucca}.
 *
 * @author Stefano Reksten
 */
public enum ContestoLancio {

    /** La mossa dell'avversario nella sfida a carta, forbici e sasso: 1, 2 o 3, nell'ordine delle mosse. */
    CARTA_FORBICI_SASSO("Mossa a carta, forbici, sasso", false),
    /** Il tipo di mostro che una locazione sceglie fra quelli possibili. */
    TIPO_MOSTRO("Tipo di mostro", true),
    /** Quanti mostri di quel tipo trova il gruppo. */
    NUMERO_MOSTRI("Numero di mostri", false),
    /** Che cosa c'è in un cofano, da 0 (niente) a 7 (pietre preziose), o da 1 a 6 nei castelli. */
    COFANO_ESITO("Contenuto del cofano", false),
    /** L'oggetto raro di un cofano, al posto dell'esito solito: una chiave scelta fra quelle proposte dal cofano. */
    COFANO_OGGETTO("Oggetto speciale del cofano", true),
    /** La percentuale di monete e preziosi che il gruppo perde fuggendo, da 1 al massimo (vedi CostantiPersonaggi). */
    FUGA_PERDITA_DENARO("Fuga, perdita denaro (%)", false),
    /** La percentuale di incantesimi e pozioni che il gruppo perde fuggendo, da 1 al massimo (vedi CostantiPersonaggi). */
    FUGA_PERDITA_SCORTE("Fuga, perdita scorte (%)", false),
    /** Il tiro (da 1 a 100) con cui un mostro colpisce: va a segno se non supera la probabilità di colpire, che arriva al massimo a 95. */
    COLPO_DEI_MOSTRI("Colpo dei mostri", false),
    /** Il tiro (da 1 a 100) di una voce delle tattiche dei mostri con una probabilità, per esempio quella di lanciare un incantesimo: la voce vale se non supera la percentuale. */
    TATTICA_DEI_MOSTRI("Tattica dei mostri", false),
    /** Il tiro (da 1 a 100) per passare inosservati: riesce se non supera la probabilità, che è almeno 5. */
    PASSAGGIO_INOSSERVATO("Passare inosservati", false),
    /** Il tiro del dado della corruzione (da 1 a 10): riesce se supera la soglia, avendo le monete. */
    CORRUZIONE("Corruzione", false),
    /** Il tiro del dado dell'amicizia: riesce se è minore del carisma. */
    AMICIZIA("Amicizia", false),
    /** Il tiro (da 1 a 100) che fa scattare la sfida a carta, forbici e sasso dopo un'amicizia: scatta se non supera la percentuale. */
    SFIDA_CARTA_FORBICI_SASSO("Sfida a carta", false);

    private final String descrizione;
    private final boolean scelta;

    ContestoLancio(String descrizione, boolean scelta) {
        this.descrizione = descrizione;
        this.scelta = scelta;
    }

    /**
     * Il nome del contesto, come lo mostra la finestra di prova.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Vero se il contesto sceglie fra elementi (un tipo di mostro, un oggetto) invece di dare un numero.
     */
    public boolean isScelta() {
        return scelta;
    }
}
