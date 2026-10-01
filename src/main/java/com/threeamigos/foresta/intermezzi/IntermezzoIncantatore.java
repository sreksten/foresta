package com.threeamigos.foresta.intermezzi;

import java.util.Collections;
import java.util.List;

/**
 * Intermezzo per il checkpoint {@link MomentoIntermezzo#INGRESSO_INCANTATORE}: scatta una
 * sola volta per l'intera partita, la prima volta che si entra dall'incantatore di una
 * città. Per ora l'incantatore si limita a salutare; in futuro spiegherà come funziona
 * il negozio.
 */
public class IntermezzoIncantatore implements Intermezzo {

	private static final double X_INCANTATORE = 0.5;
	private static final double Y_INCANTATORE = 0.6;

	@Override
	public String getId() {
		return ClasseIntermezzo.INTERMEZZO_INCANTATORE.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.INGRESSO_INCANTATORE;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		PaginaIntermezzo pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa("fondi/InternoIncantatore.gif"))
				.conRitaglioSuSfondo()
				.conElemento(ElementoIntermezzo.di("incantatore", ImmagineIntermezzo.risorsa("personaggi/Incantatore.gif"), X_INCANTATORE, Y_INCANTATORE)
						.conBocca(0.5, -0.15))
				.conBattuta(BattutaIntermezzo.di("incantatore", "Ciao."));
		return Collections.singletonList(pagina);
	}
}
