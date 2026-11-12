package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.tipi.SchermoDiProva;
import com.threeamigos.foresta.tipi.TipoDiLayout;
import com.threeamigos.foresta.ui.immagini.MisureDaiFile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La scelta del layout sugli schermi di prova: è la tabella di layout_telefono.md §2.3, con le misure vere delle cornici.
 */
class SceltaDelLayoutTest {

    private static final double ZOOM = 2.0;
    private final MisureDaiFile misure = new MisureDaiFile();

    @Test
    void ilMinimoDelTelefonoE842Per505() {
        Misura minimo = SceltaDelLayout.minimoDelTelefono(misure, ZOOM);
        assertEquals(842, minimo.larghezza);
        assertEquals(505, minimo.altezza);
    }

    /**
     * Senza una scelta del giocatore: il tablet con la barra in basso se ci sta (1022x804), altrimenti a destra (1094x732); il telefono a destra
     */
    @ParameterizedTest
    @CsvSource({
            "MINIMO, TELEFONO, DESTRA, 0.95, 842, 505",
            "GALAXY_S2, TELEFONO, DESTRA, 0.63, 842, 505",
            "ECONOMICO, TELEFONO, DESTRA, 0.71, 1122, 505",
            "GALAXY_S24, TELEFONO, DESTRA, 0.71, 1094, 505",
            "GALAXY_M31, TELEFONO, DESTRA, 0.81, 1094, 505",
            "PIXEL_8, TELEFONO, DESTRA, 0.81, 1122, 505",
            "PIXEL_8_PRO, TELEFONO, DESTRA, 0.89, 1124, 505",
            "IPHONE_SE, TELEFONO, DESTRA, 0.74, 898, 505",
            "IPHONE_15, TELEFONO, DESTRA, 0.78, 1095, 505",
            "IPHONE_15_PRO_MAX, TELEFONO, DESTRA, 0.85, 1095, 505",
            "REALME_PAD, TABLET, DESTRA, 1.09, 1094, 732",
            "TABLET_ECONOMICO, TABLET, DESTRA, 1.09, 1094, 732",
            "TABLET_FULL_HD, TABLET, DESTRA, 1.09, 1094, 732",
            "IPAD_MINI, TABLET, DESTRA, 1.02, 1094, 732",
            "IPAD, TABLET, BASSO, 1.02, 1022, 804",
            "IPAD_PRO_13, TABLET, BASSO, 1.28, 1022, 804",
            "PORTATILE_HD, TABLET, DESTRA, 1.05, 1094, 732",
            "FULL_HD, TABLET, BASSO, 1.34, 1022, 804",
    })
    void laTabellaDegliSchermiDiProva(SchermoDiProva profilo, TipoDiLayout tipo, PosizioneDellaBarra barra, double scala, int larghezza, int altezza) {
        SceltaDelLayout scelta = SceltaDelLayout.per(profilo.getMisura(), misure, ZOOM);
        assertEquals(tipo, scelta.getTipo());
        assertEquals(barra == PosizioneDellaBarra.BASSO ? Orientamento.ORIZZONTALE : Orientamento.VERTICALE, scelta.getOrientamento());
        assertFalse(scelta.isBarraASinistra());
        assertEquals(scala, scelta.getScala(), 0.005);
        assertEquals(larghezza, scelta.getAreaVirtuale().larghezza);
        assertEquals(altezza, scelta.getAreaVirtuale().altezza);
    }

    @Test
    void laBarraVaDoveLaVuoleIlGiocatoreSeCiSta() {
        // Sul tablet grande: a sinistra e a destra di lato, in basso in basso
        SceltaDelLayout aSinistra = SceltaDelLayout.per(SchermoDiProva.IPAD.getMisura(), misure, ZOOM, PosizioneDellaBarra.SINISTRA);
        assertEquals(Orientamento.VERTICALE, aSinistra.getOrientamento());
        assertTrue(aSinistra.isBarraASinistra());
        SceltaDelLayout aDestra = SceltaDelLayout.per(SchermoDiProva.IPAD.getMisura(), misure, ZOOM, PosizioneDellaBarra.DESTRA);
        assertEquals(Orientamento.VERTICALE, aDestra.getOrientamento());
        assertFalse(aDestra.isBarraASinistra());
        assertTrue(SceltaDelLayout.barraInBassoPossibile(SchermoDiProva.IPAD.getMisura(), misure, ZOOM));
        // Sul telefono e sul tablet basso la barra in basso non c'è: va a destra
        SceltaDelLayout telefono = SceltaDelLayout.per(SchermoDiProva.GALAXY_S24.getMisura(), misure, ZOOM, PosizioneDellaBarra.BASSO);
        assertEquals(Orientamento.VERTICALE, telefono.getOrientamento());
        assertFalse(SceltaDelLayout.barraInBassoPossibile(SchermoDiProva.GALAXY_S24.getMisura(), misure, ZOOM));
        assertFalse(SceltaDelLayout.barraInBassoPossibile(SchermoDiProva.REALME_PAD.getMisura(), misure, ZOOM));
        // A sinistra anche sul telefono
        assertTrue(SceltaDelLayout.per(SchermoDiProva.MINIMO.getMisura(), misure, ZOOM, PosizioneDellaBarra.SINISTRA).isBarraASinistra());
    }

    @Test
    void ilTabletEIlLayoutDiSempreConLaBarraADestra() {
        Misura verticale = DisposizioneDelloSchermo.dimensioniDelloSchermo(Orientamento.VERTICALE, misure, ZOOM);
        SceltaDelLayout giusto = SceltaDelLayout.per(new MisuraDelloSchermo(verticale.larghezza, verticale.altezza, 1), misure, ZOOM);
        assertEquals(TipoDiLayout.TABLET, giusto.getTipo());
        assertEquals(1.0, giusto.getScala(), 1e-9);
        SceltaDelLayout pocoPiuBasso = SceltaDelLayout.per(new MisuraDelloSchermo(verticale.larghezza, verticale.altezza - 1, 1), misure, ZOOM);
        assertEquals(TipoDiLayout.TELEFONO, pocoPiuBasso.getTipo());
    }

    @Test
    void sulTelefonoLAreaVirtualeHaLaFormaDelloSchermoENonScendeSottoIlMinimo() {
        for (SchermoDiProva profilo : SchermoDiProva.values()) {
            SceltaDelLayout scelta = SceltaDelLayout.per(profilo.getMisura(), misure, ZOOM);
            if (scelta.getTipo() != TipoDiLayout.TELEFONO) {
                continue;
            }
            Misura area = scelta.getAreaVirtuale();
            assertTrue(area.larghezza >= 842 && area.altezza >= 505, profilo.name());
            double rapportoSchermo = profilo.getMisura().getLarghezzaDp() / profilo.getMisura().getAltezzaDp();
            assertEquals(rapportoSchermo, (double) area.larghezza / area.altezza, 0.01, "senza bande: " + profilo);
        }
        // Uno schermo più grande del minimo ma più piccolo del tablet si usa a scala 1, tutto per il testo
        SceltaDelLayout grande = SceltaDelLayout.per(new MisuraDelloSchermo(1000, 600, 1), misure, ZOOM);
        assertEquals(1.0, grande.getScala(), 1e-9);
        assertEquals(1000, grande.getAreaVirtuale().larghezza);
        assertEquals(600, grande.getAreaVirtuale().altezza);
    }

    @Test
    void laMisuraSiLeggeConOSenzaDensitaEIlLatoLungoELaLarghezza() {
        MisuraDelloSchermo pixel = MisuraDelloSchermo.leggi("2400x1080@2.625");
        assertEquals(2400, pixel.getLarghezza());
        assertEquals(1080, pixel.getAltezza());
        assertEquals(411.43, pixel.getAltezzaDp(), 0.01);
        MisuraDelloSchermo inPiedi = MisuraDelloSchermo.leggi("480X800");
        assertEquals(800, inPiedi.getLarghezza());
        assertEquals(480, inPiedi.getAltezza());
        assertEquals(1.0, inPiedi.getDensita());
        assertThrows(IllegalArgumentException.class, () -> MisuraDelloSchermo.leggi("grande"));
        assertThrows(IllegalArgumentException.class, () -> MisuraDelloSchermo.leggi("800x0"));
        assertThrows(IllegalArgumentException.class, () -> MisuraDelloSchermo.leggi("800x480@0"));
    }
}
