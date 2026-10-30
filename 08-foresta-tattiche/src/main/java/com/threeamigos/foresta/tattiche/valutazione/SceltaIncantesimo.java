package com.threeamigos.foresta.tattiche.valutazione;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.abilita.DardoArcano;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tipi.CategoriaIncantesimo;
import com.threeamigos.foresta.tipi.CostantiTattiche;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoAzione;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Quale dei sette incantesimi elementali lancia <code>INCANTESIMO_DI_DANNO</code> contro un bersaglio, secondo il suo criterio
 * (tattiche_di_combattimento_automatiche.md §11.10). I danni sono attesi (senza dadi). Solo A_CASO tira il dado.
 */
public final class SceltaIncantesimo {

	/**
	 * Gli incantesimi che "del repertorio" comprende: non la Morte (può ritorcersi contro chi la lancia), non il Silenzio, non i benefici
	 */
	public static final List<TipoIncantesimo> ELEMENTALI = Arrays.asList(TipoIncantesimo.ARIA, TipoIncantesimo.ACQUA, TipoIncantesimo.TERRA,
			TipoIncantesimo.FUOCO, TipoIncantesimo.FULMINE, TipoIncantesimo.GELO, TipoIncantesimo.VELENO);

	/**
	 * Gli incantesimi che "del repertorio" comprende: tutti i malefici, anche la Morte e il Silenzio (come scelgono i mostri oggi)
	 */
	static final List<TipoIncantesimo> REPERTORIO = repertorio();

	private static List<TipoIncantesimo> repertorio() {
		List<TipoIncantesimo> malefici = new ArrayList<>();
		for (TipoIncantesimo classe : TipoIncantesimo.values()) {
			// Gli incantesimi GLOBALI non hanno un bersaglio da scegliere: le tattiche li chiamano per nome
			if (classe.getCategoriaIncantesimo() == CategoriaIncantesimo.MALEFICO && classe.getPortata() != PortataIncantesimo.GLOBALE) {
				malefici.add(classe);
			}
		}
		return malefici;
	}

	/**
	 * Fra quali incantesimi sceglie l'azione: gli elementali per INCANTESIMO_DI_DANNO, tutti i malefici per INCANTESIMO_DEL_REPERTORIO
	 */
	private static List<TipoIncantesimo> elencoDi(AzioneMD azione) {
		return azione.getTipo() == TipoAzione.INCANTESIMO_DEL_REPERTORIO ? REPERTORIO : ELEMENTALI;
	}

	private SceltaIncantesimo() {
	}

	/**
	 * Gli elementali che l'attore può lanciare adesso contro quel bersaglio: pagabili e disponibili, a cui non è immune (di classe o per una
	 * scoperta) e che non sono già falliti troppe volte
	 */
	public static List<TipoIncantesimo> lanciabiliContro(Personaggio attore, Personaggio bersaglio, ContestoTattico contesto) {
		return lanciabiliContro(attore, bersaglio, contesto, ELEMENTALI);
	}

	/**
	 * I lanciabili fra quelli dell'elenco, nell'ordine dell'elenco (da cui "a parità vince il primo" negli altri criteri). Tutti i controlli, tranne la
	 * pergamena, sono sul bersaglio: dipendono quindi da chi è colpito, non solo da chi lancia.
	 */
	private static List<TipoIncantesimo> lanciabiliContro(Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
														  List<TipoIncantesimo> elenco) {
		List<TipoIncantesimo> lanciabili = new ArrayList<>();
		if (bersaglio == null || bersaglio.isFuoriCombattimento()) {
			return lanciabili;
		}
		for (TipoIncantesimo classe : elenco) {
			// Il Silenzio non si lancia su chi è già silenziato o è immune al silenzio
			if (classe == TipoIncantesimo.SILENZIO
					&& (bersaglio.hasEffettoDiStato(com.threeamigos.foresta.tipi.TipoEffettoDiStato.SILENZIATO)
					|| bersaglio.isImmuneAEffetto(com.threeamigos.foresta.tipi.TipoEffettoDiStato.SILENZIATO))) {
				continue;
			}
			TipoAzione azione = TipoAzione.di(classe);
			if (Requisiti.puoLanciare(attore, classe, contesto)
					&& !bersaglio.isImmuneAIncantesimo(classe) && !bersaglio.isImmuneATipoDanno(classe.getTipoDanno())
					&& !contesto.getStato().isImmunitaScoperta(bersaglio, azione)
					&& contesto.getStato().getTentativiFalliti(attore, bersaglio, azione) < CostantiTattiche.MAX_TENTATIVI_FALLITI_PER_AZIONE) {
				lanciabili.add(classe);
			}
		}
		return lanciabili;
	}

	/**
	 * L'incantesimo scelto, o null se non se ne può lanciare nessuno (o, con <code>soloSePiuForteDellAttacco</code>, se le armi rendono di più)
	 */
	public static TipoIncantesimo scegli(AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto) {
		List<TipoIncantesimo> lanciabili = lanciabiliContro(attore, bersaglio, contesto, elencoDi(azione));
		if (lanciabili.isEmpty()) {
			return null;
		}
		TipoIncantesimo scelto = secondoIlCriterio(azione, attore, bersaglio, contesto, lanciabili);
		// Il confronto con l'attacco si fa solo sull'incantesimo già scelto: se il migliore secondo il criterio non batte le armi, non si prova con gli
		// altri e l'azione non è applicabile (la voce passa alla sua azione successiva, di solito l'attacco fisico)
		if (azione.isSoloSePiuForteDellAttacco()
				&& dannoAttesoSuUno(attore, bersaglio, scelto) <= CalcolatoreCombattimento.dannoAttesoDellaMischia(attore, bersaglio)) {
			return null;
		}
		return scelto;
	}

	/**
	 * Applica il criterio dell'azione a una lista non vuota di lanciabili; a parità di misura vince il primo dell'elenco
	 */
	private static TipoIncantesimo secondoIlCriterio(AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
													 List<TipoIncantesimo> lanciabili) {
		switch (azione.getCriterio()) {
			case A_CASO:
				return lanciabili.get(Dado.tiraAncheAUnaFaccia(lanciabili.size()) - 1);
			case PIU_POTENTE:
				return migliore(lanciabili, c -> ((IncantesimoMalefico) FabbricaIncantesimi.crea(c, attore.getLivello())).getDanni());
			case PIU_ADATTO:
				return migliore(lanciabili, c -> danniTotali(attore, bersaglio, c, contesto));
			case PIU_ADATTO_ESPERTO:
				return piuAdattoEsperto(attore, bersaglio, contesto, lanciabili);
			default:
				return piuEconomicoCheBasta(attore, bersaglio, contesto, lanciabili);
		}
	}

	/**
	 * Come l'incantesimo è stato scelto, per il registro delle decisioni (tattiche_di_combattimento_automatiche.md §7.7): i lanciabili con la misura del
	 * criterio, per esempio "PIU_ADATTO: FUOCO 4.2, FULMINE 9.8"
	 */
	public static String descrivi(AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto) {
		List<TipoIncantesimo> lanciabili = lanciabiliContro(attore, bersaglio, contesto, elencoDi(azione));
		StringBuilder sb = new StringBuilder(azione.getCriterio().name()).append(": ");
		for (int i = 0; i < lanciabili.size(); i++) {
			TipoIncantesimo classe = lanciabili.get(i);
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(classe);
			switch (azione.getCriterio()) {
				case A_CASO:
					break;
				case PIU_POTENTE:
					sb.append(' ').append(((IncantesimoMalefico) FabbricaIncantesimi.crea(classe, attore.getLivello())).getDanni());
					break;
				case PIU_ADATTO:
					sb.append(String.format(Locale.ROOT, " %.1f", danniTotali(attore, bersaglio, classe, contesto)));
					break;
				case PIU_ADATTO_ESPERTO:
					sb.append(String.format(Locale.ROOT, " %.2f", misuraEsperta(attore, bersaglio, classe, contesto)));
					break;
				default:
					double danno = dannoConLaConoscenza(attore, bersaglio, classe, contesto, 0);
					sb.append(String.format(Locale.ROOT, " %.1f (%s)", danno, danno >= bersaglio.getSalute() ? "basta" : "non basta"));
					break;
			}
		}
		if (azione.isSoloSePiuForteDellAttacco()) {
			sb.append("; attacco con le armi ").append(String.format(Locale.ROOT, "%.1f", CalcolatoreCombattimento.dannoAttesoDellaMischia(attore, bersaglio)));
		}
		return sb.toString();
	}

	/**
	 * Il valore che un criterio massimizza per un incantesimo
	 */
	private interface Misura {
		double di(TipoIncantesimo classe);
	}

	/**
	 * Quello con la misura più alta; a parità il primo
	 */
	private static TipoIncantesimo migliore(List<TipoIncantesimo> classi, Misura misura) {
		TipoIncantesimo migliore = classi.get(0);
		double valoreMigliore = misura.di(migliore);
		for (int i = 1; i < classi.size(); i++) {
			double valore = misura.di(classi.get(i));
			if (valore > valoreMigliore) {
				migliore = classi.get(i);
				valoreMigliore = valore;
			}
		}
		return migliore;
	}

	/**
	 * L'incantesimo di quella classe come arma, al livello dell'attore (per chiedere al calcolatore il danno atteso senza lanciarlo)
	 */
	private static Arma incantesimo(Personaggio attore, TipoIncantesimo classe) {
		return (Arma) FabbricaIncantesimi.crea(classe, attore.getLivello());
	}

	/**
	 * Il danno atteso dell'incantesimo sul solo bersaglio principale
	 */
	static double dannoAttesoSuUno(Personaggio attore, Personaggio bersaglio, TipoIncantesimo classe) {
		return CalcolatoreCombattimento.dannoAtteso(attore, bersaglio, incantesimo(attore, classe));
	}

	/**
	 * Se il dardo arcano rende più dell'attacco con le armi sul bersaglio, in danni attesi (per DARDO_ARCANO con soloSePiuForteDellAttacco)
	 */
	static boolean piuForteDellAttaccoConIlDardo(Personaggio attore, Personaggio bersaglio) {
		return CalcolatoreCombattimento.dannoAtteso(attore, bersaglio, new DardoArcano(attore))
				> CalcolatoreCombattimento.dannoAttesoDellaMischia(attore, bersaglio);
	}

	/**
	 * Chi subisce l'incantesimo: il bersaglio principale e, se colpisce più bersagli (MULTIPLO), i successivi vivi dell'elenco degli
	 * avversari, fino al numero di bersagli dell'attore
	 */
	public static List<Personaggio> bersagli(Personaggio attore, Personaggio principale, TipoIncantesimo classe, ContestoTattico contesto) {
		List<Personaggio> bersagli = new ArrayList<>();
		bersagli.add(principale);
		// Un mostro colpisce solo il bersaglio scelto, anche con un incantesimo a più bersagli (come sempre)
		if (classe.getPortata() == PortataIncantesimo.MULTIPLO && !contesto.isMostro(attore)) {
			for (Personaggio altro : contesto.getAvversari()) {
				if (bersagli.size() >= Math.max(1, attore.getBersagli())) {
					break;
				}
				if (altro != principale && !altro.isFuoriCombattimento()) {
					bersagli.add(altro);
				}
			}
		}
		return bersagli;
	}

	/**
	 * S3: la somma dei danni attesi sui bersagli (debolezze e resistenze di classe e di stati)
	 */
	private static double danniTotali(Personaggio attore, Personaggio bersaglio, TipoIncantesimo classe, ContestoTattico contesto) {
		double totale = 0;
		List<Personaggio> colpiti = bersagli(attore, bersaglio, classe, contesto);
		for (int posizione = 0; posizione < colpiti.size(); posizione++) {
			totale += CalcolatoreCombattimento.dannoAtteso(attore, colpiti.get(posizione), incantesimo(attore, classe),
					CalcolatoreCombattimento.quotaDelDannoPerPosizione(posizione));
		}
		return totale;
	}

	/**
	 * Il danno atteso con il fattore dell'equipaggiamento osservato (1 se non si è visto niente)
	 */
	private static double dannoConLaConoscenza(Personaggio attore, Personaggio colpito, TipoIncantesimo classe, ContestoTattico contesto,
											   int posizione) {
		return CalcolatoreCombattimento.dannoAtteso(attore, colpito, incantesimo(attore, classe),
				CalcolatoreCombattimento.quotaDelDannoPerPosizione(posizione))
				* contesto.getStato().getFattoreEquipaggiamentoNoto(colpito, classe.getTipoDanno());
	}

	/**
	 * S4: con più bersagli, la somma della quota di salute che l'incantesimo toglie a ciascuno (al massimo 1: chi verrebbe portato vicino alla
	 * fine o ucciso pesa quanto uno ucciso); con uno solo, il suo danno atteso
	 */
	private static double misuraEsperta(Personaggio attore, Personaggio bersaglio, TipoIncantesimo classe, ContestoTattico contesto) {
		List<Personaggio> colpiti = bersagli(attore, bersaglio, classe, contesto);
		if (classe.getPortata() != PortataIncantesimo.MULTIPLO) {
			return dannoConLaConoscenza(attore, bersaglio, classe, contesto, 0);
		}
		double totale = 0;
		for (int posizione = 0; posizione < colpiti.size(); posizione++) {
			Personaggio colpito = colpiti.get(posizione);
			totale += Math.min(1.0d, dannoConLaConoscenza(attore, colpito, classe, contesto, posizione) / Math.max(1, colpito.getSalute()));
		}
		return totale;
	}

	private static TipoIncantesimo piuAdattoEsperto(Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
													List<TipoIncantesimo> lanciabili) {
		return migliore(lanciabili, c -> misuraEsperta(attore, bersaglio, c, contesto));
	}

	/**
	 * Fra quelli il cui danno atteso (con la conoscenza dell'equipaggiamento) basta a uccidere il bersaglio principale, il meno caro: il prezzo della
	 * pergamena per i miei, il costo di lancio in MAGIA per i mostri. Se nessuno basta, il più adatto esperto.
	 */
	private static TipoIncantesimo piuEconomicoCheBasta(Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
														List<TipoIncantesimo> lanciabili) {
		TipoIncantesimo economico = null;
		int costoMinimo = Integer.MAX_VALUE;
		for (TipoIncantesimo classe : lanciabili) {
			if (dannoConLaConoscenza(attore, bersaglio, classe, contesto, 0) >= bersaglio.getSalute()) {
				int costo = contesto.isMostro(attore) ? classe.getCostoLancio() : classe.getCostoAcquisto();
				if (costo < costoMinimo) {
					economico = classe;
					costoMinimo = costo;
				}
			}
		}
		return economico != null ? economico : piuAdattoEsperto(attore, bersaglio, contesto, lanciabili);
	}
}
