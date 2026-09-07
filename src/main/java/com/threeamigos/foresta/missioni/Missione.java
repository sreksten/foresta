package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.modellodati.MissioneMD;

import java.util.List;

public interface Missione {

	String getId();

	String getNome();
	
	String getDescrizione();

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

	MissioneMD getModelloDati();
	
	void setModelloDati(MissioneMD modelloDati);

	String ottieniProprieta(String nome);

	void aggiungiProprieta(String nome, String valore);

	void rimuoviProprieta(String nome);

	void aggiungiMissione(Missione missione);

	void rimuoviMissione(Missione missione);

	List<Missione> getMissioniSecondarie();

}
