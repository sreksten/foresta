package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.SelettoreMD;
import com.threeamigos.foresta.modellodati.PredicatoMD;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.modellodati.VoceMD;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.tattiche.*;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.*;

import java.util.*;
import com.threeamigos.foresta.testi.Testi;

/**
 * Le tattiche base: quelle dei mostri, una per classe, e quelle predefinite dei personaggi giocabili, da cui il giocatore può partire
 * (tattiche_di_combattimento_automatiche.md §11.16). Sono dati scritti con le stesse voci che il giocatore scrive nella schermata delle tattiche.
 * <p>
 * <b>I mostri</b> hanno una lista sola per classe, divisa in <b>fasce di astuzia</b> (§4): da 31 (stratega), da 20, da 12, e sotto (bruto). Ogni
 * fascia comincia con la condizione <code>NON ASTUZIA MINORE soglia</code>, e le fasce stanno dalla più alta alla più bassa: la prima voce eseguibile
 * vince, quindi un mostro astuto usa le sue voci e, se nessuna si può eseguire, ripiega su quelle più semplici. Un mostro entra nelle fasce che la sua
 * astuzia massima può raggiungere (la rampa sul livello lo porta in su col salire del mondo). Come si comporta ogni fascia:
 * <ul>
 * <li><b>Bruto</b> (sotto 12), l'istinto: se qualcuno l'ha appena colpito si volta contro di lui (le bestie e i bruti), altrimenti sceglie a caso, ospiti
 * compresi, e se sa fare magie ne lancia una a caso, spesso. Come i mostri di poca intelligenza dei manuali di tattica: colpiscono il più vicino o chi li ha colpiti.</li>
 * <li><b>Astuto</b> (da 12): finisce chi è sotto il 30% della salute, poi cerca un incantatore, poi il più ferito; la magia è la più forte, ma solo se rende più
 * dell'attacco con le armi. L'Hobgoblin, soldato disciplinato, tiene il fuoco sullo stesso bersaglio.</li>
 * <li><b>Esperto</b> (da 20): finisce chi è sotto il 35% con l'incantesimo più economico che basta; zittisce gli incantatori (dove ha il Silenzio); sceglie l'incantesimo più
 * adatto al bersaglio, con le sue debolezze. I giganti (non magici) colpiscono invece il più forte, che sono quelli che rovinano il loro assalto.</li>
 * <li><b>Stratega</b> (da 31): il Colpo di Morte sul quasi caduto (sotto il 25%, dove funziona e non si ritorce), il Silenzio sull'incantatore con più magia (uno alla volta, una volta su due), il colpo
 * finale, poi gli incantesimi più adatti conoscendo l'equipaggiamento.</li>
 * </ul>
 * <b>I personaggi giocabili</b> hanno un ruolo: il guerriero e il ladro curano se stessi quando stanno male e colpiscono i feriti; i maghi e gli elfi zittiscono, usano
 * l'incantesimo più economico che basta e il dardo arcano; il sacerdote cura gli altri, risuscita e toglie gli stati con l'Alba sacra; il bardo canta (curativo, di valore,
 * ipnotico e urlo contro più avversari).
 */
public final class TatticheBase {

	/** Le soglie delle fasce di astuzia (§4): 12, 20, 31 */
	private static final int SOGLIA_ASTUTO = 12;
	private static final int SOGLIA_ESPERTO = 20;
	private static final int SOGLIA_STRATEGA = 31;

	private static final Map<TipoPersonaggio, Integer> ASTUZIA_MASSIMA = new EnumMap<>(TipoPersonaggio.class);
	// TODO: questi tre elenchi (MAGICI, CON_SILENZIO, CON_MORTE) sono scritti a mano e vanno tenuti allineati con PersonaggioBase.getRepertorioIncantesimi
	//  e con le classi: se cambia il repertorio di un mostro, o ne arriva uno nuovo, nessun test se ne accorge. Trovare il modo di derivarli dal codice (o
	//  almeno un test che li confronti con le classi vere) e togliere gli elenchi.
	/** Le classi che sanno fare magie (vedi PersonaggioBase.getRepertorioIncantesimi) */
	private static final Set<TipoPersonaggio> MAGICI = EnumSet.of(TipoPersonaggio.ARPIA, TipoPersonaggio.CENTAURO, TipoPersonaggio.CHIMERA,
			TipoPersonaggio.CHIMERA_DRAGO, TipoPersonaggio.DRAGO, TipoPersonaggio.EREMITA, TipoPersonaggio.FANTASMA, TipoPersonaggio.FOLLETTO,
			TipoPersonaggio.GARGOYLE, TipoPersonaggio.IDRA, TipoPersonaggio.LICH, TipoPersonaggio.NECROMANTE, TipoPersonaggio.OMBRA_NERA, TipoPersonaggio.OMBRAFIAMMA,
			TipoPersonaggio.SPETTRO, TipoPersonaggio.SPIRITO, TipoPersonaggio.STREGA, TipoPersonaggio.VIVERNA);
	/** Quelle che hanno il Silenzio nel repertorio */
	private static final Set<TipoPersonaggio> CON_SILENZIO = EnumSet.of(TipoPersonaggio.CENTAURO, TipoPersonaggio.DRAGO, TipoPersonaggio.EREMITA,
			TipoPersonaggio.GARGOYLE, TipoPersonaggio.LICH, TipoPersonaggio.NECROMANTE, TipoPersonaggio.OMBRAFIAMMA, TipoPersonaggio.STREGA);
	/** Quelle che hanno la Morte nel repertorio (i boss) */
	private static final Set<TipoPersonaggio> CON_MORTE = EnumSet.of(TipoPersonaggio.DRAGO, TipoPersonaggio.LICH,
			TipoPersonaggio.OMBRAFIAMMA, TipoPersonaggio.STREGA);
	/** I giganti: non hanno magia e vanno dritti sul più forte */
	private static final Set<TipoPersonaggio> CACCIATORI_DEL_PIU_FORTE = EnumSet.of(TipoPersonaggio.GIGANTE, TipoPersonaggio.TITANO,
			TipoPersonaggio.MINOTAURO_GIGANTE);
	/** I bruti che si voltano contro chi li ha colpiti, oltre alle bestie */
	private static final Set<TipoPersonaggio> VENDICATIVI = EnumSet.of(TipoPersonaggio.TROLL, TipoPersonaggio.MINOTAURO, TipoPersonaggio.GOBLIN);

	static {
		ASTUZIA_MASSIMA.put(TipoPersonaggio.ARPIA, CostantiPersonaggi.ARPIA_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.CENTAURO, CostantiPersonaggi.CENTAURO_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.CHIMERA, CostantiPersonaggi.CHIMERA_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.CHIMERA_DRAGO, CostantiPersonaggi.CHIMERADRAGO_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.DRAGO, CostantiPersonaggi.DRAGO_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.EREMITA, CostantiPersonaggi.EREMITA_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.FANTASMA, CostantiPersonaggi.FANTASMA_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.FOLLETTO, CostantiPersonaggi.FOLLETTO_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.GARGOYLE, CostantiPersonaggi.GARGOYLE_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.GIGANTE, CostantiPersonaggi.GIGANTE_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.GOBLIN, CostantiPersonaggi.GOBLIN_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.HOBGOBLIN, CostantiPersonaggi.HOBGOBLIN_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.IDRA, CostantiPersonaggi.IDRA_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.LICH, CostantiPersonaggi.LICH_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.MINOTAURO, CostantiPersonaggi.MINOTAURO_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.MINOTAURO_GIGANTE, CostantiPersonaggi.MINOTAUROGIGANTE_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.NECROMANTE, CostantiPersonaggi.NECROMANTE_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.OMBRA_NERA, CostantiPersonaggi.OMBRANERA_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.OMBRAFIAMMA, CostantiPersonaggi.OMBRAFIAMMA_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.SCHELETRO, CostantiPersonaggi.SCHELETRO_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.SPETTRO, CostantiPersonaggi.SPETTRO_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.SPIRITO, CostantiPersonaggi.SPIRITO_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.STREGA, CostantiPersonaggi.STREGA_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.TITANO, CostantiPersonaggi.TITANO_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.TROLL, CostantiPersonaggi.TROLL_MAX_INTELLIGENZA);
		ASTUZIA_MASSIMA.put(TipoPersonaggio.VIVERNA, CostantiPersonaggi.VIVERNA_MAX_INTELLIGENZA);
	}

	// TODO: i mostri non hanno azioni proprie: oggi le tattiche usano solo l'attacco e gli incantesimi del repertorio. Mancano le abilità caratteristiche, che
	//  vanno prima create come azioni (TipoAzione) e poi usate qui: la rigenerazione del Troll, le teste dell'Idra, il canto dell'Arpia.
	private TatticheBase() {
	}

	/**
	 * Quanto spesso un mostro lancia una magia invece di colpire, per fascia di astuzia (ogni voce con una magia la lancia con questa probabilità, e le voci
	 * sono più d'una per fascia, quindi la quota vera è più alta). I folletti ne fanno un uso sfrenato; chi combatte soprattutto con le armi (l'idra, che
	 * sa due magie, la viverna e la chimera drago, che hanno le zanne) la lancia di rado.
	 */
	private static int[] frequenzaDegliIncantesimi(TipoPersonaggio classe) {
		switch (classe) {
			case FOLLETTO:
				return new int[]{80, 80, 80, 90};
			case IDRA:
				return new int[]{10, 10, 12, 12};
			case VIVERNA:
				return new int[]{20, 20, 22, 22};
			case CHIMERA_DRAGO:
				return new int[]{25, 25, 30, 30};
			default:
				return new int[]{50, 60, 75, 90};
		}
	}

	/**
	 * Il criterio con cui un mostro sceglie la magia: la chimera drago, che ha Fuoco e Veleno, li alterna a caso (altrimenti lancerebbe sempre il più forte),
	 * gli altri seguono il criterio della fascia.
	 */
	private static CriterioIncantesimo criterio(TipoPersonaggio classe, CriterioIncantesimo dellaFascia) {
		return classe == TipoPersonaggio.CHIMERA_DRAGO ? CriterioIncantesimo.A_CASO : dellaFascia;
	}

	/**
	 * Le classi di mostri che hanno una tattica base
	 */
	public static Set<TipoPersonaggio> classiDeiMostri() {
		return Collections.unmodifiableSet(ASTUZIA_MASSIMA.keySet());
	}

	// ======================================================================= i pezzi

	/**
	 * Un selettore: la rosa, l'ordinamento (senza parametri) e i filtri
	 */
	private static SelettoreMD su(TipoRosa rosa, TipoOrdinamento ordinamento, FiltroMD... filtri) {
		return new SelettoreMD(rosa, Arrays.asList(filtri), OrdinamentoMD.di(ordinamento));
	}

	/**
	 * Filtro: salute sotto quella percentuale della massima
	 */
	private static FiltroMD sotto(int percentuale) {
		return FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, TipoConfronto.MINORE, percentuale);
	}

	/**
	 * Filtro: sa fare magie in questo momento (vedi Filtri.isMagico)
	 */
	private static FiltroMD magico() {
		return FiltroMD.senzaParametro(TipoFiltro.MAGICO);
	}

	/**
	 * Condizione vera nella percentuale indicata dei casi (tira il dado a ogni valutazione)
	 */
	private static PredicatoMD probabilita(int percentuale) {
		return PredicatoMD.probabilita(percentuale);
	}

	/** Ci sono almeno tanti alleati con un effetto di stato da togliere */
	private static PredicatoMD alleatiConUnStatoNegativo(int almeno) {
		return PredicatoMD.quanti(TipoRosa.ALLEATI, Collections.singletonList(FiltroMD.senzaParametro(TipoFiltro.STATO_NEGATIVO)), TipoConfronto.MAGGIORE, almeno - 1);
	}

	/** Sono passati almeno tanti round dall'ultimo uso della voce (se non l'ha mai usata, è passato abbastanza) */
	private static PredicatoMD dopoRound(int round) {
		return PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND_DALL_ULTIMO_USO, TipoConfronto.MINORE, round).negato();
	}

	/** Nessun avversario è silenziato in questo momento: un Silenzio alla volta, finché dura non se ne lancia un altro */
	private static PredicatoMD nessunAvversarioSilenziato() {
		return PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.singletonList(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SILENZIATO)),
				TipoConfronto.MINORE, 1);
	}

	/** La fascia di astuzia: l'astuzia non è sotto la soglia */
	private static PredicatoMD daAstuzia(int soglia) {
		return PredicatoMD.conConfrontoENumero(TipoPredicato.ASTUZIA, TipoConfronto.MINORE, soglia).negato();
	}

	/**
	 * L'attacco con le armi: l'ultima azione di quasi tutte le voci, perché non fallisce mai (vedi Requisiti)
	 */
	private static AzioneMD attacco() {
		return AzioneMD.di(TipoAzione.ATTACCO_FISICO);
	}

	/**
	 * L'incantesimo di danno (uno dei sette elementali) con il criterio di scelta dato
	 */
	private static AzioneMD danno(CriterioIncantesimo criterio, boolean soloSePiuForte) {
		return AzioneMD.incantesimoDiDanno(criterio, soloSePiuForte);
	}

	/**
	 * L'incantesimo del repertorio del mostro (anche la Morte e il Silenzio) con il criterio di scelta dato
	 */
	private static AzioneMD repertorio(CriterioIncantesimo criterio, boolean soloSePiuForte) {
		return AzioneMD.incantesimoDelRepertorio(criterio, soloSePiuForte);
	}

	/**
	 * Una voce attiva con un nome, la voce {@code VOCE.…} di {@code tattiche_base_<lingua>.properties} (la schermata delle tattiche e il log lo mostrano al posto di "Tattica" e il numero)
	 */
	private static VoceMD voce(String chiave, SelettoreMD selettore, List<PredicatoMD> condizioni, AzioneMD... azioni) {
		return new VoceMD(selettore, condizioni, Arrays.asList(azioni), true, Testi.get("tattiche_base", chiave).orElse(chiave));
	}

	/**
	 * Le condizioni di una voce; il primo può essere null per dire "nessuna condizione" e allora viene saltato
	 */
	private static List<PredicatoMD> condizioni(PredicatoMD primo, PredicatoMD... altre) {
		List<PredicatoMD> lista = new ArrayList<>();
		if (primo != null) {
			lista.add(primo);
		}
		lista.addAll(Arrays.asList(altre));
		return lista;
	}

	// ======================================================================= i mostri

	/**
	 * La tattica base di una classe di mostri, o null se non ne ha una (vedi {@link #classiDeiMostri})
	 */
	public static TatticaMD deiMostri(TipoPersonaggio classe) {
		Integer massima = ASTUZIA_MASSIMA.get(classe);
		if (massima == null) {
			return null;
		}
		boolean magico = MAGICI.contains(classe);
		boolean cacciatoreDelPiuForte = CACCIATORI_DEL_PIU_FORTE.contains(classe);
		boolean vendicativo = VENDICATIVI.contains(classe) || classe.isDellaCategoria(CategoriaPersonaggio.BESTIA);
		int[] incantesimo = frequenzaDegliIncantesimi(classe);

		// Le fasce si aggiungono dalla più alta alla più bassa. Ogni fascia, tranne la bruta, comincia con una condizione sull'astuzia: la rampa del livello
		// (Astuzia.di) porta il mostro in una fascia più alta col salire del mondo, e se nessuna voce di quella si può eseguire il mostro scende a quella sotto.
		// Un mostro la cui astuzia massima non raggiunge la soglia non ha proprio quelle voci
		List<VoceMD> voci = new ArrayList<>();
		if (massima >= SOGLIA_STRATEGA) {
			voci.addAll(fasciaStratega(classe, magico, incantesimo[3]));
		}
		if (massima >= SOGLIA_ESPERTO) {
			voci.addAll(fasciaEsperto(classe, magico, cacciatoreDelPiuForte, incantesimo[2]));
		}
		if (massima >= SOGLIA_ASTUTO) {
			voci.addAll(fasciaAstuta(classe, magico, cacciatoreDelPiuForte, incantesimo[1]));
		}
		voci.addAll(fasciaBruta(magico, vendicativo, incantesimo[0]));
		return new TatticaMD(voci);
	}

	/**
	 * Sotto 12: l'istinto. Si volta contro chi l'ha colpito (se è vendicativo), altrimenti sceglie a caso fra gli avversari e gli ospiti.
	 */
	private static List<VoceMD> fasciaBruta(boolean magico, boolean vendicativo, int incantesimo) {
		List<VoceMD> voci = new ArrayList<>();
		SelettoreMD aCaso = su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.A_CASO);
		if (vendicativo) {
			SelettoreMD chiMhaColpito = su(TipoRosa.AVVERSARI, TipoOrdinamento.ULTIMO_ATTACCANTE);
			voci.add(magico ? voce("VOCE.BRUTO_VENDETTA", chiMhaColpito, condizioni(probabilita(50)), repertorio(CriterioIncantesimo.A_CASO, false), attacco())
					: voce("VOCE.BRUTO_VENDETTA", chiMhaColpito, condizioni(probabilita(50)), attacco()));
		}
		if (magico) {
			voci.add(voce("VOCE.BRUTO_MAGIA_A_CASO", aCaso, condizioni(probabilita(incantesimo)), repertorio(CriterioIncantesimo.A_CASO, false), attacco()));
		}
		voci.add(voce("VOCE.BRUTO_ATTACCO_A_CASO", aCaso, condizioni(null), attacco()));
		return voci;
	}

	/**
	 * Da 12: finisce i feriti, poi cerca un incantatore (o il più forte, i giganti), poi il più ferito. L'Hobgoblin tiene il fuoco sullo stesso bersaglio.
	 */
	private static List<VoceMD> fasciaAstuta(TipoPersonaggio classe, boolean magico, boolean cacciatoreDelPiuForte, int incantesimo) {
		List<VoceMD> voci = new ArrayList<>();
		PredicatoMD fascia = daAstuzia(SOGLIA_ASTUTO);
		SelettoreMD ferito = su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.MENO_SALUTE, sotto(30));
		SelettoreMD piuFerito = su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.MENO_SALUTE);
		AzioneMD magia = repertorio(criterio(classe, CriterioIncantesimo.PIU_POTENTE), true);

		voci.add(magico ? voce("VOCE.ASTUTO_FINISCE_I_FERITI", ferito, condizioni(fascia), magia, attacco()) : voce("VOCE.ASTUTO_FINISCE_I_FERITI", ferito, condizioni(fascia), attacco()));
		if (classe == TipoPersonaggio.HOBGOBLIN) {
			voci.add(voce("VOCE.ASTUTO_TIENE_IL_BERSAGLIO", su(TipoRosa.AVVERSARI, TipoOrdinamento.ULTIMO_SCELTO), condizioni(fascia), attacco()));
		}
		SelettoreMD preferito = cacciatoreDelPiuForte ? su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.LIVELLO_PIU_ALTO)
				: su(TipoRosa.AVVERSARI, TipoOrdinamento.A_CASO, magico());
		if (cacciatoreDelPiuForte) {
			voci.add(voce("VOCE.ASTUTO_CACCIA_IL_PIU_FORTE", preferito, condizioni(fascia, probabilita(60)), attacco()));
		} else {
			voci.add(magico ? voce("VOCE.ASTUTO_CERCA_GLI_INCANTATORI", preferito, condizioni(fascia, probabilita(incantesimo)), magia, attacco())
					: voce("VOCE.ASTUTO_CERCA_GLI_INCANTATORI", preferito, condizioni(fascia, probabilita(60)), attacco()));
		}
		voci.add(magico ? voce("VOCE.ASTUTO_IL_PIU_FERITO", piuFerito, condizioni(fascia, probabilita(incantesimo)), magia, attacco())
				: voce("VOCE.ASTUTO_IL_PIU_FERITO", piuFerito, condizioni(fascia), attacco()));
		return voci;
	}

	/**
	 * Da 20: il colpo più economico che basta sui feriti, il Silenzio sugli incantatori, l'incantesimo più adatto al bersaglio.
	 */
	private static List<VoceMD> fasciaEsperto(TipoPersonaggio classe, boolean magico, boolean cacciatoreDelPiuForte, int incantesimo) {
		List<VoceMD> voci = new ArrayList<>();
		PredicatoMD fascia = daAstuzia(SOGLIA_ESPERTO);
		SelettoreMD ferito = su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.MENO_SALUTE, sotto(35));
		SelettoreMD piuFerito = su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.MENO_SALUTE);
		SelettoreMD incantatore = su(TipoRosa.AVVERSARI, TipoOrdinamento.PIU_MAGIA, magico());
		AzioneMD adatto = danno(criterio(classe, CriterioIncantesimo.PIU_ADATTO), false);

		if (magico) {
			voci.add(voce("VOCE.ESPERTO_COLPO_ECONOMICO_SUI_FERITI", ferito, condizioni(fascia), danno(CriterioIncantesimo.PIU_ECONOMICO_CHE_BASTA, false), attacco()));
			if (CON_SILENZIO.contains(classe)) {
				// Un Silenzio alla volta, come nella fascia stratega (vedi todo.md)
				voci.add(voce("VOCE.ESPERTO_SILENZIO_SULL_INCANTATORE", incantatore, condizioni(fascia, probabilita(50), nessunAvversarioSilenziato()),
						AzioneMD.di(TipoAzione.SILENZIO)));
			}
			voci.add(voce("VOCE.ESPERTO_INCANTESIMO_SULL_INCANTATORE", incantatore, condizioni(fascia, probabilita(incantesimo)), adatto, attacco()));
			voci.add(voce("VOCE.ESPERTO_INCANTESIMO_SUL_PIU_FERITO", piuFerito, condizioni(fascia, probabilita(incantesimo)), adatto, attacco()));
		} else {
			voci.add(voce("VOCE.ESPERTO_FINISCE_I_FERITI", ferito, condizioni(fascia), attacco()));
			if (cacciatoreDelPiuForte) {
				voci.add(voce("VOCE.ESPERTO_CACCIA_IL_PIU_FORTE", su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.LIVELLO_PIU_ALTO), condizioni(fascia, probabilita(60)), attacco()));
			}
		}
		voci.add(voce("VOCE.ESPERTO_IL_PIU_FERITO", piuFerito, condizioni(fascia), attacco()));
		return voci;
	}

	/**
	 * Da 31: il Colpo di Morte sul quasi caduto (sotto la soglia di Morte, {@link com.threeamigos.foresta.incantesimi.Morte#puoUccidere}, dove uccide: altrimenti si ritorce contro chi lo lancia), il Silenzio
	 * sull'incantatore con più magia (uno alla volta, e una volta su due: altrimenti non farebbe altro), il colpo finale e gli incantesimi più adatti conoscendo l'equipaggiamento.
	 */
	private static List<VoceMD> fasciaStratega(TipoPersonaggio classe, boolean magico, int incantesimo) {
		List<VoceMD> voci = new ArrayList<>();
		PredicatoMD fascia = daAstuzia(SOGLIA_STRATEGA);
		SelettoreMD quasiCaduto = su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.MENO_SALUTE, sotto(CostantiIncantesimi.INCANTESIMO_MORTE_SOGLIA_SALUTE_PERCENTUALE));
		SelettoreMD ferito = su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.MENO_SALUTE, sotto(35));
		SelettoreMD piuFerito = su(TipoRosa.AVVERSARI_E_OSPITI, TipoOrdinamento.MENO_SALUTE);
		SelettoreMD incantatore = su(TipoRosa.AVVERSARI, TipoOrdinamento.PIU_MAGIA, magico());
		AzioneMD esperto = danno(CriterioIncantesimo.PIU_ADATTO_ESPERTO, false);

		if (CON_MORTE.contains(classe)) {
			voci.add(voce("VOCE.STRATEGA_COLPO_DI_MORTE_SUL_QUASI_CADUTO", quasiCaduto, condizioni(fascia), AzioneMD.di(TipoAzione.MORTE)));
		}
		if (CON_SILENZIO.contains(classe)) {
			// Un Silenzio alla volta e non sempre: senza queste due condizioni il boss lo lanciava a ogni turno (vedi todo.md), senza fare danno
			voci.add(voce("VOCE.STRATEGA_SILENZIO_SULL_INCANTATORE", incantatore, condizioni(fascia, probabilita(50), nessunAvversarioSilenziato()),
					AzioneMD.di(TipoAzione.SILENZIO)));
		}
		if (magico) {
			voci.add(voce("VOCE.STRATEGA_COLPO_ECONOMICO_SUI_FERITI", ferito, condizioni(fascia), danno(CriterioIncantesimo.PIU_ECONOMICO_CHE_BASTA, false), attacco()));
			voci.add(voce("VOCE.STRATEGA_INCANTESIMO_SULL_INCANTATORE", incantatore, condizioni(fascia, probabilita(incantesimo)), esperto, attacco()));
			voci.add(voce("VOCE.STRATEGA_INCANTESIMO_SUL_PIU_FERITO", piuFerito, condizioni(fascia, probabilita(incantesimo)), esperto, attacco()));
		}
		voci.add(voce("VOCE.STRATEGA_IL_PIU_FERITO", piuFerito, condizioni(fascia), attacco()));
		return voci;
	}

	// ======================================================================= i personaggi giocabili

	/**
	 * La tattica predefinita di un personaggio giocabile (il giocatore può riportarcisi dalla schermata delle tattiche), o null per le altre classi. Le coppie
	 * di genere (Guerriero e Guerriera...) hanno la stessa.
	 */
	public static TatticaMD deiGiocabili(TipoPersonaggio classe) {
		// Il ruolo, non la classe: Mago e Maga hanno la stessa tattica
		switch (classe.getRuolo()) {
			case GUERRIERO:
				return guerriero();
			case LADRO:
				return ladro();
			case MAGO:
				return mago();
			case ELFO:
				return elfo();
			case SACERDOTE:
				return sacerdote();
			case BARDO:
				return bardo();
			case CHIERICO:
				return chierico();
			case DRUIDO:
				return druido();
			default:
				return null;
		}
	}

	/**
	 * Le pozioni della Salute: sotto la metà della soglia prima la grande (manca molto), poi la normale; fra la metà e la soglia prima la normale e, se non
	 * ce ne sono, la grande.
	 */
	private static List<VoceMD> pozioniDiSalute(int percentuale, boolean ancheGliAltri) {
		TipoRosa rosa = ancheGliAltri ? TipoRosa.ALLEATI_E_OSPITI : TipoRosa.SE_STESSO;
		AzioneMD normale = AzioneMD.di(TipoAzione.POZIONE_SALUTE);
		AzioneMD grande = AzioneMD.di(TipoAzione.POZIONE_SALUTE_GRANDE);
		TipoOrdinamento ordinamento = ancheGliAltri ? TipoOrdinamento.MENO_SALUTE : TipoOrdinamento.PRIMO;
		return Arrays.asList(
				voce("VOCE.POZIONE_DI_SALUTE_GRANDE", su(rosa, ordinamento, sotto(percentuale / 2)), condizioni(null), grande, normale),
				voce("VOCE.POZIONE_DI_SALUTE", su(rosa, ordinamento, sotto(percentuale)), condizioni(null), normale, grande));
	}

	/**
	 * Le pozioni della Magia, per chi è a secco: come per la Salute, la grande quando la Magia è quasi finita
	 */
	private static List<VoceMD> pozioniDiMagia(int sottoQuanto) {
		AzioneMD normale = AzioneMD.di(TipoAzione.POZIONE_MAGIA);
		AzioneMD grande = AzioneMD.di(TipoAzione.POZIONE_MAGIA_GRANDE);
		return Arrays.asList(
				voce("VOCE.POZIONE_DI_MAGIA_GRANDE", su(TipoRosa.SE_STESSO, TipoOrdinamento.PRIMO, FiltroMD.conConfrontoENumero(TipoFiltro.MAGIA, TipoConfronto.MINORE, sottoQuanto / 2)),
						condizioni(null), grande, normale),
				voce("VOCE.POZIONE_DI_MAGIA", su(TipoRosa.SE_STESSO, TipoOrdinamento.PRIMO, FiltroMD.conConfrontoENumero(TipoFiltro.MAGIA, TipoConfronto.MINORE, sottoQuanto)),
						condizioni(null), normale, grande));
	}

	/**
	 * Una tattica dalle voci e dalle liste di voci (le pozioni sono due voci) nell'ordine dato
	 */
	private static TatticaMD tattica(Object... parti) {
		List<VoceMD> voci = new ArrayList<>();
		for (Object parte : parti) {
			if (parte instanceof VoceMD) {
				voci.add((VoceMD) parte);
			} else {
				@SuppressWarnings("unchecked")
				List<VoceMD> lista = (List<VoceMD>) parte;
				voci.addAll(lista);
			}
		}
		return new TatticaMD(voci);
	}

	/**
	 * In prima linea: si cura da solo quando sta male, finisce i feriti, poi i maghi nemici, poi il primo.
	 */
	private static TatticaMD guerriero() {
		return tattica(
				pozioniDiSalute(30, false),
				voce("VOCE.FINISCE_I_FERITI", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE, sotto(50)), condizioni(null), attacco()),
				voce("VOCE.COLPISCE_I_MAGHI", su(TipoRosa.AVVERSARI, TipoOrdinamento.LIVELLO_PIU_ALTO, magico()), condizioni(null), attacco()),
				voce("VOCE.ATTACCA_IL_PRIMO", su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), condizioni(null), attacco()));
	}

	/**
	 * Colpisce dove fa più male: i feriti prima, poi chi è più debole; si cura da solo.
	 */
	private static TatticaMD ladro() {
		return tattica(
				pozioniDiSalute(30, false),
				voce("VOCE.ATTACCO_ALLE_SPALLE_SUL_PIU_FORTE", su(TipoRosa.AVVERSARI, TipoOrdinamento.LIVELLO_PIU_ALTO), condizioni(null),
						AzioneMD.di(TipoAzione.ATTACCO_ALLE_SPALLE), attacco()),
				voce("VOCE.FINISCE_I_FERITI", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE, sotto(50)), condizioni(null), attacco()),
				voce("VOCE.ATTACCA_IL_PIU_FERITO", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), condizioni(null), attacco()));
	}

	/**
	 * Il cannone di vetro: la pozione della Magia quando è a secco, il Silenzio sui maghi nemici, il colpo più economico che basta sui feriti, poi
	 * l'incantesimo più adatto contro il più forte e il dardo arcano, che non costa pergamene.
	 */
	private static TatticaMD mago() {
		AzioneMD dardo = AzioneMD.di(TipoAzione.DARDO_ARCANO);
		return tattica(
				pozioniDiSalute(25, false),
				pozioniDiMagia(20),
				voce("VOCE.SILENZIO_SUI_MAGHI", su(TipoRosa.AVVERSARI, TipoOrdinamento.PIU_MAGIA, magico()), condizioni(probabilita(60)), AzioneMD.di(TipoAzione.SILENZIO)),
				voce("VOCE.COLPO_ECONOMICO_SUI_FERITI", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE, sotto(40)), condizioni(null),
						danno(CriterioIncantesimo.PIU_ECONOMICO_CHE_BASTA, false), dardo, attacco()),
				voce("VOCE.INCANTESIMO_SUL_PIU_FORTE", su(TipoRosa.AVVERSARI, TipoOrdinamento.LIVELLO_PIU_ALTO), condizioni(null),
						danno(CriterioIncantesimo.PIU_ADATTO_ESPERTO, true), dardo, attacco()));
	}

	/**
	 * A metà fra il guerriero e il mago: il dardo e l'incantesimo solo dove rendono più dell'attacco con le armi, che resta la sua arma.
	 */
	private static TatticaMD elfo() {
		AzioneMD dardo = AzioneMD.di(TipoAzione.DARDO_ARCANO).soloSePiuForteDellAttacco();
		return tattica(
				pozioniDiSalute(30, false),
				pozioniDiMagia(15),
				voce("VOCE.DARDO_SUI_MAGHI", su(TipoRosa.AVVERSARI, TipoOrdinamento.PIU_MAGIA, magico()), condizioni(null), dardo, attacco()),
				voce("VOCE.MAGIA_SUL_PIU_FERITO", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), condizioni(null), danno(CriterioIncantesimo.PIU_ADATTO, true), dardo,
						attacco()));
	}

	/**
	 * Il sostegno del gruppo: cura chi sta male (anche gli ospiti), risuscita i caduti, toglie gli stati con l'Alba sacra, brucia i non morti con la Luce divina, e quando può colpisce il più ferito.
	 */
	private static TatticaMD sacerdote() {
		return tattica(
				pozioniDiSalute(40, true),
				voce("VOCE.RESURREZIONE_DEI_CADUTI", su(TipoRosa.ALLEATI_CADUTI, TipoOrdinamento.PRIMO), condizioni(null), AzioneMD.di(TipoAzione.RESURREZIONE)),
				voce("VOCE.ALBA_SACRA", su(TipoRosa.ALLEATI, TipoOrdinamento.PRIMO), condizioni(alleatiConUnStatoNegativo(2), dopoRound(3)), AzioneMD.di(TipoAzione.ALBA_SACRA)),
				pozioniDiMagia(20),
				voce("VOCE.LUCE_DIVINA_SUI_NON_MORTI", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE,
						FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.NON_MORTO)), condizioni(null), AzioneMD.di(TipoAzione.LUCE_DIVINA)),
				voce("VOCE.COLPISCE_IL_PIU_FERITO", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), condizioni(null), danno(CriterioIncantesimo.PIU_ADATTO, true), attacco()));
	}

	/**
	 * Canta: il curativo su chi sta male, il canto di valore quando c'è chi non è ispirato, l'ipnotico e l'urlo (se lo conosce) contro più avversari;
	 * poi colpisce il più ferito.
	 */
	private static TatticaMD bardo() {
		PredicatoMD piuDiUnAvversario = PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.emptyList(), TipoConfronto.MAGGIORE, 1);
		return tattica(
				pozioniDiSalute(25, false),
				voce("VOCE.CANTO_CURATIVO", su(TipoRosa.ALLEATI_E_OSPITI, TipoOrdinamento.MENO_SALUTE, sotto(60)), condizioni(null), AzioneMD.di(TipoAzione.CANTO_CURATIVO)),
				voce("VOCE.CANTO_DI_VALORE", su(TipoRosa.ALLEATI, TipoOrdinamento.PRIMO), condizioni(null), AzioneMD.di(TipoAzione.CANTO_DI_VALORE)),
				voce("VOCE.URLO_E_CANTO_IPNOTICO", su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), condizioni(piuDiUnAvversario), AzioneMD.di(TipoAzione.URLO_DELLA_BANSHEE),
						AzioneMD.di(TipoAzione.CANTO_IPNOTICO)),
				pozioniDiMagia(20),
				voce("VOCE.COLPISCE_IL_PIU_FERITO", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), condizioni(null), danno(CriterioIncantesimo.PIU_ADATTO, true), attacco()));
	}

	/**
	 * Il chierico, che combatte in prima linea: tiene in piedi i compagni (imposizione delle mani, pozioni), scaccia i non morti, e per il resto colpisce con
	 * la mazza il più ferito. Niente alba sacra e luce divina: sono del solo sacerdote.
	 */
	private static TatticaMD chierico() {
		return tattica(
				pozioniDiSalute(30, true),
				voce("VOCE.IMPOSIZIONE_DELLE_MANI", su(TipoRosa.ALLEATI_E_OSPITI, TipoOrdinamento.MENO_SALUTE, sotto(50)), condizioni(null),
						AzioneMD.di(TipoAzione.IMPOSIZIONE_DELLE_MANI)),
				voce("VOCE.SCACCIARE_I_NON_MORTI", su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), condizioni(null), AzioneMD.di(TipoAzione.SCACCIARE_I_NON_MORTI)),
				pozioniDiMagia(15),
				voce("VOCE.MARTELLO_SACRO_SUL_PIU_FORTE", su(TipoRosa.AVVERSARI, TipoOrdinamento.LIVELLO_PIU_ALTO), condizioni(null), AzioneMD.di(TipoAzione.MARTELLO_SACRO), attacco()),
				voce("VOCE.COLPISCE_IL_PIU_FERITO", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), condizioni(null), attacco()));
	}

	/**
	 * L'incantatore della natura: protegge il gruppo con la pelle di corteccia, fa rigenerare chi sta male, ferma e avvelena gli avversari con le radici
	 * e lo sciame, e per il resto usa un incantesimo o il bastone sul più ferito.
	 */
	private static TatticaMD druido() {
		return tattica(
				pozioniDiSalute(30, false),
				voce("VOCE.RIGENERAZIONE", su(TipoRosa.ALLEATI_E_OSPITI, TipoOrdinamento.MENO_SALUTE, sotto(70)), condizioni(null), AzioneMD.di(TipoAzione.RIGENERAZIONE)),
				voce("VOCE.PELLE_DI_CORTECCIA", su(TipoRosa.ALLEATI, TipoOrdinamento.PRIMO), condizioni(null), AzioneMD.di(TipoAzione.PELLE_DI_CORTECCIA)),
				pozioniDiMagia(20),
				voce("VOCE.RADICI_E_SCIAME", su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), condizioni(null), AzioneMD.di(TipoAzione.RADICI),
						AzioneMD.di(TipoAzione.SCIAME)),
				voce("VOCE.COLPISCE_IL_PIU_FERITO", su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), condizioni(null), danno(CriterioIncantesimo.PIU_ADATTO, true), attacco()));
	}
}
