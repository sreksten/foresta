package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostaTattica;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpostaPersonaggio;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.tattiche.*;
import com.threeamigos.foresta.tattiche.modifica.BozzaTattica;
import com.threeamigos.foresta.tattiche.modifica.TestoTattica;
import com.threeamigos.foresta.tipi.*;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.tutorial.DisposizioneDelFumetto;
import com.threeamigos.foresta.ui.Finestra.MovimentoRotella;
import com.threeamigos.foresta.ui.Finestra.Tasto;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.ImmagineDelNodo;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * La schermata delle tattiche di combattimento, che si apre dall'inventario (comando MOSTRA_TATTICHE), senza disegno: a sinistra la tattica del personaggio
 * scelto come elenco di voci (ognuna con i suoi filtri, le sue condizioni e le sue azioni), al centro il personaggio e i pulsanti, a destra la spiegazione
 * della voce sotto il mouse. Si cambia una bozza ({@link BozzaTattica}); solo con il pulsante Sì la tattica passa al motore (<code>ComandoImpostaTattica</code>),
 * con No si torna a quella di prima. Le frecce sotto il personaggio lo spostano nel gruppo (<code>ComandoSpostaPersonaggio</code>). Il personaggio si sceglie
 * dalla barra delle icone, come nell'inventario.
 * <p>
 * Ogni riga dell'elenco è fatta di <i>pezzi</i> ({@link Token}): testo che non si tocca, testo che apre un menu a discesa ({@link MenuADiscesa}) o cambia un
 * valore, icone a destra (su, giù, togli). L'elenco è un albero {@link ElencoScorrevole} che si ricostruisce a ogni disegno e a ogni click; ogni riga è
 * descritta come un {@link ContenutoDiColonna} (testi e icone con il loro posto), che chi disegna (Swing, libGDX) mostra com'è. Anche la colonna centrale e la
 * spiegazione sono contenuti. Non è una finestra: lo tiene una schermata di scambio ({@link SchermataDiScambio}) a cui chiede le zone, il mouse e lo scorrimento.
 */
public final class EditorDelleTattiche {

	private static final int PROFONDITA_INDENTAZIONE = 12;
	private static final int SPAZIO_FRA_PEZZI = 5;
	private static final int MARGINE_RIGA = 2;
	/** Quanto è opaca un'icona che non si può usare */
	private static final float OPACITA_DELL_ICONA_SPENTA = 0.35f;

	/**
	 * Dove si apre un menu: sotto la riga che lo ha aperto, o sopra se sotto non c'è posto
	 */
	public static final class Ancora {
		final int x;
		final int sotto;
		final int sopra;
		/** La voce della riga che apre il menu, per la spiegazione */
		final int voce;

		Ancora(int x, int sotto, int sopra, int voce) {
			this.x = x;
			this.sotto = sotto;
			this.sopra = sopra;
			this.voce = voce;
		}
	}

	/**
	 * Un pezzo di una riga: un testo o un'icona, che se ha un'azione si può cliccare
	 */
	public static final class Token {
		final String testo;
		final String icona;
		final DoomdarkColorModel.Color colore;
		final Consumer<Ancora> azione;
		final String aiuto;
		final boolean adDestra;
		final boolean abilitato;
		/** Che cosa è questo pezzo e che cosa fa, per il riquadro destro (con il titolo); null se non c'è */
		String titolo;
		String spiegazione;
		int x;
		int linea;
		int larghezza;

		Token(String testo, String icona, DoomdarkColorModel.Color colore, Consumer<Ancora> azione, String aiuto, boolean adDestra,
			  boolean abilitato) {
			this.testo = testo;
			this.icona = icona;
			this.colore = colore;
			this.azione = azione;
			this.aiuto = aiuto;
			this.adDestra = adDestra;
			this.abilitato = abilitato;
		}

		boolean isCliccabile() {
			return azione != null && abilitato;
		}

		Token spiega(String titolo, String spiegazione) {
			this.titolo = titolo;
			this.spiegazione = spiegazione;
			return this;
		}
	}

	/**
	 * Una riga dell'elenco: i suoi pezzi, la voce a cui appartiene (-1 per le righe fuori dalle voci) e quanto è indentata
	 */
	public static final class Riga {
		final int profondita;
		final int voce;
		final List<Token> pezzi = new ArrayList<>();
		int linee = 1;
		/** La riga è di una voce disattivata: si disegna tutta in grigio scuro (tranne il numero, rosso) */
		boolean spenta;

		Riga(int profondita, int voce) {
			this.profondita = profondita;
			this.voce = voce;
		}
	}

	/**
	 * La bozza di un personaggio lasciato a metà: vale ancora se la sua tattica nel motore è quella da cui partiva
	 */
	private static final class BozzaLasciata {
		final BozzaTattica bozza;
		final TatticaMD applicata;

		BozzaLasciata(BozzaTattica bozza, TatticaMD applicata) {
			this.bozza = bozza;
			this.applicata = applicata;
		}
	}

	/**
	 * Un elenco di filtri che la bozza sa modificare: quello di una voce o quello di una condizione QUANTI
	 */
	private interface ListaFiltri {
		/** La rosa a cui si applicano i filtri: decide quali hanno senso */
		TipoRosa rosa();

		List<FiltroMD> get();

		void set(List<FiltroMD> nuova);
	}

	private final SchermataDiScambio schermata;
	// Sul telefono, nella riga di una voce, il numero, su, giù e il cestino stanno a distanze uguali su tutta la larghezza: col dito si prendono meglio
	private boolean iconeDistanziate;
	private final VistaPartita vistaPartita;
	private final MisureDelleImmagini misure;
	private final DoomdarkFont fontRiga = DoomdarkFont.medio();
	private final int altezzaRiga;

	private VistaPersonaggio personaggio;
	private TatticaMD applicata = TatticaMD.VUOTA;
	private TatticaMD predefinita = TatticaMD.VUOTA;
	// Le azioni che la classe del personaggio può avere nella tattica; null: tutte
	private java.util.Set<TipoAzione> azioniPossibili;
	private BozzaTattica bozza = BozzaTattica.da(TatticaMD.VUOTA);
	// Le bozze modificate e non confermate di chi si è lasciato per un altro personaggio (per UUID), con la tattica da cui partivano
	private final Map<String, BozzaLasciata> bozzeLasciate = new HashMap<>();
	private MenuADiscesa<?> menu;
	// Il pezzo che ha aperto il menu: la sua spiegazione resta nel riquadro destro finché il mouse non è su una scelta
	private String titoloMenu;
	private String spiegazioneMenu;
	private int voceDelMenu = -1;

	// Le aree dei pulsanti della colonna centrale, aggiornate a ogni disegno
	private final Area[] areaSposta = new Area[2];
	private Area areaSi;
	private Area areaNo;
	private Area areaPredefinita;
	private boolean puoSpostareASinistra;
	private boolean puoSpostareADestra;

	// Per rinominare una voce: chi chiede un testo al giocatore (testo predefinito, e chi riceve quello confermato)
	private BiConsumer<String, Consumer<String>> richiestaTesto;
	private Consumer<Consumer<String>> annullaRichiestaTesto;
	// Chi aspetta il nome di una voce, finché il giocatore non risponde o la richiesta decade
	private Consumer<String> richiestaInCorso;
	// Il nome di una voce si clicca per ripiegarla; due click ravvicinati sullo stesso nome la rinominano
	private static final long INTERVALLO_DOPPIO_CLICK_MS = 400;
	private BozzaTattica.Voce ultimaVoceCliccata;
	private long ultimoClickSulNome;

	public EditorDelleTattiche(SchermataDiScambio schermata, VistaPartita vistaPartita, MisureDelleImmagini misure) {
		this.schermata = schermata;
		this.vistaPartita = vistaPartita;
		this.misure = misure;
		altezzaRiga = Math.max(fontRiga.getHeight(), 12) + 2 * MARGINE_RIGA;
	}

	/**
	 * @param richiestaTesto chi chiede un testo al giocatore: il testo predefinito e chi riceve quello confermato
	 * @param annullaRichiesta chi chiude la richiesta di prima se non serve più, riconoscendola da chi doveva riceverla
	 */
	public void setRichiestaTesto(BiConsumer<String, Consumer<String>> richiestaTesto,
								  Consumer<Consumer<String>> annullaRichiesta) {
		this.richiestaTesto = richiestaTesto;
		this.annullaRichiestaTesto = annullaRichiesta;
	}

	/**
	 * Se si sta ancora aspettando il nome di una voce, lo si smette di aspettare: la bozza è cambiata, o si è lasciata la schermata, e
	 * l'Invio non deve scrivere su una voce che non c'è più.
	 */
	public void annullaLaRichiestaDiTesto() {
		if (richiestaInCorso != null && annullaRichiestaTesto != null) {
			annullaRichiestaTesto.accept(richiestaInCorso);
		}
		richiestaInCorso = null;
	}

	private static String nomeDellaVoce(BozzaTattica.Voce voce, int indice) {
		return voce.nome != null ? voce.nome : BozzaTattica.nomePredefinito(indice);
	}

	/**
	 * Un click sul nome ripiega o riapre la voce; il secondo click ravvicinato annulla il primo e chiede il nuovo nome.
	 */
	public void clickSulNome(BozzaTattica.Voce voce, int indice) {
		long ora = System.currentTimeMillis();
		boolean doppio = voce == ultimaVoceCliccata && ora - ultimoClickSulNome <= INTERVALLO_DOPPIO_CLICK_MS;
		voce.chiusa = !voce.chiusa;
		if (!doppio) {
			ultimaVoceCliccata = voce;
			ultimoClickSulNome = ora;
			return;
		}
		ultimaVoceCliccata = null;
		if (richiestaTesto != null) {
			annullaLaRichiestaDiTesto();
			BozzaTattica bozzaDellaRichiesta = bozza;
			Consumer<String> risposta = testo -> {
				richiestaInCorso = null;
				// La bozza di allora potrebbe non essere più quella (si è cambiato personaggio o si è tornati alla tattica applicata)
				if (bozza != bozzaDellaRichiesta) {
					return;
				}
				String nuovo = testo.trim();
				voce.nome = nuovo.isEmpty() ? BozzaTattica.nomePredefinito(indice) : nuovo;
			};
			richiestaInCorso = risposta;
			richiestaTesto.accept(nomeDellaVoce(voce, indice), risposta);
		}
	}

	/**
	 * Mostra la tattica del personaggio. Se è lo stesso di prima e la tattica nel motore non è cambiata la bozza resta com'è (arriva di nuovo
	 * la richiesta quando si sposta il personaggio nel gruppo); altrimenti si riparte da quella del motore, a meno che il personaggio sia stato
	 * lasciato con una bozza a metà e la sua tattica nel motore non sia cambiata nel frattempo.
	 */
	public void impostaTattica(VistaPersonaggio nuovo, TatticaMD tattica, TatticaMD tatticaPredefinita, java.util.Set<TipoAzione> azioniPossibili) {
		boolean stesso = personaggio != null && personaggio.getUuid().equals(nuovo.getUuid());
		if (!(stesso && tattica.equals(applicata))) {
			annullaLaRichiestaDiTesto();
			if (personaggio != null && !stesso && isModificata()) {
				bozzeLasciate.put(personaggio.getUuid(), new BozzaLasciata(bozza, applicata));
			}
			BozzaLasciata lasciata = bozzeLasciate.remove(nuovo.getUuid());
			bozza = lasciata != null && lasciata.applicata.equals(tattica) ? lasciata.bozza : BozzaTattica.da(tattica);
			schermata.scorrimentoSinistra.reimposta();
		}
		personaggio = nuovo;
		applicata = tattica;
		predefinita = tatticaPredefinita;
		this.azioniPossibili = azioniPossibili;
		menu = null;
	}

	/**
	 * Se la bozza è diversa dalla tattica applicata nel motore (a parte l'attacco di riserva): i pulsanti Sì e No hanno senso solo allora
	 */
	private boolean isModificata() {
		// Una voce appena aggiunta, ancora senza azioni, non entra nella tattica da mandare al motore, ma la bozza è comunque cambiata
		return !bozza.descrive(applicata) || bozza.haVociSenzaAzioni();
	}

	public VistaPersonaggio getPersonaggio() {
		return personaggio;
	}

	// ============================================================== quel che si vede

	/**
	 * Quel che la schermata mostra adesso, a parte la colonna centrale ({@link #colonnaCentrale()}): l'elenco delle voci, la spiegazione a destra, il menu
	 * aperto (che si disegna per ultimo) e il testo di aiuto del pezzo sotto il mouse (per il cartiglio).
	 */
	public static final class Quadro {
		/** L'elenco delle righe, ognuna con il suo contenuto come immagine del nodo (un {@link ContenutoDiColonna}): lo scorrimento è quello della zona sinistra */
		public final ElencoScorrevole<Riga> elenco;
		/** I testi della spiegazione, nelle coordinate dello schermo */
		public final ContenutoDiColonna spiegazione;
		/** Il menu aperto, o null */
		public final MenuADiscesa<?> menu;
		/** Il testo del cartiglio dell'aiuto, o null */
		public final String aiuto;

		private Quadro(ElencoScorrevole<Riga> elenco, ContenutoDiColonna spiegazione, MenuADiscesa<?> menu, String aiuto) {
			this.elenco = elenco;
			this.spiegazione = spiegazione;
			this.menu = menu;
			this.aiuto = aiuto;
		}
	}

	/**
	 * Costruisce quel che si vede adesso (da chiamare a ogni disegno, dopo {@link #colonnaCentrale()}; non senza personaggio): aggiorna lo scorrimento
	 * dell'elenco, trova il pezzo sotto il mouse, e chiude il menu se si è chiuso da solo.
	 */
	public Quadro quadro() {
		schermata.scorrimentoSinistra.aggiorna();
		List<Riga> righe = costruisciRighe();
		Hover hover = menu != null ? Hover.NESSUNO : trovaHover(righe, schermata.mouseX, schermata.mouseY);
		ElencoScorrevole<Riga> elenco = costruisciElenco(righe, hover);
		int voceSpiegata = hover.riga != null ? hover.riga.voce : -1;
		String titoloElemento = null;
		String testoElemento = null;
		if (menu != null && menu.isAperto()) {
			// Con un menu aperto: la scelta sotto il mouse, altrimenti il pezzo che lo ha aperto
			voceSpiegata = voceDelMenu;
			MenuADiscesa.Scelta<?> sotto = menu.sceltaSotto(schermata.mouseX, schermata.mouseY);
			if (sotto != null && sotto.spiegazione != null) {
				titoloElemento = sotto.testo;
				testoElemento = sotto.spiegazione;
			} else {
				titoloElemento = titoloMenu;
				testoElemento = spiegazioneMenu;
			}
		} else if (hover.token != null && hover.token.spiegazione != null) {
			titoloElemento = hover.token.titolo;
			testoElemento = hover.token.spiegazione;
		}
		ContenutoDiColonna spiegazione = spiegazione(voceSpiegata, titoloElemento, testoElemento);
		MenuADiscesa<?> menuAperto = null;
		String aiuto = null;
		if (menu != null) {
			if (menu.isAperto()) {
				menuAperto = menu;
			} else {
				menu = null;
			}
		} else if (hover.token != null && hover.token.aiuto != null) {
			aiuto = hover.token.aiuto;
		}
		return new Quadro(elenco, spiegazione, menuAperto, aiuto);
	}

	/**
	 * La colonna centrale, nelle coordinate dello schermo: il nome e l'immagine del personaggio, se è il capo o è caduto, il suo posto nel gruppo con le frecce
	 * che lo spostano, Sì e No (solo se la bozza è cambiata) e "predefinita". Aggiorna le aree dei pulsanti.
	 */
	public ContenutoDiColonna colonnaCentrale() {
		List<Elemento> elementi = new ArrayList<>();
		int width = schermata.width;
		DoomdarkFont font = schermata.font;
		DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;
		int y = SchermataDiScambio.SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		String nome = personaggio.getNome(VistaPersonaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		elementi.add(Elemento.testo(Collections.singletonList(nome), font, coloreTestata, (width - larghezzaDelTesto(font, nome)) / 2, y));
		y += schermata.fontHeight + SchermataDiScambio.SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

		Misura immagine = misure.personaggio(personaggio.getClasse());
		y += schermata.altezzaLadro;
		elementi.add(Elemento.immagine(NomiDelleImmagini.personaggio(personaggio.getClasse()), immagine.larghezza, immagine.altezza, false, null,
				(width - immagine.larghezza) / 2, y - immagine.altezza));
		y += SchermataDiScambio.SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

		boolean capo = vistaPartita.getGruppoGiocatore().getCapo() != null
				&& vistaPartita.getGruppoGiocatore().getCapo().getUuid().equals(personaggio.getUuid());
		if (capo) {
			y = centrato(elementi, TestiDellaUI.get("TATTICHE_CAPO"), DoomdarkColorModel.Color.RED, y);
		}
		if (!personaggio.isVivo()) {
			y = centrato(elementi, TestiDellaUI.get("TATTICHE_CADUTO"), DoomdarkColorModel.Color.MEDIUM_GRAY, y);
		}
		y += SchermataDiScambio.SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI / 2;

		// Il posto nel gruppo: le frecce lo spostano a sinistra o a destra
		List<? extends VistaPersonaggio> permanenti = permanenti();
		int posto = -1;
		for (int i = 0; i < permanenti.size(); i++) {
			if (permanenti.get(i).getUuid().equals(personaggio.getUuid())) {
				posto = i;
			}
		}
		int lato = misure.immagine(NomiDelleImmagini.ICONA_SINISTRA).larghezza;
		int centro = width / 2;
		if (posto < 0) {
			// Chi è nel gruppo a tempo non ha un posto da spostare
			puoSpostareASinistra = false;
			puoSpostareADestra = false;
			areaSposta[0] = null;
			areaSposta[1] = null;
			String aTempo = TestiDellaUI.get("TATTICHE_GRUPPO_A_TEMPO");
			elementi.add(Elemento.testo(Collections.singletonList(aTempo), font, DoomdarkColorModel.Color.MEDIUM_GRAY,
					centro - larghezzaDelTesto(font, aTempo) / 2, y + (lato - font.getHeight()) / 2));
		} else {
			puoSpostareASinistra = posto > 0;
			puoSpostareADestra = posto < permanenti.size() - 1;
			String postoTesto = TestiDellaUI.get("TATTICHE_POSTO", posto + 1, permanenti.size());
			int mezzoTesto = larghezzaDelTesto(font, postoTesto) / 2;
			areaSposta[0] = new Area(centro - mezzoTesto - 2 * Finestra.SPACING - lato, y, lato, lato);
			areaSposta[1] = new Area(centro + mezzoTesto + 2 * Finestra.SPACING, y, lato, lato);
			elementi.add(icona(NomiDelleImmagini.ICONA_SINISTRA, areaSposta[0], puoSpostareASinistra));
			elementi.add(icona(NomiDelleImmagini.ICONA_DESTRA, areaSposta[1], puoSpostareADestra));
			elementi.add(Elemento.testo(Collections.singletonList(postoTesto), font, DoomdarkColorModel.Color.MEDIUM_GRAY, centro - mezzoTesto,
					y + (lato - font.getHeight()) / 2));
		}
		y += lato + SchermataDiScambio.SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

		// Conferma e annulla, solo se c'è qualcosa da confermare
		boolean modificata = isModificata();
		boolean aPosto = bozza.problemi().isEmpty();
		if (modificata) {
			int lSi = misure.immagine(NomiDelleImmagini.ICONA_SI).larghezza;
			areaSi = new Area(centro - lSi - Finestra.SPACING * 3, y, lSi, lSi);
			areaNo = new Area(centro + Finestra.SPACING * 3, y, lSi, lSi);
			elementi.add(icona(NomiDelleImmagini.ICONA_SI, areaSi, aPosto));
			elementi.add(icona(NomiDelleImmagini.ICONA_NO, areaNo, true));
			y += lSi + Finestra.SPACING;
			y = centrato(elementi, aPosto ? TestiDellaUI.get("TATTICHE_CONFERMA") : TestiDellaUI.get("TATTICHE_INCOMPLETA"),
					aPosto ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED, y);
		} else {
			areaSi = null;
			areaNo = null;
		}
		y += SchermataDiScambio.SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI / 2;
		String predefinitaTesto = TestiDellaUI.get("TATTICHE_PREDEFINITA");
		int larghezzaPredefinita = larghezzaDelTesto(font, predefinitaTesto);
		areaPredefinita = new Area((width - larghezzaPredefinita) / 2, y, larghezzaPredefinita, font.getHeight());
		elementi.add(Elemento.testo(Collections.singletonList(predefinitaTesto), font,
				areaPredefinita.contiene(schermata.mouseX, schermata.mouseY) && menu == null ? DoomdarkColorModel.Color.WHITE : DoomdarkColorModel.Color.LIGHT_GRAY,
				areaPredefinita.getX(), areaPredefinita.getY()));
		return new ContenutoDiColonna(width, y + font.getHeight(), elementi);
	}

	private static Elemento icona(String risorsa, Area area, boolean abilitata) {
		return Elemento.immagineConOpacita(risorsa, area.getLarghezza(), area.getAltezza(), abilitata ? 1f : OPACITA_DELL_ICONA_SPENTA, area.getX(), area.getY());
	}

	private int centrato(List<Elemento> elementi, String testo, DoomdarkColorModel.Color colore, int y) {
		DoomdarkFont font = schermata.font;
		int larghezzaMassima = schermata.xMassimaZonaCentrale - schermata.xMinimaZonaCentrale;
		for (String riga : FontTool.split(font, testo, larghezzaMassima)) {
			elementi.add(Elemento.testo(Collections.singletonList(riga), font, colore, (schermata.width - larghezzaDelTesto(font, riga)) / 2, y));
			y += schermata.fontHeight + Finestra.SPACING;
		}
		return y;
	}

	private static int larghezzaDelTesto(DoomdarkFont font, String testo) {
		return Math.max(1, FontTool.getWidth(font, testo));
	}

	private int larghezzaDelTesto(String testo) {
		return larghezzaDelTesto(fontRiga, testo);
	}

	private List<? extends VistaPersonaggio> permanenti() {
		List<VistaPersonaggio> permanenti = new ArrayList<>();
		for (VistaPersonaggio p : vistaPartita.getGruppoGiocatore().getPersonaggi()) {
			if (!p.isATempo()) {
				permanenti.add(p);
			}
		}
		return permanenti;
	}

	/**
	 * I testi del riquadro destro: l'elemento sotto il mouse (la rosa, il filtro, l'azione...), poi la voce a cui appartiene con la sua spiegazione, i problemi da
	 * sistemare. Ogni riga è un elemento, a capo entro il riquadro; si smette quando non c'è più posto.
	 */
	private ContenutoDiColonna spiegazione(int indiceVoce, String titoloElemento, String testoElemento) {
		List<String> testi = new ArrayList<>();
		List<DoomdarkColorModel.Color> colori = new ArrayList<>();
		List<String> problemi = bozza.problemi();
		if (applicata.isDisabilitata()) {
			testi.add(TestiDellaUI.get("TATTICHE_DISATTIVATA"));
			colori.add(DoomdarkColorModel.Color.RED);
			testi.add(TestiDellaUI.get("TATTICHE_ERRORI"));
			colori.add(DoomdarkColorModel.Color.LIGHT_GRAY);
		}
		// Prima l'elemento sotto il mouse (la rosa, il filtro, l'azione...), poi la voce a cui appartiene
		if (testoElemento != null) {
			testi.add(titoloElemento);
			colori.add(DoomdarkColorModel.Color.WHITE);
			testi.add(testoElemento);
			colori.add(DoomdarkColorModel.Color.LIGHT_GRAY);
		}
		if (indiceVoce >= 0 && indiceVoce < bozza.getVoci().size()) {
			BozzaTattica.Voce voce = bozza.getVoci().get(indiceVoce);
			testi.add(TestiDellaUI.get("TATTICHE_VOCE_N", indiceVoce + 1));
			colori.add(DoomdarkColorModel.Color.YELLOW);
			if (voce.azioni.isEmpty()) {
				testi.add(TestiDellaUI.get("TATTICHE_SENZA_AZIONI"));
				colori.add(DoomdarkColorModel.Color.RED);
			} else {
				for (String riga : TestoTattica.spiegazione(bozza.toVoceMD(voce))) {
					testi.add(riga);
					colori.add(DoomdarkColorModel.Color.LIGHT_GRAY);
				}
			}
		} else {
			testi.add(TestiDellaUI.get("TATTICHE_REGOLA_1"));
			colori.add(DoomdarkColorModel.Color.LIGHT_GRAY);
			testi.add(TestiDellaUI.get("TATTICHE_REGOLA_2"));
			colori.add(DoomdarkColorModel.Color.LIGHT_GRAY);
			testi.add(TestiDellaUI.get("TATTICHE_REGOLA_3"));
			colori.add(DoomdarkColorModel.Color.MEDIUM_GRAY);
		}
		if (!problemi.isEmpty()) {
			testi.add(TestiDellaUI.get("TATTICHE_DA_SISTEMARE"));
			colori.add(DoomdarkColorModel.Color.RED);
			for (String problema : problemi) {
				testi.add("- " + problema);
				colori.add(DoomdarkColorModel.Color.RED);
			}
		}
		List<Elemento> elementi = new ArrayList<>();
		int y = SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + 2 * Finestra.SPACING;
		int fine = y + schermata.altezzaDisponibileInRiquadroInventario();
		int x = schermata.xMinimaZonaDestra + Finestra.SPACING;
		for (int i = 0; i < testi.size(); i++) {
			for (String riga : FontTool.split(fontRiga, testi.get(i), schermata.larghezzaDisponibileInRiquadroInventario())) {
				if (y + fontRiga.getHeight() > fine) {
					return new ContenutoDiColonna(schermata.width, y, elementi);
				}
				elementi.add(Elemento.testo(Collections.singletonList(riga), fontRiga, colori.get(i), x, y));
				y += fontRiga.getHeight() + Finestra.SPACING;
			}
			y += Finestra.SPACING;
		}
		return new ContenutoDiColonna(schermata.width, y, elementi);
	}

	// ============================================================== le righe

	public static final class Hover {
		public static final Hover NESSUNO = new Hover(null, null);
		public final Riga riga;
		public final Token token;

		Hover(Riga riga, Token token) {
			this.riga = riga;
			this.token = token;
		}
	}

	private int xInternoElenco(int x) {
		return x - (schermata.xMinimaZonaSinistra + Finestra.SPACING);
	}

	private int yInternoElenco(int y) {
		return y - (SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + 2 * Finestra.SPACING);
	}

	/**
	 * La riga e il pezzo sotto il punto (nelle coordinate della finestra), se ce ne sono
	 */
	private Hover trovaHover(List<Riga> righe, int x, int y) {
		int xi = xInternoElenco(x);
		int yi = yInternoElenco(y);
		if (x < 0 || xi < 0 || xi >= schermata.larghezzaDisponibileInRiquadroInventario() || yi < 0 || yi >= schermata.altezzaDisponibileInRiquadroInventario()) {
			return Hover.NESSUNO;
		}
		ElencoScorrevole<Riga> elenco = costruisciElenco(righe, Hover.NESSUNO);
		Riga riga = elenco.riferimentoTitoloAllaQuota(yi + schermata.offsetYZonaSinistra());
		if (riga == null) {
			return Hover.NESSUNO;
		}
		Punto posizione = elenco.posizioneTitolo(r -> r == riga);
		int linea = posizione == null ? 0 : Math.min(riga.linee - 1, (yi + schermata.offsetYZonaSinistra() - posizione.y) / altezzaRiga);
		for (Token token : riga.pezzi) {
			if (token.linea == linea && xi >= token.x && xi < token.x + token.larghezza) {
				return new Hover(riga, token);
			}
		}
		return new Hover(riga, null);
	}

	/**
	 * L'albero delle righe: il contenuto di ognuna è l'immagine del nodo
	 */
	public ElencoScorrevole<Riga> costruisciElenco(List<Riga> righe, Hover hover) {
		int larghezza = schermata.larghezzaDisponibileInRiquadroInventario();
		ElencoScorrevole<Riga> elenco = new ElencoScorrevole<>(larghezza, 0, 1);
		for (Riga riga : righe) {
			elenco.creaNodo(null, null, null, null, new ImmagineDelNodo(contenutoDella(riga, hover), new Misura(larghezza, altezzaRiga * riga.linee)), riga);
		}
		return elenco;
	}

	/**
	 * Sul telefono (le dita sono grosse e lo schermo piccolo): nella riga di una voce il numero, su, giù e il cestino stanno a distanze uguali, dal bordo
	 * sinistro al destro; il nome, se non ci sta fra il numero e su, va sotto
	 */
	public void setIconeDistanziate(boolean iconeDistanziate) {
		this.iconeDistanziate = iconeDistanziate;
	}

	/**
	 * Il posto dei pezzi: le icone a destra sulla prima linea, gli altri di seguito da sinistra (dopo l'indentazione) e a capo, con un po' di
	 * rientro, quando non stanno più nella linea
	 */
	private void disponi(Riga riga) {
		int larghezzaRiga = schermata.larghezzaDisponibileInRiquadroInventario();
		spezzaIPezziTroppoLarghi(riga, larghezzaRiga - riga.profondita * PROFONDITA_INDENTAZIONE - PROFONDITA_INDENTAZIONE);
		int xDestra = larghezzaRiga;
		for (Token token : riga.pezzi) {
			token.larghezza = token.icona != null ? altezzaRiga : larghezzaDelTesto(token.testo);
			if (token.adDestra) {
				xDestra -= token.larghezza + SPAZIO_FRA_PEZZI;
				token.x = xDestra;
				token.linea = 0;
			}
		}
		if (iconeDistanziate && riga.profondita == 0) {
			xDestra = distanzia(riga, larghezzaRiga - SPAZIO_FRA_PEZZI);
		}
		int xInizio = riga.profondita * PROFONDITA_INDENTAZIONE;
		int xSinistra = xInizio;
		int linea = 0;
		for (Token token : riga.pezzi) {
			if (token.adDestra) {
				continue;
			}
			int limite = linea == 0 ? xDestra : larghezzaRiga;
			if (xSinistra > xInizio && xSinistra + token.larghezza > limite) {
				linea++;
				xSinistra = xInizio + PROFONDITA_INDENTAZIONE;
			}
			token.x = xSinistra;
			token.linea = linea;
			xSinistra += token.larghezza + SPAZIO_FRA_PEZZI;
		}
		riga.linee = linea + 1;
	}

	/**
	 * Nella riga di una voce, il numero (il primo pezzo, a sinistra) e le icone a destra a distanze uguali fino al bordo destro: le icone sono nella riga
	 * da destra a sinistra (togli, giù, su). Dice dove comincia la prima icona, il limite della prima linea per i pezzi di sinistra.
	 */
	private int distanzia(Riga riga, int bordoDestro) {
		List<Token> icone = new ArrayList<>();
		int larghezzaIcone = 0;
		for (Token token : riga.pezzi) {
			if (token.adDestra) {
				icone.add(token);
				larghezzaIcone += token.larghezza;
			}
		}
		Token numero = riga.pezzi.get(0);
		if (icone.isEmpty() || numero.adDestra) {
			return bordoDestro;
		}
		int spazio = Math.max(SPAZIO_FRA_PEZZI, (bordoDestro - numero.larghezza - larghezzaIcone) / icone.size());
		int x = numero.larghezza;
		for (int i = icone.size() - 1; i >= 0; i--) {
			x += spazio;
			icone.get(i).x = x;
			x += icone.get(i).larghezza;
		}
		return icone.get(icone.size() - 1).x;
	}

	/**
	 * Un pezzo di testo più largo di una riga intera (il nome di un'azione lungo, "Pozione della Salute grande") si divide in parole, che vanno a capo: ogni
	 * frammento fa quello che faceva il pezzo (si clicca ovunque si apre lo stesso menu).
	 */
	private void spezzaIPezziTroppoLarghi(Riga riga, int larghezzaMassima) {
		List<Token> nuovi = new ArrayList<>();
		for (Token token : riga.pezzi) {
			if (token.icona != null || token.adDestra || larghezzaDelTesto(token.testo) <= larghezzaMassima
					|| !token.testo.contains(" ")) {
				nuovi.add(token);
				continue;
			}
			StringBuilder frammento = new StringBuilder();
			for (String parola : token.testo.split(" ")) {
				String prova = frammento.length() == 0 ? parola : frammento + " " + parola;
				if (frammento.length() > 0 && larghezzaDelTesto(prova) > larghezzaMassima) {
					nuovi.add(frammentoDi(token, frammento.toString()));
					frammento = new StringBuilder(parola);
				} else {
					frammento = new StringBuilder(prova);
				}
			}
			nuovi.add(frammentoDi(token, frammento.toString()));
		}
		riga.pezzi.clear();
		riga.pezzi.addAll(nuovi);
	}

	private static Token frammentoDi(Token token, String testo) {
		return new Token(testo, null, token.colore, token.azione, token.aiuto, false, token.abilitato).spiega(token.titolo, token.spiegazione);
	}

	/**
	 * Quel che c'è in una riga, con il posto di ogni pezzo: le icone un poco più piccole se non sono sotto il mouse, e semitrasparenti se non si possono usare;
	 * i testi di colore bianco se sono sotto il mouse e si possono cliccare, grigio scuro se la voce è disattivata (tranne il numero).
	 */
	private ContenutoDiColonna contenutoDella(Riga riga, Hover hover) {
		int larghezzaRiga = schermata.larghezzaDisponibileInRiquadroInventario();
		List<Elemento> elementi = new ArrayList<>();
		for (Token token : riga.pezzi) {
			boolean sotto = hover.token == token && token.isCliccabile();
			if (token.icona != null) {
				int lato = altezzaRiga - (sotto ? 0 : 2);
				elementi.add(Elemento.immagineConOpacita(token.icona, lato, lato, token.abilitato ? 1f : OPACITA_DELL_ICONA_SPENTA,
						token.x + (altezzaRiga - lato) / 2, token.linea * altezzaRiga + (altezzaRiga - lato) / 2));
			} else {
				boolean numeroDellaVoce = riga.profondita == 0 && token == riga.pezzi.get(0);
				DoomdarkColorModel.Color colore = sotto ? DoomdarkColorModel.Color.WHITE
						: riga.spenta && !numeroDellaVoce ? DoomdarkColorModel.Color.DARK_GRAY : token.colore;
				elementi.add(Elemento.testo(Collections.singletonList(token.testo), fontRiga, colore, token.x,
						token.linea * altezzaRiga + (altezzaRiga - fontRiga.getHeight()) / 2));
			}
		}
		return new ContenutoDiColonna(larghezzaRiga, altezzaRiga * riga.linee, elementi);
	}

	private static Token testo(String testo) {
		return new Token(testo, null, DoomdarkColorModel.Color.MEDIUM_GRAY, null, null, false, true);
	}

	private static Token testoColorato(String testo, DoomdarkColorModel.Color colore) {
		return new Token(testo, null, colore, null, null, false, true);
	}

	private static Token scelta(String testo, Consumer<Ancora> azione, String aiuto) {
		return new Token(testo, null, DoomdarkColorModel.Color.LIGHT_GRAY, azione, aiuto, false, true);
	}

	private static Token icona(String icona, Runnable azione, String aiuto, boolean abilitata) {
		return new Token(null, icona, null, ancora -> azione.run(), aiuto, true, abilitata);
	}

	/**
	 * Un pezzo che apre un menu a discesa con le scelte e le applica
	 */
	private <T> Token menu(String etichetta, List<MenuADiscesa.Scelta<T>> scelte, T corrente, Consumer<T> alloScegliere, String aiuto) {
		// Il riquadro destro spiega la scelta attuale, o a che cosa serve il menu se non ce n'è una
		String titolo = etichetta;
		String spiegazione = aiuto;
		for (MenuADiscesa.Scelta<T> scelta : scelte) {
			if (scelta.valore.equals(corrente) && scelta.spiegazione != null) {
				spiegazione = scelta.spiegazione;
			}
		}
		final String titoloDelMenu = titolo;
		final String spiegazioneDelMenu = spiegazione;
		return scelta(etichetta, ancora -> {
			menu = new MenuADiscesa<>(scelte, corrente, ancora.x, ancora.sotto, ancora.sopra, schermata.width, schermata.height, misure, alloScegliere);
			titoloMenu = titoloDelMenu;
			spiegazioneMenu = spiegazioneDelMenu;
			voceDelMenu = ancora.voce;
		}, aiuto).spiega(titolo, spiegazione);
	}

	/**
	 * I tre pezzi di un numero: meno, il valore e più; la rotella non serve
	 */
	private void aggiungiNumero(Riga riga, int valore, int passo, int minimo, int massimo, IntConsumer alCambio) {
		riga.pezzi.add(new Token(" - ", null, DoomdarkColorModel.Color.LIGHT_GRAY, a -> alCambio.accept(Math.max(minimo, valore - passo)),
				TestiDellaUI.get("TATTICHE_DIMINUISCE_DI", passo), false, valore > minimo));
		riga.pezzi.add(testoColorato(String.valueOf(valore), DoomdarkColorModel.Color.YELLOW)
				.spiega(TestiDellaUI.get("TATTICHE_NUMERO"), TestiDellaUI.get("TATTICHE_NUMERO_SPIEGAZIONE", passo)));
		riga.pezzi.add(new Token(" + ", null, DoomdarkColorModel.Color.LIGHT_GRAY, a -> alCambio.accept(Math.min(massimo, valore + passo)),
				TestiDellaUI.get("TATTICHE_AUMENTA_DI", passo), false, valore < massimo));
	}

	private static <T extends Enum<T>> List<MenuADiscesa.Scelta<T>> sceltePer(List<T> valori, java.util.function.Function<T, String> etichetta) {
		List<MenuADiscesa.Scelta<T>> scelte = new ArrayList<>();
		for (T valore : valori) {
			scelte.add(new MenuADiscesa.Scelta<>(etichetta.apply(valore), valore, spiegazioneDi(valore)));
		}
		return scelte;
	}

	/**
	 * Come {@link #sceltePer}, ma in ordine alfabetico: per le liste lunghe di valori (stati, classi, categorie, tipi di danno), dove si cerca a occhio
	 */
	private static <T extends Enum<T>> List<MenuADiscesa.Scelta<T>> sceltePerInOrdineAlfabetico(List<T> valori,
			java.util.function.Function<T, String> etichetta) {
		List<MenuADiscesa.Scelta<T>> scelte = sceltePer(valori, etichetta);
		java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.ITALIAN);
		scelte.sort((a, b) -> collator.compare(a.testo, b.testo));
		return scelte;
	}

	/**
	 * Che cosa vuol dire un valore di un menu, quando c'è una spiegazione scritta (rose, filtri, condizioni, ordinamenti, criteri, azioni)
	 */
	private static String spiegazioneDi(Object valore) {
		if (valore instanceof TipoRosa) {
			return TestoTattica.spiega((TipoRosa) valore);
		}
		if (valore instanceof TipoFiltro) {
			return TestoTattica.spiega((TipoFiltro) valore);
		}
		if (valore instanceof TipoPredicato) {
			return TestoTattica.spiega((TipoPredicato) valore);
		}
		if (valore instanceof TipoOrdinamento) {
			return TestoTattica.spiega((TipoOrdinamento) valore);
		}
		if (valore instanceof CriterioIncantesimo) {
			return TestoTattica.spiega((CriterioIncantesimo) valore);
		}
		if (valore instanceof TipoAzione) {
			return TestoTattica.spiega((TipoAzione) valore);
		}
		return null;
	}

	private static String etichettaClasse(TipoPersonaggio classe) {
		return classe.getNome();
	}

	private List<Riga> costruisciRighe() {
		List<Riga> righe = new ArrayList<>();
		List<BozzaTattica.Voce> voci = bozza.getVoci();
		for (int i = 0; i < voci.size(); i++) {
			aggiungiVoce(righe, voci, i);
		}
		// Il tasto per una voce nuova, e sotto l'attacco di riserva che c'è sempre
		Riga nuova = new Riga(0, -1);
		nuova.pezzi.add(new Token(null, NomiDelleImmagini.ICONA_AGGIUNGI, null, a -> bozza.aggiungiVoce(), TestiDellaUI.get("TATTICHE_AGGIUNGE_VOCE"), false, true));
		nuova.pezzi.add(scelta(TestiDellaUI.get("NUOVA_VOCE"), a -> bozza.aggiungiVoce(), TestiDellaUI.get("TATTICHE_AGGIUNGE_VOCE")));
		righe.add(nuova);
		Riga riserva = new Riga(0, -1);
		// Parola per parola, perché vada a capo: un pezzo solo più largo della riga verrebbe tagliato
		for (String parola : TestiDellaUI.get("TATTICHE_RISERVA_RIGA", voci.size() + 1).split(" ")) {
			riserva.pezzi.add(testoColorato(parola, DoomdarkColorModel.Color.DARK_GRAY)
					.spiega(TestiDellaUI.get("TATTICHE_RISERVA"), TestiDellaUI.get("TATTICHE_RISERVA_SPIEGAZIONE")));
		}
		righe.add(riserva);
		for (Riga riga : righe) {
			riga.spenta = riga.voce >= 0 && riga.voce < voci.size() && !voci.get(riga.voce).attiva;
			disponi(riga);
		}
		return righe;
	}

	private void aggiungiVoce(List<Riga> righe, List<BozzaTattica.Voce> voci, int i) {
		BozzaTattica.Voce voce = voci.get(i);
		// Il numero dice se la voce è attiva: verde se vale, rosso se è disattivata (e viene saltata)
		DoomdarkColorModel.Color coloreVoce = voce.attiva ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;

		// La riga della voce: numero (attiva o no), rosa, ordinamento; a destra su, giù, togli
		Riga riga = new Riga(0, i);
		riga.pezzi.add(new Token((i + 1) + ".", null, coloreVoce, a -> voce.attiva = !voce.attiva,
				voce.attiva ? TestiDellaUI.get("TATTICHE_CLICK_DISATTIVA") : TestiDellaUI.get("TATTICHE_CLICK_RIATTIVA"), false, true)
				.spiega(TestiDellaUI.get("TATTICHE_VOCE_N", i + 1), TestiDellaUI.get("TATTICHE_NUMERO_VOCE_SPIEGAZIONE")));
		riga.pezzi.add(new Token(nomeDellaVoce(voce, i), null, DoomdarkColorModel.Color.WHITE, a -> clickSulNome(voce, i),
				TestiDellaUI.get(voce.chiusa ? "TATTICHE_CLICK_APRI" : "TATTICHE_CLICK_RIPIEGA"), false, true)
				.spiega(TestiDellaUI.get("TATTICHE_NOME"), TestiDellaUI.get("TATTICHE_NOME_SPIEGAZIONE")));
		riga.pezzi.add(icona(NomiDelleImmagini.ICONA_ELIMINA, () -> bozza.rimuoviVoce(i), TestiDellaUI.get("TATTICHE_TOGLIE_VOCE"), true));
		riga.pezzi.add(icona(NomiDelleImmagini.ICONA_GIU, () -> bozza.spostaVoce(i, 1), TestiDellaUI.get("TATTICHE_VOCE_GIU"), i < voci.size() - 1));
		riga.pezzi.add(icona(NomiDelleImmagini.ICONA_SU, () -> bozza.spostaVoce(i, -1), TestiDellaUI.get("TATTICHE_VOCE_SU"), i > 0));
		righe.add(riga);
		if (voce.chiusa) {
			return;
		}

		// Il corpo, sotto il nome: su chi agisce la voce e come sceglie
		riga = new Riga(1, i);
		riga.pezzi.add(testo(TestiDellaUI.get("TATTICHE_PAROLA_SU")).spiega(TestiDellaUI.get("TATTICHE_SU_CHI"), TestiDellaUI.get("TATTICHE_SU_CHI_SPIEGAZIONE")));
		List<TipoRosa> rose = BozzaTattica.roseAmmesse(voce);
		if (voce.rosa == null || rose.size() != 1) {
			riga.pezzi.add(menu(voce.rosa == null ? TestiDellaUI.get("TATTICHE_SCEGLI_SU_CHI") : voce.rosa.getDescrizione(), sceltePer(rose, TipoRosa::getDescrizione), voce.rosa,
					rosa -> {
						voce.rosa = rosa;
						voce.filtri.clear();
						if (voce.ordinamento == null || !BozzaTattica.ordinamentiAmmessi(rosa).contains(voce.ordinamento.getTipoOrdinamento())) {
							voce.ordinamento = BozzaTattica.ordinamentoPredefinito(TipoOrdinamento.PRIMO);
						}
					}, TestiDellaUI.get("TATTICHE_SU_CHI_AGISCE")));
		} else {
			// Una sola scelta possibile: non è un menu, ma dice perché
			riga.pezzi.add(new Token(voce.rosa.getDescrizione(), null, DoomdarkColorModel.Color.LIGHT_GRAY, null,
					TestiDellaUI.get("TATTICHE_SOLO_SU", voce.rosa.getDescrizione().toLowerCase(Locale.ROOT)), false, true));
		}
		if (haOrdinamento(voce)) {
			riga.pezzi.add(testo(":"));
			riga.pezzi.add(menu(TestoTattica.ordinamento(voce.ordinamento), sceltePer(BozzaTattica.ordinamentiAmmessi(voce.rosa),
					t -> t.getDescrizione()), voce.ordinamento.getTipoOrdinamento(),
					tipo -> voce.ordinamento = BozzaTattica.ordinamentoPredefinito(tipo), TestiDellaUI.get("TATTICHE_COME_SCEGLIE")));
			if (voce.ordinamento.getTipoDanno() != null) {
				riga.pezzi.add(menu(voce.ordinamento.getTipoDanno().getNome(), sceltePerInOrdineAlfabetico(Arrays.asList(TipoDanno.values()), TipoDanno::getNome),
						voce.ordinamento.getTipoDanno(), danno -> voce.ordinamento = OrdinamentoMD.di(voce.ordinamento.getTipoOrdinamento(), danno),
						TestiDellaUI.get("TATTICHE_TIPO_DANNO")));
			}
		}
		righe.add(riga);

		// I filtri del selettore
		aggiungiFiltri(righe, new ListaFiltri() {
			@Override
			public TipoRosa rosa() {
				return voce.rosa;
			}

			@Override
			public List<FiltroMD> get() {
				return voce.filtri;
			}

			@Override
			public void set(List<FiltroMD> nuova) {
				voce.filtri.clear();
				voce.filtri.addAll(nuova);
			}
		}, 1, i, TestiDellaUI.get("TATTICHE_CON"));

		// Le condizioni
		for (int c = 0; c < voce.condizioni.size(); c++) {
			aggiungiCondizione(righe, voce, c, i);
		}
		Riga nuovaCondizione = new Riga(1, i);
		nuovaCondizione.pezzi.add(testo(TestiDellaUI.get("TATTICHE_PAROLA_SE")).spiega(TestiDellaUI.get("TATTICHE_CONDIZIONE"), TestiDellaUI.get("TATTICHE_CONDIZIONI_SPIEGAZIONE")));
		nuovaCondizione.pezzi.add(menu(TestiDellaUI.get("TATTICHE_AGGIUNGI_CONDIZIONE"), sceltePer(BozzaTattica.predicatiAmmessi(), t -> t.getDescrizione()), null,
				tipo -> voce.condizioni.add(BozzaTattica.predicatoPredefinito(tipo)), TestiDellaUI.get("TATTICHE_CONDIZIONE_AIUTO")));
		righe.add(nuovaCondizione);

		// Le azioni
		for (int a = 0; a < voce.azioni.size(); a++) {
			aggiungiAzione(righe, voce, a, i);
		}
		Riga nuovaAzione = new Riga(1, i);
		nuovaAzione.pezzi.add(testo(TestiDellaUI.get("TATTICHE_PAROLA_FAI")).spiega(TestiDellaUI.get("TATTICHE_AZIONE"), TestiDellaUI.get("TATTICHE_AZIONI_SPIEGAZIONE")));
		nuovaAzione.pezzi.add(menu(TestiDellaUI.get("TATTICHE_AGGIUNGI_AZIONE"), sceltePer(BozzaTattica.azioniAmmesse(voce, azioniPossibili), t -> t.getDescrizione()), null,
				tipo -> bozza.aggiungiAzione(voce, tipo), TestiDellaUI.get("TATTICHE_AZIONE_AIUTO")));
		righe.add(nuovaAzione);
	}

	/**
	 * L'ordinamento ha senso solo dove la voce sceglie un bersaglio fra più candidati: non su se stessi, non per le azioni su tutto il gruppo né per il cambio d'arma
	 */
	private static boolean haOrdinamento(BozzaTattica.Voce voce) {
		// Su se stesso il candidato è uno solo: non c'è niente da scegliere
		if (voce.ordinamento == null || voce.rosa == null || voce.rosa == TipoRosa.SE_STESSO) {
			return false;
		}
		if (voce.azioni.isEmpty()) {
			return true;
		}
		switch (PortataAzione.di(voce.azioni.get(0).getTipo())) {
			case GRUPPO_ALLEATI:
			case GRUPPO_AVVERSARI:
			case ARMA:
				return false;
			default:
				return true;
		}
	}

	private void aggiungiFiltri(List<Riga> righe, ListaFiltri lista, int profondita, int indiceVoce, String parola) {
		for (int f = 0; f < lista.get().size(); f++) {
			final int indice = f;
			FiltroMD filtro = lista.get().get(f);
			Riga riga = new Riga(profondita, indiceVoce);
			if (BozzaTattica.isNegabile(filtro.getTipoFiltro())) {
				// Un menu come gli altri: "con" tiene i candidati per cui il filtro è vero, "senza" quelli per cui è falso. Si riconosce dal colore dalle
				// parole fisse: chiaro se si può scegliere, grigio scuro se no (i filtri numerici)
				List<MenuADiscesa.Scelta<Boolean>> modi = Arrays.asList(
						new MenuADiscesa.Scelta<>(parola, false, TestiDellaUI.get("TATTICHE_CON_AIUTO")),
						new MenuADiscesa.Scelta<>(TestiDellaUI.get("TATTICHE_SENZA"), true, TestiDellaUI.get("TATTICHE_SENZA_AIUTO")));
				riga.pezzi.add(menu(filtro.isNegato() ? TestiDellaUI.get("TATTICHE_SENZA") : parola, modi, filtro.isNegato(),
						senza -> sostituisci(lista, indice, senza == filtro.isNegato() ? filtro : filtro.negato()), TestiDellaUI.get("TATTICHE_CON_O_SENZA")));
			} else {
				riga.pezzi.add(testo(parola).spiega(TestiDellaUI.get("TATTICHE_FILTRO"), TestiDellaUI.get("TATTICHE_FILTRI_SPIEGAZIONE")));
			}
			riga.pezzi.add(menu(filtro.getTipoFiltro().getDescrizione(), sceltePer(BozzaTattica.filtriAmmessi(lista.rosa()), t -> t.getDescrizione()),
					filtro.getTipoFiltro(), tipo -> {
						FiltroMD nuovo = BozzaTattica.filtroPredefinito(tipo);
						// Il "senza" resta solo dove ha senso (non nei filtri numerici)
						sostituisci(lista, indice, filtro.isNegato() && BozzaTattica.isNegabile(tipo) ? nuovo.negato() : nuovo);
					}, TestiDellaUI.get("TATTICHE_TIPO_FILTRO")));
			aggiungiParametriFiltro(riga, lista, indice, filtro);
			riga.pezzi.add(icona(NomiDelleImmagini.ICONA_ELIMINA, () -> {
				List<FiltroMD> nuova = new ArrayList<>(lista.get());
				nuova.remove(indice);
				lista.set(nuova);
			}, TestiDellaUI.get("TATTICHE_TOGLIE_FILTRO"), true));
			righe.add(riga);
		}
		Riga nuovo = new Riga(profondita, indiceVoce);
		nuovo.pezzi.add(testo(parola).spiega(TestiDellaUI.get("TATTICHE_FILTRO"), TestiDellaUI.get("TATTICHE_FILTRI_SPIEGAZIONE")));
		nuovo.pezzi.add(menu(TestiDellaUI.get("TATTICHE_AGGIUNGI_FILTRO"), sceltePer(BozzaTattica.filtriAmmessi(lista.rosa()), t -> t.getDescrizione()), null, tipo -> {
			List<FiltroMD> nuova = new ArrayList<>(lista.get());
			nuova.add(BozzaTattica.filtroPredefinito(tipo));
			lista.set(nuova);
		}, TestiDellaUI.get("TATTICHE_RESTRINGE")));
		righe.add(nuovo);
	}

	private static void sostituisci(ListaFiltri lista, int indice, FiltroMD nuovo) {
		List<FiltroMD> nuova = new ArrayList<>(lista.get());
		nuova.set(indice, nuovo);
		lista.set(nuova);
	}

	private void aggiungiParametriFiltro(Riga riga, ListaFiltri lista, int indice, FiltroMD filtro) {
		TipoParametro parametro = filtro.getTipoFiltro().getParametro();
		if (parametro == TipoParametro.CONFRONTO_E_NUMERO || parametro == TipoParametro.CONFRONTO) {
			riga.pezzi.add(menu(TestoTattica.relazione(filtro.getConfronto(), filtro.isNegato()), relazioni(filtro.getConfronto(), filtro.isNegato()),
					new Relazione(filtro.getConfronto(), filtro.isNegato()),
					r -> sostituisci(lista, indice, BozzaTattica.conRelazione(filtro, r.tipoConfronto, r.negato)), TestiDellaUI.get("TATTICHE_CONFRONTO")));
			if (parametro == TipoParametro.CONFRONTO_E_NUMERO) {
				aggiungiNumero(riga, filtro.getNumero(), BozzaTattica.passo(filtro.getTipoFiltro()), 0, BozzaTattica.massimo(filtro.getTipoFiltro()),
						n -> sostituisci(lista, indice, BozzaTattica.conNumero(filtro, n)));
			}
		} else if (parametro == TipoParametro.STATO) {
			riga.pezzi.add(menu(filtro.getTipoEffettoDiStato().getDescrizione(), sceltePerInOrdineAlfabetico(Arrays.asList(TipoEffettoDiStato.values()),
					TipoEffettoDiStato::getDescrizione), filtro.getTipoEffettoDiStato(),
					s -> sostituisci(lista, indice, BozzaTattica.conParametro(filtro, s)), TestiDellaUI.get("TATTICHE_STATO")));
		} else if (parametro == TipoParametro.CLASSE) {
			riga.pezzi.add(menu(etichettaClasse(filtro.getTipoPersonaggio()), sceltePerInOrdineAlfabetico(Arrays.asList(TipoPersonaggio.values()),
					EditorDelleTattiche::etichettaClasse), filtro.getTipoPersonaggio(),
					c -> sostituisci(lista, indice, BozzaTattica.conParametro(filtro, c)), TestiDellaUI.get("TATTICHE_CLASSE")));
		} else if (parametro == TipoParametro.CATEGORIA) {
			riga.pezzi.add(menu(filtro.getCategoriaPersonaggio().getNome(), sceltePerInOrdineAlfabetico(Arrays.asList(CategoriaPersonaggio.values()),
					CategoriaPersonaggio::getNome), filtro.getCategoriaPersonaggio(),
					c -> sostituisci(lista, indice, BozzaTattica.conParametro(filtro, c)), TestiDellaUI.get("TATTICHE_CATEGORIA")));
		} else if (parametro == TipoParametro.TIPO_DANNO) {
			riga.pezzi.add(menu(filtro.getTipoDanno().getNome(), sceltePerInOrdineAlfabetico(Arrays.asList(TipoDanno.values()), TipoDanno::getNome),
					filtro.getTipoDanno(), d -> sostituisci(lista, indice, BozzaTattica.conParametro(filtro, d)), TestiDellaUI.get("TATTICHE_TIPO_DANNO")));
		}
	}

	/**
	 * Un confronto con il suo eventuale rovescio ("non maggiore di" è "al massimo"), che la tattica può avere anche se il menu non lo propone
	 */
	private static final class Relazione {
		final TipoConfronto tipoConfronto;
		final boolean negato;

		Relazione(TipoConfronto tipoConfronto, boolean negato) {
			this.tipoConfronto = tipoConfronto;
			this.negato = negato;
		}

		@Override
		public boolean equals(Object altro) {
			return altro instanceof Relazione && ((Relazione) altro).tipoConfronto == tipoConfronto && ((Relazione) altro).negato == negato;
		}

		@Override
		public int hashCode() {
			return tipoConfronto.hashCode() * 2 + (negato ? 1 : 0);
		}
	}

	/**
	 * Le tre relazioni fra un valore e un numero: minore di, uguale a, maggiore di. Con numeri interi "al massimo 50" è "minore di 51": si va a grandi passi. Il
	 * rovescio ("almeno", "al massimo", "diverso da") c'è solo se la tattica letta già lo contiene.
	 */
	private static List<MenuADiscesa.Scelta<Relazione>> relazioni(TipoConfronto attuale, boolean attualeNegato) {
		List<MenuADiscesa.Scelta<Relazione>> scelte = new ArrayList<>();
		for (TipoConfronto tipoConfronto : new TipoConfronto[]{TipoConfronto.MINORE, TipoConfronto.UGUALE, TipoConfronto.MAGGIORE}) {
			scelte.add(new MenuADiscesa.Scelta<>(TestoTattica.confronto(tipoConfronto), new Relazione(tipoConfronto, false),
					TestoTattica.spiega(tipoConfronto)));
		}
		if (attualeNegato) {
			scelte.add(new MenuADiscesa.Scelta<>(TestoTattica.relazione(attuale, true), new Relazione(attuale, true), TestoTattica.spiega(attuale, true)));
		}
		return scelte;
	}

	private void aggiungiCondizione(List<Riga> righe, BozzaTattica.Voce voce, int c, int indiceVoce) {
		PredicatoMD predicato = voce.condizioni.get(c);
		Riga riga = new Riga(1, indiceVoce);
		riga.pezzi.add(testo(TestiDellaUI.get("TATTICHE_PAROLA_SE")).spiega(TestiDellaUI.get("TATTICHE_CONDIZIONE"), TestiDellaUI.get("TATTICHE_CONDIZIONI_SPIEGAZIONE")));
		riga.pezzi.add(menu(predicato.getTipoPredicato().getDescrizione(), sceltePer(BozzaTattica.predicatiAmmessi(), t -> t.getDescrizione()),
				predicato.getTipoPredicato(), tipo -> {
					voce.condizioni.set(c, BozzaTattica.predicatoPredefinito(tipo));
				}, TestiDellaUI.get("TATTICHE_TIPO_CONDIZIONE")));
		if (predicato.getTipoPredicato() == TipoPredicato.QUANTI) {
			riga.pezzi.add(menu(predicato.getTipoRosa().getDescrizione(), sceltePer(rosePerQuanti(predicato.getTipoRosa()), TipoRosa::getDescrizione), predicato.getTipoRosa(),
					r -> voce.condizioni.set(c, BozzaTattica.conRosa(predicato, r)), TestiDellaUI.get("TATTICHE_DA_QUALI_SI_CONTA")));
		}
		if (predicato.getTipoPredicato() != TipoPredicato.PROBABILITA) {
			riga.pezzi.add(menu(TestoTattica.relazione(predicato.getTipoConfronto(), predicato.isNegato()),
					relazioni(predicato.getTipoConfronto(), predicato.isNegato()), new Relazione(predicato.getTipoConfronto(), predicato.isNegato()),
					r -> voce.condizioni.set(c, BozzaTattica.conRelazione(predicato, r.tipoConfronto, r.negato)), TestiDellaUI.get("TATTICHE_CONFRONTO")));
		}
		aggiungiNumero(riga, predicato.getNumero(), BozzaTattica.passo(predicato.getTipoPredicato()), 0, BozzaTattica.massimo(predicato.getTipoPredicato()),
				n -> voce.condizioni.set(c, BozzaTattica.conNumero(predicato, n)));
		if (predicato.getTipoPredicato() == TipoPredicato.PROBABILITA) {
			riga.pezzi.add(testo("%"));
		}
		riga.pezzi.add(icona(NomiDelleImmagini.ICONA_ELIMINA, () -> voce.condizioni.remove(c), TestiDellaUI.get("TATTICHE_TOGLIE_CONDIZIONE"), true));
		righe.add(riga);
		if (predicato.getTipoPredicato() == TipoPredicato.QUANTI) {
			aggiungiFiltri(righe, new ListaFiltri() {
				@Override
				public TipoRosa rosa() {
					return voce.condizioni.get(c).getTipoRosa();
				}

				@Override
				public List<FiltroMD> get() {
					return voce.condizioni.get(c).getFiltri();
				}

				@Override
				public void set(List<FiltroMD> nuova) {
					voce.condizioni.set(c, BozzaTattica.conFiltri(voce.condizioni.get(c), nuova));
				}
			}, 2, indiceVoce, TestiDellaUI.get("TATTICHE_CON"));
		}
	}

	/**
	 * Le rose da cui "Quanti" conta: non se stessi (contare un candidato solo non ha senso). Quella già scelta resta, anche se è se stessi, perché
	 * una tattica letta può averla.
	 */
	private static List<TipoRosa> rosePerQuanti(TipoRosa attuale) {
		List<TipoRosa> rose = new ArrayList<>();
		for (TipoRosa rosa : TipoRosa.values()) {
			if (rosa.valePer(true) && (rosa != TipoRosa.SE_STESSO || attuale == TipoRosa.SE_STESSO)) {
				rose.add(rosa);
			}
		}
		return rose;
	}

	private void aggiungiAzione(List<Riga> righe, BozzaTattica.Voce voce, int a, int indiceVoce) {
		AzioneMD azione = voce.azioni.get(a);
		Riga riga = new Riga(1, indiceVoce);
		riga.pezzi.add(testo(TestiDellaUI.get("TATTICHE_PAROLA_FAI")).spiega(TestiDellaUI.get("TATTICHE_AZIONE"), TestiDellaUI.get("TATTICHE_AZIONI_SPIEGAZIONE")));
		riga.pezzi.add(menu(azione.getTipo().getDescrizione(), sceltePer(BozzaTattica.azioniAmmesse(voce, azioniPossibili), t -> t.getDescrizione()),
				azione.getTipo(), tipo -> bozza.sostituisciAzione(voce, a, tipo), TestiDellaUI.get("TATTICHE_L_AZIONE")));
		if (azione.getCriterio() != null) {
			riga.pezzi.add(menu(TestoTattica.criterio(azione.getCriterio()), sceltePer(Arrays.asList(CriterioIncantesimo.values()),
					TestoTattica::criterio), azione.getCriterio(),
					c -> voce.azioni.set(a, BozzaTattica.conCriterio(azione, c)), TestiDellaUI.get("TATTICHE_QUALE_INCANTESIMO")));
		}
		if (azione.getArmaDi() != null) {
			riga.pezzi.add(testo(TestiDellaUI.get("TATTICHE_PAROLA_DI")).spiega(TestiDellaUI.get("TATTICHE_ARMA"), TestiDellaUI.get("TATTICHE_ARMA_SPIEGAZIONE")));
			riga.pezzi.add(menu(azione.getArmaDi().getNome(), sceltePerInOrdineAlfabetico(Arrays.asList(TipoDanno.values()), TipoDanno::getNome), azione.getArmaDi(),
					d -> voce.azioni.set(a, AzioneMD.cambiaArma(d)), TestiDellaUI.get("TATTICHE_ARMA_AIUTO")));
		}
		if (BozzaTattica.haSoloSePiuForte(azione)) {
			riga.pezzi.add(new Token(TestiDellaUI.get(azione.isSoloSePiuForteDellAttacco() ? "TATTICHE_SOLO_SE_PIU_FORTE" : "TATTICHE_SEMPRE"), null,
					azione.isSoloSePiuForteDellAttacco() ? DoomdarkColorModel.Color.YELLOW : DoomdarkColorModel.Color.DARK_GRAY,
					x -> voce.azioni.set(a, BozzaTattica.commutaSoloSePiuForte(azione)),
					TestiDellaUI.get("TATTICHE_SOLO_SE_PIU_FORTE_CLICK"), false, true)
					.spiega(TestiDellaUI.get("TATTICHE_SOLO_SE_PIU_FORTE_TITOLO"), TestiDellaUI.get("TATTICHE_SOLO_SE_PIU_FORTE_SPIEGAZIONE")));
		}
		riga.pezzi.add(icona(NomiDelleImmagini.ICONA_ELIMINA, () -> voce.azioni.remove(a), TestiDellaUI.get("TATTICHE_TOGLIE_AZIONE"), true));
		righe.add(riga);
	}

	// ============================================================== il mouse

	/**
	 * Se un menu a discesa è aperto: finché lo è prende per sé pressione, trascinamento, rilascio, rotella e click, e la lista sotto non si scorre
	 */
	public boolean isMenuAperto() {
		return menu != null && menu.isAperto();
	}

	/**
	 * La pressione: con il menu aperto è per il menu; dice se l'ha presa.
	 */
	public boolean processaPressione(int x, int y, Tasto tasto) {
		if (!isMenuAperto()) {
			return false;
		}
		if (tasto == Tasto.SINISTRO) {
			menu.premi(x, y);
		}
		return true;
	}

	public boolean processaTrascinamento(int x, int y) {
		if (!isMenuAperto()) {
			return false;
		}
		menu.trascina(y);
		return true;
	}

	public boolean processaRilascio(int x, int y, Tasto tasto) {
		if (!isMenuAperto()) {
			return false;
		}
		if (tasto == Tasto.SINISTRO) {
			menu.rilascia();
		}
		return true;
	}

	public boolean processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (!isMenuAperto()) {
			return false;
		}
		menu.rotella(numeroRotazioni, movimentoRotella == MovimentoRotella.SU);
		return true;
	}

	/**
	 * Un click: con il menu aperto è per il menu (un click fuori lo chiude); altrimenti i pulsanti della colonna centrale e i pezzi dell'elenco.
	 */
	public void processaClick(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO || personaggio == null) {
			return;
		}
		if (isMenuAperto()) {
			// Il rilascio di un trascinamento del menu non sceglie niente
			if (menu.isGestoTrascinato()) {
				return;
			}
			menu.click(x, y);
			if (!menu.isAperto()) {
				menu = null;
			}
			return;
		}
		// Il rilascio di un trascinamento della lista non è un click
		if (schermata.isGestoTrascinato()) {
			return;
		}
		menu = null;
		if (processaClickColonnaCentrale(x, y)) {
			return;
		}
		List<Riga> righe = costruisciRighe();
		Hover hover = trovaHover(righe, x, y);
		if (hover.token == null || !hover.token.isCliccabile()) {
			return;
		}
		// Dove si apre un menu: sotto la riga cliccata (o sopra), allineato al pezzo
		ElencoScorrevole<Riga> elenco = costruisciElenco(righe, Hover.NESSUNO);
		Punto posizione = elenco.posizioneTitolo(r -> r == hover.riga);
		int yRiga = SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + 2 * Finestra.SPACING + (posizione == null ? 0 : posizione.y) - schermata.offsetYZonaSinistra();
		int yLinea = yRiga + hover.token.linea * altezzaRiga;
		hover.token.azione.accept(new Ancora(schermata.xMinimaZonaSinistra + Finestra.SPACING + hover.token.x, yLinea + altezzaRiga, yLinea, hover.riga.voce));
	}

	/**
	 * Per i test: come il click sul primo pezzo con quel testo dell'elenco, senza passare dalle coordinate.
	 *
	 * @return false se non c'è
	 */
	public boolean attivaPezzo(String testo) {
		for (Riga riga : costruisciRighe()) {
			for (Token token : riga.pezzi) {
				if (testo.equals(token.testo) && token.isCliccabile()) {
					token.azione.accept(new Ancora(100, 100, 90, -1));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Dove sta la colonna con le regole della tattica, nelle coordinate dello schermo (per il tutorial).
	 */
	public Area getAreaDellaLista() {
		return new Area(schermata.xMinimaZonaSinistra + Finestra.SPACING, SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + 2 * Finestra.SPACING,
				schermata.larghezzaDisponibileInRiquadroInventario(), schermata.altezzaDisponibileInRiquadroInventario());
	}

	/**
	 * Dove sta la colonna che spiega l'elemento sotto il mouse (per il tutorial).
	 */
	public Area getAreaDellaSpiegazione() {
		return new Area(schermata.xMinimaZonaDestra + Finestra.SPACING, SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + 2 * Finestra.SPACING,
				schermata.larghezzaDisponibileInRiquadroInventario(), schermata.altezzaDisponibileInRiquadroInventario());
	}

	/**
	 * Dove stanno Sì, No e Predefinita (per il tutorial): l'area che li contiene, quelli che ci sono; se la colonna centrale non è ancora stata
	 * disegnata, tutta la colonna centrale.
	 */
	public Area getAreaDeiPulsanti() {
		Area totale = null;
		for (Area area : new Area[] {areaSi, areaNo, areaPredefinita}) {
			if (area != null) {
				totale = totale == null ? area : DisposizioneDelFumetto.unione(totale, area);
			}
		}
		return totale != null ? totale : new Area(schermata.xMinimaZonaCentrale, SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + 2 * Finestra.SPACING,
				schermata.xMassimaZonaCentrale - schermata.xMinimaZonaCentrale, schermata.altezzaDisponibileInRiquadroInventario());
	}

	public BozzaTattica getBozza() {
		return bozza;
	}

	/**
	 * I click sulle aree della colonna centrale: le frecce spostano il personaggio nel gruppo, Sì manda la tattica al motore (solo se la bozza non ha
	 * problemi: la conferma è ignorata altrimenti), No butta la bozza e torna alla tattica applicata, "predefinita" riparte da quella della classe.
	 * Né No né "predefinita" toccano il motore: solo Sì cambia la tattica del personaggio, e il motore la ripubblica (RichiestaAperturaTattiche) per
	 * far aggiornare la schermata.
	 *
	 * @return true se il click era su una di queste aree
	 */
	private boolean processaClickColonnaCentrale(int x, int y) {
		if (areaSposta[0] != null && areaSposta[0].contiene(x, y)) {
			if (puoSpostareASinistra) {
				BusEventi.pubblica(new ComandoSpostaPersonaggio(personaggio.getUuid(), false));
			}
			return true;
		}
		if (areaSposta[1] != null && areaSposta[1].contiene(x, y)) {
			if (puoSpostareADestra) {
				BusEventi.pubblica(new ComandoSpostaPersonaggio(personaggio.getUuid(), true));
			}
			return true;
		}
		if (areaSi != null && areaSi.contiene(x, y)) {
			if (bozza.problemi().isEmpty()) {
				BusEventi.pubblica(new ComandoImpostaTattica(personaggio.getUuid(), bozza.toTattica()));
			}
			return true;
		}
		if (areaNo != null && areaNo.contiene(x, y)) {
			annullaLaRichiestaDiTesto();
			bozza = BozzaTattica.da(applicata);
			return true;
		}
		if (areaPredefinita != null && areaPredefinita.contiene(x, y)) {
			annullaLaRichiestaDiTesto();
			bozza = BozzaTattica.da(predefinita);
			return true;
		}
		return false;
	}
}
