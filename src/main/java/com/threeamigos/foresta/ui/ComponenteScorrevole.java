package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Elenco ad albero scorrevole. Ogni nodo può portarsi dietro un riferimento
 * all'oggetto che rappresenta, per poter risalire dalla posizione di un click
 * a quell'oggetto.
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

    public Nodo creaNodo(String testo, DoomdarkFont doomdarkFontTesto,
                         String descrizione, DoomdarkFont doomdarkFontDescrizione, T riferimento) {
        return creaNodo(testo, doomdarkFontTesto, descrizione, doomdarkFontDescrizione, null, riferimento);
    }

    public Nodo creaNodo(String testo, DoomdarkFont doomdarkFontTesto,
                         String descrizione, DoomdarkFont doomdarkFontDescrizione,
                         BufferedImage immagine, T riferimento) {
        Nodo nodo = new Nodo(testo, doomdarkFontTesto, descrizione, doomdarkFontDescrizione, immagine, 0, riferimento);
        nodi.add(nodo);
        return nodo;
    }

    public void aggiungi(Nodo nodo) {
        nodi.add(nodo);
    }

    /**
     * Riporta l'offset di scorrimento entro i limiti della lista: non si scorre
     * sopra la prima riga né oltre l'ultima.
     */
    public int limitaOffset(int altezzaMassima, int offset) {
        return limitaOffset(altezzaImmagine(espandiNodi().testi, altezzaMassima), altezzaMassima, offset);
    }

    public Image produci(int altezzaMassima, int offset) {

        Contenuto contenuto = espandiNodi();
        List<TestoDoomdark> nodiEspansi = contenuto.testi;

        int altezzaImmagine = altezzaImmagine(nodiEspansi, altezzaMassima);
        offset = limitaOffset(altezzaImmagine, altezzaMassima, offset);

        BufferedImage risultato = new BufferedImage(larghezza, altezzaImmagine, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = risultato.createGraphics();

        for (ImmagineDoomdark immagineDoomdark : contenuto.immagini) {
            // Stessa logica di clipping delle righe di testo: si evita di disegnare
            // le immagini dei nodi che non intersecano la finestra ritagliata.
            if (immagineDoomdark.altezza + immagineDoomdark.immagine.getHeight() > offset && immagineDoomdark.altezza < offset + altezzaMassima) {
                g2d.drawImage(immagineDoomdark.immagine, immagineDoomdark.indentazione, immagineDoomdark.altezza, null);
            }
        }

        int altezzaRaggiunta = 0;
        for (TestoDoomdark testoDoomdark : nodiEspansi) {
            // Ogni riga viene disegnata alla propria posizione assoluta: si evita il
            // disegno di quelle che non intersecano la finestra ritagliata, non lo spazio
            // che occupano.
            if (testoDoomdark.testo != null && altezzaRaggiunta + testoDoomdark.altezza > offset && altezzaRaggiunta < offset + altezzaMassima) {
                Image image = DoomdarkTextProducer.getImage(testoDoomdark.testo, testoDoomdark.doomdarkFont,
                        testoDoomdark.colore, larghezza - testoDoomdark.indentazione);
                g2d.drawImage(image, testoDoomdark.indentazione, altezzaRaggiunta, null);
            }
            altezzaRaggiunta += testoDoomdark.altezza;
        }
        g2d.dispose();
        return risultato.getSubimage(0, offset, larghezza, altezzaMassima);
    }

    /**
     * L'immagine deve contenere tutta la lista, ma non essere più bassa della finestra
     * ritagliata, altrimenti getSubimage() esce dal raster.
     */
    private int altezzaImmagine(List<TestoDoomdark> nodiEspansi, int altezzaMassima) {
        return Math.max(nodiEspansi.stream().mapToInt(td -> td.altezza).sum(), altezzaMassima);
    }

    private int limitaOffset(int altezzaImmagine, int altezzaMassima, int offset) {
        return Math.max(0, Math.min(offset, altezzaImmagine - altezzaMassima));
    }

    private Contenuto espandiNodi() {
        List<TestoDoomdark> listaRisultante = new ArrayList<>();
        List<ImmagineDoomdark> immaginiRisultante = new ArrayList<>();
        // L'alternanza dei colori scandisce i soli nodi radice: figli e nipoti
        // mantengono il colore del proprio nodo padre.
        DoomdarkColorAlternante coloreAlternante = new DoomdarkColorAlternante();
        // Accumulatore mutabile: serve a conoscere la quota assoluta a cui inizia
        // il titolo di ciascun nodo, per posizionarne l'eventuale immagine.
        int[] altezzaAccumulata = {0};
        for (Nodo nodo : nodi) {
            addNodo(nodo, listaRisultante, immaginiRisultante, altezzaAccumulata, coloreAlternante.getColor());
        }
        return new Contenuto(listaRisultante, immaginiRisultante);
    }

    private void addNodo(Nodo nodo, List<TestoDoomdark> listaRisultante, List<ImmagineDoomdark> immaginiRisultante,
                          int[] altezzaAccumulata, DoomdarkColorModel.Color colore) {
        // Il colore imposto sul nodo vale per il solo nodo: i figli continuano a
        // ereditare quello del proprio ramo, e decidono a loro volta se sovrascriverlo.
        DoomdarkColorModel.Color coloreNodo = nodo.colore != null ? nodo.colore : colore;
        int altezzaInizioNodo = altezzaAccumulata[0];
        if (nodo.immagine != null) {
            immaginiRisultante.add(new ImmagineDoomdark(nodo.immagine, nodo.indentazione, altezzaInizioNodo));
        }
        for (String s : nodo.testo) {
            TestoDoomdark testoDoomdark = new TestoDoomdark(s, nodo.doomdarkFontTesto, nodo.indentazioneTesto, coloreNodo, nodo.riferimento, true);
            listaRisultante.add(testoDoomdark);
            altezzaAccumulata[0] += testoDoomdark.altezza;
        }
        if (nodo.isFigliVisibili()) {
            // La descrizione è indentata come i nodi figli
            int indentazioneDescrizione = nodo.indentazioneTesto + larghezzaIndentazione;
            for (String s : nodo.descrizione) {
                TestoDoomdark testoDoomdark = new TestoDoomdark(s, nodo.doomdarkFontDescrizione, indentazioneDescrizione, coloreNodo, nodo.riferimento, false);
                listaRisultante.add(testoDoomdark);
                altezzaAccumulata[0] += testoDoomdark.altezza;
            }
        }
        if (nodo.immagine != null) {
            // Se l'immagine è più alta del testo/descrizione del nodo, deve determinare
            // lei l'altezza del pezzetto: si aggiunge uno spaziatore per non far
            // sovrapporre il contenuto successivo (figli o nodo seguente).
            int altezzaPezzetto = altezzaAccumulata[0] - altezzaInizioNodo;
            int spazioMancante = nodo.immagine.getHeight() - altezzaPezzetto;
            if (spazioMancante > 0) {
                listaRisultante.add(new TestoDoomdark(spazioMancante));
                altezzaAccumulata[0] += spazioMancante;
            }
        }
        if (nodo.isFigliVisibili()) {
            for (Nodo figlio : nodo.figli) {
                addNodo(figlio, listaRisultante, immaginiRisultante, altezzaAccumulata, colore);
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
        for (TestoDoomdark testoDoomdark : espandiNodi().testi) {
            if (quota < altezzaRaggiunta + testoDoomdark.altezza) {
                return testoDoomdark.titolo ? testoDoomdark.riferimento : null;
            }
            altezzaRaggiunta += testoDoomdark.altezza;
        }
        return null;
    }

    public class Nodo {
        private final DoomdarkFont doomdarkFontTesto;
        private final List<String> testo;
        private final DoomdarkFont doomdarkFontDescrizione;
        private final List<String> descrizione;
        private final BufferedImage immagine;
        private final int indentazione;
        // Indentazione effettiva di testo/descrizione: se è presente un'immagine,
        // slitta fissa verso destra di (larghezza immagine + ImageCache.SPACING).
        private final int indentazioneTesto;
        private final T riferimento;
        private final List<Nodo> figli = new ArrayList<>();
        private boolean figliVisibili = true;
        // Se valorizzato, prevale sul colore ereditato dal ramo
        private DoomdarkColorModel.Color colore;

        Nodo(String testoOriginale, DoomdarkFont doomdarkFontTestoOriginale,
             String descrizioneOriginale, DoomdarkFont doomdarkFontDescrizioneOriginale,
             BufferedImage immagine, int indentazione, T riferimento) {
            this.riferimento = riferimento;
            this.immagine = immagine;
            this.indentazione = indentazione;
            int larghezzaImmagine = immagine != null ? immagine.getWidth() + ImageCache.SPACING : 0;
            this.indentazioneTesto = indentazione + larghezzaImmagine;
            // Il testo va spezzato sulla larghezza effettivamente disponibile, che
            // l'indentazione (e l'eventuale immagine) riducono. La descrizione è
            // indentata di un livello in più, e il wrapping resta fisso su questa
            // stessa X anche se il testo supera in altezza l'immagine.
            int larghezzaDisponibile = larghezza - indentazioneTesto;
            this.doomdarkFontTesto = doomdarkFontTestoOriginale;
            this.testo = FontTool.split(this.doomdarkFontTesto, testoOriginale, larghezzaDisponibile);
            this.doomdarkFontDescrizione = doomdarkFontDescrizioneOriginale;
            this.descrizione = FontTool.split(this.doomdarkFontDescrizione, descrizioneOriginale, larghezzaDisponibile - larghezzaIndentazione);
        }

        public Nodo creaNodo(String testo, DoomdarkFont doomdarkFontTesto,
                             String descrizione, DoomdarkFont doomdarkFontDescrizione, T riferimento) {
            return creaNodo(testo, doomdarkFontTesto, descrizione, doomdarkFontDescrizione, null, riferimento);
        }

        public Nodo creaNodo(String testo, DoomdarkFont doomdarkFontTesto,
                             String descrizione, DoomdarkFont doomdarkFontDescrizione,
                             BufferedImage immagine, T riferimento) {
            Nodo nodo = new Nodo(testo, doomdarkFontTesto, descrizione, doomdarkFontDescrizione, immagine,
                    this.indentazione + larghezzaIndentazione, riferimento);
            figli.add(nodo);
            return nodo;
        }

        public int getAltezza() {
            int altezzaPezzetto = this.testo.size() * doomdarkFontTesto.getHeight() + interlinea;
            if (figliVisibili) {
                altezzaPezzetto += this.descrizione.size() * doomdarkFontDescrizione.getHeight() + interlinea;
            }
            if (immagine != null) {
                altezzaPezzetto = Math.max(altezzaPezzetto, immagine.getHeight());
            }
            int altezza = altezzaPezzetto;
            if (figliVisibili) {
                altezza += figli.stream().mapToInt(Nodo::getAltezza).sum();
            }
            return altezza;
        }

        public int getIndentazione() {
            return indentazione;
        }

        public List<Nodo> getFigli() {
            return figli;
        }

        public void setColore(DoomdarkColorModel.Color colore) {
            this.colore = colore;
        }

        public void setFigliVisibili(boolean figliVisibili) {
            this.figliVisibili = figliVisibili;
        }

        public boolean isFigliVisibili() {
            return figliVisibili;
        }
    }

    private class TestoDoomdark {
        private final String testo;
        private final DoomdarkFont doomdarkFont;
        private final int altezza;
        private final int indentazione;
        private final DoomdarkColorModel.Color colore;
        private final T riferimento;
        // Distingue le righe del titolo del nodo da quelle della sua descrizione
        private final boolean titolo;

        public TestoDoomdark(String testo, DoomdarkFont doomdarkFont, int indentazione,
                             DoomdarkColorModel.Color colore, T riferimento, boolean titolo) {
            this.testo = testo;
            this.doomdarkFont = doomdarkFont;
            this.altezza = doomdarkFont.getHeight() + interlinea;
            this.indentazione = indentazione;
            this.colore = colore;
            this.riferimento = riferimento;
            this.titolo = titolo;
        }

        /**
         * Spaziatore senza testo, usato per far occupare a un'immagine più alta
         * del testo/descrizione tutto lo spazio verticale che le compete.
         */
        public TestoDoomdark(int altezza) {
            this.testo = null;
            this.doomdarkFont = null;
            this.altezza = altezza;
            this.indentazione = 0;
            this.colore = null;
            this.riferimento = null;
            this.titolo = false;
        }

        @Override
        public String toString() {
            return testo;
        }
    }

    private class ImmagineDoomdark {
        private final BufferedImage immagine;
        private final int indentazione;
        private final int altezza;

        ImmagineDoomdark(BufferedImage immagine, int indentazione, int altezza) {
            this.immagine = immagine;
            this.indentazione = indentazione;
            this.altezza = altezza;
        }
    }

    private class Contenuto {
        private final List<TestoDoomdark> testi;
        private final List<ImmagineDoomdark> immagini;

        Contenuto(List<TestoDoomdark> testi, List<ImmagineDoomdark> immagini) {
            this.testi = testi;
            this.immagini = immagini;
        }
    }
}
