package com.threeamigos.foresta.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * La barra delle icone come il Dock di macOS: l'icona sotto il cursore si ingrandisce, le vicine un po' meno man
 * mano che si allontanano, e la fila si allarga spingendo di lato le altre. Scelte, pagine e frecce sono quelle di
 * DisplayableCanvasBarraIcone; cambiano solo il disegno e il modo di trovare l'icona sotto il cursore.
 * <p>
 * Le icone ingrandite escono sopra la barra: la loro geometria si ricalcola a ogni movimento del mouse, e la barra
 * reclama come suoi anche i punti fuori dal suo rettangolo che cadono su un'icona (vedi contiene), così si possono
 * cliccare e il cursore che ci sale sopra non spegne l'effetto. Entrando e uscendo dalla barra l'effetto non scatta
 * ma cresce e si sgonfia in DURATA_TRANSIZIONE_NANOS; dentro la barra le icone seguono subito il cursore. Solo in
 * orientamento orizzontale.
 */
class DisplayableCanvasBarraIconeDock extends DisplayableCanvasBarraIcone {

	/**
	 * L'icona sotto il cursore diventa 1,5 volte più grande: le icone sono disegni 31x32 raddoppiati, quindi a 93x96
	 * i pixel restano netti (3x)
	 */
	private static final double SCALA_MASSIMA = 1.5d;
	/**
	 * Oltre questa distanza dal cursore, in pixel, un'icona resta della sua misura
	 */
	private static final double RAGGIO = 2.0d * ICONA_WIDTH;
	/**
	 * Quanto ci mette l'effetto a crescere entrando nella barra e a sgonfiarsi uscendo
	 */
	private static final long DURATA_TRANSIZIONE_NANOS = 120_000_000L;

	/**
	 * Se il cursore è sulla barra, e dove era l'ultima volta: uscendo le icone si sgonfiano attorno a quel punto
	 */
	private boolean cursoreDentro;
	private int ultimoMouseX = -1;
	/**
	 * L'intensità dell'effetto (0 spento, 1 pieno) all'ultimo ingresso o uscita, e quando è avvenuto: da lì
	 * l'intensità va verso 1 o verso 0. Letto anche dal thread di animazione (vedi inTransizione).
	 */
	private double intensitaAlCambio;
	private volatile long istanteCambio = System.nanoTime() - DURATA_TRANSIZIONE_NANOS;

	/**
	 * Dove sono disegnate adesso le icone, nello stesso ordine di iconeVisibili (coordinate relative alla barra;
	 * la y è negativa per la parte che esce sopra)
	 */
	private final List<Rectangle> rettangoli = new ArrayList<>();

	DisplayableCanvasBarraIconeDock(int offsetX, int offsetY, int larghezza, int altezza) {
		super(DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE, offsetX, offsetY, larghezza, altezza);
	}

	/**
	 * La scala di un'icona secondo la distanza del suo centro dal cursore: a campana, da SCALA_MASSIMA sotto il
	 * cursore a 1 a distanza RAGGIO.
	 */
	private static double scala(double distanza) {
		if (distanza >= RAGGIO) {
			return 1.0d;
		}
		double coseno = Math.cos(Math.PI / 2.0d * distanza / RAGGIO);
		return 1.0d + (SCALA_MASSIMA - 1.0d) * coseno * coseno;
	}

	/**
	 * L'intensità dell'effetto adesso: dall'intensità all'ultimo cambio verso 1 (cursore dentro) o 0 (fuori), con
	 * una curva che parte e arriva dolce.
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
	boolean inTransizione() {
		return System.nanoTime() - istanteCambio < DURATA_TRANSIZIONE_NANOS;
	}

	@Override
	public void processaMovimento(int x, int y) {
		if (!cursoreDentro) {
			cambia(true);
		}
		ultimoMouseX = x;
		super.processaMovimento(x, y);
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (!cursoreDentro) {
			cambia(true);
		}
		ultimoMouseX = x;
		super.processaPressione(x, y, tasto);
	}

	@Override
	public void processaUscita(int x, int y) {
		if (cursoreDentro) {
			cambia(false);
		}
		super.processaUscita(x, y);
	}

	/**
	 * Ricalcola i rettangoli delle icone dalla posizione del cursore. Le scale si misurano sulle posizioni di base
	 * (quelle della barra classica), non su quelle ingrandite, altrimenti ogni movimento cambierebbe le distanze che
	 * lo determinano. La fila allargata si distribuisce attorno al cursore in proporzione a dove cade nella fila di
	 * base, così l'icona sotto il cursore gli resta sotto; le icone poggiano sul fondo e crescono verso l'alto.
	 */
	private void ricalcola() {
		rettangoli.clear();
		if (iconeVisibili.isEmpty()) {
			return;
		}
		// Uscendo, finché l'effetto si sgonfia, conta l'ultima posizione del cursore
		int cursoreX = cursoreDentro ? mouseX : ultimoMouseX;
		double intensita = intensita();
		boolean attivo = cursoreX >= 0 && intensita > 0.0d;
		double[] scale = new double[iconeVisibili.size()];
		double larghezzaTotale = 0.0d;
		for (int i = 0; i < scale.length; i++) {
			Rectangle base = iconeVisibili.get(i).rettangolo;
			scale[i] = attivo ? 1.0d + (scala(Math.abs(cursoreX - (base.x + base.width / 2.0d))) - 1.0d) * intensita : 1.0d;
			larghezzaTotale += base.width * scale[i];
		}
		Rectangle primo = iconeVisibili.get(0).rettangolo;
		Rectangle ultimo = iconeVisibili.get(iconeVisibili.size() - 1).rettangolo;
		double inizioBase = primo.x;
		double larghezzaBase = ultimo.x + ultimo.width - inizioBase;
		double quota = attivo ? Math.max(0.0d, Math.min(1.0d, (cursoreX - inizioBase) / larghezzaBase)) : 0.5d;
		double x = inizioBase - (larghezzaTotale - larghezzaBase) * quota;
		x = Math.max(0.0d, Math.min(larghezza - larghezzaTotale, x));
		for (int i = 0; i < scale.length; i++) {
			Rectangle base = iconeVisibili.get(i).rettangolo;
			int w = (int) Math.round(base.width * scale[i]);
			int h = (int) Math.round(base.height * scale[i]);
			rettangoli.add(new Rectangle((int) Math.round(x), base.y + base.height - h, w, h));
			x += base.width * scale[i];
		}
	}

	@Override
	boolean contiene(int x, int y) {
		if (super.contiene(x, y)) {
			return true;
		}
		ricalcola();
		return rettangoli.stream().anyMatch(r -> r.contains(x, y));
	}

	@Override
	void disegna(Graphics2D graphics) {
		graphics.setColor(Color.black);
		graphics.fillRect(offsetX, offsetY, larghezza, altezza);
		ricalcola();
		Object interpolazione = graphics.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		// Pixel netti: niente sfocatura sulle icone ingrandite
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		for (int i = 0; i < rettangoli.size(); i++) {
			Rectangle r = rettangoli.get(i);
			int x = offsetX + r.x;
			int y = offsetY + r.y;
			graphics.drawImage(iconeVisibili.get(i).icona, x, y, r.width, r.height, null);
			if (r.contains(mouseX, mouseY)) {
				graphics.setColor(mousePremuto ? Color.white : Color.lightGray);
				graphics.drawRect(x, y, r.width - 1, r.height - 1);
			}
		}
		if (interpolazione != null) {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolazione);
		}
		copyright(graphics);
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO) {
			return;
		}
		ricalcola();
		for (int i = 0; i < rettangoli.size(); i++) {
			if (rettangoli.get(i).contains(x, y)) {
				esegui(iconeVisibili.get(i).comando);
				return;
			}
		}
	}
}
