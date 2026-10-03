package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoRichiestaAperturaFinestraCombattimento;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.ColpoRichiesto;
import com.threeamigos.foresta.missioni.IlColpo;
import com.threeamigos.foresta.missioni.TipoMissione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il colpo: nel posto segnato sulla mappa ci sono le guardie, e il colpo riesce passando inosservati; se il gruppo
 * combatte lì è scoperto e il colpo fallisce, se fugge può riprovare.
 */
class ScenarioColpoTest {

    private static final String LIBRO = "CHIAVE=LIBRO_DI_PROVA;TIPO=FURTO;ASPETTO=QUALUNQUE;MANDANTE=il fornaio;LUOGO=ROVINE;"
            + "NEMICO=TROLL;NUMERO=2;MONETE=40;TITOLO=Il libro;RICHIESTA=Il libro.;BATTUTA=Chi lo sorveglia?;RISPOSTA=Due troll.;"
            + "COLPO=Prendete il libro.;SCOPERTI=Vi hanno visti.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui c'era il libro.";

    @Test
    void ogniColpoSiLegge() {
        Set<TipoMissione> tipi = EnumSet.noneOf(TipoMissione.class);
        for (int i = 0; i < 300; i++) {
            tipi.add(ColpoRichiesto.da(ProduttoreDiTestiCasuale.rigaDiMissioni("COLPO")).getTipo());
        }
        assertEquals(EnumSet.of(TipoMissione.FURTO, TipoMissione.SABOTAGGIO, TipoMissione.VANDALISMO, TipoMissione.INCENDIO,
                TipoMissione.VIOLAZIONE_DOMICILIO, TipoMissione.AVVELENAMENTO), tipi);
        assertThrows(IllegalArgumentException.class, () -> ColpoRichiesto.da(LIBRO.replace("SCOPERTI=Vi hanno visti.;", "")));
    }

    @Test
    void passandoInosservatiFraLeGuardieIlColpoRiesce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(271)) {
            IlColpo colpo = prendiIlColpo(partita);
            CoordinateMD rovine = colpo.getPosto();
            entra(partita, rovine);
            assertEquals(2, GruppoAvversario.getIstanza().getNumeroPersonaggiVivi());
            assertTrue(partita.comandiDisponibili().contains(Comando.PASSA_INOSSERVATO), String.valueOf(partita.comandiDisponibili()));

            Dado.trucca(1);
            partita.comando(Comando.PASSA_INOSSERVATO);
            assertEquals(0, Dado.trucchiRimasti());
            assertNotEquals(Stato.IN_LOCAZIONE, partita.stato());
            assertEquals("RITORNO", colpo.getPassoCorrente());
            assertTrue(partita.testi().contains("Prendete il libro. Il fornaio aspetta a Nyena."), String.valueOf(partita.testi()));
        }
    }

    @Test
    void seLeGuardieSeNeAccorgonoAttaccanoESiProvaUnaVoltaSola() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(272)) {
            IlColpo colpo = prendiIlColpo(partita);
            entra(partita, colpo.getPosto());
            Dado.trucca(100);
            partita.comando(Comando.PASSA_INOSSERVATO);
            Dado.ripristina();
            assertTrue(partita.testi().contains("Un rametto si spezza: gli avversari vi hanno visti, e attaccano per primi!"),
                    String.valueOf(partita.testi()));
            if (partita.stato() == Stato.GIOCO_PERSO) {
                return;
            }
            partita.assertStato(Stato.IN_LOCAZIONE);
            assertFalse(partita.comandiDisponibili().contains(Comando.PASSA_INOSSERVATO), "si prova una volta sola");
            assertEquals("COLPO", colpo.getPassoCorrente());
            assertFalse(colpo.isFallita(), "l'attacco delle guardie non è ancora un combattimento del gruppo");
        }
    }

    @Test
    void combattendoNelPostoDelColpoIlGruppoEScopertoEIlColpoFallisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(273)) {
            IlColpo colpo = prendiIlColpo(partita);
            // Combattere altrove, per strada, non conta
            partita.gruppo().setCoordinate(new CoordinateMD(colpo.getPosto().getX(), colpo.getPosto().getY() + 1));
            partita.pubblica(new InternoRichiestaAperturaFinestraCombattimento(partita.gruppo().getCapo(), partita.gruppo().getCapo()));
            colpo.controllaPostLocazione();
            assertFalse(colpo.isFallita());

            partita.gruppo().setCoordinate(colpo.getPosto());
            partita.pubblica(new InternoRichiestaAperturaFinestraCombattimento(partita.gruppo().getCapo(), partita.gruppo().getCapo()));
            colpo.controllaPostLocazione();
            assertTrue(colpo.isFallita());
            assertTrue(partita.testi().contains("Vi hanno visti."), String.valueOf(partita.testi()));
        }
    }

    @Test
    void controIlCovoDiUnaMissioneNonSiPassaInosservati() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(274)) {
            IlColpo colpo = prendiIlColpo(partita);
            assertTrue(colpo.getGuardie().crea().stream().noneMatch(p -> p.isDaAffrontare()));
            // In città il comando non c'è
            assertFalse(partita.comandiDisponibili().contains(Comando.PASSA_INOSSERVATO));
        }
    }

    private static IlColpo prendiIlColpo(PartitaDiTest partita) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
        IlColpo colpo = RegistroMissioni.getTutteLeMissioni().stream().filter(IlColpo.class::isInstance)
                .map(IlColpo.class::cast).findFirst().orElseThrow(AssertionError::new);
        colpo.aggiungiProprieta("PARAMETRO_" + IlColpo.COLPO, LIBRO);
        colpo.controllaPreLocazione();
        colpo.segnaIntermezzoPassoMostrato("INCARICO");
        colpo.controllaInLocazione();
        assertTrue(colpo.isAttiva());
        assertEquals(ClassiLocazione.ROVINE, Foresta.getLocazione(colpo.getPosto()));
        return colpo;
    }

    private static void entra(PartitaDiTest partita, CoordinateMD posto) {
        partita.comando(Comando.ESCI_DA_CITTA);
        partita.gruppo().setCoordinate(new CoordinateMD(posto.getX(), posto.getY() + 1));
        partita.assertStato(Stato.SCELTA_DIREZIONE);
        partita.comando(Comando.NORD).comando(Comando.NUMERO_1);
        assertEquals(posto, partita.gruppo().getCoordinate());
        partita.assertStato(Stato.IN_LOCAZIONE);
    }

    @Test
    void dopoQualunqueAltraAzioneNonSiPassaPiuInosservati() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(275)) {
            IlColpo colpo = prendiIlColpo(partita);
            partita.gruppo().addPozioniSalute(1);
            entra(partita, colpo.getPosto());
            // Farsi ridescrivere la locazione non è un'azione
            partita.comando(Comando.AIUTO);
            assertTrue(partita.comandiDisponibili().contains(Comando.PASSA_INOSSERVATO), String.valueOf(partita.comandiDisponibili()));
            // Bere una pozione sì
            partita.comando(Comando.POZIONE_SALUTE);
            partita.assertStato(Stato.IN_LOCAZIONE);
            assertFalse(partita.comandiDisponibili().contains(Comando.PASSA_INOSSERVATO), String.valueOf(partita.comandiDisponibili()));
        }
    }
}
