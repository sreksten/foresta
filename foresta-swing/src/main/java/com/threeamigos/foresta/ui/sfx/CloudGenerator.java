package com.threeamigos.foresta.ui.sfx;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CloudGenerator {

    /**
     * Genera una BufferedImage contenente una nuvola procedurale.
     *
     * @param width  Larghezza dell'immagine da generare
     * @param height Altezza dell'immagine da generare
     * @return BufferedImage con la nuvola su sfondo trasparente
     */
    public static BufferedImage generateCloud(int width, int height) {
        // Crea un'immagine con supporto alla trasparenza (ARGB)
        BufferedImage cloudImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = cloudImg.createGraphics();

        // Attiva l'antialiasing per bordi più morbidi
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Random rand = new Random();

        // Numero di "soffici cerchi" che comporranno la nuvola (es. tra 5 e 8)
        int numPuffs = 5 + rand.nextInt(4);

        for (int i = 0; i < numPuffs; i++) {
            // Calcola dimensioni casuali per ogni singolo puff di nuvola
            int puffRadius = Math.min(width, height) / 3 + rand.nextInt(Math.min(width, height) / 4);

            // Posiziona i cerchi principalmente verso il centro dell'immagine
            int centerX = width / 2 + (rand.nextInt(width / 2) - width / 4);
            int centerY = height / 2 + (rand.nextInt(height / 4) - height / 8);

            // Definisce i colori della sfumatura radiale:
            // Al centro il cerchio è bianco semitrasparente, al bordo diventa completamente trasparente
            float[] dist = {0.0f, 0.8f, 1.0f};
            Color[] colors = {
                    new Color(255, 255, 255, 180), // Centro: bianco compatto ma morbido
                    new Color(240, 240, 245, 90),  // Metà strada: un po' più trasparente e freddo
                    new Color(255, 255, 255, 0)    // Bordo esterno: completamente invisibile
            };

            // Crea la sfumatura radiale centrata sul puff corrente
            RadialGradientPaint gradient = new RadialGradientPaint(
                    new Point(centerX, centerY),
                    puffRadius,
                    dist,
                    colors
            );

            g2d.setPaint(gradient);

            // Disegna il puff di nuvola (usiamo un'ellisse leggermente schiacciata per farla sembrare realistica)
            int puffWidth = puffRadius * 2;
            int puffHeight = (int)(puffRadius * 1.4); // leggermente schiacciata sull'asse Y

            g2d.fillOval(centerX - puffWidth / 2, centerY - puffHeight / 2, puffWidth, puffHeight);
        }

        g2d.dispose();
        return cloudImg;
    }
}