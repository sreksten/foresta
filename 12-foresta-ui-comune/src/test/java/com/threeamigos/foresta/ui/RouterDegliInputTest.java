package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouterDegliInputTest {

	/** Una finestra che registra quel che riceve */
	private static class Finta implements Finestra {
		final String nome;
		final List<String> eventi = new ArrayList<>();
		boolean doppioClick;
		boolean catturaTrascinamento;
		boolean visibile = true;
		int passi = 1;

		Finta(String nome) {
			this.nome = nome;
		}

		@Override
		public boolean isVisibile() {
			return visibile;
		}

		@Override
		public boolean gestisceDoppioClick() {
			return doppioClick;
		}

		@Override
		public boolean catturaIlTrascinamento() {
			return catturaTrascinamento;
		}

		@Override
		public int passiPerScattoDellaRotella() {
			return passi;
		}

		@Override
		public void processaEntrata(int x, int y) {
			eventi.add("entrata " + x + "," + y);
		}

		@Override
		public void processaUscita(int x, int y) {
			eventi.add("uscita " + x + "," + y);
		}

		@Override
		public void processaMovimento(int x, int y) {
			eventi.add("movimento " + x + "," + y);
		}

		@Override
		public void processaTrascinamento(int x, int y) {
			eventi.add("trascinamento " + x + "," + y);
		}

		@Override
		public void processaPressione(int x, int y, Tasto tasto) {
			eventi.add("pressione " + x + "," + y + " " + tasto);
		}

		@Override
		public void processaRilascio(int x, int y, Tasto tasto) {
			eventi.add("rilascio " + x + "," + y + " " + tasto);
		}

		@Override
		public void processaClick(int x, int y, Tasto tasto) {
			eventi.add("click " + x + "," + y);
		}

		@Override
		public void processaDoppioClick(int x, int y, Tasto tasto) {
			eventi.add("doppio " + x + "," + y);
		}

		@Override
		public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
			eventi.add("rotella " + numeroRotazioni + " " + movimentoRotella);
		}
	}

	/** Un orologio e dei timer manuali */
	private static class Manuale implements Pianificatore {
		final List<Runnable> doppioni = new ArrayList<>();
		long ora;
		final List<Object[]> compiti = new ArrayList<>(); // {scadenza, periodo, azione, attiva[]}

		@Override
		public Pianificata dopo(int ms, Runnable azione) {
			return aggiungi(ms, 0, azione);
		}

		@Override
		public Pianificata ogni(int ms, Runnable azione) {
			return aggiungi(ms, ms, azione);
		}

		private Pianificata aggiungi(int ms, int periodo, Runnable azione) {
			boolean[] attiva = {true};
			compiti.add(new Object[]{ora + ms, periodo, azione, attiva});
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

		void avanza(int ms) {
			long fine = ora + ms;
			boolean fatto = true;
			while (fatto) {
				fatto = false;
				for (Object[] c : new ArrayList<>(compiti)) {
					boolean[] attiva = (boolean[]) c[3];
					if (attiva[0] && (Long) c[0] <= fine) {
						ora = Math.max(ora, (Long) c[0]);
						if ((Integer) c[1] > 0) {
							c[0] = (Long) c[0] + (Integer) c[1];
						} else {
							attiva[0] = false;
						}
						((Runnable) c[2]).run();
						fatto = true;
					}
				}
			}
			ora = fine;
		}
	}

	private final Map<Finestra, Area> aree = new HashMap<>();
	private final List<Finestra> pila = new ArrayList<>();
	private Finestra barra;
	private Finestra aTuttoSchermo;
	private Object stato = "gioco";
	private Finestra portataDavanti;
	private int ridisegni;
	private Manuale manuale;
	private RouterDegliInput router;
	private Finta sotto;
	private Finta sopra;
	// La pila che scorre di lato, i riquadri che scorrono con lei e la zona della sua freccia (come sul telefono)
	private PilaScorrevole pilaScorrevole;
	private final List<Finestra> nellaPila = new ArrayList<>();
	private Area freccia;

	@BeforeEach
	void prepara() {
		sotto = new Finta("sotto");
		sopra = new Finta("sopra");
		aree.put(sotto, new Area(0, 0, 100, 100));
		aree.put(sopra, new Area(50, 50, 100, 100));
		pila.clear();
		pila.addAll(Arrays.asList(sotto, sopra));
		barra = null;
		aTuttoSchermo = null;
		pilaScorrevole = null;
		nellaPila.clear();
		freccia = null;
		manuale = new Manuale();
		router = new RouterDegliInput(new RouterDegliInput.Contesto() {
			@Override
			public Finestra getBarraIcone() {
				return barra;
			}

			@Override
			public Finestra getFinestraATuttoSchermo() {
				return aTuttoSchermo;
			}

			@Override
			public List<Finestra> getFinestreImpilate() {
				return pila;
			}

			@Override
			public Area getArea(Finestra finestra) {
				return aree.get(finestra);
			}

			@Override
			public boolean portaInPrimoPianoSeImpilato(Finestra finestra) {
				int posizione = pila.indexOf(finestra);
				if (posizione < 0 || posizione == pila.size() - 1) {
					return false;
				}
				pila.remove(finestra);
				pila.add(finestra);
				portataDavanti = finestra;
				return true;
			}

			@Override
			public PilaScorrevole getPilaScorrevole() {
				return pilaScorrevole;
			}

			@Override
			public boolean scorreConLaPila(Finestra finestra) {
				return nellaPila.contains(finestra);
			}

			@Override
			public boolean isSullaFrecciaDellaPila(int x, int y) {
				return freccia != null && freccia.contiene(x, y);
			}

			@Override
			public Object getStato() {
				return stato;
			}

			@Override
			public void ridisegna() {
				ridisegni++;
			}
		}, manuale);
	}

	@Test
	void lEventoSpettaAllaFinestraPiuInPrimoPianoCheContieneIlPunto() {
		router.mosso(60, 60);
		assertEquals(Arrays.asList("entrata 10,10", "movimento 10,10"), sopra.eventi);
		assertTrue(sotto.eventi.isEmpty());
		router.mosso(10, 10);
		assertEquals("movimento 10,10", sotto.eventi.get(sotto.eventi.size() - 1));
	}

	@Test
	void passandoDaUnaFinestraAUnaltraSiSintetizzanoUscitaEEntrata() {
		router.mosso(10, 10);
		router.mosso(60, 60);
		assertEquals(Arrays.asList("entrata 10,10", "movimento 10,10", "uscita 60,60"), sotto.eventi);
		assertEquals(Arrays.asList("entrata 10,10", "movimento 10,10"), sopra.eventi);
		router.mouseUscito(0, 0);
		assertEquals("uscita -50,-50", sopra.eventi.get(sopra.eventi.size() - 1));
	}

	@Test
	void laBarraDelleIconeHaLaPrecedenzaEDecideDaSeQualiPuntiSonoSuoi() {
		Finta icone = new Finta("barra");
		aree.put(icone, new Area(0, 80, 200, 20));
		barra = icone;
		router.mosso(60, 90);
		assertEquals(Arrays.asList("entrata 60,10", "movimento 60,10"), icone.eventi);
		assertTrue(sopra.eventi.isEmpty());
	}

	@Test
	void conUnaFinestraATuttoSchermoLaPilaNonContaEUnaNonVisibileNonRiceveNulla() {
		Finta tutto = new Finta("tutto");
		aree.put(tutto, new Area(0, 0, 200, 200));
		aTuttoSchermo = tutto;
		router.premuto(60, 60, Finestra.Tasto.SINISTRO);
		assertEquals(Arrays.asList("pressione 60,60 SINISTRO"), tutto.eventi);
		assertTrue(sopra.eventi.isEmpty());
		aTuttoSchermo = null;
		sopra.visibile = false;
		router.premuto(60, 60, Finestra.Tasto.SINISTRO);
		assertEquals(Arrays.asList("pressione 60,60 SINISTRO"), sotto.eventi);
	}

	@Test
	void chiCatturaIlTrascinamentoLoRiceveAncheFuoriDalSuoRettangolo() {
		sopra.catturaTrascinamento = true;
		router.premuto(60, 60, Finestra.Tasto.SINISTRO);
		router.trascinato(10, 10);
		router.rilasciato(500, 500, Finestra.Tasto.SINISTRO);
		assertEquals(Arrays.asList("entrata 10,10", "pressione 10,10 SINISTRO", "trascinamento -40,-40", "rilascio 450,450 SINISTRO")
				.subList(1, 4), sopra.eventi.subList(0, 3));
		assertTrue(sotto.eventi.isEmpty());
	}

	@Test
	void unClickSuUnRiquadroImpilatoNonUltimoLoPortaDavantiENullAltro() {
		router.clic(10, 10, Finestra.Tasto.SINISTRO, 1);
		assertEquals(sotto, portataDavanti);
		assertTrue(sotto.eventi.isEmpty());
		// Adesso è lui l'ultimo: il click successivo lo riceve
		router.clic(10, 10, Finestra.Tasto.SINISTRO, 1);
		assertEquals(Arrays.asList("click 10,10"), sotto.eventi);
	}

	@Test
	void ilClickSingoloDiChiNonDistingueIlDoppioScattaSubito() {
		router.clic(60, 60, Finestra.Tasto.SINISTRO, 1);
		assertEquals(Arrays.asList("click 10,10"), sopra.eventi);
	}

	@Test
	void ilClickSingoloDiChiDistingueIlDoppioAspettaEUnDoppioClickLoScarta() {
		sopra.doppioClick = true;
		router.clic(60, 60, Finestra.Tasto.SINISTRO, 1);
		assertTrue(sopra.eventi.isEmpty());
		manuale.avanza(100);
		router.clic(60, 60, Finestra.Tasto.SINISTRO, 2);
		manuale.avanza(500);
		assertEquals(Arrays.asList("doppio 10,10"), sopra.eventi);

		sopra.eventi.clear();
		router.clic(60, 60, Finestra.Tasto.SINISTRO, 1);
		manuale.avanza(RouterDegliInput.INTERVALLO_DOPPIO_CLICK + 1);
		assertEquals(Arrays.asList("click 10,10"), sopra.eventi);
	}

	@Test
	void ilClickInAttesaNonValeSeLaSchermataCambia() {
		sopra.doppioClick = true;
		router.clic(60, 60, Finestra.Tasto.SINISTRO, 1);
		stato = "altra";
		manuale.avanza(500);
		assertTrue(sopra.eventi.isEmpty());
	}

	@Test
	void laRotellaDiChiScorreAPassiSiDivideInPassiPerFotogramma() {
		sopra.passi = 6;
		router.rotella(60, 60, 1);
		assertTrue(sopra.eventi.isEmpty(), "niente subito: i passi arrivano coi fotogrammi");
		manuale.avanza(1000);
		int totale = 0;
		for (String evento : sopra.eventi) {
			assertTrue(evento.endsWith("GIU"), evento);
			totale += Integer.parseInt(evento.split(" ")[1]);
		}
		assertEquals(6, totale);
		assertTrue(sopra.eventi.size() > 1, "in più di un fotogramma");
	}

	@Test
	void laRotellaDiChiScorreAScattiArrivaSubitoEConIlSegnoGiusto() {
		router.rotella(60, 60, -3);
		assertEquals(Arrays.asList("rotella 3 SU"), sopra.eventi);
	}

	/** Sopra scorre con la pila, che compattata se ne fa vedere 20 pixel */
	private void conLaPila() {
		pilaScorrevole = new PilaScorrevole(100);
		nellaPila.add(sopra);
		sopra.catturaTrascinamento = true;
	}

	@Test
	void unTrascinamentoOrizzontaleSuUnRiquadroDellaPilaLaFaScorrereEIlRiquadroLoLascia() {
		conLaPila();
		router.premuto(60, 60, Finestra.Tasto.SINISTRO);
		router.trascinato(66, 61);
		assertEquals(0, pilaScorrevole.getScostamento(), "entro la soglia del click il gesto non è ancora di nessuno");
		router.trascinato(75, 62);
		// Il riquadro, che magari stava facendo scorrere la sua lista, riceve il rilascio dove si era premuto
		assertEquals("rilascio 10,10 SINISTRO", sopra.eventi.get(sopra.eventi.size() - 1));
		assertEquals(15, pilaScorrevole.getScostamento());
		int eventiDelRiquadro = sopra.eventi.size();
		router.trascinato(90, 70);
		assertEquals(30, pilaScorrevole.getScostamento(), "da qui la pila segue il dito, anche se va un po' in verticale");
		router.rilasciato(90, 70, Finestra.Tasto.SINISTRO);
		assertFalse(sopra.eventi.subList(eventiDelRiquadro, sopra.eventi.size()).stream().anyMatch(e -> e.startsWith("trascinamento")
				|| e.startsWith("rilascio")), "il riquadro non vede più il gesto");
		pilaScorrevole.avanza(1);
		assertEquals(80, pilaScorrevole.getScostamento(), "lasciata va fino in fondo");
	}

	@Test
	void unTrascinamentoVerticaleSuUnRiquadroDellaPilaRestaDelRiquadro() {
		conLaPila();
		router.premuto(60, 60, Finestra.Tasto.SINISTRO);
		router.trascinato(62, 75);
		router.trascinato(90, 80);
		router.rilasciato(90, 80, Finestra.Tasto.SINISTRO);
		assertEquals(0, pilaScorrevole.getScostamento());
		assertFalse(pilaScorrevole.isInMovimento());
		assertTrue(sopra.eventi.contains("trascinamento 40,30"), "anche il pezzo orizzontale dopo resta al riquadro");
		assertTrue(sopra.eventi.contains("rilascio 40,30 SINISTRO"));
	}

	@Test
	void unaFinestraFuoriDallaPilaNonLaFaScorrere() {
		conLaPila();
		sotto.catturaTrascinamento = true;
		router.premuto(10, 10, Finestra.Tasto.SINISTRO);
		router.trascinato(40, 10);
		router.rilasciato(40, 10, Finestra.Tasto.SINISTRO);
		assertEquals(0, pilaScorrevole.getScostamento());
		assertTrue(sotto.eventi.contains("trascinamento 40,10"));
	}

	@Test
	void ilClickSullaFrecciaFaScorrereLaPilaEBastaSenzaPortareDavantiIlRiquadroSotto() {
		conLaPila();
		freccia = new Area(20, 20, 40, 40);
		router.clic(30, 30, Finestra.Tasto.SINISTRO, 1);
		assertTrue(pilaScorrevole.isInMovimento());
		assertNull(portataDavanti, "il riquadro sotto la freccia resta dov'è");
		assertTrue(sotto.eventi.stream().noneMatch(e -> e.startsWith("click")));
	}
}
