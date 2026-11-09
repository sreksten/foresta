package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoMostraSchermataGioco;
import com.threeamigos.foresta.eventi.interni.InternoUiInattiva;
import com.threeamigos.foresta.eventi.interni.InternoUiOccupata;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.FabbricaIntermezzi;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.CostantiMotore;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'intermezzo dell'eremita (INTERMEZZO_FINE_PRIMA_LOCAZIONE): scatta all'inizio della seconda locazione della partita
 * ({@link MomentoIntermezzo#INIZIO_LOCAZIONE}) e non si ripete più; con lui si provano anche l'attesa della UI e il ritorno al gioco.
 */
class IntermezzoLocazioneCompletataTest {

	@Test
	void scattaUnaVoltaSolaAllaFineDellaPrimaLocazione() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);

			assertEquals(0, contaBattutaEremita(partita),
					"prima della fine locazione l'intermezzo non deve ancora essere comparso");

			partita.comando(Comando.ESCI_DA_CITTA);
			partita.assertStato(Stato.SCELTA_DIREZIONE);
			assertEquals(0, contaBattutaEremita(partita),
					"a fine della prima locazione l'eremita non c'è ancora: c'è il tutorial");

			AutomaCasiRimastiTest.camminaUnPasso(partita);
			assertEquals(1, contaBattutaEremita(partita),
					"all'inizio della seconda locazione l'intermezzo deve comparire una volta sola");
			assertTrue(ModelloDati.getIstanza().getIntermezziMD()
							.isScattato(TipoIntermezzo.INTERMEZZO_FINE_PRIMA_LOCAZIONE.name()),
					"il registro deve ricordare che l'intermezzo e' scattato");

			// Da questo momento in poi il checkpoint non troverebbe piu' nulla da mostrare: non si ripete
			assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE),
					"all'inizio di una locazione successiva l'intermezzo non deve scattare di nuovo");
		}
	}

	/**
	 * L'eremita regala al gruppo incantesimi e pozioni (si comincia senza), e lo fa una volta sola. La modalità di prova dei test ha già
	 * le sue scorte: si guarda quanto aumentano.
	 */
	@Test
	void lEremitaRegalaIncantesimiEPozioniUnaVoltaSola() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
			int aria = gruppo.getIncantesimi(TipoIncantesimo.ARIA);
			int acqua = gruppo.getIncantesimi(TipoIncantesimo.ACQUA);
			int terra = gruppo.getIncantesimi(TipoIncantesimo.TERRA);
			int pozioni = gruppo.getPozioniSalute();

			partita.comando(Comando.ESCI_DA_CITTA);
			AutomaCasiRimastiTest.camminaUnPasso(partita);

			int perElemento = CostantiMotore.DOTAZIONE_EREMITA_INCANTESIMI_PER_ELEMENTO;
			assertEquals(aria + perElemento, gruppo.getIncantesimi(TipoIncantesimo.ARIA));
			assertEquals(acqua + perElemento, gruppo.getIncantesimi(TipoIncantesimo.ACQUA));
			assertEquals(terra + perElemento, gruppo.getIncantesimi(TipoIncantesimo.TERRA));
			assertEquals(pozioni + CostantiMotore.DOTAZIONE_EREMITA_POZIONI_SALUTE, gruppo.getPozioniSalute());

			// L'intermezzo non si ripete, e nemmeno la dotazione
			assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE));
			assertEquals(pozioni + CostantiMotore.DOTAZIONE_EREMITA_POZIONI_SALUTE, gruppo.getPozioniSalute());
		}
	}

	/**
	 * Verifica che l'Automa aspetti {@link InternoUiInattiva} prima di mostrare un intermezzo pronto
	 * a scattare, se la UI ha segnalato di essere ancora occupata (uno sprite, un annuncio globale)
	 * con {@link InternoUiOccupata}.
	 */
	@Test
	void aspettaCheLaUiSiaInattivaPrimaDiMostrareLintermezzo() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);

			partita.comando(Comando.ESCI_DA_CITTA);
			partita.pubblica(new InternoUiOccupata());

			AutomaCasiRimastiTest.camminaUnPasso(partita);

			assertEquals(0, contaBattutaEremita(partita),
					"l'intermezzo non deve comparire finché la UI è occupata");
			partita.assertStato(Stato.ATTESA_UI_PER_INTERMEZZO);
			assertTrue(partita.comandiDisponibili().isEmpty(),
					"in attesa della UI la barra delle icone deve essere vuota");

			partita.pubblica(new InternoUiInattiva());

			assertEquals(1, contaBattutaEremita(partita),
					"appena la UI segnala di essere inattiva l'intermezzo in attesa deve comparire");
		}
	}

	/**
	 * Terminando l'ultima pagina di un intermezzo mentre la UI è ancora occupata, se non ci sono
	 * altri intermezzi da mostrare non si aspetta la UI: si torna subito al gioco
	 * ({@link InternoMostraSchermataGioco}, una volta sola) e alla scelta della direzione.
	 */
	@Test
	void dopoLultimaPaginaDiUnIntermezzoNonAspettaLaUiSeNonCeNeSonoAltri() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(InternoMostraSchermataGioco.class);
			partita.nonSaltareIntermezzi();

			partita.comando(Comando.ESCI_DA_CITTA);
			AutomaCasiRimastiTest.camminaUnPasso(partita);
			partita.assertStato(Stato.INTERMEZZO);

			partita.pubblica(new InternoUiOccupata());
			while (partita.stato() == Stato.INTERMEZZO) {
				partita.comando(Comando.PERGAMENA);
			}

			assertNotEquals(Stato.ATTESA_UI_PER_INTERMEZZO, partita.stato(), "finito l'intermezzo non si aspetta la UI");
			assertEquals(1, partita.eventi().tutti(InternoMostraSchermataGioco.class).size(),
					"la UI deve tornare al gioco una volta sola");
		}
	}

	/**
	 * A fine locazione, se non c'è nessun intermezzo da mostrare, non si aspetta che la UI finisca
	 * le sue animazioni: si passa subito alla scelta della direzione.
	 */
	@Test
	void senzaIntermezziNonAspettaLaUiAFineLocazione() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			RegistroIntermezzi.segnaScattato(FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_FINE_PRIMA_LOCAZIONE));
			assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA),
					"precondizione: nessun intermezzo deve scattare a fine locazione");

			partita.pubblica(new InternoUiOccupata());
			partita.comando(Comando.ESCI_DA_CITTA);

			partita.assertStato(Stato.SCELTA_DIREZIONE);
		}
	}

	/**
	 * Quante volte la battuta dell'eremita di {@code IntermezzoFineLocazioneDiProva} e' comparsa finora.
	 */
	private static long contaBattutaEremita(PartitaDiTest partita) {
		return partita.eventi().tutti(NotificaPaginaIntermezzo.class).stream()
				.flatMap(n -> n.getPagina().getBattuteProgrammate().stream())
				.map(b -> b.getBattuta().getTesto())
				.filter(testo -> testo.contains("Buona fortuna nella tua missione!"))
				.count();
	}
}
