package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaAperturaFinestraCombattimento;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.Bottino;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I mattoni con cui si scrivono le missioni a passi (vai, ricompensa, scorta, sorveglia, costruisci, affida...), provati su una missione
 * inventata i cui passi li decide la prova.
 */
class PassiSinteticiTest {

    /** Una missione senza una storia sua: i passi li mette chi la prova. */
    static final class Sintetica extends MissioneAPassi {
        final Map<String, java.util.function.Function<Sintetica, Passo>> passi = new HashMap<>();
        String iniziale = "A";
        boolean ripetibile;
        Missione passataAllaNuova;

        Sintetica() {
            super(MissionePredefinita.MISSIONE_DI_PROVA);
        }

        Sintetica passo(String id, java.util.function.Function<Sintetica, Passo> passo) {
            passi.put(id, passo);
            return this;
        }

        @Override
        protected String passoIniziale() {
            return iniziale;
        }

        @Override
        protected Passo costruisciPasso(String id) {
            java.util.function.Function<Sintetica, Passo> passo = passi.get(id);
            if (passo == null) {
                throw new IllegalArgumentException("Passo sconosciuto: " + id);
            }
            return passo.apply(this);
        }

        @Override
        public String getNome() {
            return "Sintetica";
        }

        @Override
        public String getDescrizione() {
            return "Una missione di prova";
        }

        @Override
        protected boolean isRipetibile() {
            return ripetibile;
        }

        @Override
        protected void passaAllaNuova(Missione nuova) {
            passataAllaNuova = nuova;
        }

        // I mattoni sono protetti: la prova li chiama da qui
        Passo _vai(MomentoControllo m, CoordinateMD c) {
            return vai(m, c);
        }

        Passo _vai(MomentoControllo m, TipoLocazione t) {
            return vai(m, t);
        }

        Passo _torna(MomentoControllo m) {
            return tornaAlPuntoDiPartenza(m);
        }

        Passo _dialogo(MomentoControllo m, String testo) {
            return dialogo(m, () -> testo);
        }

        Passo _ricompensa(MomentoControllo m, Ricompensa r) {
            return ricompensa(m, r, () -> "ricompensa");
        }

        Passo _attendi(MomentoControllo m, int ore) {
            return attendiOre(m, ore);
        }

        Passo _conta(MomentoControllo m, String c, int n) {
            return contaFinche(m, c, n);
        }

        void _incrementa(String c, int n) {
            incrementaContatore(c, n);
        }

        Passo _sconfiggi(MomentoControllo m, TipoPersonaggio classe, int n) {
            return sconfiggi(m, classe, n);
        }

        Passo _raccogli(MomentoControllo m, TipoBottino tipo, int n) {
            return raccogli(m, tipo, n);
        }

        Passo _raccogli(MomentoControllo m, OggettiDaRaccogliere o) {
            return raccogli(m, o);
        }

        Passo _consegna(MomentoControllo m, BooleanSupplier dove, OggettiDaRaccogliere o) {
            return consegna(m, dove, o, () -> "consegnato");
        }

        Passo _sorveglia(Supplier<CoordinateMD> dove, int volte, int ore) {
            return sorveglia(dove, volte, ore);
        }

        Passo _esplora(int n) {
            return esplora(n);
        }

        Passo _evita(MomentoControllo m, Supplier<CoordinateMD> dove) {
            return evitaCombattimento(m, dove, () -> "scoperti");
        }

        Passo _costruisci(MomentoControllo m, BooleanSupplier dove, Costruzione c) {
            return costruisci(m, dove, c, () -> "costruito");
        }

        Passo _parametri(Map<String, Supplier<String>> p) {
            return generaParametri(MomentoControllo.PRE_LOCAZIONE, p);
        }

        String _parametro(String nome, Supplier<String> g) {
            return parametro(nome, g);
        }

        Passo _affida(String chiave, BooleanSupplier quando, Supplier<List<Missione>> missioni) {
            return affida(chiave, MomentoControllo.PRE_LOCAZIONE, quando, missioni);
        }

        Passo _attendiLeAffidate(String chiave) {
            return attendiLeAffidate(chiave, MomentoControllo.PRE_LOCAZIONE);
        }

        Passo _combatti(Supplier<CoordinateMD> dove, IncontroDiMissione i) {
            return combatti(dove, i);
        }

        Passo _combattiIlCapo(Supplier<CoordinateMD> dove, IncontroDiMissione i) {
            return combattiIlCapo(dove, i);
        }

        Passo _prendiInScorta(BooleanSupplier quando) {
            return prendiInScorta(MomentoControllo.PRE_LOCAZIONE, quando, "Viandante di prova");
        }

        Passo _scorta(Supplier<CoordinateMD> dove, boolean congeda) {
            return scorta(MomentoControllo.PRE_LOCAZIONE, dove, congeda);
        }

        Passo _scortaConTesto(Supplier<CoordinateMD> dove, boolean congeda) {
            return scorta(MomentoControllo.PRE_LOCAZIONE, dove, () -> "il viandante è morto", congeda);
        }

        Passo _scortaDefault(Supplier<CoordinateMD> dove) {
            return scorta(MomentoControllo.PRE_LOCAZIONE, dove);
        }

        Passo _scortaDefaultConTesto(Supplier<CoordinateMD> dove) {
            return scorta(MomentoControllo.PRE_LOCAZIONE, dove, () -> "morto");
        }

        Passo _scortaFinoAllaMeta(Supplier<CoordinateMD> dove, boolean congeda) {
            return scortaFinoAllaMeta(MomentoControllo.PRE_LOCAZIONE, dove, congeda);
        }

        Passo _scortaFinoAllaMetaDefault(Supplier<CoordinateMD> dove) {
            return scortaFinoAllaMeta(MomentoControllo.PRE_LOCAZIONE, dove);
        }

        Passo _cercaLocazione(TipoLocazione t) {
            return cercaLocazione(MomentoControllo.PRE_LOCAZIONE, t);
        }

        void _sequenza(List<String> ids) {
            impostaSequenzaPassi(ids);
        }

        List<String> _leggiSequenza() {
            return leggiSequenzaPassi();
        }

        Supplier<String> _prossimoNellaSequenza() {
            return prossimoNellaSequenza();
        }

        boolean _inLuogoForzato() {
            return isInLuogoDellaPartenzaForzata();
        }

        boolean _disponibile() {
            return isDisponibile();
        }

        void _dimentica(String id) {
            dimenticaRisposta(id);
        }

        int _oreBase() {
            return getOreFraUnaMissioneELAltra();
        }
    }

    private static final MomentoControllo PRE = MomentoControllo.PRE_LOCAZIONE;
    private static final MomentoControllo IN = MomentoControllo.IN_LOCAZIONE;
    private static final MomentoControllo POST = MomentoControllo.POST_LOCAZIONE;

    private CoordinateMD qui;
    private CoordinateMD altrove;

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(21);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        FabbricaMissioni.registra(MissionePredefinita.MISSIONE_DI_PROVA, Sintetica::new);
        qui = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA);
        altrove = new CoordinateMD(15, 15);
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private Sintetica nuova() {
        Sintetica m = new Sintetica();
        m.getModelloDati().setId("SINTETICA");
        RegistroMissioni.aggiungiMissioneSecondaria(m);
        return m;
    }

    private void vaiA(CoordinateMD c) {
        Foresta.setPosizioneDelGruppo(c);
    }

    // ---- Il motore dei passi

    @Test
    void laPartenzaForzataFaCominciareSubito() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        assertFalse(m.isPartenzaForzata());
        assertFalse(m._inLuogoForzato());
        m.forzaPartenza(() -> false);
        assertTrue(m.isPartenzaForzata());
        assertFalse(m._inLuogoForzato());
        m.controllaPreLocazione();
        assertFalse(m.isCompleta());
        m.forzaPartenza(() -> true);
        assertTrue(m._inLuogoForzato());
        m.controllaPreLocazione();
        assertTrue(m.isCompleta());
        assertFalse(m.isPartenzaForzata());
    }

    @Test
    void unaDomandaNonSiForzaEPassiInFilaSenzaFineSiFermano() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> true).poi("B")).passo("B", s -> Passo.quando(PRE, () -> true).poi("A"));
        assertThrows(IllegalStateException.class, m::controllaPreLocazione);
        Sintetica d = nuova().passo("A", s -> Passo.quando(PRE, () -> true).chiediConferma("Si fa?").poi(Passo.FINE));
        d.forzaPartenza(() -> true);
        d.controllaPreLocazione();
        assertFalse(d.isCompleta(), "la domanda aspetta la risposta");
    }

    @Test
    void unaMissioneAFineNonFaPiuNiente() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> true).poi(Passo.FINE));
        m.controllaPreLocazione();
        assertTrue(m.isCompleta());
        assertEquals(Passo.FINE, m.getPassoCorrente());
        Sintetica f = nuova();
        f.aggiungiProprieta("PASSO_CORRENTE", Passo.FINE);
        f.controllaPreLocazione();
        assertFalse(f.isCompleta());
        assertNull(f.getDomandaDaPorre(PRE));
        assertFalse(f.getOggettoInLocazione(qui, TipoLocazione.BOSCO, false).isPresent());
        assertFalse(f.getIncontroInLocazione(qui).isPresent());
        assertTrue(f.getOndateSuccessiveInLocazione(qui).isEmpty());
        assertFalse(f.haUnIntermezzoInArrivo(PRE, com.threeamigos.foresta.tipi.MomentoIntermezzo.INIZIO_LOCAZIONE));
    }

    @Test
    void ilPuntoDiPartenzaSiRicordaDaQuandoSiAttiva() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        assertNull(m.getPuntoDiPartenza());
        vaiA(qui);
        m.attivaMissione();
        assertEquals(qui, m.getPuntoDiPartenza());
        vaiA(altrove);
        m.attivaMissione();
        assertEquals(qui, m.getPuntoDiPartenza(), "non si riscrive");
        Passo torna = m._torna(PRE);
        assertFalse(torna.isConcluso());
        vaiA(qui);
        assertTrue(torna.isConcluso());
        assertFalse(nuova()._torna(PRE).isConcluso(), "senza un punto di partenza non si torna");
    }

    // ---- I passi pronti

    @Test
    void vaiDiciEDialogo() {
        Sintetica m = nuova();
        vaiA(altrove);
        assertFalse(m._vai(PRE, qui).isConcluso());
        assertFalse(m._vai(PRE, TipoLocazione.CITTA_NYENA).isConcluso());
        assertEquals(qui, m._vai(PRE, TipoLocazione.CITTA_NYENA).getLuogoDaSegnalare());
        vaiA(qui);
        assertTrue(m._vai(PRE, qui).isConcluso());
        assertTrue(m._vai(PRE, TipoLocazione.CITTA_NYENA).isConcluso());
        Passo dialogo = m._dialogo(IN, "Buongiorno");
        assertTrue(dialogo.isConcluso());
        dialogo.eseguiAzione();
    }

    @Test
    void laRicompensaDaTuttoQuelloCheHa() {
        Sintetica m = nuova();
        m._ricompensa(PRE, Ricompensa.inMonete(30).conPreziosi(2).conEsperienza(50)
                .conArtefatto(() -> GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 1))).eseguiAzione();
        assertEquals(30, Cassa.getMonete());
        assertEquals(2, Cassa.getPreziosi());
        assertFalse(GruppoGiocatore.getIstanza().getInventario().isEmpty());
        m._ricompensa(PRE, Ricompensa.nulla()).eseguiAzione();
        assertEquals(30, Cassa.getMonete());
        assertThrows(IllegalArgumentException.class, () -> Ricompensa.inMonete(-1));
        assertThrows(IllegalArgumentException.class, () -> Ricompensa.nulla().conPreziosi(-1));
        assertThrows(IllegalArgumentException.class, () -> Ricompensa.nulla().conEsperienza(-1));
    }

    @Test
    void attesaContatoriEventi() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false));
        m.aggiungiProprieta("PASSO_CORRENTE", "A");
        assertEquals(0, m.getContatore("X"));
        Passo conta = m._conta(PRE, "X", 2);
        assertFalse(conta.isConcluso());
        m._incrementa("X", 2);
        assertTrue(conta.isConcluso());
        assertThrows(IllegalArgumentException.class, () -> m._incrementa("a§b", 1));
        Passo sconfiggi = m._sconfiggi(PRE, TipoPersonaggio.GOBLIN, 2);
        Passo raccogli = m._raccogli(PRE, TipoBottino.MONETA, 3);
        assertFalse(sconfiggi.isConcluso());
        assertFalse(raccogli.isConcluso());
        m.registraEvento(MissioneAPassi.eventoSconfitto(TipoPersonaggio.GOBLIN), 2);
        m.registraEvento(MissioneAPassi.eventoRaccolto(TipoBottino.MONETA), 3);
        assertTrue(sconfiggi.isConcluso());
        assertTrue(raccogli.isConcluso());
        assertEquals(2, m.getConteggioNelPassoCorrente(MissioneAPassi.eventoSconfitto(TipoPersonaggio.GOBLIN)));
        Passo attendi = m._attendi(PRE, 5);
        assertFalse(attendi.isConcluso());
        LineaTemporale.aggiungiOre(6);
        assertTrue(attendi.isConcluso());
    }

    @Test
    void gliEventiSiContanoSoloFinoAllaFine() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        m.registraEvento("QUALCOSA", 1);
        assertEquals(1, m.getConteggioNelPassoCorrente("QUALCOSA"));
        m.completaMissione();
        m.registraEvento("QUALCOSA", 1);
        m.oggettoDiMissioneRaccolto("COSA", 3);
        assertEquals(0, m.getContatore("COSA"));
        Sintetica f = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        f.oggettoDiMissioneRaccolto("COSA", 3);
        assertEquals(3, f.getContatore("COSA"));
    }

    @Test
    void gliOggettiDaRaccogliereSiSeminanoEConsegnano() {
        NomeOggetto nome = NomeOggetto.femminile("radice", "radici");
        OggettiDaRaccogliere nelleLocande = OggettiDaRaccogliere.di("RADICI", nome, 4).in(TipoLocazione.PALUDE, TipoLocazione.BOSCO)
                .conProbabilita(100).alPiuPerLocazione(2);
        Sintetica m = nuova().passo("A", s -> s._raccogli(PRE, nelleLocande).poi(Passo.FINE));
        m.attivaMissione();
        Optional<Bottino> su = m.getOggettoInLocazione(new CoordinateMD(1, 1), TipoLocazione.BOSCO, false);
        assertTrue(su.isPresent());
        assertFalse(m.getOggettoInLocazione(new CoordinateMD(1, 1), TipoLocazione.BOSCO, true).isPresent(), "già visitata");
        assertFalse(m.getOggettoInLocazione(new CoordinateMD(1, 1), TipoLocazione.RADURA, false).isPresent(), "tipo sbagliato");
        // Dopo qualche ora il ripiego segna un posto dove ce ne sono sempre
        LineaTemporale.aggiungiOre(OggettiDaRaccogliere.ORE_AL_RIPIEGO + 1);
        m.controllaPreLocazione();
        CoordinateMD ripiego = m.getRipiego(nelleLocande);
        assertNotNull(ripiego);
        assertTrue(m.getOggettoInLocazione(ripiego, Foresta.getLocazione(ripiego), true).isPresent());
        m.oggettoDiMissioneRaccolto("RADICI", 4);
        assertFalse(m.getOggettoInLocazione(ripiego, Foresta.getLocazione(ripiego), true).isPresent(), "ormai ne ha tutte");
        Passo consegna = m._consegna(PRE, () -> true, nelleLocande);
        assertTrue(consegna.isConcluso());
        consegna.eseguiAzione();
        assertEquals(0, m.getContatore("RADICI"));
        assertFalse(m._consegna(PRE, () -> true, nelleLocande).isConcluso());
        // Quelli in una casella sola
        OggettiDaRaccogliere inCasella = OggettiDaRaccogliere.di("CHIAVE", nome, 1).nellaCasella(altrove);
        Sintetica c = nuova().passo("A", s -> s._raccogli(PRE, inCasella).poi(Passo.FINE));
        c.attivaMissione();
        assertTrue(c.getOggettoInLocazione(altrove, TipoLocazione.BOSCO, true).isPresent());
        assertFalse(c.getOggettoInLocazione(qui, TipoLocazione.BOSCO, false).isPresent());
        c.controllaPreLocazione();
        assertNull(c.getRipiego(inCasella));
    }

    @Test
    void iTrofeiSiPortanoAddosso() {
        NomeOggetto nome = NomeOggetto.maschile("dente", "denti");
        OggettiDaRaccogliere trofei = OggettiDaRaccogliere.di("DENTI", nome, 3).daiNemici(TipoPersonaggio.GOBLIN).conProbabilita(100)
                .alPiuPerLocazione(2).conRipiegoDopoOre(5);
        assertTrue(trofei.isTrofeo());
        Sintetica m = nuova().passo("A", s -> s._raccogli(PRE, trofei).poi(Passo.FINE));
        m.attivaMissione();
        assertFalse(m.getOggettoInLocazione(qui, TipoLocazione.BOSCO, false).isPresent(), "nessun goblin");
        GruppoAvversario nemici = GruppoAvversario.getIstanza();
        nemici.reimposta();
        nemici.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1));
        nemici.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1));
        assertTrue(m.getOggettoInLocazione(qui, TipoLocazione.BOSCO, false).isPresent());
        LineaTemporale.aggiungiOre(6);
        m.controllaPreLocazione();
        CoordinateMD ripiego = m.getRipiego(trofei);
        assertNotNull(ripiego);
        assertTrue(m.getIncontroInLocazione(ripiego).isPresent(), "nel posto del ripiego ci sono i mostri che portano i trofei");
        assertTrue(m.getOggettoInLocazione(ripiego, TipoLocazione.BOSCO, true).isPresent());
    }

    @Test
    void sorvegliareEsplorareEvitare() {
        Sintetica m = nuova();
        m.passo("A", s -> m._sorveglia(() -> qui, 2, 3).poi(Passo.FINE));
        m.attivaMissione();
        vaiA(altrove);
        m.controllaPreLocazione();
        assertEquals(0, m.getVisiteNelPassoCorrente());
        vaiA(qui);
        m.controllaPreLocazione();
        assertEquals(1, m.getVisiteNelPassoCorrente());
        m.controllaPreLocazione();
        assertEquals(1, m.getVisiteNelPassoCorrente(), "troppo presto per un'altra visita");
        LineaTemporale.aggiungiOre(4);
        m.controllaPreLocazione();
        assertTrue(m.isCompleta());
        Sintetica e = nuova();
        e.passo("A", s -> e._esplora(2).poi(Passo.FINE));
        e.attivaMissione();
        vaiA(new CoordinateMD(20, 20));
        e.controllaPreLocazione();
        Foresta.setLocazioneVisitata(new CoordinateMD(20, 20), true);
        e.controllaPreLocazione();
        assertEquals(1, e.getCaselleEsplorate());
        vaiA(new CoordinateMD(21, 21));
        e.controllaPreLocazione();
        e.controllaPreLocazione();
        assertTrue(e.isCompleta());
        Sintetica v = nuova();
        v.passo("A", s -> v._evita(PRE, () -> qui).poi(Passo.FINE));
        v.attivaMissione();
        vaiA(qui);
        BusEventi.pubblica(new InternoRichiestaAperturaFinestraCombattimento(null, null));
        assertTrue(v.haCombattutoNelPassoCorrente());
        v.controllaPreLocazione();
        assertTrue(v.isFallita());
        assertFalse(v._evita(PRE, () -> null).isConcluso());
    }

    @Test
    void costruireEParametri() {
        OggettiDaRaccogliere legno = OggettiDaRaccogliere.di("LEGNO", NomeOggetto.maschile("ceppo", "ceppi"), 2);
        Costruzione costruzione = Costruzione.con(legno).conMonete(40).inOre(3);
        assertThrows(IllegalArgumentException.class, () -> Costruzione.senzaMateriali().conMonete(-1));
        assertThrows(IllegalArgumentException.class, () -> Costruzione.senzaMateriali().inOre(-1));
        Sintetica m = nuova();
        assertFalse(m.isCostruibile(costruzione));
        m._incrementa("LEGNO", 2);
        assertFalse(m.isCostruibile(costruzione), "mancano le monete");
        Cassa.addMonete(100);
        assertTrue(m.isCostruibile(costruzione));
        Passo passo = m._costruisci(PRE, () -> true, costruzione);
        assertTrue(passo.isConcluso());
        int ora = LineaTemporale.getOra();
        passo.eseguiAzione();
        assertEquals(60, Cassa.getMonete());
        assertEquals(0, m.getContatore("LEGNO"));
        assertFalse(m._costruisci(PRE, () -> false, costruzione).isConcluso());
        Costruzione gratis = Costruzione.senzaMateriali();
        m._costruisci(PRE, () -> true, gratis).eseguiAzione();
        assertEquals(60, Cassa.getMonete());
        Map<String, Supplier<String>> parametri = new HashMap<>();
        parametri.put("NOME", () -> "Gualtiero");
        m._parametri(parametri).eseguiAzione();
        assertEquals("Gualtiero", m.getParametro("NOME"));
        parametri.put("NOME", () -> "Altro");
        m._parametri(parametri).eseguiAzione();
        assertEquals("Gualtiero", m.getParametro("NOME"), "un parametro si fissa una volta sola");
        assertEquals("Gualtiero", m._parametro("NOME", () -> "Altro"));
        assertEquals("Nuovo", m._parametro("ALTRO", () -> "Nuovo"));
        assertTrue(m._cercaLocazione(TipoLocazione.TEMPIO).isConcluso());
    }

    @Test
    void lemissioniAffidate() {
        Sintetica m = nuova();
        Sintetica figlia = new Sintetica();
        figlia.getModelloDati().setId("FIGLIA");
        figlia.passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        boolean[] via = {false};
        Passo affida = m._affida("GIRO", () -> via[0], () -> Collections.singletonList(figlia));
        assertFalse(affida.isConcluso());
        via[0] = true;
        assertTrue(affida.isConcluso());
        affida.eseguiAzione();
        assertEquals(1, m.getMissioniAffidate("GIRO").size());
        assertTrue(figlia.isAttiva());
        affida.eseguiAzione();
        assertEquals(1, m.getMissioniAffidate("GIRO").size(), "si affida una volta sola");
        assertTrue(m.getMissioniAffidate("NIENTE").isEmpty());
        Passo attesa = m._attendiLeAffidate("GIRO");
        assertFalse(attesa.isConcluso());
        assertFalse(m.sonoRiusciteLeAffidate("GIRO"));
        figlia.fallisciMissione();
        assertTrue(attesa.isConcluso());
        assertFalse(m.sonoRiusciteLeAffidate("GIRO"));
        assertFalse(m._attendiLeAffidate("NIENTE").isConcluso());
        assertFalse(m.sonoRiusciteLeAffidate("NIENTE"));
    }

    @Test
    void ilCombattimentoDellaMissione() {
        IncontroDiMissione banda = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 2);
        Sintetica m = nuova().passo("A", s -> s._combatti(() -> altrove, banda).poi(Passo.FINE));
        m.attivaMissione();
        assertFalse(m.getIncontroInLocazione(qui).isPresent());
        Optional<List<Personaggio>> incontro = m.getIncontroInLocazione(altrove);
        assertTrue(incontro.isPresent());
        assertEquals(2, incontro.get().size());
        assertTrue(m.getOndateSuccessiveInLocazione(altrove).isEmpty());
        assertTrue(m.getOndateSuccessiveInLocazione(qui).isEmpty());
        vaiA(altrove);
        m.controllaPostLocazione();
        assertFalse(m.isCompleta());
        m.registraEvento(MissioneAPassi.eventoSconfittoIn(TipoPersonaggio.GOBLIN, altrove), 2);
        assertEquals(2, m.getConteggioNelPassoCorrente(MissioneAPassi.eventoSconfittoIn(TipoPersonaggio.GOBLIN, altrove)));
        assertEquals(altrove, Foresta.getPosizioneDelGruppo());
        assertTrue(m.costruisciPasso("A").isConcluso());
        m.controllaPostLocazione();
        assertTrue(m.isCompleta());
        IncontroDiMissione capo = IncontroDiMissione.di(TipoPersonaggio.GOBLIN, 2).conCapo("Brogo", TipoPersonaggio.TROLL);
        Sintetica c = nuova().passo("A", s -> s._combattiIlCapo(() -> altrove, capo).poi(Passo.FINE));
        c.attivaMissione();
        vaiA(altrove);
        c.controllaPostLocazione();
        assertFalse(c.isCompleta());
        c.registraEvento(MissioneAPassi.eventoSconfittoIn(TipoPersonaggio.TROLL, altrove), 1);
        c.controllaPostLocazione();
        assertTrue(c.isCompleta());
        assertFalse(m._combatti(() -> null, banda).isConcluso());
        assertFalse(m._combattiIlCapo(() -> null, capo).isConcluso());
    }

    @Test
    void lAnonimoDaScortare() {
        Sintetica m = nuova();
        CoordinateMD meta = altrove;
        Passo prendi = m._prendiInScorta(() -> true);
        assertTrue(prendi.isConcluso());
        prendi.eseguiAzione();
        assertTrue(m.getScortato().isPresent());
        Passo scorta = m._scorta(() -> meta, true);
        assertFalse(scorta.isConcluso());
        vaiA(meta);
        assertTrue(scorta.isConcluso());
        scorta.eseguiAzione();
        assertFalse(m.getScortato().isPresent(), "congedato");
        // senza congedo resta
        Sintetica n = nuova();
        n._prendiInScorta(() -> true).eseguiAzione();
        n._scorta(() -> meta, false).eseguiAzione();
        assertTrue(n.getScortato().isPresent());
        // se muore, la missione fallisce e la scorta con il testo lo sa
        Passo conTesto = n._scortaConTesto(() -> meta, true);
        assertFalse(conTesto.isFallito());
        n.getScortato().get().subSalute(100000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertTrue(conTesto.isFallito());
        assertEquals("il viandante è morto", conTesto.getTestoFallimento());
        assertFalse(n._scortaDefault(() -> meta).isConcluso());
        assertTrue(n._scortaDefaultConTesto(() -> meta).isFallito());
        assertFalse(n._scorta(() -> null, true).isConcluso());
    }

    @Test
    void scortaFinoAllaMetaAncheDaMorto() {
        CoordinateMD meta = altrove;
        Sintetica vivo = nuova();
        vivo._prendiInScorta(() -> true).eseguiAzione();
        Passo passo = vivo._scortaFinoAllaMeta(() -> meta, true);
        assertFalse(passo.isConcluso());
        vaiA(meta);
        assertTrue(passo.isConcluso());
        passo.eseguiAzione();
        assertFalse(vivo.isScortatoMorto());
        assertFalse(vivo.getScortato().isPresent());
        Sintetica morto = nuova();
        morto._prendiInScorta(() -> true).eseguiAzione();
        morto.getScortato().get().subSalute(100000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        Passo finoAllaMeta = morto._scortaFinoAllaMetaDefault(() -> qui);
        assertTrue(finoAllaMeta.isConcluso());
        finoAllaMeta.eseguiAzione();
        assertTrue(morto.isScortatoMorto());
        Sintetica senzaCongedo = nuova();
        senzaCongedo._prendiInScorta(() -> true).eseguiAzione();
        senzaCongedo._scortaFinoAllaMeta(() -> meta, false).eseguiAzione();
        assertTrue(senzaCongedo.getScortato().isPresent());
        assertFalse(senzaCongedo._scortaFinoAllaMeta(() -> null, true).isConcluso());
    }

    // ---- Domande, sequenze, ripetizioni

    @Test
    void leDomandeEIlLoroErrori() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> true).chiediScelta("Quale?", Arrays.asList("uno", "due", "tre")).poi(() -> "B"))
                .passo("B", s -> Passo.quando(PRE, () -> true).poi(Passo.FINE));
        assertEquals(Arrays.asList("1", "2", "3"), m.costruisciPasso("A").getRispostePossibili());
        assertNotNull(m.getDomandaDaPorre(PRE));
        assertNull(m.getDomandaDaPorre(IN));
        assertThrows(IllegalArgumentException.class, () -> m.rispondi("9"));
        m.rispondi("2");
        assertEquals("2", m.getRisposta("A"));
        assertNull(m.getDomandaDaPorre(PRE), "ha già risposto");
        m._dimentica("A");
        assertNull(m.getRisposta("A"));
        m.rispondi("1");
        m.controllaPreLocazione();
        assertTrue(m.isCompleta());
        assertNull(m.getDomandaDaPorre(PRE), "finita");
        Sintetica senzaDomanda = nuova().passo("A", s -> Passo.quando(PRE, () -> true).poi(Passo.FINE));
        assertThrows(IllegalStateException.class, () -> senzaDomanda.rispondi("SI"));
        Passo conferma = Passo.quando(PRE, () -> true).chiediConferma("Sicuro?");
        assertTrue(conferma.isConferma());
        assertEquals(Arrays.asList(Passo.SI, Passo.NO), conferma.getRispostePossibili());
        assertTrue(Passo.quando(PRE, () -> true).getRispostePossibili().isEmpty());
        assertTrue(Passo.quando(PRE, () -> true).getOpzioni().isEmpty());
        assertFalse(Passo.quando(PRE, () -> true).isConferma());
        assertThrows(IllegalArgumentException.class, () -> Passo.quando(PRE, () -> true).chiediScelta("x", Collections.singletonList("solo")));
        assertThrows(IllegalStateException.class, () -> Passo.quando(PRE, () -> true).getPagine());
        assertThrows(IllegalStateException.class, () -> Passo.quando(PRE, () -> true).getTestoFallimento());
        assertEquals(Passo.FINE, Passo.quando(PRE, () -> true).getProssimoPasso());
        assertEquals("Quale?", m.costruisciPasso("A").getDomanda());
    }

    @Test
    void laSequenzaDeiPassi() {
        Sintetica m = nuova();
        assertThrows(IllegalArgumentException.class, () -> m._sequenza(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> m._sequenza(Arrays.asList("A", "A")));
        assertThrows(IllegalArgumentException.class, () -> m._sequenza(Arrays.asList("A", "a|b")));
        assertThrows(IllegalArgumentException.class, () -> m._sequenza(Arrays.asList("A", "")));
        assertTrue(m._leggiSequenza().isEmpty());
        m._sequenza(Arrays.asList("A", "B"));
        assertEquals(Arrays.asList("A", "B"), m._leggiSequenza());
        Supplier<String> prossimo = m._prossimoNellaSequenza();
        assertEquals("B", prossimo.get());
        m.aggiungiProprieta("PASSO_CORRENTE", "B");
        assertEquals(Passo.FINE, prossimo.get());
        m.aggiungiProprieta("PASSO_CORRENTE", "Z");
        assertThrows(IllegalStateException.class, prossimo::get);
    }

    @Test
    void unaMissioneChiusaNonDaPiuNiente() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        m.completaMissione();
        m.completaMissione();
        m.fallisciMissione();
        assertTrue(m.isCompleta());
    }

    @Test
    void ilFallimentoAvvisaSoloSeEraInCorsoELasciaLaNuova() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        m.ripetibile = true;
        assertTrue(m._disponibile());
        assertEquals(MissioneAPassi.ORE_FRA_UNA_MISSIONE_E_L_ALTRA, m._oreBase());
        m.attivaMissione();
        m.fallisciMissione();
        assertTrue(m.isFallita());
        assertNotNull(m.passataAllaNuova);
        assertFalse(((Sintetica) m.passataAllaNuova)._disponibile(), "la nuova arriva dopo qualche ora");
        m.fallisciMissione();
        Sintetica c = nuova().passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        c.ripetibile = true;
        c.completaMissione();
        assertNotNull(c.passataAllaNuova);
    }

    @Test
    void gliIntermezziInAttesaESegnati() {
        Sintetica m = nuova().passo("A", s -> Passo.quando(PRE, () -> true)
                        .conIntermezzo(com.threeamigos.foresta.tipi.MomentoIntermezzo.INIZIO_LOCAZIONE, Collections::emptyList).poi("B"))
                .passo("B", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        assertTrue(m.haUnIntermezzoInArrivo(PRE, com.threeamigos.foresta.tipi.MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertFalse(m.haUnIntermezzoInArrivo(IN, com.threeamigos.foresta.tipi.MomentoIntermezzo.INIZIO_LOCAZIONE));
        m.controllaPreLocazione();
        assertEquals(Collections.singletonList("A"), m.getPassiConIntermezzoInAttesa());
        assertEquals("A", m.getPassoConIntermezzoInAttesa(com.threeamigos.foresta.tipi.MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertNull(m.getPassoConIntermezzoInAttesa(com.threeamigos.foresta.tipi.MomentoIntermezzo.LOCAZIONE_COMPLETATA));
        assertTrue(m.haUnIntermezzoInArrivo(POST, com.threeamigos.foresta.tipi.MomentoIntermezzo.LOCAZIONE_COMPLETATA) == false);
        assertTrue(m.haUnIntermezzoInArrivo(PRE, com.threeamigos.foresta.tipi.MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertFalse(m.isIntermezzoPassoMostrato("A"));
        m.segnaIntermezzoPassoMostrato("A");
        assertTrue(m.isIntermezzoPassoMostrato("A"));
        m.segnaIntermezzoPassoMostrato("A");
        m.segnaIntermezzoPassoMostrato("Z");
        assertTrue(m.getPassiConIntermezzoInAttesa().isEmpty());
        assertTrue(m.bloccaIntermezzo(com.threeamigos.foresta.tipi.TipoIntermezzo.INTERMEZZO_INTRODUTTIVO) == false);
        m.completaMissione();
        assertFalse(m.haUnIntermezzoInArrivo(PRE, com.threeamigos.foresta.tipi.MomentoIntermezzo.INIZIO_LOCAZIONE));
    }

}
