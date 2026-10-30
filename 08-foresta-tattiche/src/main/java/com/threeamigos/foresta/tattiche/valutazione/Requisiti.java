package com.threeamigos.foresta.tattiche.valutazione;

import com.threeamigos.foresta.abilita.AbilitaInnate;
import com.threeamigos.foresta.abilita.PelleDiCorteccia;
import com.threeamigos.foresta.canti.CantoDelBardo;
import com.threeamigos.foresta.combattimento.CambioDArma;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.IncantesimoGlobale;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tipi.*;

/**
 * Quando un'azione si può fare (tattiche_di_combattimento_automatiche.md §11.9): i requisiti che l'azione porta, e che il giocatore non scrive. Per
 * ogni azione si chiede <code>motivoDelNo</code>: null se si può fare, altrimenti il perché (per il registro delle decisioni). Non cambia
 * niente e non tira i dadi.
 * <p>
 * Le immunità e resistenze di classe si conoscono subito; quelle date da un artefatto si conoscono solo se sono state scoperte (StatoDelCombattimento).
 * Dopo {@link CostantiTattiche#MAX_TENTATIVI_FALLITI_PER_AZIONE} fallimenti per caso un'azione non si prova più su quel bersaglio.
 */
public final class Requisiti {

	private Requisiti() {
	}

	/**
	 * Le azioni che curano: quelle che non possono avere come bersaglio un non morto del gruppo del giocatore (la Resurrezione compresa)
	 */
	private static boolean isUnaCura(TipoAzione tipo) {
		return tipo == TipoAzione.CANTO_CURATIVO || tipo == TipoAzione.IMPOSIZIONE_DELLE_MANI || tipo == TipoAzione.RIGENERAZIONE
				|| tipo == TipoAzione.POZIONE_SALUTE
				|| tipo == TipoAzione.POZIONE_SALUTE_GRANDE || tipo == TipoAzione.RESURREZIONE;
	}

	/**
	 * @param bersaglio il bersaglio scelto; null per le azioni su un gruppo intero (si guarda il gruppo) e per il cambio d'arma
	 * @return null se l'azione si può fare, altrimenti il motivo
	 */
	public static String motivoDelNo(AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto) {
		TipoAzione tipo = azione.getTipo();
		StatoDelCombattimento stato = contesto.getStato();
		PortataAzione portata = PortataAzione.di(tipo);
		// Prima i requisiti comuni a tutte le azioni su un singolo bersaglio, poi (nello switch) quelli propri di ciascuna azione. Il primo che
		// fallisce dà il motivo: un'azione che può essere scartata per più ragioni mostra sempre la stessa, quella che si controlla per prima
		if (bersaglio != null && (portata == PortataAzione.AVVERSARIO || portata == PortataAzione.ALLEATO)) {
			if (stato.isImmunitaScoperta(bersaglio, tipo)) {
				return "si è scoperto che " + bersaglio.getNome() + " è immune a " + tipo;
			}
			// La memoria dello scontro: un'immunità scoperta (da chiunque, è conoscenza pubblica) o troppi fallimenti per caso. L'attacco fisico non si
			// abbandona mai: è il ripiego, e "non fallisce mai" (§5)
			if (tipo != TipoAzione.ATTACCO_FISICO
					&& stato.getTentativiFalliti(attore, bersaglio, tipo) >= CostantiTattiche.MAX_TENTATIVI_FALLITI_PER_AZIONE) {
				return tipo + " è già fallita " + CostantiTattiche.MAX_TENTATIVI_FALLITI_PER_AZIONE + " volte contro " + bersaglio.getNome();
			}
		}
		// Chi le cure non guariscono (un non morto nel gruppo del giocatore) non è un bersaglio possibile, mai
		if (bersaglio != null && !bersaglio.isCurabile() && isUnaCura(tipo)) {
			return nome(bersaglio) + " è un non morto: non si può curare";
		}
		switch (tipo) {
			case ATTACCO_FISICO:
				return bersaglio == null || bersaglio.isFuoriCombattimento() ? "nessun bersaglio" : null;
			// Gli incantesimi di danno e del repertorio non hanno una classe fissa: è SceltaIncantesimo a dire se c'è qualcosa da lanciare
			// (null: nessun lanciabile, oppure le armi rendono di più)
			case INCANTESIMO_DI_DANNO:
			case INCANTESIMO_DEL_REPERTORIO:
				return SceltaIncantesimo.scegli(azione, attore, bersaglio, contesto) == null
						? "nessun incantesimo adatto da lanciare contro " + nome(bersaglio) : null;
			// Il dardo arcano non consuma pergamene: costa solo Magia, e lo possono lanciare solo le classi che lo conoscono
			case DARDO_ARCANO:
				if (!AbilitaInnate.puoUsare(attore, TipoAbilita.DARDO_ARCANO)) {
					return "il dardo arcano non si può lanciare (classe, Magia o silenzio)";
				}
				return contro(azione, attore, bersaglio, TipoDanno.ARCANO, contesto);
			// La luce divina del Sacerdote: come il dardo, costa solo Magia
			case LUCE_DIVINA:
				if (!AbilitaInnate.puoUsare(attore, TipoAbilita.LUCE_DIVINA)) {
					return "la luce divina non si può invocare (classe, Magia o silenzio)";
				}
				return contro(azione, attore, bersaglio, TipoDanno.SACRO, contesto);
			case RESURREZIONE:
				if (!puoLanciare(attore, TipoIncantesimo.RESURREZIONE, contesto)) {
					return "manca la pergamena o la Magia per la Resurrezione";
				}
				return bersaglio == null || bersaglio.isVivo() || bersaglio.isInPanchina() ? "nessun caduto da risuscitare" : null;
			// Un'azione su tutto il gruppo non ha un bersaglio: i requisiti guardano che nel gruppo ci sia qualcuno su cui valga la pena usarla
			case ALBA_SACRA:
				if (!AbilitaInnate.puoUsare(attore, TipoAbilita.ALBA_SACRA)) {
					return "l'Alba sacra non si può invocare (classe, Magia o silenzio)";
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
			// Le abilità innate di chierico e druido costano Magia e non consumano pergamene
			case SCACCIARE_I_NON_MORTI:
				if (!AbilitaInnate.puoUsare(attore, TipoAbilita.SCACCIARE_I_NON_MORTI)) {
					return "scacciare i non morti non si può usare";
				}
				return esiste(contesto.getAvversari(), AbilitaInnate::isDaScacciare) ? null : "nessun non morto da scacciare";
			case IMPOSIZIONE_DELLE_MANI:
				if (!AbilitaInnate.puoUsare(attore, TipoAbilita.IMPOSIZIONE_DELLE_MANI)) {
					return "l'imposizione delle mani non si può usare";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getSalute() < bersaglio.getSaluteMassima() ? null : "il bersaglio non è ferito";
			case RADICI:
				return abilitaDiDanno(attore, TipoAbilita.RADICI, contesto);
			case SCIAME:
				return abilitaDiDanno(attore, TipoAbilita.SCIAME, contesto);
			case PELLE_DI_CORTECCIA:
				if (!AbilitaInnate.puoUsare(attore, TipoAbilita.PELLE_DI_CORTECCIA)) {
					return "la pelle di corteccia non si può usare";
				}
				return esiste(contesto.getAlleati(), p -> !p.isFuoriCombattimento() && !haLaPelleDiCorteccia(p))
						? null : "tutti hanno già la pelle di corteccia";
			case RIGENERAZIONE:
				if (!AbilitaInnate.puoUsare(attore, TipoAbilita.RIGENERAZIONE)) {
					return "la rigenerazione non si può usare";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getSalute() < bersaglio.getSaluteMassima()
						&& !bersaglio.hasEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE) ? null : "il bersaglio non è ferito o si rigenera già";
			// Le pozioni vengono dalle scorte del gruppo (ContestoTattico). Quelle normali e quelle grandi sono azioni distinte: una voce le elenca in
			// sequenza per scegliere quale usare per prima (vedi TatticheBase.pozioniDiSalute)
			case POZIONE_SALUTE:
				if (contesto.getPozioniSalute() <= 0) {
					return "il gruppo non ha pozioni della Salute";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getSalute() < bersaglio.getSaluteMassima() ? null : "il bersaglio non è ferito";
			case POZIONE_MAGIA:
				if (contesto.getPozioniMagia() <= 0) {
					return "il gruppo non ha pozioni della Magia";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getMagia() < bersaglio.getMagiaMassima() ? null : "il bersaglio ha la Magia piena";
			case POZIONE_SALUTE_GRANDE:
				if (contesto.getPozioniSaluteGrandi() <= 0) {
					return "il gruppo non ha pozioni della Salute grandi";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getSalute() < bersaglio.getSaluteMassima() ? null : "il bersaglio non è ferito";
			case POZIONE_MAGIA_GRANDE:
				if (contesto.getPozioniMagiaGrandi() <= 0) {
					return "il gruppo non ha pozioni della Magia grandi";
				}
				return bersaglio != null && bersaglio.isVivo() && bersaglio.getMagia() < bersaglio.getMagiaMassima() ? null : "il bersaglio ha la Magia piena";
			// Il cambio d'arma ha bisogno dell'inventario, che hanno solo i personaggi del giocatore: per un mostro il requisito fallisce sempre
			case CAMBIA_ARMA:
				if (!(contesto.getGruppoDegliAlleati() instanceof GruppoGiocatore)) {
					return "solo i personaggi del giocatore cambiano arma";
				}
				return CambioDArma.armiPerCambio((GruppoGiocatore) contesto.getGruppoDegliAlleati(), attore, azione.getArmaDi()).isEmpty()
						? "nessuna arma che dia " + azione.getArmaDi() + " che si possa prendere" : null;
			default:
				return incantesimoSingolo(azione, attore, bersaglio, contesto);
		}
	}

	/**
	 * Gli incantesimi di una classe precisa: ARIA, ACQUA, TERRA, FUOCO, FULMINE, GELO, VELENO, MORTE, SILENZIO
	 */
	private static String incantesimoSingolo(AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto) {
		TipoIncantesimo classe = azione.getTipo().getClasseIncantesimo();
		if (classe.getPortata() == PortataIncantesimo.GLOBALE) {
			return incantesimoGlobale(classe, attore, contesto);
		}
		if (!puoLanciare(attore, classe, contesto)) {
			return "manca la pergamena o la Magia per " + classe + ", o è silenziato";
		}
		if (bersaglio == null || bersaglio.isFuoriCombattimento()) {
			return "nessun bersaglio";
		}
		if (bersaglio.isImmuneAIncantesimo(classe)) {
			return nome(bersaglio) + " è immune a " + classe;
		}
		// Il Silenzio su chi è già silenziato (o non può esserlo) non ha senso, e non c'è un tipo di danno da confrontare
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
	 * Gli incantesimi GLOBALI non hanno un bersaglio: servono se nel campo c'è qualcuno su cui facciano qualcosa
	 */
	private static String incantesimoGlobale(TipoIncantesimo classe, Personaggio attore, ContestoTattico contesto) {
		if (!puoLanciare(attore, classe, contesto)) {
			return "manca la pergamena o la Magia per " + classe + ", o è silenziato";
		}
		IncantesimoGlobale incantesimo = (IncantesimoGlobale) FabbricaIncantesimi.crea(classe, attore.getLivello());
		return incantesimo.serve(contesto.getAlleati(), contesto.getAvversari()) ? null : "non cambierebbe niente: " + classe;
	}

	/**
	 * Un bersaglio immune a quel tipo di danno (di classe) non si colpisce; lo stesso per un'azione che confronta il suo danno con le armi
	 */
	private static String contro(AzioneMD azione, Personaggio attore, Personaggio bersaglio, TipoDanno tipoDanno, ContestoTattico contesto) {
		if (bersaglio == null || bersaglio.isFuoriCombattimento()) {
			return "nessun bersaglio";
		}
		if (bersaglio.isImmuneATipoDanno(tipoDanno)) {
			return nome(bersaglio) + " è immune a " + tipoDanno;
		}
		if (azione.isSoloSePiuForteDellAttacco() && azione.getTipo() == TipoAzione.DARDO_ARCANO
				&& !SceltaIncantesimo.piuForteDellAttaccoConIlDardo(attore, bersaglio)) {
			return "l'attacco con le armi rende di più";
		}
		return null;
	}

	/**
	 * Se l'incantesimo rende più dell'attacco con le armi sul bersaglio, in danni attesi (senza dadi: sempre lo stesso risultato per la stessa situazione)
	 */
	private static boolean piuForteDellAttacco(Personaggio attore, Personaggio bersaglio, TipoIncantesimo classe) {
		return SceltaIncantesimo.dannoAttesoSuUno(attore, bersaglio, classe)
				> com.threeamigos.foresta.combattimento.CalcolatoreCombattimento.dannoAttesoDellaMischia(attore, bersaglio);
	}

	/**
	 * Se l'attore può lanciare quell'incantesimo adesso: non è silenziato e ha la Magia, e ha una pergamena nel gruppo oppure è un mostro che lo
	 * ha nel suo repertorio
	 */
	static boolean puoLanciare(Personaggio attore, TipoIncantesimo classe, ContestoTattico contesto) {
		if (!attore.puoFormulare(classe)) {
			return false;
		}
		if (contesto.getPergamene(classe) > 0) {
			return true;
		}
		return contesto.isMostro(attore) && attore.isMagico() && attore.getRepertorioIncantesimi().contains(classe);
	}

	/**
	 * I canti che agiscono sul gruppo avversario (ipnotico, urlo della Banshee): servono se c'è almeno un avversario in campo non già spaventato o
	 * confuso e che non sia immune a tutti e due gli stati (un canto contro soli immuni non produrrebbe niente e si ripeterebbe a vuoto)
	 */
	private static String canto(Personaggio attore, TipoCanto canto, ContestoTattico contesto) {
		if (!CantoDelBardo.puoCantare(attore, canto)) {
			return "il canto non si può cantare";
		}
		return esiste(contesto.getAvversari(), p -> !p.isFuoriCombattimento() && !p.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)
				&& !p.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO)
				&& !(p.isImmuneAEffetto(TipoEffettoDiStato.SPAVENTATO) && p.isImmuneAEffetto(TipoEffettoDiStato.CONFUSO)))
				? null : "tutti gli avversari sono già spaventati o confusi, o immuni";
	}

	/**
	 * Le abilità che fanno danno a più avversari (radici, sciame): servono se c'è un avversario in campo
	 */
	private static String abilitaDiDanno(Personaggio attore, TipoAbilita tipo, ContestoTattico contesto) {
		if (!AbilitaInnate.puoUsare(attore, tipo)) {
			return "l'abilità non si può usare";
		}
		return esiste(contesto.getAvversari(), p -> !p.isFuoriCombattimento()) ? null : "nessun avversario in campo";
	}

	private static boolean haLaPelleDiCorteccia(Personaggio personaggio) {
		return personaggio.getModificatoriTemporanei().stream().anyMatch(m -> PelleDiCorteccia.NOTA.equals(m.getNote()));
	}

	/**
	 * Uno stato che l'Alba sacra toglie con vantaggio: qualunque tranne quelli benefici (ISPIRATO, BERSERK)
	 */
	private static boolean haUnoStatoNegativo(Personaggio personaggio) {
		return !personaggio.isFuoriCombattimento() && Filtri.haUnoStatoNegativo(personaggio);
	}

	/**
	 * Se almeno uno dei personaggi soddisfa la condizione
	 */
	private static boolean esiste(java.util.List<Personaggio> personaggi, java.util.function.Predicate<Personaggio> condizione) {
		for (Personaggio personaggio : personaggi) {
			if (condizione.test(personaggio)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Il nome per i motivi del no; "nessuno" se il bersaglio manca
	 */
	private static String nome(Personaggio personaggio) {
		return personaggio == null ? "nessuno" : personaggio.getNome();
	}
}
