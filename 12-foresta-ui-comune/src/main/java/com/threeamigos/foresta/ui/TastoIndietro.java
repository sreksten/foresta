package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.ui.barra.ComandiPossibili;

import java.util.Collection;

/**
 * Il tasto «indietro» (Android) o il gesto di chiusura (iOS), uguale per ogni toolkit: dà al gioco il comando che nello stato corrente fa tornare
 * indietro, fra quelli che il giocatore può dare ({@link ComandiPossibili}), come se toccasse l'icona. In ordine:
 * <ol>
 * <li>{@code ANNULLA}: chiude la finestra in primo piano (inventario, tattiche, trofei, bestiario…) e rinuncia a una scelta;</li>
 * <li>{@code NO}: nella scelta dello slot di salvataggio e nella conferma di uscita riporta al gioco;</li>
 * <li>{@code FLOPPY_SALVA}: dalla schermata di gioco apre il salvataggio, a cui segue la proposta di uscire (conferma compresa): è l'uscita dal
 * gioco, e non si esce mai senza che il giocatore l'abbia confermato.</li>
 * </ol>
 * Se nessuno di questi è possibile (per esempio nell'intro, o mentre il gioco è occupato) non fa niente e dice di non aver gestito il tasto: la
 * piattaforma farà il suo (di norma mandare l'app in background).
 */
public final class TastoIndietro {

	private TastoIndietro() {
	}

	/**
	 * Il comando da dare per tornare indietro fra quelli possibili, o null
	 */
	public static Comando comando(Collection<Comando> possibili) {
		if (possibili.contains(Comando.ANNULLA)) {
			return Comando.ANNULLA;
		}
		if (possibili.contains(Comando.NO)) {
			return Comando.NO;
		}
		if (possibili.contains(Comando.FLOPPY_SALVA)) {
			return Comando.FLOPPY_SALVA;
		}
		return null;
	}

	/**
	 * Il giocatore ha premuto indietro: dà il comando, se c'è e non è in ricarica.
	 *
	 * @return vero se il tasto è stato gestito
	 */
	public static boolean premuto() {
		Comando comando = comando(ComandiPossibili.getComandi());
		if (comando == null || ComandiPossibili.getRicarica(comando) != null) {
			return false;
		}
		BusEventi.pubblica(new ComandoDiGioco(comando));
		return true;
	}
}
