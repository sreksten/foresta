package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.GestorePunteggiInMemoria;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tipi.TipoAttributo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Il riquadro del Gruppo viene davanti agli altri quando cambiano le statistiche di uno dei miei, non quelle di un mostro.
 */
class GruppoDavantiSoloPerIMieiTest {

	private DisplayableCanvas canvas;

	@BeforeEach
	void creaCanvas() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		ModelloDati.setIstanza(new ModelloDati());
		GruppoGiocatore.azzeraIstanza();
		canvas = new DisplayableCanvas(1024, 768, DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, 72, false,
				new GestorePunteggiInMemoria(), new VistaPartitaMotore());
	}

	@AfterEach
	void ripristinaBus() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
		GruppoGiocatore.azzeraIstanza();
	}

	private TipoFinestra davanti() {
		List<TipoFinestra> impilati = canvas.riquadriImpilati();
		return impilati.get(impilati.size() - 1);
	}

	@Test
	void leStatisticheDiUnMostroNonPortanoDavantiIlGruppo() {
		canvas.primoPiano(TipoFinestra.MISSIONI);
		assertEquals(TipoFinestra.MISSIONI, davanti());

		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(new Goblin(1), TipoAttributo.SALUTE, 10, 5));

		assertEquals(TipoFinestra.MISSIONI, davanti());
	}

	@Test
	void leStatisticheDiUnoDeiMieiPortanoDavantiIlGruppo() {
		Guerriero guerriero = new Guerriero("Arsenio", 1);
		GruppoGiocatore.getIstanza().aggiungiPersonaggio(guerriero);
		canvas.primoPiano(TipoFinestra.MISSIONI);

		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(guerriero, TipoAttributo.SALUTE, 10, 5));

		assertEquals(TipoFinestra.STATO, davanti());
	}
}
