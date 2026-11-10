package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoUiInattiva;
import com.threeamigos.foresta.eventi.interni.InternoUiOccupata;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.motore.GestorePunteggiInMemoria;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ogni {@code InternoUiOccupata} che la UI manda all'Automa deve essere seguito, appena la UI è libera, da un {@code InternoUiInattiva}:
 * se no l'Automa resta convinto che la UI sia occupata e l'intermezzo successivo aspetta per sempre (gioco fermo, nessun comando). Succedeva
 * con la dotazione dell'eremita, i cui sprite nascono durante il suo intermezzo, dove non contano: la UI non si era segnata di averlo detto,
 * e alla seconda locazione l'intermezzo della locanda o della città non partiva più.
 */
class UiOccupataSempreSeguitaDaInattivaTest {

	private DisplayableCanvas canvas;
	private AtomicInteger occupata;
	private AtomicInteger inattiva;

	@BeforeEach
	void creaCanvas() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		canvas = new DisplayableCanvas(com.threeamigos.foresta.tipi.Orientamento.ORIZZONTALE, 1024, 768, false,
				new GestorePunteggiInMemoria(), new VistaPartitaMotore());
		occupata = new AtomicInteger();
		inattiva = new AtomicInteger();
		BusEventi.iscriviti(InternoUiOccupata.class, e -> occupata.incrementAndGet());
		BusEventi.iscriviti(InternoUiInattiva.class, e -> inattiva.incrementAndGet());
	}

	@AfterEach
	void ripristinaBus() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
	}

	@Test
	void durante_un_intermezzo_la_ui_occupata_torna_libera_al_fotogramma_successivo() {
		canvas.getSchermo().mostraPaginaIntermezzo(new PaginaIntermezzo());

		canvas.getSchermo().segnalaUiOccupata();
		canvas.getSchermo().fineFotogramma();

		assertEquals(1, occupata.get());
		assertEquals(1, inattiva.get(), "l'Automa deve sapere che la UI è di nuovo libera, se no l'intermezzo successivo non parte");
	}

	@Test
	void sulla_schermata_di_gioco_senza_niente_in_corso_la_ui_torna_libera() {
		canvas.getSchermo().iniziaGioco();

		canvas.getSchermo().segnalaUiOccupata();
		canvas.getSchermo().fineFotogramma();
		canvas.getSchermo().fineFotogramma();

		assertEquals(1, inattiva.get(), "una sola volta: il secondo fotogramma non ha niente da dire");
	}
}
