package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Prepara una partita minima fuori dal gioco, per gli strumenti di sviluppo come
 * l'anteprima degli intermezzi (ui.AnteprimaIntermezzo): una Foresta nuova, con le
 * missioni e il tempo al primo giorno, e un gruppo formato dal solo protagonista.
 * Non va usata durante una partita vera, perché ne sostituisce lo stato.
 */
public final class PartitaDiAnteprima {

	private PartitaDiAnteprima() {
	}

	/**
	 * @return il protagonista, già capo del gruppo
	 */
	public static Personaggio prepara(ClassePersonaggio classeProtagonista, String nomeProtagonista) {
		Foresta.reimposta();
		Personaggio protagonista = classeProtagonista.getIstanza(1);
		protagonista.getModelloDati().setNome(nomeProtagonista);
		GruppoGiocatore.getIstanza().aggiungiPersonaggioSenzaNotificare(protagonista);
		return protagonista;
	}
}
