package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.MappaProprieta;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.mappa.TipoLocazione;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * Il modello dati di una singola casella della Foresta. Ogni casella ha la sua
 * istanza: due locande non sono più la stessa locanda, e possono avere un nome
 * diverso, essere visitate separatamente e ricordarsi di essere state rase al
 * suolo anche dopo un salvataggio.
 * <p>
 * Lo stato durevole vive fra le proprietà, con la convenzione già usata da
 * {@code MissioneMD}: la presenza della chiave vale per vero.
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
	 * Il gruppo è arrivato in questa casella almeno una volta (anche se ne è fuggito): serve a pubblicare la prima visita
	 */
	public static final String ARRIVATO = "ARRIVATO";
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

	/**
	 * Un identificativo unico per la locazione
	 */
	private String uuid = UUID.randomUUID().toString();

	private TipoLocazione tipo;
	private final Map<String, String> proprieta = new HashMap<>();

	/**
	 * Una locazione senza tipo.
	 */
	public LocazioneMD() {
	}

	/**
	 * Una locazione di quel tipo.
	 */
	public LocazioneMD(TipoLocazione tipo) {
		this.tipo = tipo;
	}

	/**
	 * L'identificativo unico di questa casella, assegnato alla creazione e conservato nei salvataggi.
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Il tipo della locazione.
	 */
	public TipoLocazione getTipo() {
		return tipo;
	}

	/**
	 * Cambia il tipo della locazione.
	 */
	public void setTipo(TipoLocazione tipo) {
		this.tipo = tipo;
	}

	/**
	 * Imposta una proprietà (o ne sostituisce il valore). Il carattere "|" si toglie dal valore, perché separa i campi
	 * nel salvataggio, e un valore null si rifiuta. Per le proprietà che valgono per la sola presenza si usa {@link #AFFERMATIVO}.
	 */
	public void aggiungiProprieta(String nome, String valore) {
		proprieta.put(nome, MappaProprieta.pulisciValore(nome, valore));
	}

	/**
	 * Il valore di una proprietà della casella.
	 *
	 * @return il valore della proprietà, o null se la casella non ce l'ha
	 */
	public String ottieniProprieta(String nome) {
		return proprieta.get(nome);
	}

	/**
	 * Toglie la proprietà, se c'è.
	 */
	public void rimuoviProprieta(String nome) {
		proprieta.remove(nome);
	}

	/**
	 * Il nome proprio della casella, o null se non ne ha.
	 */
	public String getNome() {
		return proprieta.get(NOME);
	}

	/**
	 * Imposta il nome proprio della casella; con null lo toglie. Il carattere "|" si toglie dal nome.
	 */
	public void setNome(String nome) {
		if (nome == null) {
			proprieta.remove(NOME);
		} else {
			proprieta.put(NOME, Serializzabile.pulisciCampo(nome));
		}
	}

	/**
	 * Scrive una riga: uuid, tipo e, se ce ne sono, le proprietà (vedi {@link MappaProprieta}), tutto separato
	 * da {@link #PIPE}.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		if (proprieta.isEmpty()) {
			stampaRiga(stream,
					uuid,
					tipo);
		} else {
			stampaRiga(stream,
					uuid,
					tipo,
					MappaProprieta.salva(proprieta));
		}
	}

	/**
	 * Rilegge la riga scritta da {@link #salva(PrintWriter)}, sostituendo tutte le proprietà.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = new LettoreCampi(stream.readLine());
		uuid = campi.testo();
		tipo = campi.enumerato(TipoLocazione.class);
		MappaProprieta.leggi(campi, proprieta);
	}

}
