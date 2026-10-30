package com.threeamigos.foresta.gruppi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaIncantesimi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniAumentoLivello;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniMagia;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniMagiaGrandi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniSalute;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniSaluteGrandi;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quello che hanno in comune i gruppi: i personaggi e il loro capo, la panchina, le scorte di pergamene e di pozioni (che solo il gruppo del
 * giocatore annuncia alla UI) e l'uso delle pozioni.
 */
class GruppoTest {

    private final List<Object> annunci = new ArrayList<>();
    private final List<String> frasi = new ArrayList<>();
    private final List<Runnable> iscrizioni = new ArrayList<>();

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        DatiDiProva.nuovaPartita();
        GruppoAvversario.getIstanza().reimposta();
        for (Class<?> tipo : new Class<?>[] {NotificaVariazioneDisponibilitaIncantesimi.class, NotificaVariazioneDisponibilitaPozioniSalute.class,
                NotificaVariazioneDisponibilitaPozioniSaluteGrandi.class, NotificaVariazioneDisponibilitaPozioniMagia.class,
                NotificaVariazioneDisponibilitaPozioniMagiaGrandi.class, NotificaVariazioneDisponibilitaPozioniAumentoLivello.class}) {
            iscrizioni.add(iscrivi(tipo));
        }
        iscrizioni.add(BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio())));
    }

    @SuppressWarnings("unchecked")
    private <T> Runnable iscrivi(Class<T> tipo) {
        return BusEventi.iscriviti(tipo, e -> annunci.add(e));
    }

    @AfterEach
    void ripulisce() {
        iscrizioni.forEach(Runnable::run);
        BusEventi.impostaConsegna(null);
    }

    private static Personaggio giocante(String nome) {
        Personaggio p = new Guerriero(nome, 5);
        p.getModelloDati().setPng(false);
        return p;
    }

    // --- Le scorte

    private void provaLeScorte(Gruppo gruppo, boolean annuncia) {
        int attesi = annuncia ? 1 : 0;
        gruppo.addIncantesimi(TipoIncantesimo.FUOCO, 3);
        gruppo.subIncantesimi(TipoIncantesimo.FUOCO, 1);
        assertEquals(2, gruppo.getIncantesimi(TipoIncantesimo.FUOCO));
        assertEquals(2 * attesi, annunci.size());
        annunci.clear();

        gruppo.addPozioniSalute(4);
        gruppo.subPozioniSalute(1);
        assertEquals(3, gruppo.getPozioniSalute());
        gruppo.addPozioniSaluteGrande(4);
        gruppo.subPozioniSaluteGrande(1);
        assertEquals(3, gruppo.getPozioniSaluteGrande());
        gruppo.addPozioniMagia(4);
        gruppo.subPozioniMagia(1);
        assertEquals(3, gruppo.getPozioniMagia());
        gruppo.addPozioniMagiaGrande(4);
        gruppo.subPozioniMagiaGrande(1);
        assertEquals(3, gruppo.getPozioniMagiaGrande());
        gruppo.addPozioniAumentoLivello(4);
        gruppo.subPozioniAumentoLivello(1);
        assertEquals(3, gruppo.getPozioniAumentoLivello());
        assertEquals(10 * attesi, annunci.size());
    }

    @Test
    void ilGruppoDelGiocatoreAnnunciaLeScorteGliAvversariNo() {
        provaLeScorte(GruppoGiocatore.getIstanza(), true);
        annunci.clear();
        provaLeScorte(GruppoAvversario.getIstanza(), false);
        assertTrue(GruppoGiocatore.getIstanza().isGruppoGiocatore());
        assertFalse(GruppoAvversario.getIstanza().isGruppoGiocatore());
    }

    @Test
    void lePozioniDiSaluteEMagiaCuranoERaccontano() {
        Gruppo gruppo = GruppoGiocatore.getIstanza();
        Personaggio mago = new Mago("Merlino", 5);
        mago.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(mago);
        mago.subSalute(mago.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        mago.subMagia(mago.getMagia() / 2);
        int salute = mago.getSalute();
        int magia = mago.getMagia();
        gruppo.addPozioniSalute(1);
        gruppo.consumaPozioneSalute(mago);
        assertEquals(0, gruppo.getPozioniSalute());
        assertEquals(Math.min(mago.getSaluteMassima(), salute + CostantiPersonaggi.RECUPERO_DA_POZIONE_SALUTE), mago.getSalute());
        gruppo.addPozioniMagia(1);
        gruppo.consumaPozioneMagia(mago);
        assertTrue(mago.getMagia() >= magia);
        int saluteMassima = mago.getSaluteMassima();
        gruppo.addPozioniSaluteGrande(1);
        gruppo.consumaPozioneSaluteGrande(mago);
        assertEquals(saluteMassima + CostantiPersonaggi.AUMENTO_SALUTE_DA_POZIONE_SALUTE_GRANDE, mago.getSaluteMassima());
        int magiaMassima = mago.getMagiaMassima();
        gruppo.addPozioniMagiaGrande(1);
        gruppo.consumaPozioneMagiaGrande(mago);
        assertEquals(magiaMassima + CostantiPersonaggi.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, mago.getMagiaMassima());
        List<String> bevute = frasi.stream().filter(f -> f.contains("ha bevuto una pozione")).collect(java.util.stream.Collectors.toList());
        assertEquals(4, bevute.size(), frasi.toString());
        assertTrue(bevute.get(0).endsWith("ha bevuto una pozione che fa riacquistare salute."), frasi.toString());
    }

    @Test
    void laPozioneDiAumentoDiLivelloFaSalireDiUnoMaNonChiEGiaAlMassimo() {
        Gruppo gruppo = GruppoGiocatore.getIstanza();
        Personaggio p = new Guerriero("Arsenio", 1);
        p.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(p);
        assertFalse(gruppo.isPozioneAumentoLivelloUtilizzabile());
        gruppo.addPozioniAumentoLivello(2);
        assertTrue(gruppo.isPozioneAumentoLivelloUtilizzabile());
        int livello = p.getLivello();
        gruppo.consumaPozioneAumentoLivello(p);
        assertEquals(livello + 1, p.getLivello());
        assertEquals(1, gruppo.getPozioniAumentoLivello());
        // Al livello massimo la pozione resta
        p.impostaLivelloDopoReclutamento(CostantiPersonaggi.MASSIMO_LIVELLO);
        assertFalse(gruppo.isPozioneAumentoLivelloUtilizzabile());
        frasi.clear();
        gruppo.consumaPozioneAumentoLivello(p);
        assertEquals(1, gruppo.getPozioniAumentoLivello());
        assertTrue(frasi.get(0).contains("già al livello massimo"), frasi.toString());
    }

    // --- I personaggi

    @Test
    void ilPrimoCheEntraEIlCapoETuttoSiConta() {
        Gruppo gruppo = GruppoAvversario.getIstanza();
        assertEquals(0, gruppo.getNumeroPersonaggi());
        Personaggio a = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Personaggio b = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Personaggio c = FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1);
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiPersonaggio(b);
        gruppo.aggiungiPersonaggio(c);
        assertSame(a, gruppo.getCapo());
        assertSame(b, gruppo.getPersonaggio(1));
        assertEquals(3, gruppo.getNumeroPersonaggi());
        assertEquals(3, gruppo.getNumeroPersonaggiVivi());
        assertTrue(gruppo.contiene(b));
        assertFalse(gruppo.contiene(FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1)));
        assertTrue(gruppo.getOspitiBersaglio().isEmpty());
        // Chi è a tempo non è permanente
        c.setMotivoNelGruppo(com.threeamigos.foresta.tipi.TipoMotivoNelGruppo.AIUTO_GRATUITO);
        c.setTempo(2);
        assertEquals(2, gruppo.getNumeroPersonaggiPermanenti());
        // Chi muore non conta fra i vivi, chi è in panchina nemmeno, finché non si svuota la panchina
        b.muore("prova");
        a.setInPanchina(true);
        assertEquals(1, gruppo.getNumeroPersonaggiVivi());
        gruppo.svuotaPanchina();
        assertEquals(2, gruppo.getNumeroPersonaggiVivi());
    }

    @Test
    void ilGruppoDiceSeQualcunoVaAffrontatoSfidaOCombatteFinoAllaResa() {
        Gruppo gruppo = GruppoAvversario.getIstanza();
        Personaggio a = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        gruppo.aggiungiPersonaggio(a);
        assertFalse(gruppo.isDaAffrontare() || gruppo.isDuello() || gruppo.isSfidaACartaForbiciSasso() || gruppo.isFinoAllaResa());
        a.setDaAffrontare(true);
        a.setSfidante(true);
        a.setSfidanteACartaForbiciSasso(true);
        a.setFinoAllaResa(true);
        assertTrue(gruppo.isDaAffrontare() && gruppo.isDuello() && gruppo.isSfidaACartaForbiciSasso() && gruppo.isFinoAllaResa());
    }

    @Test
    void ilPiuFeritoEIlPiuLontanoDalMassimoEChiNonSiCuraNonConta() {
        Gruppo gruppo = GruppoGiocatore.getIstanza();
        Personaggio sano = giocante("Sano");
        Personaggio ferito = giocante("Ferito");
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 3);
        gruppo.aggiungiPersonaggio(sano);
        gruppo.aggiungiPersonaggio(ferito);
        gruppo.aggiungiPersonaggio(scheletro);
        sano.addSalute(100000);
        ferito.addSalute(100000);
        assertFalse(gruppo.piuFerito().isPresent());
        ferito.subSalute(ferito.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        scheletro.subSalute(scheletro.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertSame(ferito, gruppo.piuFerito().get());
    }

    @Test
    void siSpostanoISoloIPermanentiEnonOltreIMargini() {
        Gruppo gruppo = GruppoAvversario.getIstanza();
        Personaggio a = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Personaggio aTempo = FabbricaPersonaggi.crea(TipoPersonaggio.HOBGOBLIN, 1);
        Personaggio b = FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1);
        Personaggio fuori = FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1);
        aTempo.setMotivoNelGruppo(com.threeamigos.foresta.tipi.TipoMotivoNelGruppo.AIUTO_GRATUITO);
        aTempo.setTempo(3);
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiPersonaggio(aTempo);
        gruppo.aggiungiPersonaggio(b);
        assertFalse(gruppo.spostaPersonaggio(fuori, 1), "non c'è");
        assertFalse(gruppo.spostaPersonaggio(aTempo, 1), "è a tempo");
        assertFalse(gruppo.spostaPersonaggio(a, 0), "direzione sbagliata");
        assertFalse(gruppo.spostaPersonaggio(a, 2), "direzione sbagliata");
        assertFalse(gruppo.spostaPersonaggio(a, -1), "è già al margine");
        assertFalse(gruppo.spostaPersonaggio(b, 1), "è già al margine");
        // Si scambia con il permanente vicino, saltando chi è a tempo
        assertTrue(gruppo.spostaPersonaggio(a, 1));
        assertSame(b, gruppo.getPersonaggio(0));
        assertSame(aTempo, gruppo.getPersonaggio(1));
        assertSame(a, gruppo.getPersonaggio(2));
        // Il capo resta quello che era
        assertSame(a, gruppo.getCapo());
    }

    @Test
    void ToglierePersonaggiCambiaIlCapoSoloSeEraLui() {
        Gruppo gruppo = GruppoAvversario.getIstanza();
        Personaggio a = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Personaggio b = FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1);
        Personaggio c = FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1);
        gruppo.aggiungiPersonaggio(a);
        gruppo.aggiungiPersonaggio(b);
        gruppo.aggiungiPersonaggio(c);
        gruppo.rimuoviPersonaggio(b);
        assertSame(a, gruppo.getCapo());
        gruppo.rimuoviPersonaggio(a);
        assertSame(c, gruppo.getCapo());
        gruppo.rimuoviPersonaggio(c);
        assertNull(gruppo.getCapo());
        gruppo.aggiungiPersonaggio(a);
        gruppo.rimuoviPersonaggi();
        assertEquals(0, gruppo.getNumeroPersonaggi());
        assertNull(gruppo.getCapo());
        gruppo.aggiungiPersonaggio(a);
        gruppo.reimposta();
        assertNull(gruppo.getCapo());
        assertEquals(0, gruppo.getPersonaggi().size());
    }

    @Test
    void chiParlaEIlGruppoOIlCapo() {
        Gruppo gruppo = GruppoAvversario.getIstanza();
        gruppo.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1));
        assertEquals("il Goblin", gruppo.chi());
        assertEquals("Il Goblin", gruppo.chiMaiuscolo());
        gruppo.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1));
        assertEquals("il gruppo", gruppo.chi());
        assertEquals("Il gruppo", gruppo.chiMaiuscolo());
    }
}
