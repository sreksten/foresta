package com.threeamigos.foresta.missioni.principali;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.*;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.OffertaDelSacerdote;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Collections;
import java.util.List;

/**
 * Sette templi liberati dai mostri che li infestano. La missione parte quando il gruppo ripulisce il primo tempio: a
 * locazione conclusa si presenta un sacerdote o una sacerdotessa (a caso, e sempre lo stesso fino alla fine) che chiede
 * di liberarne altri sei. Ogni tempio ripulito, il primo compreso, viene segnato sulla casella
 * ({@link Tempio#ripulisci}) e diventa una locazione sicura, senza mostri né oggetti. Al settimo il sacerdote
 * ringrazia a nome dei devoti e regala due incantesimi della Luce Divina, due dell'Alba Sacra e uno elementale a caso.
 * <ol>
 * <li>INIZIO, a fine locazione, se il gruppo ha ripulito un tempio: l'intermezzo della presentazione;</li>
 * <li>TEMPLI, a fine locazione: ogni tempio ripulito si segna e si conta;</li>
 * <li>RINGRAZIAMENTO, subito: l'intermezzo del ringraziamento e il premio;</li>
 * <li>COMPAGNO, se il gruppo ha posto e nessun sacerdote offerto da una missione è già entrato (vedi {@link OffertaDelSacerdote}): il sacerdote
 * si offre di unirsi al gruppo, a fine locazione, subito dopo il settimo tempio.</li>
 * </ol>
 * Un tempio è ripulito quando il gruppo ne esce dopo aver sconfitto tutti i suoi custodi: fuggire non basta.
 */
public class IlGiroDelleSetteChiese extends MissioneAPassi {

	public static final int TEMPLI_DA_LIBERARE = 7;
	/**
	 * Gli incantesimi che i devoti regalano a fine missione, uno per tipo.
	 */
	public static final TipoIncantesimo[] INCANTESIMI_IN_DONO = {TipoIncantesimo.ARIA, TipoIncantesimo.ACQUA,
			TipoIncantesimo.TERRA, TipoIncantesimo.FUOCO, TipoIncantesimo.FULMINE};

	private static final String INIZIO = "INIZIO";
	private static final String TEMPLI = "TEMPLI";
	private static final String RINGRAZIAMENTO = "RINGRAZIAMENTO";
	private static final String COMPAGNO = "COMPAGNO";
	private static final String SACERDOTE = "SACERDOTE";

	private static final String ID_EROE = "eroe";
	private static final String ID_SACERDOTE = "sacerdote";

	public IlGiroDelleSetteChiese() {
		super(MissionePredefinita.IL_GIRO_DELLE_SETTE_CHIESE);
	}

	@Override
	public String getNome() {
		return "Il Giro delle Sette Chiese";
	}

	@Override
	public String getDescrizione() {
		String cardinale = Numeri.getCardinaleM(TEMPLI_DA_LIBERARE);
		if (isCompleta()) {
			return "Sette templi liberati dai mostri: i devoti sono tornati in pellegrinaggio.";
		}
		if (!isAttiva()) {
			return "Qualcuno, lassù, apprezzerebbe che un tempio infestato tornasse a essere un luogo di preghiera.";
		}
		int mancanti = TEMPLI_DA_LIBERARE - contaTempliRipuliti();
		String descrizione = "Aiuta " + (getSacerdote() == TipoPersonaggio.SACERDOTESSA ? "la sacerdotessa" : "il sacerdote") + " a liberare " + cardinale
				+ " templi dai mostri che li infestano.";
		if (mancanti == 1) {
			return descrizione + " Ne manca uno.";
		}
		return descrizione + " Ne mancano " + Numeri.getCardinaleM(mancanti) + ".";
	}

	/**
	 * La classe di chi chiede aiuto: SACERDOTE o SACERDOTESSA, a seconda di come è andato il sorteggio. Prima che la
	 * missione parta è un SACERDOTE.
	 */
	private TipoPersonaggio getSacerdote() {
		String classe = ottieniProprieta(SACERDOTE);
		return classe == null ? TipoPersonaggio.SACERDOTE : TipoPersonaggio.valueOf(classe);
	}

	/**
	 * Il tempio in cui il gruppo si trova adesso è stato appena ripulito: i suoi custodi sono tutti sconfitti.
	 * Un tempio già ripulito non conta di nuovo.
	 */
	private boolean isTempioAppenaRipulito() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		return gruppo.getTipoLocazioneCorrente() == TipoLocazione.TEMPIO
				&& GruppoAvversario.getIstanza().getPersonaggiVivi().isEmpty()
				&& !Tempio.isRipulito(Foresta.getLocazioneMD(gruppo.getCoordinate()));
	}

	private void ripulisciIlTempioCorrente() {
		Tempio.ripulisci(Foresta.getLocazioneMD(GruppoGiocatore.getIstanza().getCoordinate()));
	}

	private void scegliIlSacerdote() {
		aggiungiProprieta(SACERDOTE, (Dado.tiraAncheAUnaFaccia(2) == 1 ? TipoPersonaggio.SACERDOTE : TipoPersonaggio.SACERDOTESSA).name());
		ripulisciIlTempioCorrente();
		attivaMissione();
	}

	private void contaIlTempio() {
		if (!isTempioAppenaRipulito()) {
			return;
		}
		ripulisciIlTempioCorrente();
		int ripuliti = contaTempliRipuliti();
		if (ripuliti < TEMPLI_DA_LIBERARE) {
			BusEventi.pubblica(new NotificaTestoParagrafo("Il tempio è libero dai mostri: sono " + Numeri.getCardinaleM(ripuliti)
					+ " su " + Numeri.getCardinaleM(TEMPLI_DA_LIBERARE) + "."));
		}
	}

	private int contaTempliRipuliti() {
		int ripuliti = 0;
		for (int y = 0; y < Foresta.getDimensioneY(); y++) {
			for (int x = 0; x < Foresta.getDimensioneX(); x++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				if (Foresta.getLocazione(coordinate) == TipoLocazione.TEMPIO && Tempio.isRipulito(Foresta.getLocazioneMD(coordinate))) {
					ripuliti++;
				}
			}
		}
		return ripuliti;
	}

	/**
	 * Un incantesimo di ogni tipo in dono (vedi {@link #INCANTESIMI_IN_DONO}), per aiutare il gruppo durante il viaggio.
	 */
	private void premia() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		for (TipoIncantesimo incantesimo : INCANTESIMI_IN_DONO) {
			gruppo.addIncantesimi(incantesimo, 1);
		}
		BusEventi.pubblica(new NotificaTestoParagrafo("In dono dai devoti, per aiutarvi durante il vostro viaggio: " + elencoDeiDoni() + "."));
		OffertaDelSacerdote.avvisaSeIlGruppoEPieno(getSacerdote() == TipoPersonaggio.SACERDOTESSA ? "la sacerdotessa" : "il sacerdote");
	}

	/**
	 * "un incantesimo dell'Aria, un incantesimo dell'Acqua e un incantesimo del Fulmine"
	 */
	private static String elencoDeiDoni() {
		StringBuilder elenco = new StringBuilder();
		for (int i = 0; i < INCANTESIMI_IN_DONO.length; i++) {
			if (i > 0) {
				elenco.append(i == INCANTESIMI_IN_DONO.length - 1 ? " e " : ", ");
			}
			elenco.append("un ").append(INCANTESIMI_IN_DONO[i].getNomeSingolare());
		}
		return elenco.toString();
	}

	/**
	 * La scena nel tempio: l'eroe a sinistra, il sacerdote che arriva da destra e si ferma davanti a lui. Il testo in
	 * alto usa l'alfabeto grande, senza accenti.
	 */
	private PaginaIntermezzo scenaNelTempio(String testo) {
		return new PaginaIntermezzo(testo)
				.conSfondo(ImmagineIntermezzo.locazione(TipoLocazione.TEMPIO))
				.conElemento(ElementoIntermezzo.personaggio(ID_SACERDOTE, getSacerdote(), 1.1, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.SINISTRA)
						.poi(Tappa.inSecondi(2).verso(0.62, 0.61)))
				.conElemento(ElementoIntermezzo.personaggio(ID_EROE, GruppoGiocatore.getIstanza().getCapo().getClasse(), 0.38, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.DESTRA));
	}

	private List<PaginaIntermezzo> getPagineDellaPresentazione() {
		boolean donna = getSacerdote() == TipoPersonaggio.SACERDOTESSA;
		return Collections.singletonList(scenaNelTempio("Il tempio e' libero dai mostri.")
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Siate benedetti, viandanti! Questi muri erano infestati dai mostri, "
						+ "e nessun devoto osava più venire a pregare.").daSecondo(2))
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Sono " + (donna ? "una sacerdotessa" : "un sacerdote")
						+ " e mi prendo cura di questi luoghi sacri."))
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Altri sei templi della Foresta soffrono dello stesso male. "
						+ "Volete aiutarmi a liberarli?"))
				.conBattuta(BattutaIntermezzo.di(ID_EROE, "Va bene, ci penseremo noi."))
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Che la Luce vi accompagni. Ogni tempio che libererete "
						+ "tornerà a essere un rifugio sicuro.")));
	}

	private List<PaginaIntermezzo> getPagineDelRingraziamento() {
		return Collections.singletonList(scenaNelTempio("Sette templi liberi.")
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Sette templi liberi! Grazie a voi i devoti potranno tornare "
						+ "in pellegrinaggio.").daSecondo(2))
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Accettate questi doni a nome loro, per aiutarvi durante il vostro viaggio: "
						+ elencoDeiDoni() + "."))
				.conBattuta(BattutaIntermezzo.di(ID_EROE, "Siamo onorati. Che la Luce vi accompagni.")));
	}

	@Override
	protected String passoIniziale() {
		return INIZIO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case INIZIO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, this::isTempioAppenaRipulito)
						.esegui(this::scegliIlSacerdote)
						.conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA, this::getPagineDellaPresentazione)
						.poi(TEMPLI);
			case TEMPLI:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> contaTempliRipuliti() >= TEMPLI_DA_LIBERARE)
						.aOgniControllo(this::contaIlTempio)
						.poi(RINGRAZIAMENTO);
			case RINGRAZIAMENTO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> true)
						.esegui(this::premia)
						.conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA, this::getPagineDelRingraziamento)
						.poi(() -> OffertaDelSacerdote.puoProporsi() ? COMPAGNO : Passo.FINE);
			case COMPAGNO:
				// A fine locazione, subito dopo il settimo tempio (e prima della scena del ringraziamento, che si mostra dopo i controlli), non alla
				// partenza per la locazione dopo
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> true)
						.chiediConferma("Liberato anche l'ultimo tempio, " + (getSacerdote() == TipoPersonaggio.SACERDOTESSA ? "la sacerdotessa" : "il sacerdote")
								+ " chiede se desiderate che si unisca a voi in pianta stabile. Accettate?")
						.esegui(() -> {
							if (Passo.SI.equals(getRisposta(COMPAGNO))) {
								OffertaDelSacerdote.accogli(getSacerdote());
							}
						})
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
