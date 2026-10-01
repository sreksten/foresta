package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoMostraSchermataGioco;
import com.threeamigos.foresta.eventi.interni.InternoUiInattiva;
import com.threeamigos.foresta.eventi.interni.InternoUiOccupata;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ClasseIntermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica il checkpoint {@link MomentoIntermezzo#LOCAZIONE_COMPLETATA} (gestione_missioni.md, punto 1):
 * l'intermezzo di prova scatta alla prima fine locazione della partita e non si ripete più.
 */
class IntermezzoLocazioneCompletataTest {

	@Test
	void scattaUnaVoltaSolaAllaFineDellaPrimaLocazione() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);

			assertEquals(0, contaBattutaEremita(partita),
					"prima della fine locazione l'intermezzo non deve ancora essere comparso");

			partita.comando(Comando.ESCI_DA_CITTA);
			partita.assertStato(Stato.SCELTA_DIREZIONE);

			assertEquals(1, contaBattutaEremita(partita),
					"alla prima fine locazione l'intermezzo deve comparire una volta sola");
			assertTrue(ModelloDati.getIstanza().getIntermezziMD()
							.isScattato(ClasseIntermezzo.INTERMEZZO_FINE_PRIMA_LOCAZIONE.name()),
					"il registro deve ricordare che l'intermezzo e' scattato");

			// Da questo momento in poi il checkpoint non troverebbe piu' nulla da mostrare: non si ripete
			assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA),
					"a una successiva fine locazione l'intermezzo non deve scattare di nuovo");
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
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);

			partita.pubblica(new InternoUiOccupata());

			partita.comando(Comando.ESCI_DA_CITTA);

			assertEquals(0, contaBattutaEremita(partita),
					"l'intermezzo non deve comparire finché la UI è occupata");
			partita.assertStato(Stato.ATTESA_UI_PER_INTERMEZZO);

			partita.pubblica(new InternoUiInattiva());

			assertEquals(1, contaBattutaEremita(partita),
					"appena la UI segnala di essere inattiva l'intermezzo in attesa deve comparire");
		}
	}

	/**
	 * Riproduce il bug per cui, terminando l'ultima pagina di un intermezzo mentre la UI è ancora
	 * occupata, l'Automa avvisava subito la UI di tornare alla schermata di gioco
	 * ({@link InternoMostraSchermataGioco}) pur restando in {@link Stato#ATTESA_UI_PER_INTERMEZZO}:
	 * la UI si ritrovava così a disegnare la locazione prima che l'Automa l'avesse impostata.
	 */
	@Test
	void nonTornaAlGiocoFincheLaUiENonEInattivaDopoLultimaPaginaDiUnIntermezzo() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(InternoMostraSchermataGioco.class);
			partita.nonSaltareIntermezzi();

			partita.comando(Comando.ESCI_DA_CITTA);
			partita.assertStato(Stato.INTERMEZZO);

			partita.pubblica(new InternoUiOccupata());
			partita.comando(Comando.PERGAMENA);

			partita.assertStato(Stato.ATTESA_UI_PER_INTERMEZZO);
			assertEquals(0, partita.eventi().tutti(InternoMostraSchermataGioco.class).size(),
					"la UI non deve tornare al gioco finché è ancora occupata");

			partita.pubblica(new InternoUiInattiva());

			partita.assertStato(Stato.SCELTA_DIREZIONE);
			assertEquals(1, partita.eventi().tutti(InternoMostraSchermataGioco.class).size(),
					"appena la UI è inattiva deve tornare al gioco una volta sola");
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
