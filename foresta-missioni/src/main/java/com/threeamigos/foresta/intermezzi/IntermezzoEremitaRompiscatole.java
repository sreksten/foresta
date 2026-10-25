package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.Verso;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Collections;
import java.util.List;

/**
 * L'ultimo eremita disturbato dice al gruppo quello che pensa. Scatta una volta sola, a fine locazione, quando la
 * missione DisturbatoreDellaQuietePubblica è completa: la missione si completa all'incontro, quindi l'eremita è ancora
 * nella locazione.
 */
public class IntermezzoEremitaRompiscatole implements Intermezzo {

	private static final String EROE = "eroe";
	private static final String EREMITA = "eremita";

	@Override
	public String getId() {
		return TipoIntermezzo.INTERMEZZO_EREMITA_ROMPISCATOLE.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.LOCAZIONE_COMPLETATA && RegistroMissioni.getMissioniCompletate().stream()
				.anyMatch(missione -> missione.getClasse() == MissionePredefinita.DISTURBATORE_DELLA_QUIETE_PUBBLICA);
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		TipoLocazione sfondo = Foresta.getLocazioneDelGruppo();
		return Collections.singletonList(new PaginaIntermezzo("Dieci eremiti angustiati.")
				.conSfondo(ImmagineIntermezzo.locazione(sfondo))
				.conElemento(ElementoIntermezzo.personaggio(EREMITA, TipoPersonaggio.EREMITA, 1.1, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.SINISTRA)
						.poi(Tappa.inSecondi(2).verso(0.62, 0.61)))
				.conElemento(ElementoIntermezzo.personaggio(EROE, GruppoGiocatore.getIstanza().getCapo().getClasse(), 0.38, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.DESTRA))
				.conBattuta(BattutaIntermezzo.di(EREMITA, "Questa foresta era un luogo tranquillo prima che voi maledetti rompiscatole arrivaste!")
						.daSecondo(2)));
	}
}
