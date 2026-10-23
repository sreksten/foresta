package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.componenti.ScorrimentoVerticale;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;
import com.threeamigos.foresta.ui.testo.TestoGrande;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.List;

/**
 * Il bestiario, che si apre dall'inventario (comando MOSTRA_BESTIARIO) e si chiude con ANNULLA: a sinistra l'elenco delle voci, ognuna con la sua icona e il
 * suo nome (o "???" finché non si è sbloccata), a destra la scheda della voce scelta con un clic (vedi ImmagineBestiario). L'elenco e la scheda scorrono
 * a parte, con la rotella sul pannello su cui si trova il mouse o con le frecce.
 */
public class DisplayableCanvasBestiario implements Finestra {

	/**
	 * Di quanti pixel scorre un pannello a ogni passo della rotella (vedi {@link Finestra#PASSI_PER_SCATTO_DELLE_LISTE}).
	 */
	private static final int PASSO_SCORRIMENTO = 4;
	/**
	 * Quanti passi vale un clic su una freccia.
	 */
	private static final int SCATTI_PER_CLIC = 10;
	private static final int MARGINE = 30;
	private static final int QUOTA_TITOLO = 20;
	private static final int SPAZIO_SOTTO_IL_TITOLO = 20;
	private static final int SPAZIO_FRA_I_PANNELLI = 30;
	// Lascia libere le frecce a destra di un pannello
	private static final int SPAZIO_PER_LE_FRECCE = 24;
	private static final float OPACITA_OMBRA = 0.4f;
	private static final Color COLORE_SEPARATORE = new Color(90, 90, 90);

	private final int width;
	private final int height;
	private final VistaPartita vistaPartita;
	private final Rectangle areaElenco;
	private final Rectangle areaScheda;
	private final List<TipoPersonaggio> voci = TipoPersonaggio.getVociDelBestiario();

	private int scelta;
	private ScorrimentoVerticale elenco;
	private ScorrimentoVerticale scheda;

	public DisplayableCanvasBestiario(int width, int height, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.width = width;
		this.height = height;
		int cima = QUOTA_TITOLO + TestoGrande.ALTEZZA_RIGA + SPAZIO_SOTTO_IL_TITOLO;
		int larghezzaElenco = (width - 2 * MARGINE - SPAZIO_FRA_I_PANNELLI) * 3 / 10;
		areaElenco = new Rectangle(MARGINE, cima, larghezzaElenco, height - cima - MARGINE);
		int xScheda = MARGINE + larghezzaElenco + SPAZIO_FRA_I_PANNELLI;
		areaScheda = new Rectangle(xScheda, cima, width - MARGINE - xScheda, height - cima - MARGINE);
	}

	/**
	 * Prepara il bestiario com'è adesso, con la prima voce scelta e le due colonne dalla cima.
	 */
	public void apri() {
		scelta = 0;
		costruisciElenco(0);
		costruisciScheda();
	}

	private void costruisciElenco(int scorrimentoMantenuto) {
		elenco = new ScorrimentoVerticale(ImmagineBestiario.elenco(vistaPartita, voci, scelta, areaElenco.width - SPAZIO_PER_LE_FRECCE), areaElenco);
		elenco.scorri(scorrimentoMantenuto);
	}

	private void costruisciScheda() {
		scheda = new ScorrimentoVerticale(ImmagineBestiario.scheda(vistaPartita, voci.get(scelta), areaScheda.width - SPAZIO_PER_LE_FRECCE), areaScheda);
	}

	/**
	 * Disegna il titolo, quante voci si sono sbloccate, l'elenco e la scheda.
	 */
	public void disegna(Graphics2D graphics) {
		// L'ombra del drago dietro, più tenue che nella pagina dei trofei: qui ci sono due colonne di testo da leggere
		Composite normale = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITA_OMBRA));
		graphics.drawImage(ImageCache.ombraDelDrago, (width - ImageCache.ombraDelDrago.getWidth()) >> 1,
				(height - ImageCache.ombraDelDrago.getHeight()) >> 1, null);
		graphics.setComposite(normale);
		String titolo = TestoGrande.normalizza("bestiario");
		TestoGrande.disegnaRiga(graphics, titolo, (width - TestoGrande.larghezza(titolo)) >> 1, QUOTA_TITOLO);
		long sbloccate = voci.stream().filter(vistaPartita::isVoceDelBestiarioSbloccata).count();
		Image quante = ImageCache.get(sbloccate + "/" + voci.size(), DoomdarkFontMedium.getInstance(), DoomdarkColorModel.Color.MEDIUM_GRAY);
		graphics.drawImage(quante, width - MARGINE - quante.getWidth(null), QUOTA_TITOLO + 6, null);
		graphics.setColor(COLORE_SEPARATORE);
		int xSeparatore = areaElenco.x + areaElenco.width + SPAZIO_FRA_I_PANNELLI / 2;
		graphics.drawLine(xSeparatore, areaElenco.y, xSeparatore, areaElenco.y + areaElenco.height);
		if (elenco != null) {
			elenco.disegna(graphics, 1);
			elenco.disegnaFrecce(graphics);
		}
		if (scheda != null) {
			scheda.disegna(graphics, 1);
			scheda.disegnaFrecce(graphics);
		}
	}

	@Override
	public int passiPerScattoDellaRotella() {
		return PASSI_PER_SCATTO_DELLE_LISTE;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		ScorrimentoVerticale daScorrere = x < areaScheda.x - SPAZIO_FRA_I_PANNELLI / 2 ? elenco : scheda;
		if (daScorrere != null) {
			daScorrere.scorri((movimentoRotella == MovimentoRotella.SU ? -1 : 1) * numeroRotazioni * PASSO_SCORRIMENTO);
		}
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		if (elenco == null || scheda == null) {
			return;
		}
		if (elenco.isSullaFrecciaSu(x, y)) {
			elenco.scorri(-SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (elenco.isSullaFrecciaGiu(x, y)) {
			elenco.scorri(SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (scheda.isSullaFrecciaSu(x, y)) {
			scheda.scorri(-SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (scheda.isSullaFrecciaGiu(x, y)) {
			scheda.scorri(SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (elenco.contiene(x, y)) {
			scegli((y - elenco.getCima() + elenco.getScorrimento()) / ImmagineBestiario.ALTEZZA_RIGA_ELENCO);
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
	void selezionaVoce(TipoPersonaggio classe) {
		int riga = voci.indexOf(classe.getRuolo());
		if (riga < 0) {
			return;
		}
		scegli(riga);
		int sopra = riga * ImmagineBestiario.ALTEZZA_RIGA_ELENCO;
		int sotto = sopra + ImmagineBestiario.ALTEZZA_RIGA_ELENCO;
		int scorrimento = elenco.getScorrimento();
		if (sopra < scorrimento) {
			elenco.scorri(sopra - scorrimento);
		} else if (sotto > scorrimento + areaElenco.height) {
			elenco.scorri(sotto - scorrimento - areaElenco.height);
		}
	}

	/**
	 * La voce scelta, per i test
	 */
	TipoPersonaggio getVoceScelta() {
		return voci.get(scelta);
	}
}
