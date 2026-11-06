package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoInvioTesto;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.util.function.Consumer;

/**
 * Il campo in cui il giocatore scrive un testo (il nome di un personaggio, il suo nome in classifica, il nome di una voce delle
 * tattiche), senza toolkit: chi aspetta la risposta, l'invio (al motore come {@link ComandoInvioTesto}, o a chi l'ha chiesto per
 * sé), la rinuncia con Esc e i caratteri ammessi. Il campo vero, dove si scrive, è del toolkit ({@link Campo}): un
 * <code>JTextField</code> in Swing, la tastiera di sistema su Android e iOS.
 */
public final class CampoDiTesto {

	/**
	 * Il campo del toolkit: si mostra con un testo già scritto, si nasconde, dice che cosa c'è scritto. Quando il giocatore preme
	 * invio chiama {@link CampoDiTesto#invio()}, quando preme Esc {@link CampoDiTesto#esc()}.
	 */
	public interface Campo {

		/** Mostra il campo con quel testo, il cursore in fondo, pronto a scrivere */
		void mostra(String testo);

		void nascondi();

		/** Il testo scritto, così com'è */
		String getTestoScritto();
	}

	// I due font del gioco hanno gli stessi caratteri
	private static final DoomdarkFont FONT_DEI_TESTI = DoomdarkFont.piccolo();

	private Campo campo;
	private boolean visibile;
	// Chi ha chiesto il testo per sé (la UI, senza passare dal motore): se c'è, l'invio lo riceve lui invece del bus
	private Consumer<String> alInvio;

	/**
	 * Il campo del toolkit, che di solito si costruisce insieme a questo (vedi il <code>Prompt</code> di Swing).
	 */
	public void setCampo(Campo campo) {
		this.campo = campo;
	}

	public boolean isVisibile() {
		return visibile;
	}

	/**
	 * Mostra il campo vuoto: il testo va al motore.
	 */
	public void mostraVuoto() {
		mostra("");
	}

	/**
	 * Mostra il campo già compilato con un testo, con il cursore in fondo: il testo va al motore.
	 */
	public void mostra(String testoPredefinito) {
		alInvio = null;
		visibile = true;
		campo.mostra(testoPredefinito);
	}

	/**
	 * Come {@link #mostra(String)}, ma l'invio non va al motore: il testo lo riceve <code>alInvio</code> (con Esc non lo riceve
	 * nessuno).
	 */
	public void mostra(String testoPredefinito, Consumer<String> alInvio) {
		mostra(testoPredefinito);
		this.alInvio = alInvio;
	}

	public void nascondi() {
		alInvio = null;
		visibile = false;
		campo.nascondi();
	}

	/**
	 * Chiude il campo se sta ancora aspettando proprio quella risposta (chi l'aveva chiesta non la vuole più): se nel frattempo ne
	 * aspetta un'altra, o è già chiuso, non fa niente.
	 */
	public void annulla(Consumer<String> alInvioDaAnnullare) {
		if (visibile && alInvio != null && alInvio == alInvioDaAnnullare) {
			nascondi();
		}
	}

	/**
	 * Il testo scritto, senza i caratteri che il font non sa disegnare e senza quelli che rompono i salvataggi.
	 */
	public String getTesto() {
		return soloCaratteriDelFont(Serializzabile.pulisciCampo(campo.getTestoScritto()));
	}

	/**
	 * Il giocatore ha premuto invio: il campo si chiude e il testo va a chi l'aveva chiesto per sé, o al motore.
	 */
	public void invio() {
		Consumer<String> richiedente = alInvio;
		String testo = getTesto();
		nascondi();
		if (richiedente != null) {
			richiedente.accept(testo);
			return;
		}
		// Il testo incollato può ancora contenere un "|"
		BusEventi.pubblica(new ComandoInvioTesto(testo));
	}

	/**
	 * Il giocatore ha premuto Esc: rinuncia, ma solo per un testo chiesto dalla UI (gli altri il motore li aspetta).
	 */
	public void esc() {
		if (alInvio != null) {
			nascondi();
		}
	}

	/**
	 * Se il carattere si può scrivere: i caratteri di controllo sì (cancellano, spostano), gli altri solo se il font del gioco sa
	 * disegnarli (il "|" separa i campi dei salvataggi, e il font non ha tutti i caratteri).
	 */
	public static boolean ammesso(char c) {
		return Character.isISOControl(c) || FONT_DEI_TESTI.supportaIlCarattere(c);
	}

	/**
	 * Toglie i caratteri che il font del gioco non sa disegnare (una "ü", un "_"...): un nome così non si leggerebbe, e il riquadro
	 * del gruppo non saprebbe scriverlo.
	 */
	public static String soloCaratteriDelFont(String testo) {
		if (testo == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(testo.length());
		for (char c : testo.toCharArray()) {
			if (FONT_DEI_TESTI.supportaIlCarattere(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
