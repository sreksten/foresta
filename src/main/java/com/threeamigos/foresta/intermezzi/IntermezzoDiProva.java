package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Arrays;
import java.util.List;

/**
 * Intermezzo di prova: scatta all'arrivo nella prima locazione della partita.
 */
public class IntermezzoDiProva implements Intermezzo {

	@Override
	public String getId() {
		return ClasseIntermezzo.INTERMEZZO_DI_PROVA.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.INIZIO_LOCAZIONE && Statistiche.getTurniGiocati() == 0;
	}

	@Override
	public List<String> getPagine() {
		String eroe = GruppoGiocatore.getIstanza().getCapo().getNomeProprio().orElseThrow(Personaggio.PERSONAGGIO_SENZA_NOME);
		return Arrays.asList(
				"La Foresta è silenziosa, e " + eroe + " si inoltra fra gli alberi.",
				"Lontano, oltre le chiome, si alza un filo di fumo nero: il Drago non dorme.");
	}
}
