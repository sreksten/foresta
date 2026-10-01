package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Prepara una partita minima fuori dal gioco, per gli strumenti di sviluppo come
 * l'anteprima degli intermezzi (ui.AnteprimaIntermezzo): una Foresta nuova, con le
 * missioni e il tempo al primo giorno, e un gruppo formato dal protagonista ed
 * eventuali compagni (fino al massimo consentito dal gruppo del giocatore).
 * Non va usata durante una partita vera, perché ne sostituisce lo stato.
 */
public final class PartitaDiAnteprima {

	private PartitaDiAnteprima() {
	}

	/**
	 * @return il protagonista, già capo del gruppo
	 */
	public static Personaggio prepara(ClassePersonaggio classeProtagonista, String nomeProtagonista,
			ClassePersonaggio... classiCompagni) {
		Foresta.reimposta();
		Personaggio protagonista = classeProtagonista.getIstanza(1);
		protagonista.getModelloDati().setNome(nomeProtagonista);
		GruppoGiocatore.getIstanza().aggiungiPersonaggioSenzaNotificare(protagonista);
		for (int i = 0; i < classiCompagni.length; i++) {
			Personaggio compagno = classiCompagni[i].getIstanza(1);
			compagno.getModelloDati().setNome("Compagno" + (i + 1));
			GruppoGiocatore.getIstanza().aggiungiPersonaggioSenzaNotificare(compagno);
		}
		return protagonista;
	}
}
