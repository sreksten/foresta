package com.threeamigos.foresta.intermezzi;

import java.util.Collections;
import java.util.List;

/**
 * Intermezzo per il checkpoint {@link MomentoIntermezzo#INGRESSO_ALCHIMISTA}: scatta una
 * sola volta per l'intera partita, la prima volta che si entra dall'alchimista di una
 * città. Per ora l'alchimista si limita a salutare; in futuro spiegherà come funziona
 * il negozio.
 */
public class IntermezzoAlchimista implements Intermezzo {

	private static final double X_ALCHIMISTA = 0.5;
	private static final double Y_ALCHIMISTA = 0.6;

	@Override
	public String getId() {
		return ClasseIntermezzo.INTERMEZZO_ALCHIMISTA.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.INGRESSO_ALCHIMISTA;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		PaginaIntermezzo pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa("fondi/InternoAlchimista.gif"))
				.conRitaglioSuSfondo()
				.conElemento(ElementoIntermezzo.di("alchimista", ImmagineIntermezzo.risorsa("personaggi/Alchimista.gif"), X_ALCHIMISTA, Y_ALCHIMISTA)
						.conBocca(0.5, -0.15))
				.conBattuta(BattutaIntermezzo.di("alchimista", "Ciao."));
		return Collections.singletonList(pagina);
	}
}
