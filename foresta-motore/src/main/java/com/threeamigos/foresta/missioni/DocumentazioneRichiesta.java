package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.missioni.IndagineRichiesta.Indizio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Una documentazione (vedi LaDocumentazione), letta da una riga di DOCUMENTAZIONE in missioni.txt, che ne descrive i
 * campi: chi la chiede, i posti da documentare e che cosa ci si trova, quanto si paga e i testi.
 */
public final class DocumentazioneRichiesta {

	private static final int REPERTI_MASSIMI = 3;

	private final String riga;
	private final String chiave;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final List<Indizio> reperti = new ArrayList<>();
	private final int monete;
	private final String titolo;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final String ringraziamento;

	private DocumentazioneRichiesta(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "ASPETTO", "MANDANTE", "REPERTO_1", "REPERTO_2", "REPERTO_3",
				"MONETE", "TITOLO", "RICHIESTA", "BATTUTA", "RISPOSTA", "RINGRAZIAMENTO");
		chiave = campi.obbligatorio("CHIAVE");
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		reperti.add(Indizio.da(campi.obbligatorio("REPERTO_1"), riga));
		reperti.add(Indizio.da(campi.obbligatorio("REPERTO_2"), riga));
		Optional<String> terzo = campi.facoltativo("REPERTO_" + REPERTI_MASSIMI);
		terzo.ifPresent(reperto -> reperti.add(Indizio.da(reperto, riga)));
		monete = campi.intero("MONETE");
		if (monete < 1) {
			throw new IllegalArgumentException("Le monete sono almeno 1: " + riga);
		}
		titolo = campi.obbligatorio("TITOLO");
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
	}

	public static DocumentazioneRichiesta da(String riga) {
		return new DocumentazioneRichiesta(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	AspettoDelMandante getAspetto() {
		return aspetto;
	}

	/**
	 * Chi la chiede, con l'articolo: "lo storico".
	 */
	public String getMandante() {
		return mandante;
	}

	/**
	 * I posti da documentare, due o tre, nell'ordine, con quello che ci si trova.
	 */
	public List<Indizio> getReperti() {
		return Collections.unmodifiableList(reperti);
	}

	public int getMonete() {
		return monete;
	}

	public String getTitolo() {
		return titolo;
	}

	public String getRichiesta() {
		return richiesta;
	}

	public String getBattutaDelCapo() {
		return battutaDelCapo;
	}

	public String getRisposta() {
		return risposta;
	}

	public String getRingraziamento() {
		return ringraziamento;
	}
}
