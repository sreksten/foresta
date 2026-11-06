package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaBancoDiLavoro;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.util.function.Supplier;

/**
 * I colori con cui la bottega dell'incantatore dice a colpo d'occhio come va la fusione: nel banco di lavoro gli effetti dell'artefatto che la fusione
 * scalzerebbe sono rossi e quelli degli ingredienti che entrerebbero verdi; il livello di un artefatto incantabile è verde se ha tutti i posti presi e
 * arancione se ne ha ancora di liberi (non è sfruttato appieno); gli altri (accessori, ingredienti) restano grigi.
 */
public final class ColoriDellIncantatore implements ElencoDegliArtefatti.Colori {

	// Il banco può cambiare (o non esserci ancora) tra un disegno e l'altro
	private final Supplier<? extends VistaBancoDiLavoro> banco;

	public ColoriDellIncantatore(Supplier<? extends VistaBancoDiLavoro> banco) {
		this.banco = banco;
	}

	@Override
	public DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
		if (!artefatto.isIncantabile()) {
			return ElencoDegliArtefatti.Colori.super.coloreLivello(artefatto, parteAttiva);
		}
		return artefatto.getNumeroEffetti() < artefatto.getEffettiMassimi() ? DoomdarkColorModel.Color.ORANGE : DoomdarkColorModel.Color.GREEN;
	}

	@Override
	public DoomdarkColorModel.Color coloreEffetto(VistaArtefatto artefatto, Object effetto, boolean parteAttiva, DoomdarkColorModel.Color predefinito) {
		VistaBancoDiLavoro banco = this.banco.get();
		if (parteAttiva || banco == null) {
			return predefinito;
		}
		if (banco.isEffettoInUscita(effetto)) {
			return DoomdarkColorModel.Color.RED;
		}
		return banco.isEffettoInEntrata(effetto) ? DoomdarkColorModel.Color.GREEN : predefinito;
	}
}
