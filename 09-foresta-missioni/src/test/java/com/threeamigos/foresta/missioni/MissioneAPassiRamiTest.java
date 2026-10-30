package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.PassiSinteticiTest.Sintetica;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I rami di MissioneAPassi che le prove con una missione inventata (PassiSinteticiTest) e il giocatore automatico lasciano scoperti:
 * partenza forzata in ogni stato, missioni attive col passo finale, oggetti seminati nel posto del ripiego o senza portatori, visite e
 * esplorazioni senza una meta, id di passo non validi, intermezzi in arrivo degli altri.
 */
class MissioneAPassiRamiTest {

    private static final MomentoControllo PRE = MomentoControllo.PRE_LOCAZIONE;
    private static final MomentoControllo POST = MomentoControllo.POST_LOCAZIONE;

    private CoordinateMD qui;

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(31);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        FabbricaMissioni.registra(MissionePredefinita.MISSIONE_DI_PROVA, Sintetica::new);
        qui = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA);
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private Sintetica nuova() {
        Sintetica m = new Sintetica();
        m.getModelloDati().setId("SINTETICA");
        RegistroMissioni.aggiungiMissioneSecondaria(m);
        return m;
    }

    /** Una missione attiva che ha già fatto tutti i suoi passi, ma non è ancora completa. */
    private Sintetica attivaAlPassoFinale() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        m.attivaMissione();
        m.aggiungiProprieta("PASSO_CORRENTE", Passo.FINE);
        assertTrue(m.isAttiva());
        assertFalse(m.isCompleta());
        return m;
    }

    @Test
    void laPartenzaForzataValeSoloPerUnaMissioneNonPartita() {
        Sintetica attiva = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        attiva.forzaPartenza(() -> true);
        assertTrue(attiva.isPartenzaForzata());
        attiva.attivaMissione();
        assertFalse(attiva.isPartenzaForzata());
        Sintetica fallita = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        fallita.forzaPartenza(() -> true);
        fallita.fallisciMissione();
        assertFalse(fallita.isPartenzaForzata());
        Sintetica completa = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        completa.forzaPartenza(() -> true);
        completa.completaMissione();
        assertFalse(completa.isPartenzaForzata());
    }

    @Test
    void laPartenzaForzataSiApplicaSoloAlPassoIniziale() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> true).poi("B")).passo("B", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        m.forzaPartenza(() -> true);
        m.aggiungiProprieta("PASSO_CORRENTE", "B");
        m.controllaPreLocazione();
        assertEquals("B", m.getPassoCorrente(), "il passo B non è quello iniziale: non parte da solo");
        assertFalse(m.isCompleta());
    }

    @Test
    void unaAzioneCheCompletaLaMissioneNonLaCompletaDueVolte() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> true).esegui(s::completaMissione).poi(Passo.FINE));
        m.controllaPreLocazione();
        assertTrue(m.isCompleta());
    }

    @Test
    void ilPuntoDiPartenzaNonSiSovrascrive() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        Foresta.setPosizioneDelGruppo(qui);
        m.attivaMissione();
        assertEquals(qui, m.getPuntoDiPartenza());
        Foresta.setPosizioneDelGruppo(new CoordinateMD(20, 20));
        m.attivaMissione();
        assertEquals(qui, m.getPuntoDiPartenza(), "resta dov'era partita");
        Sintetica fine = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        fine.aggiungiProprieta("PASSO_CORRENTE", Passo.FINE);
        fine.attivaMissione();
        assertTrue(fine.isAttiva());
    }

    @Test
    void unaMissioneAttivaAlPassoFinaleNonFaPiuNiente() {
        Sintetica m = attivaAlPassoFinale();
        m.controllaPreLocazione();
        m.registraEvento("QUALCOSA", 1);
        assertFalse(m.getOggettoInLocazione(qui, TipoLocazione.BOSCO, false).isPresent());
        assertFalse(m.getIncontroInLocazione(qui).isPresent());
        assertTrue(m.getOndateSuccessiveInLocazione(qui).isEmpty());
        assertNull(m.getDomandaDaPorre(PRE));
        assertFalse(m.haUnIntermezzoInArrivo(PRE, MomentoIntermezzo.INIZIO_LOCAZIONE));
        m.oggettoDiMissioneRaccolto("CHIAVE", 1);
        assertEquals(1, m.getContatore("CHIAVE"));
        m.completaMissione();
        m.oggettoDiMissioneRaccolto("CHIAVE", 1);
        assertEquals(1, m.getContatore("CHIAVE"), "a missione finita non si conta più");
        m.registraEvento("QUALCOSA", 1);
    }

    @Test
    void gliOggettiDelRipiegoNonSonoSempreQuelliDelLuogo() {
        NomeOggetto nome = NomeOggetto.maschile("ceppo", "ceppi");
        OggettiDaRaccogliere legno = OggettiDaRaccogliere.di("LEGNO", nome, 3).in(TipoLocazione.PALUDE).conProbabilita(100).conRipiegoDopoOre(1);
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).semina(legno).poi(Passo.FINE));
        m.attivaMissione();
        com.threeamigos.foresta.statistiche.LineaTemporale.aggiungiOre(5);
        m.controllaPreLocazione();
        CoordinateMD ripiego = m.getRipiego(legno);
        assertNotNull(ripiego);
        // Il ripiego c'è, ma il tipo della locazione non è fra quelli degli oggetti: non si semina lì
        assertFalse(m.getOggettoInLocazione(ripiego, TipoLocazione.TEMPIO, true).isPresent());
        assertTrue(m.getOggettoInLocazione(ripiego, TipoLocazione.PALUDE, true).isPresent());
        // Una locazione già visitata non dà oggetti a caso, anche se è del tipo giusto
        assertFalse(m.getOggettoInLocazione(new CoordinateMD(0, 0), TipoLocazione.PALUDE, true).isPresent());
    }

    @Test
    void iTrofeiSenzaPortatoriNonSiSeminano() {
        NomeOggetto nome = NomeOggetto.maschile("dente", "denti");
        OggettiDaRaccogliere denti = OggettiDaRaccogliere.di("DENTI", nome, 3).daiNemici(TipoPersonaggio.GOBLIN).conProbabilita(100);
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).semina(denti).poi(Passo.FINE));
        m.attivaMissione();
        GruppoAvversario.getIstanza().reimposta();
        assertFalse(m.getOggettoInLocazione(new CoordinateMD(0, 0), TipoLocazione.BOSCO, false).isPresent());
        GruppoAvversario.getIstanza().aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1));
        assertTrue(m.getOggettoInLocazione(new CoordinateMD(0, 0), TipoLocazione.BOSCO, false).isPresent());
        OggettiDaRaccogliere sfortunati = OggettiDaRaccogliere.di("DENTI", nome, 3).daiNemici(TipoPersonaggio.GOBLIN).conProbabilita(1);
        Sintetica s = nuova().passo("A", x -> Passo.quando(PRE, () -> false).semina(sfortunati).poi(Passo.FINE));
        s.attivaMissione();
        int visti = 0;
        for (int i = 0; i < 300; i++) {
            visti += s.getOggettoInLocazione(new CoordinateMD(0, 0), TipoLocazione.BOSCO, false).isPresent() ? 1 : 0;
        }
        assertTrue(visti < 300, "con l'1 per cento di probabilità non si trovano sempre");
    }

    @Test
    void sorvegliareEEsplorareSenzaUnaMetaONelPostoGiaVisitato() {
        Sintetica m = nuova().passo("A", s -> s._sorveglia(() -> null, 2, 1).poi(Passo.FINE));
        m.attivaMissione();
        m.controllaPreLocazione();
        assertEquals(0, m.getVisiteNelPassoCorrente());
        Sintetica altrove = nuova().passo("A", s -> s._sorveglia(() -> new CoordinateMD(1, 1), 2, 1).poi(Passo.FINE));
        altrove.attivaMissione();
        altrove.controllaPreLocazione();
        assertEquals(0, altrove.getVisiteNelPassoCorrente());
        Sintetica e = nuova().passo("A", s -> s._esplora(2).poi(Passo.FINE));
        e.attivaMissione();
        Foresta.setPosizioneDelGruppo(new CoordinateMD(7, 7));
        Foresta.setLocazioneVisitata(new CoordinateMD(7, 7), true);
        e.controllaPreLocazione();
        assertEquals(0, e.getCaselleEsplorate(), "una casella già visitata non è nuova");
        Foresta.setLocazioneVisitata(new CoordinateMD(8, 7), false);
        Foresta.setPosizioneDelGruppo(new CoordinateMD(8, 7));
        e.controllaPreLocazione();
        e.controllaPreLocazione();
        assertEquals(1, e.getCaselleEsplorate(), "una casella conta una volta sola");
    }

    @Test
    void evitareIlCombattimentoSenzaUnaMetaNonConclude() {
        Sintetica m = nuova().passo("A", s -> s._evita(PRE, () -> null).poi(Passo.FINE));
        m.controllaPreLocazione();
        assertFalse(m.isCompleta());
    }

    @Test
    void unIdDiPassoNonValidoSiRifiuta() {
        for (String id : new String[] {"", "A,B", "A§B", "A|B"}) {
            Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> true).poi(id));
            assertThrows(IllegalArgumentException.class, m::controllaPreLocazione, id);
        }
        Sintetica nullo = nuova().passo("A", s -> Passo.quando(PRE, () -> true).poi((String) null));
        assertThrows(RuntimeException.class, nullo::controllaPreLocazione);
        Sintetica m = nuova();
        assertThrows(IllegalArgumentException.class, () -> m.segnaIntermezzoPassoMostrato("A,B"));
    }

    @Test
    void gliIntermezziInArrivoDelleAltreMissioniBloccanoLaVisitaTranquilla() {
        Sintetica aspetta = nuova().passo("A", s -> Passo.quando(PRE, () -> true)
                .conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, Collections::emptyList).poi("B")).passo("B", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        Sintetica altra = new Sintetica() {
            @Override
            protected boolean aspettaUnaVisitaTranquilla() {
                return true;
            }
        };
        altra.getModelloDati().setId("ALTRA");
        altra.passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        RegistroMissioni.aggiungiMissioneSecondaria(altra);
        // Chi aspetta una visita tranquilla conta solo se il suo intermezzo è già in attesa: ora no, quindi non blocca nessuno
        assertTrue(aspetta._visitaTranquilla());
        // Chi non la aspetta ha un passo che si concluderà con un intermezzo: blocca l'altra
        assertFalse(altra._visitaTranquilla());
        aspetta.controllaPreLocazione();
        assertNotNull(aspetta.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertFalse(altra._visitaTranquilla());
        assertNull(altra.getDomandaDaPorre(POST));
    }

    @Test
    void unaMissioneFallitaNonContaPiuNienteNeRegistraEventi() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        m.attivaMissione();
        m.fallisciMissione();
        m.oggettoDiMissioneRaccolto("CHIAVE", 1);
        assertEquals(0, m.getContatore("CHIAVE"));
        m.registraEvento("QUALCOSA", 1);
        assertEquals(0, m.getConteggioNelPassoCorrente("QUALCOSA"));
    }

    @Test
    void evitareIlCombattimentoConcludeSoloNellaCasellaGiusta() {
        CoordinateMD meta = new CoordinateMD(12, 12);
        Sintetica m = nuova().passo("A", s -> s._evita(PRE, () -> meta).poi(Passo.FINE));
        m.attivaMissione();
        m.controllaPreLocazione();
        assertFalse(m.isCompleta(), "il gruppo non è nella casella");
        Foresta.setPosizioneDelGruppo(meta);
        m.controllaPreLocazione();
        assertTrue(m.isCompleta());
        Sintetica scoperti = nuova().passo("A", s -> s._evita(PRE, () -> meta).poi(Passo.FINE));
        scoperti.attivaMissione();
        scoperti.registraEvento(MissioneAPassi.EVENTO_COMBATTIMENTO, 1);
        scoperti.controllaPreLocazione();
        assertTrue(scoperti.isFallita());
    }

    @Test
    void unIntermezzoInArrivoDipendeDalPassoCorrente() {
        MomentoIntermezzo inizio = MomentoIntermezzo.INIZIO_LOCAZIONE;
        Sintetica altroControllo = nuova().passo("A", s -> Passo.quando(POST, () -> true).conIntermezzo(inizio, Collections::emptyList).poi(Passo.FINE));
        assertFalse(altroControllo.haUnIntermezzoInArrivo(PRE, inizio), "il passo si valuta in un altro controllo");
        Sintetica altroMomento = nuova().passo("A", s -> Passo.quando(PRE, () -> true)
                .conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA, Collections::emptyList).poi(Passo.FINE));
        assertFalse(altroMomento.haUnIntermezzoInArrivo(PRE, inizio), "l'intermezzo è di un altro momento");
        Sintetica domanda = nuova().passo("A", s -> Passo.quando(PRE, () -> true).chiediConferma("Si fa?")
                .conIntermezzo(inizio, Collections::emptyList).poi(Passo.FINE));
        assertFalse(domanda.haUnIntermezzoInArrivo(PRE, inizio), "una domanda aspetta la risposta");
        Sintetica fallito = nuova().passo("A", s -> Passo.quando(PRE, () -> true).falliscoSe(() -> true, () -> "no")
                .conIntermezzo(inizio, Collections::emptyList).poi(Passo.FINE));
        assertFalse(fallito.haUnIntermezzoInArrivo(PRE, inizio), "il passo è già fallito");
        Sintetica nonConcluso = nuova().passo("A", s -> Passo.quando(PRE, () -> false).conIntermezzo(inizio, Collections::emptyList).poi(Passo.FINE));
        assertFalse(nonConcluso.haUnIntermezzoInArrivo(PRE, inizio), "il passo non è ancora concluso");
        Sintetica pronto = nuova().passo("A", s -> Passo.quando(PRE, () -> true).conIntermezzo(inizio, Collections::emptyList).poi(Passo.FINE));
        assertTrue(pronto.haUnIntermezzoInArrivo(PRE, inizio));
    }

    @Test
    void unIntermezzoGiaMostratoOGiaInAttesaNonSiAggiungeDiNuovo() {
        MomentoIntermezzo inizio = MomentoIntermezzo.INIZIO_LOCAZIONE;
        Sintetica mostrato = nuova().passo("A", s -> Passo.quando(PRE, () -> true).conIntermezzo(inizio, Collections::emptyList).poi(Passo.FINE));
        mostrato.segnaIntermezzoPassoMostrato("A");
        mostrato.controllaPreLocazione();
        assertTrue(mostrato.getPassiConIntermezzoInAttesa().isEmpty());
        Sintetica inAttesa = nuova().passo("A", s -> Passo.quando(PRE, () -> true).conIntermezzo(inizio, Collections::emptyList).poi("B"))
                .passo("B", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        inAttesa.controllaPreLocazione();
        inAttesa.aggiungiProprieta("PASSO_CORRENTE", "A");
        inAttesa.controllaPreLocazione();
        assertEquals(Collections.singletonList("A"), inAttesa.getPassiConIntermezzoInAttesa());
        // Una lista salvata ma vuota si legge come vuota
        inAttesa.aggiungiProprieta("INTERMEZZI_IN_ATTESA", "");
        assertTrue(inAttesa.getPassiConIntermezzoInAttesa().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> inAttesa.segnaIntermezzoPassoMostrato(null));
    }

    @Test
    void gliIntermezziDelleMissioniSiVedonoQuandoUnaMissioneNeHaUnoInAttesa() {
        MomentoIntermezzo inizio = MomentoIntermezzo.INIZIO_LOCAZIONE;
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> true).conIntermezzo(inizio, Collections::emptyList).poi(Passo.FINE));
        m.controllaPreLocazione();
        assertNotNull(IntermezziDelleMissioni.ISTANZA.getProssimo(inizio));
        assertTrue(IntermezziDelleMissioni.ISTANZA.haUnIntermezzoInArrivo(inizio));
        assertFalse(IntermezziDelleMissioni.ISTANZA.blocca(com.threeamigos.foresta.tipi.TipoIntermezzo.INTERMEZZO_INTRODUTTIVO));
        Sintetica chePrendeLaScena = new Sintetica() {
            @Override
            public boolean bloccaIntermezzo(com.threeamigos.foresta.tipi.TipoIntermezzo tipo) {
                return true;
            }
        };
        chePrendeLaScena.getModelloDati().setId("BLOCCA");
        RegistroMissioni.aggiungiMissioneSecondaria(chePrendeLaScena);
        assertTrue(IntermezziDelleMissioni.ISTANZA.blocca(com.threeamigos.foresta.tipi.TipoIntermezzo.INTERMEZZO_INTRODUTTIVO));
    }

    @Test
    void laVisitaTranquillaTieneContoDiChiAspettaEHaGiaUnIntermezzoInAttesa() {
        MomentoIntermezzo inizio = MomentoIntermezzo.INIZIO_LOCAZIONE;
        Sintetica aspetta = new Sintetica() {
            @Override
            protected boolean aspettaUnaVisitaTranquilla() {
                return true;
            }
        };
        aspetta.getModelloDati().setId("ASPETTA");
        aspetta.passo("A", s -> Passo.quando(PRE, () -> true).conIntermezzo(inizio, Collections::emptyList).poi(Passo.FINE));
        RegistroMissioni.aggiungiMissioneSecondaria(aspetta);
        Sintetica altra = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        assertTrue(altra._visitaTranquilla());
        aspetta.controllaPreLocazione();
        assertNotNull(aspetta.getPassoConIntermezzoInAttesa(inizio));
        assertFalse(altra._visitaTranquilla(), "chi aspettava ha l'intermezzo in attesa");
    }

    @Test
    void unPassoPuoDareLeScelteSoloDaDueACinque() {
        for (int n : new int[] {0, 1, 6}) {
            java.util.List<String> opzioni = new java.util.ArrayList<>();
            for (int i = 0; i < n; i++) {
                opzioni.add("opzione " + i);
            }
            assertThrows(IllegalArgumentException.class, () -> Passo.quando(PRE, () -> true).chiediScelta("Quale?", opzioni));
        }
        assertEquals(2, Passo.quando(PRE, () -> true).chiediScelta("Quale?", java.util.Arrays.asList("a", "b")).getRispostePossibili().size());
    }

    @Test
    void gliIncontriDiUnPassoStannoSoloNelLuogoGiusto() {
        IncontroDiMissione incontro = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 2);
        CoordinateMD luogo = new CoordinateMD(9, 9);
        Passo passo = Passo.quando(PRE, () -> true).affronta(() -> luogo, incontro);
        assertSame(incontro, passo.getIncontroIn(luogo));
        assertNull(passo.getIncontroIn(null));
        assertNull(passo.getIncontroIn(new CoordinateMD(1, 1)));
        assertNull(Passo.quando(PRE, () -> true).getIncontroIn(luogo));
    }

    @Test
    void gliIncontriDiMissioneSiCreanoConCapoOndateEResa() {
        IncontroDiMissione primo = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 3).conCapo("Gorgo", TipoPersonaggio.HOBGOBLIN).aggirabile();
        assertEquals(4, primo.crea().size());
        IncontroDiMissione centro = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 4).conCapoAlCentro("Gorgo", TipoPersonaggio.HOBGOBLIN);
        assertEquals(TipoPersonaggio.HOBGOBLIN, centro.crea().get(2).getClasse());
        IncontroDiMissione ondataGiaAllaResa = IncontroDiMissione.di(TipoPersonaggio.TROLL, 1).finoAllaResa();
        IncontroDiMissione conOndate = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1)
                .poi(ondataGiaAllaResa, "Arriva un troll.").poi(IncontroDiMissione.di(TipoPersonaggio.SCHELETRO, 1), "Arriva un orco.");
        assertEquals(2, conOndate.creaOndateSuccessive().size());
        assertTrue(ondataGiaAllaResa.isFinoAllaResa());
        IncontroDiMissione tutteAllaResa = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 1).finoAllaResa()
                .poi(IncontroDiMissione.di(TipoPersonaggio.TROLL, 1), "Arriva un troll.");
        tutteAllaResa.creaOndateSuccessive();
        assertEquals(2, tutteAllaResa.getNumeroDiOndate());
    }
}
