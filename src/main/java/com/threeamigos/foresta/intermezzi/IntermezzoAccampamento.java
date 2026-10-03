package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.IntermezziMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermezzo che scatta quando il gruppo si accampa: stesso sfondo e fuoco di
 * {@link IntermezzoIntroduttivo}. Il primo personaggio è a sinistra del fuoco e lo guarda
 * sulla sua destra, il secondo è alla destra del fuoco e lo guarda sulla sua sinistra; un
 * eventuale terzo e quarto personaggio stanno più in alto, fra il primo/secondo e il fuoco,
 * e un eventuale quinto ancora più in alto, sopra il fuoco. A differenza degli altri
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

	private static final double X_FUOCO = 0.5;
	private static final double Y_FUOCO = 0.68;

	private static final double X_PRIMO = 0.38;
	private static final double Y_PRIMO = 0.61;
	private static final double X_SECONDO = 0.62;
	private static final double Y_SECONDO = 0.61;
	private static final double X_TERZO = 0.44;
	private static final double Y_TERZO = 0.56;
	private static final double X_QUARTO = 0.56;
	private static final double Y_QUARTO = 0.56;
	private static final double X_QUINTO = 0.5;
	private static final double Y_QUINTO = 0.5;

	// La luna sorge da dietro gli alberi, sulla destra, e sale nel cielo
	private static final double X_LUNA_INIZIO = 0.40;
	private static final double Y_LUNA_INIZIO = 0.20;
	private static final double X_LUNA_FINE = 0.88;
	private static final double Y_LUNA_FINE = 0.18;
	private static final double SECONDI_SALITA_LUNA = 6;

	/**
	 * Le scene, una per accampamento: l'immagine della luna e le battute, alternate fra il primo e il secondo
	 * personaggio.
	 */
	private static final String[][] SCENE = {
			{"fondi/Luna.gif",
					"Una volta ho sentito di uno che si è svegliato trasformato in uno scarafaggio. O forse era un asino d'oro.",
					"Passi troppo tempo ad ascoltare le storie nelle locande."},
			{"fondi/LunaSW.gif",
					"Hai visto che luna stasera?",
					"Quella non è una luna... è una stazione da battaglia!",
					"Passi troppo tempo ad ascoltare le storie nelle locande."},
			{"fondi/LunaHHGTTG.gif",
					"Che luna strana stasera.",
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
		List<Personaggio> personaggiVivi = GruppoGiocatore.getIstanza().getPersonaggiVivi();
		String[] scena = SCENE[(numeroAccampamentoCorrente() - 1) % SCENE.length];

		// Niente conRitaglioSuSfondo(): la luna deve salire liberamente nel cielo, ben oltre
		// il piccolo riquadro occupato dall'immagine di sfondo (a differenza degli altri
		// personaggi, statici e già dentro quel riquadro).
		PaginaIntermezzo pagina = new PaginaIntermezzo("Il gruppo si accampa per la notte.")
				.conSfondo(ImmagineIntermezzo.locazione(ClassiLocazione.BOSCO));

		// La luna è nel cielo, più lontana di tutto il resto della scena
		pagina.conElemento(ElementoIntermezzo.di("luna", ImmagineIntermezzo.risorsa(scena[0]),
						X_LUNA_INIZIO, Y_LUNA_INIZIO)
				.poi(Tappa.inSecondi(SECONDI_SALITA_LUNA).verso(X_LUNA_FINE, Y_LUNA_FINE)));

		// Disegnati dal più lontano al più vicino (quinto, quarto, terzo, secondo, primo
		// e infine il fuoco) così gli elementi davanti coprono quelli dietro.
		if (personaggiVivi.size() > 4) {
			pagina.conElemento(personaggioVersoDestra("personaggio4", personaggiVivi.get(4).getClasse(), X_QUINTO, Y_QUINTO));
		}
		if (personaggiVivi.size() > 3) {
			pagina.conElemento(personaggioVersoSinistra("personaggio3", personaggiVivi.get(3).getClasse(), X_QUARTO, Y_QUARTO));
		}
		if (personaggiVivi.size() > 2) {
			pagina.conElemento(personaggioVersoDestra("personaggio2", personaggiVivi.get(2).getClasse(), X_TERZO, Y_TERZO));
		}
		pagina.conElemento(personaggioVersoSinistra("personaggio1", personaggiVivi.get(1).getClasse(), X_SECONDO, Y_SECONDO)
				.conBocca(0.5, -0.15));
		pagina.conElemento(personaggioVersoDestra("personaggio0", personaggiVivi.get(0).getClasse(), X_PRIMO, Y_PRIMO)
				.conBocca(0.5, -0.15));
		pagina.conElemento(ElementoIntermezzo.di("fuoco", ImmagineIntermezzo.animazione(Animazione.FUOCO_DA_CAMPO), X_FUOCO, Y_FUOCO));
		for (int i = 1; i < scena.length; i++) {
			pagina.conBattuta(BattutaIntermezzo.di(i % 2 == 1 ? "personaggio0" : "personaggio1", scena[i]));
		}

		List<PaginaIntermezzo> pagineIntermezzo = new ArrayList<>();
		pagineIntermezzo.add(pagina);
		return pagineIntermezzo;
	}

	private static ElementoIntermezzo personaggioVersoDestra(String id, ClassePersonaggio classe, double x, double y) {
		ElementoIntermezzo elemento = ElementoIntermezzo.personaggio(id, classe, x, y);
		if (VersoDiDefault.serveSpecchiare(classe, Verso.DESTRA)) {
			elemento.specchiato();
		}
		return elemento;
	}

	private static ElementoIntermezzo personaggioVersoSinistra(String id, ClassePersonaggio classe, double x, double y) {
		ElementoIntermezzo elemento = ElementoIntermezzo.personaggio(id, classe, x, y);
		if (VersoDiDefault.serveSpecchiare(classe, Verso.SINISTRA)) {
			elemento.specchiato();
		}
		return elemento;
	}
}
