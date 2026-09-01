package com.threeamigos.foresta.tools;

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

	public void setControlloreDiGioco(ControlloreDiGioco c) {
		this.controlloreDiGioco = c;
		c.setTemporizzatore(this);
	}

	public void inizia(int secondi) {
		if (timerTask != null) {
			timerTask.cancel(false);
		}
		timerTask = executor.scheduleWithFixedDelay(
			() -> controlloreDiGioco.processaAzione(Comando.TIMER),
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
