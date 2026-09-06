package com.threeamigos.foresta.ui.sfx;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CloudInstance {
    private final BufferedImage image;
    private float x;
    private float y;
    private final float speed;
    private final Rectangle clipBounds; // Il riquadro in cui la nuvola può muoversi
    private final Random rand = new Random();

    public CloudInstance(BufferedImage image, float startX, float startY, float speed, Rectangle clipBounds) {
        this.image = image;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.clipBounds = clipBounds;
    }

    // Aggiorna la posizione muovendosi solo nello spazio consentito
    public void update() {
        x += speed;

        // Confine destro del riquadro (X + larghezza)
        int maxRight = clipBounds.x + clipBounds.width;

        // Se la nuvola supera il confine destro del riquadro...
        if (x > maxRight) {
            // ...si resetta appena prima del bordo sinistro del riquadro
            x = clipBounds.x - image.getWidth();

            // Genera una nuova altezza Y casuale, ma sempre dentro il riquadro
            int altezzaMassimaDisponibile = clipBounds.height - image.getHeight();
            if (altezzaMassimaDisponibile > 0) {
                y = clipBounds.y + rand.nextInt(altezzaMassimaDisponibile);
            } else {
                y = clipBounds.y; // Fallback se la nuvola è più alta del riquadro stesso
            }
        }
    }

    // Getters per il disegno
    public BufferedImage getImage() { return image; }
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
}