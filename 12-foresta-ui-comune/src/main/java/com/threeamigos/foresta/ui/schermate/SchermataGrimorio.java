package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Il grimorio, senza disegno: si apre dall'inventario (comando MOSTRA_GRIMORIO) e si chiude con ANNULLA. A sinistra l'elenco di tutti gli incantesimi, a
 * destra la scheda di quello scelto con un clic ({@link ContenutiDelGrimorio}). Come nel bestiario, l'elenco e la scheda scorrono a parte
 * ({@link ColonnaScorrevole}), con la rotella sul pannello su cui si trova il mouse, trascinando o con le frecce; il titolo e il sottotitolo sono quelli
 * di {@link SchermataBestiario}. Chi disegna (Swing, libGDX) mette il fondo, il titolo, il sottotitolo e le due colonne.
 */
public abstract class SchermataGrimorio implements Finestra {

	/**
	 * Di quanti pixel scorre un pannello a ogni passo della rotella (vedi {@link Finestra#PASSI_PER_SCATTO_DELLE_LISTE}).
	 */
	private static final int PASSO_SCORRIMENTO = 4;
	/**
	 * Quanti passi vale un clic su una freccia.
	 */
	private static final int SCATTI_PER_CLIC = 10;
	/**
	 * Quanti sottotitoli scherzosi ci sono nei testi della UI (GRIMORIO_SOTTOTITOLO_1...): a ogni apertura se ne sceglie uno a caso.
	 */
	public static final int NUMERO_SOTTOTITOLI = 4;
	// Lascia libere le frecce a destra di un pannello
	private static final int SPAZIO_PER_LE_FRECCE = 24;

	protected final int width;
	protected final int height;
	private Area areaElenco;
	private Area areaScheda;
	private final List<TipoIncantesimo> voci = ContenutiDelGrimorio.voci();

	private int scelta;
	private List<String> sottotitolo = new ArrayList<>();
	private ColonnaScorrevole elenco;
	private ColonnaScorrevole scheda;
	private ContenutoDiColonna contenutoElenco;
	private ContenutoDiColonna contenutoScheda;
	// Quello dei due che si sta trascinando: l'ultimo su cui è stato premuto il tasto
	private ColonnaScorrevole premuto;

	protected SchermataGrimorio(int width, int height) {
		this.width = width;
		this.height = height;
		impostaLeAree(SchermataBestiario.cimaDelleColonne());
	}

	private void impostaLeAree(int cima) {
		int larghezzaElenco = (width - 2 * SchermataBestiario.MARGINE - SchermataBestiario.SPAZIO_FRA_I_PANNELLI) * 3 / 10;
		areaElenco = new Area(SchermataBestiario.MARGINE, cima, larghezzaElenco, height - cima - SchermataBestiario.MARGINE);
		int xScheda = SchermataBestiario.MARGINE + larghezzaElenco + SchermataBestiario.SPAZIO_FRA_I_PANNELLI;
		areaScheda = new Area(xScheda, cima, width - SchermataBestiario.MARGINE - xScheda, height - cima - SchermataBestiario.MARGINE);
	}

	/**
	 * Quanto è grande l'immagine (le frecce <code>icone/ComponenteScorrevole-FrecciaSu.png</code>…), come la disegna chi disegna
	 */
	protected abstract Misura misuraDellImmagine(String risorsa);

	/**
	 * I due contenuti sono stati rifatti: chi disegna prepara le sue immagini
	 */
	protected void contenutiAggiornati() {
	}

	/**
	 * Prepara il grimorio com'è adesso, con il primo incantesimo scelto, le due colonne dalla cima e un sottotitolo a caso.
	 */
	public void apri() {
		sottotitolo = SchermataBestiario.righeDelSottotitolo(TestiDellaUI.get("GRIMORIO_SOTTOTITOLO_" + Dado.tiraAncheAUnaFaccia(NUMERO_SOTTOTITOLI)),
				width - 2 * SchermataBestiario.MARGINE);
		impostaLeAree(SchermataBestiario.cimaDelleColonne(Math.max(1, sottotitolo.size())));
		scelta = 0;
		costruisciElenco(0);
		costruisciScheda();
	}

	private void costruisciElenco(int scorrimentoMantenuto) {
		contenutoElenco = ContenutiDelGrimorio.elenco(voci, scelta, areaElenco.getLarghezza() - SPAZIO_PER_LE_FRECCE, this::misuraDellImmagine);
		elenco = new ColonnaScorrevole(areaElenco, contenutoElenco.getAltezza());
		elenco.scorri(scorrimentoMantenuto);
		contenutiAggiornati();
	}

	private void costruisciScheda() {
		contenutoScheda = ContenutiDelGrimorio.scheda(voci.get(scelta), areaScheda.getLarghezza() - SPAZIO_PER_LE_FRECCE, this::misuraDellImmagine);
		scheda = new ColonnaScorrevole(areaScheda, contenutoScheda.getAltezza());
		contenutiAggiornati();
	}

	public String getTitolo() {
		return TestiDellaUI.get("TITOLO_GRIMORIO");
	}

	public int getXDelTitolo() {
		return (width - LayoutDelTestoGrande.larghezza(getTitolo())) >> 1;
	}

	/**
	 * Le righe del sottotitolo, centrate, che cominciano a quella quota e scendono di un'altezza del font medio
	 */
	public List<String> getSottotitolo() {
		return sottotitolo;
	}

	public int getQuotaDelSottotitolo() {
		return SchermataBestiario.QUOTA_TITOLO + LayoutDelTestoGrande.ALTEZZA_RIGA + SchermataBestiario.SPAZIO_SOPRA_IL_SOTTOTITOLO;
	}

	public Area getAreaElenco() {
		return areaElenco;
	}

	public Area getAreaScheda() {
		return areaScheda;
	}

	/**
	 * La colonna dell'elenco e quella della scheda (null finché il grimorio non è aperto), con i loro contenuti
	 */
	public ColonnaScorrevole getElenco() {
		return elenco;
	}

	public ColonnaScorrevole getScheda() {
		return scheda;
	}

	public ContenutoDiColonna getContenutoElenco() {
		return contenutoElenco;
	}

	public ContenutoDiColonna getContenutoScheda() {
		return contenutoScheda;
	}

	@Override
	public int passiPerScattoDellaRotella() {
		return PASSI_PER_SCATTO_DELLE_LISTE;
	}

	private boolean suPannelloElenco(int x) {
		return x < areaScheda.getX() - SchermataBestiario.SPAZIO_FRA_I_PANNELLI / 2;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		ColonnaScorrevole daScorrere = suPannelloElenco(x) ? elenco : scheda;
		if (daScorrere != null) {
			daScorrere.scorri((movimentoRotella == MovimentoRotella.SU ? -1 : 1) * numeroRotazioni * PASSO_SCORRIMENTO);
		}
	}

	/**
	 * Se una delle due colonne si muove da sola (l'inerzia dopo un trascinamento): va ridisegnato a ogni fotogramma
	 */
	public boolean inTransizione() {
		return (elenco != null && elenco.isInMovimento()) || (scheda != null && scheda.isInMovimento());
	}

	/**
	 * Il gesto che comincia sul grimorio prosegue finché non si lascia il tasto, anche fuori da esso
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
		premuto = suPannelloElenco(x) ? elenco : scheda;
		if (premuto != null) {
			premuto.premi(y);
		}
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		if (premuto != null) {
			premuto.trascina(y);
		}
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO && premuto != null) {
			premuto.rilascia();
		}
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		// Il rilascio di un trascinamento non è un click
		if (elenco == null || scheda == null || (premuto != null && premuto.isGestoTrascinato())) {
			return;
		}
		Misura su = misuraDellImmagine(NomiDelleImmagini.FRECCIA_SU);
		Misura giu = misuraDellImmagine(NomiDelleImmagini.FRECCIA_GIU);
		if (elenco.isSullaFrecciaSu(x, y, su)) {
			elenco.scorri(-SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (elenco.isSullaFrecciaGiu(x, y, giu)) {
			elenco.scorri(SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (scheda.isSullaFrecciaSu(x, y, su)) {
			scheda.scorri(-SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (scheda.isSullaFrecciaGiu(x, y, giu)) {
			scheda.scorri(SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (elenco.contiene(x, y)) {
			scegli((y - elenco.getCima() + elenco.getScorrimento()) / ContenutiDelGrimorio.ALTEZZA_RIGA_ELENCO);
		}
	}

	private void scegli(int riga) {
		if (riga < 0 || riga >= voci.size() || riga == scelta) {
			return;
		}
		scelta = riga;
		// L'elenco resta dov'è (cambia solo l'evidenziazione) e la scheda riparte dalla cima
		costruisciElenco(elenco.getScorrimento());
		costruisciScheda();
	}

	/**
	 * Sceglie l'incantesimo e porta l'elenco in modo che si veda, come un clic sulla sua riga; per i test.
	 */
	public void selezionaVoce(TipoIncantesimo incantesimo) {
		int riga = voci.indexOf(incantesimo);
		if (riga < 0) {
			return;
		}
		scegli(riga);
		int sopra = riga * ContenutiDelGrimorio.ALTEZZA_RIGA_ELENCO;
		int sotto = sopra + ContenutiDelGrimorio.ALTEZZA_RIGA_ELENCO;
		int scorrimento = elenco.getScorrimento();
		if (sopra < scorrimento) {
			elenco.scorri(sopra - scorrimento);
		} else if (sotto > scorrimento + areaElenco.getAltezza()) {
			elenco.scorri(sotto - scorrimento - areaElenco.getAltezza());
		}
	}

	/**
	 * L'incantesimo scelto, per i test
	 */
	public TipoIncantesimo getVoceScelta() {
		return voci.get(scelta);
	}
}
