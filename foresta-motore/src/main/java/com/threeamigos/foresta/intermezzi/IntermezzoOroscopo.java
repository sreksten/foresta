package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale.DialogoSullOroscopo;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.List;

/**
 * Intermezzo per l'ingresso nella bottega dell'alchimista: il gruppo entra, l'alchimista legge l'oroscopo del giorno
 * (oroscopo.txt, una frase per battuta), il capo, che vive nella Foresta e non sa che cosa siano un server o un disco
 * rigido, chiede spiegazioni su una delle cose che ha sentito, l'alchimista glissa con eleganza e poi dà il benvenuto.
 * <p>
 * Per ora scatta a ogni ingresso, non una volta sola: vedi {@link #isRipetibile()}.
 */
public class IntermezzoOroscopo implements Intermezzo {

	// Una frase finisce con un punto, un punto esclamativo o interrogativo, seguito da uno spazio
	private static final String FINE_FRASE = "(?<=[.!?])\\s+";

	private final TipoIntermezzo tipo;
	private final String saluto;

	IntermezzoOroscopo(TipoIntermezzo tipo, String saluto) {
		this.tipo = tipo;
		this.saluto = saluto;
	}

	@Override
	public String getId() {
		return tipo.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return NegozioInScena.ALCHIMISTA.isIngresso(momento);
	}

	/**
	 * Per ora l'oroscopo si legge a ogni ingresso: commentando questo metodo l'intermezzo torna a scattare una volta sola.
	 */
	@Override
	public boolean isRipetibile() {
		return true;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		DialogoSullOroscopo dialogo = ProduttoreDiTestiCasuale.dialogoSullOroscopo();
		ScenaNegozio scena = NegozioInScena.ALCHIMISTA.entraIlGruppo();
		for (String frase : dialogo.getOroscopo().split(FINE_FRASE)) {
			scena.parlaIlNegoziante(frase);
		}
		return scena.parlaIlCapo(dialogo.getDomanda())
				.parlaIlNegoziante(dialogo.getRisposta())
				.parlaIlNegoziante(saluto)
				.getPagine();
	}
}
