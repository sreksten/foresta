package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.InSorteggio;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.MissionePredefinita;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Quale incarico in città si offre quando, nella stessa visita a una città, ne sono pronti più d'uno: se ne sceglie uno a
 * caso, così non partono sempre gli stessi (prima partiva il primo nell'ordine del registro).
 * <p>
 * Le classi di incarico si dividono in due gruppi: quelle non ancora affidate nel giro corrente, che escono con una
 * probabilità di {@link #PROBABILITA_DI_UN_INCARICO_NUOVO}, e quelle già affidate, che escono con la probabilità che resta.
 * Se fra i pronti c'è solo uno dei due gruppi, si sceglie da quello. Quando nessuna classe che aspetta di offrirsi è più
 * da affidare nel giro (contano solo quelle che possono ancora offrirsi: vedi InSorteggio.puoAncoraOffrirsi), il giro
 * ricomincia e tutte tornano nuove.
 * <p>
 * Il giro in cui un incarico è stato affidato si scrive nella sua proprietà {@link #GIRO}, così si salva con la missione:
 * il giro corrente è il più alto scritto, o quello dopo se in quello non resta niente da affidare.
 * <p>
 * Il sorteggio si può spegnere ({@link #setSorteggioAttivo(boolean)}): allora parte il primo incarico pronto nell'ordine
 * del registro. Lo spengono i test che guidano un incarico alla volta, chiamandone direttamente i controlli.
 */
public final class SceltaDegliIncarichi {

	/**
	 * La probabilità che l'incarico scelto sia di una classe non ancora affidata nel giro, quando fra i pronti ci sono
	 * classi dell'uno e dell'altro gruppo.
	 */
	public static final double PROBABILITA_DI_UN_INCARICO_NUOVO = 0.75;

	/**
	 * La proprietà dell'incarico con il giro in cui è stato affidato.
	 */
	public static final String GIRO = "GIRO_DI_AFFIDAMENTO";

	// La scelta vale per una visita (ora di gioco e casella): tutti gli incarichi pronti chiedono se tocca a loro, e la
	// risposta deve essere la stessa per tutti. Non si salva: dopo una rilettura si sceglie di nuovo
	private static String visitaDellaScelta;
	private static InSorteggio scelto;
	private static boolean sorteggioAttivo = true;

	private SceltaDegliIncarichi() {
	}

	/**
	 * Se tocca a quell'incarico, pronto a offrirsi, partire in questa visita.
	 */
	public static boolean tocca(InSorteggio incarico) {
		if (!sorteggioAttivo) {
			return true;
		}
		String visita = visitaCorrente();
		// Si sceglie di nuovo anche nella stessa visita se lo scelto non può più partire (o non c'era nessuno): le
		// condizioni sono cambiate
		if (!visita.equals(visitaDellaScelta) || scelto == null || !scelto.isInAttesaDiOffrirsi()
				|| !scelto.isProntoPerOffrirsi()) {
			visitaDellaScelta = visita;
			scelto = scegli(pronti());
		}
		return incarico == scelto;
	}

	/**
	 * L'incarico si è offerto: lo si segna nel giro corrente.
	 */
	public static void affidato(InSorteggio incarico) {
		incarico.aggiungiProprieta(GIRO, String.valueOf(giroCorrente()));
	}

	/**
	 * Accende o spegne il sorteggio: spento, parte il primo incarico pronto nell'ordine del registro.
	 */
	public static void setSorteggioAttivo(boolean attivo) {
		sorteggioAttivo = attivo;
		dimentica();
	}

	public static boolean isSorteggioAttivo() {
		return sorteggioAttivo;
	}

	/**
	 * Dimentica la scelta della visita: si chiama quando si ricomincia o si rilegge una partita.
	 */
	public static void dimentica() {
		visitaDellaScelta = null;
		scelto = null;
	}

	private static String visitaCorrente() {
		CoordinateMD qui = Foresta.getPosizioneDelGruppo();
		return (LineaTemporale.getGiorno() * 24L + LineaTemporale.getOra()) + "@" + (qui == null ? "" : qui.getX() + "," + qui.getY());
	}

	private static InSorteggio scegli(List<InSorteggio> pronti) {
		if (pronti.isEmpty()) {
			return null;
		}
		if (pronti.size() == 1) {
			return pronti.get(0);
		}
		Set<MissionePredefinita> affidate = classiAffidateNelGiro(giroCorrente());
		List<InSorteggio> nuovi = new ArrayList<>();
		List<InSorteggio> giaAffidati = new ArrayList<>();
		for (InSorteggio incarico : pronti) {
			(affidate.contains(incarico.getClasse()) ? giaAffidati : nuovi).add(incarico);
		}
		List<InSorteggio> gruppo;
		if (giaAffidati.isEmpty()) {
			gruppo = nuovi;
		} else if (nuovi.isEmpty()) {
			gruppo = giaAffidati;
		} else {
			gruppo = Dado.probabilita() < PROBABILITA_DI_UN_INCARICO_NUOVO ? nuovi : giaAffidati;
		}
		return gruppo.get(Dado.tiraAncheAUnaFaccia(gruppo.size()) - 1);
	}

	/**
	 * Gli incarichi del sorteggio pronti a offrirsi in questa visita.
	 */
	private static List<InSorteggio> pronti() {
		List<InSorteggio> pronti = new ArrayList<>();
		for (InSorteggio incarico : incarichiDelSorteggio()) {
			if (incarico.isInAttesaDiOffrirsi() && incarico.isProntoPerOffrirsi()) {
				pronti.add(incarico);
			}
		}
		return pronti;
	}

	/**
	 * Il giro corrente: il più alto scritto negli incarichi, o quello dopo se in quello non resta nessuna classe da
	 * affidare fra gli incarichi che aspettano di offrirsi e possono ancora farlo.
	 */
	static int giroCorrente() {
		List<InSorteggio> incarichi = incarichiDelSorteggio();
		int giro = 0;
		for (InSorteggio incarico : incarichi) {
			giro = Math.max(giro, incarico.getGiro());
		}
		Set<MissionePredefinita> affidate = classiAffidateNelGiro(giro);
		for (InSorteggio incarico : incarichi) {
			if (incarico.isInAttesaDiOffrirsi() && incarico.puoAncoraOffrirsi() && !affidate.contains(incarico.getClasse())) {
				return giro;
			}
		}
		return giro + 1;
	}

	private static Set<MissionePredefinita> classiAffidateNelGiro(int giro) {
		Set<MissionePredefinita> affidate = EnumSet.noneOf(MissionePredefinita.class);
		for (InSorteggio incarico : incarichiDelSorteggio()) {
			if (incarico.getGiro() == giro) {
				affidate.add(incarico.getClasse());
			}
		}
		return affidate;
	}

	private static List<InSorteggio> incarichiDelSorteggio() {
		List<InSorteggio> incarichi = new ArrayList<>();
		for (Missione missione : RegistroMissioni.getTutteLeMissioni()) {
			if (missione instanceof InSorteggio) {
				incarichi.add((InSorteggio) missione);
			}
		}
		return incarichi;
	}
}
