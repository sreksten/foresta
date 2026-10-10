package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.Canto;
import com.threeamigos.foresta.incantesimi.CantoCurativo;
import com.threeamigos.foresta.incantesimi.CantoDelBardo;
import com.threeamigos.foresta.incantesimi.CantoDiValore;
import com.threeamigos.foresta.incantesimi.CantoIpnotico;
import com.threeamigos.foresta.incantesimi.FabbricaCanti;
import com.threeamigos.foresta.incantesimi.TipoCanto;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I canti innati del Bardo e della Cantastorie (vedi bardo.md, fase 2): chi può cantare, il costo, la cura al più
 * ferito e il canto ipnotico.
 */
class CantiDelBardoTest {

    private Gruppo compagni;
    private GruppoAvversario avversari;

    @BeforeEach
    void preparaGruppi() {
        ModelloDati.setIstanza(new ModelloDati());
        GruppoAvversario.azzeraIstanza();
        avversari = GruppoAvversario.getIstanza();
        compagni = new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return true;
            }
        };
    }

    private static Bardo bardo() {
        Bardo bardo = new Bardo("Orfeo", 5);
        bardo.addMagia(1000);
        return bardo;
    }

    private static void fissa(PersonaggioBase personaggio, TipoAttributo attributo, int valore) {
        personaggio.addModificatore(new ModificatoreAttributoMD(attributo, TipoModificatore.QUANTITA_ASSOLUTA, valore));
    }

    // --- Chi canta

    @Test
    void cantanoSoloIlBardoELaCantastorie() {
        assertTrue(CantoDelBardo.conosciutoDa(TipoPersonaggio.BARDO));
        assertTrue(CantoDelBardo.conosciutoDa(TipoPersonaggio.CANTASTORIE));
        assertTrue(CantoDelBardo.conosciutoDa(TipoPersonaggio.BARDO_LOCANDA));
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            if (classe != TipoPersonaggio.BARDO && classe != TipoPersonaggio.CANTASTORIE
                    && classe != TipoPersonaggio.BARDO_LOCANDA) {
                assertFalse(CantoDelBardo.conosciutoDa(classe), classe.name());
            }
        }
        for (TipoCanto tipo : TipoCanto.values()) {
            assertTrue(CantoDelBardo.puoCantare(bardo(), tipo));
            assertTrue(CantoDelBardo.puoCantare(cantastorie(), tipo));
            assertFalse(CantoDelBardo.puoCantare(new Viandante("Anselmo", 5), tipo));
            assertFalse(CantoDelBardo.puoCantare(new Locandiere("Gigio", 5), tipo));
            assertFalse(CantoDelBardo.puoCantare(new Mago("Merlino", 5), tipo));
        }
    }

    @Test
    void ancheIlBardoDellaLocandaCanta() {
        BardoLocanda bardoDellaLocanda = new BardoLocanda("Orfeo", 5);
        bardoDellaLocanda.addMagia(1000);
        assertEquals(Arrays.asList(TipoCanto.values()), CantoDelBardo.cantiPossibili(bardoDellaLocanda));
    }

    private static Cantastorie cantastorie() {
        Cantastorie cantastorie = new Cantastorie("Aedo", 5);
        cantastorie.addMagia(1000);
        return cantastorie;
    }

    @Test
    void servonoLaMagiaEUnaVoceLibera() {
        Bardo bardo = bardo();
        assertEquals(Arrays.asList(TipoCanto.values()), CantoDelBardo.cantiPossibili(bardo));

        // Con poca magia si canta solo quello che costa poco
        bardo.subMagia(bardo.getMagia() - Costanti.CANTO_IPNOTICO_COSTO_LANCIO);
        assertEquals(java.util.Collections.singletonList(TipoCanto.IPNOTICO), CantoDelBardo.cantiPossibili(bardo));
        bardo.subMagia(bardo.getMagia());
        assertTrue(CantoDelBardo.cantiPossibili(bardo).isEmpty());

        // Silenziato non canta, anche con la magia
        Bardo silenziato = bardo();
        silenziato.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertTrue(CantoDelBardo.cantiPossibili(silenziato).isEmpty());

        // Morto nemmeno
        Bardo morto = bardo();
        morto.muore("per un acuto sbagliato");
        assertTrue(CantoDelBardo.cantiPossibili(morto).isEmpty());
    }

    @Test
    void ognuniCantoCostaLaSuaMagia() {
        Bardo bardo = bardo();
        assertEquals(Costanti.CANTO_CURATIVO_COSTO_LANCIO, FabbricaCanti.crea(TipoCanto.CURATIVO, bardo).getCostoLancio());
        assertEquals(Costanti.CANTO_IPNOTICO_COSTO_LANCIO, FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo).getCostoLancio());
        assertEquals(Costanti.CANTO_DI_VALORE_COSTO_LANCIO, FabbricaCanti.crea(TipoCanto.DI_VALORE, bardo).getCostoLancio());
        assertTrue(FabbricaCanti.crea(TipoCanto.CURATIVO, bardo) instanceof CantoCurativo);
        assertTrue(FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo) instanceof CantoIpnotico);
        assertTrue(FabbricaCanti.crea(TipoCanto.DI_VALORE, bardo) instanceof CantoDiValore);
    }

    // --- Canto curativo

    @Test
    void ilCantoCuraIlPiuFeritoEMaiOltreIlMassimo() {
        Bardo bardo = bardo();
        Guerriero lieve = new Guerriero("Lieve", 5);
        Guerriero grave = new Guerriero("Grave", 5);
        Guerriero sano = new Guerriero("Sano", 5);
        compagni.aggiungiPersonaggio(bardo);
        compagni.aggiungiPersonaggio(lieve);
        compagni.aggiungiPersonaggio(grave);
        compagni.aggiungiPersonaggio(sano);
        lieve.subSalute(lieve.getSaluteMassima() / 10, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        grave.subSalute(grave.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int saluteLieve = lieve.getSalute();
        int saluteGrave = grave.getSalute();
        int magia = bardo.getMagia();
        CantoCurativo canto = (CantoCurativo) FabbricaCanti.crea(TipoCanto.CURATIVO, bardo);

        assertSame(grave, CantoCurativo.piuFerito(compagni).get());
        assertTrue(canto.canta(bardo, compagni, avversari));

        assertEquals(Math.min(grave.getSaluteMassima(), saluteGrave + canto.getCura()), grave.getSalute());
        assertEquals(saluteLieve, lieve.getSalute(), "cura uno solo");
        assertEquals(magia - Costanti.CANTO_CURATIVO_COSTO_LANCIO, bardo.getMagia());
    }

    @Test
    void laCuraCrescePerLivelloEPerSaggezza() {
        Bardo basso = new Bardo("Basso", 1);
        Bardo alto = new Bardo("Alto", 9);
        assertTrue(new CantoCurativo(alto).getCura() > new CantoCurativo(basso).getCura());
        // Il Carisma non c'entra, la Saggezza sì
        Bardo carismatico = new Bardo("Carismatico", 5);
        int cura = new CantoCurativo(carismatico).getCura();
        fissa(carismatico, TipoAttributo.CARISMA, 100);
        assertEquals(cura, new CantoCurativo(carismatico).getCura());
        fissa(carismatico, TipoAttributo.SAGGEZZA, 100);
        assertTrue(new CantoCurativo(carismatico).getCura() > cura);
    }

    @Test
    void senzaFeritiNonSiCantaENonSiPagaMagia() {
        Bardo bardo = bardo();
        compagni.aggiungiPersonaggio(bardo);
        bardo.addSalute(10000);
        int magia = bardo.getMagia();
        assertFalse(CantoCurativo.piuFerito(compagni).isPresent());
        assertFalse(FabbricaCanti.crea(TipoCanto.CURATIVO, bardo).canta(bardo, compagni, avversari));
        assertEquals(magia, bardo.getMagia());
    }

    // --- Canto ipnotico

    private void tuttiCedono(Bardo bardo, Personaggio... nemici) {
        fissa(bardo, TipoAttributo.SOGGEZIONE, 50);
        for (Personaggio nemico : nemici) {
            fissa((PersonaggioBase) nemico, TipoAttributo.CORAGGIO, 0);
            avversari.aggiungiPersonaggio(nemico);
        }
    }

    private static boolean haSpaventatoOConfuso(Personaggio p) {
        return p.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO) || p.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO);
    }

    @Test
    void ilCantoIpnoticoColpisceFinoAlNumeroDiBersagli() {
        Bardo bardo = bardo();
        fissa(bardo, TipoAttributo.NUMERO_BERSAGLI, 2);
        Goblin uno = new Goblin(1);
        Goblin due = new Goblin(1);
        Goblin tre = new Goblin(1);
        tuttiCedono(bardo, uno, due, tre);
        int magia = bardo.getMagia();

        assertTrue(FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo).canta(bardo, compagni, avversari));

        long colpiti = Arrays.asList(uno, due, tre).stream().filter(CantiDelBardoTest::haSpaventatoOConfuso).count();
        assertEquals(2, colpiti);
        assertEquals(magia - Costanti.CANTO_IPNOTICO_COSTO_LANCIO, bardo.getMagia());
    }

    @Test
    void ilCantoIpnoticoMetteLoStessoEffettoATutti() {
        Bardo bardo = bardo();
        fissa(bardo, TipoAttributo.NUMERO_BERSAGLI, 3);
        Goblin uno = new Goblin(1);
        Goblin due = new Goblin(1);
        tuttiCedono(bardo, uno, due);
        FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo).canta(bardo, compagni, avversari);
        assertEquals(uno.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO), due.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO));
        assertEquals(uno.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO), due.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO));
        assertTrue(haSpaventatoOConfuso(uno));
    }

    @Test
    void conSoggezioneNullaNessunoCede() {
        Bardo bardo = bardo();
        fissa(bardo, TipoAttributo.SOGGEZIONE, 0);
        Goblin goblin = new Goblin(1);
        avversari.aggiungiPersonaggio(goblin);
        assertEquals(0.0, CantoIpnotico.probabilitaDiCedere(bardo, goblin));
        assertTrue(FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo).canta(bardo, compagni, avversari), "si canta lo stesso");
        assertFalse(haSpaventatoOConfuso(goblin));
    }

    @Test
    void laProbabilitaDiCedereSaleConLaSoggezioneEScendeConIlCoraggio() {
        Bardo bardo = bardo();
        Goblin coraggioso = new Goblin(1);
        Goblin pauroso = new Goblin(1);
        fissa(coraggioso, TipoAttributo.CORAGGIO, 40);
        fissa(pauroso, TipoAttributo.CORAGGIO, 5);
        fissa(bardo, TipoAttributo.SOGGEZIONE, 20);
        double contro = CantoIpnotico.probabilitaDiCedere(bardo, coraggioso);
        double controPauroso = CantoIpnotico.probabilitaDiCedere(bardo, pauroso);
        assertTrue(controPauroso > contro);
        assertEquals(100.0 * 20 / 60, contro, 1e-9);
        Bardo piuImponente = bardo();
        fissa(piuImponente, TipoAttributo.SOGGEZIONE, 60);
        assertTrue(CantoIpnotico.probabilitaDiCedere(piuImponente, coraggioso) > contro);
    }

    @Test
    void senzaAvversariVivoNonSiCantaENonSiPagaMagia() {
        Bardo bardo = bardo();
        int magia = bardo.getMagia();
        assertFalse(FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo).canta(bardo, compagni, avversari));
        assertEquals(magia, bardo.getMagia());
    }

    @Test
    void ilCantoDiValoreAspettaLEffettoDiStato() {
        Bardo bardo = bardo();
        Canto canto = FabbricaCanti.crea(TipoCanto.DI_VALORE, bardo);
        assertThrows(UnsupportedOperationException.class, () -> canto.canta(bardo, compagni, avversari));
    }
}
