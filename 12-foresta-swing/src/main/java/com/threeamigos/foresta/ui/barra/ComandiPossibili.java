package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.eventi.richieste.RicaricaDelComando;
import com.threeamigos.foresta.tipi.Comando;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * I comandi che il giocatore può dare in questo momento, come li ha mandati il motore con l'ultima richiesta: la barra
 * delle icone li mostra (vedi DisplayableCanvasBarraIcone.impostaAzioni). Con loro, quelli che non può ancora dare perché
 * in ricarica: la barra li mostra spenti, con i turni che mancano.
 *
 * @author Stefano Reksten
 */
public class ComandiPossibili {

	private ComandiPossibili() {
	}

	private static final List<Comando> comandiPossibili = new ArrayList<>();
	private static final List<Comando> comandiVisibili = new ArrayList<>();
	private static final Map<Comando, RicaricaDelComando> inRicarica = new LinkedHashMap<>();

	/**
	 * Ripulisce l'elenco dei possibili comandi disponibili
	 */
	public static void reimposta() {
		comandiPossibili.clear();
		comandiVisibili.clear();
		inRicarica.clear();
	}

	/**
	 * Sostituisce i comandi possibili con questi.
	 */
	public static void set(Collection<Comando> comandi) {
		reimposta();
		comandiPossibili.addAll(comandi);
		comandiVisibili.addAll(comandi);
	}

	/**
	 * Sostituisce i comandi possibili con questi, e aggiunge da mostrare spenti quelli in ricarica.
	 *
	 * @param comandi i comandi che si possono dare adesso
	 * @param visibili tutti i comandi da mostrare, nell'ordine: i possibili e quelli in ricarica
	 * @param ricariche i comandi fra i visibili che sono in ricarica
	 */
	public static void set(Collection<Comando> comandi, List<Comando> visibili, Map<Comando, RicaricaDelComando> ricariche) {
		reimposta();
		comandiPossibili.addAll(comandi);
		comandiVisibili.addAll(visibili);
		inRicarica.putAll(ricariche);
	}

	/**
	 * I comandi possibili in questo momento, nell'ordine in cui il motore li ha mandati.
	 */
	public static List<Comando> getComandi() {
		return comandiPossibili;
	}

	/**
	 * I comandi da mostrare, nell'ordine: i possibili e quelli in ricarica, che si vedono ma non si possono dare.
	 */
	public static List<Comando> getComandiVisibili() {
		return comandiVisibili;
	}

	/**
	 * La ricarica del comando, se è in ricarica (e quindi non si può dare).
	 */
	public static RicaricaDelComando getRicarica(Comando comando) {
		return inRicarica.get(comando);
	}

	public static Map<Comando, RicaricaDelComando> getInRicarica() {
		return Collections.unmodifiableMap(inRicarica);
	}
}
