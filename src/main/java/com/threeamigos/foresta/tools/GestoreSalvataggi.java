package com.threeamigos.foresta.tools;

import java.util.List;

import com.threeamigos.foresta.tools.InterfacciaGestoreSalvataggi.InterfacciaTestataSalvataggio;

public class GestoreSalvataggi {

	private GestoreSalvataggi() {
	}

	private static InterfacciaGestoreSalvataggi interfacciaGestoreSalvataggi = null;

	public static final void impostaGestoreSalvataggi(InterfacciaGestoreSalvataggi gestoreSalvataggi) {
		interfacciaGestoreSalvataggi = gestoreSalvataggi;
	}

	public static final List<InterfacciaTestataSalvataggio> getSalvataggiDisponibili() {
		return interfacciaGestoreSalvataggi.getSalvataggiDisponibili();
	}

	public static final boolean leggi(String id) {
		return interfacciaGestoreSalvataggi.leggi(id);
	}

	public static final void salva(String id, String descrizione) {
		interfacciaGestoreSalvataggi.salva(id, descrizione);
	}

}
