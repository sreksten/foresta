package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.modellodati.LettoreCampi;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Il formato di salvataggio delle tattiche (tattiche_di_combattimento_automatiche.md §11.12): una riga per elemento, campi separati da
 * {@link com.threeamigos.foresta.modellodati.Serializzabile#PIPE}, la prima parola dice di che elemento si tratta. I campi che possono mancare
 * restano vuoti; gli elementi che ne contengono altri dicono quanti sono, e questi seguono nelle righe successive.
 * <pre>
 * TATTICA|numero di voci|disabilitata (0 o 1)
 * VOCE|attiva|ha un selettore|numero di condizioni|numero di azioni|nome dato dal giocatore
 * SELETTORE|rosa|ordinamento|tipo di danno dell'ordinamento|numero di filtri       (seguono i filtri)
 * FILTRO|tipo|negato|confronto|numero|stato|classe|categoria|tipo di danno
 * PREDICATO|tipo|negato|confronto|numero|rosa|numero di filtri                     (seguono i filtri)
 * AZIONE|tipo|criterio|solo se più forte dell'attacco|tipo di danno dell'arma
 * </pre>
 * Le condizioni e le azioni di una voce seguono il suo selettore, nell'ordine. Gli elementi sono immutabili: la rilettura è un metodo statico che li costruisce
 * (con le stesse fabbriche che li controllano, quindi un salvataggio incoerente è rifiutato).
 */
final class FormatoTattiche {

	private FormatoTattiche() {
	}

	/**
	 * Legge la riga successiva e controlla che sia del tipo atteso; restituisce i campi che seguono l'etichetta
	 */
	static LettoreCampi riga(BufferedReader stream, String etichetta) throws IOException {
		return controlla(new LettoreCampi(stream.readLine()), etichetta);
	}

	/**
	 * Controlla l'etichetta di una riga già letta
	 */
	static LettoreCampi controlla(LettoreCampi campi, String etichetta) throws IOException {
		String trovata = campi.testo();
		if (!etichetta.equals(trovata)) {
			throw new IOException("Salvataggio delle tattiche incoerente: atteso " + etichetta + ", trovato " + trovata);
		}
		return campi;
	}
}
