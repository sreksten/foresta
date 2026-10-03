package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.BenedizioneRichiesta;
import com.threeamigos.foresta.missioni.FavoreRichiesto;
import com.threeamigos.foresta.missioni.IlFavore;
import com.threeamigos.foresta.missioni.LaBenedizione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.tipi.TipoAttributo;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La benedizione: in una locanda un sacerdote chiede un favore, che è una missione secondaria; fatto il favore, in un
 * tempio un personaggio scelto dal giocatore riceve un modificatore permanente.
 */
class ScenarioBenedizioneTest {

    private static final String LUNA = "CHIAVE=LUNA_DI_PROVA;ASPETTO=SACERDOTESSA;MANDANTE=la sacerdotessa della luna;"
            + "NOME=la benedizione della luna;BENEDIZIONE=SALUTE AUMENTO_PERCENTUALE 10;RICHIESTA=Un favore.;BATTUTA=Quale?;"
            + "RISPOSTA=Le ombre.;FAVORE=Le ombre della radura;LUOGO=RADURA;NEMICO=OMBRA_NERA;NUMERO=2;"
            + "VITTORIA=Le ombre sono svanite.;BENEDETTO=La luce scende su %PERSONAGGIO%.";

    private static final String TERRA = "CHIAVE=TERRA_DI_PROVA;ASPETTO=SACERDOTE;MANDANTE=il sacerdote della terra;"
            + "NOME=la benedizione della terra;BENEDIZIONE=COSTITUZIONE AUMENTO_FISSO 2;RICHIESTA=Fiori.;BATTUTA=Quali?;"
            + "RISPOSTA=Di loto.;FAVORE=I fiori;GENERE=M;INGREDIENTE=fiore di loto bianco;INGREDIENTI=fiori di loto bianco;"
            + "DOVE=LUOGHI PALUDE;QUANTITA=4;VITTORIA=I fiori ci sono.;BENEDETTO=La terra trema per %PERSONAGGIO%.";
    private static final String NOTTE = "CHIAVE=NOTTE_DI_PROVA;ASPETTO=SACERDOTESSA;MANDANTE=la sacerdotessa della notte;"
            + "NOME=la benedizione della notte;BENEDIZIONE=PERCEZIONE AUMENTO_FISSO 2;RICHIESTA=La tomba.;BATTUTA=Quale?;"
            + "RISPOSTA=Del santo.;FAVORE=La veglia;LUOGO=BOSCO;VISITE=2;ORE=16;VEGLIA=Tutto tace.;VITTORIA=La veglia è finita.;"
            + "BENEDETTO=Il buio è meno buio per %PERSONAGGIO%.";
    private static final String GUADO = "CHIAVE=GUADO_DI_PROVA;ASPETTO=SACERDOTE;MANDANTE=il sacerdote del fiume;"
            + "NOME=la benedizione del fiume;BENEDIZIONE=SALUTE AUMENTO_PERCENTUALE 10;RICHIESTA=Il guado.;BATTUTA=Quale?;"
            + "RISPOSTA=Quello del troll.;FAVORE=Il troll del guado;LUOGO=PALUDE;NEMICO=TROLL;NUMERO=1;"
            + "VITTORIA=Il guado è libero.;BENEDETTO=L'acqua scorre per %PERSONAGGIO%.";

    @Test
    void ogniBenedizioneSiLegge() {
        Set<String> chiavi = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            BenedizioneRichiesta benedizione = BenedizioneRichiesta.da(ProduttoreDiTestiCasuale.rigaDiMissioni("BENEDIZIONE"));
            assertNotNull(benedizione.nuovoModificatore());
            chiavi.add(benedizione.getChiave());
        }
        assertTrue(chiavi.size() >= 7, String.valueOf(chiavi));
        assertEquals(FavoreRichiesto.Tipo.COMBATTIMENTO, BenedizioneRichiesta.da(LUNA).getFavore().getTipo());
        assertEquals(FavoreRichiesto.Tipo.RACCOLTA, BenedizioneRichiesta.da(TERRA).getFavore().getTipo());
        assertEquals(FavoreRichiesto.Tipo.VEGLIA, BenedizioneRichiesta.da(NOTTE).getFavore().getTipo());
        assertThrows(IllegalArgumentException.class, () -> BenedizioneRichiesta.da(LUNA + ";VISITE=2;ORE=3;VEGLIA=x"));
        assertThrows(IllegalArgumentException.class, () -> BenedizioneRichiesta.da(TERRA + ";LUOGO=PALUDE"));
        assertThrows(IllegalArgumentException.class, () -> BenedizioneRichiesta.da(LUNA.replace("SALUTE AUMENTO_PERCENTUALE 10", "SALUTE 10")));
        assertThrows(IllegalArgumentException.class, () -> BenedizioneRichiesta.da(LUNA.replace("AUMENTO_PERCENTUALE 10", "QUANTITA_ASSOLUTA 10")));
        assertThrows(IllegalArgumentException.class, () -> BenedizioneRichiesta.da(LUNA.replace("ASPETTO=SACERDOTESSA", "ASPETTO=MAGA")));
    }

    @Test
    void fattoIlFavoreIlPersonaggioSceltoRiceveLaBenedizione() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(241)) {
            LaBenedizione benedizione = incontraLaSacerdotessa(partita);
            IlFavore favore = (IlFavore) benedizione.getMissioniAffidate(LaBenedizione.FAVORE).get(0);
            assertTrue(favore.isAttiva());
            assertTrue(benedizione.getMissioniSecondarie().contains(favore));
            assertEquals("Le ombre della radura", favore.getNome());
            assertEquals("La sacerdotessa della luna ti darà la benedizione della luna in cambio di un favore: Le ombre della radura.",
                    benedizione.getDescrizione());

            // Il favore: le ombre nella radura segnata sulla mappa
            favore.controllaInLocazione();
            CoordinateMD radura = favore.getPosto();
            assertEquals(ClassiLocazione.RADURA, Foresta.getLocazione(radura));
            assertTrue(Foresta.isLocazioneConosciuta(radura));
            partita.gruppo().setCoordinate(radura);
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.OMBRA_NERA));
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.OMBRA_NERA));
            favore.controllaPostLocazione();
            assertTrue(favore.isCompleta());
            benedizione.controllaPostLocazione();
            assertTrue(partita.testi().contains("Le ombre sono svanite."), String.valueOf(partita.testi()));

            // Il tempio
            CoordinateMD tempio = benedizione.getTempio();
            assertEquals(ClassiLocazione.TEMPIO, Foresta.getLocazione(tempio));
            assertTrue(Foresta.isLocazioneConosciuta(tempio));
            assertTrue(benedizione.getDescrizione().startsWith("Il favore è fatto"), benedizione.getDescrizione());

            Guerriero compagno = new Guerriero("Compagno", 1);
            partita.gruppo().aggiungiPersonaggio(compagno);
            int saluteMassima = compagno.getSaluteMassima();
            partita.gruppo().setCoordinate(tempio);
            benedizione.controllaPreLocazione();
            Passo scelta = benedizione.getDomandaDaPorre(Passo.MomentoControllo.PRE_LOCAZIONE);
            assertNotNull(scelta);
            assertEquals("Chi riceve la benedizione della luna?", scelta.getDomanda());
            assertEquals(java.util.Arrays.asList("Arsenio", "Compagno"), scelta.getOpzioni());
            benedizione.rispondi("2");
            benedizione.controllaPreLocazione();

            assertTrue(benedizione.isCompleta());
            assertEquals("Compagno", benedizione.getBenedetto());
            assertTrue(compagno.getModelloDati().getModificatori().stream()
                    .anyMatch(m -> m.getTipoAttributo() == TipoAttributo.SALUTE && "la benedizione della luna".equals(m.getNote())));
            assertTrue(compagno.getSaluteMassima() > saluteMassima);
            assertTrue(partita.testi().contains("La luce scende su Compagno. La benedizione della luna resterà con Compagno per sempre."),
                    String.valueOf(partita.testi()));
            assertTrue(RegistroMissioni.getTutteLeMissioni().stream().anyMatch(m -> m instanceof LaBenedizione && m != benedizione),
                    "si potrà ricevere un'altra benedizione");
        }
    }

    @Test
    void nellaPaludeDelFavoreCiSonoINemici() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(245)) {
            LaBenedizione benedizione = incontra(partita, GUADO);
            IlFavore favore = (IlFavore) benedizione.getMissioniAffidate(LaBenedizione.FAVORE).get(0);
            favore.controllaInLocazione();
            CoordinateMD palude = favore.getPosto();
            assertEquals(ClassiLocazione.PALUDE, Foresta.getLocazione(palude));

            // Come in Automa.entraInStatoPreparazioneLocazione
            partita.gruppo().setCoordinate(palude);
            Locazione locazione = Foresta.costruisciIstanza(palude);
            partita.gruppo().setLocazioneCorrente(locazione);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            locazione.crea(partita.gruppo(), avversari);
            RegistroMissioni.getIncontroMissione(palude).orElseThrow(AssertionError::new).forEach(avversari::aggiungiPersonaggio);
            locazione.descrivi(partita.gruppo(), avversari);
            assertFalse(partita.testi().stream().anyMatch(t -> t.contains("non trova nulla")), String.valueOf(partita.testi()));
            assertNotEquals(Stato.FINE_LOCAZIONE, locazione.impostaAzioni(partita.gruppo(), avversari, null));
            assertFalse(locazione.isCompleta());
        }
    }

    @Test
    void ilFavoreDiRaccoltaFinisceQuandoCiSonoTuttiGliIngredienti() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(243)) {
            LaBenedizione benedizione = incontra(partita, TERRA);
            IlFavore favore = (IlFavore) benedizione.getMissioniAffidate(LaBenedizione.FAVORE).get(0);
            assertEquals("RACCOLTA", favore.getPassoCorrente());
            assertTrue(favore.getDescrizione().startsWith("Per il sacerdote della terra: raccogli quattro fiori di loto bianco"),
                    favore.getDescrizione());
            new com.threeamigos.foresta.oggetti.OggettoMissione(favore.getId(), IlFavore.INGREDIENTE, favore.getIngredienti().getNome(), 4)
                    .prendi(partita.gruppo(), null);
            favore.controllaPostLocazione();
            assertTrue(favore.isCompleta());
            benedizione.controllaPostLocazione();
            assertNotNull(benedizione.getTempio());
            assertTrue(partita.testi().contains("I fiori ci sono."), String.valueOf(partita.testi()));
        }
    }

    @Test
    void ilFavoreDiVegliaFinisceDopoLeVisite() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(244)) {
            LaBenedizione benedizione = incontra(partita, NOTTE);
            IlFavore favore = (IlFavore) benedizione.getMissioniAffidate(LaBenedizione.FAVORE).get(0);
            favore.controllaInLocazione();
            CoordinateMD bosco = favore.getPosto();
            assertEquals(ClassiLocazione.BOSCO, Foresta.getLocazione(bosco));
            partita.gruppo().setCoordinate(bosco);
            favore.controllaPreLocazione();
            assertTrue(partita.testi().contains("Tutto tace. Bisogna tornare qui ancora una volta, lasciando passare almeno 16 ore "
                    + "fra una visita e l'altra."), String.valueOf(partita.testi()));
            LineaTemporale.aggiungiOre(16);
            favore.controllaPreLocazione();
            assertTrue(favore.isCompleta());
            benedizione.controllaPostLocazione();
            assertNotNull(benedizione.getTempio());
            assertTrue(partita.testi().contains("La veglia è finita."), String.valueOf(partita.testi()));
        }
    }

    @Test
    void ilFavoreSopravviveAUnSalvataggio() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(242)) {
            LaBenedizione benedizione = incontraLaSacerdotessa(partita);
            String id = benedizione.getMissioniAffidate(LaBenedizione.FAVORE).get(0).getId();

            GestoreSalvataggi.salva(Comando.NUMERO_2);
            assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_2));
            LaBenedizione riletta = RegistroMissioni.getTutteLeMissioni().stream().filter(LaBenedizione.class::isInstance)
                    .map(LaBenedizione.class::cast).filter(Missione::isAttiva).findFirst().orElseThrow(AssertionError::new);
            List<Missione> affidate = riletta.getMissioniAffidate(LaBenedizione.FAVORE);
            assertEquals(1, affidate.size());
            assertTrue(affidate.get(0) instanceof IlFavore);
            assertEquals(id, affidate.get(0).getId());
            assertEquals("Le ombre della radura", affidate.get(0).getNome());
        }
    }

    /**
     * Il gruppo entra in una locanda già visitata due volte: la sacerdotessa offre la benedizione e affida il favore.
     */
    private static LaBenedizione incontraLaSacerdotessa(PartitaDiTest partita) {
        return incontra(partita, LUNA);
    }

    private static LaBenedizione incontra(PartitaDiTest partita, String riga) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
        CoordinateMD locanda = unaLocanda();
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locanda.LOCANDA_VISITE, "2");
        partita.gruppo().setCoordinate(locanda);
        LaBenedizione benedizione = RegistroMissioni.getTutteLeMissioni().stream().filter(LaBenedizione.class::isInstance)
                .map(LaBenedizione.class::cast).findFirst().orElseThrow(AssertionError::new);
        benedizione.aggiungiProprieta("PARAMETRO_" + LaBenedizione.BENEDIZIONE, riga);
        benedizione.controllaPreLocazione();
        assertEquals(LaBenedizione.FAVORE, benedizione.getPassoCorrente());
        benedizione.segnaIntermezzoPassoMostrato("INCONTRO");
        benedizione.controllaInLocazione();
        assertTrue(benedizione.isAttiva());
        assertEquals(1, benedizione.getMissioniAffidate(LaBenedizione.FAVORE).size());
        return benedizione;
    }

    private static CoordinateMD unaLocanda() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == ClassiLocazione.LOCANDA) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("Nessuna locanda nella Foresta");
    }
}
