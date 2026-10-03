package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

/**
 * In città una donna chiede di liberare suo marito, rapito da una banda di goblin che lo tiene in una grotta: la
 * missione rivendica la grotta, la segna sulla mappa e ci mette la banda (vedi {@link MissioneAPassi#combatti}).
 * Sconfitta la banda, l'ostaggio si unisce al gruppo come ospite vulnerabile (vedi
 * {@link MissioneAPassi#prendiInScorta(MomentoControllo, java.util.function.BooleanSupplier, String, boolean)}): gli
 * avversari lo possono attaccare, e va riportato vivo in città. Se muore per strada, la missione resta aperta finché
 * il gruppo non torna in città a dare la notizia a sua moglie: allora c'è la scena triste, e la missione fallisce.
 * <p>
 * I nomi dell'ostaggio e del capobanda vengono da missioni.txt, pescati quando l'incarico si offre: la missione si
 * ripete con altri nomi.
 */
public class IlRapimento extends IncaricoInCitta {

	private static final String OSTAGGIO = "OSTAGGIO";
	private static final String CAPOBANDA = "CAPOBANDA";
	public static final int RAPITORI = 4;
	private static final int RICOMPENSA = 35;
	private static final String COVO = "COVO";
	private static final String LIBERAZIONE = "LIBERAZIONE";
	private static final String LIBERATO = "LIBERATO";
	private static final String VIAGGIO = "VIAGGIO";
	private static final String LUTTO = "LUTTO";
	private static final String FALLIMENTO = "FALLIMENTO";

	public IlRapimento() {
		super(ClasseMissione.IL_RAPIMENTO);
	}

	/**
	 * Il nome dell'ostaggio, da missioni.txt.
	 */
	public String getOstaggio() {
		return parametro(OSTAGGIO, ProduttoreDiTestiCasuale::nomeOstaggio);
	}

	/**
	 * Il nome del capo dei rapitori, da missioni.txt.
	 */
	public String getCapobanda() {
		return parametro(CAPOBANDA, ProduttoreDiTestiCasuale::nomeCapobanda);
	}

	/**
	 * La banda di goblin dei rapitori, con il loro capo.
	 */
	public IncontroDiMissione getRapitori() {
		return IncontroDiMissione.di(ClassePersonaggio.GOBLIN, RAPITORI).conCapo(getCapobanda());
	}

	@Override
	protected void allIncarico() {
		getOstaggio();
		getCapobanda();
	}

	@Override
	public String getNome() {
		return "Il rapimento di " + getOstaggio();
	}

	@Override
	public String getDescrizione() {
		String passo = getPassoCorrente();
		if (VIAGGIO.equals(passo)) {
			return getOstaggio() + " è libero ma debole: riportalo vivo a " + getNomeCitta() + ", e proteggilo per strada.";
		}
		if (RITORNO.equals(passo)) {
			return getOstaggio() + " è a casa: sua moglie ti aspetta a " + getNomeCitta() + ".";
		}
		if (LUTTO.equals(passo) || FALLIMENTO.equals(passo)) {
			return getOstaggio() + " è morto: devi dare la notizia a sua moglie, a " + getNomeCitta() + ".";
		}
		return "La banda di goblin di " + getCapobanda() + " tiene prigioniero " + getOstaggio()
				+ " in una grotta segnata sulla mappa. Liberalo e riportalo a " + getNomeCitta() + ".";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "In questa grotta i goblin di " + getCapobanda() + " tenevano prigioniero " + getOstaggio() + ".";
	}

	/**
	 * La grotta dei rapitori, o null finché la missione non l'ha trovata.
	 */
	public CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case COVO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, ClassiLocazione.GROTTA)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getCovo());
							BusEventi.pubblica(new NotificaTestoParagrafo("La grotta dove i goblin tengono prigioniero " + getOstaggio()
									+ " è segnata sulla mappa."));
						})
						.poi(LIBERAZIONE);
			case LIBERAZIONE:
				return combatti(this::getCovo, getRapitori()).poi(LIBERATO);
			case LIBERATO:
				return prendiInScorta(MomentoControllo.POST_LOCAZIONE,
								() -> getCovo().equals(GruppoGiocatore.getIstanza().getCoordinate()), getOstaggio(), true)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(getOstaggio() + " è libero, ma è debole e non sa difendersi: "
								+ "riportatelo vivo a " + getNomeCitta() + ".")))
						.poi(VIAGGIO);
			case VIAGGIO:
				return scortaFinoAllaMeta(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getCoordinateLocazioneUnica(getCitta()))
						.esegui(() -> {
							if (isScortatoMorto()) {
								BusEventi.pubblica(new NotificaTestoParagrafo(getOstaggio() + " non ce l'ha fatta: i suoi rapitori avevano degli amici. "
										+ "Bisogna dirlo a sua moglie, a " + getNomeCitta() + "."));
							}
						})
						.poi(() -> isScortatoMorto() ? LUTTO : RITORNO);
			case LUTTO:
				// A inizio locazione la scena, in locazione il fallimento: così l'avviso arriva dopo la scena
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nellaCitta)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDelLutto().getPagine())
						.poi(FALLIMENTO);
			case FALLIMENTO:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaCitta)
						.esegui(() -> {
							BusEventi.pubblica(new NotificaTestoParagrafo("La moglie di " + getOstaggio() + " chiude la porta senza dire una parola."));
							fallisciMissione();
						})
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Vi prego, aiutatemi! Una banda di goblin ha rapito mio marito " + getOstaggio() + ".")
				.parlaIlMandante("Lo tengono in una grotta qui vicino, e il loro capo, " + getCapobanda()
						+ ", chiede un riscatto che non posso pagare.")
				.parlaIlCapo("Il riscatto glielo portiamo noi, a modo nostro.")
				.parlaIlMandante("Riportatemelo vivo e avrete " + RICOMPENSA + " monete.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(getOstaggio() + "! Sei tornato!")
				.parlaIlMandante("Non so come ringraziarvi. Ecco le " + RICOMPENSA + " monete.")
				.parlaIlCapo("Tenetelo d'occhio, la prossima volta.");
	}

	private ScenaInCitta scenaDelLutto() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Siete tornati! Ma... dov'è " + getOstaggio() + "?")
				.parlaIlCapo("Mi dispiace. Lo avevamo liberato, ma per strada ci hanno attaccati.")
				.parlaIlMandante("No... Non voglio sentire altro. Andate via.");
	}

	@Override
	protected String testoAccettazione() {
		return "La moglie di " + getOstaggio() + " pagherà " + RICOMPENSA + " monete per riaverlo a casa vivo.";
	}

	@Override
	protected String testoRicompensa() {
		return "La moglie di " + getOstaggio() + " paga le " + RICOMPENSA + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return RICOMPENSA;
	}
}
