package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.sprite.CoordinateFumetto;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * La base di una interazione tra due elenchi di oggetti scambiabili, senza disegno:
 * <ul>
 *     <li>Personaggio e inventario del gruppo</li>
 *     <li>Inventario del gruppo e Armaiolo (che vende Artefatti)</li>
 *     <li>Inventario del gruppo e Alchimista (che vende Consumabili)</li>
 *     <li>Le tattiche di un personaggio</li>
 * </ul>
 * L'idea generale è che abbiamo due elenchi di oggetti scambiabili, uno a sinistra e uno a destra, ognuno in una cornice dell'inventario; sulla colonna
 * centrale un personaggio che può essere o il commerciante o il Personaggio attivo. Qui ci sono le tre zone (sinistra, centrale, destra), il loro scorrimento
 * a mano (rotella e trascinamento), il mouse, e l'intestazione del negoziante al centro (nome su più righe e mestiere in piccolo); chi disegna (Swing, libGDX)
 * mette le cornici, gli elenchi e il personaggio.
 *
 * @author Stefano Reksten
 */
public abstract class SchermataDiScambio implements Finestra {

	protected static final int SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI = 20;

	/**
	 * Fra una riga e l'altra del nome del negoziante, e fra il nome e il mestiere scritto in piccolo sotto
	 */
	public static final int INTERLINEA_DEL_NOME = 1;
	public static final int SPAZIO_FRA_NOME_E_MESTIERE = 4;

	/**
	 * La dimensione interna del bordo della cornice inventario
	 */
	public static final int DIMENSIONE_BORDO_INTERNO = 16;

	/**
	 * Pixel di scorrimento per ogni scatto della rotella
	 */
	public static final int PASSO_SCORRIMENTO = 2;

	/**
	 * Lo stato della partita, in sola lettura: monete, prezzi, personaggi e inventario del gruppo
	 */
	protected final VistaPartita vistaPartita;

	/**
	 * Larghezza globale della finestra di scambio
	 */
	protected final int width;
	/**
	 * Altezza globale della finestra di scambio
	 */
	protected final int height;

	/**
	 * Il font usato per disegnare il testo nella finestra di scambio
	 */
	public final DoomdarkFont font = DoomdarkFont.medio();
	/**
	 * L'altezza del font usato per disegnare il testo nella finestra di scambio
	 */
	public final int fontHeight = font.getHeight();
	/**
	 * Il font piccolo usato per disegnare le descrizioni dei nodi nell'albero degli oggetti
	 */
	public final DoomdarkFont fontSmall = DoomdarkFont.piccolo();

	final int larghezzaCornice;
	private final int altezzaCornice;
	private final MisureDelleImmagini misure;
	/**
	 * Usiamo l'immagine del Ladro come riferimento per disegnare il personaggio centrale della finestra di scambio
	 */
	protected final int altezzaLadro;

	/**
	 * Coordinata X iniziale della parte sinistra
	 */
	protected final int xMinimaZonaSinistra;
	/**
	 * Coordinata X finale della parte sinistra
	 */
	protected final int xMassimaZonaSinistra;
	/**
	 * Coordinata X iniziale della parte centrale
	 */
	public final int xMinimaZonaCentrale;
	/**
	 * Coordinata X finale della parte centrale
	 */
	public final int xMassimaZonaCentrale;
	/**
	 * Coordinata X iniziale della parte destra
	 */
	protected final int xMinimaZonaDestra;
	/**
	 * Coordinata X finale della parte destra
	 */
	protected final int xMassimaZonaDestra;

	/**
	 * Scroll verso l'alto o il basso per ciascuna zona
	 */
	protected final ScorrimentoSuUnAsse scorrimentoSinistra = new ScorrimentoSuUnAsse(System::nanoTime);
	protected final ScorrimentoSuUnAsse scorrimentoCentrale = new ScorrimentoSuUnAsse(System::nanoTime);
	protected final ScorrimentoSuUnAsse scorrimentoDestra = new ScorrimentoSuUnAsse(System::nanoTime);
	/**
	 * Lo scorrimento della zona su cui è stato premuto il tasto, per il trascinamento (null se la pressione è caduta fuori)
	 */
	private ScorrimentoSuUnAsse scorrimentoPremuto;

	/**
	 * Posizione del mouse, per evidenziare in bianco l'artefatto sotto il cursore.
	 * -1 significa "cursore fuori dalla finestra".
	 */
	protected int mouseX = -1;
	protected int mouseY = -1;

	/**
	 * @param larghezzaCornice la larghezza della cornice dell'inventario, già con lo zoom
	 * @param altezzaCornice   la sua altezza
	 * @param misure           per le misure dei personaggi al centro
	 */
	protected SchermataDiScambio(int width, int height, VistaPartita vistaPartita, int larghezzaCornice, int altezzaCornice, MisureDelleImmagini misure) {
		this.vistaPartita = vistaPartita;
		this.width = width;
		this.height = height;
		this.larghezzaCornice = larghezzaCornice;
		this.altezzaCornice = altezzaCornice;
		this.misure = misure;
		this.altezzaLadro = misure.personaggio(TipoPersonaggio.LADRO).altezza;

		// Calcola l'ampiezza delle tre colonne sinistra, centrale, destra
		xMinimaZonaSinistra = SPACING + DIMENSIONE_BORDO_INTERNO;
		xMassimaZonaSinistra = xMinimaZonaSinistra + larghezzaCornice - DIMENSIONE_BORDO_INTERNO;

		xMinimaZonaCentrale = SPACING + larghezzaCornice + SPACING;
		xMassimaZonaCentrale = width - SPACING - larghezzaCornice - SPACING;

		xMinimaZonaDestra = width - SPACING - larghezzaCornice + DIMENSIONE_BORDO_INTERNO;
		xMassimaZonaDestra = width - SPACING - DIMENSIONE_BORDO_INTERNO;
	}

	protected int offsetYZonaSinistra() {
		return (int) Math.round(scorrimentoSinistra.getScorrimento());
	}

	protected int offsetYZonaCentrale() {
		return (int) Math.round(scorrimentoCentrale.getScorrimento());
	}

	protected int offsetYZonaDestra() {
		return (int) Math.round(scorrimentoDestra.getScorrimento());
	}

	/**
	 * La larghezza e l'altezza dello spazio per un elenco dentro la cornice dell'inventario
	 */
	public int larghezzaDisponibileInRiquadroInventario() {
		return larghezzaCornice - 2 * (DIMENSIONE_BORDO_INTERNO + SPACING);
	}

	public int altezzaDisponibileInRiquadroInventario() {
		return altezzaCornice - 2 * (DIMENSIONE_BORDO_INTERNO + SPACING);
	}

	/**
	 * Chiamato quando la schermata si apre: le liste ripartono dall'alto.
	 */
	public void apri() {
		scorrimentoSinistra.reimposta();
		scorrimentoCentrale.reimposta();
		scorrimentoDestra.reimposta();
		scorrimentoPremuto = null;
	}

	/**
	 * Quota (in coordinate della finestra) a cui le sottoclassi disegnano la riga "Monete" nella colonna del personaggio, usata per ancorare gli sprite
	 * locali a quella riga.
	 */
	public int yRigaMonete() {
		return 3 * SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + altezzaIntestazioneCentrale() + altezzaLadro;
	}

	/**
	 * Il mestiere del negoziante al centro della schermata (armaiolo, alchimista...): serve a cercarne il nome. Null se
	 * al centro c'è un altro personaggio, che si intitola come prima.
	 */
	public TipoPersonaggio getMestiereAlCentro() {
		return null;
	}

	/**
	 * Il mestiere come si scrive sotto il nome del negoziante ("Armaiolo"), o al posto del nome se non c'è.
	 */
	public String getEtichettaMestiere() {
		return null;
	}

	/**
	 * Il nome del negoziante spezzato in righe che stiano fra le due cornici; vuota se non c'è un negoziante o un nome.
	 */
	public List<String> getRigheDelNomeAlCentro() {
		TipoPersonaggio mestiere = getMestiereAlCentro();
		String nome = mestiere == null ? null : vistaPartita.getNomeDelPersonaggioDiCitta(mestiere);
		if (nome == null) {
			return new ArrayList<>();
		}
		return FontTool.split(font, nome, xMassimaZonaCentrale - xMinimaZonaCentrale);
	}

	/**
	 * L'altezza dell'intestazione della colonna centrale: una riga per chi non è un negoziante, per il negoziante il nome
	 * su una o più righe e sotto il mestiere in carattere piccolo.
	 */
	public int altezzaIntestazioneCentrale() {
		List<String> righe = getRigheDelNomeAlCentro();
		if (righe.isEmpty()) {
			return fontHeight;
		}
		return righe.size() * (fontHeight + INTERLINEA_DEL_NOME) - INTERLINEA_DEL_NOME + SPAZIO_FRA_NOME_E_MESTIERE + fontSmall.getHeight();
	}

	/**
	 * La colonna centrale di una bottega, come elementi nelle coordinate dello schermo: l'intestazione (il nome del negoziante su una o più righe e sotto il
	 * mestiere in piccolo, o solo il mestiere), il personaggio (sul pavimento comune a tutti, dato dal ladro), la riga delle monete del gruppo e il separatore.
	 */
	public ContenutoDiColonna colonnaDelNegoziante(TipoPersonaggio personaggio) {
		return colonnaDelNegoziante(personaggio, Collections.emptyList());
	}

	/**
	 * Come sopra, con sotto le monete altre righe (etichetta a sinistra, valore a destra, una per elemento: {etichetta, valore}), come il costo e gli effetti della
	 * fusione dell'incantatore.
	 */
	public ContenutoDiColonna colonnaDelNegoziante(TipoPersonaggio personaggio, List<String[]> righeInPiu) {
		List<Elemento> elementi = new ArrayList<>();
		DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.LIGHT_GRAY;
		int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		// Il nome del negoziante (e sotto il mestiere)
		List<String> righe = getRigheDelNomeAlCentro();
		if (righe.isEmpty()) {
			String mestiere = getEtichettaMestiere();
			elementi.add(Elemento.testo(Collections.singletonList(mestiere), font, colore, (width - Math.max(1, FontTool.getWidth(font, mestiere))) / 2, y));
			y += fontHeight;
		} else {
			for (String riga : righe) {
				elementi.add(Elemento.testo(Collections.singletonList(riga), font, colore, (width - Math.max(1, FontTool.getWidth(font, riga))) / 2, y));
				y += fontHeight + INTERLINEA_DEL_NOME;
			}
			y += SPAZIO_FRA_NOME_E_MESTIERE - INTERLINEA_DEL_NOME;
			String mestiere = getEtichettaMestiere();
			elementi.add(Elemento.testo(Collections.singletonList(mestiere), fontSmall, colore, (width - Math.max(1, FontTool.getWidth(fontSmall, mestiere))) / 2, y));
			y += fontSmall.getHeight();
		}
		y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		// Per tenere i personaggi sullo stesso livello (se si passa da un personaggio all'altro) ed evitare sfarfallamenti, il ladro è il personaggio
		// "base" per calcolare l'altezza a cui disegnare
		Misura immagine = misure.personaggio(personaggio);
		y += altezzaLadro;
		elementi.add(Elemento.immagine(NomiDelleImmagini.personaggio(personaggio), immagine.larghezza, immagine.altezza, false, null,
				(width - immagine.larghezza) / 2, y - immagine.altezza));
		y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		String monete = String.valueOf(vistaPartita.getCassa().getMonete());
		elementi.add(Elemento.testo(Collections.singletonList(TestiDellaUI.get("MONETE")), font, colore, xMinimaZonaCentrale, y));
		elementi.add(Elemento.testo(Collections.singletonList(monete), font, colore, xMassimaZonaCentrale - Math.max(1, FontTool.getWidth(font, monete)), y));
		y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		for (String[] riga : righeInPiu) {
			coppia(elementi, riga[0], riga[1], y, colore);
			y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		}
		Misura separatore = misure.immagine(NomiDelleImmagini.SEPARATORE);
		elementi.add(Elemento.immagine(NomiDelleImmagini.SEPARATORE, separatore.larghezza, separatore.altezza, false, null, (width - separatore.larghezza) / 2, y));
		return new ContenutoDiColonna(width, y + separatore.altezza, elementi);
	}

	/**
	 * La testata della colonna centrale dell'inventario, come elementi nelle coordinate dello schermo: il nome del personaggio, la sua immagine (sul
	 * pavimento comune a tutti, dato dal ladro), i set leggendari completi, livello, esperienza, quanta ne manca, i punti abilità (arancioni se ce ne sono da
	 * spendere) e il separatore. L'altezza del contenuto è dove comincia l'elenco degli attributi.
	 */
	public ContenutoDiColonna testataDelPersonaggio(VistaPersonaggio personaggio) {
		List<Elemento> elementi = new ArrayList<>();
		DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;
		int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		String nome = personaggio.getNome(VistaPersonaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		elementi.add(Elemento.testo(Collections.singletonList(nome), font, coloreTestata, (width - Math.max(1, FontTool.getWidth(font, nome))) / 2, y));
		y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		Misura immagine = misure.personaggio(personaggio.getClasse());
		y += altezzaLadro;
		elementi.add(Elemento.immagine(NomiDelleImmagini.personaggio(personaggio.getClasse()), immagine.larghezza, immagine.altezza, false, null,
				(width - immagine.larghezza) / 2, y - immagine.altezza));
		y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		// I set leggendari completi, sotto l'immagine: quello che segue scende di quanto serve
		for (String setCompleto : personaggio.getDescrizioniSetCompleti()) {
			elementi.add(Elemento.testo(Collections.singletonList(setCompleto), font, DoomdarkColorModel.Color.YELLOW,
					(width - Math.max(1, FontTool.getWidth(font, setCompleto))) / 2, y));
			y += fontHeight + SPACING;
		}
		coppia(elementi, TipoAttributo.LIVELLO.getNome(), String.valueOf(personaggio.getLivello()), y, coloreTestata);
		y += fontHeight + SPACING;
		coppia(elementi, TipoAttributo.PUNTI_ESPERIENZA.getNome(), String.valueOf(personaggio.getEsperienza()), y, coloreTestata);
		y += fontHeight + SPACING;
		if (!personaggio.isAlLivelloMassimo()) {
			coppia(elementi, TestiDellaUI.get("PROSSIMO_LIVELLO"), String.valueOf(personaggio.getEsperienzaPerProssimoLivello()), y, coloreTestata);
		} else {
			coppia(elementi, TestiDellaUI.get("LIVELLO_MASSIMO"), "", y, coloreTestata);
		}
		y += fontHeight + SPACING;
		int puntiAbilita = personaggio.getPuntiAbilitaDisponibili();
		coppia(elementi, TipoAttributo.PUNTI_ABILITA.getNome(), String.valueOf(puntiAbilita), y,
				puntiAbilita > 0 ? DoomdarkColorModel.Color.ORANGE : coloreTestata);
		y += fontHeight;
		Misura separatore = misure.immagine(NomiDelleImmagini.SEPARATORE);
		elementi.add(Elemento.immagine(NomiDelleImmagini.SEPARATORE, separatore.larghezza, separatore.altezza, false, null, (width - separatore.larghezza) / 2, y));
		return new ContenutoDiColonna(width, y + separatore.altezza, elementi);
	}

	/** Un'etichetta a sinistra e il suo valore a destra, sulla stessa riga della zona centrale. */
	private void coppia(List<Elemento> elementi, String etichetta, String valore, int y, DoomdarkColorModel.Color colore) {
		elementi.add(Elemento.testo(Collections.singletonList(etichetta), font, colore, xMinimaZonaCentrale, y));
		if (!valore.isEmpty()) {
			elementi.add(Elemento.testo(Collections.singletonList(valore), font, colore, xMassimaZonaCentrale - FontTool.getWidth(font, valore), y));
		}
	}

	/**
	 * Restituisce le coordinate alle quali disegnare il fumetto
	 */
	public CoordinateFumetto getCoordinateFumetto() {
		// L'intestazione cambia con il nome del negoziante (più righe se è lungo), quindi le coordinate non si ricordano
		Misura ladro = misure.personaggio(TipoPersonaggio.LADRO);
		int yIntestazione = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + altezzaIntestazioneCentrale() + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

		return new CoordinateFumetto(
				width / 2 + ladro.larghezza + SPACING,
				yIntestazione + ladro.altezza / 2,
				width / 2 + misure.personaggio(TipoPersonaggio.ARMAIOLO).larghezza / 3,
				yIntestazione + ladro.altezza / 3
		);
	}

	/**
	 * Se il punto (x, y) della finestra cade dentro l'elenco che comincia in boxX (la parte sinistra o la destra)
	 */
	protected boolean dentroElenco(int boxX, int x, int y) {
		int xInterno = x - (boxX + SPACING);
		int yInterno = y - (DIMENSIONE_BORDO_INTERNO + 2 * SPACING);
		return xInterno >= 0 && xInterno < larghezzaDisponibileInRiquadroInventario()
				&& yInterno >= 0 && yInterno < altezzaDisponibileInRiquadroInventario();
	}

	@Override
	public boolean gestisceDoppioClick() {
		return true;
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

	@Override
	public int passiPerScattoDellaRotella() {
		return PASSI_PER_SCATTO_DELLE_LISTE;
	}

	/**
	 * Lo scorrimento della zona che contiene quella x, o null se non cade in nessuna
	 */
	private ScorrimentoSuUnAsse scorrimentoDellaZona(int x) {
		if (x >= xMinimaZonaSinistra && x < xMassimaZonaSinistra) {
			return scorrimentoSinistra;
		} else if (x >= xMinimaZonaCentrale && x < xMassimaZonaCentrale) {
			return scorrimentoCentrale;
		} else if (x >= xMinimaZonaDestra && x < xMassimaZonaDestra) {
			return scorrimentoDestra;
		}
		return null;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		ScorrimentoSuUnAsse scorrimento = scorrimentoDellaZona(x);
		if (scorrimento != null) {
			int pixel = numeroRotazioni * PASSO_SCORRIMENTO;
			scorrimento.scorriDi(movimentoRotella == MovimentoRotella.SU ? -pixel : pixel);
		}
	}

	/**
	 * Se, ora, premendo su una zona la si può trascinare. Le sottoclassi con qualcosa sopra le liste (un menu aperto)
	 * dicono di no.
	 */
	protected boolean puoScorrere() {
		return true;
	}

	/**
	 * Il gesto che comincia sulla schermata prosegue finché non si lascia il tasto, anche fuori da essa
	 */
	@Override
	public boolean catturaIlTrascinamento() {
		return true;
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO) {
			return;
		}
		scorrimentoPremuto = puoScorrere() ? scorrimentoDellaZona(x) : null;
		if (scorrimentoPremuto != null) {
			scorrimentoPremuto.premi(y, true);
		}
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		if (scorrimentoPremuto != null) {
			scorrimentoPremuto.trascina(y);
		}
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO && scorrimentoPremuto != null) {
			scorrimentoPremuto.rilascia();
		}
	}

	/**
	 * Se l'ultimo gesto è stato un trascinamento di una lista: il click che segue il rilascio non vale, perché non
	 * era un tocco
	 */
	protected boolean isGestoTrascinato() {
		return scorrimentoPremuto != null && scorrimentoPremuto.isGestoTrascinato();
	}
}
