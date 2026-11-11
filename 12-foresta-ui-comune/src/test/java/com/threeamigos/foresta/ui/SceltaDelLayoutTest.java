package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Orientamento;
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

    @ParameterizedTest
    @CsvSource({
            "MINIMO, TELEFONO, 0.95, 842, 505",
            "GALAXY_S2, TELEFONO, 0.63, 842, 505",
            "ECONOMICO, TELEFONO, 0.71, 1122, 505",
            "GALAXY_S24, TELEFONO, 0.71, 1094, 505",
            "GALAXY_M31, TELEFONO, 0.81, 1094, 505",
            "PIXEL_8, TELEFONO, 0.81, 1122, 505",
            "PIXEL_8_PRO, TELEFONO, 0.89, 1124, 505",
            "IPHONE_SE, TELEFONO, 0.74, 898, 505",
            "IPHONE_15, TELEFONO, 0.78, 1095, 505",
            "IPHONE_15_PRO_MAX, TELEFONO, 0.85, 1095, 505",
            "REALME_PAD, TABLET, 1.09, 1094, 732",
            "TABLET_ECONOMICO, TABLET, 1.09, 1094, 732",
            "TABLET_FULL_HD, TABLET, 1.09, 1094, 732",
            "IPAD_MINI, TABLET, 1.02, 1094, 732",
            "IPAD, TABLET, 1.08, 1094, 732",
            "IPAD_PRO_13, TABLET, 1.26, 1094, 732",
            "PORTATILE_HD, TABLET, 1.05, 1094, 732",
            "FULL_HD, TABLET, 1.48, 1094, 732",
    })
    void laTabellaDegliSchermiDiProva(SchermoDiProva profilo, TipoDiLayout tipo, double scala, int larghezza, int altezza) {
        SceltaDelLayout scelta = SceltaDelLayout.per(profilo.getMisura(), misure, ZOOM);
        assertEquals(tipo, scelta.getTipo());
        assertEquals(scala, scelta.getScala(), 0.005);
        assertEquals(larghezza, scelta.getAreaVirtuale().larghezza);
        assertEquals(altezza, scelta.getAreaVirtuale().altezza);
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
