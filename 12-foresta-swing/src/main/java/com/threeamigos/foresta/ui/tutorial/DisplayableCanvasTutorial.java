package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.immagini.ImmaginiAwt;
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Il fumetto del tutorial in Swing: la nuvola con il testo della pagina, il pollice (avanti, «ho capito») e l'annulla (spegne il tutorial) in
 * basso, la coda che punta al bersaglio e il bordo del bersaglio che lampeggia. Dove sta lo decide {@link DisposizioneDelFumetto}, l'intestazione
 * {@link IntestazioneDelFumetto}, i pulsanti {@link PulsantiDelFumetto}, il testo e le pagine {@link TutorDelGioco}; qui c'è solo il disegno.
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasTutorial {

	private static final Color COLORE_DEL_BORDO = new Color(DisposizioneDelFumetto.COLORE_DEL_BORDO_RGB);

	private final SchermoDelGioco<?, ?, ?, ?> schermo;
	private final PulsantiDelFumetto pulsanti;
	// La nuvola costruita, finché il testo e la larghezza non cambiano
	private BufferedImage nuvola;
	private String chiaveDellaNuvola;
	private int larghezzaDelTesto;
	private int altezzaDelTesto;

	public DisplayableCanvasTutorial(SchermoDelGioco<?, ?, ?, ?> schermo) {
		this.schermo = schermo;
		this.pulsanti = new PulsantiDelFumetto(schermo);
	}

	/**
	 * Disegna il suggerimento aperto, se c'è.
	 */
	public void disegna(Graphics2D g, int larghezzaSchermo, int altezzaSchermo) {
		Optional<FumettoDelTutorial> aperto = schermo.getTutor().getFumetto();
		if (!aperto.isPresent()) {
			pulsanti.nascondi();
			return;
		}
		FumettoDelTutorial fumetto = aperto.get();
		Area bersaglio = schermo.getAreaDelBersaglio(fumetto.getBersaglio());
		if (schermo.getTutor().isBordoAcceso()) {
			disegnaIlBordo(g, schermo.getAreeDaBordare(fumetto.getBersaglio()));
		}

		BufferedImage iconaPollice = IconeAwt.icona(TipoIcona.SI);
		BufferedImage iconaAnnulla = IconeAwt.icona(TipoIcona.ANNULLA);
		int lato = Math.max(iconaPollice.getHeight(), iconaPollice.getWidth());
		costruisciLaNuvola(fumetto, DisposizioneDelFumetto.larghezzaMassima(larghezzaSchermo));
		int larghezza = DisposizioneDelFumetto.larghezza(larghezzaDelTesto, lato);
		int altezza = DisposizioneDelFumetto.altezza(altezzaDelTesto, lato);
		Punto posizione = DisposizioneDelFumetto.posizioneFissa(larghezza, altezza, larghezzaSchermo, altezzaSchermo);

		// La coda sotto la nuvola, perché il bordo della nuvola la copra
		Punto punta = DisposizioneDelFumetto.puntaVerso(bersaglio);
		SpriteFumetto.disegnaPunta(g, posizione.x, posizione.y + altezza, larghezza, altezza, punta.x, punta.y);
		g.setColor(Color.WHITE);
		g.fillRoundRect(posizione.x, posizione.y, larghezza, altezza, DescrizioneFumetto.SMUSSAMENTO_BORDO, DescrizioneFumetto.SMUSSAMENTO_BORDO);
		g.drawImage(nuvola, posizione.x + (larghezza - nuvola.getWidth()) / 2, posizione.y, null);

		pulsanti.posiziona(posizione, larghezza, altezza, lato);
		g.drawImage(iconaPollice, pulsanti.getPollice().getX(), pulsanti.getPollice().getY(), null);
		g.drawImage(iconaAnnulla, pulsanti.getAnnulla().getX(), pulsanti.getAnnulla().getY(), null);
	}

	private static void disegnaIlBordo(Graphics2D g, List<Area> bersagli) {
		g.setColor(COLORE_DEL_BORDO);
		for (Area segmento : DisposizioneDelFumetto.segmentiDelBordo(bersagli)) {
			g.fillRect(segmento.getX(), segmento.getY(), segmento.getLarghezza(), segmento.getAltezza());
		}
	}

	/**
	 * Il testo della pagina su più righe, senza la nuvola intorno (la nuvola la disegna {@link #disegna} sotto di lui, grande quanto serve).
	 */
	private void costruisciLaNuvola(FumettoDelTutorial fumetto, int larghezzaMassima) {
		String testo = fumetto.getTesto();
		Optional<String> titolo = fumetto.getTitolo();
		List<String> risorseDelleIcone = fumetto.getRisorseDelleIcone();
		String chiave = testo + '|' + larghezzaMassima + '|' + titolo.orElse("") + '|' + risorseDelleIcone;
		if (chiave.equals(chiaveDellaNuvola)) {
			return;
		}
		DoomdarkFont font = DoomdarkFont.medio();
		List<BufferedImage> icone = new ArrayList<>();
		for (String risorsa : risorseDelleIcone) {
			BufferedImage icona = BufferedImageBuilder.buildBufferedImage(risorsa, LivelloDiZoom.valore());
			// La tessera di un luogo ha parti trasparenti: come sulla mappa, sta sul nero
			icone.add(NomiDelleImmagini.isTesseraDellaMappa(risorsa) ? ImmaginiAwt.suFondoNero(icona) : icona);
		}
		int[] larghezze = new int[icone.size()];
		int[] altezze = new int[icone.size()];
		for (int i = 0; i < icone.size(); i++) {
			larghezze[i] = icone.get(i).getWidth();
			altezze[i] = icone.get(i).getHeight();
		}
		IntestazioneDelFumetto intestazione = new IntestazioneDelFumetto(larghezze, altezze, titolo.isPresent(),
				titolo.map(t -> FontTool.getWidth(font, t)).orElse(0), font.getHeight());
		DescrizioneFumetto dati = new DescrizioneFumetto(testo, larghezzaMassima, font, intestazione.getLarghezza(), intestazione.getAltezza());
		nuvola = new BufferedImage(dati.getLarghezza(), dati.getAltezza(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = nuvola.createGraphics();
		for (int i = 0; i < icone.size(); i++) {
			graphics.drawImage(icone.get(i), intestazione.getXDellIcona(i), intestazione.getYDellIcona(i), null);
		}
		if (titolo.isPresent()) {
			graphics.drawImage(ImageCache.get(titolo.get(), font, DoomdarkColorModel.Color.BLACK), intestazione.getXDelTitolo(), intestazione.getYDelTitolo(), null);
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
		return pulsanti.suiPulsanti(x, y);
	}

	/**
	 * Un clic: se è sul pollice o sull'annulla lo esegue.
	 *
	 * @return vero se il clic era su un pulsante (e quindi non va oltre)
	 */
	public boolean clic(int x, int y) {
		return pulsanti.premi(x, y);
	}
}
