package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Il riquadro del combattimento, senza disegno: tutti i personaggi della locazione, dei due gruppi, nell'ordine in cui agiscono nel round (vedi
 * VistaPartita.getOrdineDiCombattimento). Chi è vivo ha il nome e la barra della salute; chi è vivo ma non combatte (in panchina) ha la barra ma il nome
 * grigio; chi è morto ha solo il nome, in grigio scuro. Sotto il personaggio che agisce c'è l'azione che compie (un solo posto, che scorre con lui).
 * <p>
 * La cornice è quella di sempre, tagliata a metà: la metà alta sta in cima e la metà bassa in fondo, e in mezzo c'è lo sfondo nero con i due bordi laterali.
 * Così il riquadro cresce quanto serve. Sta centrato in orizzontale, sotto l'immagine della locazione; se non ci sta sale quanto basta per non coprire la
 * barra delle icone. {@link #disponi()} dà a chi disegna tutto quel che serve, con le misure e i colori già calcolati.
 */
public class RiquadroCombattimento implements Finestra {

	/**
	 * Un personaggio nel riquadro: la quota della riga, il colore della striscia del gruppo, la barra della salute (se è vivo) e i testi
	 */
	public static final class Riga {
		private final int y;
		private final int coloreSegnale;
		private final boolean mostraLaBarra;
		private final int larghezzaBarraPiena;
		private final int coloreBarra;
		private final List<VoceDiTesto> testi;

		private Riga(int y, int coloreSegnale, boolean mostraLaBarra, int larghezzaBarraPiena, int coloreBarra, List<VoceDiTesto> testi) {
			this.y = y;
			this.coloreSegnale = coloreSegnale;
			this.mostraLaBarra = mostraLaBarra;
			this.larghezzaBarraPiena = larghezzaBarraPiena;
			this.coloreBarra = coloreBarra;
			this.testi = testi;
		}

		public int getY() {
			return y;
		}

		/**
		 * Il colore (0xRRGGBB) della striscia a sinistra della riga: blu per il gruppo, rosso per gli avversari, giallo per chi è in panchina
		 */
		public int getColoreSegnale() {
			return coloreSegnale;
		}

		public boolean isMostraLaBarra() {
			return mostraLaBarra;
		}

		/**
		 * Quanto è larga la parte piena della barra della salute
		 */
		public int getLarghezzaBarraPiena() {
			return larghezzaBarraPiena;
		}

		/**
		 * Il colore (0xRRGGBB) della parte piena della barra
		 */
		public int getColoreBarra() {
			return coloreBarra;
		}

		public List<VoceDiTesto> getTesti() {
			return testi;
		}
	}

	/**
	 * Tutto quel che serve per disegnare il riquadro adesso
	 */
	public static final class Disposizione {
		private final Area area;
		private final int metaAlta;
		private final int metaBassa;
		private final int bordoLaterale;
		private final int xSegnale;
		private final int xBarra;
		private final int larghezzaBarra;
		private final List<Riga> righe;

		private Disposizione(Area area, int metaAlta, int metaBassa, int bordoLaterale, int xSegnale, int xBarra, int larghezzaBarra, List<Riga> righe) {
			this.area = area;
			this.metaAlta = metaAlta;
			this.metaBassa = metaBassa;
			this.bordoLaterale = bordoLaterale;
			this.xSegnale = xSegnale;
			this.xBarra = xBarra;
			this.larghezzaBarra = larghezzaBarra;
			this.righe = righe;
		}

		/**
		 * Dove sta il riquadro (cresce con i personaggi)
		 */
		public Area getArea() {
			return area;
		}

		/**
		 * L'altezza della metà alta della cornice (si disegna in cima) e di quella bassa (in fondo)
		 */
		public int getMetaAlta() {
			return metaAlta;
		}

		public int getMetaBassa() {
			return metaBassa;
		}

		/**
		 * Lo spessore dei due bordi laterali marroni, già con lo zoom
		 */
		public int getBordoLaterale() {
			return bordoLaterale;
		}

		public int getXSegnale() {
			return xSegnale;
		}

		public int getXBarra() {
			return xBarra;
		}

		public int getLarghezzaBarra() {
			return larghezzaBarra;
		}

		public List<Riga> getRighe() {
			return righe;
		}
	}

	public static final int COLORE_BORDO = 0x8c4210;
	public static final int COLORE_SFONDO = 0x000000;
	public static final int COLORE_SFONDO_BARRA = 0x282828;
	public static final int ALTEZZA_BARRA = 26;
	public static final int LARGHEZZA_SEGNALE = 4;
	private static final int COLORE_GRUPPO = 0x4682e6;
	private static final int COLORE_AVVERSARI = 0xaa3232;
	private static final int COLORE_PANCHINA = 0xe6c828;
	// Larghezza del bordo laterale nell'immagine originale (poi si applica lo zoom)
	private static final int BORDO_LATERALE = 3;
	private static final int OFFSET_DA_CORNICE = 8;
	private static final int SPAZIATURA = 2;

	private boolean visible;
	private int xOffset;
	private int yOffset;
	private final int parentWidth;
	private final int parentHeight;
	private final int yPreferita;
	private final int larghezzaCornice;
	private final int altezzaCornice;
	private Area area;
	private VistaPersonaggio combattente;
	private VistaPersonaggio avversario;
	private VistaPersonaggio chiHaDecisoLAzione;
	private String testoAzione;
	private final DoomdarkFont fontMedium = DoomdarkFont.medio();
	protected final VistaPartita vistaPartita;

	/**
	 * @param yPreferita       dove il riquadro comincia di norma: subito sotto l'immagine della locazione
	 * @param larghezzaCornice la larghezza della cornice del combattimento, già con lo zoom
	 * @param altezzaCornice   la sua altezza
	 */
	public RiquadroCombattimento(int parentWidth, int parentHeight, int yPreferita, int larghezzaCornice, int altezzaCornice, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.parentWidth = parentWidth;
		this.parentHeight = parentHeight;
		this.yPreferita = yPreferita;
		this.larghezzaCornice = larghezzaCornice;
		this.altezzaCornice = altezzaCornice;
		visible = false;
		xOffset = (parentWidth - larghezzaCornice) >> 1;
		yOffset = Math.min(yPreferita, Math.max(0, parentHeight - altezzaCornice));
		area = new Area(xOffset, yOffset, larghezzaCornice, altezzaCornice);
	}

	/** Il riquadro com'è stato disegnato l'ultima volta (cresce con i personaggi). */
	public Area getArea() {
		return area;
	}

	@Override
	public boolean isVisibile() {
		return visible;
	}

	public int getXOffset() {
		return xOffset;
	}

	public int getYOffset() {
		return yOffset;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		if (!visible) {
			chiHaDecisoLAzione = null;
			testoAzione = null;
		}
	}

	public VistaPersonaggio getCombattente() {
		return combattente;
	}

	public VistaPersonaggio getAvversario() {
		return avversario;
	}

	public void setCombattente(VistaPersonaggio combattente) {
		this.combattente = combattente;
	}

	public void setAvversario(VistaPersonaggio avversario) {
		this.avversario = avversario;
	}

	/**
	 * Che cosa fa il personaggio che agisce, da scrivere sotto di lui.
	 */
	public void setAzione(VistaPersonaggio personaggio, String testo) {
		chiHaDecisoLAzione = personaggio;
		testoAzione = testo;
	}

	/**
	 * Se c'è già un testo per l'azione di quel personaggio.
	 */
	public boolean haAzionePer(VistaPersonaggio personaggio) {
		return testoAzione != null && chiHaDecisoLAzione == personaggio;
	}

	/**
	 * Il testo da scrivere se la tattica non ha detto che cosa fa chi agisce: attacca il bersaglio, se c'è.
	 */
	public static String azionePredefinita(VistaPersonaggio bersaglio) {
		return TestoAzioneInCombattimento.predefinito(bersaglio);
	}

	/**
	 * Il testo dell'azione scelta con la tattica.
	 */
	public static String testoDellAzione(TipoAzione azione, VistaPersonaggio attore, VistaPersonaggio bersaglio) {
		return TestoAzioneInCombattimento.di(azione, attore, bersaglio);
	}

	/**
	 * Calcola dove sta il riquadro adesso (cresce con i personaggi) e come disegnarlo: l'area, le metà della cornice, i bordi e, per ogni personaggio,
	 * la riga con la barra della salute e i testi. Aggiorna anche {@link #getArea()}. Null se il riquadro non si vede o nessuno combatte.
	 */
	public Disposizione disponi() {
		if (!visible) {
			return null;
		}
		List<? extends VistaPersonaggio> personaggi = vistaPartita.getOrdineDiCombattimento();
		if (personaggi.isEmpty()) {
			return null;
		}
		final int altezzaAzione = fontMedium.getHeight() + SPAZIATURA;
		final int altezzaRiga = ALTEZZA_BARRA + SPAZIATURA;
		// Un solo posto per l'azione, sotto chi agisce: le righe sotto di lui scorrono in giù
		final int altezzaContenuto = personaggi.size() * altezzaRiga + altezzaAzione;

		final int metaAlta = altezzaCornice / 2;
		final int metaBassa = altezzaCornice - metaAlta;
		final int altezza = Math.max(altezzaCornice, altezzaContenuto + 2 * OFFSET_DA_CORNICE);
		final int larghezza = larghezzaCornice;

		xOffset = (parentWidth - larghezza) >> 1;
		yOffset = Math.max(0, Math.min(yPreferita, parentHeight - altezza));
		area = new Area(xOffset, yOffset, larghezza, altezza);

		final int larghezzaMassimaBarra = larghezza - 2 * OFFSET_DA_CORNICE - LARGHEZZA_SEGNALE - SPAZIATURA;
		final int offsetDaBarra = (ALTEZZA_BARRA - fontMedium.getHeight()) / 2;
		final int xSegnale = xOffset + OFFSET_DA_CORNICE;
		final int xBarra = xSegnale + LARGHEZZA_SEGNALE + SPAZIATURA;
		int y = yOffset + OFFSET_DA_CORNICE;

		List<Riga> righe = new ArrayList<>();
		for (VistaPersonaggio personaggio : personaggi) {
			boolean agisce = personaggio == combattente;
			int coloreSegnale = personaggio.isVivo() && personaggio.isInPanchina() ? COLORE_PANCHINA
					: vistaPartita.getGruppoAvversario().contiene(personaggio) ? COLORE_AVVERSARI : COLORE_GRUPPO;
			List<VoceDiTesto> testi = new ArrayList<>();
			boolean vivo = personaggio.isVivo();
			int larghezzaPiena = 0;
			int coloreBarra = 0;
			if (vivo) {
				double percentualeSalute = (double) personaggio.getSalute() / personaggio.getSaluteMassima();
				larghezzaPiena = (int) (larghezzaMassimaBarra * percentualeSalute);
				coloreBarra = determinaColoreSalute(percentualeSalute);
				String salute = personaggio.getSalute() + "/" + personaggio.getSaluteMassima();
				int xSalute = xBarra + larghezzaMassimaBarra - SPAZIATURA;
				testi.add(new VoceDiTesto(salute, xSalute, y + offsetDaBarra, DoomdarkColorModel.Color.WHITE, true, 0, true));
				String nome = nomeEntro(personaggio, larghezzaMassimaBarra - 3 * SPAZIATURA - FontTool.getWidth(fontMedium, "000/000"));
				testi.add(new VoceDiTesto(nome, xBarra + SPAZIATURA, y + offsetDaBarra,
						personaggio.isInPanchina() ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.WHITE, false, 0, true));
			} else {
				String nome = nomeEntro(personaggio, larghezzaMassimaBarra - 2 * SPAZIATURA);
				testi.add(new VoceDiTesto(nome, xBarra + SPAZIATURA, y + offsetDaBarra, DoomdarkColorModel.Color.DARK_GRAY, false, 0, false));
			}
			righe.add(new Riga(y, coloreSegnale, vivo, larghezzaPiena, coloreBarra, testi));
			y += altezzaRiga;
			if (agisce) {
				if (testoAzione != null) {
					String azione = testoAzione;
					int l = larghezzaMassimaBarra - 2 * SPAZIATURA;
					while (azione.length() > 1 && FontTool.getWidth(fontMedium, azione) > l) {
						azione = azione.substring(0, azione.length() - 1);
					}
					List<VoceDiTesto> testiAzione = new ArrayList<>();
					testiAzione.add(new VoceDiTesto(azione, xBarra + SPAZIATURA, y, DoomdarkColorModel.Color.YELLOW, false, 0, true));
					righe.add(new Riga(y, 0, false, 0, 0, testiAzione));
				}
				y += altezzaAzione;
			}
		}
		int bordo = Math.max(1, (int) Math.round(BORDO_LATERALE * LivelloDiZoom.valore()));
		return new Disposizione(area, metaAlta, metaBassa, bordo, xSegnale, xBarra, larghezzaMassimaBarra, righe);
	}

	/**
	 * Il nome (con il numero, se ce n'è più d'uno della stessa specie), accorciato finché ci sta.
	 */
	private String nomeEntro(VistaPersonaggio personaggio, int larghezzaMassima) {
		String nome = personaggio.getNome(VistaPersonaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		while (nome.length() > 1 && FontTool.getWidth(fontMedium, nome) > larghezzaMassima) {
			nome = nome.substring(0, nome.length() - 1);
		}
		return nome;
	}

	/**
	 * Calcola dinamicamente il colore della barra della salute in base ai punti vita rimasti (0xRRGGBB).
	 * Sfuma fluidamente: Verde (100%) &rarr; Giallo (50%) &rarr; Arancione (25%) &rarr; Rosso (0%).
	 */
	static int determinaColoreSalute(double percentuale) {
		percentuale = Math.max(0.0d, Math.min(1.0d, percentuale));
		int r;
		int g;
		if (percentuale >= 0.5d) {
			// Da verde (1.0) a giallo (0.5): il verde è fisso al massimo, il rosso sale man mano che la salute scende
			r = (int) (((1.0d - percentuale) / 0.5d) * 255);
			g = 255;
		} else if (percentuale >= 0.25d) {
			// Da giallo (0.5) ad arancione (0.25): il rosso è fisso al massimo, il verde scende da 255 a 128
			r = 255;
			g = 255 - (int) (((0.5d - percentuale) / 0.25d) * 127);
		} else {
			// Da arancione (0.25) a rosso (0.0): il verde crolla da 128 a 0
			r = 255;
			g = 128 - (int) (((0.25d - percentuale) / 0.25d) * 128);
		}
		final double fattoreOscuramento = 0.65d;
		r = (int) (r * fattoreOscuramento);
		g = (int) (g * fattoreOscuramento);
		return (r << 16) | (g << 8);
	}
}
