package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.interni.InternoSfidaCartaForbiciSasso;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CostantiMissioni;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I pezzi piccoli delle missioni: la partita a carta, forbici e sasso, gli incontri, gli oggetti da raccogliere, i testi dei luoghi, le
 * missioni secondarie e l'intermezzo di un passo.
 */
class ComponentiDelleMissioniTest {

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(5);
        RegistroMissioni.reimposta();
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    // ---- Carta, forbici e sasso

    @Test
    void laMossaChiBatteChi() {
        assertTrue(MossaCartaForbiciSasso.CARTA.batte(MossaCartaForbiciSasso.SASSO));
        assertTrue(MossaCartaForbiciSasso.SASSO.batte(MossaCartaForbiciSasso.FORBICE));
        assertTrue(MossaCartaForbiciSasso.FORBICE.batte(MossaCartaForbiciSasso.CARTA));
        assertFalse(MossaCartaForbiciSasso.SASSO.batte(MossaCartaForbiciSasso.CARTA));
    }

    @Test
    void laPartitaSiGiocaFinoAlleVittorie() {
        assertTrue(PartitaCartaForbiciSasso.puoGiocare(TipoPersonaggio.GOBLIN));
        assertFalse(PartitaCartaForbiciSasso.puoGiocare(TipoPersonaggio.OMBRAFIAMMA));
        PartitaCartaForbiciSasso partita = new PartitaCartaForbiciSasso();
        assertFalse(partita.isFinita());
        int mani = 0;
        boolean visto[] = new boolean[3];
        while (!partita.isFinita() && mani++ < 500) {
            PartitaCartaForbiciSasso.Mano mano = partita.gioca(MossaCartaForbiciSasso.values()[mani % 3]);
            assertNotNull(mano.getGiocatore());
            assertNotNull(mano.getAvversario());
            visto[mano.isPareggio() ? 0 : mano.haVintoIlGiocatore() ? 1 : 2] = true;
        }
        assertTrue(partita.isFinita());
        assertEquals(partita.getVittorieDelGiocatore() >= CostantiMissioni.VITTORIE_PER_VINCERE_LA_SFIDA, partita.haVintoIlGiocatore());
        assertTrue(partita.getVittorieDelGiocatore() + partita.getVittorieDellAvversario() >= CostantiMissioni.VITTORIE_PER_VINCERE_LA_SFIDA);
        assertThrows(IllegalStateException.class, () -> partita.gioca(MossaCartaForbiciSasso.CARTA));
        assertTrue(visto[1] || visto[2]);
    }

    @Test
    void ognEsitoDiUnaManoUnaVoltaPerUna() {
        // Con il dado truccato: l'avversario gioca sempre sasso (la terza mossa)
        PartitaCartaForbiciSasso partita = new PartitaCartaForbiciSasso();
        Dado.trucca(3);
        PartitaCartaForbiciSasso.Mano pari = partita.gioca(MossaCartaForbiciSasso.SASSO);
        assertTrue(pari.isPareggio());
        assertFalse(pari.haVintoIlGiocatore());
        Dado.trucca(3);
        assertTrue(partita.gioca(MossaCartaForbiciSasso.CARTA).haVintoIlGiocatore());
        Dado.trucca(3);
        assertFalse(partita.gioca(MossaCartaForbiciSasso.FORBICE).haVintoIlGiocatore());
        assertEquals(1, partita.getVittorieDelGiocatore());
        assertEquals(1, partita.getVittorieDellAvversario());
    }

    @Test
    void gliEventiDellaSfida() {
        InternoSfidaCartaForbiciSasso apre = InternoSfidaCartaForbiciSasso.apre();
        assertTrue(apre.isInizio());
        assertFalse(apre.isFinale());
        InternoSfidaCartaForbiciSasso mano = InternoSfidaCartaForbiciSasso.mano(MossaCartaForbiciSasso.CARTA, MossaCartaForbiciSasso.FORBICE, 1, 2, true);
        assertFalse(mano.isInizio());
        assertTrue(mano.isFinale());
        assertEquals(MossaCartaForbiciSasso.CARTA, mano.getMossaDelGiocatore());
        assertEquals(MossaCartaForbiciSasso.FORBICE, mano.getMossaDellAvversario());
        assertEquals(1, mano.getPunteggioDelGiocatore());
        assertEquals(2, mano.getPunteggioDellAvversario());
    }

    // ---- Gli incontri

    @Test
    void unIncontroSiCostruisceEImponeLeSueRegole() {
        assertThrows(IllegalArgumentException.class, () -> IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 0));
        IncontroDiMissione solo = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1).aDuello().finoAllaResa().aCartaForbiciSasso();
        assertTrue(solo.isADuello());
        assertTrue(solo.isACartaForbiciSasso());
        assertTrue(solo.isFinoAllaResa());
        assertEquals(1, solo.getNumeroDiOndate());
        assertThrows(IllegalStateException.class, () -> IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 2).aDuello());
        assertThrows(IllegalStateException.class, () -> IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 2).aCartaForbiciSasso());
        assertThrows(IllegalStateException.class,
                () -> IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1).conCapo("Brogo", TipoPersonaggio.TROLL).aDuello());
        assertThrows(IllegalStateException.class,
                () -> IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1).conCapo("Brogo", TipoPersonaggio.TROLL).aCartaForbiciSasso());
        IncontroDiMissione ondata = IncontroDiMissione.di(TipoPersonaggio.TROLL, 1);
        assertThrows(IllegalStateException.class, () -> IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1).aDuello().poi(ondata, "arrivano"));
        assertThrows(IllegalStateException.class, () -> IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 2).poi(IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1).aDuello(), "x"));
        IncontroDiMissione banda = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 2).aggirabile().finoAllaResa()
                .poi(IncontroDiMissione.di(TipoPersonaggio.TROLL, 1), "Arriva il troll").poi(IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1), "E un altro");
        assertEquals(3, banda.getNumeroDiOndate());
        assertThrows(IllegalStateException.class, () -> banda.poi(IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1), "troppi"));
        assertEquals(3, banda.getSconfittiRichiesti().get(TipoPersonaggio.GOBLIN));
        assertEquals(1, banda.getSconfittiRichiesti().get(TipoPersonaggio.TROLL));
        assertEquals(2, banda.creaOndateSuccessive().size());
        assertEquals(TipoPersonaggio.GOBLIN, banda.getClasse());
        assertEquals(2, banda.getNumero());
        assertEquals(TipoPersonaggio.GOBLIN, banda.getClasseDelCapo());
        assertNull(banda.getNomeDelCapo());
        assertEquals(2, banda.crea().size());
    }

    @Test
    void ilCapoDellaBandaPuoEssereDiUnAltraClasse() {
        IncontroDiMissione conCapo = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 3).conCapo("Brogo", TipoPersonaggio.TROLL);
        assertEquals(TipoPersonaggio.TROLL, conCapo.getClasseDelCapo());
        assertEquals("Brogo", conCapo.getNomeDelCapo());
        List<Personaggio> banda = conCapo.crea();
        assertEquals(4, banda.size(), "il capo di un'altra classe si aggiunge alla banda");
        List<Personaggio> alCentro = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 3).conCapoAlCentro("Brogo", TipoPersonaggio.TROLL).crea();
        assertEquals(4, alCentro.size());
        List<Personaggio> dellaClasse = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 3).conCapo("Gnork").crea();
        assertEquals(3, dellaClasse.size());
    }

    // ---- Gli oggetti da raccogliere

    @Test
    void gliOggettiDaRaccogliereControllanoIParametri() {
        NomeOggetto nome = NomeOggetto.maschile("fungo", "funghi");
        OggettiDaRaccogliere oggetti = OggettiDaRaccogliere.di("FUNGHI", nome, 3);
        assertEquals("FUNGHI", oggetti.getChiave());
        assertSame(nome, oggetti.getNome());
        assertEquals(3, oggetti.getQuantita());
        assertTrue(oggetti.getLocazioni().isEmpty());
        assertFalse(oggetti.isTrofeo());
        assertNull(oggetti.getCasella());
        assertTrue(oggetti.getProbabilita() >= 1);
        assertTrue(oggetti.getMassimoPerLocazione() >= 1);
        assertEquals(OggettiDaRaccogliere.ORE_AL_RIPIEGO, oggetti.getOreAlRipiego());
        assertThrows(IllegalArgumentException.class, () -> oggetti.conProbabilita(0));
        assertThrows(IllegalArgumentException.class, () -> oggetti.conProbabilita(101));
        assertThrows(IllegalArgumentException.class, () -> oggetti.alPiuPerLocazione(0));
        assertThrows(NullPointerException.class, () -> oggetti.nellaCasella(null));
        assertEquals(10, oggetti.conRipiegoDopoOre(10).getOreAlRipiego());
        assertNotNull(oggetti.toString());
        assertThrows(IllegalArgumentException.class, () -> oggetti.conRipiegoDopoOre(-1));
        assertThrows(IllegalArgumentException.class, () -> OggettiDaRaccogliere.di("X", nome, 0));
    }

    @Test
    void unOggettoDiMissioneHaIlNomeDelSuoOggetto() {
        NomeOggetto nome = NomeOggetto.femminile("radice", "radici");
        OggettoMissione radici = new OggettoMissione("M1", "RADICI", nome, 3);
        assertEquals("M1", radici.getIdMissione());
        assertEquals("RADICI", radici.getChiave());
        assertEquals(TipoBottino.OGGETTO_MISSIONE, radici.getClasse());
        assertEquals(3, radici.getQuantita());
        assertEquals("radice", radici.getNomeSingolare());
        assertEquals("radici", radici.getNomePlurale());
        assertEquals(nome.getAIS(), radici.getAIS());
        assertEquals(nome.getAIP(), radici.getAIP());
        assertEquals(nome.getADS(), radici.getADS());
        assertEquals(nome.getADP(), radici.getADP());
        assertThrows(NullPointerException.class, () -> new OggettoMissione(null, "X", nome, 1));
        assertThrows(NullPointerException.class, () -> new OggettoMissione("M", null, nome, 1));
        assertThrows(NullPointerException.class, () -> new OggettoMissione("M", "X", null, 1));
    }

    // ---- I testi dei luoghi

    @Test
    void iLuoghiARoleParole() {
        for (TipoLocazione tipo : TipoLocazione.values()) {
            assertFalse(TestiDeiLuoghi.indefinito(tipo).isEmpty(), tipo.name());
            assertTrue(TestiDeiLuoghi.dentro(tipo).contains(TestiDeiLuoghi.indefinito(tipo)));
        }
        assertTrue(TestiDeiLuoghi.dentro(TipoLocazione.ROVINE).startsWith("fra "));
        assertEquals("il posto", TestiDeiLuoghi.nome(null));
        for (TipoLocazione tipo : new TipoLocazione[] {TipoLocazione.TEMPIO, TipoLocazione.ROVINE, TipoLocazione.GROTTA, TipoLocazione.BOSCO,
                TipoLocazione.PALUDE, TipoLocazione.RADURA, TipoLocazione.LOCANDA, TipoLocazione.CITTA_NYENA}) {
            CoordinateMD c = new CoordinateMD(12, 12);
            Foresta.impostaLocazione(c, tipo);
            assertFalse(TestiDeiLuoghi.nome(c).isEmpty(), tipo.name());
        }
        CoordinateMD senzaNome = new CoordinateMD(13, 13);
        Foresta.impostaLocazione(senzaNome, TipoLocazione.ALCHIMISTA);
        assertEquals("la locanda", TestiDeiLuoghi.nome(senzaNome));
    }

    // ---- Le missioni secondarie e la base

    @Test
    void laBaseDelleMissioni() {
        PassiSinteticiTest.Sintetica madre = new PassiSinteticiTest.Sintetica();
        madre.getModelloDati().setId("MADRE");
        PassiSinteticiTest.Sintetica figlia = new PassiSinteticiTest.Sintetica();
        figlia.getModelloDati().setId("FIGLIA");
        madre.aggiungiMissione(figlia);
        madre.aggiungiMissione(figlia);
        assertEquals(1, madre.getMissioniSecondarie().size());
        madre.rimuoviMissione(figlia);
        assertTrue(madre.getMissioniSecondarie().isEmpty());
        madre.sostituisciMissioniSecondarie(Collections.singletonList(figlia));
        assertEquals(1, madre.getMissioniSecondarie().size());
        madre.nascondiDescrizione();
        assertFalse(madre.isDescrizioneVisibile());
        madre.mostraDescrizione();
        assertTrue(madre.isDescrizioneVisibile());
        assertEquals("Sintetica", madre.toString());
        madre.setModelloDati(figlia.getModelloDati());
        assertTrue(madre.getMissioniSecondarie().isEmpty(), "con altri dati le secondarie si dimenticano");
        madre.completaMissione();
        madre.completaMissione();
        assertTrue(madre.isCompleta());
        madre.getNome();
    }

    @Test
    void unPassoConIntermezzoEUnIntermezzoDelRegistro() {
        PassiSinteticiTest.Sintetica m = new PassiSinteticiTest.Sintetica();
        m.getModelloDati().setId("CONSCENA");
        RegistroMissioni.aggiungiMissioneSecondaria(m);
        m.passo("A", s -> Passo.quando(Passo.MomentoControllo.PRE_LOCAZIONE, () -> true)
                .conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, Collections::emptyList).poi(Passo.FINE));
        IntermezzoDiPasso intermezzo = new IntermezzoDiPasso(m, "A");
        assertEquals("CONSCENA/A", intermezzo.getId());
        assertSame(m, intermezzo.getMissione());
        assertEquals("A", intermezzo.getIdPasso());
        assertTrue(intermezzo.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertFalse(intermezzo.deveScattare(MomentoIntermezzo.LOCAZIONE_COMPLETATA));
        assertTrue(intermezzo.getPagine().isEmpty());
        intermezzo.segnaScattato();
        assertFalse(intermezzo.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
    }
}
