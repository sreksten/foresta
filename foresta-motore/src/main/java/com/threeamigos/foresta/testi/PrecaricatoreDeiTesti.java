package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.eventi.notifiche.NotificaUscitaDalGioco;
import com.threeamigos.foresta.strumenti.logger.Logger;

/**
 * Carica all'avvio le grammatiche di tutti i produttori di testi del gioco (quelli di {@link ProduttoriDiTesti} e quello degli
 * artefatti). Se una manca o non è valida il gioco non può andare avanti: i produttori sollevano una
 * {@link GrammaticaNonValidaException}, che qui si segnala sul bus ({@link InternoException}) e si traduce in una richiesta di
 * uscita alla piattaforma ({@link NotificaUscitaDalGioco}).
 */
public final class PrecaricatoreDeiTesti {

	private PrecaricatoreDeiTesti() {
	}

	/**
	 * Carica subito tutte le grammatiche (vedi Automa, stato LOGO_INIZIALE).
	 */
	public static void precarica() {
		precarica(() -> {
			ProduttoriDiTesti.precarica();
			ProduttoreDiArtefattiCasuale.precarica();
		});
	}

	/**
	 * Esegue il caricamento e, se solleva una {@link GrammaticaNonValidaException}, la segnala. Il bus consegna gli eventi nell'ordine in
	 * cui sono pubblicati, quindi chi ascolta le {@code InternoException} la riceve prima dell'uscita.
	 */
	static void precarica(Runnable caricamento) {
		try {
			caricamento.run();
		} catch (GrammaticaNonValidaException e) {
			Logger.log(e);
			BusEventi.pubblica(new InternoException(e.getMessage(), e));
			BusEventi.pubblica(new NotificaUscitaDalGioco(1, e.getMessage()));
		}
	}
}
