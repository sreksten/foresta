package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

/**
 * Un soccorso (vedi IlSoccorso), letto da una riga di SOCCORSO in missioni.txt, che ne descrive i campi: chi lo
 * chiede, chi va riportato a casa, dove sta e chi lo minaccia, quanto si paga e i testi. Nei testi %NOME% è il nome
 * di chi va riportato a casa, %CAPO% quello del capo dei nemici, se c'è (vedi CapoDellaRiga).
 */
public final class SoccorsoRichiesto {

	static final String NOME = "%NOME%";
	static final String CAPO = CombattimentoRichiesto.CAPO;

	private final String riga;
	private final String chiave;
	private final TipoMissione tipo;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final String persona;
	private final ClassePersonaggio nemico;
	private final int numero;
	private final CapoDellaRiga capo;
	private final ClassiLocazione luogo;
	private final int monete;
	private final String titolo;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final String soccorso;
	private final String ringraziamento;
	private final String lutto;
	private final String ricordo;

	private SoccorsoRichiesto(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "TIPO", "ASPETTO", "MANDANTE", "PERSONA", "NEMICO",
				"NUMERO", "CAPO", "LUOGO", "MONETE", "TITOLO", "RICHIESTA", "BATTUTA", "RISPOSTA", "SOCCORSO", "RINGRAZIAMENTO",
				"LUTTO", "RICORDO");
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato("TIPO", TipoMissione.class);
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		persona = campi.obbligatorio("PERSONA");
		nemico = campi.enumerato("NEMICO", ClassePersonaggio.class);
		numero = campi.intero("NUMERO");
		capo = CapoDellaRiga.da(campi.facoltativo("CAPO"));
		luogo = campi.enumerato("LUOGO", ClassiLocazione.class);
		if (!CombattimentoRichiesto.LUOGHI.contains(luogo)) {
			throw new IllegalArgumentException("Il luogo è fra " + CombattimentoRichiesto.LUOGHI + ": " + riga);
		}
		monete = campi.intero("MONETE");
		if (numero < 1 || monete < 1) {
			throw new IllegalArgumentException("Numero e monete sono almeno 1: " + riga);
		}
		titolo = campi.obbligatorio("TITOLO");
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		soccorso = campi.obbligatorio("SOCCORSO");
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		lutto = campi.obbligatorio("LUTTO");
		ricordo = campi.obbligatorio("RICORDO");
	}

	public static SoccorsoRichiesto da(String riga) {
		return new SoccorsoRichiesto(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * Il tipo di missione che il soccorso copre (soccorso, salvataggio).
	 */
	public TipoMissione getTipo() {
		return tipo;
	}

	AspettoDelMandante getAspetto() {
		return aspetto;
	}

	/**
	 * Chi lo chiede, con l'articolo: "la moglie del taglialegna".
	 */
	public String getMandante() {
		return mandante;
	}

	/**
	 * Chi va riportato a casa, con l'articolo: "il taglialegna".
	 */
	public String getPersona() {
		return persona;
	}

	public ClassePersonaggio getNemico() {
		return nemico;
	}

	public int getNumero() {
		return numero;
	}

	/**
	 * Se fra i nemici c'è un capo con un nome, un livello sopra gli altri.
	 */
	public boolean isConCapo() {
		return capo != null;
	}

	/**
	 * Il nome del capo: scritto nella riga, o pescato dalla sua produzione (vedi CapoDellaRiga). Solo se c'è un capo.
	 */
	public String pescaNomeDelCapo() {
		return capo.pescaNome();
	}

	public ClassiLocazione getLuogo() {
		return luogo;
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
	 * Che cosa si scrive quando, sconfitti i nemici, la persona si unisce al gruppo.
	 */
	public String getSoccorso() {
		return soccorso;
	}

	public String getRingraziamento() {
		return ringraziamento;
	}

	/**
	 * L'ultima battuta del mandante nella scena triste, se la persona è morta per strada.
	 */
	public String getLutto() {
		return lutto;
	}

	/**
	 * Il ricordo del luogo, a missione finita.
	 */
	public String getRicordo() {
		return ricordo;
	}
}
