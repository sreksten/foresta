package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneModificatorePersonaggio;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I modificatori di attributo di un personaggio: quelli a tempo (benedizioni, pozioni, ignominia) che si aggiungono, scorrono e finiscono, e quelli
 * degli artefatti che li fissano.
 */
class ModificatoriTemporaneiTest {

    private final List<NotificaVariazioneModificatorePersonaggio.TipoVariazione> variazioni = new ArrayList<>();
    private final List<String> frasi = new ArrayList<>();
    private final List<Runnable> iscrizioni = new ArrayList<>();

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        DatiDiProva.nuovaPartita();
        iscrizioni.add(BusEventi.iscriviti(NotificaVariazioneModificatorePersonaggio.class, e -> variazioni.add(e.getTipo())));
        iscrizioni.add(BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio())));
    }

    @AfterEach
    void ripulisce() {
        iscrizioni.forEach(Runnable::run);
        BusEventi.impostaConsegna(null);
    }

    private static ModificatoreAttributoMD aTurni(TipoAttributo attributo, TipoModificatore tipo, double quantita, int turni) {
        return new ModificatoreAttributoMD(attributo, tipo, quantita, "prova", UnitaDurataModificatore.TURNI, turni,
                BersaglioModificatore.PORTATORE, true);
    }

    @Test
    void unModificatoreSulBersaglioNonSiMetteSulPortatore() {
        Personaggio p = new Guerriero("Arsenio", 5);
        ModificatoreAttributoMD sulBersaglio = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, -2, "veleno",
                UnitaDurataModificatore.TURNI, 2, BersaglioModificatore.BERSAGLIO, true);
        assertThrows(IllegalArgumentException.class, () -> p.addModificatore(sulBersaglio));
    }

    @Test
    void unModificatoreATempoSiAggiungeSiRaccontaEScade() {
        Personaggio p = new Guerriero("Arsenio", 5);
        int forza = p.get(TipoAttributo.FORZA);
        p.addModificatore(aTurni(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 4, 2));
        assertEquals(forza + 4, p.get(TipoAttributo.FORZA));
        assertEquals(1, p.getModificatoriTemporanei().size());
        assertEquals(4.0d, p.getVariazioneDaTemporanei(TipoAttributo.FORZA), 1e-9);
        assertEquals(1, frasi.size());
        // Il primo turno ne toglie uno e lo racconta solo se la durata sale (qui scende: niente)
        p.riduciModificatori(UnitaDurataModificatore.TURNI);
        assertEquals(1, frasi.size());
        assertEquals(1, p.getModificatoriTemporanei().iterator().next().getDurata());
        // Un'altra unità non lo tocca
        p.riduciModificatori(UnitaDurataModificatore.LOCAZIONI);
        assertEquals(1, p.getModificatoriTemporanei().size());
        p.riduciModificatori(UnitaDurataModificatore.TURNI);
        assertTrue(p.getModificatoriTemporanei().isEmpty());
        assertEquals(forza, p.get(TipoAttributo.FORZA));
        assertTrue(frasi.get(frasi.size() - 1).startsWith("Per Arsenio non vale più:"), frasi.toString());
        assertEquals(NotificaVariazioneModificatorePersonaggio.TipoVariazione.RIMOZIONE, variazioni.get(variazioni.size() - 1));
    }

    @Test
    void lostessoModificatoreNonSiSommaMaRinfrescaLaDurata() {
        Personaggio p = new Guerriero("Arsenio", 5);
        p.addModificatore(aTurni(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 4, 2));
        frasi.clear();
        // Più corto o uguale: niente
        p.addModificatore(aTurni(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 4, 2));
        assertTrue(frasi.isEmpty());
        // Più lungo: rinfresca e lo racconta
        p.addModificatore(aTurni(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 4, 6));
        assertEquals(1, p.getModificatoriTemporanei().size());
        assertEquals(6, p.getModificatoriTemporanei().iterator().next().getDurata());
        assertEquals(1, frasi.size());
        assertTrue(variazioni.contains(NotificaVariazioneModificatorePersonaggio.TipoVariazione.MODIFICA));
        // Scendere di un turno è una modifica che non si racconta
        frasi.clear();
        p.riduciModificatori(UnitaDurataModificatore.TURNI);
        assertTrue(frasi.isEmpty());
    }

    @Test
    void unModificatorePermanenteNonSiRaccontaENonSiRimuove() {
        Personaggio p = new Guerriero("Arsenio", 5);
        int forza = p.get(TipoAttributo.FORZA);
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 3, "benedizione"));
        assertEquals(forza + 3, p.get(TipoAttributo.FORZA));
        assertTrue(frasi.isEmpty());
        assertTrue(p.getModificatoriTemporanei().isEmpty());
        p.rimuoviIModificatoriTemporaneiRimovibili();
        assertEquals(forza + 3, p.get(TipoAttributo.FORZA));
    }

    @Test
    void sololeRimovibiliSiTolgonoSubito() {
        Personaggio p = new Guerriero("Arsenio", 5);
        p.addModificatore(aTurni(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 4, 5));
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, -20, "ignominia",
                UnitaDurataModificatore.LOCAZIONI, 3, BersaglioModificatore.PORTATORE, false));
        p.rimuoviIModificatoriTemporaneiRimovibili();
        assertEquals(1, p.getModificatoriTemporanei().size());
        assertEquals(TipoAttributo.CORAGGIO, p.getModificatoriTemporanei().iterator().next().getTipoAttributo());
    }

    @Test
    void unBonusDiSaluteMassimaCheScadeRiportaLaSaluteEntroIlNuovoMassimo() {
        Personaggio p = new Guerriero("Arsenio", 5);
        p.addSalute(100000);
        int massima = p.getSaluteMassima();
        p.addModificatore(aTurni(TipoAttributo.SALUTE, TipoModificatore.VARIAZIONE_FISSA, 50, 1));
        assertEquals(massima + 50, p.getSaluteMassima());
        p.addSalute(100000);
        assertEquals(massima + 50, p.getSalute());
        p.riduciModificatori(UnitaDurataModificatore.TURNI);
        assertEquals(massima, p.getSaluteMassima());
        assertEquals(massima, p.getSalute());
    }

    @Test
    void unBonusDiMagiaMassimaCheScadeRiportaLaMagiaEntroIlNuovoMassimo() {
        Personaggio p = new Mago("Merlino", 5);
        p.addMagia(100000);
        int massima = p.getMagiaMassima();
        p.addModificatore(aTurni(TipoAttributo.MAGIA, TipoModificatore.VARIAZIONE_FISSA, 30, 1));
        p.addMagia(100000);
        assertEquals(massima + 30, p.getMagia());
        p.riduciModificatori(UnitaDurataModificatore.TURNI);
        assertEquals(massima, p.getMagia());
    }

    @Test
    void unModificatoreAssolutoFissaIlValoreEInfluenzaIlRiepilogo() {
        Personaggio p = new Guerriero("Arsenio", 5);
        assertFalse(p.getFissaggio(TipoAttributo.FURIA).isPresent());
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 5, "maledizione"));
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 2, "peggio"));
        // Vale il più basso
        assertEquals(2.0d, p.getFissaggio(TipoAttributo.FURIA).get().getModificatore().getQuantita(), 1e-9);
        assertEquals(2, p.get(TipoAttributo.FURIA));
        // Anche uno scritto su un artefatto, se più basso
        Artefatto spada = new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.SPADA, 1);
        spada.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 1, "lama"));
        p.addArtefatto(spada);
        assertEquals(1.0d, p.getFissaggio(TipoAttributo.FURIA).get().getModificatore().getQuantita(), 1e-9);
    }

    @Test
    void ilFissaggioPiuAltoNonSostituisceQuelloGiaTrovatoNeUnoDiAltroTipo() {
        Personaggio p = new Guerriero("Arsenio", 5);
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 2, "primo"));
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 5, "secondo"));
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.VARIAZIONE_FISSA, 1, "non assoluto"));
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.QUANTITA_ASSOLUTA, 0, "altro attributo"));
        Artefatto spada = new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.SPADA, 1);
        spada.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 9, "lama"));
        spada.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.VARIAZIONE_FISSA, 1, "lama"));
        p.addArtefatto(spada);
        assertEquals("primo", p.getFissaggio(TipoAttributo.FURIA).get().getModificatore().getNote());
    }

    @Test
    void ilFissaggioDiUnArtefattoSenzaAltriVieneDaLui() {
        Personaggio p = new Guerriero("Arsenio", 5);
        Artefatto spada = new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.SPADA, 1);
        spada.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 3, "lama"));
        // Uno sul bersaglio e uno su un altro attributo non contano
        spada.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 0, "veleno",
                UnitaDurataModificatore.TURNI, 2, BersaglioModificatore.BERSAGLIO, true));
        spada.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.QUANTITA_ASSOLUTA, 0, "altro"));
        p.addArtefatto(spada);
        assertEquals(3.0d, p.getFissaggio(TipoAttributo.FURIA).get().getModificatore().getQuantita(), 1e-9);
    }

    @Test
    void ilPermanenteNonSiContaFraLeVariazioniDaTemporanei() {
        Personaggio p = new Guerriero("Arsenio", 5);
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 3, "benedizione"));
        p.addModificatore(aTurni(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 4, 2));
        assertEquals(4.0d, p.getVariazioneDaTemporanei(TipoAttributo.FORZA), 1e-9);
        // Un temporaneo di un altro attributo non c'entra
        p.addModificatore(aTurni(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_FISSA, 9, 2));
        assertEquals(4.0d, p.getVariazioneDaTemporanei(TipoAttributo.FORZA), 1e-9);
    }

    @Test
    void ilValoreTieneContoDeiModificatoriDegliArtefattiSoloDelPortatoreEDelSuoAttributo() {
        Personaggio p = new Guerriero("Arsenio", 5);
        int base = p.get(TipoAttributo.FURIA);
        Artefatto spada = new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.SPADA, 1);
        spada.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.VARIAZIONE_FISSA, 3, "lama"));
        spada.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.VARIAZIONE_FISSA, -50, "veleno",
                UnitaDurataModificatore.TURNI, 2, BersaglioModificatore.BERSAGLIO, true));
        spada.getModelloDati().addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_FISSA, 10, "altro"));
        p.addArtefatto(spada);
        assertEquals(base + 3, p.get(TipoAttributo.FURIA));
    }

    @Test
    void ilCantoDiValoreDaCoraggioEPrecisioneEGliAltriStatiNo() {
        Personaggio p = new Guerriero("Arsenio", 5);
        int coraggio = p.get(TipoAttributo.CORAGGIO);
        p.addEffettoDiStato(com.threeamigos.foresta.tipi.TipoEffettoDiStato.CONFUSO, 3, 0);
        assertEquals(coraggio, p.get(TipoAttributo.CORAGGIO));
        p.addEffettoDiStato(com.threeamigos.foresta.tipi.TipoEffettoDiStato.ISPIRATO, 3, 0, 50.0d);
        assertTrue(p.get(TipoAttributo.CORAGGIO) > coraggio);
        assertEquals(coraggio, (int) (p.get(TipoAttributo.CORAGGIO) - p.getVariazioneDaTemporanei(TipoAttributo.CORAGGIO)), 1.0d);
    }
}
