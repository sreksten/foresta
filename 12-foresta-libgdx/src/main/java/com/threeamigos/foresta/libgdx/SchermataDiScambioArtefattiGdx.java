package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;
import com.threeamigos.foresta.ui.schermate.ScambioDegliArtefatti;
import com.threeamigos.foresta.ui.schermate.SchermataDiScambio;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.util.Collection;

/**
 * Una schermata di scambio di artefatti in libGDX: le due cornici dell'inventario con le loro intestazioni e le due liste di artefatti (l'albero, la sua
 * evidenziazione sotto il mouse e lo scorrimento a mano sono del modulo comune: {@link SchermataDiScambio}, {@link ScambioDegliArtefatti}, gli stessi di
 * Swing). Il personaggio al centro (inventario, negozi, incantatore) e lo sprite delle monete non ci sono ancora.
 */
public final class SchermataDiScambioArtefattiGdx extends SchermataDiScambio {

	private static final String CORNICE = "fondi/CorniceInventario.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx medio;
	private final ElencoScorrevoleGdx elencoGdx;
	private final ScambioDegliArtefatti scambioDegliArtefatti;
	private final float zoom = (float) LivelloDiZoom.valore();
	private final int larghezzaCornice;
	private final String intestazioneSinistra;
	private final String intestazioneDestra;
	private final String etichettaDelCentro;
	private final TipoPersonaggio mestiereAlCentro;
	private final DisegnoDiContenutoGdx disegno;

	public SchermataDiScambioArtefattiGdx(int width, int height, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio,
										  String intestazioneSinistra, String intestazioneDestra, TipoPersonaggio mestiereAlCentro, String etichettaDelCentro) {
		super(width, height, vistaPartita, immagini.larghezza(CORNICE, (float) LivelloDiZoom.valore()), immagini.altezza(CORNICE, (float) LivelloDiZoom.valore()),
				misure(immagini));
		this.immagini = immagini;
		this.medio = medio;
		this.larghezzaCornice = immagini.larghezza(CORNICE, zoom);
		this.intestazioneSinistra = intestazioneSinistra;
		this.intestazioneDestra = intestazioneDestra;
		this.etichettaDelCentro = etichettaDelCentro;
		this.mestiereAlCentro = mestiereAlCentro;
		this.disegno = new DisegnoDiContenutoGdx(immagini, piccolo, medio);
		this.elencoGdx = new ElencoScorrevoleGdx(immagini, piccolo, medio);
		this.scambioDegliArtefatti = new ScambioDegliArtefatti(this, vistaPartita, misure(immagini), new ScambioDegliArtefatti.Personalizzazioni() {
			@Override
			public String aiutoDoppioClickSinistra() {
				return "Doppio click: sposta a destra";
			}

			@Override
			public String aiutoDoppioClickDestra() {
				return "Doppio click: sposta a sinistra";
			}

			@Override
			public boolean processaClickPersonaggio(int x, int y, Tasto tasto) {
				return false;
			}

			@Override
			public boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto) {
				return false;
			}

			@Override
			public void aggiungiSpriteLocale(DescrizioneSpriteATempo sprite) {
				// Gli sprite locali arriveranno con il collegamento del motore
			}
		});
	}

	private static MisureDelleImmagini misure(ImmaginiGdx immagini) {
		return new MisureDelleImmagini() {
			@Override
			public Misura personaggio(TipoPersonaggio classe) {
				float zoom = (float) LivelloDiZoom.valore();
				return new Misura(immagini.larghezza(NomiDelleImmagini.personaggio(classe), zoom), immagini.altezza(NomiDelleImmagini.personaggio(classe), zoom));
			}

			@Override
			public Misura immagine(String risorsa) {
				return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
			}
		};
	}

	@Override
	public TipoPersonaggio getMestiereAlCentro() {
		return mestiereAlCentro;
	}

	@Override
	public String getEtichettaMestiere() {
		return etichettaDelCentro;
	}

	public void impostaScambio(VistaScambio scambio) {
		scambioDegliArtefatti.impostaScambio(scambio);
	}

	public ScambioDegliArtefatti getScambioDegliArtefatti() {
		return scambioDegliArtefatti;
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		scambioDegliArtefatti.processaClick(x, y, tasto);
	}

	@Override
	public void processaDoppioClick(int x, int y, Tasto tasto) {
		scambioDegliArtefatti.processaDoppioClick(x, y, tasto);
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		int xCorniceDestra = width - SPACING - larghezzaCornice;
		immagini.disegna(batch, CORNICE, SPACING, SPACING, zoom);
		immagini.disegna(batch, CORNICE, xCorniceDestra, SPACING, zoom);
		if (scambioDegliArtefatti.getScambio() == null) {
			return;
		}
		disegnaElenco(batch, camera, viewport, scambioDegliArtefatti.getArtefattiDellaParteAttiva(), xMinimaZonaSinistra, scorrimentoSinistra, true);
		disegnaElenco(batch, camera, viewport, scambioDegliArtefatti.getArtefattiDellaParteRemota(), xMinimaZonaDestra, scorrimentoDestra, false);
		intestazione(batch, intestazioneSinistra, SPACING + larghezzaCornice / 2);
		intestazione(batch, intestazioneDestra, width - SPACING - larghezzaCornice / 2);
		disegno.disegna(batch, colonnaDelNegoziante(mestiereAlCentro), 0, 0, 1f);
	}

	private void intestazione(Batch batch, String testo, int xCentro) {
		int x = xCentro - medio.larghezza(testo) / 2;
		medio.disegna(batch, testo, x + 2, SPACING + 2, DoomdarkColorModel.Color.BLACK);
		medio.disegna(batch, testo, x, SPACING, DoomdarkColorModel.Color.LIGHT_GRAY);
	}

	private void disegnaElenco(Batch batch, Camera camera, Viewport viewport, Collection<? extends VistaArtefatto> artefatti, int x, ScorrimentoSuUnAsse scorrimento,
							   boolean parteAttiva) {
		int altezza = altezzaDisponibileInRiquadroInventario();
		// L'elenco può essere cambiato dall'ultimo scorrimento: il limite si aggiorna e lo scorrimento resta dentro
		VistaArtefatto evidenziato = scambioDegliArtefatti.artefattoSottoIlMouse(artefatti, x, (int) Math.round(scorrimento.getScorrimento()), parteAttiva);
		ElencoScorrevole<VistaArtefatto> elenco = scambioDegliArtefatti.costruisciElenco(artefatti, evidenziato, parteAttiva);
		int massimo = elenco.getMassimoScorrimento(altezza);
		scorrimento.imposta(massimo > 0, massimo);
		scorrimento.aggiorna();
		int offset = elenco.limitaOffset(altezza, (int) Math.round(scorrimento.getScorrimento()));
		elencoGdx.disegna(batch, camera, viewport, elenco, new Area(x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, larghezzaDisponibileInRiquadroInventario(), altezza),
				offset);
	}
}
