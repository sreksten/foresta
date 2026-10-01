package com.threeamigos.foresta.intermezzi;

import java.util.Collections;
import java.util.List;

/**
 * Intermezzo per il checkpoint {@link MomentoIntermezzo#INGRESSO_VENDITORE_DI_PERGAMENE}:
 * scatta una sola volta per l'intera partita, la prima volta che si entra dal venditore
 * di pergamene di una città. Per ora il venditore si limita a salutare; in futuro
 * spiegherà come funziona il negozio.
 */
public class IntermezzoVenditoreDiPergamene implements Intermezzo {

	private static final double X_VENDITORE = 0.5;
	private static final double Y_VENDITORE = 0.6;

	@Override
	public String getId() {
		return ClasseIntermezzo.INTERMEZZO_VENDITORE_DI_PERGAMENE.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.INGRESSO_VENDITORE_DI_PERGAMENE;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		PaginaIntermezzo pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa("fondi/InternoVenditoreDiPergamene.gif"))
				.conRitaglioSuSfondo()
				.conElemento(ElementoIntermezzo.di("venditore", ImmagineIntermezzo.risorsa("personaggi/VenditoreDiPergamene.gif"), X_VENDITORE, Y_VENDITORE)
						.conBocca(0.5, -0.15))
				.conBattuta(BattutaIntermezzo.di("venditore", "Ciao."));
		return Collections.singletonList(pagina);
	}
}
