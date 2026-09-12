package com.threeamigos.foresta.ui;

/**
 *
 * @author Stefano Reksten
 */
class CoordinateFumetto {

    private final int x;
    private final int y;
    private final int pointToX;
    private final int pointToy;

    /**
     * ATTENZIONE: il punto (x, y) indica la posizione IN BASSO a sinistra del fumetto.
     */
    public CoordinateFumetto(int x, int y, int pointToX, int pointToy) {
        this.x = x;
        this.y = y;
        this.pointToX = pointToX;
        this.pointToy = pointToy;
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
        return pointToy;
    }
}
