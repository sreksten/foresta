package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public class EventoFumetto extends EventoBase {

    private final String testo;
    private final int x;
    private final int y;
    private final int pointToX;
    private final int pointToY;

    public EventoFumetto(String testo, int x, int y, int pointToX, int pointToY) {
        super(TipoEvento.FUMETTO);
        this.testo = testo;
        this.x = x;
        this.y = y;
        this.pointToX = pointToX;
        this.pointToY = pointToY;
    }

    public String getTesto() {
        return testo;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPointToX() {
        return pointToX;
    }

    public int getPointToY() {
        return pointToY;
    }

}
