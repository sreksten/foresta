package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoPassaggioInosservato;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaAperturaFinestraCombattimento;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.PassiSinteticiTest.Sintetica;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I rami di RegistroMissioni, e di chi gli sta accanto, che il giocatore automatico non raggiunge: il gruppo senza una casella, i segnalini
 * di missioni ferme o su caselle ignote, le ricerche su una mappa senza posti liberi o con artefatti, i quadranti dei castelli.
 */
class RegistroMissioniResiduiTest {

    private static final MomentoControllo PRE = MomentoControllo.PRE_LOCAZIONE;

    /** Una missione da niente, che dice nome e descrizione con quelli del suo modello dati (come nessuna missione vera). */
    private static final class Minima extends MissioneBase {
        Minima() {
            super(MissionePredefinita.MISSIONE_DI_PROVA);
        }

        @Override
        public void controllaPreLocazione() {
        }

        @Override
        public void controllaInLocazione() {
        }

        @Override
        public void controllaPostLocazione() {
        }
    }

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(51);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        FabbricaMissioni.registra(MissionePredefinita.MISSIONE_DI_PROVA, Sintetica::new);
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private Sintetica nuova(String id) {
        Sintetica m = new Sintetica();
        m.getModelloDati().setId(id);
        m.passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        RegistroMissioni.aggiungiMissioneSecondaria(m);
        return m;
    }

    /** Una mappa in cui il gruppo non ha una casella: come prima che la partita cominci. */
    private static void senzaPosizione() {
        ForestaMD md = new ForestaMD();
        Foresta.setFornitoreDiDati(() -> md);
        Foresta.azzera(MondoDiProva.LATO, MondoDiProva.LATO);
        md.setPosizioneDelGruppo(null);
    }

    @Test
    void ilModelloDatiDiUnaMissioneBaseDiceNomeEDescrizione() {
        Minima m = new Minima();
        m.getModelloDati().setNome("Una missione");
        m.getModelloDati().setDescrizione("Che fa poco");
        assertEquals("Una missione", m.getNome());
        assertEquals("Che fa poco", m.getDescrizione());
        m.controllaPreLocazione();
        m.controllaInLocazione();
        m.controllaPostLocazione();
    }

    @Test
    void senzaUnaCasellaGliEventiNonSiRegistranoPerCasella() {
        Sintetica m = nuova("M1");
        m.attivaMissione();
        senzaPosizione();
        BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
        BusEventi.pubblica(new InternoPassaggioInosservato());
        BusEventi.pubblica(new InternoRichiestaAperturaFinestraCombattimento(null, null));
        assertEquals(1, m.getConteggioNelPassoCorrente(MissioneAPassi.EVENTO_COMBATTIMENTO) > 0 ? 1 : 0);
        assertNull(Foresta.getPosizioneDelGruppo());
    }

    @Test
    void senzaUnaCasellaUnaMissioneNonRicordaIlPuntoDiPartenzaENonEsplora() {
        senzaPosizione();
        Sintetica m = new Sintetica().passo("A", s -> s._esplora(1).poi(Passo.FINE));
        m.getModelloDati().setId("SENZA_CASELLA");
        RegistroMissioni.aggiungiMissioneSecondaria(m);
        m.attivaMissione();
        assertNull(m.getPuntoDiPartenza());
        m.controllaPreLocazione();
        assertEquals(0, m.getCaselleEsplorate());
    }

    @Test
    void ilSegnalinoSiLeggeESiConsumaIlSegnaleDiSoppressione() {
        Sintetica m = nuova("M2");
        CoordinateMD c = new CoordinateMD(4, 4);
        RegistroMissioni.segnalaLocazione(m, c);
        assertEquals(c, RegistroMissioni.getLocazioneSegnalata(m));
        RegistroMissioni.sopprimiContenutoLocazione(c);
        assertFalse(RegistroMissioni.isDaSopprimere(new CoordinateMD(5, 5)), "un'altra casella non è quella segnalata");
        assertFalse(RegistroMissioni.isDaSopprimere(c), "il segnale si è consumato");
        RegistroMissioni.sopprimiContenutoLocazione(c);
        assertTrue(RegistroMissioni.isDaSopprimere(c));
    }

    @Test
    void lePosizioniDaSegnalareSiVedonoSoloPerMissioniInCorsoSuCaselleConosciute() {
        Sintetica ferma = nuova("FERMA");
        Sintetica attiva = nuova("ATTIVA");
        attiva.attivaMissione();
        Sintetica altra = nuova("ALTRA");
        altra.attivaMissione();
        CoordinateMD conosciuta = Foresta.getPosizioneDelGruppo();
        CoordinateMD ignota = new CoordinateMD(MondoDiProva.LATO - 1, MondoDiProva.LATO - 1);
        RegistroMissioni.segnalaLocazione(ferma, conosciuta);
        assertFalse(RegistroMissioni.getLocazioniDaSegnalare().contains(conosciuta), "la missione è ferma");
        RegistroMissioni.segnalaLocazione(altra, conosciuta);
        assertTrue(RegistroMissioni.getLocazioniDaSegnalare().contains(conosciuta));
        // Per la casella segnata da più missioni con lo stesso nome il nome è uno solo
        RegistroMissioni.segnalaLocazione(attiva, conosciuta);
        assertEquals(1, RegistroMissioni.getNomiMissioniDaSegnalare(conosciuta).size());
        RegistroMissioni.segnalaLocazione(ferma, ignota);
        assertTrue(RegistroMissioni.getNomiMissioniDaSegnalare(ignota).isEmpty(), "la missione che la segna è ferma");
        // Segnare una casella la fa conoscere; se la mappa si azzera, la casella torna ignota e non lampeggia più
        RegistroMissioni.segnalaLocazione(attiva, ignota);
        assertTrue(RegistroMissioni.getLocazioniDaSegnalare().contains(ignota));
        Foresta.azzera(MondoDiProva.LATO, MondoDiProva.LATO);
        Foresta.impostaLocazione(ignota, TipoLocazione.BOSCO);
        Foresta.impostaLocazione(conosciuta, TipoLocazione.BOSCO);
        assertFalse(Foresta.isLocazioneConosciuta(ignota));
        assertFalse(RegistroMissioni.getLocazioniDaSegnalare().contains(ignota));
    }

    @Test
    void leRicercheSenzaQuadranteNeCentro() {
        Sintetica m = nuova("M3");
        Optional<CoordinateMD> trovata = RegistroMissioni.cerca(TipoLocazione.TEMPIO, m);
        assertTrue(trovata.isPresent());
        Optional<CoordinateMD> vicina = RegistroMissioni.cercaVicino(TipoLocazione.TEMPIO, m, new CoordinateMD(0, 0), Collections.emptySet());
        assertTrue(vicina.isPresent());
    }

    @Test
    void suUnaMappaSenzaPostiLeRicercheFalliscono() {
        // Una mappa tutta di radure: né il posto richiesto né un bosco o una palude da sostituire
        for (int x = 0; x < MondoDiProva.LATO; x++) {
            for (int y = 0; y < MondoDiProva.LATO; y++) {
                if (Foresta.getLocazione(x, y).getCategoriaLocazione() != CategoriaLocazione.CITTA
                        && Foresta.getLocazione(x, y).getCategoriaLocazione() != CategoriaLocazione.CASTELLO) {
                    Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.RADURA);
                }
            }
        }
        Sintetica m = nuova("M4");
        assertFalse(RegistroMissioni.esisteUnPostoPer(TipoLocazione.GROTTA, m));
        assertFalse(RegistroMissioni.cercaOCostruisci(TipoLocazione.GROTTA, m).isPresent());
        assertNull(RegistroMissioni.rivendicaPerLocazioneUnica(TipoLocazione.CASTELLO_DRAGO, TipoLocazione.BOSCO, m));
    }

    @Test
    void gliArtefattiDelRegistroEscludonoLeCaselle() {
        // Una mappa di soli boschi, tutti con un artefatto: non c'è dove costruire, e nessun posto è libero
        for (int x = 0; x < MondoDiProva.LATO; x++) {
            for (int y = 0; y < MondoDiProva.LATO; y++) {
                CoordinateMD c = new CoordinateMD(x, y);
                Foresta.impostaLocazione(c, TipoLocazione.BOSCO);
                RegistroArtefatti.addArtefattoInLocazione(GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 1), c);
            }
        }
        Sintetica m = nuova("M5");
        assertFalse(RegistroMissioni.esisteUnPostoPer(TipoLocazione.BOSCO, m));
        assertFalse(RegistroMissioni.cerca(TipoLocazione.BOSCO, m).isPresent());
        assertFalse(RegistroMissioni.cercaOCostruisci(TipoLocazione.GROTTA, m).isPresent());
    }

    @Test
    void ICastelliDeiNemiciDelDragoVannoUnoPerQuadrante() {
        Sintetica m = nuova("M6");
        CoordinateMD castello = RegistroMissioni.rivendicaPerLocazioneUnica(TipoLocazione.CASTELLO_LICH, TipoLocazione.BOSCO, m);
        assertNotNull(castello);
        // Una locazione unica che non è un castello va dove capita
        CoordinateMD citta = RegistroMissioni.rivendicaPerLocazioneUnica(TipoLocazione.CITTA_NYENA, TipoLocazione.BOSCO, m);
        assertNotNull(citta);
        for (CategoriaLocazione categoria : CategoriaLocazione.values()) {
            RegistroMissioni.quadranteSenza(categoria);
        }
    }

    @Test
    void senzaCastelliUnQuadranteLiberoCeSempre() {
        Foresta.azzera(MondoDiProva.LATO, MondoDiProva.LATO);
        assertNotNull(RegistroMissioni.quadranteSenza(CategoriaLocazione.CASTELLO));
        List<TipoLocazione> castelli = java.util.Arrays.asList(TipoLocazione.CASTELLO_IDRA, TipoLocazione.CASTELLO_MINOTAURO,
                TipoLocazione.CASTELLO_LICH, TipoLocazione.CASTELLO_STREGA);
        for (int i = 0; i < castelli.size(); i++) {
            Foresta.costruisciLocazioneUnica(castelli.get(i), new CoordinateMD(2 + 25 * (i % 2), 2 + 25 * (i / 2)), false);
        }
        // Quattro castelli, uno per quadrante: restano solo i quadranti che non hanno castelli, se ce ne sono
        RegistroMissioni.quadranteSenza(CategoriaLocazione.CASTELLO);
    }
}
