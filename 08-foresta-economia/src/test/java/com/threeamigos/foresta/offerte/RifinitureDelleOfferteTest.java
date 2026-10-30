package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.economia.MondoDiProva;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOfferta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi che il dado non produce con i valori di oggi (una moneta sola per gli incantesimi), il registro acceso mentre si disegna la
 * mappa della zona, e un'informazione su un artefatto che non ha un inciso nel nome.
 */
class RifinitureDelleOfferteTest {

    private static final DestinazioneDiLog POZZO = new DestinazioneDiLog() {
        @Override
        public void scrivi(String messaggio) {
        }

        @Override
        public void scrivi(Exception e) {
        }
    };

    private GruppoGiocatore gruppo;
    private GruppoAvversario avversari;

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(20, 20);
        gruppo = GruppoGiocatore.getIstanza();
        Guerriero guerriero = new Guerriero("Arsenio", 3);
        guerriero.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(guerriero);
        avversari = GruppoAvversario.getIstanza();
        avversari.aggiungiPersonaggio(new Goblin(2));
    }

    @AfterEach
    void ripulisce() {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
        MondoDiProva.finito();
    }

    private static void impostaCosto(Offerta offerta, int costo) throws Exception {
        Field campo = offerta.getClass().getDeclaredField("costo");
        campo.setAccessible(true);
        campo.setInt(offerta, costo);
    }

    @Test
    void conUnaMonetaSolaSiDiceUnaMoneta() throws Exception {
        Offerta incantesimi = FabbricaOfferte.crea(TipoOfferta.INCANTESIMI);
        impostaCosto(incantesimi, 1);
        assertTrue(incantesimi.getDescrizione(gruppo, avversari).startsWith("Per una moneta "));
        avversari.aggiungiPersonaggio(new Goblin(2));
        assertTrue(incantesimi.getDescrizione(gruppo, avversari).startsWith("Uno di loro per una moneta"));
    }

    @Test
    void laMappaDellaZonaConIlRegistroAcceso() {
        Logger.impostaDestinazione(POZZO);
        Offerta mappa = FabbricaOfferte.crea(TipoOfferta.MAPPA_ZONA);
        for (CoordinateMD posizione : new CoordinateMD[] {MondoDiProva.QUI, new CoordinateMD(1, 1), new CoordinateMD(18, 18), new CoordinateMD(10, 10)}) {
            Foresta.setPosizioneDelGruppo(posizione);
            mappa.accetta(gruppo, avversari);
            assertTrue(Foresta.isLocazioneConosciuta(posizione));
        }
    }

    @Test
    void unArtefattoSenzaIncisoNelNomeNonVuoleLaVirgola() {
        com.threeamigos.foresta.artefatti.Artefatto artefatto = MondoDiProva.artefatto(TipoArtefatto.SPADA, 2);
        artefatto.getModelloDati().setNome("la spada");
        artefatto.getModelloDati().setDescrizione("");
        artefatto.getModelloDati().setNomeProprio("");
        RegistroArtefatti.addArtefattoInLocazione(artefatto, new CoordinateMD(8, 8));
        Dado.trucca(3);
        String descrizione = FabbricaOfferte.crea(TipoOfferta.INFORMAZIONI).getDescrizione(gruppo, avversari);
        assertTrue(descrizione.contains(" si trova in un tempio "), descrizione);
        assertFalse(descrizione.contains(", si trova"), descrizione);
    }
}
