package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.FavoreRichiesto;
import com.threeamigos.foresta.missioni.IlFavore;
import com.threeamigos.foresta.missioni.LaLealta;
import com.threeamigos.foresta.missioni.LealtaRichiesta;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Guerriera;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La lealtà: una sera, all'accampamento, un compagno confida un problema e chiede un favore, che è una missione
 * secondaria; fatto il favore, a fine locazione ringrazia e riceve un modificatore permanente.
 */
class ScenarioLealtaTest {

    private static final String GOBLIN = "CHIAVE=GOBLIN_DI_PROVA;CONFIDENZA=Ho un debito.;BATTUTA=Con chi?;RISPOSTA=Con dei goblin.;"
            + "FAVORE=Il debito;LUOGO=RADURA;NEMICO=GOBLIN;NUMERO=2;VITTORIA=Debito saldato per %PERSONAGGIO%.;"
            + "RINGRAZIAMENTO=Grazie davvero.;LEALTA=FORZA AUMENTO_FISSO 2;LEALE=%PERSONAGGIO% è più forte.";

    private static final String GIURAMENTO = "CHIAVE=GIURAMENTO_DI_PROVA;CONFIDENZA=Ho un giuramento.;BATTUTA=Quale?;RISPOSTA=Vegliare.;"
            + "FAVORE=Il giuramento;LUOGO=RADURA;VISITE=3;ORE=12;VEGLIA=Si veglia.;VITTORIA=Veglia finita.;"
            + "RINGRAZIAMENTO=Grazie.;LEALTA=FORZA AUMENTO_FISSO 2;LEALE=%PERSONAGGIO% è più forte.";

    @Test
    void ogniLealtaSiLegge() {
        Set<String> chiavi = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            LealtaRichiesta lealta = LealtaRichiesta.da(ProduttoreDiTestiCasuale.rigaDiMissioni(LaLealta.LEALTA));
            assertEquals(LealtaRichiesta.NOTA, lealta.nuovoModificatore().getNote());
            chiavi.add(lealta.getChiave());
        }
        assertTrue(chiavi.size() >= 6, String.valueOf(chiavi));
        assertEquals(FavoreRichiesto.Tipo.COMBATTIMENTO, LealtaRichiesta.da(GOBLIN).getFavore().getTipo());
        assertThrows(IllegalArgumentException.class, () -> LealtaRichiesta.da(GOBLIN.replace("LEALTA=FORZA AUMENTO_FISSO 2;", "")));
        assertThrows(IllegalArgumentException.class, () -> LealtaRichiesta.da(GOBLIN + ";VISITE=2;ORE=3;VEGLIA=x"));
        assertThrows(IllegalArgumentException.class, () -> LealtaRichiesta.da(GOBLIN + ";ASPETTO=SACERDOTE"));
    }

    @Test
    void allAccampamentoIlCompagnoChiedeUnFavoreEFattoloDiventaLeale() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(251)) {
            Guerriera bruna = new Guerriera("Bruna", 1);
            LaLealta lealta = preparaLAccampamento(partita, bruna);
            partita.nonSaltareIntermezzi();
            partita.eventi().ascolta(NotificaPaginaIntermezzo.class);
            partita.comando(Comando.ACCAMPAMENTO);

            // La confidenza intorno al fuoco, dopo la scena del primo accampamento: parla Bruna, risponde il capo
            assertTrue(lealta.isAttiva());
            assertEquals("Bruna", lealta.getNomeDelCompagno());
            assertEquals("La lealtà di Bruna", lealta.getNome());
            partita.saltaIntermezzi();
            List<String> scene = partita.eventi().tutti(NotificaPaginaIntermezzo.class).stream().map(ScenarioLealtaTest::battute)
                    .collect(Collectors.toList());
            assertEquals("Ho un debito. Con chi? Con dei goblin.", scene.get(scene.size() - 1), String.valueOf(scene));
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            assertTrue(lealta.isIntermezzoPassoMostrato("INCONTRO"));

            IlFavore favore = (IlFavore) lealta.getMissioniAffidate(LaLealta.FAVORE).get(0);
            assertTrue(favore.isAttiva());
            assertEquals("Il debito", favore.getNome());
            assertEquals("Per Bruna: sconfiggi due Goblin nel posto segnato sulla mappa.", favore.getDescrizione());
            assertEquals("Bruna ti ha chiesto un favore: Il debito.", lealta.getDescrizione());

            // Il favore: i goblin nella radura segnata sulla mappa
            favore.controllaInLocazione();
            CoordinateMD radura = favore.getPosto();
            assertEquals(ClassiLocazione.RADURA, Foresta.getLocazione(radura));
            partita.gruppo().setCoordinate(radura);
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GOBLIN));
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GOBLIN));
            favore.controllaPostLocazione();
            assertTrue(favore.isCompleta());
            assertTrue(partita.testi().contains("Debito saldato per Bruna."), String.valueOf(partita.testi()));

            // A fine locazione Bruna ringrazia, con un intermezzo, e diventa leale
            lealta.controllaPostLocazione();
            assertTrue(lealta.isCompleta());
            assertTrue(bruna.getModelloDati().getModificatori().stream().anyMatch(m -> LealtaRichiesta.NOTA.equals(m.getNote())));
            assertTrue(partita.testi().contains("Bruna è più forte."), String.valueOf(partita.testi()));
            List<PaginaIntermezzo> grazie = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA).getPagine();
            assertEquals("Bruna ha qualcosa da dire.", grazie.get(0).getTesto());

            // Chi è leale non chiede più niente: la lealtà nuova aspetta un altro compagno
            LaLealta nuova = RegistroMissioni.getTutteLeMissioni().stream().filter(LaLealta.class::isInstance)
                    .map(LaLealta.class::cast).filter(m -> m != lealta).findFirst().orElseThrow(AssertionError::new);
            LineaTemporale.aggiungiOre(LaLealta.ORE_FRA_DUE_LEALTA);
            nuova.controllaAccampamento();
            assertFalse(nuova.isAttiva());
            assertNull(nuova.getNomeDelCompagno());
        }
    }

    @Test
    void ilPostoDelFavoreSiSegnaSubitoAllAccampamentoAncheSenzaMuoversi() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(255)) {
            Guerriera bruna = new Guerriera("Bruna", 1);
            LaLealta lealta = preparaLAccampamento(partita, bruna);
            lealta.aggiungiProprieta("PARAMETRO_" + LaLealta.LEALTA, GIURAMENTO);
            partita.comando(Comando.ACCAMPAMENTO);
            partita.assertStato(Stato.SCELTA_DIREZIONE);

            // La lealtà ha affidato il favore (una veglia) durante lo stesso accampamento, senza muoversi di casella:
            // il posto deve già essere stato cercato e segnato sulla mappa, non solo dopo il prossimo ingresso in una locazione
            IlFavore favore = (IlFavore) lealta.getMissioniAffidate(LaLealta.FAVORE).get(0);
            assertTrue(favore.isAttiva());
            assertEquals(FavoreRichiesto.Tipo.VEGLIA, favore.getFavore().getTipo());
            CoordinateMD posto = favore.getPosto();
            assertNotNull(posto, "il posto del favore dovrebbe essere già segnato, senza bisogno di spostarsi");
            assertTrue(Foresta.isLocazioneConosciuta(posto));
        }
    }

    @Test
    void senzaCompagniNessunoSiConfida() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(252)) {
            LaLealta lealta = preparaLAccampamento(partita, null);
            lealta.controllaAccampamento();
            assertFalse(lealta.isAttiva());
        }
    }

    @Test
    void seIlCompagnoMuoreLaLealtaFallisceEConLeiIlFavore() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(253)) {
            Guerriera bruna = new Guerriera("Bruna", 1);
            LaLealta lealta = preparaLAccampamento(partita, bruna);
            partita.comando(Comando.ACCAMPAMENTO);
            Missione favore = lealta.getMissioniAffidate(LaLealta.FAVORE).get(0);

            bruna.subSalute(bruna.getSalute() + 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            assertFalse(bruna.isVivo());
            lealta.controllaPostLocazione();
            assertTrue(lealta.isFallita());
            assertTrue(favore.isFallita());
            assertTrue(partita.testi().contains("Bruna non è più con il gruppo: il favore non serve più."), String.valueOf(partita.testi()));
        }
    }

    @Test
    void laLealtaSopravviveAUnSalvataggio() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(254)) {
            LaLealta lealta = preparaLAccampamento(partita, new Guerriera("Bruna", 1));
            partita.comando(Comando.ACCAMPAMENTO);
            String id = lealta.getMissioniAffidate(LaLealta.FAVORE).get(0).getId();

            GestoreSalvataggi.salva(Comando.NUMERO_2);
            assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_2));
            LaLealta riletta = RegistroMissioni.getTutteLeMissioni().stream().filter(LaLealta.class::isInstance)
                    .map(LaLealta.class::cast).filter(Missione::isAttiva).findFirst().orElseThrow(AssertionError::new);
            assertEquals("Bruna", riletta.getCompagno().map(Personaggio::getNome).orElse(null));
            List<Missione> affidate = riletta.getMissioniAffidate(LaLealta.FAVORE);
            assertEquals(1, affidate.size());
            assertEquals(id, affidate.get(0).getId());
            assertEquals("Per Bruna: sconfiggi due Goblin nel posto segnato sulla mappa.", affidate.get(0).getDescrizione());
        }
    }

    /**
     * Il gruppo, con il compagno se c'è, di notte in un bosco: ci si può accampare. La lealtà ha la riga dei goblin.
     */
    private static LaLealta preparaLAccampamento(PartitaDiTest partita, Personaggio compagno) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
        partita.comando(Comando.ESCI_DA_CITTA);
        partita.gruppo().setCoordinate(unaCasellaDiBosco());
        // Ci si accampa solo con qualcuno da mettere di guardia: senza compagni la lealtà si controlla a mano
        if (compagno != null) {
            partita.gruppo().aggiungiPersonaggio(compagno);
        }
        while (LineaTemporale.getOra() <= 20) {
            LineaTemporale.aggiungiOre(1);
        }
        partita.assertStato(Stato.SCELTA_DIREZIONE);
        LaLealta lealta = RegistroMissioni.getTutteLeMissioni().stream().filter(LaLealta.class::isInstance)
                .map(LaLealta.class::cast).findFirst().orElseThrow(AssertionError::new);
        lealta.aggiungiProprieta("PARAMETRO_" + LaLealta.LEALTA, GOBLIN);
        return lealta;
    }

    private static String battute(NotificaPaginaIntermezzo evento) {
        return evento.getPagina().getBattuteProgrammate().stream().map(BattutaProgrammata::getBattuta)
                .map(b -> b.getTesto()).collect(Collectors.joining(" "));
    }

    private static CoordinateMD unaCasellaDiBosco() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == ClassiLocazione.BOSCO) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna casella di bosco");
    }
}
