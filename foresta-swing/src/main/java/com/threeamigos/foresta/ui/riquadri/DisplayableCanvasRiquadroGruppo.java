package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloPersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.barra.Cartiglio;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Il riquadro con lo stato dei personaggi del gruppo: nome, salute, magia e le altre statistiche. Si aggiorna ascoltando gli eventi di variazione.
 */
public class DisplayableCanvasRiquadroGruppo implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE = 16;

	private final int topLeftX;
	private int topLeftY;
	private final int innerWidth;
	private final DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
	private final int leftXOffsetLabelSalute;
	private final int rightXOffsetSalute;
	private final int leftXOffsetSeparatoreSalute;
	private final int rightXOffsetSaluteMassima;
	private final int leftXOffsetLabelMagia;
	private final int rightXOffsetMagia;
	private final int leftXOffsetSeparatoreMagia;
	private final int rightXOffsetMagiaMassima;
	private final int leftXOffsetLabelLivello;
	private final int rightXOffsetLivello;

	private final int leftXOffsetLabelCoraggio;
	private final int rightXOffsetCoraggio;
	private final int leftXOffsetLabelValore;
	private final int rightXOffsetValore;
	private final int leftXOffsetLabelStanchezza;
	private final int rightXOffsetStanchezza;
	private final int leftXOffsetLabelCarisma;
	private final int rightXOffsetCarisma;

	// Ogni personaggio (o ospite) occupa tre righe
	private final int altezzaBlocco;
	private final Rectangle areaLista;
	// Pixel di scorrimento per ogni scatto della rotella, come per le missioni
	private static final int PASSO_SCORRIMENTO = 2;
	// Di quanti pixel è scrollata la lista
	private int offsetY = 0;

	// Dove sta il mouse, relativo al riquadro, per l'aiuto (-1 fuori)
	private int mouseX = -1;
	private int mouseY = -1;

	private final VistaPartita vistaPartita;

	public DisplayableCanvasRiquadroGruppo(int topLeftX, int topLeftY, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		innerWidth = ImageCache.corniceGrande.getWidth() - (DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE << 1);
		
		int glyph9Width = fontMedium.getGlyphWidth('9');
		
		 // Fr:999/999 Mg:99/99 Lv: 1
		leftXOffsetLabelSalute = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
		rightXOffsetSalute = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 6;
		leftXOffsetSeparatoreSalute = rightXOffsetSalute;
		rightXOffsetSaluteMassima = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 10;
		leftXOffsetLabelMagia = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 11;
		rightXOffsetMagia = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 16;
		leftXOffsetSeparatoreMagia = rightXOffsetMagia;
		rightXOffsetMagiaMassima = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 19;
		leftXOffsetLabelLivello = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 20;
		rightXOffsetLivello = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 24;
		
		//Cr:99 Vl:99 St:99 Ca:99
		leftXOffsetLabelCoraggio = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
		rightXOffsetCoraggio = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 5;
		leftXOffsetLabelValore = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 6;
		rightXOffsetValore = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 11;
		leftXOffsetLabelStanchezza = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 12;
		rightXOffsetStanchezza = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 17;
		leftXOffsetLabelCarisma = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 18;
		rightXOffsetCarisma = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 23;

		altezzaBlocco = fontMedium.getHeight() * 3;
		areaLista = new Rectangle(topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE, topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE,
				innerWidth, ImageCache.corniceGrande.getHeight() - (DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE << 1));
	}

	/**
	 * Sposta il riquadro in verticale: topLeftY è la nuova quota del bordo superiore della cornice
	 */
	public void spostaInVerticale(int topLeftY) {
		this.topLeftY = topLeftY;
		areaLista.y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
	}

	/**
	 * Disegna lo stato dei personaggi del gruppo.
	 */
	public void disegnaStatus(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);
		IntestazioneRiquadro.disegna(graphics, "Gruppo", topLeftX, topLeftY, ImageCache.corniceGrande.getWidth());

		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		int locXOffset = areaLista.x;

		int l = g.getNumeroPersonaggi();
		List<? extends VistaPersonaggio> ospiti = g.getOspiti();
		// L'elenco può essere cambiato dall'ultimo scorrimento: l'offset va rimesso nei limiti
		offsetY = Math.min(offsetY, offsetMassimo());
		Shape clipOriginale = graphics.getClip();
		graphics.setClip(areaLista);
		VistaPersonaggio p;
		for (int i = 0; i < l; i++) {
			if (!bloccoVisibile(i)) {
				continue;
			}
			int locYOffset = yDelBlocco(i);
			p = g.getPersonaggio(i);
			Image doomdark;
			Optional<String> nomeOpt = p.getNomeProprio();
			String nome;
			if (nomeOpt.isPresent()) {
				nome = nomeOpt.get() + "-" + p.getNomeSingolare();
			} else {
				nome = p.getNomeSingolare();
			}
			if (!p.isVivo()) {
				doomdark = ImageCache.get(nome, fontMedium, DoomdarkColorModel.Color.DARK_GRAY);
				graphics.drawImage(doomdark, locXOffset, locYOffset, null);
				locYOffset += fontMedium.getHeight();
				doomdark = ImageCache.get(p.getCausaTrapasso(), fontMedium, DoomdarkColorModel.Color.DARK_GRAY, innerWidth);
				graphics.drawImage(doomdark, locXOffset, locYOffset, null);
				locYOffset += fontMedium.getHeight() * 2;
			} else {
				DoomdarkColorModel.Color color = i % 2 == 0 ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.LIGHT_GRAY;
				doomdark = ImageCache.get(nome, fontMedium, color);
				graphics.drawImage(doomdark, locXOffset, locYOffset, null);

				locYOffset += fontMedium.getHeight();
				graphics.drawImage(ImageCache.get("Sl:", fontMedium, color), leftXOffsetLabelSalute, locYOffset, null);
				doomdark = ImageCache.get(p.getSalute(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetSalute - doomdark.getWidth(null), locYOffset, null);
				graphics.drawImage(ImageCache.get("/", fontMedium, color), leftXOffsetSeparatoreSalute, locYOffset, null);
				doomdark = ImageCache.get(p.getSaluteMassima(), fontMedium, colorePerAttributo(p, TipoAttributo.SALUTE, color));
				graphics.drawImage(doomdark, rightXOffsetSaluteMassima - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("Mg:", fontMedium, color), leftXOffsetLabelMagia, locYOffset, null);
				doomdark = ImageCache.get(p.getMagia(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetMagia - doomdark.getWidth(null), locYOffset, null);
				graphics.drawImage(ImageCache.get("/", fontMedium, color), leftXOffsetSeparatoreMagia, locYOffset, null);
				doomdark = ImageCache.get(p.getMagiaMassima(), fontMedium, colorePerAttributo(p, TipoAttributo.MAGIA, color));
				graphics.drawImage(doomdark, rightXOffsetMagiaMassima - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("Lv:", fontMedium, color), leftXOffsetLabelLivello, locYOffset, null);
				doomdark = ImageCache.get(p.getLivello(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetLivello - doomdark.getWidth(null), locYOffset, null);

				locYOffset += fontMedium.getHeight();
				graphics.drawImage(ImageCache.get("Cr:", fontMedium, color), leftXOffsetLabelCoraggio, locYOffset, null);
				doomdark = ImageCache.get(p.getCoraggio(), fontMedium, colorePerAttributo(p, TipoAttributo.CORAGGIO, color));
				graphics.drawImage(doomdark, rightXOffsetCoraggio - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("Vl:", fontMedium, color), leftXOffsetLabelValore, locYOffset, null);
				doomdark = ImageCache.get(p.getValore(), fontMedium, colorePerAttributo(p, TipoAttributo.VALORE, color));
				graphics.drawImage(doomdark, rightXOffsetValore - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("St:", fontMedium, color), leftXOffsetLabelStanchezza, locYOffset, null);
				doomdark = ImageCache.get(p.getStanchezza(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetStanchezza - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("Ca:", fontMedium, color), leftXOffsetLabelCarisma, locYOffset, null);
				doomdark = ImageCache.get(p.getCarisma(), fontMedium, colorePerAttributo(p, TipoAttributo.CARISMA, color));
				graphics.drawImage(doomdark, rightXOffsetCarisma - doomdark.getWidth(null), locYOffset, null);

				locYOffset += fontMedium.getHeight();
			}
		}
		// Dopo i personaggi, gli ospiti: solo il nome, perché non combattono
		for (int i = 0; i < ospiti.size(); i++) {
			if (!bloccoVisibile(l + i)) {
				continue;
			}
			int locYOffset = yDelBlocco(l + i);
			VistaPersonaggio ospite = ospiti.get(i);
			String nome = ospite.getNomeProprio().map(n -> n + "-" + ospite.getNomeSingolare()).orElse(ospite.getNomeSingolare());
			DoomdarkColorModel.Color colore = ospite.isVivo() ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.DARK_GRAY;
			graphics.drawImage(ImageCache.get(nome, fontMedium, colore), locXOffset, locYOffset, null);
			locYOffset += fontMedium.getHeight();
			// Di un ospite che si può ferire si vede la salute
			String stato;
			if (!ospite.isVivo()) {
				stato = ospite.getCausaTrapasso();
			} else if (g.isOspiteVulnerabile(ospite)) {
				stato = "Ospite, salute " + ospite.getSalute() + "/" + ospite.getSaluteMassima();
			} else {
				stato = "Ospite del gruppo";
			}
			graphics.drawImage(ImageCache.get(stato, fontMedium, DoomdarkColorModel.Color.DARK_GRAY, innerWidth), locXOffset, locYOffset, null);
		}
		disegnaFrecce(graphics);
		graphics.setClip(clipOriginale);
	}

	private int numeroBlocchi() {
		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		return g.getNumeroPersonaggi() + g.getOspiti().size();
	}

	private int offsetMassimo() {
		return Math.max(0, numeroBlocchi() * altezzaBlocco - areaLista.height);
	}

	/**
	 * La riga del blocco di un personaggio in cui è scritto un valore: 0 il nome, 1 salute, magia e livello, 2 coraggio, valore, stanchezza e
	 * carisma (vedi disegnaStatus). Serve a mettere lo sprite di una variazione sopra il valore che cambia.
	 */
	static int rigaDelValore(TipoAttributo attributo) {
		switch (attributo) {
			case SALUTE:
			case SALUTE_MASSIMA:
			case MAGIA:
			case MAGIA_MASSIMA:
			case LIVELLO:
				return 1;
			case CORAGGIO:
			case VALORE:
			case STANCHEZZA:
			case CARISMA:
				return 2;
			default:
				throw new IllegalArgumentException("Il riquadro del gruppo non mostra " + attributo);
		}
	}

	/**
	 * La quota a cui è disegnato il blocco di un personaggio (o ospite), tenendo conto dello scorrimento: può essere
	 * fuori dalla lista
	 */
	private int yDelBlocco(int indice) {
		return areaLista.y + indice * altezzaBlocco - offsetY;
	}

	private boolean bloccoVisibile(int indice) {
		int y = yDelBlocco(indice);
		return y + altezzaBlocco > areaLista.y && y < areaLista.y + areaLista.height;
	}

	/**
	 * Le frecce delle missioni (vedi ComponenteScorrevole), in alto e in basso a destra se la lista continua da
	 * quella parte
	 */
	private void disegnaFrecce(Graphics2D graphics) {
		Image frecciaSu = ImageCache.componenteScorrevoleFrecciaSu;
		Image frecciaGiu = ImageCache.componenteScorrevoleFrecciaGiu;
		if (offsetY > 0) {
			graphics.drawImage(frecciaSu, areaLista.x + areaLista.width - frecciaSu.getWidth(null) - ImageCache.SPACING,
					areaLista.y + ImageCache.SPACING, null);
		}
		if (offsetY < offsetMassimo()) {
			graphics.drawImage(frecciaGiu, areaLista.x + areaLista.width - frecciaGiu.getWidth(null) - ImageCache.SPACING,
					areaLista.y + areaLista.height - ImageCache.SPACING - frecciaGiu.getHeight(null), null);
		}
	}

	/**
	 * Lo sprite parte dalla quota giusta anche se il blocco non si vede tutto (o per niente) perché la lista è
	 * scrollata: si vede solo la parte dentro la lista
	 */
	private SpriteATempo conRitaglio(SpriteATempo sprite) {
		sprite.setRitaglio(areaLista);
		return sprite;
	}

	/**
	 * Il colore di un valore: giallo se un modificatore porta l'attributo a un valore fisso, verde o rosso se i
	 * modificatori temporanei lo alzano o lo abbassano (i dettagli sono nella scheda del personaggio)
	 */
	private static DoomdarkColorModel.Color colorePerAttributo(VistaPersonaggio p, TipoAttributo attributo, DoomdarkColorModel.Color colore) {
		if (p.getFissaggio(attributo).isPresent()) {
			return DoomdarkColorModel.Color.YELLOW;
		}
		double variazione = p.getVariazioneDaTemporanei(attributo);
		return variazione > 0 ? DoomdarkColorModel.Color.GREEN : variazione < 0 ? DoomdarkColorModel.Color.RED : colore;
	}

	private int getOrdinalePersonaggio(VistaPersonaggio personaggio) {
		VistaGruppoGiocatore gruppo = vistaPartita.getGruppoGiocatore();
		for (int i = 0; i < gruppo.getNumeroPersonaggi(); i++) {
			if (gruppo.getPersonaggio(i).equals(personaggio)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void processaMovimento(int x, int y) {
		mouseX = x;
		mouseY = y;
	}

	@Override
	public void processaUscita(int x, int y) {
		mouseX = -1;
		mouseY = -1;
	}

	/**
	 * Ad aiuto acceso, sopra il riquadro il cartiglio della rotella, se i personaggi (ospiti compresi) non ci stanno
	 * tutti. Va chiamato dopo aver disegnato tutto lo schermo, grande
	 * larghezzaSchermo x altezzaSchermo.
	 */
	public void disegnaAiuto(Graphics2D graphics, int larghezzaSchermo, int altezzaSchermo) {
		if (!vistaPartita.isAiutoAbilitato() || mouseX < 0) {
			return;
		}
		if (offsetMassimo() > 0) {
			Cartiglio.disegnaAccantoAlMouse(graphics, Collections.singletonList(Cartiglio.AIUTO_ROTELLA),
					topLeftX + mouseX, topLeftY + mouseY, larghezzaSchermo, altezzaSchermo);
		}
	}

	@Override
	public int passiPerScattoDellaRotella() {
		return PASSI_PER_SCATTO_DELLE_LISTE;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		int spostamento = numeroRotazioni * PASSO_SCORRIMENTO;
		offsetY = movimentoRotella == MovimentoRotella.SU ? offsetY - spostamento : offsetY + spostamento;
		offsetY = Math.max(0, Math.min(offsetMassimo(), offsetY));
	}

	/**
	 * Segnala sul riquadro che un personaggio è salito di livello.
	 */
	public void gestisciEventoAumentoLivelloPersonaggio(NotificaAumentoLivelloPersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneLivello(evento.getPersonaggio(),
				evento.getLivelloAttuale() - evento.getLivelloPrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneLivello(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getOrdinalePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteAumentoLivello;
		final int y = yDelBlocco(rigaPersonaggio) + fontMedium.getHeight() * rigaDelValore(TipoAttributo.LIVELLO);
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium, rightXOffsetLivello, y, "Livello variato"));
	}

	/**
	 * Aggiorna i valori mostrati di un personaggio e segnala le variazioni.
	 */
	public void gestisciEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
		if (!vistaPartita.getGruppoGiocatore().contiene(evento.getPersonaggio())) {
			return;
		}
		switch (evento.getTipoAttributo()) {
			case SALUTE:
				gestisciEventoVariazioneSalute(evento);
				break;
			case SALUTE_MASSIMA:
				gestisciEventoVariazioneSaluteMassima(evento);
				break;
			case MAGIA:
				gestisciEventoVariazioneMagia(evento);
				break;
			case MAGIA_MASSIMA:
				gestisciEventoVariazioneMagiaMassima(evento);
				break;
			case CORAGGIO:
				gestisciEventoVariazioneCoraggio(evento);
				break;
			case VALORE:
				gestisciEventoVariazioneValore(evento);
				break;
			case CARISMA:
				gestisciEventoVariazioneCarisma(evento);
				break;
			case STANCHEZZA:
				gestisciEventoVariazioneStanchezza(evento);
				break;
			case TEMPO:
				gestisciEventoVariazioneTempo(evento);
				break;
			default:
				//Altri attributi non sono gestiti da questa finestra
				break;
		}
	}

	private void gestisciEventoVariazioneSalute(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneSalute(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneSalute(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getOrdinalePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = yDelBlocco(rigaPersonaggio) + fontMedium.getHeight() * rigaDelValore(TipoAttributo.SALUTE);
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium, rightXOffsetSalute, y, "Salute variata"));
	}

	private void gestisciEventoVariazioneSaluteMassima(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneSaluteMassima(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneSaluteMassima(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getOrdinalePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = yDelBlocco(rigaPersonaggio) + fontMedium.getHeight() * rigaDelValore(TipoAttributo.SALUTE_MASSIMA);
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium, rightXOffsetSaluteMassima, y, "Salute massima variata"));
	}

	private void gestisciEventoVariazioneMagia(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneMagia(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneMagia(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getOrdinalePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMagia;
		final int y = yDelBlocco(rigaPersonaggio) + fontMedium.getHeight() * rigaDelValore(TipoAttributo.MAGIA);
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium, rightXOffsetMagia, y, "Magia variata"));
	}

	private void gestisciEventoVariazioneMagiaMassima(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneMagiaMassima(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneMagiaMassima(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getOrdinalePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMagia;
		final int y = yDelBlocco(rigaPersonaggio) + fontMedium.getHeight() * rigaDelValore(TipoAttributo.MAGIA_MASSIMA);
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium, rightXOffsetMagiaMassima, y, "Magia massima variata"));
	}

	private void gestisciEventoVariazioneCoraggio(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneCoraggio(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneCoraggio(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getOrdinalePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = yDelBlocco(rigaPersonaggio) + fontMedium.getHeight() * rigaDelValore(TipoAttributo.CORAGGIO);
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium, rightXOffsetCoraggio, y, "Coraggio variato"));
	}

	private void gestisciEventoVariazioneValore(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneValore(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneValore(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getOrdinalePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = yDelBlocco(rigaPersonaggio) + fontMedium.getHeight() * rigaDelValore(TipoAttributo.VALORE);
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium, rightXOffsetValore, y, "Valore variato"));
	}

	private void gestisciEventoVariazioneCarisma(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneCarisma(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneCarisma(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getOrdinalePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteAmicizia;
		final int y = yDelBlocco(rigaPersonaggio) + fontMedium.getHeight() * rigaDelValore(TipoAttributo.CARISMA);
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium, rightXOffsetCarisma, y, "Carisma variato"));
	}

	private void gestisciEventoVariazioneStanchezza(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneStanchezza(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneStanchezza(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getOrdinalePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = yDelBlocco(rigaPersonaggio) + fontMedium.getHeight() * rigaDelValore(TipoAttributo.STANCHEZZA);
		DoomdarkColorModel.Color color = variazione < 0 ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium, color, rightXOffsetStanchezza, y, "Stanchezza variata"));
	}

	private void gestisciEventoVariazioneTempo(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneTempo(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneTempo(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteTempo;
		final int y = yDelBlocco(ordinalePersonaggio);
		return conRitaglio(new SpriteATempo(icona, variazione, fontMedium,
				topLeftX + ((ImageCache.corniceGrande.getWidth() - (DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE << 1)) >> 1),
				y, "Tempo variato"));
	}
}
