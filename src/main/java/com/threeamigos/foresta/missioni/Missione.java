package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.modellodati.MissioneMD;

import java.util.List;

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
