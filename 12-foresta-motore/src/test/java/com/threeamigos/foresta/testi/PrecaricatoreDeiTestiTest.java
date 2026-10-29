package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.eventi.notifiche.NotificaUscitaDalGioco;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Se una grammatica del gioco manca, il precaricamento lo segnala sul bus (prima l'eccezione, poi la richiesta di uscire) con un
 * messaggio che dice quale grammatica e perché; altrimenti non succede niente.
 */
class PrecaricatoreDeiTestiTest {

	private final List<Object> eventi = new ArrayList<>();
	private Runnable iscrizioneEccezioni;
	private Runnable iscrizioneUscita;

	@BeforeEach
	void ascolta() {
		BusEventi.impostaConsegna(Runnable::run);
		Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
		iscrizioneEccezioni = BusEventi.iscriviti(InternoException.class, eventi::add);
		iscrizioneUscita = BusEventi.iscriviti(NotificaUscitaDalGioco.class, eventi::add);
	}

	@AfterEach
	void smetti() {
		iscrizioneEccezioni.run();
		iscrizioneUscita.run();
		Logger.impostaDestinazione(null);
		BusEventi.impostaConsegna(null);
	}

	@Test
	void ilPrecaricamentoDelleGrammaticheDelGiocoNonSegnalaNiente() {
		PrecaricatoreDeiTesti.precarica();

		assertTrue(eventi.isEmpty(), eventi.toString());
	}

	@Test
	void unaGrammaticaCheMancaSiSegnalaConUnMessaggioParlante() {
		PrecaricatoreDeiTesti.precarica(() -> CaricatoreDiGrammatiche.caricaDeiTesti("non_esiste.txt", false));

		assertEquals(2, eventi.size(), eventi.toString());
		assertTrue(eventi.get(0) instanceof InternoException, "prima l'eccezione...");
		assertTrue(eventi.get(1) instanceof NotificaUscitaDalGioco, "...poi la richiesta di uscire");
		String messaggio = ((InternoException) eventi.get(0)).getMessaggio();
		assertTrue(messaggio.contains("non_esiste.txt") && messaggio.contains("Grammar resource not found"), messaggio);
	}

	@Test
	void unErroreCheNonRiguardaLeGrammaticheNonSiNasconde() {
		assertThrows(IllegalStateException.class, () -> PrecaricatoreDeiTesti.precarica(() -> {
			throw new IllegalStateException("altro");
		}));
	}
}
