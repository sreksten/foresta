package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

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
	private static final String[] NOMI_DEI_TURNI = {"primo turno", "secondo turno"};
	private static final String FINALE = "FINALE";

	public IlTorneo() {
		super(ClasseMissione.IL_TORNEO);
	}

	public TorneoRichiesto getTorneo() {
		return TorneoRichiesto.da(parametro(TORNEO, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(TORNEO)));
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
	private static String unSfidante(ClassePersonaggio classe) {
		Personaggio modello = classe.getMoltiplicatoriDiClasse();
		return modello.getAIS() + modello.getNomeSingolare();
	}

	/**
	 * Il campione per i testi: "Bradamante la Fulva, la Guerriera".
	 */
	private String ilCampione() {
		Personaggio modello = getTorneo().getCampione().getMoltiplicatoriDiClasse();
		return getCampione() + ", " + modello.getADS() + modello.getNomeSingolare();
	}

	/**
	 * Che cosa c'è in palio: "c'è lo Scudo Fiscale", "ci sono gli Stivali del Fango".
	 */
	private String inPalio() {
		String breve = getLeggendario().getNomeBreve();
		boolean plurale = breve.startsWith("gli ") || breve.startsWith("i ") || breve.startsWith("le ");
		return (plurale ? "ci sono " : "c'è ") + breve;
	}

	private void premia() {
		TorneoRichiesto torneo = getTorneo();
		Artefatto leggendario = getLeggendario().costruisci();
		GruppoGiocatore.getIstanza().addArtefatto(leggendario);
		BusEventi.pubblica(new NotificaArtefattoTrovato(leggendario, Statistiche.getLivello()));
		BusEventi.pubblica(new NotificaTestoParagrafo(testo(torneo.getFinale()) + " Il giudice di gara consegna al gruppo "
				+ getLeggendario().getNomeBreve() + ": finisce nell'inventario del gruppo. La borsa di " + torneo.getMonete()
				+ " monete aspetta a " + getNomeCitta() + "."));
	}

	@Override
	public String getNome() {
		return testo(getTorneo().getTitolo());
	}

	@Override
	public String getDescrizione() {
		TorneoRichiesto torneo = getTorneo();
		String passo = getPassoCorrente();
		if (RITORNO.equals(passo)) {
			return "Hai vinto il torneo: torna " + Misc.conPreposizione("da", torneo.getMandante()) + " a " + getNomeCitta()
					+ " per la borsa del vincitore.";
		}
		String chi;
		String turno;
		if (FINALE.equals(passo)) {
			chi = ilCampione();
			turno = "la finale";
		} else {
			int indice = TURNI[1].equals(passo) ? 1 : 0;
			chi = unSfidante(torneo.getSfidanti().get(indice));
			turno = "il " + NOMI_DEI_TURNI[indice];
		}
		return getNome() + ", " + turno + ": affronta " + chi + " nella lizza segnata sulla mappa, uno contro uno e fino alla resa. In palio "
				+ inPalio() + ".";
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
							BusEventi.pubblica(new NotificaTestoParagrafo("La lizza del torneo è segnata sulla mappa: si combatte un turno a "
									+ "ogni visita, uno contro uno e fino alla resa."));
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
										+ (FINALE.equals(prossimo) ? " Alla prossima visita alla lizza, la finale." : " Alla prossima visita alla lizza, il turno successivo."))))
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
				.parlaIlMandante("Si combatte " + TestiDeiLuoghi.dentro(torneo.getLuogo()) + " qui vicino, uno contro uno e fino alla resa: "
						+ "due turni, e in finale " + ilCampione() + ". In palio " + inPalio() + ", e una borsa di "
						+ torneo.getMonete() + " monete.")
				.parlaIlCapo(testo(torneo.getBattutaDelCapo()))
				.parlaIlMandante(testo(torneo.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getTorneo().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getTorneo().getRingraziamento()))
				.parlaIlMandante("Ecco la borsa del vincitore: " + getRicompensa() + " monete.");
	}

	@Override
	protected String testoAccettazione() {
		return Misc.inizialeMaiuscola(getTorneo().getMandante()) + " di " + getNomeCitta() + " ha iscritto il gruppo al torneo: in palio " + inPalio() + ".";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getTorneo().getMandante()) + " consegna la borsa del vincitore: " + getRicompensa() + " monete.";
	}

	@Override
	protected int getRicompensa() {
		return getTorneo().getMonete();
	}
}
