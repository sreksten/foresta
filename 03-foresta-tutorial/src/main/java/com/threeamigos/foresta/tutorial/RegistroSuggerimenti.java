package com.threeamigos.foresta.tutorial;

import com.threeamigos.foresta.interfacce.GestoreSuggerimenti;
import com.threeamigos.foresta.modellodati.SuggerimentiMD;
import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoSuggerimento;

import java.util.Optional;
import java.util.Set;

/**
 * La memoria del tutorial: se è acceso, quali suggerimenti sono già stati visti, e quale va mostrato in una certa {@link Situazione}. Non sa niente
 * della UI e non manda eventi: la UI dice che cosa vede ({@link #situazione}) e quando un suggerimento è stato letto ({@link #visto}); il motore lo
 * chiede qui per pilotare la partita.
 * <p>
 * Il tutorial vale da una partita all'altra, come i trofei e il bestiario, e non fa parte del salvataggio di una partita. Si salva a ogni novità,
 * ma solo se qualcuno ha detto dove ({@link #impostaGestore}); finché il tutorial non è finito il motore non lo dice, quindi non si salva niente e
 * parte sempre acceso e senza suggerimenti visti.
 */
public final class RegistroSuggerimenti {

	private static SuggerimentiMD suggerimentiMD = new SuggerimentiMD();
	private static GestoreSuggerimenti gestore;

	private RegistroSuggerimenti() {
	}

	/**
	 * Imposta dove leggere e salvare il tutorial, e lo rilegge da lì.
	 */
	public static synchronized void impostaGestore(GestoreSuggerimenti gestoreDelTutorial) {
		gestore = gestoreDelTutorial;
		suggerimentiMD = new SuggerimentiMD();
		gestore.carica(suggerimentiMD);
	}

	/**
	 * Se il tutorial è acceso.
	 */
	public static synchronized boolean isAttivo() {
		return suggerimentiMD.isAttivo();
	}

	/**
	 * Accende o spegne il tutorial. Spento non mostra e non pilota niente; i suggerimenti già visti restano visti.
	 */
	public static synchronized void setAttivo(boolean attivo) {
		suggerimentiMD.setAttivo(attivo);
		salva();
	}

	/**
	 * Il primo suggerimento ancora da vedere che scatta in una di queste situazioni, nell'ordine dell'enum, o niente (anche se il tutorial è spento).
	 */
	public static synchronized Optional<TipoSuggerimento> situazione(Set<Situazione> situazioni) {
		if (!suggerimentiMD.isAttivo()) {
			return Optional.empty();
		}
		for (TipoSuggerimento suggerimento : TipoSuggerimento.values()) {
			if (!suggerimentiMD.isVisto(suggerimento) && situazioni.contains(suggerimento.getInnesco())) {
				return Optional.of(suggerimento);
			}
		}
		return Optional.empty();
	}

	/**
	 * Il giocatore ha letto il suggerimento (ha premuto il pollice sull'ultima pagina): non si ripresenta.
	 */
	public static synchronized void visto(TipoSuggerimento suggerimento) {
		if (suggerimentiMD.segnaVisto(suggerimento)) {
			salva();
		}
	}

	public static synchronized boolean isVisto(TipoSuggerimento suggerimento) {
		return suggerimentiMD.isVisto(suggerimento);
	}

	/**
	 * Riaccende il tutorial e fa rivedere i suggerimenti già visti.
	 */
	public static synchronized void ripristina() {
		suggerimentiMD.ripristina();
		salva();
	}

	private static void salva() {
		if (gestore != null) {
			gestore.salva(suggerimentiMD);
		}
	}
}
