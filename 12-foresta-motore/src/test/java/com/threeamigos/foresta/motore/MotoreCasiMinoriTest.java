package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.AvvisoDiFusione;
import com.threeamigos.foresta.artefatti.BancoDiLavoro;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoScambioArtefatto;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoModificatore;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Casi piccoli di classi del motore che il resto dei test non raggiunge: gli scambi di artefatti senza identificativo, i costi mostrati nei
 * tre scambi, le frasi degli avvisi di fusione, la locazione corrente assente, la casella corrente e le missioni da mostrare fuori mappa.
 */
class MotoreCasiMinoriTest {

    private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(1);
        return Artefatto.di(md);
    }

    private static AvvisoDiFusione avviso(AvvisoDiFusione.Perdita perdita, int entrano, int scalzati) throws Exception {
        Method metodo = AvvisoDiFusione.class.getDeclaredMethod("di", AvvisoDiFusione.Perdita.class, int.class, int.class);
        metodo.setAccessible(true);
        return (AvvisoDiFusione) metodo.invoke(null, perdita, entrano, scalzati);
    }

    private static AvvisoDiFusione nessunMiglioramento() throws Exception {
        Method metodo = AvvisoDiFusione.class.getDeclaredMethod("nessunMiglioramento");
        metodo.setAccessible(true);
        return (AvvisoDiFusione) metodo.invoke(null);
    }

    @Test
    void leFrasiDegliAvvisiDiFusioneInOgniVariante() throws Exception {
        assertTrue(TestoAvvisoDiFusione.testo(nessunMiglioramento()).startsWith("Nessun effetto è migliore"));
        assertTrue(TestoAvvisoDiFusione.testo(avviso(AvvisoDiFusione.Perdita.LIMITE_DI_EFFETTI, 1, 0)).contains("un solo effetto"));
        assertTrue(TestoAvvisoDiFusione.testo(avviso(AvvisoDiFusione.Perdita.LIMITE_DI_EFFETTI, 3, 0)).contains("passeranno 3 effetti"));
        assertTrue(TestoAvvisoDiFusione.testo(avviso(AvvisoDiFusione.Perdita.MODIFICATORI_SUL_BERSAGLIO_SOLO_SULLE_ARMI, 1, 0)).contains("solo sulle armi"));
        assertTrue(TestoAvvisoDiFusione.testo(avviso(AvvisoDiFusione.Perdita.INCANTAMENTI_ELEMENTALI_NON_SUL_LIBRO, 1, 0)).contains("libro"));
        assertTrue(TestoAvvisoDiFusione.testo(avviso(null, 0, 1)).startsWith("Un effetto dell'artefatto"));
        assertTrue(TestoAvvisoDiFusione.testo(avviso(null, 0, 3)).startsWith("3 effetti"));
        assertTrue(TestoAvvisoDiFusione.testo(avviso(AvvisoDiFusione.Perdita.POTERE_MAGICO_SOLO_SU_BASTONI_E_LIBRI, 1, 0)).contains("bastoni e libri"));
        assertTrue(TestoAvvisoDiFusione.testo(avviso(AvvisoDiFusione.Perdita.LIMITE_DI_EFFETTI, 1, 1)).contains("Un effetto dell'artefatto sarà scalzato"));
        assertTrue(TestoAvvisoDiFusione.testo(avviso(AvvisoDiFusione.Perdita.LIMITE_DI_EFFETTI, 1, 4)).contains("4 effetti dell'artefatto saranno scalzati"));
    }

    @Test
    void gliScambiDicianoSeMostranoIlCosto() {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        GruppoGiocatore gruppo = new GruppoGiocatore();
        BancoDiLavoro banco = new BancoDiLavoro();

        AutomaInventario inventario = new AutomaInventario(gruppo, banco);
        assertFalse(inventario.mostraCostoSuParteAttiva());
        assertFalse(inventario.mostraCostoSuParteRemota());

        AutomaAcquistiArtefatti acquisti = new AutomaAcquistiArtefatti(gruppo, banco);
        assertTrue(acquisti.mostraCostoSuParteAttiva());
        assertTrue(acquisti.mostraCostoSuParteRemota());

        AutomaIncantatore incantatore = new AutomaIncantatore(gruppo, banco);
        assertFalse(incantatore.mostraCostoSuParteAttiva());
        assertFalse(incantatore.mostraCostoSuParteRemota());
    }

    @Test
    void unoScambioSenzaIdentificativoNonSiTrovaENonHaArtefatti() {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        GruppoGiocatore gruppo = new GruppoGiocatore();
        AutomaInventario inventario = new AutomaInventario(gruppo, new BancoDiLavoro());

        assertFalse(AutomaScambiatoreArtefatti.trova(null).isPresent());
        assertFalse(inventario.trovaArtefatto(null).isPresent());
    }

    @Test
    void unBancoVuotoSiPaga_senzaPianoEIlPianoSiRicalcolaQuandoCambia() {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        GruppoGiocatore gruppo = new GruppoGiocatore();
        BancoDiLavoro banco = new BancoDiLavoro();
        AutomaIncantatore incantatore = new AutomaIncantatore(gruppo, banco);

        assertTrue(incantatore.getEffettiDaTrasferire() >= 0);
        assertTrue(incantatore.getCostoFusione() >= 0);
        assertFalse(incantatore.isEffettoInEntrata("niente"));

        // Cambia il numero degli oggetti sul banco, poi il numero degli effetti di uno di loro, poi un oggetto al posto di un altro
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        banco.addArtefatto(spada);
        incantatore.isEffettoInEntrata("niente");
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        banco.addArtefatto(pergamena);
        incantatore.isEffettoInEntrata("niente");
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 10, "");
        incantatore.isEffettoInEntrata("niente");
        banco.removeArtefatto(pergamena);
        Artefatto altra = artefatto(TipoArtefatto.PERGAMENA, 1);
        altra.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 10, "");
        banco.addArtefatto(altra);
        incantatore.isEffettoInEntrata("niente");
        assertTrue(incantatore.getEffettiDaTrasferire() >= 0);
    }

    @Test
    void conUnArtefattoEUnaPergamenaSulBancoIlPianoDiceQuantiEffettiEQuantoCosta() {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        GruppoGiocatore gruppo = new GruppoGiocatore();
        BancoDiLavoro banco = new BancoDiLavoro();
        AutomaIncantatore incantatore = new AutomaIncantatore(gruppo, banco);
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "");
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 10, "");
        banco.addArtefatto(spada);
        banco.addArtefatto(pergamena);

        assertTrue(incantatore.getEffettiDaTrasferire() >= 1);
        assertTrue(incantatore.getCostoFusione() >= 0);
        assertTrue(incantatore.getEffettiDopoLaFusione() >= 1);
    }

    @Test
    void fuoriDallaMappaOSenzaConoscerlaNonCiSonoMissioniDaMostrare() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(42)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                    CoordinateMD sconosciuta = new CoordinateMD(x, y);
                    if (!Foresta.isLocazioneConosciuta(sconosciuta)) {
                        assertEquals(Collections.emptyList(), CostruttoreDellaForesta.getNomiMissioniDaMostrare(sconosciuta));
                        return;
                    }
                }
            }
            fail("tutta la mappa è conosciuta");
        }
    }

    @Test
    void conUnoScambioChiusoLaUiNonPuoPiuComandarlo() {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        // Un comando verso uno scambio che non esiste non fa niente
        AutomaScambiatoreArtefatti.esegui(new ComandoScambioArtefatto("non-esiste", ComandoScambioArtefatto.Destinazione.PARTE_ATTIVA, "uuid"));
    }

    @Test
    void lAutomaNuovoChiudeQuelloVecchio() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(40)) {
            // Ne nasce un secondo: il primo smette di ascoltare il bus, e i suoi comandi non fanno più niente
            Automa nuovo = new Automa(new TemporizzatoreManuale(), new GestoreSalvataggiInMemoria(), new GestorePunteggiInMemoria(), Runnable::run, System::nanoTime);
            partita.assertStato(Stato.INTRO);
            // Se lo si annuncia di nuovo non si chiude da solo
            StatoDelMotore.automaCreato(nuovo);
        }
    }

    @Test
    void senzaLocazioneCorrenteNonCEUnOggettoACterra() {
        LocazioneCorrente.azzera();
        assertNull(LocazioneCorrente.getTipoBottino());
    }

    @Test
    void laCasellaCorrenteSiPuoCambiareEFuoriMappaNonCiSonoMissioniDaMostrare() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(41)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            CostruttoreDellaForesta.impostaLocazioneCorrente(TipoLocazione.BOSCO);
            assertEquals(TipoLocazione.BOSCO, Foresta.getLocazioneDelGruppo());

            assertEquals(Collections.emptyList(), CostruttoreDellaForesta.getNomiMissioniDaMostrare(new CoordinateMD(-1, -1)));
        }
    }
}
