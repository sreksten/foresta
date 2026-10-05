package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un torneo (vedi IlTorneo), letto da una riga di TORNEO in missioni.txt, che ne descrive i campi: chi lo bandisce,
 * dove si combatte, gli sfidanti dei primi due turni, il campione della finale, la borsa del vincitore e i testi.
 * Nei testi %CAMPIONE% è il nome del campione (vedi CapoDellaRiga).
 */
public final class TorneoRichiesto {

	static final String CAMPIONE = "%CAMPIONE%";
	/**
	 * Quanti sfidanti si affrontano prima della finale.
	 */
	public static final int SFIDANTI_PRIMA_DELLA_FINALE = 2;

	private final String riga;
	private final String chiave;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final String titolo;
	private final TipoLocazione luogo;
	private final List<ClassePersonaggio> sfidanti = new ArrayList<>();
	private final ClassePersonaggio campione;
	private final CapoDellaRiga nomeDelCampione;
	private final int monete;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final List<String> turniVinti = new ArrayList<>();
	private final String finale;
	private final String ringraziamento;
	private final String ricordo;

	private TorneoRichiesto(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "ASPETTO", "MANDANTE", "TITOLO", "LUOGO", "SFIDANTI",
				"CAMPIONE", "NOME", "MONETE", "RICHIESTA", "BATTUTA", "RISPOSTA", "PRIMO_TURNO", "SECONDO_TURNO", "FINALE",
				"RINGRAZIAMENTO", "RICORDO");
		chiave = campi.obbligatorio("CHIAVE");
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		titolo = campi.obbligatorio("TITOLO");
		luogo = campi.enumerato("LUOGO", TipoLocazione.class);
		if (!CombattimentoRichiesto.LUOGHI.contains(luogo)) {
			throw new IllegalArgumentException("Il luogo è fra " + CombattimentoRichiesto.LUOGHI + ": " + riga);
		}
		for (String sfidante : campi.obbligatorio("SFIDANTI").trim().split("\\s+")) {
			sfidanti.add(ClassePersonaggio.valueOf(sfidante));
		}
		if (sfidanti.size() != SFIDANTI_PRIMA_DELLA_FINALE) {
			throw new IllegalArgumentException("Gli sfidanti prima della finale sono " + SFIDANTI_PRIMA_DELLA_FINALE
					+ ", separati da uno spazio (GUERRIERO ELFA): " + riga);
		}
		campione = campi.enumerato("CAMPIONE", ClassePersonaggio.class);
		nomeDelCampione = CapoDellaRiga.da(campi.facoltativo("NOME"));
		if (nomeDelCampione == null) {
			throw new IllegalArgumentException("Manca il campo NOME in " + riga);
		}
		monete = campi.intero("MONETE");
		if (monete < 1) {
			throw new IllegalArgumentException("Le monete sono almeno 1: " + riga);
		}
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		turniVinti.add(campi.obbligatorio("PRIMO_TURNO"));
		turniVinti.add(campi.obbligatorio("SECONDO_TURNO"));
		finale = campi.obbligatorio("FINALE");
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		ricordo = campi.obbligatorio("RICORDO");
	}

	public static TorneoRichiesto da(String riga) {
		return new TorneoRichiesto(riga);
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
	 * Chi bandisce il torneo, con l'articolo: "il borgomastro".
	 */
	public String getMandante() {
		return mandante;
	}

	/**
	 * Il nome del torneo, che è anche il nome della missione.
	 */
	public String getTitolo() {
		return titolo;
	}

	/**
	 * Dove si combatte: la lizza.
	 */
	public TipoLocazione getLuogo() {
		return luogo;
	}

	/**
	 * Gli sfidanti dei turni prima della finale, nell'ordine.
	 */
	public List<ClassePersonaggio> getSfidanti() {
		return Collections.unmodifiableList(sfidanti);
	}

	/**
	 * Il campione da battere in finale.
	 */
	public ClassePersonaggio getCampione() {
		return campione;
	}

	/**
	 * Il nome del campione: scritto nella riga, o pescato (vedi CapoDellaRiga).
	 */
	public String pescaNomeDelCampione() {
		return nomeDelCampione.pescaNome();
	}

	/**
	 * La borsa del vincitore, che si riscuote in città oltre al leggendario.
	 */
	public int getMonete() {
		return monete;
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

	/**
	 * Che cosa si scrive vinto quel turno prima della finale, da 0.
	 */
	public String getTurnoVinto(int turno) {
		return turniVinti.get(turno);
	}

	/**
	 * Che cosa si scrive vinta la finale, prima della consegna del leggendario.
	 */
	public String getFinale() {
		return finale;
	}

	public String getRingraziamento() {
		return ringraziamento;
	}

	public String getRicordo() {
		return ricordo;
	}
}
