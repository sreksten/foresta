package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;

public class MuoviALocazione extends MissioneBase {

	public MuoviALocazione() {
		super(ClasseMissione.MUOVI_A_LOCAZIONE);
	}

	private static final String COORDINATA_X = "COORDINATA_X";
	private static final String COORDINATA_Y = "COORDINATA_Y";

	public void setLocazioneUnica(TipoLocazione tipoLocazione) {
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(tipoLocazione);
		aggiungiProprieta(COORDINATA_X, String.valueOf(coordinate.getX()));
		aggiungiProprieta(COORDINATA_Y, String.valueOf(coordinate.getY()));
		StringBuilder sb = new StringBuilder();
		sb.append("Raggiungi ").append(tipoLocazione.getNomeProprio());
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
		// Finché non ci si è arrivati, la locazione da raggiungere lampeggia sulla mappa
		if (!isCompleta() && ottieniProprieta(COORDINATA_X) != null) {
			RegistroMissioni.segnalaLocazione(this, new CoordinateMD(Integer.parseInt(ottieniProprieta(COORDINATA_X)),
					Integer.parseInt(ottieniProprieta(COORDINATA_Y))));
		}
		if (String.valueOf(coordinate.getX()).equals(ottieniProprieta(COORDINATA_X)) &&
				String.valueOf(coordinate.getY()).equals(ottieniProprieta(COORDINATA_Y))) {
			completaMissione();
		}
	}

	@Override
	public void completaMissione() {
		super.completaMissione();
		BusEventi.pubblica(new NotificaTestoParagrafo("Hai raggiunto " + ottieniProprieta(NOME)));
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