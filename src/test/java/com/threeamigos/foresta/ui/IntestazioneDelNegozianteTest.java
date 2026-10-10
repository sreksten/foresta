package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoNegozio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nelle schermate dell'armaiolo, dell'alchimista, del venditore di pergamene e dell'incantatore il nome del negoziante
 * sta al posto del mestiere, spezzato su più righe se non entra fra le due cornici, e il mestiere va sotto in carattere
 * piccolo.
 */
class IntestazioneDelNegozianteTest {

    private static final String NOME_LUNGO = "Mastro Ferruccio dei Cavalieri del Sole da Ruuna";

    private static VistaPartita vistaConNome(String nome) {
        return (VistaPartita) Proxy.newProxyInstance(VistaPartita.class.getClassLoader(), new Class<?>[]{VistaPartita.class},
                (proxy, metodo, argomenti) -> {
                    if (metodo.getName().equals("getNomeDelPersonaggioDiCitta")) {
                        return nome;
                    }
                    return null;
                });
    }

    private static int larghezzaColonna(DisplayableCanvasCommerciante commerciante) {
        return commerciante.xMassimaZonaCentrale - commerciante.xMinimaZonaCentrale;
    }

    @Test
    void unNomeLungoVaSuPiuRigheEOgniRigaStaFraLeCornici() {
        DisplayableCanvasCommerciante commerciante = new DisplayableCanvasCommerciante(1024, 768, vistaConNome(NOME_LUNGO));
        commerciante.impostaNegozio(TipoNegozio.ARMAIOLO);
        List<String> righe = FontTool.split(commerciante.font, NOME_LUNGO, larghezzaColonna(commerciante));
        assertTrue(righe.size() > 1, "il nome dovrebbe andare su più righe: " + righe);
        for (String riga : righe) {
            assertTrue(FontTool.getWidth(commerciante.font, riga) <= larghezzaColonna(commerciante), riga);
        }
        int attesa = righe.size() * commerciante.fontHeight + (righe.size() - 1) * DisplayableCanvasScambiatore.INTERLINEA_DEL_NOME
                + DisplayableCanvasScambiatore.SPAZIO_FRA_NOME_E_MESTIERE + commerciante.fontSmall.getHeight();
        assertEquals(attesa, commerciante.altezzaIntestazioneCentrale());
    }

    @Test
    void senzaUnNomeRestaIlMestiereSuUnaRiga() {
        DisplayableCanvasCommerciante commerciante = new DisplayableCanvasCommerciante(1024, 768, vistaConNome(null));
        commerciante.impostaNegozio(TipoNegozio.VENDITORE_DI_PERGAMENE);
        assertEquals(commerciante.fontHeight, commerciante.altezzaIntestazioneCentrale());
    }

    @Test
    void ilDisegnoOccupaQuantoDichiaratoEIlFumettoSiSposta() {
        DisplayableCanvasCommerciante conNome = new DisplayableCanvasCommerciante(1024, 768, vistaConNome(NOME_LUNGO));
        DisplayableCanvasCommerciante senzaNome = new DisplayableCanvasCommerciante(1024, 768, vistaConNome(null));
        BufferedImage immagine = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = immagine.createGraphics();
        try {
            int sotto = conNome.disegnaIntestazioneCentrale(graphics, 20, DoomdarkColorModel.Color.LIGHT_GRAY);
            assertEquals(20 + conNome.altezzaIntestazioneCentrale(), sotto);
            assertEquals(20 + senzaNome.fontHeight, senzaNome.disegnaIntestazioneCentrale(graphics, 20, DoomdarkColorModel.Color.LIGHT_GRAY));
        } finally {
            graphics.dispose();
        }
        int sposta = conNome.altezzaIntestazioneCentrale() - senzaNome.altezzaIntestazioneCentrale();
        assertTrue(sposta > 0);
        assertEquals(senzaNome.getCoordinateFumetto().getY() + sposta, conNome.getCoordinateFumetto().getY());
        assertEquals(senzaNome.yRigaMonete() + sposta, conNome.yRigaMonete());
    }

    @Test
    void ognunoDeiQuattroMestieriHaIlSuoNome() {
        DisplayableCanvasCommerciante commerciante = new DisplayableCanvasCommerciante(1024, 768, vistaConNome("Mastro Pippo da Ruuna"));
        commerciante.impostaNegozio(TipoNegozio.ARMAIOLO);
        assertEquals(TipoPersonaggio.ARMAIOLO, commerciante.getMestiereAlCentro());
        commerciante.impostaNegozio(TipoNegozio.VENDITORE_DI_PERGAMENE);
        assertEquals(TipoPersonaggio.VENDITORE_DI_PERGAMENE, commerciante.getMestiereAlCentro());
        assertEquals("Venditore di pergamene", commerciante.getEtichettaMestiere());
        assertEquals(TipoPersonaggio.INCANTATORE, new DisplayableCanvasIncantatore(1024, 768, vistaConNome("x")).getMestiereAlCentro());
        assertEquals(TipoPersonaggio.ALCHIMISTA, new DisplayableCanvasScambiatoreConsumabili(1024, 768, vistaConNome("x")).getMestiereAlCentro());
    }

    /**
     * Tutti i caratteri dei nomi di nomi.txt, della provenienza e dei mestieri sono nei due font.
     */
    @Test
    void iNomiSiPossonoScrivereConIFontDoomdark() throws IOException {
        List<String> testi = new ArrayList<>();
        List<String> righe = Files.readAllLines(Paths.get("src/main/resources/com/threeamigos/foresta/motore/nomi.txt"), StandardCharsets.UTF_8);
        String lista = null;
        for (String riga : righe) {
            if (riga.matches("[A-Z_]+\\$?")) {
                lista = riga.replace("$", "");
            } else if (lista != null && !riga.trim().isEmpty() && !riga.startsWith("//")
                    && (lista.equals("NOME_MASCHILE") || lista.equals("NOME_FEMMINILE") || lista.equals("DEGLI_ALTISONANTE")
                    || lista.equals("COGNOME") || lista.equals("NOME_COMPARSA"))) {
                for (String voce : riga.split("\\|")) {
                    testi.add(voce.trim());
                }
            }
        }
        testi.add("Mastro Armaiolo Alchimista Incantatore Venditore di pergamene da Nyena Malgaard Ruuna Fleena");
        assertTrue(testi.size() > 200, "dovrebbe aver letto le liste di nomi: " + testi.size());
        DoomdarkFont medio = DoomdarkFontMedium.getInstance();
        DoomdarkFont piccolo = DoomdarkFontSmall.getInstance();
        for (String testo : testi) {
            for (char c : testo.toCharArray()) {
                final char carattere = c;
                assertDoesNotThrow(() -> medio.getGlyphWidth(carattere), "carattere non nel font medio: '" + carattere + "' in " + testo);
                assertDoesNotThrow(() -> piccolo.getGlyphWidth(carattere), "carattere non nel font piccolo: '" + carattere + "' in " + testo);
            }
        }
    }
}
