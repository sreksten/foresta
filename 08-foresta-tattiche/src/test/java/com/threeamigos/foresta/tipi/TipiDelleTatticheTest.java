package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le regole delle enum delle tattiche: da quali rose pesca un'azione, il suo tipo di danno, l'incantesimo che usa, i
 * confronti e i parametri di filtri, ordinamenti e predicati.
 */
class TipiDelleTatticheTest {

    @Test
    void unAttaccoDeiMieiNonColpisceGliOspitiQuelloDiUnMostroSi() {
        assertEquals(EnumSet.of(TipoRosa.AVVERSARI), PortataAzione.roseAmmesse(TipoAzione.ATTACCO_FISICO, true));
        assertEquals(EnumSet.of(TipoRosa.AVVERSARI, TipoRosa.OSPITI, TipoRosa.AVVERSARI_E_OSPITI), PortataAzione.roseAmmesse(TipoAzione.ATTACCO_FISICO, false));
    }

    @Test
    void leCureDeiMostriNonRiguardanoGliOspiti() {
        assertEquals(EnumSet.of(TipoRosa.SE_STESSO, TipoRosa.ALLEATI, TipoRosa.OSPITI, TipoRosa.ALLEATI_E_OSPITI),
                PortataAzione.roseAmmesse(TipoAzione.POZIONE_SALUTE, true));
        assertEquals(EnumSet.of(TipoRosa.SE_STESSO, TipoRosa.ALLEATI), PortataAzione.roseAmmesse(TipoAzione.POZIONE_SALUTE, false));
    }

    @Test
    void lePortateDiGruppoECambioDArmaHannoUnaRosaSolaPerTutti() {
        for (boolean delGiocatore : new boolean[]{true, false}) {
            assertEquals(EnumSet.of(TipoRosa.ALLEATI_CADUTI), PortataAzione.roseAmmesse(TipoAzione.RESURREZIONE, delGiocatore));
            assertEquals(EnumSet.of(TipoRosa.ALLEATI), PortataAzione.roseAmmesse(TipoAzione.ALBA_SACRA, delGiocatore));
            assertEquals(EnumSet.of(TipoRosa.AVVERSARI), PortataAzione.roseAmmesse(TipoAzione.CANTO_IPNOTICO, delGiocatore));
            assertEquals(EnumSet.of(TipoRosa.SE_STESSO), PortataAzione.roseAmmesse(TipoAzione.CAMBIA_ARMA, delGiocatore));
        }
    }

    @Test
    void leRoseAmmesseSonoUnaCopiaEValgonoPerChiDecide() {
        for (PortataAzione portata : PortataAzione.values()) {
            for (boolean delGiocatore : new boolean[]{true, false}) {
                Set<TipoRosa> rose = portata.roseAmmesse(delGiocatore);
                assertFalse(rose.isEmpty(), portata.name());
                for (TipoRosa rosa : rose) {
                    assertTrue(rosa.valePer(delGiocatore), portata + ": " + rosa + " non vale per " + (delGiocatore ? "il giocatore" : "i mostri"));
                }
                rose.clear();
                assertFalse(portata.roseAmmesse(delGiocatore).isEmpty(), "chi riceve le rose non cambia quelle della portata");
            }
        }
    }

    @Test
    void certeRoseValgonoSoloPerUnaParte() {
        assertTrue(TipoRosa.ALLEATI_E_OSPITI.valePer(true));
        assertFalse(TipoRosa.ALLEATI_E_OSPITI.valePer(false));
        assertFalse(TipoRosa.AVVERSARI_E_OSPITI.valePer(true));
        assertTrue(TipoRosa.AVVERSARI_E_OSPITI.valePer(false));
    }

    @Test
    void ogniIncantesimoHaLaSuaAzioneEIlSuoTipoDiDanno() {
        for (TipoIncantesimo incantesimo : TipoIncantesimo.values()) {
            TipoAzione azione = TipoAzione.di(incantesimo);
            assertNotNull(azione, incantesimo.name());
            assertSame(incantesimo, azione.getClasseIncantesimo());
            assertSame(incantesimo.getTipoDanno(), azione.getTipoDanno(), incantesimo.name());
        }
        assertNull(TipoAzione.di((TipoIncantesimo) null), "senza incantesimo non c'è un'azione che lo lancia");
        for (TipoAbilita abilita : TipoAbilita.values()) {
            assertEquals(abilita.name(), abilita.getTipoAzione().name());
        }
        assertEquals(TipoAzione.CANTO_CURATIVO, TipoCanto.CURATIVO.getTipoAzione());
        assertEquals(TipoAzione.CANTO_IPNOTICO, TipoCanto.IPNOTICO.getTipoAzione());
        assertEquals(TipoAzione.CANTO_DI_VALORE, TipoCanto.DI_VALORE.getTipoAzione());
        assertEquals(TipoAzione.URLO_DELLA_BANSHEE, TipoCanto.URLO_DELLA_BANSHEE.getTipoAzione());
        assertSame(TipoDanno.ARCANO, TipoAzione.DARDO_ARCANO.getTipoDanno(), "il dardo arcano non è un incantesimo ma fa danno arcano");
        assertNull(TipoAzione.ATTACCO_FISICO.getTipoDanno(), "dipende dall'arma");
        assertNull(TipoAzione.CANTO_CURATIVO.getTipoDanno());
        for (TipoAzione azione : TipoAzione.values()) {
            assertNotNull(PortataAzione.di(azione), azione.name());
        }
    }

    @Test
    void unConfrontoDiceSeIlValoreStaSottoUgualeOSopra() {
        assertTrue(TipoConfronto.MINORE.vale(1, 2));
        assertFalse(TipoConfronto.MINORE.vale(2, 2));
        assertTrue(TipoConfronto.UGUALE.vale(2, 2));
        assertFalse(TipoConfronto.UGUALE.vale(3, 2));
        assertTrue(TipoConfronto.MAGGIORE.vale(3, 2));
        assertFalse(TipoConfronto.MAGGIORE.vale(2, 2));
    }

    @Test
    void filtriOrdinamentiEPredicatiDiconoCheParametriVogliono() {
        for (TipoFiltro filtro : TipoFiltro.values()) {
            assertNotNull(filtro.getParametro(), filtro.name());
        }
        for (TipoOrdinamento ordinamento : TipoOrdinamento.values()) {
            assertNotNull(ordinamento.getParametro(), ordinamento.name());
        }
        for (TipoPredicato predicato : TipoPredicato.values()) {
            assertNotNull(predicato.getParametro(), predicato.name());
        }
        assertSame(TipoParametro.CONFRONTO_E_NUMERO, TipoFiltro.SALUTE.getParametro());
        assertSame(TipoParametro.TIPO_DANNO, TipoFiltro.DEBOLE_A.getParametro());
        assertSame(TipoParametro.NESSUNO, TipoOrdinamento.PRIMO.getParametro());
        assertSame(TipoParametro.SELEZIONE_CONFRONTO_E_NUMERO, TipoPredicato.QUANTI.getParametro());
    }
}
