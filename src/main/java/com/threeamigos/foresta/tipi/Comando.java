package com.threeamigos.foresta.tipi;

public enum Comando {

	// All'inizio del gioco, per scegliere il sesso del proprio personaggio
	MASCHIO("Crea un personaggio di sesso maschile"),
	FEMMINA("Crea un personaggio di sesso femminile"),

	// All'inizio del gioco, per scegliere la classe del proprio personaggio
	GUERRIERA("Crea una Guerriera"),
	GUERRIERO("Crea un Guerriero"),
	LADRA("Crea una Ladra"),
	LADRO("Crea un Ladro"),
	BARDO("Crea un Bardo"),
	CANTASTORIE("Crea una Cantastorie"),
	ELFA("Crea un'Elfa"),
	ELFO("Crea un Elfo"),
	MAGA("Crea una Maga"),
	MAGO("Crea un Mago"),

	// Le scelte tipiche all'interno di una locazione standard
	SINGOLO_ATTACCO("Esegue un singolo attacco"),
	COMBATTIMENTO("Combatte fino alla fine di uno dei due contendenti"),
	INTERRUZIONE_COMBATTIMENTO("Interrompe il combattimento in corso"),
	INCANTESIMO("Formula un incantesimo"),
	CORRUZIONE("Esegue un tentativo di corruzione"),
	AMICIZIA("Prova a stringere amicizia"),
	FUGA("Fugge a gambe levate"),
	PASSA_INOSSERVATO("Cerca di passare inosservato"),

	// Quando occorre scegliere un particolare componente del gruppo
	PERSONAGGIO_1("Il primo personaggio del gruppo"),
	PERSONAGGIO_2("Il secondo personaggio del gruppo"),
	PERSONAGGIO_3("Il terzo personaggio del gruppo"),
	PERSONAGGIO_4("Il quarto personaggio del gruppo"),
	PERSONAGGIO_5("Il quinto personaggio del gruppo"),
	PERSONAGGIO_6("Il sesto personaggio del gruppo"),
	PERSONAGGIO_7("Il settimo personaggio del gruppo"),
	PERSONAGGIO_8("L'ottavo personaggio del gruppo"),

	// Selezione di un tipo di incantesimo
	ARIA("Lancia un incantesimo di Aria"),
	ACQUA("Lancia un incantesimo di Acqua"),
	TERRA("Lancia un incantesimo di Terra"),
	FUOCO("Lancia un incantesimo del Fuoco"),
	FULMINE("Lancia un incantesimo del Fulmine"),
	GELO("Lancia un incantesimo del Gelo"),
	VELENO("Lancia un incantesimo di Veleno"),
	MORTE("Lancia un incantesimo di Morte"),
	RESURREZIONE("Lancia un incantesimo di Resurrezione"),
	ALBA_SACRA("Lancia un incantesimo di Alba Sacra"),
	// L'incantesimo innato di Mago ed Elfo, che non consuma pergamene
	DARDO_ARCANO("Lancia un Dardo Arcano"),
	// Annulla la scelta di un incantesimo
	NO_INCANTESIMO("Annulla il lancio di un incantesimo"),

	// Direzione verso la quale muoversi
	NORD("Muove verso Nord"),
	EST("Muove verso Est"),
	SUD("Muove verso Sud"),
	OVEST("Muove verso Ovest"),

	// Scelte possibili al completamento di una locazione
	ACCAMPAMENTO("Il gruppo si accampa per la notte"),
	POZIONE_SALUTE("Consuma una pozione della Salute"),
	POZIONE_SALUTE_GRANDE("Consuma una pozione della Salute, grande"),
	POZIONE_MAGIA("Consuma una pozione della Magia"),
	POZIONE_MAGIA_GRANDE("Consuma una pozione della Magia, grande"),
	MAPPA("Consulta la mappa della Foresta"),
	INVENTARIO("Consulta l'inventario del gruppo"),
	// Nell'intro, per riprendere una partita salvata; in gioco, per salvare
	FLOPPY_CARICA("Continua una partita"),
	FLOPPY_SALVA("Salva la partita"),

	// Numero di passi di cui muoversi, o scelta di uno slot di salvataggio
	NUMERO_1,
	NUMERO_2,
	NUMERO_3,
	NUMERO_4,
	NUMERO_5,

	// Scelte possibili all'interno di una città
	LOCANDA("Visita la Locanda cittadina"),
	NEGOZIO_ALCHIMISTA("Visita l'alchimista"),
	NEGOZIO_ARMAIOLO("Visita l'armaiolo"),
	NEGOZIO_VENDITORE_DI_PERGAMENE("Visita il venditore di pergamene"),
	NEGOZIO_INCANTATORE("Visita l'incantatore"),
	// Nella bottega dell'incantatore, conferma la fusione
	FUSIONE("Esegue la fusione"),
	ESCI_DA_CITTA("Esce dalla città"),

	// Dall'alchimista, sceglie se agire su un personaggio o su tutto il gruppo
	GRUPPO("Tutto il gruppo"),
	SINGOLO("Singolo personaggio"),

	SI("Si"),
	NO("No"),
	ANNULLA("Annulla"),

	AIUTO("Mostra l'aiuto"),
	NO_AIUTO("Disabilita l'aiuto"),

	// Dall'inventario, la pagina dei trofei
	MOSTRA_TROFEI("Mostra i trofei vinti"),

	// Usata per chiedere conferma all'utente prima di andare avanti
	PERGAMENA("Avanti"),

	// In caso di mancanza di posto per l'elenco delle possibili azioni, appaiono due frecce
	// agli estremi (a seconda del layout di visualizzazione)
	SU,
	GIU,
	DESTRA,
	SINISTRA,

	CARTA("Carta"),
	FORBICE("Forbice"),
	SASSO("Sasso"),

	// Azione automaticamente generata dal sistema quando si è in una locazione, per le animazioni
	TIMER,

	// Top secret
	RUTTOLOMEO,
	STORPSGORBLIN;

	public static final int MAX_MOVIMENTO = 5;

	public static final Comando of(int ordinal) {
		for (Comando azioneCorrente : Comando.values()) {
			if (azioneCorrente.ordinal() == ordinal) {
				return azioneCorrente;
			}
		}
		throw new IllegalArgumentException();
	}

	private final String descrizione;

	Comando() {
		this.descrizione = null;
	}

	Comando(String descrizione) {
		this.descrizione = descrizione;
	}
	
	public String getDescrizione() {
		return descrizione;
	}

	/**
	 * Se il comando indica uno dei personaggi del gruppo (PERSONAGGIO_1 ... PERSONAGGIO_8).
	 */
	public boolean isPersonaggio() {
		return ordinal() >= PERSONAGGIO_1.ordinal() && ordinal() <= PERSONAGGIO_8.ordinal();
	}

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
