package com.threeamigos.foresta.intermezzi;

import java.util.Collections;
import java.util.List;

/**
 * Intermezzo per il checkpoint {@link MomentoIntermezzo#INGRESSO_ARMAIOLO}: scatta una
 * sola volta per l'intera partita, la prima volta che si entra dall'armaiolo di una
 * città. Per ora l'armaiolo si limita a salutare; in futuro spiegherà come funziona
 * il negozio.
 */
public class IntermezzoArmaiolo implements Intermezzo {

	private static final double X_ARMAIOLO = 0.5;
	private static final double Y_ARMAIOLO = 0.6;

	@Override
	public String getId() {
		return ClasseIntermezzo.INTERMEZZO_ARMAIOLO.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.INGRESSO_ARMAIOLO;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		PaginaIntermezzo pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa("fondi/InternoArmaiolo.gif"))
				.conRitaglioSuSfondo()
				.conElemento(ElementoIntermezzo.di("armaiolo", ImmagineIntermezzo.risorsa("personaggi/Armaiolo.gif"), X_ARMAIOLO, Y_ARMAIOLO)
						.conBocca(0.5, -0.15))
				.conBattuta(BattutaIntermezzo.di("armaiolo", "Ciao."));
		return Collections.singletonList(pagina);
	}
}
