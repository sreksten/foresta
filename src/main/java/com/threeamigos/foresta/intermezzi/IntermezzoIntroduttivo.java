package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermezzo di prova: scatta all'arrivo nella prima locazione della partita. Le prime
 * due pagine mostrano il protagonista sotto il testo; la terza prova sfondo, un elemento
 * animato sullo sfondo che si gira quando torna indietro, un'animazione fornita da una
 * classe della UI (il fuoco) e un dialogo a fumetti.
 */
public class IntermezzoIntroduttivo implements Intermezzo {

	@Override
	public String getId() {
		return ClasseIntermezzo.INTERMEZZO_INTRODUTTIVO.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.INIZIO_GIOCO;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		Personaggio capo = GruppoGiocatore.getIstanza().getCapo();
		// Senza nome proprio l'eroe si chiama con la sua classe ("il guerriero")
		String eroe = capo.getNomeProprio()
				.orElseGet(() -> capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
		// Il protagonista al centro, a due terzi dell'altezza, sotto il testo. L'alfabeto
		// grande ha solo lettere, cifre e ' , . ? : niente accenti (si scrive e') né due punti.
		TipoPersonaggio classeEroe = capo.getClasse();
		List<PaginaIntermezzo> pagineIntermezzo = new ArrayList<>();
		pagineIntermezzo.add(new PaginaIntermezzo("La Foresta e' silenziosa, e " + eroe + " si inoltra fra gli alberi.")
				.perSecondi(3)
				.conElemento(personaggioVersoSinistra(classeEroe, 0.5, 2.0 / 3)));
		pagineIntermezzo.add(new PaginaIntermezzo("Lontano, oltre le chiome, si alza un filo di fumo nero. Il Drago non dorme.")
				.perSecondi(3)
				.conElemento(personaggioVersoSinistra(classeEroe, 0.5, 2.0 / 3)));
		PaginaIntermezzo finale = new PaginaIntermezzo()
						.conSfondo(ImmagineIntermezzo.locazione(TipoLocazione.BOSCO))
						.conRitaglioSuSfondo()
						.conElemento(ElementoIntermezzo.di("fuoco", ImmagineIntermezzo.animazione(Animazione.FUOCO_DA_CAMPO), 0.5, 0.68))
						// Guarda a sinistra finché non si accorge dell'eremita, poi si volta verso di lui
						.conElemento(personaggioVersoSinistra(classeEroe, 0.38, 0.61)
								.conBocca(0.5, -0.15)
								.poi(Tappa.inSecondi(2))
								.poi(Tappa.inSecondi(0.1).specchiata(VersoDiDefault.serveSpecchiare(classeEroe, Verso.DESTRA))))
						// L'eremita entra da destra e si ferma davanti all'eroe
						.conElemento(ElementoIntermezzo.personaggio("eremita", TipoPersonaggio.EREMITA, 1.1, 0.61)
								.conBocca(0.5, -0.15)
								.specchiato()
								.poi(Tappa.inSecondi(2).verso(0.62, 0.61)))
						.conBattuta(BattutaIntermezzo.di("eremita", "Chi va là?").daSecondo(2))
						.conBattuta(BattutaIntermezzo.di("eroe", "Mi chiamo " + eroe + ". Sto cercando il Drago."));
		finale.conBattuta(BattutaIntermezzo.di("eremita", "Il suo castello è nascosto da un incantesimo." +
				" Non riuscirai a trovarlo, a meno che tu prima non sconfigga i suoi alleati.").perSecondi(4));
		if (GruppoGiocatore.getIstanza().getCapo().getClasse() == TipoPersonaggio.OMBRAFIAMMA) {
			finale.conBattuta(BattutaIntermezzo.di("eremita", "E comunque... Non vorrei essere nei suoi panni."));
		}
		pagineIntermezzo.add(finale);
		return pagineIntermezzo;
	}

	/**
	 * L'eroe guarda a sinistra all'inizio, indipendentemente dal verso con cui è
	 * disegnata l'immagine della sua classe (vedi {@link VersoDiDefault}).
	 */
	private static ElementoIntermezzo personaggioVersoSinistra(TipoPersonaggio classe, double x, double y) {
		ElementoIntermezzo elemento = ElementoIntermezzo.personaggio("eroe", classe, x, y);
		if (VersoDiDefault.serveSpecchiare(classe, Verso.SINISTRA)) {
			elemento.specchiato();
		}
		return elemento;
	}
}
