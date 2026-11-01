package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.testi.Testi;

import java.util.Collections;
import java.util.List;

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

	private static List<String> pagine(String prefisso) {
		return Collections.unmodifiableList(Testi.serie(FILE, prefisso));
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
}
