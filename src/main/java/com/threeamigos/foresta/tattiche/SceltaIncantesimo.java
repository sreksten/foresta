package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Quale dei sette incantesimi elementali lancia <code>INCANTESIMO_DI_DANNO</code> contro un bersaglio, secondo il suo criterio
 * (tattiche_di_combattimento_automatiche.md §11.10). I danni sono attesi (senza dadi). Solo A_CASO tira il dado.
 */
public final class SceltaIncantesimo {

	/**
	 * Gli incantesimi che "del repertorio" comprende: non la Morte (può ritorcersi contro chi la lancia), non il Silenzio, non i benefici
	 */
	static final List<ClasseIncantesimo> ELEMENTALI = Arrays.asList(ClasseIncantesimo.ARIA, ClasseIncantesimo.ACQUA, ClasseIncantesimo.TERRA,
			ClasseIncantesimo.FUOCO, ClasseIncantesimo.FULMINE, ClasseIncantesimo.GELO, ClasseIncantesimo.VELENO);

	/**
	 * Gli incantesimi che "del repertorio" comprende: tutti i malefici, anche la Morte e il Silenzio (come scelgono i mostri oggi)
	 */
	static final List<ClasseIncantesimo> REPERTORIO = repertorio();

	private static List<ClasseIncantesimo> repertorio() {
		List<ClasseIncantesimo> malefici = new ArrayList<>();
		for (ClasseIncantesimo classe : ClasseIncantesimo.values()) {
			if (classe.getTipo() == com.threeamigos.foresta.tipi.TipoIncantesimo.MALEFICO) {
				malefici.add(classe);
			}
		}
		return malefici;
	}

	/**
	 * Fra quali incantesimi sceglie l'azione: gli elementali per INCANTESIMO_DI_DANNO, tutti i malefici per INCANTESIMO_DEL_REPERTORIO
	 */
	private static List<ClasseIncantesimo> elencoDi(AzioneMD azione) {
		return azione.getTipo() == TipoAzione.INCANTESIMO_DEL_REPERTORIO ? REPERTORIO : ELEMENTALI;
	}

	private SceltaIncantesimo() {
	}

	/**
	 * Gli elementali che l'attore può lanciare adesso contro quel bersaglio: pagabili e disponibili, a cui non è immune (di classe o per una
	 * scoperta) e che non sono già falliti troppe volte
	 */
	public static List<ClasseIncantesimo> lanciabiliContro(Personaggio attore, Personaggio bersaglio, ContestoTattico contesto) {
		return lanciabiliContro(attore, bersaglio, contesto, ELEMENTALI);
	}

	private static List<ClasseIncantesimo> lanciabiliContro(Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
															List<ClasseIncantesimo> elenco) {
		List<ClasseIncantesimo> lanciabili = new ArrayList<>();
		if (bersaglio == null || bersaglio.isFuoriCombattimento()) {
			return lanciabili;
		}
		for (ClasseIncantesimo classe : elenco) {
			// Il Silenzio non si lancia su chi è già silenziato o è immune al silenzio
			if (classe == ClasseIncantesimo.SILENZIO
					&& (bersaglio.hasEffettoDiStato(com.threeamigos.foresta.tipi.TipoEffettoDiStato.SILENZIATO)
					|| bersaglio.isImmuneAEffetto(com.threeamigos.foresta.tipi.TipoEffettoDiStato.SILENZIATO))) {
				continue;
			}
			TipoAzione azione = TipoAzione.di(classe);
			if (Requisiti.puoLanciare(attore, classe, contesto)
					&& !bersaglio.isImmuneAIncantesimo(classe) && !bersaglio.isImmuneATipoDanno(classe.getTipoDanno())
					&& !contesto.getStato().isImmunitaScoperta(bersaglio, azione)
					&& contesto.getStato().getTentativiFalliti(attore, bersaglio, azione) < Costanti.MAX_TENTATIVI_FALLITI_PER_AZIONE) {
				lanciabili.add(classe);
			}
		}
		return lanciabili;
	}

	/**
	 * L'incantesimo scelto, o null se non se ne può lanciare nessuno (o, con <code>soloSePiuForteDellAttacco</code>, se le armi rendono di più)
	 */
	public static ClasseIncantesimo scegli(AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto) {
		List<ClasseIncantesimo> lanciabili = lanciabiliContro(attore, bersaglio, contesto, elencoDi(azione));
		if (lanciabili.isEmpty()) {
			return null;
		}
		ClasseIncantesimo scelto = secondoIlCriterio(azione, attore, bersaglio, contesto, lanciabili);
		if (azione.isSoloSePiuForteDellAttacco()
				&& dannoAttesoSuUno(attore, bersaglio, scelto) <= CalcolatoreCombattimento.dannoAttesoDellaMischia(attore, bersaglio)) {
			return null;
		}
		return scelto;
	}

	private static ClasseIncantesimo secondoIlCriterio(AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
													   List<ClasseIncantesimo> lanciabili) {
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
		List<ClasseIncantesimo> lanciabili = lanciabiliContro(attore, bersaglio, contesto, elencoDi(azione));
		StringBuilder sb = new StringBuilder(azione.getCriterio().name()).append(": ");
		for (int i = 0; i < lanciabili.size(); i++) {
			ClasseIncantesimo classe = lanciabili.get(i);
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
					sb.append(String.format(" %.1f", danniTotali(attore, bersaglio, classe, contesto)));
					break;
				case PIU_ADATTO_ESPERTO:
					sb.append(String.format(" %.2f", misuraEsperta(attore, bersaglio, classe, contesto)));
					break;
				default:
					double danno = dannoConLaConoscenza(attore, bersaglio, classe, contesto);
					sb.append(String.format(" %.1f (%s)", danno, danno >= bersaglio.getSalute() ? "basta" : "non basta"));
					break;
			}
		}
		if (azione.isSoloSePiuForteDellAttacco()) {
			sb.append("; attacco con le armi ").append(String.format("%.1f", CalcolatoreCombattimento.dannoAttesoDellaMischia(attore, bersaglio)));
		}
		return sb.toString();
	}

	private interface Misura {
		double di(ClasseIncantesimo classe);
	}

	/**
	 * Quello con la misura più alta; a parità il primo
	 */
	private static ClasseIncantesimo migliore(List<ClasseIncantesimo> classi, Misura misura) {
		ClasseIncantesimo migliore = classi.get(0);
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

	private static Arma incantesimo(Personaggio attore, ClasseIncantesimo classe) {
		return (Arma) FabbricaIncantesimi.crea(classe, attore.getLivello());
	}

	/**
	 * Il danno atteso dell'incantesimo sul solo bersaglio principale
	 */
	static double dannoAttesoSuUno(Personaggio attore, Personaggio bersaglio, ClasseIncantesimo classe) {
		return CalcolatoreCombattimento.dannoAtteso(attore, bersaglio, incantesimo(attore, classe));
	}

	static boolean piuForteDellAttaccoConIlDardo(Personaggio attore, Personaggio bersaglio) {
		return CalcolatoreCombattimento.dannoAtteso(attore, bersaglio, new DardoArcano(attore))
				> CalcolatoreCombattimento.dannoAttesoDellaMischia(attore, bersaglio);
	}

	/**
	 * Chi subisce l'incantesimo: il bersaglio principale e, se colpisce più bersagli (MULTIPLO), i successivi vivi dell'elenco degli
	 * avversari, fino al numero di bersagli dell'attore
	 */
	public static List<Personaggio> bersagli(Personaggio attore, Personaggio principale, ClasseIncantesimo classe, ContestoTattico contesto) {
		List<Personaggio> bersagli = new ArrayList<>();
		bersagli.add(principale);
		// Un mostro colpisce solo il bersaglio scelto, come oggi (PersonaggioBase.attacca)
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
	private static double danniTotali(Personaggio attore, Personaggio bersaglio, ClasseIncantesimo classe, ContestoTattico contesto) {
		double totale = 0;
		for (Personaggio colpito : bersagli(attore, bersaglio, classe, contesto)) {
			totale += CalcolatoreCombattimento.dannoAtteso(attore, colpito, incantesimo(attore, classe));
		}
		return totale;
	}

	/**
	 * Il danno atteso con il fattore dell'equipaggiamento osservato (1 se non si è visto niente)
	 */
	private static double dannoConLaConoscenza(Personaggio attore, Personaggio colpito, ClasseIncantesimo classe, ContestoTattico contesto) {
		return CalcolatoreCombattimento.dannoAtteso(attore, colpito, incantesimo(attore, classe))
				* contesto.getStato().getFattoreEquipaggiamentoNoto(colpito, classe.getTipoDanno());
	}

	/**
	 * S4: con più bersagli, la somma della quota di salute che l'incantesimo toglie a ciascuno (al massimo 1: chi verrebbe portato vicino alla
	 * fine o ucciso pesa quanto uno ucciso); con uno solo, il suo danno atteso
	 */
	private static double misuraEsperta(Personaggio attore, Personaggio bersaglio, ClasseIncantesimo classe, ContestoTattico contesto) {
		List<Personaggio> colpiti = bersagli(attore, bersaglio, classe, contesto);
		if (classe.getPortata() != PortataIncantesimo.MULTIPLO) {
			return dannoConLaConoscenza(attore, bersaglio, classe, contesto);
		}
		double totale = 0;
		for (Personaggio colpito : colpiti) {
			totale += Math.min(1.0d, dannoConLaConoscenza(attore, colpito, classe, contesto) / Math.max(1, colpito.getSalute()));
		}
		return totale;
	}

	private static ClasseIncantesimo piuAdattoEsperto(Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
													  List<ClasseIncantesimo> lanciabili) {
		return migliore(lanciabili, c -> misuraEsperta(attore, bersaglio, c, contesto));
	}

	/**
	 * Fra quelli il cui danno atteso (con la conoscenza dell'equipaggiamento) basta a uccidere il bersaglio principale, il meno caro: il prezzo della
	 * pergamena per i miei, il costo di lancio in MAGIA per i mostri. Se nessuno basta, il più adatto esperto.
	 */
	private static ClasseIncantesimo piuEconomicoCheBasta(Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
														  List<ClasseIncantesimo> lanciabili) {
		ClasseIncantesimo economico = null;
		int costoMinimo = Integer.MAX_VALUE;
		for (ClasseIncantesimo classe : lanciabili) {
			if (dannoConLaConoscenza(attore, bersaglio, classe, contesto) >= bersaglio.getSalute()) {
				int costo = contesto.isMostro(attore) ? FabbricaIncantesimi.costoLancio(classe) : FabbricaIncantesimi.costoAcquisto(classe);
				if (costo < costoMinimo) {
					economico = classe;
					costoMinimo = costo;
				}
			}
		}
		return economico != null ? economico : piuAdattoEsperto(attore, bersaglio, contesto, lanciabili);
	}

	static TipoDanno tipoDanno(ClasseIncantesimo classe) {
		return classe.getTipoDanno();
	}
}
