package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Collections;
import java.util.List;

/**
 * La pagina di un intermezzo in un negozio o in una locanda: il negoziante è al suo posto,
 * il gruppo entra camminando verso il centro (capo in testa, gli altri in fila) e, appena
 * arrivato, cominciano le battute di negoziante e capo. La usa anche {@link ScenaInCitta}, all'aperto e senza
 * primo piano.
 */
final class ScenaNegozio {

	private static final double X_PARTENZA_PERSONAGGI = -0.15;
	private static final double DISTANZA_FRA_PERSONAGGI = 0.05;
	private static final double SECONDI_CAMMINATA = 2.0;
	private static final double MARGINE_DOPO_CAMMINATA = 0.3;

	// Centrato come lo sfondo, di cui ha le stesse dimensioni
	private static final double X_PRIMO_PIANO = 0.5;
	private static final double Y_PRIMO_PIANO = 0.5;

	private final PaginaIntermezzo pagina;
	static final String ID_OSPITE = "ospite";

	private final String idNegoziante;
	private final String idCapo;
	private final double secondoFineIngresso;
	private boolean primaBattuta = true;

	/**
	 * Senza ospite: vedi {@link #ScenaNegozio(String, String, String, ElementoIntermezzo, double, double, double, TipoPersonaggio)}.
	 */
	ScenaNegozio(String sfondo, String primoPiano, String idNegoziante, ElementoIntermezzo negoziante,
				 double yPersonaggi, double xArrivoCapo, double ritardoFraPartenze) {
		this(sfondo, primoPiano, idNegoziante, negoziante, yPersonaggi, xArrivoCapo, ritardoFraPartenze, null);
	}

	/**
	 * @param primoPiano l'immagine sopra a tutta la scena (il bancone), o null se non ce n'è
	 * @param ospite la classe di chi la missione ha scortato fin qui (un ostaggio liberato, un colpevole catturato), o
	 *               null: entra subito dopo il capo, prima degli altri, camminando come loro verso il negoziante, e
	 *               quindi lo guarda. Non è nel gruppo (la missione lo ha già congedato), perciò la scena non lo trova:
	 *               lo dice chi la costruisce
	 */
	ScenaNegozio(String sfondo, String primoPiano, String idNegoziante, ElementoIntermezzo negoziante,
				 double yPersonaggi, double xArrivoCapo, double ritardoFraPartenze, TipoPersonaggio ospite) {
		this.idNegoziante = idNegoziante;
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		List<Personaggio> personaggiVivi = gruppo.getPersonaggiVivi();

		pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa(sfondo))
				.conRitaglioSuSfondo()
				.conElemento(negoziante.conBocca(0.5, -0.15));

		String capo = null;
		// Chi entra, nell'ordine: i personaggi, con l'ospite subito dopo il capo. Il posto in fila decide quando parte
		// e dove si ferma.
		int posto = 0;
		for (int i = 0; i < personaggiVivi.size(); i++) {
			Personaggio personaggio = personaggiVivi.get(i);
			String idElemento = "personaggio" + i;
			ElementoIntermezzo elemento = cammina(idElemento, personaggio.getClasse(), posto++, xArrivoCapo, yPersonaggi, ritardoFraPartenze);
			if (personaggio == gruppo.getCapo()) {
				capo = idElemento;
				elemento.conBocca(0.5, -0.15);
			}
			pagina.conElemento(elemento);
			if (personaggio == gruppo.getCapo() && ospite != null) {
				pagina.conElemento(cammina(ID_OSPITE, ospite, posto++, xArrivoCapo, yPersonaggi, ritardoFraPartenze)
						.conBocca(0.5, -0.15));
			}
		}
		idCapo = capo;
		secondoFineIngresso = (posto - 1) * ritardoFraPartenze + SECONDI_CAMMINATA + MARGINE_DOPO_CAMMINATA;

		// Aggiunto per ultimo così resta sopra a tutto il resto della scena
		if (primoPiano != null) {
			pagina.conElemento(ElementoIntermezzo.di("foreground", ImmagineIntermezzo.risorsa(primoPiano), X_PRIMO_PIANO, Y_PRIMO_PIANO));
		}
	}

	/**
	 * Un personaggio che entra: parte da fuori schermo con un ritardo che dipende dal suo posto in fila, cammina fino al
	 * suo posto (il primo è il capo, poi uno ogni DISTANZA_FRA_PERSONAGGI verso sinistra) e guarda dalla parte in cui
	 * va, cioè verso il negoziante.
	 */
	private static ElementoIntermezzo cammina(String id, TipoPersonaggio classe, int posto, double xArrivoCapo,
											  double yPersonaggi, double ritardoFraPartenze) {
		return ElementoIntermezzo.personaggio(id, classe, X_PARTENZA_PERSONAGGI, yPersonaggi)
				.orientaNelVersoDelMoto(VersoDiDefault.di(classe))
				// Ritardo scaglionato: ogni personaggio parte un po' dopo il precedente
				.attendi(posto * ritardoFraPartenze)
				.poi(Tappa.inSecondi(SECONDI_CAMMINATA).verso(xArrivoCapo - posto * DISTANZA_FRA_PERSONAGGI, yPersonaggi));
	}

	ScenaNegozio parlaIlNegoziante(String testo) {
		return battuta(idNegoziante, testo);
	}

	ScenaNegozio parlaIlCapo(String testo) {
		return battuta(idCapo, testo);
	}

	ScenaNegozio parlaLOspite(String testo) {
		return battuta(ID_OSPITE, testo);
	}

	/**
	 * La prima battuta aspetta che il gruppo sia arrivato, le altre la seguono.
	 */
	private ScenaNegozio battuta(String idElemento, String testo) {
		BattutaIntermezzo battuta = BattutaIntermezzo.di(idElemento, testo);
		if (primaBattuta) {
			battuta.daSecondo(secondoFineIngresso);
			primaBattuta = false;
		}
		pagina.conBattuta(battuta);
		return this;
	}

	List<PaginaIntermezzo> getPagine() {
		return Collections.singletonList(pagina);
	}
}
