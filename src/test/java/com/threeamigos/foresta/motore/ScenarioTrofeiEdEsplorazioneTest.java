package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.OggettiDaRaccogliere;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
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
            .di("ORECCHIE", NomeOggetto.femminile("orecchia di goblin", "orecchie di goblin"), 3)
            .daiNemici(ClassePersonaggio.GOBLIN)
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

    private static void avversari(ClassePersonaggio classe, int quanti) {
        GruppoAvversario avversari = GruppoAvversario.getIstanza();
        avversari.rimuoviPersonaggi();
        for (int i = 0; i < quanti; i++) {
            avversari.aggiungiPersonaggio(classe.getIstanza(1));
        }
    }

    @Test
    void iTrofeiCompaionoSoloDoveCiSonoIMostriGiusti() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(181)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            MissioneDiUnPasso caccia = attiva(MissioneDiUnPasso::trofei);
            CoordinateMD casella = new CoordinateMD(0, 0);

            avversari(ClassePersonaggio.TROLL, 2);
            assertEquals(Optional.empty(), caccia.getOggettoInLocazione(casella, ClassiLocazione.BOSCO, false), "niente goblin, niente orecchie");

            // Due goblin: al più due orecchie, anche in una casella già visitata e in qualunque locazione
            avversari(ClassePersonaggio.GOBLIN, 2);
            Oggetto orecchie = caccia.getOggettoInLocazione(casella, ClassiLocazione.ROVINE, true).orElseThrow(AssertionError::new);
            assertTrue(orecchie.getQuantita() >= 1 && orecchie.getQuantita() <= 2, String.valueOf(orecchie.getQuantita()));
            assertEquals("orecchia di goblin", orecchie.getNomeSingolare());

            // Mai più di quante ne mancano
            orecchie.prendi(partita.gruppo(), null);
            avversari(ClassePersonaggio.GOBLIN, 4);
            int mancanti = 3 - caccia.getContatore("ORECCHIE");
            for (int i = 0; i < 20; i++) {
                caccia.getOggettoInLocazione(casella, ClassiLocazione.BOSCO, false)
                        .ifPresent(o -> assertTrue(o.getQuantita() <= mancanti));
            }

            // Dopo tre giorni il ripiego: un bosco segnato sulla mappa, con i goblin che portano le orecchie mancanti
            LineaTemporale.aggiungiOre(OggettiDaRaccogliere.ORE_AL_RIPIEGO);
            caccia.controllaPreLocazione();
            CoordinateMD ripiego = caccia.getRipiego(ORECCHIE);
            assertNotNull(ripiego);
            assertEquals(ClassiLocazione.BOSCO, Foresta.getLocazione(ripiego));
            assertTrue(Foresta.isLocazioneConosciuta(ripiego));
            java.util.List<com.threeamigos.foresta.personaggi.Personaggio> goblin = RegistroMissioni.getIncontroMissione(ripiego)
                    .orElseThrow(AssertionError::new);
            assertEquals(mancanti, goblin.size());
            goblin.forEach(g -> assertEquals(ClassePersonaggio.GOBLIN, g.getClasse()));
            assertEquals(mancanti, caccia.getOggettoInLocazione(ripiego, ClassiLocazione.BOSCO, true)
                    .orElseThrow(AssertionError::new).getQuantita());
        }
    }

    @Test
    void lEsplorazioneContaSoloLeCaselleNuoveEOgnunaUnaVolta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(182)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
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
