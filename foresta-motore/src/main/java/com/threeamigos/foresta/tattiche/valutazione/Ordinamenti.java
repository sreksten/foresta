package com.threeamigos.foresta.tattiche.valutazione;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tattiche.ContestoTattico;

import java.util.List;
import java.util.Locale;

/**
 * Sceglie uno fra i candidati con un {@link OrdinamentoMD} (tattiche_di_combattimento_automatiche.md §11.6). A parità di misura vince il primo
 * dell'elenco, quindi la scelta è ripetibile; solo A_CASO tira il dado.
 */
public final class Ordinamenti {

	private Ordinamenti() {
	}

	/**
	 * @return il candidato scelto, o null se non ce ne sono
	 */
	public static Personaggio scegli(List<Personaggio> candidati, OrdinamentoMD ordinamento) {
		return scegli(candidati, ordinamento, null);
	}

	/**
	 * Come {@link #scegli(List, OrdinamentoMD)}, con il contesto che serve a chi guarda la storia di chi decide (ULTIMO_ATTACCANTE, ULTIMO_SCELTO): lì
	 * sceglie solo quel personaggio, se è fra i candidati, altrimenti nessuno.
	 */
	public static Personaggio scegli(List<Personaggio> candidati, OrdinamentoMD ordinamento, ContestoTattico contesto) {
		if (candidati.isEmpty()) {
			return null;
		}
		switch (ordinamento.getTipoOrdinamento()) {
			case ULTIMO_ATTACCANTE:
				return fra(candidati, contesto.getStato().getUltimoAttaccante(contesto.getAttore()));
			case ULTIMO_SCELTO:
				Personaggio avversario = fra(candidati, contesto.getStato().getUltimoAvversario(contesto.getAttore()));
				return avversario != null ? avversario : fra(candidati, contesto.getStato().getUltimoAlleato(contesto.getAttore()));
			case PRIMO:
				return candidati.get(0);
			case A_CASO:
				// L'unica fonte di caso: Dado, così le prove possono truccare il tiro. tiraAncheAUnaFaccia accetta anche un solo candidato (1 faccia)
				return candidati.get(Dado.tiraAncheAUnaFaccia(candidati.size()) - 1);
			default:
				return conLaMisuraMigliore(candidati, ordinamento);
		}
	}

	/**
	 * Il personaggio cercato se è fra i candidati, altrimenti null. La memoria di chi decide (l'ultimo attaccante, l'ultimo scelto) può riferirsi a
	 * qualcuno che nel frattempo è caduto o non è più in rosa: lo si scopre qui, non quando lo si ricorda.
	 */
	private static Personaggio fra(List<Personaggio> candidati, Personaggio cercato) {
		return cercato != null && candidati.contains(cercato) ? cercato : null;
	}

	/**
	 * Il candidato con la misura più alta; a parità il primo, perché il confronto è "maggiore" e non "maggiore o uguale". Tutti gli ordinamenti
	 * "meno" sono resi "più" cambiando il segno della misura (vedi {@link #misura}).
	 */
	private static Personaggio conLaMisuraMigliore(List<Personaggio> candidati, OrdinamentoMD ordinamento) {
		Personaggio migliore = candidati.get(0);
		double misuraMigliore = misura(migliore, ordinamento);
		for (int i = 1; i < candidati.size(); i++) {
			double misura = misura(candidati.get(i), ordinamento);
			if (misura > misuraMigliore) {
				migliore = candidati.get(i);
				misuraMigliore = misura;
			}
		}
		return migliore;
	}

	/**
	 * Le misure con cui l'ordinamento ha confrontato i candidati, per il registro delle decisioni: "Mira 12, Orfeo 4" (il valore vero, non il suo opposto).
	 * Vuota per gli ordinamenti che non misurano (il primo, a caso).
	 */
	public static String descriviLeMisure(List<Personaggio> candidati, OrdinamentoMD ordinamento) {
		if (ordinamento.getTipoOrdinamento() == com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.PRIMO
				|| ordinamento.getTipoOrdinamento() == com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.A_CASO
				|| ordinamento.getTipoOrdinamento() == com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.ULTIMO_ATTACCANTE
				|| ordinamento.getTipoOrdinamento() == com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.ULTIMO_SCELTO) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Personaggio candidato : candidati) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			double valore = Math.abs(misura(candidato, ordinamento));
			sb.append(candidato.getNome()).append(' ').append(valore == Math.rint(valore) ? String.valueOf((long) valore) : String.format(Locale.ROOT, "%.2f", valore));
		}
		return sb.toString();
	}

	/**
	 * Il valore che l'ordinamento massimizza (per i "meno", il suo opposto)
	 */
	private static double misura(Personaggio candidato, OrdinamentoMD ordinamento) {
		switch (ordinamento.getTipoOrdinamento()) {
			case MENO_SALUTE:
				return -candidato.getSalute();
			case PIU_SALUTE:
				return candidato.getSalute();
			case LIVELLO_PIU_BASSO:
				return -candidato.getLivello();
			case LIVELLO_PIU_ALTO:
				return candidato.getLivello();
			// Il moltiplicatore atteso comprende la difesa dell'equipaggiamento oltre a debolezze e resistenze di classe e di stati; un immune vale 0, quindi è
			// il "più resistente". Il filtro DEBOLE_A, invece, guarda solo classe e stati (vedi Filtri.isDebole)
			case PIU_DEBOLE_A:
				return CalcolatoreCombattimento.moltiplicatoreAtteso(candidato, ordinamento.getTipoDanno());
			case PIU_RESISTENTE_A:
				return -CalcolatoreCombattimento.moltiplicatoreAtteso(candidato, ordinamento.getTipoDanno());
			case PIU_MAGIA:
				return candidato.getMagia();
			case MENO_MAGIA:
				return -candidato.getMagia();
			default:
				throw new IllegalStateException("OrdinamentoMD senza misura: " + ordinamento);
		}
	}
}
