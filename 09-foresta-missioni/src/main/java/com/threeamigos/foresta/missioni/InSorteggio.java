package com.threeamigos.foresta.missioni;

/**
 * Una missione che si offre entrando in un posto e che, se in quella visita ne sono pronte più d'una, entra nel sorteggio che ne sceglie
 * una sola (vedi SceltaDegliIncarichi): gli incarichi in città, le leggende, la falce del Tristo Mietitore.
 */
public interface InSorteggio extends Missione {

	/**
	 * Se la missione non si è ancora offerta: è al primo passo e non è finita.
	 */
	boolean isInAttesaDiOffrirsi();

	/**
	 * Se il gruppo è dove la missione si offre e ci sono tutte le condizioni per farlo adesso.
	 */
	boolean isProntoPerOffrirsi();

	/**
	 * Se la missione potrà ancora offrirsi in questa partita, prima o poi: una che non potrà più farlo non tiene aperto il giro.
	 */
	boolean puoAncoraOffrirsi();

	/**
	 * Il giro in cui la missione è stata affidata (vedi SceltaDegliIncarichi), o -1 se non si è ancora offerta.
	 */
	int getGiro();
}
