package com.threeamigos.foresta.tipi;

/**
 * Le classi di missione della Foresta.
 * <p>
 * Il nome è anche l'identificativo con cui il modello dati (MissioneMD) dichiara il tipo di ogni nodo
 * dell'albero delle missioni, così che la ricostruzione dopo un caricamento sia polimorfica. La missione vera la
 * costruisce missioni.FabbricaMissioni. Da non confondere con {@link TipoMissione}, le categorie di incarico.
 * <p>
 * Quelle segnate come predefinite ({@link #isPredefinita()}) esistono fin dall'inizio della partita: le crea
 * RegistroMissioni.reimposta, nell'ordine di questo elenco, e restano in attesa finché la loro condizione non scatta. Le
 * altre nascono durante il gioco, figlie o affidate da un'altra missione (o nei test). Le missioni a famiglie (incarichi
 * di combattimento, soccorsi, rituali, indagini...) pescano le loro varianti dalle righe della risorsa missioni.txt:
 * aggiungendo righe si creano nuove varianti senza scrivere codice.
 * <p>
 * L'ordine conta: nello stesso controllo le missioni si guardano nell'ordine del registro, e la prima pronta a mostrare
 * un intermezzo entrando in una locazione fa aspettare le altre (per esempio i negozi, che stanno prima degli incarichi
 * in città). Fra gli incarichi in città pronti nella stessa visita invece ne parte uno a caso (vedi
 * missioni.incarichi.SceltaDegliIncarichi). Un incarico ripetibile, finito, lascia la sua copia in fondo al registro.
 *
 * @author Stefano Reksten
 */
public enum MissionePredefinita {

	/**
	 * La missione principale: si attiva al primo controllo prima di una locazione, cioè subito. Il castello del Drago
	 * compare quando le quattro figlie sono completate.
	 */
	SCONFIGGI_IL_DRAGO(true),
	// Le quattro figlie di SCONFIGGI_IL_DRAGO, che le crea con sé. Ciascuna si attiva al primo controllo prima di una
	// locazione in cui riesce a far sorgere il suo castello al posto di un bosco (cioè praticamente subito).
	SCONFIGGI_IL_MINOTAURO_GIGANTE,
	SCONFIGGI_L_IDRA,
	SCONFIGGI_IL_LICH,
	SCONFIGGI_LA_STREGA,

	// Scatta alla fine della prima locazione.
	NESSUN_BOCCALE_LASCIATO_INDIETRO(true),

	// Scatta al primo incontro con un Eremita, appena descritta la locazione. Conta l'incontro, non come va a finire.
	DISTURBATORE_DELLA_QUIETE_PUBBLICA(true),

	// Parte alla terza sfida a carta, forbici e sasso vinta stringendo amicizia. Non è ripetibile.
	LA_SFIDA_DEI_CAMPIONI,
	// Le quattro figlie di LA_SFIDA_DEI_CAMPIONI, che le affida quando parte: un campione da battere ciascuna.
	IL_CAMPIONE,

	// Scatta a un accampamento, se nel gruppo c'è un compagno (non il capo, non a tempo) che
	// non ha ancora chiesto un favore. Ripetibile, dopo 72 ore, con un altro compagno.
	LA_LEALTA(true),

	// Scatta entrando in una locanda nel bosco per la terza volta (la stessa locanda), a una visita tranquilla, se c'è
	// una città ancora in piedi a cui riportare il bardo (a inizio gioco ovviamente si!); una volta sola per partita.
	NON_SPARATE_SUL_PIANISTA(true),
	// Scatta dopo che NON_SPARATE_SUL_PIANISTA è stata completata, entrando in una locanda nel bosco già visitata
	// (non alla prima visita, che ha il benvenuto), a una visita tranquilla, se la città in cui era stato riportato
	// il bardo e almeno un'altra sono ancora in piedi; una sola volta per partita.
	IL_VIAGGIO_DELLA_MOGLIE(true),
	// Scatta entrando in una locanda nel bosco dalla terza visita (la stessa locanda), a una
	// visita tranquilla. Ripetibile, dopo 48 ore.
	LA_BENEDIZIONE(true),

	// Figlia di LA_BENEDIZIONE (in locanda) e di LA_LEALTA (all'accampamento): il favore da fare in cambio.
	IL_FAVORE,

	// Scatta entrando nella prima città.
	CRONACHE_DI_UN_FEGATO_EROICO(true),
	// Le figlie di CRONACHE_DI_UN_FEGATO_EROICO, che le crea con sé: una tappa per città
	VISITA_LOCANDA,

	// Scatta non appena si ripulisce il primo tempio.
	IL_GIRO_DELLE_SETTE_CHIESE(true),

	// Le leggende: l'armaiolo racconta in una città qualsiasi, il locandiere dalla terza visita alla stessa locanda nel
	// bosco. Entrambe a una visita tranquilla, se resta un leggendario da pescare e se nessuna leggenda è stata
	// raccontata nelle ultime 36 ore; sono ripetibili.
	LA_LEGGENDA_DELL_ARMAIOLO(true),
	LA_LEGGENDA_DEL_LOCANDIERE(true),

	// I negozi: a inizio partita ogni città riceve una di queste quattro missioni, a caso; ciascuna si offre come un
	// incarico in città (vedi sotto), ma solo nella sua città, e non si ripete. A completamento si costruisce un nuovo
	// tipo di locazione al posto delle rovine.
	IL_NEGOZIO_DELL_ALCHIMISTA(true),
	IL_NEGOZIO_DELL_ARMAIOLO(true),
	IL_NEGOZIO_DEL_VENDITORE_DI_PERGAMENE(true),
	IL_NEGOZIO_DELL_INCANTATORE(true),

	// Gli incarichi in città (IncaricoInCitta): si offrono entrando in una città qualsiasi non distrutta, a una visita
	// tranquilla (nessun'altra missione mostra un intermezzo entrando lì, per esempio quando si torna a concluderne una).
	// Un incarico solo per visita: se ne sono pronti più d'uno, si sceglie a caso, con il 75% di probabilità fra le classi
	// non ancora affidate nel giro e il 25% fra quelle già affidate (SceltaDegliIncarichi). Sono ripetibili: finiti, bene o male,
	// ne lasciano uno uguale che si offre dopo 48 ore di gioco.
	CACCIA_AI_GOBLIN(true),
	RICHIESTA_ALCHIMISTA(true),
	RICHIESTA_ARMAIOLO(true),
	RICHIESTA_CAPITANO(true),
	RICHIESTA_LOCANDIERE(true),
	LA_TAGLIA_SULLA_BANDA(true),
	IL_PELLEGRINO(true),
	IL_RAPIMENTO(true),
	CACCIATORE_DI_TAGLIE(true),
	IL_CARTOGRAFO(true),
	// Il corriere e il contrabbandiere, solo se c'è un'altra città non distrutta a cui portare la merce
	IL_CORRIERE(true),
	IL_CONTRABBANDIERE(true),
	L_OGGETTO_SMARRITO(true),
	INCARICO_DI_COMBATTIMENTO(true),
	LA_SORVEGLIANZA(true),
	IL_SOCCORSO(true),
	L_INDAGINE(true),
	IL_RITUALE(true),
	LA_DOCUMENTAZIONE(true),
	IL_COLPO(true),
	// Un incarico in città, ma solo se resta un leggendario da mettere in palio, e fra un torneo e l'altro passano 72 ore
	IL_TORNEO(true),

	// Usate dai test, che inventano le loro missioni: non hanno una classe nel gioco
	MISSIONE_DI_PROVA,
	MISSIONE_CHE_FALLISCE;

	private final boolean predefinita;

	MissionePredefinita() {
		this(false);
	}

	MissionePredefinita(boolean predefinita) {
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
	public static MissionePredefinita predefinitaConId(String id) {
		for (MissionePredefinita classe : values()) {
			if (classe.predefinita && classe.name().equals(id)) {
				return classe;
			}
		}
		return null;
	}
}
