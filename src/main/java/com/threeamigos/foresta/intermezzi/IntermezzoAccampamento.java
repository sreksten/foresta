package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.IntermezziMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

import java.util.List;

/**
 * Intermezzo che scatta quando il gruppo si accampa: stesso sfondo e fuoco di
 * {@link IntermezzoIntroduttivo}, con il gruppo intorno al fuoco (vedi {@link ScenaFraCompagni}). A differenza degli altri
 * intermezzi scatta una volta per accampamento (non di più nello stesso ciclo, vedi
 * {@link com.threeamigos.foresta.motore.Automa}), fino a un massimo di
 * {@value #MASSIMO_OCCORRENZE} accampamenti nella partita: {@link #getId()} include quindi
 * il numero dell'accampamento corrente ({@link IntermezziMD#getNumeroAccampamenti()}), cosa
 * possibile perché {@link ClasseIntermezzo#getIstanza()} crea una nuova istanza a ogni
 * controllo e {@link IntermezziMD} è un semplice insieme di stringhe senza vincoli di formato.
 * <p>
 * Ogni accampamento ha la sua scena, con la sua luna e le sue battute: la storia di quello trasformato in scarafaggio
 * sotto una luna normale, la luna che è una stazione da battaglia, e la luna strana di cui non farsi prendere dal panico.
 */
public class IntermezzoAccampamento implements Intermezzo {

	private static final int MASSIMO_OCCORRENZE = 3;

	/**
	 * Le scene, una per accampamento: l'immagine della luna e le battute, alternate fra il primo e il secondo
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
		return ClasseIntermezzo.INTERMEZZO_ACCAMPAMENTO.name() + "_" + numeroAccampamentoCorrente();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.ACCAMPAMENTO && numeroAccampamentoCorrente() <= MASSIMO_OCCORRENZE;
	}

	private static int numeroAccampamentoCorrente() {
		return ModelloDati.getIstanza().getIntermezziMD().getNumeroAccampamenti();
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		String[] scena = SCENE[(numeroAccampamentoCorrente() - 1) % SCENE.length];
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
