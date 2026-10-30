package com.threeamigos.foresta.gruppi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPrelievoArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoPuntiEsperienzaAssegnati;
import com.threeamigos.foresta.eventi.interni.InternoStoccaggioArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazionePrelievoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneStoccaggioArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoPrelievoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il gruppo del giocatore: chi ne fa parte e chi lo accompagna soltanto, come si salva e si ricostruisce, la fuga, il riposo, l'esperienza,
 * l'inventario e gli spostamenti di artefatti fra il gruppo e i personaggi.
 */
class GruppoGiocatoreTest {

    private final List<String> frasi = new ArrayList<>();
    private final List<Object> eventi = new ArrayList<>();
    private final List<Runnable> iscrizioni = new ArrayList<>();

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        // Altri test creano gruppi con new e li lasciano iscritti: gestirebbero anch'essi gli eventi
        BusEventi.azzera();
        DatiDiProva.nuovaPartita();
        iscrizioni.add(BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio())));
        iscrizioni.add(BusEventi.iscriviti(InternoPuntiEsperienzaAssegnati.class, eventi::add));
        iscrizioni.add(BusEventi.iscriviti(NotificaApprovazionePrelievoArtefatto.class, eventi::add));
        iscrizioni.add(BusEventi.iscriviti(NotificaRifiutoPrelievoArtefatto.class, eventi::add));
        iscrizioni.add(BusEventi.iscriviti(NotificaApprovazioneStoccaggioArtefatto.class, eventi::add));
    }

    @AfterEach
    void ripulisce() {
        iscrizioni.forEach(Runnable::run);
        BusEventi.impostaConsegna(null);
        DatiDiProva.nuovaPartita();
    }

    private static Artefatto spada() {
        return new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.SPADA, 1);
    }

    private static Personaggio guerriero(String nome) {
        return new Guerriero(nome, 3);
    }

    // --- Istanze e dati

    @Test
    void sensaIDatiDelGruppoNonSiCreaEL_istanzaSiDimenticaDaSola() {
        GruppoGiocatore.setFornitoreDiDati(null);
        GruppoGiocatore.azzeraIstanza();
        assertThrows(IllegalStateException.class, GruppoGiocatore::getIstanza);
        DatiDiProva.nuovaPartita();
        GruppoGiocatore primo = GruppoGiocatore.getIstanza();
        assertSame(primo, GruppoGiocatore.getIstanza());
        GruppoGiocatore.azzeraIstanza();
        GruppoGiocatore.azzeraIstanza();
        assertNotSame(primo, GruppoGiocatore.getIstanza());
    }

    @Test
    void ilGruppoSiRicostruisceDaiSuoiDatiConCapoEOspiti() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio capo = guerriero("Arsenio");
        Personaggio mago = new Mago("Merlino", 3);
        Personaggio ospite = FabbricaPersonaggi.crea(TipoPersonaggio.UOMO, 1);
        gruppo.aggiungiPersonaggio(capo);
        gruppo.aggiungiPersonaggio(mago);
        gruppo.aggiungiOspite(ospite, true);
        assertTrue(gruppo.spostaPersonaggio(capo, 1));
        GruppoGiocatoreMD dati = gruppo.getGruppoMD() instanceof GruppoGiocatoreMD ? (GruppoGiocatoreMD) gruppo.getGruppoMD() : null;
        assertNotNull(dati);

        GruppoGiocatore ricostruito = GruppoGiocatore.of(dati);
        assertEquals(2, ricostruito.getNumeroPersonaggi());
        assertEquals("Arsenio", ricostruito.getCapo().getNomeProprio().get());
        assertSame(ricostruito.getPersonaggio(1), ricostruito.getCapo());
        assertEquals(1, ricostruito.getOspiti().size());
        assertTrue(ricostruito.isOspiteVulnerabile(ricostruito.getOspiti().get(0)));
        // Un indice del capo che non c'è lascia il gruppo senza capo
        dati.setIndiceCapo(-1);
        ricostruito.setModelloDati(dati);
        assertSame(ricostruito.getPersonaggio(0), ricostruito.getCapo());
        dati.setIndiceCapo(7);
        ricostruito.setModelloDati(dati);
        assertSame(ricostruito.getPersonaggio(0), ricostruito.getCapo(), "senza un indice valido il capo è il primo");
    }

    // --- Membri e ospiti

    @Test
    void chiEntraPerRestareDiventaUnPersonaggioVeroEChiStaATempoNo() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio capo = guerriero("Arsenio");
        assertTrue(capo.isPNG());
        gruppo.aggiungiPersonaggio(capo);
        assertFalse(capo.isPNG());
        assertTrue(frasi.stream().anyMatch(f -> f.equals("Arsenio è felice di poter far parte del gruppo.")), frasi.toString());

        Personaggio riluttante = guerriero("Brunilde");
        riluttante.setMotivoNelGruppo(TipoMotivoNelGruppo.MIETITURA);
        gruppo.aggiungiPersonaggio(riluttante);
        assertTrue(frasi.stream().anyMatch(f -> f.equals("Brunilde entra a far parte del gruppo.")), frasi.toString());

        Personaggio aiuto = guerriero("Cesare");
        aiuto.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_GRATUITO);
        gruppo.aggiungiPersonaggio(aiuto);
        assertTrue(aiuto.isPNG());

        Personaggio senzaNome = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        gruppo.aggiungiPersonaggioSenzaNotificare(senzaNome);
        assertTrue(senzaNome.isPNG());
    }

    @Test
    void gliOspitiViaggianoSenzaFarneParteESoloIVulnerabiliSiColpiscono() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio ospite = FabbricaPersonaggi.crea(TipoPersonaggio.UOMO, 1);
        Personaggio ostaggio = FabbricaPersonaggi.crea(TipoPersonaggio.DONNA, 1);
        gruppo.aggiungiOspite(ospite);
        gruppo.aggiungiOspite(ostaggio, true);
        assertEquals(2, gruppo.getOspiti().size());
        assertFalse(gruppo.isOspiteVulnerabile(ospite));
        assertTrue(gruppo.isOspiteVulnerabile(ostaggio));
        assertEquals(1, gruppo.getOspitiBersaglio().size());
        assertTrue(frasi.stream().anyMatch(f -> f.endsWith(" viaggia con il gruppo.")), frasi.toString());
        // Visti dalla UI
        assertTrue(gruppo.isOspiteVulnerabile((com.threeamigos.foresta.interfacce.VistaPersonaggio) ostaggio));
        assertFalse(gruppo.isOspiteVulnerabile((com.threeamigos.foresta.interfacce.VistaPersonaggio) ospite));
        // Il vulnerabile morto non si bersaglia
        ostaggio.muore("prova");
        assertTrue(gruppo.getOspitiBersaglio().isEmpty());
        gruppo.rimuoviOspite(ostaggio);
        gruppo.rimuoviOspite(ospite);
        assertTrue(gruppo.getOspiti().isEmpty());
        assertTrue(frasi.stream().anyMatch(f -> f.endsWith(" si separa dal gruppo.")), frasi.toString());
    }

    @Test
    void spostareUnPersonaggioCambiaAncheLOrdineSalvatoEIlCapoRestaDovera() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio a = guerriero("Arsenio");
        Personaggio b = guerriero("Brunilde");
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiPersonaggio(b);
        assertFalse(gruppo.spostaPersonaggio(a, -1));
        assertTrue(gruppo.spostaPersonaggio(a, 1));
        assertSame(b, gruppo.getPersonaggio(0));
        assertEquals(1, gruppo.getGruppoMD().getIndiceCapo());
        assertSame(b.getModelloDati(), gruppo.getGruppoMD().getPersonaggiMD().get(0));
    }

    @Test
    void chiLasciaIlGruppoLoDiceELaMietituraRaccontaLaFineDelloScheletro() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio a = guerriero("Arsenio");
        Personaggio scheletroVivo = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        Personaggio scheletroCaduto = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        scheletroVivo.setMotivoNelGruppo(TipoMotivoNelGruppo.MIETITURA);
        scheletroCaduto.setMotivoNelGruppo(TipoMotivoNelGruppo.MIETITURA);
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiPersonaggio(scheletroVivo);
        gruppo.aggiungiPersonaggio(scheletroCaduto);
        frasi.clear();
        gruppo.rimuoviPersonaggio(scheletroVivo);
        assertTrue(frasi.get(0).contains("l'effetto della Mietitura è finito"), frasi.toString());
        scheletroCaduto.muore("prova");
        frasi.clear();
        gruppo.congedaChiNonRisorge();
        assertTrue(frasi.get(0).contains("torna ad essere polvere"), frasi.toString());
        // Chi è morto ma può essere risuscitato resta, e chi è vivo anche
        Personaggio caduto = guerriero("Cesare");
        gruppo.aggiungiPersonaggio(caduto);
        caduto.muore("prova");
        gruppo.congedaChiNonRisorge();
        assertTrue(gruppo.contiene(caduto));
        frasi.clear();
        gruppo.rimuoviPersonaggio(a);
        assertTrue(frasi.get(0).endsWith("lascia il gruppo."), frasi.toString());
        assertFalse(gruppo.contiene(a));
    }

    @Test
    void trascorrereUnTurnoFaScorrereGliEffettiAnchePerGliOspitiVulnerabili() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio a = guerriero("Arsenio");
        Personaggio ostaggio = FabbricaPersonaggi.crea(TipoPersonaggio.DONNA, 1);
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiOspite(ostaggio, true);
        a.addEffettoDiStato(com.threeamigos.foresta.tipi.TipoEffettoDiStato.CONFUSO, 2, 0);
        ostaggio.addEffettoDiStato(com.threeamigos.foresta.tipi.TipoEffettoDiStato.CONFUSO, 2, 0);
        gruppo.trascorriUnTurno();
        assertEquals(1, a.getQuantitaEffettoDiStato(com.threeamigos.foresta.tipi.TipoEffettoDiStato.CONFUSO));
        assertEquals(1, ostaggio.getQuantitaEffettoDiStato(com.threeamigos.foresta.tipi.TipoEffettoDiStato.CONFUSO));
    }

    // --- Dati vari

    @Test
    void leSfideVinteIlSacerdoteAccoltoEIlFormulanteSiRicordano() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        assertEquals(0, gruppo.getSfideVinte());
        gruppo.addSfidaVinta();
        gruppo.addSfidaVinta();
        assertEquals(2, gruppo.getSfideVinte());
        assertFalse(gruppo.isSacerdoteAccolto());
        gruppo.setSacerdoteAccolto(true);
        assertTrue(gruppo.isSacerdoteAccolto());
        Personaggio a = guerriero("Arsenio");
        gruppo.setFormulante(a);
        assertSame(a, gruppo.getFormulante());
    }

    @Test
    void reimpostareDaLeScorteDiPartenza() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(guerriero("Arsenio"));
        gruppo.aggiungiOspite(FabbricaPersonaggi.crea(TipoPersonaggio.UOMO, 1));
        gruppo.reimposta();
        assertEquals(0, gruppo.getNumeroPersonaggi());
        assertTrue(gruppo.getOspiti().isEmpty());
        assertEquals(3, gruppo.getIncantesimi(TipoIncantesimo.ARIA));
        assertEquals(CostantiPersonaggi.POZIONI_SALUTE_INIZIALI, gruppo.getPozioniSalute());
        assertEquals(0, gruppo.getPozioniMagia());
    }

    @Test
    void lAcontrattazioneEQuellaDiChiTrattaMeglioFraIVivi() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        assertEquals(0, gruppo.getContrattazione());
        Personaggio a = guerriero("Arsenio");
        Personaggio b = new Mago("Merlino", 3);
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiPersonaggio(b);
        assertEquals(Math.max(a.get(TipoAttributo.CONTRATTAZIONE), b.get(TipoAttributo.CONTRATTAZIONE)), gruppo.getContrattazione());
    }

    // --- Esperienza, riposo e fuga

    @Test
    void l_esperienzaVaATuttiIViviEAlleStatistiche() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio a = guerriero("Arsenio");
        Personaggio b = guerriero("Brunilde");
        a.getModelloDati().setPng(false);
        b.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiPersonaggio(b);
        b.muore("prova");
        gruppo.addPuntiEsperienza(10);
        assertEquals(10, a.getEsperienza());
        assertEquals(0, b.getEsperienza());
        assertEquals(1, eventi.size());
    }

    @Test
    void riposandoRecuperanoIViviEGliOspitiVulnerabili() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio a = guerriero("Arsenio");
        Personaggio ostaggio = FabbricaPersonaggi.crea(TipoPersonaggio.DONNA, 1);
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiOspite(ostaggio, true);
        a.subSalute(a.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        ostaggio.subSalute(ostaggio.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int salutaA = a.getSalute();
        int salutaOstaggio = ostaggio.getSalute();
        gruppo.riposa(8, TipoRiposo.AL_COPERTO);
        assertTrue(a.getSalute() > salutaA);
        assertTrue(ostaggio.getSalute() > salutaOstaggio);
    }

    @Test
    void fuggendoSiPerdeUnaPartedelleScorteEISiFerisce() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio a = guerriero("Arsenio");
        gruppo.aggiungiPersonaggio(a);
        gruppo.addPozioniSalute(100);
        gruppo.addPozioniMagia(100);
        gruppo.addIncantesimi(TipoIncantesimo.FUOCO, 100);
        int massima = a.getSaluteMassima();
        gruppo.fugge();
        assertTrue(gruppo.getPozioniSalute() < 100 + CostantiPersonaggi.POZIONI_SALUTE_INIZIALI);
        assertEquals(100 - 100 * CostantiPersonaggi.FUGA_PERDITA_CONSUMABILI_PERCENTUALE / 100, gruppo.getPozioniMagia());
        assertEquals(100 - 100 * CostantiPersonaggi.FUGA_PERDITA_CONSUMABILI_PERCENTUALE / 100, gruppo.getIncantesimi(TipoIncantesimo.FUOCO));
        assertTrue(a.getSalute() < massima);
        assertTrue(frasi.stream().anyMatch(f -> f.contains("cerca la salvezza nella fuga")), frasi.toString());
    }

    // --- Inventario del gruppo

    @Test
    void l_inventarioDelGruppoNonHaNienteDiEquipaggiato() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Artefatto spada = spada();
        gruppo.addArtefatto(spada);
        assertEquals(1, gruppo.getInventario().size());
        assertNull(spada.getModelloDati().getSlotEquipaggiamento());
        gruppo.removeArtefatto(spada);
        assertTrue(gruppo.getInventario().isEmpty());
    }

    @Test
    void eEquipaggiaEStoccaPassanoDalGruppoAlPersonaggioEViceversa() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio guerriero = guerriero("Arsenio");
        Personaggio mago = new Mago("Merlino", 3);
        gruppo.aggiungiPersonaggio(guerriero);
        gruppo.aggiungiPersonaggio(mago);
        Artefatto spada = spada();
        gruppo.addArtefatto(spada);
        assertFalse(gruppo.controllaPrelievo(guerriero, spada).isPresent());
        assertTrue(gruppo.controllaPrelievo(mago, spada).isPresent());
        // Chi la può impugnare è il guerriero, non il mago; non chi è a tempo né chi è morto
        assertEquals(1, gruppo.chiPuoEquipaggiare(spada).size());
        assertEquals(0, (int) gruppo.chiPuoEquipaggiare(spada).get(0));
        mago.muore("prova");
        gruppo.equipaggia(guerriero, spada);
        assertTrue(gruppo.getInventario().isEmpty());
        assertEquals(1, guerriero.getInventario().size());
        gruppo.stocca(guerriero, spada);
        assertEquals(1, gruppo.getInventario().size());
        assertTrue(guerriero.getInventario().isEmpty());
        guerriero.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_GRATUITO);
        guerriero.setTempo(3);
        assertTrue(gruppo.chiPuoEquipaggiare(spada).isEmpty());
    }

    @Test
    void gliEventiDiPrelievoEStoccaggioSiApprovanoOSiRifiutano() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio guerriero = guerriero("Arsenio");
        Personaggio mago = new Mago("Merlino", 3);
        gruppo.aggiungiPersonaggio(guerriero);
        gruppo.aggiungiPersonaggio(mago);
        Artefatto spada = spada();
        gruppo.addArtefatto(spada);

        BusEventi.pubblica(new InternoPrelievoArtefatto(mago, gruppo, spada, null));
        assertEquals(1, eventi.size());
        assertTrue(eventi.get(0) instanceof NotificaRifiutoPrelievoArtefatto);
        assertEquals(1, gruppo.getInventario().size());

        eventi.clear();
        BusEventi.pubblica(new InternoPrelievoArtefatto(guerriero, gruppo, spada, null));
        assertTrue(eventi.get(0) instanceof NotificaApprovazionePrelievoArtefatto);
        assertTrue(gruppo.getInventario().isEmpty());

        eventi.clear();
        BusEventi.pubblica(new InternoStoccaggioArtefatto(guerriero, gruppo, spada, null));
        assertTrue(eventi.get(0) instanceof NotificaApprovazioneStoccaggioArtefatto);
        assertEquals(1, gruppo.getInventario().size());
    }

    // --- Pezzi dei set

    @Test
    void ipezziDiUnSetLeggendarioSiCercanoFraInventarioEPersonaggi() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(guerriero("Arsenio"));
        Artefatto spada = spada();
        gruppo.addArtefatto(spada);
        assertNotNull(gruppo.getPezziDelSet(spada));
    }
}
