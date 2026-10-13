package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.modellodati.tattiche.FiltroMD;
import com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.tattiche.PredicatoMD;
import com.threeamigos.foresta.modellodati.tattiche.SelettoreMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.modellodati.tattiche.VoceMD;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Le tattiche base dei mostri (tattiche_di_combattimento_automatiche.md §4): quella di una classe è la sua, se qualcuno gliene ha registrata una, altrimenti
 * {@link #tatticaDiOggi()}.
 * <p>
 * <b>La tattica di oggi</b> è la trasposizione, voce per voce, di come un mostro sceglieva chi colpire e come prima delle tattiche (quel codice, in
 * <code>PersonaggioBase</code>, non c'è più):
 * <ul>
 * <li><b>Chi colpire</b>: il Mago o la Maga più potenti (di livello più alto, a parità il primo); altrimenti il Sacerdote o la
 * Sacerdotessa; altrimenti l'Elfo o l'Elfa; altrimenti il Bardo o la Cantastorie; altrimenti uno a caso fra tutti i personaggi vivi e gli ospiti vulnerabili.
 * Sono cinque selettori.</li>
 * <li><b>Se e quale incantesimo</b>, per ognuno di quei bersagli: non lancia se non è magico (cioè se non lo è, è
 * silenziato o ha 0 MAGIA); poi una volta su tre rinuncia (<code>PROBABILITA 67</code>); poi, secondo l'<b>INTELLIGENZA</b>: sotto 5 un incantesimo del repertorio
 * a caso, da 5 a 6 il più potente (il danno base più alto), da 7 il più potente solo se rende più dell'attacco con le armi (qui con i danni attesi, senza dadi);
 * se non ne ha uno che può pagare, o se l'incantesimo non conviene, attacca con le armi.</li>
 * </ul>
 * Quattro voci per ogni bersaglio (tre fasce di intelligenza e l'attacco semplice), venti in tutto. Rispetto a come sceglievano prima i mostri non lanciano
 * un incantesimo a cui il bersaglio è immune per la sua classe, né il Silenzio su chi è già silenziato; il confronto con l'attacco usa i danni attesi e non i tiri.
 * È una replica brutale, da sostituire con le tattiche vere di ogni classe (con l'astuzia al posto dell'INTELLIGENZA, §4).
 */
public final class RegistroTattiche {

	private static final TatticaMD DI_OGGI = costruisciLaTatticaDiOggi();
	private static final Map<TipoPersonaggio, TatticaMD> PER_CLASSE = new EnumMap<>(TipoPersonaggio.class);
	private static final Map<TipoPersonaggio, TatticaMD> BASE_DEI_MOSTRI = new java.util.concurrent.ConcurrentHashMap<>();

	private RegistroTattiche() {
	}

	/**
	 * La tattica base di una classe di mostri: quella registrata, altrimenti quella scritta in {@link TatticheBase} per la classe, altrimenti
	 * (per le classi che non ne hanno una) quella di oggi. Non è mai null.
	 */
	public static TatticaMD perClasse(TipoPersonaggio classe) {
		TatticaMD registrata = PER_CLASSE.get(classe);
		if (registrata != null) {
			return registrata;
		}
		return BASE_DEI_MOSTRI.computeIfAbsent(classe, c -> {
			TatticaMD base = TatticheBase.deiMostri(c);
			return base != null ? base : DI_OGGI;
		});
	}

	/**
	 * La tattica predefinita di un personaggio giocabile, da cui il giocatore può partire (vedi {@link TatticheBase#deiGiocabili}); per le altre classi
	 * la tattica vuota, che attacca il primo avversario.
	 */
	public static TatticaMD predefinitaDelGiocabile(TipoPersonaggio classe) {
		TatticaMD predefinita = TatticheBase.deiGiocabili(classe);
		return predefinita != null ? predefinita : TatticaMD.VUOTA;
	}

	/**
	 * Registra la tattica base di una classe (e toglie quella di prima se la tattica è null)
	 */
	public static void registra(TipoPersonaggio classe, TatticaMD tattica) {
		if (tattica == null) {
			PER_CLASSE.remove(classe);
		} else {
			PER_CLASSE.put(classe, tattica);
		}
	}

	/**
	 * Le azioni che un personaggio di quella classe può avere nella tattica: tutte tranne quelle che sono una caratteristica di altre classi (i canti del
	 * bardo, il dardo arcano di maghi ed elfi, l'Alba sacra dei sacerdoti) e quelle solo dei mostri. Gli incantesimi in generale si lanciano da pergamena,
	 * quindi li può avere chiunque. I canti sono di chi sa cantare; l'Urlo della Banshee lo si impara, ma resta fra le azioni (se non si conosce ancora, i requisiti lo saltano).
	 */
	public static java.util.Set<TipoAzione> azioniPossibili(TipoPersonaggio classe) {
		java.util.Set<TipoAzione> possibili = java.util.EnumSet.allOf(TipoAzione.class);
		possibili.remove(TipoAzione.INCANTESIMO_DEL_REPERTORIO);
		if (!com.threeamigos.foresta.incantesimi.CantoDelBardo.conosciutoDa(classe)) {
			possibili.removeAll(java.util.EnumSet.of(TipoAzione.CANTO_CURATIVO, TipoAzione.CANTO_IPNOTICO, TipoAzione.CANTO_DI_VALORE,
					TipoAzione.URLO_DELLA_BANSHEE));
		}
		if (!com.threeamigos.foresta.incantesimi.DardoArcano.conosciutoDa(classe)) {
			possibili.remove(TipoAzione.DARDO_ARCANO);
		}
		if (!com.threeamigos.foresta.incantesimi.AlbaSacra.conosciutaDa(classe)) {
			possibili.remove(TipoAzione.ALBA_SACRA);
		}
		return possibili;
	}

	/**
	 * La replica di come i mostri sceglievano prima delle tattiche (vedi la descrizione della classe)
	 */
	public static TatticaMD tatticaDiOggi() {
		return DI_OGGI;
	}

	private static TatticaMD costruisciLaTatticaDiOggi() {
		List<SelettoreMD> bersagli = new ArrayList<>();
		for (TipoPersonaggio ruolo : Arrays.asList(TipoPersonaggio.MAGO, TipoPersonaggio.SACERDOTE, TipoPersonaggio.ELFO, TipoPersonaggio.BARDO)) {
			bersagli.add(new SelettoreMD(TipoRosa.AVVERSARI, Collections.singletonList(FiltroMD.conClasse(TipoFiltro.CLASSE, ruolo)),
					OrdinamentoMD.di(TipoOrdinamento.LIVELLO_PIU_ALTO)));
		}
		bersagli.add(new SelettoreMD(TipoRosa.AVVERSARI_E_OSPITI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.A_CASO)));

		// Non lancia se non è magico (non lo è, è silenziato o ha 0 MAGIA), e una volta su tre rinuncia
		PredicatoMD magico = PredicatoMD.quanti(TipoRosa.SE_STESSO, Collections.singletonList(FiltroMD.senzaParametro(TipoFiltro.MAGICO)), Confronto.MAGGIORE, 0);
		PredicatoMD sotto5 = PredicatoMD.conConfrontoENumero(TipoPredicato.INTELLIGENZA, Confronto.MINORE, 5);
		PredicatoMD sotto7 = PredicatoMD.conConfrontoENumero(TipoPredicato.INTELLIGENZA, Confronto.MINORE, 7);
		PredicatoMD unaVoltaSuTreRinuncia = PredicatoMD.probabilita(67);
		AzioneMD attacco = AzioneMD.di(TipoAzione.ATTACCO_FISICO);

		List<VoceMD> voci = new ArrayList<>();
		for (SelettoreMD bersaglio : bersagli) {
			// Poco intelligente: un incantesimo a caso
			voci.add(new VoceMD(bersaglio, Arrays.asList(magico, sotto5, unaVoltaSuTreRinuncia),
					Arrays.asList(AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.A_CASO, false), attacco)));
			// Mediamente furbo: il più potente
			voci.add(new VoceMD(bersaglio, Arrays.asList(magico, sotto5.negato(), sotto7, unaVoltaSuTreRinuncia),
					Arrays.asList(AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.PIU_POTENTE, false), attacco)));
			// Furbo: il più potente, se rende più dell'attacco con le armi
			voci.add(new VoceMD(bersaglio, Arrays.asList(magico, sotto7.negato(), unaVoltaSuTreRinuncia),
					Arrays.asList(AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.PIU_POTENTE, true), attacco)));
			// Altrimenti, con le armi
			voci.add(VoceMD.su(bersaglio, attacco));
		}
		return new TatticaMD(voci);
	}
}
