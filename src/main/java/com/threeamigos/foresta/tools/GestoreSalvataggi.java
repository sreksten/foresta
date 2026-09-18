package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

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
		boolean letturaRiuscita = interfacciaGestoreSalvataggi.leggi(id);
		if (letturaRiuscita) {
			ricostruisciModelloDati();
		}
		return letturaRiuscita;
	}

	public static void salva(Comando id) {
		interfacciaGestoreSalvataggi.salva(id);
	}

	/**
	 * Punto unico di ricostruzione dello stato derivato dopo che una InterfacciaGestoreSalvataggi
	 * ha popolato e installato un nuovo ModelloDati (via ModelloDati.setIstanza). Va eseguito
	 * dopo l'installazione, altrimenti le classi di dominio richiamate leggerebbero ancora
	 * la vecchia istanza tramite ModelloDati.getIstanza().
	 */
	private static void ricostruisciModelloDati() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		gruppo.setModelloDati(ModelloDati.getIstanza().getGruppoGiocatoreMD());
		gruppo.setLocazioneCorrente(Foresta.costruisciIstanza(gruppo.getCoordinate()));
		RegistroMissioni.aggiornaDopoRilettura();
	}

}
