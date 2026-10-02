package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.ModalitaDiProva;
import com.threeamigos.foresta.intermezzi.ClasseIntermezzo;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.missioni.IntermezzoDiPasso;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.motore.modellodati.IntermezziMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

/**
 * Facciata su {@link IntermezziMD}: sceglie il prossimo intermezzo da mostrare e
 * ricorda quelli già scattati. Gli intermezzi fissi sono quelli elencati in
 * {@link ClasseIntermezzo}; in più ci sono quelli dei passi conclusi delle missioni a
 * passi ({@link IntermezzoDiPasso}), che si ricordano nella missione stessa.
 */
public class RegistroIntermezzi {

	// Se nel momento in corso è già scattato un intermezzo: quelli di ripiego non devono
	// sommarsi agli altri (vedi Intermezzo.isDiRipiego)
	private static boolean scattatoNelMomento;

	private RegistroIntermezzi() {
	}

	/**
	 * Comincia un nuovo momento (un ingresso, una fine locazione...): gli intermezzi che
	 * scatteranno da qui in poi gli appartengono.
	 */
	public static void nuovoMomento() {
		scattatoNelMomento = false;
	}

	private static IntermezziMD getIntermezziMD() {
		return ModelloDati.getIstanza().getIntermezziMD();
	}

	/**
	 * Il primo intermezzo non ancora scattato che deve scattare nel momento indicato; null se
	 * nessuno. Prima quelli fissi, nell'ordine di ClasseIntermezzo, poi quelli dei passi delle
	 * missioni, nell'ordine in cui i passi si sono conclusi; quelli di ripiego solo se nel
	 * momento non scatta, e non è già scattato, nessun altro.
	 */
	public static Intermezzo getProssimoIntermezzo(MomentoIntermezzo momento) {
		Intermezzo intermezzo = getProssimoIntermezzo(momento, false);
		if (intermezzo == null) {
			intermezzo = getProssimoIntermezzoDiPasso(momento);
		}
		if (intermezzo == null && !scattatoNelMomento) {
			intermezzo = getProssimoIntermezzo(momento, true);
		}
		return intermezzo;
	}

	private static Intermezzo getProssimoIntermezzo(MomentoIntermezzo momento, boolean diRipiego) {
		for (ClasseIntermezzo classeIntermezzo : ClasseIntermezzo.values()) {
			if (classeIntermezzo.isDiProva() && !ModalitaDiProva.isAttiva()) {
				continue;
			}
			Intermezzo intermezzo = classeIntermezzo.getIstanza();
			if (intermezzo.isDiRipiego() == diRipiego && !getIntermezziMD().isScattato(intermezzo.getId())
					&& intermezzo.deveScattare(momento)) {
				return intermezzo;
			}
		}
		return null;
	}

	/**
	 * L'intermezzo del primo passo concluso, in qualunque missione (anche già completata: il suo
	 * ultimo passo può avere un intermezzo), che aspetta di essere mostrato in quel momento.
	 */
	private static Intermezzo getProssimoIntermezzoDiPasso(MomentoIntermezzo momento) {
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
	 * Ricorda che l'intermezzo è scattato: quelli dei passi nella loro missione, gli altri in IntermezziMD.
	 */
	public static void segnaScattato(Intermezzo intermezzo) {
		if (intermezzo instanceof IntermezzoDiPasso) {
			((IntermezzoDiPasso) intermezzo).segnaMostrato();
		} else {
			getIntermezziMD().aggiungiScattato(intermezzo.getId());
		}
		scattatoNelMomento = true;
	}
}
