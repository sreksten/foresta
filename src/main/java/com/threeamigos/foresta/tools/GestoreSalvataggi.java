package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.tools.InterfacciaGestoreSalvataggi.TestataSalvataggio;

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

	public static boolean leggi(String id) {
		return interfacciaGestoreSalvataggi.leggi(id);
	}

	public static void salva(String id, String descrizione) {
		interfacciaGestoreSalvataggi.salva(id, descrizione);
	}

}
