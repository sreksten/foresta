package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        boolean preso = RaccoltaBottino.raccogli(BottinoArtefatto.giaFatto(artefatto(1)), gruppo, Comando.GRUPPO);
        // Then
        assertTrue(preso);
        assertEquals(1, gruppo.getInventario().size());
        assertTrue(guerriero.getInventario().isEmpty());
    }

    @Test
    void ilPersonaggioPrendeUnArtefattoCheRientraNelSuoCarico() {
        // When
        boolean preso = RaccoltaBottino.raccogli(BottinoArtefatto.giaFatto(artefatto(1)), gruppo, Comando.PERSONAGGIO_1);
        // Then
        assertTrue(preso);
        assertEquals(1, guerriero.getInventario().size());
        assertTrue(gruppo.getInventario().isEmpty());
    }

    @Test
    void unArtefattoTroppoPesanteFinisceNellInventarioDelGruppo() {
        // When
        boolean preso = RaccoltaBottino.raccogli(BottinoArtefatto.giaFatto(artefatto(100_000)), gruppo, Comando.PERSONAGGIO_1);
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
        boolean preso = RaccoltaBottino.raccogli(BottinoArtefatto.giaFatto(spada), gruppo, Comando.PERSONAGGIO_1);
        // Then
        assertTrue(preso);
        assertTrue(guerriero.getInventario().isEmpty());
        assertEquals(1, gruppo.getInventario().size());
    }

    @Test
    void unaSecondaSpadaPerIlGuerrieroFinisceNellInventarioDelGruppo() {
        // Given
        RaccoltaBottino.raccogli(BottinoArtefatto.giaFatto(artefatto(1)), gruppo, Comando.PERSONAGGIO_1);
        // When
        boolean preso = RaccoltaBottino.raccogli(BottinoArtefatto.giaFatto(artefatto(1)), gruppo, Comando.PERSONAGGIO_1);
        // Then
        assertTrue(preso);
        assertEquals(1, guerriero.getInventario().size());
        assertEquals(1, gruppo.getInventario().size());
        assertNull(gruppo.getInventario().iterator().next().getModelloDati().getSlotEquipaggiamento());
    }

    @Test
    void senzaComandoEConUnSoloCandidatoLoPrendeLui() {
        // When
        boolean preso = RaccoltaBottino.raccogli(BottinoArtefatto.giaFatto(artefatto(1)), gruppo, null);
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
