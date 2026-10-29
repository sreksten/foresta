package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.tipi.CostantiMissioni;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.CostantiArtefatti;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.LaForgia;
import com.threeamigos.foresta.missioni.richieste.ForgiaRichiesta;
import com.threeamigos.foresta.missioni.richieste.MaterialeRichiesto;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.oggetti.RaccoltaBottino;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La forgia: l'armaiolo forgia un'arma molto migliore delle altre per chi gli porta un metallo, un trofeo e le monete per il lavoro
 * (vedi LaForgia e ForgiaRichiesta).
 */
class ScenarioLaForgiaTest {

    @Test
    void leRigheDellaGrammaticaSonoValideEDannoUnLeggendarioMigliore() {
        for (int i = 0; i < 300; i++) {
            ForgiaRichiesta arma = ForgiaRichiesta.da(ProduttoreDiMissioniCasuale.rigaDiMissioni("FORGIA"));
            assertFalse(MaterialeRichiesto.da(ProduttoreDiMissioniCasuale.materialeRichiesto("FORGIA_METALLO")).isTrofeo());
            assertTrue(MaterialeRichiesto.da(ProduttoreDiMissioniCasuale.materialeRichiesto("FORGIA_TROFEO")).isTrofeo());
            for (int livello = 1; livello <= 12; livello++) {
                Artefatto forgiata = arma.leggendario(livello).costruisci(Statistiche.getLivello());
                assertEquals(TipoRaritaArtefatto.LEGGENDARIO, forgiata.getRarita(), arma.getChiave());
                assertEquals(arma.getTipo(), forgiata.getTipo(), arma.getChiave());
                assertTrue(forgiata.getLivello() >= livello);
                assertTrue(forgiata.getDanni() > danniDiUnArmaComune(arma, livello),
                        arma.getChiave() + " a livello " + livello + " non fa più danno di un'arma comune");
                assertFalse(forgiata.getModelloDati().getIncantamenti().isEmpty(), "ha la sua tempra");
            }
        }
    }

    @Test
    void daiMaterialiAllArmaForgiataInTuttiIPassi() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(72)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LaForgia forgia = trova(LaForgia.class);
            assertFalse(forgia.isAttiva(), "alla prima visita non è ancora partita");
            assertEquals("L'armaiolo e la forgia", forgia.getNome());

            // La seconda visita: l'armaiolo si presenta, e l'incarico parte dopo il suo intermezzo
            forgia.controllaPreLocazione();
            assertEquals("ACCETTAZIONE", forgia.getPassoCorrente());
            forgia.segnaIntermezzoPassoMostrato("INCARICO");
            forgia.controllaInLocazione();
            assertTrue(forgia.isAttiva());
            assertEquals("RACCOLTA_METALLO", forgia.getPassoCorrente());
            ForgiaRichiesta arma = forgia.getArma();
            assertTrue(forgia.getNome().contains(arma.getNomeBreve()), forgia.getNome());
            assertTrue(forgia.getDescrizione().contains(arma.getNomeBreve()), forgia.getDescrizione());
            assertFalse(RaccoltaBottino.candidati(partita.gruppo(), arma.leggendario(1).costruisci(Statistiche.getLivello())).isEmpty(),
                    "l'arma scelta la può impugnare qualcuno del gruppo");
            int costo = forgia.getCosto();
            assertEquals(CostantiMissioni.FORGIA_MONETE_PER_LIVELLO * Statistiche.getLivello(), costo);

            // Il metallo, poi il trofeo
            raccogli(forgia, partita, LaForgia.METALLO, forgia.getMetallo(), forgia.getQuantitaDelMetallo());
            forgia.controllaPostLocazione();
            assertEquals("RACCOLTA_TROFEO", forgia.getPassoCorrente());
            raccogli(forgia, partita, LaForgia.TROFEO, forgia.getTrofeo(), forgia.getQuantitaDelTrofeo());
            forgia.controllaPostLocazione();
            assertEquals("FORGIATURA", forgia.getPassoCorrente());

            // Senza le monete per il lavoro la forgia non si accende
            Cassa.addMonete(-Cassa.getMonete());
            int giornoEOra = LineaTemporale.getGiorno() * 24 + LineaTemporale.getOra();
            forgia.controllaInLocazione();
            assertEquals("FORGIATURA", forgia.getPassoCorrente());
            assertEquals(giornoEOra, LineaTemporale.getGiorno() * 24 + LineaTemporale.getOra(), "non è passato tempo");

            // Con le monete si forgia: i materiali e le monete si consumano, passano le ore di lavoro
            Cassa.addMonete(costo + 7);
            forgia.controllaInLocazione();
            assertEquals(7, Cassa.getMonete(), "il lavoro è costato quanto promesso");
            assertEquals(0, forgia.getContatore(LaForgia.METALLO));
            assertEquals(0, forgia.getContatore(LaForgia.TROFEO));
            assertEquals(giornoEOra + CostantiMissioni.FORGIA_ORE_DI_LAVORO, LineaTemporale.getGiorno() * 24 + LineaTemporale.getOra());
            // Le ultime parole dell'armaiolo (un passo che passa oltre al primo controllo), poi la lama si raffredda
            forgia.controllaInLocazione();
            assertEquals("RAFFREDDAMENTO", forgia.getPassoCorrente());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("Tornate fra mezza giornata")), String.valueOf(partita.testi()));

            // Finché non sono passate le ore, la lama resta nella forgia
            forgia.controllaPostLocazione();
            assertEquals("RAFFREDDAMENTO", forgia.getPassoCorrente());
            LineaTemporale.aggiungiOre(CostantiMissioni.FORGIA_ORE_DI_ATTESA - 1);
            forgia.controllaPostLocazione();
            assertEquals("RAFFREDDAMENTO", forgia.getPassoCorrente());
            LineaTemporale.aggiungiOre(1);
            forgia.controllaPostLocazione();
            assertEquals("RITORNO", forgia.getPassoCorrente());

            // Tornati in città, l'armaiolo consegna l'arma
            int armiPrima = partita.gruppo().getInventario().size();
            forgia.controllaPreLocazione();
            assertEquals("RITORNO", forgia.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            forgia.controllaInLocazione();
            assertTrue(forgia.isCompleta());
            assertEquals(armiPrima + 1, partita.gruppo().getInventario().size());
            Artefatto forgiata = partita.gruppo().getInventario().stream()
                    .filter(a -> a.getNome().equals(arma.getNome())).findFirst().orElseThrow(AssertionError::new);
            assertEquals(TipoRaritaArtefatto.LEGGENDARIO, forgiata.getRarita());
            assertTrue(forgiata.getDanni() > danniDiUnArmaComune(arma, Statistiche.getLivello()));
        }
    }

    /**
     * Il danno medio di un'arma comune dello stesso tipo: lo spadone fa di più, il bastone magico la metà (vedi GeneratoreArtefattiTabelle).
     */
    private static int danniDiUnArmaComune(ForgiaRichiesta arma, int livello) {
        int danni = GeneratoreArtefatti.danniMediArma(livello);
        if (arma.getTipo() == TipoArtefatto.SPADONE) {
            return (int) Math.round(danni * CostantiArtefatti.ARTEFATTO_MOLTIPLICATORE_DUE_MANI);
        }
        return arma.getTipo() == TipoArtefatto.BASTONE_MAGICO ? danni / 2 : danni;
    }

    private static void raccogli(LaForgia forgia, PartitaDiTest partita, String chiave, MaterialeRichiesto materiale, int quantita) {
        RaccoltaBottino.raccogli(new OggettoMissione(forgia.getId(), chiave, materiale.getNome(), quantita), partita.gruppo(), null);
        assertEquals(quantita, forgia.getContatore(chiave));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Missione> T trova(Class<T> tipo) {
        return (T) RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }
}
