package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.tipi.StileDellaBarra;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * La schermata delle opzioni, che si apre dall'intro, senza disegno: il titolo e una riga per opzione, con a sinistra un pulsante (l'icona come nella
 * barra, che chi disegna compone) e accanto la scritta nel font grande dei titoli, con sotto un sottotitolo in bianco nel font medio (come le descrizioni
 * dei trofei). Le righe: la lingua (la bandiera di quella in uso e il suo nome), il
 * tutorial da ripristinare, la barra dei comandi (l'icona della sua posizione, poi quella del suo stile), la classifica e i salvataggi da cancellare. Il pulsante manda il suo comando al motore, che mette le scelte nella barra (le
 * lingue, Sì e No): finché aspetta, il pulsante di quella riga è incorniciato.
 */
public class SchermataOpzioni implements Finestra {

	// Dal bordo, sopra il titolo e ai lati
	private static final int MARGINE = 16;
	// Fra il pulsante e la scritta, e fra una riga e l'altra
	private static final int SPAZIO = 16;
	// Fra una riga e l'altra, se lo schermo è basso, non meno di così
	private static final int SPAZIO_MINIMO = 4;
	// La cornice attorno al pulsante della riga che aspetta una scelta nella barra
	private static final int SPESSORE_DELLA_CORNICE = 3;
	private static final int COLORE_DELLA_CORNICE = 0xFFFFD700;
	private static final DoomdarkColorModel.Color COLORE_DEL_SOTTOTITOLO = DoomdarkColorModel.Color.WHITE;

	/**
	 * Una riga: il comando del pulsante, l'icona con cui si disegna, la scritta, e dove stanno il pulsante e la riga intera (che si clicca tutta)
	 */
	public static final class Riga {
		private final Comando comando;
		private final TipoIcona icona;
		private final String testo;
		private final String sottotitolo;
		private List<String> righeDelSottotitolo;
		private Area pulsante;
		private Area area;
		// Dove sta la scritta grande: il sottotitolo è sotto
		private int yDellaScritta;

		Riga(Comando comando, TipoIcona icona, String testo, String sottotitolo) {
			this.comando = comando;
			this.icona = icona;
			this.testo = testo;
			this.sottotitolo = sottotitolo;
		}

		public Comando getComando() {
			return comando;
		}

		public TipoIcona getIcona() {
			return icona;
		}

		public String getTesto() {
			return testo;
		}

		public String getSottotitolo() {
			return sottotitolo;
		}

		/** Il sottotitolo andato a capo nella larghezza delle scritte */
		public List<String> getRigheDelSottotitolo() {
			return righeDelSottotitolo;
		}

		/** Dove sta il pulsante, in coordinate dello schermo */
		public Area getPulsante() {
			return pulsante;
		}

		/** La riga intera, pulsante e scritta: un click ovunque vale come sul pulsante */
		public Area getArea() {
			return area;
		}
	}

	protected final int width;
	protected final int height;
	// Quanto è grande un pulsante: un'icona della barra, come la compone chi disegna
	private final Misura pulsante;
	// La riga che ha mandato il suo comando e aspetta la scelta nella barra (la lingua, la conferma di una cancellazione), o null
	private Comando inAttesa;
	// Dove sta adesso la barra dei comandi: l'icona del suo pulsante
	private PosizioneDellaBarra posizioneDellaBarra = PosizioneDellaBarra.BASSO;
	// Com'è adesso la barra: l'icona del pulsante del suo stile
	private StileDellaBarra stileDellaBarra = StileDellaBarra.DOCK;
	// Il contenuto si rifà solo quando cambia quel che mostra: chi disegna tiene le immagini delle scritte per elemento, e un contenuto nuovo a ogni
	// fotogramma le farebbe crescere senza fine
	private ContenutoDiColonna contenutoPronto;
	private String chiaveDelContenuto;

	/**
	 * @param pulsante quanto è grande un'icona della barra come la compone chi disegna
	 */
	public SchermataOpzioni(int width, int height, Misura pulsante) {
		this.width = width;
		this.height = height;
		this.pulsante = pulsante;
	}

	/**
	 * Il motore ha messo nella barra le scelte di questo momento: se c'è solo l'annulla, nessuna riga aspetta più
	 */
	public void aggiorna(Collection<Comando> comandiNellaBarra) {
		if (comandiNellaBarra.isEmpty() || comandiNellaBarra.size() == 1 && comandiNellaBarra.contains(Comando.ANNULLA)) {
			inAttesa = null;
		}
	}

	/**
	 * Dove sta adesso la barra: il pulsante della sua riga ne ha l'icona
	 */
	public void setPosizioneDellaBarra(PosizioneDellaBarra posizioneDellaBarra) {
		this.posizioneDellaBarra = posizioneDellaBarra;
	}

	/**
	 * Com'è adesso la barra, normale o Dock: il pulsante della sua riga ne ha l'icona
	 */
	public void setStileDellaBarra(StileDellaBarra stileDellaBarra) {
		this.stileDellaBarra = stileDellaBarra;
	}

	/**
	 * Le righe di adesso, già disposte: centrate in larghezza, e in altezza nello spazio sotto il titolo
	 */
	public List<Riga> getRighe() {
		List<Riga> righe = new ArrayList<>();
		Comando lingua = linguaInUso();
		righe.add(new Riga(Comando.OPZIONI_LINGUA, TipoIcona.ofComando(lingua), lingua.getDescrizione(), TestiDellaUI.get("OPZIONI_LINGUA_SOTTOTITOLO")));
		righe.add(new Riga(Comando.RIPRISTINA_TUTORIAL, TipoIcona.RIPRISTINA_TUTORIAL, TestiDellaUI.get("OPZIONI_RIPRISTINA_TUTORIAL"),
				TestiDellaUI.get("OPZIONI_RIPRISTINA_TUTORIAL_SOTTOTITOLO")));
		righe.add(new Riga(Comando.POSIZIONE_BARRA, iconaDellaBarra(), TestiDellaUI.get("OPZIONI_BARRA"), TestiDellaUI.get("OPZIONI_BARRA_SOTTOTITOLO")));
		righe.add(new Riga(Comando.STILE_BARRA, stileDellaBarra == StileDellaBarra.NORMALE ? TipoIcona.BARRA_NORMALE : TipoIcona.BARRA_DOCK,
				TestiDellaUI.get("OPZIONI_STILE_BARRA"), TestiDellaUI.get("OPZIONI_STILE_BARRA_SOTTOTITOLO")));
		righe.add(new Riga(Comando.CANCELLA_PUNTEGGI, TipoIcona.CANCELLA_PUNTEGGI, TestiDellaUI.get("OPZIONI_CANCELLA_PUNTEGGI"),
				TestiDellaUI.get("OPZIONI_CANCELLA_PUNTEGGI_SOTTOTITOLO")));
		righe.add(new Riga(Comando.CANCELLA_SALVATAGGI, TipoIcona.CANCELLA_SALVATAGGI, TestiDellaUI.get("OPZIONI_CANCELLA_SALVATAGGI"),
				TestiDellaUI.get("OPZIONI_CANCELLA_SALVATAGGI_SOTTOTITOLO")));
		disponi(righe);
		return righe;
	}

	private void disponi(List<Riga> righe) {
		DoomdarkFont font = DoomdarkFont.medio();
		int larghezzaDelleScritte = 0;
		for (Riga riga : righe) {
			larghezzaDelleScritte = Math.max(larghezzaDelleScritte, LayoutDelTestoGrande.larghezza(riga.testo));
			larghezzaDelleScritte = Math.max(larghezzaDelleScritte, FontTool.getWidth(font, riga.sottotitolo));
		}
		larghezzaDelleScritte = Math.min(larghezzaDelleScritte, larghezzaMassimaDelleScritte());
		// Ogni riga è alta quanto il pulsante, o quanto la scritta con il suo sottotitolo se è di più
		int[] altezze = new int[righe.size()];
		int altezzaDelleRighe = 0;
		for (int i = 0; i < righe.size(); i++) {
			Riga riga = righe.get(i);
			riga.righeDelSottotitolo = FontTool.split(font, riga.sottotitolo, Math.max(larghezzaDelleScritte, font.getHeight()));
			altezze[i] = Math.max(pulsante.altezza, altezzaDelTesto(riga));
			altezzaDelleRighe += altezze[i];
		}
		int x = Math.max(MARGINE, (width - pulsante.larghezza - SPAZIO - larghezzaDelleScritte) / 2);
		int sottoIlTitolo = MARGINE + LayoutDelTestoGrande.ALTEZZA_RIGA + SPAZIO;
		int spazio = righe.size() < 2 ? SPAZIO
				: Math.max(SPAZIO_MINIMO, Math.min(SPAZIO, (height - sottoIlTitolo - MARGINE - altezzaDelleRighe) / (righe.size() - 1)));
		altezzaDelleRighe += (righe.size() - 1) * spazio;
		int y = sottoIlTitolo + Math.max(0, (height - sottoIlTitolo - altezzaDelleRighe) / 2);
		for (int i = 0; i < righe.size(); i++) {
			Riga riga = righe.get(i);
			int altezzaDellaRiga = altezze[i];
			riga.pulsante = new Area(x, y + (altezzaDellaRiga - pulsante.altezza) / 2, pulsante.larghezza, pulsante.altezza);
			riga.area = new Area(x, y, pulsante.larghezza + SPAZIO + larghezzaDelleScritte, altezzaDellaRiga);
			riga.yDellaScritta = y + (altezzaDellaRiga - altezzaDelTesto(riga)) / 2;
			y += altezzaDellaRiga + spazio;
		}
	}

	// La scritta grande e sotto il sottotitolo, che sale sulla scritta di un quarto di lettera come la descrizione di un trofeo sul suo nome
	private static int altezzaDelTesto(Riga riga) {
		DoomdarkFont font = DoomdarkFont.medio();
		return LayoutDelTestoGrande.ALTEZZA_RIGA - sovrapposizione() + riga.righeDelSottotitolo.size() * font.getHeight();
	}

	private static int sovrapposizione() {
		return DoomdarkFont.medio().getHeight() / 4;
	}

	private int larghezzaMassimaDelleScritte() {
		return Math.max(1, width - 2 * MARGINE - pulsante.larghezza - SPAZIO);
	}

	/**
	 * Il titolo, le scritte delle righe nel font grande e la cornice del pulsante che aspetta: i pulsanti li disegna chi disegna, da {@link #getRighe()}
	 */
	public ContenutoDiColonna contenuto() {
		String chiave = Testi.getLingua() + "|" + inAttesa + "|" + posizioneDellaBarra + "|" + stileDellaBarra;
		if (!chiave.equals(chiaveDelContenuto)) {
			contenutoPronto = costruisciIlContenuto();
			chiaveDelContenuto = chiave;
		}
		return contenutoPronto;
	}

	private ContenutoDiColonna costruisciIlContenuto() {
		List<Elemento> elementi = new ArrayList<>();
		String titolo = TestiDellaUI.get("OPZIONI_TITOLO");
		ImmagineDiPixel pixelDelTitolo = LayoutDelTestoGrande.immagine(titolo, Math.max(1, width - 2 * MARGINE), true);
		elementi.add(Elemento.testoGrande(pixelDelTitolo, "opzioni|" + titolo + "|" + width, (width - pixelDelTitolo.getLarghezza()) / 2, MARGINE));
		for (Riga riga : getRighe()) {
			ImmagineDiPixel scritta = LayoutDelTestoGrande.immagine(riga.testo, larghezzaMassimaDelleScritte(), false);
			int xScritta = riga.pulsante.getX() + riga.pulsante.getLarghezza() + SPAZIO;
			int yScritta = riga.yDellaScritta + (LayoutDelTestoGrande.ALTEZZA_RIGA - scritta.getAltezza()) / 2;
			elementi.add(Elemento.testoGrande(scritta, "opzioni|" + riga.testo + "|" + width, xScritta, yScritta));
			elementi.add(Elemento.testo(riga.righeDelSottotitolo, DoomdarkFont.medio(), COLORE_DEL_SOTTOTITOLO, xScritta,
					riga.yDellaScritta + LayoutDelTestoGrande.ALTEZZA_RIGA - sovrapposizione()));
			if (riga.comando == inAttesa) {
				Area p = riga.pulsante;
				int s = SPESSORE_DELLA_CORNICE;
				elementi.add(Elemento.rettangolo(p.getLarghezza() + 2 * s, s, COLORE_DELLA_CORNICE, p.getX() - s, p.getY() - s));
				elementi.add(Elemento.rettangolo(p.getLarghezza() + 2 * s, s, COLORE_DELLA_CORNICE, p.getX() - s, p.getY() + p.getAltezza()));
				elementi.add(Elemento.rettangolo(s, p.getAltezza(), COLORE_DELLA_CORNICE, p.getX() - s, p.getY()));
				elementi.add(Elemento.rettangolo(s, p.getAltezza(), COLORE_DELLA_CORNICE, p.getX() + p.getLarghezza(), p.getY()));
			}
		}
		return new ContenutoDiColonna(width, height, elementi);
	}

	private TipoIcona iconaDellaBarra() {
		switch (posizioneDellaBarra) {
			case SINISTRA:
				return TipoIcona.BARRA_SINISTRA;
			case DESTRA:
				return TipoIcona.BARRA_DESTRA;
			default:
				return TipoIcona.BARRA_BASSO;
		}
	}

	/**
	 * Il comando della lingua in uso, quello della sua bandiera
	 */
	static Comando linguaInUso() {
		String lingua = Testi.getLingua();
		if ("en-GB".equalsIgnoreCase(lingua)) {
			return Comando.LINGUA_INGLESE_GB;
		}
		return lingua != null && lingua.startsWith("en") ? Comando.LINGUA_INGLESE_USA : Comando.LINGUA_ITALIANO;
	}

	/**
	 * Un click su una riga (sul pulsante o sulla scritta) manda il suo comando al motore
	 */
	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		// Mentre una riga aspetta la sua scelta nella barra il motore non accetta altri comandi
		if (tasto != Tasto.SINISTRO || inAttesa != null) {
			return;
		}
		for (Riga riga : getRighe()) {
			if (riga.area.contiene(x, y)) {
				inAttesa = riga.comando;
				BusEventi.pubblica(new ComandoDiGioco(riga.comando));
				return;
			}
		}
	}
}
