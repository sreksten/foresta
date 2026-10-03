package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

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
		super(ClasseMissione.IL_SOCCORSO);
	}

	public SoccorsoRichiesto getSoccorso() {
		return SoccorsoRichiesto.da(parametro(SOCCORSO, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(SOCCORSO)));
	}

	/**
	 * Il nome di chi va riportato a casa, da missioni.txt (come gli ostaggi).
	 */
	public String getNomeDellaPersona() {
		return parametro(NOME, ProduttoreDiTestiCasuale::nomeOstaggio);
	}

	/**
	 * Il nome del capo dei nemici, se il soccorso ne ha uno (da missioni.txt), altrimenti vuoto.
	 */
	public String getCapo() {
		return getSoccorso().isConCapo() ? parametro(CAPO, ProduttoreDiTestiCasuale::nomeCapobanda) : "";
	}

	/**
	 * Il testo della grammatica, con i nomi al posto di %NOME% e %CAPO%.
	 */
	private String testo(String testo) {
		return testo.replace(SoccorsoRichiesto.NOME, getNomeDellaPersona()).replace(SoccorsoRichiesto.CAPO, getCapo());
	}

	private String getMandanteDiCitta() {
		return getSoccorso().getMandante() + " di " + getNomeCitta();
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
		Personaggio modello = soccorso.getNemico().getMoltiplicatoriDiClasse();
		if (soccorso.getNumero() == 1) {
			String nemico = modello.getADS() + modello.getNomeSingolare();
			return soccorso.isConCapo() ? getCapo() + ", " + nemico : nemico;
		}
		String nemici = Misc.getCardinaleM(soccorso.getNumero()) + " " + modello.getNomePlurale();
		return soccorso.isConCapo() ? nemici + " guidati da " + getCapo() : nemici;
	}

	@Override
	protected String getPersona() {
		return getNomeDellaPersona();
	}

	@Override
	protected ClassiLocazione getLuogoDelCovo() {
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
			return nome + " è salvo ma debole: riportalo vivo a " + getNomeCitta() + ", e proteggilo per strada.";
		}
		if (RITORNO.equals(passo)) {
			return nome + " è a casa: " + mandante + " ti aspetta a " + getNomeCitta() + ".";
		}
		if (LUTTO.equals(passo) || FALLIMENTO.equals(passo)) {
			return nome + " è morto: devi dare la notizia " + Misc.conPreposizione("a", mandante) + ", a " + getNomeCitta() + ".";
		}
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " ti ha chiesto di salvare " + getNomeEPersona()
				+ ", dal posto segnato sulla mappa, dove " + (getSoccorso().getNumero() == 1 ? "lo minaccia " : "lo minacciano ")
				+ getNomeDeiNemici() + ".";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return testo(getSoccorso().getRicordo());
	}

	@Override
	protected String testoCovoSegnato() {
		return "Il posto dove si trova " + getNomeDellaPersona() + " è segnato sulla mappa.";
	}

	@Override
	protected String testoLiberato() {
		return testo(getSoccorso().getSoccorso()) + " Riportatelo vivo a " + getNomeCitta() + ".";
	}

	@Override
	protected String testoMorto() {
		return getNomeDellaPersona() + " non ce l'ha fatta. Bisogna dirlo " + Misc.conPreposizione("a", getSoccorso().getMandante())
				+ ", a " + getNomeCitta() + ".";
	}

	@Override
	protected String testoPortaChiusa() {
		return Misc.inizialeMaiuscola(getSoccorso().getMandante()) + " chiude la porta senza dire una parola.";
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		SoccorsoRichiesto soccorso = getSoccorso();
		return soccorso.getAspetto().nuovaScena()
				.parlaIlMandante(testo(soccorso.getRichiesta()))
				.parlaIlMandante("È " + TestiDeiLuoghi.dentro(soccorso.getLuogo()) + " qui vicino: ve lo segno sulla mappa. "
						+ "Riportatelo vivo e avrete " + getRicompensa() + " monete.")
				.parlaIlCapo(testo(soccorso.getBattutaDelCapo()))
				.parlaIlMandante(testo(soccorso.getRisposta()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getSoccorso().getAspetto().nuovaScena()
				.parlaIlMandante(testo(getSoccorso().getRingraziamento()))
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse.");
	}

	@Override
	protected ScenaInCitta scenaDelLutto() {
		return getSoccorso().getAspetto().nuovaScena()
				.parlaIlMandante("Siete tornati! Ma... dov'è " + getNomeDellaPersona() + "?")
				.parlaIlCapo("Mi dispiace. Lo avevamo salvato, ma per strada ci hanno attaccati.")
				.parlaIlMandante(testo(getSoccorso().getLutto()));
	}

	@Override
	protected String testoAccettazione() {
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " pagherà " + getRicompensa() + " monete per riavere "
				+ getNomeDellaPersona() + " a casa vivo.";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getSoccorso().getMandante()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return getSoccorso().getMonete();
	}
}
