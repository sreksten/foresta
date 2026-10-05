package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.OggettiDaRaccogliere;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I trofei che si prendono ai mostri (OggettiDaRaccogliere.daiNemici, con il ripiego) e l'esplorazione di caselle nuove
 * (MissioneAPassi.esplora).
 */
class ScenarioTrofeiEdEsplorazioneTest {

    static final OggettiDaRaccogliere ORECCHIE = OggettiDaRaccogliere
            .di("ORECCHIE", new NomeOggetto("orecchio di goblin", "orecchie di goblin", "un ", "alcune ", "il ", "le "), 3)
            .daiNemici(TipoPersonaggio.GOBLIN)
            .alPiuPerLocazione(5);

    static class MissioneDiUnPasso extends MissioneAPassi {

        private final Function<MissioneDiUnPasso, Passo> passo;

        MissioneDiUnPasso(Function<MissioneDiUnPasso, Passo> passo) {
            super(ClasseMissione.MISSIONE_DI_PROVA);
            this.passo = passo;
        }

        @Override
        protected String passoIniziale() {
            return "UNICO";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            return passo.apply(this).poi(Passo.FINE);
        }

        Passo trofei() {
            return raccogli(MomentoControllo.POST_LOCAZIONE, ORECCHIE);
        }

        Passo esplorazione(int caselle) {
            return esplora(caselle);
        }
    }

    private static MissioneDiUnPasso attiva(Function<MissioneDiUnPasso, Passo> passo) {
        MissioneDiUnPasso missione = new MissioneDiUnPasso(passo);
        RegistroMissioni.getMissionePrincipale().aggiungiMissione(missione);
        missione.attivaMissione();
        return missione;
    }

    private static void avversari(TipoPersonaggio classe, int quanti) {
        GruppoAvversario avversari = GruppoAvversario.getIstanza();
        avversari.rimuoviPersonaggi();
        for (int i = 0; i < quanti; i++) {
            avversari.aggiungiPersonaggio(FabbricaPersonaggi.crea(classe, 1));
        }
    }

    @Test
    void iTrofeiCompaionoSoloDoveCiSonoIMostriGiusti() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(181)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            MissioneDiUnPasso caccia = attiva(MissioneDiUnPasso::trofei);
            CoordinateMD casella = new CoordinateMD(0, 0);

            avversari(TipoPersonaggio.TROLL, 2);
            assertEquals(Optional.empty(), caccia.getOggettoInLocazione(casella, TipoLocazione.BOSCO, false), "niente goblin, niente orecchie");

            // Due goblin: al più due orecchie, anche in una casella già visitata e in qualunque locazione
            avversari(TipoPersonaggio.GOBLIN, 2);
            Oggetto orecchie = caccia.getOggettoInLocazione(casella, TipoLocazione.ROVINE, true).orElseThrow(AssertionError::new);
            assertTrue(orecchie.getQuantita() >= 1 && orecchie.getQuantita() <= 2, String.valueOf(orecchie.getQuantita()));
            assertEquals("orecchio di goblin", orecchie.getNomeSingolare());

            // Mai più di quante ne mancano
            orecchie.prendi(partita.gruppo(), null);
            avversari(TipoPersonaggio.GOBLIN, 4);
            int mancanti = 3 - caccia.getContatore("ORECCHIE");
            for (int i = 0; i < 20; i++) {
                caccia.getOggettoInLocazione(casella, TipoLocazione.BOSCO, false)
                        .ifPresent(o -> assertTrue(o.getQuantita() <= mancanti));
            }

            // Dopo tre giorni il ripiego: un bosco segnato sulla mappa, con i goblin che portano le orecchie mancanti
            LineaTemporale.aggiungiOre(OggettiDaRaccogliere.ORE_AL_RIPIEGO);
            caccia.controllaPreLocazione();
            CoordinateMD ripiego = caccia.getRipiego(ORECCHIE);
            assertNotNull(ripiego);
            assertEquals(TipoLocazione.BOSCO, Foresta.getLocazione(ripiego));
            assertTrue(Foresta.isLocazioneConosciuta(ripiego));
            java.util.List<com.threeamigos.foresta.personaggi.Personaggio> goblin = RegistroMissioni.getIncontroMissione(ripiego)
                    .orElseThrow(AssertionError::new);
            assertEquals(mancanti, goblin.size());
            goblin.forEach(g -> assertEquals(TipoPersonaggio.GOBLIN, g.getClasse()));
            assertEquals(mancanti, caccia.getOggettoInLocazione(ripiego, TipoLocazione.BOSCO, true)
                    .orElseThrow(AssertionError::new).getQuantita());
        }
    }

    @Test
    void lEsplorazioneContaSoloLeCaselleNuoveEOgnunaUnaVolta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(182)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            MissioneDiUnPasso esplorazione = attiva(m -> m.esplorazione(2));
            CoordinateMD prima = new CoordinateMD(0, 0);
            CoordinateMD visitata = new CoordinateMD(1, 0);
            CoordinateMD seconda = new CoordinateMD(2, 0);
            Foresta.setLocazioneVisitata(prima, false);
            Foresta.setLocazioneVisitata(visitata, true);
            Foresta.setLocazioneVisitata(seconda, false);

            partita.gruppo().setCoordinate(prima);
            esplorazione.controllaPreLocazione();
            esplorazione.controllaPreLocazione();
            assertEquals(1, esplorazione.getCaselleEsplorate(), "la stessa casella conta una volta");

            partita.gruppo().setCoordinate(visitata);
            esplorazione.controllaPreLocazione();
            assertEquals(1, esplorazione.getCaselleEsplorate(), "una casella già visitata non conta");

            partita.gruppo().setCoordinate(seconda);
            esplorazione.controllaPreLocazione();
            assertTrue(esplorazione.isCompleta());
        }
    }
}
