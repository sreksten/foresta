package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.interfacce.OggettoConArticoli;
import com.threeamigos.foresta.tipi.TipoOggetto;

import java.util.Optional;

/**
 * Il bottino: qualcosa che il gruppo trova in una locazione e può prendere (come lo prende lo decide
 * {@link RaccoltaBottino}). Sono monete, pietre preziose, un cofano, un anello, un oggetto di una missione o un artefatto;
 * di ciascuno sa cos'è ({@link #getClasse()}), come si chiama e quanti ne sono stati trovati ({@link #getQuantita()}), ed
 * eventualmente quale artefatto porta ({@link #getArtefatto()}).
 * <p>
 * Il bottino delle locazioni non si salva: se il gruppo non lo prende, alla visita successiva la locazione decide di nuovo che
 * cosa c'è. Un {@link Artefatto} non è bottino: è il pezzo di equipaggiamento vero, che si conserva e si salva con
 * il suo {@code ArtefattoMD}. Quando il gruppo ne trova uno già fatto (quello di un tempio) lo trova dentro un
 * {@link BottinoArtefatto}.
 */
public interface Bottino extends OggettoConArticoli {

	/**
	 * La classe dell'oggetto da TipoOggetto
	 */
    TipoOggetto getClasse();

	/**
	 * Il nome singolare dell'oggetto
	 */
    String getNomeSingolare();
	/**
	 * Il nome plurale dell'oggetto
	 */
    String getNomePlurale();

	/**
	 * Quanti oggetti il gruppo giocante ha trovato
	 */
    int getQuantita();

	/**
	 * L'artefatto che l'oggetto consegna a chi lo prende, se ne ha uno (artefatti dei templi,
	 * spade, scudi, anelli magici...): serve per proporre solo i personaggi che possono equipaggiarlo.
	 */
	default Optional<Artefatto> getArtefatto() {
		return Optional.empty();
	}
}
