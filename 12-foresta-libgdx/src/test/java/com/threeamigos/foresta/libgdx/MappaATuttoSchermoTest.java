package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.schermate.MappaATuttoSchermo;
import com.threeamigos.foresta.ui.schermate.StrisciaDelleNotizie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La mappa a tutto schermo e il notiziario, sui dati di prova: il modello è del modulo comune, qui si prova con la partita finta di libGDX.
 */
class MappaATuttoSchermoTest {

	private static final int LARGHEZZA = 800;
	private static final int ALTEZZA = 480;
	private static final int ICONA = 16;

	private VistaPartita partita;
	private MappaATuttoSchermo mappa;

	@BeforeEach
	void prepara() {
		partita = DatiDiProva.partita();
		mappa = new MappaATuttoSchermo(LARGHEZZA, ALTEZZA, partita, ICONA, ICONA);
		mappa.centraSuGiocatore();
	}

	@Test
	void conLeNotizieLaFasciaInBassoTogliePostoAllaMappa() {
		assertFalse(partita.getUltimeNotizie().isEmpty());
		assertTrue(mappa.isNotiziarioVisibile());
		assertEquals(ALTEZZA - ICONA, mappa.altezzaMappa());
	}

	@Test
	void ilGruppoStaDentroLoSchermoDopoLaCentratura() {
		assertTrue(mappa.getSegnalino().x >= 0 && mappa.getSegnalino().x < LARGHEZZA);
		assertTrue(mappa.getSegnalino().y >= 0 && mappa.getSegnalino().y < mappa.altezzaMappa());
		assertFalse(mappa.getTessere().isEmpty(), "qualche casella è conosciuta");
	}

	@Test
	void trascinandoLaMappaNonSiEsceDaiBordi() {
		mappa.processaPressione(100, 100, Finestra.Tasto.SINISTRO);
		mappa.processaTrascinamento(100 + 5000, 100 + 5000);
		mappa.processaRilascio(5100, 5100, Finestra.Tasto.SINISTRO);
		int larghezzaMappa = partita.getMappa().getDimensioneX() * ICONA;
		if (larghezzaMappa > LARGHEZZA) {
			assertTrue(mappa.getMappaXOffset() <= 0);
		} else {
			assertEquals((LARGHEZZA - larghezzaMappa) / 2, mappa.getMappaXOffset());
		}
	}

	@Test
	void ilJoystickFaComparireLaManoEFinoAlRilascio() {
		mappa.abilitaJoystickVirtuale();
		assertFalse(mappa.isManoVisibile());
		int cx = mappa.getJoystick().getCentroX();
		int cy = mappa.getJoystick().getCentroY();
		mappa.processaPressione(cx, cy, Finestra.Tasto.SINISTRO);
		assertTrue(mappa.isManoVisibile());
		assertEquals(LARGHEZZA / 2, mappa.getXMouse(), "la mano parte dal centro");
		mappa.processaTrascinamento(cx + 90, cy);
		mappa.aggiornaLaMano(1_000_000_000L);
		mappa.aggiornaLaMano(1_050_000_000L);
		assertTrue(mappa.getXMouse() > LARGHEZZA / 2, "col puntatore a destra la mano va a destra");
		mappa.processaRilascio(cx, cy, Finestra.Tasto.SINISTRO);
		assertFalse(mappa.isManoVisibile());
	}

	@Test
	void ilNotiziarioScorreDelTempoPassatoERipartePerIlGiroContinuo() {
		StrisciaDelleNotizie striscia = new StrisciaDelleNotizie(LARGHEZZA, partita);
		striscia.avanza(1_000_000_000L);
		assertFalse(striscia.getPezzi().isEmpty());
		assertTrue(striscia.getLarghezzaTotale() > 0);
		assertEquals(LARGHEZZA, striscia.getX(), "ricostruita, riparte da destra");
		striscia.avanza(2_000_000_000L);
		assertEquals(LARGHEZZA - StrisciaDelleNotizie.VELOCITA_SCORRIMENTO_PX_PER_SECONDO, striscia.getX(), "un secondo di scorrimento");
		striscia.avanza(2_000_000_000L + 1_000_000_000L * 100_000);
		assertTrue(striscia.getX() > -striscia.getLarghezzaTotale() && striscia.getX() <= LARGHEZZA, "il giro ricomincia, non esce mai");
	}
}
