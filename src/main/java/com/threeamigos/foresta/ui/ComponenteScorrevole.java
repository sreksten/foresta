package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Elenco ad albero scorrevole. Ogni nodo può portarsi dietro un riferimento
 * all'oggetto che rappresenta, per poter risalire dalla posizione di un click
 * a quell'oggetto. Inoltre può contenere una immagine di accompagnamento.
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

    public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                         String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                         T riferimento) {
        return creaNodo(chiave, fontChiave, coloreChiave,
                null, null, null,
                descrizione, fontDescrizione, coloreDescrizione,
                null, riferimento);
    }

    public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                         String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                         Image icona, T riferimento) {
        return creaNodo(chiave, fontChiave, coloreChiave,
                null, null, null,
                descrizione, fontDescrizione, coloreDescrizione,
                icona, riferimento);
    }

    public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                         String valore, DoomdarkFont fontValore, DoomdarkColorModel.Color coloreValore,
                         String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                         Image icona, T riferimento) {
        Nodo nodo = new Nodo(chiave, fontChiave, coloreChiave,
                valore, fontValore, coloreValore,
                descrizione, fontDescrizione, coloreDescrizione,
                icona, 0, riferimento);
        nodi.add(nodo);
        return nodo;
    }

    public Nodo creaSeparatore() {
        return creaNodo(null, null, null,
                null, null, null,
                null, null, null,
                ImageCache.separatore, null);
    }

    private int calcolaAltezzaMassimaNodi() {
        return nodi.stream().mapToInt(Nodo::getAltezza).sum();
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

    public Image produci(int altezzaMassima, int offset) {

        List<Nodo> contenuto = espandiNodi();

        int altezzaImmagine = Math.max(calcolaAltezzaMassimaNodi(), altezzaMassima);
        offset = limitaOffset(altezzaImmagine, altezzaMassima, offset);

        BufferedImage risultato = new BufferedImage(larghezza, altezzaImmagine, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = risultato.createGraphics();

        int altezzaRaggiunta = 0;
        for (Nodo nodo : contenuto) {
            // Ogni riga viene disegnata alla propria posizione assoluta: si evita il
            // disegno di quelle che non intersecano la finestra ritagliata, non lo spazio
            // che occupano.
            int altezzaNodo = nodo.getAltezza();
            if (altezzaRaggiunta + altezzaNodo > offset && altezzaRaggiunta < offset + altezzaMassima) {
                int x = nodo.getIndentazione();
                if (nodo.icona != null) {
                    g2d.drawImage(nodo.icona, x, altezzaRaggiunta, null);
                    x += nodo.icona.getWidth(null) + ImageCache.SPACING;
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

//            if (altezzaRaggiunta > altezzaMassima) {
//                break;
//            }
        }

        int spacing = ImageCache.SPACING;
        if (offset > 0) {
            Image frecciaSu = ImageCache.componenteScorrevoleFrecciaSu;
            g2d.drawImage(frecciaSu, larghezza - frecciaSu.getWidth(null) - spacing, offset + spacing,null);
        }
        if (altezzaRaggiunta - offset > altezzaMassima) {
            Image frecciaGiu = ImageCache.componenteScorrevoleFrecciaGiu;
            g2d.drawImage(frecciaGiu,
                    larghezza - frecciaGiu.getWidth(null) - spacing,
                    offset + altezzaMassima - spacing - frecciaGiu.getHeight(null),
                    null);
        }

        g2d.dispose();
        return risultato.getSubimage(0, offset, larghezza, altezzaMassima);
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
     * Riporta il riferimento del nodo il cui titolo occupa la quota indicata, oppure null
     * se a quella quota non c'è il titolo di un nodo (spazio vuoto, o riga di descrizione).
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
            int altezzaNodo = nodo.getAltezza();
            if (quota < altezzaRaggiunta + altezzaNodo) {
                return nodo.riferimento;
            }
            altezzaRaggiunta += altezzaNodo;
        }
        return null;
    }

    public class Nodo {
        private final Image icona;
        private final Image chiave;
        private final Image valore;
        private final Image descrizione;

        private final int indentazione;
        // Indentazione effettiva di testo/descrizione: se è presente un'immagine,
        // slitta fissa verso destra di (larghezza immagine + ImageCache.SPACING).

        private final T riferimento;
        private final List<Nodo> figli = new ArrayList<>();
        private boolean figliVisibili = true;

        Nodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
             String valore, DoomdarkFont fontValore, DoomdarkColorModel.Color coloreValore,
             String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
             Image immagineAccompagnamentoSinistra, int indentazione, T riferimento) {
            this.riferimento = riferimento;
            this.icona = immagineAccompagnamentoSinistra;
            this.indentazione = indentazione;

            int larghezzaMassimaChiave = larghezza - indentazione;
            if (immagineAccompagnamentoSinistra != null) {
                larghezzaMassimaChiave = larghezzaMassimaChiave - immagineAccompagnamentoSinistra.getWidth(null) - ImageCache.SPACING;
            }

            // La descrizione è indentata un livello in più
            int larghezzaMassimaDescrizione = larghezzaMassimaChiave - indentazione;

            // Se abbiamo anche un valore la larghezza massima per la chiave si riduce, ma per la descrizione rimane la stessa
            if (valore != null) {
                this.valore = ImageCache.get(valore, fontValore, coloreValore);
                larghezzaMassimaChiave = larghezzaMassimaChiave - this.valore.getWidth(null) - ImageCache.SPACING;
            } else {
                this.valore = null;
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
        }

        private Image creaImmagine(DoomdarkFont font, DoomdarkColorModel.Color color, String testo, int larghezza) {
            List<String> testi = FontTool.split(font, testo, larghezza);
            BufferedImage canvas = new BufferedImage(larghezza, (font.getHeight() + interlinea) * testi.size(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = canvas.createGraphics();
            int y = 0;
            for (String s : testi) {
                Image image = DoomdarkTextProducer.getImage(s, font, color);
                g2d.drawImage(image, 0, y, null);
                y += font.getHeight() + interlinea;
            }
            g2d.dispose();
            return canvas;
        }

        public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                             String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                             Image immagineAccompagnamentoSinistra, T riferimento) {
            return creaNodo(chiave, fontChiave, coloreChiave,
                    null, null, null,
                    descrizione, fontDescrizione, coloreDescrizione,
                    immagineAccompagnamentoSinistra, riferimento);
        }

        public Nodo creaNodo(String chiave, DoomdarkFont fontChiave, DoomdarkColorModel.Color coloreChiave,
                             String valore, DoomdarkFont fontValore, DoomdarkColorModel.Color coloreValore,
                             String descrizione, DoomdarkFont fontDescrizione, DoomdarkColorModel.Color coloreDescrizione,
                             Image immagineAccompagnamentoSinistra, T riferimento) {
            Nodo nodo = new Nodo(chiave, fontChiave, coloreChiave,
                    valore, fontValore, coloreValore,
                    descrizione, fontDescrizione, coloreDescrizione,
                    immagineAccompagnamentoSinistra, this.indentazione + larghezzaIndentazione, riferimento);
            figli.add(nodo);
            return nodo;
        }

        public int getAltezza() {
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
                altezza += interlinea + figli.stream().mapToInt(Nodo::getAltezza).sum();
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
    }
}
