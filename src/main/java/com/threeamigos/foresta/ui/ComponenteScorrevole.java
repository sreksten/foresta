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
        Nodo nodo = new Nodo(testo, doomdarkFontTesto, descrizione, doomdarkFontDescrizione, 0, riferimento);
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
        return limitaOffset(altezzaImmagine(espandiNodi(), altezzaMassima), altezzaMassima, offset);
    }

    public Image produci(int altezzaMassima, int offset) {

        List<TestoDoomdark> nodiEspansi = espandiNodi();

        int altezzaImmagine = altezzaImmagine(nodiEspansi, altezzaMassima);
        offset = limitaOffset(altezzaImmagine, altezzaMassima, offset);

        BufferedImage risultato = new BufferedImage(larghezza, altezzaImmagine, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = risultato.createGraphics();

        int altezzaRaggiunta = 0;
        for (TestoDoomdark testoDoomdark : nodiEspansi) {
            // Ogni riga viene disegnata alla propria posizione assoluta: si evita il
            // disegno di quelle che non intersecano la finestra ritagliata, non lo spazio
            // che occupano.
            if (altezzaRaggiunta + testoDoomdark.altezza > offset && altezzaRaggiunta < offset + altezzaMassima) {
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

    private List<TestoDoomdark> espandiNodi() {
        List<TestoDoomdark> listaRisultante = new ArrayList<>();
        // L'alternanza dei colori scandisce i soli nodi radice: figli e nipoti
        // mantengono il colore del proprio nodo padre.
        DoomdarkColorAlternante coloreAlternante = new DoomdarkColorAlternante();
        for (Nodo nodo : nodi) {
            addNodo(nodo, listaRisultante, coloreAlternante.getColor());
        }
        return listaRisultante;
    }

    private void addNodo(Nodo nodo, List<TestoDoomdark> listaRisultante, DoomdarkColorModel.Color colore) {
        // Il colore imposto sul nodo vale per il solo nodo: i figli continuano a
        // ereditare quello del proprio ramo, e decidono a loro volta se sovrascriverlo.
        DoomdarkColorModel.Color coloreNodo = nodo.colore != null ? nodo.colore : colore;
        for (String s : nodo.testo) {
            listaRisultante.add(new TestoDoomdark(s, nodo.doomdarkFontTesto, nodo.indentazione, coloreNodo, nodo.riferimento, true));
        }
        if (nodo.isFigliVisibili()) {
            // La descrizione è indentata come i nodi figli
            int indentazioneDescrizione = nodo.indentazione + larghezzaIndentazione;
            for (String s : nodo.descrizione) {
                listaRisultante.add(new TestoDoomdark(s, nodo.doomdarkFontDescrizione, indentazioneDescrizione, coloreNodo, nodo.riferimento, false));
            }
            for (Nodo figlio : nodo.figli) {
                addNodo(figlio, listaRisultante, colore);
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
        for (TestoDoomdark testoDoomdark : espandiNodi()) {
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
        private final int indentazione;
        private final T riferimento;
        private final List<Nodo> figli = new ArrayList<>();
        private boolean figliVisibili = true;
        // Se valorizzato, prevale sul colore ereditato dal ramo
        private DoomdarkColorModel.Color colore;

        Nodo(String testoOriginale, DoomdarkFont doomdarkFontTestoOriginale,
             String descrizioneOriginale, DoomdarkFont doomdarkFontDescrizioneOriginale,
             int indentazione, T riferimento) {
            this.riferimento = riferimento;
            // Il testo va spezzato sulla larghezza effettivamente disponibile, che
            // l'indentazione riduce. La descrizione è indentata di un livello in più.
            int larghezzaDisponibile = larghezza - indentazione;
            this.doomdarkFontTesto = doomdarkFontTestoOriginale;
            this.testo = FontTool.split(this.doomdarkFontTesto, testoOriginale, larghezzaDisponibile);
            this.doomdarkFontDescrizione = doomdarkFontDescrizioneOriginale;
            this.descrizione = FontTool.split(this.doomdarkFontDescrizione, descrizioneOriginale, larghezzaDisponibile - larghezzaIndentazione);
            this.indentazione = indentazione;
        }

        public Nodo creaNodo(String testo, DoomdarkFont doomdarkFontTesto,
                             String descrizione, DoomdarkFont doomdarkFontDescrizione, T riferimento) {
            Nodo nodo = new Nodo(testo, doomdarkFontTesto, descrizione, doomdarkFontDescrizione,
                    this.indentazione + larghezzaIndentazione, riferimento);
            figli.add(nodo);
            return nodo;
        }

        public int getAltezza() {
            int altezza = this.testo.size() * doomdarkFontTesto.getHeight() + interlinea;
            if (figliVisibili) {
                altezza += this.descrizione.size() * doomdarkFontDescrizione.getHeight() + interlinea;
                altezza+= figli.stream().mapToInt(Nodo::getAltezza).sum();
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

        @Override
        public String toString() {
            return testo;
        }
    }
}
