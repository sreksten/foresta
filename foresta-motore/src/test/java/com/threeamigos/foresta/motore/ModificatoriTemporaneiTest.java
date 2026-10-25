package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.combattimento.ApplicazioneDelRisultato;
import com.threeamigos.foresta.artefatti.*;
import com.threeamigos.foresta.artefatti.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.DannoRisultante;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.abilita.FabbricaAbilita;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.interfacce.ModificatoreAttivo;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.strumenti.TestoDeiModificatori;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I modificatori di attributo a tempo (vedi modificatori_temporanei.md): durata in turni o in locazioni, bersaglio,
 * salvataggio, scadenza, armi che li mettono sul bersaglio, Alba Sacra, ignominia della fuga, sintassi e prezzo.
 */
class ModificatoriTemporaneiTest {

    // Le regole non conoscono il testo degli avvisi
    private static java.util.Optional<String> avviso(java.util.Collection<Artefatto> banco) {
        return RegoleIncantatura.avvisoEffettiPersi(banco).map(TestoAvvisoDiFusione::testo);
    }

    private Guerriero guerriero;

    @BeforeEach
    void preparaGuerriero() {
        ModelloDati.setIstanza(new ModelloDati());
        guerriero = new Guerriero("Arsenio", 5);
    }

    private static ModificatoreAttributoMD fortunaAZero(UnitaDurataModificatore unita, int durata) {
        return new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0, "",
                unita, durata, BersaglioModificatore.PORTATORE);
    }

    // --- Modello e salvataggio

    @Test
    void unModificatorePermanenteHaDurataZeroEUnoATempoAlmenoUno() {
        assertThrows(IllegalArgumentException.class, () -> fortunaAZero(UnitaDurataModificatore.TURNI, 0));
        assertThrows(IllegalArgumentException.class, () -> fortunaAZero(UnitaDurataModificatore.PERMANENTE, 3));
        assertFalse(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2).isTemporaneo());
        assertEquals(BersaglioModificatore.PORTATORE,
                new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2).getBersaglio());
    }

    @Test
    void ilModificatoreATempoDellArtefattoSiSalvaESiRilegge() throws IOException {
        ArtefattoMD spada = new ArtefattoMD();
        spada.setTipo(TipoArtefatto.SPADA);
        spada.setNome("la spada di prova");
        spada.setDescrizione("che serve ai test");
        spada.setLivello(3);
        ModificatoreAttributoMD sulBersaglio = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0,
                "sfortuna", UnitaDurataModificatore.TURNI, 5, BersaglioModificatore.BERSAGLIO);
        spada.addModificatore(sulBersaglio);
        spada.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, -20, "",
                UnitaDurataModificatore.LOCAZIONI, 3, BersaglioModificatore.PORTATORE));

        StringWriter scritto = new StringWriter();
        PrintWriter writer = new PrintWriter(scritto);
        spada.salva(writer);
        writer.flush();
        ArtefattoMD riletto = new ArtefattoMD();
        riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));

        assertEquals(spada.getModificatori().size(), riletto.getModificatori().size());
        assertTrue(riletto.getModificatori().contains(sulBersaglio));
        assertTrue(riletto.getModificatori().containsAll(spada.getModificatori()));
    }

    @Test
    void unModificatoreSulBersagliStaSoloSulleArmi() {
        ArtefattoMD anello = new ArtefattoMD();
        anello.setTipo(TipoArtefatto.ANELLO);
        ModificatoreAttributoMD sulBersaglio = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0,
                "", UnitaDurataModificatore.TURNI, 5, BersaglioModificatore.BERSAGLIO);
        assertThrows(IllegalArgumentException.class, () -> anello.addModificatore(sulBersaglio));
        ArtefattoMD spada = new ArtefattoMD();
        spada.setTipo(TipoArtefatto.SPADA);
        spada.addModificatore(sulBersaglio);
        assertEquals(1, spada.getModificatori().size());
    }

    @Test
    void ilModificatoreATempoDelPersonaggioSiSalvaESiRilegge() throws IOException {
        guerriero.addModificatore(fortunaAZero(UnitaDurataModificatore.TURNI, 4));
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_FISSA, 2, "benedizione: roba"));

        StringWriter scritto = new StringWriter();
        PrintWriter writer = new PrintWriter(scritto);
        guerriero.getModelloDati().salva(writer);
        writer.flush();
        PersonaggioMD riletto = new PersonaggioMD();
        riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));

        assertEquals(guerriero.getModelloDati().getModificatori().size(), riletto.getModificatori().size());
        assertTrue(riletto.getModificatori().containsAll(guerriero.getModelloDati().getModificatori()));
    }

    // --- Calcolo e scadenza

    @Test
    void unModificatoreATempoPortaLAttributoAZeroEPoiScade() {
        assertTrue(guerriero.getFortuna() > 0);
        guerriero.addModificatore(fortunaAZero(UnitaDurataModificatore.TURNI, 2));
        assertEquals(0, guerriero.getFortuna());
        assertEquals(1, guerriero.getModificatoriTemporanei().size());

        guerriero.riduciModificatori(UnitaDurataModificatore.TURNI);
        assertEquals(0, guerriero.getFortuna());
        assertEquals(1, guerriero.getModificatoriTemporanei().iterator().next().getDurata());

        guerriero.riduciModificatori(UnitaDurataModificatore.TURNI);
        assertTrue(guerriero.getFortuna() > 0);
        assertTrue(guerriero.getModificatoriTemporanei().isEmpty());
    }

    @Test
    void iTurniEIeLocazioniScalanoSeparatamente() {
        guerriero.addModificatore(fortunaAZero(UnitaDurataModificatore.TURNI, 1));
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, -20, "",
                UnitaDurataModificatore.LOCAZIONI, 2, BersaglioModificatore.PORTATORE));

        guerriero.riduciModificatori(UnitaDurataModificatore.LOCAZIONI);
        assertEquals(2, guerriero.getModificatoriTemporanei().size(), "i turni non sono scesi, le locazioni sì");
        guerriero.riduciModificatori(UnitaDurataModificatore.TURNI);
        assertEquals(1, guerriero.getModificatoriTemporanei().size());
        guerriero.riduciModificatori(UnitaDurataModificatore.LOCAZIONI);
        assertTrue(guerriero.getModificatoriTemporanei().isEmpty());
    }

    @Test
    void unModificatoreUgualeNonSiSommaMaRinfrescaLaDurata() {
        int fortuna = guerriero.getFortuna();
        ModificatoreAttributoMD meno = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.VARIAZIONE_FISSA, -1, "",
                UnitaDurataModificatore.TURNI, 2, BersaglioModificatore.PORTATORE);
        guerriero.addModificatore(meno);
        guerriero.addModificatore(meno.conDurata(5));
        assertEquals(fortuna - 1, guerriero.getFortuna(), "non si somma");
        assertEquals(1, guerriero.getModificatoriTemporanei().size());
        assertEquals(5, guerriero.getModificatoriTemporanei().iterator().next().getDurata());
        // Uno più corto non accorcia quello che c'è
        guerriero.addModificatore(meno.conDurata(1));
        assertEquals(5, guerriero.getModificatoriTemporanei().iterator().next().getDurata());
    }

    @Test
    void ilModificatorePermanenteNonScade() {
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 3));
        int forza = guerriero.getForza();
        guerriero.riduciModificatori(UnitaDurataModificatore.TURNI);
        guerriero.riduciModificatori(UnitaDurataModificatore.LOCAZIONI);
        assertEquals(forza, guerriero.getForza());
    }

    @Test
    void laScadenzaDellaSaluteMassimaNonLasciaLaSaluteSopraIlMassimo() {
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.SALUTE, TipoModificatore.VARIAZIONE_FISSA, 50, "",
                UnitaDurataModificatore.TURNI, 1, BersaglioModificatore.PORTATORE));
        guerriero.addSalute(1000);
        assertEquals(guerriero.getSaluteMassima(), guerriero.getSalute());
        guerriero.riduciModificatori(UnitaDurataModificatore.TURNI);
        assertTrue(guerriero.getSalute() <= guerriero.getSaluteMassima());
    }

    @Test
    void unModificatoreSulBersaglioNonSiMetteSulPersonaggio() {
        ModificatoreAttributoMD sulBersaglio = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0,
                "", UnitaDurataModificatore.TURNI, 5, BersaglioModificatore.BERSAGLIO);
        assertThrows(IllegalArgumentException.class, () -> guerriero.addModificatore(sulBersaglio));
    }

    @Test
    void iModificatoriSulBersaglioDiUnaSpadaNonContanoPerChiLaPorta() {
        int fortuna = guerriero.getFortuna();
        guerriero.addArtefatto(spadaDellaSfortuna());
        assertEquals(fortuna, guerriero.getFortuna());
    }

    // --- Armi

    @Test
    void laSpadaDellaSfortunaPortaLaFortunaDelBersaglioAZeroPerCinqueTurni() {
        Guerriero bersaglio = new Guerriero("Bersaglio", 5);
        // Con poca forza il tiro per applicare il modificatore riesce sempre
        bersaglio.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.QUANTITA_ASSOLUTA, 1));
        assertTrue(bersaglio.getFortuna() > 0);
        Artefatto spada = spadaDellaSfortuna();
        guerriero.addArtefatto(spada);
        Arma arma = (Arma) spada;

        DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(guerriero, bersaglio, arma);
        assertEquals(1, risultato.getModificatoriDaAggiungere().size());
        ApplicazioneDelRisultato.applica(bersaglio, risultato);

        assertEquals(0, bersaglio.getFortuna());
        ModificatoreAttributoMD sulBersaglio = bersaglio.getModificatoriTemporanei().iterator().next();
        assertEquals(BersaglioModificatore.PORTATORE, sulBersaglio.getBersaglio());
        assertEquals(5, sulBersaglio.getDurata());
        assertEquals(UnitaDurataModificatore.TURNI, sulBersaglio.getUnitaDurata());
        assertTrue(guerriero.getFortuna() > 0, "chi colpisce non ne risente");

        // Un secondo colpo rinfresca, non somma
        for (int turno = 0; turno < 3; turno++) {
            bersaglio.riduciModificatori(UnitaDurataModificatore.TURNI);
        }
        assertEquals(2, bersaglio.getModificatoriTemporanei().iterator().next().getDurata());
        ApplicazioneDelRisultato.applica(bersaglio, CalcolatoreCombattimento.calcolaDannoRisultante(guerriero, bersaglio, arma));
        assertEquals(1, bersaglio.getModificatoriTemporanei().size());
        assertEquals(5, bersaglio.getModificatoriTemporanei().iterator().next().getDurata());

        for (int turno = 0; turno < 5; turno++) {
            bersaglio.riduciModificatori(UnitaDurataModificatore.TURNI);
        }
        assertTrue(bersaglio.getFortuna() > 0);
    }

    @Test
    void unBersaglioImmuneAlTipoDiDannoDellArmaNonRiceveIlModificatore() {
        Guerriero bersaglio = new Guerriero("Bersaglio", 5);
        bersaglio.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.QUANTITA_ASSOLUTA, 1));
        Arma arma = new Arma() {
            private final Arma vera = (Arma) spadaDellaSfortuna();

            @Override
            public int getDanni() {
                return vera.getDanni();
            }

            @Override
            public int getLivello() {
                return vera.getLivello();
            }

            @Override
            public TipoDanno getTipoDanno() {
                return vera.getTipoDanno();
            }

            @Override
            public boolean isIncantata() {
                return false;
            }

            @Override
            public Collection<com.threeamigos.foresta.artefatti.IncantamentoMD> getIncantamenti() {
                return vera.getIncantamenti();
            }

            @Override
            public Collection<ModificatoreAttributoMD> getModificatoriSulBersaglio() {
                return vera.getModificatoriSulBersaglio();
            }
        };
        // Senza immunità il modificatore arriva
        assertEquals(1, CalcolatoreCombattimento.calcolaDannoRisultante(guerriero, bersaglio, arma).getModificatoriDaAggiungere().size());
        // Un bersaglio che non soffre il taglio non ne riceve
        Guerriero immune = new Guerriero("Immune", 5) {
            @Override
            public boolean isImmuneATipoDanno(TipoDanno tipoDanno) {
                return true;
            }
        };
        immune.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.QUANTITA_ASSOLUTA, 1));
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(guerriero, immune, arma).getModificatoriDaAggiungere().isEmpty());
    }

    @Test
    void ilModificatoreSulBersaglioDiUnaPergamenaSiFondeSoloSuUnArma() {
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA);
        pergamena.getModelloDati().addModificatore(ModificatoreDelTesto.da("FORTUNA =0 @BERSAGLIO 5TURNI"));
        pergamena.getModelloDati().addModificatore(ModificatoreDelTesto.da("FORZA +1"));

        java.util.List<Artefatto> suUnaSpada = java.util.Arrays.asList(artefatto(TipoArtefatto.SPADA), pergamena);
        assertEquals(2, RegoleIncantatura.effettiDaTrasferire(suUnaSpada));
        assertFalse(avviso(suUnaSpada).isPresent());

        java.util.List<Artefatto> suUnElmo = java.util.Arrays.asList(artefatto(TipoArtefatto.ELMO), pergamena);
        assertEquals(1, RegoleIncantatura.effettiDaTrasferire(suUnElmo), "passa solo la forza");
        assertTrue(avviso(suUnElmo).get().contains("sul bersaglio"));
    }

    // --- Alba Sacra e fuga

    @Test
    void lAlbaSacraTogliePureITemporaneiMaNonIPermanenti() {
        guerriero.addModificatore(fortunaAZero(UnitaDurataModificatore.TURNI, 4));
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, 20, "",
                UnitaDurataModificatore.LOCAZIONI, 2, BersaglioModificatore.PORTATORE));
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 3, "benedizione"));
        int forza = guerriero.getForza();

        guerriero.rimuoviIModificatoriTemporaneiRimovibili();

        assertTrue(guerriero.getModificatoriTemporanei().isEmpty());
        assertTrue(guerriero.getFortuna() > 0);
        assertEquals(forza, guerriero.getForza());
        assertEquals(3, guerriero.getModelloDati().getModificatori().stream()
                .filter(m -> m.getTipoAttributo() == TipoAttributo.FORZA).mapToInt(m -> (int) m.getQuantita()).sum());
    }

    @Test
    void laFugaMetteUnMalusSuCoraggioValoreECarismaPerTreLocazioniDopoLaFuga() {
        int coraggio = guerriero.getCoraggio();
        // Non deve morire per le ferite della fuga
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.SALUTE, TipoModificatore.VARIAZIONE_FISSA, 10000));
        guerriero.addSalute(10000);
        guerriero.fugge();

        assertEquals(3, guerriero.getModificatoriTemporanei().size());
        assertTrue(guerriero.getCoraggio() < coraggio);
        // La fuga finisce in FINE_LOCAZIONE_2, che fa scalare di una locazione: ne restano tre
        guerriero.riduciModificatori(UnitaDurataModificatore.LOCAZIONI);
        for (int locazione = 0; locazione < 2; locazione++) {
            guerriero.riduciModificatori(UnitaDurataModificatore.LOCAZIONI);
            assertEquals(3, guerriero.getModificatoriTemporanei().size());
        }
        guerriero.riduciModificatori(UnitaDurataModificatore.LOCAZIONI);
        assertTrue(guerriero.getModificatoriTemporanei().isEmpty());
        assertEquals(coraggio, guerriero.getCoraggio());
    }

    // --- Vista per la UI

    @Test
    void laVistaDiceDiQuantoITemporaneiSpostanoUnAttributoEChiLoFissa() {
        assertEquals(0.0, guerriero.getVariazioneDaTemporanei(TipoAttributo.FORZA));
        assertFalse(guerriero.getFissaggio(TipoAttributo.FORTUNA).isPresent());

        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 3, "",
                UnitaDurataModificatore.TURNI, 2, BersaglioModificatore.PORTATORE));
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, -50, "",
                UnitaDurataModificatore.TURNI, 2, BersaglioModificatore.PORTATORE));
        assertEquals(3.0, guerriero.getVariazioneDaTemporanei(TipoAttributo.FORZA));
        assertTrue(guerriero.getVariazioneDaTemporanei(TipoAttributo.CORAGGIO) < 0);
        // Un permanente non conta come temporaneo
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.DESTREZZA, TipoModificatore.VARIAZIONE_FISSA, 3));
        assertEquals(0.0, guerriero.getVariazioneDaTemporanei(TipoAttributo.DESTREZZA));

        // Fissata da un artefatto (permanente) e poi da un temporaneo più basso: vince il più basso
        guerriero.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 5, "Anello"));
        Optional<ModificatoreAttivo> fissaggio = guerriero.getFissaggio(TipoAttributo.FORTUNA);
        assertTrue(fissaggio.isPresent());
        assertEquals(5.0, fissaggio.get().getModificatore().getQuantita());
        assertEquals("Anello", fissaggio.get().getFonte());
        guerriero.addModificatore(fortunaAZero(UnitaDurataModificatore.TURNI, 3));
        assertEquals(0.0, guerriero.getFissaggio(TipoAttributo.FORTUNA).get().getModificatore().getQuantita());
    }

    @Test
    void laFonteDelFissaggioDiUnArtefattoENelSuoNome() {
        Artefatto anello = artefatto(TipoArtefatto.ANELLO);
        anello.getModelloDati().setNome("l'anello fissatore");
        anello.getModelloDati().addModificatore(TipoAttributo.PERCEZIONE, TipoModificatore.QUANTITA_ASSOLUTA, 7, "");
        guerriero.addArtefatto(anello);
        Optional<ModificatoreAttivo> fissaggio = guerriero.getFissaggio(TipoAttributo.PERCEZIONE);
        assertTrue(fissaggio.isPresent());
        assertEquals("l'anello fissatore", fissaggio.get().getFonte());
        assertEquals(7, guerriero.getPercezione());
    }

    // --- Sintassi

    @Test
    void laSintassiDeiModificatoriNeiFile() {
        ModificatoreAttributoMD permanente = ModificatoreDelTesto.da("FORZA +100%");
        assertEquals(TipoModificatore.VARIAZIONE_PERCENTUALE, permanente.getTipoModificatoreAttributo());
        assertEquals(100, permanente.getQuantita());
        assertFalse(permanente.isTemporaneo());
        assertEquals(TipoModificatore.VARIAZIONE_FISSA, ModificatoreDelTesto.da("VALORE +2").getTipoModificatoreAttributo());

        ModificatoreAttributoMD sfortuna = ModificatoreDelTesto.da("FORTUNA =0 @BERSAGLIO 3TURNI");
        assertEquals(TipoModificatore.QUANTITA_ASSOLUTA, sfortuna.getTipoModificatoreAttributo());
        assertEquals(BersaglioModificatore.BERSAGLIO, sfortuna.getBersaglio());
        assertEquals(UnitaDurataModificatore.TURNI, sfortuna.getUnitaDurata());
        assertEquals(3, sfortuna.getDurata());
        assertEquals(sfortuna, ModificatoreDelTesto.da("FORTUNA =0 @BERSAGLIO 3 TURNI"));

        ModificatoreAttributoMD locazioni = ModificatoreDelTesto.da("CORAGGIO -20% 3 LOCAZIONI");
        assertEquals(UnitaDurataModificatore.LOCAZIONI, locazioni.getUnitaDurata());
        assertEquals(BersaglioModificatore.PORTATORE, locazioni.getBersaglio());
        assertEquals(1, ModificatoreDelTesto.da("CORAGGIO -20% 1LOCAZIONE").getDurata());
        assertEquals(1, ModificatoreDelTesto.da("CORAGGIO -20% 1 TURNO").getDurata());

        assertThrows(IllegalArgumentException.class, () -> ModificatoreDelTesto.da("FORZA"));
        assertThrows(IllegalArgumentException.class, () -> ModificatoreDelTesto.da("FORZA +2 3GIORNI"));
        assertThrows(IllegalArgumentException.class, () -> ModificatoreDelTesto.da("FORZA +2 0TURNI"));
        assertThrows(IllegalArgumentException.class, () -> ModificatoreDelTesto.da("FORZA +2 @PORTATORE"));
    }

    // --- Prezzo

    @Test
    void unModificatoreATempoCostaIlPermanenteInPiuPerOgniTurnoFinoAUnTetto() {
        ModificatoreAttributoMD permanente = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10);
        double base = ListinoPergamene.prezzo(permanente);
        ModificatoreAttributoMD treTurni = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "",
                UnitaDurataModificatore.TURNI, 3, BersaglioModificatore.PORTATORE);
        assertEquals(base * 3 * CostantiArtefatti.PERGAMENA_COEFFICIENTE_PREZZO_PER_TURNO, ListinoPergamene.prezzo(treTurni), 1e-9);
        ModificatoreAttributoMD moltiTurni = treTurni.conDurata(1000);
        assertEquals(base * CostantiArtefatti.PERGAMENA_MOLTIPLICATORE_PREZZO_TEMPO_MASSIMO, ListinoPergamene.prezzo(moltiTurni), 1e-9);
    }

    @Test
    void portaAZeroCostaCinquantaFissoEPortaAQCinqueVolteQ() {
        ModificatoreAttributoMD aZero = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0);
        assertEquals(50.0, ListinoPergamene.prezzo(aZero), 1e-9);
        ModificatoreAttributoMD aSei = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 6);
        assertEquals(30.0, ListinoPergamene.prezzo(aSei), 1e-9);
    }

    @Test
    void laDescrizioneBrevePerGliSprite() {
        assertEquals("Furtività +5", TestoDeiModificatori.breve(ModificatoreDelTesto.da("FURTIVITA +5")));
        assertEquals("Coraggio -3", TestoDeiModificatori.breve(ModificatoreDelTesto.da("CORAGGIO -3")));
        assertEquals("Coraggio -20%", TestoDeiModificatori.breve(ModificatoreDelTesto.da("CORAGGIO -20%")));
        assertEquals("Intelligenza a 0", TestoDeiModificatori.breve(ModificatoreDelTesto.da("INTELLIGENZA =0")));
    }

    // --- Leggendario di prova

    @Test
    void laSpadaDellaSfortunaDiRomyJonaSiLegge() {
        Artefatto spada = Leggendari.con("SPADA_DELLA_SFORTUNA").costruisci(Statistiche.getLivello());
        Collection<ModificatoreAttributoMD> sulBersaglio = ((Arma) spada).getModificatoriSulBersaglio();
        assertEquals(1, sulBersaglio.size());
        ModificatoreAttributoMD modificatore = sulBersaglio.iterator().next();
        assertEquals(TipoAttributo.FORTUNA, modificatore.getTipoAttributo());
        assertEquals(TipoModificatore.QUANTITA_ASSOLUTA, modificatore.getTipoModificatoreAttributo());
        assertEquals(5, modificatore.getDurata());
    }

    @Test
    void laRivelazioneEIlTestoDicono() {
        ModificatoreAttributoMD modificatore = ModificatoreDelTesto.da("FORTUNA =0 @BERSAGLIO 5TURNI");
        assertEquals("Fortuna porta a 0 sul bersaglio per 5 turni", TestoDeiModificatori.completo(modificatore));
        assertEquals("Coraggio -20% per 1 locazione", TestoDeiModificatori.completo(ModificatoreDelTesto.da("CORAGGIO -20% 1LOCAZIONE")));
        assertEquals("Forza +3", TestoDeiModificatori.completo(ModificatoreDelTesto.da("FORZA +3")));
    }

    // --- Alba Sacra sul gruppo

    @Test
    void lAlbaSacraPulisceITemporaneiDelGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(77)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            com.threeamigos.foresta.personaggi.Personaggio capo = partita.gruppo().getCapo();
            capo.addModificatore(fortunaAZero(UnitaDurataModificatore.TURNI, 4));
            capo.addMagia(1000);
            FabbricaAbilita.crea(TipoAbilita.ALBA_SACRA, capo).usa(capo, partita.gruppo(), GruppoAvversario.getIstanza());
            assertTrue(capo.getModificatoriTemporanei().isEmpty());
        }
    }

    @Test
    void lAlbaSacraNonTogliLIgnominiaDellaFuga() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(78)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            com.threeamigos.foresta.personaggi.Personaggio capo = partita.gruppo().getCapo();
            capo.fugge();
            capo.addModificatore(fortunaAZero(UnitaDurataModificatore.TURNI, 4));
            capo.addMagia(1000);
            FabbricaAbilita.crea(TipoAbilita.ALBA_SACRA, capo).usa(capo, partita.gruppo(), GruppoAvversario.getIstanza());
            assertEquals(3, capo.getModificatoriTemporanei().size(), "restano coraggio, valore e carisma");
            assertTrue(capo.getModificatoriTemporanei().stream().noneMatch(ModificatoreAttributoMD::isRimovibile));
        }
    }

    @Test
    void inModalitaDiProvaIlGruppoHaLeDueSpadeDiLivelloUnoPerProvareIModificatori() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(258)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            java.util.Map<String, ModificatoreAttributoMD> spade = new java.util.HashMap<>();
            for (Artefatto a : partita.gruppo().getInventario()) {
                if (a.getTipo() == TipoArtefatto.SPADA && !a.getModelloDati().getModificatori().isEmpty()
                        && a.getLivello() == 1) {
                    spade.put(a.getNome(), a.getModelloDati().getModificatori().iterator().next());
                }
            }
            ModificatoreAttributoMD inciampo = spade.get("la Spada dell'Inciampo");
            assertEquals("Fortuna porta a 0 sul bersaglio per 2 turni", TestoDeiModificatori.completo(inciampo));
            ModificatoreAttributoMD debolezza = spade.get("la Spada della Debolezza");
            assertEquals("Forza -2 sul bersaglio per 3 turni", TestoDeiModificatori.completo(debolezza));
        }
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(259)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            assertTrue(partita.gruppo().getInventario().stream().noneMatch(a -> a.getNome().startsWith("la Spada dell")));
        }
    }

    // --- Aiuti

    private Artefatto artefatto(TipoArtefatto tipo) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(3);
        md.setPeso(1);
        md.setDanni(tipo.getCategoria() == CategoriaArtefatto.ARMA ? 10 : 0);
        return Artefatto.di(md);
    }

    private Artefatto spadaDellaSfortuna() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA);
        spada.getModelloDati().addModificatore(ModificatoreDelTesto.da("FORTUNA =0 @BERSAGLIO 5TURNI"));
        return spada;
    }
}
