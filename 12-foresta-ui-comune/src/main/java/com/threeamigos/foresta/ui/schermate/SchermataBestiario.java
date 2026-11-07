package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Il bestiario, senza disegno: si apre dall'inventario (comando MOSTRA_BESTIARIO) e si chiude con ANNULLA. A sinistra l'elenco delle voci, ognuna con la sua
 * icona e il suo nome (o "???" finché non si è sbloccata), a destra la scheda della voce scelta con un clic ({@link ContenutiDelBestiario}). L'elenco e la
 * scheda scorrono a parte ({@link ColonnaScorrevole}), con la rotella sul pannello su cui si trova il mouse, trascinando o con le frecce. Chi disegna
 * (Swing, libGDX) mette il fondo, il titolo, il sottotitolo, quante voci si sono sbloccate e le due colonne, e dice quanto sono grandi le immagini
 * ({@link #getMisure()}).
 */
public abstract class SchermataBestiario implements Finestra {

	/**
	 * Di quanti pixel scorre un pannello a ogni passo della rotella (vedi {@link Finestra#PASSI_PER_SCATTO_DELLE_LISTE}).
	 */
	private static final int PASSO_SCORRIMENTO = 4;
	/**
	 * Quanti passi vale un clic su una freccia.
	 */
	private static final int SCATTI_PER_CLIC = 10;
	public static final int MARGINE = 30;
	public static final int QUOTA_TITOLO = 20;
	private static final int SPAZIO_SOTTO_IL_TITOLO = 20;
	/**
	 * Quanti sottotitoli scherzosi ci sono nei testi della UI (BESTIARIO_SOTTOTITOLO_1...): a ogni apertura se ne sceglie uno a caso.
	 */
	public static final int NUMERO_SOTTOTITOLI = 6;
	public static final int SPAZIO_SOPRA_IL_SOTTOTITOLO = 2;
	public static final int SPAZIO_FRA_I_PANNELLI = 30;
	// Lascia libere le frecce a destra di un pannello
	private static final int SPAZIO_PER_LE_FRECCE = 24;

	protected final int width;
	protected final int height;
	protected final VistaPartita vistaPartita;
	private Area areaElenco;
	private Area areaScheda;
	private List<TipoPersonaggio> voci = TipoPersonaggio.getVociDelBestiario();
	// Alla scelta della classe il bestiario mostra solo i personaggi giocabili, con un altro titolo (vedi apriPerLaSceltaDellaClasse)
	private String titoloDellaScelta;

	private int scelta;
	private List<String> sottotitolo = new ArrayList<>();
	private ColonnaScorrevole elenco;
	private ColonnaScorrevole scheda;
	private ContenutoDiColonna contenutoElenco;
	private ContenutoDiColonna contenutoScheda;
	// Quello dei due che si sta trascinando: l'ultimo su cui è stato premuto il tasto
	private ColonnaScorrevole premuto;

	protected SchermataBestiario(int width, int height, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.width = width;
		this.height = height;
		impostaLeAree(cimaDelleColonne());
	}

	private void impostaLeAree(int cima) {
		int larghezzaElenco = (width - 2 * MARGINE - SPAZIO_FRA_I_PANNELLI) * 3 / 10;
		areaElenco = new Area(MARGINE, cima, larghezzaElenco, height - cima - MARGINE);
		int xScheda = MARGINE + larghezzaElenco + SPAZIO_FRA_I_PANNELLI;
		areaScheda = new Area(xScheda, cima, width - MARGINE - xScheda, height - cima - MARGINE);
	}

	/**
	 * Quanto sono grandi le immagini dei personaggi e degli svolazzi, come le tiene chi disegna
	 */
	protected abstract MisureDelleImmagini getMisure();

	/**
	 * Quanto è grande l'immagine (le frecce <code>icone/ComponenteScorrevole-FrecciaSu.png</code>…), come la disegna chi disegna
	 */
	protected abstract Misura misuraDellImmagine(String risorsa);

	/**
	 * I due contenuti sono stati rifatti (l'elenco con un'altra voce evidenziata, la scheda di un'altra voce): chi disegna prepara le sue immagini
	 */
	protected void contenutiAggiornati() {
	}

	/**
	 * Dove cominciano le colonne se il sottotitolo sta su una riga sola: sotto il titolo e il sottotitolo.
	 */
	public static int cimaDelleColonne() {
		return cimaDelleColonne(1);
	}

	public static int cimaDelleColonne(int righeDelSottotitolo) {
		return QUOTA_TITOLO + LayoutDelTestoGrande.ALTEZZA_RIGA + SPAZIO_SOPRA_IL_SOTTOTITOLO + righeDelSottotitolo * DoomdarkFont.medio().getHeight()
				+ SPAZIO_SOTTO_IL_TITOLO;
	}

	/**
	 * Il sottotitolo a capo dove non sta nella larghezza, parola per parola (una parola da sola più larga di una riga resta intera).
	 */
	public static List<String> righeDelSottotitolo(String testo, int larghezzaMassima) {
		List<String> righe = new ArrayList<>();
		String corrente = "";
		for (String parola : testo.split(" ")) {
			String prova = corrente.isEmpty() ? parola : corrente + " " + parola;
			if (!corrente.isEmpty() && larghezza(prova) > larghezzaMassima) {
				righe.add(corrente);
				corrente = parola;
			} else {
				corrente = prova;
			}
		}
		if (!corrente.isEmpty()) {
			righe.add(corrente);
		}
		return righe;
	}

	private static int larghezza(String riga) {
		return Math.max(1, FontTool.getWidth(DoomdarkFont.medio(), riga));
	}

	/**
	 * Mette il sottotitolo e, se va a capo, abbassa le due colonne quanto serve.
	 */
	private void impostaIlSottotitolo(String testo) {
		sottotitolo = righeDelSottotitolo(testo, width - 2 * MARGINE);
		impostaLeAree(cimaDelleColonne(Math.max(1, sottotitolo.size())));
	}

	/**
	 * Prepara il bestiario com'è adesso, con la prima voce scelta, le due colonne dalla cima e un sottotitolo a caso.
	 */
	public void apri() {
		voci = TipoPersonaggio.getVociDelBestiario();
		titoloDellaScelta = null;
		impostaIlSottotitolo(TestiDellaUI.get("BESTIARIO_SOTTOTITOLO_" + Dado.tiraAncheAUnaFaccia(NUMERO_SOTTOTITOLI)));
		scelta = 0;
		costruisciElenco(0);
		costruisciScheda();
	}

	/**
	 * Prepara il bestiario per la scelta della classe: nell'elenco ci sono solo i personaggi giocabili (le cui schede sono sempre complete), il titolo è
	 * quello dato (per esempio «Scegli la classe di Arsenio») e il sottotitolo dice come leggerle. La scelta vera si fa con le icone della barra: passarci
	 * sopra ({@link #selezionaDaComando}) mostra la scheda di quella classe, e un clic su una riga dell'elenco fa lo stesso (per chi non ha il mouse).
	 */
	public void apriPerLaSceltaDellaClasse(String titolo) {
		voci = TipoPersonaggio.getVociDeiGiocabili();
		titoloDellaScelta = titolo;
		impostaIlSottotitolo(TestiDellaUI.get("SCELTA_DELLA_CLASSE_SOTTOTITOLO"));
		scelta = 0;
		costruisciElenco(0);
		costruisciScheda();
	}

	/**
	 * Se il bestiario è aperto per la scelta della classe
	 */
	public boolean isPerLaSceltaDellaClasse() {
		return titoloDellaScelta != null;
	}

	/**
	 * Mostra la scheda della classe che quel comando crea (l'icona della barra sotto il mouse nella scelta della classe).
	 *
	 * @return vero se il comando è quello di una classe che ha una voce nell'elenco
	 */
	public boolean selezionaDaComando(Comando comando) {
		if (comando == null || elenco == null) {
			return false;
		}
		TipoPersonaggio classe;
		try {
			classe = TipoPersonaggio.valueOf(comando.name());
		} catch (IllegalArgumentException e) {
			return false;
		}
		if (!voci.contains(classe.getRuolo())) {
			return false;
		}
		selezionaVoce(classe);
		return true;
	}

	private void costruisciElenco(int scorrimentoMantenuto) {
		contenutoElenco = ContenutiDelBestiario.elenco(vistaPartita, voci, scelta, areaElenco.getLarghezza() - SPAZIO_PER_LE_FRECCE, getMisure());
		elenco = new ColonnaScorrevole(areaElenco, contenutoElenco.getAltezza());
		elenco.scorri(scorrimentoMantenuto);
		contenutiAggiornati();
	}

	private void costruisciScheda() {
		contenutoScheda = ContenutiDelBestiario.scheda(vistaPartita, voci.get(scelta), areaScheda.getLarghezza() - SPAZIO_PER_LE_FRECCE, getMisure());
		scheda = new ColonnaScorrevole(areaScheda, contenutoScheda.getAltezza());
		contenutiAggiornati();
	}

	public String getTitolo() {
		return titoloDellaScelta != null ? titoloDellaScelta : TestiDellaUI.get("TITOLO_BESTIARIO");
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
		return QUOTA_TITOLO + LayoutDelTestoGrande.ALTEZZA_RIGA + SPAZIO_SOPRA_IL_SOTTOTITOLO;
	}

	/**
	 * Quante voci si sono sbloccate su quante ce ne sono da scoprire, come "3/25": sta in alto a destra. I personaggi giocabili, noti da sempre, non si
	 * contano; alla scelta della classe non si scrive niente.
	 */
	public String getQuanteSbloccate() {
		if (isPerLaSceltaDellaClasse()) {
			return "";
		}
		List<TipoPersonaggio> daScoprire = TipoPersonaggio.getVociDaScoprire();
		long sbloccate = daScoprire.stream().filter(vistaPartita::isVoceDelBestiarioSbloccata).count();
		return sbloccate + "/" + daScoprire.size();
	}

	public Area getAreaElenco() {
		return areaElenco;
	}

	public Area getAreaScheda() {
		return areaScheda;
	}

	/**
	 * La colonna dell'elenco e quella della scheda (null finché il bestiario non è aperto), con i loro contenuti
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
		return x < areaScheda.getX() - SPAZIO_FRA_I_PANNELLI / 2;
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
	 * Il gesto che comincia sul bestiario prosegue finché non si lascia il tasto, anche fuori da esso
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
			scegli(ContenutiDelBestiario.voceDellaRiga(voci,
					(y - elenco.getCima() + elenco.getScorrimento()) / ContenutiDelBestiario.ALTEZZA_RIGA_ELENCO));
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
	 * Sceglie la voce di quella classe e porta l'elenco in modo che si veda, come un clic sulla sua riga; per i test.
	 */
	public void selezionaVoce(TipoPersonaggio classe) {
		int riga = voci.indexOf(classe.getRuolo());
		if (riga < 0) {
			return;
		}
		scegli(riga);
		int sopra = ContenutiDelBestiario.rigaDellaVoce(voci, riga) * ContenutiDelBestiario.ALTEZZA_RIGA_ELENCO;
		int sotto = sopra + ContenutiDelBestiario.ALTEZZA_RIGA_ELENCO;
		int scorrimento = elenco.getScorrimento();
		if (sopra < scorrimento) {
			elenco.scorri(sopra - scorrimento);
		} else if (sotto > scorrimento + areaElenco.getAltezza()) {
			elenco.scorri(sotto - scorrimento - areaElenco.getAltezza());
		}
	}

	/**
	 * La voce scelta, per i test
	 */
	public TipoPersonaggio getVoceScelta() {
		return voci.get(scelta);
	}
}
