package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tattiche.TatticheDeiPersonaggi;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.eventi.interni.InternoDecisioneTattica;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tattiche.*;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La tattica del personaggio comanda il combattimento automatico (tattiche_di_combattimento_automatiche.md §11): a ogni scatto del TIMER il personaggio
 * di turno fa quello che la sua tattica decide, i mostri con una tattica anche loro, e senza tattica tutto resta com'era.
 */
class CombattimentoConTatticheTest {

    private static final StatoDelCombattimento.Lato G = StatoDelCombattimento.Lato.GIOCATORE;

    private static CoordinateMD unaCasellaDiBosco() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.BOSCO) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna casella di bosco");
    }

    private static void resistente(Personaggio p) {
        // La salute massima sale davvero (senza, addSalute si ferma al massimo di prima e uno scontro finirebbe al primo incantesimo)
        p.addSaluteMassima(1_000_000, "prova");
        p.addSalute(1_000_000);
    }

    private static Locazione preparaLo(PartitaDiTest partita, int goblin) {
        CoordinateMD bosco = unaCasellaDiBosco();
        Foresta.setPosizioneDelGruppo(bosco);
        Locazione locazione = CostruttoreDellaForesta.costruisciIstanza(bosco);
        LocazioneCorrente.set(locazione);
        GruppoAvversario avversari = GruppoAvversario.getIstanza();
        avversari.reimposta();
        for (int i = 0; i < goblin; i++) {
            Personaggio g = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
            resistente(g);
            avversari.aggiungiPersonaggio(g);
        }
        for (Personaggio p : partita.gruppo().getPersonaggi()) {
            resistente(p);
        }
        StatoDelCombattimento.getIstanza().reimposta();
        return locazione;
    }

    private static void iniziaLaMischia(Locazione locazione, PartitaDiTest partita) {
        GruppoAvversario avversari = GruppoAvversario.getIstanza();
        locazione.impostaAzioni(partita.gruppo(), avversari, null);
        locazione.impostaAzioni(partita.gruppo(), avversari, Comando.COMBATTIMENTO);
        locazione.impostaAzioni(partita.gruppo(), avversari, Comando.ofPersonaggio(0));
    }

    private static void scatta(Locazione locazione, PartitaDiTest partita, int volte) {
        for (int i = 0; i < volte; i++) {
            locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.TIMER);
        }
    }

    private static SelettoreMD su(TipoRosa rosa, TipoOrdinamento ordinamento, FiltroMD... filtri) {
        return new SelettoreMD(rosa, Arrays.asList(filtri), OrdinamentoMD.di(ordinamento));
    }

    private static int salute(Personaggio p) {
        return p.getSalute();
    }

    /** Il gruppo parte con qualche pergamena: per il test si sa esattamente quali ci sono */
    private static void svuotaLePergamene(GruppoGiocatore gruppo) {
        for (TipoIncantesimo classe : TipoIncantesimo.values()) {
            gruppo.subIncantesimi(classe, gruppo.getIncantesimi(classe));
        }
        for (int i = gruppo.getPozioniSalute(); i > 0; i--) {
            gruppo.subPozioniSalute(1);
        }
    }

    private static int pergameneDiDanno(GruppoGiocatore gruppo) {
        int totale = 0;
        for (TipoIncantesimo classe : TipoIncantesimo.values()) {
            totale += gruppo.getIncantesimi(classe);
        }
        return totale;
    }

    @Test
    void ilMagoLanciaIlFulmineSulBagnatoEConsumaLaPergamenaELaMagia() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(601)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.MAGO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 3);
            Personaggio mago = partita.gruppo().getCapo();
            mago.addMagia(mago.getMagiaMassima());
            svuotaLePergamene(partita.gruppo());
            partita.gruppo().addIncantesimi(TipoIncantesimo.FULMINE, 3);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            Personaggio bagnato = avversari.getPersonaggi().get(1);
            bagnato.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 5, 0);
            TatticheDeiPersonaggi.imposta(mago, TatticaMD.di(VoceMD.su(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BAGNATO)),
                    AzioneMD.di(TipoAzione.FULMINE))));
            partita.eventi().ascolta(InternoAzioneInCombattimento.class);
            int magiaPrima = mago.getMagia();
            iniziaLaMischia(locazione, partita);
            partita.eventi().svuota();
            scatta(locazione, partita, 1);

            assertEquals(2, partita.gruppo().getIncantesimi(TipoIncantesimo.FULMINE), "la pergamena si consuma");
            assertTrue(mago.getMagia() < magiaPrima, "il lancio costa Magia");
            InternoAzioneInCombattimento annuncio = partita.eventi().tutti(InternoAzioneInCombattimento.class).get(0);
            assertSame(mago, annuncio.getPersonaggio());
            assertSame(bagnato, annuncio.getBersaglio(), "il bersaglio è il bagnato, anche se è il secondo");
            assertEquals(1, StatoDelCombattimento.getIstanza().getUsiDellaVoce(mago, "0"));
        }
    }

    @Test
    void leCondizioniDellaVoceCambianoLAzioneAlRoundDopo() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(602)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.MAGO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 1);
            Personaggio mago = partita.gruppo().getCapo();
            mago.addMagia(mago.getMagiaMassima());
            svuotaLePergamene(partita.gruppo());
            partita.gruppo().addIncantesimi(TipoIncantesimo.FUOCO, 5);
            // "Una volta per scontro" un incantesimo, poi sempre l'attacco fisico (il ripiego)
            VoceMD unaVolta = new VoceMD(null, Collections.singletonList(PredicatoMD.conConfrontoENumero(TipoPredicato.VOCE_USATA, TipoConfronto.UGUALE, 0)),
                    Collections.singletonList(AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_POTENTE, false)));
            TatticheDeiPersonaggi.imposta(mago, TatticaMD.di(unaVolta));
            iniziaLaMischia(locazione, partita);
            // Piano G A: tre azioni mie in sei scatti
            scatta(locazione, partita, 6);
            assertEquals(4, pergameneDiDanno(partita.gruppo()), "una sola pergamena consumata");
            assertEquals(1, StatoDelCombattimento.getIstanza().getUsiDellaVoce(mago, "0"));
        }
    }

    @Test
    void chiStaMaleBeveLaPozioneDelGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(603)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 1);
            Personaggio capo = partita.gruppo().getCapo();
            capo.subSalute(capo.getSalute() * 7 / 10, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            svuotaLePergamene(partita.gruppo());
            partita.gruppo().addPozioniSalute(2);
            TatticheDeiPersonaggi.imposta(capo, TatticaMD.di(VoceMD.su(su(TipoRosa.SE_STESSO, TipoOrdinamento.PRIMO,
                    FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, TipoConfronto.MINORE, 50)), AzioneMD.di(TipoAzione.POZIONE_SALUTE))));
            int pozioniPrima = partita.gruppo().getPozioniSalute();
            iniziaLaMischia(locazione, partita);
            scatta(locazione, partita, 1);
            assertEquals(pozioniPrima - 1, partita.gruppo().getPozioniSalute(), "ha bevuto, e ha usato il suo turno");
            assertEquals(StatoDelCombattimento.Lato.AVVERSARI, StatoDelCombattimento.getIstanza().getProssimoLato(), "dopo la sua azione tocca all'avversario");
        }
    }

    @Test
    void senzaTatticaIMieiAttaccanoIlPrimoComeSempre() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(604)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 3);
            Personaggio capo = partita.gruppo().getCapo();
            assertNull(TatticheDeiPersonaggi.di(capo));
            Personaggio primo = GruppoAvversario.getIstanza().getPersonaggi().get(0);
            java.util.Map<Personaggio, Integer> saluteIniziale = new java.util.IdentityHashMap<>();
            for (Personaggio p : GruppoAvversario.getIstanza().getPersonaggi()) {
                saluteIniziale.put(p, p.getSalute());
            }
            // Molti attacchi: il primo si fa male, e almeno uno degli altri no finché il primo è vivo (resistentissimo)
            iniziaLaMischia(locazione, partita);
            scatta(locazione, partita, 12);
            for (Personaggio altro : GruppoAvversario.getIstanza().getPersonaggi()) {
                if (altro != primo) {
                    assertEquals(saluteIniziale.get(altro).intValue(), salute(altro), "il primo avversario è l'unico colpito: gli altri sono interi");
                }
            }
        }
    }

    @Test
    void unMostroConUnaTatticaAttaccaGliOspitiVulnerabili() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(605)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 1);
            Personaggio ostaggio = new Viandante("Ottone", 1);
            partita.gruppo().aggiungiOspite(ostaggio, true);
            Personaggio goblin = GruppoAvversario.getIstanza().getPersonaggi().get(0);
            TatticheDeiPersonaggi.imposta(goblin, TatticaMD.di(VoceMD.su(su(TipoRosa.OSPITI, TipoOrdinamento.PRIMO), AzioneMD.di(TipoAzione.ATTACCO_FISICO))));
            int salutePrima = salute(ostaggio);
            iniziaLaMischia(locazione, partita);
            // Il piano è G A: dopo due scatti ha agito anche il goblin
            scatta(locazione, partita, 40);
            assertTrue(salute(ostaggio) < salutePrima, "il goblin con la sua tattica attacca l'ostaggio (con tanti colpi almeno uno va a segno)");
        }
    }

    @Test
    void dopoUnCaricamentoIlPersonaggioAgisceComeImpostato() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(606)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.MAGO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio compagno = new com.threeamigos.foresta.personaggi.Guerriero("Compagno", 1);
            partita.gruppo().aggiungiPersonaggio(compagno);
            TatticaMD tattica = TatticaMD.di(VoceMD.su(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BAGNATO)),
                    AzioneMD.di(TipoAzione.FULMINE), AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
            TatticaMD dellAltro = TatticaMD.di(VoceMD.su(su(TipoRosa.SE_STESSO, TipoOrdinamento.PRIMO), AzioneMD.di(TipoAzione.POZIONE_SALUTE)));
            TatticheDeiPersonaggi.imposta(partita.gruppo().getCapo(), tattica);
            TatticheDeiPersonaggi.imposta(compagno, dellAltro);
            assertTrue(partita.salva(Comando.NUMERO_1));
            TatticheDeiPersonaggi.imposta(partita.gruppo().getCapo(), TatticaMD.VUOTA);
            TatticheDeiPersonaggi.imposta(compagno, null);
            assertTrue(partita.leggi(Comando.NUMERO_1));

            Personaggio capo = partita.gruppo().getCapo();
            assertEquals(tattica, TatticheDeiPersonaggi.di(capo), "la tattica del capo torna uguale");
            assertEquals(dellAltro, TatticheDeiPersonaggi.di(partita.gruppo().getPersonaggio(1)), "e quella del compagno");

            // Il personaggio rilegge e agisce davvero come impostato
            Locazione locazione = preparaLo(partita, 3);
            capo.addMagia(capo.getMagiaMassima());
            svuotaLePergamene(partita.gruppo());
            partita.gruppo().addIncantesimi(TipoIncantesimo.FULMINE, 2);
            GruppoAvversario.getIstanza().getPersonaggi().get(2).addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 5, 0);
            iniziaLaMischia(locazione, partita);
            scatta(locazione, partita, 1);
            assertEquals(1, partita.gruppo().getIncantesimi(TipoIncantesimo.FULMINE), "ha lanciato il fulmine della sua tattica");
        }
    }

    @Test
    void ogniDecisioneSiAnnunciaConIlPerche() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(607)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.MAGO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 3);
            Personaggio mago = partita.gruppo().getCapo();
            mago.addMagia(mago.getMagiaMassima());
            svuotaLePergamene(partita.gruppo());
            partita.gruppo().addIncantesimi(TipoIncantesimo.FULMINE, 1);
            Personaggio bagnato = GruppoAvversario.getIstanza().getPersonaggi().get(1);
            bagnato.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 5, 0);
            TatticheDeiPersonaggi.imposta(mago, TatticaMD.di(
                    VoceMD.su(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BAGNATO)),
                            AzioneMD.di(TipoAzione.FULMINE)),
                    VoceMD.su(su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), AzioneMD.di(TipoAzione.ATTACCO_FISICO))));
            partita.eventi().ascolta(InternoDecisioneTattica.class);
            iniziaLaMischia(locazione, partita);
            // G A A A G A A A: la prima azione è il fulmine sul bagnato, la seconda (la pergamena è finita) l'attacco al più ferito
            scatta(locazione, partita, 5);
            java.util.List<InternoDecisioneTattica> decisioni = new java.util.ArrayList<>();
            for (InternoDecisioneTattica d : partita.eventi().tutti(InternoDecisioneTattica.class)) {
                // Anche i goblin decidono, con la tattica base: qui interessano quelle del mago (i goblin colpiti dal fulmine possono essere storditi)
                if (d.getPersonaggio() == mago) {
                    decisioni.add(d);
                }
            }
            assertEquals(2, decisioni.size(), "una per ogni azione del mago");

            InternoDecisioneTattica fulmine = decisioni.get(0);
            assertSame(mago, fulmine.getPersonaggio());
            assertSame(bagnato, fulmine.getBersaglio());
            assertEquals(TipoAzione.FULMINE, fulmine.getAzione());
            assertEquals(0, fulmine.getIndiceVoce());
            assertFalse(fulmine.isRipiego());
            String spiegazione = String.join("\n", fulmine.getSpiegazione());
            assertTrue(spiegazione.contains("STATO BAGNATO: 1 ["), spiegazione);
            assertTrue(spiegazione.contains("AVVERSARI: 3 ["), "la rosa, con i nomi dei candidati: " + spiegazione);

            InternoDecisioneTattica attacco = decisioni.get(1);
            assertEquals(TipoAzione.ATTACCO_FISICO, attacco.getAzione());
            assertEquals(1, attacco.getIndiceVoce());
            String perche = String.join("\n", attacco.getSpiegazione());
            assertTrue(perche.contains("voce 1: nessun bersaglio") && perche.contains("azione applicabile: 0"), "la pergamena è finita: " + perche);
            assertTrue(perche.contains("MENO_SALUTE ("), "le misure dell'ordinamento: " + perche);
        }
    }

    @Test
    void unPersonaggioSenzaTatticaAgisceConLaPredefinitaDelSuoRuolo() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(608)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 1);
            assertNull(TatticheDeiPersonaggi.di(partita.gruppo().getCapo()));
            partita.eventi().ascolta(InternoDecisioneTattica.class);
            iniziaLaMischia(locazione, partita);
            scatta(locazione, partita, 1);
            InternoDecisioneTattica decisione = partita.eventi().tutti(InternoDecisioneTattica.class).get(0);
            assertSame(partita.gruppo().getCapo(), decisione.getPersonaggio());
            assertFalse(decisione.isRipiego(), "ha una voce: la predefinita del guerriero");
            assertEquals(4, decisione.getIndiceVoce(), "sano e senza maghi davanti: l'ultima voce, il primo avversario");
        }
    }

    @Test
    void unaTatticaDisabilitataEComeSeNonCiFosse() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(608)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 1);
            // Una tattica che farebbe solo un'azione impossibile: disabilitata, si usa la predefinita
            TatticheDeiPersonaggi.imposta(partita.gruppo().getCapo(), TatticaMD.VUOTA.disabilitata());
            partita.eventi().ascolta(InternoDecisioneTattica.class);
            iniziaLaMischia(locazione, partita);
            scatta(locazione, partita, 1);
            InternoDecisioneTattica decisione = partita.eventi().tutti(InternoDecisioneTattica.class).get(0);
            assertSame(partita.gruppo().getCapo(), decisione.getPersonaggio());
            assertFalse(decisione.isRipiego(), "la vuota disabilitata non conta: vale la predefinita del guerriero");
            assertEquals(4, decisione.getIndiceVoce());
        }
    }

    @Test
    void ilRipiegoSiAnnunciaSenzaVoce() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(608)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 1);
            // Una tattica vuota esplicita: senza, il personaggio userebbe quella predefinita del suo ruolo
            TatticheDeiPersonaggi.imposta(partita.gruppo().getCapo(), TatticaMD.VUOTA);
            partita.eventi().ascolta(InternoDecisioneTattica.class);
            iniziaLaMischia(locazione, partita);
            scatta(locazione, partita, 1);
            InternoDecisioneTattica decisione = partita.eventi().tutti(InternoDecisioneTattica.class).get(0);
            assertSame(partita.gruppo().getCapo(), decisione.getPersonaggio());
            assertTrue(decisione.isRipiego());
            assertEquals(-1, decisione.getIndiceVoce());
            assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getAzione());
            assertSame(GruppoAvversario.getIstanza().getPersonaggi().get(0), decisione.getBersaglio());
        }
    }

    @Test
    void iMostriSenzaTatticaAgisconoConQuellaBaseDellaLoroClasse() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(611)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 2);
            Personaggio capo = partita.gruppo().getCapo();
            partita.eventi().ascolta(InternoDecisioneTattica.class);
            iniziaLaMischia(locazione, partita);
            // Piano G A G..: due scatti bastano a far agire un goblin
            scatta(locazione, partita, 2);
            InternoDecisioneTattica decisione = null;
            for (InternoDecisioneTattica d : partita.eventi().tutti(InternoDecisioneTattica.class)) {
                if (GruppoAvversario.getIstanza().getPersonaggi().contains(d.getPersonaggio())) {
                    decisione = d;
                }
            }
            assertNotNull(decisione, "un goblin ha deciso con la tattica base");
            assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getAzione());
            assertSame(capo, decisione.getBersaglio(), "chi altro?");
            assertEquals(1, decisione.getIndiceVoce(), "l'ultima voce del goblin, che è un bruto: uno a caso (non l'ha colpito nessuno)");
            String perche = String.join("\n", decisione.getSpiegazione());
            assertTrue(perche.contains("AVVERSARI_E_OSPITI"), perche);
        }
    }
}
