package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.BersaglioDelSuggerimento;
import com.threeamigos.foresta.tipi.TipoSuggerimento;

/**
 * Un suggerimento aperto sullo schermo: quale è, a che pagina siamo e che cosa c'è scritto. Non disegna: chi disegna (Swing, libGDX) legge il
 * testo della pagina, il bersaglio a cui punta la coda e se c'è un'altra pagina dopo (il pollice dice «avanti» o «ho capito»).
 *
 * @author Stefano Reksten
 */
public final class FumettoDelTutorial {

	private final TipoSuggerimento suggerimento;
	private final boolean dito;
	private int pagina = 1;

	FumettoDelTutorial(TipoSuggerimento suggerimento, boolean dito) {
		this.suggerimento = suggerimento;
		this.dito = dito;
	}

	public TipoSuggerimento getSuggerimento() {
		return suggerimento;
	}

	public BersaglioDelSuggerimento getBersaglio() {
		return suggerimento.getBersaglio();
	}

	/**
	 * La pagina in cui siamo, da 1.
	 */
	public int getPagina() {
		return pagina;
	}

	public int getNumeroPagine() {
		return suggerimento.getNumeroPagine();
	}

	public boolean isUltimaPagina() {
		return pagina >= getNumeroPagine();
	}

	/**
	 * Il testo della pagina in cui siamo, nella lingua in uso e nella variante per il dito se il gioco si sta giocando con il dito.
	 */
	public String getTesto() {
		return suggerimento.getPagina(pagina, dito);
	}

	/**
	 * Passa alla pagina successiva.
	 *
	 * @return false se era l'ultima (e non si va avanti)
	 */
	boolean prossimaPagina() {
		if (isUltimaPagina()) {
			return false;
		}
		pagina++;
		return true;
	}
}
