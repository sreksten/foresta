package com.threeamigos.foresta.ui;

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

	public static void add(Comando comando) {
		comandiPossibili.add(comando);
	}
	
	public static void add(Comando ... comandi) {
		for (Comando comando : comandi) {
			add(comando);
		}
	}

	public static void set(Collection<Comando> comandi) {
		reimposta();
		comandiPossibili.addAll(comandi);
	}

	public static void set(Comando ... comandi) {
		reimposta();
		add(comandi);
	}
	
	public static void set(Comando comando) {
		reimposta();
		add(comando);
	}

	public static List<Comando> getComandi() {
		return comandiPossibili;
	}
}
