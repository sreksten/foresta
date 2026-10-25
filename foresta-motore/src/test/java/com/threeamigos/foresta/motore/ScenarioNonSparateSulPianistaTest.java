package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.CategoriaLocazione;

import com.threeamigos.foresta.tipi.TipoLocazione;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.intermezzi.*;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.IntermezzoDiPasso;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Non sparate sul pianista: alla terza visita a una locanda (di città o del bosco, contate insieme) il locandiere affida al gruppo un bardo ubriaco,
 * da riportare a casa nella città più vicina.
 */
class ScenarioNonSparateSulPianistaTest {

    private static NonSparateSulPianista pianista() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(NonSparateSulPianista.class::isInstance)
                .map(NonSparateSulPianista.class::cast).findFirst().orElseThrow(AssertionError::new);
    }

    private static CoordinateMD unaLocandaNelBosco() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.LOCANDA) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna locanda nel bosco");
    }

    /**
     * Il gruppo entra nella locanda, che ha già visitato tante volte; la missione si controlla come all'ingresso.
     */
    private static void entraNellaLocanda(PartitaDiTest partita, NonSparateSulPianista pianista, CoordinateMD locanda, int visitePrecedenti) {
        partita.gruppo().setCoordinate(locanda);
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locanda.LOCANDA_VISITE, String.valueOf(visitePrecedenti));
        pianista.controllaPreLocazione();
        if ("ACCETTAZIONE".equals(pianista.getPassoCorrente())) {
            pianista.segnaIntermezzoPassoMostrato("INCARICO");
            pianista.controllaInLocazione();
        }
    }

    @Test
    void allaTerzaVisitaIlBardoSiAffidaAlGruppoEArrivaACasa() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(151)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = pianista();
            CoordinateMD locanda = unaLocandaNelBosco();

            entraNellaLocanda(partita, pianista, locanda, 1);
            assertFalse(pianista.isAttiva(), "alla seconda visita il locandiere non chiede niente");

            entraNellaLocanda(partita, pianista, locanda, 2);
            assertTrue(pianista.isAttiva());
            assertTrue(Foresta.isLocazioneConosciuta(Foresta.getCoordinateLocazioneUnica(pianista.getCitta())),
                    "il locandiere ha detto dove abita il bardo");
            assertEquals("VIAGGIO", pianista.getPassoCorrente());
            Personaggio ugolino = pianista.getScortato().orElseThrow(AssertionError::new);
            assertEquals(pianista.getBardo(), ugolino.getNome());
            assertEquals(TipoPersonaggio.BARDO_DELLE_LOCANDE, ugolino.getClasse());
            assertEquals(RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome(), ugolino.getNome(), "è il bardo della locanda, con il suo nome");
            assertFalse(partita.gruppo().isOspiteVulnerabile(ugolino), "è troppo ubriaco perché i mostri se ne curino");

            // La città è la più vicina alla locanda
            TipoLocazione citta = pianista.getCitta();
            assertNotNull(citta);
            CoordinateMD casa = Foresta.getCoordinateLocazioneUnica(citta);
            for (TipoLocazione altra : TipoLocazione.values()) {
                CoordinateMD coordinate = altra.getCategoriaLocazione() == CategoriaLocazione.CITTA
                        ? Foresta.getCoordinateLocazioneUnica(altra) : null;
                if (coordinate != null) {
                    assertTrue(distanza(locanda, casa) <= distanza(locanda, coordinate), altra + " è più vicina di " + citta);
                }
            }

            partita.gruppo().setCoordinate(casa);
            int monete = partita.gruppo().getMonete();
            pianista.controllaPreLocazione();
            assertEquals("VIAGGIO", pianista.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            assertFalse(partita.gruppo().getOspiti().isEmpty(), "il bardo c'è ancora per la scena");
            pianista.segnaIntermezzoPassoMostrato("VIAGGIO");
            pianista.controllaInLocazione();
            assertEquals(monete + 20, partita.gruppo().getMonete());
            assertTrue(pianista.isCompleta());
            String moglie = RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome();
            assertTrue(partita.testi().contains(moglie + ", la moglie di " + pianista.getBardo() + ", paga 20 monete e trascina il marito in casa per un orecchio."),
                    String.valueOf(partita.testi()));
            assertTrue(partita.gruppo().getOspiti().isEmpty(), "il bardo è a casa");
            assertEquals(1, RegistroMissioni.getTutteLeMissioni().stream().filter(NonSparateSulPianista.class::isInstance).count(),
                    "la missione si fa una volta sola: non ne lascia una nuova");
        }
    }

    @Test
    void ilBardoNonSiPuoFerireNePerStradaNeInCombattimento() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(152)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = pianista();
            entraNellaLocanda(partita, pianista, unaLocandaNelBosco(), 2);
            Personaggio ugolino = pianista.getScortato().orElseThrow(AssertionError::new);

            assertFalse(partita.gruppo().isOspiteVulnerabile(ugolino));
            // Gli avversari non lo scelgono mai come bersaglio
            assertTrue(partita.gruppo().getOspitiBersaglio().isEmpty(), "un avversario ha attaccato il bardo ubriaco");
            assertTrue(ugolino.isVivo());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("i mostri non si curano nemmeno di lui")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void conIlBardoOspiteDelGruppoNelleLocandeNuoveIlBardoEntraConIlGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(153)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
            NonSparateSulPianista pianista = pianista();
            CoordinateMD locanda = unaLocandaNelBosco();
            entraNellaLocanda(partita, pianista, locanda, 2);
            assertTrue(pianista.getScortato().isPresent(), "il bardo viaggia con il gruppo");

            // In un'altra locanda, mai vista, il bardo non può essere seduto in un angolo: entra con il gruppo, subito dopo
            // il capo come ogni ospite, e parla solo il locandiere
            CoordinateMD altra = unAltraLocandaNelBosco(locanda);
            partita.gruppo().setCoordinate(altra);
            LocazioneMD sede = Foresta.getLocazioneMD(altra);
            assertNull(sede.ottieniProprieta(Locanda.LOCANDA_VISITATA));
            sede.aggiungiProprieta(Locanda.LOCANDA_DIALOGO, "Benvenuti! Qui si beve bene.");
            IntermezzoLocandaPrimaVisita scena = new IntermezzoLocandaPrimaVisita();
            assertTrue(scena.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
            PaginaIntermezzo pagina = scena.getPagine().get(0);
            assertNull(pagina.getElemento("bardo"), "non è seduto nella locanda");
            List<String> fila = pagina.getElementi().stream().map(ElementoIntermezzo::getId)
                    .filter(id -> id.startsWith("personaggio") || id.startsWith("ospite") || id.equals("bardoOspite"))
                    .collect(Collectors.toList());
            assertEquals(Arrays.asList("personaggio0", "bardoOspite", "personaggio1"), fila, "il bardo entra secondo, dopo il capo");
            List<String> chiParla = pagina.getBattuteProgrammate().stream().map(b -> b.getBattuta().getIdElemento())
                    .distinct().collect(Collectors.toList());
            assertEquals(Collections.singletonList("locandiere"), chiParla);
            assertEquals(Arrays.asList("Benvenuti!", "Qui si beve bene."), pagina.getBattuteProgrammate().stream()
                    .map(b -> b.getBattuta().getTesto()).collect(Collectors.toList()), "solo il benvenuto, niente reazione alla fiaba");

            // Riportato a casa il bardo, la scena torna
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(pianista.getCitta()));
            pianista.controllaPreLocazione();
            pianista.segnaIntermezzoPassoMostrato("VIAGGIO");
            pianista.controllaInLocazione();
            assertTrue(pianista.isCompleta());
            partita.gruppo().setCoordinate(altra);
            PaginaIntermezzo conBardo = new IntermezzoLocandaPrimaVisita().getPagine().get(0);
            assertNotNull(conBardo.getElemento("bardo"));
            assertNull(conBardo.getElemento("bardoOspite"));
            assertTrue(conBardo.getBattuteProgrammate().stream().anyMatch(b -> "bardo".equals(b.getBattuta().getIdElemento())),
                    "il bardo racconta la sua fiaba");
        }
    }

    @Test
    void laTerzaVisitaContaTutteLeLocandeEBastaChiSiaNuova() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(154)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = pianista();
            CoordinateMD prima = unaLocandaNelBosco();
            CoordinateMD nuova = unAltraLocandaNelBosco(prima);
            CoordinateMD citta = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_FLEENA);
            Foresta.getLocazioneMD(prima).aggiungiProprieta(Locanda.LOCANDA_VISITE, "1");
            Foresta.getLocazioneMD(citta).aggiungiProprieta(Locanda.LOCANDA_VISITE, "1");

            // In città la missione non parte, per quante visite ci siano
            partita.gruppo().setCoordinate(citta);
            pianista.controllaPreLocazione();
            assertFalse(pianista.isAttiva());
            assertNull(pianista.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));

            // In una locanda del bosco mai vista, è la terza visita in tutto: il locandiere sostituisce il benvenuto
            partita.gruppo().setCoordinate(nuova);
            assertNull(Foresta.getLocazioneMD(nuova).ottieniProprieta(Locanda.LOCANDA_VISITATA));
            assertTrue(new IntermezzoLocandaPrimaVisita().deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
            pianista.controllaPreLocazione();
            assertTrue(pianista.staPerAffidareIlBardo());
            assertFalse(new IntermezzoLocandaPrimaVisita().deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE),
                    "il bardo è il tema dell'incarico: niente fiaba nell'angolo");
        }
    }

    @Test
    void dopoLaScenaDelLocandiereNonScattaAncheLaFiabaDelBardoNellAngolo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(156)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = pianista();
            CoordinateMD nuova = unaLocandaNelBosco();
            partita.gruppo().setCoordinate(nuova);
            Foresta.getLocazioneMD(nuova).aggiungiProprieta(Locanda.LOCANDA_VISITE, "2");
            assertNull(Foresta.getLocazioneMD(nuova).ottieniProprieta(Locanda.LOCANDA_VISITATA));
            pianista.controllaPreLocazione();

            // Come fa Automa agli intermezzi di inizio locazione: prima la scena del locandiere, e poi, se qualcosa scatta ancora, il resto
            RegistroIntermezzi.nuovoMomento();
            Intermezzo scenaDelLocandiere = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE);
            assertInstanceOf(IntermezzoDiPasso.class, scenaDelLocandiere, String.valueOf(scenaDelLocandiere));
            RegistroIntermezzi.segnaScattato(scenaDelLocandiere);

            Intermezzo dopo = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE);
            assertNull(dopo, "il locandiere ha già fatto da benvenuto: il bardo non può anche raccontare la fiaba nell'angolo (" + dopo + ")");
        }
    }

    @Test
    void conIlBardoOspiteAncheInUnaLocandaDiCittaNonRaccontaLaFiaba() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(155)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = pianista();
            entraNellaLocanda(partita, pianista, unaLocandaNelBosco(), 2);
            assertTrue(pianista.getScortato().isPresent());

            CoordinateMD citta = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_FLEENA);
            partita.gruppo().setCoordinate(citta);
            LocazioneMD sede = Foresta.getLocazioneMD(citta);
            sede.aggiungiProprieta(Locanda.LOCANDA_DIALOGO, "Benvenuti! Qui si beve bene.");
            IntermezzoLocandaPrimaVisita scena = new IntermezzoLocandaPrimaVisita();
            assertTrue(scena.deveScattare(MomentoIntermezzo.INGRESSO_LOCANDA_IN_CITTA));
            PaginaIntermezzo pagina = scena.getPagine().get(0);
            assertNull(pagina.getElemento("bardo"));
            assertNotNull(pagina.getElemento("bardoOspite"));
            assertEquals(Collections.singletonList("locandiere"), pagina.getBattuteProgrammate().stream()
                    .map(b -> b.getBattuta().getIdElemento()).distinct().collect(Collectors.toList()));
        }
    }

    private static CoordinateMD unAltraLocandaNelBosco(CoordinateMD diversaDa) {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.LOCANDA && !(x == diversaDa.getX() && y == diversaDa.getY())) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("una sola locanda nel bosco");
    }

    private static int distanza(CoordinateMD a, CoordinateMD b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}
