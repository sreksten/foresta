package com.threeamigos.foresta;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.threeamigos.foresta.libgdx.ForestaGdx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Il punto d'ingresso desktop (Windows, Linux, macOS) dell'interfaccia libGDX. Android e iOS hanno il loro, fuori da Maven.
 * Su macOS la finestra deve stare sul primo thread del processo (<code>-XstartOnFirstThread</code>): se la JVM non è partita così,
 * la si fa ripartire.
 */
public final class MainLibgdx {

	private static final String RIAVVIATA = "foresta.riavviata";

	private MainLibgdx() {
	}

	public static void main(String[] argomenti) throws IOException, InterruptedException {
		if (serveRiavviareSuMac()) {
			riavviaSuMac(argomenti);
			return;
		}
		Lwjgl3ApplicationConfiguration configurazione = new Lwjgl3ApplicationConfiguration();
		configurazione.setTitle("La Foresta");
		configurazione.setWindowedMode(ForestaGdx.LARGHEZZA_VIRTUALE, ForestaGdx.ALTEZZA_VIRTUALE);
		configurazione.useVsync(true);
		configurazione.setForegroundFPS(60);
		new Lwjgl3Application(new ForestaGdx(), configurazione);
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
