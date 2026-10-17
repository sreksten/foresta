package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.missioni.*;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.incarichi.CacciaAiGoblin;
import com.threeamigos.foresta.missioni.incarichi.IncaricoDiCombattimento;
import com.threeamigos.foresta.missioni.incarichi.IncaricoInCitta;
import com.threeamigos.foresta.missioni.incarichi.RichiestaDiMateriali;
import com.threeamigos.foresta.missioni.leggende.LaLeggendaDellArmaiolo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli incarichi presi in città (IncaricoInCitta): la caccia ai goblin e le radici di mandragola dell'alchimista,
 * che la missione semina nelle locazioni come oggetti di missione (OggettoMissione).
 */
class ScenarioIncarichiInCittaTest {

    @Test
    void inUnaVisitaParteUnSoloIncaricoEGliAltriAspettano() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(64)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            // L'armaiolo racconterebbe la sua leggenda prima di ogni incarico: qui non è ancora il momento
            trova(LaLeggendaDellArmaiolo.class).aggiungiProprieta("DISPONIBILE_DALLE", String.valueOf(Long.MAX_VALUE));
            List<IncaricoInCitta> incarichi = Arrays.asList(trova(CacciaAiGoblin.class), Alchimie.alchimista(),
                    trova(IncaricoDiCombattimento.class));

            // Fra più incarichi pronti nella stessa visita parte il primo controllato, gli altri aspettano
            incarichi.forEach(IncaricoInCitta::controllaPreLocazione);
            long inAttesaDelMandante = incarichi.stream().filter(i -> "ACCETTAZIONE".equals(i.getPassoCorrente())).count();
            long fermi = incarichi.stream().filter(i -> "INCARICO".equals(i.getPassoCorrente())).count();
            assertEquals(1, inAttesaDelMandante, incarichi.toString());
            assertEquals(incarichi.size() - 1, fermi);

            // Detto dal mandante, l'incarico parte e gli altri non sono partiti
            IncaricoInCitta partito = incarichi.stream().filter(i -> "ACCETTAZIONE".equals(i.getPassoCorrente())).findFirst().get();
            partito.segnaIntermezzoPassoMostrato("INCARICO");
            partito.controllaInLocazione();
            assertTrue(partito.isAttiva());
            incarichi.stream().filter(i -> i != partito).forEach(i -> assertFalse(i.isAttiva()));
        }
    }

    @Test
    void rientrandoInCittaConLAutomaVeroLIncaricoParte() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(65)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            CacciaAiGoblin caccia = trova(CacciaAiGoblin.class);
            assertFalse(caccia.isAttiva(), "alla prima visita non è ancora partito");
            CoordinateMD fleena = partita.gruppo().getCoordinate();
            // L'armaiolo racconterebbe la sua leggenda prima di ogni incarico: qui non è ancora il momento
            trova(LaLeggendaDellArmaiolo.class).aggiungiProprieta("DISPONIBILE_DALLE", String.valueOf(Long.MAX_VALUE));

            // Si esce dalla città e ci si rientra da sud, come farebbe il giocatore
            partita.comando(Comando.ESCI_DA_CITTA);
            partita.gruppo().setCoordinate(new CoordinateMD(fleena.getX(), fleena.getY() + 1));
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            partita.comando(Comando.NORD).comando(Comando.NUMERO_1);
            assertEquals(fleena, partita.gruppo().getCoordinate());

            assertTrue(caccia.isAttiva(), "alla seconda visita parte l'incarico");
            assertEquals("CACCIA", caccia.getPassoCorrente());
            assertFalse(Alchimie.alchimista().isAttiva(), "uno per visita");
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("Il mercante pagherà")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void laCacciaAiGoblinSiPrendeInCittaESiRiscuoteLi() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(61)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            CacciaAiGoblin caccia = prendiIncaricoAllaSecondaVisita(CacciaAiGoblin.class);
            assertEquals(TipoLocazione.CITTA_FLEENA, caccia.getCitta());
            assertTrue(caccia.getDescrizione().contains("Fleena"), caccia.getDescrizione());

            for (int i = 0; i < CacciaAiGoblin.GOBLIN_DA_SCONFIGGERE; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
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
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            RichiestaDiMateriali mandragola = prendiIncaricoAllaSecondaVisita(Alchimie.conLaMandragola());
            assertEquals("RACCOLTA", mandragola.getPassoCorrente());
            CoordinateMD casella = new CoordinateMD(0, 0);

            assertEquals(Optional.empty(), mandragola.getOggettoInLocazione(casella, TipoLocazione.PALUDE, false));
            for (int i = 0; i < 50; i++) {
                assertEquals(Optional.empty(), mandragola.getOggettoInLocazione(casella, TipoLocazione.BOSCO, true),
                        "non in una locazione già visitata");
            }
            OggettoMissione radici = null;
            for (int i = 0; i < 200 && radici == null; i++) {
                radici = (OggettoMissione) mandragola.getOggettoInLocazione(casella, TipoLocazione.RADURA, false).orElse(null);
            }
            assertNotNull(radici, "prima o poi una radura le ha");
            assertEquals(MissionePredefinita.RICHIESTA_ALCHIMISTA.name(), radici.getIdMissione());
            assertEquals("radice di mandragola", radici.getNomeSingolare());
            assertEquals("una ", radici.getAIS());
            assertTrue(radici.getQuantita() >= 1 && radici.getQuantita() <= 2);

            // Raccolte tutte (anche in due volte), non ne crescono più e si torna dall'alchimista
            new OggettoMissione(radici.getIdMissione(), RichiestaDiMateriali.MATERIALE, mandragola.getMateriali().getNome(), 3)
                    .prendi(partita.gruppo(), null);
            assertEquals(3, mandragola.getContatore(RichiestaDiMateriali.MATERIALE));
            for (int i = 0; i < 200; i++) {
                mandragola.getOggettoInLocazione(casella, TipoLocazione.BOSCO, false)
                        .ifPresent(o -> assertEquals(1, o.getQuantita(), "mai più di quante ne mancano"));
            }
            mandragola.controllaPostLocazione();
            assertEquals("RACCOLTA", mandragola.getPassoCorrente());
            radici.prendi(partita.gruppo(), null);
            mandragola.controllaPostLocazione();
            assertEquals("RITORNO", mandragola.getPassoCorrente());
            for (int i = 0; i < 50; i++) {
                assertEquals(Optional.empty(), mandragola.getOggettoInLocazione(casella, TipoLocazione.RADURA, false));
            }

            int monete = partita.gruppo().getMonete();
            mandragola.controllaPreLocazione();
            mandragola.controllaInLocazione();
            assertEquals(monete + 25, partita.gruppo().getMonete());
            assertTrue(mandragola.isCompleta());
        }
    }

    @Test
    void dopoTreGiorniSenzaTutteLeRadiciLaMissioneRipiegaSuUnPostoSegnatoSullaMappa() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(66)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            RichiestaDiMateriali mandragola = prendiIncaricoAllaSecondaVisita(Alchimie.conLaMandragola());
            OggettiDaRaccogliere radici = mandragola.getMateriali();
            new OggettoMissione(mandragola.getId(), RichiestaDiMateriali.MATERIALE, radici.getNome(), 1)
                    .prendi(partita.gruppo(), null);

            // Prima dei tre giorni non si ripiega
            LineaTemporale.aggiungiOre(OggettiDaRaccogliere.ORE_AL_RIPIEGO - 1);
            mandragola.controllaPreLocazione();
            assertNull(mandragola.getRipiego(radici));

            LineaTemporale.aggiungiOre(1);
            mandragola.controllaPreLocazione();
            CoordinateMD ripiego = mandragola.getRipiego(radici);
            assertNotNull(ripiego);
            assertTrue(radici.getLocazioni().contains(Foresta.getLocazione(ripiego)));
            assertTrue(Foresta.isLocazioneConosciuta(ripiego));
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("le radici di mandragola che vi mancano")),
                    String.valueOf(partita.testi()));

            // Lì ci sono tutte quelle che mancano, anche se la casella era già stata visitata; altrove, nelle
            // locazioni visitate, niente
            Oggetto lì = mandragola.getOggettoInLocazione(ripiego, Foresta.getLocazione(ripiego), true).orElseThrow(AssertionError::new);
            assertEquals(Alchimie.RADICI - 1, lì.getQuantita());
            assertEquals(Optional.empty(), mandragola.getOggettoInLocazione(
                    new CoordinateMD(ripiego.getX() == 0 ? 1 : 0, ripiego.getY()), TipoLocazione.BOSCO, true));

            // Il ripiego si salva con la missione
            partita.salva(Comando.NUMERO_2);
            assertTrue(partita.leggi(Comando.NUMERO_2));
            RichiestaDiMateriali riletta = Alchimie.alchimista();
            assertEquals(ripiego, riletta.getRipiego(radici));

            // Raccolte, la missione va avanti come sempre
            riletta.getOggettoInLocazione(ripiego, Foresta.getLocazione(ripiego), true).orElseThrow(AssertionError::new)
                    .prendi(partita.gruppo(), null);
            riletta.controllaPostLocazione();
            assertEquals("RITORNO", riletta.getPassoCorrente());
        }
    }

    /**
     * Funghi in ogni bosco, sempre: per vedere l'aggancio dell'automa senza dipendere dal dado.
     */
    static class FunghiInOgniBosco extends MissioneAPassi {

        static final OggettiDaRaccogliere FUNGHI = OggettiDaRaccogliere
                .di("FUNGHI", NomeOggetto.maschile("fungo porcino", "funghi porcini"), 2)
                .in(TipoLocazione.BOSCO)
                .alPiuPerLocazione(2);

        FunghiInOgniBosco() {
            super(MissionePredefinita.MISSIONE_DI_PROVA);
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
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
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

            // I funghi sono ancora lì, se qualcuno li custodisce; se sono incustoditi il gruppo li ha già presi
            Oggetto oggetto = partita.gruppo().getLocazioneCorrente().getOggetto();
            assertTrue(oggetto instanceof OggettoMissione || funghi.getContatore("FUNGHI") > 0, String.valueOf(oggetto));
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("fungo porcino") || t.contains("funghi porcini")),
                    String.valueOf(partita.testi()));
        }
    }

    private static CoordinateMD unBoscoMaiVisitatoConUnaCasellaASud() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY() - 1; y++) {
                CoordinateMD coordinate = new CoordinateMD(x, y);
                CoordinateMD sud = new CoordinateMD(x, y + 1);
                if (Foresta.getLocazione(x, y) == TipoLocazione.BOSCO && !Foresta.isLocazioneVisitata(coordinate)
                        && Foresta.getLocazione(x, y + 1).getCategoria() == CategoriaLocazione.STANDARD
                        && RegistroArtefatti.getArtefattoInLocazione(coordinate) == null
                        && !RegistroMissioni.getMissioneCheHaOccupato(sud).isPresent()) {
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
        return prendiIncaricoAllaSecondaVisita(trova(tipo));
    }

    private static <T extends IncaricoInCitta> T prendiIncaricoAllaSecondaVisita(T incarico) {
        incarico.controllaPreLocazione();
        assertEquals("ACCETTAZIONE", incarico.getPassoCorrente(), incarico.getClass().getSimpleName());
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
