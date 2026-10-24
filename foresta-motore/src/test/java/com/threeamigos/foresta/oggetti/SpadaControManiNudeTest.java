package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpadaControManiNudeTest {

    private static final int GIRI = 500;

    @Test
    void unaSpadaDiLivelloUnoFaPiuDelleManiNudeDiUnGuerriero() {
        ModelloDati.setIstanza(new ModelloDati());
        int maniNude = new ArmaNaturale(new Guerriero("Pippo", 1)).getDanni();
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(5));
        for (int i = 0; i < GIRI; i++) {
            int danni = generatore.generaArtefatto(TipoArtefatto.SPADA, 1).getModelloDati().getDanni();
            assertTrue(danni > maniNude, "spada " + danni + ", mani nude " + maniNude);
        }
    }
}
