package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.oggetti.Artefatto;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasInventario implements Finestra {

    private static final int DIMENSIONE_BORDO_INTERNO = 16;

    private final int width;
    private final int height;
    private final DoomdarkFont font = DoomdarkFontMedium.getInstance();

    private final int leftBoxX;
    private final int leftBoxLimit;
    private final int rightBoxX;
    private final int boxY;

    private AutomaInventario automa;

    DisplayableCanvasInventario(int width, int height) {
        this.width = width;
        this.height = height;
        leftBoxX = ImageCache.SPACING + DIMENSIONE_BORDO_INTERNO;
        leftBoxLimit = ImageCache.SPACING + ImageCache.corniceInventario.getWidth(null);
        rightBoxX = width - ImageCache.SPACING - ImageCache.corniceInventario.getWidth(null) + DIMENSIONE_BORDO_INTERNO;
        boxY = ImageCache.SPACING + DIMENSIONE_BORDO_INTERNO;
    }

    void impostaAutoma(AutomaInventario automa) {
        this.automa = automa;
    }

    void disegnaInventario(Graphics2D graphics) {

        graphics.drawImage(ImageCache.corniceInventario, ImageCache.SPACING, ImageCache.SPACING, null);
        graphics.drawImage(ImageCache.corniceInventario, width - ImageCache.SPACING - ImageCache.corniceInventario.getWidth(null), ImageCache.SPACING, null);

        if (automa == null) {
            return;
        }

        disegnaElenco(graphics, new ArrayList<>(automa.getPersonaggio().getInventario()), leftBoxX);
        disegnaElenco(graphics, automa.getArtefattiDisponibili(), rightBoxX);
    }

    private void disegnaElenco(Graphics2D graphics, List<Artefatto> artefatti, int x) {
        int locY = boxY;
        for (Artefatto artefatto : artefatti) {
            Image doomdark = DoomdarkTextProducer.getImage(artefatto.getNome(), font, DoomdarkColorModel.Color.LIGHT_GRAY);
            graphics.drawImage(doomdark, x, locY, null);
            locY += font.getHeight();
        }
    }

    @Override
    public void processaDoppioClick(int x, int y, Tasto tasto) {
        if (automa == null) {
            return;
        }
        int riga = (y - DIMENSIONE_BORDO_INTERNO) / font.getHeight();
        if (riga < 0) {
            return;
        }
        if (x < leftBoxLimit) {
            List<Artefatto> inventarioPersonaggio = new ArrayList<>(automa.getPersonaggio().getInventario());
            if (riga < inventarioPersonaggio.size()) {
                automa.spostaNelPool(inventarioPersonaggio.get(riga));
            }
        } else {
            List<Artefatto> disponibili = automa.getArtefattiDisponibili();
            if (riga < disponibili.size()) {
                automa.spostaNelPersonaggio(disponibili.get(riga));
            }
        }
    }

}
