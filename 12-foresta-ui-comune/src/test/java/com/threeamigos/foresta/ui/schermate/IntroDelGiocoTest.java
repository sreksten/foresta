package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'intro: il logo della Foresta segue la storia (compare sotto la sua ultima riga e sale con lei), si ferma al suo posto mentre la storia continua a
 * salire, e solo allora compare il logo 3AM; poi loghi e sfondo spariscono insieme e si passa alla classifica.
 */
class IntroDelGiocoTest {

	private static final int LARGHEZZA = 800;
	private static final int ALTEZZA = 600;
	private static final double PASSO = 0.05;
	private static final Misura LOGO = new Misura(100, 50);

	private IntroDelGioco intro;

	/** Una vista che sa solo dire la storia: tutto il resto non serve all'intro finché non si arriva ai trofei */
	private static VistaPartita vista() {
		List<String> storia = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			storia.add("Molto tempo fa, in una Foresta lontana lontana, un Drago dormiva sopra un tesoro e sognava città in fiamme e cavalieri in fuga.");
		}
		return (VistaPartita) Proxy.newProxyInstance(IntroDelGiocoTest.class.getClassLoader(), new Class<?>[] {VistaPartita.class}, (proxy, metodo, argomenti) -> {
			if ("getStoria".equals(metodo.getName())) {
				return storia;
			}
			throw new UnsupportedOperationException(metodo.getName());
		});
	}

	private static GestorePunteggi classifica() {
		return (GestorePunteggi) Proxy.newProxyInstance(IntroDelGiocoTest.class.getClassLoader(), new Class<?>[] {GestorePunteggi.class}, (proxy, metodo, argomenti) -> {
			switch (metodo.getName()) {
				case "getConteggio":
					return 0;
				default:
					throw new UnsupportedOperationException(metodo.getName());
			}
		});
	}

	private static final MisureDelleImmagini MISURE = new MisureDelleImmagini() {
		@Override
		public Misura personaggio(TipoPersonaggio classe) {
			return LOGO;
		}

		@Override
		public Misura immagine(String risorsa) {
			return LOGO;
		}
	};

	@BeforeEach
	void preparaLIntro() {
		intro = new IntroDelGioco(LARGHEZZA, ALTEZZA, classifica(), vista(), MISURE);
	}

	private static IntroDelGioco.Logo logo(IntroDelGioco.Quadro quadro, String risorsa) {
		for (IntroDelGioco.Logo logo : quadro.getLoghi()) {
			if (risorsa.equals(logo.getElemento().getRisorsa())) {
				return logo;
			}
		}
		return null;
	}

	/** La quota alla quale il logo della Foresta si ferma: il suo centro è a un terzo dello schermo */
	private int cimaFerma() {
		return ALTEZZA / 3 - (int) Math.round(LOGO.altezza * com.threeamigos.foresta.ui.LivelloDiZoom.valore()) / 2;
	}

	@Test
	void ilLogoDellaForestaSegueLaStoriaESaleConLei() {
		Integer cimaPrecedente = null;
		Integer fondoPrecedente = null;
		boolean visto = false;
		for (int i = 0; i < 4000 && intro.getFase() == IntroDelGioco.Fase.STORIA; i++) {
			IntroDelGioco.Quadro quadro = intro.avanzaDi(PASSO);
			IntroDelGioco.Logo foresta = logo(quadro, IntroDelGioco.RISORSA_LOGO_FORESTA);
			if (foresta == null) {
				continue;
			}
			int cima = foresta.getElemento().getY();
			int fondo = quadro.getColonna().getFondo();
			if (cima > cimaFerma() + 3 * IntroDelGioco.DISTANZA_DI_FRENATA) {
				visto = true;
				// Sta sotto l'ultima riga della storia, sempre alla stessa distanza, e sale con lei
				assertEquals(fondo + 20, cima, "mentre sale sta attaccato alla storia");
				if (cimaPrecedente != null) {
					assertTrue(cima < cimaPrecedente, "sale");
					assertEquals(fondoPrecedente - fondo, cimaPrecedente - cima, 1, "alla stessa velocità della storia");
				}
				// Finché sale il 3AM non c'è
				assertNull(logo(quadro, IntroDelGioco.RISORSA_LOGO_3AM), "il 3AM compare solo quando il logo della Foresta si ferma");
			}
			cimaPrecedente = cima;
			fondoPrecedente = fondo;
		}
		assertTrue(visto, "il logo sale con la storia prima di fermarsi");
	}

	@Test
	void ilLogoSiFermaAlSuoPostoEAlloraCompareIl3AMMentreLaStoriaContinuaASalire() {
		Integer fondoAlloStop = null;
		float opacita3AMPrima = 0;
		boolean fermo = false;
		for (int i = 0; i < 4000 && intro.getFase() == IntroDelGioco.Fase.STORIA; i++) {
			IntroDelGioco.Quadro quadro = intro.avanzaDi(PASSO);
			IntroDelGioco.Logo foresta = logo(quadro, IntroDelGioco.RISORSA_LOGO_FORESTA);
			IntroDelGioco.Logo tremAM = logo(quadro, IntroDelGioco.RISORSA_LOGO_3AM);
			if (foresta != null && foresta.getElemento().getY() <= cimaFerma()) {
				if (!fermo) {
					fermo = true;
					fondoAlloStop = quadro.getColonna().getFondo();
				}
				assertEquals(cimaFerma(), foresta.getElemento().getY(), "fermo, sta dov'era il logo prima di questa modifica");
				assertNotNull(tremAM, "da quando il logo si ferma c'è anche il 3AM");
				assertTrue(tremAM.getOpacita() >= opacita3AMPrima - 0.0001f || quadro.getColonna().isUscito(), "compare in dissolvenza");
				opacita3AMPrima = tremAM.getOpacita();
				// Il 3AM sta in basso a destra, a 20 pixel dai due bordi
				assertEquals(LARGHEZZA - 20, tremAM.getElemento().getX() + tremAM.getElemento().getLarghezza());
				assertEquals(ALTEZZA - 20, tremAM.getElemento().getY() + tremAM.getElemento().getAltezza());
				// Il logo della Foresta è pieno finché la storia non è uscita; poi spariscono insieme, e il 3AM non è mai più opaco di lui
				if (!quadro.getColonna().isUscito()) {
					assertEquals(1f, foresta.getOpacita(), 0.0001f);
				}
				assertTrue(foresta.getOpacita() >= tremAM.getOpacita() - 0.0001f);
			}
		}
		assertTrue(fermo, "il logo si ferma");
		assertNotNull(fondoAlloStop);
	}

	@Test
	void laStoriaContinuaASalireDopoCheIlLogoSiEFermato() {
		IntroDelGioco.Quadro quadro = null;
		int fondoAlloStop = Integer.MIN_VALUE;
		for (int i = 0; i < 4000 && intro.getFase() == IntroDelGioco.Fase.STORIA; i++) {
			quadro = intro.avanzaDi(PASSO);
			IntroDelGioco.Logo foresta = logo(quadro, IntroDelGioco.RISORSA_LOGO_FORESTA);
			if (foresta != null && foresta.getElemento().getY() <= cimaFerma() && fondoAlloStop == Integer.MIN_VALUE) {
				fondoAlloStop = quadro.getColonna().getFondo();
			}
			if (fondoAlloStop != Integer.MIN_VALUE && quadro.getColonna().getFondo() < fondoAlloStop - 20) {
				return;
			}
		}
		fail("la storia deve continuare a salire dopo che il logo si è fermato");
	}

	@Test
	void allaFineLogoESfondoSpannoInsiemeESiPassaAllaClassifica() {
		boolean ilLogoSiVede = false;
		for (int i = 0; i < 20000 && intro.getFase() == IntroDelGioco.Fase.STORIA; i++) {
			IntroDelGioco.Quadro quadro = intro.avanzaDi(PASSO);
			if (!quadro.getLoghi().isEmpty()) {
				ilLogoSiVede = true;
			}
			// Loghi e sfondo spariscono insieme: se i loghi sono tutti spariti dopo essere comparsi, anche lo sfondo lo è quasi
			if (ilLogoSiVede && quadro.getLoghi().isEmpty() && quadro.getColonna().isUscito()) {
				assertTrue(quadro.getOpacitaDelloSfondoDellaStoria() <= 0.0001f, "sparito il logo, lo sfondo non resta");
			}
		}
		assertTrue(ilLogoSiVede);
		assertEquals(IntroDelGioco.Fase.CLASSIFICA, intro.getFase(), "dopo la storia c'è la classifica");
	}

	@Test
	void ognunoHaLaSuaOpacitaESonoNelloIntervallo() {
		for (int i = 0; i < 20000 && intro.getFase() == IntroDelGioco.Fase.STORIA; i++) {
			for (IntroDelGioco.Logo logo : intro.avanzaDi(PASSO).getLoghi()) {
				assertTrue(logo.getOpacita() > 0f && logo.getOpacita() <= 1f, "opacità " + logo.getOpacita());
			}
		}
	}

	@Test
	void ilLogoDellaForestaFrenaPianoPianoInveceDiFermarsiDiBotto() {
		int frenata = IntroDelGioco.DISTANZA_DI_FRENATA;
		// Lontano dal posto segue la storia alla stessa distanza; passata la frenata è fermo
		assertEquals(500, IntroDelGioco.frenata(500));
		assertEquals(frenata, IntroDelGioco.frenata(frenata));
		assertEquals(0, IntroDelGioco.frenata(-frenata));
		assertEquals(0, IntroDelGioco.frenata(-3 * frenata));
		// La storia sale di un pixel alla volta: il logo prima la segue, poi sale sempre meno (mai di più di un pixel, mai a scatti) fino a fermarsi
		int precedente = IntroDelGioco.frenata(3 * frenata);
		int salitiNellaPrimaMeta = 0;
		int salitiNellaSecondaMeta = 0;
		for (int distanza = 3 * frenata - 1; distanza >= -3 * frenata; distanza--) {
			int corrente = IntroDelGioco.frenata(distanza);
			int passo = precedente - corrente;
			assertTrue(passo >= 0 && passo <= 1, "passo " + passo + " a distanza " + distanza);
			assertTrue(corrente >= distanza, "il logo non sale oltre la storia: " + corrente + " a " + distanza);
			if (distanza < frenata && distanza >= 0) {
				salitiNellaPrimaMeta += passo;
			} else if (distanza < 0 && distanza >= -frenata) {
				salitiNellaSecondaMeta += passo;
			}
			precedente = corrente;
		}
		assertEquals(0, precedente);
		assertTrue(salitiNellaPrimaMeta > salitiNellaSecondaMeta, "rallenta: " + salitiNellaPrimaMeta + " pixel nella prima metà, " + salitiNellaSecondaMeta + " nella seconda");
		assertTrue(salitiNellaSecondaMeta > 0, "e si ferma piano piano, non di botto");
	}

	@Test
	void iLoghiDaPrecaricareSonoIDueDellIntroConAlone() {
		List<ContenutoDiColonna.Elemento> loghi = IntroDelGioco.loghiDaPrecaricare();
		assertEquals(2, loghi.size());
		assertEquals(IntroDelGioco.RISORSA_LOGO_FORESTA, loghi.get(0).getRisorsa());
		assertEquals(IntroDelGioco.RISORSA_LOGO_3AM, loghi.get(1).getRisorsa());
		for (ContenutoDiColonna.Elemento logo : loghi) {
			assertEquals(SchermateSenzaPartita.RAGGIO_ALONE, logo.getRaggioDellAlone());
		}
	}

	private static GestorePunteggi classificaCon(String... nomi) {
		return (GestorePunteggi) Proxy.newProxyInstance(IntroDelGiocoTest.class.getClassLoader(), new Class<?>[] {GestorePunteggi.class}, (proxy, metodo, argomenti) -> {
			switch (metodo.getName()) {
				case "getConteggio":
					return nomi.length;
				case "getPunteggio":
					String nome = nomi[(Integer) argomenti[0]];
					return new com.threeamigos.foresta.strumenti.Punteggio() {
						@Override public String getNome() { return nome; }
						@Override public int getPunteggio() { return 123456; }
						@Override public String getIdPartita() { return null; }
					};
				default:
					throw new UnsupportedOperationException(metodo.getName());
			}
		});
	}

	@Test
	void nellaClassificaUnNomeLungoVaACapoSenzaArrivareSulPunteggioEIPostiDopoScendono() {
		// La larghezza di contenuto del telefono più stretto (layout_telefono.md §5)
		int larghezza = 770;
		ContenutoDiColonna corta = IntroDelGioco.classifica(classificaCon("Aldo", "Bea"), larghezza);
		ContenutoDiColonna lunga = IntroDelGioco.classifica(classificaCon("Bartolomeo Massimiliano Della Foresta Antica", "Bea"), larghezza);
		// Gli elementi: il titolo, poi nome e punteggio di ogni posto
		ContenutoDiColonna.Elemento nome = lunga.getElementi().get(1);
		ContenutoDiColonna.Elemento punteggio = lunga.getElementi().get(2);
		assertTrue(nome.getPixel().getAltezza() > corta.getElementi().get(1).getPixel().getAltezza(), "il nome va a capo");
		assertTrue(nome.getX() + nome.getPixel().getLarghezza() <= punteggio.getX(), "non arriva sul punteggio");
		assertTrue(lunga.getElementi().get(3).getY() > corta.getElementi().get(3).getY(), "il posto dopo scende");
		assertTrue(lunga.getAltezza() > corta.getAltezza());
	}

	private static String[] dieciNomi() {
		String[] nomi = new String[10];
		for (int i = 0; i < nomi.length; i++) {
			nomi[i] = "Eroe " + i;
		}
		return nomi;
	}

	/** Porta l'intro alla classifica e la fa andare finché non si passa ai trofei; dice se la classifica si è mai fermata */
	private static boolean laClassificaSiFerma(IntroDelGioco intro) {
		intro.posizionaSuPunteggi();
		boolean ferma = false;
		for (int i = 0; i < 20000 && intro.getFase() == IntroDelGioco.Fase.CLASSIFICA; i++) {
			IntroDelGioco.Quadro quadro = intro.avanzaDi(PASSO);
			ferma |= quadro.getColonna().isArrivato();
		}
		assertEquals(IntroDelGioco.Fase.TROFEI, intro.getFase(), "dopo la classifica ci sono i trofei");
		return ferma;
	}

	@Test
	void unaClassificaCheCiStaSiFermaInCima() {
		IntroDelGioco alta = new IntroDelGioco(LARGHEZZA, 2000, classificaCon(dieciNomi()), vista(), MISURE);
		assertTrue(laClassificaSiFerma(alta));
	}

	@Test
	void unaClassificaCheNonCiStaScorreFinoAUscireTutta() {
		IntroDelGioco bassa = new IntroDelGioco(LARGHEZZA, 480, classificaCon(dieciNomi()), vista(), MISURE);
		assertFalse(laClassificaSiFerma(bassa), "non si ferma: l'ultima riga deve passare");
	}

	@Test
	void laClassificaSiFermaSoloSeIlFondoNonEntraNellaFasciaCheSfuma() {
		int altezza = 600 - SchermateSenzaPartita.QUOTA_CLASSIFICA - IntroDelGioco.FASCIA;
		assertTrue(IntroDelGioco.classificaFerma(altezza, 600));
		assertFalse(IntroDelGioco.classificaFerma(altezza + 1, 600));
	}

	@Test
	void laStoriaSiRifaSeLaLinguaCambiaDalleOpzioni() {
		try {
			intro.resettaSequenza();
			ContenutoDiColonna italiana = intro.avanza().getContenuto();
			intro.resettaSequenza();
			assertSame(italiana, intro.avanza().getContenuto(), "nella stessa lingua la storia si fa una volta sola");
			Testi.impostaLingua("en");
			intro.resettaSequenza();
			ContenutoDiColonna inglese = intro.avanza().getContenuto();
			assertNotSame(italiana, inglese);
			assertNotEquals(italiana.getElementi().get(0).getChiave(), inglese.getElementi().get(0).getChiave(),
					"la chiave dell'immagine dice la lingua: libGDX tiene le immagini per chiave");
		} finally {
			Testi.impostaLingua(null);
		}
	}
}
