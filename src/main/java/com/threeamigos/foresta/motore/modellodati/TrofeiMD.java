package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.tipi.TipoTrofeo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * I trofei vinti e il progresso verso quelli che si conquistano accumulando partita dopo
 * partita (es. le locande visitate). Non fa parte di {@link ModelloDati}: non appartiene a
 * una partita ma passa dall'una all'altra, quindi non viene mai reimpostato e si salva in
 * un file a parte.
 */
public class TrofeiMD implements Serializzabile {

	private final Set<TipoTrofeo> vinti = EnumSet.noneOf(TipoTrofeo.class);
	private final Map<TipoTrofeo, Integer> progressi = new EnumMap<>(TipoTrofeo.class);

	public boolean isVinto(TipoTrofeo trofeo) {
		return vinti.contains(trofeo);
	}

	public void aggiungiVinto(TipoTrofeo trofeo) {
		vinti.add(trofeo);
	}

	public int getProgresso(TipoTrofeo trofeo) {
		return progressi.getOrDefault(trofeo, 0);
	}

	public void incrementaProgresso(TipoTrofeo trofeo, int quantita) {
		progressi.merge(trofeo, quantita, Integer::sum);
	}

	/**
	 * Una riga per trofeo: name() del trofeo, se è stato vinto e il progresso, separati da "|".
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		for (TipoTrofeo trofeo : TipoTrofeo.values()) {
			stream.print(trofeo.name());
			stream.print(PIPE);
			stream.print(isVinto(trofeo));
			stream.print(PIPE);
			stream.println(getProgresso(trofeo));
		}
	}

	/**
	 * Un trofeo che manca dal file non è stato vinto e non ha progressi.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		vinti.clear();
		progressi.clear();
		String riga;
		while ((riga = stream.readLine()) != null) {
			if (riga.isEmpty()) {
				continue;
			}
			LettoreCampi campi = new LettoreCampi(riga);
			TipoTrofeo trofeo = campi.enumerato(TipoTrofeo.class);
			if (campi.booleano()) {
				vinti.add(trofeo);
			}
			progressi.put(trofeo, campi.intero());
		}
	}
}
