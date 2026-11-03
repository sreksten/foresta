package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.TestiDeiLuoghi;
import com.threeamigos.foresta.missioni.leggende.ConLeggendario;
import com.threeamigos.foresta.missioni.leggende.PescaLeggendaria;
import com.threeamigos.foresta.missioni.richieste.TorneoRichiesto;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.testi.RegoleDellaLingua;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;

/**
 * In città qualcuno bandisce un torneo (vedi {@link TorneoRichiesto}, da missioni.txt), con in palio un oggetto
 * leggendario, che nessuna leggenda e nessun altro torneo della partita ha già messo in palio (vedi
 * {@link PescaLeggendaria}), e una borsa di monete. Si combatte nella lizza, un posto segnato sulla mappa: due turni e
 * la finale, uno per visita, uno contro uno e fino alla resa (vedi IncontroDiMissione.aDuello e finoAllaResa). Chi
 * perde un turno può tornare per la rivincita, come in ogni duello. In finale il campione ha un nome e un livello in
 * più; vinta la finale, il leggendario va al gruppo, e la borsa si riscuote in città.
 * <ol>
 * <li>LIZZA, in locazione, nella città: la lizza compare sulla mappa;</li>
 * <li>PRIMO_TURNO e SECONDO_TURNO, a fine locazione, nella lizza: lo sfidante si è arreso (o peggio);</li>
 * <li>FINALE, a fine locazione, nella lizza: il campione si è arreso, e il gruppo riceve il leggendario.</li>
 * </ol>
 * Si ripete con altri tornei finché restano leggendari da mettere in palio.
 */
public class IlTorneo extends IncaricoInCitta implements ConLeggendario {

	public static final String TORNEO = "TORNEO";
	public static final int ORE_FRA_DUE_TORNEI = 72;
	private static final String CAMPIONE = "CAMPIONE";
	private static final String LEGGENDARIO = "LEGGENDARIO";
	private static final String LIZZA = "LIZZA";
	private static final String[] TURNI = {"PRIMO_TURNO", "SECONDO_TURNO"};
	// I nomi dei turni (e della finale) stanno in missioni.txt: IL_TORNEO_TURNO_1, _2 e _FINALE
	private static final String FINALE = "FINALE";

	public IlTorneo() {
		super(MissionePredefinita.IL_TORNEO);
	}

	public TorneoRichiesto getTorneo() {
		return TorneoRichiesto.da(parametro(TORNEO, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni(TORNEO)));
	}

	/**
	 * Il nome del campione della finale.
	 */
	public String getCampione() {
		return parametro(CAMPIONE, getTorneo()::pescaNomeDelCampione);
	}

	@Override
	public OggettoLeggendario getLeggendario() {
		String riga = ottieniProprieta(LEGGENDARIO);
		return riga == null ? null : OggettoLeggendario.da(riga);
	}

	/**
	 * La lizza, o null finché la missione non l'ha trovata.
	 */
	public CoordinateMD getLizza() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	@Override
	protected int getOreFraUnaMissioneELAltra() {
		return ORE_FRA_DUE_TORNEI;
	}

	/**
	 * Un torneo si bandisce solo se c'è ancora un leggendario da mettere in palio.
	 */
	@Override
	protected boolean isPossibileQui() {
		return getLeggendario() != null || PescaLeggendaria.restaUnLeggendario(PescaLeggendaria.giaPescati(this));
	}

	/**
	 * Finiti i leggendari, i tornei non si bandiscono più.
	 */
	@Override
	protected boolean puoAncoraOffrirsi() {
		return isPossibileQui();
	}

	@Override
	protected void allIncarico() {
		getTorneo();
		getCampione();
		if (getLeggendario() == null) {
			aggiungiProprieta(LEGGENDARIO, PescaLeggendaria.pesca(PescaLeggendaria.giaPescati(this), 0));
		}
	}

	private String testo(String testo) {
		return testo.replace(TorneoRichiesto.CAMPIONE, getCampione());
	}

	/**
	 * Lo sfidante di quel turno prima della finale: uno contro uno, fino alla resa.
	 */
	private IncontroDiMissione getSfidante(int turno) {
		return IncontroDiMissione.di(getTorneo().getSfidanti().get(turno), 1).finoAllaResa().aDuello();
	}

	/**
	 * Il campione della finale: con un nome e un livello in più.
	 */
	private IncontroDiMissione getCampioneDellaFinale() {
		return IncontroDiMissione.di(getTorneo().getCampione(), 1).conCapo(getCampione()).finoAllaResa().aDuello();
	}

	/**
	 * Uno sfidante per i testi: "un Guerriero".
	 */
	private static String unSfidante(TipoPersonaggio classe) {
		Personaggio modello = FabbricaPersonaggi.modello(classe);
		return modello.getAIS() + modello.getNomeSingolare();
	}

	/**
	 * Il campione per i testi: "Bradamante la Fulva, la Guerriera".
	 */
	private String ilCampione() {
		Personaggio modello = FabbricaPersonaggi.modello(getTorneo().getCampione());
		return getCampione() + ", " + modello.getADS() + modello.getNomeSingolare();
	}

	/**
	 * Che cosa c'è in palio: "c'è lo Scudo Fiscale", "ci sono gli Stivali del Fango".
	 */
	private String inPalio() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione(getLeggendario().isPlurale() ? "IL_TORNEO_IN_PALIO_PLURALE" : "IL_TORNEO_IN_PALIO",
				"PARAM_PREMIO_BREVE", getLeggendario().getNomeBreve());
	}

	private void premia() {
		TorneoRichiesto torneo = getTorneo();
		Artefatto leggendario = getLeggendario().costruisci(Statistiche.getLivello());
		GruppoGiocatore.getIstanza().addArtefatto(leggendario);
		BusEventi.pubblica(new NotificaArtefattoTrovato(leggendario, Statistiche.getLivello()));
		BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_TORNEO_PREMIO", valori())));
	}

	@Override
	public String getNome() {
		return testo(getTorneo().getTitolo());
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		if (getLeggendario() == null) {
			return "IL_TORNEO_DESCRIZIONE_ATTESA";
		}
		return RITORNO.equals(passo) ? "IL_TORNEO_DESCRIZIONE_RITORNO" : "IL_TORNEO_DESCRIZIONE_TURNO";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Tutti i segnaposto dei testi della missione: il luogo, il campione, il premio, il turno in corso e chi si affronta (vuoti prima del
	 * primo turno e al ritorno), la città, le monete e il mandante nelle sue forme. Prima che il torneo sia bandito il premio non c'è.
	 */
	private Object[] valori() {
		TorneoRichiesto torneo = getTorneo();
		RegoleDellaLingua lingua = Lingue.corrente();
		String mandante = mandante(torneo.getMandante());
		String passo = getPassoCorrente();
		String turno = "";
		String chi = "";
		if (FINALE.equals(passo)) {
			turno = ProduttoreDiMissioniCasuale.testoDellaMissione("IL_TORNEO_TURNO_FINALE");
			chi = ilCampione();
		} else if (TURNI[0].equals(passo) || TURNI[1].equals(passo)) {
			int indice = TURNI[1].equals(passo) ? 1 : 0;
			turno = ProduttoreDiMissioniCasuale.testoDellaMissione("IL_TORNEO_TURNO_" + (indice + 1));
			chi = unSfidante(torneo.getSfidanti().get(indice));
		}
		boolean bandito = getLeggendario() != null;
		return new Object[] {
				"PARAM_TITOLO", getNome(),
				"PARAM_DENTRO_LUOGO", TestiDeiLuoghi.dentro(torneo.getLuogo()),
				"PARAM_CAMPIONE", ilCampione(),
				"PARAM_PREMIO", bandito ? inPalio() : "",
				"PARAM_PREMIO_BREVE", bandito ? getLeggendario().getNomeBreve() : "",
				"PARAM_TURNO", turno,
				"PARAM_CHI", chi,
				"PARAM_FINALE", testo(torneo.getFinale()),
				"PARAM_CITTA", getNomeCitta(),
				"PARAM_MONETE", getRicompensa(),
				"PARAM_MANDANTE_MAIUSC", lingua.inizialeMaiuscola(mandante),
				"PARAM_MANDANTE_DI_CITTA_MAIUSC", lingua.inizialeMaiuscola(mandanteDiCitta(torneo.getMandante())),
				"PARAM_PRESSO_MANDANTE", lingua.conPreposizione(Preposizione.PRESSO, mandante)};
	}

	@Override
	public String getRicordoDellaLocazione() {
		return testo(getTorneo().getRicordo());
	}

	@Override
	protected String primoPassoDelCompito() {
		return LIZZA;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		TorneoRichiesto torneo = getTorneo();
		switch (id) {
			case LIZZA:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, torneo.getLuogo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getLizza());
							BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_TORNEO_LIZZA_SEGNATA", valori())));
						})
						.poi(TURNI[0]);
			case FINALE:
				return combatti(this::getLizza, getCampioneDellaFinale())
						.esegui(this::premia)
						.poi(RITORNO);
			default:
				for (int turno = 0; turno < TURNI.length; turno++) {
					if (TURNI[turno].equals(id)) {
						String vinto = torneo.getTurnoVinto(turno);
						String prossimo = turno + 1 < TURNI.length ? TURNI[turno + 1] : FINALE;
						return combatti(this::getLizza, getSfidante(turno))
								.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(vinto)
										+ " " + ProduttoreDiMissioniCasuale.testoDellaMissione(FINALE.equals(prossimo) ? "IL_TORNEO_PROSSIMA_FINALE" : "IL_TORNEO_PROSSIMO_TURNO"))))
								.poi(prossimo);
					}
				}
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		TorneoRichiesto torneo = getTorneo();
		return torneo.getAspetto().nuovaScena()
				.parlaIlMandante(testo(torneo.getRichiesta()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_TORNEO_INCARICO_1", valori()))
				.parlaIlCapo(testo(torneo.getBattutaDelCapo()))
				.parlaIlMandante(testo(torneo.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getTorneo().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getTorneo().getRingraziamento()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_TORNEO_RINGRAZIAMENTO_2", valori()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_TORNEO_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_TORNEO_RICOMPENSA", valori());
	}

	@Override
	protected int getRicompensaBase() {
		return getTorneo().getMonete();
	}
}
