package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoRifiutoAcquisto;
import com.threeamigos.foresta.eventi.EventoRifiutoPrelievo;
import com.threeamigos.foresta.motore.AutomaScambiatoreArtefatti;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.SupertipoArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Incantamento;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Stefano Reksten
 */
abstract class DisplayableCanvasScambiatoreArtefatti  implements Finestra {

    protected static final int SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI = 20;

    /**
     * Le coordinate del fumetto sono a comune tra Inventario e Armaiolo perché entrambe comunque disegnano un personaggio a metà schermo
     */
    protected static CoordinateFumetto COORDINATE_FUMETTO;

    protected static final int DIMENSIONE_BORDO_INTERNO = 16;
    protected static final int corniceInventarioWidth = ImageCache.corniceInventario.getWidth(null);
    protected static final int ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO = ImageCache.corniceInventario.getHeight()
            - 2 * (DIMENSIONE_BORDO_INTERNO + ImageCache.SPACING);
    protected static final int LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO = ImageCache.corniceInventario.getWidth()
            - 2 * (DIMENSIONE_BORDO_INTERNO + ImageCache.SPACING);
    // Pixel di scorrimento per ogni scatto della rotella
    protected static final int PASSO_SCORRIMENTO = 2;

    protected static final int ALTEZZA_LADRO = ClassePersonaggioImmagine.getImmagine(ClassePersonaggio.LADRO).getHeight(null);

    protected final DisplayableCanvas displayableCanvas;
    protected final int width;
    protected final int height;
    protected final DoomdarkFont font = DoomdarkFontMedium.getInstance();
    protected final int fontHeight = font.getHeight();
    protected final DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();

    protected final int leftBoxX;
    protected final int leftBoxLimit;
    protected final int centerBoxX;
    protected final int centerBoxLimit;
    protected final int rightBoxX;
    protected final int rightBoxLimit;

    protected AutomaScambiatoreArtefatti automa;
    protected int offsetYLeftBox = 0;
    protected int offsetYBoxPersonaggio = 0;
    protected int offsetYRightBox = 0;

    // Posizione del mouse, per evidenziare in bianco l'artefatto sotto il cursore.
    // -1 significa "cursore fuori dalla finestra".
    protected int mouseX = -1;
    protected int mouseY = -1;

    // Quota (in coordinate della finestra) a cui inizia l'elenco delle caratteristiche del
    // personaggio, aggiornata a ogni disegnaInventario e usata per l'hit-test dei click.
    protected int yAttributi = 0;

    DisplayableCanvasScambiatoreArtefatti(DisplayableCanvas displayableCanvas, int width, int height) {
        this.displayableCanvas = displayableCanvas;
        this.width = width;
        this.height = height;

        leftBoxX = SPACING + DIMENSIONE_BORDO_INTERNO;
        leftBoxLimit = leftBoxX + corniceInventarioWidth - DIMENSIONE_BORDO_INTERNO;

        centerBoxX = SPACING + corniceInventarioWidth + SPACING;
        centerBoxLimit = width - SPACING - corniceInventarioWidth - SPACING;

        rightBoxX = width - SPACING - corniceInventarioWidth + DIMENSIONE_BORDO_INTERNO;
        rightBoxLimit = width - SPACING - DIMENSIONE_BORDO_INTERNO;

        BusEventi.iscriviti(EventoRifiutoAcquisto.class, this::onEventoRifiutoAcquisto);
        BusEventi.iscriviti(EventoRifiutoPrelievo.class, this::onEventoRifiutoPrelievo);
    }

    CoordinateFumetto getCoordinateFumetto() {
        if (COORDINATE_FUMETTO == null) {
            COORDINATE_FUMETTO = new CoordinateFumetto(
                    width / 2 + ImageCache.armaiolo.getWidth() + SPACING,
                    SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + ImageCache.armaiolo.getHeight() / 2,
                    width / 2 + ImageCache.armaiolo.getWidth() / 3,
                    SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + ImageCache.armaiolo.getHeight() / 3
            );
        }
        return COORDINATE_FUMETTO;
    }

    private void onEventoRifiutoAcquisto(EventoRifiutoAcquisto evento) {
        displayableCanvas.notificaFumetto("Non hai abbastanza monete per comprare questo oggetto.", getCoordinateFumetto());
    }

    private void onEventoRifiutoPrelievo(EventoRifiutoPrelievo evento) {
        displayableCanvas.notificaFumetto("Questo oggetto è troppo pesante.", getCoordinateFumetto());
    }

    void impostaAutoma(AutomaScambiatoreArtefatti automa) {
        this.automa = automa;
    }

    void disegnaInventario(Graphics2D graphics) {

        graphics.drawImage(ImageCache.corniceInventario, SPACING, SPACING, null);
        graphics.drawImage(ImageCache.corniceInventario, width - SPACING - corniceInventarioWidth, SPACING, null);

        if (automa == null) {
            return;
        }

        disegnaColonnaPersonaggio(graphics);

        // Inventario personaggio
        offsetYLeftBox = disegnaElenco(graphics, new ArrayList<>(automa.getParteAttiva().getInventario()), leftBoxX,
                offsetYLeftBox, automa.mostraCostoSuParteAttiva());
        // Inventario gruppo
        offsetYRightBox = disegnaElenco(graphics, automa.getParteRemota().getInventario(), rightBoxX, offsetYRightBox,
                automa.mostraCostoSuParteRemota());

        disegnaIntestazioniInventario(graphics);
    }

    // Demandato alle sottoclassi che sanno cosa rappresentano i due rettangoli e che chiamano la impl
    abstract void disegnaIntestazioniInventario(Graphics2D graphics);

    protected void disegnaIntestazioniInventarioImpl(Graphics2D graphics, String intestazioneSinistra, String intestazioneDestra) {
        Image image = ImageCache.get(intestazioneSinistra, DoomdarkColorModel.Color.BLACK);
        int x = SPACING + corniceInventarioWidth / 2 - image.getWidth(null) / 2;
        int y = SPACING;
        graphics.drawImage(image, x + 2, y + 2, null);
        image = ImageCache.get(intestazioneSinistra, DoomdarkColorModel.Color.LIGHT_GRAY);
        graphics.drawImage(image, x, y, null);

        image = ImageCache.get(intestazioneDestra, DoomdarkColorModel.Color.BLACK);
        x = width - SPACING - corniceInventarioWidth / 2 - image.getWidth(null) / 2;
        graphics.drawImage(image, x + 2, y + 2, null);
        image = ImageCache.get(intestazioneDestra, DoomdarkColorModel.Color.LIGHT_GRAY);
        graphics.drawImage(image, x, y, null);
    }

    abstract void disegnaColonnaPersonaggio(Graphics2D graphics);

    private int disegnaElenco(Graphics2D graphics, Collection<Artefatto> artefatti, int x, int offset,
                              boolean mostraCosto) {

        Artefatto evidenziato = trovaArtefatto(artefatti, x, offset, mouseX, mouseY);
        ComponenteScorrevole<Artefatto> componenteScorrevole = costruisciComponenteScorrevoleArtefatti(artefatti, evidenziato, mostraCosto);

        int nuovoOffset = componenteScorrevole.limitaOffset(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, offset);
        Image image = componenteScorrevole.produci(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, nuovoOffset);
        graphics.drawImage(image, x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);

        return nuovoOffset;
    }

    /**
     * L'albero viene ricostruito a ogni disegno e a ogni click. L'artefatto passato in
     * evidenziato (se non null) viene disegnato in bianco invece che in grigio chiaro.
     */
    private ComponenteScorrevole<Artefatto> costruisciComponenteScorrevoleArtefatti(Collection<Artefatto> artefatti,
                                                                                    Artefatto evidenziato, boolean mostraCosto) {

        ComponenteScorrevole<Artefatto> componenteScorrevole = new ComponenteScorrevole<>(
                LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, 10, 2);

        Collection<Artefatto> artefattiDaDisegnare = ordinaArtefattiDaDisegnare(artefatti);

        SupertipoArtefatto supertipoPrecedente = null;
        for (Artefatto artefatto : artefattiDaDisegnare) {

            if (supertipoPrecedente != artefatto.getTipo().getSupertipo()) {
                supertipoPrecedente = artefatto.getTipo().getSupertipo();
                componenteScorrevole.creaNodo(
                        null, null, null,
                        null, null, null,
                        null, null, null,
                        getImmagineSupertipo(supertipoPrecedente), null);
            }

            DoomdarkColorModel.Color colore = artefatto == evidenziato
                    ? DoomdarkColorModel.Color.WHITE
                    : DoomdarkColorModel.Color.LIGHT_GRAY;
            DoomdarkColorModel.Color coloreAttributi = artefatto == evidenziato
                    ? DoomdarkColorModel.Color.LIGHT_GRAY
                    : DoomdarkColorModel.Color.MEDIUM_GRAY;
            DoomdarkColorModel.Color coloreSeparatori = artefatto == evidenziato
                    ? DoomdarkColorModel.Color.MEDIUM_GRAY
                    : DoomdarkColorModel.Color.DARK_GRAY;

            String nome = artefatto.getNome();
            nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);
            ComponenteScorrevole<Artefatto>.Nodo nodo = componenteScorrevole.creaNodo(
                    nome, font, colore,
                    mostraCosto ? String.valueOf(artefatto.getCostoAcquisto()) : null, fontSmall, DoomdarkColorModel.Color.YELLOW,
                    artefatto.getTipo().getDescrizione(), fontSmall, colore,
                    null, artefatto);
            nodo.setFigliVisibili(artefatto.isFigliVisibili());
            if (!artefatto.getModificatori().isEmpty()) {
                nodo.creaNodo(
                        "Modificatori:", font, coloreSeparatori,
                        null, null, null,
                        null, null, null,
                        null, artefatto);
                for (ModificatoreAttributo modificatore : artefatto.getModificatori()) {
                    String valore;
                    switch (modificatore.getTipoModificatoreAttributo()) {
                        case AUMENTO_FISSO:
                            valore = (modificatore.getQuantita() < 0 ? "-" : "+") + (int) modificatore.getQuantita();
                            break;
                        case AUMENTO_PERCENTUALE:
                            valore = (modificatore.getQuantita() < 0 ? "-" : "+") + (int) modificatore.getQuantita() + "%";
                            break;
                        case QUANTITA_ASSOLUTA:
                            valore = "Porta a " + (int) modificatore.getQuantita();
                            break;
                        default:
                            valore = "";
                            break;
                    }
                    nodo.creaNodo(
                            modificatore.getTipoAttributo().getNome(), font, coloreAttributi,
                            valore, font, coloreAttributi,
                            null, null, null,
                            null, artefatto);

                }
            }
            if (!artefatto.getIncantamenti().isEmpty()) {
                nodo.creaNodo(
                        "Incantamenti:", font, coloreSeparatori,
                        null, null, null,
                        null, null, null,
                        null, artefatto);
                for (Incantamento incantamento : artefatto.getIncantamenti()) {
                    nodo.creaNodo(
                            incantamento.getNomeIncantamento(), font, coloreAttributi,
                            null, null, null,
                            null, artefatto);
                    int bonusFisso = incantamento.getDannoBonusFisso();
                    nodo.creaNodo(
                            incantamento.getTipoDannoElementale().getNome(), font, coloreAttributi,
                            (bonusFisso < 0 ? "-" : "+") + bonusFisso +
                                    " + " + (int) (incantamento.getCoefficienteScala() * 100) + "%", font, coloreAttributi,
                            null, null, null,
                            null, artefatto);
                }
            }
        }

        return componenteScorrevole;
    }

    private static Collection<Artefatto> ordinaArtefattiDaDisegnare(Collection<Artefatto> artefatti) {
        java.util.List<Artefatto> artefattiDaDisegnare = new ArrayList<>(artefatti);
        artefattiDaDisegnare.sort((a1, a2) -> {
            int ordinaleSupertipo1 = a1.getTipo().getSupertipo().ordinal();
            int ordinaleSupertipo2 = a2.getTipo().getSupertipo().ordinal();
            if (ordinaleSupertipo1 == ordinaleSupertipo2) {
                int ordinaleTipo1 = a1.getTipo().ordinal();
                int ordinaleTipo2 = a2.getTipo().ordinal();
                if (ordinaleTipo1 == ordinaleTipo2) {
                    return a1.getNome().compareTo(a2.getNome());
                }
                return Integer.compare(ordinaleTipo1, ordinaleTipo2);
            }
            return Integer.compare(ordinaleSupertipo1, ordinaleSupertipo2);
        });
        return artefattiDaDisegnare;
    }

    /**
     * Riporta l'artefatto disegnato alla posizione (x, y) espressa in coordinate della
     * finestra, oppure null se il punto non cade sull'elenco o non corrisponde al titolo
     * di un artefatto (es. una riga di modificatore/incantamento, o spazio vuoto).
     */
    private Artefatto trovaArtefatto(Collection<Artefatto> artefatti, int boxX, int offset, int x, int y) {
        int xInterno = x - (boxX + SPACING);
        int yInterno = y - (DIMENSIONE_BORDO_INTERNO + 2 * SPACING);
        if (xInterno < 0 || xInterno >= LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO
                || yInterno < 0 || yInterno >= ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO) {
            return null;
        }
        return costruisciComponenteScorrevoleArtefatti(artefatti, null, false).riferimentoTitoloAllaQuota(yInterno + offset);
    }

    @Override
    public void processaMovimento(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    @Override
    public void processaUscita(int x, int y) {
        mouseX = -1;
        mouseY = -1;
    }

    protected abstract boolean processaClickPersonaggio(int x, int y, Tasto tasto);

    @Override
    public void processaClick(int x, int y, Tasto tasto) {
        if (tasto != Tasto.SINISTRO || automa == null) {
            return;
        }
        if (processaClickPersonaggio(x, y, tasto)) {
            return;
        }
        java.util.List<Artefatto> inventarioPersonaggio = new ArrayList<>(automa.getParteAttiva().getInventario());
        Artefatto artefatto = trovaArtefatto(inventarioPersonaggio, leftBoxX, offsetYLeftBox, x, y);
        if (artefatto == null) {
            artefatto = trovaArtefatto(automa.getArtefattiDisponibili(), rightBoxX, offsetYRightBox, x, y);
        }
        if (artefatto == null) {
            return;
        }
        if (artefatto.isFigliVisibili()) {
            artefatto.nascondiFigli();
        } else {
            artefatto.mostraFigli();
        }
    }

    protected abstract boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto);

    @Override
    public void processaDoppioClick(int x, int y, Tasto tasto) {
        if (automa == null) {
            return;
        }
        if (processaDoppioClickPersonaggio(x, y, tasto)) {
            return;
        }
        List<Artefatto> inventarioPersonaggio = new ArrayList<>(automa.getParteAttiva().getInventario());
        Artefatto artefatto = trovaArtefatto(inventarioPersonaggio, leftBoxX, offsetYLeftBox, x, y);
        if (artefatto != null) {
            automa.richiediSpostamentoSuParteRemota(artefatto);
            //automa.spostaSuParteRemota(artefatto);
            return;
        }
        Collection<Artefatto> disponibili = automa.getArtefattiDisponibili();
        artefatto = trovaArtefatto(disponibili, rightBoxX, offsetYRightBox, x, y);
        if (artefatto != null) {
            automa.richiediSpostamentoSuParteAttiva(artefatto);
            //automa.spostaSuParteAttiva(artefatto);
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

    protected Image getImmagineSupertipo(SupertipoArtefatto supertipo) {
        switch (supertipo) {
            case ARMA:
                return ImageCache.separatoreArmi;
            case ARMATURA:
                return ImageCache.separatoreArmature;
            case SCUDO:
                return ImageCache.separatoreScudi;
            case ALTRO:
                return ImageCache.separatoreNinnoli;
            default:
                throw new IllegalArgumentException("SupertipoArtefatto senza immagine associata");
        }
    }
}
