package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasArmaiolo extends DisplayableCanvasScambiatoreArtefatti {

    DisplayableCanvasArmaiolo(int width, int height) {
        super(width, height);
    }

    @Override
    void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, "Inventario gruppo", "Inventario armaiolo");
    }

    void disegnaColonnaPersonaggio(Graphics2D graphics) {

        final int SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI = 20;

        Image doomdark;
        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

        // Nome personaggio
        doomdark = ImageCache.get("Armaiolo", coloreTestata);
        graphics.drawImage(doomdark, (width - doomdark.getWidth(null)) / 2, y, null);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Immagine personaggio
        BufferedImage immaginePersonaggio = ImageCache.armaiolo;

        // Per tenere i personaggi sullo stesso livello (se si passa da un personaggio all'altro)
        // ed evitare sfarfallamenti, scegliamo il ladro come personaggio "base" per calcolare l'altezza a cui disegnare.
        y += ALTEZZA_LADRO;
        graphics.drawImage(immaginePersonaggio, (width - immaginePersonaggio.getWidth()) / 2, y - immaginePersonaggio.getHeight(), null);
    }

    @Override
    protected boolean processaClickPersonaggio(int x, int y, Tasto tasto) {
        return false;
    }

    @Override
    protected boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto) {
        return false;
    }
}
