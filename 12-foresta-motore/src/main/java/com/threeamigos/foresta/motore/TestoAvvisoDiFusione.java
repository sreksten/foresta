package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.AvvisoDiFusione;

import java.util.ArrayList;
import java.util.List;

/**
 * Le frasi con cui l'incantatore avvisa di quel che la fusione farà perdere (vedi {@link AvvisoDiFusione}).
 */
final class TestoAvvisoDiFusione {

	private TestoAvvisoDiFusione() {
	}

	static String testo(AvvisoDiFusione avviso) {
		if (avviso.isNessunMiglioramento()) {
			return "Nessun effetto è migliore di quelli dell'artefatto: la fusione non cambierebbe niente.";
		}
		List<String> frasi = new ArrayList<>();
		if (avviso.getPerdita() != null) {
			frasi.add(frasePerdita(avviso));
		}
		int scalzati = avviso.getEffettiScalzati();
		if (scalzati > 0) {
			frasi.add(scalzati == 1
					? "Un effetto dell'artefatto sarà scalzato da uno migliore."
					: scalzati + " effetti dell'artefatto saranno scalzati da altri migliori.");
		}
		return String.join(" ", frasi);
	}

	private static String frasePerdita(AvvisoDiFusione avviso) {
		switch (avviso.getPerdita()) {
			case LIMITE_DI_EFFETTI:
				return avviso.getEffettiCheEntrano() == 1
						? "L'artefatto tiene i migliori effetti: passerà un solo effetto, gli altri andranno persi."
						: "L'artefatto tiene i migliori effetti: passeranno " + avviso.getEffettiCheEntrano() + " effetti, gli altri andranno persi.";
			case MODIFICATORI_SUL_BERSAGLIO_SOLO_SULLE_ARMI:
				return "I modificatori sul bersaglio si fondono solo sulle armi: passeranno solo gli altri effetti.";
			case INCANTAMENTI_ELEMENTALI_NON_SUL_LIBRO:
				return "Sul libro gli incantamenti elementali non hanno effetto: passeranno solo gli altri effetti.";
			default:
				return "Il potere magico si fonde solo su bastoni e libri magici: passeranno solo gli altri effetti.";
		}
	}
}
