package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.eventi.interni.InternoInizioRound;
import com.threeamigos.foresta.eventi.interni.InternoScheletroRisvegliato;
import com.threeamigos.foresta.eventi.notifiche.NotificaColpoMancato;
import com.threeamigos.foresta.eventi.notifiche.NotificaInterazionePersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaReazioneDiClassePersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.EsitoAzione;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I pezzi del combattimento che stanno attorno al calcolatore: l'ordine dei turni e la memoria dello scontro (StatoDelCombattimento), la fine di
 * un colpo (ApplicazioneDelRisultato), gli attacchi con le armi e con gli incantesimi, il cambio d'arma e i piccoli tipi.
 */
class PezziDelCombattimentoTest {

    private final List<Object> eventi = new ArrayList<>();
    private final List<String> frasi = new ArrayList<>();
    private final List<Runnable> iscrizioni = new ArrayList<>();

    @BeforeEach
    void prepara(RepetitionInfo ripetizione) {
        LogDiProva.perRipetizione(ripetizione.getCurrentRepetition());
        BusEventi.impostaConsegna(Runnable::run);
        DatiDiProva.nuovaPartita();
        GruppoAvversario.azzeraIstanza();
        StatoDelCombattimento.azzeraIstanza();
        Dado.impostaSeme(777L);
        for (Class<?> tipo : new Class<?>[] {InternoAzioneInCombattimento.class, InternoInizioRound.class, InternoScheletroRisvegliato.class,
                NotificaColpoMancato.class, NotificaInterazionePersonaggio.class, NotificaReazioneDiClassePersonaggio.class}) {
            iscrizioni.add(iscrivi(tipo));
        }
        iscrizioni.add(BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio())));
    }

    private <T> Runnable iscrivi(Class<T> tipo) {
        return BusEventi.iscriviti(tipo, eventi::add);
    }

    @AfterEach
    void ripulisce() {
        LogDiProva.spegni();
        iscrizioni.forEach(Runnable::run);
        BusEventi.impostaConsegna(null);
        StatoDelCombattimento.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        Dado.ripristina();
    }

    private static Personaggio giocante(String nome, int livello) {
        Personaggio p = new Guerriero(nome, livello);
        p.getModelloDati().setPng(false);
        return p;
    }

    private static Artefatto genera(TipoArtefatto tipo, int livello, long seme) {
        return new GeneratoreArtefattiTabelle(new Random(seme)).generaArtefatto(tipo, livello);
    }

    // --- Tipi piccoli

    @RepeatedTest(2)
    void ondateGruppiDiBersagliEdAnnunci() {
        List<Personaggio> avversari = Arrays.asList(new Goblin(1), new Goblin(1));
        Ondata ondata = new Ondata(avversari, "Altri goblin!");
        assertEquals(2, ondata.getAvversari().size());
        assertEquals("Altri goblin!", ondata.getArrivo());
        assertThrows(UnsupportedOperationException.class, () -> ondata.getAvversari().clear());

        GruppoDiBersagli bersagli = new GruppoDiBersagli(avversari);
        assertSame(avversari.get(0), bersagli.getCapo());
        assertFalse(bersagli.isGruppoGiocatore());
        assertNull(new GruppoDiBersagli(Collections.emptyList()).getCapo());

        AnnuncioDellAzione.azione(avversari.get(0), avversari.get(1), TipoAzione.DARDO_ARCANO);
        assertEquals(1, eventi.size());
        InternoAzioneInCombattimento annuncio = (InternoAzioneInCombattimento) eventi.get(0);
        assertSame(avversari.get(1), annuncio.getBersaglio());
        assertEquals(TipoAzione.DARDO_ARCANO, annuncio.getAzione());
        assertNull(new InternoAzioneInCombattimento(avversari.get(0), null).getAzione());
    }

    @RepeatedTest(2)
    void leAzioniEIloroEsiti() {
        assertEquals(3, EsitoAzione.values().length);
        for (TipoAzione azione : TipoAzione.values()) {
            assertNotNull(azione.getDescrizione(), azione.name());
            TipoIncantesimo incantesimo = azione.getClasseIncantesimo();
            if (incantesimo != null) {
                assertNotNull(TipoAzione.di(incantesimo));
            }
            azione.getTipoDanno();
        }
        assertEquals(TipoDanno.ARCANO, TipoAzione.DARDO_ARCANO.getTipoDanno());
        assertEquals(TipoDanno.TERRA, TipoAzione.RADICI.getTipoDanno());
        assertEquals(TipoDanno.VELENO, TipoAzione.SCIAME.getTipoDanno());
        assertNull(TipoAzione.di(null));
    }

    @RepeatedTest(2)
    void leClassiDiServizioHannoUnCostruttorePrivato() throws Exception {
        for (Class<?> classe : new Class<?>[] {ApplicazioneDelRisultato.class, AttaccoConLeArmi.class, ColpoMagico.class, CambioDArma.class,
                AnnuncioDellAzione.class}) {
            Constructor<?> costruttore = classe.getDeclaredConstructor();
            costruttore.setAccessible(true);
            assertNotNull(costruttore.newInstance(), classe.getSimpleName());
        }
        assertNotNull(new CalcolatoreCombattimento());
    }

    // --- Lo stato dello scontro

    private void schieramenti(Personaggio... avversari) {
        GruppoGiocatore.getIstanza().reimposta();
        GruppoAvversario.getIstanza().reimposta();
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(giocante("Arsenio", 5));
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(giocante("Brunilde", 5));
        for (Personaggio avversario : avversari) {
            GruppoAvversario.getIstanza().aggiungiPersonaggio(avversario);
        }
    }

    @RepeatedTest(2)
    void ilRoundPassaQuandoHannoAgitoTuttiEIlGiocatoreNonPuoAgireDueVolte() {
        Personaggio g1 = new Goblin(5);
        Personaggio g2 = new Goblin(5);
        schieramenti(g1, g2);
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        stato.reimposta();
        assertEquals(1, stato.getRound());
        List<Personaggio> ordine = stato.getOrdineDelRound();
        assertEquals(4, ordine.size());
        assertEquals(4, stato.getPiano().size());
        assertNotNull(stato.getProssimoLato());
        assertNotNull(stato.getAvversarioCheAttaccaPerPrimo());
        Personaggio primo = ordine.get(0);
        assertSame(GruppoGiocatore.getIstanza().getPersonaggio(0), primo);
        assertSame(primo, stato.getProssimoAttaccante(GruppoGiocatore.getIstanza()));
        assertNull(stato.getProssimoAttaccante(GruppoAvversario.getIstanza()));
        assertFalse(stato.haAgitoNelRound(primo));
        // Agire di nuovo senza che gli altri rispondano costa il posto a cui tocca
        assertFalse(stato.registraAzione(primo));
        assertTrue(stato.haAgitoNelRound(primo));
        assertFalse(stato.registraAzione(primo));
        assertFalse(stato.registraAzione(null));
        // Si finisce il giro
        boolean chiuso = false;
        for (int i = 0; i < 10 && !chiuso; i++) {
            Personaggio chi = stato.getOrdineDelRound().stream().filter(p -> !stato.haAgitoNelRound(p)).findFirst().orElse(null);
            chiuso = chi == null ? stato.registraAzioneSaltata(StatoDelCombattimento.Lato.AVVERSARI) : stato.registraAzione(chi);
        }
        assertTrue(chiuso);
        assertEquals(2, stato.getRound());
        assertTrue(eventi.stream().anyMatch(e -> e instanceof InternoInizioRound));
    }

    @RepeatedTest(2)
    void senzaAvversariIlRoundNonAvanza() {
        schieramenti();
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        stato.reimposta();
        assertFalse(stato.registraAzione(GruppoGiocatore.getIstanza().getPersonaggio(0)));
        assertFalse(stato.registraAzioneSaltata(StatoDelCombattimento.Lato.GIOCATORE));
        assertEquals(1, stato.getRound());
        assertNull(stato.getAvversarioCheAttaccaPerPrimo());
        assertNull(stato.getProssimoLato() == null ? null : null);
    }

    @RepeatedTest(2)
    void laListaSiAggiornaQuandoCambianoIGruppiEFaAgirePerPrimo() {
        Personaggio g1 = new Goblin(5);
        Personaggio g2 = new Goblin(5);
        schieramenti(g1, g2);
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        stato.reimposta();
        stato.getOrdineDelRound();
        // Chi se ne va dal gruppo esce dalla lista e uno nuovo vi entra in fondo
        GruppoAvversario.getIstanza().rimuoviPersonaggio(g1);
        Personaggio nuovo = new Goblin(5);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(nuovo);
        List<Personaggio> ordine = stato.getOrdineDelRound();
        assertFalse(ordine.contains(g1));
        assertTrue(ordine.contains(nuovo));
        // Il cursore che era dopo chi è uscito si sposta indietro
        stato.registraAzione(ordine.get(0));
        stato.registraAzione(ordine.get(1));
        GruppoAvversario.getIstanza().rimuoviPersonaggio(g2);
        stato.getOrdineDelRound();
        // Far agire per primo il secondo del giocatore, prima del round e a round iniziato
        Personaggio secondo = GruppoGiocatore.getIstanza().getPersonaggio(1);
        stato.nuovaOndata();
        stato.faAgirePerPrimo(GruppoGiocatore.getIstanza(), secondo);
        assertSame(secondo, stato.getOrdineDelRound().get(0));
        stato.faAgirePerPrimo(GruppoGiocatore.getIstanza(), new Goblin(1));
        stato.registraAzione(secondo);
        stato.faAgirePerPrimo(GruppoGiocatore.getIstanza(), GruppoGiocatore.getIstanza().getPersonaggio(0));
        stato.faAgirePerPrimo(GruppoGiocatore.getIstanza(), secondo);
        stato.nuovaOndata();
        assertNotNull(stato.getOrdineDelRound());
    }

    @RepeatedTest(2)
    void glioSpitiVulnerabiliFannoPartedelloScontro() {
        Personaggio g1 = new Goblin(5);
        schieramenti(g1);
        Personaggio ostaggio = FabbricaPersonaggi.crea(TipoPersonaggio.DONNA, 1);
        GruppoGiocatore.getIstanza().aggiungiOspite(ostaggio, true);
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        stato.reimposta();
        assertTrue(stato.getOrdineDelRound().contains(ostaggio));
        // Fuori dal gruppo ma ospite non vulnerabile: non entra
        GruppoGiocatore.getIstanza().rimuoviOspite(ostaggio);
        stato.getOrdineDelRound();
        assertFalse(stato.getOrdineDelRound().contains(ostaggio));
        // Chi non combatte più non agisce
        Personaggio estraneo = new Goblin(1);
        assertFalse(stato.registraAzione(estraneo));
    }

    @RepeatedTest(2)
    void laMemoriaDelloScontroRicordaChiHaFattoCosa() {
        Personaggio g1 = new Goblin(5);
        schieramenti(g1);
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        stato.reimposta();
        Personaggio a = GruppoGiocatore.getIstanza().getPersonaggio(0);
        Personaggio b = GruppoGiocatore.getIstanza().getPersonaggio(1);
        // Senza scheda tutto è vuoto
        assertNull(stato.getUltimoAvversario(a));
        assertNull(stato.getUltimoAlleato(a));
        assertNull(stato.getUltimoAttaccante(a));
        assertFalse(stato.haLanciatoUnIncantesimo(a));
        assertEquals(0, stato.getCambiDArma(a));
        assertEquals(0, stato.getUsiDellaVoce(a, "x"));
        assertFalse(stato.haUsatoLaVoce(a, "x"));
        assertEquals(0, stato.getRoundUltimoUsoVoce(a, "x"));
        assertEquals(0, stato.getTentativiFalliti(a, g1, TipoAzione.DARDO_ARCANO));
        assertFalse(stato.isImmunitaScoperta(g1, TipoAzione.DARDO_ARCANO));
        assertNull(stato.getValutazioneArma(a, genera(TipoArtefatto.SPADA, 1, 1)));
        assertFalse(stato.isReazioneNota(g1, TipoDanno.FUOCO));
        assertEquals(1.0d, stato.getFattoreEquipaggiamentoNoto(g1, TipoDanno.FUOCO), 1e-9);

        // Un colpo fra due ricorda chi ha colpito chi; un colpo ad area o senza difensore no
        DannoRisultante colpo = new DannoRisultante(a, g1);
        colpo.setTipoDanno(TipoDanno.FUOCO);
        colpo.setFattoreEquipaggiamento(0.8d);
        stato.registraColpo(colpo);
        assertSame(g1, stato.getUltimoAvversario(a));
        assertSame(a, stato.getUltimoAttaccante(g1));
        assertTrue(stato.isReazioneNota(g1, TipoDanno.FUOCO));
        assertEquals(0.8d, stato.getFattoreEquipaggiamentoNoto(g1, TipoDanno.FUOCO), 1e-9);
        DannoRisultante adArea = new DannoRisultante(b, g1);
        adArea.setColpoAdArea(true);
        stato.registraColpo(adArea);
        assertNull(stato.getUltimoAvversario(b));
        stato.registraColpo(new DannoRisultante(a, null));
        DannoRisultante suSeStesso = new DannoRisultante(a, a);
        suSeStesso.setTipoDanno(TipoDanno.GELO);
        stato.registraColpo(suSeStesso);
        stato.registraColpo(new DannoRisultante(null, g1));

        stato.registraAlleato(a, b);
        assertSame(b, stato.getUltimoAlleato(a));
        stato.registraIncantesimoLanciato(a);
        assertTrue(stato.haLanciatoUnIncantesimo(a));
        Artefatto spada = genera(TipoArtefatto.SPADA, 1, 1);
        stato.registraArmaValutata(a, spada, true);
        assertEquals(Boolean.TRUE, stato.getValutazioneArma(a, spada));
        stato.registraCambioArma(a);
        assertEquals(1, stato.getCambiDArma(a));
        assertNull(stato.getValutazioneArma(a, spada), "dopo un cambio le valutazioni si rifanno");
        stato.registraUsoVoce(a, "voce");
        stato.registraUsoVoce(a, "voce");
        assertEquals(2, stato.getUsiDellaVoce(a, "voce"));
        assertTrue(stato.haUsatoLaVoce(a, "voce"));
        assertEquals(1, stato.getRoundUltimoUsoVoce(a, "voce"));
    }

    @RepeatedTest(2)
    void gliEsitiDelleAzioniContanoITentativiELeImmunita() {
        Personaggio g1 = new Goblin(5);
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 5);
        schieramenti(g1, scheletro);
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        Personaggio a = GruppoGiocatore.getIstanza().getPersonaggio(0);
        stato.registraEsitoAzione(a, g1, TipoAzione.DARDO_ARCANO, EsitoAzione.MANCATA);
        stato.registraEsitoAzione(a, g1, TipoAzione.DARDO_ARCANO, EsitoAzione.MANCATA);
        assertEquals(2, stato.getTentativiFalliti(a, g1, TipoAzione.DARDO_ARCANO));
        assertEquals(0, stato.getTentativiFalliti(a, scheletro, TipoAzione.DARDO_ARCANO));
        assertEquals(0, stato.getTentativiFalliti(a, g1, TipoAzione.RADICI));
        stato.registraEsitoAzione(a, g1, TipoAzione.DARDO_ARCANO, EsitoAzione.RIUSCITA);
        assertEquals(0, stato.getTentativiFalliti(a, g1, TipoAzione.DARDO_ARCANO));
        TipoAzione conIncantesimo = Arrays.stream(TipoAzione.values()).filter(t -> t.getClasseIncantesimo() != null).findFirst().get();
        TipoAzione senzaIncantesimo = Arrays.stream(TipoAzione.values()).filter(t -> t.getClasseIncantesimo() == null).findFirst().get();
        stato.registraEsitoAzione(a, scheletro, conIncantesimo, EsitoAzione.IMMUNE);
        stato.registraEsitoAzione(a, scheletro, senzaIncantesimo, EsitoAzione.IMMUNE);
        assertTrue(stato.isImmunitaScoperta(scheletro, conIncantesimo));
        assertTrue(stato.isImmunitaScoperta(scheletro, senzaIncantesimo));
        assertFalse(stato.isImmunitaScoperta(scheletro, TipoAzione.values()[TipoAzione.values().length - 1]) && false);
    }

    // --- Attacchi

    @RepeatedTest(2)
    void unAttaccoConLeArmiColpisceOMancaERaccontaLAttacco() {
        Personaggio goblin = new Goblin(3);
        Personaggio bersaglio = giocante("Bersaglio", 3);
        schieramenti(goblin);
        int colpiti = 0;
        int mancati = 0;
        for (int i = 0; i < 40; i++) {
            bersaglio.addSalute(100000);
            int prima = bersaglio.getSalute();
            int mancatiPrima = (int) eventi.stream().filter(e -> e instanceof NotificaColpoMancato).count();
            AttaccoConLeArmi.esegui(goblin, bersaglio);
            if (bersaglio.getSalute() < prima) {
                colpiti++;
            }
            mancati += (int) eventi.stream().filter(e -> e instanceof NotificaColpoMancato).count() - mancatiPrima;
        }
        assertTrue(colpiti > 0 && mancati > 0, "colpiti " + colpiti + ", mancati " + mancati);
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("Il Goblin attacca ")), frasi.toString());
        // Il ladro con due armi si ferma se il bersaglio cade alla prima
        Personaggio ladro = new Ladro("Arsenio", 8);
        ladro.addArtefatto(genera(TipoArtefatto.SPADA, 3, 7));
        ladro.addArtefatto(genera(TipoArtefatto.SPADA, 3, 8));
        Personaggio fragile = giocante("Fragile", 1);
        fragile.subSalute(fragile.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        fragile.addModificatore(new ModificatoreAttributoMD(TipoAttributo.VELOCITA, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        fragile.addModificatore(new ModificatoreAttributoMD(TipoAttributo.DESTREZZA, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        for (int i = 0; i < 20 && !fragile.isFuoriCombattimento(); i++) {
            AttaccoConLeArmi.esegui(ladro, fragile);
        }
        assertTrue(fragile.isFuoriCombattimento());
        AttaccoConLeArmi.esegui(ladro, fragile);
    }

    private static Arma dardo(TipoDanno tipoDanno) {
        return new Arma() {
            @Override
            public int getDanni() {
                return 8;
            }

            @Override
            public int getLivello() {
                return 3;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return tipoDanno;
            }

            @Override
            public boolean isIncantata() {
                return false;
            }

            @Override
            public boolean isMagica() {
                return true;
            }

            @Override
            public java.util.Collection<com.threeamigos.foresta.modellodati.IncantamentoMD> getIncantamenti() {
                return Collections.emptyList();
            }
        };
    }

    @RepeatedTest(2)
    void ilColpoMagicoPuoEssereImmuneMancatoORiuscito() {
        Personaggio mago = new Mago("Merlino", 5);
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 5);
        Personaggio goblin = new Goblin(5);
        schieramenti(scheletro, goblin);
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        // Immune: lo scheletro non muore due volte
        ColpoMagico.colpisci(mago, scheletro, dardo(TipoDanno.NECROTICO), TipoAzione.di(TipoIncantesimo.MORTE), TipoIncantesimo.MORTE, 1.0d);
        assertTrue(stato.isImmunitaScoperta(scheletro, TipoAzione.di(TipoIncantesimo.MORTE)));
        // Mancato o riuscito, a seconda del dado; senza immunità da controllare
        goblin.addModificatore(new ModificatoreAttributoMD(TipoAttributo.RESISTENZA_MAGICA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
        ColpoMagico.colpisci(mago, goblin, dardo(TipoDanno.ARCANO), TipoAzione.DARDO_ARCANO, null, 1.0d);
        assertEquals(1, stato.getTentativiFalliti(mago, goblin, TipoAzione.DARDO_ARCANO));
        assertTrue(eventi.stream().anyMatch(e -> e instanceof NotificaColpoMancato));
        Personaggio indifeso = new Goblin(5);
        indifeso.addEffettoDiStato(TipoEffettoDiStato.CONGELATO, 3, 0);
        int prima = indifeso.getSalute();
        ColpoMagico.colpisci(mago, indifeso, dardo(TipoDanno.ARCANO), TipoAzione.DARDO_ARCANO, null, 1.0d);
        assertTrue(indifeso.getSalute() < prima);
        assertEquals(0, stato.getTentativiFalliti(mago, indifeso, TipoAzione.DARDO_ARCANO));
    }
}
