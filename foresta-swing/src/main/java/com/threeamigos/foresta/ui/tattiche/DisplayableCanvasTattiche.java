package com.threeamigos.foresta.ui.tattiche;

import com.threeamigos.foresta.tipi.PortataAzione;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostaTattica;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpostaPersonaggio;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tattiche.*;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.*;
import com.threeamigos.foresta.ui.componenti.ComponenteScorrevole;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasScambiatore;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * La schermata delle tattiche di combattimento, che si apre dall'inventario (comando MOSTRA_TATTICHE): a sinistra la tattica del personaggio
 * scelto come elenco di voci (ognuna con i suoi filtri, le sue condizioni e le sue azioni), al centro il personaggio e i pulsanti, a destra la
 * spiegazione della voce sotto il mouse. Si cambia una bozza ({@link BozzaTattica}); solo con il pulsante Sì la tattica passa al motore
 * (<code>ComandoImpostaTattica</code>), con No si torna a quella di prima. Le frecce sotto il personaggio lo spostano nel gruppo
 * (<code>ComandoSpostaPersonaggio</code>). Il personaggio si sceglie dalla barra delle icone, come nell'inventario.
 * <p>
 * Ogni riga dell'elenco è fatta di <i>pezzi</i> ({@link Token}): testo che non si tocca, testo che apre un menu a discesa ({@link CasellaADiscesa}) o
 * cambia un valore, icone a destra (su, giù, togli). L'elenco è un {@link ComponenteScorrevole} che si ricostruisce a ogni disegno e a ogni click.
 */
public class DisplayableCanvasTattiche extends DisplayableCanvasScambiatore {

	private static final int PROFONDITA_INDENTAZIONE = 12;
	private static final int SPAZIO_FRA_PEZZI = 5;
	private static final int MARGINE_RIGA = 2;

	private static final BufferedImage ICONA_SU = BufferedImageBuilder.buildBufferedImage("icone/Su.gif");
	private static final BufferedImage ICONA_GIU = BufferedImageBuilder.buildBufferedImage("icone/Giu.gif");
	private static final BufferedImage ICONA_ELIMINA = BufferedImageBuilder.buildBufferedImage("icone/Rimuovi.gif");
	private static final BufferedImage ICONA_AGGIUNGI = BufferedImageBuilder.buildBufferedImage("icone/Aggiungi.gif");
	private static final BufferedImage ICONA_SINISTRA = BufferedImageBuilder.buildBufferedImage("icone/Sinistra.gif");
	private static final BufferedImage ICONA_DESTRA = BufferedImageBuilder.buildBufferedImage("icone/Destra.gif");
	private static final BufferedImage ICONA_SI = BufferedImageBuilder.buildBufferedImage("icone/Si.gif");
	private static final BufferedImage ICONA_NO = BufferedImageBuilder.buildBufferedImage("icone/No.gif");

	/**
	 * Dove si apre un menu: sotto la riga che lo ha aperto, o sopra se sotto non c'è posto
	 */
	private static final class Ancora {
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
	private static final class Token {
		final String testo;
		final BufferedImage icona;
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

		Token(String testo, BufferedImage icona, DoomdarkColorModel.Color colore, Consumer<Ancora> azione, String aiuto, boolean adDestra,
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
	private static final class Riga {
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

	private final DoomdarkFont fontRiga = DoomdarkFontMedium.getInstance();
	private final int altezzaRiga;

	private VistaPersonaggio personaggio;
	private TatticaMD applicata = TatticaMD.VUOTA;
	private TatticaMD predefinita = TatticaMD.VUOTA;
	// Le azioni che la classe del personaggio può avere nella tattica; null: tutte
	private java.util.Set<TipoAzione> azioniPossibili;
	private BozzaTattica bozza = BozzaTattica.da(TatticaMD.VUOTA);
	// Le bozze modificate e non confermate di chi si è lasciato per un altro personaggio (per UUID), con la tattica da cui partivano
	private final Map<String, BozzaLasciata> bozzeLasciate = new HashMap<>();
	private CasellaADiscesa<?> menu;
	// Il pezzo che ha aperto il menu: la sua spiegazione resta nel riquadro destro finché il mouse non è su una scelta
	private String titoloMenu;
	private String spiegazioneMenu;
	private int voceDelMenu = -1;

	// Le aree dei pulsanti della colonna centrale, aggiornate a ogni disegno
	private final Rectangle[] areaSposta = new Rectangle[2];
	private Rectangle areaSi;
	private Rectangle areaNo;
	private Rectangle areaPredefinita;
	private boolean puoSpostareASinistra;
	private boolean puoSpostareADestra;

	// Per rinominare una voce: chi chiede un testo al giocatore (testo predefinito, e chi riceve quello confermato)
	private java.util.function.BiConsumer<String, Consumer<String>> richiestaTesto;
	private Consumer<Consumer<String>> annullaRichiestaTesto;
	// Chi aspetta il nome di una voce, finché il giocatore non risponde o la richiesta decade
	private Consumer<String> richiestaInCorso;
	// Il nome di una voce si clicca per ripiegarla; due click ravvicinati sullo stesso nome la rinominano
	private static final long INTERVALLO_DOPPIO_CLICK_MS = 400;
	private BozzaTattica.Voce ultimaVoceCliccata;
	private long ultimoClickSulNome;

	public DisplayableCanvasTattiche(int width, int height, VistaPartita vistaPartita) {
		super(width, height, vistaPartita);
		altezzaRiga = Math.max(fontRiga.getHeight(), 12) + 2 * MARGINE_RIGA;
	}

	/**
	 * @param richiestaTesto chi chiede un testo al giocatore: il testo predefinito e chi riceve quello confermato
	 * @param annullaRichiesta chi chiude la richiesta di prima se non serve più, riconoscendola da chi doveva riceverla
	 */
	public void setRichiestaTesto(java.util.function.BiConsumer<String, Consumer<String>> richiestaTesto,
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
	void clickSulNome(BozzaTattica.Voce voce, int indice) {
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
			offsetYZonaSinistra = 0;
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

	// ============================================================== il disegno

	/**
	 * Disegna la schermata: la colonna del personaggio e l'elenco delle voci della sua tattica, con le caselle a discesa aperte per ultime.
	 */
	public void disegna(Graphics2D graphics) {
		super.disegnaInventario(graphics);
		if (personaggio == null) {
			return;
		}
		disegnaColonnaPersonaggio(graphics);
		List<Riga> righe = costruisciRighe();
		Hover hover = menu != null ? Hover.NESSUNO : trovaHover(righe, mouseX, mouseY);
		ComponenteScorrevole<Riga> elenco = costruisciElenco(righe, hover);
		offsetYZonaSinistra = elenco.limitaOffset(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, offsetYZonaSinistra);
		graphics.drawImage(elenco.produci(ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, offsetYZonaSinistra),
				xMinimaZonaSinistra + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);
		int voceSpiegata = hover.riga != null ? hover.riga.voce : -1;
		String titoloElemento = null;
		String testoElemento = null;
		if (menu != null && menu.isAperta()) {
			// Con un menu aperto: la scelta sotto il mouse, altrimenti il pezzo che lo ha aperto
			voceSpiegata = voceDelMenu;
			CasellaADiscesa.Scelta<?> sotto = menu.sceltaSotto(mouseX, mouseY);
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
		disegnaSpiegazione(graphics, voceSpiegata, titoloElemento, testoElemento);
		if (menu != null) {
			if (menu.isAperta()) {
				menu.disegna(graphics, mouseX, mouseY);
			} else {
				menu = null;
			}
		} else if (hover.token != null && hover.token.aiuto != null) {
			disegnaAiuto(graphics, Collections.singletonList(hover.token.aiuto));
		}
	}

	@Override
	public void disegnaIntestazioniInventario(Graphics2D graphics) {
		disegnaIntestazioniInventarioImpl(graphics, "Tattica", "Spiegazione");
	}

	@Override
	public void disegnaColonnaPersonaggio(Graphics2D graphics) {
		DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;
		int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
		Image nome = ImageCache.get(personaggio.getNome(VistaPersonaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), coloreTestata);
		graphics.drawImage(nome, (width - nome.getWidth(null)) / 2, y, null);
		y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

		BufferedImage immagine = MappaPersonaggioImmagine.getImmagine(personaggio.getClasse());
		y += ALTEZZA_LADRO;
		graphics.drawImage(immagine, (width - immagine.getWidth()) / 2, y - immagine.getHeight(), null);
		y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

		boolean capo = vistaPartita.getGruppoGiocatore().getCapo() != null
				&& vistaPartita.getGruppoGiocatore().getCapo().getUuid().equals(personaggio.getUuid());
		if (capo) {
			y = disegnaCentrato(graphics, "Capo del gruppo", DoomdarkColorModel.Color.RED, y);
		}
		if (!personaggio.isVivo()) {
			y = disegnaCentrato(graphics, "Caduto: la sua tattica vale quando torna in vita", DoomdarkColorModel.Color.MEDIUM_GRAY, y);
		}
		y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI / 2;

		// Il posto nel gruppo: le frecce lo spostano a sinistra o a destra
		List<? extends VistaPersonaggio> permanenti = permanenti();
		int posto = -1;
		for (int i = 0; i < permanenti.size(); i++) {
			if (permanenti.get(i).getUuid().equals(personaggio.getUuid())) {
				posto = i;
			}
		}
		int lato = ICONA_SINISTRA.getWidth();
		int centro = width / 2;
		if (posto < 0) {
			// Chi è nel gruppo a tempo non ha un posto da spostare
			puoSpostareASinistra = false;
			puoSpostareADestra = false;
			areaSposta[0] = null;
			areaSposta[1] = null;
			Image aTempo = ImageCache.get("Con il gruppo a tempo", DoomdarkColorModel.Color.MEDIUM_GRAY);
			graphics.drawImage(aTempo, centro - aTempo.getWidth(null) / 2, y + (lato - aTempo.getHeight(null)) / 2, null);
		} else {
			puoSpostareASinistra = posto > 0;
			puoSpostareADestra = posto < permanenti.size() - 1;
			Image postoTesto = ImageCache.get("Posto " + (posto + 1) + " di " + permanenti.size(), DoomdarkColorModel.Color.MEDIUM_GRAY);
			int mezzoTesto = postoTesto.getWidth(null) / 2;
			areaSposta[0] = new Rectangle(centro - mezzoTesto - 2 * SPACING - lato, y, lato, lato);
			areaSposta[1] = new Rectangle(centro + mezzoTesto + 2 * SPACING, y, lato, lato);
			disegnaIcona(graphics, ICONA_SINISTRA, areaSposta[0], puoSpostareASinistra);
			disegnaIcona(graphics, ICONA_DESTRA, areaSposta[1], puoSpostareADestra);
			graphics.drawImage(postoTesto, centro - mezzoTesto, y + (lato - postoTesto.getHeight(null)) / 2, null);
		}
		y += lato + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

		// Conferma e annulla, solo se c'è qualcosa da confermare
		boolean modificata = isModificata();
		boolean aPosto = bozza.problemi().isEmpty();
		if (modificata) {
			int lSi = ICONA_SI.getWidth();
			areaSi = new Rectangle(centro - lSi - SPACING * 3, y, lSi, lSi);
			areaNo = new Rectangle(centro + SPACING * 3, y, lSi, lSi);
			disegnaIcona(graphics, ICONA_SI, areaSi, aPosto);
			disegnaIcona(graphics, ICONA_NO, areaNo, true);
			y += lSi + SPACING;
			y = disegnaCentrato(graphics, aPosto ? "Sì per applicare, No per tornare a prima" : "Non ancora completa: vedi a destra",
					aPosto ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED, y);
		} else {
			areaSi = null;
			areaNo = null;
		}
		y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI / 2;
		Image predefinitaTesto = ImageCache.get("Predefinita", DoomdarkColorModel.Color.LIGHT_GRAY);
		areaPredefinita = new Rectangle((width - predefinitaTesto.getWidth(null)) / 2, y, predefinitaTesto.getWidth(null),
				predefinitaTesto.getHeight(null));
		graphics.drawImage(ImageCache.get("Predefinita", areaPredefinita.contains(mouseX, mouseY) && menu == null
				? DoomdarkColorModel.Color.WHITE : DoomdarkColorModel.Color.LIGHT_GRAY), areaPredefinita.x, areaPredefinita.y, null);
	}

	private int disegnaCentrato(Graphics2D graphics, String testo, DoomdarkColorModel.Color colore, int y) {
		int larghezzaMassima = xMassimaZonaCentrale - xMinimaZonaCentrale;
		for (String riga : FontTool.split(font, testo, larghezzaMassima)) {
			Image immagine = ImageCache.get(riga, font, colore);
			graphics.drawImage(immagine, (width - immagine.getWidth(null)) / 2, y, null);
			y += fontHeight + SPACING;
		}
		return y;
	}

	private static void disegnaIcona(Graphics2D graphics, BufferedImage icona, Rectangle area, boolean abilitata) {
		java.awt.Composite precedente = graphics.getComposite();
		if (!abilitata) {
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
		}
		graphics.drawImage(icona, area.x, area.y, area.width, area.height, null);
		graphics.setComposite(precedente);
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

	private void disegnaSpiegazione(Graphics2D graphics, int indiceVoce, String titoloElemento, String testoElemento) {
		List<String> testi = new ArrayList<>();
		List<DoomdarkColorModel.Color> colori = new ArrayList<>();
		List<String> problemi = bozza.problemi();
		if (applicata.isDisabilitata()) {
			testi.add("Tattica disattivata");
			colori.add(DoomdarkColorModel.Color.RED);
			testi.add("Ha degli errori (di solito perché il gioco è cambiato dopo che l'hai scritta): finché non li correggi il personaggio usa quella predefinita. "
					+ "Correggi le voci e conferma con Sì.");
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
			testi.add("Voce " + (indiceVoce + 1));
			colori.add(DoomdarkColorModel.Color.YELLOW);
			if (voce.azioni.isEmpty()) {
				testi.add("Senza azioni: aggiungine una.");
				colori.add(DoomdarkColorModel.Color.RED);
			} else {
				for (String riga : TestoTattica.spiegazione(bozza.toVoceMD(voce))) {
					testi.add(riga);
					colori.add(DoomdarkColorModel.Color.LIGHT_GRAY);
				}
			}
		} else {
			testi.add("Le voci si valutano dall'alto: la prima che si può eseguire vince.");
			colori.add(DoomdarkColorModel.Color.LIGHT_GRAY);
			testi.add("Se nessuna si può eseguire, il personaggio attacca con le armi il primo avversario.");
			colori.add(DoomdarkColorModel.Color.LIGHT_GRAY);
			testi.add("Passa il mouse su una voce per leggere come ragiona.");
			colori.add(DoomdarkColorModel.Color.MEDIUM_GRAY);
		}
		if (!problemi.isEmpty()) {
			testi.add("Da sistemare:");
			colori.add(DoomdarkColorModel.Color.RED);
			for (String problema : problemi) {
				testi.add("- " + problema);
				colori.add(DoomdarkColorModel.Color.RED);
			}
		}
		int y = DIMENSIONE_BORDO_INTERNO + 2 * SPACING;
		int x = xMinimaZonaDestra + SPACING;
		for (int i = 0; i < testi.size(); i++) {
			for (String riga : FontTool.split(fontRiga, testi.get(i), LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO)) {
				if (y + fontRiga.getHeight() > DIMENSIONE_BORDO_INTERNO + 2 * SPACING + ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO) {
					return;
				}
				graphics.drawImage(ImageCache.get(riga, fontRiga, colori.get(i)), x, y, null);
				y += fontRiga.getHeight() + SPACING;
			}
			y += SPACING;
		}
	}

	// ============================================================== le righe

	private static final class Hover {
		static final Hover NESSUNO = new Hover(null, null);
		final Riga riga;
		final Token token;

		Hover(Riga riga, Token token) {
			this.riga = riga;
			this.token = token;
		}
	}

	private int xInternoElenco(int x) {
		return x - (xMinimaZonaSinistra + SPACING);
	}

	private int yInternoElenco(int y) {
		return y - (DIMENSIONE_BORDO_INTERNO + 2 * SPACING);
	}

	/**
	 * La riga e il pezzo sotto il punto (nelle coordinate della finestra), se ce ne sono
	 */
	private Hover trovaHover(List<Riga> righe, int x, int y) {
		int xi = xInternoElenco(x);
		int yi = yInternoElenco(y);
		if (x < 0 || xi < 0 || xi >= LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO || yi < 0 || yi >= ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO) {
			return Hover.NESSUNO;
		}
		ComponenteScorrevole<Riga> elenco = costruisciElenco(righe, Hover.NESSUNO);
		Riga riga = elenco.riferimentoTitoloAllaQuota(yi + offsetYZonaSinistra);
		if (riga == null) {
			return Hover.NESSUNO;
		}
		java.awt.Point posizione = elenco.posizioneTitolo(r -> r == riga);
		int linea = posizione == null ? 0 : Math.min(riga.linee - 1, (yi + offsetYZonaSinistra - posizione.y) / altezzaRiga);
		for (Token token : riga.pezzi) {
			if (token.linea == linea && xi >= token.x && xi < token.x + token.larghezza) {
				return new Hover(riga, token);
			}
		}
		return new Hover(riga, null);
	}

	private ComponenteScorrevole<Riga> costruisciElenco(List<Riga> righe, Hover hover) {
		ComponenteScorrevole<Riga> elenco = new ComponenteScorrevole<>(LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO, 0, 2);
		for (Riga riga : righe) {
			elenco.creaNodo(null, null, null, null, null, null, immagineDella(riga, hover), riga);
		}
		return elenco;
	}

	/**
	 * Il posto dei pezzi: le icone a destra sulla prima linea, gli altri di seguito da sinistra (dopo l'indentazione) e a capo, con un po' di
	 * rientro, quando non stanno più nella linea
	 */
	private void disponi(Riga riga) {
		int larghezzaRiga = LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO;
		spezzaIPezziTroppoLarghi(riga, larghezzaRiga - riga.profondita * PROFONDITA_INDENTAZIONE - PROFONDITA_INDENTAZIONE);
		int xDestra = larghezzaRiga;
		for (Token token : riga.pezzi) {
			token.larghezza = token.icona != null ? altezzaRiga : ImageCache.get(token.testo, fontRiga, token.colore).getWidth(null);
			if (token.adDestra) {
				xDestra -= token.larghezza + SPAZIO_FRA_PEZZI;
				token.x = xDestra;
				token.linea = 0;
			}
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
	 * Un pezzo di testo più largo di una riga intera (il nome di un'azione lungo, "Pozione della Salute grande") si divide in parole, che vanno a capo: ogni
	 * frammento fa quello che faceva il pezzo (si clicca ovunque si apre lo stesso menu).
	 */
	private void spezzaIPezziTroppoLarghi(Riga riga, int larghezzaMassima) {
		List<Token> nuovi = new ArrayList<>();
		for (Token token : riga.pezzi) {
			if (token.icona != null || token.adDestra || ImageCache.get(token.testo, fontRiga, token.colore).getWidth(null) <= larghezzaMassima
					|| !token.testo.contains(" ")) {
				nuovi.add(token);
				continue;
			}
			StringBuilder frammento = new StringBuilder();
			for (String parola : token.testo.split(" ")) {
				String prova = frammento.length() == 0 ? parola : frammento + " " + parola;
				if (frammento.length() > 0 && ImageCache.get(prova, fontRiga, token.colore).getWidth(null) > larghezzaMassima) {
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

	private BufferedImage immagineDella(Riga riga, Hover hover) {
		int larghezzaRiga = LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO;
		BufferedImage immagine = new BufferedImage(larghezzaRiga, altezzaRiga * riga.linee, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = immagine.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		for (Token token : riga.pezzi) {
			boolean sotto = hover.token == token && token.isCliccabile();
			if (token.icona != null) {
				if (!token.abilitato) {
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
				}
				int lato = altezzaRiga - (sotto ? 0 : 2);
				g2d.drawImage(token.icona, token.x + (altezzaRiga - lato) / 2, token.linea * altezzaRiga + (altezzaRiga - lato) / 2, lato, lato, null);
				g2d.setComposite(AlphaComposite.SrcOver);
			} else {
				boolean numeroDellaVoce = riga.profondita == 0 && token == riga.pezzi.get(0);
				DoomdarkColorModel.Color colore = sotto ? DoomdarkColorModel.Color.WHITE
						: riga.spenta && !numeroDellaVoce ? DoomdarkColorModel.Color.DARK_GRAY : token.colore;
				Image testo = ImageCache.get(token.testo, fontRiga, colore);
				g2d.drawImage(testo, token.x, token.linea * altezzaRiga + (altezzaRiga - testo.getHeight(null)) / 2, null);
			}
		}
		g2d.dispose();
		return immagine;
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

	private static Token icona(BufferedImage icona, Runnable azione, String aiuto, boolean abilitata) {
		return new Token(null, icona, null, ancora -> azione.run(), aiuto, true, abilitata);
	}

	/**
	 * Un pezzo che apre un menu a discesa con le scelte e le applica
	 */
	private <T> Token menu(String etichetta, List<CasellaADiscesa.Scelta<T>> scelte, T corrente, Consumer<T> alloScegliere, String aiuto) {
		// Il riquadro destro spiega la scelta attuale, o a che cosa serve il menu se non ce n'è una
		String titolo = etichetta;
		String spiegazione = aiuto;
		for (CasellaADiscesa.Scelta<T> scelta : scelte) {
			if (scelta.valore.equals(corrente) && scelta.spiegazione != null) {
				spiegazione = scelta.spiegazione;
			}
		}
		final String titoloDelMenu = titolo;
		final String spiegazioneDelMenu = spiegazione;
		return scelta(etichetta, ancora -> {
			menu = new CasellaADiscesa<>(scelte, corrente, ancora.x, ancora.sotto, ancora.sopra, width, height, alloScegliere);
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
				"Diminuisce di " + passo, false, valore > minimo));
		riga.pezzi.add(testoColorato(String.valueOf(valore), DoomdarkColorModel.Color.YELLOW)
				.spiega("Numero", "Il valore con cui si confronta. Con - e + sale e scende di " + passo + "."));
		riga.pezzi.add(new Token(" + ", null, DoomdarkColorModel.Color.LIGHT_GRAY, a -> alCambio.accept(Math.min(massimo, valore + passo)),
				"Aumenta di " + passo, false, valore < massimo));
	}

	private static <T extends Enum<T>> List<CasellaADiscesa.Scelta<T>> sceltePer(List<T> valori, java.util.function.Function<T, String> etichetta) {
		List<CasellaADiscesa.Scelta<T>> scelte = new ArrayList<>();
		for (T valore : valori) {
			scelte.add(new CasellaADiscesa.Scelta<>(etichetta.apply(valore), valore, spiegazioneDi(valore)));
		}
		return scelte;
	}

	/**
	 * Come {@link #sceltePer}, ma in ordine alfabetico: per le liste lunghe di valori (stati, classi, categorie, tipi di danno), dove si cerca a occhio
	 */
	private static <T extends Enum<T>> List<CasellaADiscesa.Scelta<T>> sceltePerInOrdineAlfabetico(List<T> valori,
			java.util.function.Function<T, String> etichetta) {
		List<CasellaADiscesa.Scelta<T>> scelte = sceltePer(valori, etichetta);
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
		nuova.pezzi.add(new Token(null, ICONA_AGGIUNGI, null, a -> bozza.aggiungiVoce(), "Aggiunge una voce in fondo", false, true));
		nuova.pezzi.add(scelta("Nuova voce", a -> bozza.aggiungiVoce(), "Aggiunge una voce in fondo"));
		righe.add(nuova);
		Riga riserva = new Riga(0, -1);
		// Parola per parola, perché vada a capo: un pezzo solo più largo della riga verrebbe tagliato
		for (String parola : ((voci.size() + 1) + ". Attacco fisico sul primo avversario").split(" ")) {
			riserva.pezzi.add(testoColorato(parola, DoomdarkColorModel.Color.DARK_GRAY)
					.spiega("Attacco di riserva", "Attacca con le armi il primo avversario disponibile. Scatta se nessun'altra azione precedente può essere intrapresa."));
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
				voce.attiva ? "Click: disattiva la voce (viene saltata)" : "Click: riattiva la voce", false, true)
				.spiega("Voce " + (i + 1), "Le voci si valutano dall'alto in basso: la prima che si può eseguire vince. Il numero è verde se la voce è attiva, rosso se è disattivata e viene saltata: click per cambiare."));
		riga.pezzi.add(new Token(nomeDellaVoce(voce, i), null, DoomdarkColorModel.Color.WHITE, a -> clickSulNome(voce, i),
				voce.chiusa ? "Click: apre la tattica. Doppio click: la rinomina" : "Click: ripiega la tattica. Doppio click: la rinomina", false, true)
				.spiega("Nome della tattica", "Click sul nome per ripiegare la tattica e risparmiare posto, o aprirla di nuovo. Doppio click per cambiarne il nome."));
		riga.pezzi.add(icona(ICONA_ELIMINA, () -> bozza.rimuoviVoce(i), "Toglie la voce", true));
		riga.pezzi.add(icona(ICONA_GIU, () -> bozza.spostaVoce(i, 1), "Sposta la voce in basso", i < voci.size() - 1));
		riga.pezzi.add(icona(ICONA_SU, () -> bozza.spostaVoce(i, -1), "Sposta la voce in alto", i > 0));
		righe.add(riga);
		if (voce.chiusa) {
			return;
		}

		// Il corpo, sotto il nome: su chi agisce la voce e come sceglie
		riga = new Riga(1, i);
		riga.pezzi.add(testo("su").spiega("Su chi", "La rosa dei candidati e come se ne sceglie uno: prima la rosa, poi i filtri la restringono, infine l'ordinamento ne sceglie uno."));
		List<TipoRosa> rose = BozzaTattica.roseAmmesse(voce);
		if (voce.rosa == null || rose.size() != 1) {
			riga.pezzi.add(menu(voce.rosa == null ? "(scegli su chi)" : voce.rosa.getDescrizione(), sceltePer(rose, TipoRosa::getDescrizione), voce.rosa,
					rosa -> {
						voce.rosa = rosa;
						voce.filtri.clear();
						if (voce.ordinamento == null || !BozzaTattica.ordinamentiAmmessi(rosa).contains(voce.ordinamento.getTipoOrdinamento())) {
							voce.ordinamento = BozzaTattica.ordinamentoPredefinito(TipoOrdinamento.PRIMO);
						}
					}, "Su chi agisce la voce"));
		} else {
			// Una sola scelta possibile: non è un menu, ma dice perché
			riga.pezzi.add(new Token(voce.rosa.getDescrizione(), null, DoomdarkColorModel.Color.LIGHT_GRAY, null,
					"Le azioni della voce agiscono solo su: " + voce.rosa.getDescrizione().toLowerCase(Locale.ROOT) + ". Per cambiare, cambia le azioni.", false, true));
		}
		if (haOrdinamento(voce)) {
			riga.pezzi.add(testo(":"));
			riga.pezzi.add(menu(TestoTattica.ordinamento(voce.ordinamento), sceltePer(BozzaTattica.ordinamentiAmmessi(voce.rosa),
					t -> t.getDescrizione()), voce.ordinamento.getTipoOrdinamento(),
					tipo -> voce.ordinamento = BozzaTattica.ordinamentoPredefinito(tipo), "Come sceglie fra i candidati"));
			if (voce.ordinamento.getTipoDanno() != null) {
				riga.pezzi.add(menu(voce.ordinamento.getTipoDanno().getNome(), sceltePerInOrdineAlfabetico(Arrays.asList(TipoDanno.values()), TipoDanno::getNome),
						voce.ordinamento.getTipoDanno(), danno -> voce.ordinamento = OrdinamentoMD.di(voce.ordinamento.getTipoOrdinamento(), danno),
						"Il tipo di danno"));
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
		}, 1, i, "con");

		// Le condizioni
		for (int c = 0; c < voce.condizioni.size(); c++) {
			aggiungiCondizione(righe, voce, c, i);
		}
		Riga nuovaCondizione = new Riga(1, i);
		nuovaCondizione.pezzi.add(testo("se").spiega("Condizione", "Le condizioni decidono se la voce vale in questo momento: devono essere tutte vere. Non riguardano un personaggio ma lo scontro (il round, le probabilità, quanti candidati ci sono)."));
		nuovaCondizione.pezzi.add(menu("(aggiungi una condizione)", sceltePer(BozzaTattica.predicatiAmmessi(), t -> t.getDescrizione()), null,
				tipo -> voce.condizioni.add(BozzaTattica.predicatoPredefinito(tipo)), "Una condizione che deve valere per eseguire la voce"));
		righe.add(nuovaCondizione);

		// Le azioni
		for (int a = 0; a < voce.azioni.size(); a++) {
			aggiungiAzione(righe, voce, a, i);
		}
		Riga nuovaAzione = new Riga(1, i);
		nuovaAzione.pezzi.add(testo("fai").spiega("Azione", "Le azioni sono ciò che il personaggio fa. Si prova la prima che si può fare sul bersaglio (serve la MAGIA, la pergamena, la pozione...); se nessuna si può fare, si passa alla voce dopo."));
		nuovaAzione.pezzi.add(menu("(aggiungi un'azione)", sceltePer(BozzaTattica.azioniAmmesse(voce, azioniPossibili), t -> t.getDescrizione()), null,
				tipo -> bozza.aggiungiAzione(voce, tipo), "Un'azione: la prima che si può eseguire vince"));
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
				List<CasellaADiscesa.Scelta<Boolean>> modi = Arrays.asList(
						new CasellaADiscesa.Scelta<>(parola, false, "Tiene i candidati per cui il filtro è vero."),
						new CasellaADiscesa.Scelta<>("senza", true, "Tiene i candidati per cui il filtro è falso."));
				riga.pezzi.add(menu(filtro.isNegato() ? "senza" : parola, modi, filtro.isNegato(),
						senza -> sostituisci(lista, indice, senza == filtro.isNegato() ? filtro : filtro.negato()), "Con o senza il filtro"));
			} else {
				riga.pezzi.add(testo(parola).spiega("Filtro", "I filtri restringono i candidati della rosa: restano solo quelli per cui sono tutti veri. "
						+ "Passa il mouse su un filtro per capire che cosa controlla."));
			}
			riga.pezzi.add(menu(filtro.getTipoFiltro().getDescrizione(), sceltePer(BozzaTattica.filtriAmmessi(lista.rosa()), t -> t.getDescrizione()),
					filtro.getTipoFiltro(), tipo -> {
						FiltroMD nuovo = BozzaTattica.filtroPredefinito(tipo);
						// Il "senza" resta solo dove ha senso (non nei filtri numerici)
						sostituisci(lista, indice, filtro.isNegato() && BozzaTattica.isNegabile(tipo) ? nuovo.negato() : nuovo);
					}, "Il tipo di filtro"));
			aggiungiParametriFiltro(riga, lista, indice, filtro);
			riga.pezzi.add(icona(ICONA_ELIMINA, () -> {
				List<FiltroMD> nuova = new ArrayList<>(lista.get());
				nuova.remove(indice);
				lista.set(nuova);
			}, "Toglie il filtro", true));
			righe.add(riga);
		}
		Riga nuovo = new Riga(profondita, indiceVoce);
		nuovo.pezzi.add(testo(parola).spiega("Filtro", "I filtri restringono i candidati della rosa: restano solo quelli per cui sono tutti veri. Passa il mouse su un filtro per capire che cosa controlla."));
		nuovo.pezzi.add(menu("(aggiungi un filtro)", sceltePer(BozzaTattica.filtriAmmessi(lista.rosa()), t -> t.getDescrizione()), null, tipo -> {
			List<FiltroMD> nuova = new ArrayList<>(lista.get());
			nuova.add(BozzaTattica.filtroPredefinito(tipo));
			lista.set(nuova);
		}, "Restringe i candidati"));
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
					r -> sostituisci(lista, indice, BozzaTattica.conRelazione(filtro, r.tipoConfronto, r.negato)), "Il confronto"));
			if (parametro == TipoParametro.CONFRONTO_E_NUMERO) {
				aggiungiNumero(riga, filtro.getNumero(), BozzaTattica.passo(filtro.getTipoFiltro()), 0, BozzaTattica.massimo(filtro.getTipoFiltro()),
						n -> sostituisci(lista, indice, BozzaTattica.conNumero(filtro, n)));
			}
		} else if (parametro == TipoParametro.STATO) {
			riga.pezzi.add(menu(filtro.getTipoEffettoDiStato().getDescrizione(), sceltePerInOrdineAlfabetico(Arrays.asList(TipoEffettoDiStato.values()),
					TipoEffettoDiStato::getDescrizione), filtro.getTipoEffettoDiStato(),
					s -> sostituisci(lista, indice, BozzaTattica.conParametro(filtro, s)), "Lo stato"));
		} else if (parametro == TipoParametro.CLASSE) {
			riga.pezzi.add(menu(etichettaClasse(filtro.getTipoPersonaggio()), sceltePerInOrdineAlfabetico(Arrays.asList(TipoPersonaggio.values()),
					DisplayableCanvasTattiche::etichettaClasse), filtro.getTipoPersonaggio(),
					c -> sostituisci(lista, indice, BozzaTattica.conParametro(filtro, c)), "La classe (il ruolo: Mago e Maga sono lo stesso)"));
		} else if (parametro == TipoParametro.CATEGORIA) {
			riga.pezzi.add(menu(filtro.getCategoriaPersonaggio().getNome(), sceltePerInOrdineAlfabetico(Arrays.asList(CategoriaPersonaggio.values()),
					CategoriaPersonaggio::getNome), filtro.getCategoriaPersonaggio(),
					c -> sostituisci(lista, indice, BozzaTattica.conParametro(filtro, c)), "La categoria di creatura"));
		} else if (parametro == TipoParametro.TIPO_DANNO) {
			riga.pezzi.add(menu(filtro.getTipoDanno().getNome(), sceltePerInOrdineAlfabetico(Arrays.asList(TipoDanno.values()), TipoDanno::getNome),
					filtro.getTipoDanno(), d -> sostituisci(lista, indice, BozzaTattica.conParametro(filtro, d)), "Il tipo di danno"));
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
	private static List<CasellaADiscesa.Scelta<Relazione>> relazioni(TipoConfronto attuale, boolean attualeNegato) {
		List<CasellaADiscesa.Scelta<Relazione>> scelte = new ArrayList<>();
		for (TipoConfronto tipoConfronto : new TipoConfronto[]{TipoConfronto.MINORE, TipoConfronto.UGUALE, TipoConfronto.MAGGIORE}) {
			scelte.add(new CasellaADiscesa.Scelta<>(TestoTattica.confronto(tipoConfronto), new Relazione(tipoConfronto, false),
					TestoTattica.spiega(tipoConfronto)));
		}
		if (attualeNegato) {
			scelte.add(new CasellaADiscesa.Scelta<>(TestoTattica.relazione(attuale, true), new Relazione(attuale, true), TestoTattica.spiega(attuale, true)));
		}
		return scelte;
	}

	private void aggiungiCondizione(List<Riga> righe, BozzaTattica.Voce voce, int c, int indiceVoce) {
		PredicatoMD predicato = voce.condizioni.get(c);
		Riga riga = new Riga(1, indiceVoce);
		riga.pezzi.add(testo("se").spiega("Condizione", "Le condizioni decidono se la voce vale in questo momento: devono essere tutte vere. Non riguardano un personaggio ma lo scontro (il round, le probabilità, quanti candidati ci sono)."));
		riga.pezzi.add(menu(predicato.getTipoPredicato().getDescrizione(), sceltePer(BozzaTattica.predicatiAmmessi(), t -> t.getDescrizione()),
				predicato.getTipoPredicato(), tipo -> {
					voce.condizioni.set(c, BozzaTattica.predicatoPredefinito(tipo));
				}, "Il tipo di condizione"));
		if (predicato.getTipoPredicato() == TipoPredicato.QUANTI) {
			riga.pezzi.add(menu(predicato.getTipoRosa().getDescrizione(), sceltePer(rosePerQuanti(predicato.getTipoRosa()), TipoRosa::getDescrizione), predicato.getTipoRosa(),
					r -> voce.condizioni.set(c, BozzaTattica.conRosa(predicato, r)), "Da quali candidati si conta"));
		}
		if (predicato.getTipoPredicato() != TipoPredicato.PROBABILITA) {
			riga.pezzi.add(menu(TestoTattica.relazione(predicato.getTipoConfronto(), predicato.isNegato()),
					relazioni(predicato.getTipoConfronto(), predicato.isNegato()), new Relazione(predicato.getTipoConfronto(), predicato.isNegato()),
					r -> voce.condizioni.set(c, BozzaTattica.conRelazione(predicato, r.tipoConfronto, r.negato)), "Il confronto"));
		}
		aggiungiNumero(riga, predicato.getNumero(), BozzaTattica.passo(predicato.getTipoPredicato()), 0, BozzaTattica.massimo(predicato.getTipoPredicato()),
				n -> voce.condizioni.set(c, BozzaTattica.conNumero(predicato, n)));
		if (predicato.getTipoPredicato() == TipoPredicato.PROBABILITA) {
			riga.pezzi.add(testo("%"));
		}
		riga.pezzi.add(icona(ICONA_ELIMINA, () -> voce.condizioni.remove(c), "Toglie la condizione", true));
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
			}, 2, indiceVoce, "con");
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
		riga.pezzi.add(testo("fai").spiega("Azione", "Le azioni sono ciò che il personaggio fa. Si prova la prima che si può fare sul bersaglio (serve la MAGIA, la pergamena, la pozione...); se nessuna si può fare, si passa alla voce dopo."));
		riga.pezzi.add(menu(azione.getTipo().getDescrizione(), sceltePer(BozzaTattica.azioniAmmesse(voce, azioniPossibili), t -> t.getDescrizione()),
				azione.getTipo(), tipo -> bozza.sostituisciAzione(voce, a, tipo), "L'azione"));
		if (azione.getCriterio() != null) {
			riga.pezzi.add(menu(TestoTattica.criterio(azione.getCriterio()), sceltePer(Arrays.asList(CriterioIncantesimo.values()),
					TestoTattica::criterio), azione.getCriterio(),
					c -> voce.azioni.set(a, BozzaTattica.conCriterio(azione, c)), "Come sceglie quale incantesimo lanciare"));
		}
		if (azione.getArmaDi() != null) {
			riga.pezzi.add(testo("di").spiega("Arma", "Il tipo di danno dell'arma da impugnare."));
			riga.pezzi.add(menu(azione.getArmaDi().getNome(), sceltePerInOrdineAlfabetico(Arrays.asList(TipoDanno.values()), TipoDanno::getNome), azione.getArmaDi(),
					d -> voce.azioni.set(a, AzioneMD.cambiaArma(d)), "Il tipo di danno dell'arma da impugnare"));
		}
		if (BozzaTattica.haSoloSePiuForte(azione)) {
			riga.pezzi.add(new Token(azione.isSoloSePiuForteDellAttacco() ? "solo se più forte" : "(sempre)", null,
					azione.isSoloSePiuForteDellAttacco() ? DoomdarkColorModel.Color.YELLOW : DoomdarkColorModel.Color.DARK_GRAY,
					x -> voce.azioni.set(a, BozzaTattica.commutaSoloSePiuForte(azione)),
					"Click: lo fa solo se fa più danno dell'attacco con le armi", false, true)
					.spiega("Solo se più forte", "Se acceso, l'azione si fa solo quando rende più dell'attacco con le armi sullo stesso bersaglio; altrimenti si passa all'azione dopo."));
		}
		riga.pezzi.add(icona(ICONA_ELIMINA, () -> voce.azioni.remove(a), "Toglie l'azione", true));
		righe.add(riga);
	}

	// ============================================================== il mouse

	@Override
	public boolean gestisceDoppioClick() {
		return false;
	}

	@Override
	public int passiPerScattoDellaRotella() {
		// il menu a discesa ha già un passo suo
		return menu != null && menu.isAperta() ? 1 : super.passiPerScattoDellaRotella();
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (menu != null && menu.isAperta()) {
			menu.rotella(numeroRotazioni, movimentoRotella == MovimentoRotella.SU);
			return;
		}
		super.processaRotella(x, y, numeroRotazioni, movimentoRotella);
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO || personaggio == null) {
			return;
		}
		if (menu != null && menu.isAperta()) {
			menu.click(x, y);
			if (!menu.isAperta()) {
				menu = null;
			}
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
		ComponenteScorrevole<Riga> elenco = costruisciElenco(righe, Hover.NESSUNO);
		java.awt.Point posizione = elenco.posizioneTitolo(r -> r == hover.riga);
		int yRiga = DIMENSIONE_BORDO_INTERNO + 2 * SPACING + (posizione == null ? 0 : posizione.y) - offsetYZonaSinistra;
		int yLinea = yRiga + hover.token.linea * altezzaRiga;
		hover.token.azione.accept(new Ancora(xMinimaZonaSinistra + SPACING + hover.token.x, yLinea + altezzaRiga, yLinea, hover.riga.voce));
	}

	/**
	 * Per i test: come il click sul primo pezzo con quel testo dell'elenco, senza passare dalle coordinate.
	 *
	 * @return false se non c'è
	 */
	boolean attivaPezzo(String testo) {
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
	 * Per i test: se c'è un menu aperto
	 */
	boolean isMenuAperto() {
		return menu != null && menu.isAperta();
	}

	BozzaTattica getBozza() {
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
		if (areaSposta[0] != null && areaSposta[0].contains(x, y)) {
			if (puoSpostareASinistra) {
				BusEventi.pubblica(new ComandoSpostaPersonaggio(personaggio.getUuid(), false));
			}
			return true;
		}
		if (areaSposta[1] != null && areaSposta[1].contains(x, y)) {
			if (puoSpostareADestra) {
				BusEventi.pubblica(new ComandoSpostaPersonaggio(personaggio.getUuid(), true));
			}
			return true;
		}
		if (areaSi != null && areaSi.contains(x, y)) {
			if (bozza.problemi().isEmpty()) {
				BusEventi.pubblica(new ComandoImpostaTattica(personaggio.getUuid(), bozza.toTattica()));
			}
			return true;
		}
		if (areaNo != null && areaNo.contains(x, y)) {
			annullaLaRichiestaDiTesto();
			bozza = BozzaTattica.da(applicata);
			return true;
		}
		if (areaPredefinita != null && areaPredefinita.contains(x, y)) {
			annullaLaRichiestaDiTesto();
			bozza = BozzaTattica.da(predefinita);
			return true;
		}
		return false;
	}
}
