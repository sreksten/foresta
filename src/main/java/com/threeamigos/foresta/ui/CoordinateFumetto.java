package com.threeamigos.foresta.ui;

/**
 * Coordinate da usare per disegnare un fumetto a schermo. Le coordinate X e Y sono quelle del punto
 * INFERIORE sinistro del fumetto; le coordinate pointTo quelle del punto verso cui la freccia del balloon punta.
 *
 * @author Stefano Reksten
 */
public class CoordinateFumetto {

    /**
     * Coordinata sinistra del fumetto
     */
    private final int x;
    /**
     * Coordinata INFERIORE del fumetto (per facilitare il disegno del fumetto verso l'alto)
     */
    private final int y;
    /**
     * Coordinata X del punto a cui la freccia del balloon punta
     */
    private final int pointToX;
    /**
     * Coordinata Y del punto a cui la freccia del balloon punta
     */
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

    /**
     * @return coordinata sinistra del fumetto
     */
    public int getX() {
        return x;
    }

    /**
     * @return coordinata INFERIORE del fumetto
     */
    public int getY() {
        return y;
    }

    /**
     * @return coordinata X del punto a cui la freccia del balloon punta
     */
    public int getPointToX() {
        return pointToX;
    }

    /**
     * @return coordinata Y del punto a cui la freccia del balloon punta
     */
    public int getPointToY() {
        return pointToy;
    }
}
