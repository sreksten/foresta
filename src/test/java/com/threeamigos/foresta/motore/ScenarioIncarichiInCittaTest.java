package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.CacciaAiGoblin;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.IncaricoInCitta;
import com.threeamigos.foresta.missioni.LAlchimistaELaMandragola;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.OggettiDaRaccogliere;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.RecuperaIlMedaglione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli incarichi presi in città (IncaricoInCitta): la caccia ai goblin e le radici di mandragola dell'alchimista,
 * che la missione semina nelle locazioni come oggetti di missione (OggettoMissione).
 */
class ScenarioIncarichiInCittaTest {

    @Test
    void gliIncarichiNonSiSovrappongonoAlleAltreMissioniDellaCitta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(64)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_FLEENA));
            CacciaAiGoblin caccia = trova(CacciaAiGoblin.class);
            LAlchimistaELaMandragola mandragola = trova(LAlchimistaELaMandragola.class);
            MissioneAPassi medaglione = trova(RecuperaIlMedaglione.class);

            // Alla prima visita parte il medaglione, e gli incarichi aspettano
            assertTrue(medaglione.isAttiva());
            assertEquals("INCARICO", caccia.getPassoCorrente());
            assertEquals("INCARICO", mandragola.getPassoCorrente());

            // Tornati col medaglione: c'è il ringraziamento, e gli incarichi aspettano ancora
            medaglione.aggiungiProprieta("PASSO_CORRENTE", "RITORNO");
            caccia.controllaPreLocazione();
            mandragola.controllaPreLocazione();
            assertEquals("INCARICO", caccia.getPassoCorrente());
            assertEquals("INCARICO", mandragola.getPassoCorrente());

            // A medaglione ancora da trovare la visita è tranquilla: parte il primo incarico controllato, l'altro aspetta
            medaglione.aggiungiProprieta("PASSO_CORRENTE", "RECUPERO");
            caccia.controllaPreLocazione();
            mandragola.controllaPreLocazione();
            assertEquals("ACCETTAZIONE", caccia.getPassoCorrente());
            assertEquals("INCARICO", mandragola.getPassoCorrente());
            caccia.controllaInLocazione();
            assertTrue(caccia.isAttiva());
            assertFalse(mandragola.isAttiva());
        }
    }

    @Test
    void laCacciaAiGoblinSiPrendeInCittaESiRiscuoteLi() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(61)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_FLEENA));
            CacciaAiGoblin caccia = prendiIncaricoAllaSecondaVisita(CacciaAiGoblin.class);
            assertEquals(ClassiLocazione.CITTA_FLEENA, caccia.getCitta());
            assertTrue(caccia.getDescrizione().contains("Fleena"), caccia.getDescrizione());

            for (int i = 0; i < CacciaAiGoblin.GOBLIN_DA_SCONFIGGERE; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GOBLIN));
            }
            caccia.controllaPostLocazione();
            assertEquals("RITORNO", caccia.getPassoCorrente());

            int monete = partita.gruppo().getMonete();
            caccia.controllaPreLocazione();
            assertEquals("RITORNO", caccia.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            caccia.controllaInLocazione();
            assertEquals(monete + 15, partita.gruppo().getMonete());
            assertTrue(caccia.isCompleta());
        }
    }

    @Test
    void leRadiciDiMandragolaCresconoSoloNelleRaduraENeiBoschiMaiVisitati() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(62)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            LAlchimistaELaMandragola mandragola = prendiIncaricoAllaSecondaVisita(LAlchimistaELaMandragola.class);
            assertEquals("RACCOLTA", mandragola.getPassoCorrente());
            CoordinateMD casella = new CoordinateMD(0, 0);

            assertEquals(Optional.empty(), mandragola.getOggettoInLocazione(casella, ClassiLocazione.PALUDE, false));
            for (int i = 0; i < 50; i++) {
                assertEquals(Optional.empty(), mandragola.getOggettoInLocazione(casella, ClassiLocazione.BOSCO, true),
                        "non in una locazione già visitata");
            }
            OggettoMissione radici = null;
            for (int i = 0; i < 200 && radici == null; i++) {
                radici = (OggettoMissione) mandragola.getOggettoInLocazione(casella, ClassiLocazione.RADURA, false).orElse(null);
            }
            assertNotNull(radici, "prima o poi una radura le ha");
            assertEquals(ClasseMissione.L_ALCHIMISTA_E_LA_MANDRAGOLA.name(), radici.getIdMissione());
            assertEquals("radice di mandragola", radici.getNomeSingolare());
            assertEquals("una ", radici.getAIS());
            assertTrue(radici.getQuantita() >= 1 && radici.getQuantita() <= 2);

            // Raccolte tutte (anche in due volte), non ne crescono più e si torna dall'alchimista
            new OggettoMissione(radici.getIdMissione(), LAlchimistaELaMandragola.MANDRAGOLA, LAlchimistaELaMandragola.RADICI_DI_MANDRAGOLA.getNome(), 3)
                    .prendi(partita.gruppo(), null);
            assertEquals(3, mandragola.getContatore(LAlchimistaELaMandragola.MANDRAGOLA));
            for (int i = 0; i < 200; i++) {
                mandragola.getOggettoInLocazione(casella, ClassiLocazione.BOSCO, false)
                        .ifPresent(o -> assertEquals(1, o.getQuantita(), "mai più di quante ne mancano"));
            }
            mandragola.controllaPostLocazione();
            assertEquals("RACCOLTA", mandragola.getPassoCorrente());
            radici.prendi(partita.gruppo(), null);
            mandragola.controllaPostLocazione();
            assertEquals("RITORNO", mandragola.getPassoCorrente());
            for (int i = 0; i < 50; i++) {
                assertEquals(Optional.empty(), mandragola.getOggettoInLocazione(casella, ClassiLocazione.RADURA, false));
            }

            int monete = partita.gruppo().getMonete();
            mandragola.controllaPreLocazione();
            mandragola.controllaInLocazione();
            assertEquals(monete + 25, partita.gruppo().getMonete());
            assertTrue(mandragola.isCompleta());
        }
    }

    /**
     * Funghi in ogni bosco, sempre: per vedere l'aggancio dell'automa senza dipendere dal dado.
     */
    static class FunghiInOgniBosco extends MissioneAPassi {

        static final OggettiDaRaccogliere FUNGHI = OggettiDaRaccogliere
                .di("FUNGHI", NomeOggetto.maschile("fungo porcino", "funghi porcini"), 2)
                .in(ClassiLocazione.BOSCO)
                .alPiuPerLocazione(2);

        FunghiInOgniBosco() {
            super(ClasseMissione.MISSIONE_DI_PROVA);
        }

        @Override
        protected String passoIniziale() {
            return "RACCOLTA";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            return raccogli(MomentoControllo.POST_LOCAZIONE, FUNGHI).poi(Passo.FINE);
        }
    }

    @Test
    void entrandoInUnBoscoLOggettoDellaMissionePrendeIlPostoDiQuelloDellaLocazione() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(63)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            FunghiInOgniBosco funghi = new FunghiInOgniBosco();
            RegistroMissioni.getMissionePrincipale().aggiungiMissione(funghi);
            funghi.attivaMissione();
            partita.comando(Comando.ESCI_DA_CITTA);

            // Il gruppo va a sud di un bosco mai visitato ed entra andando a nord
            CoordinateMD bosco = unBoscoMaiVisitatoConUnaCasellaASud();
            partita.gruppo().setCoordinate(new CoordinateMD(bosco.getX(), bosco.getY() + 1));
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            partita.comando(Comando.NORD).comando(Comando.NUMERO_1);
            assertEquals(bosco, partita.gruppo().getCoordinate());

            Oggetto oggetto = partita.gruppo().getLocazioneCorrente().getOggetto();
            assertTrue(oggetto instanceof OggettoMissione, String.valueOf(oggetto));
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("fungo porcino") || t.contains("funghi porcini")),
                    String.valueOf(partita.testi()));
        }
    }

    private static CoordinateMD unBoscoMaiVisitatoConUnaCasellaASud() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY() - 1; y++) {
                CoordinateMD coordinate = new CoordinateMD(x, y);
                CoordinateMD sud = new CoordinateMD(x, y + 1);
                if (Foresta.getLocazione(x, y) == ClassiLocazione.BOSCO && !Foresta.isLocazioneVisitata(coordinate)
                        && Foresta.getLocazione(x, y + 1).getTipoLocazione() == ClassiLocazione.TipoLocazione.STANDARD
                        && RegistroArtefatti.getArtefattoInLocazione(coordinate) == null
                        && RegistroMissioni.getMissioneCheHaOccupato(sud).isEmpty()) {
                    return coordinate;
                }
            }
        }
        throw new AssertionError("nessun bosco adatto");
    }

    /**
     * Come se il gruppo tornasse nella città in cui si trova: l'intermezzo della prima visita è già stato mostrato e
     * l'incarico si può prendere.
     */
    private static <T extends IncaricoInCitta> T prendiIncaricoAllaSecondaVisita(Class<T> tipo) {
        T incarico = trova(tipo);
        incarico.controllaPreLocazione();
        assertEquals("ACCETTAZIONE", incarico.getPassoCorrente(), tipo.getSimpleName());
        // L'intermezzo del mandante lo mostrerebbe l'automa
        incarico.segnaIntermezzoPassoMostrato("INCARICO");
        incarico.controllaInLocazione();
        assertTrue(incarico.isAttiva());
        return incarico;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Missione> T trova(Class<T> tipo) {
        return (T) RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }
}
