package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;

import java.util.Optional;
import java.util.Set;

/**
 * Chi decide, a ogni cambiamento della UI, se mostrare un suggerimento del tutorial: la UI gli dice che cosa vede ({@link #aggiorna}), lui
 * chiede al {@link RegistroSuggerimenti} se c'è qualcosa da spiegare e tiene aperto il fumetto finché il giocatore non preme il pollice
 * ({@link #pollice}) o lo spegne ({@link #annulla}). Mai due fumetti insieme: gli altri aspettano, uno dopo l'altro, in un ordine fisso.
 * <p>
 * Non disegna e non pubblica eventi. Chi disegna legge {@link #getFumetto} e {@link #isBordoAcceso}; lo schermo conta il fumetto aperto fra le
 * cose che tengono occupata la UI (finché è aperto il motore aspetta: vedi {@code SchermoDelGioco.fineFotogramma}).
 *
 * @author Stefano Reksten
 */
public final class TutorDelGioco {

	/** Il bordo del bersaglio lampeggia: acceso per questo tempo, poi spento per altrettanto */
	static final long MEZZO_PERIODO_DEL_LAMPEGGIO_MS = 400L;

	private FumettoDelTutorial fumetto;
	private boolean dito;
	private boolean abilitato;

	/**
	 * Il toolkit dice che sa disegnare il fumetto e ascoltare il pollice e l'annulla: finché non lo dice non si apre niente (un fumetto che non
	 * si può chiudere terrebbe ferma la UI per sempre).
	 */
	public synchronized void setAbilitato(boolean abilitato) {
		this.abilitato = abilitato;
		if (!abilitato) {
			fumetto = null;
		}
	}

	/**
	 * Se si gioca con il dito: i testi cambiano (trascinare invece del mouse, tenere premuto per la descrizione).
	 */
	public synchronized void setDito(boolean dito) {
		this.dito = dito;
	}

	public synchronized boolean isDito() {
		return dito;
	}

	/**
	 * Il fumetto aperto, o niente.
	 */
	public synchronized Optional<FumettoDelTutorial> getFumetto() {
		return Optional.ofNullable(fumetto);
	}

	public synchronized boolean isAperto() {
		return fumetto != null;
	}

	/**
	 * La UI ha cambiato qualcosa: se non c'è già un fumetto aperto e il momento lo consente, apre il primo suggerimento ancora da vedere che scatta
	 * in una di queste situazioni.
	 *
	 * @param situazioni che cosa vede la UI adesso
	 * @param consentito (con il tutorial non abilitato non si apre mai niente) falso durante un intermezzo o nel mezzo di un'azione che chiede attenzione: allora non si apre niente
	 * @return il fumetto appena aperto, se ce n'è uno
	 */
	public synchronized Optional<FumettoDelTutorial> aggiorna(Set<Situazione> situazioni, boolean consentito) {
		if (!abilitato || fumetto != null || !consentito) {
			return Optional.empty();
		}
		Optional<TipoSuggerimento> suggerimento = RegistroSuggerimenti.situazione(situazioni);
		if (suggerimento.isPresent() && suggerimento.get().getNumeroPagine() > 0) {
			fumetto = new FumettoDelTutorial(suggerimento.get(), dito);
			return Optional.of(fumetto);
		}
		return Optional.empty();
	}

	/**
	 * Il giocatore ha premuto il pollice: avanti di una pagina, e sull'ultima «ho capito». Il suggerimento è visto e non si ripresenta.
	 *
	 * @return vero se il fumetto si è chiuso
	 */
	public synchronized boolean pollice() {
		if (fumetto == null) {
			return false;
		}
		if (fumetto.prossimaPagina()) {
			return false;
		}
		RegistroSuggerimenti.visto(fumetto.getSuggerimento());
		fumetto = null;
		return true;
	}

	/**
	 * Il giocatore ha premuto l'annulla: il tutorial si spegne, tutto, e il fumetto si chiude. I suggerimenti già visti restano visti.
	 */
	public synchronized void annulla() {
		RegistroSuggerimenti.setAttivo(false);
		fumetto = null;
	}

	/**
	 * Se in questo istante il bordo del bersaglio è acceso (lampeggia mentre il fumetto è aperto, e parte acceso).
	 */
	public synchronized boolean isBordoAcceso(long millisecondiDallApertura) {
		return fumetto != null && (Math.max(0L, millisecondiDallApertura) / MEZZO_PERIODO_DEL_LAMPEGGIO_MS) % 2 == 0;
	}

	/**
	 * Chiude il fumetto senza segnare nulla (si cambia partita, o la UI si reinizializza): il suggerimento si riproporrà.
	 */
	public synchronized void chiudi() {
		fumetto = null;
	}
}
