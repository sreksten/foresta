package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.componenti.ElementoDelElenco;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ValoreDiDueTesti;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;

import java.util.List;

/**
 * Il disegno in libGDX di un {@link ElencoScorrevole} (il modello è del modulo comune, lo stesso di Swing): la parte che si vede nello spazio dato, scorsa
 * di quanto dice l'offset, ritagliata, con le frecce su e giù se c'è altro. Le immagini che il modello dà come oggetti opachi sono nomi di risorse (icone,
 * separatori; con lo zoom se vanno ingrandite) o {@link ValoreDiDueTesti}.
 */
public final class ElencoScorrevoleGdx {

	private final ImmaginiGdx immagini;
	private final TestoGdx piccolo;
	private final TestoGdx medio;
	private final float zoom = (float) LivelloDiZoom.valore();

	public ElencoScorrevoleGdx(ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio) {
		this.immagini = immagini;
		this.piccolo = piccolo;
		this.medio = medio;
	}

	private TestoGdx testoDi(com.threeamigos.foresta.ui.testo.DoomdarkFont font) {
		return font == piccolo.getFont() ? piccolo : medio;
	}

	/**
	 * Disegna l'elenco nell'area (coordinate dello schermo), scorso di quell'offset (già nei limiti: vedi {@link ElencoScorrevole#limitaOffset})
	 */
	public void disegna(Batch batch, Camera camera, Viewport viewport, ElencoScorrevole<?> elenco, Area area, int offset) {
		List<ElementoDelElenco> elementi = elenco.elementiVisibili(area.getAltezza(), offset);
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
		int x0 = area.getX();
		int y0 = area.getY() - offset;
		for (ElementoDelElenco e : elementi) {
			int x = x0 + e.getX();
			int y = y0 + e.getY();
			switch (e.getTipo()) {
				case ICONA:
				case VALORE_IMMAGINE:
					disegnaImmagine(batch, e.getImmagine(), x, y);
					break;
				case VALORE_TESTO:
					testoDi(e.getFont()).disegna(batch, e.getTesto(), x, y, e.getColore());
					break;
				default:
					int yRiga = y;
					for (String riga : e.getRighe()) {
						testoDi(e.getFont()).disegna(batch, riga, x, yRiga, e.getColore());
						yRiga += e.getPassoRiga();
					}
					break;
			}
		}
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}
		int spazio = Finestra.SPACING;
		if (elenco.isFrecciaSuVisibile(offset)) {
			int w = immagini.larghezza(NomiDelleImmagini.FRECCIA_SU, 1f);
			immagini.disegna(batch, NomiDelleImmagini.FRECCIA_SU, area.getX() + area.getLarghezza() - w - spazio, area.getY() + spazio, 1f);
		}
		if (elenco.isFrecciaGiuVisibile(area.getAltezza(), offset)) {
			int w = immagini.larghezza(NomiDelleImmagini.FRECCIA_GIU, 1f);
			int h = immagini.altezza(NomiDelleImmagini.FRECCIA_GIU, 1f);
			immagini.disegna(batch, NomiDelleImmagini.FRECCIA_GIU, area.getX() + area.getLarghezza() - w - spazio, area.getY() + area.getAltezza() - h - spazio, 1f);
		}
	}

	private void disegnaImmagine(Batch batch, Object oggetto, int x, int y) {
		if (oggetto instanceof ValoreDiDueTesti) {
			ValoreDiDueTesti v = (ValoreDiDueTesti) oggetto;
			TestoGdx testo = testoDi(v.getFont());
			testo.disegna(batch, v.getPrimo(), x, y, v.getColoreDelPrimo());
			testo.disegna(batch, v.getSecondo(), x + v.getXDelSecondo(), y, v.getColoreDelSecondo());
			return;
		}
		String risorsa = (String) oggetto;
		immagini.disegna(batch, risorsa, x, y, NomiDelleImmagini.conZoom(risorsa) ? zoom : 1f);
	}
}
