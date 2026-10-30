package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPersonaggioArreso;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatoVitalePersonaggio;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le ferite, la morte, la resa, la resurrezione, la magia, il riposo e la fuga di un personaggio.
 */
class SaluteEMorteTest {

    private final List<String> frasi = new ArrayList<>();
    private final List<Boolean> statiVitali = new ArrayList<>();
    private final List<Object> arresi = new ArrayList<>();
    private final List<Runnable> iscrizioni = new ArrayList<>();

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        DatiDiProva.nuovaPartita();
        GruppoAvversario.getIstanza().reimposta();
        iscrizioni.add(BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio())));
        iscrizioni.add(BusEventi.iscriviti(NotificaVariazioneStatoVitalePersonaggio.class, e -> statiVitali.add(e.isVivo())));
        iscrizioni.add(BusEventi.iscriviti(InternoPersonaggioArreso.class, arresi::add));
    }

    @AfterEach
    void ripulisce() {
        iscrizioni.forEach(Runnable::run);
        BusEventi.impostaConsegna(null);
    }

    private static Personaggio goblin() {
        return FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 5);
    }

    @Test
    void unaFeritaLevaSaluteEDiceQuantaNeResta() {
        Personaggio p = goblin();
        int prima = p.getSalute();
        p.subSalute(3, null, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
        assertEquals(prima - 3, p.getSalute());
        assertTrue(frasi.stream().anyMatch(f -> f.contains("ha ancora " + (prima - 3) + " punti ferita su")), frasi.toString());
        // Senza notifica non si dice niente
        frasi.clear();
        p.subSalute(1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertTrue(frasi.isEmpty());
    }

    @Test
    void conUnPuntoDiSaluteSiDiceAlSingolare() {
        Personaggio p = goblin();
        p.subSalute(p.getSalute() - 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        p.subSalute(1, null, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
        assertTrue(frasi.stream().anyMatch(f -> f.contains("ha ancora 1 punto ferita")), frasi.toString());
    }

    @Test
    void unDannoNulloSiRaccontaSoloSeRichiesto() {
        Personaggio p = goblin();
        Personaggio avversario = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        p.subSalute(0, avversario, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
        assertTrue(frasi.stream().anyMatch(f -> f.contains("non ha riportato danni dall'attacco")), frasi.toString());
        frasi.clear();
        p.subSalute(-4, avversario, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
        assertTrue(frasi.isEmpty());
    }

    @Test
    void chiMuoreDiFeritaLoDiceELasciaLaCausa() {
        Personaggio p = goblin();
        Personaggio assassino = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        p.subSalute(10000, assassino, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
        assertFalse(p.isVivo());
        assertEquals(0, p.getSalute());
        assertTrue(frasi.stream().anyMatch(f -> f.contains("Goblin è mort") && f.endsWith("per le ferite riportate.")), frasi.toString());
        assertTrue(p.getCausaTrapasso().startsWith("Uccis"), p.getCausaTrapasso());
        assertEquals(Boolean.FALSE, statiVitali.get(statiVitali.size() - 1));
        // Un morto non subisce altri danni, né si annuncia la morte due volte
        int frasiPrima = frasi.size();
        p.subSalute(5, null, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
        assertEquals(frasiPrima, frasi.size());
    }

    @Test
    void senzaAttaccanteSiMuorePerIDanniNelTempo() {
        Personaggio p = goblin();
        p.subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(p.isVivo());
        assertTrue(p.getCausaTrapasso().contains("danni nel tempo"), p.getCausaTrapasso());
        assertTrue(frasi.isEmpty(), "senza la notifica di morte non si racconta");
    }

    @Test
    void ilNumeroDistingueGliAvversariDellaStessaSpecie() {
        Personaggio primo = goblin();
        Personaggio secondo = goblin();
        GruppoAvversario.getIstanza().aggiungiPersonaggio(primo);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(secondo);
        secondo.subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("Il secondo Goblin è morto")) || frasi.stream().anyMatch(f -> f.contains("Goblin")), frasi.toString());
        // Una femmina nello stesso gruppo: il numero è all'ordinale femminile
        Personaggio strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        Personaggio altra = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(strega);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(altra);
        frasi.clear();
        altra.subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
        assertTrue(frasi.stream().anyMatch(f -> f.contains("Strega") && f.contains("mort")), frasi.toString());
    }

    @Test
    void unPersonaggioConNomeMuoreChiamatoPerNome() {
        Personaggio p = new Guerriero("Arsenio", 3);
        p.subSalute(10000, null, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
        assertTrue(frasi.contains("Arsenio è morto per le ferite riportate."), frasi.toString());
    }

    @Test
    void chiLasciaIlGruppoQuandoMuoreNonHaFeriteDaRaccontare() {
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        scheletro.setMotivoNelGruppo(TipoMotivoNelGruppo.MIETITURA);
        scheletro.subSalute(10000, null, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
        assertFalse(scheletro.isVivo());
        assertTrue(frasi.isEmpty(), frasi.toString());
    }

    @Test
    void ChiNonPuoMorireNonMuore() {
        Personaggio immortale = new OmbraFiamma(3);
        immortale.subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
        assertTrue(immortale.isVivo());
        immortale.muore("niente");
        assertTrue(immortale.isVivo());
    }

    @Test
    void chiSiArrendeResta_conUnPuntoEVaInPanchina() {
        Personaggio nemico = goblin();
        nemico.setFinoAllaResa(true);
        nemico.subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
        assertTrue(nemico.isVivo());
        assertEquals(1, nemico.getSalute());
        assertTrue(nemico.isInPanchina());
        assertEquals(1, arresi.size());
        assertTrue(frasi.stream().anyMatch(f -> f.contains("abbassa le armi e si arrende")), frasi.toString());
    }

    @Test
    void ChiEDelGruppoSiArrendeSeGliAvversariCombattonoFinoAllaResa() {
        Personaggio mio = new Guerriero("Arsenio", 3);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(mio);
        Personaggio capobanda = goblin();
        capobanda.setFinoAllaResa(true);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(capobanda);
        mio.subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
        assertTrue(mio.isVivo());
        assertTrue(frasi.stream().anyMatch(f -> f.contains("ammette la sconfitta e si fa da parte")), frasi.toString());
    }

    @Test
    void ilGuerrieroInDifficoltaDiventaFurioso() {
        Personaggio guerriero = new Guerriero("Arsenio", 10);
        assertTrue(guerriero.get(TipoAttributo.FURIA) > 0);
        guerriero.addSalute(100000);
        guerriero.subSalute(guerriero.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.BERSERK));
        guerriero.subSalute(guerriero.getSalute() - guerriero.getSaluteMassima() / 4, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertTrue(guerriero.hasEffettoDiStato(TipoEffettoDiStato.BERSERK));
        // Già furioso, un altro colpo non cambia niente
        guerriero.subSalute(1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertEquals(1, guerriero.getQuantitaEffettoDiStato(TipoEffettoDiStato.BERSERK));
        // Senza furia (la si azzera con un modificatore assoluto) non si diventa furiosi
        Personaggio placido = new Guerriero("Placido", 10);
        placido.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        placido.addSalute(100000);
        placido.subSalute(placido.getSaluteMassima() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(placido.hasEffettoDiStato(TipoEffettoDiStato.BERSERK));
    }

    @Test
    void unaGuerrieraEUnCapitanoDiventanoFuriosiComeIlGuerriero() {
        for (Personaggio p : new Personaggio[] {new Guerriera("Brunilde", 10), FabbricaPersonaggi.crea(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, 10)}) {
            p.addSalute(100000);
            p.subSalute(p.getSaluteMassima() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            assertEquals(p.get(TipoAttributo.FURIA) > 0, p.hasEffettoDiStato(TipoEffettoDiStato.BERSERK), p.getClasse().name());
        }
        // Chi non ha furia non la ottiene, e il Mago non è fra chi la può avere
        Personaggio mago = new Mago("Merlino", 10);
        mago.addSalute(100000);
        mago.subSalute(mago.getSaluteMassima() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(mago.hasEffettoDiStato(TipoEffettoDiStato.BERSERK));
    }

    @Test
    void unAlleatoSenzaNomeMuoreComeUnAvversarioNelProprioGruppo() {
        Personaggio ospite = goblin();
        ospite.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_MERCENARIO);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(ospite);
        ospite.subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("Il Goblin è morto")), frasi.toString());
    }

    @Test
    void ilGruppoDelGiocatoreNonSiArrendeSeGliAvversariNonCombattonoFinoAllaResa() {
        Personaggio mio = new Guerriero("Arsenio", 3);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(mio);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(goblin());
        mio.subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
        assertFalse(mio.isVivo());
    }

    @Test
    void laSaluteNonSuperaIlMassimoEIlRecuperoNonScendeSottoZero() {
        Personaggio p = goblin();
        p.subSalute(5, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        p.addSalute(100000);
        assertEquals(p.getSaluteMassima(), p.getSalute());
    }

    @Test
    void chiMuoreRestaSenzaEffettiENzaModificatoriATempo() {
        Personaggio p = goblin();
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 1);
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2, "prova",
                com.threeamigos.foresta.tipi.UnitaDurataModificatore.TURNI, 3, com.threeamigos.foresta.tipi.BersaglioModificatore.PORTATORE, true));
        assertFalse(p.getModificatoriTemporanei().isEmpty());
        p.muore("veleno");
        assertTrue(p.getEffettiDiStato().isEmpty());
        assertTrue(p.getModificatoriTemporanei().isEmpty());
        assertEquals("veleno", p.getCausaTrapasso());
    }

    @Test
    void laResurrezioneRidaUnDecimoDellaSalute() {
        Personaggio p = goblin();
        p.muore("prova");
        p.resuscita();
        assertTrue(p.isVivo());
        assertEquals((int) (p.getSaluteMassima() / 10.0d), p.getSalute(), 1);
        assertEquals(9, p.get(TipoAttributo.STANCHEZZA));
        assertEquals(Boolean.TRUE, statiVitali.get(statiVitali.size() - 1));
    }

    @Test
    void laMagiaSiConsumaESiRecuperaEntroIlMassimo() {
        Personaggio mago = new Mago("Merlino", 5);
        int massima = mago.getMagiaMassima();
        assertTrue(massima > 0);
        mago.addMagia(massima);
        mago.subMagia(3);
        assertEquals(massima - 3, mago.getMagia());
        mago.addMagia(100000);
        assertEquals(massima, mago.getMagia());
        assertThrows(IllegalStateException.class, () -> mago.subMagia(massima + 1));
    }

    @Test
    void riposandoSiRecuperaESiRiposaDallaFatica() {
        Personaggio mago = new Mago("Merlino", 5);
        mago.subSalute(mago.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        mago.addStanchezza(8);
        int saluteMentreStanco = mago.getSalute();
        int stanchezza = mago.get(TipoAttributo.STANCHEZZA);
        mago.riposa(8, TipoRiposo.AL_COPERTO);
        assertTrue(mago.getSalute() > saluteMentreStanco);
        assertTrue(mago.get(TipoAttributo.STANCHEZZA) < stanchezza);
    }

    @Test
    void laRigenerazioneDeiNonMortiENulla() {
        assertEquals(0, FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 5).getRigenerazioneSalute());
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        assertTrue(guerriero.getRigenerazioneSalute() > 0);
        assertTrue(new Mago("Merlino", 5).getRigenerazioneMagia() > 0);
    }

    @Test
    void fuggireCostaSaluteMaNonUccideEDaIgnominia() {
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        int massima = guerriero.getSaluteMassima();
        int coraggio = guerriero.get(TipoAttributo.CORAGGIO);
        guerriero.fugge();
        assertTrue(guerriero.isVivo());
        assertTrue(guerriero.getSalute() < massima);
        assertTrue(guerriero.get(TipoAttributo.CORAGGIO) <= coraggio);
        assertEquals(3, guerriero.getModificatoriTemporanei().size());
        // Con un punto di salute non si perde altro, ma l'ignominia resta
        guerriero.subSalute(guerriero.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        guerriero.fugge();
        assertEquals(1, guerriero.getSalute());
    }

    @Test
    void chiNonVivePiuNonAccumulaIgnominiaFuggendo() {
        Personaggio p = goblin();
        p.muore("prova");
        int prima = p.getModificatoriTemporanei().size();
        p.fugge();
        assertEquals(prima, p.getModificatoriTemporanei().size());
        assertEquals(frasi.stream().collect(Collectors.toList()), frasi);
    }
}
