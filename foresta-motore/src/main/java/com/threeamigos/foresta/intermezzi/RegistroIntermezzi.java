package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.missioni.IntermezzoDiPasso;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.modellodati.IntermezziMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.motore.ModalitaDiProva;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import java.util.HashSet;
import java.util.Set;

/**
 * Facciata su {@link IntermezziMD}: sceglie il prossimo intermezzo da mostrare e
 * ricorda quelli già scattati. Gli intermezzi fissi sono quelli elencati in
 * {@link TipoIntermezzo}; in più ci sono quelli dei passi conclusi delle missioni a
 * passi ({@link IntermezzoDiPasso}), che si ricordano nella missione stessa.
 */
public class RegistroIntermezzi {

	// Se nel momento in corso è già scattato un intermezzo: quelli di ripiego non devono
	// sommarsi agli altri (vedi Intermezzo.isDiRipiego)
	private static boolean scattatoNelMomento;
	// Gli intermezzi ripetibili già mostrati nel momento in corso: non si segnano come scattati, ma in uno stesso momento
	// (un ingresso) scattano una volta sola, altrimenti finito l'uno ricomincerebbe subito
	private static final Set<String> ripetibiliDelMomento = new HashSet<>();

	private RegistroIntermezzi() {
	}

	/**
	 * Comincia un nuovo momento (un ingresso, una fine locazione...): gli intermezzi che
	 * scatteranno da qui in poi gli appartengono.
	 */
	public static void nuovoMomento() {
		scattatoNelMomento = false;
		ripetibiliDelMomento.clear();
	}

	/**
	 * Se nel momento in corso è già scattato un intermezzo: usato da {@link
	 * com.threeamigos.foresta.intermezzi.IntermezzoAccampamento} per non sovrapporsi
	 * all'intermezzo di una missione (vedi LaLealta) già mostrato in questo stesso momento.
	 */
	public static boolean isScattatoNelMomento() {
		return scattatoNelMomento;
	}

	private static IntermezziMD getIntermezziMD() {
		return ModelloDati.getIstanza().getIntermezziMD();
	}

	/**
	 * Il primo intermezzo non ancora scattato che deve scattare nel momento indicato; null se
	 * nessuno. Prima quelli fissi, nell'ordine di TipoIntermezzo, poi quelli dei passi delle
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
		for (TipoIntermezzo tipoIntermezzo : TipoIntermezzo.values()) {
			if (tipoIntermezzo.isDiProva() && !ModalitaDiProva.isAttiva()) {
				continue;
			}
			Intermezzo intermezzo = FabbricaIntermezzi.crea(tipoIntermezzo);
			if (intermezzo.isDiRipiego() == diRipiego && !getIntermezziMD().isScattato(intermezzo.getId())
					&& !ripetibiliDelMomento.contains(intermezzo.getId()) && intermezzo.deveScattare(momento)) {
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
	 * Se l'intermezzo fisso di quel tipo è già scattato (una volta sola per partita, vedi IntermezziMD).
	 */
	public static boolean isScattato(TipoIntermezzo tipo) {
		return getIntermezziMD().isScattato(tipo.name());
	}

	/**
	 * Se l'intermezzo con quell'identificativo è già scattato (vedi Intermezzo.getId).
	 */
	public static boolean isScattato(String id) {
		return getIntermezziMD().isScattato(id);
	}

	/**
	 * Ricorda che l'intermezzo è scattato: quelli dei passi nella loro missione, gli altri in IntermezziMD.
	 */
	public static void segnaScattato(Intermezzo intermezzo) {
		if (intermezzo instanceof IntermezzoDiPasso) {
			((IntermezzoDiPasso) intermezzo).segnaMostrato();
		} else if (intermezzo.isRipetibile()) {
			ripetibiliDelMomento.add(intermezzo.getId());
		} else {
			getIntermezziMD().aggiungiScattato(intermezzo.getId());
		}
		scattatoNelMomento = true;
	}
}
