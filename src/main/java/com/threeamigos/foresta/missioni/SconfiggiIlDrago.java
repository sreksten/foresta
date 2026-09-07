package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.UI;

public class SconfiggiIlDrago extends MissioneBase implements Missione {

	public SconfiggiIlDrago() {
		super(ClasseMissione.SCONFIGGI_IL_DRAGO);
	}

	private static final String DRAGO_APPARSO = "DRAGO_APPARSO";

	@Override
	public String getNome() {
		return "Sconfiggi il Drago";
	}

	@Override
	public String getDescrizione() {
		StringBuilder sb = new StringBuilder();
		sb.append("La Foresta è minacciata da un temibile Drago. ");
		if (isDragoNonApparso()) {
			sb.append("Il Castello dove si trova è nascosto da un incantesimo. ");
			sb.append(GruppoGiocatore.getIstanza().getCapo().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
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
		if (isDragoNonApparso() && castelliDistrutti()) {
			UI.notifica("L'incantesimo che nascondeva il castello del Drago è svanito! La missione è quasi giunta al termine!");
			CoordinateMD coordinateCastelloDrago = Foresta.costruisciLocazioneUnica(ClassiLocazione.CASTELLO_DRAGO, false);
			Foresta.setLocazioneVisitata(coordinateCastelloDrago, false);
			setDragoApparso();
		} else {
			GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
			if (gruppo.getClasseLocazioneCorrente() == ClassiLocazione.CASTELLO_DRAGO && gruppo.getClasseLocazioneCorrente().getIstanza().isCompleta()) {
				completaMissione();
				UI.notifica("Il Drago è morto!");
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
	
	public boolean isDragoNonApparso() {
		return md.ottieniProprieta(DRAGO_APPARSO) == null;
	}
	
	public void setDragoApparso() {
		md.aggiungiProprieta(DRAGO_APPARSO, AFFERMATIVO);
	}
}
