package com.threeamigos.foresta.modificatori;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un modificatore di attributo: la coerenza fra durata e unità, le copie con un dato cambiato, l'uguaglianza e il campo
 * nel salvataggio.
 */
class ModificatoreAttributoMDTest {

    private static ModificatoreAttributoMD aTempo(int turni) {
        return new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, -20.0d, "paura",
                UnitaDurataModificatore.TURNI, turni, BersaglioModificatore.BERSAGLIO);
    }

    @Test
    void laDurataDeveEssereCoerenteConLUnita() {
        assertThrows(IllegalArgumentException.class, () -> new ModificatoreAttributoMD(TipoAttributo.FORZA,
                TipoModificatore.VARIAZIONE_FISSA, 1.0d, "", UnitaDurataModificatore.PERMANENTE, 2, BersaglioModificatore.PORTATORE),
                "un permanente ha durata 0");
        IllegalArgumentException errore = assertThrows(IllegalArgumentException.class, () -> aTempo(0), "uno a tempo almeno 1");
        assertTrue(errore.getMessage().contains("TURNI"), errore.getMessage());
    }

    @Test
    void iDatiSiLeggonoEIlTemporaneoHaUnaScadenza() {
        ModificatoreAttributoMD modificatore = aTempo(3);
        assertSame(TipoAttributo.CORAGGIO, modificatore.getTipoAttributo());
        assertSame(TipoModificatore.VARIAZIONE_PERCENTUALE, modificatore.getTipoModificatoreAttributo());
        assertEquals(-20.0d, modificatore.getQuantita());
        assertEquals("paura", modificatore.getNote());
        assertSame(UnitaDurataModificatore.TURNI, modificatore.getUnitaDurata());
        assertEquals(3, modificatore.getDurata());
        assertSame(BersaglioModificatore.BERSAGLIO, modificatore.getBersaglio());
        assertTrue(modificatore.isTemporaneo());
        assertFalse(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1.0d).isTemporaneo());
    }

    @Test
    void leCopieCambianoUnDatoSolo() {
        ModificatoreAttributoMD modificatore = aTempo(3);
        ModificatoreAttributoMD piuBreve = modificatore.conDurata(1);
        assertEquals(1, piuBreve.getDurata());
        assertTrue(piuBreve.stessoEffetto(modificatore));
        assertNotEquals(modificatore, piuBreve, "la durata conta per equals");

        ModificatoreAttributoMD sulPortatore = modificatore.suPortatore();
        assertSame(BersaglioModificatore.PORTATORE, sulPortatore.getBersaglio());
        assertFalse(sulPortatore.stessoEffetto(modificatore));

        ModificatoreAttributoMD doppio = modificatore.conQuantita(-40.0d);
        assertEquals(-40.0d, doppio.getQuantita());
        assertFalse(doppio.stessoEffetto(modificatore));
    }

    @Test
    void sonoUgualiSeTuttiIDatiSonoUguali() {
        ModificatoreAttributoMD modificatore = aTempo(3);
        assertEquals(aTempo(3), modificatore);
        assertEquals(aTempo(3).hashCode(), modificatore.hashCode());
        assertFalse(modificatore.equals("paura"));
        assertNotEquals(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_PERCENTUALE, -20.0d, "paura",
                UnitaDurataModificatore.TURNI, 3, BersaglioModificatore.BERSAGLIO), modificatore, "un altro attributo");
        assertNotEquals(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_FISSA, -20.0d, "paura",
                UnitaDurataModificatore.TURNI, 3, BersaglioModificatore.BERSAGLIO), modificatore, "un altro tipo");
        assertNotEquals(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, -20.0d, "coraggio",
                UnitaDurataModificatore.TURNI, 3, BersaglioModificatore.BERSAGLIO), modificatore, "un'altra nota");
        assertNotEquals(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, -20.0d, "paura",
                UnitaDurataModificatore.LOCAZIONI, 3, BersaglioModificatore.BERSAGLIO), modificatore, "un'altra unità");
    }

    @Test
    void ilFlagRimovibileSiConservaNelleCopieNelCampoEInEquals() throws IOException {
        ModificatoreAttributoMD fisso = new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE,
                -20.0d, "ignominia", UnitaDurataModificatore.LOCAZIONI, 3, BersaglioModificatore.PORTATORE, false);
        assertTrue(aTempo(3).isRimovibile(), "di norma è rimovibile");
        assertFalse(fisso.isRimovibile());
        assertFalse(fisso.conDurata(2).isRimovibile());
        assertFalse(fisso.conQuantita(-10.0d).isRimovibile());
        assertFalse(fisso.suPortatore().isRimovibile());
        assertFalse(fisso.stessoEffetto(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE,
                -20.0d, "ignominia", UnitaDurataModificatore.LOCAZIONI, 3, BersaglioModificatore.PORTATORE)));
        ModificatoreAttributoMD riletto = ModificatoreAttributoMD.daCampo(new LettoreCampi(fisso.comeCampo()));
        assertEquals(fisso, riletto);
        assertFalse(riletto.isRimovibile());
    }

    @Test
    void unaNotaNullEVuotaEIlModificatoreRilettoELoStesso() throws IOException {
        ModificatoreAttributoMD senzaNota = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2.0d, null);
        assertEquals("", senzaNota.getNote());
        assertEquals(senzaNota, ModificatoreAttributoMD.daCampo(new LettoreCampi(senzaNota.comeCampo())));
        assertEquals(senzaNota, new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2.0d));
    }

    @Test
    void laQuantitaSiConfrontaComeUnDouble() {
        ModificatoreAttributoMD nan = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, Double.NaN);
        assertEquals(nan, new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, Double.NaN));
        assertEquals(nan.hashCode(), new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, Double.NaN).hashCode());
    }

    @Test
    void unCampoSenzaNotaSiRileggeConLaNotaVuota() throws IOException {
        ModificatoreAttributoMD riletto = ModificatoreAttributoMD.daCampo(new LettoreCampi("FORZA§VARIAZIONE_FISSA§2.0§PERMANENTE§0§PORTATORE§true"));
        assertEquals(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2.0d), riletto);
    }
}
