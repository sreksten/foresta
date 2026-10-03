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
 * Il favore che un sacerdote chiede in cambio della sua benedizione (vedi LaBenedizione, che lo affida come missione
 * secondaria), di tre tipi (vedi BenedizioneRichiesta.TipoFavore): sconfiggere qualcuno in un posto, raccogliere
 * qualcosa, vegliare un posto passandoci più volte. Fatto il favore la missione si completa lì, senza tornare da
 * nessuno: è la benedizione ad andare avanti. Legge la riga della benedizione, che gliela passa la madre.
 * <ol>
 * <li>POSTO, in locazione, subito, per un combattimento o una veglia: il posto compare sulla mappa;</li>
 * <li>CACCIA, a fine locazione, nel posto: i nemici sono stati sconfitti tutti; oppure</li>
 * <li>RACCOLTA, a fine locazione: ci sono tutti gli ingredienti; oppure</li>
 * <li>VEGLIA, a inizio locazione, nel posto: il gruppo ci è passato abbastanza volte (vedi MissioneAPassi.sorveglia).</li>
 * </ol>
 */
public class IlFavore extends MissioneAPassi {

	private static final String BENEDIZIONE = "BENEDIZIONE";
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
		IlFavore favore = new IlFavore();
		favore.aggiungiProprieta(PARAMETRO + BENEDIZIONE, benedizione.getRiga());
		if (benedizione.isConCapo()) {
			favore.aggiungiProprieta(PARAMETRO + CAPO, benedizione.pescaNomeDelCapo());
		}
		return favore;
	}

	public BenedizioneRichiesta getBenedizione() {
		return BenedizioneRichiesta.da(getParametro(BENEDIZIONE));
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
		BenedizioneRichiesta benedizione = getBenedizione();
		return benedizione.getIngrediente().daRaccogliere(INGREDIENTE, benedizione.getQuantitaDaRaccogliere());
	}

	public IncontroDiMissione getNemici() {
		BenedizioneRichiesta benedizione = getBenedizione();
		IncontroDiMissione nemici = IncontroDiMissione.di(benedizione.getNemico(), benedizione.getNumero());
		return benedizione.isConCapo() ? nemici.conCapo(getCapo()) : nemici;
	}

	/**
	 * Il posto del favore, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getPosto() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	private String testo(String testo) {
		return testo.replace(BenedizioneRichiesta.CAPO, getCapo());
	}

	/**
	 * I nemici per i testi: "la Chimera-drago", "tre Spettri", "cinque Goblin guidati da Gruk".
	 */
	private String getNomeDeiNemici() {
		BenedizioneRichiesta benedizione = getBenedizione();
		Personaggio modello = benedizione.getNemico().getMoltiplicatoriDiClasse();
		if (benedizione.getNumero() == 1) {
			String nemico = modello.getADS() + modello.getNomeSingolare();
			return benedizione.isConCapo() ? getCapo() + ", " + nemico : nemico;
		}
		String nemici = Misc.getCardinaleM(benedizione.getNumero()) + " " + modello.getNomePlurale();
		return benedizione.isConCapo() ? nemici + " guidati da " + getCapo() : nemici;
	}

	@Override
	public String getNome() {
		return testo(getBenedizione().getFavore());
	}

	@Override
	public String getDescrizione() {
		BenedizioneRichiesta benedizione = getBenedizione();
		String per = "Per " + benedizione.getMandante() + ": ";
		switch (benedizione.getTipoFavore()) {
			case RACCOLTA:
				MaterialeRichiesto ingrediente = benedizione.getIngrediente();
				return per + "raccogli " + ingrediente.quanti(benedizione.getQuantitaDaRaccogliere()) + ", che "
						+ ingrediente.getDaDoveViene() + ". Finora: " + getContatore(INGREDIENTE) + ".";
			case VEGLIA:
				return per + "veglia il posto segnato sulla mappa, passandoci " + Misc.getCardinaleF(benedizione.getVisite())
						+ " volte e lasciando passare almeno " + benedizione.getOre() + " ore fra una visita e l'altra. Finora: "
						+ (VEGLIA.equals(getPassoCorrente()) ? getVisiteNelPassoCorrente() : 0) + ".";
			default:
				return per + "sconfiggi " + getNomeDeiNemici() + " nel posto segnato sulla mappa.";
		}
	}

	/**
	 * A una visita della veglia che conta, se non è l'ultima: il testo della veglia, e quante ne mancano.
	 */
	private void raccontaLaVeglia() {
		BenedizioneRichiesta benedizione = getBenedizione();
		int visite = getVisiteNelPassoCorrente();
		String raccontate = ottieniProprieta(VEGLIE);
		if (visite >= benedizione.getVisite() || visite <= (raccontate == null ? 0 : Integer.parseInt(raccontate))) {
			return;
		}
		aggiungiProprieta(VEGLIE, String.valueOf(visite));
		int mancanti = benedizione.getVisite() - visite;
		BusEventi.pubblica(new NotificaTestoParagrafo(benedizione.getVeglia() + " Bisogna tornare qui ancora "
				+ Misc.getCardinaleF(mancanti) + (mancanti == 1 ? " volta" : " volte") + ", lasciando passare almeno "
				+ benedizione.getOre() + " ore fra una visita e l'altra."));
	}

	@Override
	protected String passoIniziale() {
		return getBenedizione().getTipoFavore() == BenedizioneRichiesta.TipoFavore.RACCOLTA ? RACCOLTA : POSTO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		BenedizioneRichiesta benedizione = getBenedizione();
		switch (id) {
			case POSTO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, benedizione.getLuogo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getPosto());
							BusEventi.pubblica(new NotificaTestoParagrafo("Il posto del favore è segnato sulla mappa."));
						})
						.poi(benedizione.getTipoFavore() == BenedizioneRichiesta.TipoFavore.VEGLIA ? VEGLIA : CACCIA);
			case CACCIA:
				return combatti(this::getPosto, getNemici())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(benedizione.getVittoria()))))
						.poi(Passo.FINE);
			case RACCOLTA:
				return raccogli(MomentoControllo.POST_LOCAZIONE, getIngredienti())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(benedizione.getVittoria()))))
						.poi(Passo.FINE);
			case VEGLIA:
				return sorveglia(this::getPosto, benedizione.getVisite(), benedizione.getOre())
						.aOgniControllo(this::raccontaLaVeglia)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(benedizione.getVittoria()))))
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
