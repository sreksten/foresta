package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.missioni.AspettoDelMandante;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoMissione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * Un incarico di combattimento (vedi IncaricoDiCombattimento), letto da una riga di INCARICO_DI_COMBATTIMENTO in
 * missioni.txt, che ne descrive i campi: chi lo chiede, chi va sconfitto, dove, quanto si paga e i testi.
 * Nei testi %CAPO% è il nome del capo, se c'è (vedi CapoDellaRiga).
 */
public final class CombattimentoRichiesto {

	/**
	 * Dove si può nascondere chi va sconfitto.
	 */
	public static final Set<TipoLocazione> LUOGHI = EnumSet.of(TipoLocazione.GROTTA, TipoLocazione.ROVINE, TipoLocazione.BOSCO,
			TipoLocazione.PALUDE, TipoLocazione.RADURA);
	public static final String CAPO = "%CAPO%";
	private static final String PER_LIVELLO = "LIVELLO:";

	private final String riga;
	private final String chiave;
	private final TipoMissione tipo;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final TipoPersonaggio nemico;
	// Quanti sono; se numeroPerLivello, il massimo (vedi getNumero)
	private final int numero;
	private final boolean numeroPerLivello;
	private final CapoDellaRiga capo;
	private final boolean finoAllaResa;
	private final boolean aDuello;
	private final OndateDellaRiga ondate;
	private final TipoLocazione luogo;
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
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, OndateDellaRiga.conICampiDelleOndate("CHIAVE", "TIPO", "ASPETTO",
				"MANDANTE", "NEMICO", "NUMERO", "CAPO", "RESA", "DUELLO", "LUOGO", "MONETE", "TITOLO", "RICHIESTA", "BATTUTA",
				"RISPOSTA", "VITTORIA", "RINGRAZIAMENTO", "RICORDO"));
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato("TIPO", TipoMissione.class);
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		nemico = campi.enumerato("NEMICO", TipoPersonaggio.class);
		String quanti = campi.obbligatorio("NUMERO");
		numeroPerLivello = quanti.startsWith(PER_LIVELLO);
		try {
			numero = Integer.parseInt(numeroPerLivello ? quanti.substring(PER_LIVELLO.length()) : quanti);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("NUMERO è un numero, o " + PER_LIVELLO + " e il massimo: " + riga, e);
		}
		capo = CapoDellaRiga.da(campi.facoltativo("CAPO"));
		finoAllaResa = campi.facoltativo("RESA").map("SI"::equals).orElse(false);
		aDuello = campi.facoltativo("DUELLO").map("SI"::equals).orElse(false);
		luogo = campi.enumerato("LUOGO", TipoLocazione.class);
		if (!LUOGHI.contains(luogo)) {
			throw new IllegalArgumentException("Il luogo è fra " + LUOGHI + ": " + riga);
		}
		monete = campi.intero("MONETE");
		if (numero < 1 || monete < 1) {
			throw new IllegalArgumentException("Numero e monete sono almeno 1: " + riga);
		}
		if (aDuello && (numero != 1 || numeroPerLivello)) {
			throw new IllegalArgumentException("A duello si sfida da soli: NUMERO=1 con DUELLO=SI: " + riga);
		}
		ondate = OndateDellaRiga.da(campi, riga);
		if (aDuello && !ondate.isVuota()) {
			throw new IllegalArgumentException("Un duello non arriva a ondate: " + riga);
		}
		titolo = campi.obbligatorio("TITOLO");
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		vittoria = campi.obbligatorio("VITTORIA");
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		ricordo = campi.obbligatorio("RICORDO");
	}

	/**
	 * Le ondate che arrivano dopo i primi nemici, se ce ne sono (ONDATA_2=, ONDATA_3=).
	 */
	public OndateDellaRiga getOndate() {
		return ondate;
	}

	/**
	 * Legge la riga della grammatica che descrive l'incarico di combattimento.
	 *
	 * @throws IllegalArgumentException se la riga è sbagliata o manca un campo obbligatorio
	 */
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

	public AspettoDelMandante getAspetto() {
		return aspetto;
	}

	/**
	 * Chi lo chiede, con l'articolo: "il mugnaio".
	 */
	public String getMandante() {
		return mandante;
	}

	public TipoPersonaggio getNemico() {
		return nemico;
	}

	/**
	 * Quanti sono i nemici. Di solito il numero scritto nella riga; con NUMERO=LIVELLO:4 tanti quanto il livello del mondo
	 * di quando l'incarico si è offerto (almeno uno), fino al massimo scritto dopo i due punti.
	 */
	public int getNumero(int livelloDelMondo) {
		return numeroPerLivello ? Math.max(1, Math.min(livelloDelMondo, numero)) : numero;
	}

	/**
	 * Se c'è un capo con un nome, un livello sopra gli altri.
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

	/**
	 * La classe del capo se non è uno della banda (un hobgoblin a capo dei goblin), altrimenti null. Solo se c'è un capo.
	 */
	public TipoPersonaggio getClasseDelCapo() {
		return capo.getClasse();
	}

	public TipoLocazione getLuogo() {
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

	/**
	 * Se si combatte fino alla resa e non all'ultimo sangue (RESA=SI): vedi IncontroDiMissione.finoAllaResa.
	 */
	public boolean isFinoAllaResa() {
		return finoAllaResa;
	}


	/**
	 * Se il nemico sfida a duello, uno contro uno (DUELLO=SI): vedi IncontroDiMissione.aDuello.
	 */
	public boolean isADuello() {
		return aDuello;
	}
}
