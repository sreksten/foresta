package com.threeamigos.foresta.tipi.tattiche;

/**
 * Su che cosa agisce un'azione, e quindi come usa il selettore della voce (tattiche_di_combattimento_automatiche.md §11.2).
 */
public enum PortataAzione {
	/** Un avversario: il selettore sceglie il bersaglio */
	AVVERSARIO,
	/** Un alleato (anche chi decide): il selettore sceglie il bersaglio */
	ALLEATO,
	/** Un alleato caduto: il selettore sceglie il bersaglio */
	ALLEATO_CADUTO,
	/** Tutto il gruppo alleato: il selettore è solo un test di esistenza */
	GRUPPO_ALLEATI,
	/** Tutti gli avversari: il selettore è solo un test di esistenza */
	GRUPPO_AVVERSARI,
	/** Un'arma: il selettore (sempre SE_STESSO) dice chi cambia arma */
	ARMA
}
