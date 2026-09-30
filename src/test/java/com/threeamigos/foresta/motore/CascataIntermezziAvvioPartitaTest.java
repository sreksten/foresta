package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import com.threeamigos.foresta.eventi.interni.InternoMostraSchermataGioco;
import com.threeamigos.foresta.eventi.interni.InternoUiInattiva;
import com.threeamigos.foresta.eventi.interni.InternoUiOccupata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Riproduce il bug per cui, all'avvio di una nuova partita, l'Automa avvisava la UI di tornare
 * alla schermata di gioco ({@link InternoMostraSchermataGioco}) appena finiti gli intermezzi di
 * {@link com.threeamigos.foresta.intermezzi.MomentoIntermezzo#INIZIO_GIOCO}, anche se la cascata
 * automatica successiva (checkpoint {@link com.threeamigos.foresta.intermezzi.MomentoIntermezzo#INIZIO_LOCAZIONE})
 * si fermava subito dopo in {@link Stato#ATTESA_UI_PER_INTERMEZZO} perché la UI era ancora occupata:
 * la UI si ritrovava così a disegnare una locazione che l'Automa non aveva ancora impostato
 * (GruppoGiocatore.getLocazioneCorrente() nullo), causando un NullPointerException.
 */
class CascataIntermezziAvvioPartitaTest {

	@Test
	void nonTornaAlGiocoFincheLaLocazioneNonENonEImpostataAllAvvioPartita() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.nonSaltareIntermezzi();
			partita.eventi().ascolta(InternoMostraSchermataGioco.class);

			// La UI diventa occupata esattamente quando l'Automa sta per entrare in
			// Stato.INZIO_LOCAZIONE, cioe' subito dopo l'ultimo intermezzo di INIZIO_GIOCO:
			// e' il checkpoint che, nel bug originale, scattava avanti di uno step (verso
			// PREPARAZIONE_LOCAZIONE) e faceva scattare la notifica alla UI un passo troppo presto.
			BusEventi.iscriviti(InternoMessaggio.class, e -> {
				if (e.getMessaggioInterno().contains("Automa in stato " + Stato.INZIO_LOCAZIONE.name())) {
					partita.pubblica(new InternoUiOccupata());
				}
			});

			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
			});
			partita.assertStato(Stato.INTERMEZZO);
			assertNull(partita.gruppo().getLocazioneCorrente(),
					"precondizione: la prima locazione non e' ancora stata costruita");

			// Si fanno avanzare tutte le pagine di tutti gli intermezzi di INIZIO_GIOCO: l'ultimo
			// click fa scattare anche il checkpoint INIZIO_LOCAZIONE, che trova la UI occupata e
			// si ferma subito in ATTESA_UI_PER_INTERMEZZO
			while (partita.stato() == Stato.INTERMEZZO) {
				partita.comando(Comando.PERGAMENA);
			}
			partita.assertStato(Stato.ATTESA_UI_PER_INTERMEZZO);

			assertEquals(0, partita.eventi().tutti(InternoMostraSchermataGioco.class).size(),
					"la UI non deve tornare al gioco finché la locazione non è stata impostata");
			assertNull(partita.gruppo().getLocazioneCorrente(),
					"la locazione non deve ancora esistere mentre si aspetta la UI");

			partita.pubblica(new InternoUiInattiva());

			assertNotNull(partita.gruppo().getLocazioneCorrente(),
					"appena la UI è pronta la locazione va impostata prima di mostrare il gioco");
			assertEquals(1, partita.eventi().tutti(InternoMostraSchermataGioco.class).size(),
					"una volta impostata la locazione la UI deve tornare al gioco una volta sola");
		}
	}
}
