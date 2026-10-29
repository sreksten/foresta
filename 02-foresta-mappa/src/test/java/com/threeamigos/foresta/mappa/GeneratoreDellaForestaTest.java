package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.Quadrante;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale;
import com.threeamigos.foresta.testi.ProduttoriDiTesti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La Foresta nuova: città una per quadrante, locande per i personaggi da piazzare, templi per gli artefatti, il resto grotte, paludi,
 * rovine, radure e bosco; a chi la abita pensa il popolatore, che qui è finto.
 */
class GeneratoreDellaForestaTest {

    private static final int DIMENSIONE = 20;

    private ForestaMD dati;

    /** Un popolatore che prende nota di quel che gli viene detto, con quanti personaggi e artefatti da piazzare. */
    private static class PopolatoreFinto implements PopolatoreDellaForesta {
        int personaggi;
        int artefatti;
        final List<TipoLocazione> citta = new ArrayList<>();
        final List<CoordinateMD> locande = new ArrayList<>();
        final List<CoordinateMD> templi = new ArrayList<>();
        final List<String> ordine = new ArrayList<>();

        PopolatoreFinto(int personaggi, int artefatti) {
            this.personaggi = personaggi;
            this.artefatti = artefatti;
        }

        @Override
        public void impostaDatiLocanda(LocazioneMD locazione, ProduttoreDiLocandeCasuale.DatiLocanda datiLocanda) {
            locazione.aggiungiProprieta("LOCANDA_PROVA", datiLocanda.getNome());
        }

        @Override
        public void cittaCostruita(TipoLocazione tipo, CoordinateMD coordinate) {
            citta.add(tipo);
            ordine.add("citta");
        }

        @Override
        public void dopoLeCitta() {
            ordine.add("dopoLeCitta");
        }

        @Override
        public boolean prelevaPersonaggio() {
            return personaggi-- > 0;
        }

        @Override
        public void locandaCostruita(CoordinateMD coordinate) {
            locande.add(coordinate);
            ordine.add("locanda");
        }

        @Override
        public boolean prelevaArtefatto() {
            return artefatti-- > 0;
        }

        @Override
        public void tempioCostruito(CoordinateMD coordinate) {
            templi.add(coordinate);
            ordine.add("tempio");
        }
    }

    @BeforeEach
    void preparaLaMappa() {
        dati = new ForestaMD();
        Foresta.setFornitoreDiDati(() -> dati);
        // I nomi delle locande si pescano una volta sola per partita: ogni prova è una partita nuova
        ProduttoriDiTesti.resetProduzioni();
        Dado.impostaSeme(7);
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private static int contaLeCaselle(TipoLocazione tipo) {
        int n = 0;
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == tipo) {
                    n++;
                }
            }
        }
        return n;
    }

    @Test
    void ogniCasellaHaUnaLocazioneEQuattroCittaStannoInQuattroQuadranti() {
        PopolatoreFinto popolatore = new PopolatoreFinto(3, 2);

        GeneratoreDellaForesta.genera(DIMENSIONE, DIMENSIONE, 3, popolatore);

        for (int x = 0; x < DIMENSIONE; x++) {
            for (int y = 0; y < DIMENSIONE; y++) {
                assertNotNull(Foresta.getLocazione(x, y), x + "," + y);
            }
        }
        assertEquals(4, popolatore.citta.size());
        Set<Quadrante> quadranti = EnumSet.noneOf(Quadrante.class);
        for (TipoLocazione citta : popolatore.citta) {
            quadranti.add(Quadrante.di(Foresta.getCoordinateLocazioneUnica(citta)));
        }
        assertEquals(EnumSet.allOf(Quadrante.class), quadranti, "una per quadrante");
    }

    @Test
    void leLocandeSonoUnaPerPersonaggioEPoiTantePerArrivareAUnQuartoDelLato() {
        PopolatoreFinto popolatore = new PopolatoreFinto(3, 2);

        GeneratoreDellaForesta.genera(DIMENSIONE, DIMENSIONE, 3, popolatore);

        assertEquals(3, popolatore.locande.size(), "una per ognuno dei tre personaggi");
        assertEquals((DIMENSIONE + DIMENSIONE) >> 2, contaLeCaselle(TipoLocazione.LOCANDA), "e in tutto quanto vuole la media");
        for (CoordinateMD coordinate : popolatore.locande) {
            assertEquals(TipoLocazione.LOCANDA, Foresta.getLocazione(coordinate));
            assertNotNull(Foresta.getLocazioneMD(coordinate).ottieniProprieta("LOCANDA_PROVA"), "i dati della locanda li scrive il popolatore");
        }
    }

    @Test
    void conPiuPersonaggiDelleLocandeDellaMediaSeNeCostruisconoDiPiu() {
        PopolatoreFinto popolatore = new PopolatoreFinto(8, 0);

        GeneratoreDellaForesta.genera(DIMENSIONE, DIMENSIONE, 8, popolatore);

        assertEquals(8, popolatore.locande.size());
        assertEquals(10, contaLeCaselle(TipoLocazione.LOCANDA), "non meno di quelle che servono ai personaggi");
    }

    @Test
    void iTempliSonoUnoPerArtefattoEAlmenoMetaSenzaNeSonoLiberi() {
        PopolatoreFinto popolatore = new PopolatoreFinto(0, 3);

        GeneratoreDellaForesta.genera(DIMENSIONE, DIMENSIONE, 0, popolatore);

        assertEquals(3, popolatore.templi.size());
        // media = 10: i templi senza artefatto sono max(3, 10 - 3) = 7
        assertEquals(3 + 7, contaLeCaselle(TipoLocazione.TEMPIO));
        for (CoordinateMD coordinate : popolatore.templi) {
            assertNotNull(Foresta.getLocazioneMD(coordinate).getNome(), "il tempio ha il suo nome da quando nasce");
        }
    }

    @Test
    void ilRestoDellaForestaEFattoDiGrotteSerPaludiRovineRadureEBosco() {
        GeneratoreDellaForesta.genera(DIMENSIONE, DIMENSIONE, 0, new PopolatoreFinto(0, 0));

        int media = DIMENSIONE;
        assertEquals(media >> 1, contaLeCaselle(TipoLocazione.GROTTA));
        assertEquals(media, contaLeCaselle(TipoLocazione.PALUDE));
        assertEquals(media, contaLeCaselle(TipoLocazione.ROVINE));
        assertEquals(media >> 1, contaLeCaselle(TipoLocazione.RADURA));
        assertTrue(contaLeCaselle(TipoLocazione.BOSCO) > 0);
        // Ogni bosco ha la sua variante di immagine, e le rovine il loro nome
        for (int x = 0; x < DIMENSIONE; x++) {
            for (int y = 0; y < DIMENSIONE; y++) {
                LocazioneMD md = Foresta.getLocazioneMD(new CoordinateMD(x, y));
                if (md.getTipo() == TipoLocazione.BOSCO) {
                    int variante = Foresta.getVarianteDelBosco(md);
                    assertTrue(variante >= 1 && variante <= Foresta.NUMERO_VARIANTI_BOSCO, "variante " + variante);
                } else if (md.getTipo() == TipoLocazione.ROVINE) {
                    assertNotNull(md.getNome());
                }
            }
        }
    }

    @Test
    void ilPopolatoreSentePrimaLeCittaPoiLeLocandePoiITempli() {
        PopolatoreFinto popolatore = new PopolatoreFinto(1, 1);

        GeneratoreDellaForesta.genera(DIMENSIONE, DIMENSIONE, 1, popolatore);

        assertEquals(java.util.Arrays.asList("citta", "citta", "citta", "citta", "dopoLeCitta", "locanda", "tempio"), popolatore.ordine);
    }

    @Test
    void unaCasellaLiberaDiUnQuadranteStaNelQuadrante() {
        GeneratoreDellaForesta.genera(DIMENSIONE, DIMENSIONE, 0, new PopolatoreFinto(0, 0));
        // Una Foresta tutta piena di bosco, tranne una casella: l'unica libera di quel quadrante è un bosco
        for (Quadrante quadrante : Quadrante.values()) {
            for (int i = 0; i < 20; i++) {
                CoordinateMD coordinate = GeneratoreDellaForesta.getCoordinateLibere(quadrante);
                assertTrue(quadrante.contiene(coordinate));
                assertTrue(Foresta.isLibera(Foresta.getLocazione(coordinate)));
            }
        }
    }
}
