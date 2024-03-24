package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.ControlloreDiGioco;

public class TemporizzatoreJ2SE implements Temporizzatore {

	private class MyThread extends Thread {
		private boolean termina;
		private int secondi;

		public MyThread(int secondi) {
			this.secondi = secondi;
			termina = false;
			setDaemon(true);
			start();
		}

		public void interrompi() {
			termina = true;
		}

		@Override
		public void run() {
			try {
				while (!termina) {
					sleep((long) secondi * 1000);
					if (!termina) {
						controlloreDiGioco.processaAzione(Comando.TIMER);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private ControlloreDiGioco controlloreDiGioco;
	private MyThread myThread;

	public void setControlloreDiGioco(ControlloreDiGioco c) {
		this.controlloreDiGioco = c;
		c.setTemporizzatore(this);
	}

	public void inizia(int secondi) {
		if (myThread != null) {
			myThread.interrompi();
		}
		myThread = new MyThread(secondi);
	}

	public void termina() {
		if (myThread != null) {
			myThread.interrompi();
		}
	}
}
