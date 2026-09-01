package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.LocazioneUnica;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

import java.util.ArrayList;
import java.util.List;

// Ubicazione artefatto/citta'/castello
public class Informazioni implements Offerta {

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return true;
	}

	@Override
	public boolean isGratuita(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return true;
	}

	@Override
	public String getDescrizione(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		StringBuilder sb = new StringBuilder("Scambiando quattro chiacchiere, ")
				.append(gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE))
				.append(" viene a sapere che ");
		ClassiLocazione classeLocazione = gruppo.getClasseLocazioneCorrente();
		int tipo;
		if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA) {
			// Qualsiasi informazione ma non quelle sulle citta' visto che gia' ci siamo
			tipo = Dado.tira(2, 3);
		} else {
			tipo = Dado.tira(3);
		}
		switch (tipo) {
			case 1:
				informazioniSuCitta(gruppo, sb);
				break;
			case 2:
				informazioniSuCastello(gruppo, sb);
				break;
			default:
				informazioneSuArtefatti(gruppo, sb);
				break;
		}
		return sb.toString();
	}
		
	private void informazioniSuCitta(GruppoGiocatore gruppo, StringBuilder sb) {
		List<ClassiLocazione> citta = new ArrayList<>();
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA &&
					Foresta.getCoordinateLocazioneUnica(classeLocazione) != null) {
				citta.add(classeLocazione);
			}
		}
		if (citta.isEmpty()) {
			sb.append("tutte le città sono state distrutte dal Drago.");
		} else {
			int indice = Dado.tira(citta.size()) - 1;
			ClassiLocazione classeLocazione = citta.get(indice);
			CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(classeLocazione);
			Foresta.setLocazioneConosciuta(coordinate);
			sb.append(((LocazioneUnica)classeLocazione.getIstanza()).getNome());
			sb.append(" si trova ").append(Misc.getDirezione(gruppo, coordinate));
		}
	}

	private void informazioniSuCastello(GruppoGiocatore gruppo, StringBuilder sb) {
		ClassiLocazione classeLocazione = ClassiLocazione.CASTELLO_DRAGO;
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CASTELLO_DRAGO);
		if (coordinate == null) {
			List<ClassiLocazione> castelli = new ArrayList<>();
			for (ClassiLocazione corrente : ClassiLocazione.values()) {
				if (corrente.getTipoLocazione() == ClassiLocazione.TipoLocazione.CASTELLO &&
						Foresta.getCoordinateLocazioneUnica(corrente) != null) {
					castelli.add(corrente);
				}
			}
			int indice = Dado.tira(castelli.size()) - 1;
			classeLocazione = castelli.get(indice);
			coordinate = Foresta.getCoordinateLocazioneUnica(classeLocazione);
		}
		Foresta.setLocazioneConosciuta(coordinate);
		sb.append(((LocazioneUnica)classeLocazione.getIstanza()).getNome());
		sb.append(" sorge ").append(Misc.getDirezione(gruppo, coordinate));
	}

	private void informazioneSuArtefatti(GruppoGiocatore gruppo, StringBuilder sb) {
		RegistroArtefattiMD.ArtefattoESuaUbicazione informazioni = RegistroArtefatti.getArtefattoCasuale();
		if (informazioni == null) {
			sb.append(" tutti gli artefatti sono stati recuperati da intrepidi eroi.");
		} else {
			ArtefattoMD artefatto = informazioni.getArtefattoMD();
			sb.append(artefatto.getNome()).append(", ").append(artefatto.getDescrizione())
			.append(", si trova in un tempio ")
			.append(Misc.getDirezione(gruppo, informazioni.getCoordinate()));
		}
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		// Le informazioni vengono accettate automaticamente
	}
}
