package com.threeamigos.foresta.ui.immagini;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Lo sfondo delle icone in ricarica: la cornice si riempie dal basso, dal rosso scuro al verde scuro passando per il giallo scuro, sul grigio scuro.
 */
class SfondoARiempimentoTest {
	/**
	 * Un'immagine che si può modificare, per preparare i dati della prova
	 */
	static final class Tela {
		private final int larghezza;
		private final int altezza;
		private final int[] pixel;

		Tela(int larghezza, int altezza) {
			this.larghezza = larghezza;
			this.altezza = altezza;
			this.pixel = new int[larghezza * altezza];
		}

		void setRGB(int x, int y, int argb) {
			pixel[y * larghezza + x] = argb;
		}

		ImmagineDiPixel immagine() {
			return new ImmagineDiPixel(larghezza, altezza, pixel.clone());
		}
	}


    private static final int BORDO = 0xFFAA5500;

    /**
     * Una cornice 12x12: bordo di un pixel e interno trasparente, con gli angoli fuori dal bordo trasparenti.
     */
    private static Tela cornice() {
        Tela cornice = new Tela(12, 12);
        for (int i = 1; i < 11; i++) {
            cornice.setRGB(i, 1, BORDO);
            cornice.setRGB(i, 10, BORDO);
            cornice.setRGB(1, i, BORDO);
            cornice.setRGB(10, i, BORDO);
        }
        return cornice;
    }

    private static int righeRiempite(ImmagineDiPixel sfondo, int colore) {
        int righe = 0;
        for (int y = 2; y < 10; y++) {
            if ((sfondo.getArgb(5, y) & 0xFFFFFF) == colore) {
                righe++;
            }
        }
        return righe;
    }

    @Test
    void ilBordoResta() {
        ImmagineDiPixel sfondo = SfondoARiempimento.crea(cornice().immagine(), 1, 3);
        assertEquals(BORDO, sfondo.getArgb(5, 1));
        assertEquals(BORDO, sfondo.getArgb(1, 5));
    }

    @Test
    void gliAngoliFuoriDalBordoRestanoTrasparenti() {
        ImmagineDiPixel sfondo = SfondoARiempimento.crea(cornice().immagine(), 2, 3);
        assertEquals(0, sfondo.getArgb(0, 0) >>> 24);
        assertEquals(0, sfondo.getArgb(11, 11) >>> 24);
    }

    @Test
    void conTreTurniDiRicaricaSiRiempieUnQuartoMetaETreQuarti() {
        // Le righe interne sono 8: un quarto sono 2, metà 4, tre quarti 6; il resto è nero
        for (int passati = 1; passati <= 3; passati++) {
            ImmagineDiPixel sfondo = SfondoARiempimento.crea(cornice().immagine(), passati, 3);
            int nere = righeRiempite(sfondo, 0x000000);
            assertEquals(8 - 2 * passati, nere, "nero dopo " + passati + " turni");
        }
    }

    @Test
    void ilRiempimentoSaleDalBasso() {
        ImmagineDiPixel sfondo = SfondoARiempimento.crea(cornice().immagine(), 2, 3);
        assertEquals(0x000000, sfondo.getArgb(5, 2) & 0xFFFFFF, "in alto è ancora nero");
        assertNotEquals(0x000000, sfondo.getArgb(5, 9) & 0xFFFFFF, "in basso è riempito");
    }

    @Test
    void aMetaDelCamminoIlColoreEGialloENonMarrone() {
        // Con una ricarica di un turno il solo passo è a metà: giallo scuro, rosso e verde alla pari
        int meta = SfondoARiempimento.crea(cornice().immagine(), 1, 1).getArgb(5, 9) & 0xFFFFFF;
        assertEquals(0x8C8C00, meta);
    }

    @Test
    void ilGradienteHaIlGialloFraRossoEVerde() {
        // Con 3 turni: un quarto (fra rosso e giallo, il verde sale), metà (giallo), tre quarti (fra giallo e verde, il rosso cala)
        int uno = SfondoARiempimento.crea(cornice().immagine(), 1, 3).getArgb(5, 9) & 0xFFFFFF;
        int due = SfondoARiempimento.crea(cornice().immagine(), 2, 3).getArgb(5, 9) & 0xFFFFFF;
        int tre = SfondoARiempimento.crea(cornice().immagine(), 3, 3).getArgb(5, 9) & 0xFFFFFF;
        assertEquals(0x8C, (uno >> 16) & 0xFF, "il rosso resta pieno fino al giallo");
        assertTrue(((uno >> 8) & 0xFF) > 0 && ((uno >> 8) & 0xFF) < 0x8C, "il verde sale");
        assertEquals(0x8C, (due >> 16) & 0xFF);
        assertTrue(((tre >> 16) & 0xFF) < 0x8C && ((tre >> 16) & 0xFF) > 0, "il rosso cala");
        assertTrue(((tre >> 8) & 0xFF) < 0x8C && ((tre >> 8) & 0xFF) > 0x52, "il verde scende al suo valore");
    }

    @Test
    void ilColoreVaDalRossoScuroAlVerdeScuro() {
        int primo = SfondoARiempimento.crea(cornice().immagine(), 1, 5).getArgb(5, 9) & 0xFFFFFF;
        int ultimo = SfondoARiempimento.crea(cornice().immagine(), 5, 5).getArgb(5, 9) & 0xFFFFFF;
        assertTrue(((primo >> 16) & 0xFF) > ((ultimo >> 16) & 0xFF), "il rosso cala");
        assertTrue(((primo >> 8) & 0xFF) < ((ultimo >> 8) & 0xFF), "il verde cresce");
        assertEquals(0, primo & 0xFF);
        assertEquals(0, ultimo & 0xFF);
    }
}
