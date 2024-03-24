package com.threeamigos.foresta.motore;

public enum Comando {

	MASCHIO,
	FEMMINA,

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

	COMBATTIMENTO,
	INCANTESIMO,
	CORRUZIONE,
	AMICIZIA,
	FUGA,

	PERSONAGGIO_1,
	PERSONAGGIO_2,
	PERSONAGGIO_3,
	PERSONAGGIO_4,
	PERSONAGGIO_5,

	ARIA,
	ACQUA,
	TERRA,
	FUOCO,
	FULMINE,
	MORTE,
	RESURREZIONE,
	NO_INCANTESIMO,

	NORD,
	EST,
	SUD,
	OVEST,

	ACCAMPAMENTO,
	FORZA,
	GRANDE_FORZA,
	MAGIA,
	MAPPA,
	FLOPPY,

	NUMERO_1,
	NUMERO_2,
	NUMERO_3,
	NUMERO_4,
	NUMERO_5,

	LOCANDA,
	ALCHIMISTA,
	ESCI_DA_CITTA,

	GRUPPO,
	SINGOLO,

	SI,
	NO,
	
	ANNULLA,

	AIUTO,
	PERGAMENA,

	SU,
	GIU,
	DESTRA,
	SINISTRA,

	TIMER,

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
	
	public static final Comando ofPersonaggio(int personaggio) {
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

	public static final Comando ofNumero(int numero) {
		if (numero == 0) {
			return NUMERO_1;
		} else if (numero == 1) {
			return NUMERO_2;
		} else if (numero == 2) {
			return NUMERO_3;
		} else if (numero == 3) {
			return NUMERO_4;
		} else if (numero == 4) {
			return NUMERO_5;
		}
		throw new IllegalArgumentException();
	}
}
