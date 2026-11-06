package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.ImmagineDelNodo;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.TestoDelNodo;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Elenco ad albero scorrevole, nella versione Swing: il modello (nodi, misure, scorrimento, posizioni) è
 * {@link ElencoScorrevole}, nel modulo comune; qui resta solo il disegno in un'immagine. Ogni nodo può portarsi dietro
 * un riferimento all'oggetto che rappresenta, per poter risalire dalla posizione di un click a quell'oggetto.
 * Inoltre può contenere una icona messa a sinistra.
 *
 * @param <T> il tipo dell'oggetto rappresentato da ciascun nodo
 *
 * @author Stefano Reksten
 */
public class ComponenteScorrevole<T> {

    private final int larghezza;
    private final int interlinea;
    private final int larghezzaIndentazione;
    private final ElencoScorrevole<T> elenco;
    // Come si ricava l'immagine di un'icona o di un valore che il modello dà come oggetto opaco (di solito il nome di una risorsa)
    private final Function<Object, Image> risolviImmagine;

    public ComponenteScorrevole(int larghezza, int larghezzaIndentazione, int interlinea) {
        this.larghezza = larghezza;
        this.larghezzaIndentazione = larghezzaIndentazione;
        this.interlinea = interlinea;
        this.elenco = new ElencoScorrevole<>(larghezza, larghezzaIndentazione, interlinea);
        this.risolviImmagine = oggetto -> (Image) oggetto;
    }

    /**
     * Il disegno di un elenco già fatto (di solito dal modulo comune, come {@link com.threeamigos.foresta.ui.schermate.ElencoDegliArtefatti}), le cui
     * immagini sono oggetti opachi che questo componente sa trasformare in immagini con <code>risolviImmagine</code>.
     */
    public ComponenteScorrevole(ElencoScorrevole<T> elenco, int larghezza, int larghezzaIndentazione, int interlinea, Function<Object, Image> risolviImmagine) {
        this.larghezza = larghezza;
        this.larghezzaIndentazione = larghezzaIndentazione;
        this.interlinea = interlinea;
        this.elenco = elenco;
        this.risolviImmagine = risolviImmagine;
    }

    private static TestoDelNodo testo(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore) {
        return testo != null ? new TestoDelNodo(testo, font, colore) : null;
    }

    private static ImmagineDelNodo immagine(Image immagine) {
        return immagine != null ? new ImmagineDelNodo(immagine, new Misura(immagine.getWidth(null), immagine.getHeight(null))) : null;
    }

    /**
     * Aggiunge un nodo con una chiave e una descrizione.
     */
    public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                         String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                         T riferimento) {
        return creaNodo(chiave, fontChiave, coloreChiave,
                null, null, null,
                descrizione, fontDescrizione, coloreDescrizione,
                null, riferimento);
    }

    /**
     * Aggiunge un nodo con una chiave, una descrizione e un'icona a sinistra.
     */
    public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                         String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                         Image icona, T riferimento) {
        return creaNodo(chiave, fontChiave, coloreChiave,
                null, null, null,
                descrizione, fontDescrizione, coloreDescrizione,
                icona, riferimento);
    }

    /**
     * Aggiunge un nodo con una chiave, un valore scritto, una descrizione e un'icona a sinistra.
     */
    public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                         String valore, DoomdarkFont fontValore, DoomdarkColorModel.Color coloreValore,
                         String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                         Image icona, T riferimento) {
        return creaNodo(chiave, fontChiave, coloreChiave,
                valore != null ? ImageCache.get(valore, fontValore, coloreValore) : null,
                descrizione, fontDescrizione, coloreDescrizione,
                icona, riferimento);
    }

    /**
     * Come sopra, con il valore già disegnato: serve quando è fatto di più parti di colori diversi.
     */
    public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                         Image valore,
                         String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                         Image icona, T riferimento) {
        return new Nodo(elenco.creaNodo(testo(chiave, fontChiave, coloreChiave), null, immagine(valore),
                testo(descrizione, fontDescrizione, coloreDescrizione), immagine(icona), riferimento));
    }

    /**
     * Aggiunge un separatore orizzontale.
     */
    public void creaSeparatore() {
        elenco.creaSeparatore(immagine(ImageCache.separatore));
    }

    /**
     * Di quanti pixel si può scorrere al massimo la lista in uno spazio alto altezzaMassima: 0 se ci sta tutta.
     */
    public int getMassimoScorrimento(int altezzaMassima) {
        return elenco.getMassimoScorrimento(altezzaMassima);
    }

    /**
     * Se la lista è più alta dello spazio in cui la si disegna, e quindi si scorre (con le frecce su e giù che lo
     * segnalano, vedi produci)
     */
    public boolean isScorrevole(int altezzaMassima) {
        return elenco.isScorrevole(altezzaMassima);
    }

    /**
     * Riporta l'offset di scorrimento entro i limiti della lista: non si scorre
     * sopra la prima riga né oltre l'ultima.
     */
    public int limitaOffset(int altezzaMassima, int offset) {
        return elenco.limitaOffset(altezzaMassima, offset);
    }

    /**
     * Disegna la porzione dell'elenco che sta nello spazio alto altezzaMassima, scorsa di quanto dice lo scorrimento.
     * Prima gli dice di quanto la lista può scorrere, così lo scorrimento resta nei limiti anche se l'elenco è
     * cambiato dall'ultima volta (più corto, o senza più niente da scorrere).
     */
    public Image produci(int altezzaMassima, ScorrimentoSuUnAsse scorrimento) {
        int massimo = getMassimoScorrimento(altezzaMassima);
        scorrimento.imposta(massimo > 0, massimo);
        int offset = (int) Math.round(scorrimento.getScorrimento());
        int dentro = limitaOffset(altezzaMassima, offset);
        Image porzione = produci(altezzaMassima, dentro);
        if (dentro == offset) {
            return porzione;
        }
        // Tirata oltre un capo (vedi ScorrimentoSuUnAsse): la porzione si sposta di quanto è oltre, e resta vuoto il resto
        BufferedImage tirata = BufferedImageBuilder.creaCompatibile(Math.max(1, larghezza), Math.max(1, altezzaMassima), Transparency.TRANSLUCENT);
        Graphics2D g2d = tirata.createGraphics();
        try {
            g2d.drawImage(porzione, 0, dentro - offset, null);
        } finally {
            g2d.dispose();
        }
        return tirata;
    }

    /**
     * Disegna una porzione dell'elenco, alta al più quell'altezza e a partire da quella posizione di scorrimento.
     */
    public Image produci(int altezzaMassima, int offset) {
        // Un menu senza scelte o un riquadro senza altezza chiedono un'immagine vuota: BufferedImage non la crea (e lancerebbe a ogni
        // fotogramma), quindi si produce comunque almeno un pixel
        final int altezza = Math.max(1, altezzaMassima);
        final int scorso = limitaOffset(altezza, offset);
        final List<ElementoDelElenco> elementi = elenco.elementiVisibili(altezza, scorso);
        final boolean frecciaSu = elenco.isFrecciaSuVisibile(scorso);
        final boolean frecciaGiu = elenco.isFrecciaGiuVisibile(altezza, scorso);

        // I nodi sono nuovi a ogni fotogramma, ma le loro immagini sono quelle in cache: se non è cambiato niente
        // (stesse immagini, stessi testi, stesse posizioni, stesso scorrimento) la porzione di prima è ancora giusta
        List<Object> firma = new ArrayList<>(Arrays.asList(larghezza, altezza, scorso, elenco.altezzaDellElenco(altezza), interlinea,
                larghezzaIndentazione, frecciaSu, frecciaGiu));
        for (ElementoDelElenco e : elementi) {
            firma.addAll(Arrays.asList(e.getTipo(), e.getImmagine(), e.getTesto(), e.getFont(), e.getColore(), e.getX(), e.getY(),
                    e.getLarghezzaDiImpaginazione()));
        }
        return ImageCache.getPorzione(firma, () -> disegnaPorzione(elementi, altezza, scorso, frecciaSu, frecciaGiu));
    }

    private Image disegnaPorzione(List<ElementoDelElenco> elementi, int altezzaMassima, int offset, boolean frecciaSu, boolean frecciaGiu) {
        // Si disegna solo la porzione visibile, non l'intera lista
        BufferedImage risultato = BufferedImageBuilder.creaCompatibile(Math.max(1, larghezza), altezzaMassima, Transparency.TRANSLUCENT);

        Graphics2D g2d = risultato.createGraphics();
        g2d.translate(0, -offset);

        final int SPACING = Finestra.SPACING;

        for (ElementoDelElenco e : elementi) {
            Image immagine;
            switch (e.getTipo()) {
                case ICONA:
                case VALORE_IMMAGINE:
                    immagine = risolviImmagine.apply(e.getImmagine());
                    break;
                default:
                    // Titolo, descrizione o valore scritto: il testo a capo, già in cache come immagine
                    immagine = ImageCache.getTestoACapo(e.getTesto(), e.getFont(), e.getColore(),
                            Math.max(e.getLarghezzaDiImpaginazione(), e.getFont().getHeight()), interlinea);
                    if (e.getTipo() == ElementoDelElenco.Tipo.VALORE_TESTO) {
                        immagine = ImageCache.get(e.getTesto(), e.getFont(), e.getColore());
                    }
                    break;
            }
            g2d.drawImage(immagine, e.getX(), e.getY(), null);
        }

        if (frecciaSu) {
            Image freccia = ImageCache.componenteScorrevoleFrecciaSu;
            g2d.drawImage(freccia, larghezza - freccia.getWidth(null) - SPACING, offset + SPACING, null);
        }
        if (frecciaGiu) {
            Image freccia = ImageCache.componenteScorrevoleFrecciaGiu;
            g2d.drawImage(freccia,
                    larghezza - freccia.getWidth(null) - SPACING,
                    offset + altezzaMassima - SPACING - freccia.getHeight(null),
                    null);
        }

        g2d.dispose();
        return risultato;
    }

    /**
     * Riporta il riferimento del nodo che occupa la quota indicata (con la sua descrizione e i suoi spazi, non solo
     * il titolo), oppure null se la quota è sopra la lista o oltre l'ultimo nodo.
     *
     * @param quota espressa in coordinate della lista, quindi comprensiva dell'offset
     *              di scorrimento con cui la lista è stata prodotta
     */
    public T riferimentoTitoloAllaQuota(int quota) {
        return elenco.riferimentoTitoloAllaQuota(quota);
    }

    /**
     * Riporta la posizione (indentazione come x, quota assoluta nelle coordinate della lista
     * come y, prima di sottrarre l'offset di scorrimento) a cui viene disegnata l'icona/il
     * titolo del primo nodo il cui riferimento soddisfa il predicato indicato, oppure null se
     * nessun nodo corrisponde.
     */
    public Point posizioneTitolo(Predicate<T> predicato) {
        com.threeamigos.foresta.ui.Punto p = elenco.posizioneTitolo(predicato);
        return p != null ? new Point(p.x, p.y) : null;
    }

    /**
     * Un nodo dell'elenco, visto da Swing: avvolge quello del modello e accetta le immagini di AWT.
     */
    public class Nodo {
        private final ElencoScorrevole<T>.Nodo nodo;

        private Nodo(ElencoScorrevole<T>.Nodo nodo) {
            this.nodo = nodo;
        }

        /**
         * Aggiunge a questo nodo un figlio con una chiave, una descrizione e un'icona.
         */
        public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                             String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                             Image icona, T riferimento) {
            return creaNodo(chiave, fontChiave, coloreChiave,
                    null, null, null,
                    descrizione, fontDescrizione, coloreDescrizione,
                    icona, riferimento);
        }

        /**
         * Aggiunge a questo nodo un figlio con una chiave, un valore scritto, una descrizione e un'icona.
         */
        public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                             String valore, DoomdarkFont fontValore, DoomdarkColorModel.Color coloreValore,
                             String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                             Image icona, T riferimento) {
            return new Nodo(nodo.creaNodo(testo(chiave, fontChiave, coloreChiave), null,
                    valore != null ? immagine(ImageCache.get(valore, fontValore, coloreValore)) : null,
                    testo(descrizione, fontDescrizione, coloreDescrizione), immagine(icona), riferimento));
        }

        public int getAltezzaCompleta() {
            return nodo.getAltezzaCompleta();
        }

        public int getAltezzaSenzaFigli() {
            return nodo.getAltezzaSenzaFigli();
        }

        public int getIndentazione() {
            return nodo.getIndentazione();
        }

        public void setFigliVisibili(boolean figliVisibili) {
            nodo.setFigliVisibili(figliVisibili);
        }

        public boolean isFigliVisibili() {
            return nodo.isFigliVisibili();
        }

        @Override
        public String toString() {
            return nodo.toString();
        }
    }
}
