package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoAumentoLivelloPersonaggio;
import com.threeamigos.foresta.eventi.EventoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.EventoVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class DisplayableCanvasRiquadroGruppo implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE = 16;

	private final int topLeftX;
	private final int topLeftY;
	private final int innerWidth;
	private final Map<Object, Image> lightGrayMap = new HashMap<>();
	private final Map<Object, Image> mediumGrayMap = new HashMap<>();
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

	DisplayableCanvasRiquadroGruppo(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		innerWidth = ImageCache.corniceGrande.getWidth() - (DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE << 1);
		lightGrayMap.put(TipoAttributo.SALUTE, DoomdarkTextProducer.getImage("Sl:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(TipoAttributo.MAGIA, DoomdarkTextProducer.getImage("Mg:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(TipoAttributo.LIVELLO, DoomdarkTextProducer.getImage("Lv:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(TipoAttributo.CORAGGIO, DoomdarkTextProducer.getImage("Cr:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(TipoAttributo.VALORE, DoomdarkTextProducer.getImage("Vl:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(TipoAttributo.STANCHEZZA, DoomdarkTextProducer.getImage("St:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(TipoAttributo.CARISMA, DoomdarkTextProducer.getImage("Ca:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put("/", DoomdarkTextProducer.getImage("/", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		mediumGrayMap.put(TipoAttributo.SALUTE, DoomdarkTextProducer.getImage("Sl:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(TipoAttributo.MAGIA, DoomdarkTextProducer.getImage("Mg:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(TipoAttributo.LIVELLO, DoomdarkTextProducer.getImage("Lv:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(TipoAttributo.CORAGGIO, DoomdarkTextProducer.getImage("Cr:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(TipoAttributo.VALORE, DoomdarkTextProducer.getImage("Vl:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(TipoAttributo.STANCHEZZA, DoomdarkTextProducer.getImage("St:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(TipoAttributo.CARISMA, DoomdarkTextProducer.getImage("Ca:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put("/", DoomdarkTextProducer.getImage("/", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		
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
	}

	void disegnaStatus(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);
		
		GruppoGiocatore g = GruppoGiocatore.getIstanza();
		int locXOffset = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
		int locYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;

		int l = g.getNumeroPersonaggi();
		Personaggio p;
		for (int i = 0; i < l; i++) {
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
				doomdark = DoomdarkTextProducer.getImage(nome, fontMedium, DoomdarkColorModel.Color.DARK_GRAY);
				graphics.drawImage(doomdark, locXOffset, locYOffset, null);
				locYOffset += fontMedium.getHeight();
				doomdark = DoomdarkTextProducer.getImage(p.getCausaTrapasso(), fontMedium, DoomdarkColorModel.Color.DARK_GRAY, innerWidth);
				graphics.drawImage(doomdark, locXOffset, locYOffset, null);
				locYOffset += fontMedium.getHeight() * 2;
			} else {
				DoomdarkColorModel.Color color = i % 2 == 0 ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.LIGHT_GRAY;
				Map<Object, Image> imageMap = i % 2 == 0 ? mediumGrayMap : lightGrayMap;
				doomdark = DoomdarkTextProducer.getImage(nome, fontMedium, color);
				graphics.drawImage(doomdark, locXOffset, locYOffset, null);

				locYOffset += fontMedium.getHeight();
				graphics.drawImage(imageMap.get(TipoAttributo.SALUTE), leftXOffsetLabelSalute, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getSalute(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetSalute - doomdark.getWidth(null), locYOffset, null);
				graphics.drawImage(imageMap.get("/"), leftXOffsetSeparatoreSalute, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getSaluteMassima(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetSaluteMassima - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(TipoAttributo.MAGIA), leftXOffsetLabelMagia, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getMagia(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetMagia - doomdark.getWidth(null), locYOffset, null);
				graphics.drawImage(imageMap.get("/"), leftXOffsetSeparatoreMagia, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getMagiaMassima(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetMagiaMassima - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(TipoAttributo.MAGIA), leftXOffsetLabelMagia, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getMagia(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetMagia - doomdark.getWidth(null), locYOffset, null);
				graphics.drawImage(imageMap.get("/"), leftXOffsetSeparatoreMagia, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getMagiaMassima(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetMagiaMassima - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(TipoAttributo.LIVELLO), leftXOffsetLabelLivello, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getLivello(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetLivello - doomdark.getWidth(null), locYOffset, null);

				locYOffset += fontMedium.getHeight();
				graphics.drawImage(imageMap.get(TipoAttributo.CORAGGIO), leftXOffsetLabelCoraggio, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getCoraggio(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetCoraggio - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(TipoAttributo.VALORE), leftXOffsetLabelValore, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getValore(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetValore - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(TipoAttributo.STANCHEZZA), leftXOffsetLabelStanchezza, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getStanchezza(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetStanchezza - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(TipoAttributo.CARISMA), leftXOffsetLabelCarisma, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getCarisma(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetCarisma - doomdark.getWidth(null), locYOffset, null);

				locYOffset += fontMedium.getHeight();
			}
		}
	}

	private int getOrdinalePersonaggio(Personaggio personaggio) {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		for (int i = 0; i < gruppo.getNumeroPersonaggi(); i++) {
			if (gruppo.getPersonaggio(i).equals(personaggio)) {
				return i;
			}
		}
		return -1;
	}

	void gestisciEventoAumentoLivelloPersonaggio(EventoAumentoLivelloPersonaggio evento) {
		BusEventi.pubblica(new EventoCreazioneSpriteATempo(costruisciSpritePerVariazioneLivello(evento.getPersonaggio(),
				evento.getLivelloAttuale() - evento.getLivelloPrecedente())));
	}

	private SpriteATempo costruisciSpritePerVariazioneLivello(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteAumentoLivello;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetLivello, y, "Livello variato");
	}

	void gestisciEventoVariazioneStatistichePersonaggio(EventoVariazioneStatistichePersonaggio evento) {
		if (!GruppoGiocatore.getIstanza().contiene(evento.getPersonaggio())) {
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

	private void gestisciEventoVariazioneSalute(EventoVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneSalute(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new EventoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneSalute(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetSalute, y, "Salute variata");
	}

	private void gestisciEventoVariazioneSaluteMassima(EventoVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneSaluteMassima(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new EventoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneSaluteMassima(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetSaluteMassima, y, "Salute massima variata");
	}

	private void gestisciEventoVariazioneMagia(EventoVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneMagia(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new EventoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneMagia(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMagia;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetMagia, y, "Magia variata");
	}

	private void gestisciEventoVariazioneMagiaMassima(EventoVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneMagiaMassima(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new EventoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneMagiaMassima(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMagia;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetMagiaMassima, y, "Magia massima variata");
	}

	private void gestisciEventoVariazioneCoraggio(EventoVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneCoraggio(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new EventoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneCoraggio(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 2);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetCoraggio, y, "Coraggio variato");
	}

	private void gestisciEventoVariazioneValore(EventoVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneValore(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new EventoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneValore(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetValore, y, "Valore variato");
	}

	private void gestisciEventoVariazioneCarisma(EventoVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneCarisma(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new EventoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneCarisma(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteAmicizia;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetCarisma, y, "Carisma variato");
	}

	private void gestisciEventoVariazioneStanchezza(EventoVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneStanchezza(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new EventoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneStanchezza(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 2);
		DoomdarkColorModel.Color color = variazione < 0 ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
		return new SpriteATempo(icona, variazione, fontMedium, color, rightXOffsetStanchezza, y, "Stanchezza variata");
	}

	private void gestisciEventoVariazioneTempo(EventoVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneTempo(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new EventoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneTempo(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteTempo;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + ordinalePersonaggio * fontMedium.getHeight() * 3;
		return new SpriteATempo(icona, variazione, fontMedium,
				topLeftX + ((ImageCache.corniceGrande.getWidth() - (DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE << 1)) >> 1),
				y, "Tempo variato");
	}
}
