package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.ui.testo.CaratteriDelFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;
import com.threeamigos.foresta.ui.testo.DoomdarkFontSmall;
import com.threeamigos.foresta.ui.testo.ImageBasedDoomdarkFont;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Strumento di sviluppo: scrive i font Doomdark piccolo e medio come immagini GIF, da cui blittare le scritte da un
 * atlante. Tre righe: le maiuscole, le minuscole e tutti gli altri caratteri. Ogni carattere sta in un riquadro rosso
 * (le linee della griglia sono condivise fra riquadri vicini), tutti della stessa misura: larghi come il carattere
 * più largo del font più EXTRA_LARGHEZZA (per lettere come æ e œ), alti come il font più le righe in più in cima (per
 * gli accenti) e in fondo (per cediglie, virgole sotto, ogonek): i glifi del font stanno fra le une e le altre. I glifi sono bianchi su nero e allineati a sinistra,
 * quindi la larghezza di un glifo è la posizione del suo pixel acceso più a destra, più uno.
 * <p>
 * Le righe comprendono anche le lettere delle lingue europee con alfabeto latino (francese, tedesco, norvegese,
 * islandese, spagnolo, portoghese, polacco, ceco, slovacco, croato, sloveno, ungherese, rumeno, turco, lettone,
 * lituano, maltese, gallese, sami: vedi MAIUSCOLE e MINUSCOLE) e i segni di interpunzione che ne vengono (vedi ALTRI),
 * che i font non hanno ancora: sono disegnate con Arial, in rosso, come segnaposto da ridisegnare.
 * Un segnaposto lascia un pixel nero attorno, per non confondersi con il riquadro, e riempie il resto del riquadro:
 * ogni lettera è ingrandita (o ridotta) a sé, con le sue proporzioni, finché non tocca il margine in larghezza o in
 * altezza, allineata in alto a sinistra; non hanno quindi né lo stesso corpo né la stessa linea di base, e vanno
 * ridisegnati. I segni della terza riga, invece, hanno tutti la stessa scala e la stessa linea di base (altrimenti un
 * punto diventerebbe un quadrato pieno e un trattino salirebbe in cima).
 * Per leggerli meglio, {@code doomdark_riferimento_arial.png} ha le stesse tre righe tutte in Arial, in riquadri 30x30.
 * <p>
 * Uso, dalla radice del progetto dopo {@code mvn test-compile}:
 * <pre>
 *     java -cp ... com.threeamigos.foresta.ui.GeneratoreImmagineDeiFont [CARTELLA]
 * </pre>
 * La cartella predefinita è {@code risorse_e_documenti_vari}; scrive {@code doomdark_small.gif},
 * {@code doomdark_medium.gif} e {@code doomdark_riferimento_arial.png}. Alla fine rilegge le immagini e stampa dove la larghezza dichiarata dal font non
 * coincide con quella misurata sui pixel.
 */
public class GeneratoreImmagineDeiFont {

    // L'elenco dei caratteri è quello che usa il lettore delle immagini (ImmagineDelFont): lo dice la posizione
    private static final String MAIUSCOLE = CaratteriDelFont.MAIUSCOLE;
    private static final String MINUSCOLE = CaratteriDelFont.MINUSCOLE;
    private static final String ALTRI = CaratteriDelFont.ALTRI;

    /** Le colonne in più a destra in ogni riquadro, per le lettere più larghe del font (æ, œ) */
    static final int EXTRA_LARGHEZZA = 4;

    /** Il lato dei riquadri della tabella di riferimento in Arial */
    static final int LATO_RIFERIMENTO = 30;

    private static final int NERO = 0;
    private static final int BIANCO = 1;
    private static final int ROSSO = 2;

    private GeneratoreImmagineDeiFont() {
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");
        File cartella = new File(args.length > 0 ? args[0] : "risorse_e_documenti_vari");
        scrivi(DoomdarkFontSmall.getInstance(), new File(cartella, "doomdark_small.gif"), ImageBasedDoomdarkFont.EXTRA_IN_CIMA_PICCOLO, ImageBasedDoomdarkFont.EXTRA_IN_FONDO_PICCOLO);
        scrivi(DoomdarkFontMedium.getInstance(), new File(cartella, "doomdark_medium.gif"), ImageBasedDoomdarkFont.EXTRA_IN_CIMA_MEDIO, ImageBasedDoomdarkFont.EXTRA_IN_FONDO_MEDIO);
        scriviRiferimento(new File(cartella, "doomdark_riferimento_arial.png"));
    }

    /** La larghezza dei riquadri: la più larga fra i caratteri che il font ha */
    static int larghezzaMassima(DoomdarkFont font) {
        int massima = 0;
        for (String riga : new String[]{MAIUSCOLE, MINUSCOLE, ALTRI}) {
            for (char c : riga.toCharArray()) {
                if (font.supportaIlCarattere(c)) {
                    massima = Math.max(massima, font.getGlyphWidth(c));
                }
            }
        }
        return massima;
    }

    static void scrivi(DoomdarkFont font, File file, int extraInCima, int extraInFondo) throws IOException {
        String[] righe = {MAIUSCOLE, MINUSCOLE, ALTRI};
        int larghezza = larghezzaMassima(font) + EXTRA_LARGHEZZA;
        int altezza = font.getHeight() + extraInCima + extraInFondo;
        int colonne = 0;
        for (String riga : righe) {
            colonne = Math.max(colonne, riga.length());
        }
        int passoX = larghezza + 1;
        int passoY = altezza + 1;
        BufferedImage immagine = nuovaImmagine(colonne * passoX + 1, righe.length * passoY + 1);

        List<Character> mancanti = new ArrayList<>();
        Font arial = new Font("Arial", Font.PLAIN, CORPO_DI_PARTENZA);
        for (String riga : righe) {
            for (char c : riga.toCharArray()) {
                if (!font.supportaIlCarattere(c)) {
                    mancanti.add(c);
                    if (!arial.canDisplay(c)) {
                        System.out.println("  ATTENZIONE: Arial non ha '" + c + "' (U+" + Integer.toHexString(c).toUpperCase(Locale.ROOT) + ")");
                    }
                }
            }
        }

        SegniInComune segni = new SegniInComune(ALTRI, larghezza - 2, altezza - 2);
        for (int r = 0; r < righe.length; r++) {
            for (int c = 0; c < righe[r].length(); c++) {
                char carattere = righe[r].charAt(c);
                int x0 = c * passoX;
                int y0 = r * passoY;
                disegnaRiquadro(immagine, x0, y0, passoX, passoY);
                if (font.supportaIlCarattere(carattere)) {
                    disegnaGlifo(immagine, font, carattere, x0 + 1, y0 + 1 + extraInCima);
                } else {
                    disegnaSegnaposto(immagine, carattere, x0 + 1, y0 + 1, larghezza, altezza, r == 2 ? segni : null);
                }
            }
        }
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        ImageIO.write(immagine, "gif", file);
        System.out.println(file + ": " + immagine.getWidth() + "x" + immagine.getHeight() + ", riquadri " + larghezza + "x" + altezza
                + " (" + font.getHeight() + " del font, " + extraInCima + " in più in cima e " + extraInFondo + " in fondo; larghezza massima "
                + larghezzaMassima(font) + ", " + EXTRA_LARGHEZZA + " in più), " + mancanti.size() + " segnaposto in Arial");
        verifica(font, file, righe, larghezza, altezza, extraInCima);
    }

    private static BufferedImage nuovaImmagine(int larghezza, int altezza) {
        byte[] r = {0, (byte) 255, (byte) 255};
        byte[] g = {0, (byte) 255, 0};
        byte[] b = {0, (byte) 255, 0};
        return new BufferedImage(larghezza, altezza, BufferedImage.TYPE_BYTE_INDEXED, new IndexColorModel(2, 3, r, g, b));
    }

    private static void punto(BufferedImage immagine, int x, int y, int colore) {
        immagine.getRaster().setSample(x, y, 0, colore);
    }

    private static void disegnaRiquadro(BufferedImage immagine, int x0, int y0, int passoX, int passoY) {
        for (int x = x0; x <= x0 + passoX; x++) {
            punto(immagine, x, y0, ROSSO);
            punto(immagine, x, y0 + passoY, ROSSO);
        }
        for (int y = y0; y <= y0 + passoY; y++) {
            punto(immagine, x0, y, ROSSO);
            punto(immagine, x0 + passoX, y, ROSSO);
        }
    }

    /** I bit del glifo, quanti ne usa il font per disegnarlo (la larghezza dichiarata) */
    private static void disegnaGlifo(BufferedImage immagine, DoomdarkFont font, char c, int x0, int y0) {
        byte[] dati = font.getGlyphData(c);
        int larghezzaDelGlifo = dati[0];
        int byteDiUnaRiga = font.getDataWidthInBytes();
        for (int bit = 0; bit < larghezzaDelGlifo; bit++) {
            for (int riga = 0; riga < font.getHeight(); riga++) {
                if (((dati[riga * byteDiUnaRiga + 1 + (bit >> 3)] >> (7 - (bit & 0b111))) & 1) != 0) {
                    punto(immagine, x0 + bit, y0 + riga, BIANCO);
                }
            }
        }
    }

    private static final FontRenderContext CONTESTO = new FontRenderContext(null, false, false);
    /** Il corpo con cui si disegna un segnaposto prima di ridurlo: grande, per avere i dettagli degli accenti */
    private static final int CORPO_DI_PARTENZA = 200;
    /** Un pixel del segnaposto è acceso se almeno questa parte del suo quadrato, nel disegno grande, è coperta */
    private static final double COPERTURA_MINIMA = 0.35d;

    /**
     * La scala e la linea di base comuni a un gruppo di segnaposto da tenere fra loro in proporzione (i segni di
     * interpunzione): la scala più piccola fra quelle che farebbero riempire a ciascuno l'area, e la linea di base per
     * cui il segno più alto tocca il margine in alto.
     */
    private static final class SegniInComune {
        final double scala;
        final int lineaDiBase;

        SegniInComune(String caratteri, int areaLarghezza, int areaAltezza) {
            Font arial = new Font("Arial", Font.PLAIN, CORPO_DI_PARTENZA);
            double minima = Double.MAX_VALUE;
            double sopra = 0;
            double sotto = 0;
            for (char c : caratteri.toCharArray()) {
                Rectangle2D limiti = arial.createGlyphVector(CONTESTO, String.valueOf(c)).getVisualBounds();
                if (limiti.getWidth() > 0) {
                    minima = Math.min(minima, areaLarghezza / limiti.getWidth());
                }
                sopra = Math.min(sopra, limiti.getMinY());
                sotto = Math.max(sotto, limiti.getMaxY());
            }
            // Tutti insieme, dal segno più alto a quello che scende di più, devono stare nell'altezza
            scala = Math.min(minima, areaAltezza / (sotto - sopra));
            lineaDiBase = 1 + (int) Math.ceil(-sopra * scala);
        }
    }

    /**
     * Il segnaposto in rosso: il glifo di Arial, ridotto. Senza {@code inComune} riempie il riquadro (che parte da
     * (x0, y0) ed è largo e alto come dato) meno un pixel di margine, con le sue proporzioni, in alto a sinistra; con
     * {@code inComune} ha la scala e la linea di base del gruppo.
     */
    private static void disegnaSegnaposto(BufferedImage immagine, char c, int x0, int y0, int larghezza, int altezza,
                                          SegniInComune inComune) {
        int areaLarghezza = larghezza - 2;
        int areaAltezza = altezza - 2;
        Font arial = new Font("Arial", Font.PLAIN, CORPO_DI_PARTENZA);
        Rectangle2D limiti = arial.createGlyphVector(CONTESTO, String.valueOf(c)).getVisualBounds();
        int sorgenteLarghezza = (int) Math.ceil(limiti.getWidth()) + 2;
        int sorgenteAltezza = (int) Math.ceil(limiti.getHeight()) + 2;
        BufferedImage grande = new BufferedImage(sorgenteLarghezza, sorgenteAltezza, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = grande.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setFont(arial);
            graphics.drawString(String.valueOf(c), (float) (1 - limiti.getMinX()), (float) (1 - limiti.getMinY()));
        } finally {
            graphics.dispose();
        }
        double scala = inComune != null ? inComune.scala
                : Math.min(areaLarghezza / limiti.getWidth(), areaAltezza / limiti.getHeight());
        int w = Math.max(1, Math.min(areaLarghezza, (int) Math.round(limiti.getWidth() * scala)));
        int h = Math.max(1, Math.min(areaAltezza, (int) Math.round(limiti.getHeight() * scala)));
        // In alto: sul margine, o con la linea di base del gruppo
        int cima = inComune != null ? inComune.lineaDiBase + (int) Math.round(limiti.getMinY() * scala) : 1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int daX = 1 + (int) Math.floor(x * limiti.getWidth() / w);
                int aX = Math.max(daX + 1, 1 + (int) Math.ceil((x + 1) * limiti.getWidth() / w));
                int daY = 1 + (int) Math.floor(y * limiti.getHeight() / h);
                int aY = Math.max(daY + 1, 1 + (int) Math.ceil((y + 1) * limiti.getHeight() / h));
                double coperto = 0;
                int quanti = 0;
                for (int sy = daY; sy < aY && sy < sorgenteAltezza; sy++) {
                    for (int sx = daX; sx < aX && sx < sorgenteLarghezza; sx++) {
                        coperto += (grande.getRGB(sx, sy) >>> 24) / 255.0d;
                        quanti++;
                    }
                }
                int destinazioneY = cima + y;
                if (quanti > 0 && coperto / quanti >= COPERTURA_MINIMA && destinazioneY >= 1 && destinazioneY <= areaAltezza) {
                    punto(immagine, x0 + 1 + x, y0 + destinazioneY, ROSSO);
                }
            }
        }
    }

    /**
     * La stessa tabella di righe e colonne tutta in Arial (anche i caratteri che i font hanno), bianca su nero, in
     * riquadri di LATO_RIFERIMENTO pixel con la griglia in grigio: serve a leggere cosa c'è in ogni posto.
     */
    static void scriviRiferimento(File file) throws IOException {
        String[] righe = {MAIUSCOLE, MINUSCOLE, ALTRI};
        int colonne = 0;
        for (String riga : righe) {
            colonne = Math.max(colonne, riga.length());
        }
        int passo = LATO_RIFERIMENTO + 1;
        BufferedImage immagine = new BufferedImage(colonne * passo + 1, righe.length * passo + 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = immagine.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font arial = new Font("Arial", Font.PLAIN, LATO_RIFERIMENTO * 2 / 3);
            graphics.setFont(arial);
            // La stessa linea di base per tutti: l'accento più alto e la discendente più bassa stanno nel riquadro
            double sopra = 0;
            double sotto = 0;
            for (String riga : righe) {
                for (char c : riga.toCharArray()) {
                    Rectangle2D limiti = arial.createGlyphVector(CONTESTO, String.valueOf(c)).getVisualBounds();
                    sopra = Math.min(sopra, limiti.getMinY());
                    sotto = Math.max(sotto, limiti.getMaxY());
                }
            }
            int lineaDiBase = (int) Math.round((LATO_RIFERIMENTO - (sotto - sopra)) / 2.0d - sopra);
            graphics.setColor(new Color(0x606060));
            for (int r = 0; r <= righe.length; r++) {
                graphics.drawLine(0, r * passo, colonne * passo, r * passo);
            }
            for (int c = 0; c <= colonne; c++) {
                graphics.drawLine(c * passo, 0, c * passo, righe.length * passo);
            }
            graphics.setColor(Color.WHITE);
            for (int r = 0; r < righe.length; r++) {
                for (int c = 0; c < righe[r].length(); c++) {
                    String carattere = String.valueOf(righe[r].charAt(c));
                    Rectangle2D limiti = arial.createGlyphVector(CONTESTO, carattere).getVisualBounds();
                    double x = c * passo + 1 + (LATO_RIFERIMENTO - limiti.getWidth()) / 2.0d - limiti.getMinX();
                    graphics.drawString(carattere, (float) x, r * passo + 1 + lineaDiBase);
                }
            }
        } finally {
            graphics.dispose();
        }
        ImageIO.write(immagine, "png", file);
        System.out.println(file + ": " + immagine.getWidth() + "x" + immagine.getHeight() + ", riquadri " + LATO_RIFERIMENTO + "x" + LATO_RIFERIMENTO);
    }

    /**
     * Rilegge l'immagine: i bit di ogni glifo devono essere quelli del font, e la larghezza misurata sui pixel (il
     * pixel bianco più a destra, più uno) va confrontata con quella dichiarata dal font.
     */
    private static void verifica(DoomdarkFont font, File file, String[] righe, int larghezza, int altezza, int extraInCima) throws IOException {
        BufferedImage letta = ImageIO.read(file);
        int passoX = larghezza + 1;
        int passoY = altezza + 1;
        int diversi = 0;
        List<String> larghezzeDiverse = new ArrayList<>();
        for (int r = 0; r < righe.length; r++) {
            for (int c = 0; c < righe[r].length(); c++) {
                char carattere = righe[r].charAt(c);
                if (!font.supportaIlCarattere(carattere)) {
                    continue;
                }
                byte[] dati = font.getGlyphData(carattere);
                int dichiarata = dati[0];
                int misurata = 0;
                for (int bit = 0; bit < larghezza; bit++) {
                    for (int riga = 0; riga < altezza; riga++) {
                        int rgb = letta.getRGB(c * passoX + 1 + bit, r * passoY + 1 + riga) & 0xFFFFFF;
                        boolean acceso = rgb == 0xFFFFFF;
                        int rigaDelFont = riga - extraInCima;
                        boolean atteso = rigaDelFont >= 0 && rigaDelFont < font.getHeight() && bit < dichiarata
                                && ((dati[rigaDelFont * font.getDataWidthInBytes() + 1 + (bit >> 3)] >> (7 - (bit & 0b111))) & 1) != 0;
                        if (acceso != atteso) {
                            diversi++;
                        }
                        if (acceso) {
                            misurata = Math.max(misurata, bit + 1);
                        }
                    }
                }
                if (misurata != dichiarata) {
                    larghezzeDiverse.add("'" + carattere + "' dichiarata " + dichiarata + ", misurata " + misurata);
                }
            }
        }
        System.out.println("  pixel diversi dal font: " + diversi);
        System.out.println("  larghezze dichiarate diverse da quelle misurate: " + (larghezzeDiverse.isEmpty() ? "nessuna" : larghezzeDiverse));
    }
}
