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
		return soccorso.isConCapo() ? nemici + " guidati da " + getCapo() : nemici;
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
		String passo = getPassoCorrente();
		String nome = getNomeDellaPersona();
		String mandante = getSoccorso().getMandante();
		if (VIAGGIO.equals(passo)) {
			return Testi.formatta("missioni", "IlSoccorso.t1", nome, getNomeCitta());
		}
		if (RITORNO.equals(passo)) {
			return Testi.formatta("missioni", "IlSoccorso.t2", nome, mandante(mandante), getNomeCitta());
		}
		if (LUTTO.equals(passo) || FALLIMENTO.equals(passo)) {
			return Testi.formatta("missioni", "IlSoccorso.t3", nome, Lingue.corrente().conPreposizione(Preposizione.A, mandanteSenzaVirgolaFinale(mandante)), getNomeCitta());
		}
		return Testi.formatta("missioni", getSoccorso().getNumero() == 1 ? "IlSoccorso.descrizioneUno" : "IlSoccorso.descrizioneMolti",
				Lingue.corrente().inizialeMaiuscola(getMandanteDiCitta()), getNomeEPersona(), getNomeDeiNemici());
	}

	@Override
	public String getRicordoDellaLocazione() {
		return testo(getSoccorso().getRicordo());
	}

	@Override
	protected String testoCovoSegnato() {
		return Testi.formatta("missioni", "IlSoccorso.t4", getNomeDellaPersona());
	}

	@Override
	protected String testoLiberato() {
		return Testi.formatta("missioni", "IlSoccorso.t5", testo(getSoccorso().getSoccorso()), getNomeCitta());
	}

	@Override
	protected String testoMorto() {
		return Testi.formatta("missioni", "IlSoccorso.t6", getNomeDellaPersona(), Lingue.corrente().conPreposizione(Preposizione.A, mandanteSenzaVirgolaFinale(getSoccorso().getMandante())), getNomeCitta());
	}

	@Override
	protected String testoPortaChiusa() {
		return Testi.formatta("missioni", "IlSoccorso.t7", Lingue.corrente().inizialeMaiuscola(mandante(getSoccorso().getMandante())));
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		SoccorsoRichiesto soccorso = getSoccorso();
		return soccorso.getAspetto().nuovaScena()
				.parlaIlMandante(testo(soccorso.getRichiesta()))
				.parlaIlMandante(Testi.formatta("missioni", "IlSoccorso.1", TestiDeiLuoghi.dentro(soccorso.getLuogo()), getRicompensa()))
				.parlaIlCapo(testo(soccorso.getBattutaDelCapo()))
				.parlaIlMandante(testo(soccorso.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getSoccorso().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getSoccorso().getRingraziamento()))
				.parlaIlMandante(Testi.formatta("missioni", "IlSoccorso.2", getRicompensa()));
	}

	@Override
	protected ScenaInCitta scenaDelLutto() {
		return getSoccorso().getAspetto().nuovaScena()
				.parlaIlMandante(Testi.formatta("missioni", "IlSoccorso.3", getNomeDellaPersona()))
				.parlaIlCapo(Testi.formatta("missioni", "IlSoccorso.4"))
				.parlaIlMandante(testo(getSoccorso().getLutto()));
	}

	@Override
	protected String testoAccettazione() {
		return Testi.formatta("missioni", "IlSoccorso.t8", Lingue.corrente().inizialeMaiuscola(getMandanteDiCitta()), getRicompensa(), getNomeDellaPersona());
	}

	@Override
	protected String testoRicompensa() {
		return Testi.formatta("missioni", "IlSoccorso.t9", Lingue.corrente().inizialeMaiuscola(mandante(getSoccorso().getMandante())), getRicompensa());
	}

	@Override
	protected int getRicompensaBase() {
		return getSoccorso().getMonete();
	}
}
