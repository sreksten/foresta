package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Il favore che una riga di missioni.txt chiede (vedi IlFavore): sconfiggere qualcuno in un posto, raccogliere
 * qualcosa o vegliare un posto. Sta dentro le righe di BENEDIZIONE (vedi BenedizioneRichiesta) e di LEALTA (vedi
 * LealtaRichiesta), che lo leggono con i loro campi; nei testi %CAPO% è il nome del capo dei nemici, se c'è (vedi
 * CapoDellaRiga).
 */
public final class FavoreRichiesto {

	/**
	 * Che favore è: lo dicono i campi della riga (NEMICO, INGREDIENTE o VISITE).
	 */
	public enum Tipo {
		COMBATTIMENTO,
		RACCOLTA,
		VEGLIA
	}

	/**
	 * I campi del favore, da ammettere nelle righe che lo contengono.
	 */
	static final String[] CAMPI = {"FAVORE", "LUOGO", "NEMICO", "NUMERO", "CAPO", "GENERE", "INGREDIENTE", "INGREDIENTI",
			"DOVE", "QUANTITA", "VISITE", "ORE", "VEGLIA", "VITTORIA"};

	private final String nome;
	private final Tipo tipo;
	private final TipoLocazione luogo;
	private final ClassePersonaggio nemico;
	private final int numero;
	private final CapoDellaRiga capo;
	private final MaterialeRichiesto ingrediente;
	private final int quantitaDaRaccogliere;
	private final int visite;
	private final int ore;
	private final String veglia;
	private final String vittoria;

	FavoreRichiesto(CampiDiGrammatica campi, String riga) {
		nome = campi.obbligatorio("FAVORE");
		boolean combattimento = campi.facoltativo("NEMICO").isPresent();
		boolean raccolta = campi.facoltativo("INGREDIENTE").isPresent();
		boolean conVeglia = campi.facoltativo("VISITE").isPresent();
		if ((combattimento ? 1 : 0) + (raccolta ? 1 : 0) + (conVeglia ? 1 : 0) != 1) {
			throw new IllegalArgumentException("Il favore è uno solo: NEMICO, INGREDIENTE o VISITE: " + riga);
		}
		tipo = combattimento ? Tipo.COMBATTIMENTO : raccolta ? Tipo.RACCOLTA : Tipo.VEGLIA;
		if (tipo == Tipo.RACCOLTA) {
			luogo = null;
			if (campi.facoltativo("LUOGO").isPresent()) {
				throw new IllegalArgumentException("Per una raccolta il luogo è nella provenienza (DOVE=): " + riga);
			}
		} else {
			luogo = campi.enumerato("LUOGO", TipoLocazione.class);
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
	}

	/**
	 * Unisce ai campi di una produzione quelli del favore.
	 */
	static String[] conICampiDelFavore(String... campi) {
		String[] tutti = new String[campi.length + CAMPI.length];
		System.arraycopy(campi, 0, tutti, 0, campi.length);
		System.arraycopy(CAMPI, 0, tutti, campi.length, CAMPI.length);
		return tutti;
	}

	/**
	 * Il nome del favore, che è anche il nome della missione secondaria: "Gli spettri della cappella".
	 */
	public String getNome() {
		return nome;
	}

	public Tipo getTipo() {
		return tipo;
	}

	/**
	 * Dove si fa il favore, per un combattimento o una veglia; null per una raccolta.
	 */
	public TipoLocazione getLuogo() {
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
	 * Il nome del capo dei nemici: scritto nella riga, o pescato (vedi CapoDellaRiga). Solo se c'è un capo.
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
}
