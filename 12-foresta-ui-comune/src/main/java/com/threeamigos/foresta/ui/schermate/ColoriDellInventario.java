package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.util.function.Supplier;

/**
 * I colori con cui l'inventario dice a colpo d'occhio se il personaggio può prendere un artefatto del gruppo: nel livello, verde se può prenderlo adesso e
 * rosso se no, per qualunque motivo (livello, classe, peso, forza, posto occupato). Quelli che ha già restano grigi.
 */
public final class ColoriDellInventario implements ElencoDegliArtefatti.Colori {

	// Il personaggio può cambiare (o non esserci ancora) tra un disegno e l'altro
	private final Supplier<? extends VistaPersonaggio> personaggio;

	public ColoriDellInventario(Supplier<? extends VistaPersonaggio> personaggio) {
		this.personaggio = personaggio;
	}

	@Override
	public DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
		VistaPersonaggio p = personaggio.get();
		if (parteAttiva || p == null) {
			return ElencoDegliArtefatti.Colori.super.coloreLivello(artefatto, parteAttiva);
		}
		return p.isEquipaggiabile(artefatto) ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
	}
}
