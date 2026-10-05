package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermezzo di prova per il checkpoint {@link MomentoIntermezzo#LOCAZIONE_COMPLETATA}:
 * copia spudorata di {@link IntermezzoIntroduttivo}, con l'unica differenza che l'eremita
 * pronuncia una sola battuta. Scatta alla prima fine locazione della partita e non si
 * ripete più.
 */
public class IntermezzoFinePrimaLocazione implements Intermezzo {

	@Override
	public String getId() {
		return ClasseIntermezzo.INTERMEZZO_FINE_PRIMA_LOCAZIONE.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.LOCAZIONE_COMPLETATA;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		Personaggio capo = GruppoGiocatore.getIstanza().getCapo();
		// Senza nome proprio l'eroe si chiama con la sua classe ("il guerriero")
		String eroe = capo.getNomeProprio()
				.orElseGet(() -> capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
		TipoPersonaggio classeEroe = capo.getClasse();
		List<PaginaIntermezzo> pagineIntermezzo = new ArrayList<>();
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
						.conBattuta(BattutaIntermezzo.di("eremita", "Vedo che sei preparat" +
								GruppoGiocatore.getIstanza().getCapo().getLetteraFinaleAttributo() +
								". Buona fortuna nella tua missione!").daSecondo(2));
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
