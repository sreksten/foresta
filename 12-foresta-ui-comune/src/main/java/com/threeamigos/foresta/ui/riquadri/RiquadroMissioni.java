package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoCommutazioneElenco;
import com.threeamigos.foresta.interfacce.VistaMissione;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ElementoDelElenco;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.ImmagineDelNodo;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.TestoDelNodo;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.DoomdarkColorAlternante;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellAiuto;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Il riquadro con le missioni, senza disegno: un albero di missioni attive, poi (dopo un separatore) completate, poi (dopo un altro) fallite, che si scorre con
 * la rotella e col trascinamento e si apre e chiude con un click sul nome. L'albero ({@link ElencoScorrevole}) viene ricostruito a ogni disegno e a ogni
 * click: lo stato di apertura dei nodi non vive qui ma nel modello dati delle missioni. Chi disegna (Swing, libGDX) dice quanto è grande un'icona
 * ({@link #misuraDellIcona}), chiama {@link #sincronizza()} e disegna gli {@link #elementiVisibili()} nell'{@link #getAreaElenco() area dell'elenco},
 * sposta in su di {@link #getOffset()}.
 */
public abstract class RiquadroMissioni implements Finestra {

	/**
	 * Lo spessore del bordo interno della cornice grande più lo spazio: l'elenco parte da lì
	 */
	public static final int BORDO = 16 + SPACING;

	// Dove sta il mouse, relativo al riquadro, per l'aiuto (-1 fuori)
	private int mouseX = -1;
	private int mouseY = -1;
	// Pixel di scorrimento per ogni scatto della rotella
	private static final int PASSO_SCORRIMENTO = 2;
	private static final int INDENTAZIONE = 10;
	private static final int INTERLINEA = 1;

	private static final DoomdarkFont FONT_NOME = DoomdarkFont.medio();
	private static final DoomdarkFont FONT_DESCRIZIONE = DoomdarkFont.piccolo();

	private final int topLeftX;
	private int topLeftY;
	private final int larghezzaCornice;
	private final int larghezzaInterna;
	private final int altezzaInterna;
	private final VistaPartita vistaPartita;
	// Quanto la lista è scorsa: con la rotella e trascinandola (anche col dito, e le prosegue per inerzia)
	private final ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(System::nanoTime);
	private ElencoScorrevole<VistaMissione> elenco;

	/**
	 * @param larghezzaCornice larghezza della cornice grande, già con lo zoom
	 * @param altezzaCornice   altezza della cornice grande, già con lo zoom
	 */
	protected RiquadroMissioni(int topLeftX, int topLeftY, int larghezzaCornice, int altezzaCornice, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.larghezzaCornice = larghezzaCornice;
		this.larghezzaInterna = larghezzaCornice - 2 * BORDO;
		this.altezzaInterna = altezzaCornice - 2 * BORDO;
	}

	/**
	 * Quanto è grande l'icona (<code>icone/Missione-gallo.png</code>…) alla sua dimensione originale, come la tiene chi disegna; lo zoom lo applica questa classe
	 * ({@link #scalaDellIcona})
	 */
	protected abstract Misura misuraDellIcona(String risorsa);

	/**
	 * Di quanto si ingrandisce un'icona: l'icona della birra è l'unica che segue lo zoom
	 */
	public static float scalaDellIcona(String risorsa) {
		return NomiDelleImmagini.MISSIONE_BIRRA.equals(risorsa) ? (float) LivelloDiZoom.valore() : 1f;
	}

	private ImmagineDelNodo icona(String risorsa) {
		if (risorsa == null) {
			return null;
		}
		Misura originale = misuraDellIcona(risorsa);
		float scala = scalaDellIcona(risorsa);
		return new ImmagineDelNodo(risorsa, new Misura(Math.round(originale.larghezza * scala), Math.round(originale.altezza * scala)));
	}

	public int getTopLeftX() {
		return topLeftX;
	}

	public int getTopLeftY() {
		return topLeftY;
	}

	public int getLarghezzaCornice() {
		return larghezzaCornice;
	}

	/**
	 * Sposta il riquadro in verticale: topLeftY è la nuova quota del bordo superiore della cornice
	 */
	public void spostaInVerticale(int topLeftY) {
		this.topLeftY = topLeftY;
	}

	/**
	 * La parte del riquadro in cui sta l'elenco (dentro il bordo): fuori da lì il disegno si ritaglia
	 */
	public Area getAreaElenco() {
		return new Area(topLeftX + BORDO, topLeftY + BORDO, larghezzaInterna, altezzaInterna);
	}

	/**
	 * Ricostruisce l'albero dalla partita e rimette lo scorrimento nei limiti (l'elenco può essere cambiato dall'ultimo disegno). Da chiamare prima di ogni
	 * disegno.
	 */
	protected void sincronizza() {
		elenco = costruisciElenco();
		int massimo = elenco.getMassimoScorrimento(altezzaInterna);
		scorrimento.imposta(massimo > 0, massimo);
		scorrimento.aggiorna();
	}

	/**
	 * Di quanti pixel la lista è scorsa (può essere oltre un capo, se la si sta tirando: vedi {@link ScorrimentoSuUnAsse})
	 */
	public int getOffset() {
		return (int) Math.round(scorrimento.getScorrimento());
	}

	public boolean isFrecciaSuVisibile() {
		return elenco().isFrecciaSuVisibile(elenco().limitaOffset(altezzaInterna, getOffset()));
	}

	public boolean isFrecciaGiuVisibile() {
		return elenco().isFrecciaGiuVisibile(altezzaInterna, elenco().limitaOffset(altezzaInterna, getOffset()));
	}

	/**
	 * Quel che va disegnato dell'elenco: le coordinate sono quelle dell'elenco intero, da spostare nell'area e in su di {@link #getOffset()}
	 */
	public List<ElementoDelElenco> elementiVisibili() {
		return elenco().elementiVisibili(altezzaInterna, elenco().limitaOffset(altezzaInterna, getOffset()));
	}

	private ElencoScorrevole<VistaMissione> elenco() {
		if (elenco == null) {
			sincronizza();
		}
		return elenco;
	}

	/**
	 * Se l'elenco è più alto del riquadro (e quindi si scorre)
	 */
	public boolean isScorrevole() {
		return elenco().isScorrevole(altezzaInterna);
	}

	/**
	 * La missione il cui nome sta sotto un punto del riquadro (coordinate relative al suo angolo in alto a sinistra), o null se il punto è fuori dall'elenco
	 * o in uno spazio vuoto
	 */
	public VistaMissione missioneInPosizione(int x, int y) {
		int xInterno = x - BORDO;
		int yInterno = y - BORDO;
		// Il punto può cadere sulla cornice, fuori dall'elenco
		if (xInterno < 0 || xInterno >= larghezzaInterna || yInterno < 0 || yInterno >= altezzaInterna) {
			return null;
		}
		// La quota va espressa in coordinate della lista, non della finestra visibile
		return costruisciElenco().riferimentoTitoloAllaQuota(yInterno + getOffset());
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

	@Override
	public void processaMovimento(int x, int y) {
		mouseX = x;
		mouseY = y;
	}

	@Override
	public void processaUscita(int x, int y) {
		mouseX = -1;
		mouseY = -1;
	}

	/**
	 * Ad aiuto acceso, le righe del cartiglio accanto al mouse dentro l'elenco: sul nome di una missione che cosa fa il click
	 * (aprire o chiudere la descrizione), e se l'elenco è più alto del riquadro la rotella. Vuota se non c'è niente da dire; il
	 * cartiglio va accanto al mouse ({@link #getMouseX()}, {@link #getMouseY()}, relativi al riquadro).
	 */
	public List<String> righeDellAiuto() {
		List<String> righe = new ArrayList<>();
		if (!vistaPartita.isAiutoAbilitato() || mouseX < 0) {
			return righe;
		}
		int xInterno = mouseX - BORDO;
		int yInterno = mouseY - BORDO;
		if (xInterno < 0 || xInterno >= larghezzaInterna || yInterno < 0 || yInterno >= altezzaInterna) {
			return righe;
		}
		VistaMissione missione = missioneInPosizione(mouseX, mouseY);
		if (missione != null) {
			righe.add(TestiDellAiuto.click(missione.isDescrizioneVisibile(), TestiDellaUI.get("COSA_DESCRIZIONE")));
		}
		if (isScorrevole()) {
			righe.add(TestiDellAiuto.rotella());
		}
		return righe;
	}

	/**
	 * L'albero viene ricostruito a ogni disegno e a ogni click
	 */
	private ElencoScorrevole<VistaMissione> costruisciElenco() {
		ElencoScorrevole<VistaMissione> nuovo = new ElencoScorrevole<>(larghezzaInterna, INDENTAZIONE, INTERLINEA);
		DoomdarkColorAlternante coloreAlternante = new DoomdarkColorAlternante();
		for (VistaMissione missione : vistaPartita.getMissioniAttive()) {
			DoomdarkColorModel.Color colore = coloreAlternante.getColor();
			configuraNodo(nuovo.creaNodo(nome(missione, colore), null, null, descrizione(missione, colore), icona(nomeDellIcona(missione)), missione),
					missione, colore, true);
		}
		List<? extends VistaMissione> completate = vistaPartita.getMissioniCompletate();
		if (!completate.isEmpty()) {
			nuovo.creaSeparatore(icona(NomiDelleImmagini.SEPARATORE));
			for (VistaMissione missione : completate) {
				DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.DARK_GRAY;
				configuraNodo(nuovo.creaNodo(nome(missione, colore), null, null, descrizione(missione, colore), icona(nomeDellIcona(missione)), missione),
						missione, colore, false);
			}
		}
		// Le fallite per ultime, dopo un altro separatore, in rosso
		List<? extends VistaMissione> fallite = vistaPartita.getMissioniFallite();
		if (!fallite.isEmpty()) {
			nuovo.creaSeparatore(icona(NomiDelleImmagini.SEPARATORE));
			for (VistaMissione missione : fallite) {
				DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.RED;
				configuraNodo(nuovo.creaNodo(nome(missione, colore), null, null, descrizione(missione, colore), icona(nomeDellIcona(missione)), missione),
						missione, colore, false);
			}
		}
		return nuovo;
	}

	private static TestoDelNodo nome(VistaMissione missione, DoomdarkColorModel.Color colore) {
		return new TestoDelNodo(missione.getNome(), FONT_NOME, colore);
	}

	private static TestoDelNodo descrizione(VistaMissione missione, DoomdarkColorModel.Color colore) {
		return new TestoDelNodo(missione.getDescrizione(), FONT_DESCRIZIONE, colore);
	}

	/**
	 * Una sotto-missione non ancora attivata non viene mostrata. Fra le missioni in corso si nascondono anche le
	 * sotto-missioni completate, che compaiono fra le completate (vedi RegistroMissioni.getMissioniCompletate).
	 * Vale a ogni livello dell'albero.
	 */
	private void configuraNodo(ElencoScorrevole<VistaMissione>.Nodo nodo, VistaMissione missione, DoomdarkColorModel.Color colore, boolean nascondiCompletate) {
		nodo.setFigliVisibili(missione.isDescrizioneVisibile());
		for (VistaMissione secondaria : missione.getMissioniSecondarie()) {
			if (!secondaria.isAttiva() || (nascondiCompletate && secondaria.isCompleta())) {
				continue;
			}
			configuraNodo(nodo.creaNodo(nome(secondaria, colore), null, null, descrizione(secondaria, colore), null, secondaria), secondaria, colore,
					nascondiCompletate);
		}
	}

	private static String nomeDellIcona(VistaMissione missione) {
		switch (missione.getClasse()) {
			case NESSUN_BOCCALE_LASCIATO_INDIETRO:
			case CRONACHE_DI_UN_FEGATO_EROICO:
				return NomiDelleImmagini.MISSIONE_BIRRA;
			case DISTURBATORE_DELLA_QUIETE_PUBBLICA:
				return NomiDelleImmagini.MISSIONE_GALLO;
			default:
				return null;
		}
	}

	/**
	 * Il gesto che comincia sul riquadro prosegue anche se il puntatore ne esce, finché non si lascia
	 */
	@Override
	public boolean catturaIlTrascinamento() {
		return true;
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.premi(y, true);
		}
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		scorrimento.trascina(y);
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.rilascia();
		}
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		// Il rilascio di un trascinamento non è un click sul nome di una missione
		if (tasto != Tasto.SINISTRO || scorrimento.isGestoTrascinato()) {
			return;
		}
		VistaMissione missione = missioneInPosizione(x, y);
		if (missione != null) {
			BusEventi.pubblica(ComandoCommutazioneElenco.missione(missione.getId()));
		}
	}

	@Override
	public int passiPerScattoDellaRotella() {
		return PASSI_PER_SCATTO_DELLE_LISTE;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		int pixel = numeroRotazioni * PASSO_SCORRIMENTO;
		scorrimento.scorriDi(movimentoRotella == MovimentoRotella.SU ? -pixel : pixel);
	}

	protected VistaPartita getVistaPartita() {
		return vistaPartita;
	}

	protected int getAltezzaInterna() {
		return altezzaInterna;
	}
}
