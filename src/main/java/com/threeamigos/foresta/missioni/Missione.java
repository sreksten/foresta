package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.MissioneMD;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.List;
import java.util.Optional;

public interface Missione {

	String getId();

	String getNome();
	
	String getDescrizione();

	boolean isDescrizioneVisibile();

	void mostraDescrizione();

	void nascondiDescrizione();
	
	void controllaPreLocazione();
	
	void controllaInLocazione();

	void controllaPostLocazione();

	/**
	 * Quando il gruppo si accampa, prima che scattino gli intermezzi dell'accampamento: le missioni che nascono
	 * intorno al fuoco (vedi LaLealta). Fuori da una locazione: solo le missioni a passi lo usano.
	 */
	default void controllaAccampamento() {
	}

	boolean isPrimaria();
	
	boolean isAttiva();
	
	void attivaMissione();

	boolean isCompleta();
	
	void completaMissione();

	/**
	 * Vero se la missione non puo' piu' essere completata (per esempio la citta' in cui andava conclusa e' stata
	 * distrutta): e' finita, ma senza successo.
	 */
	boolean isFallita();

	/**
	 * La missione termina senza successo: esce dalle attive e passa tra le fallite, senza esperienza.
	 */
	void fallisciMissione();

	/**
	 * La frase da scrivere a chi entra nella locazione che la missione aveva rivendicato, una volta finita (vedi
	 * RegistroMissioni.getRicordo): per esempio "Qui sorgeva il castello della Strega." fra le rovine del castello.
	 * È la frase intera perché il verbo dipende dal luogo: un castello sorgeva, in una grotta si nascondeva
	 * qualcosa. Null se non c'è niente da ricordare.
	 */
	default String getRicordoDellaLocazione() {
		return null;
	}

	/**
	 * L'oggetto che la missione vuole nella locazione in cui il gruppo sta entrando, al posto di quello che la
	 * locazione avrebbe avuto (vedi RegistroMissioni.getOggettoMissione): per esempio le radici di mandragola che
	 * chiede l'alchimista. Vuoto se non ne vuole.
	 *
	 * @param visitata se il gruppo aveva già completato la locazione in una visita precedente
	 */
	default Optional<Oggetto> getOggettoInLocazione(CoordinateMD coordinate, ClassiLocazione classe, boolean visitata) {
		return Optional.empty();
	}

	/**
	 * Gli avversari che la missione vuole nella locazione in cui il gruppo sta entrando, al posto di quelli che la
	 * locazione avrebbe avuto (vedi RegistroMissioni.getIncontroMissione): per esempio la banda di un brigante su
	 * cui c'è una taglia. Vuoto se non ne vuole.
	 */
	default Optional<List<Personaggio>> getIncontroInLocazione(CoordinateMD coordinate) {
		return Optional.empty();
	}

	MissioneMD getModelloDati();
	
	void setModelloDati(MissioneMD modelloDati);

	String ottieniProprieta(String nome);

	void aggiungiProprieta(String nome, String valore);

	void rimuoviProprieta(String nome);

	void aggiungiMissione(Missione missione);

	void rimuoviMissione(Missione missione);

	void sostituisciMissioniSecondarie(List<Missione> missioni);

	List<Missione> getMissioniSecondarie();

}
