package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.modellodati.MissioneMD;

public interface Missione {

	public String getNome();
	
	public String getDescrizione();
	
	public void controllaPreLocazione();
	
	public void controllaInLocazione();

	public void controllaPostLocazione();

	public boolean isPrimaria();
	
	public boolean isAttiva();
	
	public void attivaMissione();

	public boolean isCompleta();
	
	public void completaMissione();

	public MissioneMD getModelloDati();
	
	public void setModelloDati(MissioneMD modelloDati);
	
}
