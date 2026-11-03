package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.TestiDeiLuoghi;
import com.threeamigos.foresta.missioni.richieste.SoccorsoRichiesto;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.RegoleDellaLingua;
import com.threeamigos.foresta.testi.Testi;

/**
 * In città qualcuno chiede di riportare a casa una persona rimasta nella foresta, ferita o prigioniera, in mezzo a
 * dei nemici: il taglialegna circondato dalle arpie, il minatore bloccato in una grotta da un troll (vedi
 * {@link SoccorsoRichiesto}, da missioni.txt, e {@link LaLiberazione}). Sconfitti i nemici, la persona va riportata
 * viva in città. Il soccorso e il nome della persona si pescano quando l'incarico si offre: la missione si ripete con
 * altri.
 */
public class IlSoccorso extends LaLiberazione {

	public static final String SOCCORSO = "SOCCORSO";
	private static final String NOME = "NOME";
	private static final String CAPO = "CAPO";

	public IlSoccorso() {
		super(MissionePredefinita.IL_SOCCORSO);
	}

	public SoccorsoRichiesto getSoccorso() {
		return SoccorsoRichiesto.da(parametro(SOCCORSO, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni(SOCCORSO)));
	}

	/**
	 * Il nome di chi va riportato a casa, da missioni.txt (come gli ostaggi).
	 */
	public String getNomeDellaPersona() {
		return parametro(NOME, ProduttoreDiMissioniCasuale::nomeOstaggio);
	}

	/**
	 * Il nome del capo dei nemici, se il soccorso ne ha uno (da missioni.txt), altrimenti vuoto.
	 */
	public String getCapo() {
		SoccorsoRichiesto soccorso = getSoccorso();
		return soccorso.isConCapo() ? parametro(CAPO, soccorso::pescaNomeDelCapo) : "";
	}

	/**
	 * Il testo della grammatica, con i nomi al posto di %NOME% e %CAPO%.
	 */
	private String testo(String testo) {
		return testo.replace(SoccorsoRichiesto.NOME, getNomeDellaPersona()).replace(SoccorsoRichiesto.CAPO, getCapo());
	}

	private String getMandanteDiCitta() {
		return mandanteDiCitta(getSoccorso().getMandante());
	}

	/**
	 * "Gervasio, il taglialegna".
	 */
	private String getNomeEPersona() {
		return getNomeDellaPersona() + ", " + getSoccorso().getPersona();
	}

	/**
	 * I nemici per i testi: "la Chimera-drago", "tre Arpie", e con il capo "Gruk, il Troll", "cinque Goblin guidati da
	 * Gruk".
	 */
	private String getNomeDeiNemici() {
		SoccorsoRichiesto soccorso = getSoccorso();
		Personaggio modello = FabbricaPersonaggi.modello(soccorso.getNemico());
		if (soccorso.getNumero() == 1) {
			String nemico = modello.getADS() + modello.getNomeSingolare();
			return soccorso.isConCapo() ? getCapo() + ", " + nemico : nemico;
		}
		String nemici = Lingue.corrente().cardinale(soccorso.getNumero(), Genere.MASCHILE) + " " + modello.getNomePlurale();
		return soccorso.isConCapo() ? ProduttoreDiMissioniCasuale.testoDellaMissione("NEMICI_GUIDATI_DA", "PARAM_NEMICI", nemici, "PARAM_CAPO", getCapo()) : nemici;
	}

	@Override
	protected String getPersona() {
		return getNomeDellaPersona();
	}

	@Override
	protected TipoLocazione getLuogoDelCovo() {
		return getSoccorso().getLuogo();
	}

	@Override
	protected IncontroDiMissione getNemiciDelCovo() {
		SoccorsoRichiesto soccorso = getSoccorso();
		IncontroDiMissione nemici = IncontroDiMissione.di(soccorso.getNemico(), soccorso.getNumero());
		return soccorso.isConCapo() ? nemici.conCapo(getCapo()) : nemici;
	}

	@Override
	protected void allIncarico() {
		getSoccorso();
		getNomeDellaPersona();
		getCapo();
	}

	@Override
	public String getNome() {
		return testo(getSoccorso().getTitolo());
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		if (VIAGGIO.equals(passo)) {
			return "IL_SOCCORSO_DESCRIZIONE_VIAGGIO";
		}
		if (RITORNO.equals(passo)) {
			return "IL_SOCCORSO_DESCRIZIONE_RITORNO";
		}
		if (LUTTO.equals(passo) || FALLIMENTO.equals(passo)) {
			return "IL_SOCCORSO_DESCRIZIONE_LUTTO";
		}
		return getSoccorso().getNumero() == 1 ? "IL_SOCCORSO_DESCRIZIONE_UNO" : "IL_SOCCORSO_DESCRIZIONE_MOLTI";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Tutti i segnaposto dei testi della missione: la persona, la città, il luogo, i nemici, le monete e il mandante nelle sue forme.
	 * Senza "private" per i test.
	 */
	Object[] valori() {
		SoccorsoRichiesto soccorso = getSoccorso();
		RegoleDellaLingua lingua = Lingue.corrente();
		String mandante = mandante(soccorso.getMandante());
		return new Object[] {
				"PARAM_NOME", getNomeDellaPersona(),
				"PARAM_NOME_E_PERSONA", getNomeEPersona(),
				"PARAM_CITTA", getNomeCitta(),
				"PARAM_DENTRO_LUOGO", TestiDeiLuoghi.dentro(soccorso.getLuogo()),
				"PARAM_NEMICI", getNomeDeiNemici(),
				"PARAM_MONETE", getRicompensa(),
				"PARAM_SOCCORSO", testo(soccorso.getSoccorso()),
				"PARAM_MANDANTE", mandante,
				"PARAM_MANDANTE_MAIUSC", lingua.inizialeMaiuscola(mandante),
				"PARAM_MANDANTE_DI_CITTA_MAIUSC", lingua.inizialeMaiuscola(getMandanteDiCitta()),
				"PARAM_A_MANDANTE_SENZA_VIRGOLA", lingua.conPreposizione(Preposizione.A, mandanteSenzaVirgolaFinale(soccorso.getMandante()))};
	}

	@Override
	public String getRicordoDellaLocazione() {
		return testo(getSoccorso().getRicordo());
	}

	@Override
	protected String testoCovoSegnato() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_COVO_SEGNATO", valori());
	}

	@Override
	protected String testoLiberato() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_LIBERATO", valori());
	}

	@Override
	protected String testoMorto() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_MORTO", valori());
	}

	@Override
	protected String testoPortaChiusa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_PORTA_CHIUSA", valori());
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		SoccorsoRichiesto soccorso = getSoccorso();
		return soccorso.getAspetto().nuovaScena()
				.parlaIlMandante(testo(soccorso.getRichiesta()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_INCARICO_1", valori()))
				.parlaIlCapo(testo(soccorso.getBattutaDelCapo()))
				.parlaIlMandante(testo(soccorso.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getSoccorso().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getSoccorso().getRingraziamento()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_RINGRAZIAMENTO_2", valori()));
	}

	@Override
	protected ScenaInCitta scenaDelLutto() {
		return getSoccorso().getAspetto().nuovaScena()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_LUTTO_1", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_LUTTO_2"))
				.parlaIlMandante(testo(getSoccorso().getLutto()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_SOCCORSO_RICOMPENSA", valori());
	}

	@Override
	protected int getRicompensaBase() {
		return getSoccorso().getMonete();
	}
}
