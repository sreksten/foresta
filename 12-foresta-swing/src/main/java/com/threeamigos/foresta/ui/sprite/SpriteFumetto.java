package com.threeamigos.foresta.ui.sprite;


import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Il fumetto di un personaggio: un riquadro con il testo e una punta che indica chi parla, che cresce verso l'alto da un punto e svanisce dopo qualche secondo.
 *
 * @author Stefano Reksten
 */
public class SpriteFumetto extends SpriteBase {

    private static final float DURATA_IN_SECONDI = 3.2f;
    private static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = DURATA_IN_SECONDI / 2;
    private static final float SCALA_INIZIALE = 1.0f;
    private static final float SCALA_FINALE = 1.0f;

    private static final int DIMENSIONE_SMUSSAMENTO_BORDO = 8;

    private final String testo;
    // La larghezza massima: la nuvola può essere più stretta, la sua larghezza vera è quella dell'immagine
    private final int larghezzaMassima;
    private int altezza;
    private final int puntaVersoX;
    private final int puntaVersoY;

    // Campi usati solo da buildImage()
    private final DoomdarkFont font;
    private final DoomdarkColorModel.Color colore;

    /**
     * Un fumetto con quel testo, che arriva a quella posizione.
     *
     * @param testo il testo del fumetto
     * @param maxLarghezza la larghezza massima oltre la quale il testo va a capo
     * @param xIniziale la coordinata X del fumetto
     * @param yIniziale la coordinata Y del fumetto; può essere riaggiustata se l'altezza del fumetto risulterebbe troppo alta
     *                  rispetto al margine superiore dello schermo + altezza del fumetto
     * @param font il carattere del testo
     * @param colore il colore del testo
     * @param puntaVersoX coordinata X verso cui la punta del fumetto deve andare
     * @param puntaVersoY coordinata Y verso cui la punta del fumetto deve andare
     */
    public SpriteFumetto(String testo, int maxLarghezza, int xIniziale, int yIniziale, DoomdarkFont font, DoomdarkColorModel.Color colore,
                  int puntaVersoX, int puntaVersoY) {
        this.testo = testo;
        this.larghezzaMassima = maxLarghezza;
        this.font = font;
        this.colore = colore;
        this.puntaVersoX = puntaVersoX;
        this.puntaVersoY = puntaVersoY;
        BufferedImage immagineCostruita = buildImage(); // imposta this.altezza
        // Il fumetto cresce verso l'alto partendo da y: non deve mai sforare oltre il margine superiore
        float yClampata = Math.max(yIniziale, altezza + ImageCache.SPACING);
        inizializza(immagineCostruita,
                DURATA_IN_SECONDI, ATTACCO_DISSOLVENZA_DOPO_SECONDI,
                null, 0,
                xIniziale, yClampata,
                xIniziale, yClampata,
                SCALA_INIZIALE, SCALA_FINALE);
    }

    @Override
    protected BufferedImage buildImage() {
        BufferedImage nuvola = costruisciNuvola(testo, larghezzaMassima, font, colore);
        this.altezza = nuvola.getHeight();
        return nuvola;
    }

    /**
     * La nuvola del fumetto, senza la punta: un rettangolo bianco smussato con il testo
     * spezzato su più righe entro la larghezza massima.
     */
    public static BufferedImage costruisciNuvola(String testo, int larghezza, DoomdarkFont font, DoomdarkColorModel.Color colore) {
        // Costruzione del rettangolo
        final int larghezzaInterna = larghezza - 2 * DIMENSIONE_SMUSSAMENTO_BORDO;
        List<String> parti = FontTool.split(font, testo, larghezzaInterna);
        final int interlinea = 4;

        List<Image> testiDisegnati = new ArrayList<>();
        int maxLarghezzaEffettiva = 0;
        for (String s : parti) {
            Image testoDisegnato = ImageCache.get(s, font, colore);
            maxLarghezzaEffettiva = Math.max(maxLarghezzaEffettiva, testoDisegnato.getWidth(null));
            testiDisegnati.add(testoDisegnato);
        }
        maxLarghezzaEffettiva += 2 * DIMENSIONE_SMUSSAMENTO_BORDO;

        // Spaziature, testo, interlinee
        int altezzaCostruita = 2 * DIMENSIONE_SMUSSAMENTO_BORDO + parti.size() * font.getHeight() + interlinea * (parti.size() - 1);
        BufferedImage immagineRisultante = new BufferedImage(maxLarghezzaEffettiva, altezzaCostruita, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = immagineRisultante.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(0, 0, maxLarghezzaEffettiva, altezzaCostruita, DIMENSIONE_SMUSSAMENTO_BORDO, DIMENSIONE_SMUSSAMENTO_BORDO);
        int testoY = DIMENSIONE_SMUSSAMENTO_BORDO;
        for (Image testoDisegnato : testiDisegnati) {
            graphics.drawImage(testoDisegnato, DIMENSIONE_SMUSSAMENTO_BORDO, testoY, null);
            testoY += font.getHeight() + interlinea;
        }
        graphics.dispose();

        return immagineRisultante;
    }

    @Override
    protected float calcolaAlpha(float secondiTrascorsi) {
        return dissolvenzaIperbolicaConSoglia(secondiTrascorsi, momentoInizioFade);
    }

    @Override
    public void disegna(Graphics2D g, float xf, float yf, float scala) {
        final int x = Math.round(xf);
        final int y = Math.round(yf);
        g.drawImage(immagine, x, y - immagine.getHeight(null), null);

        // La punta parte dai bordi della nuvola com'è, che è larga quanto il suo testo più largo e non quanto il massimo
        disegnaPunta(g, x, y, immagine.getWidth(null), altezza, puntaVersoX, puntaVersoY);
    }

    /**
     * La punta del fumetto, dal bordo della nuvola verso il punto indicato. (x, y) è
     * l'angolo IN BASSO a sinistra della nuvola, come in CoordinateFumetto; se il punto
     * cade dentro la nuvola la punta non viene disegnata.
     */
    public static void disegnaPunta(Graphics2D g, int x, int y, int larghezza, int altezza, int puntaVersoX, int puntaVersoY) {
        final int limiteSinistro = x;
        final int limiteDestro = x + larghezza;
        final int limiteSuperiore = y - altezza;
        final int limiteInferiore = y;

        final boolean puntaVersoSinistra = puntaVersoX < limiteSinistro;
        final boolean puntaVersoDestra = puntaVersoX > limiteDestro;
        final boolean puntaSopra = puntaVersoY < limiteSuperiore;
        final boolean puntaSotto = puntaVersoY > limiteInferiore;

        int triangoloX1 = 0;
        int triangoloY1 = 0;
        int triangoloX2 = 0;
        int triangoloY2 = 0;
        boolean posizioneValida = true;

        if (puntaVersoSinistra && puntaSotto) {
            // Il fumetto appare in alto a destra rispetto al punto interessato:
            // il triangolino parte dallo smusso dell'angolo in basso a sinistra
            triangoloX1 = limiteSinistro;
            triangoloY1 = limiteInferiore - DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloX2 = limiteSinistro + DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloY2 = limiteInferiore;
        } else if (puntaVersoDestra && puntaSotto) {
            // Il fumetto appare in alto a sinistra rispetto al punto interessato:
            // il triangolino parte dallo smusso dell'angolo in basso a destra
            triangoloX1 = limiteDestro - DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloY1 = limiteInferiore;
            triangoloX2 = limiteDestro;
            triangoloY2 = limiteInferiore - DIMENSIONE_SMUSSAMENTO_BORDO;
        } else if (puntaVersoSinistra && puntaSopra) {
            // Il fumetto appare in basso a destra rispetto al punto interessato:
            // il triangolino parte dallo smusso dell'angolo in alto a sinistra
            triangoloX1 = limiteSinistro;
            triangoloY1 = limiteSuperiore + DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloX2 = limiteSinistro + DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloY2 = limiteSuperiore;
        } else if (puntaVersoDestra && puntaSopra) {
            // Il fumetto appare in basso a sinistra rispetto al punto interessato:
            // il triangolino parte dallo smusso dell'angolo in alto a destra
            triangoloX1 = limiteDestro - DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloY1 = limiteSuperiore;
            triangoloX2 = limiteDestro;
            triangoloY2 = limiteSuperiore + DIMENSIONE_SMUSSAMENTO_BORDO;
        } else if (puntaSopra) {
            // Il punto è sopra il fumetto, allineato orizzontalmente: il triangolino
            // parte dal centro del lato superiore
            int centroX = limiteSinistro + larghezza / 2;
            triangoloX1 = centroX - DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloY1 = limiteSuperiore;
            triangoloX2 = centroX + DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloY2 = limiteSuperiore;
        } else if (puntaSotto) {
            // Il punto è sotto il fumetto, allineato orizzontalmente: il triangolino
            // parte dal centro del lato inferiore
            int centroX = limiteSinistro + larghezza / 2;
            triangoloX1 = centroX - DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloY1 = limiteInferiore;
            triangoloX2 = centroX + DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloY2 = limiteInferiore;
        } else if (puntaVersoSinistra) {
            // Il punto è a sinistra del fumetto, allineato verticalmente: il triangolino
            // parte dal centro del lato sinistro
            int centroY = limiteSuperiore + altezza / 2;
            triangoloX1 = limiteSinistro;
            triangoloY1 = centroY - DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloX2 = limiteSinistro;
            triangoloY2 = centroY + DIMENSIONE_SMUSSAMENTO_BORDO;
        } else if (puntaVersoDestra) {
            // Il punto è a destra del fumetto, allineato verticalmente: il triangolino
            // parte dal centro del lato destro
            int centroY = limiteSuperiore + altezza / 2;
            triangoloX1 = limiteDestro;
            triangoloY1 = centroY - DIMENSIONE_SMUSSAMENTO_BORDO;
            triangoloX2 = limiteDestro;
            triangoloY2 = centroY + DIMENSIONE_SMUSSAMENTO_BORDO;
        } else {
            // Il punto è coperto dal fumetto: nessun triangolino
            posizioneValida = false;
        }

        if (posizioneValida) {
            g.setColor(Color.WHITE);
            g.fillPolygon(new int[] {triangoloX1, triangoloX2, puntaVersoX}, new int[] {triangoloY1, triangoloY2, puntaVersoY}, 3);
        }
    }

    public String getTesto() {
        return testo;
    }

    /**
     * Fa ripartire da zero il tempo trascorso, per esempio quando un fumetto resta in vita perché ne arriva uno uguale.
     */
    public void resettaImpulsi() {
        resettaTempoTrascorso();
    }
}
