package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Un colpo (vedi IlColpo), letto da una riga di COLPO in missioni.txt, che ne descrive i campi: chi lo chiede, dove,
 * chi fa la guardia, che cosa si fa, quanto si paga e i testi. Nei testi %CAPO% è il nome del capo delle guardie, se
 * c'è (vedi CapoDellaRiga).
 */
public final class ColpoRichiesto {

	static final String CAPO = CombattimentoRichiesto.CAPO;

	private final String riga;
	private final String chiave;
	private final TipoMissione tipo;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final TipoLocazione luogo;
	private final TipoPersonaggio guardia;
	private final int numero;
	private final CapoDellaRiga capo;
	private final int monete;
	private final String titolo;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final String colpo;
	private final String scoperti;
	private final String ringraziamento;
	private final String ricordo;

	private ColpoRichiesto(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "TIPO", "ASPETTO", "MANDANTE", "LUOGO", "NEMICO", "NUMERO",
				"CAPO", "MONETE", "TITOLO", "RICHIESTA", "BATTUTA", "RISPOSTA", "COLPO", "SCOPERTI", "RINGRAZIAMENTO", "RICORDO");
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato("TIPO", TipoMissione.class);
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		luogo = campi.enumerato("LUOGO", TipoLocazione.class);
		if (!CombattimentoRichiesto.LUOGHI.contains(luogo)) {
			throw new IllegalArgumentException("Il luogo è fra " + CombattimentoRichiesto.LUOGHI + ": " + riga);
		}
		guardia = campi.enumerato("NEMICO", TipoPersonaggio.class);
		numero = campi.intero("NUMERO");
		capo = CapoDellaRiga.da(campi.facoltativo("CAPO"));
		monete = campi.intero("MONETE");
		if (numero < 1 || monete < 1) {
			throw new IllegalArgumentException("Numero e monete sono almeno 1: " + riga);
		}
		titolo = campi.obbligatorio("TITOLO");
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		colpo = campi.obbligatorio("COLPO");
		scoperti = campi.obbligatorio("SCOPERTI");
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		ricordo = campi.obbligatorio("RICORDO");
	}

	public static ColpoRichiesto da(String riga) {
		return new ColpoRichiesto(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * Il tipo di missione che il colpo copre (furto, sabotaggio, incendio...).
	 */
	public TipoMissione getTipo() {
		return tipo;
	}

	AspettoDelMandante getAspetto() {
		return aspetto;
	}

	/**
	 * Chi lo chiede, con l'articolo: "il mercante".
	 */
	public String getMandante() {
		return mandante;
	}

	/**
	 * Dove si fa il colpo.
	 */
	public TipoLocazione getLuogo() {
		return luogo;
	}

	/**
	 * Chi fa la guardia.
	 */
	public TipoPersonaggio getGuardia() {
		return guardia;
	}

	public int getNumero() {
		return numero;
	}

	public boolean isConCapo() {
		return capo != null;
	}

	/**
	 * Il nome del capo delle guardie: scritto nella riga, o pescato (vedi CapoDellaRiga). Solo se c'è un capo.
	 */
	public String pescaNomeDelCapo() {
		return capo.pescaNome();
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

	/**
	 * Che cosa si scrive quando il colpo riesce: che cosa si è fatto.
	 */
	public String getColpo() {
		return colpo;
	}

	/**
	 * Che cosa si scrive quando il gruppo combatte con le guardie: il colpo è fallito.
	 */
	public String getScoperti() {
		return scoperti;
	}

	public String getRingraziamento() {
		return ringraziamento;
	}

	/**
	 * Il ricordo del posto, a missione finita.
	 */
	public String getRicordo() {
		return ricordo;
	}
}
