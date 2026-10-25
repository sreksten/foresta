package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.mappa.TipoLocazione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La mappa della Foresta: leggere e cambiare le caselle, sapere quali il gruppo conosce o ha visitato, trovare una casella libera e
 * trasformare le locazioni uniche.
 */
class ForestaTest {

    private static final CoordinateMD QUI = new CoordinateMD(3, 4);

    @BeforeEach
    void nuovaMappa() {
        ForestaMD dati = new ForestaMD();
        Foresta.setFornitoreDiDati(() -> dati);
        Foresta.azzera(10, 8);
    }

    // Una mappa piena di bosco: sapere cosa c'è in una casella presuppone che una locazione ci sia
    private static void riempiDiBosco() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.BOSCO);
            }
        }
    }

    @Test
    void laMappaHaLeDimensioniDateEUnaCasellaVuotaNonHaTipo() {
        assertEquals(10, Foresta.getDimensioneX());
        assertEquals(8, Foresta.getDimensioneY());
        assertNull(Foresta.getLocazione(QUI));
        assertNull(Foresta.getLocazione(3, 4));
        assertTrue(Foresta.isDentroLaMappa(new CoordinateMD(0, 0)));
        assertTrue(Foresta.isDentroLaMappa(new CoordinateMD(9, 7)));
        assertFalse(Foresta.isDentroLaMappa(new CoordinateMD(-1, 0)));
        assertFalse(Foresta.isDentroLaMappa(new CoordinateMD(10, 0)));
        assertFalse(Foresta.isDentroLaMappa(new CoordinateMD(0, -1)));
        assertFalse(Foresta.isDentroLaMappa(new CoordinateMD(0, 8)));
    }

    @Test
    void unaCasellaSiImpostaESiLegge() {
        Foresta.impostaLocazione(QUI, TipoLocazione.TEMPIO);
        assertEquals(TipoLocazione.TEMPIO, Foresta.getLocazione(QUI));
        assertEquals(TipoLocazione.TEMPIO, Foresta.getLocazione(3, 4));
        LocazioneMD md = Foresta.getLocazioneMD(QUI);
        assertEquals(TipoLocazione.TEMPIO, md.getTipo());
    }

    @Test
    void visitataEConosciutaSonoDueCose() {
        Foresta.impostaLocazione(QUI, TipoLocazione.BOSCO);
        assertFalse(Foresta.isLocazioneVisitata(QUI));
        assertFalse(Foresta.isLocazioneConosciuta(QUI));
        int versione = Foresta.getVersioneMappa();

        Foresta.setLocazioneVisitata(QUI);
        assertTrue(Foresta.isLocazioneVisitata(QUI));
        Foresta.setLocazioneVisitata(QUI, false);
        assertFalse(Foresta.isLocazioneVisitata(QUI));
        Foresta.setLocazioneVisitata(QUI, true);
        assertTrue(Foresta.isLocazioneVisitata(QUI));

        Foresta.setLocazioneConosciuta(QUI);
        assertTrue(Foresta.isLocazioneConosciuta(QUI));
        assertNotEquals(versione, Foresta.getVersioneMappa(), "la mappa cambiata si riconosce dalla versione");
    }

    @Test
    void ilNomeDaMostrareC_eSoloSeLaCasellaEConosciutaEDentroLaMappa() {
        Foresta.impostaLocazione(QUI, TipoLocazione.TEMPIO);
        Foresta.getLocazioneMD(QUI).setNome("il Tempio della Luna");
        assertNull(Foresta.getNomeDaMostrare(QUI), "non ancora conosciuta");
        Foresta.setLocazioneConosciuta(QUI);
        assertEquals("il Tempio della Luna", Foresta.getNomeDaMostrare(QUI));
        assertNull(Foresta.getNomeDaMostrare(new CoordinateMD(-1, 2)));
        assertNull(Foresta.getNomeDaMostrare(new CoordinateMD(2, 8)));
    }

    @Test
    void ottenereLaMappaFaConoscereTutteLeCaselle() {
        riempiDiBosco();
        Foresta.ottieniMappa();
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(0, 0)));
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(9, 7)));
        assertEquals(0, Foresta.getMinXConosciuta());
        assertEquals(9, Foresta.getMaxXConosciuta());
        assertEquals(0, Foresta.getMinYConosciuta());
        assertEquals(7, Foresta.getMaxYConosciuta());
    }

    @Test
    void unaZonaDellaMappaSiFermaAiBordi() {
        riempiDiBosco();
        Foresta.ottieniMappaZona(-2, 6, 2, 12);
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(0, 6)));
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(2, 7)));
        assertFalse(Foresta.isLocazioneConosciuta(new CoordinateMD(3, 7)));
        assertFalse(Foresta.isLocazioneConosciuta(new CoordinateMD(0, 5)));
        assertEquals(0, Foresta.getMinXConosciuta());
        assertEquals(2, Foresta.getMaxXConosciuta());
        assertEquals(6, Foresta.getMinYConosciuta());
        assertEquals(7, Foresta.getMaxYConosciuta());
    }

    @Test
    void unaZonaOltreIlBordoDestroESuperioreSiFermaAncheLi() {
        riempiDiBosco();
        Foresta.ottieniMappaZona(8, -3, 12, 1);
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(9, 0)));
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(8, 1)));
        assertFalse(Foresta.isLocazioneConosciuta(new CoordinateMD(8, 2)));
        assertEquals(8, Foresta.getMinXConosciuta());
        assertEquals(9, Foresta.getMaxXConosciuta());
        assertEquals(0, Foresta.getMinYConosciuta());
        assertEquals(1, Foresta.getMaxYConosciuta());
    }

    @Test
    void unaCasellaLiberaEVuotaOUnBosco() {
        assertTrue(Foresta.isLibera(null));
        assertTrue(Foresta.isLibera(TipoLocazione.BOSCO));
        assertFalse(Foresta.isLibera(TipoLocazione.RADURA));
        assertFalse(Foresta.isLibera(TipoLocazione.TEMPIO));
        // Tutta la mappa è occupata tranne una casella, che è un bosco: è l'unica che si può trovare
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 8; y++) {
                Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.TEMPIO);
            }
        }
        Foresta.impostaLocazione(QUI, TipoLocazione.BOSCO);
        CoordinateMD trovata = Foresta.getCoordinateLibere();
        assertEquals(QUI.getX(), trovata.getX());
        assertEquals(QUI.getY(), trovata.getY());
    }

    @Test
    void laLocazioneUnicaSiCostruisceEIlSuoNomeEQuelloProprio() {
        CoordinateMD coordinate = Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_NYENA, false);
        assertEquals(TipoLocazione.CITTA_NYENA, Foresta.getLocazione(coordinate));
        assertEquals(TipoLocazione.CITTA_NYENA.getNomeProprio(), Foresta.getLocazioneMD(coordinate).getNome());
        assertEquals(coordinate, Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        assertFalse(Foresta.isLocazioneConosciuta(coordinate));

        CoordinateMD altra = Foresta.costruisciLocazioneUnica(TipoLocazione.CASTELLO_IDRA, QUI, true);
        assertEquals(QUI, altra);
        assertTrue(Foresta.isLocazioneConosciuta(QUI));
    }

    @Test
    void siCostruisconoSoloLeLocazioniUnicheEDiLocazioneUnicaSiParlaSoloDiQuelle() {
        assertThrows(IllegalArgumentException.class, () -> Foresta.costruisciLocazioneUnica(TipoLocazione.BOSCO, false));
        assertThrows(IllegalArgumentException.class, () -> Foresta.getCoordinateLocazioneUnica(TipoLocazione.BOSCO));
    }

    @Test
    void ilDragoDistruggeUnaCittaERestanoDelleRovineCheNeConservanoIlNome() {
        CoordinateMD coordinate = Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_NYENA, true);
        Foresta.distruggiLocazioneUnica(TipoLocazione.CITTA_NYENA, TipoLocazione.ROVINE);
        assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(coordinate));
        assertTrue(Foresta.getLocazioneMD(coordinate).getNome().startsWith("le Rovine "));
        assertNull(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA), "non è più unica");
    }

    @Test
    void distruggereUnaLocazioneUnicaNonCostruitaNonFaNiente() {
        Foresta.distruggiLocazioneUnica(TipoLocazione.CASTELLO_IDRA, TipoLocazione.ROVINE);
        assertNull(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CASTELLO_IDRA));
    }

    @Test
    void distruggereUnaLocazioneUnicaSenzaFarneRovineNonLeDaIlNomeDelleRovine() {
        CoordinateMD coordinate = Foresta.costruisciLocazioneUnica(TipoLocazione.CASTELLO_IDRA, false);
        Foresta.distruggiLocazioneUnica(TipoLocazione.CASTELLO_IDRA, TipoLocazione.BOSCO);
        assertEquals(TipoLocazione.BOSCO, Foresta.getLocazione(coordinate));
    }

    @Test
    void unNomeNuovoEviteQuelliGiaDatiAUnaCasellaDelloStessoTipo() {
        Foresta.impostaLocazione(new CoordinateMD(0, 0), TipoLocazione.TEMPIO);
        Foresta.getLocazioneMD(new CoordinateMD(0, 0)).setNome("il Tempio A");
        // Un'altra casella dello stesso tipo ma senza nome, e una di un altro tipo con lo stesso nome: non contano
        Foresta.impostaLocazione(new CoordinateMD(1, 0), TipoLocazione.TEMPIO);
        Foresta.impostaLocazione(new CoordinateMD(2, 0), TipoLocazione.LOCANDA);
        Foresta.getLocazioneMD(new CoordinateMD(2, 0)).setNome("il Tempio B");
        String[] proposte = {"il Tempio A", "il Tempio A", "il Tempio B"};
        AtomicInteger i = new AtomicInteger();
        assertEquals("il Tempio B", Foresta.nomeNuovo(TipoLocazione.TEMPIO, () -> proposte[i.getAndIncrement()]));
        assertEquals(3, i.get());
    }

    @Test
    void seUnNomeNuovoNonSiTrovaVaBeneUnDoppione() {
        Foresta.impostaLocazione(new CoordinateMD(0, 0), TipoLocazione.TEMPIO);
        Foresta.getLocazioneMD(new CoordinateMD(0, 0)).setNome("il Tempio A");
        AtomicInteger tentativi = new AtomicInteger();
        assertEquals("il Tempio A", Foresta.nomeNuovo(TipoLocazione.TEMPIO, () -> {
            tentativi.incrementAndGet();
            return "il Tempio A";
        }));
        assertEquals(20, tentativi.get());
    }

    @Test
    void senzaUnFornitoreDiDatiLaMappaNonSaDoveLeggere() {
        Foresta.setFornitoreDiDati(null);
        assertThrows(IllegalStateException.class, Foresta::getDimensioneX);
    }

    @Test
    void ilFornitoreSiChiamaAOgniUsoCosiUnSalvataggioCaricatoSiVedeSubito() {
        ForestaMD altra = new ForestaMD();
        altra.reimposta(5, 5);
        ForestaMD[] corrente = {altra};
        Foresta.setFornitoreDiDati(() -> corrente[0]);
        assertEquals(5, Foresta.getDimensioneX());
        ForestaMD terza = new ForestaMD();
        terza.reimposta(7, 7);
        corrente[0] = terza;
        assertEquals(7, Foresta.getDimensioneX());
    }

    @Test
    void lePartiDelleNotificheEDegliEventiDellaMappaPortanoIlLoroDato() {
        com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneConoscenzaMappa zona =
                new com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneConoscenzaMappa(1, 2, 3, 4);
        assertEquals(1, zona.getDaX());
        assertEquals(2, zona.getDaY());
        assertEquals(3, zona.getAX());
        assertEquals(4, zona.getAY());
        assertEquals(TipoLocazione.TEMPIO, new com.threeamigos.foresta.eventi.interni.InternoLocazioneVisitata(TipoLocazione.TEMPIO).getTipo());
    }

    @Test
    void laZonaIntornoAlGruppoSiVedeSettePerSetteECeSiSpostaAiBordi() {
        riempiDiBosco();
        // Al centro (la mappa è 10 per 8): le tre caselle per lato
        Foresta.ottieniMappaIntorno(5, 4);
        assertEquals(2, Foresta.getMinXConosciuta());
        assertEquals(8, Foresta.getMaxXConosciuta());
        assertEquals(1, Foresta.getMinYConosciuta());
        assertEquals(7, Foresta.getMaxYConosciuta());

        // Negli angoli la zona resta di sette per sette ma dentro la mappa
        riempiDiBosco();
        Foresta.azzera(10, 8);
        riempiDiBosco();
        Foresta.ottieniMappaIntorno(0, 0);
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(6, 6)));
        assertFalse(Foresta.isLocazioneConosciuta(new CoordinateMD(7, 0)));
        assertFalse(Foresta.isLocazioneConosciuta(new CoordinateMD(0, 7)));

        Foresta.azzera(10, 8);
        riempiDiBosco();
        Foresta.ottieniMappaIntorno(9, 7);
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(3, 1)));
        assertFalse(Foresta.isLocazioneConosciuta(new CoordinateMD(2, 1)));
        assertFalse(Foresta.isLocazioneConosciuta(new CoordinateMD(3, 0)));
    }

    @Test
    void unoSpostamentoFaAlMassimoCinqueCaselle() {
        assertEquals(5, Foresta.MAX_MOVIMENTO);
    }

    @Test
    void ilNomeDelTempioSiPescaUnaVoltaESiRicorda() {
        Foresta.impostaLocazione(QUI, TipoLocazione.TEMPIO);
        String nome = Foresta.nomeDelTempio(QUI);
        assertNotNull(nome);
        assertEquals(nome, Foresta.nomeDelTempio(Foresta.getLocazioneMD(QUI)), "il nome non cambia");
    }

    @Test
    void ilNomeDelleRovineSiPescaUnaVoltaESiRicorda() {
        Foresta.impostaLocazione(QUI, TipoLocazione.ROVINE);
        String nome = Foresta.nomeDelleRovine(Foresta.getLocazioneMD(QUI));
        assertNotNull(nome);
        assertEquals(nome, Foresta.nomeDelleRovine(Foresta.getLocazioneMD(QUI)));
    }

    @Test
    void laVarianteDelBoscoSiScriveESiLegge() {
        LocazioneMD md = new LocazioneMD(TipoLocazione.BOSCO);
        assertEquals(1, Foresta.getVarianteDelBosco(md), "senza variante vale la prima");
        Foresta.impostaVarianteDelBoscoACaso(md);
        int variante = Foresta.getVarianteDelBosco(md);
        assertTrue(variante >= 1 && variante <= Foresta.NUMERO_VARIANTI_BOSCO);
    }

    @Test
    void costruireUnaLocazioneOrdinariaLaFaNonVisitataEDaIlNomeAiTempli() {
        Foresta.impostaLocazione(QUI, TipoLocazione.BOSCO);
        Foresta.setLocazioneVisitata(QUI, true);

        Foresta.costruisciLocazione(QUI, TipoLocazione.TEMPIO);

        assertEquals(TipoLocazione.TEMPIO, Foresta.getLocazione(QUI));
        assertFalse(Foresta.isLocazioneVisitata(QUI));
        assertNotNull(Foresta.getLocazioneMD(QUI).getNome(), "il tempio ha il suo nome da quando nasce");
        assertThrows(IllegalArgumentException.class, () -> Foresta.costruisciLocazione(QUI, TipoLocazione.CITTA_NYENA));
    }
}
