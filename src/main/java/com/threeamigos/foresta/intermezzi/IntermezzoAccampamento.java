package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroIntermezzi;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.IntermezziMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

import java.util.List;

/**
 * Intermezzo che scatta quando il gruppo si accampa: stesso sfondo e fuoco di
 * {@link IntermezzoIntroduttivo}, con il gruppo intorno al fuoco (vedi {@link ScenaFraCompagni}). A differenza degli altri
 * intermezzi scatta una volta per accampamento (non di più nello stesso ciclo, vedi
 * {@link com.threeamigos.foresta.motore.Automa}), fino a un massimo di
 * {@value #MASSIMO_OCCORRENZE} volte nella partita: {@link #getId()} include quindi la sua
 * occorrenza (vedi {@link #prossimaOccorrenza()}), cosa possibile perché
 * {@link ClasseIntermezzo#getIstanza()} crea una nuova istanza a ogni controllo e
 * {@link IntermezziMD} è un semplice insieme di stringhe senza vincoli di formato.
 * <p>
 * Ogni occorrenza ha la sua scena, con la sua luna e le sue battute: la storia di quello trasformato in scarafaggio
 * sotto una luna normale, la luna che è una stazione da battaglia, e la luna strana di cui non farsi prendere dal panico.
 * <p>
 * Se in questo stesso accampamento una missione mostrerà il suo intermezzo (vedi LaLealta), questo non scatta: si
 * vedrebbero in sovrapposizione, e quello della missione viene prima (vedi {@link #deveScattare}). Non essendo
 * scattato, lo si vedrà al prossimo accampamento libero da missioni: l'occorrenza (e quindi la scena) si basa su
 * quante volte l'intermezzo è stato effettivamente mostrato finora, non su quanti accampamenti sono avvenuti in
 * tutto, altrimenti un accampamento in cui non scatta ne "mangerebbe" la scena, mai più mostrata.
 */
public class IntermezzoAccampamento implements Intermezzo {

	private static final int MASSIMO_OCCORRENZE = 3;

	/**
	 * Le scene, una per occorrenza: l'immagine della luna e le battute, alternate fra il primo e il secondo
	 * personaggio.
	 */
	private static final String[][] SCENE = {
			{"fondi/Luna.gif",
					"Una volta ho sentito di uno che si è svegliato trasformato in uno scarafaggio. O forse era un asino d'oro.",
					"Tu presti troppa attenzione a quelle assurde storie nelle locande."},
			{"fondi/Mimas.gif", // Una luna di Saturno
					"Hai visto che luna stasera?",
					"Quella non è una luna... è una stazione da battaglia!",
					"Tu passi troppo tempo ad ascoltare le storie nelle locande."},
			{"fondi/LunaHHGTTG.gif",
					"Che luna strana stasera...",
					"Non fatevi prendere dal panico."}
	};

	@Override
	public String getId() {
		return idPer(prossimaOccorrenza());
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.ACCAMPAMENTO && prossimaOccorrenza() <= MASSIMO_OCCORRENZE
				&& !RegistroIntermezzi.isScattatoNelMomento() && !unaMissioneHaUnIntermezzoInArrivo();
	}

	private static String idPer(int occorrenza) {
		return ClasseIntermezzo.INTERMEZZO_ACCAMPAMENTO.name() + "_" + occorrenza;
	}

	/**
	 * La prossima occorrenza (1-based) non ancora mostrata nella partita, trovata scorrendo
	 * {@link IntermezziMD#isScattato} finché non se ne trova una libera: se tutte fino a
	 * {@value #MASSIMO_OCCORRENZE} sono già state mostrate, restituisce un numero oltre il
	 * massimo.
	 */
	private static int prossimaOccorrenza() {
		IntermezziMD intermezziMD = ModelloDati.getIstanza().getIntermezziMD();
		int occorrenza = 1;
		while (occorrenza <= MASSIMO_OCCORRENZE && intermezziMD.isScattato(idPer(occorrenza))) {
			occorrenza++;
		}
		return occorrenza;
	}

	private static boolean unaMissioneHaUnIntermezzoInArrivo() {
		for (Missione missione : RegistroMissioni.getTutteLeMissioni()) {
			if (missione instanceof MissioneAPassi
					&& ((MissioneAPassi) missione).haUnIntermezzoInArrivo(MomentoControllo.ACCAMPAMENTO, MomentoIntermezzo.ACCAMPAMENTO)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		String[] scena = SCENE[(prossimaOccorrenza() - 1) % SCENE.length];
		ScenaFraCompagni pagina = ScenaFraCompagni.allAccampamento("Il gruppo si accampa per la notte.",
				GruppoGiocatore.getIstanza().getPersonaggiVivi()).conLuna(scena[0]);
		for (int i = 1; i < scena.length; i++) {
			if (i % 2 == 1) {
				pagina.parlaIlPrimo(scena[i]);
			} else {
				pagina.parlaIlSecondo(scena[i]);
			}
		}
		return pagina.getPagine();
	}
}
