package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.locazioni.Bosco;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 * @author Stefano Reksten
 */
public class DisegnatoreMappa {

    protected static final int LARGHEZZA_ICONA = ImageCache.mappa.get(TipoLocazione.BOSCO).getWidth();
    protected static final int ALTEZZA_ICONA = ImageCache.mappa.get(TipoLocazione.BOSCO).getHeight();

    // Le caselle conosciute di tutta la foresta, condivise fra minimappa e mappa a tutto
    // schermo: si ricostruisce pigramente solo quando versioneMappaGenerata non è più
    // aggiornata rispetto a Foresta.getVersioneMappa(), invece che a ogni frame.
    private static BufferedImage mappaGenerale;
    private static int versioneMappaGenerata = -1;

    protected static BufferedImage ottieniMappaGenerale() {
        int versioneCorrente = Foresta.getVersioneMappa();
        if (mappaGenerale == null || versioneCorrente != versioneMappaGenerata) {
            mappaGenerale = costruisciMappaGenerale();
            versioneMappaGenerata = versioneCorrente;
        }
        return mappaGenerale;
    }

    /**
     * Costruisce l'immagine delle caselle conosciute di tutta la foresta. La casella del
     * gruppo resta vuota: il segnalino lampeggia, e viene disegnato sopra l'immagine a
     * ogni frame da chi la usa.
     */
    private static BufferedImage costruisciMappaGenerale() {
        BufferedImage immagineMappa = new BufferedImage(Foresta.getDimensioneX() * LARGHEZZA_ICONA,
                Foresta.getDimensioneY() * ALTEZZA_ICONA, BufferedImage.TYPE_INT_ARGB);
        CoordinateMD coordinateGruppo = GruppoGiocatore.getIstanza().getCoordinate();

        Graphics2D graphics = immagineMappa.createGraphics();

        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                int coordinateX = x * LARGHEZZA_ICONA;
                int coordinateY = y * ALTEZZA_ICONA;
                CoordinateMD coordinateCorrenti = new CoordinateMD(x, y);
                if (coordinateCorrenti.equals(coordinateGruppo)) {
                    continue;
                }
                if (Foresta.isLocazioneConosciuta(coordinateCorrenti)) {
                    Image image = recuperaImmaginePerLocazione(coordinateCorrenti);
                    graphics.drawImage(image, coordinateX, coordinateY, null);
                    if (Foresta.isLocazioneVisitata(coordinateCorrenti)) {
                        scurisci(graphics, coordinateX, coordinateY, LARGHEZZA_ICONA, ALTEZZA_ICONA, 50);
                    }
                }
            }
        }
        graphics.dispose();
        return immagineMappa;
    }

    protected boolean isSegnaliniVisibili() {
        return (System.currentTimeMillis() / 1000) % 2 == 0;
    }

    protected static Image recuperaImmaginePerLocazione(CoordinateMD coordinateMD) {
        TipoLocazione tipoLocazione = Foresta.getLocazione(coordinateMD);
        BufferedImage image;
        if (tipoLocazione == TipoLocazione.BOSCO) {
            image = ImageCache.getImmagineMappaBosco(Bosco.getVarianteMappa(Foresta.getLocazioneMD(coordinateMD)));
        } else {
            image = ImageCache.mappa.get(tipoLocazione);
        }
        return image;
    }

    protected static void scurisci(Graphics2D g, int x, int y, int width, int height, int percentualeOscuramento) {
        // Calcola alpha (0 = trasparente, 255 = nero opaco)
        int alpha = (int) (percentualeOscuramento * 2.55f);
        // Imposta il colore nero con la trasparenza calcolata
        g.setColor(new java.awt.Color(0, 0, 0, alpha));
        // Disegna il rettangolo sopra l'immagine
        g.fillRect(x, y, width, height);
    }

}
