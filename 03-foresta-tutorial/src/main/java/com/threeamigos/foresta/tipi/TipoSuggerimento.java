package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

/**
 * I suggerimenti del tutorial, nell'ordine in cui si danno quando più di uno scatta nella stessa situazione (vedi {@code tutorial.md} §4). Ognuno
 * dice quando scatta (la {@link Situazione} che lo innesca), a che cosa punta (il {@link BersaglioDelSuggerimento}) e ha le sue pagine di testo
 * (un file per lingua, {@code tipo_suggerimento_it.properties}, chiavi {@code COSTANTE.PAGINA_1}, {@code COSTANTE.PAGINA_2}...; la variante per il
 * dito è {@code COSTANTE.PAGINA_1.TOCCO}, e se manca vale quella normale). Una pagina per ogni cosa che si può fare, al massimo
 * {@value #PAGINE_NORMALI} (gli elenchi dei luoghi e delle botteghe fanno eccezione, vedi {@link #PAGINE_MASSIME}). A ogni pagina può puntare qualcosa di diverso.
 *
 * @author Stefano Reksten
 */
public enum TipoSuggerimento {

	SCELTA_DELLA_CLASSE(Situazione.SCELTA_DELLA_CLASSE, BersaglioDelSuggerimento.BARRA_DELLE_ICONE),
	PRIMI_PASSI(Situazione.PRIMA_SCHERMATA_DI_GIOCO, BersaglioDelSuggerimento.RIQUADRI),
	// I riquadri prima della barra: il benvenuto, poi il riquadro delle missioni (e come si scorre), e solo dopo la barra delle icone sotto
	MISSIONI(Situazione.MISSIONE_ATTIVA, BersaglioDelSuggerimento.RIQUADRO_DELLE_MISSIONI),
	// La prima pagina parla della barra, la seconda del punto interrogativo
	BARRA_DELLE_ICONE(Situazione.BARRA_DELLE_ICONE_VISIBILE, BersaglioDelSuggerimento.BARRA_DELLE_ICONE, BersaglioDelSuggerimento.ICONA_DELL_AIUTO),
	// Il primo scontro e quel che ne viene, nell'ordine: lo scontro (un attacco alla volta), l'effetto di stato del secondo colpo, il combattimento
	// automatico, l'oggetto trovato; solo dopo si parla di spostarsi (le icone di movimento aspettano l'oggetto: vedi SituazioniDellaUI)
	COMBATTIMENTO(Situazione.COMBATTIMENTO_APERTO, BersaglioDelSuggerimento.RIQUADRO_DEL_COMBATTIMENTO),
	// La prima volta che un effetto di stato si applica, in pieno combattimento: il combattimento si ferma finché non si legge
	EFFETTI_DI_STATO(Situazione.EFFETTO_DI_STATO_APPLICATO, BersaglioDelSuggerimento.RIQUADRO_DEL_COMBATTIMENTO),
	// Il primo scontro si fa un attacco alla volta, senza l'icona del combattimento automatico: compare quando l'effetto di stato è arrivato, e questo la
	// spiega subito dopo aver letto quello (vedi PilotaggioDelTutorial.isCombattimentoAutomaticoVietato)
	COMBATTIMENTO_AUTOMATICO(Situazione.COMBATTIMENTO_AUTOMATICO_DISPONIBILE, BersaglioDelSuggerimento.ICONA_DEL_COMBATTIMENTO_AUTOMATICO),
	// La prima pagina dice dell'oggetto e di chi lo prende (punta il gruppo: a fine scontro la barra non ha ancora le icone, e il suggerimento deve aprirsi
	// prima dell'eremita), la seconda che può andare nell'inventario (punta la sua icona)
	OGGETTO_TROVATO(Situazione.OGGETTO_TROVATO, BersaglioDelSuggerimento.RIQUADRO_DEL_GRUPPO, BersaglioDelSuggerimento.ICONA_DELL_INVENTARIO),
	// La prima volta che la barra chiede di scegliere fra più personaggi chi compie l'azione; dopo l'oggetto trovato, che dice già che si sceglie chi lo prende
	SCELTA_DEL_PERSONAGGIO(Situazione.SCELTA_DEL_PERSONAGGIO, BersaglioDelSuggerimento.ICONE_DEI_PERSONAGGI),
	// Un personaggio è salito di livello: la prima pagina mostra il riquadro del gruppo, dove si vede, la seconda l'icona che apre l'inventario, dove si spende il punto
	LIVELLO_SALITO(Situazione.PUNTI_ABILITA_DISPONIBILI, BersaglioDelSuggerimento.RIQUADRO_DEL_GRUPPO, BersaglioDelSuggerimento.ICONA_DELL_INVENTARIO),
	// La prima pagina parla delle frecce, la seconda del riquadro della mappa, la terza dell'icona che apre la mappa a tutto schermo
	MOVIMENTO_E_MAPPA(Situazione.ICONE_DI_MOVIMENTO_PRESENTI, BersaglioDelSuggerimento.ICONE_DI_MOVIMENTO, BersaglioDelSuggerimento.RIQUADRO_DELLA_MAPPA,
			BersaglioDelSuggerimento.ICONA_DELLA_MAPPA),
	// Questi due si danno aprendo la mappa a tutto schermo (vedi SchermoDelGioco.isSchermataDelSuggerimento)
	JOYSTICK_DELLA_MAPPA(Situazione.JOYSTICK_SULLA_MAPPA, BersaglioDelSuggerimento.JOYSTICK_VIRTUALE),
	TRASCINARE_LA_MAPPA(Situazione.MAPPA_PIU_GRANDE_DELLO_SCHERMO, BersaglioDelSuggerimento.SCHERMATA),
	// I tipi di luogo: più pagine del solito, perché sono tanti e chi comincia non sa dove andare (vedi PAGINE_MASSIME). Le botteghe no: all'inizio stanno
	// solo in città, e le spiega BOTTEGHE quando ci si arriva
	LUOGHI(Situazione.ICONA_DELLA_MAPPA_PRESENTE, BersaglioDelSuggerimento.RIQUADRO_DELLA_MAPPA),
	POZIONI(Situazione.FERITO_CON_UNA_POZIONE, BersaglioDelSuggerimento.ICONE_DELLE_POZIONI),
	// La prima reazione elementale: un effetto di stato più un certo tipo di attacco fanno più danni
	REAZIONI_ELEMENTALI(Situazione.REAZIONE_ELEMENTALE_SCATENATA, BersaglioDelSuggerimento.RIQUADRO_DEL_COMBATTIMENTO),
	// A fine combattimento, se qualcuno è caduto: la prima pagina parla del gruppo, la seconda dell'incantesimo, la terza dell'icona che lo lancia
	RESURREZIONE(Situazione.PERSONAGGIO_CADUTO, BersaglioDelSuggerimento.RIQUADRO_DEL_GRUPPO, BersaglioDelSuggerimento.RIQUADRO_DEGLI_INCANTESIMI_E_DELLE_POZIONI,
			BersaglioDelSuggerimento.ICONA_DEGLI_INCANTESIMI),
	SCELTE_IN_COMBATTIMENTO(Situazione.SCELTE_IN_COMBATTIMENTO, BersaglioDelSuggerimento.ICONE_DI_COMBATTIMENTO),
	// L'ultimo della prima locazione: letto come ci si muove, per i prossimi passi si va verso i boschi e le radure; punta le frecce. Sta dopo gli altri nell'ordine
	// dell'enum, così se più d'uno scatta insieme viene per ultimo
	AVANZA_VERSO_I_BOSCHI(Situazione.MOVIMENTO_SPIEGATO, BersaglioDelSuggerimento.ICONE_DI_MOVIMENTO),
	// Il recupero fra le locazioni: il motore dice quando è avvenuto (NotificaMessaggioDelTutorial), la prima volta che qualcuno era ferito o aveva speso
	// magia; viene dopo il movimento e il consiglio dei boschi. Punta alle barre del gruppo
	RECUPERO_FRA_LOCAZIONI(Situazione.RECUPERO_FRA_LOCAZIONI, BersaglioDelSuggerimento.RIQUADRO_DEL_GRUPPO),
	// Le tattiche si aprono dall'inventario: il suggerimento punta alla sua icona, perché uno sia curioso di entrarci
	TATTICHE(Situazione.TATTICHE_DISPONIBILI, BersaglioDelSuggerimento.ICONA_DELL_INVENTARIO),
	// Si dà aprendo l'inventario: la prima pagina parla delle due liste, la seconda delle icone delle altre pagine
	INVENTARIO(Situazione.INVENTARIO_APERTO, BersaglioDelSuggerimento.SCHERMATA, BersaglioDelSuggerimento.ICONE_DELLE_PAGINE),
	// Nell'inventario, quando qualcuno ha punti abilità, prima di dire come si spendono: che cosa sono gli attributi (primari e secondari) e che gli artefatti
	// li aumentano
	ATTRIBUTI(Situazione.ATTRIBUTI_DA_INTRODURRE, BersaglioDelSuggerimento.SCHERMATA, BersaglioDelSuggerimento.SCHERMATA),
	// Nell'inventario, quando qualcuno ha punti abilità: gli attributi primari in arancione si aumentano con un doppio click
	PUNTI_ABILITA(Situazione.PUNTI_ABILITA_DA_SPENDERE, BersaglioDelSuggerimento.SCHERMATA),
	// La schermata delle tattiche, spiegata tutta alla prima apertura (vedi tutorial_tattiche.md): leggere una tattica (pagine 1-5: l'ordine, le icone e i
	// colori, il riquadro della spiegazione), su chi, quali e quale (6-8), quando e che cosa (9-10), provare e confermare (11-12). Dodici pagine, come
	// l'elenco dei luoghi è una deroga alla regola delle tre
	SCHERMATA_DELLE_TATTICHE(Situazione.TATTICHE_APERTE,
			BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE, BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE, BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE,
			BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE, BersaglioDelSuggerimento.SPIEGAZIONE_DELLE_TATTICHE,
			BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE, BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE, BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE,
			BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE, BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE,
			BersaglioDelSuggerimento.PULSANTI_DELLE_TATTICHE, BersaglioDelSuggerimento.LISTA_DELLE_TATTICHE),
	// Non scatta da una situazione: il motore lo chiede (NotificaMessaggioDelTutorial) quando rifiuta un viaggio che finirebbe in un luogo non ancora per
	// il giocatore («Non è ancora il momento di visitare {0}»); punta alle frecce, per scegliere un'altra direzione
	NON_ANCORA_IL_MOMENTO(Situazione.MESSAGGIO_DEL_MOTORE, BersaglioDelSuggerimento.ICONE_DI_MOVIMENTO),
	CORRUZIONE(Situazione.AVVERSARIO_CORROMPIBILE, BersaglioDelSuggerimento.ICONA_DELLA_CORRUZIONE),
	AMICIZIA(Situazione.AVVERSARIO_AMICHEVOLE, BersaglioDelSuggerimento.ICONA_DELL_AMICIZIA),
	// La prima pagina parla dell'icona che apre l'elenco (incantesimi e abilità innate), la seconda del riquadro con le scorte
	INCANTESIMI(Situazione.INCANTESIMO_LANCIABILE, BersaglioDelSuggerimento.ICONA_DEGLI_INCANTESIMI,
			BersaglioDelSuggerimento.RIQUADRO_DEGLI_INCANTESIMI_E_DELLE_POZIONI),
	// Arrivati in città: la prima pagina dà il benvenuto, le altre quattro (una per bottega, con la sua tessera, come l'elenco dei luoghi) dicono che cosa fa
	// ognuna
	BOTTEGHE(Situazione.INGRESSO_IN_BOTTEGA, BersaglioDelSuggerimento.ICONE_DELLE_BOTTEGHE),
	// La prima volta che in una locanda qualcuno chiede di unirsi al gruppo: il motore lo dice (NotificaMessaggioDelTutorial) prima di chiedere sì o no.
	// La prima pagina dice che entra nel gruppo per sempre, la seconda che le locande costano di più ma conviene lo stesso
	COMPAGNO_IN_LOCANDA(Situazione.COMPAGNO_IN_LOCANDA, BersaglioDelSuggerimento.ICONE_SI_NO),
	ACCAMPAMENTO(Situazione.NOTTE, BersaglioDelSuggerimento.ICONA_DELL_ACCAMPAMENTO),
	GESTI_SUL_TOCCO(Situazione.DITO_SULLO_SCHERMO, BersaglioDelSuggerimento.BARRA_DELLE_ICONE);

	/**
	 * Il massimo di pagine che un suggerimento può avere. Di norma sono al massimo {@value #PAGINE_NORMALI} (oltre, il concetto si spezza in due
	 * suggerimenti); gli elenchi dei luoghi e delle botteghe e la schermata delle tattiche ne hanno di più.
	 */
	public static final int PAGINE_MASSIME = 12;
	/** Il massimo di pagine di un suggerimento che non sia una delle eccezioni (luoghi, botteghe, tattiche). */
	public static final int PAGINE_NORMALI = 3;

	private final Situazione innesco;
	private final BersaglioDelSuggerimento[] bersagli;

	/**
	 * @param bersagli a che cosa punta la coda, pagina per pagina; l'ultimo vale anche per le pagine che seguono
	 */
	TipoSuggerimento(Situazione innesco, BersaglioDelSuggerimento... bersagli) {
		this.innesco = innesco;
		this.bersagli = bersagli;
	}

	/**
	 * La situazione in cui scatta.
	 */
	public Situazione getInnesco() {
		return innesco;
	}

	/**
	 * A che cosa punta il fumetto nella prima pagina: è quello che deve essere a schermo perché il suggerimento si apra.
	 */
	public BersaglioDelSuggerimento getBersaglio() {
		return bersagli[0];
	}

	/**
	 * A che cosa punta il fumetto in quella pagina (da 1): la pagina che parla dell'icona della mappa punta alla mappa, non alle frecce.
	 */
	public BersaglioDelSuggerimento getBersaglio(int pagina) {
		return bersagli[Math.max(0, Math.min(pagina, bersagli.length) - 1)];
	}

	/**
	 * Quante pagine ha nella lingua in uso (da 1 a {@value #PAGINE_MASSIME}).
	 */
	public int getNumeroPagine() {
		int pagine = 0;
		while (pagine < PAGINE_MASSIME && Testi.testo(this, "PAGINA_" + (pagine + 1)).isPresent()) {
			pagine++;
		}
		return pagine;
	}

	/**
	 * Il titolo di una pagina (da 1), se ne ha uno: va in una riga in cima, accanto all'icona (vedi {@link #getIcona}). Sta nel file dei testi,
	 * alla voce {@code PAGINA_n.TITOLO}.
	 */
	public java.util.Optional<String> getTitolo(int pagina) {
		return Testi.testo(this, "PAGINA_" + pagina + ".TITOLO");
	}

	/**
	 * L'icona di una pagina (da 1), se ne ha una: il nome della locazione (una costante di TipoLocazione, per esempio {@code LOCANDA}) di cui la pagina
	 * parla, di cui la UI disegna la tessera che ha sulla mappa. Sta nel file dei testi, alla voce {@code PAGINA_n.ICONA}.
	 */
	public java.util.Optional<String> getIcona(int pagina) {
		return Testi.testo(this, "PAGINA_" + pagina + ".ICONA");
	}

	/**
	 * Il testo di una pagina (da 1), nella variante per il dito se {@code dito} è vero e la pagina ne ha una.
	 */
	public String getPagina(int pagina, boolean dito) {
		String voce = "PAGINA_" + pagina;
		if (dito) {
			java.util.Optional<String> perIlDito = Testi.testo(this, voce + ".TOCCO");
			if (perIlDito.isPresent()) {
				return perIlDito.get();
			}
		}
		return Testi.testoOCostante(this, voce);
	}
}
