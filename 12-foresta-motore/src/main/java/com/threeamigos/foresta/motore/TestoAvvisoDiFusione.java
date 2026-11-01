package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.AvvisoDiFusione;

import java.util.ArrayList;
import java.util.List;
import com.threeamigos.foresta.testi.Testi;

/**
 * Le frasi con cui l'incantatore avvisa di quel che la fusione farà perdere (vedi {@link AvvisoDiFusione}).
 */
final class TestoAvvisoDiFusione {

	private TestoAvvisoDiFusione() {
	}

	static String testo(AvvisoDiFusione avviso) {
		if (avviso.isNessunMiglioramento()) {
			return Testi.formatta("messaggi_motore", "AvvisoDiFusione.nessunMiglioramento");
		}
		List<String> frasi = new ArrayList<>();
		if (avviso.getPerdita() != null) {
			frasi.add(frasePerdita(avviso));
		}
		int scalzati = avviso.getEffettiScalzati();
		if (scalzati > 0) {
			frasi.add(Testi.formatta("messaggi_motore", scalzati == 1 ? "AvvisoDiFusione.scalzatoUno" : "AvvisoDiFusione.scalzati", scalzati));
		}
		return String.join(" ", frasi);
	}

	private static String frasePerdita(AvvisoDiFusione avviso) {
		switch (avviso.getPerdita()) {
			case LIMITE_DI_EFFETTI:
				return Testi.formatta("messaggi_motore", avviso.getEffettiCheEntrano() == 1 ? "AvvisoDiFusione.limiteUno" : "AvvisoDiFusione.limite", avviso.getEffettiCheEntrano());
			case MODIFICATORI_SUL_BERSAGLIO_SOLO_SULLE_ARMI:
				return Testi.formatta("messaggi_motore", "AvvisoDiFusione.bersaglioSoloArmi");
			case INCANTAMENTI_ELEMENTALI_NON_SUL_LIBRO:
				return Testi.formatta("messaggi_motore", "AvvisoDiFusione.elementaliSulLibro");
			default:
				return Testi.formatta("messaggi_motore", "AvvisoDiFusione.potereMagico");
		}
	}
}
