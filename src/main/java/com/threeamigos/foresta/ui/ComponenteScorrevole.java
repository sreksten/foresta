package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefano Reksten
 */
public class ComponenteScorrevole {

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
                         String descrizione, DoomdarkFont doomdarkFontDescrizione) {
        Nodo nodo = new Nodo(testo, doomdarkFontTesto, descrizione, doomdarkFontDescrizione, 0);
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

        DoomdarkColorAlternante colore = new DoomdarkColorAlternante();

        BufferedImage risultato = new BufferedImage(larghezza, altezzaImmagine, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = risultato.createGraphics();

        int altezzaRaggiunta = 0;
        for (TestoDoomdark testoDoomdark : nodiEspansi) {
            // Il colore va consumato per ogni riga, anche per quelle fuori dalla finestra,
            // altrimenti l'alternanza si inverte man mano che si scorre.
            DoomdarkColorModel.Color coloreRiga = colore.getColor();
            // Ogni riga viene disegnata alla propria posizione assoluta: si evita il
            // disegno di quelle che non intersecano la finestra ritagliata, non lo spazio
            // che occupano.
            if (altezzaRaggiunta + testoDoomdark.altezza > offset && altezzaRaggiunta < offset + altezzaMassima) {
                Image image = DoomdarkTextProducer.getImage(testoDoomdark.testo, testoDoomdark.doomdarkFont,
                        coloreRiga, larghezza - testoDoomdark.indentazione);
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
        addNodi(nodi, listaRisultante);
        return listaRisultante;
    }

    private void addNodi(List<Nodo> nodiDaEspandere, List<TestoDoomdark> listaRisultante) {
        for (Nodo nodo : nodiDaEspandere) {
            for (String s : nodo.testo) {
                listaRisultante.add(new TestoDoomdark(s, nodo.doomdarkFontTesto, nodo.indentazione));
            }
            if (nodo.isFigliVisibili()) {
                for (String s : nodo.descrizione) {
                    listaRisultante.add(new TestoDoomdark(s, nodo.doomdarkFontDescrizione, nodo.indentazione));
                }
                addNodi(nodo.figli, listaRisultante);
            }
        }
    }

    public class Nodo {
        private final DoomdarkFont doomdarkFontTesto;
        private final List<String> testo;
        private final DoomdarkFont doomdarkFontDescrizione;
        private final List<String> descrizione;
        private final int indentazione;
        private final List<Nodo> figli = new ArrayList<>();
        private boolean figliVisibili = true;

        Nodo(String testoOriginale, DoomdarkFont doomdarkFontTestoOriginale,
             String descrizioneOriginale, DoomdarkFont doomdarkFontDescrizioneOriginale,
             int indentazione) {
            // Il testo va spezzato sulla larghezza effettivamente disponibile, che
            // l'indentazione riduce.
            int larghezzaDisponibile = larghezza - indentazione;
            this.doomdarkFontTesto = doomdarkFontTestoOriginale;
            this.testo = FontTool.split(this.doomdarkFontTesto, testoOriginale, larghezzaDisponibile);
            this.doomdarkFontDescrizione = doomdarkFontDescrizioneOriginale;
            this.descrizione = FontTool.split(this.doomdarkFontDescrizione, descrizioneOriginale, larghezzaDisponibile);
            this.indentazione = indentazione;
        }

        public Nodo creaNodo(String testo, DoomdarkFont doomdarkFontTesto,
                             String descrizione, DoomdarkFont doomdarkFontDescrizione) {
            Nodo nodo = new Nodo(testo, doomdarkFontTesto, descrizione, doomdarkFontDescrizione, this.indentazione + larghezzaIndentazione);
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

        public TestoDoomdark(String testo, DoomdarkFont doomdarkFont, int indentazione) {
            this.testo = testo;
            this.doomdarkFont = doomdarkFont;
            this.altezza = doomdarkFont.getHeight() + interlinea;
            this.indentazione = indentazione;
        }

        @Override
        public String toString() {
            return testo;
        }
    }

    private class DoomdarkColorAlternante {

        private DoomdarkColorModel.Color color = DoomdarkColorModel.Color.LIGHT_GRAY;

        public DoomdarkColorModel.Color getColor() {
            color = color == DoomdarkColorModel.Color.LIGHT_GRAY ? DoomdarkColorModel.Color.DARK_GRAY : DoomdarkColorModel.Color.LIGHT_GRAY;
            return color;
        }
    }
}
