package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroArtefatti;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tools.Misc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * In città qualcuno ha perso qualcosa nella foresta, vicino a un posto che si ricorda (un tempio, delle rovine, una
 * locanda, una grotta: vedi {@link OggettoSmarrito}, da missioni.txt). La missione rivendica il posto e lo segna sulla
 * mappa, e mette l'oggetto in una casella a non più di {@link #RAGGIO} passi da lì, anche già visitata: il gruppo lo
 * deve cercare. Trovato, lo riporta a chi l'ha perso, che paga (vedi IncaricoInCitta).
 * <ol>
 * <li>PARTENZA, in locazione, nella città: il posto compare sulla mappa;</li>
 * <li>RICERCA, a fine locazione: il gruppo ha raccolto l'oggetto, e torna in città.</li>
 * </ol>
 * L'oggetto, il posto e la casella si scelgono quando l'incarico si offre: la missione si ripete con altri.
 */
public class LOggettoSmarrito extends IncaricoInCitta {

	/**
	 * La chiave sotto cui la missione conta l'oggetto ritrovato.
	 */
	public static final String OGGETTO = "OGGETTO";
	public static final String SMARRITO = "SMARRITO";
	/**
	 * A quanti passi al più dal posto sta l'oggetto, contando i passi a nord, sud, est e ovest.
	 */
	public static final int RAGGIO = 2;
	private static final String CASELLA = "CASELLA";
	private static final String PARTENZA = "PARTENZA";
	private static final String RICERCA = "RICERCA";
	private static final String SEPARATORE = ",";
	/**
	 * Dove l'oggetto può stare: non nelle città e nei castelli, né nelle locande, che si completano appena si entra.
	 */
	private static final Set<TipoLocazione> NASCONDIGLI = EnumSet.of(TipoLocazione.RADURA, TipoLocazione.BOSCO,
			TipoLocazione.PALUDE, TipoLocazione.ROVINE, TipoLocazione.TEMPIO, TipoLocazione.GROTTA);

	public LOggettoSmarrito() {
		super(ClasseMissione.L_OGGETTO_SMARRITO);
	}

	public OggettoSmarrito getSmarrito() {
		return OggettoSmarrito.da(parametro(SMARRITO, ProduttoreDiTestiCasuale::oggettoSmarrito));
	}

	/**
	 * Il posto vicino a cui l'oggetto è stato perso, o null finché l'incarico non si offre.
	 */
	public CoordinateMD getPosto() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * La casella in cui sta l'oggetto, o null finché l'incarico non si offre.
	 */
	public CoordinateMD getCasella() {
		String valore = ottieniProprieta(CASELLA);
		if (valore == null) {
			return null;
		}
		String[] parti = valore.split(SEPARATORE);
		return new CoordinateMD(Integer.parseInt(parti[0]), Integer.parseInt(parti[1]));
	}

	/**
	 * Quando l'incarico si offre: un posto di una delle classi della riga, rivendicato (vedi
	 * RegistroMissioni.cercaOCostruisci), e una casella lì intorno per l'oggetto.
	 */
	private void scegliIlPosto() {
		if (getCasella() != null) {
			return;
		}
		List<TipoLocazione> posti = getSmarrito().getPosti();
		TipoLocazione classe = posti.get(Dado.tiraAncheAUnaFaccia(posti.size()) - 1);
		CoordinateMD posto = RegistroMissioni.cercaOCostruisci(classe, this)
				// Solo se nella foresta non c'è più neanche un bosco o una palude: allora l'ha perso appena fuori città
				.orElseGet(() -> Foresta.getCoordinateLocazioneUnica(getCitta()));
		List<CoordinateMD> nascondigli = nascondigliAttorno(posto);
		CoordinateMD casella = nascondigli.isEmpty() ? posto : nascondigli.get(Dado.tiraAncheAUnaFaccia(nascondigli.size()) - 1);
		aggiungiProprieta(CASELLA, casella.getX() + SEPARATORE + casella.getY());
	}

	/**
	 * Le caselle a non più di {@link #RAGGIO} passi dal posto in cui l'oggetto può stare: dentro la mappa, fra i
	 * {@link #NASCONDIGLI}, senza un artefatto del registro (che l'oggetto non può sostituire) e non rivendicate da
	 * un'altra missione.
	 */
	private List<CoordinateMD> nascondigliAttorno(CoordinateMD posto) {
		List<CoordinateMD> caselle = new ArrayList<>();
		for (int dx = -RAGGIO; dx <= RAGGIO; dx++) {
			for (int dy = -RAGGIO; dy <= RAGGIO; dy++) {
				CoordinateMD casella = new CoordinateMD(posto.getX() + dx, posto.getY() + dy);
				if (Math.abs(dx) + Math.abs(dy) <= RAGGIO
						&& casella.getX() >= 0 && casella.getX() < Foresta.getDimensioneX()
						&& casella.getY() >= 0 && casella.getY() < Foresta.getDimensioneY()
						&& NASCONDIGLI.contains(Foresta.getLocazione(casella))
						&& RegistroArtefatti.getArtefattoInLocazione(casella) == null
						&& RegistroMissioni.isDisponibile(casella, this)) {
					caselle.add(casella);
				}
			}
		}
		return caselle;
	}

	/**
	 * Il nome del posto, con l'articolo: "il Tempio del Sole", "la grotta"; "il posto" finché non c'è.
	 */
	private String getNomeDelPosto() {
		return TestiDeiLuoghi.nome(getPosto());
	}

	private String getProprietarioDiCitta() {
		return getSmarrito().getProprietario() + " di " + getNomeCitta();
	}

	@Override
	protected void allIncarico() {
		getSmarrito();
		scegliIlPosto();
	}

	@Override
	public String getNome() {
		OggettoSmarrito smarrito = getSmarrito();
		return Misc.inizialeMaiuscola(smarrito.getOggettoConArticolo()) + " " + Misc.conPreposizione("di", smarrito.getProprietario());
	}

	@Override
	public String getDescrizione() {
		OggettoSmarrito smarrito = getSmarrito();
		if (RITORNO.equals(getPassoCorrente())) {
			return "Hai trovato " + smarrito.getOggettoConArticolo() + ": riporta" + smarrito.getPronome() + " "
					+ Misc.conPreposizione("a", getProprietarioDiCitta()) + ".";
		}
		return "Cerca " + smarrito.getOggettoConArticolo() + " che " + getProprietarioDiCitta() + " ha perso vicino " + Misc.conPreposizione("a", getNomeDelPosto()) + ", che trovi sulla mappa: è al più a " + Misc.getCardinaleM(RAGGIO)
				+ " passi da lì.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "Qui intorno " + getSmarrito().getProprietario() + " di " + getNomeCitta() + " perse " + getSmarrito().getOggettoConArticolo() + ".";
	}

	@Override
	protected String primoPassoDelCompito() {
		return PARTENZA;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		OggettoSmarrito smarrito = getSmarrito();
		switch (id) {
			case PARTENZA:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaCitta)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getPosto());
							BusEventi.pubblica(new NotificaTestoParagrafo("Sulla mappa ora c'è " + getNomeDelPosto() + ": "
									+ smarrito.getOggettoConArticolo() + " è lì intorno."));
						})
						.poi(RICERCA);
			case RICERCA:
				return raccogli(MomentoControllo.POST_LOCAZIONE, getOggettoDaCercare())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Ecco " + smarrito.getOggettoConArticolo() + " "
								+ Misc.conPreposizione("di", smarrito.getProprietario()) + "! " + Misc.inizialeMaiuscola(smarrito.getProprietario())
								+ " " + smarrito.getPronome() + " aspetta a " + getNomeCitta() + ".")))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	/**
	 * L'oggetto da cercare, nella sua casella.
	 */
	public OggettiDaRaccogliere getOggettoDaCercare() {
		return OggettiDaRaccogliere.di(OGGETTO, getSmarrito().getNome(), 1).nellaCasella(getCasella());
	}

	@Override
	protected OggettiDaRaccogliere getOggettiDaConsegnare() {
		return OggettiDaRaccogliere.di(OGGETTO, getSmarrito().getNome(), 1);
	}

	@Override
	protected String testoConsegna() {
		OggettoSmarrito smarrito = getSmarrito();
		return Misc.inizialeMaiuscola(smarrito.getOggettoConArticolo()) + " torna " + Misc.conPreposizione("a", smarrito.getProprietario()) + ".";
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		OggettoSmarrito smarrito = getSmarrito();
		return ScenaInCitta.conMandante()
				.parlaIlMandante(smarrito.getRacconto())
				.parlaIlMandante("L'ho " + smarrito.getPerso() + " vicino " + Misc.conPreposizione("a", getNomeDelPosto())
						+ ". Vi segno il posto sulla mappa: dev'essere lì intorno, a non più di " + Misc.getCardinaleM(RAGGIO) + " passi. Riportatemel"
						+ (smarrito.isFemminile() ? "a" : "o") + " e avrete " + getRicompensa() + " monete.")
				.parlaIlCapo(smarrito.getBattutaDelCapo())
				.parlaIlMandante(smarrito.getRisposta());
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(getSmarrito().getRingraziamento())
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse.");
	}

	@Override
	protected String testoAccettazione() {
		OggettoSmarrito smarrito = getSmarrito();
		return Misc.inizialeMaiuscola(getProprietarioDiCitta()) + " pagherà " + getRicompensa() + " monete per "
				+ smarrito.getOggettoConArticolo() + " che ha perso vicino " + Misc.conPreposizione("a", getNomeDelPosto()) + ".";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getSmarrito().getProprietario()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return getSmarrito().getMonete();
	}
}
