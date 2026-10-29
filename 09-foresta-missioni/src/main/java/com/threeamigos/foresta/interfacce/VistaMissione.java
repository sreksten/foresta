package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.tipi.MissionePredefinita;

import java.util.List;

/**
 * Una missione vista dalla UI, in sola lettura (vedi VistaPartita). La implementa Missione.
 */
public interface VistaMissione {

	String getId();

	MissionePredefinita getClasse();

	String getNome();

	String getDescrizione();

	/**
	 * Se nel riquadro delle missioni la descrizione è aperta (si cambia con ComandoCommutazioneElenco)
	 */
	boolean isDescrizioneVisibile();

	boolean isAttiva();

	boolean isCompleta();

	List<? extends VistaMissione> getMissioniSecondarie();

}
