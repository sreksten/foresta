package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.ArmaFisica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class RegistroArtefattiTest {

    private final CoordinateMD tempio = new CoordinateMD(3, 4);

    @BeforeEach
    void nuovoModelloDati() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    @Test
    void unArmaNelTempioVieneRicostruitaComeArmaFisica() {
        // Given
        RegistroArtefatti.addArtefattoInLocazione(Artefatto.di(artefatto(TipoArtefatto.SPADA)), tempio);
        // When
        Artefatto artefatto = RegistroArtefatti.getArtefattoInLocazione(tempio);
        // Then: getArmaEquipaggiata() fa il cast ad Arma del primo artefatto di supertipo ARMA
        assertInstanceOf(ArmaFisica.class, artefatto);
    }

    @Test
    void unArtefattoCheNonEUnArmaNonVieneRicostruitoComeArmaFisica() {
        // Given
        RegistroArtefatti.addArtefattoInLocazione(Artefatto.di(artefatto(TipoArtefatto.ELMO)), tempio);
        // When
        Artefatto artefatto = RegistroArtefatti.getArtefattoInLocazione(tempio);
        // Then
        assertFalse(artefatto instanceof ArmaFisica);
    }

    @Test
    void unArtefattoRimossoNonSiRipresentaNellaLocazione() {
        // Given
        RegistroArtefatti.addArtefattoInLocazione(Artefatto.di(artefatto(TipoArtefatto.SPADA)), tempio);
        // When
        RegistroArtefatti.rimuoviArtefattoInLocazione(tempio);
        // Then
        assertNull(RegistroArtefatti.getArtefattoInLocazione(tempio));
    }

    private static ArtefattoMD artefatto(TipoArtefatto tipo) {
        ArtefattoMD artefatto = new ArtefattoMD();
        artefatto.setTipo(tipo);
        artefatto.setNome("l'artefatto di prova");
        artefatto.setDescrizione("che serve ai test");
        artefatto.setLivello(1);
        artefatto.setDanni(tipo == TipoArtefatto.SPADA ? 5 : 0);
        artefatto.setCostoAcquisto(10);
        artefatto.setPeso(1);
        return artefatto;
    }
}
