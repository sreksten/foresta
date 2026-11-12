package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.ui.Area;

import java.util.ArrayList;
import java.util.List;

/**
 * La barra delle icone come il Dock di macOS, senza disegno: l'icona sotto il cursore (o sotto il dito) si ingrandisce, le vicine un po' meno man mano che si
 * allontanano, e la fila si allarga spingendo di lato le altre. Scelte, pagine e frecce sono quelle di {@link BarraIcone}, compreso lo scorrimento in pixel, il trascinamento e
 * l'inerzia; cambia solo la geometria delle icone, ingrandite attorno al cursore. Se le icone scorrono, le frecce ai capi restano della loro misura e l'effetto
 * vale per le icone della fascia di mezzo, ritagliate ai suoi bordi.
 * <p>
 * Funziona nei due orientamenti: l'ingrandimento si calcola lungo l'asse della barra e le icone crescono verso il contenuto, cioè verso l'alto nella barra
 * orizzontale (in fondo allo schermo) e verso sinistra in quella verticale (a destra; verso destra se la barra sta a sinistra). Le icone ingrandite escono quindi dalla barra: la loro geometria si
 * ricalcola a ogni movimento del puntatore, e la barra reclama come suoi anche i punti fuori dal suo rettangolo che cadono su un'icona (vedi
 * {@link #contiene}), così si possono cliccare e il cursore che ci sale sopra non spegne l'effetto. Entrando e uscendo dalla barra l'effetto non scatta ma
 * cresce e si sgonfia in {@link #DURATA_TRANSIZIONE_NANOS}; dentro la barra le icone seguono subito il cursore. Col dito (vedi
 * {@link #setAreaSensibileLarga}) non c'è un cursore che resta sopra la barra: le icone si ingrandiscono finché il dito è appoggiato e si sgonfiano quando
 * si alza.
 */
public class BarraIconeDock extends BarraIcone {

	/**
	 * L'icona sotto il cursore diventa 1,5 volte più grande: le icone sono disegni 32x32 raddoppiati, quindi a 96x96 i pixel restano netti (3x)
	 */
	private static final double SCALA_MASSIMA = 1.5d;
	/**
	 * Quanto ci mette l'effetto a crescere entrando nella barra e a sgonfiarsi uscendo
	 */
	protected static final long DURATA_TRANSIZIONE_NANOS = 120_000_000L;

	/**
	 * Oltre questa distanza dal cursore, in pixel, un'icona resta della sua misura
	 */
	private final double raggio;

	/**
	 * Se il cursore è sulla barra, e dove era l'ultima volta lungo la barra: uscendo le icone si sgonfiano attorno a quel punto
	 */
	private boolean cursoreDentro;
	private int ultimaPosizioneSullAsse = -1;
	/**
	 * L'intensità dell'effetto (0 spento, 1 pieno) all'ultimo ingresso o uscita, e quando è avvenuto: da lì l'intensità va verso 1 o verso 0. Letto anche dal
	 * thread di animazione (vedi inTransizione).
	 */
	private double intensitaAlCambio;
	private volatile long istanteCambio = System.nanoTime() - DURATA_TRANSIZIONE_NANOS;

	/**
	 * Dove sono disegnate adesso le icone, nello stesso ordine di iconeVisibili (coordinate relative alla barra; la y in orizzontale e la x in verticale sono
	 * negative per la parte che esce)
	 */
	private final List<Area> rettangoli = new ArrayList<>();

	public BarraIconeDock(Orientamento orientamento, int offsetX, int offsetY, int larghezza, int altezza, int larghezzaIcona, int altezzaIcona,
						  VistaPartita vistaPartita) {
		super(orientamento, offsetX, offsetY, larghezza, altezza, larghezzaIcona, altezzaIcona, vistaPartita);
		this.raggio = 2.0d * (orientamento == Orientamento.ORIZZONTALE ? larghezzaIcona : altezzaIcona);
	}

	/**
	 * La scala di un'icona secondo la distanza del suo centro dal cursore: a campana, da SCALA_MASSIMA sotto il cursore a 1 a distanza del raggio.
	 */
	private double scala(double distanza) {
		if (distanza >= raggio) {
			return 1.0d;
		}
		double coseno = Math.cos(Math.PI / 2.0d * distanza / raggio);
		return 1.0d + (SCALA_MASSIMA - 1.0d) * coseno * coseno;
	}

	/**
	 * L'intensità dell'effetto adesso: dall'intensità all'ultimo cambio verso 1 (cursore dentro) o 0 (fuori), con una curva che parte e arriva dolce.
	 */
	private double intensita() {
		double avanzamento = Math.min(1.0d, (System.nanoTime() - istanteCambio) / (double) DURATA_TRANSIZIONE_NANOS);
		double dolce = avanzamento * avanzamento * (3.0d - 2.0d * avanzamento);
		double obiettivo = cursoreDentro ? 1.0d : 0.0d;
		return intensitaAlCambio + (obiettivo - intensitaAlCambio) * dolce;
	}

	private void cambia(boolean dentro) {
		intensitaAlCambio = intensita();
		cursoreDentro = dentro;
		istanteCambio = System.nanoTime();
	}

	@Override
	public boolean inTransizione() {
		return System.nanoTime() - istanteCambio < DURATA_TRANSIZIONE_NANOS || super.inTransizione();
	}

	@Override
	public void processaMovimento(int x, int y) {
		if (!cursoreDentro) {
			cambia(true);
		}
		ultimaPosizioneSullAsse = asse(x, y);
		super.processaMovimento(x, y);
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (!cursoreDentro) {
			cambia(true);
		}
		ultimaPosizioneSullAsse = asse(x, y);
		super.processaPressione(x, y, tasto);
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		// Trascinando si può uscire dalla barra senza che lo sappia: l'effetto segue il puntatore finché il tasto è premuto
		if (!cursoreDentro) {
			cambia(true);
		}
		ultimaPosizioneSullAsse = asse(x, y);
		super.processaTrascinamento(x, y);
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		super.processaRilascio(x, y, tasto);
		// Col dito, alzato il dito non c'è più niente sopra la barra: le icone si sgonfiano (col mouse il cursore resta lì, e l'effetto con lui)
		if (isAreaSensibileLarga() && cursoreDentro) {
			ultimaPosizioneSullAsse = asse(x, y);
			cambia(false);
		}
	}

	@Override
	public void processaUscita(int x, int y) {
		if (cursoreDentro) {
			cambia(false);
		}
		super.processaUscita(x, y);
	}

	/**
	 * Ricalcola i rettangoli delle icone dalla posizione del cursore lungo la barra. Le scale si misurano sulle posizioni di base (quelle della barra
	 * classica), non su quelle ingrandite, altrimenti ogni movimento cambierebbe le distanze che lo determinano. La fila allargata si distribuisce attorno al
	 * cursore in proporzione a dove cade nella fila di base, così l'icona sotto il cursore gli resta sotto; le icone poggiano sul bordo dello schermo e crescono
	 * verso il contenuto (in su in orizzontale, a sinistra in verticale). Gli intervalli fra le icone di base si conservano. Se le icone scorrono la fila non
	 * si tiene dentro la barra: sporge ai capi e la ritaglia il disegno.
	 */
	private void ricalcola() {
		rettangoli.clear();
		int quante = iconeVisibili.size();
		if (quante == 0) {
			return;
		}
		// Cursore e intensità di sempre; uscendo, finché l'effetto si sgonfia, conta l'ultima posizione del cursore
		int cursore = cursoreDentro && mouseX >= 0 ? asse(mouseX, mouseY) : ultimaPosizioneSullAsse;
		double intensita = intensita();
		boolean attivo = cursore >= 0 && intensita > 0.0d;
		boolean scorre = isScorrevole();
		double[] scale = new double[quante];
		double lunghezzaTotale = 0.0d;
		for (int i = 0; i < quante; i++) {
			Area base = iconeVisibili.get(i).rettangolo;
			scale[i] = attivo ? 1.0d + (scala(Math.abs(cursore - (inizio(base) + lunghezza(base) / 2.0d))) - 1.0d) * intensita : 1.0d;
			lunghezzaTotale += lunghezza(base) * scale[i] + (i > 0 ? intervalloDiBase(i) : 0.0d);
		}
		Area primo = iconeVisibili.get(0).rettangolo;
		Area ultimo = iconeVisibili.get(quante - 1).rettangolo;
		double inizioBase = inizio(primo);
		double lunghezzaBase = inizio(ultimo) + lunghezza(ultimo) - inizioBase;
		double quota = attivo ? Math.max(0.0d, Math.min(1.0d, (cursore - inizioBase) / lunghezzaBase)) : 0.5d;
		double posizione = inizioBase - (lunghezzaTotale - lunghezzaBase) * quota;
		if (!scorre) {
			posizione = Math.max(0.0d, Math.min(lunghezzaUtile() - lunghezzaTotale, posizione));
		}
		for (int i = 0; i < quante; i++) {
			Area base = iconeVisibili.get(i).rettangolo;
			if (i > 0) {
				posizione += intervalloDiBase(i);
			}
			int w = (int) Math.round(base.getLarghezza() * scale[i]);
			int h = (int) Math.round(base.getAltezza() * scale[i]);
			int p = (int) Math.round(posizione);
			// In verticale le icone poggiano sul bordo dello schermo: quello destro, o il sinistro con la barra a sinistra
			rettangoli.add(orizzontale
					? new Area(p, base.getY() + base.getAltezza() - h, w, h)
					: new Area(aSinistra ? base.getX() : base.getX() + base.getLarghezza() - w, p, w, h));
			posizione += lunghezza(base) * scale[i];
		}
	}

	/** Dove comincia il rettangolo lungo la barra */
	private int inizio(Area rettangolo) {
		return orizzontale ? rettangolo.getX() : rettangolo.getY();
	}

	/** Quanto è lungo il rettangolo lungo la barra */
	private int lunghezza(Area rettangolo) {
		return orizzontale ? rettangolo.getLarghezza() : rettangolo.getAltezza();
	}

	/**
	 * Lo spazio, nella fila di base, fra l'icona con questo indice e la precedente
	 */
	private double intervalloDiBase(int indice) {
		Area precedente = iconeVisibili.get(indice - 1).rettangolo;
		return inizio(iconeVisibili.get(indice).rettangolo) - (inizio(precedente) + lunghezza(precedente));
	}

	@Override
	protected void preparaGeometria() {
		ricalcola();
	}

	@Override
	public Area rettangoloDisegnato(int indice) {
		return indice < rettangoli.size() ? rettangoli.get(indice) : super.rettangoloDisegnato(indice);
	}

	@Override
	public boolean contiene(int x, int y) {
		if (super.contiene(x, y)) {
			return true;
		}
		ricalcola();
		for (int i = 0; i < rettangoli.size(); i++) {
			if (rettangoli.get(i).contiene(x, y) && nellaFascia(x, y)) {
				return true;
			}
		}
		return false;
	}
}
