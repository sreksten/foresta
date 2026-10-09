package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.function.Function;

/**
 * Chi nelle scene non combatte: il locandiere, l'armaiolo, l'alchimista, il venditore di pergamene, l'incantatore, la
 * moglie del bardo. Ha le caratteristiche del bardo ma un poco più basse (Costanti.NON_COMBATTENTE_*), nessuna
 * capacità propria (niente dardo arcano né alba sacra), non si incontra, non si recluta e non si corrompe: compare
 * nelle scene e viaggia con il gruppo come ospite quando una missione lo deve scortare (vedi Viandante).
 */
public abstract class PersonaggioNonCombattente extends Bardo {

	protected PersonaggioNonCombattente(String nome, TipoPersonaggio classe, int livello) {
		super(nome, classe, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setCorrompibile(false);
		setAmichevole(false);
		md.setForza(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.FORZA))));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.DESTREZZA))));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.COSTITUZIONE))));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.INTELLIGENZA))));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.SAGGEZZA))));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.CARISMA))));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.FORTUNA))));
		setQuantitaMassima(Costanti.BARDO_MAX_NUMERO);
	}

	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		return super.getMaxStatistica(tipoAttributo) * Costanti.NON_COMBATTENTE_FATTORE_STATISTICHE;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.NON_COMBATTENTE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.NON_COMBATTENTE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.NON_COMBATTENTE_LIVELLAMENTO_MAGIA;
	}
}
