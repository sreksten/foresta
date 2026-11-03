package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;

import java.util.ArrayList;
import java.util.List;

/**
 * Per i test: i testi di un incarico in città che le sottoclassi tengono protetti (le scene e le frasi che il motore
 * mostra nei vari momenti), così si possono controllare senza guidare l'automa.
 */
public final class TestiDiUnIncarico {

	private TestiDiUnIncarico() {
	}

	/** Le battute della scena in cui il mandante offre l'incarico, in ordine. */
	public static List<String> sceneDellIncarico(IncaricoInCitta incarico) {
		return battute(incarico.scenaIncarico().getPagine());
	}

	/** Le battute della scena in cui il mandante ringrazia, in ordine. */
	public static List<String> sceneDelRingraziamento(IncaricoInCitta incarico) {
		return battute(incarico.scenaRingraziamento().getPagine());
	}

	public static String accettazione(IncaricoInCitta incarico) {
		return incarico.testoAccettazione();
	}

	public static String consegna(IncaricoInCitta incarico) {
		return incarico.testoConsegna();
	}

	public static String ricompensa(IncaricoInCitta incarico) {
		return incarico.testoRicompensa();
	}

	private static List<String> battute(List<PaginaIntermezzo> pagine) {
		List<String> testi = new ArrayList<>();
		for (PaginaIntermezzo pagina : pagine) {
			if (pagina.getTesto() != null && !pagina.getTesto().isEmpty()) {
				testi.add(pagina.getTesto());
			}
			for (BattutaProgrammata programmata : pagina.getBattuteProgrammate()) {
				testi.add(programmata.getBattuta().getTesto());
			}
		}
		return testi;
	}
}
