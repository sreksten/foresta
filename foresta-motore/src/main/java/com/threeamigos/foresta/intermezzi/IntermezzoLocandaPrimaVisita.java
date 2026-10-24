package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.testi.ProduttoreDiFiabeCasuale;
import com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Intermezzo per i checkpoint {@link MomentoIntermezzo#INIZIO_LOCAZIONE} (locanda nel
 * bosco) e {@link MomentoIntermezzo#INGRESSO_LOCANDA_IN_CITTA} (locanda in città): scatta
 * alla prima visita del gruppo a una locanda non ancora visitata. Il gruppo entra in scena
 * camminando verso il centro (capo in testa), un bardo saluta con "C'era una volta...",
 * il locandiere lo zittisce e pronuncia il dialogo di benvenuto specifico di quella
 * locanda.
 * <p>
 * A differenza degli altri {@link Intermezzo}, che scattano una volta sola per l'intera
 * partita, questo deve scattare una volta per ogni locanda: {@link #getId()} include
 * quindi l'identificativo della locanda corrente, cosa possibile perché
 * {@link FabbricaIntermezzi#crea} crea una nuova istanza a ogni controllo e
 * {@link com.threeamigos.foresta.modellodati.IntermezziMD} è un semplice insieme
 * di stringhe senza vincoli di formato.
 * <p>
 * Gli ospiti vivi del gruppo entrano subito dopo il capo, prima degli altri personaggi, come nelle scene dei negozi
 * (ScenaNegozio). Mentre il bardo viaggia con il gruppo come ospite (NonSparateSulPianista lo riporta a casa) non può
 * essere anche seduto in un angolo della locanda: entra con il gruppo, al suo posto di ospite, barcollando; niente fiaba
 * né reazione del locandiere, che dà soltanto il benvenuto.
 */
public class IntermezzoLocandaPrimaVisita implements Intermezzo {

	private static final String REGEX_PER_SPEZZARE_FRASI = "(?<=[.!?][\"“”])(?!\\s*,)(?=(?:[^\"“”]*[\"“”][^\"“”]*[\"“”])*[^\"“”]*$)"
			+ "|(?<=[.!?])(?![.!?])(?![\"“”])(?=(?:[^\"“”]*[\"“”][^\"“”]*[\"“”])*[^\"“”]*$)";

	private static final double X_PARTENZA_PERSONAGGI = -0.15;
	private static final double X_TARGET_BASE = 0.5;
	private static final double DISTANZA_FRA_PERSONAGGI = 0.05;
	private static final double RITARDO_FRA_PARTENZE = 0.6;
	private static final double SECONDI_CAMMINATA = 2.0;
	private static final double MARGINE_DOPO_CAMMINATA = 0.3;

	private static final double X_LOCANDIERE = 0.65;
	private static final double Y_LOCANDIERE = 0.6;
	private static final double X_BARDO = 0.60;
	private static final double Y_BARDO = 0.6;
	// Il bardo ospite, ubriaco, entra barcollando: tanti tratti a zig-zag, alti e bassi di tanto, e più lento degli altri
	private static final int PASSI_BARCOLLANTI = 6;
	private static final double OSCILLAZIONE_BARDO = 0.015;
	private static final double FATTORE_LENTEZZA_BARDO = 1.5;

	// Centrato come lo sfondo, di cui ha le stesse dimensioni
	private static final double X_FOREGROUND = 0.5;
	private static final double Y_FOREGROUND = 0.5;

	@Override
	public String getId() {
		String identificativo = Locande.getIdentificativoLocandaCorrente();
		return TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA.name()
				+ (identificativo == null ? "" : "_" + identificativo);
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		if (!Locande.momentoCoerenteConLocazioneCorrente(momento)) {
			return false;
		}
		LocazioneMD locazioneMD = Locande.getLocazioneMDLocandaCorrente();
		return locazioneMD != null && locazioneMD.ottieniProprieta(Locanda.LOCANDA_VISITATA) == null && !staPerAffidareIlBardo();
	}

	/**
	 * Se in questa locanda, mai visitata, il locandiere sta per chiedere di riportare a casa il bardo (NonSparateSulPianista):
	 * la sua scena fa da benvenuto, e il bardo non può essere anche seduto nell'angolo a raccontare la fiaba.
	 */
	private static boolean staPerAffidareIlBardo() {
		return RegistroMissioni.getTutteLeMissioni().stream()
				.anyMatch(m -> m instanceof NonSparateSulPianista && ((NonSparateSulPianista) m).staPerAffidareIlBardo());
	}

	/**
	 * Chi entra camminando, al suo posto in fila: il posto decide quando parte e dove si ferma.
	 */
	private static ElementoIntermezzo cammina(String id, TipoPersonaggio classe, int posizione) {
		double targetX = X_TARGET_BASE - posizione * DISTANZA_FRA_PERSONAGGI;
		return ElementoIntermezzo.personaggio(id, classe, X_PARTENZA_PERSONAGGI, Y_BARDO)
				.orientaNelVersoDelMoto()
				// Ritardo scaglionato: ognuno parte un po' dopo il precedente
				.attendi(posizione * RITARDO_FRA_PARTENZE)
				.poi(Tappa.inSecondi(SECONDI_CAMMINATA).verso(targetX, Y_BARDO));
	}

	/**
	 * Il bardo ospite del gruppo entra al posto che gli spetta in fila: è ubriaco, quindi barcolla (avanza a zig-zag) ed
	 * è più lento degli altri.
	 */
	private static ElementoIntermezzo bardoCheBarcolla(int posizione) {
		double targetX = X_TARGET_BASE - posizione * DISTANZA_FRA_PERSONAGGI;
		double secondiPerPasso = SECONDI_CAMMINATA * FATTORE_LENTEZZA_BARDO / PASSI_BARCOLLANTI;
		ElementoIntermezzo bardo = ElementoIntermezzo.personaggio("bardoOspite", TipoPersonaggio.BARDO_DELLE_LOCANDE,
						X_PARTENZA_PERSONAGGI, Y_BARDO)
				.orientaNelVersoDelMoto()
				.attendi(posizione * RITARDO_FRA_PARTENZE);
		for (int passo = 1; passo <= PASSI_BARCOLLANTI; passo++) {
			double x = X_PARTENZA_PERSONAGGI + (targetX - X_PARTENZA_PERSONAGGI) * passo / PASSI_BARCOLLANTI;
			// Un passo su, uno giù; l'ultimo lo rimette in fila
			double y = passo == PASSI_BARCOLLANTI ? Y_BARDO : Y_BARDO + (passo % 2 == 0 ? OSCILLAZIONE_BARDO : -OSCILLAZIONE_BARDO);
			bardo.poi(Tappa.inSecondi(secondiPerPasso).verso(x, y));
		}
		return bardo;
	}

	/**
	 * Il bardo della locanda è ospite del gruppo (lo stanno riportando a casa).
	 */
	private static boolean ilBardoViaggiaColGruppo() {
		return GruppoGiocatore.getIstanza().getOspiti().stream()
				.anyMatch(ospite -> ospite.getClasse() == TipoPersonaggio.BARDO_DELLE_LOCANDE);
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		LocazioneMD locazioneMD = Locande.getLocazioneMDLocandaCorrente();
		String dialogo = locazioneMD.ottieniProprieta(Locanda.LOCANDA_DIALOGO);

		List<Personaggio> personaggiVivi = GruppoGiocatore.getIstanza().getPersonaggiVivi();
		boolean bardoInScena = !ilBardoViaggiaColGruppo();

		PaginaIntermezzo pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa("fondinon2x2/InternoLocanda.gif"))
				.conRitaglioSuSfondo()
				.conElemento(ElementoIntermezzo.personaggio("locandiere", TipoPersonaggio.LOCANDIERE, X_LOCANDIERE, Y_LOCANDIERE)
						.conBocca(0.5, -0.15));
		if (bardoInScena) {
			pagina.conElemento(ElementoIntermezzo.personaggio("bardo", TipoPersonaggio.BARDO_DELLE_LOCANDE, X_BARDO, Y_BARDO)
					.conBocca(0.5, -0.15));
		}

		// Chi entra, nell'ordine: i personaggi, con gli ospiti subito dopo il capo
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		List<Personaggio> ospiti = gruppo.getOspiti().stream().filter(Personaggio::isVivo).collect(Collectors.toList());
		int posto = 0;
		for (int i = 0; i < personaggiVivi.size(); i++) {
			Personaggio personaggio = personaggiVivi.get(i);
			pagina.conElemento(cammina("personaggio" + i, personaggio.getClasse(), posto++));
			if (personaggio == gruppo.getCapo()) {
				for (int k = 0; k < ospiti.size(); k++) {
					TipoPersonaggio classe = ospiti.get(k).getClasse();
					pagina.conElemento(classe == TipoPersonaggio.BARDO_DELLE_LOCANDE
							? bardoCheBarcolla(posto++) : cammina("ospite" + k, classe, posto++));
				}
			}
		}

		//double tempoFineCammino = (personaggiVivi.size() - 1) * RITARDO_FRA_PARTENZE + SECONDI_CAMMINATA;
		//pagina.conBattuta(BattutaIntermezzo.di("bardo", "C'era una volta...").daSecondo(tempoFineCammino + MARGINE_DOPO_CAMMINATA));

		// La fiaba del bardo e il locandiere che lo zittisce, se il bardo è seduto nella locanda
		if (bardoInScena) {
			String fiaba = ProduttoreDiFiabeCasuale.fiaba().get(0);
			Collection<String> frasiFiaba = Arrays.stream(fiaba.split(REGEX_PER_SPEZZARE_FRASI))
					.map(String::trim)
					.collect(Collectors.toList());

			frasiFiaba.forEach(frase -> pagina.conBattuta(BattutaIntermezzo.di("bardo", frase)));

			String reazioneLocandiere = ProduttoreDiLocandeCasuale.reazioneLocandiere();
			Collection<String> frasiReazioneLocandiere = Arrays.stream(reazioneLocandiere.split(REGEX_PER_SPEZZARE_FRASI))
					.map(String::trim)
					.collect(Collectors.toList());

			frasiReazioneLocandiere.forEach(frase -> pagina.conBattuta(BattutaIntermezzo.di("locandiere", frase)));
		}

		Collection<String> frasiLocandiere = Arrays.stream(dialogo.split(REGEX_PER_SPEZZARE_FRASI))
				.map(String::trim)
				.collect(Collectors.toList());

		frasiLocandiere.forEach(frase -> pagina.conBattuta(BattutaIntermezzo.di("locandiere", frase)));

		//pagina.conBattuta(BattutaIntermezzo.di("locandiere", dialogo).conLarghezza(0.8).centrataOrizzontalmente());

		// Aggiunto per ultimo così resta sopra a tutto il resto della scena
		pagina.conElemento(ElementoIntermezzo.di("foreground", ImmagineIntermezzo.risorsa("fondinon2x2/ForegroundLocanda.gif"), X_FOREGROUND, Y_FOREGROUND));

		// Evita che Locanda.descrivi(), chiamato subito dopo con la locanda ormai
		// costruita, ripeta il dialogo o anticipi la recensione appena mostrati qui
		locazioneMD.aggiungiProprieta(Locanda.LOCANDA_DIALOGO_LETTO, LocazioneMD.AFFERMATIVO);
		locazioneMD.aggiungiProprieta(Locanda.LOCANDA_RECENSIONE_LETTA, LocazioneMD.AFFERMATIVO);

		List<PaginaIntermezzo> pagineIntermezzo = new ArrayList<>();
		pagineIntermezzo.add(pagina);
		return pagineIntermezzo;
	}
}
