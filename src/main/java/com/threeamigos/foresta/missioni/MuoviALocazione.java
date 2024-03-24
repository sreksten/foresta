package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.LocazioneUnica;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.ui.UI;

public class MuoviALocazione extends MissioneBase {

	private static final String COORDINATA_X = "COORDINATA_X";
	private static final String COORDINATA_Y = "COORDINATA_Y";

	public void setLocazioneUnica(ClassiLocazione classeLocazione) {
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(classeLocazione);
		aggiungiProprieta(COORDINATA_X, String.valueOf(coordinate.getX()));
		aggiungiProprieta(COORDINATA_Y, String.valueOf(coordinate.getY()));
		StringBuilder sb = new StringBuilder();
		sb.append("Raggiungi ").append(((LocazioneUnica)classeLocazione.getIstanza()).getNome());
		aggiungiProprieta(NOME, sb.toString());
		sb = new StringBuilder();
		sb.append("Raggiungi la locazione designata");
		aggiungiProprieta(DESCRIZIONE, sb.toString());
	}

	@Override
	public String getNome() {
		return ottieniProprieta(NOME);
	}

	@Override
	public String getDescrizione() {
		return ottieniProprieta(DESCRIZIONE);
	}

	@Override
	public void controllaPreLocazione() {
		CoordinateMD coordinate = GruppoGiocatore.getIstanza().getCoordinate();
		if (String.valueOf(coordinate.getX()).equals(ottieniProprieta(COORDINATA_X)) &&
				String.valueOf(coordinate.getY()).equals(ottieniProprieta(COORDINATA_Y))) {
			completaMissione();
		}
	}

	@Override
	public void completaMissione() {
		super.completaMissione();
		UI.notifica("Hai raggiunto " + ottieniProprieta(NOME));
	}

	@Override
	public void controllaInLocazione() {
		// Il controllo di raggiunta locazione viene fatto immediatamente
	}

	@Override
	public void controllaPostLocazione() {
		// Il controllo di raggiunta locazione viene fatto immediatamente
	}
}