package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoUiOccupata;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Riproduce il bug per cui un annuncio globale (es. "NUOVA MISSIONE", attivata da un
 * controllo dell'Automa a cascata) richiesto mentre è ancora a schermo l'ultima pagina
 * di un intermezzo veniva disegnato subito, sovrapponendosi all'intermezzo invece di
 * aspettare che la UI sia tornata al gioco.
 */
class DisplayableCanvasAnnuncioGlobaleTest {

	private DisplayableCanvas canvas;
	private AtomicInteger uiOccupataPubblicati;

	@BeforeEach
	void creaCanvas() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		canvas = new DisplayableCanvas(1024, 768, DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, 72, false);
		uiOccupataPubblicati = new AtomicInteger();
		BusEventi.iscriviti(InternoUiOccupata.class, e -> uiOccupataPubblicati.incrementAndGet());
	}

	@AfterEach
	void ripristinaBus() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_EDT);
	}

	@Test
	void unAnnuncioRichiestoDuranteUnIntermezzoAspettaDiTornareAlGioco() {
		canvas.mostraPaginaIntermezzo(new PaginaIntermezzo());

		canvas.notificaAnnuncioGlobale("NUOVA MISSIONE", "Missione di prova");

		assertEquals(0, uiOccupataPubblicati.get(),
				"l'annuncio non deve comparire finché l'intermezzo è ancora a schermo");

		canvas.iniziaGioco();

		assertEquals(1, uiOccupataPubblicati.get(),
				"appena si torna al gioco l'annuncio rimandato deve comparire");
	}

	@Test
	void unAnnuncioFuoriDaUnIntermezzoComparesubito() {
		canvas.notificaAnnuncioGlobale("LIVELLO SUCCESSIVO", "Notizia di prova");

		assertEquals(1, uiOccupataPubblicati.get(),
				"fuori da un intermezzo l'annuncio deve comparire subito, come prima di questa modifica");
	}
}
