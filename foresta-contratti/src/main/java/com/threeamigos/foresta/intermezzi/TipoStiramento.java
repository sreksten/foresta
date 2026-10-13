package com.threeamigos.foresta.intermezzi;

/**
 * Come lo sfondo di una pagina si adatta all'area disponibile: su quali assi viene stirato
 * per riempirla esattamente, anche distorcendo le proporzioni dell'immagine. Gli assi non
 * stirati restano a dimensione nativa, centrati.
 */
public enum TipoStiramento {
	ORIZZONTALE,
	VERTICALE,
	ORIZZONTALE_E_VERTICALE,
	NESSUNO
}
