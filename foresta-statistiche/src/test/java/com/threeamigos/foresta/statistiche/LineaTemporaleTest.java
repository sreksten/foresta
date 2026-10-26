package com.threeamigos.foresta.statistiche;

import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'ora e il giorno della partita, e quel che il passare dei giorni provoca: il Drago che distrugge le città e la fine del gioco.
 */
class LineaTemporaleTest {

    private LineaTemporaleMD dati;
    private GruppoGiocatore gruppo;

    @BeforeEach
    void preparaLaPartita() {
        dati = new LineaTemporaleMD();
        LineaTemporale.setFornitoreDiDati(() -> dati);
        ForestaMD forestaMD = new ForestaMD();
        Foresta.setFornitoreDiDati(() -> forestaMD);
        Foresta.azzera(30, 30);
        for (int x = 0; x < 30; x++) {
            for (int y = 0; y < 30; y++) {
                Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.BOSCO);
            }
        }
        DatiDiProva.nuovaPartita();
        gruppo = new GruppoGiocatore();
        gruppo.aggiungiPersonaggio(new Guerriero("Arsenio", 1));
        Foresta.setPosizioneDelGruppo(new CoordinateMD(0, 0));
    }

    @AfterEach
    void ripristina() {
        LineaTemporale.setFornitoreDiDati(null);
    }

    private void alGiorno(int giorno) {
        dati.setGiorno(giorno);
    }

    @Test
    void senzaUnFornitoreLaLineaTemporaleNonSaDoveLeggere() {
        LineaTemporale.setFornitoreDiDati(null);
        assertThrows(IllegalStateException.class, LineaTemporale::getOra);
    }

    @Test
    void unaPartitaNuovaCominciaAlMattinoDelPrimoGiorno() {
        dati.setOra(15);
        dati.setGiorno(7);
        LineaTemporale.reimposta();
        assertEquals(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO, LineaTemporale.getOra());
        assertEquals(1, LineaTemporale.getGiorno());
    }

    @Test
    void leOrePassanoAnchePiuDiUnGiornoAllaVolta() {
        dati.setOra(20);
        dati.setGiorno(1);
        LineaTemporale.aggiungiOre(3);
        assertEquals(23, LineaTemporale.getOra());
        assertEquals(1, LineaTemporale.getGiorno());

        LineaTemporale.aggiungiOre(5);
        assertEquals(4, LineaTemporale.getOra());
        assertEquals(2, LineaTemporale.getGiorno());

        LineaTemporale.aggiungiOre(50);
        assertEquals(6, LineaTemporale.getOra());
        assertEquals(4, LineaTemporale.getGiorno());
    }

    @Test
    void ilMattinoSeguenteEQuelloDelloStessoGiornoSeEraNotteFonda() {
        dati.setOra(3);
        dati.setGiorno(5);
        assertEquals(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO - 3, LineaTemporale.oreFinoAlMattino());
        LineaTemporale.mattinoSeguente();
        assertEquals(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO, LineaTemporale.getOra());
        assertEquals(5, LineaTemporale.getGiorno());
    }

    @Test
    void ilMattinoSeguenteEQuelloDelGiornoDopoSeLOraEGiaPassata() {
        dati.setOra(21);
        dati.setGiorno(5);
        assertEquals(24 + LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO - 21, LineaTemporale.oreFinoAlMattino());
        LineaTemporale.mattinoSeguente();
        assertEquals(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO, LineaTemporale.getOra());
        assertEquals(6, LineaTemporale.getGiorno());
    }

    @Test
    void lOraDelGiornoSiDiceAParoleConLArticoloGiusto() {
        dati.setOra(0);
        dati.setGiorno(2);
        assertTrue(LineaTemporale.getDescrizioneOraDelGiorno().startsWith("E' la mezzanotte del "), LineaTemporale.getDescrizioneOraDelGiorno());
        assertTrue(LineaTemporale.getDescrizioneOraDelGiorno().endsWith(" giorno."));

        // L'ottavo e l'undicesimo prendono l'apostrofo
        dati.setOra(12);
        dati.setGiorno(8);
        assertTrue(LineaTemporale.getDescrizioneOraDelGiorno().startsWith("E' mezzogiorno dell'"), LineaTemporale.getDescrizioneOraDelGiorno());
        dati.setGiorno(11);
        assertTrue(LineaTemporale.getDescrizioneOraDelGiorno().startsWith("E' mezzogiorno dell'"), LineaTemporale.getDescrizioneOraDelGiorno());
    }

    @Test
    void primaDelVentesimoGiornoIlDragoNonFaNiente() {
        Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_RUUNA, true);
        alGiorno(19);
        LineaTemporale.eventi(gruppo);
        assertNull(LineaTemporale.getEvento());
        assertFalse(Foresta.isCittaDistrutta(TipoLocazione.CITTA_RUUNA));
        assertFalse(LineaTemporale.isGiocoFinito());
    }

    @Test
    void dalVentesimoGiornoIlDragoDistruggeUnaCittaEIlGruppoVedeIlFumo() {
        CoordinateMD ruuna = Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_RUUNA, true);
        Foresta.setPosizioneDelGruppo(new CoordinateMD((ruuna.getX() + 10) % 30, ruuna.getY()));
        alGiorno(20);

        LineaTemporale.eventi(gruppo);

        assertTrue(Foresta.isCittaDistrutta(TipoLocazione.CITTA_RUUNA));
        assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(ruuna));
        String evento = LineaTemporale.getEvento();
        assertNotNull(evento);
        assertTrue(evento.contains("vede levarsi una colonna di fumo"), evento);
        assertNull(LineaTemporale.getEvento(), "un evento si consuma una volta sola");
    }

    @Test
    void unaCittaGiaDistruttaNonCadeDueVolte() {
        Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_RUUNA, true);
        alGiorno(20);
        LineaTemporale.eventi(gruppo);
        LineaTemporale.getEvento();

        LineaTemporale.eventi(gruppo);

        assertNull(LineaTemporale.getEvento(), "al secondo controllo dello stesso giorno non c'è altro da distruggere");
    }

    @Test
    void chiCamminaNellaCittaCheCadeTrovaSoloLeRovine() {
        CoordinateMD ruuna = Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_RUUNA, true);
        Foresta.setPosizioneDelGruppo(ruuna);
        alGiorno(20);

        LineaTemporale.eventi(gruppo);

        String evento = LineaTemporale.getEvento();
        assertTrue(evento.contains("trova solo le rovine"), evento);
    }

    @Test
    void chiDormivaNellaCittaCheCadeScampaPerUnPelo() {
        CoordinateMD ruuna = Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_RUUNA, true);
        Foresta.setPosizioneDelGruppo(ruuna);
        alGiorno(20);

        LineaTemporale.eventiDiNotte(gruppo);

        String evento = LineaTemporale.getEvento();
        assertTrue(evento.contains("per un pelo"), evento);
    }

    @Test
    void ogniCinqueGiorniCadeUnaCittaFinoAllaFine() {
        for (TipoLocazione citta : new TipoLocazione[]{TipoLocazione.CITTA_RUUNA, TipoLocazione.CITTA_NYENA,
                TipoLocazione.CITTA_FLEENA, TipoLocazione.CITTA_MALGAARD}) {
            Foresta.costruisciLocazioneUnica(citta, true);
        }

        alGiorno(35);
        LineaTemporale.eventi(gruppo);
        assertTrue(Foresta.isCittaDistrutta(TipoLocazione.CITTA_MALGAARD));
        assertFalse(Foresta.isCittaDistrutta(TipoLocazione.CITTA_FLEENA));

        LineaTemporale.eventi(gruppo);
        assertTrue(Foresta.isCittaDistrutta(TipoLocazione.CITTA_FLEENA));
        assertFalse(Foresta.isCittaDistrutta(TipoLocazione.CITTA_NYENA));

        LineaTemporale.eventi(gruppo);
        assertTrue(Foresta.isCittaDistrutta(TipoLocazione.CITTA_NYENA));
        assertFalse(Foresta.isCittaDistrutta(TipoLocazione.CITTA_RUUNA));

        LineaTemporale.eventi(gruppo);
        assertTrue(Foresta.isCittaDistrutta(TipoLocazione.CITTA_RUUNA));
    }

    @Test
    void unaCittaCheNonEsisteSullaMappaSiSegnaDistruttaSenzaUnEvento() {
        alGiorno(20);
        LineaTemporale.eventi(gruppo);
        assertTrue(Foresta.isCittaDistrutta(TipoLocazione.CITTA_RUUNA));
        assertNull(LineaTemporale.getEvento());
    }

    @Test
    void dopoIlQuarantesimoGiornoIlGiocoEPerduto() {
        alGiorno(41);
        LineaTemporale.eventi(gruppo);
        assertTrue(LineaTemporale.isGiocoFinito());
        assertTrue(LineaTemporale.getEvento().startsWith("Sventura!"));
    }

    @Test
    void ilGiocoFinitoSiPuoImpostareEQuandoIlDragoESconfittoSiTerminaComeVincitori() {
        LineaTemporale.setGiocoFinito(true);
        assertTrue(LineaTemporale.isGiocoFinito());
        LineaTemporale.setGiocoFinito(false);
        assertFalse(LineaTemporale.isGiocoFinito());
        LineaTemporale.setDragoSconfitto(true);
        assertTrue(LineaTemporale.isGiocoFinito());
    }
}
