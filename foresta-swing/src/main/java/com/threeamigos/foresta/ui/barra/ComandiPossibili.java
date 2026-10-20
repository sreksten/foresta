package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.tipi.Comando;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * I comandi che il giocatore può dare in questo momento, come li ha mandati il motore con l'ultima richiesta: la barra
 * delle icone li mostra (vedi DisplayableCanvasBarraIcone.impostaAzioni).
 *
 * @author Stefano Reksten
 */
public class ComandiPossibili {

	private ComandiPossibili() {
	}

	private static final List<Comando> comandiPossibili = new ArrayList<>();

	/**
	 * Ripulisce l'elenco dei possibili comandi disponibili
	 */
	public static void reimposta() {
		comandiPossibili.clear();
	}

	/**
	 * Sostituisce i comandi possibili con questi.
	 */
	public static void set(Collection<Comando> comandi) {
		reimposta();
		comandiPossibili.addAll(comandi);
	}

	/**
	 * I comandi possibili in questo momento, nell'ordine in cui il motore li ha mandati.
	 */
	public static List<Comando> getComandi() {
		return comandiPossibili;
	}
}
