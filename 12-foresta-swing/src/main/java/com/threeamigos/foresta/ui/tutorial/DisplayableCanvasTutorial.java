package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.SchermoDelGioco;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.IconeAwt;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.sprite.SpriteFumetto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Il fumetto del tutorial in Swing: la nuvola con il testo della pagina, il pollice (avanti, «ho capito») e l'annulla (spegne il tutorial) in
 * basso, la coda che punta al bersaglio e il bordo del bersaglio che lampeggia. Dove sta lo decide {@link DisposizioneDelFumetto}, il testo e le
 * pagine {@link TutorDelGioco}; qui c'è solo il disegno e il clic sui due pulsanti.
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasTutorial {

	private static final Color COLORE_DEL_BORDO = new Color(255, 215, 0);
	private static final int SPESSORE_DEL_BORDO = 3;
	/** Fra l'icona e il titolo, e fra la riga dell'intestazione e il testo */
	private static final int SPAZIO_FRA_ICONA_E_TITOLO = 8;
	private static final int SPAZIO_SOTTO_L_INTESTAZIONE = 4;

	private final SchermoDelGioco<?, ?, ?, ?> schermo;
	// La nuvola costruita, finché il testo e la larghezza non cambiano
	private BufferedImage nuvola;
	private String chiaveDellaNuvola;
	private int larghezzaDelTesto;
	private int altezzaDelTesto;
	// Dove stanno i due pulsanti nell'ultimo fotogramma disegnato, per il clic
	private volatile Area pollice;
	private volatile Area annulla;

	public DisplayableCanvasTutorial(SchermoDelGioco<?, ?, ?, ?> schermo) {
		this.schermo = schermo;
	}

	/**
	 * Disegna il suggerimento aperto, se c'è.
	 */
	public void disegna(Graphics2D g, int larghezzaSchermo, int altezzaSchermo) {
		Optional<FumettoDelTutorial> aperto = schermo.getTutor().getFumetto();
		if (!aperto.isPresent()) {
			pollice = null;
			annulla = null;
			return;
		}
		FumettoDelTutorial fumetto = aperto.get();
		Area bersaglio = schermo.getAreaDelBersaglio(fumetto.getBersaglio());
		if (schermo.getTutor().isBordoAcceso()) {
			disegnaIlBordo(g, bersaglio);
		}

		BufferedImage iconaPollice = IconeAwt.icona(TipoIcona.SI);
		BufferedImage iconaAnnulla = IconeAwt.icona(TipoIcona.ANNULLA);
		int lato = Math.max(iconaPollice.getHeight(), iconaPollice.getWidth());
		costruisciLaNuvola(fumetto, larghezzaMassima(larghezzaSchermo));
		int larghezza = DisposizioneDelFumetto.larghezza(larghezzaDelTesto, lato);
		int altezza = DisposizioneDelFumetto.altezza(altezzaDelTesto, lato);
		Punto posizione = DisposizioneDelFumetto.posizioneFissa(larghezza, altezza, larghezzaSchermo, altezzaSchermo);

		// La coda sotto la nuvola, perché il bordo della nuvola la copra
		Punto punta = DisposizioneDelFumetto.puntaVerso(bersaglio);
		SpriteFumetto.disegnaPunta(g, posizione.x, posizione.y + altezza, larghezza, altezza, punta.x, punta.y);
		g.setColor(Color.WHITE);
		g.fillRoundRect(posizione.x, posizione.y, larghezza, altezza, DescrizioneFumetto.SMUSSAMENTO_BORDO, DescrizioneFumetto.SMUSSAMENTO_BORDO);
		g.drawImage(nuvola, posizione.x + (larghezza - nuvola.getWidth()) / 2, posizione.y, null);

		Area areaPollice = DisposizioneDelFumetto.pollice(posizione, larghezza, altezza, lato);
		Area areaAnnulla = DisposizioneDelFumetto.annulla(posizione, larghezza, altezza, lato);
		g.drawImage(iconaPollice, areaPollice.getX(), areaPollice.getY(), null);
		g.drawImage(iconaAnnulla, areaAnnulla.getX(), areaAnnulla.getY(), null);
		pollice = areaPollice;
		annulla = areaAnnulla;
	}

	private static int larghezzaMassima(int larghezzaSchermo) {
		return Math.min(larghezzaSchermo - 4 * DisposizioneDelFumetto.MARGINE, Math.max(larghezzaSchermo / 3, 300));
	}

	private void disegnaIlBordo(Graphics2D g, Area bersaglio) {
		java.awt.Stroke precedente = g.getStroke();
		g.setColor(COLORE_DEL_BORDO);
		g.setStroke(new BasicStroke(SPESSORE_DEL_BORDO));
		g.drawRect(bersaglio.getX() - SPESSORE_DEL_BORDO, bersaglio.getY() - SPESSORE_DEL_BORDO,
				bersaglio.getLarghezza() + 2 * SPESSORE_DEL_BORDO, bersaglio.getAltezza() + 2 * SPESSORE_DEL_BORDO);
		g.setStroke(precedente);
	}

	/**
	 * Il testo della pagina su più righe, senza la nuvola intorno (la nuvola la disegna {@link #disegna} sotto di lui, grande quanto serve).
	 */
	private void costruisciLaNuvola(FumettoDelTutorial fumetto, int larghezzaMassima) {
		String testo = fumetto.getTesto();
		Optional<String> titolo = fumetto.getTitolo();
		java.util.List<String> risorseDelleIcone = fumetto.getRisorseDelleIcone();
		String chiave = testo + '|' + larghezzaMassima + '|' + titolo.orElse("") + '|' + risorseDelleIcone;
		if (chiave.equals(chiaveDellaNuvola)) {
			return;
		}
		DoomdarkFont font = DoomdarkFont.medio();
		// L'intestazione, se c'è: l'icona (la tessera che quel luogo ha sulla mappa) e il titolo accanto, in cima; il testo scende sotto di lei
		java.util.List<BufferedImage> icone = new java.util.ArrayList<>();
		for (String risorsa : risorseDelleIcone) {
			icone.add(BufferedImageBuilder.buildBufferedImage(risorsa, LivelloDiZoom.valore()));
		}
		java.awt.Image immagineDelTitolo = titolo.isPresent() ? ImageCache.get(titolo.get(), font, DoomdarkColorModel.Color.BLACK) : null;
		int larghezzaIcone = 0;
		int altezzaIcone = 0;
		for (BufferedImage icona : icone) {
			larghezzaIcone += icona.getWidth() + (larghezzaIcone > 0 ? SPAZIO_FRA_ICONA_E_TITOLO : 0);
			altezzaIcone = Math.max(altezzaIcone, icona.getHeight());
		}
		int larghezzaTitolo = immagineDelTitolo == null ? 0 : Math.max(1, FontTool.getWidth(font, titolo.get()));
		boolean conIntestazione = !icone.isEmpty() || immagineDelTitolo != null;
		int altezzaIntestazione = !conIntestazione ? 0 : Math.max(altezzaIcone, immagineDelTitolo == null ? 0 : font.getHeight()) + SPAZIO_SOTTO_L_INTESTAZIONE;
		int larghezzaIntestazione = larghezzaIcone + (!icone.isEmpty() && immagineDelTitolo != null ? SPAZIO_FRA_ICONA_E_TITOLO : 0) + larghezzaTitolo;
		DescrizioneFumetto dati = new DescrizioneFumetto(testo, larghezzaMassima, font, larghezzaIntestazione, altezzaIntestazione);
		nuvola = new BufferedImage(dati.getLarghezza(), dati.getAltezza(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = nuvola.createGraphics();
		int altezzaDellaRiga = altezzaIntestazione - SPAZIO_SOTTO_L_INTESTAZIONE;
		int x = DescrizioneFumetto.SMUSSAMENTO_BORDO;
		for (BufferedImage icona : icone) {
			graphics.drawImage(icona, x, DescrizioneFumetto.SMUSSAMENTO_BORDO + (altezzaDellaRiga - icona.getHeight()) / 2, null);
			x += icona.getWidth() + SPAZIO_FRA_ICONA_E_TITOLO;
		}
		if (immagineDelTitolo != null) {
			graphics.drawImage(immagineDelTitolo, x, DescrizioneFumetto.SMUSSAMENTO_BORDO + (altezzaDellaRiga - font.getHeight()) / 2, null);
		}
		for (DescrizioneFumetto.Riga riga : dati.getRighe()) {
			graphics.drawImage(ImageCache.get(riga.getTesto(), font, DoomdarkColorModel.Color.BLACK), riga.getX(), riga.getY(), null);
		}
		graphics.dispose();
		larghezzaDelTesto = dati.getLarghezza();
		altezzaDelTesto = dati.getAltezza();
		chiaveDellaNuvola = chiave;
	}

	/**
	 * Se il punto sta su uno dei due pulsanti (anche solo premuto o rilasciato: il clic e quel che lo precede non devono arrivare al resto).
	 */
	public boolean suiPulsanti(int x, int y) {
		Area p = pollice;
		Area a = annulla;
		return (p != null && p.contiene(x, y)) || (a != null && a.contiene(x, y));
	}

	/**
	 * Un clic: se è sul pollice o sull'annulla lo esegue.
	 *
	 * @return vero se il clic era su un pulsante (e quindi non va oltre)
	 */
	public boolean clic(int x, int y) {
		Area p = pollice;
		Area a = annulla;
		if (p != null && p.contiene(x, y)) {
			schermo.pollicePremuto();
			return true;
		}
		if (a != null && a.contiene(x, y)) {
			schermo.annullaPremuto();
			return true;
		}
		return false;
	}
}
