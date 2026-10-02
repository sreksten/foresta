package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.ModalitaDiProva;
import com.threeamigos.foresta.intermezzi.ClasseIntermezzo;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.motore.modellodati.IntermezziMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

/**
 * Facciata su {@link IntermezziMD}: sceglie il prossimo intermezzo da mostrare e
 * ricorda quelli già scattati. Gli intermezzi esistenti sono quelli elencati in
 * {@link ClasseIntermezzo}.
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
	 * Il primo intermezzo, nell'ordine di ClasseIntermezzo, non ancora scattato che deve
	 * scattare nel momento indicato; null se nessuno. Quelli di ripiego solo se nel momento
	 * non scatta, e non è già scattato, nessun altro.
	 */
	public static Intermezzo getProssimoIntermezzo(MomentoIntermezzo momento) {
		Intermezzo intermezzo = getProssimoIntermezzo(momento, false);
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

	public static void segnaScattato(Intermezzo intermezzo) {
		getIntermezziMD().aggiungiScattato(intermezzo.getId());
		scattatoNelMomento = true;
	}
}
