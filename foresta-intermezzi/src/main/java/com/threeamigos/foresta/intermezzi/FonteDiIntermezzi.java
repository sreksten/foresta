package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

/**
 * Chi ha intermezzi suoi, oltre a quelli fissi di {@link TipoIntermezzo}: le missioni, con gli intermezzi dei loro passi. Gli intermezzi
 * non sanno che le missioni esistono: è chi sta sopra a dirlo al registro (vedi {@link RegistroIntermezzi#aggiungiFonte}).
 *
 * @author Stefano Reksten
 */
public interface FonteDiIntermezzi {

	/**
	 * Il primo intermezzo che la fonte vuole mostrare in quel momento, o null. Scatta dopo quelli fissi e prima di quelli di ripiego.
	 */
	Intermezzo getProssimo(MomentoIntermezzo momento);

	/**
	 * Se la fonte avrà un intermezzo da mostrare in quel momento, anche se non è ancora pronto (per esempio un passo che si conclude
	 * proprio adesso): chi scatterebbe al suo posto può farsi da parte (vedi IntermezzoAccampamento).
	 */
	boolean haUnIntermezzoInArrivo(MomentoIntermezzo momento);

	/**
	 * Se la fonte tiene per sé la scena dell'intermezzo fisso di quel tipo: una storia che si svolge proprio lì, e con cui l'intermezzo
	 * fisso non può convivere (vedi IntermezzoLocandaPrimaVisita).
	 */
	default boolean blocca(TipoIntermezzo tipo) {
		return false;
	}
}
