package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.tipi.TipoAttributo;
import com.threeamigos.foresta.motore.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

/**
 * Una benedizione (vedi LaBenedizione), letta da una riga di BENEDIZIONE in missioni.txt, che ne descrive i campi: chi
 * la offre in una locanda, il favore che chiede in cambio (vedi IlFavore: sconfiggere qualcuno, raccogliere qualcosa
 * o vegliare un posto), la benedizione che dà in un tempio e i testi. Nei testi %CAPO% è il nome del capo dei nemici del favore, se c'è (vedi
 * CapoDellaRiga), e %PERSONAGGIO% il nome di chi riceve la benedizione.
 */
public final class BenedizioneRichiesta {

	/**
	 * Che favore chiede il sacerdote: lo dicono i campi della riga (NEMICO, INGREDIENTE o VISITE).
	 */
	public enum TipoFavore {
		COMBATTIMENTO,
		RACCOLTA,
		VEGLIA
	}

	static final String CAPO = CombattimentoRichiesto.CAPO;
	static final String PERSONAGGIO = "%PERSONAGGIO%";
	private static final String SACERDOTESSA = "SACERDOTESSA";
	private static final String SACERDOTE = "SACERDOTE";

	private final String riga;
	private final String chiave;
	private final boolean sacerdotessa;
	private final String mandante;
	private final String nomeDellaBenedizione;
	private final TipoAttributo attributo;
	private final TipoModificatore tipoModificatore;
	private final int quantita;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final String favore;
	private final TipoFavore tipoFavore;
	private final ClassiLocazione luogo;
	private final ClassePersonaggio nemico;
	private final int numero;
	private final CapoDellaRiga capo;
	private final MaterialeRichiesto ingrediente;
	private final int quantitaDaRaccogliere;
	private final int visite;
	private final int ore;
	private final String veglia;
	private final String vittoria;
	private final String benedetto;

	private BenedizioneRichiesta(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "ASPETTO", "MANDANTE", "NOME", "BENEDIZIONE", "RICHIESTA",
				"BATTUTA", "RISPOSTA", "FAVORE", "LUOGO", "NEMICO", "NUMERO", "CAPO", "GENERE", "INGREDIENTE", "INGREDIENTI", "DOVE",
				"QUANTITA", "VISITE", "ORE", "VEGLIA", "VITTORIA", "BENEDETTO");
		chiave = campi.obbligatorio("CHIAVE");
		String aspetto = campi.obbligatorio("ASPETTO");
		if (!SACERDOTESSA.equals(aspetto) && !SACERDOTE.equals(aspetto)) {
			throw new IllegalArgumentException("L'aspetto è SACERDOTE o SACERDOTESSA: " + riga);
		}
		sacerdotessa = SACERDOTESSA.equals(aspetto);
		mandante = campi.obbligatorio("MANDANTE");
		nomeDellaBenedizione = campi.obbligatorio("NOME");
		String[] benedizione = campi.obbligatorio("BENEDIZIONE").trim().split("\\s+");
		if (benedizione.length != 3) {
			throw new IllegalArgumentException("La benedizione è ATTRIBUTO TIPO QUANTITA (FORZA AUMENTO_FISSO 2): " + riga);
		}
		attributo = TipoAttributo.valueOf(benedizione[0]);
		tipoModificatore = TipoModificatore.valueOf(benedizione[1]);
		quantita = Integer.parseInt(benedizione[2]);
		if (tipoModificatore == TipoModificatore.QUANTITA_ASSOLUTA || quantita < 1) {
			throw new IllegalArgumentException("La benedizione è un aumento, fisso o in percentuale, di almeno 1: " + riga);
		}
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		favore = campi.obbligatorio("FAVORE");
		boolean combattimento = campi.facoltativo("NEMICO").isPresent();
		boolean raccolta = campi.facoltativo("INGREDIENTE").isPresent();
		boolean conVeglia = campi.facoltativo("VISITE").isPresent();
		if ((combattimento ? 1 : 0) + (raccolta ? 1 : 0) + (conVeglia ? 1 : 0) != 1) {
			throw new IllegalArgumentException("Il favore è uno solo: NEMICO, INGREDIENTE o VISITE: " + riga);
		}
		tipoFavore = combattimento ? TipoFavore.COMBATTIMENTO : raccolta ? TipoFavore.RACCOLTA : TipoFavore.VEGLIA;
		if (tipoFavore == TipoFavore.RACCOLTA) {
			luogo = null;
			if (campi.facoltativo("LUOGO").isPresent()) {
				throw new IllegalArgumentException("Per una raccolta il luogo è nella provenienza (DOVE=): " + riga);
			}
		} else {
			luogo = campi.enumerato("LUOGO", ClassiLocazione.class);
			if (!CombattimentoRichiesto.LUOGHI.contains(luogo)) {
				throw new IllegalArgumentException("Il luogo del favore è fra " + CombattimentoRichiesto.LUOGHI + ": " + riga);
			}
		}
		nemico = combattimento ? campi.enumerato("NEMICO", ClassePersonaggio.class) : null;
		numero = combattimento ? campi.intero("NUMERO") : 0;
		if (combattimento && numero < 1) {
			throw new IllegalArgumentException("Il numero è almeno 1: " + riga);
		}
		capo = CapoDellaRiga.da(campi.facoltativo("CAPO"));
		if (capo != null && !combattimento) {
			throw new IllegalArgumentException("Il capo c'è solo se c'è da combattere: " + riga);
		}
		quantitaDaRaccogliere = raccolta ? campi.intero("QUANTITA") : 0;
		ingrediente = raccolta ? MaterialeRichiesto.ingrediente(campi.obbligatorio("GENERE"), campi.obbligatorio("INGREDIENTE"),
				campi.obbligatorio("INGREDIENTI"), campi.obbligatorio("DOVE"), quantitaDaRaccogliere) : null;
		visite = conVeglia ? campi.intero("VISITE") : 0;
		ore = conVeglia ? campi.intero("ORE") : 0;
		veglia = conVeglia ? campi.obbligatorio("VEGLIA") : null;
		if (conVeglia && (visite < 2 || ore < 1)) {
			throw new IllegalArgumentException("Una veglia ha almeno 2 visite e 1 ora fra l'una e l'altra: " + riga);
		}
		vittoria = campi.obbligatorio("VITTORIA");
		benedetto = campi.obbligatorio("BENEDETTO");
	}

	public static BenedizioneRichiesta da(String riga) {
		return new BenedizioneRichiesta(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * Se a offrire la benedizione è una sacerdotessa (altrimenti un sacerdote).
	 */
	public boolean isSacerdotessa() {
		return sacerdotessa;
	}

	/**
	 * Chi la offre, con l'articolo: "la sacerdotessa della luna".
	 */
	public String getMandante() {
		return mandante;
	}

	/**
	 * Il nome della benedizione, che resta scritto nel modificatore: "la benedizione della luna".
	 */
	public String getNomeDellaBenedizione() {
		return nomeDellaBenedizione;
	}

	public TipoAttributo getAttributo() {
		return attributo;
	}

	public TipoModificatore getTipoModificatore() {
		return tipoModificatore;
	}

	public int getQuantita() {
		return quantita;
	}

	/**
	 * Il modificatore permanente che la benedizione dà a chi la riceve.
	 */
	public ModificatoreAttributo nuovoModificatore() {
		return new ModificatoreAttributo(attributo, tipoModificatore, quantita, nomeDellaBenedizione);
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
	 * Il nome del favore, che è anche il nome della missione secondaria: "Gli spettri della cappella".
	 */
	public String getFavore() {
		return favore;
	}

	public TipoFavore getTipoFavore() {
		return tipoFavore;
	}

	/**
	 * Dove si fa il favore, per un combattimento o una veglia; null per una raccolta.
	 */
	public ClassiLocazione getLuogo() {
		return luogo;
	}

	public ClassePersonaggio getNemico() {
		return nemico;
	}

	public int getNumero() {
		return numero;
	}

	public boolean isConCapo() {
		return capo != null;
	}

	/**
	 * Il nome del capo dei nemici del favore: scritto nella riga, o pescato (vedi CapoDellaRiga). Solo se c'è un capo.
	 */
	public String pescaNomeDelCapo() {
		return capo.pescaNome();
	}

	/**
	 * L'ingrediente da raccogliere, per una raccolta; null altrimenti.
	 */
	public MaterialeRichiesto getIngrediente() {
		return ingrediente;
	}

	/**
	 * Quanti ingredienti raccogliere, per una raccolta.
	 */
	public int getQuantitaDaRaccogliere() {
		return quantitaDaRaccogliere;
	}

	/**
	 * Quante volte passare dal posto, per una veglia.
	 */
	public int getVisite() {
		return visite;
	}

	/**
	 * Quante ore lasciar passare almeno fra una visita e l'altra, per una veglia.
	 */
	public int getOre() {
		return ore;
	}

	/**
	 * Che cosa si scrive a una visita della veglia che conta, se non è l'ultima; null se non è una veglia.
	 */
	public String getVeglia() {
		return veglia;
	}

	/**
	 * Che cosa si scrive quando il favore è fatto.
	 */
	public String getVittoria() {
		return vittoria;
	}

	/**
	 * Che cosa si scrive quando un personaggio riceve la benedizione, con il suo nome al posto di %PERSONAGGIO%.
	 */
	public String getBenedetto() {
		return benedetto;
	}
}
