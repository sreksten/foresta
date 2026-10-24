package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.interfacce.OggettoConArticoli;
import com.threeamigos.foresta.tipi.TipoOggetto;

import java.util.Optional;

/**
 * Qualcosa che il gruppo trova in una locazione e può prendere (come lo prende lo decide {@link RaccoltaBottino}): monete, pietre preziose, un cofano, un anello, un oggetto di
 * una missione o un artefatto.
 * <p>
 * Gli oggetti delle locazioni non si salvano: se il gruppo non li prende, alla visita successiva la locazione decide di nuovo che
 * cosa c'è. Un {@link Artefatto} è anche un oggetto, ma è il pezzo di equipaggiamento vero, che si conserva e si salva con
 * il suo {@code ArtefattoMD}.
 */
public interface Oggetto extends OggettoConArticoli {

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
