package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloPersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.tipi.TipoAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class DisplayableCanvasRiquadroGruppo implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE = 16;

	private final int topLeftX;
	private final int topLeftY;
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

	private final int personaggiVisibili;
	private int saltaPrimi = 0;

	// Dove sta il mouse, relativo al riquadro, per l'aiuto (-1 fuori)
	private int mouseX = -1;
	private int mouseY = -1;

	DisplayableCanvasRiquadroGruppo(int topLeftX, int topLeftY) {
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

		personaggiVisibili = (ImageCache.corniceGrande.getHeight() - (DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE << 1)) / (fontMedium.getHeight() * 3);
	}

	void disegnaStatus(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);
		
		GruppoGiocatore g = GruppoGiocatore.getIstanza();
		int locXOffset = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
		int locYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;

		int l = g.getNumeroPersonaggi();
		List<Personaggio> ospiti = g.getOspiti();
		saltaPrimi = Math.min(saltaPrimi, Math.max(0, l + ospiti.size() - personaggiVisibili));
		Personaggio p;
		for (int i = 0; i < l; i++) {
			if (i < saltaPrimi) {
				continue;
			}
			if (i - saltaPrimi >= personaggiVisibili) {
				break;
			}
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
				doomdark = ImageCache.get(p.getSaluteMassima(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetSaluteMassima - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("Mg:", fontMedium, color), leftXOffsetLabelMagia, locYOffset, null);
				doomdark = ImageCache.get(p.getMagia(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetMagia - doomdark.getWidth(null), locYOffset, null);
				graphics.drawImage(ImageCache.get("/", fontMedium, color), leftXOffsetSeparatoreMagia, locYOffset, null);
				doomdark = ImageCache.get(p.getMagiaMassima(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetMagiaMassima - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("Lv:", fontMedium, color), leftXOffsetLabelLivello, locYOffset, null);
				doomdark = ImageCache.get(p.getLivello(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetLivello - doomdark.getWidth(null), locYOffset, null);

				locYOffset += fontMedium.getHeight();
				graphics.drawImage(ImageCache.get("Cr:", fontMedium, color), leftXOffsetLabelCoraggio, locYOffset, null);
				doomdark = ImageCache.get(p.getCoraggio(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetCoraggio - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("Vl:", fontMedium, color), leftXOffsetLabelValore, locYOffset, null);
				doomdark = ImageCache.get(p.getValore(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetValore - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("St:", fontMedium, color), leftXOffsetLabelStanchezza, locYOffset, null);
				doomdark = ImageCache.get(p.getStanchezza(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetStanchezza - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(ImageCache.get("Ca:", fontMedium, color), leftXOffsetLabelCarisma, locYOffset, null);
				doomdark = ImageCache.get(p.getCarisma(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetCarisma - doomdark.getWidth(null), locYOffset, null);

				locYOffset += fontMedium.getHeight();
			}
		}
		// Dopo i personaggi, gli ospiti: solo il nome, perché non combattono
		for (int i = 0; i < ospiti.size(); i++) {
			int riga = l + i - saltaPrimi;
			if (riga < 0) {
				continue;
			}
			if (riga >= personaggiVisibili) {
				break;
			}
			Personaggio ospite = ospiti.get(i);
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
			locYOffset += fontMedium.getHeight() * 2;
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

	/**
	 * A differenza di {@link #getOrdinalePersonaggio}, riporta la riga a cui il personaggio è
	 * effettivamente disegnato, tenendo conto dello scorrimento; -1 se il personaggio non è
	 * visibile nella porzione corrente del riquadro.
	 */
	private int getRigaVisibilePersonaggio(Personaggio personaggio) {
		int ordinale = getOrdinalePersonaggio(personaggio);
		if (ordinale == -1) {
			return -1;
		}
		int riga = ordinale - saltaPrimi;
		return (riga < 0 || riga >= personaggiVisibili) ? -1 : riga;
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
	 * tutti (più di personaggiVisibili, cioè 5). Va chiamato dopo aver disegnato tutto lo schermo, grande
	 * larghezzaSchermo x altezzaSchermo.
	 */
	void disegnaAiuto(Graphics2D graphics, int larghezzaSchermo, int altezzaSchermo) {
		if (!ModelloDati.getIstanza().isAiutoAbilitato() || mouseX < 0) {
			return;
		}
		GruppoGiocatore g = GruppoGiocatore.getIstanza();
		if (g.getNumeroPersonaggi() + g.getOspiti().size() > personaggiVisibili) {
			Cartiglio.disegnaAccantoAlMouse(graphics, Collections.singletonList(Cartiglio.AIUTO_ROTELLA),
					topLeftX + mouseX, topLeftY + mouseY, larghezzaSchermo, altezzaSchermo);
		}
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (movimentoRotella == MovimentoRotella.SU) {
			saltaPrimi = Math.max(0, saltaPrimi - numeroRotazioni);
		} else if (movimentoRotella == MovimentoRotella.GIU) {
			saltaPrimi += numeroRotazioni;
		}
	}

	void gestisciEventoAumentoLivelloPersonaggio(NotificaAumentoLivelloPersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneLivello(evento.getPersonaggio(),
				evento.getLivelloAttuale() - evento.getLivelloPrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneLivello(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getRigaVisibilePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteAumentoLivello;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (rigaPersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetLivello, y, "Livello variato");
	}

	void gestisciEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
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

	private void gestisciEventoVariazioneSalute(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneSalute(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneSalute(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getRigaVisibilePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (rigaPersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetSalute, y, "Salute variata");
	}

	private void gestisciEventoVariazioneSaluteMassima(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneSaluteMassima(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneSaluteMassima(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getRigaVisibilePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (rigaPersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetSaluteMassima, y, "Salute massima variata");
	}

	private void gestisciEventoVariazioneMagia(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneMagia(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneMagia(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getRigaVisibilePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMagia;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (rigaPersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetMagia, y, "Magia variata");
	}

	private void gestisciEventoVariazioneMagiaMassima(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneMagiaMassima(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneMagiaMassima(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getRigaVisibilePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMagia;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (rigaPersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetMagiaMassima, y, "Magia massima variata");
	}

	private void gestisciEventoVariazioneCoraggio(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneCoraggio(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneCoraggio(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getRigaVisibilePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (rigaPersonaggio * 3 + 2);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetCoraggio, y, "Coraggio variato");
	}

	private void gestisciEventoVariazioneValore(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneValore(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneValore(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getRigaVisibilePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (rigaPersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetValore, y, "Valore variato");
	}

	private void gestisciEventoVariazioneCarisma(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneCarisma(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneCarisma(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getRigaVisibilePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteAmicizia;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (rigaPersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetCarisma, y, "Carisma variato");
	}

	private void gestisciEventoVariazioneStanchezza(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneStanchezza(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneStanchezza(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int rigaPersonaggio = getRigaVisibilePersonaggio(personaggio);
		if (rigaPersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (rigaPersonaggio * 3 + 2);
		DoomdarkColorModel.Color color = variazione < 0 ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
		return new SpriteATempo(icona, variazione, fontMedium, color, rightXOffsetStanchezza, y, "Stanchezza variata");
	}

	private void gestisciEventoVariazioneTempo(NotificaVariazioneStatistichePersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneTempo(evento.getPersonaggio(),
				(int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
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
