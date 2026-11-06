package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra.Tasto;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;
import com.threeamigos.foresta.ui.schermate.ScambioDeiConsumabili;
import com.threeamigos.foresta.ui.schermate.ScambioDeiConsumabili.Voce;
import com.threeamigos.foresta.ui.schermate.SchermataDiScambio;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le liste dell'alchimista, sui dati di prova: il modello è del modulo comune, qui si prova con la partita e il listino finti di libGDX.
 */
class ScambioDeiConsumabiliTest {

	private VistaPartita partita;
	private ScambioDeiConsumabili scambio;
	private SchermataDiProva schermata;
	private final List<DescrizioneSpriteATempo> sprite = new ArrayList<>();

	/** Una schermata di scambio senza disegno, che lascia vedere dove sta la lista di destra. */
	private static final class SchermataDiProva extends SchermataDiScambio {
		SchermataDiProva(VistaPartita partita, MisureDelleImmagini misure) {
			super(800, 480, partita, 300, 400, misure);
		}

		int xDestra() {
			return xMinimaZonaDestra;
		}

		@Override
		public TipoPersonaggio getMestiereAlCentro() {
			return TipoPersonaggio.ALCHIMISTA;
		}

		@Override
		public String getEtichettaMestiere() {
			return "Alchimista";
		}
	}

	@BeforeEach
	void prepara() {
		partita = DatiDiProva.partita();
		MisureDelleImmagini misure = new MisureDelleImmagini() {
			@Override
			public Misura personaggio(TipoPersonaggio classe) {
				return new Misura(40, 60);
			}

			@Override
			public Misura immagine(String risorsa) {
				return new Misura(27, 28);
			}
		};
		schermata = new SchermataDiProva(partita, misure);
		scambio = new ScambioDeiConsumabili(schermata, partita, misure, sprite::add);
		scambio.impostaOfferte(DatiDiProva.offerteDelAlchimista());
	}

	@Test
	void ilGruppoMostraSoloQuelloCheHaConLaQuantita() {
		List<Voce> gruppo = scambio.getElencoGruppo();
		// Nel gruppo di prova: fuoco 3, gelo 12, pozioni di salute 5, di magia 2 (la panacea e il resto: nessuno)
		assertEquals(4, gruppo.size());
		assertEquals(TipoIncantesimo.FUOCO, gruppo.get(0).getTipoIncantesimo());
		assertEquals(3, gruppo.get(0).getQuantita());
		assertEquals(12, gruppo.get(1).getQuantita());
		assertEquals(TipoConsumabile.POZIONE_SALUTE, gruppo.get(2).getTipo());
		assertEquals(5, gruppo.get(2).getQuantita());
	}

	@Test
	void l_alchimistaVendeTuttoIlListinoUnoAllaVolta() {
		List<Voce> venditore = scambio.getElencoVenditore();
		assertEquals(5, venditore.size());
		for (Voce voce : venditore) {
			assertEquals(1, voce.getQuantita());
		}
	}

	@Test
	void l_alberoHaUnaVocePerConsumabileEUnSeparatoreInCima() {
		ElencoScorrevole<Voce> elenco = scambio.costruisciElenco(scambio.getElencoVenditore(), null, true);
		java.util.Set<Voce> trovate = new java.util.LinkedHashSet<>();
		int primaQuota = -1;
		for (int quota = 0; quota < elenco.altezzaDellElenco(10_000); quota++) {
			Voce voce = elenco.riferimentoTitoloAllaQuota(quota);
			if (voce != null) {
				trovate.add(voce);
				if (primaQuota < 0) {
					primaQuota = quota;
				}
			}
		}
		assertEquals(5, trovate.size());
		assertTrue(primaQuota > 0, "sopra la prima voce c'è il separatore degli incantesimi");
	}

	@Test
	void conIlDoppioClickSiCompraSoloColSinistro() {
		List<Object> comandi = new ArrayList<>();
		BusEventi.iscriviti(ComandoAcquistoConsumabile.class, comandi::add);
		// La prima voce del listino sta in alto nella lista di destra, dopo il separatore
		int x = schermata.xDestra() + 30;
		for (int y = 20; y < 120; y += 4) {
			scambio.processaDoppioClick(x, y, Tasto.DESTRO);
		}
		assertTrue(comandi.isEmpty(), "il tasto destro non compra");
		for (int y = 20; y < 120; y += 4) {
			scambio.processaDoppioClick(x, y, Tasto.SINISTRO);
		}
		assertFalse(comandi.isEmpty(), "il tasto sinistro su una voce compra");
	}

	@Test
	void ilCliccoApreEChiudeLaDescrizioneEL_aiutoLoDice() {
		int x = schermata.xDestra() + 30;
		String prima = null;
		for (int y = 20; y < 120 && prima == null; y += 4) {
			schermata.processaMovimento(x, y);
			List<String> righe = scambio.righeAiuto();
			if (righe.size() >= 2) {
				prima = righe.get(0);
			}
		}
		assertNotNull(prima, "sopra una voce dell'alchimista l'aiuto dice cosa fanno click e doppio click");
	}
}
