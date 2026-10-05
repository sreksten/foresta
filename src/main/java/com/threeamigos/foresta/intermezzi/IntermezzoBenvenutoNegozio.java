package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.TipoIntermezzo;
import java.util.List;

/**
 * Intermezzo per l'ingresso in un negozio di città: scatta una sola volta per l'intera
 * partita, la prima volta che si entra in quel tipo di negozio. Il gruppo entra in scena e
 * il negoziante saluta; in futuro spiegherà come funziona il negozio.
 */
public class IntermezzoBenvenutoNegozio implements Intermezzo {

	private final TipoIntermezzo tipo;
	private final NegozioInScena negozio;
	private final String saluto;

	IntermezzoBenvenutoNegozio(TipoIntermezzo tipo, NegozioInScena negozio, String saluto) {
		this.tipo = tipo;
		this.negozio = negozio;
		this.saluto = saluto;
	}

	@Override
	public String getId() {
		return tipo.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return negozio.isIngresso(momento);
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		return negozio.entraIlGruppo()
				.parlaIlNegoziante(saluto)
				.getPagine();
	}
}
