package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.ui.UI;

public class SconfiggiIlDrago extends MissioneBase implements Missione {

	private static final String DRAGO_APPARSO = "DRAGO_APPARSO";
	
	public SconfiggiIlDrago() {
	}

	@Override
	public String getNome() {
		return "Sconfiggi il Drago";
	}

	@Override
	public String getDescrizione() {
		StringBuilder sb = new StringBuilder();
		sb.append("La Foresta e' minacciata da un temibile Drago. ");
		if (!isDragoApparso()) {
			sb.append("Il Castello dove si trova e' nascosto da un incantesimo. ");
			sb.append(GruppoGiocatore.getIstanza().getCapo().getNome());
			sb.append(" deve sconfiggere tutti i suoi alleati per poterlo affrontare!");			
		} else {
			sb.append("Occorre entrare nel suo Castello ed affrontarlo!");
		}
		return sb.toString();
	}

	@Override
	public void controllaPreLocazione() {
		if (!isAttiva()) {
			UI.notifica(getDescrizione());
			attivaMissione();
		}
	}
	
	@Override
	public void controllaInLocazione() {
		// Non succede niente
	}

	@Override
	public void controllaPostLocazione() {
		if (!isDragoApparso() && castelliDistrutti()) {
			UI.notifica("L'incantesimo che nascondeva il castello del Drago e' svanito! La missione e' quasi giunta al termine!");
			CoordinateMD coordinateCastelloDrago = Foresta.costruisciLocazioneUnica(ClassiLocazione.CASTELLO_DRAGO, false);
			Foresta.setLocazioneVisitata(coordinateCastelloDrago, false);
			setDragoApparso();
		} else {
			GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
			if (gruppo.getClasseLocazione() == ClassiLocazione.CASTELLO_DRAGO && gruppo.getClasseLocazione().getIstanza().isCompleta()) {
				completaMissione();
				UI.notifica("Il Drago e' morto!");
				LineaTemporale.setGiocoFinito(true);
			}
		}
	}

	private boolean castelliDistrutti() {
		boolean castelliDistrutti = true;
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == TipoLocazione.CASTELLO && classeLocazione != ClassiLocazione.CASTELLO_DRAGO &&
					!classeLocazione.getIstanza().isCompleta()) {
				Logger.log(classeLocazione.name() + " non ancora completata");
				castelliDistrutti = false;
			}
		}
		return castelliDistrutti;
	}

	@Override
	public boolean isPrimaria() {
		return true;
	}
	
	public boolean isDragoApparso() {
		return md.ottieniProprieta(DRAGO_APPARSO) != null;
	}
	
	public void setDragoApparso() {
		md.aggiungiProprieta(DRAGO_APPARSO, AFFERMATIVO);
	}
}
