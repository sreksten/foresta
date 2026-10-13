package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.List;

/**
 * Scenetta di chi entra in un negozio o in una locanda a ore impossibili (dopo la chiusura
 * del locale e prima della prima ora del mattino): il capo chiede se quelli come il
 * negoziante non chiudono mai, e il negoziante gli dà la risposta che si merita. Scatta
 * una sola volta per partita per ogni tipo di locale, e solo se all'ingresso non scatta
 * nessun altro intermezzo (vedi {@link #isDiRipiego()}): la prima volta in un negozio si
 * viene comunque accolti dal benvenuto. In una locanda, solo dalla terza visita.
 */
public class IntermezzoNonChiudeteMai implements Intermezzo {

	private static final int VISITE_ALLA_LOCANDA_PRIMA_DELLA_SCENETTA = 2;

	private final TipoIntermezzo tipo;
	private final NegozioInScena negozio;

	IntermezzoNonChiudeteMai(TipoIntermezzo tipo, NegozioInScena negozio) {
		this.tipo = tipo;
		this.negozio = negozio;
	}

	@Override
	public String getId() {
		return tipo.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		if (!negozio.isIngresso(momento) || !isNotte()) {
			return false;
		}
		return negozio != NegozioInScena.LOCANDA
				|| Locande.getVisiteLocandaCorrente() >= VISITE_ALLA_LOCANDA_PRIMA_DELLA_SCENETTA;
	}

	private boolean isNotte() {
		int ora = LineaTemporale.getOra();
		return ora >= negozio.getOraDiChiusura() || ora < LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO;
	}

	@Override
	public boolean isDiRipiego() {
		return true;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		return negozio.entraIlGruppo()
				.parlaIlCapo("Ma voi " + negozio.getNegozianti() + " non chiudete mai?")
				.parlaIlNegoziante("No, perché esistono clienti come te.")
				.getPagine();
	}
}
