package com.threeamigos.foresta.ui;

/**
 * Una classe che restituisce una volta un colore e una volta un altro
 * Default: MEDIUM_GRAY, LIGHT_GRAY
 * @author Stefano Reksten
 */
public class DoomdarkColorAlternante {

    private final DoomdarkColorModel.Color color1;
    private final DoomdarkColorModel.Color color2;
    private DoomdarkColorModel.Color color;

    public DoomdarkColorAlternante() {
        this(DoomdarkColorModel.Color.MEDIUM_GRAY, DoomdarkColorModel.Color.LIGHT_GRAY);
    }

    public DoomdarkColorAlternante(DoomdarkColorModel.Color color1, DoomdarkColorModel.Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        color = color1;
    }

    public DoomdarkColorModel.Color getColor() {
        color = color == color1 ? color2 : color1;
        return color;
    }
}
