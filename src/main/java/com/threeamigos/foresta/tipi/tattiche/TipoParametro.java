package com.threeamigos.foresta.tipi.tattiche;

/**
 * Il tipo del parametro di un filtro, di un ordinamento o di un predicato: la lettura controlla che ci sia e sia giusto, e la scheda del
 * giocatore mostra l'editor adatto (un numero, un menu di stati...).
 */
public enum TipoParametro {
	NESSUNO,
	/** Un confronto e un numero: <code>SALUTE MINORE 50</code> */
	CONFRONTO_E_NUMERO,
	/** Solo un confronto: <code>LIVELLO_RISPETTO_AL_MIO MAGGIORE</code> */
	CONFRONTO,
	/** Un effetto di stato (<code>TipoEffettoDiStato</code>) */
	STATO,
	/** Una classe di personaggio, il ruolo (<code>TipoPersonaggio</code>) */
	CLASSE,
	/** Una categoria di creatura (<code>CategoriaPersonaggio</code>) */
	CATEGORIA,
	/** Un tipo di danno (<code>TipoDanno</code>) */
	TIPO_DANNO
}
