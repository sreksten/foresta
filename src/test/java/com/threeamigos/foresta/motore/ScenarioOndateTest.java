package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAggiornamentoComandiDisponibili;
import com.threeamigos.foresta.eventi.interni.InternoAssegnaCoordinateAPersonaggi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.IncaricoDiCombattimento;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le ondate: sconfitti tutti gli avversari in campo, la locazione si riempie di nuovo, fino a tre ondate in tutto. Con
 * delle ondate in arrivo non si corrompe, non si fa amicizia e non si passa inosservati; chi fugge ricomincia.
 */
class ScenarioOndateTest {

    private static final String BATTAGLIA = "CHIAVE=BATTAGLIA_DI_PROVA;TIPO=BATTAGLIA;ASPETTO=CAPITANO;MANDANTE=il capitano;"
            + "NEMICO=GOBLIN;NUMERO=2;LUOGO=RADURA;MONETE=60;TITOLO=La battaglia;RICHIESTA=Goblin.;BATTUTA=Quanti?;RISPOSTA=Tanti.;"
            + "ONDATA_2=HOBGOBLIN 1;ARRIVO_2=Arriva un hobgoblin!;ONDATA_3=TROLL 1 Gruk;ARRIVO_3=Arriva %CAPO_3%!;"
            + "VITTORIA=Anche %CAPO_3% è a terra.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui cadde %CAPO_3%.";

    @Test
    void alPiuTreOndateEMaiInUnDuello() {
        IncontroDiMissione incontro = IncontroDiMissione.di(ClassePersonaggio.GOBLIN, 2)
                .poi(IncontroDiMissione.di(ClassePersonaggio.GOBLIN, 3), "Altri goblin!")
                .poi(IncontroDiMissione.di(ClassePersonaggio.TROLL, 1), "Un troll!");
        assertEquals(3, incontro.getNumeroDiOndate());
        assertEquals(Integer.valueOf(5), incontro.getSconfittiRichiesti().get(ClassePersonaggio.GOBLIN));
        assertEquals(Integer.valueOf(1), incontro.getSconfittiRichiesti().get(ClassePersonaggio.TROLL));
        assertThrows(IllegalStateException.class, () -> incontro.poi(IncontroDiMissione.di(ClassePersonaggio.GIGANTE, 1), "Troppi!"));
        assertThrows(IllegalStateException.class, () -> IncontroDiMissione.di(ClassePersonaggio.GUERRIERO, 1).aDuello()
                .poi(IncontroDiMissione.di(ClassePersonaggio.GUERRIERO, 1), "Un altro!"));
    }

    @Test
    void sconfittaUnOndataArrivaLaSuccessivaFinoAllUltima() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(291)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            CoordinateMD bosco = unaCasellaDi(TipoLocazione.BOSCO);
            partita.gruppo().setCoordinate(bosco);
            Locazione locazione = Foresta.costruisciIstanza(bosco);
            partita.gruppo().setLocazioneCorrente(locazione);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            IncontroDiMissione incontro = IncontroDiMissione.di(ClassePersonaggio.GOBLIN, 1)
                    .poi(IncontroDiMissione.di(ClassePersonaggio.HOBGOBLIN, 2), "Arrivano due hobgoblin!")
                    .poi(IncontroDiMissione.di(ClassePersonaggio.TROLL, 1).conCapo("Gruk"), "Arriva Gruk!");
            incontro.crea().forEach(avversari::aggiungiPersonaggio);
            avversari.setOndateSuccessive(incontro.creaOndateSuccessive());

            // All'ingresso: niente corruzione, amicizia o passaggio inosservato
            assertEquals(Stato.IN_LOCAZIONE, locazione.impostaAzioni(partita.gruppo(), avversari, null));
            assertComandiSenzaScorciatoie(partita);

            // Prima ondata sconfitta: arriva la seconda
            partita.eventi().ascolta(InternoAssegnaCoordinateAPersonaggi.class);
            sconfiggiTutti(avversari);
            assertEquals(Stato.IN_LOCAZIONE, locazione.impostaAzioni(partita.gruppo(), avversari, null));
            assertFalse(locazione.isCompleta());
            assertEquals(Arrays.asList(ClassePersonaggio.HOBGOBLIN, ClassePersonaggio.HOBGOBLIN), classi(avversari.getPersonaggiVivi()));
            assertTrue(partita.testi().contains("Arrivano due hobgoblin!"), String.valueOf(partita.testi()));
            assertEquals(1, partita.eventi().tutti(InternoAssegnaCoordinateAPersonaggi.class).size());
            assertComandiSenzaScorciatoie(partita);

            // Seconda sconfitta: arriva Gruk, l'ultima
            sconfiggiTutti(avversari);
            assertEquals(Stato.IN_LOCAZIONE, locazione.impostaAzioni(partita.gruppo(), avversari, null));
            assertEquals("Gruk", avversari.getPersonaggioVivo().getNome());
            assertFalse(avversari.hasOndateSuccessive());

            // Sconfitto anche lui, la locazione finisce
            sconfiggiTutti(avversari);
            assertEquals(Stato.FINE_LOCAZIONE, locazione.impostaAzioni(partita.gruppo(), avversari, null));
            assertEquals(2, partita.eventi().tutti(InternoAssegnaCoordinateAPersonaggi.class).size());
        }
    }

    @Test
    void laBattagliaSiVinceBattendoTutteLeOndateInUnaVisita() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(292)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            IncaricoDiCombattimento battaglia = RegistroMissioni.getTutteLeMissioni().stream()
                    .filter(IncaricoDiCombattimento.class::isInstance).map(IncaricoDiCombattimento.class::cast)
                    .findFirst().orElseThrow(AssertionError::new);
            battaglia.aggiungiProprieta("PARAMETRO_" + IncaricoDiCombattimento.INCARICO, BATTAGLIA);
            battaglia.controllaPreLocazione();
            battaglia.segnaIntermezzoPassoMostrato("INCARICO");
            battaglia.controllaInLocazione();
            CoordinateMD radura = battaglia.getCovo();
            assertEquals("Qui cadde Gruk.", battaglia.getRicordoDellaLocazione());

            // Si entra: due goblin, poi un hobgoblin, poi il troll Gruk
            assertEquals(Arrays.asList(ClassePersonaggio.GOBLIN, ClassePersonaggio.GOBLIN),
                    classi(RegistroMissioni.getIncontroMissione(radura).orElseThrow(AssertionError::new)));
            List<Ondata> ondate = RegistroMissioni.getOndateSuccessiveMissione(radura);
            assertEquals(2, ondate.size());
            assertEquals("Arriva un hobgoblin!", ondate.get(0).getArrivo());
            assertEquals("Arriva Gruk!", ondate.get(1).getArrivo());
            assertEquals("Gruk", ondate.get(1).getAvversari().get(0).getNome());

            // Battuti i goblin e l'hobgoblin, ma non Gruk (il gruppo è fuggito): la battaglia non è vinta
            partita.gruppo().setCoordinate(radura);
            sconfitti(partita, ClassePersonaggio.GOBLIN, ClassePersonaggio.GOBLIN, ClassePersonaggio.HOBGOBLIN);
            battaglia.controllaPostLocazione();
            assertEquals("CACCIA", battaglia.getPassoCorrente());

            // Alla visita dopo si ricomincia, anche a contare: battere di nuovo solo i goblin e l'hobgoblin non basta
            RegistroMissioni.getIncontroMissione(radura);
            sconfitti(partita, ClassePersonaggio.GOBLIN, ClassePersonaggio.GOBLIN, ClassePersonaggio.HOBGOBLIN);
            battaglia.controllaPostLocazione();
            assertEquals("CACCIA", battaglia.getPassoCorrente());

            // Tutte e tre le ondate nella stessa visita
            RegistroMissioni.getIncontroMissione(radura);
            sconfitti(partita, ClassePersonaggio.GOBLIN, ClassePersonaggio.GOBLIN, ClassePersonaggio.HOBGOBLIN, ClassePersonaggio.TROLL);
            battaglia.controllaPostLocazione();
            assertEquals("RITORNO", battaglia.getPassoCorrente());
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("Anche Gruk è a terra.")), String.valueOf(partita.testi()));
        }
    }

    private static void assertComandiSenzaScorciatoie(PartitaDiTest partita) {
        java.util.Collection<Comando> comandi = partita.eventi().ultimo(InternoAggiornamentoComandiDisponibili.class).getPossibilita();
        assertTrue(comandi.contains(Comando.COMBATTIMENTO), String.valueOf(comandi));
        assertFalse(comandi.contains(Comando.CORRUZIONE) || comandi.contains(Comando.AMICIZIA) || comandi.contains(Comando.PASSA_INOSSERVATO),
                String.valueOf(comandi));
    }

    private static void sconfiggiTutti(GruppoAvversario avversari) {
        for (Personaggio avversario : avversari.getPersonaggiVivi()) {
            avversario.subSalute(avversario.getSalute() + 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        }
    }

    private static void sconfitti(PartitaDiTest partita, ClassePersonaggio... classi) {
        for (ClassePersonaggio classe : classi) {
            partita.pubblica(new InternoAvversarioSconfitto(classe));
        }
    }

    private static List<ClassePersonaggio> classi(List<Personaggio> personaggi) {
        return personaggi.stream().map(Personaggio::getClasse).collect(java.util.stream.Collectors.toList());
    }

    private static CoordinateMD unaCasellaDi(TipoLocazione tipo) {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == tipo) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna casella di " + tipo);
    }
}
