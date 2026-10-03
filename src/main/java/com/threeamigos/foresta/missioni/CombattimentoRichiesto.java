package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * Un incarico di combattimento (vedi IncaricoDiCombattimento), letto da una riga di INCARICO_DI_COMBATTIMENTO in
 * missioni.txt, che ne descrive i campi: chi lo chiede, chi va sconfitto, dove, quanto si paga e i testi.
 * Nei testi %CAPO% è il nome del capo, se c'è.
 */
public final class CombattimentoRichiesto {

	/**
	 * Dove si può nascondere chi va sconfitto.
	 */
	static final Set<ClassiLocazione> LUOGHI = EnumSet.of(ClassiLocazione.GROTTA, ClassiLocazione.ROVINE, ClassiLocazione.BOSCO,
			ClassiLocazione.PALUDE, ClassiLocazione.RADURA);
	static final String CAPO = "%CAPO%";

	private final String riga;
	private final String chiave;
	private final TipoMissione tipo;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final ClassePersonaggio nemico;
	private final int numero;
	private final boolean conCapo;
	private final ClassiLocazione luogo;
	private final int monete;
	private final String titolo;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final String vittoria;
	private final String ringraziamento;
	private final String ricordo;

	private CombattimentoRichiesto(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "TIPO", "ASPETTO", "MANDANTE", "NEMICO", "NUMERO",
				"CAPO", "LUOGO", "MONETE", "TITOLO", "RICHIESTA", "BATTUTA", "RISPOSTA", "VITTORIA", "RINGRAZIAMENTO", "RICORDO");
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato("TIPO", TipoMissione.class);
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		nemico = campi.enumerato("NEMICO", ClassePersonaggio.class);
		numero = campi.intero("NUMERO");
		conCapo = campi.facoltativo("CAPO").map("SI"::equals).orElse(false);
		luogo = campi.enumerato("LUOGO", ClassiLocazione.class);
		if (!LUOGHI.contains(luogo)) {
			throw new IllegalArgumentException("Il luogo è fra " + LUOGHI + ": " + riga);
		}
		monete = campi.intero("MONETE");
		if (numero < 1 || monete < 1) {
			throw new IllegalArgumentException("Numero e monete sono almeno 1: " + riga);
		}
		titolo = campi.obbligatorio("TITOLO");
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		vittoria = campi.obbligatorio("VITTORIA");
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		ricordo = campi.obbligatorio("RICORDO");
	}

	public static CombattimentoRichiesto da(String riga) {
		return new CombattimentoRichiesto(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * Il tipo di missione che l'incarico copre (vendetta, bestia, pulizia di un covo...).
	 */
	public TipoMissione getTipo() {
		return tipo;
	}

	AspettoDelMandante getAspetto() {
		return aspetto;
	}

	/**
	 * Chi lo chiede, con l'articolo: "il mugnaio".
	 */
	public String getMandante() {
		return mandante;
	}

	public ClassePersonaggio getNemico() {
		return nemico;
	}

	public int getNumero() {
		return numero;
	}

	/**
	 * Se c'è un capo con un nome (da NOME_CAPOBANDA), un livello sopra gli altri.
	 */
	public boolean isConCapo() {
		return conCapo;
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
	 * Che cosa si scrive quando sono stati sconfitti.
	 */
	public String getVittoria() {
		return vittoria;
	}

	public String getRingraziamento() {
		return ringraziamento;
	}

	/**
	 * Il ricordo del luogo, a missione finita.
	 */
	public String getRicordo() {
		return ricordo;
	}
}
