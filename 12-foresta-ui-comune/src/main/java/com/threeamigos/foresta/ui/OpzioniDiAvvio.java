package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.strumenti.ModalitaDiProva;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.tipi.SchermoDiProva;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Le opzioni di avvio dalla riga di comando, uguali per Swing e libGDX (senza distinzione fra maiuscole e minuscole):
 * {@code ORIZZONTALE} (la barra in basso), {@code VERTICALE} (a destra), {@code SINISTRA} (a sinistra), {@code TUTTOSCHERMO}, {@code SALTALOGO}, {@code BARRACLASSICA}, {@code JOYSTICK_VIRTUALE}, {@code TOCCO}
 * (per provare con il mouse i gesti del dito; vale solo in libGDX, dove il dito c'è davvero sui telefoni),
 * {@code LINGUA=<codice>} ({@code it}, {@code en}, {@code en-GB}), {@code ZOOM=<numero>}, {@code MODALITA_DI_PROVA},
 * {@code SCHERMO=<profilo>} o {@code SCHERMO=<larghezza>x<altezza>[@<densità>]} (per provare dal desktop lo schermo di un telefono o di
 * un tablet: i profili sono quelli di {@link SchermoDiProva}; porta con sé {@code VERTICALE}, se non si scrive {@code ORIZZONTALE}, e
 * {@code TOCCO}, perché quegli schermi si usano con il dito). La lingua, lo
 * zoom e la modalità di prova si impostano subito; gli argomenti sconosciuti restano a chi avvia ({@link #getAltri()}), che può
 * averne di suoi.
 */
public final class OpzioniDiAvvio {

	private Orientamento orientamento = Orientamento.ORIZZONTALE;
	// Se l'orientamento è scritto negli argomenti: allora vince su quello che porta con sé SCHERMO= e su quello delle opzioni
	private boolean orientamentoScritto;
	// Con SINISTRA, la barra verticale sta a sinistra
	private boolean barraASinistra;
	// Lo schermo da simulare sul desktop con SCHERMO=, o null
	private MisuraDelloSchermo schermoDiProva;
	private boolean tuttoSchermo;
	// Per le partite di prova: niente logo iniziale, si va dritti all'INTRO
	private boolean saltaLogo;
	// Con LINGUA=<codice> la lingua è decisa e una partita nuova non la chiede
	private boolean linguaDecisa;
	// La barra delle icone che si ingrandisce sotto il cursore, come il Dock di macOS (in entrambi gli orientamenti); con BARRACLASSICA la barra fissa
	private boolean barraDock = true;
	// Il joystick a schermo sulla mappa a tutto schermo, per i dispositivi senza mouse (qui, per provarlo)
	private boolean joystickVirtuale;
	// Mouse trattato come un dito: pressione prolungata per le descrizioni, doppio tocco piu' lento, area sensibile piu' larga
	private boolean tocco;
	private final List<String> altri = new ArrayList<>();

	private OpzioniDiAvvio() {
	}

	/**
	 * Legge gli argomenti; uno {@code ZOOM=} che non è un numero positivo è ignorato con una riga nel log.
	 */
	public static OpzioniDiAvvio leggi(String[] argomenti) {
		OpzioniDiAvvio opzioni = new OpzioniDiAvvio();
		for (String argomento : argomenti) {
			opzioni.leggiUno(argomento);
		}
		if (opzioni.schermoDiProva != null) {
			if (!opzioni.orientamentoScritto) {
				opzioni.orientamento = Orientamento.VERTICALE;
			}
			opzioni.tocco = true;
		}
		return opzioni;
	}

	private void leggiUno(String argomento) {
		String maiuscolo = argomento.toUpperCase(Locale.ROOT);
		if (maiuscolo.equals("ORIZZONTALE")) {
			orientamento = Orientamento.ORIZZONTALE;
			orientamentoScritto = true;
		} else if (maiuscolo.equals("VERTICALE")) {
			orientamento = Orientamento.VERTICALE;
			orientamentoScritto = true;
		} else if (maiuscolo.equals("SINISTRA")) {
			orientamento = Orientamento.VERTICALE;
			barraASinistra = true;
			orientamentoScritto = true;
		} else if (maiuscolo.equals("TUTTOSCHERMO")) {
			tuttoSchermo = true;
		} else if (maiuscolo.equals("SALTALOGO")) {
			saltaLogo = true;
		} else if (maiuscolo.equals("BARRACLASSICA")) {
			barraDock = false;
		} else if (maiuscolo.equals("JOYSTICK_VIRTUALE")) {
			joystickVirtuale = true;
		} else if (maiuscolo.equals("TOCCO")) {
			tocco = true;
		} else if (maiuscolo.startsWith("LINGUA=")) {
			Testi.impostaLingua(argomento.substring("LINGUA=".length()));
			linguaDecisa = true;
		} else if (maiuscolo.startsWith("ZOOM=")) {
			try {
				LivelloDiZoom.imposta(Double.parseDouble(argomento.substring("ZOOM=".length())));
			} catch (IllegalArgumentException e) {
				// Anche NumberFormatException: un valore che non è un numero, o non è positivo, lascia lo zoom predefinito
				Logger.log(() -> "Argomento ignorato, lo zoom resta " + LivelloDiZoom.valore() + ": " + argomento);
			}
		} else if (maiuscolo.startsWith("SCHERMO=")) {
			leggiLoSchermo(maiuscolo.substring("SCHERMO=".length()), argomento);
		} else if (maiuscolo.equals("MODALITA_DI_PROVA")) {
			ModalitaDiProva.setAttiva(true);
		} else {
			altri.add(argomento);
		}
	}

	private void leggiLoSchermo(String valore, String argomento) {
		for (SchermoDiProva profilo : SchermoDiProva.values()) {
			if (profilo.name().equals(valore)) {
				schermoDiProva = profilo.getMisura();
				return;
			}
		}
		try {
			schermoDiProva = MisuraDelloSchermo.leggi(valore);
		} catch (IllegalArgumentException e) {
			// Anche NumberFormatException: si resta sullo schermo del desktop, e nel log c'è l'elenco dei profili
			Logger.log(() -> "Argomento ignorato, non è un profilo né una misura come 2400x1080@2.625: " + argomento);
			for (SchermoDiProva profilo : SchermoDiProva.values()) {
				Logger.log(() -> "  SCHERMO=" + profilo + ": " + profilo.getMisura() + ", " + profilo.getDescrizione());
			}
		}
	}

	/**
	 * Lo schermo da simulare sul desktop ({@code SCHERMO=}), o null per lo schermo di sempre
	 */
	public MisuraDelloSchermo getSchermoDiProva() {
		return schermoDiProva;
	}

	public Orientamento getOrientamento() {
		return orientamento;
	}

	/**
	 * Dove gli argomenti vogliono la barra (ORIZZONTALE, VERTICALE, SINISTRA), o null se non lo dicono: allora vale quella delle opzioni, o la decide il
	 * gioco
	 */
	public PosizioneDellaBarra getPosizioneDellaBarra() {
		if (!orientamentoScritto) {
			return null;
		}
		return orientamento == Orientamento.ORIZZONTALE ? PosizioneDellaBarra.BASSO
				: barraASinistra ? PosizioneDellaBarra.SINISTRA : PosizioneDellaBarra.DESTRA;
	}

	public boolean isTuttoSchermo() {
		return tuttoSchermo;
	}

	public boolean isSaltaLogo() {
		return saltaLogo;
	}

	public boolean isLinguaDecisa() {
		return linguaDecisa;
	}

	public boolean isBarraDock() {
		return barraDock;
	}

	public boolean isJoystickVirtuale() {
		return joystickVirtuale;
	}

	public boolean isTocco() {
		return tocco;
	}

	/**
	 * Gli argomenti che non sono opzioni comuni, nell'ordine
	 */
	public List<String> getAltri() {
		return Collections.unmodifiableList(altri);
	}

	/**
	 * Se fra gli altri argomenti c'è quello (senza distinzione fra maiuscole e minuscole)
	 */
	public boolean haAltro(String argomento) {
		return altri.stream().anyMatch(argomento::equalsIgnoreCase);
	}
}
