package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Collections;
import java.util.List;

/**
 * Intermezzo per il checkpoint {@link MomentoIntermezzo#INGRESSO_ARMAIOLO}: scatta una
 * sola volta per l'intera partita, la prima volta che si entra dall'armaiolo di una
 * città. Il gruppo entra in scena camminando verso il centro (capo in testa, gli altri
 * in fila) e l'armaiolo saluta; in futuro spiegherà come funziona il negozio.
 */
public class IntermezzoArmaiolo implements Intermezzo {

	private static final double X_PARTENZA_PERSONAGGI = -0.15;
	private static final double X_TARGET_BASE = 0.55;
	private static final double DISTANZA_FRA_PERSONAGGI = 0.05;
	private static final double RITARDO_FRA_PARTENZE = 0.8;
	private static final double SECONDI_CAMMINATA = 2.0;
	private static final double MARGINE_DOPO_CAMMINATA = 0.3;

	private static final double X_ARMAIOLO = 0.65;
	private static final double Y_ARMAIOLO = 0.6;

	// Centrato come lo sfondo, di cui ha le stesse dimensioni
	private static final double X_FOREGROUND = 0.5;
	private static final double Y_FOREGROUND = 0.5;

	@Override
	public String getId() {
		return ClasseIntermezzo.INTERMEZZO_ARMAIOLO.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.INGRESSO_ARMAIOLO;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		List<Personaggio> personaggiVivi = GruppoGiocatore.getIstanza().getPersonaggiVivi();

		PaginaIntermezzo pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa("fondi/InternoArmaiolo.gif"))
				.conRitaglioSuSfondo()
				.conElemento(ElementoIntermezzo.di("armaiolo", ImmagineIntermezzo.risorsa("personaggi/Armaiolo.gif"), X_ARMAIOLO, Y_ARMAIOLO)
						.conBocca(0.5, -0.15));

		for (int i = 0; i < personaggiVivi.size(); i++) {
			ClassePersonaggio classe = personaggiVivi.get(i).getClasse();
			double targetX = X_TARGET_BASE - i * DISTANZA_FRA_PERSONAGGI;
			pagina.conElemento(ElementoIntermezzo.personaggio("personaggio" + i, classe, X_PARTENZA_PERSONAGGI, Y_ARMAIOLO)
					.orientaNelVersoDelMoto(VersoDiDefault.di(classe))
					// Ritardo scaglionato: ogni personaggio parte un secondo dopo il precedente
					.attendi(i * RITARDO_FRA_PARTENZE)
					.poi(Tappa.inSecondi(SECONDI_CAMMINATA).verso(targetX, Y_ARMAIOLO)));
		}

		double tempoFineCammino = (personaggiVivi.size() - 1) * RITARDO_FRA_PARTENZE + SECONDI_CAMMINATA;
		pagina.conBattuta(BattutaIntermezzo.di("armaiolo", "Benvenuto nella mia bottega da armaiolo. Compro e vendo armi, armature, elmi, tutto della migliore qualità.").daSecondo(tempoFineCammino + MARGINE_DOPO_CAMMINATA));

		// Aggiunto per ultimo così resta sopra a tutto il resto della scena
		pagina.conElemento(ElementoIntermezzo.di("foreground", ImmagineIntermezzo.risorsa("fondi/ForegroundArmaiolo.gif"), X_FOREGROUND, Y_FOREGROUND));

		return Collections.singletonList(pagina);
	}
}
