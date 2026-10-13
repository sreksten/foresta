package com.threeamigos.foresta.tattiche.valutazione;

import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.incantesimi.AlbaSacra;
import com.threeamigos.foresta.incantesimi.LuceDivina;
import com.threeamigos.foresta.canti.CantoDelBardo;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.canti.TipoCanto;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.tattiche.PortataAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.tattiche.ContestoTattico;

/**
 * Quando un'azione si può fare (tattiche_di_combattimento_automatiche.md §11.9): i requisiti che l'azione porta, e che il giocatore non scrive. Per
 * ogni azione si chiede <code>motivoDelNo</code>: null se si può fare, altrimenti il perché (per il registro delle decisioni). Non cambia
 * niente e non tira i dadi.
 * <p>
 * Le immunità e resistenze di classe si conoscono subito; quelle date da un artefatto si conoscono solo se sono state scoperte (StatoDelCombattimento).
 * Dopo {@link Costanti#MAX_TENTATIVI_FALLITI_PER_AZIONE} fallimenti per caso un'azione non si prova più su quel bersaglio.
 */
public final class Requisiti {

	private Requisiti() {
	}

	private static boolean isUnaCura(TipoAzione tipo) {
		return tipo == TipoAzione.CANTO_CURATIVO || tipo == TipoAzione.POZIONE_SALUTE
				|| tipo == TipoAzione.POZIONE_SALUTE_GRANDE || tipo == TipoAzione.RESURREZIONE;
	}

	/**
	 * @param bersaglio il bersaglio scelto; null per le azioni su un gruppo intero (si guarda il gruppo) e per il cambio d'arma
	 * @return null se l'azione si può fare, altrimenti il motivo
	 */
	public static String motivoDelNo(AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto) {
		TipoAzione tipo = azione.getTipo();
		StatoDelCombattimento stato = contesto.getStato();
		PortataAzione portata = tipo.getPortata();
		if (bersaglio != null && (portata == PortataAzione.AVVERSARIO || portata == PortataAzione.ALLEATO)) {
			if (stato.isImmunitaScoperta(bersaglio, tipo)) {
				return "si è scoperto che " + bersaglio.getNome() + " è immune a " + tipo;
			}
			// L'attacco fisico non si abbandona mai: è il ripiego, e "non fallisce mai" (§5)
			if (tipo != TipoAzione.ATTACCO_FISICO
					&& stato.getTentativiFalliti(attore, bersaglio, tipo) >= Costanti.MAX_TENTATIVI_FALLITI_PER_AZIONE) {
				return tipo + " è già fallita " + Costanti.MAX_TENTATIVI_FALLITI_PER_AZIONE + " volte contro " + bersaglio.getNome();
			}
		}
		// Chi le cure non guariscono (un non morto nel gruppo del giocatore) non è un bersaglio possibile, mai
		if (bersaglio != null && !bersaglio.isCurabile() && isUnaCura(tipo)) {
			return nome(bersaglio) + " è un non morto: non si può curare";
		}
		switch (tipo) {
			case ATTACCO_FISICO:
				return bersaglio == null || bersaglio.isFuoriCombattimento() ? "nessun bersaglio" : null;
			case INCANTESIMO_DI_DANNO:
			case INCANTESIMO_DEL_REPERTORIO:
				return SceltaIncantesimo.scegli(azione, attore, bersaglio, contesto) == null
						? "nessun incantesimo adatto da lanciare contro " + nome(bersaglio) : null;
			case DARDO_ARCANO:
				if (!DardoArcano.puoLanciarlo(attore)) {
					return "il dardo arcano non si può lanciare (classe, MAGIA o silenzio)";
				}
				return contro(azione, attore, bersaglio, TipoDanno.ARCANO, contesto);
			case RESURREZIONE:
				if (!puoLanciare(attore, ClasseIncantesimo.RESURREZIONE, contesto)) {
					return "manca la pergamena o la MAGIA per la Resurrezione";
				}
				return bersaglio == null || bersaglio.isVivo() || bersaglio.isInPanchina() ? "nessun caduto da risuscitare" : null;
			case ALBA_SACRA:
				if (!AlbaSacra.puoLanciarlaInnata(attore) && !puoLanciare(attore, ClasseIncantesimo.ALBA_SACRA, contesto)) {
					return "manca l'Alba sacra innata o la pergamena con la MAGIA";
				}
				return esiste(contesto.getAlleati(), p -> haUnoStatoNegativo(p)) ? null : "nessun alleato ha uno stato da togliere";
			case CANTO_CURATIVO:
				if (!CantoDelBardo.puoCantare(attore, TipoCanto.CURATIVO)) {
					return "il canto curativo non si può cantare";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getSalute() < bersaglio.getSaluteMassima() ? null : "il bersaglio non è ferito";
			case CANTO_IPNOTICO:
				return canto(attore, TipoCanto.IPNOTICO, contesto);
			case URLO_DELLA_BANSHEE:
				return canto(attore, TipoCanto.URLO_DELLA_BANSHEE, contesto);
			case CANTO_DI_VALORE:
				if (!CantoDelBardo.puoCantare(attore, TipoCanto.DI_VALORE)) {
					return "il canto di valore non si può cantare";
				}
				return esiste(contesto.getAlleati(), p -> !p.isFuoriCombattimento() && !p.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO))
						? null : "tutti sono già ispirati";
			case POZIONE_SALUTE:
				if (contesto.getPozioniSalute() <= 0) {
					return "il gruppo non ha pozioni della Salute";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getSalute() < bersaglio.getSaluteMassima() ? null : "il bersaglio non è ferito";
			case POZIONE_MAGIA:
				if (contesto.getPozioniMagia() <= 0) {
					return "il gruppo non ha pozioni della Magia";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getMagia() < bersaglio.getMagiaMassima() ? null : "il bersaglio ha la MAGIA piena";
			case POZIONE_SALUTE_GRANDE:
				if (contesto.getPozioniSaluteGrandi() <= 0) {
					return "il gruppo non ha pozioni della Salute grandi";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getSalute() < bersaglio.getSaluteMassima() ? null : "il bersaglio non è ferito";
			case POZIONE_MAGIA_GRANDE:
				if (contesto.getPozioniMagiaGrandi() <= 0) {
					return "il gruppo non ha pozioni della Magia grandi";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getMagia() < bersaglio.getMagiaMassima() ? null : "il bersaglio ha la MAGIA piena";
			case CAMBIA_ARMA:
				if (!(contesto.getGruppoDegliAlleati() instanceof GruppoGiocatore)) {
					return "solo i personaggi del giocatore cambiano arma";
				}
				return ((GruppoGiocatore) contesto.getGruppoDegliAlleati()).armiPerCambio(attore, azione.getArmaDi()).isEmpty()
						? "nessuna arma che dia " + azione.getArmaDi() + " che si possa prendere" : null;
			default:
				return incantesimoSingolo(azione, attore, bersaglio, contesto);
		}
	}

	/**
	 * Gli incantesimi di una classe precisa: ARIA, ACQUA, TERRA, FUOCO, FULMINE, GELO, VELENO, MORTE, SILENZIO, LUCE_DIVINA
	 */
	private static String incantesimoSingolo(AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto) {
		ClasseIncantesimo classe = azione.getTipo().getClasseIncantesimo();
		if (classe == null) {
			return "azione sconosciuta " + azione.getTipo();
		}
		if (!puoLanciare(attore, classe, contesto)) {
			return "manca la pergamena o la MAGIA per " + classe + ", o è silenziato";
		}
		if (bersaglio == null || bersaglio.isFuoriCombattimento()) {
			return "nessun bersaglio";
		}
		if (bersaglio.isImmuneAIncantesimo(classe)) {
			return nome(bersaglio) + " è immune a " + classe;
		}
		if (azione.getTipo() == TipoAzione.SILENZIO
				&& (bersaglio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO) || bersaglio.isImmuneAEffetto(TipoEffettoDiStato.SILENZIATO))) {
			return nome(bersaglio) + " è già silenziato o è immune al silenzio";
		}
		String perTipoDanno = contro(azione, attore, bersaglio, classe.getTipoDanno(), contesto);
		if (perTipoDanno != null) {
			return perTipoDanno;
		}
		if (azione.isSoloSePiuForteDellAttacco() && !piuForteDellAttacco(attore, bersaglio, classe)) {
			return "l'attacco con le armi rende di più";
		}
		return null;
	}

	/**
	 * Un bersaglio immune a quel tipo di danno (di classe) non si colpisce; lo stesso per un'azione che confronta il suo danno con le armi
	 */
	private static String contro(AzioneMD azione, Personaggio attore, Personaggio bersaglio, TipoDanno tipoDanno, ContestoTattico contesto) {
		if (bersaglio == null || bersaglio.isFuoriCombattimento()) {
			return "nessun bersaglio";
		}
		if (tipoDanno != null && bersaglio.isImmuneATipoDanno(tipoDanno)) {
			return nome(bersaglio) + " è immune a " + tipoDanno;
		}
		if (azione.isSoloSePiuForteDellAttacco() && azione.getTipo() == TipoAzione.DARDO_ARCANO
				&& !SceltaIncantesimo.piuForteDellAttaccoConIlDardo(attore, bersaglio)) {
			return "l'attacco con le armi rende di più";
		}
		return null;
	}

	private static boolean piuForteDellAttacco(Personaggio attore, Personaggio bersaglio, ClasseIncantesimo classe) {
		return SceltaIncantesimo.dannoAttesoSuUno(attore, bersaglio, classe)
				> com.threeamigos.foresta.combattimento.CalcolatoreCombattimento.dannoAttesoDellaMischia(attore, bersaglio);
	}

	/**
	 * Se l'attore può lanciare quell'incantesimo adesso: non è silenziato e ha la MAGIA, e ha una pergamena nel gruppo oppure è un mostro che lo
	 * ha nel suo repertorio
	 */
	static boolean puoLanciare(Personaggio attore, ClasseIncantesimo classe, ContestoTattico contesto) {
		if (!attore.puoFormulare(classe)) {
			return false;
		}
		if (contesto.getPergamene(classe) > 0) {
			return true;
		}
		if (classe == ClasseIncantesimo.LUCE_DIVINA && LuceDivina.puoLanciarlaInnata(attore)) {
			return true;
		}
		return contesto.isMostro(attore) && attore.isMagico() && attore.getRepertorioIncantesimi().contains(classe);
	}

	private static String canto(Personaggio attore, TipoCanto canto, ContestoTattico contesto) {
		if (!CantoDelBardo.puoCantare(attore, canto)) {
			return "il canto non si può cantare";
		}
		return esiste(contesto.getAvversari(), p -> !p.isFuoriCombattimento() && !p.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)
				&& !p.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO)) ? null : "tutti gli avversari sono già spaventati o confusi";
	}

	/**
	 * Uno stato che l'Alba sacra toglie con vantaggio: qualunque tranne quelli benefici (ISPIRATO, BERSERK)
	 */
	private static boolean haUnoStatoNegativo(Personaggio personaggio) {
		return !personaggio.isFuoriCombattimento() && Filtri.haUnoStatoNegativo(personaggio);
	}

	private static boolean esiste(java.util.List<Personaggio> personaggi, java.util.function.Predicate<Personaggio> condizione) {
		for (Personaggio personaggio : personaggi) {
			if (condizione.test(personaggio)) {
				return true;
			}
		}
		return false;
	}

	private static String nome(Personaggio personaggio) {
		return personaggio == null ? "nessuno" : personaggio.getNome();
	}
}
