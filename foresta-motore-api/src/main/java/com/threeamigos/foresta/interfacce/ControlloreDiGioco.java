package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.tipi.Comando;

/**
 * L'automa a stati finiti che tiene traccia dello svolgimento del gioco.
 * L'unica implementazione è Automa, e non dovrebbero servirne altre.
 */
public interface ControlloreDiGioco {

	void inizia();

	void processaComando(Comando azione);

	/**
	 * L'app passa in background (Android {@code onPause}/{@code onStop}, iOS {@code sceneDidEnterBackground}): il
	 * temporizzatore si ferma e, se la partita è in un punto sicuro (fra una locazione e l'altra, dove si può
	 * salvare anche a mano), si scrive il salvataggio automatico, perché il sistema può chiudere l'app senza
	 * preavviso. Altrove non si scrive niente, e resta il salvataggio automatico di un background precedente, se c'è (è lo stato
	 * dell'ultima volta che l'app è uscita dal primo piano in un punto sicuro, non dell'ultimo punto sicuro attraversato). Il salvataggio automatico si cancella a fine partita, a una partita nuova e quando si carica un altro salvataggio.
	 */
	void inBackground();

	/**
	 * L'app torna in primo piano: il temporizzatore riparte.
	 */
	void inPrimoPiano();

	/**
	 * Dalla schermata iniziale, riprende la partita interrotta dal salvataggio automatico, saltando la scelta dello
	 * slot. Chi avvia l'app, se vuole offrire la ripresa, la chiama prima che il giocatore scelga.
	 *
	 * @return true se la partita è stata ripresa; false se non c'è un salvataggio automatico valido o il gioco non
	 * è alla schermata iniziale (e allora non cambia niente)
	 */
	boolean riprendiPartitaInterrotta();

}
