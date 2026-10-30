package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.comandigiocatore.*;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.eventi.richieste.*;
import com.threeamigos.foresta.modellodati.MessaggioMD;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.FaseDiGioco;
import com.threeamigos.foresta.tipi.TipoEvento;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ogni evento del modulo porta il suo tipo e i dati che gli sono stati dati.
 */
class EventiDelModuloTest {

    private static final Collection<Comando> SI_NO = Arrays.asList(Comando.SI, Comando.NO);

    @Test
    void glieventiSenzaDatiPortanoSoloIlTipo() {
        assertEquals(TipoEvento.INTERNO_UI_OCCUPATA, new InternoUiOccupata().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_UI_INATTIVA, new InternoUiInattiva().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_RICHIESTA_CHIUSURA_FINESTRA_COMBATTIMENTO, new InternoRichiestaChiusuraFinestraCombattimento().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_ASSEGNA_COORDINATE_A_PERSONAGGI, new InternoAssegnaCoordinateAPersonaggi().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_FINE_LOGO_INIZIALE, new InternoFineLogoIniziale().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_MOSTRA_FINESTRA_STATISTICHE, new InternoMostraFinestraStatistiche().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_MOSTRA_SCHERMATA_GIOCO, new InternoMostraSchermataGioco().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_PRECARICAMENTO_MOTORE_COMPLETATO, new InternoPrecaricamentoMotoreCompletato().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_INTERFACCIA_UTENTE_PRONTA, new InternoInterfacciaUtentePronta().getTipoEvento());
        assertEquals(TipoEvento.INTERNO_RICHIESTA_REINIZIALIZZAZIONE_UI, new InternoRichiestaReinizializzazioneUI().getTipoEvento());
        assertEquals(TipoEvento.NOTIFICA_MOSTRA_STATISTICHE_FINE_GIOCO, new NotificaMostraStatisticheFineGioco().getTipoEvento());
        assertEquals(TipoEvento.NOTIFICA_MOSTRA_PUNTEGGI_MIGLIORI, new NotificaMostraPunteggiMigliori().getTipoEvento());
        assertEquals(TipoEvento.NOTIFICA_RACCOLTA_OGGETTI, new NotificaRaccoltaOggetti().getTipoEvento());
    }

    @Test
    void glieventiInterniPortanoIlLoroDato() {
        Comando[] comandi = {Comando.SI, Comando.NO};
        InternoAggiornamentoComandiDisponibili elenco = new InternoAggiornamentoComandiDisponibili(comandi);
        assertEquals(TipoEvento.INTERNO_AGGIORNAMENTO_COMANDI_DISPONIBILI, elenco.getTipoEvento());
        assertEquals(SI_NO, elenco.getPossibilita());
        assertEquals(SI_NO, new InternoAggiornamentoComandiDisponibili(SI_NO).getPossibilita());

        List<MessaggioMD> messaggi = Collections.singletonList(new MessaggioMD("ciao", true));
        InternoCaricamentoCompletato caricamento = new InternoCaricamentoCompletato(messaggi);
        assertEquals(TipoEvento.INTERNO_CARICAMENTO_COMPLETATO, caricamento.getTipoEvento());
        assertSame(messaggi, caricamento.getUltimiMessaggi());

        InternoPuliziaCacheDinamicaImmagini pulizia = new InternoPuliziaCacheDinamicaImmagini(10, 4);
        assertEquals(TipoEvento.INTERNO_PULIZIA_CACHE_DINAMICA_IMMAGINI, pulizia.getTipoEvento());
        assertEquals(10, pulizia.getElementiPrima());
        assertEquals(4, pulizia.getElementiDopo());
    }

    @Test
    void laFaseDiGiocoPortaIComandiPossibili() {
        FaseDiGioco fase = FaseDiGioco.values()[0];
        InternoFaseDiGioco senza = new InternoFaseDiGioco(fase);
        assertEquals(TipoEvento.INTERNO_FASE_DI_GIOCO, senza.getTipoEvento());
        assertSame(fase, senza.getFase());
        assertTrue(senza.getComandiPossibili().isEmpty());
        assertEquals(SI_NO, new InternoFaseDiGioco(fase, Comando.SI, Comando.NO).getComandiPossibili());
        assertEquals(SI_NO, new InternoFaseDiGioco(fase, SI_NO).getComandiPossibili());
    }

    @Test
    void leNotifichePortanoIlLoroDato() {
        NotificaUscitaDalGioco normale = new NotificaUscitaDalGioco(0, "fine");
        assertEquals(TipoEvento.NOTIFICA_USCITA_DAL_GIOCO, normale.getTipoEvento());
        assertEquals(0, normale.getCodice());
        assertEquals("fine", normale.getMotivo());
        assertFalse(normale.isErrore());
        assertTrue(new NotificaUscitaDalGioco(1, "guasto").isErrore());

        NotificaFineGioco fine = new NotificaFineGioco(true);
        assertEquals(TipoEvento.NOTIFICA_FINE_GIOCO, fine.getTipoEvento());
        assertTrue(fine.isCompletatoConSuccesso());
        assertFalse(new NotificaFineGioco(false).isCompletatoConSuccesso());

        Exception errore = new Exception("rotto");
        NotificaErroreCaricamento caricamento = new NotificaErroreCaricamento(errore);
        assertEquals(TipoEvento.NOTIFICA_ERRORE_CARICAMENTO, caricamento.getTipoEvento());
        assertSame(errore, caricamento.getException());
    }

    @Test
    void leRichiesteDiTestoHannoUnTestoPredefinito() {
        RichiestaTesto semplice = new RichiestaTesto("Nome?");
        assertEquals(TipoEvento.RICHIESTA_TESTO, semplice.getTipoEvento());
        assertEquals("Nome?", semplice.getRichiesta());
        assertEquals("", semplice.getTestoPredefinito());
        assertEquals("", new RichiestaTesto("Nome?", null).getTestoPredefinito());
        assertEquals("Ugo", new RichiestaTesto("Nome?", "Ugo").getTestoPredefinito());
    }

    @Test
    void leRichiesteConScelta() {
        RichiestaSelezioneSiNo siNo = new RichiestaSelezioneSiNo();
        assertEquals(TipoEvento.RICHIESTA_SELEZIONE_SI_O_NO, siNo.getTipoEvento());
        assertEquals(SI_NO, siNo.getPossibilita());

        RichiestaUscitaDalGioco uscita = new RichiestaUscitaDalGioco();
        assertEquals(TipoEvento.RICHIESTA_USCITA_DAL_GIOCO, uscita.getTipoEvento());
        assertEquals(SI_NO, uscita.getPossibilita());

        Collection<Comando> direzioni = Arrays.asList(Comando.FUGA);
        RichiestaSelezioneDirezione direzione = new RichiestaSelezioneDirezione(direzioni);
        assertEquals(TipoEvento.RICHIESTA_SELEZIONE_DIREZIONE, direzione.getTipoEvento());
        assertEquals(direzioni, direzione.getPossibilita());

        RichiestaAperturaInventarioGruppo inventario = new RichiestaAperturaInventarioGruppo(direzioni, null, null);
        assertEquals(TipoEvento.RICHIESTA_APERTURA_INVENTARIO_GRUPPO, inventario.getTipoEvento());
        assertNull(inventario.getScambio());
        assertNull(inventario.getPersonaggio());
    }

    @Test
    void leRichiesteDiSlotPortanoISalvataggi() {
        Collection<TestataSalvataggio> salvataggi = Collections.singletonList(new TestataSalvataggio(Comando.SI, "uno", null));
        RichiestaSelezioneSlotPerRilettura rilettura = new RichiestaSelezioneSlotPerRilettura(salvataggi);
        assertEquals(TipoEvento.RICHIESTA_SELEZIONE_SLOT_PER_RILETTURA, rilettura.getTipoEvento());
        assertSame(salvataggi, rilettura.getSalvataggiDisponibili());
        RichiestaSelezioneSlotPerSalvataggio salvataggio = new RichiestaSelezioneSlotPerSalvataggio(salvataggi);
        assertEquals(TipoEvento.RICHIESTA_SELEZIONE_SLOT_PER_SALVATAGGIO, salvataggio.getTipoEvento());
        assertSame(salvataggi, salvataggio.getSalvataggiDisponibili());
    }

    @Test
    void iComandiDelGiocatorePortanoIlLoroDato() {
        ComandoSpostaPersonaggio sposta = new ComandoSpostaPersonaggio("p1", true);
        assertEquals(TipoEvento.COMANDO_SPOSTA_PERSONAGGIO, sposta.getTipoEvento());
        assertEquals("p1", sposta.getUuidPersonaggio());
        assertTrue(sposta.isVersoDestra());

        ComandoImpostazioneAiuto aiuto = new ComandoImpostazioneAiuto(true);
        assertEquals(TipoEvento.COMANDO_IMPOSTAZIONE_AIUTO, aiuto.getTipoEvento());
        assertTrue(aiuto.isAbilitato());

        ComandoInvioTesto testo = new ComandoInvioTesto("ciao");
        assertEquals(TipoEvento.COMANDO_INVIO_TESTO, testo.getTipoEvento());
        assertEquals("ciao", testo.getTesto());

        ComandoDiGioco comando = new ComandoDiGioco(Comando.FUGA);
        assertEquals(TipoEvento.COMANDO_DI_GIOCO, comando.getTipoEvento());
        assertSame(Comando.FUGA, comando.getComando());

        ComandoScambioArtefatto scambio = new ComandoScambioArtefatto("s", ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, "a1");
        assertEquals(TipoEvento.COMANDO_SCAMBIO_ARTEFATTO, scambio.getTipoEvento());
        assertEquals("s", scambio.getIdScambio());
        assertSame(ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, scambio.getDestinazione());
        assertEquals("a1", scambio.getUuidArtefatto());
    }

    @Test
    void laCommutazioneRiguardaUnArtefattoOUnaMissione() {
        ComandoCommutazioneElenco artefatto = ComandoCommutazioneElenco.artefatto("s", "a1");
        assertEquals(TipoEvento.COMANDO_COMMUTAZIONE_ELENCO, artefatto.getTipoEvento());
        assertEquals("s", artefatto.getIdScambio());
        assertEquals("a1", artefatto.getUuidArtefatto());
        assertNull(artefatto.getIdMissione());

        ComandoCommutazioneElenco missione = ComandoCommutazioneElenco.missione("m1");
        assertEquals("m1", missione.getIdMissione());
        assertNull(missione.getIdScambio());
        assertNull(missione.getUuidArtefatto());
    }
}
