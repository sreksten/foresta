package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Comando;

import java.util.List;

public class GestoreSalvataggi {

	private GestoreSalvataggi() {
	}

	private static InterfacciaGestoreSalvataggi interfacciaGestoreSalvataggi;

	public static void impostaGestoreSalvataggi(InterfacciaGestoreSalvataggi gestoreSalvataggi) {
		interfacciaGestoreSalvataggi = gestoreSalvataggi;
	}

	public static List<TestataSalvataggio> getSalvataggiDisponibili() {
		return interfacciaGestoreSalvataggi.getSalvataggiDisponibili();
	}

	public static boolean leggi(Comando id) {
		return interfacciaGestoreSalvataggi.leggi(id);
	}

	public static void salva(Comando id) {
		interfacciaGestoreSalvataggi.salva(id);
	}

}
