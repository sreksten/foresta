package com.threeamigos.foresta.tipi;

/**
 * Le classi dei personaggi, giocanti e mostri: l'identificativo che salva il modello dati (PersonaggioMD,
 * StatisticheMD...) e che leggono motore e UI. Il personaggio vero, i suoi nomi e quanti se ne incontrano al massimo
 * in una locazione li dà personaggi.FabbricaPersonaggi.
 */
public enum TipoPersonaggio {

	ARPIA,
	CENTAURO,
	CHIMERA,
	CHIMERA_DRAGO,
	DRAGO,
	EREMITA,
	FANTASMA,
	FOLLETTO,
	GARGOYLE,
	GIGANTE,
	GOBLIN,
	HOBGOBLIN,
	IDRA,
	LICH,
	MINOTAURO,
	MINOTAURO_GIGANTE,
	OMBRA_NERA,
	SCHELETRO,
	SPETTRO,
	SPIRITO,
	STREGA,
	TITANO,
	TROLL,
	VIVERNA,

	BARDO,
	CANTASTORIE,
	ELFA,
	ELFO,
	GUERRIERA,
	GUERRIERO,
	LADRA,
	LADRO,
	MAGA,
	MAGO,
	OMBRAFIAMMA,

	// Solo per le missioni di scorta: non si incontra e non si recluta
	VIANDANTE;
}
