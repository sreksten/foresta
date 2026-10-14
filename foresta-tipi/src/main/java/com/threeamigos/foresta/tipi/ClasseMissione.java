package com.threeamigos.foresta.tipi;

/**
 * Le classi concrete di missione: l'identificativo con cui il modello dati (MissioneMD) dichiara il tipo di ogni nodo
 * dell'albero delle missioni, così che la ricostruzione dopo un caricamento sia polimorfica. La missione vera la
 * costruisce missioni.FabbricaMissioni. (Da non confondere con missioni.TipoMissione, le categorie di incarico.)
 *
 * Le <i>predefinite</i> sono le missioni che esistono dall'inizio della partita, in questo ordine: il loro nome è
 * l'identificativo con cui la missione si salva. Le altre nascono durante il gioco (o nei test).
 */
public enum ClasseMissione {

	SCONFIGGI_IL_DRAGO(true),
	LA_LEGGENDA_DELL_ARMAIOLO(true),
	LA_LEGGENDA_DEL_LOCANDIERE(true),
	CRONACHE_DI_UN_FEGATO_EROICO(true),
	NESSUN_BOCCALE_LASCIATO_INDIETRO(true),
	DISTURBATORE_DELLA_QUIETE_PUBBLICA(true),
	CACCIA_AI_GOBLIN(true),
	RICHIESTA_ALCHIMISTA(true),
	RICHIESTA_ARMAIOLO(true),
	RICHIESTA_CAPITANO(true),
	RICHIESTA_LOCANDIERE(true),
	LA_TAGLIA_SULLA_BANDA(true),
	IL_PELLEGRINO(true),
	IL_RAPIMENTO(true),
	NON_SPARATE_SUL_PIANISTA(true),
	IL_VIAGGIO_DELLA_MOGLIE(true),
	CACCIATORE_DI_TAGLIE(true),
	IL_CARTOGRAFO(true),
	IL_CORRIERE(true),
	L_OGGETTO_SMARRITO(true),
	INCARICO_DI_COMBATTIMENTO(true),
	LA_SORVEGLIANZA(true),
	IL_CONTRABBANDIERE(true),
	IL_SOCCORSO(true),
	L_INDAGINE(true),
	IL_RITUALE(true),
	LA_BENEDIZIONE(true),
	LA_DOCUMENTAZIONE(true),
	IL_COLPO(true),
	LA_LEALTA(true),
	IL_TORNEO(true),
	// Nascono durante il gioco
	SCONFIGGI_IL_MINOTAURO_GIGANTE,
	SCONFIGGI_L_IDRA,
	SCONFIGGI_IL_LICH,
	SCONFIGGI_LA_STREGA,
	IL_FAVORE,
	LA_SFIDA_DEI_CAMPIONI,
	IL_CAMPIONE,
	VISITA_LOCANDA,
	MUOVI_A_LOCAZIONE,
	MISSIONE_SECONDARIA,
	// Usate dai test, che inventano le loro missioni: non hanno una classe nel gioco
	MISSIONE_DI_PROVA,
	MISSIONE_CHE_FALLISCE;

	private final boolean predefinita;

	ClasseMissione() {
		this(false);
	}

	ClasseMissione(boolean predefinita) {
		this.predefinita = predefinita;
	}

	/**
	 * Dice se la missione esiste dall'inizio della partita.
	 */
	public boolean isPredefinita() {
		return predefinita;
	}

	/**
	 * Dice se quell'identificativo è quello di una missione predefinita.
	 */
	public static boolean isIdDiMissionePredefinita(String id) {
		return predefinitaConId(id) != null;
	}

	/**
	 * La classe della missione predefinita con quell'identificativo, o null se non è di una predefinita.
	 */
	public static ClasseMissione predefinitaConId(String id) {
		for (ClasseMissione classe : values()) {
			if (classe.predefinita && classe.name().equals(id)) {
				return classe;
			}
		}
		return null;
	}
}
