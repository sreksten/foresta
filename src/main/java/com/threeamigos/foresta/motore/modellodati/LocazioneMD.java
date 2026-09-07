package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.locazioni.ClassiLocazione;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Il modello dati di una singola casella della Foresta. Ogni casella ha la sua
 * istanza: due locande non sono più la stessa locanda, e possono avere un nome
 * diverso, essere visitate separatamente e ricordarsi di essere state rase al
 * suolo anche dopo un salvataggio.
 * <p>
 * Lo stato durevole vive fra le proprietà, con la convenzione già usata da
 * {@link MissioneMD}: la presenza della chiave vale per vero.
 */
public class LocazioneMD implements Serializzabile {

	/**
	 * Il nome proprio della casella, se ne ha uno
	 */
	public static final String NOME = "NOME";
	/**
	 * Il gruppo è già passato da questa casella
	 */
	public static final String VISITATA = "VISITATA";
	/**
	 * Il gruppo sa cosa c'è in questa casella e la vede sulla mappa
	 */
	public static final String CONOSCIUTA = "CONOSCIUTA";
	/**
	 * Il gruppo ha portato a termine la locazione senza fuggire
	 */
	public static final String COMPLETA = "COMPLETA";

	/**
	 * Valore convenzionale delle proprietà che valgono per la loro sola presenza
	 */
	public static final String AFFERMATIVO = "S";

	private ClassiLocazione classe;
	private final Map<String, String> proprieta = new HashMap<>();

	public LocazioneMD() {
	}

	public LocazioneMD(ClassiLocazione classe) {
		this.classe = classe;
	}

	public ClassiLocazione getClasse() {
		return classe;
	}

	public void setClasse(ClassiLocazione classe) {
		this.classe = classe;
	}

	public void aggiungiProprieta(String nome, String valore) {
		proprieta.put(nome, valore);
	}

	public String ottieniProprieta(String nome) {
		return proprieta.get(nome);
	}

	public void rimuoviProprieta(String nome) {
		proprieta.remove(nome);
	}

	public String getNome() {
		return proprieta.get(NOME);
	}

	public void setNome(String nome) {
		if (nome == null) {
			proprieta.remove(NOME);
		} else {
			proprieta.put(NOME, nome);
		}
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(classe.name());
		if (!proprieta.isEmpty()) {
			stream.print(PIPE);
			stream.print(proprieta.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(PIPE)));
		}
		stream.println();
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		// Il limite -1 conserva gli eventuali campi vuoti in coda
		String[] tokens = line.split("\\|", -1);
		classe = ClassiLocazione.valueOf(tokens[0]);
		proprieta.clear();
		for (int i = 1; i < tokens.length; i++) {
			// Limite 2: il valore può contenere ':' ed essere vuoto
			String[] proprietaCorrente = tokens[i].split(":", 2);
			proprieta.put(proprietaCorrente[0], proprietaCorrente.length > 1 ? proprietaCorrente[1] : "");
		}
	}
}
