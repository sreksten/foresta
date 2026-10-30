package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.economia.MondoDiProva;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdotessa;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoOfferta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfferteTest {

    private GruppoGiocatore gruppo;
    private GruppoAvversario avversari;
    private final List<Object> eventi = new ArrayList<>();

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(20, 20);
        gruppo = GruppoGiocatore.getIstanza();
        aggiungiAlGruppo(new Guerriero("Arsenio", 3));
        avversari = GruppoAvversario.getIstanza();
        BusEventi.iscriviti(Object.class, eventi::add);
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private void aggiungiAlGruppo(Personaggio personaggio) {
        personaggio.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(personaggio);
    }

    private void unAvversario(boolean maschio) {
        avversari.reimposta();
        avversari.aggiungiPersonaggio(maschio ? new Goblin(2) : new Sacerdotessa(2));
    }

    private void dueAvversari(boolean maschio) {
        unAvversario(maschio);
        avversari.aggiungiPersonaggio(new Goblin(2));
    }

    private Offerta offerta(TipoOfferta tipo) {
        return FabbricaOfferte.crea(tipo);
    }

    @Test
    void laFabbricaCostruisceOgniTipo() {
        for (TipoOfferta tipo : TipoOfferta.values()) {
            assertNotNull(FabbricaOfferte.crea(tipo), tipo.name());
        }
    }

    // ---- Aiuto

    @Test
    void unAvversarioSiUnisceGratuitamente() {
        unAvversario(true);
        Offerta aiuto = offerta(TipoOfferta.AIUTO_GRATUITO);
        assertFalse(aiuto.isGratuita(gruppo, avversari));
        assertTrue(aiuto.isFattibile(gruppo, avversari));
        assertTrue(aiuto.getDescrizione(gruppo, avversari).contains("è dispost"));
        int prima = gruppo.getNumeroPersonaggi();
        aiuto.accetta(gruppo, avversari);
        assertEquals(prima + 1, gruppo.getNumeroPersonaggi());
    }

    @Test
    void l_aiutoGratuitoDiPiuAvversariEDiPiuGiocanti() {
        dueAvversari(false);
        aggiungiAlGruppo(new Guerriero("Merlino", 3));
        Offerta aiuto = offerta(TipoOfferta.AIUTO_GRATUITO);
        assertTrue(aiuto.getDescrizione(gruppo, avversari).startsWith("Una di loro"));
        avversari.reimposta();
        dueAvversari(true);
        assertTrue(offerta(TipoOfferta.AIUTO_GRATUITO).getDescrizione(gruppo, avversari).startsWith("Uno di loro"));
    }

    @Test
    void senzaAvversariOASquadraPienaNessunoSiUnisce() {
        assertFalse(offerta(TipoOfferta.AIUTO_GRATUITO).isFattibile(gruppo, avversari));
        Cassa.addMonete(100);
        assertFalse(offerta(TipoOfferta.AIUTO_MERCENARIO).isFattibile(gruppo, avversari));
        unAvversario(true);
        while (gruppo.getNumeroPersonaggi() < CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE) {
            aggiungiAlGruppo(new Guerriero("Comparsa" + gruppo.getNumeroPersonaggi(), 1));
        }
        assertFalse(offerta(TipoOfferta.AIUTO_GRATUITO).isFattibile(gruppo, avversari));
        assertFalse(offerta(TipoOfferta.AIUTO_MERCENARIO).isFattibile(gruppo, avversari));
    }

    @Test
    void ilMercenarioVuoleLeMonete() {
        unAvversario(false);
        Dado.trucca(1);
        Offerta mercenario = offerta(TipoOfferta.AIUTO_MERCENARIO);
        assertFalse(mercenario.isFattibile(gruppo, avversari));
        Cassa.addMonete(10);
        assertTrue(mercenario.isFattibile(gruppo, avversari));
        assertFalse(mercenario.isGratuita(gruppo, avversari));
        assertTrue(mercenario.getDescrizione(gruppo, avversari).contains("una moneta"));
        int prima = gruppo.getNumeroPersonaggi();
        mercenario.accetta(gruppo, avversari);
        assertEquals(prima + 1, gruppo.getNumeroPersonaggi());
        assertEquals(9, Cassa.getMonete());
    }

    @Test
    void ilMercenarioDescrittoConPiuMonetePerUnoOPerPiuAvversari() {
        unAvversario(true);
        Dado.trucca(4);
        assertTrue(offerta(TipoOfferta.AIUTO_MERCENARIO).getDescrizione(gruppo, avversari).contains("4 monete"));
        avversari.reimposta();
        dueAvversari(false);
        Dado.trucca(1);
        assertTrue(offerta(TipoOfferta.AIUTO_MERCENARIO).getDescrizione(gruppo, avversari).startsWith("Una di loro per una moneta"));
        Dado.trucca(4);
        assertTrue(offerta(TipoOfferta.AIUTO_MERCENARIO).getDescrizione(gruppo, avversari).startsWith("Una di loro per 4 monete"));
        avversari.reimposta();
        dueAvversari(true);
        aggiungiAlGruppo(new Guerriero("Merlino", 3));
        Dado.trucca(2);
        assertTrue(offerta(TipoOfferta.AIUTO_MERCENARIO).getDescrizione(gruppo, avversari).startsWith("Uno di loro"));
    }

    @Test
    void ilMercenarioConUnSoloGiocanteNominaIlCapo() {
        unAvversario(true);
        Dado.trucca(1);
        String descrizione = offerta(TipoOfferta.AIUTO_MERCENARIO).getDescrizione(gruppo, avversari);
        assertTrue(descrizione.startsWith("Per una moneta"));
        assertTrue(descrizione.contains(" per un po'."));
    }

    // ---- Incantesimi

    @Test
    void laOffertaDiIncantesimi() {
        unAvversario(true);
        Dado.trucca(1);
        Offerta incantesimi = offerta(TipoOfferta.INCANTESIMI);
        assertFalse(incantesimi.isGratuita(gruppo, avversari));
        assertFalse(incantesimi.isFattibile(gruppo, avversari));
        Cassa.addMonete(100000);
        assertTrue(incantesimi.isFattibile(gruppo, avversari));
        String descrizione = incantesimi.getDescrizione(gruppo, avversari);
        assertTrue(descrizione.contains("a vendere un "), descrizione);
        incantesimi.accetta(gruppo, avversari);
        assertTrue(Cassa.getMonete() < 100000);
    }

    @Test
    void leOfferteDiIncantesimiInTutteLeVarianti() {
        for (int quantita = 1; quantita <= 3; quantita++) {
            for (boolean molti : new boolean[] {false, true}) {
                for (boolean maschio : new boolean[] {false, true}) {
                    avversari.reimposta();
                    if (molti) {
                        dueAvversari(maschio);
                    } else {
                        unAvversario(maschio);
                    }
                    Dado.trucca(quantita);
                    Offerta incantesimi = offerta(TipoOfferta.INCANTESIMI);
                    String descrizione = incantesimi.getDescrizione(gruppo, avversari);
                    assertTrue(descrizione.endsWith("."));
                    assertEquals(molti, descrizione.startsWith("Un"));
                }
            }
        }
    }

    // ---- Informazioni

    @Test
    void leInformazioniSonoGratisESiAccettanoDaSole() {
        Offerta informazioni = offerta(TipoOfferta.INFORMAZIONI);
        assertTrue(informazioni.isFattibile(gruppo, avversari));
        assertTrue(informazioni.isGratuita(gruppo, avversari));
        informazioni.accetta(gruppo, avversari);
    }

    @Test
    void informazioniSuCittaECastelli() {
        Offerta informazioni = offerta(TipoOfferta.INFORMAZIONI);
        // Nessuna città né castello costruito: il Drago ha distrutto tutto
        Dado.trucca(1);
        assertTrue(informazioni.getDescrizione(gruppo, avversari).contains("tutte le città sono state distrutte"));
        Dado.trucca(2);
        assertTrue(informazioni.getDescrizione(gruppo, avversari).contains("tutti i castelli sono stati distrutti"));
        Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_NYENA, new CoordinateMD(1, 1), false);
        Dado.trucca(1);
        String citta = informazioni.getDescrizione(gruppo, avversari);
        assertTrue(citta.contains(TipoLocazione.CITTA_NYENA.getNomeProprio()), citta);
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(1, 1)));
        // Un altro castello oltre a quello del Drago
        Foresta.costruisciLocazioneUnica(TipoLocazione.CASTELLO_LICH, new CoordinateMD(3, 3), false);
        Dado.trucca(2);
        String castello = informazioni.getDescrizione(gruppo, avversari);
        assertTrue(castello.contains(TipoLocazione.CASTELLO_LICH.getNomeProprio()), castello);
        // Il castello del Drago ha la precedenza
        Foresta.costruisciLocazioneUnica(TipoLocazione.CASTELLO_DRAGO, new CoordinateMD(4, 4), false);
        Dado.trucca(2);
        assertTrue(informazioni.getDescrizione(gruppo, avversari).contains(TipoLocazione.CASTELLO_DRAGO.getNomeProprio()));
    }

    @Test
    void inCittaLeInformazioniNonParlanoDiCitta() {
        Foresta.impostaLocazione(MondoDiProva.QUI, TipoLocazione.CITTA_NYENA);
        Offerta informazioni = offerta(TipoOfferta.INFORMAZIONI);
        // Il dado ha facce da 2 a 3: con il trucco 3 sono gli artefatti
        Dado.trucca(3);
        assertTrue(informazioni.getDescrizione(gruppo, avversari).contains("tutti gli artefatti sono stati recuperati"));
        for (int i = 0; i < 20; i++) {
            assertFalse(informazioni.getDescrizione(gruppo, avversari).contains("tutte le città"));
        }
    }

    @Test
    void informazioniSugliArtefattiNeiTempli() {
        Offerta informazioni = offerta(TipoOfferta.INFORMAZIONI);
        RegistroArtefatti.addArtefattoInLocazione(MondoDiProva.artefatto(TipoArtefatto.SPADA, 2), new CoordinateMD(8, 8));
        Dado.trucca(3);
        String descrizione = informazioni.getDescrizione(gruppo, avversari);
        assertTrue(descrizione.contains(" si trova in un tempio "), descrizione);
        assertTrue(RegistroArtefatti.isLocalizzazioneConosciuta(new CoordinateMD(8, 8)));
        // Con il nome proprio il nome completo è un inciso, chiuso da una virgola
        RegistroArtefatti.addArtefattoInLocazione(artefattoConNomeProprio(), new CoordinateMD(9, 9));
        RegistroArtefatti.rimuoviArtefattoInLocazione(new CoordinateMD(8, 8));
        Dado.trucca(3);
        assertTrue(informazioni.getDescrizione(gruppo, avversari).contains(", si trova in un tempio "));
    }

    private static com.threeamigos.foresta.artefatti.Artefatto artefattoConNomeProprio() {
        com.threeamigos.foresta.artefatti.Artefatto artefatto = MondoDiProva.artefatto(TipoArtefatto.SPADA, 2);
        artefatto.getModelloDati().setNomeProprio("Diavolina");
        return artefatto;
    }

    // ---- Mappe

    @Test
    void laMappaDellaForesta() {
        unAvversario(true);
        Offerta mappa = offerta(TipoOfferta.MAPPA_FORESTA);
        assertFalse(mappa.isGratuita(gruppo, avversari));
        assertFalse(mappa.isFattibile(gruppo, avversari));
        assertTrue(mappa.getDescrizione(gruppo, avversari).startsWith("Per 20 monete"));
        dueAvversari(false);
        assertTrue(mappa.getDescrizione(gruppo, avversari).startsWith("Una di loro per 20 monete"));
        avversari.reimposta();
        dueAvversari(true);
        assertTrue(mappa.getDescrizione(gruppo, avversari).startsWith("Uno di loro"));
        Cassa.addMonete(100);
        assertTrue(mappa.isFattibile(gruppo, avversari));
        mappa.accetta(gruppo, avversari);
        assertEquals(80, Cassa.getMonete());
        assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(0, 0)));
    }

    @Test
    void laMappaDellaZonaIntornoAlGruppoEAiBordi() {
        unAvversario(true);
        Offerta mappa = offerta(TipoOfferta.MAPPA_ZONA);
        assertTrue(mappa.isGratuita(gruppo, avversari));
        assertTrue(mappa.isFattibile(gruppo, avversari));
        assertTrue(mappa.getDescrizione(gruppo, avversari).startsWith("Durante la conversazione"));
        dueAvversari(false);
        assertTrue(mappa.getDescrizione(gruppo, avversari).startsWith("Una di loro"));
        avversari.reimposta();
        dueAvversari(true);
        assertTrue(mappa.getDescrizione(gruppo, avversari).startsWith("Uno di loro"));
        CoordinateMD[] posizioni = {MondoDiProva.QUI, new CoordinateMD(1, 1), new CoordinateMD(18, 18), new CoordinateMD(1, 18), new CoordinateMD(18, 1)};
        for (CoordinateMD posizione : posizioni) {
            Foresta.setPosizioneDelGruppo(posizione);
            mappa.accetta(gruppo, avversari);
            assertTrue(Foresta.isLocazioneConosciuta(posizione));
        }
    }

    @Test
    void laMappaDellaZonaInUnaForestaPiccola() {
        MondoDiProva.nuovo(10, 8);
        GruppoAvversario nemici = GruppoAvversario.getIstanza();
        nemici.aggiungiPersonaggio(new Goblin(1));
        Offerta mappa = offerta(TipoOfferta.MAPPA_ZONA);
        for (CoordinateMD posizione : new CoordinateMD[] {new CoordinateMD(1, 1), new CoordinateMD(8, 6), new CoordinateMD(9, 7)}) {
            Foresta.setPosizioneDelGruppo(posizione);
            mappa.accetta(GruppoGiocatore.getIstanza(), nemici);
        }
    }

    // ---- Il pasto

    @Test
    void ilPastoSquisitoRendeSalute() {
        Dado.trucca(4);
        Offerta pasto = offerta(TipoOfferta.PASTO);
        assertTrue(pasto.isFattibile(gruppo, avversari));
        assertTrue(pasto.isGratuita(gruppo, avversari));
        assertTrue(pasto.getDescrizione(gruppo, avversari).contains("squisito"));
        int salute = gruppo.getCapo().getSalute();
        pasto.accetta(gruppo, avversari);
        assertTrue(gruppo.getCapo().getSalute() > salute);
    }

    @Test
    void ilPastoCheNonPiaceNonFaNiente() {
        Dado.trucca(1);
        Offerta pasto = offerta(TipoOfferta.PASTO);
        assertTrue(pasto.getDescrizione(gruppo, avversari).contains("non incontra i gusti"));
        aggiungiAlGruppo(new Guerriero("Merlino", 3));
        Dado.trucca(1);
        assertTrue(offerta(TipoOfferta.PASTO).getDescrizione(gruppo, avversari).endsWith("del gruppo."));
        int salute = gruppo.getCapo().getSalute();
        pasto.accetta(gruppo, avversari);
        assertEquals(salute, gruppo.getCapo().getSalute());
    }
}
