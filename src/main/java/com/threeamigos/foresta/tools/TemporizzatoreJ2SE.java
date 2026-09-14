package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoException;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.ControlloreDiGioco;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TemporizzatoreJ2SE implements Temporizzatore {

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	});

	private ScheduledFuture<?> timerTask;
	private ControlloreDiGioco controlloreDiGioco;

	public void setConsumatore(ControlloreDiGioco c) {
		this.controlloreDiGioco = c;
	}

	public void inizia(int secondi) {
		if (timerTask != null) {
			timerTask.cancel(false);
		}
		timerTask = executor.scheduleWithFixedDelay(
			() -> {
				try {
					controlloreDiGioco.processaAzione(Comando.TIMER);
				} catch (RuntimeException e) {
					BusEventi.pubblica(new EventoException(e));
				}
			},
                secondi,
                secondi,
			TimeUnit.SECONDS
		);
	}

	public void termina() {
		if (timerTask != null) {
			timerTask.cancel(false);
		}
	}
}
