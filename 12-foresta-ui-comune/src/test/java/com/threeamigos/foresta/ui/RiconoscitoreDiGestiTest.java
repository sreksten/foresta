package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiconoscitoreDiGestiTest {

	/** Registra gli eventi che arrivano al router */
	private static final class Registro implements EventiDelPuntatore {
		final List<String> eventi = new ArrayList<>();
		int intervallo = RouterDegliInput.INTERVALLO_DOPPIO_TOCCO;

		@Override
		public void mosso(int x, int y) {
			eventi.add("mosso " + x + "," + y);
		}

		@Override
		public void mouseUscito(int x, int y) {
			eventi.add("uscito " + x + "," + y);
		}

		@Override
		public void premuto(int x, int y, Finestra.Tasto tasto) {
			eventi.add("premuto " + x + "," + y);
		}

		@Override
		public void trascinato(int x, int y) {
			eventi.add("trascinato " + x + "," + y);
		}

		@Override
		public void rilasciato(int x, int y, Finestra.Tasto tasto) {
			eventi.add("rilasciato " + x + "," + y);
		}

		@Override
		public void clic(int x, int y, Finestra.Tasto tasto, int conteggio) {
			eventi.add("clic" + conteggio + " " + x + "," + y);
		}

		@Override
		public void rotella(int x, int y, int rotazioni) {
			eventi.add("rotella " + rotazioni);
		}

		@Override
		public int getIntervalloDoppioClick() {
			return intervallo;
		}
	}

	/** Un pianificatore che scatta solo quando glielo si dice */
	private static final class PianificatoreManuale implements Pianificatore {
		final List<Runnable> azioni = new ArrayList<>();
		final List<boolean[]> attive = new ArrayList<>();
		int ultimoRitardo;

		@Override
		public Pianificata dopo(int millisecondi, Runnable azione) {
			ultimoRitardo = millisecondi;
			boolean[] attiva = {true};
			azioni.add(azione);
			attive.add(attiva);
			return new Pianificata() {
				@Override
				public void annulla() {
					attiva[0] = false;
				}

				@Override
				public boolean isAttiva() {
					return attiva[0];
				}
			};
		}

		@Override
		public Pianificata ogni(int millisecondi, Runnable azione) {
			throw new UnsupportedOperationException();
		}

		/** Fa scattare le azioni ancora attive */
		void passaIlTempo() {
			for (int i = 0; i < azioni.size(); i++) {
				if (attive.get(i)[0]) {
					attive.get(i)[0] = false;
					azioni.get(i).run();
				}
			}
		}
	}

	private final Registro registro = new Registro();
	private final PianificatoreManuale pianificatore = new PianificatoreManuale();
	private long ora = 1000;

	private RiconoscitoreDiGesti dito() {
		return new RiconoscitoreDiGesti(registro, pianificatore, true, () -> ora);
	}

	private RiconoscitoreDiGesti mouse() {
		return new RiconoscitoreDiGesti(registro, pianificatore, false, () -> ora);
	}

	@Test
	void colMouseOgniEventoPassaComeE() {
		RiconoscitoreDiGesti gesti = mouse();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		gesti.trascinato(11, 10);
		gesti.rilasciato(11, 10, Finestra.Tasto.SINISTRO);
		assertEquals(java.util.Arrays.asList("premuto 10,10", "trascinato 11,10", "rilasciato 11,10", "clic1 11,10"), registro.eventi);
		assertTrue(pianificatore.azioni.isEmpty(), "col mouse non c'e' pressione prolungata");
	}

	@Test
	void laRotellaPassaAlRouter() {
		mouse().rotella(5, 5, -2);
		assertEquals(java.util.Collections.singletonList("rotella -2"), registro.eventi);
	}

	@Test
	void colMouseUnTrascinamentoOltreLaSogliaNonEUnClick() {
		RiconoscitoreDiGesti gesti = mouse();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		gesti.trascinato(40, 10);
		gesti.rilasciato(40, 10, Finestra.Tasto.SINISTRO);
		assertEquals(java.util.Arrays.asList("premuto 10,10", "trascinato 40,10", "rilasciato 40,10"), registro.eventi);
	}

	@Test
	void colDitoLaPressioneArrivaAlRilascioEPoiIlClick() {
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		assertTrue(registro.eventi.isEmpty(), "la pressione non si consegna subito");
		assertEquals(RiconoscitoreDiGesti.PRESSIONE_PROLUNGATA_MS, pianificatore.ultimoRitardo);
		gesti.rilasciato(10, 10, Finestra.Tasto.SINISTRO);
		assertEquals(java.util.Arrays.asList("premuto 10,10", "rilasciato 10,10", "clic1 10,10"), registro.eventi);
		pianificatore.passaIlTempo();
		assertEquals(3, registro.eventi.size(), "rilasciato in tempo, la pressione prolungata e' annullata");
	}

	@Test
	void colDitoIlTremolioNonETrascinamento() {
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		gesti.trascinato(14, 12);
		assertTrue(registro.eventi.isEmpty());
		gesti.rilasciato(14, 12, Finestra.Tasto.SINISTRO);
		assertEquals(java.util.Arrays.asList("premuto 10,10", "rilasciato 14,12", "clic1 14,12"), registro.eventi);
	}

	@Test
	void colDitoOltreLaSogliaLaPressioneArrivaDalPuntoDiPartenzaEIlTrascinamentoSegue() {
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		gesti.trascinato(30, 10);
		gesti.trascinato(50, 10);
		gesti.rilasciato(50, 10, Finestra.Tasto.SINISTRO);
		assertEquals(java.util.Arrays.asList("premuto 10,10", "trascinato 30,10", "trascinato 50,10", "rilasciato 50,10"), registro.eventi);
		pianificatore.passaIlTempo();
		assertEquals(4, registro.eventi.size(), "muovendosi la pressione prolungata e' annullata");
	}

	@Test
	void laPressioneProlungataMostraLeDescrizioniEIlRilascioLeChiudeSenzaClick() {
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		pianificatore.passaIlTempo();
		assertTrue(gesti.isPressioneProlungata());
		assertEquals(java.util.Arrays.asList("mosso 10,10"), registro.eventi);
		gesti.rilasciato(10, 10, Finestra.Tasto.SINISTRO);
		assertFalse(gesti.isPressioneProlungata());
		// la finestra non ha mai visto premuto ne' rilasciato (la barra delle icone esegue il comando al rilascio) e non c'e' nessun click
		assertEquals(java.util.Arrays.asList("mosso 10,10", "uscito 10,10"), registro.eventi);
	}

	@Test
	void durantePressioneProlungataLeDescrizioniSeguonoIlDito() {
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		pianificatore.passaIlTempo();
		gesti.trascinato(60, 10);
		gesti.rilasciato(60, 10, Finestra.Tasto.SINISTRO);
		assertEquals(java.util.Arrays.asList("mosso 10,10", "mosso 60,10", "uscito 60,10"), registro.eventi);
	}

	@Test
	void unaPressioneProlungataNonContaComeClickPerIlDoppio() {
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		pianificatore.passaIlTempo();
		gesti.rilasciato(10, 10, Finestra.Tasto.SINISTRO);
		registro.eventi.clear();
		ora += 100;
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		gesti.rilasciato(10, 10, Finestra.Tasto.SINISTRO);
		assertEquals(java.util.Arrays.asList("premuto 10,10", "rilasciato 10,10", "clic1 10,10"), registro.eventi);
	}

	@Test
	void ilSecondoTocco_entroLIntervalloEVicino_ELSecondoClick() {
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		gesti.rilasciato(10, 10, Finestra.Tasto.SINISTRO);
		ora += registro.intervallo;
		gesti.premuto(20, 12, Finestra.Tasto.SINISTRO);
		gesti.rilasciato(20, 12, Finestra.Tasto.SINISTRO);
		assertEquals("clic2 20,12", registro.eventi.get(registro.eventi.size() - 1));
	}

	@Test
	void ilSecondoToccoOltreLIntervalloDelRouterENuovoPrimoClick() {
		// Il router ha gia' eseguito il click singolo: contarlo come secondo farebbe scattare anche il doppio
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		gesti.rilasciato(10, 10, Finestra.Tasto.SINISTRO);
		ora += registro.intervallo + 1;
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		gesti.rilasciato(10, 10, Finestra.Tasto.SINISTRO);
		assertEquals("clic1 10,10", registro.eventi.get(registro.eventi.size() - 1));
	}

	@Test
	void ilSecondoToccoLontanoENuovoPrimoClick() {
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
		gesti.rilasciato(10, 10, Finestra.Tasto.SINISTRO);
		ora += 50;
		gesti.premuto(10 + RiconoscitoreDiGesti.DISTANZA_FRA_I_CLICK + 1, 10, Finestra.Tasto.SINISTRO);
		gesti.rilasciato(10 + RiconoscitoreDiGesti.DISTANZA_FRA_I_CLICK + 1, 10, Finestra.Tasto.SINISTRO);
		assertTrue(registro.eventi.get(registro.eventi.size() - 1).startsWith("clic1"));
	}

	@Test
	void unTerzoToccoRicominciaDaCapo() {
		RiconoscitoreDiGesti gesti = dito();
		for (int i = 0; i < 3; i++) {
			gesti.premuto(10, 10, Finestra.Tasto.SINISTRO);
			gesti.rilasciato(10, 10, Finestra.Tasto.SINISTRO);
			ora += 50;
		}
		List<String> clic = new ArrayList<>();
		for (String evento : registro.eventi) {
			if (evento.startsWith("clic")) {
				clic.add(evento.substring(0, 5));
			}
		}
		assertEquals(java.util.Arrays.asList("clic1", "clic2", "clic1"), clic);
	}

	@Test
	void ilTastoDestroColDitoNonAspettaLaPressioneProlungata() {
		RiconoscitoreDiGesti gesti = dito();
		gesti.premuto(10, 10, Finestra.Tasto.DESTRO);
		assertEquals(java.util.Arrays.asList("premuto 10,10"), registro.eventi);
		assertTrue(pianificatore.azioni.isEmpty());
	}
}
