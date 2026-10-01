package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaIncantatore;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioFornitore;
import com.threeamigos.foresta.eventi.interni.InternoMostraSchermataGioco;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ClasseIntermezzo;
import com.threeamigos.foresta.intermezzi.IntermezzoLocandaPrimaVisita;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica gli intermezzi di ingresso nei negozi di città ({@link MomentoIntermezzo#INGRESSO_ARMAIOLO},
 * {@link MomentoIntermezzo#INGRESSO_ALCHIMISTA}, {@link MomentoIntermezzo#INGRESSO_VENDITORE_DI_PERGAMENE},
 * {@link MomentoIntermezzo#INGRESSO_INCANTATORE}) e il riuso, per la locanda in città, degli intermezzi già
 * esistenti per la locanda nel bosco ({@link MomentoIntermezzo#INGRESSO_LOCANDA_IN_CITTA}).
 */
class IntermezzoIngressoNegozioTest {

	@Test
	void entrareInCittaNonFaScattarePrematuramenteLintermezzoDellaLocanda() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);

			assertEquals(0, contaBattuta(partita, "C'era una volta..."),
					"il solo ingresso in città non deve far scattare l'intermezzo della locanda, prima ancora di aver scelto \"Locanda\"");
		}
	}

	@Test
	void armaioloMostraIlSuoIntermezzoUnaSolaVolta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(InternoMostraSchermataGioco.class);
			partita.nonSaltareIntermezzi();

			partita.comando(Comando.ARMAIOLO);
			partita.assertStato(Stato.INTERMEZZO);
			assertEquals(1, contaBattuta(partita, "armaiolo", "Ciao."),
					"la prima volta dall'armaiolo deve comparire il suo intermezzo");
			assertEquals(0, partita.eventi().tutti(ComandoAperturaInventarioCommerciante.class).size(),
					"il negozio non deve apparire ancora, finché l'intermezzo non è terminato");

			partita.saltaIntermezzi();
			partita.assertStato(Stato.IN_LOCAZIONE);
			assertTrue(ModelloDati.getIstanza().getIntermezziMD().isScattato(ClasseIntermezzo.INTERMEZZO_ARMAIOLO.name()),
					"il registro deve ricordare che l'intermezzo e' scattato");
			assertEquals(1, partita.eventi().tutti(ComandoAperturaInventarioCommerciante.class).size(),
					"terminato l'intermezzo il negozio dell'armaiolo deve apparire comunque");
			assertApreNegozioSenzaTornareAllaSchermataDiGioco(partita, ComandoAperturaInventarioCommerciante.class);

			// Si torna in piazza e si rientra dall'armaiolo una seconda volta
			partita.comando(Comando.ANNULLA);
			partita.comando(Comando.ARMAIOLO);
			partita.assertStato(Stato.IN_LOCAZIONE);
			assertEquals(1, contaBattuta(partita, "armaiolo", "Ciao."),
					"alla seconda visita l'intermezzo non deve ripetersi");
			assertEquals(2, partita.eventi().tutti(ComandoAperturaInventarioCommerciante.class).size(),
					"il negozio deve comunque apparire anche senza intermezzo");
		}
	}

	@Test
	void alchimistaEVenditoreDiPergameneMostranoIlLoroIntermezzo() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(InternoMostraSchermataGioco.class);
			partita.nonSaltareIntermezzi();

			partita.comando(Comando.ALCHIMISTA);
			partita.assertStato(Stato.INTERMEZZO);
			assertEquals(1, contaBattuta(partita, "alchimista", "Ciao."));
			partita.saltaIntermezzi();
			partita.assertStato(Stato.IN_LOCAZIONE);
			assertEquals(1, partita.eventi().tutti(ComandoAperturaInventarioFornitore.class).size());
			assertApreNegozioSenzaTornareAllaSchermataDiGioco(partita, ComandoAperturaInventarioFornitore.class);

			partita.comando(Comando.ANNULLA);
			partita.comando(Comando.VENDITORE_DI_PERGAMENE);
			partita.assertStato(Stato.INTERMEZZO);
			assertEquals(1, contaBattuta(partita, "venditore", "Ciao."));
			partita.saltaIntermezzi();
			partita.assertStato(Stato.IN_LOCAZIONE);
			assertEquals(1, partita.eventi().tutti(ComandoAperturaInventarioCommerciante.class).size());
			assertApreNegozioSenzaTornareAllaSchermataDiGioco(partita, ComandoAperturaInventarioCommerciante.class);
		}
	}

	@Test
	void incantatoreMostraIlSuoIntermezzoUnaSolaVolta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(ComandoAperturaIncantatore.class, InternoMostraSchermataGioco.class);
			partita.nonSaltareIntermezzi();

			partita.comando(Comando.INCANTATORE);
			partita.assertStato(Stato.INTERMEZZO);
			assertEquals(1, contaBattuta(partita, "incantatore", "Ciao."));
			partita.saltaIntermezzi();
			partita.assertStato(Stato.IN_LOCAZIONE);
			assertEquals(1, partita.eventi().tutti(ComandoAperturaIncantatore.class).size());
			assertApreNegozioSenzaTornareAllaSchermataDiGioco(partita, ComandoAperturaIncantatore.class);

			partita.comando(Comando.ANNULLA);
			partita.comando(Comando.INCANTATORE);
			partita.assertStato(Stato.IN_LOCAZIONE);
			assertEquals(1, contaBattuta(partita, "incantatore", "Ciao."), "non deve ripetersi alla seconda visita");
			assertEquals(2, partita.eventi().tutti(ComandoAperturaIncantatore.class).size());
		}
	}

	@Test
	void laLocandaInCittaRiusaLintermezzoDellaLocandaNelBosco() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.nonSaltareIntermezzi();

			partita.comando(Comando.LOCANDA);
			partita.assertStato(Stato.INTERMEZZO);
			assertEquals(1, contaBattuta(partita, "bardo", "C'era una volta..."),
					"scegliendo \"Locanda\" la prima volta deve scattare l'intermezzo della prima visita");

			partita.saltaIntermezzi();
			partita.assertStato(Stato.IN_LOCAZIONE);

			// Stessa identica chiamata che farebbe RegistroIntermezzi per questa locanda: il
			// registro deve già ricordarla come visitata, così una seconda visita non ripete il bardo.
			String id = new IntermezzoLocandaPrimaVisita().getId();
			assertTrue(ModelloDati.getIstanza().getIntermezziMD().isScattato(id),
					"il registro deve ricordare la visita a questa locanda, per non ripetere l'intermezzo");
		}
	}

	/**
	 * Verifica che, dopo l'apertura del negozio ({@code tipoEventoAperturaNegozio}), non sia stato
	 * pubblicato un {@link InternoMostraSchermataGioco}: la UI reagisce a quest'ultimo evento tornando
	 * alla schermata di gioco normale ({@code ForestaUI.gestisciEventoMostraSchermataGioco}), il che
	 * sovrascriverebbe la finestra del negozio appena richiesta.
	 */
	private static void assertApreNegozioSenzaTornareAllaSchermataDiGioco(PartitaDiTest partita,
			Class<?> tipoEventoAperturaNegozio) {
		java.util.List<Object> eventi = partita.eventi().inOrdine(tipoEventoAperturaNegozio, InternoMostraSchermataGioco.class);
		assertFalse(eventi.isEmpty(), "il negozio doveva essere stato aperto");
		assertTrue(tipoEventoAperturaNegozio.isInstance(eventi.get(eventi.size() - 1)),
				"dopo l'apertura del negozio non deve arrivare InternoMostraSchermataGioco, altrimenti la UI tornerebbe alla schermata di gioco normale invece di mostrare l'inventario");
	}

	private static long contaBattuta(PartitaDiTest partita, String testoContenuto) {
		return partita.eventi().tutti(NotificaPaginaIntermezzo.class).stream()
				.flatMap(n -> n.getPagina().getBattuteProgrammate().stream())
				.map(b -> b.getBattuta().getTesto())
				.filter(testo -> testo != null && testo.contains(testoContenuto))
				.count();
	}

	private static long contaBattuta(PartitaDiTest partita, String idElemento, String testoContenuto) {
		return partita.eventi().tutti(NotificaPaginaIntermezzo.class).stream()
				.flatMap(n -> n.getPagina().getBattuteProgrammate().stream())
				.filter(b -> idElemento.equals(b.getBattuta().getIdElemento()))
				.map(b -> b.getBattuta().getTesto())
				.filter(testo -> testo != null && testo.contains(testoContenuto))
				.count();
	}
}
