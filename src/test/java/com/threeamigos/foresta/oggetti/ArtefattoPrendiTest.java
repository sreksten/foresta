package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtefattoPrendiTest {

    private GruppoGiocatore gruppo;
    private Personaggio guerriero;

    @BeforeEach
    void nuovoGruppo() {
        ModelloDati.setIstanza(new ModelloDati());
        gruppo = new GruppoGiocatore();
        guerriero = new Guerriero("Pippo", 1);
        gruppo.aggiungiPersonaggioSenzaNotificare(guerriero);
    }

    @Test
    void conGruppoFinisceNellInventarioDelGruppo() {
        // When
        boolean preso = artefatto(1).prendi(gruppo, Comando.GRUPPO);
        // Then
        assertTrue(preso);
        assertEquals(1, gruppo.getInventario().size());
        assertTrue(guerriero.getInventario().isEmpty());
    }

    @Test
    void ilPersonaggioPrendeUnArtefattoCheRientraNelSuoCarico() {
        // When
        boolean preso = artefatto(1).prendi(gruppo, Comando.PERSONAGGIO_1);
        // Then
        assertTrue(preso);
        assertEquals(1, guerriero.getInventario().size());
        assertTrue(gruppo.getInventario().isEmpty());
    }

    @Test
    void unArtefattoTroppoPesanteFinisceNellInventarioDelGruppo() {
        // When
        boolean preso = artefatto(100_000).prendi(gruppo, Comando.PERSONAGGIO_1);
        // Then: l'oggetto è comunque raccolto, ma dal gruppo
        assertTrue(preso);
        assertTrue(guerriero.getInventario().isEmpty());
        assertEquals(1, gruppo.getInventario().size());
    }

    @Test
    void unArtefattoDiLivelloTroppoAltoFinisceNellInventarioDelGruppo() {
        // Given
        Artefatto spada = artefatto(1);
        spada.getModelloDati().setLivello(2);
        // When
        boolean preso = spada.prendi(gruppo, Comando.PERSONAGGIO_1);
        // Then
        assertTrue(preso);
        assertTrue(guerriero.getInventario().isEmpty());
        assertEquals(1, gruppo.getInventario().size());
    }

    @Test
    void unaSecondaSpadaPerIlGuerrieroFinisceNellInventarioDelGruppo() {
        // Given
        artefatto(1).prendi(gruppo, Comando.PERSONAGGIO_1);
        // When
        boolean preso = artefatto(1).prendi(gruppo, Comando.PERSONAGGIO_1);
        // Then
        assertTrue(preso);
        assertEquals(1, guerriero.getInventario().size());
        assertEquals(1, gruppo.getInventario().size());
        assertEquals(null, gruppo.getInventario().iterator().next().getModelloDati().getSlotEquipaggiamento());
    }

    @Test
    void senzaComandoEConUnSoloCandidatoLoPrendeLui() {
        // When
        boolean preso = artefatto(1).prendi(gruppo, null);
        // Then
        assertTrue(preso);
        assertEquals(1, guerriero.getInventario().size());
        assertTrue(gruppo.getInventario().isEmpty());
    }

    @Test
    void soloIComandiPersonaggioIndicanoUnPersonaggio() {
        assertTrue(Comando.PERSONAGGIO_1.isPersonaggio());
        assertTrue(Comando.PERSONAGGIO_5.isPersonaggio());
        assertFalse(Comando.GRUPPO.isPersonaggio());
        assertFalse(Comando.ANNULLA.isPersonaggio());
        assertFalse(Comando.TIMER.isPersonaggio());
    }

    private static Artefatto artefatto(double peso) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(TipoArtefatto.SPADA);
        md.setNome("la spada di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(1);
        md.setDanni(5);
        md.setCostoAcquisto(10);
        md.setPeso(peso);
        return Artefatto.di(md);
    }
}
