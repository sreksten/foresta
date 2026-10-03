package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

/**
 * Il favore che un sacerdote chiede in cambio della sua benedizione (vedi LaBenedizione), o che un compagno chiede
 * al gruppo (vedi LaLealta): la madre lo affida come missione secondaria. È di tre tipi (vedi FavoreRichiesto.Tipo):
 * sconfiggere qualcuno in un posto, raccogliere qualcosa, vegliare un posto passandoci più volte. Fatto il favore la
 * missione si completa lì, senza tornare da nessuno: è la madre ad andare avanti. Legge la riga della madre, che gliela
 * passa con la sua origine (BENEDIZIONE o LEALTA), e chi chiede il favore.
 * <ol>
 * <li>POSTO, in locazione, subito, per un combattimento o una veglia: il posto compare sulla mappa;</li>
 * <li>CACCIA, a fine locazione, nel posto: i nemici sono stati sconfitti tutti; oppure</li>
 * <li>RACCOLTA, a fine locazione: ci sono tutti gli ingredienti; oppure</li>
 * <li>VEGLIA, a inizio locazione, nel posto: il gruppo ci è passato abbastanza volte (vedi MissioneAPassi.sorveglia).</li>
 * </ol>
 */
public class IlFavore extends MissioneAPassi {

	private static final String BENEDIZIONE = "BENEDIZIONE";
	private static final String LEALTA = "LEALTA";
	private static final String ORIGINE = "ORIGINE";
	private static final String RIGA = "RIGA";
	private static final String MANDANTE = "MANDANTE";
	private static final String CAPO = "CAPO";
	private static final String POSTO = "POSTO";
	private static final String CACCIA = "CACCIA";
	private static final String RACCOLTA = "RACCOLTA";
	private static final String VEGLIA = "VEGLIA";
	private static final String VEGLIE = "VEGLIE";
	/**
	 * La chiave sotto cui la missione conta gli ingredienti raccolti.
	 */
	public static final String INGREDIENTE = "INGREDIENTE";

	public IlFavore() {
		super(ClasseMissione.IL_FAVORE);
	}

	/**
	 * Il favore di quella benedizione, con il nome del capo dei nemici già pescato.
	 */
	static IlFavore per(BenedizioneRichiesta benedizione) {
		return per(BENEDIZIONE, benedizione.getRiga(), benedizione.getFavore(), benedizione.getMandante());
	}

	/**
	 * Il favore che chiede quel compagno, con il nome del capo dei nemici già pescato.
	 */
	static IlFavore per(LealtaRichiesta lealta, String compagno) {
		return per(LEALTA, lealta.getRiga(), lealta.getFavore(), compagno);
	}

	private static IlFavore per(String origine, String riga, FavoreRichiesto richiesto, String mandante) {
		IlFavore favore = new IlFavore();
		favore.aggiungiProprieta(PARAMETRO + ORIGINE, origine);
		favore.aggiungiProprieta(PARAMETRO + RIGA, riga);
		favore.aggiungiProprieta(PARAMETRO + MANDANTE, mandante);
		if (richiesto.isConCapo()) {
			favore.aggiungiProprieta(PARAMETRO + CAPO, richiesto.pescaNomeDelCapo());
		}
		return favore;
	}

	public FavoreRichiesto getFavore() {
		String riga = getParametro(RIGA);
		return LEALTA.equals(getParametro(ORIGINE)) ? LealtaRichiesta.da(riga).getFavore() : BenedizioneRichiesta.da(riga).getFavore();
	}

	/**
	 * Chi chiede il favore: "la sacerdotessa della luna", o il nome del compagno.
	 */
	public String getMandante() {
		return getParametro(MANDANTE);
	}

	/**
	 * Il nome del capo dei nemici, se c'è, altrimenti vuoto.
	 */
	public String getCapo() {
		String capo = getParametro(CAPO);
		return capo == null ? "" : capo;
	}

	/**
	 * Gli ingredienti da raccogliere, per una raccolta.
	 */
	public OggettiDaRaccogliere getIngredienti() {
		FavoreRichiesto favore = getFavore();
		return favore.getIngrediente().daRaccogliere(INGREDIENTE, favore.getQuantitaDaRaccogliere());
	}

	public IncontroDiMissione getNemici() {
		FavoreRichiesto favore = getFavore();
		IncontroDiMissione nemici = IncontroDiMissione.di(favore.getNemico(), favore.getNumero());
		return favore.isConCapo() ? nemici.conCapo(getCapo()) : nemici;
	}

	/**
	 * Il posto del favore, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getPosto() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Il testo con il nome del capo dei nemici al posto di %CAPO%, e chi chiede il favore al posto di %PERSONAGGIO%
	 * (il compagno, in una lealtà).
	 */
	private String testo(String testo) {
		return testo.replace(CombattimentoRichiesto.CAPO, getCapo()).replace(BenedizioneRichiesta.PERSONAGGIO, getMandante());
	}

	/**
	 * I nemici per i testi: "la Chimera-drago", "tre Spettri", "cinque Goblin guidati da Gruk".
	 */
	private String getNomeDeiNemici() {
		FavoreRichiesto favore = getFavore();
		Personaggio modello = favore.getNemico().getMoltiplicatoriDiClasse();
		if (favore.getNumero() == 1) {
			String nemico = modello.getADS() + modello.getNomeSingolare();
			return favore.isConCapo() ? getCapo() + ", " + nemico : nemico;
		}
		String nemici = Misc.getCardinaleM(favore.getNumero()) + " " + modello.getNomePlurale();
		return favore.isConCapo() ? nemici + " guidati da " + getCapo() : nemici;
	}

	@Override
	public String getNome() {
		return testo(getFavore().getNome());
	}

	@Override
	public String getDescrizione() {
		FavoreRichiesto favore = getFavore();
		String per = "Per " + getMandante() + ": ";
		switch (favore.getTipo()) {
			case RACCOLTA:
				MaterialeRichiesto ingrediente = favore.getIngrediente();
				return per + "raccogli " + ingrediente.quanti(favore.getQuantitaDaRaccogliere()) + ", che "
						+ ingrediente.getDaDoveViene() + ". Finora: " + getContatore(INGREDIENTE) + ".";
			case VEGLIA:
				return per + "veglia il posto segnato sulla mappa, passandoci " + Misc.getCardinaleF(favore.getVisite())
						+ " volte e lasciando passare almeno " + favore.getOre() + " ore fra una visita e l'altra. Finora: "
						+ (VEGLIA.equals(getPassoCorrente()) ? getVisiteNelPassoCorrente() : 0) + ".";
			default:
				return per + "sconfiggi " + getNomeDeiNemici() + " nel posto segnato sulla mappa.";
		}
	}

	/**
	 * A una visita della veglia che conta, se non è l'ultima: il testo della veglia, e quante ne mancano.
	 */
	private void raccontaLaVeglia() {
		FavoreRichiesto favore = getFavore();
		int visite = getVisiteNelPassoCorrente();
		String raccontate = ottieniProprieta(VEGLIE);
		if (visite >= favore.getVisite() || visite <= (raccontate == null ? 0 : Integer.parseInt(raccontate))) {
			return;
		}
		aggiungiProprieta(VEGLIE, String.valueOf(visite));
		int mancanti = favore.getVisite() - visite;
		BusEventi.pubblica(new NotificaTestoParagrafo(testo(favore.getVeglia()) + " Bisogna tornare qui ancora "
				+ Misc.getCardinaleF(mancanti) + (mancanti == 1 ? " volta" : " volte") + ", lasciando passare almeno "
				+ favore.getOre() + " ore fra una visita e l'altra."));
	}

	@Override
	protected String passoIniziale() {
		return getFavore().getTipo() == FavoreRichiesto.Tipo.RACCOLTA ? RACCOLTA : POSTO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		FavoreRichiesto favore = getFavore();
		switch (id) {
			case POSTO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, favore.getLuogo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getPosto());
							BusEventi.pubblica(new NotificaTestoParagrafo("Il posto del favore è segnato sulla mappa."));
						})
						.poi(favore.getTipo() == FavoreRichiesto.Tipo.VEGLIA ? VEGLIA : CACCIA);
			case CACCIA:
				return combatti(this::getPosto, getNemici())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(favore.getVittoria()))))
						.poi(Passo.FINE);
			case RACCOLTA:
				return raccogli(MomentoControllo.POST_LOCAZIONE, getIngredienti())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(favore.getVittoria()))))
						.poi(Passo.FINE);
			case VEGLIA:
				return sorveglia(this::getPosto, favore.getVisite(), favore.getOre())
						.aOgniControllo(this::raccontaLaVeglia)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(favore.getVittoria()))))
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
