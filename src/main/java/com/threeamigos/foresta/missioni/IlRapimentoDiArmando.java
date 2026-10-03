package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

/**
 * In città una donna chiede di liberare suo marito Armando, rapito da una banda di goblin che lo tiene in una grotta:
 * la missione rivendica la grotta, la segna sulla mappa e ci mette la banda (vedi {@link MissioneAPassi#combatti}).
 * Sconfitta la banda, Armando si unisce al gruppo come ospite vulnerabile (vedi
 * {@link MissioneAPassi#prendiInScorta(MomentoControllo, java.util.function.BooleanSupplier, String, boolean)}): gli
 * avversari lo possono attaccare, e va riportato vivo in città. Se muore per strada, la missione resta aperta finché
 * il gruppo non torna in città a dare la notizia a sua moglie: allora c'è la scena triste, e la missione fallisce.
 */
public class IlRapimentoDiArmando extends IncaricoInCitta {

	public static final String ARMANDO = "Armando";
	public static final IncontroDiMissione RAPITORI = IncontroDiMissione.di(ClassePersonaggio.GOBLIN, 4).conCapo("Ghignazzo");
	private static final int RICOMPENSA = 35;
	private static final String COVO = "COVO";
	private static final String LIBERAZIONE = "LIBERAZIONE";
	private static final String LIBERATO = "LIBERATO";
	private static final String VIAGGIO = "VIAGGIO";
	private static final String LUTTO = "LUTTO";
	private static final String FALLIMENTO = "FALLIMENTO";

	public IlRapimentoDiArmando() {
		super(ClasseMissione.IL_RAPIMENTO_DI_ARMANDO);
	}

	/**
	 * Ha un personaggio con un nome proprio: finché il nome non viene da una grammatica, la stessa storia non si
	 * ripete.
	 */
	@Override
	protected boolean isRipetibile() {
		return false;
	}

	@Override
	public String getNome() {
		return "Il rapimento di Armando";
	}

	@Override
	public String getDescrizione() {
		String passo = getPassoCorrente();
		if (VIAGGIO.equals(passo)) {
			return "Armando è libero ma debole: riportalo vivo a " + getNomeCitta() + ", e proteggilo per strada.";
		}
		if (RITORNO.equals(passo)) {
			return "Armando è a casa: sua moglie ti aspetta a " + getNomeCitta() + ".";
		}
		if (LUTTO.equals(passo) || FALLIMENTO.equals(passo)) {
			return "Armando è morto: devi dare la notizia a sua moglie, a " + getNomeCitta() + ".";
		}
		return "Una banda di goblin tiene prigioniero Armando in una grotta segnata sulla mappa. Liberalo e riportalo a "
				+ getNomeCitta() + ".";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "In questa grotta i goblin di Ghignazzo tenevano prigioniero Armando.";
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
							BusEventi.pubblica(new NotificaTestoParagrafo("La grotta dove i goblin tengono prigioniero Armando è segnata sulla mappa."));
						})
						.poi(LIBERAZIONE);
			case LIBERAZIONE:
				return combatti(this::getCovo, RAPITORI).poi(LIBERATO);
			case LIBERATO:
				return prendiInScorta(MomentoControllo.POST_LOCAZIONE,
								() -> getCovo().equals(GruppoGiocatore.getIstanza().getCoordinate()), ARMANDO, true)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Armando è libero, ma è debole e non sa difendersi: "
								+ "riportatelo vivo a " + getNomeCitta() + ".")))
						.poi(VIAGGIO);
			case VIAGGIO:
				return scortaFinoAllaMeta(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getCoordinateLocazioneUnica(getCitta()))
						.esegui(() -> {
							if (isScortatoMorto()) {
								BusEventi.pubblica(new NotificaTestoParagrafo("Armando non ce l'ha fatta: i suoi rapitori avevano degli amici. "
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
							BusEventi.pubblica(new NotificaTestoParagrafo("La moglie di Armando chiude la porta senza dire una parola."));
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
				.parlaIlMandante("Vi prego, aiutatemi! Una banda di goblin ha rapito mio marito Armando.")
				.parlaIlMandante("Lo tengono in una grotta qui vicino, e il loro capo, Ghignazzo, chiede un riscatto che non posso pagare.")
				.parlaIlCapo("Il riscatto glielo portiamo noi, a modo nostro.")
				.parlaIlMandante("Riportatemelo vivo e avrete " + RICOMPENSA + " monete.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Armando! Sei tornato!")
				.parlaIlMandante("Non so come ringraziarvi. Ecco le " + RICOMPENSA + " monete.")
				.parlaIlCapo("Tenetelo d'occhio, la prossima volta.");
	}

	private ScenaInCitta scenaDelLutto() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Siete tornati! Ma... dov'è Armando?")
				.parlaIlCapo("Mi dispiace. Lo avevamo liberato, ma per strada ci hanno attaccati.")
				.parlaIlMandante("No... Non voglio sentire altro. Andate via.");
	}

	@Override
	protected String testoAccettazione() {
		return "La moglie di Armando pagherà " + RICOMPENSA + " monete per riaverlo a casa vivo.";
	}

	@Override
	protected String testoRicompensa() {
		return "La moglie di Armando paga le " + RICOMPENSA + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return RICOMPENSA;
	}
}
