package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Incantamento;
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
    // Pixel di scorrimento per ogni scatto della rotella
    private static final int PASSO_SCORRIMENTO = 2;

    private final int width;
    private final int height;
    private final DoomdarkFont font = DoomdarkFontMedium.getInstance();
    private final int fontHeight = font.getHeight();
    private final DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();

    private final int leftBoxX;
    private final int leftBoxLimit;
    private final int centerBoxX;
    private final int centerBoxLimit;
    private final int rightBoxX;
    private final int rightBoxLimit;
    private final int boxY;

    private AutomaInventario automa;
    int offsetYLeftBox = 0;
    int offsetYBoxPersonaggio = 0;
    int offsetYRightBox = 0;

    DisplayableCanvasInventario(int width, int height) {
        this.width = width;
        this.height = height;

        leftBoxX = SPACING + DIMENSIONE_BORDO_INTERNO;
        leftBoxLimit = leftBoxX + corniceInventarioWidth - DIMENSIONE_BORDO_INTERNO;

        centerBoxX = SPACING + corniceInventarioWidth + SPACING;
        centerBoxLimit = width - SPACING - corniceInventarioWidth - SPACING;

        rightBoxX = width - SPACING - corniceInventarioWidth + DIMENSIONE_BORDO_INTERNO;
        rightBoxLimit = width - SPACING - DIMENSIONE_BORDO_INTERNO;

        boxY = SPACING + DIMENSIONE_BORDO_INTERNO;
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

        // Nome personaggio
        doomdark = ImageCache.get(p.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), coloreTestata);
        graphics.drawImage(doomdark, (width - doomdark.getWidth(null)) / 2, y, null);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Immagine personaggio
        BufferedImage immaginePersonaggio = ClassePersonaggioImmagine.getImmagine(p.getClasse());
        graphics.drawImage(immaginePersonaggio, (width - immaginePersonaggio.getWidth()) / 2, y, null);
        y += immaginePersonaggio.getHeight() + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Livello, XP, punti disponibili
        disegna(TipoAttributo.LIVELLO, p.getLivello(), graphics, y, coloreTestata);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.PUNTI_ESPERIENZA, p.getPuntiEsperienza(), graphics, y, coloreTestata);
        y += fontHeight + SPACING;
        disegna(TipoAttributo.PUNTI_ABILITA, p.getPuntiAbilitaDisponibili(), graphics, y, coloreTestata);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        BufferedImage separatore = ImageCache.separatore;
        graphics.drawImage(separatore, (width - separatore.getWidth()) / 2, y, null);
        y += separatore.getHeight() + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Attributi personaggio
        ComponenteScorrevole<TipoAttributo> componenteScorrevole = new ComponenteScorrevole<>(
                width - 2 * corniceInventarioWidth - 4 * SPACING, 10, 2);
        //FIXME offsetY = componenteScorrevole.limitaOffset(height - y, offsetY);

        DoomdarkColorAlternante coloreAlternante = new DoomdarkColorAlternante();
        DoomdarkColorModel.Color colore;

        colore = coloreAlternante.getColor();

        creaNodo(componenteScorrevole, colore, TipoAttributo.FORZA, p.getForza());
        creaNodo(componenteScorrevole, colore, TipoAttributo.DESTREZZA, p.getDestrezza());
        creaNodo(componenteScorrevole, colore, TipoAttributo.COSTITUZIONE, p.getCostituzione());
        creaNodo(componenteScorrevole, colore, TipoAttributo.INTELLIGENZA, p.getIntelligenza());
        creaNodo(componenteScorrevole, colore, TipoAttributo.SAGGEZZA, p.getSaggezza());
        creaNodo(componenteScorrevole, colore, TipoAttributo.CARISMA, p.getCarisma());
        creaNodo(componenteScorrevole, colore, TipoAttributo.FORTUNA, p.getFortuna());
        creaNodo(componenteScorrevole, colore, TipoAttributo.NUMERO_BERSAGLI, p.getBersagli());
        creaNodo(componenteScorrevole, colore, TipoAttributo.RIGENERAZIONE_SALUTE, p.getRigenerazioneSalute());
        creaNodo(componenteScorrevole, colore, TipoAttributo.RIGENERAZIONE_MAGIA, p.getRigenerazioneMagia());

        creaNodo(componenteScorrevole, colore, TipoAttributo.CARICO_MASSIMO, p.getCaricoMassimo());
        creaNodo(componenteScorrevole, colore, TipoAttributo.CRITICO, p.getCritico());
        creaNodo(componenteScorrevole, colore, TipoAttributo.PRECISIONE, p.getPrecisione());
        creaNodo(componenteScorrevole, colore, TipoAttributo.VELOCITA, p.getVelocita());
        creaNodo(componenteScorrevole, colore, TipoAttributo.FURTIVITA, p.getFurtivita());
        creaNodo(componenteScorrevole, colore, TipoAttributo.PARATA, p.getParata());
        creaNodo(componenteScorrevole, colore, TipoAttributo.RESISTENZA_MAGICA, p.getResistenzaMagica());
        creaNodo(componenteScorrevole, colore, TipoAttributo.PERCEZIONE, p.getPercezione());
        creaNodo(componenteScorrevole, colore, TipoAttributo.SOGGEZIONE, p.getSoggezione());
        creaNodo(componenteScorrevole, colore, TipoAttributo.FURIA, p.getFuria());
        creaNodo(componenteScorrevole, colore, TipoAttributo.CORAGGIO, p.getCoraggio());
        creaNodo(componenteScorrevole, colore, TipoAttributo.VALORE, p.getValore());

        offsetYBoxPersonaggio = componenteScorrevole.limitaOffset(height, offsetYBoxPersonaggio);
        Image image = componenteScorrevole.produci(height - y, offsetYBoxPersonaggio);
        graphics.drawImage(image, corniceInventarioWidth + 2 * SPACING, y, null);

        final int width = corniceInventarioWidth - 2 * (SPACING + DIMENSIONE_BORDO_INTERNO);
        // Inventario personaggio
        offsetYLeftBox = disegnaElenco(graphics, new ArrayList<>(p.getInventario()), leftBoxX, width, offsetYLeftBox);
        // Inventario gruppo
        offsetYRightBox = disegnaElenco(graphics, automa.getArtefattiDisponibili(), rightBoxX, width, offsetYRightBox);
    }

    private void creaNodo(ComponenteScorrevole<TipoAttributo> componenteScorrevole, DoomdarkColorModel.Color colore,
                          TipoAttributo tipoAttributo, double valoreAttributo) {
        componenteScorrevole.creaNodo(
                tipoAttributo.getNome(), font, colore,
                String.valueOf((int)valoreAttributo), font, colore,
                tipoAttributo.getDescrizione(), fontSmall, colore,
                null, tipoAttributo);
    }

    private void disegna(TipoAttributo attributo, int valore, Graphics2D graphics, int y, DoomdarkColorModel.Color colore) {
        Image i = ImageCache.get(attributo.getNome(), colore);
        graphics.drawImage(i, centerBoxX, y, null);
        i = ImageCache.get(valore, colore);
        graphics.drawImage(i, centerBoxLimit - i.getWidth(null), y, null);
    }

    private int disegnaElenco(Graphics2D graphics, List<Artefatto> artefatti, int x, int width, int offset) {

        ComponenteScorrevole<Artefatto> componenteScorrevole = new ComponenteScorrevole<>(width, 10, 2);

        for (Artefatto artefatto : artefatti) {
            String nome = artefatto.getNome();
            nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);
            ComponenteScorrevole<Artefatto>.Nodo nodo = componenteScorrevole.creaNodo(
                    nome, font, DoomdarkColorModel.Color.LIGHT_GRAY,
                    artefatto.getTipo().getDescrizione(), fontSmall, DoomdarkColorModel.Color.LIGHT_GRAY,
                    null, artefatto);
            for (ModificatoreAttributo modificatore : artefatto.getModificatori()) {
                String valore;
                switch (modificatore.getTipoModificatoreAttributo()) {
                    case AUMENTO_FISSO:
                        valore = (modificatore.getQuantita() < 0 ? "-" : "+") + (int)modificatore.getQuantita();
                        break;
                    case AUMENTO_PERCENTUALE:
                        valore = (modificatore.getQuantita() < 0 ? "-" : "+") + (int)modificatore.getQuantita() + "%";
                        break;
                    case QUANTITA_ASSOLUTA:
                        valore = "Porta a " + (int)modificatore.getQuantita();
                        break;
                    default:
                        valore = "";
                        break;
                }
                nodo.creaNodo(
                        modificatore.getTipoAttributo().getNome(), font, DoomdarkColorModel.Color.LIGHT_GRAY,
                        valore, font, DoomdarkColorModel.Color.LIGHT_GRAY,
                        null, null, null,
                        null, artefatto);

            }
            for (Incantamento incantamento : artefatto.getIncantamenti()) {
                nodo.creaNodo(
                        incantamento.getNomeIncantamento(), font, DoomdarkColorModel.Color.LIGHT_GRAY,
                        null, null, null,
                        null, artefatto);
                nodo.creaNodo(
                        incantamento.getTipoDannoElementale().getNome(), font, DoomdarkColorModel.Color.LIGHT_GRAY,
                        incantamento.getDannoBonusFisso() + " + " + (int)(incantamento.getCoefficienteScala() * 100) + "%", font, DoomdarkColorModel.Color.LIGHT_GRAY,
                        null, null, null,
                        null, artefatto);
            }
        }

        int altezzaMassima = ImageCache.corniceInventario.getHeight() - 2 * (DIMENSIONE_BORDO_INTERNO + SPACING);
        int nuovoOffset = componenteScorrevole.limitaOffset(altezzaMassima, offset);
        Image image = componenteScorrevole.produci(altezzaMassima, nuovoOffset);
        graphics.drawImage(image, x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);

        return nuovoOffset;
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

    @Override
    public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
        if (movimentoRotella == MovimentoRotella.SU) {
            if (x >= leftBoxX && x < leftBoxLimit) {
                offsetYLeftBox = Math.max(0, offsetYLeftBox - numeroRotazioni * PASSO_SCORRIMENTO);
            } else if (x >= centerBoxX && x < centerBoxLimit) {
                offsetYBoxPersonaggio = Math.max(0, offsetYBoxPersonaggio - numeroRotazioni * PASSO_SCORRIMENTO);
            } else if (x >= rightBoxX && x < rightBoxLimit) {
                offsetYRightBox = Math.max(0, offsetYRightBox - numeroRotazioni * PASSO_SCORRIMENTO);
            }
        } else if (movimentoRotella == MovimentoRotella.GIU) {
            if (x >= leftBoxX && x < leftBoxLimit) {
                offsetYLeftBox += numeroRotazioni * PASSO_SCORRIMENTO;
            } else if (x >= centerBoxX && x < centerBoxLimit) {
                offsetYBoxPersonaggio += numeroRotazioni * PASSO_SCORRIMENTO;
            } else if (x >= rightBoxX && x < rightBoxLimit) {
                offsetYRightBox += numeroRotazioni * PASSO_SCORRIMENTO;
            }
        }
    }

}
