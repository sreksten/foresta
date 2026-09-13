package com.threeamigos.foresta.ui;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefano Reksten
 */
public class SpriteFumetto implements SpriteInterface {

    private static final int MAX_TICKS = 32;
    private static final int TICK_LIMIT_BEFORE_FADING = MAX_TICKS >> 1;
    private static final int ROUND_BORDER_SIZE = 8;

    boolean active;
    private final Image image;
    private final String testo;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int pointToX;
    private final int pointToY;
    private int ticks;

    SpriteFumetto(String testo, int maxLarghezza, int x, int y, DoomdarkFont font, DoomdarkColorModel.Color color, int pointToX, int pointToY) {

        // Costruzione del rettangolo
        final int larghezzaInterna = maxLarghezza - 2 * ROUND_BORDER_SIZE;
        List<String> parti = FontTool.split(font, testo, larghezzaInterna);
        final int interlinea = 4;

        List<Image> testiDisegnati = new ArrayList<>();
        int maxLarghezzaEffettiva = 0;
        for (String s : parti) {
            Image testoDisegnato = DoomdarkTextProducer.getImage(s, font, color);
            maxLarghezzaEffettiva = Math.max(maxLarghezzaEffettiva, testoDisegnato.getWidth(null));
            testiDisegnati.add(testoDisegnato);
        }
        maxLarghezzaEffettiva += 2 * ROUND_BORDER_SIZE;

        // Spaziature, testo, interlinee
        height = 2 * ROUND_BORDER_SIZE + parti.size() * font.getHeight() + interlinea * (parti.size() - 1);
        BufferedImage resultingImage = new BufferedImage(maxLarghezzaEffettiva, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resultingImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(0, 0, maxLarghezzaEffettiva - 1, height - 1, ROUND_BORDER_SIZE, ROUND_BORDER_SIZE);
        int testoY = ROUND_BORDER_SIZE;
        for (Image testoDisegnato : testiDisegnati) {
            graphics.drawImage(testoDisegnato, ROUND_BORDER_SIZE, testoY, null);
            testoY += font.getHeight() + interlinea;
        }
        graphics.dispose();

        this.image = resultingImage;
        this.testo = testo;
        this.x = x;
        // Il fumetto cresce verso l'alto partendo da y: non deve mai sforare oltre il margine superiore
        this.y = Math.max(y, height + ImageCache.SPACING);
        this.width = maxLarghezza;
        this.pointToX = pointToX;
        this.pointToY = pointToY;
        ticks = 0;
        active = true;
    }

    SpriteFumetto(Image image, int x, int y, int pointToX, int pointToY) {
        width = image.getWidth(null) + 2 * ROUND_BORDER_SIZE;
        height = image.getHeight(null) + 2 * ROUND_BORDER_SIZE;
        BufferedImage resultingImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resultingImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(0, 0, width - 1, height - 1, ROUND_BORDER_SIZE, ROUND_BORDER_SIZE);
        graphics.drawImage(image, ROUND_BORDER_SIZE, ROUND_BORDER_SIZE, null);
        graphics.dispose();

        this.image = resultingImage;
        this.testo = null;
        this.x = x;
        // Il fumetto cresce verso l'alto partendo da y: non deve mai sforare oltre il margine superiore
        this.y = Math.max(y, height + ImageCache.SPACING);
        this.pointToX = pointToX;
        this.pointToY = pointToY;
        ticks = 0;
        active = true;
    }

    public void animate(Graphics2D g) {
        if (active) {
            if (ticks > TICK_LIMIT_BEFORE_FADING) {
                float transparency = 1.0f / (float)(ticks - TICK_LIMIT_BEFORE_FADING);
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
                g.setComposite(ac);
            } else {
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
                g.setComposite(ac);
            }
            g.drawImage(image, x, y - image.getHeight(null), null);

            final int boxLeft = x;
            final int boxRight = x + width;
            final int boxTop = y - height;
            final int boxBottom = y;

            final boolean pointLeft = pointToX < boxLeft;
            final boolean pointRight = pointToX > boxRight;
            final boolean pointAbove = pointToY < boxTop;
            final boolean pointBelow = pointToY > boxBottom;

            int triangleX1 = 0;
            int triangleY1 = 0;
            int triangleX2 = 0;
            int triangleY2 = 0;
            boolean validPosition = true;

            if (pointLeft && pointBelow) {
                // Il fumetto appare in alto a destra rispetto al punto interessato:
                // il triangolino parte dallo smusso dell'angolo in basso a sinistra
                triangleX1 = boxLeft;
                triangleY1 = boxBottom - ROUND_BORDER_SIZE;
                triangleX2 = boxLeft + ROUND_BORDER_SIZE;
                triangleY2 = boxBottom;
            } else if (pointRight && pointBelow) {
                // Il fumetto appare in alto a sinistra rispetto al punto interessato:
                // il triangolino parte dallo smusso dell'angolo in basso a destra
                triangleX1 = boxRight - ROUND_BORDER_SIZE;
                triangleY1 = boxBottom;
                triangleX2 = boxRight;
                triangleY2 = boxBottom - ROUND_BORDER_SIZE;
            } else if (pointLeft && pointAbove) {
                // Il fumetto appare in basso a destra rispetto al punto interessato:
                // il triangolino parte dallo smusso dell'angolo in alto a sinistra
                triangleX1 = boxLeft;
                triangleY1 = boxTop + ROUND_BORDER_SIZE;
                triangleX2 = boxLeft + ROUND_BORDER_SIZE;
                triangleY2 = boxTop;
            } else if (pointRight && pointAbove) {
                // Il fumetto appare in basso a sinistra rispetto al punto interessato:
                // il triangolino parte dallo smusso dell'angolo in alto a destra
                triangleX1 = boxRight - ROUND_BORDER_SIZE;
                triangleY1 = boxTop;
                triangleX2 = boxRight;
                triangleY2 = boxTop + ROUND_BORDER_SIZE;
            } else if (pointAbove) {
                // Il punto è sopra il fumetto, allineato orizzontalmente: il triangolino
                // parte dal centro del lato superiore
                int centroX = boxLeft + width / 2;
                triangleX1 = centroX - ROUND_BORDER_SIZE;
                triangleY1 = boxTop;
                triangleX2 = centroX + ROUND_BORDER_SIZE;
                triangleY2 = boxTop;
            } else if (pointBelow) {
                // Il punto è sotto il fumetto, allineato orizzontalmente: il triangolino
                // parte dal centro del lato inferiore
                int centroX = boxLeft + width / 2;
                triangleX1 = centroX - ROUND_BORDER_SIZE;
                triangleY1 = boxBottom;
                triangleX2 = centroX + ROUND_BORDER_SIZE;
                triangleY2 = boxBottom;
            } else if (pointLeft) {
                // Il punto è a sinistra del fumetto, allineato verticalmente: il triangolino
                // parte dal centro del lato sinistro
                int centroY = boxTop + height / 2;
                triangleX1 = boxLeft;
                triangleY1 = centroY - ROUND_BORDER_SIZE;
                triangleX2 = boxLeft;
                triangleY2 = centroY + ROUND_BORDER_SIZE;
            } else if (pointRight) {
                // Il punto è a destra del fumetto, allineato verticalmente: il triangolino
                // parte dal centro del lato destro
                int centroY = boxTop + height / 2;
                triangleX1 = boxRight;
                triangleY1 = centroY - ROUND_BORDER_SIZE;
                triangleX2 = boxRight;
                triangleY2 = centroY + ROUND_BORDER_SIZE;
            } else {
                // Il punto è coperto dal fumetto: nessun triangolino
                validPosition = false;
            }

            if (validPosition) {
                g.setColor(Color.WHITE);
                g.fillPolygon(new int[] {triangleX1, triangleX2, pointToX}, new int[] {triangleY1, triangleY2, pointToY}, 3);
            }

            ticks++;
            if (ticks >= MAX_TICKS) {
                active = false;
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    String getTesto() {
        return testo;
    }

    void resetTicks() {
        ticks = 0;
    }
}
