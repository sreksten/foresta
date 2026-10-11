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
 * <b>La tattica di oggi</b> è la trasposizione, voce per voce, di come un mostro sceglieva chi colpire e come (<code>PersonaggioBase.attacca(Gruppo)</code>
 * e <code>scegliIncantesimoContro</code>), che le tattiche sostituiscono:
 * <ul>
 * <li><b>Chi colpire</b> (<code>scegliBersaglio</code>): il Mago o la Maga più potenti (di livello più alto, a parità il primo); altrimenti il Sacerdote o la
 * Sacerdotessa; altrimenti l'Elfo o l'Elfa; altrimenti il Bardo o la Cantastorie; altrimenti uno a caso fra tutti i personaggi vivi e gli ospiti vulnerabili.
 * Sono cinque selettori.</li>
 * <li><b>Se e quale incantesimo</b> (<code>scegliIncantesimoContro</code>), per ognuno di quei bersagli: non lancia se non è magico (cioè se non lo è, è
 * silenziato o ha 0 MAGIA); poi una volta su tre rinuncia (<code>PROBABILITA 67</code>); poi, secondo l'<b>INTELLIGENZA</b>: sotto 5 un incantesimo del repertorio
 * a caso, da 5 a 6 il più potente (il danno base più alto), da 7 il più potente solo se rende più dell'attacco con le armi (qui con i danni attesi, senza dadi);
 * se non ne ha uno che può pagare, o se l'incantesimo non conviene, attacca con le armi.</li>
 * </ul>
 * Quattro voci per ogni bersaglio (tre fasce di intelligenza e l'attacco semplice), venti in tutto. Differenze dalla logica di prima: i mostri non lanciano
 * un incantesimo a cui il bersaglio è immune per la sua classe, né il Silenzio su chi è già silenziato; il confronto con l'attacco usa i danni attesi e non i tiri.
 * È una replica brutale, da sostituire con le tattiche vere di ogni classe (con l'astuzia al posto dell'INTELLIGENZA, §4).
 */
public final class RegistroTattiche {

	private static final TatticaMD DI_OGGI = costruisciLaTatticaDiOggi();
	private static final Map<TipoPersonaggio, TatticaMD> PER_CLASSE = new EnumMap<>(TipoPersonaggio.class);

	private RegistroTattiche() {
	}

	/**
	 * La tattica base di una classe di mostri: quella registrata, altrimenti quella di oggi. Non è mai null.
	 */
	public static TatticaMD perClasse(TipoPersonaggio classe) {
		TatticaMD registrata = PER_CLASSE.get(classe);
		return registrata != null ? registrata : DI_OGGI;
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
