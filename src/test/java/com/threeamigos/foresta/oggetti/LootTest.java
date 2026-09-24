package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.SlotArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LootTest {

    private GruppoGiocatore gruppo;
    private PersonaggioBase primo;
    private PersonaggioBase secondo;

    @BeforeEach
    void nuovoGruppo() {
        ModelloDati.setIstanza(new ModelloDati());
        gruppo = new GruppoGiocatore();
        primo = new Guerriero("Pippo", 1);
        secondo = new Guerriero("Pluto", 1);
        gruppo.aggiungiPersonaggioSenzaNotificare(primo);
        gruppo.aggiungiPersonaggioSenzaNotificare(secondo);
    }

    @Test
    void laSpadaDelLootEUnArtefattoVero() {
        Spada spada = new Spada();
        Artefatto artefatto = spada.getArtefatto().orElseThrow(AssertionError::new);
        assertEquals(TipoArtefatto.SPADA, artefatto.getTipo());
        assertTrue(artefatto.getLivello() >= 1);
        assertEquals(TipoArtefatto.SCUDO, new Scudo().getArtefatto().orElseThrow(AssertionError::new).getTipo());
    }

    @Test
    void conPiuCandidatiSceglieIlGiocatore() {
        Spada spada = new Spada();
        assertEquals(Arrays.asList(Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_2),
                Artefatto.candidati(gruppo, spada.getArtefatto().get()));
        // Senza comando non la prende nessuno: la scelta passa all'automa
        assertFalse(spada.prendi(gruppo, null));
        assertTrue(primo.getInventario().isEmpty());
        assertTrue(secondo.getInventario().isEmpty());
        // Scelto il secondo, la prende lui
        assertTrue(spada.prendi(gruppo, Comando.PERSONAGGIO_2));
        assertEquals(1, secondo.getInventario().size());
    }

    @Test
    void chiHaGiaLoSlotOccupatoNonECandidato() {
        // Given: il primo impugna già una spada
        primo.addArtefatto(artefatto(TipoArtefatto.SPADA, 1));
        Spada spada = new Spada();
        // When: resta un solo candidato
        assertEquals(Collections.singletonList(Comando.PERSONAGGIO_2), Artefatto.candidati(gruppo, spada.getArtefatto().get()));
        boolean presa = spada.prendi(gruppo, null);
        // Then: va a lui senza domanda
        assertTrue(presa);
        assertEquals(1, primo.getInventario().size());
        assertEquals(1, secondo.getInventario().size());
        assertTrue(gruppo.getInventario().isEmpty());
    }

    @Test
    void senzaCandidatiVaNelGruppo() {
        // Given: tutti e due hanno già uno scudo
        primo.addArtefatto(artefatto(TipoArtefatto.SCUDO, 1));
        secondo.addArtefatto(artefatto(TipoArtefatto.SCUDO, 1));
        // When
        boolean preso = new Scudo().prendi(gruppo, null);
        // Then: niente sostituzioni, lo scudo nuovo va nel gruppo e non è equipaggiato
        assertTrue(preso);
        assertEquals(1, gruppo.getInventario().size());
        assertNull(gruppo.getInventario().iterator().next().getModelloDati().getSlotEquipaggiamento());
    }

    @Test
    void iMortiNonSonoCandidati() {
        secondo.getModelloDati().setVivo(false);
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 1);
        assertEquals(Collections.singletonList(Comando.PERSONAGGIO_1), Artefatto.candidati(gruppo, spada));
        assertTrue(spada.prendi(gruppo, null));
        assertEquals(1, primo.getInventario().size());
    }

    @Test
    void unArtefattoDiLivelloTroppoAltoVaNelGruppo() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        assertTrue(Artefatto.candidati(gruppo, spada).isEmpty());
        assertTrue(spada.prendi(gruppo, null));
        assertEquals(1, gruppo.getInventario().size());
    }

    @Test
    void lePergameneVannoSempreNelGruppo() {
        Artefatto pergamena = new GeneratoreArtefattiTabelle(new Random(1)).generaPergamena(1);
        assertTrue(Artefatto.candidati(gruppo, pergamena).isEmpty());
        assertTrue(pergamena.prendi(gruppo, null));
        assertEquals(1, gruppo.getInventario().size());
    }

    @Test
    void ilLadroPrendeLaSecondaSpadaNellaManoSecondaria() {
        GruppoGiocatore gruppoDelLadro = new GruppoGiocatore();
        PersonaggioBase ladro = new Ladro("Lupin", 1);
        gruppoDelLadro.aggiungiPersonaggioSenzaNotificare(ladro);
        ladro.addArtefatto(artefatto(TipoArtefatto.SPADA, 1));
        Artefatto seconda = artefatto(TipoArtefatto.SPADA, 1);
        assertTrue(seconda.prendi(gruppoDelLadro, null));
        assertEquals(SlotArtefatto.MANO_SECONDARIA, seconda.getModelloDati().getSlotEquipaggiamento());
    }

    @Test
    void conGruppoVaNelGruppoAncheSeQualcunoPotrebbePrenderlo() {
        assertTrue(new Spada().prendi(gruppo, Comando.GRUPPO));
        assertEquals(1, gruppo.getInventario().size());
    }

    @Test
    void cofanoCinquePerCentoPergamenaCinquePerCentoArtefatto() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(2));
        assertEquals(TipoArtefatto.INCANTAMENTO, Cofano.artefattoRaro(0.0, generatore, 3).getTipo());
        assertEquals(TipoArtefatto.INCANTAMENTO, Cofano.artefattoRaro(0.049, generatore, 3).getTipo());
        Artefatto artefatto = Cofano.artefattoRaro(0.05, generatore, 3);
        assertTrue(artefatto.getTipo() != TipoArtefatto.INCANTAMENTO);
        assertTrue(Cofano.artefattoRaro(0.099, generatore, 3).getTipo() != TipoArtefatto.INCANTAMENTO);
        assertNull(Cofano.artefattoRaro(0.10, generatore, 3));
        assertNull(Cofano.artefattoRaro(0.99, generatore, 3));
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(0.1);
        return Artefatto.di(md);
    }
}
