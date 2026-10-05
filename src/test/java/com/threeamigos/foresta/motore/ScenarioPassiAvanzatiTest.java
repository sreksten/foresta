package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.Costruzione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.OggettiDaRaccogliere;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Ricompensa;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli ultimi passi del catalogo (passi_missioni.md, §2): GENERA_PARAMETRI, SORVEGLIA, EVITA_COMBATTIMENTO,
 * COSTRUISCI e le ricompense che non sono solo monete.
 */
class ScenarioPassiAvanzatiTest {

    /**
     * Una missione di un passo solo (più la fine), scelto dal test.
     */
    static class MissioneDiUnPasso extends MissioneAPassi {

        private final java.util.function.Function<MissioneDiUnPasso, Passo> passo;

        MissioneDiUnPasso(java.util.function.Function<MissioneDiUnPasso, Passo> passo) {
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

        Passo generaParametri(Map<String, Supplier<String>> parametri) {
            return generaParametri(MomentoControllo.PRE_LOCAZIONE, parametri);
        }

        Passo sorveglia(CoordinateMD dove, int volte, int ore) {
            return sorveglia(() -> dove, volte, ore);
        }

        Passo evita(CoordinateMD dove) {
            return evitaCombattimento(MomentoControllo.PRE_LOCAZIONE, () -> dove, () -> "Siete stati scoperti!");
        }

        Passo costruisci(Costruzione costruzione) {
            return costruisci(MomentoControllo.IN_LOCAZIONE, () -> true, costruzione, () -> "Il ponte è riparato.");
        }

        Passo ricompensa(Ricompensa ricompensa) {
            return ricompensa(MomentoControllo.IN_LOCAZIONE, ricompensa, () -> "Ecco la vostra ricompensa.");
        }
    }

    private static MissioneDiUnPasso attiva(java.util.function.Function<MissioneDiUnPasso, Passo> passo) {
        MissioneDiUnPasso missione = new MissioneDiUnPasso(passo);
        RegistroMissioni.getMissionePrincipale().aggiungiMissione(missione);
        missione.attivaMissione();
        return missione;
    }

    private static PartitaDiTest partita(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        return partita;
    }

    @Test
    void iParametriSiGeneranoUnaVoltaSola() {
        try (PartitaDiTest partita = partita(101)) {
            AtomicInteger generati = new AtomicInteger();
            Map<String, Supplier<String>> parametri = new LinkedHashMap<>();
            parametri.put("MANDANTE", () -> "il mugnaio " + generati.incrementAndGet());
            parametri.put("QUANTITA", () -> "3");
            MissioneDiUnPasso missione = attiva(m -> m.generaParametri(parametri));
            missione.aggiungiProprieta("PARAMETRO_MANDANTE", "il fornaio");

            missione.controllaPreLocazione();
            assertTrue(missione.isCompleta());
            assertEquals("il fornaio", missione.getParametro("MANDANTE"), "un parametro che c'è già non si rigenera");
            assertEquals("3", missione.getParametro("QUANTITA"));
            assertEquals(0, generati.get());
        }
    }

    @Test
    void laSorveglianzaContaSoloLeVisiteAbbastanzaDistanti() {
        try (PartitaDiTest partita = partita(102)) {
            CoordinateMD faro = partita.gruppo().getCoordinate();
            MissioneDiUnPasso missione = attiva(m -> m.sorveglia(faro, 2, 12));

            missione.controllaPreLocazione();
            assertEquals(1, missione.getVisiteNelPassoCorrente());
            LineaTemporale.aggiungiOre(11);
            missione.controllaPreLocazione();
            assertEquals(1, missione.getVisiteNelPassoCorrente(), "troppo presto per una seconda visita");

            // Altrove non conta
            partita.gruppo().setCoordinate(new CoordinateMD(faro.getX() == 0 ? 1 : faro.getX() - 1, faro.getY()));
            LineaTemporale.aggiungiOre(1);
            missione.controllaPreLocazione();
            assertEquals(1, missione.getVisiteNelPassoCorrente());

            partita.gruppo().setCoordinate(faro);
            missione.controllaPreLocazione();
            assertTrue(missione.isCompleta());
        }
    }

    @Test
    void chiEvitaIlCombattimentoArrivaChiCombatteFallisce() {
        try (PartitaDiTest partita = partita(103)) {
            CoordinateMD qui = partita.gruppo().getCoordinate();
            CoordinateMD altrove = new CoordinateMD(qui.getX() == 0 ? 1 : qui.getX() - 1, qui.getY());

            MissioneDiUnPasso furtiva = attiva(m -> m.evita(qui));
            furtiva.controllaPreLocazione();
            assertTrue(furtiva.isCompleta());

            MissioneDiUnPasso scoperta = attiva(m -> m.evita(altrove));
            scoperta.controllaPreLocazione();
            assertFalse(scoperta.haCombattutoNelPassoCorrente());
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
            assertTrue(scoperta.haCombattutoNelPassoCorrente());
            scoperta.controllaPostLocazione();
            assertTrue(scoperta.isFallita());
            assertTrue(partita.testi().contains("Siete stati scoperti!"), String.valueOf(partita.testi()));
        }
    }

    @Test
    void laCostruzioneConsumaMaterialiMoneteEOre() {
        try (PartitaDiTest partita = partita(104)) {
            OggettiDaRaccogliere assi = OggettiDaRaccogliere.di("ASSI", NomeOggetto.femminile("asse", "assi"), 3)
                    .in(TipoLocazione.BOSCO);
            MissioneDiUnPasso missione = attiva(m -> m.costruisci(Costruzione.con(assi).conMonete(10).inOre(5)));

            missione.controllaInLocazione();
            assertFalse(missione.isCompleta(), "mancano le assi");

            new OggettoMissione(missione.getId(), "ASSI", assi.getNome(), 4).prendi(partita.gruppo(), null);
            partita.gruppo().addMonete(10 - partita.gruppo().getMonete());
            long oraPrima = LineaTemporale.getGiorno() * 24L + LineaTemporale.getOra();
            missione.controllaInLocazione();
            assertTrue(missione.isCompleta());
            assertEquals(1, missione.getContatore("ASSI"), "ne avanza una");
            assertEquals(0, partita.gruppo().getMonete());
            assertEquals(oraPrima + 5, LineaTemporale.getGiorno() * 24L + LineaTemporale.getOra());
            assertTrue(partita.testi().contains("Il ponte è riparato."));
        }
    }

    @Test
    void laRicompensaPuoDareAncheEsperienzaPreziosiEUnArtefatto() {
        try (PartitaDiTest partita = partita(105)) {
            partita.eventi().ascolta(NotificaArtefattoTrovato.class);
            int monete = partita.gruppo().getMonete();
            int preziosi = partita.gruppo().getPreziosi();
            int esperienza = Statistiche.getPuntiEsperienza();
            int inventario = partita.gruppo().getInventario().size();
            MissioneDiUnPasso missione = attiva(m -> m.ricompensa(Ricompensa.inMonete(7).conPreziosi(2).conEsperienza(30)
                    .conArtefatto(() -> GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 2))));

            missione.controllaInLocazione();
            assertTrue(missione.isCompleta());
            assertEquals(monete + 7, partita.gruppo().getMonete());
            assertEquals(preziosi + 2, partita.gruppo().getPreziosi());
            // Più quella che dà ogni missione completata
            assertTrue(Statistiche.getPuntiEsperienza() >= esperienza + 30, String.valueOf(Statistiche.getPuntiEsperienza()));
            assertEquals(inventario + 1, partita.gruppo().getInventario().size());
            assertEquals(1, partita.eventi().tutti(NotificaArtefattoTrovato.class).size(), "l'artefatto si rivela");
        }
    }
}
