package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.richieste.ViandanteRichiesto;
import com.threeamigos.foresta.missioni.storie.IlViandante;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il viandante nel bosco: dopo due o tre giorni, entrando in un bosco, chiede di andare in una città, sconfiggere dei mostri e
 * tornare da lui (vedi IlViandante e ViandanteRichiesto).
 */
class ScenarioIlViandanteTest {

    @Test
    void leRigheDellaGrammaticaSonoValide() {
        for (int i = 0; i < 300; i++) {
            ViandanteRichiesto richiesta = ViandanteRichiesto.da(ProduttoreDiMissioniCasuale.rigaDiMissioni("VIANDANTE"));
            assertTrue(richiesta.getRichiesta("Ruuna").contains("Ruuna"), richiesta.getChiave());
            assertTrue(richiesta.getArrivo("Ruuna").contains("Ruuna"), richiesta.getChiave());
            assertFalse(richiesta.getRichiesta("Ruuna").contains(ViandanteRichiesto.CITTA));
            assertFalse(richiesta.getArrivo("Ruuna").contains(ViandanteRichiesto.CITTA));
        }
    }

    @Test
    void dopoDueOTreGiorniInUnBoscoIlViandanteChiedeDiAndareSconfiggereETornare() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(81)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.rimandaLeMissioniCheSiOffrono();
            IlViandante viandante = trova(IlViandante.class);
            assertFalse(viandante.isAttiva());

            // All'inizio si fissano giorno, richiesta, quantità e città
            viandante.controllaPreLocazione();
            int giorno = viandante.getGiornoDellIncontro();
            assertTrue(giorno >= IlViandante.GIORNO_MINIMO && giorno <= IlViandante.GIORNO_MASSIMO, "giorno " + giorno);
            ViandanteRichiesto richiesta = viandante.getRichiesta();
            assertNotNull(richiesta);
            assertTrue(viandante.getQuantita() >= richiesta.getQuantitaMinima() && viandante.getQuantita() <= richiesta.getQuantitaMassima());
            TipoLocazione citta = viandante.getCitta();
            assertEquals(CategoriaLocazione.CITTA, citta.getCategoriaLocazione());
            assertEquals("INCONTRO", viandante.getPassoCorrente());

            // Né in una città né in una radura, nemmeno dal giorno giusto
            CoordinateMD bosco = cellaSenzaLocazioniUniche(0);
            Foresta.costruisciLocazione(bosco, TipoLocazione.BOSCO);
            LineaTemporale.aggiungiOre(24 * (giorno - 2));
            Foresta.setPosizioneDelGruppo(bosco);
            viandante.controllaPreLocazione();
            assertEquals("INCONTRO", viandante.getPassoCorrente(), "il giorno prima non c'è ancora");
            LineaTemporale.aggiungiOre(24);
            assertEquals(giorno, LineaTemporale.getGiorno());
            CoordinateMD radura = cellaSenzaLocazioniUniche(1);
            Foresta.costruisciLocazione(radura, TipoLocazione.RADURA);
            Foresta.setPosizioneDelGruppo(radura);
            viandante.controllaPreLocazione();
            assertEquals("INCONTRO", viandante.getPassoCorrente(), "non in una radura");
            Foresta.setPosizioneDelGruppo(coordinateDi(citta));
            viandante.controllaPreLocazione();
            assertEquals("INCONTRO", viandante.getPassoCorrente(), "non in una città");

            // Nel bosco, dal giorno fissato, il viandante si fa incontrare
            Foresta.setPosizioneDelGruppo(bosco);
            viandante.controllaPreLocazione();
            assertEquals("ACCETTAZIONE", viandante.getPassoCorrente());
            assertEquals("INCONTRO", viandante.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            viandante.segnaIntermezzoPassoMostrato("INCONTRO");
            viandante.controllaInLocazione();
            assertTrue(viandante.isAttiva());
            assertEquals("ANDATA", viandante.getPassoCorrente());
            assertEquals(bosco, viandante.getPuntoDiPartenza());
            assertTrue(viandante.getDescrizione().contains(citta.getNomeDellaCitta()), viandante.getDescrizione());

            // Un mostro sconfitto prima di arrivare non conta
            partita.pubblica(new InternoAvversarioSconfitto(richiesta.getNemico()));
            viandante.controllaPostLocazione();
            assertEquals("ANDATA", viandante.getPassoCorrente(), "prima si va in città");
            Foresta.setPosizioneDelGruppo(coordinateDi(citta));
            viandante.controllaPreLocazione();
            assertEquals("CACCIA", viandante.getPassoCorrente());

            // Sconfitti tutti i mostri chiesti (dovunque), si torna al punto di partenza
            for (int i = 0; i < viandante.getQuantita(); i++) {
                assertEquals("CACCIA", viandante.getPassoCorrente());
                partita.pubblica(new InternoAvversarioSconfitto(richiesta.getNemico()));
                viandante.controllaPostLocazione();
            }
            assertEquals("RITORNO", viandante.getPassoCorrente());
            viandante.controllaPreLocazione();
            assertEquals("RITORNO", viandante.getPassoCorrente(), "il viandante è nel bosco");

            int monete = Cassa.getMonete();
            Foresta.setPosizioneDelGruppo(bosco);
            viandante.controllaPreLocazione();
            assertEquals("RITORNO", viandante.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            viandante.segnaIntermezzoPassoMostrato("RITORNO");
            viandante.controllaInLocazione();
            assertTrue(viandante.isCompleta());
            assertEquals(monete + viandante.getMonete(), Cassa.getMonete());
            assertEquals(richiesta.getMonete() * viandante.getQuantita() * Statistiche.getLivello(), viandante.getMonete());
        }
    }

    @Test
    void seLaCittaVieneDistruttaLaMissioneFallisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(82)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.rimandaLeMissioniCheSiOffrono();
            IlViandante viandante = trova(IlViandante.class);
            viandante.controllaPreLocazione();
            CoordinateMD bosco = cellaSenzaLocazioniUniche(0);
            Foresta.costruisciLocazione(bosco, TipoLocazione.BOSCO);
            LineaTemporale.aggiungiOre(24 * (viandante.getGiornoDellIncontro() - 1));
            Foresta.setPosizioneDelGruppo(bosco);
            viandante.controllaPreLocazione();
            viandante.segnaIntermezzoPassoMostrato("INCONTRO");
            viandante.controllaInLocazione();
            assertEquals("ANDATA", viandante.getPassoCorrente());

            ModelloDati.getIstanza().getForestaMD().addCittaDistrutta(viandante.getCitta());
            viandante.controllaPreLocazione();
            assertTrue(viandante.isFallita());
        }
    }

    /**
     * Una casella della mappa che non sia una città né un castello (le locazioni uniche, che il sorteggio mette dove vuole), così
     * il test può farne un bosco o una radura senza cancellare la destinazione della missione. Le prime {@code saltate} caselle
     * così si lasciano ad altre prove.
     */
    private static CoordinateMD cellaSenzaLocazioniUniche(int saltate) {
        int daSaltare = saltate;
        for (int y = 0; y < Foresta.getDimensioneY(); y++) {
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                CoordinateMD coordinate = new CoordinateMD(x, y);
                TipoLocazione tipo = Foresta.getLocazione(coordinate);
                if (tipo.getCategoriaLocazione() != CategoriaLocazione.CITTA && tipo.getCategoriaLocazione() != CategoriaLocazione.CASTELLO
                        && daSaltare-- == 0) {
                    return coordinate;
                }
            }
        }
        throw new AssertionError("nessuna casella libera");
    }

    private static CoordinateMD coordinateDi(TipoLocazione citta) {
        return Foresta.getCoordinateLocazioneUnica(citta);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Missione> T trova(Class<T> tipo) {
        return (T) RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }
}
