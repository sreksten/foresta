package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.TestiDeiLuoghi;
import com.threeamigos.foresta.missioni.richieste.ColpoRichiesto;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.RegoleDellaLingua;
import com.threeamigos.foresta.testi.Testi;

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
		super(MissionePredefinita.IL_COLPO);
	}

	public ColpoRichiesto getColpo() {
		return ColpoRichiesto.da(parametro(COLPO, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni(COLPO)));
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
		return mandanteDiCitta(getColpo().getMandante());
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
		String guardie = Lingue.corrente().cardinale(colpo.getNumero(), Genere.MASCHILE) + " " + modello.getNomePlurale();
		return colpo.isConCapo() ? Testi.formatta("missioni", "Nemici.guidatiDa", guardie, getCapo()) : guardie;
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
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return RITORNO.equals(passo) ? "IL_COLPO_DESCRIZIONE_RITORNO" : "IL_COLPO_DESCRIZIONE_COLPO";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Tutti i segnaposto dei testi della missione: il luogo, le guardie, la città, le monete e il mandante nelle sue forme.
	 * Senza "private" per i test.
	 */
	Object[] valori() {
		ColpoRichiesto colpo = getColpo();
		RegoleDellaLingua lingua = Lingue.corrente();
		String mandante = mandante(colpo.getMandante());
		return new Object[] {
				"PARAM_DENTRO_LUOGO", TestiDeiLuoghi.dentro(colpo.getLuogo()),
				"PARAM_GUARDIE", getNomeDelleGuardie(),
				"PARAM_CITTA", getNomeCitta(),
				"PARAM_MONETE", getRicompensa(),
				"PARAM_MANDANTE_MAIUSC", lingua.inizialeMaiuscola(mandante),
				"PARAM_MANDANTE_DI_CITTA_MAIUSC", lingua.inizialeMaiuscola(getMandanteDiCitta()),
				"PARAM_PRESSO_MANDANTE", lingua.conPreposizione(Preposizione.PRESSO, mandante)};
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
							BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_COLPO_POSTO_SEGNATO", valori())));
						})
						.poi(COLPO);
			case COLPO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE,
								() -> getPosto() != null && getConteggioNelPassoCorrente(eventoInosservatoIn(getPosto())) > 0)
						.affronta(this::getPosto, getGuardie())
						.falliscoSe(this::haCombattutoNelPosto, () -> testo(colpo.getScoperti()))
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo(colpo.getColpo()) + " "
								+ Testi.formatta("missioni", "Incarico.aspettaA", Lingue.corrente().inizialeMaiuscola(mandante(colpo.getMandante())), getNomeCitta()))))
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
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_COLPO_INCARICO_1", valori()))
				.parlaIlCapo(testo(colpo.getBattutaDelCapo()))
				.parlaIlMandante(testo(colpo.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getColpo().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getColpo().getRingraziamento()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_COLPO_RINGRAZIAMENTO_2", valori()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_COLPO_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_COLPO_RICOMPENSA", valori());
	}

	@Override
	protected int getRicompensaBase() {
		return getColpo().getMonete();
	}
}
