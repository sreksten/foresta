package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tools.Misc;

/**
 * In città qualcuno chiede un colpo: rubare qualcosa, guastare le provviste di qualcuno, appiccare un fuoco, entrare
 * dove non si dovrebbe (vedi {@link ColpoRichiesto}, da missioni.txt). Il posto si segna sulla mappa, e lì ci sono le
 * guardie; il colpo riesce se il gruppo passa inosservato (vedi LocazioneBase, PASSA_INOSSERVATO). Se combatte lì,
 * il gruppo è scoperto e il colpo fallisce; se fugge, può riprovare un'altra volta, e le guardie sono ancora lì. Poi
 * si torna a riscuotere (vedi IncaricoInCitta). Il colpo si pesca quando si offre: la missione si ripete con altri.
 * <ol>
 * <li>COVO, in locazione, nella città: il posto compare sulla mappa;</li>
 * <li>COLPO, a fine locazione, nel posto: il gruppo è passato inosservato fra le guardie.</li>
 * </ol>
 */
public class IlColpo extends IncaricoInCitta {

	public static final String COLPO = "COLPO";
	private static final String CAPO = "CAPO";
	private static final String COVO = "COVO";

	public IlColpo() {
		super(ClasseMissione.IL_COLPO);
	}

	public ColpoRichiesto getColpo() {
		return ColpoRichiesto.da(parametro(COLPO, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(COLPO)));
	}

	/**
	 * Il nome del capo delle guardie, se c'è, altrimenti vuoto.
	 */
	public String getCapo() {
		ColpoRichiesto colpo = getColpo();
		return colpo.isConCapo() ? parametro(CAPO, colpo::pescaNomeDelCapo) : "";
	}

	/**
	 * Le guardie del posto, che si possono evitare passando inosservati.
	 */
	public IncontroDiMissione getGuardie() {
		ColpoRichiesto colpo = getColpo();
		IncontroDiMissione guardie = IncontroDiMissione.di(colpo.getGuardia(), colpo.getNumero()).aggirabile();
		return colpo.isConCapo() ? guardie.conCapo(getCapo()) : guardie;
	}

	/**
	 * Il posto del colpo, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getPosto() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	private String testo(String testo) {
		return testo.replace(ColpoRichiesto.CAPO, getCapo());
	}

	private String getMandanteDiCitta() {
		return getColpo().getMandante() + " di " + getNomeCitta();
	}

	/**
	 * Le guardie per i testi: "tre Hobgoblin", "il Troll", "quattro Goblin guidati da Gruk".
	 */
	private String getNomeDelleGuardie() {
		ColpoRichiesto colpo = getColpo();
		Personaggio modello = FabbricaPersonaggi.modello(colpo.getGuardia());
		if (colpo.getNumero() == 1) {
			String guardia = modello.getADS() + modello.getNomeSingolare();
			return colpo.isConCapo() ? getCapo() + ", " + guardia : guardia;
		}
		String guardie = Misc.getCardinaleM(colpo.getNumero()) + " " + modello.getNomePlurale();
		return colpo.isConCapo() ? guardie + " guidati da " + getCapo() : guardie;
	}

	private boolean haCombattutoNelPosto() {
		return getPosto() != null && getConteggioNelPassoCorrente(eventoCombattimentoIn(getPosto())) > 0;
	}

	@Override
	protected void allIncarico() {
		getColpo();
		getCapo();
	}

	@Override
	public String getNome() {
		return testo(getColpo().getTitolo());
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return "Il colpo è riuscito: torna " + Misc.conPreposizione("da", getColpo().getMandante()) + " a " + getNomeCitta() + " a riscuotere.";
		}
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " ti ha chiesto un colpo nel posto segnato sulla mappa, sorvegliato da "
				+ getNomeDelleGuardie() + ": passa inosservato. Se combatti lì, il colpo fallisce.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return isFallita() ? null : testo(getColpo().getRicordo());
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		ColpoRichiesto colpo = getColpo();
		switch (id) {
			case COVO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, colpo.getLuogo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getPosto());
							BusEventi.pubblica(new NotificaTestoParagrafo("Il posto del colpo è segnato sulla mappa."));
						})
						.poi(COLPO);
			case COLPO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE,
								() -> getPosto() != null && getConteggioNelPassoCorrente(eventoInosservatoIn(getPosto())) > 0)
						.affronta(this::getPosto, getGuardie())
						.falliscoSe(this::haCombattutoNelPosto, () -> testo(colpo.getScoperti()))
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(colpo.getColpo()) + " "
								+ Misc.inizialeMaiuscola(colpo.getMandante()) + " aspetta a " + getNomeCitta() + ".")))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		ColpoRichiesto colpo = getColpo();
		return colpo.getAspetto().nuovaScena()
				.parlaIlMandante(testo(colpo.getRichiesta()))
				.parlaIlMandante("Il posto è " + TestiDeiLuoghi.dentro(colpo.getLuogo()) + " qui vicino, e lo sorvegliano "
						+ getNomeDelleGuardie() + ": ve lo segno sulla mappa. Non fatevi vedere, e non combattete: se vi scoprono, "
						+ "è tutto perduto. " + getRicompensa() + " monete a colpo fatto.")
				.parlaIlCapo(testo(colpo.getBattutaDelCapo()))
				.parlaIlMandante(testo(colpo.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getColpo().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getColpo().getRingraziamento()))
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse. E non ci siamo mai visti.");
	}

	@Override
	protected String testoAccettazione() {
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " pagherà " + getRicompensa() + " monete per un colpo "
				+ TestiDeiLuoghi.dentro(getColpo().getLuogo()) + ".";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getColpo().getMandante()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return getColpo().getMonete();
	}
}
