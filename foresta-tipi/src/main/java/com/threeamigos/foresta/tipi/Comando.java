package com.threeamigos.foresta.tipi;

/**
 * Tutto ciò che il giocatore può comandare (le scelte nel menu, i personaggi da selezionare, le direzioni, le mosse di
 * carta, forbici e sasso) più alcuni comandi generati dal sistema, come {@link #TIMER}. La UI li manda al motore come
 * eventi (vedi comandigiocatore). Molti hanno una {@link #getDescrizione() descrizione} da mostrare, scritta in un file
 * di testi per lingua e non qui, in modo che il gioco si possa tradurre.
 *
 * @author Stefano Reksten
 */
public enum Comando {

	// All'inizio del gioco, per scegliere il sesso del proprio personaggio
	MASCHIO,
	FEMMINA,

	// All'inizio del gioco, per scegliere la classe del proprio personaggio
	GUERRIERA,
	GUERRIERO,
	LADRA,
	LADRO,
	BARDO,
	CANTASTORIE,
	ELFA,
	ELFO,
	MAGA,
	MAGO,
	SACERDOTESSA,
	SACERDOTE,

	// Le scelte tipiche all'interno di una locazione standard
	SINGOLO_ATTACCO,
	COMBATTIMENTO,
	INTERRUZIONE_COMBATTIMENTO,
	INCANTESIMO,
	CORRUZIONE,
	AMICIZIA,
	FUGA,
	PASSA_INOSSERVATO,

	// Quando occorre scegliere un particolare componente del gruppo
	PERSONAGGIO_1,
	PERSONAGGIO_2,
	PERSONAGGIO_3,
	PERSONAGGIO_4,
	PERSONAGGIO_5,
	PERSONAGGIO_6,
	PERSONAGGIO_7,
	PERSONAGGIO_8,

	// Selezione di un tipo di incantesimo
	ARIA,
	ACQUA,
	TERRA,
	FUOCO,
	FULMINE,
	GELO,
	VELENO,
	MORTE,
	RESURREZIONE,
	ALBA_SACRA,
	SILENZIO,
	LUCE_DIVINA,
	// L'incantesimo innato di Mago ed Elfo, che non consuma pergamene
	DARDO_ARCANO,
	// I canti innati del Bardo e della Cantastorie, che non consumano pergamene
	CANTO_CURATIVO,
	CANTO_IPNOTICO,
	CANTO_DI_VALORE,
	// Il canto che il bardo della locanda insegna al gruppo (vedi IntermezzoUrloDellaBanshee)
	URLO_DELLA_BANSHEE,

	// Direzione verso la quale muoversi
	NORD,
	EST,
	SUD,
	OVEST,

	// Scelte possibili al completamento di una locazione
	ACCAMPAMENTO,
	POZIONE_SALUTE,
	POZIONE_SALUTE_GRANDE,
	POZIONE_MAGIA,
	POZIONE_MAGIA_GRANDE,
	POZIONE_AUMENTO_LIVELLO,
	MAPPA,
	INVENTARIO,
	// Nell'intro, per riprendere una partita salvata; in gioco, per salvare
	FLOPPY_CARICA,
	FLOPPY_SALVA,

	// Numero di passi di cui muoversi, o scelta di uno slot di salvataggio
	NUMERO_1,
	NUMERO_2,
	NUMERO_3,
	NUMERO_4,
	NUMERO_5,
	// Lo slot del salvataggio automatico quando l'app passa in background: non si sceglie con le icone
	SALVATAGGIO_AUTOMATICO,

	// Scelte possibili all'interno di una città
	LOCANDA,
	NEGOZIO_ALCHIMISTA,
	NEGOZIO_ARMAIOLO,
	NEGOZIO_VENDITORE_DI_PERGAMENE,
	NEGOZIO_INCANTATORE,
	// Nella bottega dell'incantatore, conferma la fusione
	FUSIONE,
	ESCI_DA_CITTA,

	// Dall'alchimista, sceglie se agire su un personaggio o su tutto il gruppo
	GRUPPO,
	SINGOLO,

	SI,
	NO,
	ANNULLA,

	AIUTO,
	NO_AIUTO,

	// Dall'inventario, la pagina dei trofei
	MOSTRA_TROFEI,
	// Dall'inventario, la schermata delle tattiche di combattimento
	MOSTRA_TATTICHE,

	// Usata per chiedere conferma all'utente prima di andare avanti
	PERGAMENA,

	// In caso di mancanza di posto per l'elenco delle possibili azioni, appaiono due frecce
	// agli estremi (a seconda del layout di visualizzazione)
	SU,
	GIU,
	DESTRA,
	SINISTRA,

	CARTA,
	FORBICE,
	SASSO,

	// Azione automaticamente generata dal sistema quando si è in una locazione, per le animazioni
	TIMER,

	// Top secret
	RUTTOLOMEO,
	STORPSGORBLIN;

	public static final int MAX_MOVIMENTO = 5;

	/**
	 * Il comando che ha quella posizione nell'elenco.
	 *
	 * @throws IllegalArgumentException se la posizione non esiste
	 */
	public static final Comando of(int ordinal) {
		for (Comando azioneCorrente : Comando.values()) {
			if (azioneCorrente.ordinal() == ordinal) {
				return azioneCorrente;
			}
		}
		throw new IllegalArgumentException();
	}

	private static final String FILE_DESCRIZIONI = "comandi";

	/**
	 * Quello che il comando fa, per il cartiglio della barra delle icone ("Salva la partita"), nella lingua dei testi
	 * (vedi {@link Testi}); null per i comandi che non hanno una descrizione (gli slot numerici, le frecce di
	 * scorrimento, il timer). Le descrizioni stanno nel file {@code comandi_<lingua>.properties}, non qui.
	 */
	public String getDescrizione() {
		return Testi.get(FILE_DESCRIZIONI, name()).orElse(null);
	}

	/**
	 * Se il comando indica uno dei personaggi del gruppo (PERSONAGGIO_1 ... PERSONAGGIO_8).
	 */
	public boolean isPersonaggio() {
		return ordinal() >= PERSONAGGIO_1.ordinal() && ordinal() <= PERSONAGGIO_8.ordinal();
	}

	/**
	 * Il comando che sceglie il personaggio in quella posizione del gruppo, contando da 0.
	 *
	 * @throws IllegalArgumentException se la posizione non è fra 0 e 7
	 */
	public static Comando ofPersonaggio(int personaggio) {
		if (personaggio == 0) {
			return PERSONAGGIO_1;
		} else if (personaggio == 1) {
			return PERSONAGGIO_2;
		} else if (personaggio == 2) {
			return PERSONAGGIO_3;
		} else if (personaggio == 3) {
			return PERSONAGGIO_4;
		} else if (personaggio == 4) {
			return PERSONAGGIO_5;
		} else if (personaggio == 5) {
			return PERSONAGGIO_6;
		} else if (personaggio == 6) {
			return PERSONAGGIO_7;
		} else if (personaggio == 7) {
			return PERSONAGGIO_8;
		}
		throw new IllegalArgumentException();
	}

}
