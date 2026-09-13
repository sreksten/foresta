package com.threeamigos.foresta.eventi;

/**
 * Evento interno del motore UI che mostra un certo messaggio sotto forma di fumetto.
 * (Commerciante che rifiuta una vendita, Personaggio che non può aggiungere un Artefatto al proprio inventario...)
 *
 * @author Stefano Reksten
 */
public class EventoFumetto extends EventoBase {

    private final String testo;
    private final int x;
    private final int y;
    private final int pointToX;
    private final int pointToY;

    /**
     * @param testo il testo del fumetto da mostrare
     * @param x la coordinata sinistra del fumetto
     * @param y la coordinata INFERIORE del fumetto (per semplificare il calcolo della posizione del fumetto)
     * @param pointToX la coordinata X del punto verso cui la freccia del balloon deve puntare
     * @param pointToY la coordinata Y del punto verso cui la freccia del balloon deve puntare
     */
    public EventoFumetto(String testo, int x, int y, int pointToX, int pointToY) {
        super(TipoEvento.FUMETTO);
        this.testo = testo;
        this.x = x;
        this.y = y;
        this.pointToX = pointToX;
        this.pointToY = pointToY;
    }

    /**
     * @return il testo del fumetto da mostrare
     */
    public String getTesto() {
        return testo;
    }

    /**
     * @return la coordinata sinistra del fumetto
     */
    public int getX() {
        return x;
    }

    /**
     * @return la coordinata INFERIORE del fumetto (per semplificare il calcolo della posizione del fumetto)
     */
    public int getY() {
        return y;
    }

    /**
     * @return la coordinata X del punto verso cui la freccia del balloon deve puntare
     */
    public int getPointToX() {
        return pointToX;
    }

    /**
     * @return la coordinata Y del punto verso cui la freccia del balloon deve puntare
     */
    public int getPointToY() {
        return pointToY;
    }

}
