package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Arrays;
import java.util.List;

/**
 * Intermezzo di prova: scatta all'arrivo nella prima locazione della partita. Le prime
 * due pagine mostrano il protagonista sotto il testo; la terza prova sfondo, un elemento
 * animato sullo sfondo che si gira quando torna indietro, un'animazione fornita da una
 * classe della UI (il fuoco) e un dialogo a fumetti.
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
	public List<PaginaIntermezzo> getPagine() {
		Personaggio capo = GruppoGiocatore.getIstanza().getCapo();
		String eroe = capo.getNomeProprio().orElseThrow(Personaggio.PERSONAGGIO_SENZA_NOME);
		// Il protagonista al centro, a due terzi dell'altezza, sotto il testo. L'alfabeto
		// grande ha solo lettere, cifre e ' , . ? : niente accenti (si scrive e') né due punti.
		ClassePersonaggio classeEroe = capo.getClasse();
		return Arrays.asList(
				new PaginaIntermezzo("La Foresta e' silenziosa, e " + eroe + " si inoltra fra gli alberi.")
						.conElemento(ElementoIntermezzo.personaggio("eroe", classeEroe, 0.5, 2.0 / 3)),
				new PaginaIntermezzo("Lontano, oltre le chiome, si alza un filo di fumo nero. Il Drago non dorme.")
						.conElemento(ElementoIntermezzo.personaggio("eroe", classeEroe, 0.5, 2.0 / 3)),
				new PaginaIntermezzo()
						.conSfondo(ImmagineIntermezzo.locazione(ClassiLocazione.RADURA))
						// Il drago attraversa il cielo, piccolo e semitrasparente, avanti e indietro,
						// guardando sempre dove va (l'immagine originale guarda a sinistra)
						.conElemento(ElementoIntermezzo.personaggio("drago", ClassePersonaggio.DRAGO, 1.1, 0.25)
								.conScala(0.4).conOpacita(0.6)
								.poi(Tappa.inSecondi(8).verso(-0.1, 0.15))
								.ripeti(Ripetizione.AVANTI_E_INDIETRO)
								.orientaNelVersoDelMoto(Verso.SINISTRA))
						.conElemento(ElementoIntermezzo.di("fuoco", ImmagineIntermezzo.animazione(Animazione.FUOCO_DA_CAMPO), 0.5, 0.76))
						.conElemento(ElementoIntermezzo.personaggio("eroe", classeEroe, 0.3, 0.7))
						// L'eremita entra da destra e si ferma davanti all'eroe
						.conElemento(ElementoIntermezzo.personaggio("eremita", ClassePersonaggio.EREMITA, 1.1, 0.7)
								.specchiato()
								.poi(Tappa.inSecondi(2).verso(0.7, 0.7)))
						.conBattuta(BattutaIntermezzo.di("eremita", "Chi va là?").daSecondo(2))
						.conBattuta(BattutaIntermezzo.di("eroe", "Mi chiamo " + eroe + ", e cerco il Drago."))
						.conBattuta(BattutaIntermezzo.di("eremita", "Allora guarda in alto, e prega di non trovarlo.")));
	}
}
