package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.modellodati.MissioneMD;

public interface Missione {

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
	
}
