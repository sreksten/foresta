package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroArtefatti;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.ArrayList;
import java.util.List;

// Ubicazione artefatto/citta'/castello
/**
 * L'offerta di qualche notizia gratuita: dove si trova un artefatto smarrito, una città o un castello.
 */
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
		TipoLocazione tipoLocazione = Foresta.getLocazioneDelGruppo();
		int tipo;
		if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
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
		List<TipoLocazione> citta = new ArrayList<>();
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA &&
					Foresta.getCoordinateLocazioneUnica(tipoLocazione) != null) {
				citta.add(tipoLocazione);
			}
		}
		if (citta.isEmpty()) {
			sb.append("tutte le città sono state distrutte dal Drago.");
		} else {
			int indice = Dado.tiraAncheAUnaFaccia(citta.size()) - 1;
			TipoLocazione tipoLocazione = citta.get(indice);
			CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(tipoLocazione);
			Foresta.setLocazioneConosciuta(coordinate);
			sb.append(tipoLocazione.getNomeProprio());
			sb.append(" si trova ").append(Misc.getDirezione(Foresta.getPosizioneDelGruppo(), coordinate));
		}
	}

	private void informazioniSuCastello(GruppoGiocatore gruppo, StringBuilder sb) {
		TipoLocazione tipoLocazione = TipoLocazione.CASTELLO_DRAGO;
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CASTELLO_DRAGO);
		if (coordinate == null) {
			List<TipoLocazione> castelli = new ArrayList<>();
			for (TipoLocazione corrente : TipoLocazione.values()) {
				if (corrente.getCategoriaLocazione() == CategoriaLocazione.CASTELLO &&
						Foresta.getCoordinateLocazioneUnica(corrente) != null) {
					castelli.add(corrente);
				}
			}
			if (castelli.isEmpty()) {
				sb.append("tutti i castelli sono stati distrutti dal Drago.");
				return;
			}
			int indice = Dado.tiraAncheAUnaFaccia(castelli.size()) - 1;
			tipoLocazione = castelli.get(indice);
			coordinate = Foresta.getCoordinateLocazioneUnica(tipoLocazione);
		}
		Foresta.setLocazioneConosciuta(coordinate);
		sb.append(tipoLocazione.getNomeProprio());
		sb.append(" sorge ").append(Misc.getDirezione(Foresta.getPosizioneDelGruppo(), coordinate));
	}

	private void informazioneSuArtefatti(GruppoGiocatore gruppo, StringBuilder sb) {
		RegistroArtefattiMD.ArtefattoESuaUbicazione informazioni = RegistroArtefatti.getArtefattoCasuale();
		if (informazioni == null) {
			sb.append("tutti gli artefatti sono stati recuperati da intrepidi eroi.");
		} else {
			ArtefattoMD artefatto = informazioni.getArtefattoMD();
			Foresta.setLocazioneConosciuta(informazioni.getCoordinate());
			RegistroArtefatti.segnaLocalizzazioneConosciuta(informazioni.getCoordinate());
			String nomeCompleto = artefatto.getNomeCompleto();
			sb.append(nomeCompleto);
			// Con il nome proprio o la descrizione il nome completo è un inciso ("Diavolina, la spada di fuoco, che brucia"): la virgola lo chiude
			if (nomeCompleto.indexOf(',') >= 0) {
				sb.append(',');
			}
			sb.append(" si trova in un tempio ")
			.append(Misc.getDirezione(Foresta.getPosizioneDelGruppo(), informazioni.getCoordinate()));
		}
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		// Le informazioni vengono accettate automaticamente
	}
}
