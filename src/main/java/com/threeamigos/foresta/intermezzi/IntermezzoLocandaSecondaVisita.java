package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermezzo per i checkpoint {@link MomentoIntermezzo#INIZIO_LOCAZIONE} (locanda nel
 * bosco) e {@link MomentoIntermezzo#INGRESSO_LOCANDA_IN_CITTA} (locanda in città): scatta
 * alla seconda visita del gruppo a una locanda (qualunque locanda), e solo se il gruppo ha più
 * di un personaggio. Niente bardo questa volta: l'oste è sulla destra, il gruppo avanza
 * con il capo in testa fino a poco oltre il centro, gli altri lo seguono in fila. Il capo
 * si volta verso il gruppo (guarda a sinistra, mentre tutti gli altri, fermi dove sono
 * arrivati camminando verso destra, continuano a guardare a destra) e racconta al secondo
 * personaggio la recensione della locanda.
 * <p>
 * Stesso trucco di {@link IntermezzoLocandaPrimaVisita} per scattare una volta per ogni
 * locanda: {@link #getId()} include l'identificativo della locanda corrente.
 */
public class IntermezzoLocandaSecondaVisita implements Intermezzo {

	private static final double X_PARTENZA_PERSONAGGI = -0.15;
	private static final double X_TARGET_CAPO = 0.55;
	private static final double Y_PERSONAGGI = 0.6;
	private static final double DISTANZA_FRA_PERSONAGGI = 0.08;
	private static final double RITARDO_FRA_PARTENZE = 0.4;
	private static final double SECONDI_CAMMINATA = 2.0;
	private static final double SECONDI_VOLTATA_CAPO = 0.1;
	private static final double MARGINE_DOPO_CAMMINATA = 0.3;

	private static final double X_OSTE = 0.68;

	// Centrato come lo sfondo, di cui ha le stesse dimensioni
	private static final double X_FOREGROUND = 0.5;
	private static final double Y_FOREGROUND = 0.5;

	@Override
	public String getId() {
		String identificativo = Locande.getIdentificativoLocandaCorrente();
		return TipoIntermezzo.INTERMEZZO_LOCANDA_SECONDA_VISITA.name()
				+ (identificativo == null ? "" : "_" + identificativo);
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		if (!Locande.momentoCoerenteConLocazioneCorrente(momento)) {
			return false;
		}
		LocazioneMD locazioneMD = Locande.getLocazioneMDLocandaCorrente();
		if (locazioneMD == null || locazioneMD.ottieniProprieta(Locanda.LOCANDA_VISITATA) == null) {
			return false;
		}
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		return gruppo.getPersonaggiVivi().size() > 1 && gruppo.getCapo().isVivo();
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		LocazioneMD locazioneMD = Locande.getLocazioneMDLocandaCorrente();
		String recensione = locazioneMD.ottieniProprieta(Locanda.LOCANDA_RECENSIONE);

		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		List<Personaggio> personaggiVivi = gruppo.getPersonaggiVivi();
		Personaggio capo = gruppo.getCapo();

		PaginaIntermezzo pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa("fondi/InternoLocanda.gif"))
				.conRitaglioSuSfondo()
				.conElemento(ElementoIntermezzo.di("oste", ImmagineIntermezzo.risorsa("personaggi/Locandiere.gif"), X_OSTE, Y_PERSONAGGI));

		String idElementoCapo = null;
		for (int i = 0; i < personaggiVivi.size(); i++) {
			Personaggio personaggio = personaggiVivi.get(i);
			TipoPersonaggio classe = personaggio.getClasse();
			String idElemento = "personaggio" + i;
			double targetX = X_TARGET_CAPO - i * DISTANZA_FRA_PERSONAGGI;
			ElementoIntermezzo elemento = ElementoIntermezzo.personaggio(idElemento, classe, X_PARTENZA_PERSONAGGI, Y_PERSONAGGI)
					.orientaNelVersoDelMoto(VersoDiDefault.di(classe))
					// Ritardo scaglionato: ogni personaggio parte un secondo dopo il precedente
					.attendi(i * RITARDO_FRA_PARTENZE)
					.poi(Tappa.inSecondi(SECONDI_CAMMINATA).verso(targetX, Y_PERSONAGGI));
			if (personaggio == capo) {
				idElementoCapo = idElemento;
				// Arrivato, il capo si volta verso il gruppo per parlare con il secondo
				// personaggio; gli altri, fermi dopo aver camminato verso destra, restano
				// voltati in quella direzione
				elemento.conBocca(0.5, -0.15)
						.poi(Tappa.inSecondi(SECONDI_VOLTATA_CAPO).specchiata(VersoDiDefault.serveSpecchiare(classe, Verso.SINISTRA)));
			}
			pagina.conElemento(elemento);
		}

		double tempoFineCammino = (personaggiVivi.size() - 1) * RITARDO_FRA_PARTENZE + SECONDI_CAMMINATA;
		pagina.conBattuta(BattutaIntermezzo.di(idElementoCapo, recensione).daSecondo(tempoFineCammino + MARGINE_DOPO_CAMMINATA));

		// Aggiunto per ultimo così resta sopra a tutto il resto della scena
		pagina.conElemento(ElementoIntermezzo.di("foreground", ImmagineIntermezzo.risorsa("fondi/ForegroundLocanda.gif"), X_FOREGROUND, Y_FOREGROUND));

		List<PaginaIntermezzo> pagineIntermezzo = new ArrayList<>();
		pagineIntermezzo.add(pagina);
		return pagineIntermezzo;
	}
}
