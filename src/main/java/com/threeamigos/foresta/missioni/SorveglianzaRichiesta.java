package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.Optional;

/**
 * Una sorveglianza (vedi LaSorveglianza), letta da una riga di SORVEGLIANZA in missioni.txt, che ne descrive i campi:
 * chi la chiede, che posto tenere d'occhio, quante volte e a quante ore di distanza, chi salta fuori alla fine (se
 * qualcuno salta fuori), quanto si paga e i testi. Nei testi %CAPO% è il nome del capo, se c'è (vedi CapoDellaRiga).
 */
public final class SorveglianzaRichiesta {

	static final String CAPO = CombattimentoRichiesto.CAPO;

	private final String riga;
	private final String chiave;
	private final TipoMissione tipo;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final TipoLocazione luogo;
	private final int visite;
	private final int ore;
	private final ClassePersonaggio nemico;
	private final int numero;
	private final CapoDellaRiga capo;
	private final int monete;
	private final String titolo;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final String veglia;
	private final String scoperta;
	private final String vittoria;
	private final String ringraziamento;
	private final String ricordo;
	private final OndateDellaRiga ondate;

	private SorveglianzaRichiesta(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, OndateDellaRiga.conICampiDelleOndate("CHIAVE", "TIPO", "ASPETTO",
				"MANDANTE", "LUOGO", "VISITE", "ORE", "NEMICO", "NUMERO", "CAPO", "MONETE", "TITOLO", "RICHIESTA", "BATTUTA",
				"RISPOSTA", "VEGLIA", "SCOPERTA", "VITTORIA", "RINGRAZIAMENTO", "RICORDO"));
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato("TIPO", TipoMissione.class);
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		luogo = campi.enumerato("LUOGO", TipoLocazione.class);
		if (!CombattimentoRichiesto.LUOGHI.contains(luogo)) {
			throw new IllegalArgumentException("Il luogo è fra " + CombattimentoRichiesto.LUOGHI + ": " + riga);
		}
		visite = campi.intero("VISITE");
		ore = campi.intero("ORE");
		monete = campi.intero("MONETE");
		if (visite < 2 || ore < 1 || monete < 1) {
			throw new IllegalArgumentException("Le visite sono almeno 2, ore e monete almeno 1: " + riga);
		}
		Optional<String> classe = campi.facoltativo("NEMICO");
		nemico = classe.map(ClassePersonaggio::valueOf).orElse(null);
		numero = nemico == null ? 0 : campi.intero("NUMERO");
		capo = CapoDellaRiga.da(campi.facoltativo("CAPO"));
		vittoria = campi.facoltativo("VITTORIA").orElse(null);
		if (nemico == null ? campi.facoltativo("NUMERO").isPresent() || capo != null || vittoria != null
				: numero < 1 || vittoria == null) {
			throw new IllegalArgumentException("NEMICO, NUMERO e VITTORIA vanno insieme, e CAPO solo con loro: " + riga);
		}
		ondate = OndateDellaRiga.da(campi, riga);
		if (nemico == null && !ondate.isVuota()) {
			throw new IllegalArgumentException("Le ondate arrivano dopo i nemici dell'ultima visita (NEMICO=): " + riga);
		}
		titolo = campi.obbligatorio("TITOLO");
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		veglia = campi.obbligatorio("VEGLIA");
		scoperta = campi.obbligatorio("SCOPERTA");
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		ricordo = campi.obbligatorio("RICORDO");
	}

	/**
	 * Le ondate che arrivano all'ultima visita dopo i primi nemici, se ce ne sono (ONDATA_2=, ONDATA_3=).
	 */
	OndateDellaRiga getOndate() {
		return ondate;
	}

	public static SorveglianzaRichiesta da(String riga) {
		return new SorveglianzaRichiesta(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * Il tipo di missione che la sorveglianza copre (vigilia, sorveglianza, spionaggio).
	 */
	public TipoMissione getTipo() {
		return tipo;
	}

	AspettoDelMandante getAspetto() {
		return aspetto;
	}

	/**
	 * Chi la chiede, con l'articolo: "il capitano delle guardie".
	 */
	public String getMandante() {
		return mandante;
	}

	public TipoLocazione getLuogo() {
		return luogo;
	}

	/**
	 * Quante volte bisogna passare dal posto.
	 */
	public int getVisite() {
		return visite;
	}

	/**
	 * Quante ore di gioco devono passare almeno fra una visita che conta e la successiva.
	 */
	public int getOre() {
		return ore;
	}

	/**
	 * Se alla fine della sorveglianza salta fuori qualcuno da sconfiggere.
	 */
	public boolean isConNemici() {
		return nemico != null;
	}

	/**
	 * Chi salta fuori alla fine, o null se nessuno.
	 */
	public ClassePersonaggio getNemico() {
		return nemico;
	}

	public int getNumero() {
		return numero;
	}

	/**
	 * Se fra chi salta fuori c'è un capo con un nome, un livello sopra gli altri.
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
	 * Che cosa si scrive a una visita che conta, se non è l'ultima.
	 */
	public String getVeglia() {
		return veglia;
	}

	/**
	 * Che cosa si scrive all'ultima visita: che cosa si è scoperto, o chi salta fuori.
	 */
	public String getScoperta() {
		return scoperta;
	}

	/**
	 * Che cosa si scrive quando chi è saltato fuori è stato sconfitto, o null se nessuno salta fuori.
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
