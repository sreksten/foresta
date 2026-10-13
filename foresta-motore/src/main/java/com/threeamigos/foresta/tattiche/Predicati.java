package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.tattiche.PredicatoMD;
import com.threeamigos.foresta.modellodati.tattiche.SelettoreMD;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;

import java.util.EnumMap;
import java.util.Map;

/**
 * La risposta di ogni {@link TipoPredicato} (tattiche_di_combattimento_automatiche.md §11.8). I predicati sulla voce (VOCE_USATA,
 * ROUND_DALL_ULTIMO_USO) vogliono che il contesto sappia quale voce si sta valutando ({@link ContestoTattico#conVoce}). Non cambiano niente;
 * solo PROBABILITA tira il dado.
 */
public final class Predicati {

	private interface Verifica {
		boolean vale(PredicatoMD predicato, ContestoTattico contesto);
	}

	private static final Map<TipoPredicato, Verifica> VERIFICHE = new EnumMap<>(TipoPredicato.class);

	static {
		VERIFICHE.put(TipoPredicato.QUANTI, (p, ctx) -> p.getConfronto().vale(
				Selettori.seleziona(new SelettoreMD(p.getRosa(), p.getFiltri(), OrdinamentoMD.di(com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.PRIMO)),
						ctx).getRimasti().size(), p.getNumero()));
		VERIFICHE.put(TipoPredicato.ROUND, (p, ctx) -> p.getConfronto().vale(ctx.getStato().getRound(), p.getNumero()));
		VERIFICHE.put(TipoPredicato.ASTUZIA, (p, ctx) -> p.getConfronto().vale(Astuzia.di(ctx.getAttore()), p.getNumero()));
		VERIFICHE.put(TipoPredicato.INTELLIGENZA, (p, ctx) -> p.getConfronto().vale(ctx.getAttore().getIntelligenza(), p.getNumero()));
		VERIFICHE.put(TipoPredicato.PROBABILITA, (p, ctx) -> Dado.tira(100) <= p.getNumero());
		VERIFICHE.put(TipoPredicato.VOCE_USATA, (p, ctx) -> p.getConfronto().vale(
				ctx.getStato().getUsiDellaVoce(ctx.getAttore(), voce(ctx)), p.getNumero()));
		VERIFICHE.put(TipoPredicato.ROUND_DALL_ULTIMO_USO, (p, ctx) -> p.getConfronto().vale(roundDallUltimoUso(ctx), p.getNumero()));
	}

	private Predicati() {
	}

	/**
	 * Se il predicato è vero (tenendo conto della negazione)
	 */
	public static boolean vale(PredicatoMD predicato, ContestoTattico contesto) {
		return VERIFICHE.get(predicato.getTipo()).vale(predicato, contesto) != predicato.isNegato();
	}

	/**
	 * Se tutti i predicati sono veri (le condizioni di una voce sono in AND; nessuna condizione vale sempre)
	 */
	public static boolean valgonoTutti(java.util.List<PredicatoMD> predicati, ContestoTattico contesto) {
		for (PredicatoMD predicato : predicati) {
			if (!vale(predicato, contesto)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Se per ogni tipo di predicato c'è la sua risposta (lo controlla un test)
	 */
	static boolean haLaSuaRisposta(TipoPredicato tipo) {
		return VERIFICHE.containsKey(tipo);
	}

	private static String voce(ContestoTattico contesto) {
		if (contesto.getIdentificativoVoce() == null) {
			throw new IllegalStateException("Questo predicato riguarda una voce, ma il contesto non dice quale (ContestoTattico.conVoce)");
		}
		return contesto.getIdentificativoVoce();
	}

	/**
	 * I round passati dall'ultimo uso della voce; se non l'ha mai usata, moltissimi (il periodo di attesa di un'abilità mai usata è finito)
	 */
	private static double roundDallUltimoUso(ContestoTattico contesto) {
		Personaggio attore = contesto.getAttore();
		StatoDelCombattimento stato = contesto.getStato();
		String voce = voce(contesto);
		return stato.haUsatoLaVoce(attore, voce) ? stato.getRound() - stato.getRoundUltimoUsoVoce(attore, voce) : Integer.MAX_VALUE;
	}
}
