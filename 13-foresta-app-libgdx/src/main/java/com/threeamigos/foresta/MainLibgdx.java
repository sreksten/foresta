package com.threeamigos.foresta;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.threeamigos.foresta.libgdx.EsecutoreGdx;
import com.threeamigos.foresta.libgdx.ForestaGdx;
import com.threeamigos.foresta.libgdx.ForestaLibGDX;
import com.threeamigos.foresta.motore.AvvioDelMotore;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.OpzioniDiAvvio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Il punto d'ingresso desktop (Windows, Linux, macOS) dell'interfaccia libGDX: come <code>Main</code> per Swing, l'unico posto che
 * conosce insieme motore e interfaccia e li collega. Il motore lo collega {@link AvvioDelMotore} (lo stesso di Swing), con il thread
 * di rendering di libGDX come thread principale. Android e iOS hanno il loro punto d'ingresso, fuori da Maven.
 * <p>
 * Su macOS la finestra deve stare sul primo thread del processo (<code>-XstartOnFirstThread</code>): se la JVM non è partita così,
 * la si fa ripartire.
 * <p>
 * Gli argomenti sono quelli di {@link OpzioniDiAvvio} (l'orientamento, il tutto schermo e il logo non contano ancora), più
 * {@code PAGINE_DI_PROVA} per le pagine di prova dei pezzi dell'interfaccia (<code>ForestaGdx</code>) al posto del gioco.
 */
public final class MainLibgdx {

	private static final String RIAVVIATA = "foresta.riavviata";
	private static final String PAGINE_DI_PROVA = "PAGINE_DI_PROVA";

	private MainLibgdx() {
	}

	public static void main(String[] argomenti) throws IOException, InterruptedException {
		if (serveRiavviareSuMac()) {
			riavviaSuMac(argomenti);
			return;
		}
		OpzioniDiAvvio opzioni = OpzioniDiAvvio.leggi(argomenti);
		for (String sconosciuto : opzioni.getAltri()) {
			if (!sconosciuto.equalsIgnoreCase(PAGINE_DI_PROVA)) {
				Logger.log(() -> "Argomento sconosciuto ignorato: " + sconosciuto);
			}
		}
		ApplicationListener applicazione;
		int larghezza;
		int altezza;
		if (opzioni.haAltro(PAGINE_DI_PROVA)) {
			applicazione = new ForestaGdx();
			larghezza = ForestaGdx.LARGHEZZA_VIRTUALE;
			altezza = ForestaGdx.ALTEZZA_VIRTUALE;
		} else {
			EsecutoreGdx esecutore = new EsecutoreGdx();
			// Il motore chiede di uscire con NotificaUscitaDalGioco: qui si chiude l'applicazione, e il processo finisce con lei (i
			// thread del motore sono demoni)
			AvvioDelMotore motore = new AvvioDelMotore(esecutore, e -> Gdx.app.exit(), opzioni.isLinguaDecisa());
			applicazione = new ForestaLibGDX(esecutore, motore.getControlloreDiGioco(), motore.getGestorePunteggi(), motore.getVistaPartita(),
					AvvioDelMotore.nuovoTemporizzatore(), opzioni.isBarraDock(), opzioni.isJoystickVirtuale());
			Misura dimensioni = ForestaLibGDX.dimensioniDelloSchermo();
			larghezza = dimensioni.larghezza;
			altezza = dimensioni.altezza;
		}

		Lwjgl3ApplicationConfiguration configurazione = new Lwjgl3ApplicationConfiguration();
		configurazione.setTitle("La Foresta");
		configurazione.setWindowedMode(larghezza, altezza);
		configurazione.useVsync(true);
		configurazione.setForegroundFPS(60);
		new Lwjgl3Application(applicazione, configurazione);
	}

	private static boolean serveRiavviareSuMac() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("mac") && System.getProperty(RIAVVIATA) == null;
	}

	private static void riavviaSuMac(String[] argomenti) throws IOException, InterruptedException {
		List<String> comando = new ArrayList<>();
		comando.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
		comando.add("-XstartOnFirstThread");
		comando.add("-D" + RIAVVIATA + "=true");
		// Le proprietà della Foresta (foresta.*) passano alla nuova JVM
		for (String nome : System.getProperties().stringPropertyNames()) {
			if (nome.startsWith("foresta.") && !nome.equals(RIAVVIATA)) {
				comando.add("-D" + nome + "=" + System.getProperty(nome));
			}
		}
		comando.add("-cp");
		comando.add(System.getProperty("java.class.path"));
		comando.add(MainLibgdx.class.getName());
		for (String argomento : argomenti) {
			comando.add(argomento);
		}
		new ProcessBuilder(comando).inheritIO().start().waitFor();
	}
}
