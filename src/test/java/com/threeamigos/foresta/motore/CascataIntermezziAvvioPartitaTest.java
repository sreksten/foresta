package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import com.threeamigos.foresta.eventi.interni.InternoMostraSchermataGioco;
import com.threeamigos.foresta.eventi.interni.InternoUiOccupata;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Riproduce il bug per cui, all'avvio di una nuova partita, l'Automa avvisava la UI di tornare
 * alla schermata di gioco ({@link InternoMostraSchermataGioco}) appena finiti gli intermezzi di
 * {@link com.threeamigos.foresta.intermezzi.MomentoIntermezzo#INIZIO_GIOCO}, prima che la cascata
 * automatica successiva avesse costruito la prima locazione: la UI si ritrovava così a disegnare
 * una locazione che l'Automa non aveva ancora impostato (GruppoGiocatore.getLocazioneCorrente()
 * nullo), causando un NullPointerException.
 */
class CascataIntermezziAvvioPartitaTest {

	@Test
	void nonTornaAlGiocoFincheLaLocazioneNonEImpostataAllAvvioPartita() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.nonSaltareIntermezzi();
			partita.eventi().ascolta(InternoMostraSchermataGioco.class);

			// Per ogni ritorno al gioco si annota se la locazione esisteva già in quel momento
			List<Boolean> locazioneImpostataAlRitorno = new ArrayList<>();
			BusEventi.iscriviti(InternoMostraSchermataGioco.class,
					e -> locazioneImpostataAlRitorno.add(partita.gruppo().getLocazioneCorrente() != null));

			// La UI diventa occupata esattamente quando l'Automa sta per entrare in
			// Stato.INZIO_LOCAZIONE, cioe' subito dopo l'ultimo intermezzo di INIZIO_GIOCO
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
			// click fa scattare anche il checkpoint INIZIO_LOCAZIONE che, non avendo intermezzi
			// da mostrare, non aspetta la UI occupata e costruisce subito la locazione
			while (partita.stato() == Stato.INTERMEZZO) {
				partita.comando(Comando.PERGAMENA);
			}
			assertNotEquals(Stato.ATTESA_UI_PER_INTERMEZZO, partita.stato(),
					"senza intermezzi da mostrare non si deve aspettare la UI");

			assertEquals(1, partita.eventi().tutti(InternoMostraSchermataGioco.class).size(),
					"la UI deve tornare al gioco una volta sola");
			assertTrue(locazioneImpostataAlRitorno.get(0),
					"la locazione va impostata prima di mostrare il gioco");
		}
	}
}
