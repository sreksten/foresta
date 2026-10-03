package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

/**
 * In città qualcuno vuole che si tenga d'occhio un posto nella foresta: vegliare una tomba, scoprire chi passa da un
 * bosco, spiare un accampamento (vedi {@link SorveglianzaRichiesta}, da missioni.txt). La missione rivendica un posto
 * della classe giusta e lo segna sulla mappa; il gruppo ci deve passare più volte, lasciando passare abbastanza ore
 * fra una visita e l'altra (vedi {@link MissioneAPassi#sorveglia}). All'ultima visita si scopre qualcosa, e a volte
 * salta fuori qualcuno da sconfiggere lì; poi si torna a riscuotere (vedi IncaricoInCitta). La sorveglianza si pesca
 * quando si offre: la missione si ripete con altre.
 * <ol>
 * <li>POSTO, in locazione, nella città: il posto compare sulla mappa;</li>
 * <li>SORVEGLIA, a inizio locazione, nel posto: il gruppo ci è passato abbastanza volte;</li>
 * <li>AGGUATO, a fine locazione, nel posto, se qualcuno salta fuori: è stato sconfitto.</li>
 * </ol>
 */
public class LaSorveglianza extends IncaricoInCitta {

	public static final String SORVEGLIANZA = "SORVEGLIANZA";
	private static final String CAPO = "CAPO";
	private static final String VEGLIE = "VEGLIE";
	private static final String POSTO = "POSTO";
	private static final String SORVEGLIA = "SORVEGLIA";
	private static final String AGGUATO = "AGGUATO";

	public LaSorveglianza() {
		super(ClasseMissione.LA_SORVEGLIANZA);
	}

	public SorveglianzaRichiesta getSorveglianza() {
		return SorveglianzaRichiesta.da(parametro(SORVEGLIANZA, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(SORVEGLIANZA)));
	}

	/**
	 * Il nome del capo di chi salta fuori, se ce n'è uno (da missioni.txt), altrimenti vuoto.
	 */
	public String getCapo() {
		return getSorveglianza().isConCapo() ? parametro(CAPO, ProduttoreDiTestiCasuale::nomeCapobanda) : "";
	}

	/**
	 * Chi salta fuori all'ultima visita, con il suo capo se c'è; null se nessuno.
	 */
	public IncontroDiMissione getNemici() {
		SorveglianzaRichiesta sorveglianza = getSorveglianza();
		if (!sorveglianza.isConNemici()) {
			return null;
		}
		IncontroDiMissione nemici = IncontroDiMissione.di(sorveglianza.getNemico(), sorveglianza.getNumero());
		return sorveglianza.isConCapo() ? nemici.conCapo(getCapo()) : nemici;
	}

	/**
	 * Il posto da sorvegliare, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getPosto() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Il testo della grammatica, con il nome del capo al posto di %CAPO%.
	 */
	private String testo(String testo) {
		return testo.replace(SorveglianzaRichiesta.CAPO, getCapo());
	}

	private String getMandanteDiCitta() {
		return getSorveglianza().getMandante() + " di " + getNomeCitta();
	}

	/**
	 * "ancora due volte", "ancora una volta".
	 */
	private String getVisiteMancanti() {
		int mancanti = getSorveglianza().getVisite() - getVisiteNelPassoCorrente();
		return "ancora " + Misc.getCardinaleF(mancanti) + (mancanti == 1 ? " volta" : " volte");
	}

	private String getIntervallo() {
		return "lasciando passare almeno " + getSorveglianza().getOre() + " ore fra una visita e l'altra";
	}

	/**
	 * A una visita che conta, se non è l'ultima: il testo della veglia, e quante ne mancano.
	 */
	private void raccontaLaVeglia() {
		int visite = getVisiteNelPassoCorrente();
		String raccontate = ottieniProprieta(VEGLIE);
		if (visite >= getSorveglianza().getVisite() || visite <= (raccontate == null ? 0 : Integer.parseInt(raccontate))) {
			return;
		}
		aggiungiProprieta(VEGLIE, String.valueOf(visite));
		BusEventi.pubblica(new NotificaTestoParagrafo(testo(getSorveglianza().getVeglia()) + " Bisogna tornare qui "
				+ getVisiteMancanti() + ", " + getIntervallo() + "."));
	}

	private String getAttesa() {
		return Misc.inizialeMaiuscola(getSorveglianza().getMandante()) + " aspetta a " + getNomeCitta() + ".";
	}

	@Override
	protected void allIncarico() {
		getSorveglianza();
		getCapo();
	}

	@Override
	public String getNome() {
		return testo(getSorveglianza().getTitolo());
	}

	@Override
	public String getDescrizione() {
		String passo = getPassoCorrente();
		if (RITORNO.equals(passo)) {
			return "Fatto: torna " + Misc.conPreposizione("da", getSorveglianza().getMandante()) + " a " + getNomeCitta()
					+ " a riscuotere.";
		}
		if (AGGUATO.equals(passo)) {
			return "Sconfiggi " + getNomeDeiNemici() + " nel posto segnato sulla mappa, poi torna "
					+ Misc.conPreposizione("da", getSorveglianza().getMandante()) + " a " + getNomeCitta() + ".";
		}
		String descrizione = Misc.inizialeMaiuscola(getMandanteDiCitta()) + " ti ha chiesto di sorvegliare il posto segnato sulla mappa";
		if (SORVEGLIA.equals(passo) && getVisiteNelPassoCorrente() > 0) {
			return descrizione + ": passaci " + getVisiteMancanti() + ", " + getIntervallo() + ".";
		}
		return descrizione + ", passandoci " + Misc.getCardinaleF(getSorveglianza().getVisite()) + " volte e " + getIntervallo() + ".";
	}

	/**
	 * Chi salta fuori, per i testi: "tre Hobgoblin", "Gruk, il Troll", "cinque Goblin guidati da Gruk".
	 */
	private String getNomeDeiNemici() {
		SorveglianzaRichiesta sorveglianza = getSorveglianza();
		Personaggio modello = sorveglianza.getNemico().getMoltiplicatoriDiClasse();
		if (sorveglianza.getNumero() == 1) {
			String nemico = modello.getADS() + modello.getNomeSingolare();
			return sorveglianza.isConCapo() ? getCapo() + ", " + nemico : nemico;
		}
		String nemici = Misc.getCardinaleM(sorveglianza.getNumero()) + " " + modello.getNomePlurale();
		return sorveglianza.isConCapo() ? nemici + " guidati da " + getCapo() : nemici;
	}

	@Override
	public String getRicordoDellaLocazione() {
		return testo(getSorveglianza().getRicordo());
	}

	@Override
	protected String primoPassoDelCompito() {
		return POSTO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		SorveglianzaRichiesta sorveglianza = getSorveglianza();
		switch (id) {
			case POSTO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, sorveglianza.getLuogo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getPosto());
							BusEventi.pubblica(new NotificaTestoParagrafo("Il posto da sorvegliare è segnato sulla mappa."));
						})
						.poi(SORVEGLIA);
			case SORVEGLIA:
				return sorveglia(this::getPosto, sorveglianza.getVisite(), sorveglianza.getOre())
						.aOgniControllo(this::raccontaLaVeglia)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(sorveglianza.getScoperta())
								+ (sorveglianza.isConNemici() ? "" : " " + getAttesa()))))
						.poi(sorveglianza.isConNemici() ? AGGUATO : RITORNO);
			case AGGUATO:
				return combatti(this::getPosto, getNemici())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(sorveglianza.getVittoria()) + " " + getAttesa())))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		SorveglianzaRichiesta sorveglianza = getSorveglianza();
		return sorveglianza.getAspetto().nuovaScena()
				.parlaIlMandante(testo(sorveglianza.getRichiesta()))
				.parlaIlMandante("Il posto è " + TestiDeiLuoghi.indefinito(sorveglianza.getLuogo()) + " qui vicino: ve lo segno sulla mappa. "
						+ "Passateci " + Misc.getCardinaleF(sorveglianza.getVisite()) + " volte, " + getIntervallo() + ". "
						+ getRicompensa() + " monete a lavoro fatto.")
				.parlaIlCapo(testo(sorveglianza.getBattutaDelCapo()))
				.parlaIlMandante(testo(sorveglianza.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getSorveglianza().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getSorveglianza().getRingraziamento()))
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse.");
	}

	@Override
	protected String testoAccettazione() {
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " pagherà " + getRicompensa() + " monete per sorvegliare "
				+ TestiDeiLuoghi.indefinito(getSorveglianza().getLuogo()) + ".";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getSorveglianza().getMandante()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return getSorveglianza().getMonete();
	}
}
