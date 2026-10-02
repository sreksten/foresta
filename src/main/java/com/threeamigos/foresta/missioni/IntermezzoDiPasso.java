package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;

import java.util.List;

/**
 * L'intermezzo di un passo concluso di una {@link MissioneAPassi} (vedi gestione_missioni.md, §2-3), come lo
 * vede il registro degli intermezzi. Non è in ClasseIntermezzo e non si ricorda in IntermezziMD: il "già mostrato"
 * è una proprietà della missione, così ogni istanza di una missione generata ha il suo.
 */
public final class IntermezzoDiPasso implements Intermezzo {

	private final MissioneAPassi missione;
	private final String idPasso;

	public IntermezzoDiPasso(MissioneAPassi missione, String idPasso) {
		this.missione = missione;
		this.idPasso = idPasso;
	}

	/**
	 * Solo per i log: il registro non usa l'id per ricordare che è scattato.
	 */
	@Override
	public String getId() {
		return missione.getId() + "/" + idPasso;
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return !missione.isIntermezzoPassoMostrato(idPasso) && missione.costruisciPasso(idPasso).getMomentoIntermezzo() == momento;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		return missione.costruisciPasso(idPasso).getPagine();
	}

	/**
	 * Ricorda nella missione che l'intermezzo è stato mostrato.
	 */
	public void segnaMostrato() {
		missione.segnaIntermezzoPassoMostrato(idPasso);
	}

	public MissioneAPassi getMissione() {
		return missione;
	}

	public String getIdPasso() {
		return idPasso;
	}
}
