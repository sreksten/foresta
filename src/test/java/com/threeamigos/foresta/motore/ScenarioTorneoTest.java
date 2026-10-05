package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.missioni.IlTorneo;
import com.threeamigos.foresta.missioni.LaLeggenda;
import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.missioni.PescaLeggendaria;
import com.threeamigos.foresta.missioni.TorneoRichiesto;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il torneo: in città qualcuno lo bandisce, con in palio un leggendario; nella lizza segnata sulla mappa si combattono
 * due turni e la finale, uno contro uno e fino alla resa; vinta la finale, il leggendario va al gruppo, e in città si
 * riscuote la borsa del vincitore.
 */
class ScenarioTorneoTest {

    private static final String ROSA = "CHIAVE=ROSA_DI_PROVA;ASPETTO=QUALUNQUE;MANDANTE=il borgomastro;TITOLO=Il torneo della Rosa;"
            + "LUOGO=RADURA;SFIDANTI=GUERRIERO ELFA;CAMPIONE=GUERRIERA;NOME=Bradamante;MONETE=40;RICHIESTA=Un torneo.;"
            + "BATTUTA=Il regolamento?;RISPOSTA=Chi vince vince.;PRIMO_TURNO=Il primo si arrende.;SECONDO_TURNO=La seconda si arrende.;"
            + "FINALE=%CAMPIONE% si arrende.;RINGRAZIAMENTO=Che torneo!;RICORDO=Qui %CAMPIONE% perse il torneo.";

    @Test
    void ogniTorneoSiLegge() {
        Set<String> chiavi = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            TorneoRichiesto torneo = TorneoRichiesto.da(ProduttoreDiTestiCasuale.rigaDiMissioni(IlTorneo.TORNEO));
            assertEquals(TorneoRichiesto.SFIDANTI_PRIMA_DELLA_FINALE, torneo.getSfidanti().size());
            assertFalse(torneo.pescaNomeDelCampione().isEmpty());
            chiavi.add(torneo.getChiave());
        }
        assertTrue(chiavi.size() >= 3, String.valueOf(chiavi));
        assertEquals("Bradamante", TorneoRichiesto.da(ROSA).pescaNomeDelCampione());
        assertThrows(IllegalArgumentException.class, () -> TorneoRichiesto.da(ROSA.replace("SFIDANTI=GUERRIERO ELFA", "SFIDANTI=GUERRIERO")));
        assertThrows(IllegalArgumentException.class, () -> TorneoRichiesto.da(ROSA.replace("NOME=Bradamante;", "")));
        assertThrows(IllegalArgumentException.class, () -> TorneoRichiesto.da(ROSA.replace("LUOGO=RADURA", "LUOGO=TEMPIO")));
    }

    @Test
    void dueTurniELaFinaleNellaLizzaPoiIlLeggendarioELaBorsa() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(281)) {
            IlTorneo torneo = iscriviti(partita);
            assertTrue(torneo.isAttiva());
            OggettoLeggendario leggendario = torneo.getLeggendario();
            assertNotNull(leggendario);
            assertEquals("Il torneo della Rosa", torneo.getNome());
            assertTrue(torneo.getDescrizione().startsWith("Il torneo della Rosa, il primo turno: affronta un Guerriero nella lizza "
                    + "segnata sulla mappa, uno contro uno e fino alla resa. In palio "), torneo.getDescrizione());

            CoordinateMD lizza = torneo.getLizza();
            assertEquals(TipoLocazione.RADURA, Foresta.getLocazione(lizza));
            assertTrue(Foresta.isLocazioneConosciuta(lizza));
            partita.gruppo().setCoordinate(lizza);

            // Il primo turno: un guerriero, uno contro uno e fino alla resa
            Personaggio guerriero = unoSolo(lizza);
            assertEquals(ClassePersonaggio.GUERRIERO, guerriero.getClasse());
            assertTrue(guerriero.isSfidante() && guerriero.isFinoAllaResa());
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GUERRIERO));
            torneo.controllaPostLocazione();
            assertEquals("SECONDO_TURNO", torneo.getPassoCorrente());
            assertTrue(partita.testi().contains("Il primo si arrende. Alla prossima visita alla lizza, il turno successivo."),
                    String.valueOf(partita.testi()));

            // Il secondo: un'elfa
            assertEquals(ClassePersonaggio.ELFA, unoSolo(lizza).getClasse());
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.ELFA));
            torneo.controllaPostLocazione();
            assertEquals("FINALE", torneo.getPassoCorrente());
            assertEquals("Il torneo della Rosa, la finale: affronta Bradamante, la Guerriera nella lizza segnata sulla mappa, uno "
                    + "contro uno e fino alla resa. In palio " + (torneo.getDescrizione().contains("ci sono") ? "ci sono " : "c'è ")
                    + leggendario.getNomeBreve() + ".", torneo.getDescrizione());

            // La finale: la campionessa, con il suo nome; vinta, il leggendario va al gruppo
            Personaggio campionessa = unoSolo(lizza);
            assertEquals(ClassePersonaggio.GUERRIERA, campionessa.getClasse());
            assertEquals("Bradamante", campionessa.getNome());
            int artefatti = partita.gruppo().getInventario().size();
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GUERRIERA));
            torneo.controllaPostLocazione();
            assertEquals("RITORNO", torneo.getPassoCorrente());
            assertEquals(artefatti + 1, partita.gruppo().getInventario().size());
            assertTrue(partita.gruppo().getInventario().stream().anyMatch(a -> leggendario.getNome().equals(a.getNome())),
                    String.valueOf(partita.gruppo().getInventario()));
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("Bradamante si arrende. Il giudice di gara consegna al gruppo")),
                    String.valueOf(partita.testi()));
            assertEquals("Qui Bradamante perse il torneo.", torneo.getRicordoDellaLocazione());

            // In città, la borsa del vincitore
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            int monete = partita.gruppo().getMonete();
            torneo.controllaPreLocazione();
            torneo.segnaIntermezzoPassoMostrato("RITORNO");
            torneo.controllaInLocazione();
            assertEquals(monete + 40, partita.gruppo().getMonete());
            assertTrue(torneo.isCompleta());
        }
    }

    @Test
    void ilLeggendarioDelTorneoNonEsceInUnaLeggenda() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(282)) {
            IlTorneo torneo = iscriviti(partita);
            String chiave = torneo.getLeggendario().getChiave();
            LaLeggenda leggenda = RegistroMissioni.getTutteLeMissioni().stream().filter(LaLeggenda.class::isInstance)
                    .map(LaLeggenda.class::cast).findFirst().orElseThrow(AssertionError::new);
            assertTrue(PescaLeggendaria.giaPescati(leggenda).contains(chiave));
            assertFalse(PescaLeggendaria.giaPescati(torneo).contains(chiave));
        }
    }

    /**
     * Il gruppo è a Nyena, dove il borgomastro bandisce il torneo della Rosa: il gruppo si iscrive.
     */
    private static IlTorneo iscriviti(PartitaDiTest partita) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        IlTorneo torneo = RegistroMissioni.getTutteLeMissioni().stream().filter(IlTorneo.class::isInstance)
                .map(IlTorneo.class::cast).findFirst().orElseThrow(AssertionError::new);
        torneo.aggiungiProprieta("PARAMETRO_" + IlTorneo.TORNEO, ROSA);
        torneo.controllaPreLocazione();
        torneo.segnaIntermezzoPassoMostrato("INCARICO");
        torneo.controllaInLocazione();
        return torneo;
    }

    private static Personaggio unoSolo(CoordinateMD lizza) {
        List<Personaggio> avversari = RegistroMissioni.getIncontroMissione(lizza).orElseThrow(AssertionError::new);
        assertEquals(1, avversari.size());
        return avversari.get(0);
    }
}
