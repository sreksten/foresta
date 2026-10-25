package com.threeamigos.foresta.tipi.tattiche;

import com.threeamigos.foresta.tipi.TipoPredicato;
import com.threeamigos.foresta.tipi.PortataAzione;
import com.threeamigos.foresta.tipi.TipoFiltro;
import com.threeamigos.foresta.tipi.TipoRosa;
import com.threeamigos.foresta.tipi.TipoOrdinamento;
import com.threeamigos.foresta.tipi.TipoConfronto;
import com.threeamigos.foresta.tipi.TipoParametro;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
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
        assertEquals(EnumSet.of(TipoRosa.AVVERSARI), TipoAzione.ATTACCO_FISICO.roseAmmesse(true));
        assertEquals(EnumSet.of(TipoRosa.AVVERSARI, TipoRosa.OSPITI, TipoRosa.AVVERSARI_E_OSPITI), TipoAzione.ATTACCO_FISICO.roseAmmesse(false));
    }

    @Test
    void leCureDeiMostriNonRiguardanoGliOspiti() {
        assertEquals(EnumSet.of(TipoRosa.SE_STESSO, TipoRosa.ALLEATI, TipoRosa.OSPITI, TipoRosa.ALLEATI_E_OSPITI),
                TipoAzione.POZIONE_SALUTE.roseAmmesse(true));
        assertEquals(EnumSet.of(TipoRosa.SE_STESSO, TipoRosa.ALLEATI), TipoAzione.POZIONE_SALUTE.roseAmmesse(false));
    }

    @Test
    void lePortateDiGruppoECambioDArmaHannoUnaRosaSolaPerTutti() {
        for (boolean delGiocatore : new boolean[]{true, false}) {
            assertEquals(EnumSet.of(TipoRosa.ALLEATI_CADUTI), TipoAzione.RESURREZIONE.roseAmmesse(delGiocatore));
            assertEquals(EnumSet.of(TipoRosa.ALLEATI), TipoAzione.ALBA_SACRA.roseAmmesse(delGiocatore));
            assertEquals(EnumSet.of(TipoRosa.AVVERSARI), TipoAzione.CANTO_IPNOTICO.roseAmmesse(delGiocatore));
            assertEquals(EnumSet.of(TipoRosa.SE_STESSO), TipoAzione.CAMBIA_ARMA.roseAmmesse(delGiocatore));
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
            assertEquals(abilita.name(), TipoAzione.di(abilita).name());
        }
        assertEquals(TipoAzione.CANTO_CURATIVO, TipoAzione.di(TipoCanto.CURATIVO));
        assertEquals(TipoAzione.CANTO_IPNOTICO, TipoAzione.di(TipoCanto.IPNOTICO));
        assertEquals(TipoAzione.CANTO_DI_VALORE, TipoAzione.di(TipoCanto.DI_VALORE));
        assertEquals(TipoAzione.URLO_DELLA_BANSHEE, TipoAzione.di(TipoCanto.URLO_DELLA_BANSHEE));
        assertSame(TipoDanno.ARCANO, TipoAzione.DARDO_ARCANO.getTipoDanno(), "il dardo arcano non è un incantesimo ma fa danno arcano");
        assertNull(TipoAzione.ATTACCO_FISICO.getTipoDanno(), "dipende dall'arma");
        assertNull(TipoAzione.CANTO_CURATIVO.getTipoDanno());
        for (TipoAzione azione : TipoAzione.values()) {
            assertNotNull(azione.getPortata(), azione.name());
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
