package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Elenco ad albero scorrevole. Ogni nodo può portarsi dietro un riferimento
 * all'oggetto che rappresenta, per poter risalire dalla posizione di un click
 * a quell'oggetto. Inoltre può contenere una icona messa a sinistra.
 *
 * @param <T> il tipo dell'oggetto rappresentato da ciascun nodo
 *
 * @author Stefano Reksten
 */
public class ComponenteScorrevole<T> {

    private final int larghezza;

    private final int larghezzaIndentazione;
    private final int interlinea;
    private final List<Nodo> nodi = new ArrayList<>();

    public ComponenteScorrevole(int larghezza, int larghezzaIndentazione, int interlinea) {
        this.larghezza = larghezza;
        this.larghezzaIndentazione = larghezzaIndentazione;
        this.interlinea = interlinea;
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
        Nodo nodo = new Nodo(chiave, fontChiave, coloreChiave,
                valore,
                descrizione, fontDescrizione, coloreDescrizione,
                icona, 0, riferimento);
        nodi.add(nodo);
        return nodo;
    }

    /**
     * Aggiunge un separatore orizzontale.
     */
    public void creaSeparatore() {
        creaNodo(null, null, null,
                null, null, null,
                null, null, null,
                ImageCache.separatore, null);
    }

    private int calcolaAltezzaMassimaNodi() {
        return nodi.stream().mapToInt(Nodo::getAltezzaCompleta).sum();
    }

    /**
     * Se la lista è più alta dello spazio in cui la si disegna, e quindi si scorre (con le frecce su e giù che lo
     * segnalano, vedi produci)
     */
    public boolean isScorrevole(int altezzaMassima) {
        return calcolaAltezzaMassimaNodi() > altezzaMassima;
    }

    /**
     * Riporta l'offset di scorrimento entro i limiti della lista: non si scorre
     * sopra la prima riga né oltre l'ultima.
     */
    public int limitaOffset(int altezzaMassima, int offset) {
        return limitaOffset(calcolaAltezzaMassimaNodi(), altezzaMassima, offset);
    }

    private int limitaOffset(int altezzaImmagine, int altezzaMassima, int offset) {
        return Math.max(0, Math.min(offset, altezzaImmagine - altezzaMassima));
    }

    /**
     * Disegna una porzione dell'elenco, alta al più quell'altezza e a partire da quella posizione di scorrimento.
     */
    public Image produci(int altezzaMassima, int offset) {
        // Un menu senza scelte o un riquadro senza altezza chiedono un'immagine vuota: BufferedImage non la crea (e lancerebbe a ogni
        // fotogramma), quindi si produce comunque almeno un pixel
        altezzaMassima = Math.max(1, altezzaMassima);

        List<Nodo> contenuto = espandiNodi();

        int altezzaImmagine = Math.max(calcolaAltezzaMassimaNodi(), altezzaMassima);
        offset = limitaOffset(altezzaImmagine, altezzaMassima, offset);

        // I nodi sono nuovi a ogni fotogramma, ma le loro immagini sono quelle in cache: se non è cambiato niente
        // (stesse immagini, stesse posizioni, stesso scorrimento) la porzione di prima è ancora giusta
        List<Object> firma = new ArrayList<>();
        firma.addAll(Arrays.asList(larghezza, altezzaMassima, offset, altezzaImmagine, interlinea, larghezzaIndentazione));
        for (Nodo nodo : contenuto) {
            firma.add(nodo.icona);
            firma.add(nodo.chiave);
            firma.add(nodo.valore);
            firma.add(nodo.isFigliVisibili() ? nodo.descrizione : null);
            firma.add(nodo.indentazione);
        }
        final int altezzaMassimaFinale = altezzaMassima;
        final int offsetFinale = offset;
        return ImageCache.getPorzione(firma, () -> disegnaPorzione(contenuto, altezzaMassimaFinale, offsetFinale));
    }

    private Image disegnaPorzione(List<Nodo> contenuto, int altezzaMassima, int offset) {
        // Si disegna solo la porzione visibile, non l'intera lista
        BufferedImage risultato = BufferedImageBuilder.creaCompatibile(Math.max(1, larghezza), altezzaMassima, Transparency.TRANSLUCENT);

        Graphics2D g2d = risultato.createGraphics();
        g2d.translate(0, -offset);

        final int SPACING = ImageCache.SPACING;

        int altezzaRaggiunta = 0;
        for (Nodo nodo : contenuto) {
            // Ogni riga viene disegnata alla propria posizione assoluta: si evita il
            // disegno di quelle che non intersecano la finestra ritagliata, non lo spazio
            // che occupano.
            int altezzaNodo = nodo.getAltezzaSenzaFigli();
            if (altezzaRaggiunta + altezzaNodo > offset && altezzaRaggiunta < offset + altezzaMassima) {
                int x = nodo.getIndentazione();
                if (nodo.icona != null) {
                    g2d.drawImage(nodo.icona, x, altezzaRaggiunta, null);
                    x += nodo.icona.getWidth(null) + SPACING;
                }
                if (nodo.chiave != null) {
                    g2d.drawImage(nodo.chiave, x, altezzaRaggiunta, null);
                }
                if (nodo.valore != null) {
                    g2d.drawImage(nodo.valore, larghezza - nodo.valore.getWidth(null), altezzaRaggiunta, null);
                }
                if (nodo.isFigliVisibili() && nodo.descrizione != null) {
                    int altezzaDescrizione = altezzaRaggiunta +
                            (nodo.chiave != null ? nodo.chiave.getHeight(null) : 0) +
                            interlinea;
                    g2d.drawImage(nodo.descrizione, x + larghezzaIndentazione, altezzaDescrizione, null);
                }
            }
            altezzaRaggiunta += altezzaNodo;
        }

        if (offset > 0) {
            Image frecciaSu = ImageCache.componenteScorrevoleFrecciaSu;
            g2d.drawImage(frecciaSu, larghezza - frecciaSu.getWidth(null) - SPACING, offset + SPACING,null);
        }
        if (altezzaRaggiunta - offset > altezzaMassima) {
            Image frecciaGiu = ImageCache.componenteScorrevoleFrecciaGiu;
            g2d.drawImage(frecciaGiu,
                    larghezza - frecciaGiu.getWidth(null) - SPACING,
                    offset + altezzaMassima - SPACING - frecciaGiu.getHeight(null),
                    null);
        }

        g2d.dispose();
        return risultato;
    }

    private List<Nodo> espandiNodi() {
        List<Nodo> listaRisultante = new ArrayList<>();
        for (Nodo nodo : nodi) {
            addNodoAListaRisultante(nodo, listaRisultante);
        }
        return listaRisultante;
    }

    private void addNodoAListaRisultante(Nodo nodo, List<Nodo> listaRisultante) {
        listaRisultante.add(nodo);
        if (nodo.isFigliVisibili()) {
            for (Nodo figlio : nodo.figli) {
                addNodoAListaRisultante(figlio, listaRisultante);
            }
        }
    }

    /**
     * Riporta il riferimento del nodo che occupa la quota indicata (con la sua descrizione e i suoi spazi, non solo
     * il titolo), oppure null se la quota è sopra la lista o oltre l'ultimo nodo.
     *
     * @param quota espressa in coordinate della lista, quindi comprensiva dell'offset
     *              di scorrimento con cui la lista è stata prodotta
     */
    public T riferimentoTitoloAllaQuota(int quota) {
        if (quota < 0) {
            return null;
        }
        int altezzaRaggiunta = 0;
        for (Nodo nodo : espandiNodi()) {
            int altezzaNodo = nodo.getAltezzaSenzaFigli();
            if (quota < altezzaRaggiunta + altezzaNodo) {
                return nodo.riferimento;
            }
            altezzaRaggiunta += altezzaNodo;
        }
        return null;
    }

    /**
     * Riporta la posizione (indentazione come x, quota assoluta nelle coordinate della lista
     * come y, prima di sottrarre l'offset di scorrimento) a cui viene disegnata l'icona/il
     * titolo del primo nodo il cui riferimento soddisfa il predicato indicato, oppure null se
     * nessun nodo corrisponde.
     */
    public Point posizioneTitolo(Predicate<T> predicato) {
        int altezzaRaggiunta = 0;
        for (Nodo nodo : espandiNodi()) {
            if (nodo.riferimento != null && predicato.test(nodo.riferimento)) {
                return new Point(nodo.getIndentazione(), altezzaRaggiunta);
            }
            altezzaRaggiunta += nodo.getAltezzaSenzaFigli();
        }
        return null;
    }

    /**
     * Un nodo dell'elenco: le immagini di chiave, valore e descrizione già composte, l'icona e i dati originali, più i nodi figli che si possono aprire e chiudere.
     */
    public class Nodo {
        private final Image icona;
        private final Image chiave;
        private final Image valore;
        private final Image descrizione;
        private final String datiOriginali;

        private final int indentazione;
        // Indentazione effettiva di testo/descrizione: se è presente un'immagine,
        // slitta fissa verso destra di (larghezza immagine + ImageCache.SPACING).

        private final T riferimento;
        private final List<Nodo> figli = new ArrayList<>();
        private boolean figliVisibili = true;

        Nodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
             Image valore,
             String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
             Image icona, int indentazione, T riferimento) {
            this.riferimento = riferimento;
            this.icona = icona;
            this.indentazione = indentazione;

            int larghezzaMassimaChiave = larghezza - indentazione;
            if (icona != null) {
                larghezzaMassimaChiave = larghezzaMassimaChiave - icona.getWidth(null) - ImageCache.SPACING;
            }

            // La descrizione è indentata un livello in più
            int larghezzaMassimaDescrizione = larghezzaMassimaChiave - larghezzaIndentazione;

            // Se abbiamo anche un valore la larghezza massima per la chiave si riduce, ma per la descrizione rimane la stessa
            this.valore = valore;
            if (valore != null) {
                larghezzaMassimaChiave = larghezzaMassimaChiave - valore.getWidth(null) - ImageCache.SPACING;
            }
            if (chiave != null) {
                this.chiave = creaImmagine(fontChiave, coloreChiave, chiave, larghezzaMassimaChiave);
            } else {
                this.chiave = null;
            }
            if (descrizione != null) {
                this.descrizione = creaImmagine(fontDescrizione, coloreDescrizione, descrizione, larghezzaMassimaDescrizione);
            } else {
                this.descrizione = null;
            }

            this.datiOriginali = "Chiave: " + chiave +
                    " Valore: " + valore +
                    " Descrizione: " + descrizione;
        }

        private Image creaImmagine(DoomdarkFont font, DoomdarkColorModel.Color color, String testo, int larghezza) {
            // Un valore molto largo non deve lasciare alla chiave una larghezza negativa: l'immagine non si creerebbe
            return ImageCache.getTestoACapo(testo, font, color, Math.max(larghezza, font.getHeight()), interlinea);
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
            Nodo nodo = new Nodo(chiave, fontChiave, coloreChiave,
                    valore != null ? ImageCache.get(valore, fontValore, coloreValore) : null,
                    descrizione, fontDescrizione, coloreDescrizione,
                    icona, this.indentazione + larghezzaIndentazione, riferimento);
            figli.add(nodo);
            return nodo;
        }

        public int getAltezzaCompleta() {
            return getAltezza(true);
        }

        public int getAltezzaSenzaFigli() {
            return getAltezza(false);
        }

        private int getAltezza(boolean inclusiNodiFigli) {
            int altezza = 0;
            if (chiave != null) {
                altezza = chiave.getHeight(null);
            }
            if (valore != null) {
                altezza = Math.max(altezza, valore.getHeight(null));
            }
            if (figliVisibili) {
                if (descrizione != null) {
                    altezza += interlinea + descrizione.getHeight(null);
                }
                if (inclusiNodiFigli) {
                    altezza += figli.stream().mapToInt(Nodo::getAltezzaCompleta).sum();
                }
            }
            if (icona != null) {
                altezza = Math.max(altezza, icona.getHeight(null));
            }
            altezza += interlinea;
            return altezza;
        }

        public int getIndentazione() {
            return indentazione;
        }

        public void setFigliVisibili(boolean figliVisibili) {
            this.figliVisibili = figliVisibili;
        }

        public boolean isFigliVisibili() {
            return figliVisibili;
        }

        @Override
        public String toString() {
            return datiOriginali;
        }
    }
}
