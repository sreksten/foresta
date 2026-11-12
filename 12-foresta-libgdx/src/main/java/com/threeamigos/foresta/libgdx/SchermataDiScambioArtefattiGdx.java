package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.tipi.TipoBottega;
import com.threeamigos.foresta.tipi.TipoNegozio;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaBancoDiLavoro;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.schermate.ColonnaDelPersonaggio;
import com.threeamigos.foresta.ui.schermate.ColoriDellIncantatore;
import com.threeamigos.foresta.ui.schermate.ColoriDellInventario;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;
import com.threeamigos.foresta.ui.schermate.RigheDellIncantatore;
import com.threeamigos.foresta.ui.schermate.ScambioDegliArtefatti;
import com.threeamigos.foresta.ui.schermate.SchermataDiScambio;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Una schermata di scambio di artefatti in libGDX: le due cornici dell'inventario con le loro intestazioni e le due liste di artefatti (l'albero, la sua
 * evidenziazione sotto il mouse e lo scorrimento a mano sono del modulo comune: {@link SchermataDiScambio}, {@link ScambioDegliArtefatti}, gli stessi di
 * Swing). Il personaggio al centro e gli sprite delle monete spese o ricavate ci sono.
 */
public final class SchermataDiScambioArtefattiGdx extends SchermataDiScambio {

	private static final String CORNICE = "fondi/CorniceInventario.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx medio;
	private final ElencoScorrevoleGdx elencoGdx;
	private final ScambioDegliArtefatti scambioDegliArtefatti;
	private final float zoom = (float) LivelloDiZoom.valore();
	// Intestazioni, negoziante e aiuto del doppio click; quella del commerciante cambia secondo il negozio (vedi impostaNegozio)
	private TipoBottega bottega;
	private final DisegnoDiContenutoGdx disegno;
	private final CartiglioGdx cartiglio;
	// Se c'è, la bottega è quella dell'incantatore: sotto le monete vanno le righe della fusione
	private VistaBancoDiLavoro banco;
	// Gli sprite che restano confinati nella schermata (le monete spese o ricavate)
	private final List<SpriteATempoGdx> spriteLocali = new ArrayList<>();
	private final ColoriDellInventario coloriDellInventario = new ColoriDellInventario(() -> this.colonna.getPersonaggio());
	private final ColoriDellIncantatore coloriDellIncantatore = new ColoriDellIncantatore(() -> banco);
	// La colonna centrale dell'inventario (il personaggio con le sue caratteristiche), nel comune; senza personaggio la colonna è quella del negoziante
	private final ColonnaDelPersonaggio colonna = new ColonnaDelPersonaggio(this);

	public SchermataDiScambioArtefattiGdx(int width, int height, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio,
										  TipoBottega bottega) {
		super(width, height, vistaPartita, immagini.larghezza(CORNICE, (float) LivelloDiZoom.valore()), immagini.altezza(CORNICE, (float) LivelloDiZoom.valore()),
				misure(immagini));
		this.immagini = immagini;
		this.medio = medio;
		this.bottega = bottega;
		this.disegno = new DisegnoDiContenutoGdx(immagini, piccolo, medio);
		this.cartiglio = new CartiglioGdx(immagini, medio);
		this.elencoGdx = new ElencoScorrevoleGdx(immagini, piccolo, medio);
		this.scambioDegliArtefatti = new ScambioDegliArtefatti(this, vistaPartita, misure(immagini), new ScambioDegliArtefatti.Personalizzazioni() {
			// Con un banco di lavoro la bottega è quella dell'incantatore e ha i suoi colori; con un personaggio è l'inventario; altrimenti quelli di suo
			@Override
			public DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
				return banco != null ? coloriDellIncantatore.coloreLivello(artefatto, parteAttiva) : coloriDellInventario.coloreLivello(artefatto, parteAttiva);
			}

			@Override
			public DoomdarkColorModel.Color coloreEffetto(VistaArtefatto artefatto, Object effetto, boolean parteAttiva, DoomdarkColorModel.Color predefinito) {
				return banco == null ? predefinito : coloriDellIncantatore.coloreEffetto(artefatto, effetto, parteAttiva, predefinito);
			}

			@Override
			public String aiutoDoppioClickSinistra() {
				return bottega.getAiutoDoppioClickSinistra();
			}

			@Override
			public String aiutoDoppioClickDestra() {
				return bottega.getAiutoDoppioClickDestra();
			}

			@Override
			public List<String> righeAiutoColonnaPersonaggio() {
				return colonna.getPersonaggio() == null ? new ArrayList<>() : colonna.righeAiuto();
			}

			@Override
			public boolean processaClickPersonaggio(int x, int y, Tasto tasto) {
				return colonna.getPersonaggio() != null && colonna.processaClick(x, y);
			}

			@Override
			public boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto) {
				return colonna.getPersonaggio() != null && colonna.processaDoppioClick(x, y);
			}

			@Override
			public void aggiungiSpriteLocale(DescrizioneSpriteATempo sprite) {
				spriteLocali.add(new SpriteATempoGdx(sprite, immagini, medio));
			}
		});
	}

	/**
	 * La schermata del commerciante: armaiolo o venditore di pergamene, con l'intestazione, il personaggio e il mestiere del negozio.
	 */
	public void impostaNegozio(TipoNegozio negozio) {
		bottega = TipoBottega.del(negozio);
	}

	static MisureDelleImmagini misure(ImmaginiGdx immagini) {
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
		return bottega.getMestiere();
	}

	@Override
	public String getEtichettaMestiere() {
		return bottega.getEtichettaDelMestiere();
	}

	public void impostaScambio(VistaScambio scambio) {
		scambioDegliArtefatti.impostaScambio(scambio);
	}

	/** Quando la schermata si apre gli sprite della volta scorsa, anche se non finiti, non ricompaiono. */
	@Override
	public void apri() {
		super.apri();
		spriteLocali.clear();
	}

	/** Fa comparire sulla riga delle monete la variazione (negativa se spese), come dopo un acquisto o una vendita. */
	public void mostraVariazioneDelleMonete(int variazione) {
		String moneta = NomiDelleImmagini.SPRITE_MONETA;
		spriteLocali.add(new SpriteATempoGdx(DescrizioneSpriteATempo.conVariazione(moneta, misure(immagini).immagine(moneta), variazione, font,
				xMassimaZonaCentrale, yRigaMonete(), false, variazione < 0 ? "Monete spese" : "Monete acquisite"), immagini, medio));
	}

	/** Il banco di lavoro (e lo scambio) dell'incantatore. */
	public void impostaBanco(VistaBancoDiLavoro banco) {
		impostaScambio(banco);
		this.banco = banco;
	}

	public void impostaPersonaggio(VistaPersonaggio personaggio) {
		colonna.impostaPersonaggio(personaggio);
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
		int xCorniceDestra = width - SPACING - getLarghezzaCornice();
		immagini.disegnaAPezzi(batch, CORNICE, SPACING, SPACING, zoom, getFasceDellaCornice(), getColonneDellaCornice());
		immagini.disegnaAPezzi(batch, CORNICE, xCorniceDestra, SPACING, zoom, getFasceDellaCornice(), getColonneDellaCornice());
		if (scambioDegliArtefatti.getScambio() == null) {
			return;
		}
		disegnaElenco(batch, camera, viewport, scambioDegliArtefatti.getArtefattiDellaParteAttiva(), xMinimaZonaSinistra, scorrimentoSinistra, true);
		disegnaElenco(batch, camera, viewport, scambioDegliArtefatti.getArtefattiDellaParteRemota(), xMinimaZonaDestra, scorrimentoDestra, false);
		ContenutoDiColonna testata = testataADestra();
		if (testata != null) {
			disegno.disegna(batch, testata, 0, 0, 1f);
		}
		intestazione(batch, titoloDellaCornice(bottega.getChiaveIntestazioneSinistra()), SPACING + getLarghezzaCornice() / 2);
		intestazione(batch, titoloDellaCornice(bottega.getChiaveIntestazioneDestra()), width - SPACING - getLarghezzaCornice() / 2);
		if (colonna.getPersonaggio() == null) {
			disegno.disegna(batch, colonnaDelNegoziante(bottega.getMestiere(), banco == null ? Collections.emptyList() : RigheDellIncantatore.righe(banco)), 0, 0, 1f);
		} else {
			disegnaColonnaDelPersonaggio(batch, camera, viewport);
		}
		for (SpriteATempoGdx sprite : new ArrayList<>(spriteLocali)) {
			sprite.anima(batch, camera, viewport);
			if (!sprite.isAttivo()) {
				spriteLocali.remove(sprite);
			}
		}
		// Per ultimo, sopra il resto
		if (vistaPartita.isAiutoAbilitato() && mouseX >= 0) {
			cartiglio.disegnaAccantoAlMouse(batch, scambioDegliArtefatti.righeAiuto(), mouseX, mouseY, width, height);
		}
	}

	/** La testata del personaggio e sotto l'elenco delle sue caratteristiche, che scorre. */
	private void disegnaColonnaDelPersonaggio(Batch batch, Camera camera, Viewport viewport) {
		disegno.disegna(batch, colonna.testata(), 0, 0, 1f);
		int y = colonna.getYAttributi();
		int altezza = height - y;
		ElencoScorrevole<Object> elenco = colonna.elenco();
		int massimo = elenco.getMassimoScorrimento(altezza);
		scorrimentoCentrale.imposta(massimo > 0, massimo);
		scorrimentoCentrale.aggiorna();
		int offset = elenco.limitaOffset(altezza, offsetYZonaCentrale());
		elencoGdx.disegna(batch, camera, viewport, elenco, new Area(colonna.getXAttributi(), y, colonna.getLarghezzaAttributi(), altezza), offset);
	}

	private void intestazione(Batch batch, String testo, int xCentro) {
		int x = xCentro - medio.larghezza(testo) / 2;
		medio.disegna(batch, testo, x + 2, SPACING + 2, DoomdarkColorModel.Color.BLACK);
		medio.disegna(batch, testo, x, SPACING, DoomdarkColorModel.Color.LIGHT_GRAY);
	}

	private void disegnaElenco(Batch batch, Camera camera, Viewport viewport, Collection<? extends VistaArtefatto> artefatti, int x, ScorrimentoSuUnAsse scorrimento,
							   boolean parteAttiva) {
		int altezza = altezzaElenco(x);
		// L'elenco può essere cambiato dall'ultimo scorrimento: il limite si aggiorna e lo scorrimento resta dentro
		VistaArtefatto evidenziato = scambioDegliArtefatti.artefattoSottoIlMouse(artefatti, x, (int) Math.round(scorrimento.getScorrimento()), parteAttiva);
		ElencoScorrevole<VistaArtefatto> elenco = scambioDegliArtefatti.costruisciElenco(artefatti, evidenziato, parteAttiva);
		int massimo = elenco.getMassimoScorrimento(altezza);
		scorrimento.imposta(massimo > 0, massimo);
		scorrimento.aggiorna();
		int offset = elenco.limitaOffset(altezza, (int) Math.round(scorrimento.getScorrimento()));
		elencoGdx.disegna(batch, camera, viewport, elenco, new Area(x + SPACING, yElenco(x), larghezzaDisponibileInRiquadroInventario(), altezza),
				offset);
	}
}
