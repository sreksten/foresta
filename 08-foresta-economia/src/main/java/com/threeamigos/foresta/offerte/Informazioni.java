package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.mappa.Direzioni;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.ArrayList;
import java.util.List;
import com.threeamigos.foresta.testi.Testi;

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
		StringBuilder sb = new StringBuilder(Testi.formatta("offerte", "INFORMAZIONI.INTRO",
				gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)));
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
			sb.append(Testi.formatta("offerte", "INFORMAZIONI.CITTA_DISTRUTTE"));
		} else {
			int indice = Dado.tiraAncheAUnaFaccia(citta.size()) - 1;
			TipoLocazione tipoLocazione = citta.get(indice);
			CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(tipoLocazione);
			Foresta.setLocazioneConosciuta(coordinate);
			sb.append(Testi.formatta("offerte", "INFORMAZIONI.CITTA", tipoLocazione.getNomeProprio(),
					Direzioni.getDirezione(Foresta.getPosizioneDelGruppo(), coordinate)));
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
				sb.append(Testi.formatta("offerte", "INFORMAZIONI.CASTELLI_DISTRUTTI"));
				return;
			}
			int indice = Dado.tiraAncheAUnaFaccia(castelli.size()) - 1;
			tipoLocazione = castelli.get(indice);
			coordinate = Foresta.getCoordinateLocazioneUnica(tipoLocazione);
		}
		Foresta.setLocazioneConosciuta(coordinate);
		sb.append(Testi.formatta("offerte", "INFORMAZIONI.CASTELLO", tipoLocazione.getNomeProprio(),
				Direzioni.getDirezione(Foresta.getPosizioneDelGruppo(), coordinate)));
	}

	private void informazioneSuArtefatti(GruppoGiocatore gruppo, StringBuilder sb) {
		RegistroArtefattiMD.ArtefattoESuaUbicazione informazioni = RegistroArtefatti.getArtefattoCasuale();
		if (informazioni == null) {
			sb.append(Testi.formatta("offerte", "INFORMAZIONI.ARTEFATTI_RECUPERATI"));
		} else {
			ArtefattoMD artefatto = informazioni.getArtefattoMD();
			Foresta.setLocazioneConosciuta(informazioni.getCoordinate());
			RegistroArtefatti.segnaLocalizzazioneConosciuta(informazioni.getCoordinate());
			String nomeCompleto = artefatto.getNomeCompleto();
			// Con il nome proprio o la descrizione il nome completo è un inciso ("Diavolina, la spada di fuoco, che brucia"): la virgola lo chiude
			sb.append(Testi.formatta("offerte", nomeCompleto.indexOf(',') >= 0 ? "INFORMAZIONI.ARTEFATTO_INCISO" : "INFORMAZIONI.ARTEFATTO", nomeCompleto,
					Direzioni.getDirezione(Foresta.getPosizioneDelGruppo(), informazioni.getCoordinate())));
		}
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		// Le informazioni vengono accettate automaticamente
	}
}
