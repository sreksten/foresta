package com.threeamigos.foresta.offerte;

import java.util.ArrayList;
import java.util.List;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.LocazioneUnica;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroArtefatti;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.tools.Random;

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
				.append(gruppo.getCapo().getNome()).append(" viene a sapere che ");
		ClassiLocazione classeLocazione = gruppo.getClasseLocazione();
		int tipo;
		if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA) {
			// Qualsiasi informazione ma non quelle sulle citta' visto che gia' ci siamo
			tipo = 1 + Random.getInt(2);
		} else {
			tipo = Random.getInt(3);
		}
		if (tipo == 0 || tipo == 1) {
			if (tipo == 0) {
				informazioniSuCitta(gruppo, sb);
			} else {
				informazioniSuCastello(gruppo, sb);
			}
		} else {
			informazioneSuArtefatti(gruppo, sb);
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
			sb.append("tutte le citta' sono state distrutte dal Drago.");
		} else {
			ClassiLocazione classeLocazione = citta.get(Random.getInt(citta.size()));
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
			classeLocazione = castelli.get(Random.getInt(castelli.size()));
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
