package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.IlRituale;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.RitualeRichiesto;
import com.threeamigos.foresta.missioni.TipoMissione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il rito: si raccolgono gli ingredienti, poi nel posto segnato sulla mappa si decide se cominciarlo, o come; a volte
 * salta fuori qualcuno da sconfiggere; poi si torna a riscuotere.
 */
class ScenarioRitualeTest {

    private static final String VARCO = "CHIAVE=VARCO_DI_PROVA;TIPO=SIGILLO;ASPETTO=QUALUNQUE;MANDANTE=il sacerdote;LUOGO=ROVINE;"
            + "GENERE=M;INGREDIENTE=frammento d'argento;INGREDIENTI=frammenti d'argento;DOVE=LUOGHI GROTTA;QUANTITA=3;"
            + "DOMANDA=Sigillate il varco?;NEMICO=SPETTRO;NUMERO=2;MONETE=40;TITOLO=Il varco;RICHIESTA=Un varco.;"
            + "BATTUTA=Spettri?;RISPOSTA=Due.;RITO=Escono due spettri.;VITTORIA=Il varco è chiuso.;RINGRAZIAMENTO=Grazie.;"
            + "RICORDO=Qui c'era un varco.";
    private static final String PASTORE = "CHIAVE=PASTORE_DI_PROVA;TIPO=POSSESSIONE;ASPETTO=QUALUNQUE;MANDANTE=la moglie;"
            + "LUOGO=TEMPIO;GENERE=F;INGREDIENTE=radice di valeriana;INGREDIENTI=radici di valeriana;DOVE=LUOGHI RADURA BOSCO;"
            + "QUANTITA=3;DOMANDA=Come lo liberate?;METODI=La formula/Il fumo/Il sale;METODO=2;ERRORE=Lo spirito ride.;"
            + "MONETE=40;TITOLO=Il pastore;RICHIESTA=Mio marito.;BATTUTA=E noi?;RISPOSTA=Col fumo.;RITO=Lo spirito esce.;"
            + "RINGRAZIAMENTO=Grazie.;RICORDO=Qui c'era un posseduto.";

    @Test
    void ogniRitoSiLegge() {
        Set<String> chiavi = new HashSet<>();
        Set<TipoMissione> tipi = EnumSet.noneOf(TipoMissione.class);
        for (int i = 0; i < 600; i++) {
            RitualeRichiesto rituale = RitualeRichiesto.da(ProduttoreDiTestiCasuale.rigaDiMissioni("RITUALE"));
            chiavi.add(rituale.getChiave());
            tipi.add(rituale.getTipo());
        }
        assertTrue(chiavi.size() >= 31, String.valueOf(chiavi));
        assertEquals(EnumSet.of(TipoMissione.RITUALE, TipoMissione.SIGILLO, TipoMissione.BENEDIZIONE, TipoMissione.SPEZZATURA,
                TipoMissione.PURIFICAZIONE, TipoMissione.POSSESSIONE, TipoMissione.COMUNICAZIONE, TipoMissione.EVOCAZIONE,
                TipoMissione.BARRIERA_MAGICA, TipoMissione.SANTUARIO, TipoMissione.DIVINAZIONE, TipoMissione.ASTRI,
                TipoMissione.VISIONE_PASSATO, TipoMissione.TRASMUTAZIONE, TipoMissione.CREAZIONE_GOLEM, TipoMissione.PATTO_ANIMA,
                TipoMissione.ANTI_MAGIA, TipoMissione.MALEDIZIONE, TipoMissione.NECROMANZIA, TipoMissione.CONTROLLO_ELEMENTALE,
                TipoMissione.ANIMAZIONE_OGGETTI, TipoMissione.ILLUSIONE, TipoMissione.LEGAME_SPIRITUALE, TipoMissione.CHANNELING,
                TipoMissione.COMUNIONE, TipoMissione.TRANCE, TipoMissione.INCANTESIMO, TipoMissione.VISIONE_FUTURO,
                TipoMissione.PERDONO, TipoMissione.RISCATTO, TipoMissione.REDENZIONE_PUBBLICA), tipi);

        RitualeRichiesto pastore = RitualeRichiesto.da(PASTORE);
        assertEquals(Arrays.asList("La formula", "Il fumo", "Il sale"), pastore.getMetodi());
        assertFalse(pastore.isConNemici());
        assertEquals("le radici di valeriana", pastore.getIngrediente().getPluraleConArticolo());
        assertThrows(IllegalArgumentException.class, () -> RitualeRichiesto.da(PASTORE.replace("ERRORE=Lo spirito ride.;", "")));
        assertThrows(IllegalArgumentException.class, () -> RitualeRichiesto.da(VARCO + ";METODO=1"));
        assertThrows(IllegalArgumentException.class, () -> RitualeRichiesto.da(VARCO.replace("VITTORIA=Il varco è chiuso.;", "")));
        assertThrows(IllegalArgumentException.class, () -> RitualeRichiesto.da(VARCO.replace("LUOGO=ROVINE", "LUOGO=LOCANDA")));
    }

    @Test
    void ilVarcoSiSigillaDopoUnRinvioESconfittiGliSpettri() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(211)) {
            IlRituale rituale = prendiLIncarico(partita, VARCO);
            CoordinateMD rovine = rituale.getPosto();
            assertEquals(ClassiLocazione.ROVINE, Foresta.getLocazione(rovine));
            assertTrue(Foresta.isLocazioneConosciuta(rovine));
            assertTrue(rituale.getDescrizione().endsWith("Finora: 0."), rituale.getDescrizione());

            // Senza ingredienti, nel posto non si chiede niente
            partita.gruppo().setCoordinate(rovine);
            rituale.controllaPreLocazione();
            assertNull(rituale.getDomandaDaPorre(Passo.MomentoControllo.PRE_LOCAZIONE));

            raccogliGliIngredienti(partita, rituale);
            rituale.controllaPreLocazione();
            Passo domanda = rituale.getDomandaDaPorre(Passo.MomentoControllo.PRE_LOCAZIONE);
            assertNotNull(domanda);
            assertTrue(domanda.isConferma());

            // Un no rimanda il rito alla prossima volta che si torna qui
            rituale.rispondi(Passo.NO);
            rituale.controllaPreLocazione();
            assertEquals("RINVIO", rituale.getPassoCorrente());
            assertFalse(RegistroMissioni.getIncontroMissione(rovine).isPresent());
            partita.gruppo().setCoordinate(new CoordinateMD(rovine.getX() == 0 ? 1 : rovine.getX() - 1, rovine.getY()));
            rituale.controllaPreLocazione();
            assertEquals(IlRituale.RITO, rituale.getPassoCorrente());
            assertNull(rituale.getDomandaDaPorre(Passo.MomentoControllo.PRE_LOCAZIONE), "lontano dal posto non si chiede");

            partita.gruppo().setCoordinate(rovine);
            rituale.controllaPreLocazione();
            assertNotNull(rituale.getDomandaDaPorre(Passo.MomentoControllo.PRE_LOCAZIONE));
            rituale.rispondi(Passo.SI);
            rituale.controllaPreLocazione();
            assertEquals("GUARDIANO", rituale.getPassoCorrente());
            assertTrue(partita.testi().contains("Escono due spettri."), String.valueOf(partita.testi()));
            assertEquals(2, RegistroMissioni.getIncontroMissione(rovine).orElseThrow(AssertionError::new).size());

            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.SPETTRO));
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.SPETTRO));
            rituale.controllaPostLocazione();
            assertEquals("RITORNO", rituale.getPassoCorrente());
            assertTrue(partita.testi().contains("Il varco è chiuso. Il sacerdote aspetta a Nyena."), String.valueOf(partita.testi()));

            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA));
            int monete = partita.gruppo().getMonete();
            rituale.controllaPreLocazione();
            rituale.segnaIntermezzoPassoMostrato("RITORNO");
            rituale.controllaInLocazione();
            assertEquals(monete + 40, partita.gruppo().getMonete());
            assertTrue(rituale.isCompleta());
            assertTrue(RegistroMissioni.getTutteLeMissioni().stream().anyMatch(m -> m instanceof IlRituale && m != rituale));
        }
    }

    @Test
    void conIlMetodoGiustoIlPastoreELiberoSenzaCombattere() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(212)) {
            IlRituale rituale = prendiLIncarico(partita, PASTORE);
            assertEquals(ClassiLocazione.TEMPIO, Foresta.getLocazione(rituale.getPosto()));
            raccogliGliIngredienti(partita, rituale);
            partita.gruppo().setCoordinate(rituale.getPosto());
            rituale.controllaPreLocazione();
            Passo domanda = rituale.getDomandaDaPorre(Passo.MomentoControllo.PRE_LOCAZIONE);
            assertEquals(Arrays.asList("La formula", "Il fumo", "Il sale", "Non ancora"), domanda.getOpzioni());

            // "Non ancora" rimanda, come un no
            rituale.rispondi("4");
            rituale.controllaPreLocazione();
            assertEquals("RINVIO", rituale.getPassoCorrente());
            assertFalse(rituale.isFallita());
        }
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(213)) {
            IlRituale rituale = prendiLIncarico(partita, PASTORE);
            raccogliGliIngredienti(partita, rituale);
            partita.gruppo().setCoordinate(rituale.getPosto());
            rituale.controllaPreLocazione();
            rituale.rispondi("2");
            rituale.controllaPreLocazione();
            assertEquals("RITORNO", rituale.getPassoCorrente());
            assertTrue(partita.testi().contains("Lo spirito esce. La moglie aspetta a Nyena."), String.valueOf(partita.testi()));
        }
    }

    @Test
    void conIlMetodoSbagliatoLaMissioneFallisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(214)) {
            IlRituale rituale = prendiLIncarico(partita, PASTORE);
            raccogliGliIngredienti(partita, rituale);
            partita.gruppo().setCoordinate(rituale.getPosto());
            rituale.controllaPreLocazione();
            rituale.rispondi("3");
            rituale.controllaPreLocazione();
            assertTrue(rituale.isFallita());
            assertTrue(partita.testi().contains("Lo spirito ride."), String.valueOf(partita.testi()));
            assertNull(rituale.getRicordoDellaLocazione());
        }
    }

    private static IlRituale prendiLIncarico(PartitaDiTest partita, String riga) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
        IlRituale rituale = RegistroMissioni.getTutteLeMissioni().stream().filter(IlRituale.class::isInstance)
                .map(IlRituale.class::cast).findFirst().orElseThrow(AssertionError::new);
        rituale.aggiungiProprieta("PARAMETRO_" + IlRituale.RITUALE, riga);
        rituale.controllaPreLocazione();
        rituale.segnaIntermezzoPassoMostrato("INCARICO");
        rituale.controllaInLocazione();
        assertTrue(rituale.isAttiva());
        return rituale;
    }

    private static void raccogliGliIngredienti(PartitaDiTest partita, IlRituale rituale) {
        new OggettoMissione(rituale.getId(), IlRituale.INGREDIENTE, rituale.getIngredienti().getNome(), 3).prendi(partita.gruppo(), null);
        rituale.controllaPostLocazione();
        assertEquals(IlRituale.RITO, rituale.getPassoCorrente());
    }

    @Test
    void nelGiocoLaDomandaArrivaEntrandoEGliSpettriCiSonoGiaInQuestaVisita() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(215)) {
            IlRituale rituale = prendiLIncarico(partita, VARCO);
            raccogliGliIngredienti(partita, rituale);
            CoordinateMD rovine = rituale.getPosto();

            partita.comando(Comando.ESCI_DA_CITTA);
            partita.gruppo().setCoordinate(new CoordinateMD(rovine.getX(), rovine.getY() + 1));
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            partita.comando(Comando.NORD).comando(Comando.NUMERO_1);
            partita.assertStato(Stato.ATTESA_RISPOSTA_MISSIONE);
            assertEquals(Arrays.asList(Comando.SI, Comando.NO), new java.util.ArrayList<>(partita.comandiDisponibili()));

            partita.comando(Comando.SI);
            assertEquals(rovine, partita.gruppo().getCoordinate());
            assertEquals("GUARDIANO", rituale.getPassoCorrente());
            assertEquals(2, GruppoAvversario.getIstanza().getNumeroPersonaggi());
            assertEquals(ClassePersonaggio.SPETTRO, GruppoAvversario.getIstanza().getCapo().getClasse());
        }
    }
}
