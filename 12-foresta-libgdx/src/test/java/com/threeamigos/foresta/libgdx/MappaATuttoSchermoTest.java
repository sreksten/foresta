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
	void ilNotiziarioSiAlzaSoloFinoAlBordoDellaMappaPiuIlGapSenzaToccarla() {
		int inFondo = mappa.altezzaMappa();
		assertEquals(inFondo, mappa.yDelNotiziario(0), "senza sollevamento sta in fondo");
		int fineDellaMappa = mappa.getMappaYOffset() + partita.getMappa().getDimensioneY() * ICONA;
		int yMassimo = mappa.yDelNotiziario(10_000);
		if (fineDellaMappa + Finestra.SPACING >= inFondo) {
			// La mappa riempie l'area: non c'e' spazio sotto, la fascia resta in fondo
			assertEquals(inFondo, yMassimo);
		} else {
			assertEquals(fineDellaMappa + Finestra.SPACING, yMassimo, "bordo della mappa piu' il gap");
		}
		assertTrue(yMassimo >= fineDellaMappa, "la fascia non copre mai la mappa se questa sta nell'area");
		// il sollevamento massimo e' un tetto
		assertTrue(mappa.yDelNotiziario(5) >= inFondo - 5);
		// la mappa non cambia
		assertEquals(inFondo, mappa.altezzaMappa());
	}

	@Test
	void conUnaMappaPiuGrandeDelloSchermoIlNotiziarioRestaInFondo() {
		MappaATuttoSchermo piccola = new MappaATuttoSchermo(LARGHEZZA, 60, partita, ICONA, ICONA);
		piccola.centraSuGiocatore();
		assertEquals(piccola.altezzaMappa(), piccola.yDelNotiziario(70), "senza spazio libero sotto la mappa non si alza");
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
	void ilJoystickVuoleLaPressioneSubitoSoloNelSuoDisco() {
		// Senza joystick niente: col dito la pressione arriva al rilascio o al primo movimento
		assertFalse(mappa.vuoleLaPressioneSubito(LARGHEZZA - 50, ALTEZZA - 50));
		mappa.abilitaJoystickVirtuale();
		int cx = mappa.getJoystick().getCentroX();
		int cy = mappa.getJoystick().getCentroY();
		assertTrue(mappa.vuoleLaPressioneSubito(cx, cy), "al centro del disco");
		assertTrue(mappa.vuoleLaPressioneSubito(cx + 90, cy), "sul bordo del disco, dove un dito fermo deve gia' muovere la mano");
		assertFalse(mappa.vuoleLaPressioneSubito(cx - 150, cy), "fuori dal disco si trascina la mappa");
	}

	@Test
	void laSchermataInoltraAlModelloLaRichiestaDellaPressioneSubito() throws NoSuchMethodException {
		// Il router parla con la schermata, non con il modello: se non inoltra, col dito il joystick non si attiva mai (e' gia' successo).
		// Costruirla chiede il contesto grafico, quindi si controlla che il metodo ci sia, come per gli altri che inoltra.
		assertEquals(SchermataMappaGdx.class, SchermataMappaGdx.class.getDeclaredMethod("vuoleLaPressioneSubito", int.class, int.class)
				.getDeclaringClass());
		assertEquals(SchermataMappaGdx.class, SchermataMappaGdx.class.getDeclaredMethod("catturaIlTrascinamento").getDeclaringClass());
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
