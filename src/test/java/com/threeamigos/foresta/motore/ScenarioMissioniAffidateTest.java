package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le missioni secondarie affidate durante una missione (vedi MissioneAPassi.affida): una o più, o una invece di
 * un'altra secondo la scelta del giocatore; la missione aspetta che finiscano e sa com'è andata.
 */
class ScenarioMissioniAffidateTest {

    /**
     * Una missione secondaria che si completa o fallisce quando glielo si dice.
     */
    static class Incombenza extends MissioneAPassi {

        final String nome;
        boolean fatta;
        boolean fallita;

        Incombenza(String nome) {
            super(ClasseMissione.MISSIONE_DI_PROVA);
            this.nome = nome;
        }

        @Override
        public String getNome() {
            return nome;
        }

        @Override
        protected String passoIniziale() {
            return "FARE";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> fatta || fallita)
                    .esegui(() -> {
                        if (fallita) {
                            fallisciMissione();
                        }
                    })
                    .poi(Passo.FINE);
        }
    }

    /**
     * Chiede quale strada prendere: la prima affida un'incombenza, la seconda due.
     */
    static class MissioneDelleIncombenze extends MissioneAPassi {

        int affidamenti;

        MissioneDelleIncombenze() {
            super(ClasseMissione.MISSIONE_DI_PROVA);
        }

        @Override
        protected String passoIniziale() {
            return "SCELTA";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            switch (id) {
                case "SCELTA":
                    return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true)
                            .chiediScelta("Quale strada?", Arrays.asList("Una incombenza", "Due incombenze"))
                            .esegui(this::attivaMissione)
                            .poi("AFFIDO");
                case "AFFIDO":
                    return affida("INCOMBENZE", MomentoControllo.IN_LOCAZIONE, () -> true, this::nuoveIncombenze).poi("ATTESA");
                case "ATTESA":
                    return attendiLeAffidate("INCOMBENZE", MomentoControllo.POST_LOCAZIONE)
                            .poi(() -> sonoRiusciteLeAffidate("INCOMBENZE") ? Passo.FINE : "FALLIMENTO");
                default:
                    return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> true).esegui(this::fallisciMissione).poi(Passo.FINE);
            }
        }

        private List<Missione> nuoveIncombenze() {
            affidamenti++;
            return "1".equals(getRisposta("SCELTA")) ? Collections.singletonList(new Incombenza("A"))
                    : Arrays.asList(new Incombenza("B"), new Incombenza("C"));
        }
    }

    private static MissioneDelleIncombenze avvia(PartitaDiTest partita, String risposta) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
        MissioneDelleIncombenze missione = new MissioneDelleIncombenze();
        RegistroMissioni.getMissionePrincipale().aggiungiMissione(missione);
        missione.controllaInLocazione();
        missione.rispondi(risposta);
        missione.controllaInLocazione();
        return missione;
    }

    @Test
    void unaSceltaAffidaUnaMissioneLAltraDueEUnaVoltaSola() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(231)) {
            MissioneDelleIncombenze una = avvia(partita, "1");
            assertEquals("ATTESA", una.getPassoCorrente());
            List<Missione> affidate = una.getMissioniAffidate("INCOMBENZE");
            assertEquals(1, affidate.size());
            assertEquals("A", affidate.get(0).getNome());
            assertTrue(affidate.get(0).isAttiva());
            assertEquals(affidate, una.getMissioniSecondarie());
            assertTrue(RegistroMissioni.getTutteLeMissioni().contains(affidate.get(0)), "il registro vede anche le affidate");
            una.controllaInLocazione();
            assertEquals(1, una.affidamenti, "si affidano una volta sola");
        }
        try (PartitaDiTest partita = PartitaDiTest.nuova(232)) {
            MissioneDelleIncombenze due = avvia(partita, "2");
            List<Missione> affidate = due.getMissioniAffidate("INCOMBENZE");
            assertEquals(Arrays.asList("B", "C"), Arrays.asList(affidate.get(0).getNome(), affidate.get(1).getNome()));
        }
    }

    @Test
    void laMissioneAspettaCheFiniscanoTutteESaComEAndata() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(233)) {
            MissioneDelleIncombenze missione = avvia(partita, "2");
            Incombenza b = (Incombenza) missione.getMissioniAffidate("INCOMBENZE").get(0);
            Incombenza c = (Incombenza) missione.getMissioniAffidate("INCOMBENZE").get(1);

            b.fatta = true;
            b.controllaPostLocazione();
            missione.controllaPostLocazione();
            assertEquals("ATTESA", missione.getPassoCorrente(), "manca ancora C");

            c.fatta = true;
            c.controllaPostLocazione();
            missione.controllaPostLocazione();
            assertTrue(missione.isCompleta());
        }
        try (PartitaDiTest partita = PartitaDiTest.nuova(234)) {
            MissioneDelleIncombenze missione = avvia(partita, "1");
            Incombenza a = (Incombenza) missione.getMissioniAffidate("INCOMBENZE").get(0);
            a.fallita = true;
            a.controllaPostLocazione();
            assertTrue(a.isFallita());
            missione.controllaPostLocazione();
            assertFalse(missione.sonoRiusciteLeAffidate("INCOMBENZE"));
            assertTrue(missione.isFallita());
        }
    }
}
