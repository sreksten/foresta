package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoScheletroRisvegliato;
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
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quel che succede a chi riceve il risultato di un colpo: salute, stati, modificatori, notifiche, cure e colpi ad area, sifone vitale, diluizione
 * ematica, collasso entropico e mietitura.
 */
class ApplicazioneDelRisultatoTest {

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
        Dado.impostaSeme(99L);
        for (Class<?> tipo : new Class<?>[] {NotificaInterazionePersonaggio.class, NotificaReazioneDiClassePersonaggio.class,
                InternoScheletroRisvegliato.class}) {
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

    @RepeatedTest(2)
    void ilRisultatoTogliSaluteMettePiuStatiTienePerOgniTipoQuelloChedDuraDiPiuEAvvisa() {
        Personaggio attaccante = new Mago("Merlino", 5);
        Personaggio bersaglio = giocante("Arsenio", 5);
        bersaglio.addSalute(100000);
        bersaglio.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 2, 1);
        bersaglio.addEffettoDiStato(TipoEffettoDiStato.RALLENTATO, 2, 0);
        int prima = bersaglio.getSalute();
        DannoRisultante risultato = new DannoRisultante(attaccante, bersaglio);
        risultato.setTipoDanno(TipoDanno.FUOCO);
        risultato.setDanno(7);
        risultato.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 1, 0);
        risultato.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        risultato.addEffettoDiStato(TipoEffettoDiStato.ACCECATO, 3, 0);
        risultato.addEffettoDiStato(TipoEffettoDiStato.ACCECATO, 2, 0);
        risultato.addModificatoreDaAggiungere(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0, "colpo",
                com.threeamigos.foresta.tipi.UnitaDurataModificatore.TURNI, 2, com.threeamigos.foresta.tipi.BersaglioModificatore.PORTATORE, true));
        risultato.rimuoviEffettoDiStato(TipoEffettoDiStato.RALLENTATO);
        risultato.addInterazione(TipoInterazioneConEffettiDiStato.VAPORIZZAZIONE);
        risultato.addReazioneDiClasse(TipoDanno.FUOCO, 2.0d);
        ApplicazioneDelRisultato.applica(bersaglio, risultato);
        assertEquals(prima - 7, bersaglio.getSalute());
        assertEquals(3, bersaglio.getQuantitaEffettoDiStato(TipoEffettoDiStato.CONFUSO));
        assertEquals(3, bersaglio.getQuantitaEffettoDiStato(TipoEffettoDiStato.ACCECATO));
        assertFalse(bersaglio.hasEffettoDiStato(TipoEffettoDiStato.RALLENTATO));
        assertEquals(1, bersaglio.getModificatoriTemporanei().size());
        assertTrue(eventi.stream().anyMatch(e -> e instanceof NotificaInterazionePersonaggio));
        assertTrue(eventi.stream().anyMatch(e -> e instanceof NotificaReazioneDiClassePersonaggio));
    }

    @RepeatedTest(2)
    void laCuraAdAreaVaAgliAlleatiDelBersaglioNelSuoGruppo() {
        Personaggio a = giocante("Arsenio", 5);
        Personaggio b = giocante("Brunilde", 5);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(a);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(b);
        b.subSalute(b.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int saluteB = b.getSalute();
        DannoRisultante contro = new DannoRisultante(new Goblin(5), a);
        contro.setDanno(1);
        contro.setCuraAdArea(10);
        ApplicazioneDelRisultato.applica(a, contro);
        assertEquals(saluteB + 10, b.getSalute());
        // E fra gli avversari
        Personaggio g1 = new Goblin(5);
        Personaggio g2 = new Goblin(5);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(g1);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(g2);
        g2.subSalute(g2.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int saluteG2 = g2.getSalute();
        DannoRisultante controAvversari = new DannoRisultante(a, g1);
        controAvversari.setDanno(1);
        controAvversari.setCuraAdArea(5);
        ApplicazioneDelRisultato.applica(g1, controAvversari);
        assertEquals(saluteG2 + 5, g2.getSalute());
    }

    @RepeatedTest(2)
    void iColpiAdAreaInvestonoIVicini() {
        List<Personaggio> avversari = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Personaggio g = new Goblin(5);
            GruppoAvversario.getIstanza().aggiungiPersonaggio(g);
            avversari.add(g);
        }
        Personaggio attaccante = new Mago("Merlino", 5);
        DannoRisultante risultato = new DannoRisultante(attaccante, avversari.get(1));
        risultato.setDanno(1);
        risultato.addDannoAdArea(new DannoRisultante.DannoAdArea(TipoInterazioneConEffettiDiStato.RIGETTO, 60.0d));
        int s0 = avversari.get(0).getSalute();
        int s2 = avversari.get(2).getSalute();
        ApplicazioneDelRisultato.applica(avversari.get(1), risultato);
        assertTrue(avversari.get(0).getSalute() < s0);
        assertTrue(avversari.get(2).getSalute() < s2);
        assertTrue(frasi.stream().anyMatch(f -> f.endsWith(".")));
        // Anche contro il gruppo del giocatore
        Personaggio a = giocante("Arsenio", 5);
        Personaggio b = giocante("Brunilde", 5);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(a);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(b);
        DannoRisultante controGiocatore = new DannoRisultante(avversari.get(0), a);
        controGiocatore.setDanno(1);
        controGiocatore.addDannoAdArea(new DannoRisultante.DannoAdArea(TipoInterazioneConEffettiDiStato.ESPLOSIONE_DI_GAS, 60.0d));
        int sB = b.getSalute();
        ApplicazioneDelRisultato.applica(a, controGiocatore);
        assertTrue(b.getSalute() < sB);
        controGiocatore.togliDanniAdArea();
        assertTrue(controGiocatore.getDanniAdArea().isEmpty());
    }

    @RepeatedTest(2)
    void ilSifoneVitaleRiempieLaMagiaEPoiLaSalute() {
        Personaggio mago = new Mago("Merlino", 5);
        Personaggio bersaglio = new Goblin(5);
        mago.subMagia(mago.getMagia() / 2);
        int mancante = mago.getMagiaMassima() - mago.getMagia();
        mago.subSalute(10, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int salute = mago.getSalute();
        DannoRisultante solaMagia = new DannoRisultante(mago, bersaglio);
        solaMagia.setDanno(1);
        solaMagia.setSifoneVitale(Math.max(1, mancante - 1));
        ApplicazioneDelRisultato.applica(bersaglio, solaMagia);
        assertEquals(salute, mago.getSalute());
        // Oltre il massimo di magia va in salute
        DannoRisultante conEccedenza = new DannoRisultante(mago, bersaglio);
        conEccedenza.setDanno(1);
        conEccedenza.setSifoneVitale(10000);
        ApplicazioneDelRisultato.applica(bersaglio, conEccedenza);
        assertEquals(mago.getMagiaMassima(), mago.getMagia());
        assertTrue(mago.getSalute() > salute);
        // Con la magia già piena è tutta salute
        mago.subSalute(mago.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        DannoRisultante tuttaSalute = new DannoRisultante(mago, bersaglio);
        tuttaSalute.setDanno(1);
        tuttaSalute.setSifoneVitale(5);
        ApplicazioneDelRisultato.applica(bersaglio, tuttaSalute);
        assertEquals(6, mago.getSalute());
    }

    @RepeatedTest(2)
    void laDiluizioneEmaticaDimezzaIlSanguinamento() {
        Personaggio bersaglio = giocante("Arsenio", 5);
        DannoRisultante senzaSangue = new DannoRisultante(new Goblin(5), bersaglio);
        senzaSangue.setDanno(1);
        senzaSangue.setDiluizioneEmaticaAttiva(true);
        ApplicazioneDelRisultato.applica(bersaglio, senzaSangue);
        bersaglio.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        bersaglio.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 3, 8);
        DannoRisultante risultato = new DannoRisultante(new Goblin(5), bersaglio);
        risultato.setDanno(1);
        risultato.setDiluizioneEmaticaAttiva(true);
        ApplicazioneDelRisultato.applica(bersaglio, risultato);
        assertEquals(4, bersaglio.getEffettiDiStato().stream().filter(e -> e.getTipoEffettoDiStato() == TipoEffettoDiStato.SANGUINAMENTO).findFirst().get().getDanniNelTempo());
    }

    @RepeatedTest(2)
    void ilCollassoEntropicoBruciaMagiaESalute() {
        Personaggio mago = new Mago("Merlino", 5);
        mago.getModelloDati().setPng(false);
        mago.addMagia(100000);
        int magia = mago.getMagia();
        int salute = mago.getSalute();
        DannoRisultante risultato = new DannoRisultante(new Goblin(5), mago);
        risultato.setDanno(1);
        risultato.setCollassoEntropicoAttivo(true);
        ApplicazioneDelRisultato.applica(mago, risultato);
        assertTrue(mago.getMagia() < magia);
        assertTrue(mago.getSalute() < salute);
        // Senza magia da bruciare non succede altro
        Personaggio guerriero = giocante("Arsenio", 5);
        guerriero.getModelloDati().setMagia(0);
        int saluteG = guerriero.getSalute();
        DannoRisultante senzaMagia = new DannoRisultante(new Goblin(5), guerriero);
        senzaMagia.setDanno(1);
        senzaMagia.setCollassoEntropicoAttivo(true);
        ApplicazioneDelRisultato.applica(guerriero, senzaMagia);
        assertEquals(saluteG - 1, guerriero.getSalute());
    }

    private Personaggio servitoreQuasiMorto() {
        Personaggio goblin = new Goblin(5);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(goblin);
        goblin.getModelloDati().setSalute(1);
        return goblin;
    }

    @RepeatedTest(2)
    void laMietituraRisvegliaUnMinionQuasiMortoComeScheletroSeCePosto() {
        Personaggio goblin = servitoreQuasiMorto();
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(giocante("Arsenio", 5));
        DannoRisultante risultato = new DannoRisultante(GruppoGiocatore.getIstanza().getPersonaggio(0), goblin);
        risultato.setDanno(0);
        risultato.setMietituraAttiva(true);
        ApplicazioneDelRisultato.applica(goblin, risultato);
        assertFalse(goblin.isVivo());
        assertFalse(GruppoAvversario.getIstanza().contiene(goblin));
        assertEquals(2, GruppoGiocatore.getIstanza().getNumeroPersonaggi());
        assertEquals(TipoMotivoNelGruppo.MIETITURA, GruppoGiocatore.getIstanza().getPersonaggio(1).getMotivoNelGruppo());
        assertTrue(eventi.stream().anyMatch(e -> e instanceof InternoScheletroRisvegliato));
    }

    @RepeatedTest(2)
    void agruppoPienoIlMinionMuoreComunqueMaNonSiRisveglia() {
        Personaggio goblin = servitoreQuasiMorto();
        for (int i = 0; i < CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE; i++) {
            GruppoGiocatore.getIstanza().aggiungiPersonaggioSenzaNotificare(giocante("Eroe" + i, 1));
        }
        DannoRisultante risultato = new DannoRisultante(GruppoGiocatore.getIstanza().getPersonaggio(0), goblin);
        risultato.setDanno(0);
        risultato.setMietituraAttiva(true);
        ApplicazioneDelRisultato.applica(goblin, risultato);
        assertFalse(goblin.isVivo());
        assertEquals(CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE, GruppoGiocatore.getIstanza().getNumeroPersonaggi());
        assertTrue(eventi.stream().noneMatch(e -> e instanceof InternoScheletroRisvegliato));
    }

    @RepeatedTest(2)
    void laMietituraNonTocca_ibossNeChiEBenInSalute() {
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(giocante("Arsenio", 5));
        Personaggio drago = FabbricaPersonaggi.crea(TipoPersonaggio.DRAGO, 5);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(drago);
        DannoRisultante contro = new DannoRisultante(GruppoGiocatore.getIstanza().getPersonaggio(0), drago);
        contro.setDanno(1);
        contro.setMietituraAttiva(true);
        ApplicazioneDelRisultato.applica(drago, contro);
        assertTrue(drago.isVivo());
        Personaggio sano = new Goblin(5);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(sano);
        DannoRisultante controSano = new DannoRisultante(GruppoGiocatore.getIstanza().getPersonaggio(0), sano);
        controSano.setDanno(1);
        controSano.setMietituraAttiva(true);
        ApplicazioneDelRisultato.applica(sano, controSano);
        assertTrue(sano.isVivo());
        // Un bersaglio fuori dal gruppo degli avversari non è un servitore
        Personaggio estraneo = new Goblin(5);
        estraneo.getModelloDati().setSalute(1);
        DannoRisultante controEstraneo = new DannoRisultante(GruppoGiocatore.getIstanza().getPersonaggio(0), estraneo);
        controEstraneo.setDanno(0);
        controEstraneo.setMietituraAttiva(true);
        ApplicazioneDelRisultato.applica(estraneo, controEstraneo);
        assertTrue(estraneo.isVivo());
        // Già morto di suo non muore due volte
        Personaggio morto = servitoreQuasiMorto();
        morto.muore("prova");
        DannoRisultante controMorto = new DannoRisultante(GruppoGiocatore.getIstanza().getPersonaggio(0), morto);
        controMorto.setDanno(0);
        controMorto.setMietituraAttiva(true);
        ApplicazioneDelRisultato.applica(morto, controMorto);
        assertFalse(GruppoAvversario.getIstanza().contiene(morto));
    }
}
