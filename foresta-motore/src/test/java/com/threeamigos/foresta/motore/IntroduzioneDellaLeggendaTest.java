package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.leggende.OggettoLeggendario;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.oggetti.CatalogoLeggendari;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;

/**
 * La prima battuta della leggenda dice di quale oggetto si parla, a meno che la prima battuta dell'oggetto non lo
 * faccia già.
 */
class IntroduzioneDellaLeggendaTest {

    @Test
    void ogniIntroduzioneNominaLOggettoConLaPreposizioneGiusta() {
        Set<String> varianti = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            String frase = ProduttoreDiTestiCasuale.introduzioneDellaLeggenda("lo Scudo Fiscale");
            assertTrue(frase.contains("Scudo Fiscale"), frase);
            assertFalse(frase.contains("di lo ") || frase.contains("[") || frase.contains("oggetto leggendario"), frase);
            varianti.add(frase);
        }
        assertTrue(varianti.size() >= 6, "le frasi devono variare: " + varianti);
        assertTrue(varianti.stream().anyMatch(f -> f.contains("dello Scudo Fiscale")), String.valueOf(varianti));
        assertTrue(ProduttoreDiTestiCasuale.introduzioneDellaLeggenda("l'Elmo del Pensiero Laterale").contains("Elmo del Pensiero Laterale"));
        assertTrue(ProduttoreDiTestiCasuale.introduzioneDellaLeggenda("gli Scarponi Chiodati di RomyJona").contains("Scarponi"));
    }

    @Test
    void loScudoFiscaleELaSpadaDellaMorteNonServeIntrodurli() {
        for (OggettoLeggendario leggendario : CatalogoLeggendari.getTuttiILeggendari().values()) {
            if (leggendario.getChiave().equals(Leggendari.SCUDO_DELL_ESATTORE) || leggendario.getChiave().equals(Leggendari.SPADA_DI_ROMY_JONA)) {
                assertTrue(leggendario.laLeggendaNominaLOggetto(), leggendario.getNomeBreve());
            }
        }
    }

    @Test
    void chiNonNominaLOggettoNellaPrimaBattutaRiceveUnIntroduzione() {
        OggettoLeggendario stuzzicadenti = OggettoLeggendario.da("CHIAVE=X;TIPO=SPADONE;NOME=lo Spadone del Gigante Distratto;"
                + "BREVE=lo Spadone del Gigante Distratto;DESCRIZIONE=x;LIVELLO=5;COSTO=6;PESO=1;DANNI=9;"
                + "LEGGENDA=Il gigante Brontolone usava questo spadone come stuzzicadenti.;LEGGENDA=b");
        assertFalse(stuzzicadenti.laLeggendaNominaLOggetto());
    }
}
