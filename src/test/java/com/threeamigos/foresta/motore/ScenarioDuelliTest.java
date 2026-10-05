package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoPersonaggioArreso;
import com.threeamigos.foresta.missioni.CombattimentoRichiesto;
import com.threeamigos.foresta.missioni.IncaricoDiCombattimento;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I combattimenti fino alla resa: lo sfidante sconfitto si arrende e resta vivo, ma conta come sconfitto; se si
 * arrende tutto il gruppo, il duello è perso, nessuno muore e lo sfidante aspetta la rivincita.
 */
class ScenarioDuelliTest {

    private static final String SFIDA = "CHIAVE=SFIDA_DI_PROVA;TIPO=DUELLO;ASPETTO=QUALUNQUE;MANDANTE=il maestro d'armi;"
            + "NEMICO=GUERRIERO;NUMERO=1;CAPO=Uberto;RESA=SI;LUOGO=RADURA;MONETE=35;TITOLO=La sfida;RICHIESTA=Sfida.;"
            + "BATTUTA=E noi?;RISPOSTA=Voi.;VITTORIA=Uberto vi stringe la mano.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui sfidava Uberto.";
    private static final String SFIDA_A_DUELLO = SFIDA + ";DUELLO=SI";

    @Test
    void chiCombatteFinoAllaResaSiArrendeInveceDiMorire() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(221)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.eventi().ascolta(InternoPersonaggioArreso.class);
            Personaggio uberto = IncontroDiMissione.di(TipoPersonaggio.GUERRIERO, 1).conCapo("Uberto").finoAllaResa().crea().get(0);
            assertTrue(uberto.isFinoAllaResa());
            GruppoAvversario.getIstanza().aggiungiPersonaggio(uberto);

            uberto.subSalute(10000, partita.gruppo().getCapo(), Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
            assertTrue(uberto.isVivo());
            assertTrue(uberto.isInPanchina());
            assertTrue(uberto.isFuoriCombattimento());
            assertEquals(1, uberto.getSalute());
            assertFalse(GruppoAvversario.getIstanza().getPersonaggiVivi().contains(uberto), "chi è in panchina non conta fra i vivi");
            assertSame(uberto, partita.eventi().ultimo(InternoPersonaggioArreso.class).getPersonaggio());
            assertTrue(partita.testi().contains("Uberto abbassa le armi e si arrende."), String.valueOf(partita.testi()));

            // Contro di lui si arrende anche chi è del gruppo
            Personaggio arsenio = partita.gruppo().getCapo();
            arsenio.subSalute(10000, uberto, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
            assertTrue(arsenio.isVivo());
            assertTrue(arsenio.isInPanchina());
            assertTrue(partita.testi().contains("Arsenio ammette la sconfitta e si fa da parte."), String.valueOf(partita.testi()));

            partita.gruppo().svuotaPanchina();
            assertFalse(arsenio.isInPanchina());
        }
    }

    @Test
    void inUnCombattimentoNormaleSiMuoreComePrima() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(222)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio goblin = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1).crea().get(0);
            GruppoAvversario.getIstanza().aggiungiPersonaggio(goblin);
            Guerriero compagno = new Guerriero("Compagno", 1);
            partita.gruppo().aggiungiPersonaggio(compagno);
            goblin.subSalute(10000, partita.gruppo().getCapo(), Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
            compagno.subSalute(10000, goblin, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
            assertFalse(goblin.isVivo());
            assertFalse(compagno.isVivo());
            assertFalse(goblin.isInPanchina());
        }
    }

    @Test
    void loSfidanteSconfittoSiArrendeEContaComeSconfitto() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(223)) {
            IncaricoDiCombattimento sfida = prendiLaSfida(partita);
            partita.eventi().ascolta(InternoAvversarioSconfitto.class, InternoPersonaggioArreso.class);
            entraNelCovo(partita, sfida.getCovo());
            Personaggio uberto = GruppoAvversario.getIstanza().getCapo();
            assertEquals("Uberto", uberto.getNome());
            assertTrue(uberto.isFinoAllaResa());
            // Uberto è già allo stremo: basta un colpo
            uberto.getModelloDati().set(TipoAttributo.SALUTE, 1);

            combatti(partita);
            assertTrue(uberto.isVivo(), "si è arreso, non è morto");
            assertEquals(1, partita.eventi().tutti(InternoAvversarioSconfitto.class).size(), "conta come sconfitto, una volta sola");
            assertTrue(partita.eventi().haRicevuto(InternoPersonaggioArreso.class));
            assertEquals("RITORNO", sfida.getPassoCorrente());
            assertTrue(partita.testi().contains("Uberto vi stringe la mano. Il maestro d'armi aspetta a Nyena."), String.valueOf(partita.testi()));
        }
    }

    @Test
    void ilDuelloPersoNonUccideNessunoELoSfidanteAspettaLaRivincita() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(224)) {
            IncaricoDiCombattimento sfida = prendiLaSfida(partita);
            CoordinateMD covo = sfida.getCovo();
            Personaggio arsenio = partita.gruppo().getCapo();
            // Arsenio è già allo stremo: al primo colpo si arrende
            arsenio.getModelloDati().set(TipoAttributo.SALUTE, 1);
            entraNelCovo(partita, covo);
            // Uberto invece regge quanto serve: Arsenio colpisce per primo, e non deve farlo arrendere prima del suo colpo
            GruppoAvversario.getIstanza().getCapo().getModelloDati().set(TipoAttributo.SALUTE, 10_000);

            combatti(partita);
            assertNotEquals(Stato.GIOCO_PERSO, partita.stato());
            assertTrue(arsenio.isVivo());
            assertFalse(arsenio.isInPanchina(), "finito il combattimento si torna in campo");
            assertTrue(partita.testi().contains("Arsenio ammette la sconfitta e si fa da parte."), String.valueOf(partita.testi()));
            assertTrue(partita.testi().contains("Uberto ha vinto, e vi lascia andare. Vi aspetta qui per la rivincita, quando sarete in forze."),
                    String.valueOf(partita.testi()));
            assertEquals("CACCIA", sfida.getPassoCorrente());
            assertFalse(sfida.isFallita());
            assertTrue(RegistroMissioni.getIncontroMissione(covo).isPresent(), "Uberto aspetta la rivincita");
        }
    }

    @Test
    void aDuelloSiSceglieChiAffrontaLoSfidanteEGliAltriVannoInPanchina() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(225)) {
            IncaricoDiCombattimento sfida = prendiLaSfida(partita, SFIDA_A_DUELLO);
            Personaggio arsenio = partita.gruppo().getCapo();
            Guerriero compagno = new Guerriero("Compagno", 1);
            partita.gruppo().aggiungiPersonaggio(compagno);
            entraNelCovo(partita, sfida.getCovo());

            partita.assertStato(Stato.SCELTA_MANUALE_PERSONAGGIO);
            assertTrue(partita.testi().contains("Uberto sfida a duello uno di voi: chi accetta la sfida?"), String.valueOf(partita.testi()));
            assertTrue(partita.comandiDisponibili().containsAll(Arrays.asList(Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_2, Comando.ANNULLA)));
            partita.comando(Comando.PERSONAGGIO_2);
            assertTrue(partita.testi().contains("Compagno accetta la sfida: gli altri si fanno da parte."), String.valueOf(partita.testi()));
            assertTrue(arsenio.isInPanchina());
            assertFalse(compagno.isInPanchina());
            assertEquals(Collections.singletonList(compagno), partita.gruppo().getPersonaggiVivi());

            // Combatte solo il compagno, scelto da solo perché è l'unico in campo
            GruppoAvversario.getIstanza().getCapo().getModelloDati().set(TipoAttributo.SALUTE, 1);
            combatti(partita);
            assertEquals("RITORNO", sfida.getPassoCorrente());
            assertFalse(arsenio.isInPanchina(), "finito il duello si torna tutti in campo");
        }
    }

    @Test
    void rifiutandoLaSfidaIlGruppoSeNeVaELoSfidanteResta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(226)) {
            IncaricoDiCombattimento sfida = prendiLaSfida(partita, SFIDA_A_DUELLO);
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
            entraNelCovo(partita, sfida.getCovo());
            partita.comando(Comando.ANNULLA);
            assertTrue(partita.testi().contains("Rifiutate la sfida. Uberto vi aspetta qui, se cambiate idea."), String.valueOf(partita.testi()));
            assertNotEquals(Stato.IN_LOCAZIONE, partita.stato());
            assertEquals("CACCIA", sfida.getPassoCorrente());
            assertTrue(RegistroMissioni.getIncontroMissione(sfida.getCovo()).isPresent());
        }
    }

    @Test
    void seIlDuellantePerdeIlDuelloEPersoAncheSeGliAltriSonoInPanchina() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(227)) {
            IncaricoDiCombattimento sfida = prendiLaSfida(partita, SFIDA_A_DUELLO);
            Guerriero compagno = new Guerriero("Compagno", 1);
            partita.gruppo().aggiungiPersonaggio(compagno);
            compagno.getModelloDati().set(TipoAttributo.SALUTE, 1);
            entraNelCovo(partita, sfida.getCovo());
            partita.comando(Comando.PERSONAGGIO_2);

            combatti(partita);
            assertNotEquals(Stato.GIOCO_PERSO, partita.stato());
            assertTrue(compagno.isVivo());
            assertTrue(partita.testi().contains("Uberto ha vinto, e vi lascia andare. Vi aspetta qui per la rivincita, quando sarete in forze."),
                    String.valueOf(partita.testi()));
            assertEquals("CACCIA", sfida.getPassoCorrente());
        }
    }

    @Test
    void daSoloNonSiSceglieNiente() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(228)) {
            IncaricoDiCombattimento sfida = prendiLaSfida(partita, SFIDA_A_DUELLO);
            entraNelCovo(partita, sfida.getCovo());
            partita.assertStato(Stato.IN_LOCAZIONE);
            assertTrue(partita.testi().contains("Uberto vi sfida a duello."), String.valueOf(partita.testi()));
            assertTrue(partita.testi().contains("Arsenio accetta la sfida."), String.valueOf(partita.testi()));
            assertThrows(IllegalArgumentException.class, () -> CombattimentoRichiesto.da(SFIDA_A_DUELLO.replace("NUMERO=1", "NUMERO=2")));
        }
    }

    private static IncaricoDiCombattimento prendiLaSfida(PartitaDiTest partita) {
        return prendiLaSfida(partita, SFIDA);
    }

    private static IncaricoDiCombattimento prendiLaSfida(PartitaDiTest partita, String riga) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        assertTrue(CombattimentoRichiesto.da(riga).isFinoAllaResa());
        IncaricoDiCombattimento sfida = RegistroMissioni.getTutteLeMissioni().stream().filter(IncaricoDiCombattimento.class::isInstance)
                .map(IncaricoDiCombattimento.class::cast).findFirst().orElseThrow(AssertionError::new);
        sfida.aggiungiProprieta("PARAMETRO_" + IncaricoDiCombattimento.INCARICO, riga);
        sfida.controllaPreLocazione();
        sfida.segnaIntermezzoPassoMostrato("INCARICO");
        sfida.controllaInLocazione();
        assertTrue(sfida.isAttiva());
        return sfida;
    }

    private static void entraNelCovo(PartitaDiTest partita, CoordinateMD covo) {
        partita.comando(Comando.ESCI_DA_CITTA);
        partita.gruppo().setCoordinate(new CoordinateMD(covo.getX(), covo.getY() + 1));
        partita.assertStato(Stato.SCELTA_DIREZIONE);
        partita.comando(Comando.NORD).comando(Comando.NUMERO_1);
        assertEquals(covo, partita.gruppo().getCoordinate());
    }

    /**
     * Combatte finché il combattimento non finisce, in un modo o nell'altro.
     */
    private static void combatti(PartitaDiTest partita) {
        partita.comando(Comando.COMBATTIMENTO);
        for (int giri = 0; partita.stato() == Stato.IN_COMBATTIMENTO && giri < 2000; giri++) {
            partita.scatta();
        }
        assertNotEquals(Stato.IN_COMBATTIMENTO, partita.stato());
    }
}
