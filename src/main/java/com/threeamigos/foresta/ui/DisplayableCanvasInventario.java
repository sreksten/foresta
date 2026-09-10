package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasInventario implements Finestra {

    private static final int DIMENSIONE_BORDO_INTERNO = 16;
    private static final int corniceInventarioWidth = ImageCache.corniceInventario.getWidth(null);

    private final int width;
    private final int height;
    private final DoomdarkFont font = DoomdarkFontMedium.getInstance();
    private final int fontHeight = font.getHeight();

    private final int leftBoxX;
    private final int leftBoxLimit;
    private final int rightBoxX;
    private final int boxY;

    private final int centerBoxX;
    private final int centerBoxLimit;

    private AutomaInventario automa;

    DisplayableCanvasInventario(int width, int height) {
        this.width = width;
        this.height = height;
        leftBoxX = SPACING + DIMENSIONE_BORDO_INTERNO;
        leftBoxLimit = SPACING + corniceInventarioWidth;
        rightBoxX = width - SPACING - corniceInventarioWidth + DIMENSIONE_BORDO_INTERNO;
        boxY = SPACING + DIMENSIONE_BORDO_INTERNO;

        centerBoxX = SPACING + corniceInventarioWidth + SPACING;
        centerBoxLimit = width - SPACING - corniceInventarioWidth - SPACING;

    }

    void impostaAutoma(AutomaInventario automa) {
        this.automa = automa;
    }

    void disegnaInventario(Graphics2D graphics) {

        graphics.drawImage(ImageCache.corniceInventario, SPACING, SPACING, null);
        graphics.drawImage(ImageCache.corniceInventario, width - SPACING - corniceInventarioWidth, SPACING, null);

        if (automa == null) {
            return;
        }

        Personaggio p = automa.getPersonaggio();

        final int SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI = 20;

        Image doomdark;
        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

        doomdark = ImageCache.get(p.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), coloreTestata);
        graphics.drawImage(doomdark, (width - doomdark.getWidth(null)) / 2, y, null);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        BufferedImage immaginePersonaggio = ClassePersonaggioImmagine.getImmagine(p.getClasse());
        graphics.drawImage(immaginePersonaggio, (width - immaginePersonaggio.getWidth(null)) / 2, y, null);
        y += immaginePersonaggio.getHeight() + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        disegna(TipoAttributo.LIVELLO, p.getLivello(), graphics, y, coloreTestata);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.PUNTI_ESPERIENZA, p.getPuntiEsperienza(), graphics, y, coloreTestata);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.PUNTI_ABILITA, p.getPuntiAbilitaDisponibili(), graphics, y, coloreTestata);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        DoomdarkColorAlternante colore = new DoomdarkColorAlternante();
        disegna(TipoAttributo.FORZA, p.getForza(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.DESTREZZA, p.getDestrezza(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.COSTITUZIONE, p.getCostituzione(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.INTELLIGENZA, p.getIntelligenza(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.SAGGEZZA, p.getSaggezza(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.CARISMA, p.getCarisma(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.FORTUNA, p.getCostituzione(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.NUMERO_BERSAGLI, p.getBersagli(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.RIGENERAZIONE_SALUTE, p.getRigenerazioneSalute(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.RIGENERAZIONE_MAGIA, p.getRigenerazioneMagia(), graphics, y, colore);
        y += fontHeight + SPACING * 2;

        disegna(TipoAttributo.CARICO_MASSIMO, p.getCaricoMassimo(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.CRITICO, p.getCaricoMassimo(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.PRECISIONE, p.getCaricoMassimo(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.VELOCITA, p.getVelocita(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.FURTIVITA, p.getFurtivita(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.PARATA, p.getParata(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.RESISTENZA_MAGICA, p.getResistenzaMagica(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.PERCEZIONE, p.getPercezione(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.SOGGEZIONE, p.getSoggezione(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.FURIA, p.getFuria(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.CORAGGIO, p.getCoraggio(), graphics, y, colore);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.VALORE, p.getValore(), graphics, y, colore);
        y += fontHeight + SPACING;

        disegnaElenco(graphics, new ArrayList<>(p.getInventario()), leftBoxX);
        disegnaElenco(graphics, automa.getArtefattiDisponibili(), rightBoxX);
    }

    private void disegna(TipoAttributo attributo, int valore, Graphics2D graphics, int y, DoomdarkColorAlternante colore) {
        disegna(attributo, valore, graphics, y, colore.getColor());
    }

    private void disegna(TipoAttributo attributo, int valore, Graphics2D graphics, int y, DoomdarkColorModel.Color colore) {
        Image i = ImageCache.get(attributo.getNome(), colore);
        graphics.drawImage(i, centerBoxX, y, null);
        i = ImageCache.get(valore, colore);
        graphics.drawImage(i, centerBoxLimit - i.getWidth(null), y, null);
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
