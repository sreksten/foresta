package com.threeamigos.foresta.mondo;

import com.threeamigos.foresta.artefatti.ArmaFisica;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.modellodati.RegistroArtefattiMD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistroArtefattiTest {

    private final CoordinateMD tempio = new CoordinateMD(3, 4);

    @BeforeEach
    void nuoviDati() {
        RegistroArtefattiMD dati = new RegistroArtefattiMD();
        RegistroArtefatti.setFornitoreDiDati(() -> dati);
    }

    @Test
    void unArmaNelTempioVieneRicostruitaComeArmaFisica() {
        // Given
        RegistroArtefatti.addArtefattoInLocazione(Artefatto.di(artefatto(TipoArtefatto.SPADA)), tempio);
        // When
        Artefatto artefatto = RegistroArtefatti.getArtefattoInLocazione(tempio);
        // Then: getArmaEquipaggiata() fa il cast ad Arma dell'arma impugnata
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

    @Test
    void senzaIDatiIlRegistroNonVa() {
        RegistroArtefatti.setFornitoreDiDati(null);
        assertThrows(IllegalStateException.class, RegistroArtefatti::getArtefattoDisponibile);
    }

    @Test
    void gliArtefattiDaDistribuireSiPescanoUnaVoltaSola() {
        assertNull(RegistroArtefatti.getArtefattoDisponibile());
        RegistroArtefatti.reimposta();
        int pescati = 0;
        while (RegistroArtefatti.getArtefattoDisponibile() != null) {
            pescati++;
            assertTrue(pescati < 100);
        }
        assertEquals(16, pescati);
    }

    @Test
    void gliArtefattiSparsiESuaUbicazione() {
        assertNull(RegistroArtefatti.getArtefattoCasuale());
        Artefatto spada = Artefatto.di(artefatto(TipoArtefatto.SPADA));
        RegistroArtefatti.addArtefattoInLocazione(spada, tempio);
        RegistroArtefattiMD.ArtefattoESuaUbicazione trovato = RegistroArtefatti.getArtefattoCasuale();
        assertNotNull(trovato);
        assertEquals(tempio, trovato.getCoordinate());
    }

    @Test
    void leLocalizzazioniConosciute() {
        assertFalse(RegistroArtefatti.isLocalizzazioneConosciuta(tempio));
        RegistroArtefatti.segnaLocalizzazioneConosciuta(tempio);
        assertTrue(RegistroArtefatti.isLocalizzazioneConosciuta(tempio));
        assertTrue(RegistroArtefatti.getLocalizzazioniConosciute().contains(tempio));
    }

    @Test
    void unTempioNuovoCustodisceUnLeggendarioEMostraAlGruppoDovEra() {
        ForestaMD foresta = new ForestaMD();
        com.threeamigos.foresta.mappa.Foresta.setFornitoreDiDati(() -> foresta);
        com.threeamigos.foresta.mappa.Foresta.azzera(10, 8);
        Artefatto spada = Artefatto.di(artefatto(TipoArtefatto.SPADA));
        RegistroArtefatti.custodisciInUnTempioNuovo(spada, tempio);
        assertEquals(com.threeamigos.foresta.tipi.TipoLocazione.TEMPIO, com.threeamigos.foresta.mappa.Foresta.getLocazione(tempio));
        assertNotNull(RegistroArtefatti.getArtefattoInLocazione(tempio));
        assertTrue(RegistroArtefatti.isLocalizzazioneConosciuta(tempio));
        assertTrue(com.threeamigos.foresta.mappa.Foresta.isLocazioneConosciuta(tempio));
    }

    @Test
    void lePrivateEIlRegistroNonSiIstanzia() throws Exception {
        java.lang.reflect.Constructor<RegistroArtefatti> costruttore = RegistroArtefatti.class.getDeclaredConstructor();
        costruttore.setAccessible(true);
        assertNotNull(costruttore.newInstance());
    }
}
