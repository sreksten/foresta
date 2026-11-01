package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.testi.Testi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * I pezzi della storia che non dipendono dalla partita: l'introduzione e i finali. Sono contenuto del gioco, non della UI: la UI li
 * riceve dal motore attraverso VistaPartita, così ogni piattaforma racconta la stessa storia. I testi stanno in
 * {@code storia_<lingua>.properties}, nella lingua scelta con {@link Testi#impostaLingua(String)}.
 *
 * @author Stefano Reksten
 */
public final class Racconto {

	private static final String FILE = "storia";

	private Racconto() {
	}

	/**
	 * L'introduzione, un paragrafo per elemento.
	 */
	public static List<String> getStoria() {
		return pagine("INTRODUZIONE");
	}

	/**
	 * Il finale della partita persa, una pagina per elemento.
	 */
	public static List<String> getFinaleDellaSconfitta() {
		return pagine("FINALE_SCONFITTA");
	}

	/**
	 * Il finale della partita vinta, una pagina per elemento.
	 */
	public static List<String> getFinaleDellaVittoria() {
		return pagine("FINALE_VITTORIA");
	}

	// Le voci PREFISSO.1, PREFISSO.2... finché ce ne sono
	private static List<String> pagine(String prefisso) {
		List<String> pagine = new ArrayList<>();
		for (int i = 1;; i++) {
			Optional<String> pagina = Testi.get(FILE, prefisso + "." + i);
			if (!pagina.isPresent()) {
				return Collections.unmodifiableList(pagine);
			}
			pagine.add(pagina.get());
		}
	}
}
