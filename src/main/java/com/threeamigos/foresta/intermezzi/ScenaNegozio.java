package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

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
	private final String idNegoziante;
	private final String idCapo;
	private final double secondoFineIngresso;
	private boolean primaBattuta = true;

	/**
	 * @param primoPiano l'immagine sopra a tutta la scena (il bancone), o null se non ce n'è
	 */
	ScenaNegozio(String sfondo, String primoPiano, String idNegoziante, ElementoIntermezzo negoziante,
				 double yPersonaggi, double xArrivoCapo, double ritardoFraPartenze) {
		this.idNegoziante = idNegoziante;
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		List<Personaggio> personaggiVivi = gruppo.getPersonaggiVivi();

		pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa(sfondo))
				.conRitaglioSuSfondo()
				.conElemento(negoziante.conBocca(0.5, -0.15));

		String capo = null;
		for (int i = 0; i < personaggiVivi.size(); i++) {
			Personaggio personaggio = personaggiVivi.get(i);
			ClassePersonaggio classe = personaggio.getClasse();
			String idElemento = "personaggio" + i;
			double xArrivo = xArrivoCapo - i * DISTANZA_FRA_PERSONAGGI;
			ElementoIntermezzo elemento = ElementoIntermezzo.personaggio(idElemento, classe, X_PARTENZA_PERSONAGGI, yPersonaggi)
					.orientaNelVersoDelMoto(VersoDiDefault.di(classe))
					// Ritardo scaglionato: ogni personaggio parte un po' dopo il precedente
					.attendi(i * ritardoFraPartenze)
					.poi(Tappa.inSecondi(SECONDI_CAMMINATA).verso(xArrivo, yPersonaggi));
			if (personaggio == gruppo.getCapo()) {
				capo = idElemento;
				elemento.conBocca(0.5, -0.15);
			}
			pagina.conElemento(elemento);
		}
		idCapo = capo;
		secondoFineIngresso = (personaggiVivi.size() - 1) * ritardoFraPartenze + SECONDI_CAMMINATA + MARGINE_DOPO_CAMMINATA;

		// Aggiunto per ultimo così resta sopra a tutto il resto della scena
		if (primoPiano != null) {
			pagina.conElemento(ElementoIntermezzo.di("foreground", ImmagineIntermezzo.risorsa(primoPiano), X_PRIMO_PIANO, Y_PRIMO_PIANO));
		}
	}

	ScenaNegozio parlaIlNegoziante(String testo) {
		return battuta(idNegoziante, testo);
	}

	ScenaNegozio parlaIlCapo(String testo) {
		return battuta(idCapo, testo);
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
