package com.threeamigos.foresta.motore;

import java.util.ArrayList;
import java.util.List;

public class Azioni {
	
	private Azioni() {
	}

	private static List<Comando> possibiliAzioni = new ArrayList<>();
	
	public static void clear() {
		possibiliAzioni.clear();
	}

	public static void add(Comando comando) {
		possibiliAzioni.add(comando);
	}
	
	public static void add(Comando ... comandi) {
		for (Comando comando : comandi) {
			add(comando);
		}
	}
	
	public static void set(Comando ... comandi) {
		clear();
		add(comandi);
	}
	
	public static void set(Comando comando) {
		clear();
		add(comando);
	}

	public static List<Comando> getComandi() {
		return possibiliAzioni;
	}
}
