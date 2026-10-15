package com.threeamigos.foresta.missioni.principali;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.BattutaIntermezzo;
import com.threeamigos.foresta.intermezzi.ElementoIntermezzo;
import com.threeamigos.foresta.intermezzi.ImmagineIntermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.Tappa;
import com.threeamigos.foresta.intermezzi.Verso;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Collections;
import java.util.List;

/**
 * Sette templi liberati dai mostri che li infestano. La missione parte quando il gruppo ripulisce il primo tempio: a
 * locazione conclusa si presenta un sacerdote o una sacerdotessa (a caso, e sempre lo stesso fino alla fine) che chiede
 * di liberarne altri sei. Ogni tempio ripulito, il primo compreso, viene segnato sulla casella
 * ({@link Tempio#ripulisci}) e diventa una locazione sicura, senza mostri né oggetti. Al settimo il sacerdote
 * ringrazia a nome dei devoti e regala due incantesimi della Luce Divina, due dell'Alba Sacra e uno elementale a caso.
 * <ol>
 * <li>INIZIO, a fine locazione, se il gruppo ha ripulito un tempio: l'intermezzo della presentazione;</li>
 * <li>TEMPLI, a fine locazione: ogni tempio ripulito si segna e si conta;</li>
 * <li>RINGRAZIAMENTO, subito: l'intermezzo del ringraziamento e il premio.</li>
 * </ol>
 * Un tempio è ripulito quando il gruppo ne esce dopo aver sconfitto tutti i suoi custodi: fuggire non basta.
 */
public class IlGiroDelleSetteChiese extends MissioneAPassi {

	public static final int TEMPLI_DA_LIBERARE = 7;
	public static final int INCANTESIMI_LUCE_DIVINA = 2;
	public static final int INCANTESIMI_ALBA_SACRA = 2;
	public static final ClasseIncantesimo[] INCANTESIMI_ELEMENTALI = {ClasseIncantesimo.ARIA, ClasseIncantesimo.ACQUA,
			ClasseIncantesimo.TERRA, ClasseIncantesimo.FUOCO, ClasseIncantesimo.FULMINE, ClasseIncantesimo.GELO};

	private static final String INIZIO = "INIZIO";
	private static final String TEMPLI = "TEMPLI";
	private static final String RINGRAZIAMENTO = "RINGRAZIAMENTO";
	private static final String SACERDOTE = "SACERDOTE";
	private static final String INCANTESIMO_ELEMENTALE = "INCANTESIMO_ELEMENTALE";

	private static final String ID_EROE = "eroe";
	private static final String ID_SACERDOTE = "sacerdote";

	public IlGiroDelleSetteChiese() {
		super(ClasseMissione.IL_GIRO_DELLE_SETTE_CHIESE);
	}

	@Override
	public String getNome() {
		return "Il Giro delle Sette Chiese";
	}

	@Override
	public String getDescrizione() {
		String cardinale = Misc.getCardinaleM(TEMPLI_DA_LIBERARE);
		if (isCompleta()) {
			return "Sette templi liberati dai mostri: i devoti sono tornati in pellegrinaggio.";
		}
		if (!isAttiva()) {
			return "Qualcuno, lassù, apprezzerebbe che un tempio infestato tornasse a essere un luogo di preghiera.";
		}
		int mancanti = TEMPLI_DA_LIBERARE - contaTempliRipuliti();
		String descrizione = "Aiuta " + (getSacerdote() == TipoPersonaggio.SACERDOTESSA ? "la sacerdotessa" : "il sacerdote") + " a liberare " + cardinale
				+ " templi dai mostri che li infestano.";
		if (mancanti == 1) {
			return descrizione + " Ne manca uno.";
		}
		return descrizione + " Ne mancano " + Misc.getCardinaleM(mancanti) + ".";
	}

	/**
	 * La classe di chi chiede aiuto: SACERDOTE o SACERDOTESSA, a seconda di come è andato il sorteggio. Prima che la
	 * missione parta è un SACERDOTE.
	 */
	private TipoPersonaggio getSacerdote() {
		String classe = ottieniProprieta(SACERDOTE);
		return classe == null ? TipoPersonaggio.SACERDOTE : TipoPersonaggio.valueOf(classe);
	}

	/**
	 * Il tempio in cui il gruppo si trova adesso è stato appena ripulito: i suoi custodi sono tutti sconfitti.
	 * Un tempio già ripulito non conta di nuovo.
	 */
	private boolean isTempioAppenaRipulito() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		return gruppo.getTipoLocazioneCorrente() == TipoLocazione.TEMPIO
				&& GruppoAvversario.getIstanza().getPersonaggiVivi().isEmpty()
				&& !Tempio.isRipulito(Foresta.getLocazioneMD(gruppo.getCoordinate()));
	}

	private void ripulisciIlTempioCorrente() {
		Tempio.ripulisci(Foresta.getLocazioneMD(GruppoGiocatore.getIstanza().getCoordinate()));
	}

	private void scegliIlSacerdote() {
		aggiungiProprieta(SACERDOTE, (Dado.tiraAncheAUnaFaccia(2) == 1 ? TipoPersonaggio.SACERDOTE : TipoPersonaggio.SACERDOTESSA).name());
		ripulisciIlTempioCorrente();
		attivaMissione();
	}

	private void contaIlTempio() {
		if (!isTempioAppenaRipulito()) {
			return;
		}
		ripulisciIlTempioCorrente();
		int ripuliti = contaTempliRipuliti();
		if (ripuliti < TEMPLI_DA_LIBERARE) {
			BusEventi.pubblica(new NotificaTestoParagrafo("Il tempio è libero dai mostri: sono " + Misc.getCardinaleM(ripuliti)
					+ " su " + Misc.getCardinaleM(TEMPLI_DA_LIBERARE) + "."));
		}
	}

	private int contaTempliRipuliti() {
		int ripuliti = 0;
		for (int y = 0; y < Foresta.getDimensioneY(); y++) {
			for (int x = 0; x < Foresta.getDimensioneX(); x++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				if (Foresta.getLocazione(coordinate) == TipoLocazione.TEMPIO && Tempio.isRipulito(Foresta.getLocazioneMD(coordinate))) {
					ripuliti++;
				}
			}
		}
		return ripuliti;
	}

	/**
	 * Due Luce Divina, due Alba Sacra e un incantesimo elementale a caso, che si ricorda per il ringraziamento.
	 */
	private void premia() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		ClasseIncantesimo elementale = INCANTESIMI_ELEMENTALI[Dado.tiraAncheAUnaFaccia(INCANTESIMI_ELEMENTALI.length) - 1];
		aggiungiProprieta(INCANTESIMO_ELEMENTALE, elementale.name());
		gruppo.addIncantesimi(ClasseIncantesimo.LUCE_DIVINA, INCANTESIMI_LUCE_DIVINA);
		gruppo.addIncantesimi(ClasseIncantesimo.ALBA_SACRA, INCANTESIMI_ALBA_SACRA);
		gruppo.addIncantesimi(elementale, 1);
		BusEventi.pubblica(new NotificaTestoParagrafo("In dono dai devoti: " + Misc.getCardinaleM(INCANTESIMI_LUCE_DIVINA) + " "
				+ ClasseIncantesimo.LUCE_DIVINA.getNomePlurale() + ", " + Misc.getCardinaleM(INCANTESIMI_ALBA_SACRA) + " "
				+ ClasseIncantesimo.ALBA_SACRA.getNomePlurale() + " e un " + elementale.getNomeSingolare() + "."));
	}

	private ClasseIncantesimo getIncantesimoElementale() {
		return ClasseIncantesimo.valueOf(ottieniProprieta(INCANTESIMO_ELEMENTALE));
	}

	/**
	 * La scena nel tempio: l'eroe a sinistra, il sacerdote che arriva da destra e si ferma davanti a lui. Il testo in
	 * alto usa l'alfabeto grande, senza accenti.
	 */
	private PaginaIntermezzo scenaNelTempio(String testo) {
		return new PaginaIntermezzo(testo)
				.conSfondo(ImmagineIntermezzo.locazione(TipoLocazione.TEMPIO))
				.conElemento(ElementoIntermezzo.personaggio(ID_SACERDOTE, getSacerdote(), 1.1, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.SINISTRA)
						.poi(Tappa.inSecondi(2).verso(0.62, 0.61)))
				.conElemento(ElementoIntermezzo.personaggio(ID_EROE, GruppoGiocatore.getIstanza().getCapo().getClasse(), 0.38, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.DESTRA));
	}

	private List<PaginaIntermezzo> getPagineDellaPresentazione() {
		boolean donna = getSacerdote() == TipoPersonaggio.SACERDOTESSA;
		return Collections.singletonList(scenaNelTempio("Il tempio e' libero dai mostri.")
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Siate benedetti, viandanti! Questi muri erano infestati dai mostri, "
						+ "e nessun devoto osava più venire a pregare.").daSecondo(2))
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Sono " + (donna ? "una sacerdotessa" : "un sacerdote")
						+ " e mi prendo cura di questi luoghi sacri."))
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Altri sei templi della Foresta soffrono dello stesso male. "
						+ "Volete aiutarmi a liberarli?"))
				.conBattuta(BattutaIntermezzo.di(ID_EROE, "Va bene, ci penseremo noi."))
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Che la Luce vi accompagni. Ogni tempio che libererete "
						+ "tornerà a essere un rifugio sicuro.")));
	}

	private List<PaginaIntermezzo> getPagineDelRingraziamento() {
		return Collections.singletonList(scenaNelTempio("Sette templi liberi.")
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Sette templi liberi! Grazie a voi i devoti potranno tornare "
						+ "in pellegrinaggio.").daSecondo(2))
				.conBattuta(BattutaIntermezzo.di(ID_SACERDOTE, "Accettate questi doni a nome loro: "
						+ Misc.getCardinaleM(INCANTESIMI_LUCE_DIVINA) + " " + ClasseIncantesimo.LUCE_DIVINA.getNomePlurale() + ", "
						+ Misc.getCardinaleM(INCANTESIMI_ALBA_SACRA) + " " + ClasseIncantesimo.ALBA_SACRA.getNomePlurale()
						+ " e un " + getIncantesimoElementale().getNomeSingolare() + "."))
				.conBattuta(BattutaIntermezzo.di(ID_EROE, "Siamo onorati. Che la Luce vi accompagni.")));
	}

	@Override
	protected String passoIniziale() {
		return INIZIO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case INIZIO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, this::isTempioAppenaRipulito)
						.esegui(this::scegliIlSacerdote)
						.conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA, this::getPagineDellaPresentazione)
						.poi(TEMPLI);
			case TEMPLI:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> contaTempliRipuliti() >= TEMPLI_DA_LIBERARE)
						.aOgniControllo(this::contaIlTempio)
						.poi(RINGRAZIAMENTO);
			case RINGRAZIAMENTO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> true)
						.esegui(this::premia)
						.conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA, this::getPagineDelRingraziamento)
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
