package com.threeamigos.foresta.motore.modellodati;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Stefano Reksten
 */
class ArtefattoMDTest {

    @Test
    void memorizzaValori() {
        // Given
        TipoArtefatto tipo = TipoArtefatto.SPADA;
        String nome = "Excalibur";
        String descrizione = "La spada della Dama del Lago";
        int livello = 100;
        int danni = 50;
        int costoAcquisto = 999;
        double peso = 5.0;
        // When
        ArtefattoMD artefatto = new ArtefattoMD();
        artefatto.setTipo(tipo);
        artefatto.setNome(nome);
        artefatto.setDescrizione(descrizione);
        artefatto.setLivello(livello);
        artefatto.setDanni(danni);
        artefatto.setCostoAcquisto(costoAcquisto);
        artefatto.setPeso(peso);
        // Then
        assertEquals(tipo, artefatto.getTipo());
        assertEquals(nome, artefatto.getNome());
        assertEquals(descrizione, artefatto.getDescrizione());
        assertEquals(livello, artefatto.getLivello());
        assertEquals(danni, artefatto.getDanni());
        assertEquals(costoAcquisto, artefatto.getCostoAcquisto());
        assertEquals(peso, artefatto.getPeso());
    }

    @Test
    void memorizzaModificatori() {
        // Given
        ArtefattoMD artefatto = new ArtefattoMD();
        // When
        artefatto.addModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_PERCENTUALE, 10, "Carisma");
        artefatto.addModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_FISSO, 5, "Valore");
        // Then
        Collection<ModificatoreAttributo> modificatori = artefatto.getModificatori();
        assertEquals(2, modificatori.size());
        assertTrue(modificatori.contains(new ModificatoreAttributo(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_PERCENTUALE, 10, "Carisma")));
        assertTrue(modificatori.contains(new ModificatoreAttributo(TipoAttributo.VALORE, TipoModificatore.AUMENTO_FISSO, 5, "Valore")));
    }

    @Test
    void salvaERicarica() throws IOException {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        TipoArtefatto tipo = TipoArtefatto.SPADA;
        String nome = "Excalibur";
        String descrizione = "La spada della Dama del Lago";
        int livello = 100;
        int danni = 50;
        int costoAcquisto = 999;
        double peso = 5.0;
        ArtefattoMD artefatto = new ArtefattoMD();
        artefatto.setTipo(tipo);
        artefatto.setNome(nome);
        artefatto.setDescrizione(descrizione);
        artefatto.setLivello(livello);
        artefatto.setDanni(danni);
        artefatto.setCostoAcquisto(costoAcquisto);
        artefatto.setPeso(peso);
        artefatto.addModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_PERCENTUALE, 10, "Carisma");
        ModificatoreAttributo modificatore = new ModificatoreAttributo(TipoAttributo.VALORE, TipoModificatore.AUMENTO_FISSO, 5, "Valore");
        artefatto.addModificatore(modificatore);
        // When
        artefatto.salva(printWriter);
        printWriter.flush();
        String risultato = stringWriter.toString();
        StringReader stringReader = new StringReader(risultato);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        ArtefattoMD artefattoRicaricato = new ArtefattoMD();
        artefattoRicaricato.leggi(bufferedReader);
        // Then
        assertEquals(tipo, artefattoRicaricato.getTipo());
        assertEquals(nome, artefattoRicaricato.getNome());
        assertEquals(descrizione, artefattoRicaricato.getDescrizione());
        assertEquals(livello, artefattoRicaricato.getLivello());
        assertEquals(danni, artefattoRicaricato.getDanni());
        assertEquals(costoAcquisto, artefattoRicaricato.getCostoAcquisto());
        assertEquals(peso, artefattoRicaricato.getPeso(), 0.0001);
        Collection<ModificatoreAttributo> modificatoriRicaricati = artefattoRicaricato.getModificatori();
        assertEquals(2, modificatoriRicaricati.size());
        assertTrue(modificatoriRicaricati.contains(new ModificatoreAttributo(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_PERCENTUALE, 10, "Carisma")));
        assertTrue(modificatoriRicaricati.contains(new ModificatoreAttributo(TipoAttributo.VALORE, TipoModificatore.AUMENTO_FISSO, 5, "Valore")));
    }

}