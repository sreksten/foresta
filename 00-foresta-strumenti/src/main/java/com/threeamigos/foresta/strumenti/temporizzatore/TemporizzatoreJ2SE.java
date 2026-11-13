package com.threeamigos.foresta.strumenti.temporizzatore;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.concurrent.atomic.AtomicInteger;
import com.threeamigos.foresta.strumenti.logger.Logger;

/**
 * Il temporizzatore basato sull'esecutore pianificato di Java: gli impulsi si pianificano su un thread a parte, ma ognuno viene eseguito sul thread principale
 * (vedi {@link ThreadPrincipale}). Si può sospendere e riprendere con il ciclo di vita dell'app ({@link #sospendi()}). Ogni impulso ricorda la pianificazione da cui viene: se nel frattempo è cambiata, l'impulso già accodato non fa niente, così
 * nessun impulso arriva dopo un cambio di stato.
 */
public class TemporizzatoreJ2SE implements Temporizzatore {

	// Il thread che conta il tempo: uno solo, "daemon" perché non deve tenere acceso il programma quando tutto il resto è
	// finito. Non esegue lui gli impulsi: li passa al thread principale (vedi pianifica)
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	});

	private ScheduledFuture<?> timerTask;
	// Cambia a ogni inizia/iniziaDopo/termina. cancel() ferma i prossimi impulsi ma non quelli già accodati
	// sul thread principale con accoda: ognuno ricorda la pianificazione da cui viene e, se non è più quella corrente,
	// non fa nulla. Così nessun TIMER arriva all'automa dopo un cambio di stato.
	private final AtomicInteger pianificazione = new AtomicInteger();
	// volatile: si imposta con il lucchetto ma si legge sul thread principale, a ogni impulso, senza lucchetto
	private volatile Temporizzabile temporizzabile;
	// Gli impulsi falliti di fila (sul thread principale): una riuscita li azzera
	private static final int MAX_IMPULSI_FALLITI_DI_FILA = 3;
	private int impulsiFalliti;

	// Il ritmo dell'ultimo inizia/iniziaDopo, per riprenderlo dopo una sospensione; 0 se non ci sono impulsi da fare
	private int periodoAttivo;
	private boolean sospeso;
	// A chi si dice che un impulso è fallito: il messaggio (null per un semplice fallimento) e l'eccezione
	private final BiConsumer<String, RuntimeException> segnalaErrore;

	/**
	 * Un temporizzatore che scrive nel registro gli impulsi falliti.
	 */
	public TemporizzatoreJ2SE() {
		this(TemporizzatoreJ2SE::scriviNelRegistro);
	}

	/**
	 * Un temporizzatore che dice a {@code segnalaErrore} quando un impulso fallisce, con un messaggio (null se è un semplice fallimento)
	 * e l'eccezione.
	 */
	public TemporizzatoreJ2SE(BiConsumer<String, RuntimeException> segnalaErrore) {
		this.segnalaErrore = segnalaErrore;
	}

	// Scrive il messaggio (se c'è) e poi l'eccezione con la sua traccia
	private static void scriviNelRegistro(String messaggio, RuntimeException errore) {
		if (messaggio != null) {
			Logger.log(messaggio);
		}
		Logger.log(errore);
	}

	public synchronized void setTemporizzabile(Temporizzabile temporizzabile) {
		this.temporizzabile = temporizzabile;
	}

	/**
	 * Avvia gli impulsi: il primo subito, poi uno ogni quei millisecondi.
	 */
	public synchronized void inizia(int millisecondi) {
		pianifica(0, millisecondi);
	}

	/**
	 * Avvia gli impulsi: il primo dopo quei millisecondi, poi uno ogni quei millisecondi.
	 */
	public synchronized void iniziaDopo(int millisecondi) {
		pianifica(millisecondi, millisecondi);
	}

	private void pianifica(int ritardoIniziale, int millisecondi) {
		fermaLaPianificazioneInCorso();
		impulsiFalliti = 0;
		periodoAttivo = millisecondi;
		if (sospeso) {
			// Il ritmo è ricordato: gli impulsi partono alla ripresa
			return;
		}
		int questaPianificazione = pianificazione.get();
		// scheduleWithFixedDelay: il prossimo impulso parte quei millisecondi dopo la fine del precedente (non a orario
		// fisso), così se il thread principale è lento gli impulsi non si accumulano
		timerTask = executor.scheduleWithFixedDelay(
			// Il tick va eseguito sul thread principale: processaComando() pubblica eventi sul bus, che
			// se chiamato da un thread diverso li accoda. Questo apre una
			// finestra in cui lo stato di gioco è già cambiato (es. un personaggio è morto) ma la
			// UI non l'ha ancora saputo, causando artefatti visivi (es. flicker alla morte di un mostro).
			() -> ThreadPrincipale.accoda(() -> {
				if (pianificazione.get() != questaPianificazione) {
					return;
				}
				try {
					temporizzabile.tick();
					impulsiFalliti = 0;
				} catch (RuntimeException e) {
					segnalaErrore.accept(null, e);
					// Un impulso che fallisce ogni secondo riempirebbe il log e infastidirebbe il giocatore con lo stesso errore: dopo qualche
					// fallimento di fila gli impulsi si fermano (i comandi del giocatore continuano ad arrivare)
					if (++impulsiFalliti >= MAX_IMPULSI_FALLITI_DI_FILA) {
						segnalaErrore.accept("Impulsi fermati dopo " + impulsiFalliti + " errori di fila", e);
						termina();
					}
				}
			}),
                ritardoIniziale,
                millisecondi,
			TimeUnit.MILLISECONDS
		);
	}

	// Cancella i prossimi impulsi e rende non più validi quelli già accodati
	private void fermaLaPianificazioneInCorso() {
		pianificazione.incrementAndGet();
		if (timerTask != null) {
			timerTask.cancel(false);
			timerTask = null;
		}
	}

	/**
	 * Ferma gli impulsi, anche quelli già accodati.
	 */
	public synchronized void termina() {
		fermaLaPianificazioneInCorso();
		periodoAttivo = 0;
	}

	/**
	 * Ferma gli impulsi, anche quelli già accodati, ricordando il ritmo per {@link #riprendi()}.
	 */
	public synchronized void sospendi() {
		sospeso = true;
		fermaLaPianificazioneInCorso();
	}

	/**
	 * Rimette in marcia gli impulsi sospesi, con lo stesso ritmo e il primo dopo un periodo intero.
	 */
	public synchronized void riprendi() {
		if (!sospeso) {
			return;
		}
		sospeso = false;
		if (periodoAttivo > 0) {
			pianifica(periodoAttivo, periodoAttivo);
		}
	}
}
