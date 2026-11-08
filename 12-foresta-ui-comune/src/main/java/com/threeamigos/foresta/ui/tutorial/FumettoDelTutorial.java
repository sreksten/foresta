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
	private final long apertoAlleNs = System.nanoTime();
	private int pagina = 1;
	// I valori dei segnaposto {0}, {1}... del testo, per i messaggi che il motore chiede di dire (vuoto per un suggerimento che scatta da solo)
	private final java.util.List<String> parametri;

	FumettoDelTutorial(TipoSuggerimento suggerimento, boolean dito) {
		this(suggerimento, dito, 1);
	}

	/**
	 * Un messaggio che il motore ha chiesto di dire: una sola pagina, il cui testo ha dei segnaposto.
	 */
	FumettoDelTutorial(TipoSuggerimento suggerimento, boolean dito, java.util.List<String> parametri) {
		this.suggerimento = suggerimento;
		this.dito = dito;
		this.parametri = parametri;
	}

	/**
	 * @param pagina da quale pagina si riparte (da 1): una già letta prima di lasciare la schermata non si rilegge
	 */
	FumettoDelTutorial(TipoSuggerimento suggerimento, boolean dito, int pagina) {
		this.suggerimento = suggerimento;
		this.dito = dito;
		this.parametri = java.util.Collections.emptyList();
		this.pagina = Math.max(1, Math.min(pagina, Math.max(1, suggerimento.getNumeroPagine())));
	}

	public TipoSuggerimento getSuggerimento() {
		return suggerimento;
	}

	/**
	 * A che cosa punta la coda nella pagina in cui siamo (la pagina sulla mappa punta alla mappa, quella sul movimento alle frecce).
	 */
	public BersaglioDelSuggerimento getBersaglio() {
		return suggerimento.getBersaglio(pagina);
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
		String testo = suggerimento.getPagina(pagina, dito);
		for (int i = 0; i < parametri.size(); i++) {
			testo = testo.replace("{" + i + "}", parametri.get(i));
		}
		return testo;
	}

	/**
	 * Il titolo della pagina in cui siamo, se ne ha uno (sta in cima, accanto all'icona).
	 */
	public java.util.Optional<String> getTitolo() {
		return suggerimento.getTitolo(pagina);
	}

	/**
	 * Le risorse delle immagini dell'intestazione della pagina in cui siamo, una accanto all'altra (vuoto se non ne ha): la tessera che un luogo ha sulla
	 * mappa, oppure una delle quattro frecce blu degli elenchi che si scorrono.
	 */
	public java.util.List<String> getRisorseDelleIcone() {
		java.util.Optional<String> nomi = suggerimento.getIcona(pagina);
		if (!nomi.isPresent()) {
			return java.util.Collections.emptyList();
		}
		java.util.List<String> risorse = new java.util.ArrayList<>();
		for (String nome : nomi.get().split(",")) {
			String risorsa = risorsaDellIcona(nome.trim());
			if (risorsa != null) {
				risorse.add(risorsa);
			}
		}
		return risorse;
	}

	/**
	 * Il nome di un'icona nei testi: una costante di TipoLocazione (la sua tessera) o FRECCIA_SU, FRECCIA_GIU, FRECCIA_SINISTRA, FRECCIA_DESTRA.
	 */
	static String risorsaDellIcona(String nome) {
		switch (nome) {
			case "FRECCIA_SU":
				return com.threeamigos.foresta.ui.immagini.NomiDelleImmagini.FRECCIA_SU;
			case "FRECCIA_GIU":
				return com.threeamigos.foresta.ui.immagini.NomiDelleImmagini.FRECCIA_GIU;
			case "FRECCIA_SINISTRA":
				return com.threeamigos.foresta.ui.immagini.NomiDelleImmagini.FRECCIA_SINISTRA;
			case "FRECCIA_DESTRA":
				return com.threeamigos.foresta.ui.immagini.NomiDelleImmagini.FRECCIA_DESTRA;
			default:
				return com.threeamigos.foresta.ui.immagini.NomiDelleImmagini.tessera(com.threeamigos.foresta.tipi.TipoLocazione.valueOf(nome));
		}
	}

	/**
	 * Da quanto tempo è aperto, in millisecondi: il bordo del bersaglio lampeggia a questo ritmo.
	 */
	public long getMillisDallApertura() {
		return (System.nanoTime() - apertoAlleNs) / 1_000_000L;
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
