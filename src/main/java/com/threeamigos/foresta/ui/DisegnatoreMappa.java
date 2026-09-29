package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.locazioni.Bosco;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 * @author Stefano Reksten
 */
public class DisegnatoreMappa {

    protected static final int LARGHEZZA_ICONA = ImageCache.mappa.get(ClassiLocazione.BOSCO).getWidth();
    protected static final int ALTEZZA_ICONA = ImageCache.mappa.get(ClassiLocazione.BOSCO).getHeight();

    protected boolean isSegnaliniVisibili() {
        return (System.currentTimeMillis() / 1000) % 2 == 0;
    }

    protected Image recuperaImmaginePerLocazione(CoordinateMD coordinateMD) {
        ClassiLocazione classeLocazione = Foresta.getLocazione(coordinateMD);
        BufferedImage image;
        if (classeLocazione == ClassiLocazione.BOSCO) {
            image = ImageCache.getImmagineMappaBosco(Bosco.getVarianteMappa(Foresta.getLocazioneMD(coordinateMD)));
        } else {
            image = ImageCache.mappa.get(classeLocazione);
        }
        return image;
    }

    protected void scurisci(Graphics2D g, int x, int y, int width, int height, int percentualeOscuramento) {
        // Calcola alpha (0 = trasparente, 255 = nero opaco)
        int alpha = (int) (percentualeOscuramento * 2.55f);
        // Imposta il colore nero con la trasparenza calcolata
        g.setColor(new java.awt.Color(0, 0, 0, alpha));
        // Disegna il rettangolo sopra l'immagine
        g.fillRect(x, y, width, height);
    }

}
