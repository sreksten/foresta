package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.ChimeraDrago;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.OmbraFiamma;
import com.threeamigos.foresta.tipi.TipoDanno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L'attacco naturale dell'Ombrafiamma è il fuoco, ma contro chi è immune al fuoco (i draghi) ripiega sugli artigli:
 * altrimenti lo scontro non finirebbe mai.
 */
class ArmaNaturaleOmbraFiammaTest {

    @BeforeEach
    void preparaModello() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    @Test
    void controChiNonEImmuneAlFuocoAttaccaConLaFiammaViva() {
        OmbraFiamma ombra = new OmbraFiamma("Alakazam", 5);
        List<FaseDiAttacco> fasi = CalcolatoreCombattimento.fasiDiAttacco(ombra, new Guerriero("Pippo", 5));
        assertEquals(1, fasi.size());
        assertEquals(TipoDanno.FUOCO, fasi.get(0).getArma().getTipoDanno());
    }

    @Test
    void controChiEImmuneAlFuocoAttaccaConGliArtigli() {
        OmbraFiamma ombra = new OmbraFiamma("Alakazam", 5);
        ChimeraDrago chimeraDrago = new ChimeraDrago(1);
        assertTrue(chimeraDrago.isImmuneATipoDanno(TipoDanno.FUOCO));
        List<FaseDiAttacco> fasi = CalcolatoreCombattimento.fasiDiAttacco(ombra, chimeraDrago);
        assertEquals(1, fasi.size());
        assertEquals(1.0, fasi.get(0).getFattore(), 1e-9);
        assertEquals(TipoDanno.TAGLIENTE, fasi.get(0).getArma().getTipoDanno());
        // ...e stavolta il danno c'è
        int danno = CalcolatoreCombattimento.calcolaDannoRisultante(ombra, chimeraDrago, fasi.get(0).getArma(), 1.0).getDanno();
        assertTrue(danno > 0, "danno con gli artigli: " + danno);
    }

    @Test
    void senzaIlDifensoreLArmaRestaLaFiammaViva() {
        OmbraFiamma ombra = new OmbraFiamma("Alakazam", 5);
        assertEquals(TipoDanno.FUOCO, CalcolatoreCombattimento.fasiDiAttacco(ombra).get(0).getArma().getTipoDanno());
    }

    @Test
    void glialtriNonCambianoArmaControChiEImmune() {
        // Il drago morde con il veleno, a prescindere da chi ha davanti
        ChimeraDrago chimeraDrago = new ChimeraDrago(1);
        List<FaseDiAttacco> fasi = CalcolatoreCombattimento.fasiDiAttacco(chimeraDrago, new Guerriero("Pippo", 5));
        assertEquals(TipoDanno.VELENO, fasi.get(0).getArma().getTipoDanno());
    }
}
