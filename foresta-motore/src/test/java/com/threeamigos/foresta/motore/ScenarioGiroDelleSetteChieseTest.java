package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoEremitiIncontrati;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoLocazioneVisitata;
import com.threeamigos.foresta.intermezzi.*;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.missioni.OffertaDelSacerdote;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.principali.IlGiroDelleSetteChiese;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tipi.*;
import com.threeamigos.foresta.trofei.RegistroTrofei;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il Giro delle Sette Chiese: ripulito il primo tempio si presenta un sacerdote che chiede di liberarne altri sei; ogni
 * tempio ripulito diventa una locazione sicura; al settimo il ringraziamento e il premio in incantesimi.
 */
class ScenarioGiroDelleSetteChieseTest {

    private static List<CoordinateMD> templi() {
        List<CoordinateMD> templi = new ArrayList<>();
        for (int y = 0; y < Foresta.getDimensioneY(); y++) {
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                CoordinateMD coordinate = new CoordinateMD(x, y);
                if (Foresta.getLocazione(coordinate) == TipoLocazione.TEMPIO) {
                    templi.add(coordinate);
                }
            }
        }
        return templi;
    }

    private static IlGiroDelleSetteChiese giro() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(IlGiroDelleSetteChiese.class::isInstance)
                .map(IlGiroDelleSetteChiese.class::cast).findFirst().orElseThrow(AssertionError::new);
    }

    private static String testo(PaginaIntermezzo pagina) {
        return pagina.getBattuteProgrammate().stream().map(BattutaProgrammata::getBattuta)
                .map(b -> b.getTesto()).collect(Collectors.joining(" "));
    }

    @Test
    void ripulitoIlPrimoTempioSiPresentaIlSacerdoteEAlSettimoCiSonoIlRingraziamentoEIlPremio() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(301)) {
            avvia(partita);
            IlGiroDelleSetteChiese giro = giro();
            List<CoordinateMD> templi = templi();
            assertTrue(templi.size() >= 7);
            assertFalse(giro.isAttiva());

            // Fuggire o non aver sconfitto i custodi non basta
            partita.gruppo().setCoordinate(templi.get(0));
            GruppoAvversario.getIstanza().aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.VIVERNA, 1));
            giro.controllaPostLocazione();
            assertFalse(giro.isAttiva());
            assertFalse(Tempio.isRipulito(Foresta.getLocazioneMD(templi.get(0))));

            // Il primo tempio ripulito: la missione parte e il sacerdote si presenta a locazione conclusa
            GruppoAvversario.getIstanza().rimuoviPersonaggi();
            giro.controllaPostLocazione();
            assertTrue(giro.isAttiva());
            assertTrue(Tempio.isRipulito(Foresta.getLocazioneMD(templi.get(0))));
            assertTrue(giro.getDescrizione().endsWith("Ne mancano sei."), giro.getDescrizione());
            Intermezzo presentazione = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA);
            assertNotNull(presentazione);
            List<PaginaIntermezzo> pagine = presentazione.getPagine();
            assertEquals(1, pagine.size());
            assertTrue(testo(pagine.get(0)).contains("Altri sei templi"), testo(pagine.get(0)));
            RegistroIntermezzi.segnaScattato(presentazione);

            // Tornare nello stesso tempio non conta
            giro.controllaPostLocazione();
            assertTrue(giro.getDescrizione().endsWith("Ne mancano sei."), giro.getDescrizione());

            int luce = partita.gruppo().getIncantesimi(TipoIncantesimo.LUCE_DIVINA);
            int alba = partita.gruppo().getIncantesimi(TipoIncantesimo.ALBA_SACRA);
            int elementali = elementali(partita);
            for (int i = 1; i < 6; i++) {
                partita.gruppo().setCoordinate(templi.get(i));
                giro.controllaPostLocazione();
                assertFalse(giro.isCompleta());
            }
            assertTrue(giro.getDescrizione().endsWith("Ne manca uno."), giro.getDescrizione());
            assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA));

            partita.gruppo().setCoordinate(templi.get(6));
            giro.controllaPostLocazione();
            assertFalse(giro.isCompleta(), "prima l'offerta del sacerdote");
            assertEquals(luce + 2, partita.gruppo().getIncantesimi(TipoIncantesimo.LUCE_DIVINA));
            assertEquals(alba + 2, partita.gruppo().getIncantesimi(TipoIncantesimo.ALBA_SACRA));
            assertEquals(elementali + 1, elementali(partita));
            Intermezzo ringraziamento = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA);
            assertNotNull(ringraziamento);
            String grazie = testo(ringraziamento.getPagine().get(0));
            assertTrue(grazie.contains("due incantesimi della Luce Divina"), grazie);
            assertTrue(grazie.contains("due incantesimi dell'Alba Sacra"), grazie);
        }
    }

    private static IlGiroDelleSetteChiese giroAlRingraziamento(PartitaDiTest partita, int compagniGiaNelGruppo) {
        avvia(partita);
        IlGiroDelleSetteChiese giro = giro();
        for (int i = 0; i < compagniGiaNelGruppo; i++) {
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno" + i, 1));
        }
        List<CoordinateMD> templi = templi();
        for (int i = 0; i < 7; i++) {
            partita.gruppo().setCoordinate(templi.get(i));
            giro.controllaPostLocazione();
        }
        return giro;
    }

    @Test
    void alTermineIlSacerdoteSiOffreEAccettandoEntraNelGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(302)) {
            IlGiroDelleSetteChiese giro = giroAlRingraziamento(partita, 0);
            assertFalse(partita.gruppo().isSacerdoteAccolto());
            Passo proposta = giro.getDomandaDaPorre(Passo.MomentoControllo.POST_LOCAZIONE);
            assertNotNull(proposta);
            assertTrue(proposta.isConferma());

            giro.rispondi(Passo.SI);
            giro.controllaPostLocazione();

            assertEquals(2, partita.gruppo().getNumeroPersonaggiPermanenti());
            assertTrue(partita.gruppo().isSacerdoteAccolto());
            assertFalse(OffertaDelSacerdote.puoProporsi(), "la benedizione non lo offrirà di nuovo");
            assertTrue(giro.isCompleta());
            TipoPersonaggio classe = partita.gruppo().getPersonaggi().get(1).getClasse();
            assertTrue(classe == TipoPersonaggio.SACERDOTE || classe == TipoPersonaggio.SACERDOTESSA, classe.name());
        }
    }

    @Test
    void rifiutandoNonEntraENonPrecludeUnaProssimaOfferta() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(303)) {
            IlGiroDelleSetteChiese giro = giroAlRingraziamento(partita, 0);
            giro.rispondi(Passo.NO);
            giro.controllaPostLocazione();

            assertEquals(1, partita.gruppo().getNumeroPersonaggiPermanenti());
            assertFalse(partita.gruppo().isSacerdoteAccolto());
            assertTrue(giro.isCompleta());
            assertTrue(OffertaDelSacerdote.puoProporsi());
        }
    }

    @Test
    void conCinqueFissiIlSacerdoteDiceCheSieteTroppiENonEntra() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(304)) {
            IlGiroDelleSetteChiese giro = giroAlRingraziamento(partita, 4);

            assertNull(giro.getDomandaDaPorre(Passo.MomentoControllo.POST_LOCAZIONE));
            assertTrue(giro.isCompleta());
            assertEquals(5, partita.gruppo().getNumeroPersonaggiPermanenti());
            assertFalse(partita.gruppo().isSacerdoteAccolto());
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("Siete già in cinque: non penso abbiate bisogno anche dell'aiuto d")),
                    String.valueOf(partita.testi()));
        }
    }

    @Test
    void seUnSacerdoteEGiaEntratoIlGiroNonLoRipropone() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(305)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.gruppo().setSacerdoteAccolto(true);
            partita.comando(Comando.ESCI_DA_CITTA);
            IlGiroDelleSetteChiese giro = giro();
            List<CoordinateMD> templi = templi();
            for (int i = 0; i < 7; i++) {
                partita.gruppo().setCoordinate(templi.get(i));
                giro.controllaPostLocazione();
            }

            assertNull(giro.getDomandaDaPorre(Passo.MomentoControllo.POST_LOCAZIONE));
            assertTrue(giro.isCompleta());
            assertEquals(1, partita.gruppo().getNumeroPersonaggiPermanenti());
            assertTrue(partita.testi().stream().noneMatch(t -> t.startsWith("Siete già in")), String.valueOf(partita.testi()));
        }
    }

    private static void avvia(PartitaDiTest partita) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        partita.comando(Comando.ESCI_DA_CITTA);
    }

    private static int elementali(PartitaDiTest partita) {
        int totale = 0;
        for (TipoIncantesimo classe : com.threeamigos.foresta.missioni.principali.IlGiroDelleSetteChiese.INCANTESIMI_ELEMENTALI) {
            totale += partita.gruppo().getIncantesimi(classe);
        }
        return totale;
    }

    @Test
    void ilTempioRipulitoNonHaMostriNeOggetti() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(302)) {
            avvia(partita);
            // Un tempio senza artefatto: quello di un artefatto lo custodisce comunque (vedi più sotto)
            CoordinateMD coordinate = templi().stream()
                    .filter(tempio -> RegistroArtefatti.getArtefattoInLocazione(tempio) == null).findFirst().get();
            LocazioneMD modello = Foresta.getLocazioneMD(coordinate);
            partita.gruppo().setCoordinate(coordinate);
            Tempio.ripulisci(modello);
            Tempio tempio = new Tempio();
            tempio.setModelloDati(modello);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.rimuoviPersonaggi();
            tempio.crea(partita.gruppo(), avversari);
            assertEquals(0, avversari.getNumeroPersonaggi());
            assertNull(tempio.getOggetto());

            // Un artefatto che si trova lì, invece, c'è: il gruppo lo prende come in ogni locazione senza custodi
            ArtefattoMD artefatto = new ArtefattoMD();
            artefatto.setTipo(TipoArtefatto.ELMO);
            artefatto.setNome("l'elmo di Gorgor");
            artefatto.setDescrizione("che protegge poco");
            artefatto.setLivello(3);
            artefatto.setPeso(1.5);
            RegistroArtefatti.addArtefattoInLocazione(Artefatto.di(artefatto), coordinate);
            Tempio conArtefatto = new Tempio();
            conArtefatto.setModelloDati(modello);
            conArtefatto.crea(partita.gruppo(), avversari);
            assertEquals(0, avversari.getNumeroPersonaggi());
            assertNotNull(conArtefatto.getOggetto());
            // Un elmo: il bottino ha la forma del suo artefatto (e il suo sprite), ma si descrive e si rivela come artefatto già fatto
            assertEquals(TipoBottino.ELMO, conArtefatto.getOggetto().getClasse());
            assertTrue(conArtefatto.getOggetto().isDaRivelare());
        }
    }

    @Test
    void ITrofeiDelleVisiteEDegliEremitiContanoAFineLocazione() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(303)) {
            partita.pubblica(new InternoLocazioneVisitata(TipoLocazione.GROTTA));
            partita.pubblica(new InternoLocazioneVisitata(TipoLocazione.GROTTA));
            partita.pubblica(new InternoLocazioneVisitata(TipoLocazione.TEMPIO));
            partita.pubblica(new InternoLocazioneVisitata(TipoLocazione.BOSCO));
            partita.pubblica(new InternoEremitiIncontrati(3));
            assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.SPELEOLOGO), "il progresso resta in sospeso fino a fine locazione");
            partita.pubblica(new InternoFineLocazione());
            assertEquals(2, RegistroTrofei.getProgresso(TipoTrofeo.SPELEOLOGO));
            assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.TUTTI_I_SANTI));
            assertEquals(3, RegistroTrofei.getProgresso(TipoTrofeo.VERO_ROMPISCATOLE));
        }
    }
}
