package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Nome;

/**
 * La base degli oggetti delle locazioni: tiene la quantità, che di solito si tira a caso fra uno e il massimo della classe, e
 * quando l'oggetto viene preso assegna i punti del suo valore e riapre le finestre di stato e di statistiche.
 */
public abstract class OggettoBase implements Bottino {
	
	protected int quantita;
	
	OggettoBase() {
		int quantitaMassima = getClasse().getQuantitaMassima();
		quantita = Dado.tiraAncheAUnaFaccia(quantitaMassima);
	}

	/**
	 * Un oggetto in una quantità decisa da chi lo crea, non dal dado (vedi OggettoMissione).
	 */
	OggettoBase(int quantita) {
		this.quantita = quantita;
	}
	
	public final int getQuantita() {
		return quantita;
	}

	// Il nome della classe di bottino con quel che serve per articolarlo, nella lingua in uso (tipo_bottino_<lingua>.properties)
	private Nome nome() {
		return Nome.di(getClasse());
	}

	@Override
	public String getNomeSingolare() {
		return nome().getSingolare();
	}

	@Override
	public String getNomePlurale() {
		return nome().getPlurale();
	}

	@Override
	public String getAIS() {
		return Lingue.corrente().articoloIndeterminativo(nome(), false);
	}

	@Override
	public String getAIP() {
		return Lingue.corrente().articoloIndeterminativo(nome(), true);
	}

	@Override
	public String getADS() {
		return Lingue.corrente().articoloDeterminativo(nome(), false);
	}

	@Override
	public String getADP() {
		return Lingue.corrente().articoloDeterminativo(nome(), true);
	}

}
