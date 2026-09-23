package com.threeamigos.foresta.motore;

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

	private RegistroIntermezzi() {
	}

	private static IntermezziMD getIntermezziMD() {
		return ModelloDati.getIstanza().getIntermezziMD();
	}

	/**
	 * Il primo intermezzo, nell'ordine di ClasseIntermezzo, non ancora scattato che deve
	 * scattare nel momento indicato; null se nessuno.
	 */
	public static Intermezzo getProssimoIntermezzo(MomentoIntermezzo momento) {
		for (ClasseIntermezzo classeIntermezzo : ClasseIntermezzo.values()) {
			Intermezzo intermezzo = classeIntermezzo.getIstanza();
			if (!getIntermezziMD().isScattato(intermezzo.getId()) && intermezzo.deveScattare(momento)) {
				return intermezzo;
			}
		}
		return null;
	}

	public static void segnaScattato(Intermezzo intermezzo) {
		getIntermezziMD().aggiungiScattato(intermezzo.getId());
	}
}
