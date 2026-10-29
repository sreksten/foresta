package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.FonteDiIntermezzi;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

/**
 * Gli intermezzi delle missioni, come li vede il registro degli intermezzi: quelli dei passi conclusi delle missioni a passi
 * ({@link IntermezzoDiPasso}), e le scene che una missione tiene per sé (vedi {@link MissioneAPassi#bloccaIntermezzo}).
 *
 * @author Stefano Reksten
 */
public final class IntermezziDelleMissioni implements FonteDiIntermezzi {

	public static final IntermezziDelleMissioni ISTANZA = new IntermezziDelleMissioni();

	private IntermezziDelleMissioni() {
	}

	/**
	 * L'intermezzo del primo passo concluso, in qualunque missione (anche già completata: il suo
	 * ultimo passo può avere un intermezzo), che aspetta di essere mostrato in quel momento.
	 */
	@Override
	public Intermezzo getProssimo(MomentoIntermezzo momento) {
		for (Missione missione : RegistroMissioni.getTutteLeMissioni()) {
			if (missione instanceof MissioneAPassi) {
				MissioneAPassi missioneAPassi = (MissioneAPassi) missione;
				String idPasso = missioneAPassi.getPassoConIntermezzoInAttesa(momento);
				if (idPasso != null) {
					return new IntermezzoDiPasso(missioneAPassi, idPasso);
				}
			}
		}
		return null;
	}

	/**
	 * Se una missione a passi ha un intermezzo in attesa per quel momento, o ha un passo che si conclude nel controllo di quel
	 * momento con un intermezzo.
	 */
	@Override
	public boolean haUnIntermezzoInArrivo(MomentoIntermezzo momento) {
		MomentoControllo controllo = controlloDi(momento);
		for (Missione missione : RegistroMissioni.getTutteLeMissioni()) {
			if (missione instanceof MissioneAPassi && ((MissioneAPassi) missione).haUnIntermezzoInArrivo(controllo, momento)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean blocca(TipoIntermezzo tipo) {
		for (Missione missione : RegistroMissioni.getTutteLeMissioni()) {
			if (missione instanceof MissioneAPassi && ((MissioneAPassi) missione).bloccaIntermezzo(tipo)) {
				return true;
			}
		}
		return false;
	}

	// Il controllo delle missioni in cui si conclude un passo con l'intermezzo di quel momento; null se nessuno
	private static MomentoControllo controlloDi(MomentoIntermezzo momento) {
		switch (momento) {
			case INIZIO_LOCAZIONE:
				return MomentoControllo.PRE_LOCAZIONE;
			case LOCAZIONE_COMPLETATA:
				return MomentoControllo.POST_LOCAZIONE;
			case ACCAMPAMENTO:
				return MomentoControllo.ACCAMPAMENTO;
			default:
				return null;
		}
	}
}
