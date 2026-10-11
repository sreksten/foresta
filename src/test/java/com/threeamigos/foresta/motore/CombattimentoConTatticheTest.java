package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.modellodati.tattiche.FiltroMD;
import com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.tattiche.PredicatoMD;
import com.threeamigos.foresta.modellodati.tattiche.SelettoreMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.modellodati.tattiche.VoceMD;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
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
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.COSTITUZIONE, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
        p.addSalute(1_000_000);
    }

    private static Locazione preparaLo(PartitaDiTest partita, int goblin) {
        CoordinateMD bosco = unaCasellaDiBosco();
        partita.gruppo().setCoordinate(bosco);
        Locazione locazione = Foresta.costruisciIstanza(bosco);
        partita.gruppo().setLocazioneCorrente(locazione);
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
        for (ClasseIncantesimo classe : ClasseIncantesimo.values()) {
            gruppo.subIncantesimi(classe, gruppo.getIncantesimi(classe));
        }
        for (int i = gruppo.getPozioniSalute(); i > 0; i--) {
            gruppo.subPozioniSalute(1);
        }
    }

    private static int pergameneDiDanno(GruppoGiocatore gruppo) {
        int totale = 0;
        for (ClasseIncantesimo classe : ClasseIncantesimo.values()) {
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
            partita.gruppo().addIncantesimi(ClasseIncantesimo.FULMINE, 3);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            Personaggio bagnato = avversari.getPersonaggi().get(1);
            bagnato.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 5, 0);
            mago.setTattica(TatticaMD.di(VoceMD.su(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BAGNATO)),
                    AzioneMD.di(TipoAzione.FULMINE))));
            partita.eventi().ascolta(InternoAzioneInCombattimento.class);
            int magiaPrima = mago.getMagia();
            iniziaLaMischia(locazione, partita);
            partita.eventi().svuota();
            scatta(locazione, partita, 1);

            assertEquals(2, partita.gruppo().getIncantesimi(ClasseIncantesimo.FULMINE), "la pergamena si consuma");
            assertTrue(mago.getMagia() < magiaPrima, "il lancio costa MAGIA");
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
            partita.gruppo().addIncantesimi(ClasseIncantesimo.FUOCO, 5);
            // "Una volta per scontro" un incantesimo, poi sempre l'attacco fisico (il ripiego)
            VoceMD unaVolta = new VoceMD(null, Collections.singletonList(PredicatoMD.conConfrontoENumero(TipoPredicato.VOCE_USATA, Confronto.UGUALE, 0)),
                    Collections.singletonList(AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_POTENTE, false)));
            mago.setTattica(TatticaMD.di(unaVolta));
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
            capo.setTattica(TatticaMD.di(VoceMD.su(su(TipoRosa.SE_STESSO, TipoOrdinamento.PRIMO,
                    FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, Confronto.MINORE, 50)), AzioneMD.di(TipoAzione.POZIONE_SALUTE))));
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
            assertNull(capo.getTattica());
            Personaggio primo = GruppoAvversario.getIstanza().getPersonaggi().get(0);
            // Molti attacchi: il primo si fa male, e almeno uno degli altri no finché il primo è vivo (resistentissimo)
            iniziaLaMischia(locazione, partita);
            scatta(locazione, partita, 12);
            for (Personaggio altro : GruppoAvversario.getIstanza().getPersonaggi()) {
                if (altro != primo) {
                    assertEquals(altro.getSaluteMassima(), salute(altro), "il primo avversario è l'unico colpito: gli altri sono interi");
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
            goblin.setTattica(TatticaMD.di(VoceMD.su(su(TipoRosa.OSPITI, TipoOrdinamento.PRIMO), AzioneMD.di(TipoAzione.ATTACCO_FISICO))));
            int salutePrima = salute(ostaggio);
            iniziaLaMischia(locazione, partita);
            // Il piano è G A: dopo due scatti ha agito anche il goblin
            scatta(locazione, partita, 40);
            assertTrue(salute(ostaggio) < salutePrima, "il goblin con la sua tattica attacca l'ostaggio (con tanti colpi almeno uno va a segno)");
        }
    }
}
