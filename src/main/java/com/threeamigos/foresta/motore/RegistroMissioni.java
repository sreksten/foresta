package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.ModalitaDiProva;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.SconfiggiIlDrago;
import com.threeamigos.foresta.motore.modellodati.MissioneMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.RegistroMissioniMD;

import java.util.*;

public class RegistroMissioni {

	private static RegistroMissioniMD getRegistroMissioni() {
		return ModelloDati.getIstanza().getRegistroMissioniMD();
	}

	private RegistroMissioni() {
	}

	public enum TipoMissionePredefinita {
		SCONFIGGI_IL_DRAGO(ClasseMissione.SCONFIGGI_IL_DRAGO),
		SCONFIGGI_IL_MINOTAURO_GIGANTE(ClasseMissione.SCONFIGGI_IL_MINOTAURO_GIGANTE),
		SCONFIGGI_L_IDRA(ClasseMissione.SCONFIGGI_L_IDRA),
		SCONFIGGI_IL_LICH(ClasseMissione.SCONFIGGI_IL_LICH),
		SCONFIGGI_LA_STREGA(ClasseMissione.SCONFIGGI_LA_STREGA),
		// Solo in modalità di prova (vedi ModalitaDiProva)
		MISSIONE_DI_PROVA(ClasseMissione.MISSIONE_DI_PROVA, true),
		// Solo in modalità di prova: serve a vedere una missione fallita nella finestra delle missioni
		MISSIONE_CHE_FALLISCE(ClasseMissione.MISSIONE_CHE_FALLISCE, true),
		RECUPERA_IL_MEDAGLIONE(ClasseMissione.RECUPERA_IL_MEDAGLIONE),
		RECUPERA_LE_DERRATE_ALIMENTARI(ClasseMissione.RECUPERA_LE_DERRATE_ALIMENTARI),
		CRONACHE_DI_UN_FEGATO_EROICO(ClasseMissione.CRONACHE_DI_UN_FEGATO_EROICO),
		NESSUN_BOCCALE_LASCIATO_INDIETRO(ClasseMissione.NESSUN_BOCCALE_LASCIATO_INDIETRO),
		DISTURBATORE_DELLA_QUIETE_PUBBLICA(ClasseMissione.DISTURBATORE_DELLA_QUIETE_PUBBLICA);

		TipoMissionePredefinita(ClasseMissione classeMissione) {
			this(classeMissione, false);
		}

		TipoMissionePredefinita(ClasseMissione classeMissione, boolean diProva) {
			this.classeMissione = classeMissione;
			this.diProva = diProva;
		}

		private final ClasseMissione classeMissione;
		private final boolean diProva;

		public Missione getIstanza() {
			return classeMissione.getIstanza();
		}

		public static boolean contieneMissione(String id) {
			return Arrays.stream(TipoMissionePredefinita.values()).anyMatch(m -> m.name().equals(id));
		}
	}

	private static final Map<TipoMissionePredefinita, Missione> elencoMissioniPredefinite = new EnumMap<>(TipoMissionePredefinita.class);
	private static final Map<TipoMissionePredefinita, Missione> elencoMissioniPredefiniteCompletate = new EnumMap<>(TipoMissionePredefinita.class);
	private static final List<Missione> elencoMissioniSecondarie = new ArrayList<>();
	private static final List<Missione> elencoMissioniSecondarieCompletate = new ArrayList<>();
	// Le missioni concluse senza successo (vedi Missione.fallisciMissione)
	private static final Map<TipoMissionePredefinita, Missione> elencoMissioniPredefiniteFallite = new EnumMap<>(TipoMissionePredefinita.class);
	private static final List<Missione> elencoMissioniSecondarieFallite = new ArrayList<>();

	private static void pulisciElenchi() {
		elencoMissioniPredefinite.clear();
		elencoMissioniPredefiniteCompletate.clear();
		elencoMissioniSecondarie.clear();
		elencoMissioniSecondarieCompletate.clear();
		elencoMissioniPredefiniteFallite.clear();
		elencoMissioniSecondarieFallite.clear();
	}

	public static void reimposta() {
		RegistroMissioniMD md = getRegistroMissioni();
		md.reimposta();
		pulisciElenchi();

		for (TipoMissionePredefinita tipoMissionePredefinita : TipoMissionePredefinita.values()) {
			if (tipoMissionePredefinita.diProva && !ModalitaDiProva.isAttiva()) {
				continue;
			}
			Missione missione = tipoMissionePredefinita.getIstanza();
			missione.getModelloDati().setId(tipoMissionePredefinita.name());
			elencoMissioniPredefinite.put(tipoMissionePredefinita, missione);
			md.aggiungiMissione(tipoMissionePredefinita.name(), missione.getModelloDati());
		}
	}
	
	public static void aggiornaDopoRilettura() {
		RegistroMissioniMD md = getRegistroMissioni();
		pulisciElenchi();
		aggiornaDopoRiletturaImpl(md.getMissioniAttive());
		aggiornaDopoRiletturaImpl(md.getMissioniCompletate());
	}

	private static void aggiornaDopoRiletturaImpl(Collection<MissioneMD> missioni) {
		for (MissioneMD missioneMD : missioni) {
			if (TipoMissionePredefinita.contieneMissione(missioneMD.getId())) {
				TipoMissionePredefinita tipoMissionePredefinita = TipoMissionePredefinita.valueOf(missioneMD.getId());
				Missione missione = ricostruisci(tipoMissionePredefinita.getIstanza(), missioneMD);
				if (missione.isFallita()) {
					elencoMissioniPredefiniteFallite.put(tipoMissionePredefinita, missione);
				} else if (missione.isCompleta()) {
					elencoMissioniPredefiniteCompletate.put(tipoMissionePredefinita, missione);
				} else {
					elencoMissioniPredefinite.put(tipoMissionePredefinita, missione);
				}
			} else {
				Missione missione = ricostruisci(missioneMD);
				if (missione.isFallita()) {
					elencoMissioniSecondarieFallite.add(missione);
				} else if (missione.isCompleta()) {
					elencoMissioniSecondarieCompletate.add(missione);
				} else {
					elencoMissioniSecondarie.add(missione);
				}
			}
		}
	}

	/**
	 * Ricrea l'oggetto di dominio del nodo, della classe dichiarata nel modello dati,
	 * e ricorre sui figli.
	 */
	private static Missione ricostruisci(MissioneMD missioneMD) {
		return ricostruisci(missioneMD.getClasse().getIstanza(), missioneMD);
	}

	private static Missione ricostruisci(Missione missione, MissioneMD missioneMD) {
		missione.setModelloDati(missioneMD);
		List<Missione> missioniSecondarie = new ArrayList<>();
		for (MissioneMD missioneSecondariaMD : missioneMD.getMissioniMD()) {
			missioniSecondarie.add(ricostruisci(missioneSecondariaMD));
		}
		missione.sostituisciMissioniSecondarie(missioniSecondarie);
		return missione;
	}

	/**
	 * Riporta le missioni di primo livello non completate, attive e da attivare. La discesa
	 * nell'albero è a carico del chiamante.
	 */
	public static List<Missione> getMissioniNonCompletate() {
        List<Missione> missioni = new ArrayList<>(elencoMissioniPredefinite.values());
		missioni.addAll(elencoMissioniSecondarie);
		return missioni;
	}

	/**
	 * Riporta solo le missioni attive.
	 */
	public static List<Missione> getMissioniAttive() {
		List<Missione> missioni = new ArrayList<>();
		elencoMissioniPredefinite.values().stream().filter(Missione::isAttiva).forEach(missioni::add);
		elencoMissioniSecondarie.stream().filter(Missione::isAttiva).forEach(missioni::add);
		return missioni;
	}

	/**
	 * Le missioni di primo livello completate, seguite dalle sotto-missioni completate di quelle ancora in corso.
	 * Quando si completa anche la missione che le contiene, le sotto-missioni non compaiono più qui da sole ma
	 * sotto di lei, perché la missione passa tra le completate con tutto il suo albero.
	 */
	public static List<Missione> getMissioniCompletate() {
		List<Missione> missioni = new ArrayList<>(elencoMissioniPredefiniteCompletate.values());
		missioni.addAll(elencoMissioniSecondarieCompletate);
		for (Missione missione : getMissioniNonCompletate()) {
			aggiungiSottoMissioniCompletate(missione, missioni);
		}
		return missioni;
	}

	private static void aggiungiSottoMissioniCompletate(Missione missione, List<Missione> completate) {
		for (Missione missioneSecondaria : missione.getMissioniSecondarie()) {
			if (missioneSecondaria.isCompleta()) {
				completate.add(missioneSecondaria);
			} else if (!missioneSecondaria.isFallita()) {
				aggiungiSottoMissioniCompletate(missioneSecondaria, completate);
			}
		}
	}

	/**
	 * Le missioni di primo livello concluse senza successo.
	 */
	public static List<Missione> getMissioniFallite() {
		List<Missione> missioni = new ArrayList<>(elencoMissioniPredefiniteFallite.values());
		missioni.addAll(elencoMissioniSecondarieFallite);
		return missioni;
	}

	/**
	 * Sposta tra le fallite una missione di primo livello. Una sotto-missione fallita resta dov'e', dentro la sua
	 * missione, con la sua proprieta' FALLITA.
	 */
	public static void fallisciMissione(Missione missione) {
		if (TipoMissionePredefinita.contieneMissione(missione.getId())) {
			TipoMissionePredefinita tipoMissione = TipoMissionePredefinita.valueOf(missione.getId());
			elencoMissioniPredefinite.remove(tipoMissione);
			elencoMissioniPredefiniteFallite.put(tipoMissione, missione);
		} else if (elencoMissioniSecondarie.removeIf(missione::equals)) {
			elencoMissioniSecondarieFallite.add(missione);
		}
	}

	/**
	 * La missione principale, attiva o gia' completata: completandola (drago sconfitto) passa tra le completate,
	 * ma e' proprio allora che l'Automa chiede se lo sia per decidere tra vittoria e sconfitta.
	 */
	public static SconfiggiIlDrago getMissionePrincipale() {
		Missione missione = elencoMissioniPredefinite.get(TipoMissionePredefinita.SCONFIGGI_IL_DRAGO);
		if (missione == null) {
			missione = elencoMissioniPredefiniteCompletate.get(TipoMissionePredefinita.SCONFIGGI_IL_DRAGO);
		}
		return (SconfiggiIlDrago) missione;
	}

	/**
	 * Sposta tra le completate una missione di primo livello. Una sotto-missione completata resta dentro la sua
	 * missione, con la sua proprieta' COMPLETA (vedi getMissioniCompletate).
	 */
	public static void completaMissione(Missione missione) {
		if (TipoMissionePredefinita.contieneMissione(missione.getId())) {
			TipoMissionePredefinita tipoMissione = TipoMissionePredefinita.valueOf(missione.getId());
			elencoMissioniPredefinite.remove(tipoMissione);
			elencoMissioniPredefiniteCompletate.put(tipoMissione, missione);
		} else if (elencoMissioniSecondarie.removeIf(missione::equals)) {
			elencoMissioniSecondarieCompletate.add(missione);
		}
	}
}
