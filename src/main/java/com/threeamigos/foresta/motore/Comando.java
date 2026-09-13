package com.threeamigos.foresta.motore;

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

	// Le scelte tipiche all'interno di una locazione standard
	COMBATTIMENTO,
	INTERRUZIONE_COMBATTIMENTO,
	INCANTESIMO,
	CORRUZIONE,
	AMICIZIA,
	FUGA,

	// Quando occorre scegliere un particolare componente del gruppo
	PERSONAGGIO_1,
	PERSONAGGIO_2,
	PERSONAGGIO_3,
	PERSONAGGIO_4,
	PERSONAGGIO_5,

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
	// Annulla la scelta di un incantesimo
	NO_INCANTESIMO,

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
	MAPPA,
	INVENTARIO,
	FLOPPY,

	// Numero di passi di cui muoversi, o scelta di uno slot di salvataggio
	NUMERO_1,
	NUMERO_2,
	NUMERO_3,
	NUMERO_4,
	NUMERO_5,

	// Scelte possibili all'interno di una città
	LOCANDA,
	ALCHIMISTA,
	ARMAIOLO,
	ESCI_DA_CITTA,

	// Dall'alchimista, sceglie se agire su un personaggio o su tutto il gruppo
	GRUPPO,
	SINGOLO,

	SI,
	NO,
	ANNULLA,

	AIUTO,

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

	public static final Comando of(int ordinal) {
		for (Comando azioneCorrente : Comando.values()) {
			if (azioneCorrente.ordinal() == ordinal) {
				return azioneCorrente;
			}
		}
		throw new IllegalArgumentException();
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
		}
		throw new IllegalArgumentException();
	}

}
