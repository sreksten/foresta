package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Il logo che si traccia da solo, disegnato con Swing: la logica (maschera, contorni, corridori, fasi, opacita') e' di {@link AnimazioneDelLogo}, nel
 * modulo comune, e la spiega per intero; qui ci sono i tipi di AWT con cui si imposta ({@code TracciatoreLogo.costruttore(logo)...costruisci()}) e il
 * disegno con {@code Graphics2D}: la torcia (un gradiente radiale per ogni corridore applicato al logo con {@code AlphaComposite.DstIn}), la scia
 * (la polilinea tracciata quattro volte, con tratti via via piu' sottili e piu' opachi) e la testa luminosa. Si fa avanzare di un fotogramma alla volta con
 * {@link #avanza()} e si disegna con {@link #disegna(Graphics2D)}: chi lo usa decide dove e quando, per esempio con un Timer ogni
 * {@link #INTERVALLO_FOTOGRAMMA_MS} millisecondi.
 * <p>
 * Da riga di comando lo si prova con TracciatoreLogoDaRigaDiComando, tra i sorgenti dei test. La versione di libGDX e' <code>LogoInizialeGdx</code>.
 */
public final class TracciatoreLogo {

	/** L'intervallo tra due fotogrammi a cui l'effetto e' pensato: 16ms ~= 60 fotogrammi al secondo. */
	public static final int INTERVALLO_FOTOGRAMMA_MS = AnimazioneDelLogo.INTERVALLO_FOTOGRAMMA_MS;
	static final int SOGLIA_ALPHA_PREDEFINITA = AnimazioneDelLogo.SOGLIA_ALPHA_PREDEFINITA;
	static final int DURATA_GIRO_MS_PREDEFINITA = AnimazioneDelLogo.DURATA_GIRO_MS_PREDEFINITA;
	static final int FOTOGRAMMI_DISSOLVENZA_PREDEFINITI = AnimazioneDelLogo.FOTOGRAMMI_DISSOLVENZA_PREDEFINITI;
	/** Il colore di sfondo mostrato quando il logo e' nascosto */
	static final Color COLORE_SFONDO_PREDEFINITO = new Color(30, 30, 34);

	/**
	 * Decide se il pixel (x, y), di colore argb, va considerato sfondo (quindi escluso dal logo e dal tracciamento del contorno).
	 */
	@FunctionalInterface
	public interface PredicatoSfondo extends AnimazioneDelLogo.PredicatoSfondo {
		/** Scarta i pixel il cui canale alpha e' minore o uguale alla soglia. */
		static PredicatoSfondo perAlpha(int soglia) {
			AnimazioneDelLogo.PredicatoSfondo comune = AnimazioneDelLogo.PredicatoSfondo.perAlpha(soglia);
			return comune::isSfondo;
		}

		/**
		 * Scarta i pixel il cui colore RGB e' vicino a {@code sfondo} entro {@code tolleranza} per canale. Il canale alpha viene ignorato.
		 */
		static PredicatoSfondo perColore(Color sfondo, int tolleranza) {
			AnimazioneDelLogo.PredicatoSfondo comune = AnimazioneDelLogo.PredicatoSfondo.perColore(sfondo.getRGB() & 0xFFFFFF, tolleranza);
			return comune::isSfondo;
		}
	}

	/** Il verso in cui un punto luminoso percorre il contorno del logo. */
	public enum Verso {ORARIO, ANTIORARIO}

	private final AnimazioneDelLogo animazione;
	private final BufferedImage logo;
	private final Color coloreSfondo;

	private TracciatoreLogo(AnimazioneDelLogo animazione, BufferedImage logo, Color coloreSfondo) {
		this.animazione = animazione;
		this.logo = logo;
		this.coloreSfondo = coloreSfondo;
	}

	/**
	 * Comincia a impostare l'effetto per quel logo. Il logo viene sempre disegnato per intero; dove cercarne il contorno lo decidono il criterio di
	 * sfondo e l'eventuale regione.
	 */
	public static Costruttore costruttore(BufferedImage logo) {
		return new Costruttore(logo);
	}

	/** Dai pixel di un'immagine AWT a quelli senza toolkit */
	static ImmagineDiPixel pixel(BufferedImage immagine) {
		int w = immagine.getWidth();
		int h = immagine.getHeight();
		return new ImmagineDiPixel(w, h, immagine.getRGB(0, 0, w, h, null, 0, w));
	}

	private static AnimazioneDelLogo.Verso versoComune(Verso verso) {
		return AnimazioneDelLogo.Verso.valueOf(verso.name());
	}

	/** Fa avanzare l'animazione di un fotogramma. Finito l'effetto (vedi {@link #isFinito()}) non cambia piu' nulla. */
	public void avanza() {
		animazione.avanza();
	}

	/**
	 * Disegna il fotogramma corrente con l'angolo in alto a sinistra del logo in (0, 0) e alla sua dimensione:
	 * per spostarlo o ingrandirlo si trasforma prima {@code g2} (translate, scale).
	 */
	public void disegna(Graphics2D g2) {
		Graphics2D g = (Graphics2D) g2.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		disegnaFotogramma(g);
		g.dispose();
	}

	/** Vero quando, senza ripetizione, tracciamento, dissolvenza, pausa ed eventuale scomparsa sono terminati. */
	public boolean isFinito() {
		return animazione.isFinito();
	}

	/** Riporta l'effetto all'inizio, come appena costruito. */
	public void ricomincia() {
		animazione.ricomincia();
	}

	public int getLarghezza() {
		return logo.getWidth();
	}

	public int getAltezza() {
		return logo.getHeight();
	}

	/** Il nome della fase corrente (TRACCIAMENTO, DISSOLVENZA, PAUSA, SCOMPARSA), per la diagnostica. */
	public String getNomeFase() {
		return animazione.getNomeFase();
	}

	/**
	 * Quanti fotogrammi dura (per eccesso) un ciclo intero: il tracciamento del corridore piu' lento, in genere un giro completo, piu' il ritardo del
	 * corridore piu' «tardivo», la dissolvenza, la pausa finale e la scomparsa.
	 */
	public int stimaFotogrammiCiclo() {
		return animazione.stimaFotogrammiCiclo();
	}

	/** Cosa ha trovato e deciso il Costruttore (forme, corridori, avvertimenti): utile per calibrare i parametri. */
	public List<String> getDiagnostica() {
		return animazione.getDiagnostica();
	}

	// =========================================================================
	//  Costruttore
	// =========================================================================

	/**
	 * Imposta l'effetto con i tipi di AWT: tutti i parametri sono facoltativi (i valori predefiniti sono quelli di {@link AnimazioneDelLogo}).
	 * {@link #costruisci()} esegue le fasi 1-5 della pipeline (maschera, contorni, buchi, ricampionamento, corridori).
	 */
	public static final class Costruttore {

		private final BufferedImage logo;
		private final AnimazioneDelLogo.Costruttore comune;
		private Color coloreSfondo = COLORE_SFONDO_PREDEFINITO;
		private BufferedImage daMostrare;

		private Costruttore(BufferedImage logo) {
			this.logo = Objects.requireNonNull(logo, "logo");
			this.comune = AnimazioneDelLogo.costruttore(pixel(logo));
		}

		/** Sono sfondo i pixel con alpha minore o uguale alla soglia (predefinito: trasparenza con soglia 128). */
		public Costruttore sfondoPerAlpha(int soglia) {
			comune.sfondoPerAlpha(soglia);
			return this;
		}

		/** Sono sfondo i pixel di colore vicino a questo, entro la tolleranza per canale (scontorno per colore). */
		public Costruttore sfondoPerColore(Color colore, int tolleranza) {
			comune.sfondoPerColore(colore.getRGB() & 0xFFFFFF, tolleranza);
			return this;
		}

		/** Un criterio di sfondo qualsiasi. */
		public Costruttore sfondo(PredicatoSfondo sfondo) {
			comune.sfondo(Objects.requireNonNull(sfondo, "sfondo"));
			return this;
		}

		/**
		 * Si disegna questa immagine al posto del logo da cui si traccia: deve avere la stessa dimensione, con il disegno da tracciare esattamente nello
		 * stesso posto. Null per disegnare il logo stesso.
		 */
		public Costruttore immagineDaMostrare(BufferedImage daMostrare) {
			this.daMostrare = daMostrare;
			comune.immagineDaMostrare(daMostrare != null ? pixel(daMostrare) : null);
			return this;
		}

		/** Cerca il logo solo in questo rettangolo dell'immagine; null per tutta l'immagine. */
		public Costruttore regione(Rectangle regione) {
			comune.regione(regione != null ? new Area(regione.x, regione.y, regione.width, regione.height) : null);
			return this;
		}

		/**
		 * Aggiunge un corridore esplicito: parte dal punto del contorno piu' vicino a {@code partenza} e arriva a quello piu' vicino ad {@code arrivo},
		 * oppure fa un giro completo se {@code arrivo} e' null. Se non se ne aggiunge nessuno, c'e' un corridore a giro completo per ogni forma trovata.
		 */
		public Costruttore corridore(Point2D.Double partenza, Point2D.Double arrivo, Verso verso, int fotogrammiRitardo) {
			comune.corridore(posizione(partenza), arrivo != null ? posizione(arrivo) : null, versoComune(verso), fotogrammiRitardo);
			return this;
		}

		private static AnimazioneDelLogo.Posizione posizione(Point2D.Double punto) {
			return new AnimazioneDelLogo.Posizione(punto.x, punto.y);
		}

		/** Il verso di tutti i corridori automatici (predefinito orario). */
		public Costruttore verso(Verso verso) {
			comune.verso(versoComune(verso));
			return this;
		}

		/** Il verso dei corridori automatici, uno per forma nell'ordine in cui sono trovate; alle forme in piu' si applica il primo. */
		public Costruttore versi(List<Verso> versi) {
			List<AnimazioneDelLogo.Verso> convertiti = new ArrayList<>();
			for (Verso verso : versi) {
				convertiti.add(versoComune(verso));
			}
			comune.versi(convertiti);
			return this;
		}

		/** Vero perche' i corridori automatici siano due per ogni forma, uno orario e uno antiorario, che si incontrano a meta' del contorno. */
		public Costruttore ambiVersi(boolean ambiVersi) {
			comune.ambiVersi(ambiVersi);
			return this;
		}

		/** Il ritardo di partenza dei corridori automatici, in fotogrammi, uno per forma (le altre partono subito). */
		public Costruttore ritardi(List<Integer> ritardi) {
			comune.ritardi(ritardi);
			return this;
		}

		/** Durata di riferimento di un giro completo, in millisecondi. */
		public Costruttore durataGiroMs(int durataGiroMs) {
			comune.durataGiroMs(durataGiroMs);
			return this;
		}

		/** Vero perche' tutti i corridori arrivino in fondo insieme; predefinito falso. */
		public Costruttore velocitaAllineata(boolean velocitaAllineata) {
			comune.velocitaAllineata(velocitaAllineata);
			return this;
		}

		/** Falso per mostrare subito il logo intero invece di rivelarlo con la torcia. */
		public Costruttore rivelaConTorcia(boolean rivelaConTorcia) {
			comune.rivelaConTorcia(rivelaConTorcia);
			return this;
		}

		/** Raggio della torcia in pixel (predefinito circa il 10% del lato piu' corto del logo). */
		public Costruttore raggioTorcia(int raggioTorcia) {
			comune.raggioTorcia(raggioTorcia);
			return this;
		}

		/** Durata della dissolvenza finale, in fotogrammi. */
		public Costruttore fotogrammiDissolvenza(int fotogrammiDissolvenza) {
			comune.fotogrammiDissolvenza(fotogrammiDissolvenza);
			return this;
		}

		/** Quanti fotogrammi resta acceso il logo intero alla fine. */
		public Costruttore fotogrammiPausa(int fotogrammiPausa) {
			comune.fotogrammiPausa(fotogrammiPausa);
			return this;
		}

		/** Vero perche', mentre il logo compare in dissolvenza, le scie dei corridori svaniscano insieme e le loro teste non si disegnino piu'. */
		public Costruttore sfumaScie(boolean sfumaScie) {
			comune.sfumaScie(sfumaScie);
			return this;
		}

		/** Dopo la pausa, il logo svanisce in questi fotogrammi, e solo allora il ciclo e' finito. Predefinito 0: il logo resta acceso. */
		public Costruttore fotogrammiScomparsa(int fotogrammiScomparsa) {
			comune.fotogrammiScomparsa(fotogrammiScomparsa);
			return this;
		}

		/** Vero per ricominciare da capo dopo la pausa, all'infinito; predefinito falso (una volta sola). */
		public Costruttore ripeti(boolean ripeti) {
			comune.ripeti(ripeti);
			return this;
		}

		/** Il colore con cui riempire il rettangolo del logo prima di disegnarlo; null per non riempirlo. */
		public Costruttore coloreSfondo(Color coloreSfondo) {
			this.coloreSfondo = coloreSfondo;
			return this;
		}

		/**
		 * Esegue la pipeline e restituisce l'effetto, fermo al primo fotogramma.
		 *
		 * @throws IllegalArgumentException se la regione esce dall'immagine o l'immagine da mostrare ha un'altra dimensione
		 * @throws IllegalStateException    se non si trova nessun contorno
		 */
		public TracciatoreLogo costruisci() {
			return new TracciatoreLogo(comune.costruisci(), daMostrare != null ? daMostrare : logo, coloreSfondo);
		}
	}

	// =========================================================================
	//  Resa grafica
	// =========================================================================

	/**
	 * Disegna un fotogramma completo: sfondo, logo (a torcia oppure a piena opacita' con dissolvenza, a seconda della fase/modalita'), scia e punto
	 * luminoso di ogni corridore.
	 */
	private void disegnaFotogramma(Graphics2D g2) {
		int w = logo.getWidth();
		int h = logo.getHeight();
		// riempie lo sfondo per primo (se ce n'e' uno): qualunque cosa venga disegnata dopo vi si sovrappone
		if (coloreSfondo != null) {
			g2.setColor(coloreSfondo);
			g2.fillRect(0, 0, w, h);
		}

		if (animazione.isTorciaAttiva()) {
			// ancora nel tracciamento: una versione del logo visibile solo vicino ai corridori
			g2.drawImage(rivelaLogoConTorcia(), 0, 0, null);
		} else {
			// fuori dal tracciamento (o senza torcia): il logo intero con un'opacita' globale, tramite un AlphaComposite temporaneo (che viene
			// ripristinato subito dopo, per non alterare il composito con cui verranno disegnati scia e punto luminoso)
			Composite compositoPrecedente = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) animazione.getOpacitaLogo()));
			g2.drawImage(logo, 0, 0, null);
			g2.setComposite(compositoPrecedente);
		}

		// scia e punto luminoso vengono disegnati sopra al logo (rivelato o meno), per ogni corridore che ha già superato il proprio ritardo iniziale,
		// con l'opacita' delle scie (che le fa svanire nella dissolvenza o nella scomparsa)
		double opacitaScie = animazione.getOpacitaScie();
		if (opacitaScie <= 0) {
			return;
		}
		Composite compositoPrecedente = g2.getComposite();
		if (opacitaScie < 1) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacitaScie));
		}
		for (AnimazioneDelLogo.Corridore r : animazione.getCorridori()) {
			if (!r.isPartito()) {
				continue; // ritardo non ancora scaduto: resta invisibile
			}
			int finoA = r.getIndice();
			if (finoA > 0) {
				disegnaScia(g2, r.getPercorso(), finoA);
			}
			if (animazione.isConTeste()) {
				disegnaTestaCometa(g2, r.getPercorso(), finoA);
			}
		}
		g2.setComposite(compositoPrecedente);
	}

	/**
	 * Costruisce una copia del logo visibile solo entro un piccolo raggio attorno alla posizione corrente di ciascun corridore («torcia»), ricreata da
	 * zero ogni volta. Tecnica: disegna una maschera con un gradiente radiale morbido per ogni corridore, poi la applica al logo con
	 * {@code AlphaComposite.DstIn}, che moltiplica l'alpha di destinazione (il logo) per l'alpha sorgente (la maschera).
	 */
	private BufferedImage rivelaLogoConTorcia() {
		int w = logo.getWidth();
		int h = logo.getHeight();
		float raggio = animazione.getRaggioTorcia();

		// passo 1: la maschera (un'immagine ARGB inizialmente del tutto trasparente) con un cerchio sfumato per ogni corridore attivo
		BufferedImage maschera = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gm = maschera.createGraphics();
		gm.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (AnimazioneDelLogo.Corridore r : animazione.getCorridori()) {
			if (!r.isPartito()) {
				continue; // ritardo non ancora scaduto: nessuna torcia
			}
			AnimazioneDelLogo.Posizione p = r.getPosizione();
			// alpha massimo al centro (non 255 pieno: e' voluto, cosi' il logo «si intravede» e basta sotto la torcia), fino a 0 al bordo
			RadialGradientPaint bagliore = new RadialGradientPaint(new Point2D.Float((float) p.x, (float) p.y), raggio,
					new float[]{0f, 1f},
					new Color[]{new Color(255, 255, 255, TorciaDelLogo.ALPHA_MASSIMA), new Color(255, 255, 255, 0)});
			gm.setPaint(bagliore);
			gm.fill(new Ellipse2D.Double(p.x - raggio, p.y - raggio, 2 * raggio, 2 * raggio));
		}
		gm.dispose();

		// passo 2: il logo su una seconda immagine di appoggio, «ritagliato» attraverso la maschera
		BufferedImage rivelato = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gr = rivelato.createGraphics();
		gr.drawImage(logo, 0, 0, null);
		// DstIn (Porter-Duff «destination in source»): il risultato mantiene il colore della destinazione (il logo appena disegnato) ma con
		// alpha = alpha_destinazione * alpha_sorgente (la maschera): dove la maschera e' trasparente il logo sparisce
		gr.setComposite(AlphaComposite.DstIn);
		gr.drawImage(maschera, 0, 0, null);
		gr.dispose();
		return rivelato;
	}

	/**
	 * Disegna la scia bianca dal punto 0 fino all'indice {@code finoA} del percorso. L'effetto «bagliore» e' ottenuto disegnando la STESSA linea quattro
	 * volte, con tratti via via piu' sottili e piu' opachi: le passate larghe e trasparenti danno l'alone sfumato, l'ultima (sottile, bianco pieno) dà
	 * il «nucleo» luminoso netto al centro. E' un trucco economico per un blur senza dover convolvere l'immagine.
	 */
	private static void disegnaScia(Graphics2D g2, List<AnimazioneDelLogo.Posizione> percorso, int finoA) {
		// un'unica polilinea che collega tutti i punti già percorsi
		Path2D.Double scia = new Path2D.Double();
		AnimazioneDelLogo.Posizione p0 = percorso.get(0);
		scia.moveTo(p0.x, p0.y);
		for (int i = 1; i <= finoA; i++) {
			AnimazioneDelLogo.Posizione p = percorso.get(i);
			scia.lineTo(p.x, p.y);
		}

		// passata 1: tratto molto largo, molto trasparente -> alone esterno ampio
		g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setColor(new Color(255, 255, 255, 35));
		g2.draw(scia);

		// passata 2: più stretto, meno trasparente -> alone intermedio
		g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setColor(new Color(255, 255, 255, 80));
		g2.draw(scia);

		// passata 3: ancora più stretto, quasi opaco -> bordo del nucleo
		g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setColor(new Color(255, 255, 255, 210));
		g2.draw(scia);

		// passata 4: linea sottilissima, bianco pieno -> nucleo netto e definito
		g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setColor(Color.WHITE);
		g2.draw(scia);
	}

	/**
	 * Disegna il punto luminoso («testa della cometa») nella posizione corrente del percorso: un alone radiale morbido più un piccolo cerchio bianco
	 * pieno al centro, per un nucleo netto sopra il bagliore (le stesse forme di {@link TestaDellaCometa}).
	 */
	private static void disegnaTestaCometa(Graphics2D g2, List<AnimazioneDelLogo.Posizione> percorso, int indice) {
		AnimazioneDelLogo.Posizione p = percorso.get(Math.min(indice, percorso.size() - 1));
		float raggioTesta = TestaDellaCometa.RAGGIO;
		// gradiente radiale a tre tappe: bianco pieno al centro, bianco semitrasparente a poco meno di metà raggio, trasparente al bordo
		RadialGradientPaint bagliore = new RadialGradientPaint(new Point2D.Float((float) p.x, (float) p.y), raggioTesta,
				new float[]{0f, 0.4f, 1f},
				new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 110), new Color(255, 255, 255, 0)});
		g2.setPaint(bagliore);
		g2.fill(new Ellipse2D.Double(p.x - raggioTesta, p.y - raggioTesta, 2 * raggioTesta, 2 * raggioTesta));

		// piccolo cerchio bianco pieno sopra l'alone: un «nucleo» netto e riconoscibile
		g2.setColor(Color.WHITE);
		g2.fill(new Ellipse2D.Double(p.x - 3, p.y - 3, 6, 6));
	}
}
