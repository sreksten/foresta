package com.threeamigos.foresta.motore;

import java.util.ArrayList;
import java.util.List;

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
