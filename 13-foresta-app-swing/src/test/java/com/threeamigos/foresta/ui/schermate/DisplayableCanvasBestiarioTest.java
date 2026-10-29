package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.abilita.AbilitaInnateDiClasse;
import com.threeamigos.foresta.bestiario.RegistroBestiario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.interfacce.GestoreBestiario;
import com.threeamigos.foresta.modellodati.BestiarioMD;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.personaggi.MoltiplicatoriDiClasse;
import com.threeamigos.foresta.tipi.AbilitaInnataDiClasse;
import com.threeamigos.foresta.tipi.CaratteristicaDiClasse;
import com.threeamigos.foresta.tipi.MoltiplicatoreDiClasse;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;
import com.threeamigos.foresta.ui.testo.DoomdarkFontSmall;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il bestiario a schermo: si disegna con tutte le voci chiuse e con tutte aperte, un clic su una riga ne sceglie la voce, e tutti i testi che mostra
 * (descrizioni, note dei moltiplicatori, caratteristiche, abilità) si possono scrivere con i font del gioco.
 */
class DisplayableCanvasBestiarioTest {

	private static final int LARGHEZZA = 1024;
	private static final int ALTEZZA = 700;

	private static final GestoreBestiario SENZA_FILE = new GestoreBestiario() {
		@Override
		public boolean carica(BestiarioMD bestiarioMD) {
			return true;
		}

		@Override
		public boolean salva(BestiarioMD bestiarioMD) {
			return true;
		}
	};

	@BeforeEach
	void bestiarioVuoto() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		RegistroBestiario.impostaGestoreBestiario(SENZA_FILE);
		RegistroBestiario.registrati();
	}

	@AfterEach
	void ripristinaBus() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
	}

	private static Graphics2D tela() {
		return new BufferedImage(LARGHEZZA, ALTEZZA, BufferedImage.TYPE_INT_ARGB).createGraphics();
	}

	@Test
	void conTutteLeVociChiuseSiDisegnaSenzaErrori() {
		DisplayableCanvasBestiario bestiario = new DisplayableCanvasBestiario(LARGHEZZA, ALTEZZA, new VistaPartitaMotore());

		bestiario.apri();
		bestiario.disegna(tela());

		assertEquals(TipoPersonaggio.getVociDelBestiario().get(0), bestiario.getVoceScelta());
	}

	@Test
	void conTutteLeVociApertePerOgniVoceLaSchedaSiCostruisceESiDisegna() {
		for (TipoPersonaggio voce : TipoPersonaggio.getVociDelBestiario()) {
			BusEventi.pubblica(new InternoAvversarioSconfitto(voce));
		}
		assertEquals(TipoPersonaggio.getVociDelBestiario().size(), RegistroBestiario.getNumeroVociSbloccate());
		DisplayableCanvasBestiario bestiario = new DisplayableCanvasBestiario(LARGHEZZA, ALTEZZA, new VistaPartitaMotore());
		bestiario.apri();

		for (int riga = 0; riga < TipoPersonaggio.getVociDelBestiario().size(); riga++) {
			// Un clic nella colonna di sinistra, sulla riga: la sceglie (la prima riga sta sotto il titolo)
			bestiario.processaClick(60, 100 + riga * ImmagineBestiario.ALTEZZA_RIGA_ELENCO, Finestra.Tasto.SINISTRO);
			bestiario.disegna(tela());
		}
	}

	@Test
	void unClicSuUnaRigaSceglieLaVoceEUnoFuoriDallElencoNo() {
		DisplayableCanvasBestiario bestiario = new DisplayableCanvasBestiario(LARGHEZZA, ALTEZZA, new VistaPartitaMotore());
		bestiario.apri();
		List<TipoPersonaggio> voci = TipoPersonaggio.getVociDelBestiario();

		// L'area dell'elenco comincia a 30 pixel dal bordo e sotto il titolo: 20 + 36 + 20
		bestiario.processaClick(60, 76 + 2 * ImmagineBestiario.ALTEZZA_RIGA_ELENCO + 5, Finestra.Tasto.SINISTRO);

		assertEquals(voci.get(2), bestiario.getVoceScelta());

		bestiario.processaClick(LARGHEZZA - 40, 400, Finestra.Tasto.SINISTRO);

		assertEquals(voci.get(2), bestiario.getVoceScelta(), "un clic sulla scheda non cambia la voce");
	}

	@Test
	void laRotellaScorreIlPannelloSottoIlMouseSenzaCambiareLaVoce() {
		DisplayableCanvasBestiario bestiario = new DisplayableCanvasBestiario(LARGHEZZA, ALTEZZA, new VistaPartitaMotore());
		bestiario.apri();

		bestiario.processaRotella(60, 200, 3, Finestra.MovimentoRotella.GIU);
		bestiario.processaRotella(700, 200, 3, Finestra.MovimentoRotella.GIU);
		bestiario.processaRotella(700, 200, 50, Finestra.MovimentoRotella.SU);
		bestiario.disegna(tela());

		assertEquals(TipoPersonaggio.getVociDelBestiario().get(0), bestiario.getVoceScelta());
		assertEquals(Finestra.PASSI_PER_SCATTO_DELLE_LISTE, bestiario.passiPerScattoDellaRotella());
	}

	/**
	 * Tutti i caratteri dei testi del bestiario: se il font non ne ha uno lo salta e segnala un errore (vedi DoomdarkTextProducer)
	 */
	@Test
	void iTestiDelBestiarioSiScrivonoConIFontDelGioco() {
		List<String> testi = new ArrayList<>();
		for (TipoPersonaggio voce : TipoPersonaggio.getVociDelBestiario()) {
			testi.add(voce.getDescrizione());
			testi.add(voce.getNomeDellaVoce());
			for (CaratteristicaDiClasse caratteristica : voce.getCaratteristiche()) {
				testi.add(caratteristica.getDescrizione());
			}
			TipoPersonaggio femminile = voce.getFormaFemminile();
			for (TipoPersonaggio forma : femminile == null ? new TipoPersonaggio[]{voce} : new TipoPersonaggio[]{voce, femminile}) {
				for (MoltiplicatoreDiClasse moltiplicatore : MoltiplicatoriDiClasse.di(forma)) {
					testi.add(moltiplicatore.getNome());
					testi.add(moltiplicatore.getNota());
					testi.add("x" + moltiplicatore.getMoltiplicatoreInTesto());
				}
				for (AbilitaInnataDiClasse abilita : AbilitaInnateDiClasse.di(forma)) {
					testi.add(abilita.getNome());
					testi.add(abilita.getDescrizione());
				}
			}
		}
		testi.add("Abilità innate");
		testi.add("Forze e debolezze");
		testi.add("Carattere");
		testi.add("Niente di particolare.");
		testi.add("12 ancora da scoprire");
		testi.add("???");
		testi.add("12/32");

		for (DoomdarkFont font : new DoomdarkFont[]{DoomdarkFontMedium.getInstance(), DoomdarkFontSmall.getInstance()}) {
			for (String testo : testi) {
				for (char c : testo.toCharArray()) {
					assertTrue(font.supportaIlCarattere(c) || c == ' ',
							"Il carattere '" + c + "' (" + (int) c + ") non c'è nel font, nel testo: " + testo);
				}
			}
		}
	}
}
