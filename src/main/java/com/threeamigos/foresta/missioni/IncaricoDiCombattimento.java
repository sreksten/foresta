package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * In città qualcuno vuole sconfitto qualcosa che si nasconde nella foresta: una bestia rara, una banda in un covo, una
 * pattuglia, chi gli ha fatto un torto (vedi {@link CombattimentoRichiesto}, da missioni.txt). La missione rivendica
 * un posto della classe giusta, lo segna sulla mappa e ci mette i nemici (vedi {@link MissioneAPassi#combatti});
 * sconfitti tutti, si torna a riscuotere (vedi IncaricoInCitta). L'incarico si pesca quando si offre: la missione si
 * ripete con altri.
 * <ol>
 * <li>COVO, in locazione, nella città: il posto compare sulla mappa;</li>
 * <li>CACCIA, a fine locazione, nel posto: i nemici sono stati sconfitti tutti.</li>
 * </ol>
 */
public class IncaricoDiCombattimento extends IncaricoInCitta {

	public static final String INCARICO = "INCARICO";
	private static final String CAPO = "CAPO";
	private static final String COVO = "COVO";
	private static final String CACCIA = "CACCIA";
	private static final String PRODUZIONE = "INCARICO_DI_COMBATTIMENTO";

	public IncaricoDiCombattimento() {
		super(ClasseMissione.INCARICO_DI_COMBATTIMENTO);
	}

	public CombattimentoRichiesto getIncarico() {
		return CombattimentoRichiesto.da(parametro(INCARICO, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(PRODUZIONE)));
	}

	/**
	 * Il nome del capo, se l'incarico ne ha uno (da missioni.txt, dalla produzione che dice l'incarico), altrimenti
	 * vuoto.
	 */
	public String getCapo() {
		CombattimentoRichiesto incarico = getIncarico();
		return incarico.isConCapo() ? parametro(CAPO, incarico::pescaNomeDelCapo) : "";
	}

	/**
	 * I nemici da sconfiggere, con il loro capo se c'è.
	 */
	public IncontroDiMissione getNemici() {
		CombattimentoRichiesto incarico = getIncarico();
		IncontroDiMissione nemici = IncontroDiMissione.di(incarico.getNemico(), incarico.getNumero());
		if (incarico.isFinoAllaResa()) {
			nemici.finoAllaResa();
		}
		if (incarico.isADuello()) {
			nemici.aDuello();
		}
		if (incarico.isConCapo()) {
			nemici.conCapo(getCapo());
		}
		return incarico.getOndate().aggiungiA(nemici, this::getCapoDellOndata, this::testo);
	}

	/**
	 * Il nome del capo di quell'ondata (2, 3), pescato una volta sola.
	 */
	private String getCapoDellOndata(int ondata) {
		return parametro(CAPO + "_" + ondata, () -> getIncarico().getOndate().pescaNomeDelCapo(ondata));
	}

	/**
	 * Il posto dove si nascondono, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Il testo della grammatica, con il nome del capo al posto di %CAPO%, e quelli dei capi delle ondate al posto di
	 * %CAPO_2% e %CAPO_3%.
	 */
	private String testo(String testo) {
		String conICapi = testo.replace(CombattimentoRichiesto.CAPO, getCapo());
		for (int ondata : getIncarico().getOndate().getOndateConCapo()) {
			conICapi = conICapi.replace(OndateDellaRiga.capo(ondata), getCapoDellOndata(ondata));
		}
		return conICapi;
	}

	/**
	 * I nemici per i testi: "la Chimera-drago", "tre Troll", e con il capo "Gruk, il Troll", "cinque Goblin guidati da
	 * Gruk".
	 */
	private String getNomeDeiNemici() {
		CombattimentoRichiesto incarico = getIncarico();
		Personaggio modello = FabbricaPersonaggi.modello(incarico.getNemico());
		if (incarico.getNumero() == 1) {
			String nemico = modello.getADS() + modello.getNomeSingolare();
			return incarico.isConCapo() ? getCapo() + ", " + nemico : nemico;
		}
		String nemici = Misc.getCardinaleM(incarico.getNumero()) + " " + modello.getNomePlurale();
		return incarico.isConCapo() ? nemici + " guidati da " + getCapo() : nemici;
	}

	private String getMandanteDiCitta() {
		return getIncarico().getMandante() + " di " + getNomeCitta();
	}

	@Override
	protected void allIncarico() {
		getIncarico();
		getCapo();
		getIncarico().getOndate().getOndateConCapo().forEach(this::getCapoDellOndata);
	}

	@Override
	public String getNome() {
		return testo(getIncarico().getTitolo());
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return "Fatto: torna " + Misc.conPreposizione("da", getIncarico().getMandante()) + " a " + getNomeCitta() + " a riscuotere.";
		}
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " ti ha chiesto di sconfiggere " + getNomeDeiNemici()
				+ (getIncarico().getNumero() == 1 ? ", che si trova" : ", che si trovano") + " nel posto segnato sulla mappa.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return testo(getIncarico().getRicordo());
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		CombattimentoRichiesto incarico = getIncarico();
		switch (id) {
			case COVO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, incarico.getLuogo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getCovo());
							BusEventi.pubblica(new NotificaTestoParagrafo("Il posto dove "
										+ (incarico.getNumero() == 1 ? "si trova" : "si trovano") + " è segnato sulla mappa."));
						})
						.poi(CACCIA);
			case CACCIA:
				return combatti(this::getCovo, getNemici())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(incarico.getVittoria()) + " "
								+ Misc.inizialeMaiuscola(incarico.getMandante()) + " aspetta a " + getNomeCitta() + ".")))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		CombattimentoRichiesto incarico = getIncarico();
		return incarico.getAspetto().nuovaScena()
				.parlaIlMandante(testo(incarico.getRichiesta()))
				.parlaIlMandante((incarico.getNumero() == 1 ? "Sta " : "Stanno ") + TestiDeiLuoghi.dentro(incarico.getLuogo()) + " qui vicino: ve lo segno sulla mappa. "
						+ getRicompensa() + " monete a lavoro fatto.")
				.parlaIlCapo(testo(incarico.getBattutaDelCapo()))
				.parlaIlMandante(testo(incarico.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getIncarico().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getIncarico().getRingraziamento()))
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse.");
	}

	@Override
	protected String testoAccettazione() {
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " pagherà " + getRicompensa() + " monete per sconfiggere "
				+ getNomeDeiNemici() + ".";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getIncarico().getMandante()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return getIncarico().getMonete();
	}
}
