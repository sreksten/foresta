package com.threeamigos.foresta.strumenti;

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
    COFANO_OGGETTO("Oggetto speciale del cofano", true);

    private final String descrizione;
    private final boolean scelta;

    ContestoLancio(String descrizione, boolean scelta) {
        this.descrizione = descrizione;
        this.scelta = scelta;
    }

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
