package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.interfacce.VistaMappa;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.modellodati.CoordinateMD;
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
    // aggiornata rispetto a VistaMappa.getVersioneMappa(), invece che a ogni frame.
    private static BufferedImage mappaGenerale;
    private static int versioneMappaGenerata = -1;

    protected final VistaPartita vistaPartita;

    protected DisegnatoreMappa(VistaPartita vistaPartita) {
        this.vistaPartita = vistaPartita;
    }

    protected BufferedImage ottieniMappaGenerale() {
        int versioneCorrente = vistaPartita.getMappa().getVersioneMappa();
        if (mappaGenerale == null || versioneCorrente != versioneMappaGenerata) {
            mappaGenerale = costruisciMappaGenerale(vistaPartita.getMappa(), vistaPartita.getGruppoGiocatore().getCoordinate());
            versioneMappaGenerata = versioneCorrente;
        }
        return mappaGenerale;
    }

    /**
     * Costruisce l'immagine delle caselle conosciute di tutta la foresta. La casella del
     * gruppo resta vuota: il segnalino lampeggia, e viene disegnato sopra l'immagine a
     * ogni frame da chi la usa.
     */
    private static BufferedImage costruisciMappaGenerale(VistaMappa mappa, CoordinateMD coordinateGruppo) {
        BufferedImage immagineMappa = new BufferedImage(mappa.getDimensioneX() * LARGHEZZA_ICONA,
                mappa.getDimensioneY() * ALTEZZA_ICONA, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = immagineMappa.createGraphics();

        for (int x = 0; x < mappa.getDimensioneX(); x++) {
            for (int y = 0; y < mappa.getDimensioneY(); y++) {
                int coordinateX = x * LARGHEZZA_ICONA;
                int coordinateY = y * ALTEZZA_ICONA;
                CoordinateMD coordinateCorrenti = new CoordinateMD(x, y);
                if (coordinateCorrenti.equals(coordinateGruppo)) {
                    continue;
                }
                if (mappa.isLocazioneConosciuta(coordinateCorrenti)) {
                    Image image = recuperaImmaginePerLocazione(mappa, coordinateCorrenti);
                    graphics.drawImage(image, coordinateX, coordinateY, null);
                    if (mappa.isLocazioneVisitata(coordinateCorrenti)) {
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

    private static Image recuperaImmaginePerLocazione(VistaMappa mappa, CoordinateMD coordinateMD) {
        TipoLocazione tipoLocazione = mappa.getLocazione(coordinateMD);
        BufferedImage image;
        if (tipoLocazione == TipoLocazione.BOSCO) {
            image = ImageCache.getImmagineMappaBosco(mappa.getVarianteBosco(coordinateMD));
        } else {
            image = ImageCache.mappa.get(tipoLocazione);
        }
        return image;
    }

    private static void scurisci(Graphics2D g, int x, int y, int width, int height, int percentualeOscuramento) {
        // Calcola alpha (0 = trasparente, 255 = nero opaco)
        int alpha = (int) (percentualeOscuramento * 2.55f);
        // Imposta il colore nero con la trasparenza calcolata
        g.setColor(new java.awt.Color(0, 0, 0, alpha));
        // Disegna il rettangolo sopra l'immagine
        g.fillRect(x, y, width, height);
    }

}
