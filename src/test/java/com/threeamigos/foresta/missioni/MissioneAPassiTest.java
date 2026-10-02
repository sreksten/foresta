package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.modellodati.MissioneMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MissioneAPassiTest {

    @BeforeEach
    void nuovaPartita() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    /**
     * Una missione di prova: INCARICO (in locazione) → RECUPERO (post locazione, con intermezzo) → RAMO (in
     * locazione, si dirama secondo la proprietà SCELTA) → PREMIO o CASTIGO → fine. I passi si concludono quando il
     * test accende il loro interruttore.
     */
    static class MissioneDiProvaAPassi extends MissioneAPassi {

        final Set<String> pronti = new HashSet<>();
        final List<String> eseguiti = new ArrayList<>();

        MissioneDiProvaAPassi() {
            super(ClasseMissione.MISSIONE_DI_PROVA);
        }

        @Override
        protected String passoIniziale() {
            return "INCARICO";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            switch (id) {
                case "INCARICO":
                    return passo(MomentoControllo.IN_LOCAZIONE, id).esegui(() -> {
                        eseguiti.add(id);
                        attivaMissione();
                    }).poi("RECUPERO");
                case "RECUPERO":
                    return passo(MomentoControllo.POST_LOCAZIONE, id)
                            .conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA, Collections::emptyList)
                            .poi("RAMO");
                case "RAMO":
                    return passo(MomentoControllo.IN_LOCAZIONE, id)
                            .poi(() -> "BUONA".equals(ottieniProprieta("SCELTA")) ? "PREMIO" : "CASTIGO");
                case "PREMIO":
                case "CASTIGO":
                    return passo(MomentoControllo.IN_LOCAZIONE, id).poi(Passo.FINE);
                default:
                    throw new IllegalArgumentException(id);
            }
        }

        private Passo passo(MomentoControllo momento, String id) {
            return Passo.quando(momento, () -> pronti.contains(id)).esegui(() -> eseguiti.add(id));
        }
    }

    @Test
    void unPassoAvanzaSoloNelSuoControlloEQuandoEConcluso() {
        MissioneDiProvaAPassi missione = new MissioneDiProvaAPassi();
        assertEquals("INCARICO", missione.getPassoCorrente());
        missione.controllaInLocazione();
        assertEquals("INCARICO", missione.getPassoCorrente(), "non concluso");
        missione.pronti.add("INCARICO");
        missione.controllaPreLocazione();
        missione.controllaPostLocazione();
        assertEquals("INCARICO", missione.getPassoCorrente(), "concluso ma è un passo in locazione");
        missione.controllaInLocazione();
        assertEquals("RECUPERO", missione.getPassoCorrente());
        assertTrue(missione.isAttiva());
        assertEquals(Collections.singletonList("INCARICO"), missione.eseguiti, "l'azione si esegue una volta sola");
    }

    @Test
    void laDiramazioneLeggeLaProprietaELaFineCompletaLaMissione() {
        MissioneDiProvaAPassi missione = new MissioneDiProvaAPassi();
        missione.pronti.addAll(Arrays.asList("INCARICO", "RECUPERO", "RAMO", "PREMIO", "CASTIGO"));
        missione.controllaInLocazione();
        missione.controllaPostLocazione();
        missione.aggiungiProprieta("SCELTA", "BUONA");
        missione.controllaInLocazione();
        // RAMO, PREMIO e la fine sono tutti in locazione e già conclusi: si percorrono nello stesso controllo
        assertEquals(Arrays.asList("INCARICO", "RECUPERO", "RAMO", "PREMIO"), missione.eseguiti);
        assertEquals(Passo.FINE, missione.getPassoCorrente());
        assertTrue(missione.isCompleta());
    }

    @Test
    void ilPassoConIntermezzoRestaInAttesaFinchéNonESegnatoMostrato() {
        MissioneDiProvaAPassi missione = new MissioneDiProvaAPassi();
        missione.pronti.addAll(Arrays.asList("INCARICO", "RECUPERO"));
        missione.controllaInLocazione();
        assertTrue(missione.getPassiConIntermezzoInAttesa().isEmpty());
        missione.controllaPostLocazione();
        assertEquals(Collections.singletonList("RECUPERO"), missione.getPassiConIntermezzoInAttesa());
        assertEquals("RECUPERO", missione.getPassoConIntermezzoInAttesa(MomentoIntermezzo.LOCAZIONE_COMPLETATA));
        assertNull(missione.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
        missione.segnaIntermezzoPassoMostrato("RECUPERO");
        assertTrue(missione.isIntermezzoPassoMostrato("RECUPERO"));
        assertTrue(missione.getPassiConIntermezzoInAttesa().isEmpty());
    }

    @Test
    void dopoUnSalvataggioLaMissioneRiprendeDalPassoGiusto() throws Exception {
        MissioneDiProvaAPassi missione = new MissioneDiProvaAPassi();
        missione.pronti.addAll(Arrays.asList("INCARICO", "RECUPERO"));
        missione.controllaInLocazione();
        missione.controllaPostLocazione();

        StringWriter testo = new StringWriter();
        try (PrintWriter stream = new PrintWriter(testo)) {
            missione.getModelloDati().salva(stream);
        }
        MissioneMD riletto = new MissioneMD();
        riletto.leggi(new BufferedReader(new StringReader(testo.toString())));
        MissioneDiProvaAPassi ricaricata = new MissioneDiProvaAPassi();
        ricaricata.setModelloDati(riletto);

        assertEquals("RAMO", ricaricata.getPassoCorrente());
        assertTrue(ricaricata.isAttiva());
        assertEquals(Collections.singletonList("RECUPERO"), ricaricata.getPassiConIntermezzoInAttesa());
        ricaricata.pronti.addAll(Arrays.asList("RAMO", "CASTIGO"));
        ricaricata.controllaInLocazione();
        assertEquals(Arrays.asList("RAMO", "CASTIGO"), ricaricata.eseguiti, "senza SCELTA il ramo va al castigo");
        assertTrue(ricaricata.isCompleta());
    }

    @Test
    void unaMissioneFallitaNonAvanzaPiu() {
        MissioneDiProvaAPassi missione = new MissioneDiProvaAPassi();
        missione.pronti.add("INCARICO");
        missione.controllaInLocazione();
        missione.fallisciMissione();
        missione.pronti.add("RECUPERO");
        missione.controllaPostLocazione();
        assertEquals("RECUPERO", missione.getPassoCorrente());
        assertTrue(missione.getPassiConIntermezzoInAttesa().isEmpty());
    }

    /**
     * Una missione che percorre una sequenza di passi fissata dal primo passo, come farebbe una missione generata.
     */
    static class MissioneASequenza extends MissioneAPassi {

        final List<String> eseguiti = new ArrayList<>();

        MissioneASequenza() {
            super(ClasseMissione.MISSIONE_DI_PROVA);
        }

        @Override
        protected String passoIniziale() {
            return "GENERA";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            if ("GENERA".equals(id)) {
                return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> true).esegui(() -> {
                    attivaMissione();
                    impostaSequenzaPassi(Arrays.asList("VISITA_1", "VISITA_2", "VISITA_3"));
                }).poi(() -> leggiSequenzaPassi().get(0));
            }
            return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> true).esegui(() -> eseguiti.add(id))
                    .poi(prossimoNellaSequenza());
        }
    }

    @Test
    void unaSequenzaGenerataSiPercorreFinoInFondo() {
        MissioneASequenza missione = new MissioneASequenza();
        missione.controllaPreLocazione();
        assertEquals("VISITA_1", missione.getPassoCorrente());
        assertEquals(Arrays.asList("VISITA_1", "VISITA_2", "VISITA_3"), missione.leggiSequenzaPassi());
        missione.controllaPostLocazione();
        assertEquals(Arrays.asList("VISITA_1", "VISITA_2", "VISITA_3"), missione.eseguiti);
        assertTrue(missione.isCompleta());
    }

    @Test
    void sequenzeEIdNonValidiSonoRifiutati() {
        MissioneASequenza missione = new MissioneASequenza();
        assertThrows(IllegalArgumentException.class, () -> missione.impostaSequenzaPassi(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> missione.impostaSequenzaPassi(Arrays.asList("A", "A")));
        assertThrows(IllegalArgumentException.class, () -> missione.impostaSequenzaPassi(Arrays.asList("A", "B,C")));
        assertThrows(IllegalArgumentException.class, () -> missione.impostaSequenzaPassi(Collections.singletonList("A|B")));
    }

    @Test
    void unCicloFraPassiSempreConclusiVieneSegnalato() {
        MissioneAPassi ciclica = new MissioneAPassi(ClasseMissione.MISSIONE_DI_PROVA) {
            @Override
            protected String passoIniziale() {
                return "A";
            }

            @Override
            protected Passo costruisciPasso(String id) {
                return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true).poi("A".equals(id) ? "B" : "A");
            }
        };
        assertThrows(IllegalStateException.class, ciclica::controllaInLocazione);
    }

    /**
     * Chiede una conferma dopo la locazione, quando il test lo permette; SI porta a ACCETTATA, NO a RIFIUTATA.
     */
    static class MissioneConConferma extends MissioneAPassi {

        boolean sePuoChiedere;
        final List<String> eseguiti = new ArrayList<>();

        MissioneConConferma() {
            super(ClasseMissione.MISSIONE_DI_PROVA);
        }

        @Override
        protected String passoIniziale() {
            return "PROPOSTA";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            if ("PROPOSTA".equals(id)) {
                return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> sePuoChiedere)
                        .chiediConferma("Accetti l'incarico?")
                        .esegui(() -> eseguiti.add(id))
                        .poi(() -> Passo.SI.equals(getRisposta(id)) ? "ACCETTATA" : "RIFIUTATA");
            }
            return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> true).esegui(() -> eseguiti.add(id)).poi(Passo.FINE);
        }
    }

    @Test
    void laDomandaSiOffreSoloQuandoSiPuoPorreENelSuoControllo() {
        MissioneConConferma missione = new MissioneConConferma();
        assertNull(missione.getDomandaDaPorre(MomentoControllo.POST_LOCAZIONE), "non ancora");
        missione.sePuoChiedere = true;
        assertNull(missione.getDomandaDaPorre(MomentoControllo.IN_LOCAZIONE), "è una domanda di fine locazione");
        Passo domanda = missione.getDomandaDaPorre(MomentoControllo.POST_LOCAZIONE);
        assertNotNull(domanda);
        assertTrue(domanda.isConferma());
        assertEquals("Accetti l'incarico?", domanda.getDomanda());
        assertEquals(Arrays.asList(Passo.SI, Passo.NO), domanda.getRispostePossibili());
        // Senza risposta il passo non si conclude, anche se la condizione è vera
        missione.controllaPostLocazione();
        assertEquals("PROPOSTA", missione.getPassoCorrente());
        assertTrue(missione.eseguiti.isEmpty());
    }

    @Test
    void laRispostaConcludeIlPassoEScegliIlRamo() {
        MissioneConConferma missione = new MissioneConConferma();
        missione.sePuoChiedere = true;
        assertThrows(IllegalArgumentException.class, () -> missione.rispondi("FORSE"));
        missione.rispondi(Passo.NO);
        assertNull(missione.getDomandaDaPorre(MomentoControllo.POST_LOCAZIONE), "ha già una risposta");
        missione.controllaPostLocazione();
        assertEquals(Arrays.asList("PROPOSTA", "RIFIUTATA"), missione.eseguiti);
        assertEquals(Passo.NO, missione.getRisposta("PROPOSTA"));
        assertTrue(missione.isCompleta());
        assertThrows(IllegalStateException.class, () -> missione.rispondi(Passo.SI), "la missione è finita: nessuna domanda");
    }

    @Test
    void unaSceltaHaDaDueACinqueOpzioniNumerate() {
        Passo scelta = Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true)
                .chiediScelta("Quale?", Arrays.asList("A", "B", "C"));
        assertFalse(scelta.isConferma());
        assertEquals(Arrays.asList("A", "B", "C"), scelta.getOpzioni());
        assertEquals(Arrays.asList("1", "2", "3"), scelta.getRispostePossibili());
        assertThrows(IllegalArgumentException.class, () -> Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true)
                .chiediScelta("Quale?", Collections.singletonList("A")));
        assertThrows(IllegalArgumentException.class, () -> Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true)
                .chiediScelta("Quale?", Arrays.asList("A", "B", "C", "D", "E", "F")));
    }
}
