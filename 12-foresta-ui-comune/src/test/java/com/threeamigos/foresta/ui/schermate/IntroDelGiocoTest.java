package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Misura;
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
			if (cima > cimaFerma()) {
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
			if (fondoAlloStop != Integer.MIN_VALUE && quadro.getColonna().getFondo() < fondoAlloStop - 100) {
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
}
