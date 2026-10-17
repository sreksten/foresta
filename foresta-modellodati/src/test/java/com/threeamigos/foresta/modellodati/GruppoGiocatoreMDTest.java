package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il gruppo del giocatore: personaggi e capo, pergamene e pozioni (GruppoMD), e in più monete, preziosi, posizione, sfide,
 * artefatti e ospiti, e come si salva.
 */
class GruppoGiocatoreMDTest {

    private static PersonaggioMD personaggio(TipoPersonaggio classe, String nome) {
        PersonaggioMD personaggio = new PersonaggioMD();
        personaggio.setClasse(classe);
        personaggio.setNome(nome);
        personaggio.setVivo(true);
        return personaggio;
    }

    private static GruppoGiocatoreMD salvaERileggi(GruppoGiocatoreMD gruppo) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            gruppo.salva(stream);
        }
        GruppoGiocatoreMD riletto = new GruppoGiocatoreMD();
        riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));
        return riletto;
    }

    @Test
    void scorteECampiDelGruppoSiLeggonoESiCambiano() {
        GruppoGiocatoreMD gruppo = new GruppoGiocatoreMD();
        gruppo.setPozioniSalute(1);
        gruppo.setPozioniSaluteGrande(2);
        gruppo.setPozioniMagia(3);
        gruppo.setPozioniMagiaGrande(4);
        gruppo.setPozioniAumentoLivello(5);
        gruppo.setMonete(100);
        gruppo.setPreziosi(7);
        gruppo.setSfideVinte(2);
        gruppo.setCoordinate(new CoordinateMD(3, 9));
        assertEquals(1, gruppo.getPozioniSalute());
        assertEquals(2, gruppo.getPozioniSaluteGrande());
        assertEquals(3, gruppo.getPozioniMagia());
        assertEquals(4, gruppo.getPozioniMagiaGrande());
        assertEquals(5, gruppo.getPozioniAumentoLivello());
        assertEquals(100, gruppo.getMonete());
        assertEquals(7, gruppo.getPreziosi());
        assertEquals(2, gruppo.getSfideVinte());
        assertEquals(new CoordinateMD(3, 9), gruppo.getCoordinate());
    }

    @Test
    void ilGruppoCompletoSiSalvaESiRilegge() throws IOException {
        GruppoGiocatoreMD gruppo = new GruppoGiocatoreMD();
        gruppo.addPersonaggioMD(personaggio(TipoPersonaggio.GUERRIERO, "Arsenio"));
        gruppo.addPersonaggioMD(personaggio(TipoPersonaggio.MAGA, "Pippa"));
        gruppo.setIndiceCapo(1);
        gruppo.setIncantesimi(TipoIncantesimo.FUOCO, 3);
        gruppo.setPozioniSalute(2);
        gruppo.setPozioniMagiaGrande(1);
        gruppo.setMonete(250);
        gruppo.setPreziosi(4);
        gruppo.setSfideVinte(1);
        gruppo.setCoordinate(new CoordinateMD(5, 6));
        gruppo.aggiungiArtefatto(ArtefattoMDTest.creaArtefatto(TipoArtefatto.ANELLO, "l'anello", "che brilla"));
        PersonaggioMD bardo = personaggio(TipoPersonaggio.BARDO_DELLE_LOCANDE, "Lino");
        gruppo.aggiungiOspite(bardo);
        gruppo.aggiungiOspiteVulnerabile(bardo.getUuid());

        GruppoGiocatoreMD riletto = salvaERileggi(gruppo);

        assertEquals(Arrays.asList("Arsenio", "Pippa"), Arrays.asList(riletto.getPersonaggiMD().get(0).getNome(), riletto.getPersonaggiMD().get(1).getNome()));
        assertEquals(1, riletto.getIndiceCapo());
        assertEquals(3, riletto.getIncantesimi(TipoIncantesimo.FUOCO));
        assertEquals(2, riletto.getPozioniSalute());
        assertEquals(1, riletto.getPozioniMagiaGrande());
        assertEquals(250, riletto.getMonete());
        assertEquals(4, riletto.getPreziosi());
        assertEquals(1, riletto.getSfideVinte());
        assertEquals(new CoordinateMD(5, 6), riletto.getCoordinate());
        assertEquals("l'anello", riletto.getArtefatti().iterator().next().getNome());
        assertEquals("Lino", riletto.getOspitiMD().get(0).getNome());
        assertEquals(Collections.singleton(bardo.getUuid()), riletto.getOspitiVulnerabili());
    }

    @Test
    void ospitiEOspitiVulnerabiliSiAggiungonoESiTolgono() {
        GruppoGiocatoreMD gruppo = new GruppoGiocatoreMD();
        PersonaggioMD bardo = personaggio(TipoPersonaggio.BARDO_DELLE_LOCANDE, "Lino");
        gruppo.aggiungiOspite(bardo);
        gruppo.aggiungiOspiteVulnerabile(bardo.getUuid());
        gruppo.togliOspite(bardo);
        gruppo.togliOspiteVulnerabile(bardo.getUuid());
        assertTrue(gruppo.getOspitiMD().isEmpty());
        assertTrue(gruppo.getOspitiVulnerabili().isEmpty());
    }
}
