package com.threeamigos.foresta.ui.sfx;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TracciatoreLogoTest {

    private static BufferedImage logo3AM;

    @BeforeAll
    static void caricaLogo() throws IOException {
        logo3AM = ImageIO.read(TracciatoreLogoTest.class.getResource("/com/threeamigos/foresta/img/Logo3AM.png"));
    }

    @Test
    void nelLogo3AMTrovaTreLettereEIlBucoDellaA() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).costruisci();
        assertTrue(effetto.getDiagnostica().get(0).startsWith("4 forma/e rilevate (3 contorni esterni, 1 buchi interni)"),
                effetto.getDiagnostica().get(0));
    }

    @Test
    void conAmbiVersiCeUnCorridoreOrarioEUnoAntiorarioPerOgniForma() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).ambiVersi(true).costruisci();
        List<String> corridori = effetto.getDiagnostica().stream().filter(d -> d.startsWith("Corridore:")).collect(Collectors.toList());
        assertEquals(8, corridori.size(), corridori.toString());
        assertEquals(4, corridori.stream().filter(d -> d.contains("verso orario")).count());
        assertEquals(4, corridori.stream().filter(d -> d.contains("verso antiorario")).count());
        // la coppia parte dallo stesso punto e ognuno fa lo stesso numero di passi, cosi' si toccano a meta' del contorno
        for (int i = 0; i < corridori.size(); i += 2) {
            assertEquals(punti(corridori.get(i)), punti(corridori.get(i + 1)), corridori.get(i) + " / " + corridori.get(i + 1));
            assertEquals(partenza(corridori.get(i)), partenza(corridori.get(i + 1)));
        }
    }

    @Test
    void conAmbiVersiIlTracciamentoDuraMezzoGiro() {
        int normale = fotogrammiDiTracciamento(TracciatoreLogo.costruttore(logo3AM).costruisci());
        int ambi = fotogrammiDiTracciamento(TracciatoreLogo.costruttore(logo3AM).ambiVersi(true).costruisci());
        // Ognuno dei due fa mezza forma: in circa la meta' del tempo il giro e' completo (poi la dissolvenza, come sempre)
        assertTrue(ambi < normale * 0.6, ambi + " contro " + normale);
        assertTrue(ambi > normale * 0.4, ambi + " contro " + normale);
    }

    @Test
    void conAmbiVersiTuttoIlCicloFinisce() {
        TracciatoreLogo ambi = TracciatoreLogo.costruttore(logo3AM).ambiVersi(true).costruisci();
        int fotogrammi = 0;
        while (!ambi.isFinito() && fotogrammi < 3 * ambi.stimaFotogrammiCiclo()) {
            ambi.avanza();
            fotogrammi++;
        }
        assertTrue(ambi.isFinito());
    }

    private static int fotogrammiDiTracciamento(TracciatoreLogo effetto) {
        int fotogrammi = 0;
        while ("TRACCIAMENTO".equals(effetto.getNomeFase()) && fotogrammi < 10 * effetto.stimaFotogrammiCiclo()) {
            effetto.avanza();
            fotogrammi++;
        }
        return fotogrammi;
    }

    private static int punti(String corridore) {
        Matcher m = Pattern.compile("(\\d+) punti").matcher(corridore);
        assertTrue(m.find(), corridore);
        return Integer.parseInt(m.group(1));
    }

    private static String partenza(String corridore) {
        Matcher m = Pattern.compile("partenza agganciata a (\\([^)]*\\))").matcher(corridore);
        assertTrue(m.find(), corridore);
        return m.group(1);
    }

    @Test
    void senzaRipetizioneFinisceDopoUnCiclo() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).costruisci();
        int fotogrammi = 0;
        while (!effetto.isFinito()) {
            effetto.avanza();
            fotogrammi++;
            assertTrue(fotogrammi <= effetto.stimaFotogrammiCiclo(), "non finisce entro la stima");
        }
        assertEquals("PAUSA", effetto.getNomeFase());
        effetto.avanza();
        assertTrue(effetto.isFinito(), "finito resta finito");

        effetto.ricomincia();
        assertFalse(effetto.isFinito());
        assertEquals("TRACCIAMENTO", effetto.getNomeFase());
    }

    @Test
    void conLaRipetizioneNonFinisceMai() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).ripeti(true).costruisci();
        for (int i = 0; i < 3 * effetto.stimaFotogrammiCiclo(); i++) {
            effetto.avanza();
            assertFalse(effetto.isFinito());
        }
    }

    @Test
    void siDisegnaSenzaSchermo() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).coloreSfondo(Color.BLACK).costruisci();
        BufferedImage fotogramma = new BufferedImage(effetto.getLarghezza(), effetto.getAltezza(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fotogramma.createGraphics();
        for (int i = 0; i < 20; i++) {
            effetto.avanza();
        }
        effetto.disegna(g2);
        g2.dispose();
        boolean sfondo = false, scia = false;
        for (int y = 0; y < fotogramma.getHeight(); y++) {
            for (int x = 0; x < fotogramma.getWidth(); x++) {
                int rgb = fotogramma.getRGB(x, y) & 0xFFFFFF;
                sfondo |= rgb == 0x000000;
                scia |= rgb == 0xFFFFFF;
            }
        }
        assertTrue(sfondo, "manca lo sfondo nero");
        assertTrue(scia, "manca la scia bianca");
    }

    @Test
    void conLeScieSfumateInPausaSiVedeSoloIlLogo() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).sfumaScie(true).fotogrammiScomparsa(40).costruisci();
        while (!"PAUSA".equals(effetto.getNomeFase())) {
            effetto.avanza();
        }
        BufferedImage atteso = new BufferedImage(logo3AM.getWidth(), logo3AM.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atteso.createGraphics();
        g.setColor(TracciatoreLogo.COLORE_SFONDO_PREDEFINITO);
        g.fillRect(0, 0, atteso.getWidth(), atteso.getHeight());
        g.drawImage(logo3AM, 0, 0, null);
        g.dispose();
        assertTrue(uguali(atteso, disegna(effetto)), "in pausa restano scie o teste");
    }

    @Test
    void conLaScomparsaAllaFineRestaSoloLoSfondo() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).sfumaScie(true).fotogrammiPausa(10)
                .fotogrammiScomparsa(20).costruisci();
        int fotogrammi = 0;
        while (!effetto.isFinito()) {
            effetto.avanza();
            assertTrue(++fotogrammi <= effetto.stimaFotogrammiCiclo(), "non finisce entro la stima");
        }
        assertEquals("SCOMPARSA", effetto.getNomeFase());
        BufferedImage fotogramma = disegna(effetto);
        int sfondo = TracciatoreLogo.COLORE_SFONDO_PREDEFINITO.getRGB();
        for (int y = 0; y < fotogramma.getHeight(); y++) {
            for (int x = 0; x < fotogramma.getWidth(); x++) {
                assertEquals(sfondo, fotogramma.getRGB(x, y), "pixel (" + x + "," + y + ") non e' sfondo");
            }
        }
    }

    @Test
    void conLaScomparsaELaRipetizioneSiRicomincia() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).fotogrammiScomparsa(5).ripeti(true).costruisci();
        boolean scomparso = false;
        for (int i = 0; i < 2 * effetto.stimaFotogrammiCiclo(); i++) {
            effetto.avanza();
            if ("SCOMPARSA".equals(effetto.getNomeFase())) {
                scomparso = true;
            } else if (scomparso && "TRACCIAMENTO".equals(effetto.getNomeFase())) {
                return;
            }
        }
        throw new AssertionError("dopo la scomparsa non ricomincia");
    }

    private static BufferedImage disegna(TracciatoreLogo effetto) {
        BufferedImage fotogramma = new BufferedImage(effetto.getLarghezza(), effetto.getAltezza(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fotogramma.createGraphics();
        effetto.disegna(g2);
        g2.dispose();
        return fotogramma;
    }

    private static boolean uguali(BufferedImage a, BufferedImage b) {
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test
    void unaRegioneFuoriDallImmagineERifiutata() {
        assertThrows(IllegalArgumentException.class,
                () -> TracciatoreLogo.costruttore(logo3AM).regione(new Rectangle(200, 0, 50, 50)).costruisci());
    }

    @Test
    void unImmagineVuotaNonHaContorni() {
        BufferedImage vuota = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        assertThrows(IllegalStateException.class, () -> TracciatoreLogo.costruttore(vuota).costruisci());
    }

    private static BufferedImage leggi(String nome) throws IOException {
        return ImageIO.read(TracciatoreLogoTest.class.getResource("/com/threeamigos/foresta/img/" + nome));
    }

    private static boolean nero(int argb) {
        return (argb >>> 24) > 0 && ((argb >> 16) & 0xff) < 40 && ((argb >> 8) & 0xff) < 40 && (argb & 0xff) < 40;
    }

    @Test
    void nelLogoSenzaOmbraTrovaLeStesseQuattroFormeEIlLogoCompletoSiMostraAlSuoPosto() throws IOException {
        BufferedImage scritta = leggi("3AM_logo_senza_ombra.gif");
        BufferedImage completo = leggi("3AM_logo_completo.gif");
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(scritta).immagineDaMostrare(completo).costruisci();
        assertTrue(effetto.getDiagnostica().get(0).startsWith("4 forma/e rilevate (3 contorni esterni, 1 buchi interni)"),
                effetto.getDiagnostica().get(0));
        assertEquals(completo.getWidth(), effetto.getLarghezza());
        assertEquals(completo.getHeight(), effetto.getAltezza());
    }

    @Test
    void laScrittaDelLogoSenzaOmbraStaEsattamenteSopraQuellaDelLogoCompleto() throws IOException {
        BufferedImage scritta = leggi("3AM_logo_senza_ombra.gif");
        BufferedImage completo = leggi("3AM_logo_completo.gif");
        assertEquals(completo.getWidth(), scritta.getWidth());
        assertEquals(completo.getHeight(), scritta.getHeight());
        int neri = 0;
        int sovrapposti = 0;
        for (int y = 0; y < scritta.getHeight(); y++) {
            for (int x = 0; x < scritta.getWidth(); x++) {
                if (nero(scritta.getRGB(x, y))) {
                    neri++;
                    if (nero(completo.getRGB(x, y))) {
                        sovrapposti++;
                    }
                }
            }
        }
        assertTrue(neri > 0);
        assertTrue(sovrapposti >= neri * 0.99, sovrapposti + " pixel neri su " + neri + " stanno anche sul logo completo");
    }

    @Test
    void limmagineDaMostrareDeveAvereLaDimensioneDelLogoDaTracciare() {
        BufferedImage altra = new BufferedImage(logo3AM.getWidth() + 1, logo3AM.getHeight(), BufferedImage.TYPE_INT_ARGB);
        assertThrows(IllegalArgumentException.class, () -> TracciatoreLogo.costruttore(logo3AM).immagineDaMostrare(altra).costruisci());
    }
}
