package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.CostantiCanti;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.canti.*;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.tipi.*;
import com.threeamigos.foresta.tipi.TipoCanto;
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
        StatoDelMotore.azzera();
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        avversari = GruppoAvversario.getIstanza();
        compagni = new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return true;
            }
        };
    }

    // I canti che il Bardo e la Cantastorie sanno subito: l'Urlo della Banshee lo imparano dopo (vedi IntermezzoUrloDellaBanshee)
    private static final List<TipoCanto> CANTI_DI_BASE = Arrays.asList(TipoCanto.CURATIVO, TipoCanto.IPNOTICO, TipoCanto.DI_VALORE);

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
        assertTrue(CantoDelBardo.conosciutoDa(TipoPersonaggio.BARDO_DELLE_LOCANDE));
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            if (classe != TipoPersonaggio.BARDO && classe != TipoPersonaggio.CANTASTORIE
                    && classe != TipoPersonaggio.BARDO_DELLE_LOCANDE) {
                assertFalse(CantoDelBardo.conosciutoDa(classe), classe.name());
            }
        }
        for (TipoCanto tipo : CANTI_DI_BASE) {
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
        // L'Urlo della Banshee lo sa a prescindere, anche se il gruppo non l'ha imparato
        assertFalse(CantoDelBardo.isUrloDellaBansheeAppreso());
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
        assertEquals(CANTI_DI_BASE, CantoDelBardo.cantiPossibili(bardo));

        // Con poca magia si canta solo quello che costa poco
        bardo.subMagia(bardo.getMagia() - CostantiCanti.CANTO_IPNOTICO_COSTO_LANCIO);
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
        assertEquals(CostantiCanti.CANTO_CURATIVO_COSTO_LANCIO, FabbricaCanti.crea(TipoCanto.CURATIVO, bardo).getCostoLancio());
        assertEquals(CostantiCanti.CANTO_IPNOTICO_COSTO_LANCIO, FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo).getCostoLancio());
        assertEquals(CostantiCanti.CANTO_DI_VALORE_COSTO_LANCIO, FabbricaCanti.crea(TipoCanto.DI_VALORE, bardo).getCostoLancio());
        assertInstanceOf(CantoCurativo.class, FabbricaCanti.crea(TipoCanto.CURATIVO, bardo));
        assertInstanceOf(CantoIpnotico.class, FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo));
        assertInstanceOf(CantoDiValore.class, FabbricaCanti.crea(TipoCanto.DI_VALORE, bardo));
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

        assertSame(grave, compagni.piuFerito().get());
        assertTrue(canto.canta(bardo, compagni, avversari));

        assertEquals(Math.min(grave.getSaluteMassima(), saluteGrave + canto.getCura()), grave.getSalute());
        assertEquals(saluteLieve, lieve.getSalute(), "cura uno solo");
        assertEquals(magia - CostantiCanti.CANTO_CURATIVO_COSTO_LANCIO, bardo.getMagia());
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
        assertFalse(compagni.piuFerito().isPresent());
        assertFalse(FabbricaCanti.crea(TipoCanto.CURATIVO, bardo).canta(bardo, compagni, avversari));
        assertEquals(magia, bardo.getMagia());
    }

    // --- Urlo della Banshee

    @Test
    void l_UrloDellaBansheeSiImparaDalBardoDellaLocanda() {
        assertFalse(CantoDelBardo.isUrloDellaBansheeAppreso());
        assertFalse(CantoDelBardo.puoCantare(bardo(), TipoCanto.URLO_DELLA_BANSHEE));
        assertFalse(CantoDelBardo.puoCantare(cantastorie(), TipoCanto.URLO_DELLA_BANSHEE));
        BardoLocanda bardoDellaLocanda = new BardoLocanda("Orfeo", 5);
        bardoDellaLocanda.addMagia(1000);
        assertTrue(CantoDelBardo.puoCantare(bardoDellaLocanda, TipoCanto.URLO_DELLA_BANSHEE), "lui lo sa a prescindere");

        GruppoGiocatore.getIstanza().impara(TipoCanto.URLO_DELLA_BANSHEE);

        assertTrue(CantoDelBardo.isUrloDellaBansheeAppreso());
        assertTrue(CantoDelBardo.puoCantare(bardo(), TipoCanto.URLO_DELLA_BANSHEE));
        assertTrue(CantoDelBardo.puoCantare(cantastorie(), TipoCanto.URLO_DELLA_BANSHEE));
        assertEquals(Arrays.asList(TipoCanto.values()), CantoDelBardo.cantiPossibili(bardo()));
        // Chi non sa cantare non lo impara
        assertFalse(CantoDelBardo.puoCantare(new Mago("Merlino", 5), TipoCanto.URLO_DELLA_BANSHEE));
        assertFalse(CantoDelBardo.puoCantare(new Viandante("Anselmo", 5), TipoCanto.URLO_DELLA_BANSHEE));
    }

    @Test
    void l_UrloDellaBansheeCostaDiPiuEUsaIlSuoCanto() {
        Bardo bardo = bardo();
        assertEquals(CostantiCanti.CANTO_URLO_DELLA_BANSHEE_COSTO_LANCIO, FabbricaCanti.crea(TipoCanto.URLO_DELLA_BANSHEE, bardo).getCostoLancio());
        assertTrue(CostantiCanti.CANTO_URLO_DELLA_BANSHEE_COSTO_LANCIO > CostantiCanti.CANTO_IPNOTICO_COSTO_LANCIO);
        assertInstanceOf(UrloDellaBanshee.class, FabbricaCanti.crea(TipoCanto.URLO_DELLA_BANSHEE, bardo));
    }

    @Test
    void l_UrloDellaBansheeFaCedereConPiuProbabilitaDelCantoIpnotico() {
        Bardo bardo = bardo();
        Goblin goblin = new Goblin(1);
        fissa(bardo, TipoAttributo.SOGGEZIONE, 10);
        fissa(goblin, TipoAttributo.CORAGGIO, 10);
        assertEquals(50.0, CantoIpnotico.probabilitaDiCedere(bardo, goblin), 0.0001);
        assertEquals(100.0 * 15 / 25, UrloDellaBanshee.probabilitaDiCedere(bardo, goblin), 0.0001);
        assertTrue(UrloDellaBanshee.probabilitaDiCedere(bardo, goblin) > CantoIpnotico.probabilitaDiCedere(bardo, goblin));
        // Con soggezione nulla non cede nessuno, con coraggio nullo cedono sempre
        fissa(bardo, TipoAttributo.SOGGEZIONE, 0);
        assertEquals(0.0, UrloDellaBanshee.probabilitaDiCedere(bardo, goblin));
    }

    @Test
    void chiCedeAll_UrloPrendeSpaventatoEConfusoInsieme() {
        Bardo bardo = bardo();
        fissa(bardo, TipoAttributo.NUMERO_BERSAGLI, 2);
        Goblin uno = new Goblin(1);
        Goblin due = new Goblin(1);
        Goblin tre = new Goblin(1);
        tuttiCedono(bardo, uno, due, tre);
        int magia = bardo.getMagia();

        assertTrue(FabbricaCanti.crea(TipoCanto.URLO_DELLA_BANSHEE, bardo).canta(bardo, compagni, avversari));

        long colpiti = Arrays.asList(uno, due, tre).stream()
                .filter(g -> g.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO) && g.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO)).count();
        assertEquals(2, colpiti, "fino al numero di bersagli, ognuno con tutti e due gli stati");
        assertEquals(2, Arrays.asList(uno, due, tre).stream().filter(CantiDelBardoTest::haSpaventatoOConfuso).count(), "nessuno ha uno stato solo");
        assertEquals(magia - CostantiCanti.CANTO_URLO_DELLA_BANSHEE_COSTO_LANCIO, bardo.getMagia());
    }

    @Test
    void senzaAvversariL_UrloNonSiCanta() {
        Bardo bardo = bardo();
        assertFalse(FabbricaCanti.crea(TipoCanto.URLO_DELLA_BANSHEE, bardo).canta(bardo, compagni, avversari));
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
        assertEquals(magia - CostantiCanti.CANTO_IPNOTICO_COSTO_LANCIO, bardo.getMagia());
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

    // --- Canto di valore

    @Test
    void ilPercentualeDelCantoDiValoreCresceConIlLivelloFinoAlTetto() {
        assertEquals(22.5, new CantoDiValore(new Bardo("Orfeo", 1)).getIntensita(), 1e-9);
        assertEquals(30.0, new CantoDiValore(new Bardo("Orfeo", 4)).getIntensita(), 1e-9);
        assertEquals(50.0, new CantoDiValore(new Bardo("Orfeo", 12)).getIntensita(), 1e-9);
        assertEquals(50.0, new CantoDiValore(new Bardo("Orfeo", 30)).getIntensita(), 1e-9);
    }

    @Test
    void ilCantoDiValoreRendeIspiratiTuttiIVivi() {
        Bardo bardo = bardo();
        Guerriero vivo = new Guerriero("Vivo", 5);
        Guerriero morto = new Guerriero("Morto", 5);
        compagni.aggiungiPersonaggio(bardo);
        compagni.aggiungiPersonaggio(vivo);
        compagni.aggiungiPersonaggio(morto);
        morto.muore("per distrazione");
        int magia = bardo.getMagia();

        assertTrue(FabbricaCanti.crea(TipoCanto.DI_VALORE, bardo).canta(bardo, compagni, avversari));

        for (Personaggio p : Arrays.asList(bardo, vivo)) {
            assertTrue(p.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO), p.getNomeSingolare());
            assertEquals(CostantiCanti.CANTO_DI_VALORE_DURATA, p.getEffettiDiStato().iterator().next().getDurata());
        }
        assertFalse(morto.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO));
        assertEquals(magia - CostantiCanti.CANTO_DI_VALORE_COSTO_LANCIO, bardo.getMagia());
    }

    @Test
    void ilCantoDiValoreAlzaCoraggioEPrecisioneEBasta() {
        Guerriero guerriero = new Guerriero("Arsenio", 5);
        compagni.aggiungiPersonaggio(guerriero);
        double coraggio = guerriero.getModelloDati().get(TipoAttributo.CORAGGIO);
        double precisione = guerriero.getModelloDati().get(TipoAttributo.PRECISIONE);
        int forza = guerriero.getForza();
        assertEquals(0.0, guerriero.getVariazioneDaTemporanei(TipoAttributo.CORAGGIO));

        Bardo bardo = new Bardo("Orfeo", 4);
        bardo.addMagia(1000);
        new CantoDiValore(bardo).canta(bardo, compagni, avversari);

        assertEquals(coraggio * 0.30, guerriero.getVariazioneDaTemporanei(TipoAttributo.CORAGGIO), 1e-9);
        assertEquals(precisione * 0.30, guerriero.getVariazioneDaTemporanei(TipoAttributo.PRECISIONE), 1e-9);
        assertEquals(0.0, guerriero.getVariazioneDaTemporanei(TipoAttributo.FORZA));
        assertEquals(forza, guerriero.getForza());
    }

    @Test
    void duCantiDiValoreNonRaddoppianoMaRinfrescano() {
        Guerriero guerriero = new Guerriero("Arsenio", 5);
        compagni.aggiungiPersonaggio(guerriero);
        Bardo primo = new Bardo("Orfeo", 4);
        Bardo secondo = new Bardo("Lino", 4);
        primo.addMagia(1000);
        secondo.addMagia(1000);
        new CantoDiValore(primo).canta(primo, compagni, avversari);
        double variazione = guerriero.getVariazioneDaTemporanei(TipoAttributo.CORAGGIO);
        guerriero.riduciEffettiDiStato();
        guerriero.riduciEffettiDiStato();
        assertEquals(CostantiCanti.CANTO_DI_VALORE_DURATA - 2, guerriero.getEffettiDiStato().iterator().next().getDurata());

        new CantoDiValore(secondo).canta(secondo, compagni, avversari);

        assertEquals(1, guerriero.getEffettiDiStato().size());
        assertEquals(CostantiCanti.CANTO_DI_VALORE_DURATA, guerriero.getEffettiDiStato().iterator().next().getDurata());
        assertEquals(variazione, guerriero.getVariazioneDaTemporanei(TipoAttributo.CORAGGIO), 1e-9);
    }

    @Test
    void unCantoPiuForteSostituisceQuelloPiuDebole() {
        Guerriero guerriero = new Guerriero("Arsenio", 5);
        compagni.aggiungiPersonaggio(guerriero);
        Bardo debole = new Bardo("Orfeo", 1);
        Bardo forte = new Bardo("Lino", 10);
        debole.addMagia(1000);
        forte.addMagia(1000);
        new CantoDiValore(debole).canta(debole, compagni, avversari);
        assertEquals(22.5, guerriero.getEffettiDiStato().iterator().next().getIntensita(), 1e-9);
        new CantoDiValore(forte).canta(forte, compagni, avversari);
        assertEquals(1, guerriero.getEffettiDiStato().size());
        assertEquals(45.0, guerriero.getEffettiDiStato().iterator().next().getIntensita(), 1e-9);
        // Poi uno più debole non lo abbassa
        new CantoDiValore(debole).canta(debole, compagni, avversari);
        assertEquals(45.0, guerriero.getEffettiDiStato().iterator().next().getIntensita(), 1e-9);
    }

    @Test
    void ilCantoDiValoreScadeELAlbaSacraLoToglie() {
        Guerriero guerriero = new Guerriero("Arsenio", 5);
        compagni.aggiungiPersonaggio(guerriero);
        Bardo bardo = bardo();
        new CantoDiValore(bardo).canta(bardo, compagni, avversari);
        for (int turno = 0; turno < CostantiCanti.CANTO_DI_VALORE_DURATA; turno++) {
            guerriero.riduciEffettiDiStato();
        }
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO));
        assertEquals(0.0, guerriero.getVariazioneDaTemporanei(TipoAttributo.CORAGGIO));

        new CantoDiValore(bardo).canta(bardo, compagni, avversari);
        guerriero.rimuoviTuttiGliEffettiDiStato();
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO));
    }

    @Test
    void lIntensitaDelloStatoSiSalvaESiRilegge() throws java.io.IOException {
        Guerriero guerriero = new Guerriero("Arsenio", 5);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0, 32.5);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 2, 4);
        java.io.StringWriter scritto = new java.io.StringWriter();
        java.io.PrintWriter writer = new java.io.PrintWriter(scritto);
        guerriero.getModelloDati().salva(writer);
        writer.flush();
        com.threeamigos.foresta.modellodati.PersonaggioMD riletto = new com.threeamigos.foresta.modellodati.PersonaggioMD();
        riletto.leggi(new java.io.BufferedReader(new java.io.StringReader(scritto.toString())));
        com.threeamigos.foresta.modellodati.EffettoDiStatoMD ispirato = riletto.getEffettiDiStato().stream()
                .filter(e -> e.getTipoEffettoDiStato() == TipoEffettoDiStato.ISPIRATO).findFirst().get();
        assertEquals(3, ispirato.getDurata());
        assertEquals(32.5, ispirato.getIntensita(), 1e-9);
        com.threeamigos.foresta.modellodati.EffettoDiStatoMD veleno = riletto.getEffettiDiStato().stream()
                .filter(e -> e.getTipoEffettoDiStato() == TipoEffettoDiStato.AVVELENATO).findFirst().get();
        assertEquals(4, veleno.getDanniNelTempo());
        assertEquals(0.0, veleno.getIntensita());
    }
}
