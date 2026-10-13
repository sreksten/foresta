package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostazioneAiuto;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La vista che la UI riceve al posto delle classi del motore: segue la partita corrente, anche quando il gruppo
 * viene sostituito, e l'aiuto si cambia solo con un comando.
 */
class VistaPartitaMotoreTest {

    @Test
    void laVistaMostraLaPartitaCorrente() {
        VistaPartita vista = new VistaPartitaMotore();
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            // Il gruppo della partita, non uno tenuto da parte prima che cominciasse
            assertSame(GruppoGiocatore.getIstanza(), vista.getGruppoGiocatore());
            assertEquals(partita.gruppo().getMonete(), vista.getGruppoGiocatore().getMonete());
            CoordinateMD coordinate = vista.getGruppoGiocatore().getCoordinate();
            assertEquals(TipoLocazione.CITTA_FLEENA, vista.getMappa().getLocazione(coordinate));
            assertEquals(TipoLocazione.CITTA_FLEENA, vista.getGruppoGiocatore().getTipoLocazioneCorrente());
            assertTrue(vista.getMappa().isLocazioneConosciuta(coordinate));
            assertEquals(Foresta.getDimensioneX(), vista.getMappa().getDimensioneX());
            assertEquals(RegistroMissioni.getMissioniAttive(), vista.getMissioniAttive());
            assertEquals(LineaTemporale.getGiorno(), vista.getGiorno());
        }
    }

    @Test
    void lAiutoSiCambiaConUnComando() {
        VistaPartita vista = new VistaPartitaMotore();
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            assertTrue(vista.isAiutoAbilitato());
            partita.pubblica(new ComandoImpostazioneAiuto(false));
            assertFalse(vista.isAiutoAbilitato());
            assertFalse(ModelloDati.getIstanza().isAiutoAbilitato(), "si salva con la partita");
            partita.pubblica(new ComandoImpostazioneAiuto(true));
            assertTrue(vista.isAiutoAbilitato());
        }
    }
}
