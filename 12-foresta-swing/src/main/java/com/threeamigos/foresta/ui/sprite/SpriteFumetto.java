package com.threeamigos.foresta.ui.sprite;


import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Il fumetto di un personaggio: un riquadro con il testo e una punta che indica chi parla, che cresce verso l'alto da un punto e svanisce dopo qualche secondo.
 *
 * @author Stefano Reksten
 */
public class SpriteFumetto extends SpriteBase implements FumettoDelloSchermo {

    private static final float SCALA_INIZIALE = 1.0f;
    private static final float SCALA_FINALE = 1.0f;

    private final DescrizioneFumetto dati;
    private final int puntaVersoX;
    private final int puntaVersoY;
    private final BufferedImage nuvola;

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
        this.dati = new DescrizioneFumetto(testo, maxLarghezza, font);
        this.puntaVersoX = puntaVersoX;
        this.puntaVersoY = puntaVersoY;
        this.nuvola = costruisciNuvola(dati, colore);
        // Il fumetto cresce verso l'alto partendo da y: non deve mai sforare oltre il margine superiore
        float yClampata = Math.max(yIniziale, dati.getAltezza() + ImageCache.SPACING);
        inizializza(buildImage(),
                DescrizioneFumetto.DURATA_IN_SECONDI, DescrizioneFumetto.ATTACCO_DISSOLVENZA_DOPO_SECONDI, Dissolvenza.IPERBOLICA_CON_SOGLIA,
                null, 0,
                xIniziale, yClampata,
                xIniziale, yClampata,
                SCALA_INIZIALE, SCALA_FINALE);
    }

    @Override
    protected BufferedImage buildImage() {
        return nuvola;
    }

    /**
     * La nuvola del fumetto, senza la punta: un rettangolo bianco smussato con il testo
     * spezzato su più righe entro la larghezza massima.
     */
    public static BufferedImage costruisciNuvola(String testo, int larghezza, DoomdarkFont font, DoomdarkColorModel.Color colore) {
        return costruisciNuvola(new DescrizioneFumetto(testo, larghezza, font), colore);
    }

    public static BufferedImage costruisciNuvola(DescrizioneFumetto dati, DoomdarkColorModel.Color colore) {
        final int smussamento = DescrizioneFumetto.SMUSSAMENTO_BORDO;
        BufferedImage immagineRisultante = new BufferedImage(dati.getLarghezza(), dati.getAltezza(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = immagineRisultante.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(0, 0, dati.getLarghezza(), dati.getAltezza(), smussamento, smussamento);
        for (DescrizioneFumetto.Riga riga : dati.getRighe()) {
            graphics.drawImage(ImageCache.get(riga.getTesto(), dati.getFont(), colore), riga.getX(), riga.getY(), null);
        }
        graphics.dispose();
        return immagineRisultante;
    }

    @Override
    public void disegna(Graphics2D g, float xf, float yf, float scala) {
        final int x = Math.round(xf);
        final int y = Math.round(yf);
        g.drawImage(immagine, x, y - immagine.getHeight(null), null);

        // La punta parte dai bordi della nuvola com'è, che è larga quanto il suo testo più largo e non quanto il massimo
        disegnaPunta(g, x, y, immagine.getWidth(null), dati.getAltezza(), puntaVersoX, puntaVersoY);
    }

    /**
     * La punta del fumetto, dal bordo della nuvola verso il punto indicato. (x, y) è
     * l'angolo IN BASSO a sinistra della nuvola, come in CoordinateFumetto; se il punto
     * cade dentro la nuvola la punta non viene disegnata.
     */
    public static void disegnaPunta(Graphics2D g, int x, int y, int larghezza, int altezza, int puntaVersoX, int puntaVersoY) {
        int[] punta = DescrizioneFumetto.punta(x, y, larghezza, altezza, puntaVersoX, puntaVersoY);
        if (punta != null) {
            g.setColor(Color.WHITE);
            g.fillPolygon(new int[] {punta[0], punta[2], punta[4]}, new int[] {punta[1], punta[3], punta[5]}, 3);
        }
    }

    public String getTesto() {
        return dati.getTesto();
    }

    /**
     * Fa ripartire da zero il tempo trascorso, per esempio quando un fumetto resta in vita perché ne arriva uno uguale.
     */
    public void resettaImpulsi() {
        resettaTempoTrascorso();
    }
}
