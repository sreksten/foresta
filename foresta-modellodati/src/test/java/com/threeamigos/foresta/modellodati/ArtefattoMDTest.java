package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        artefatto.addModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "Carisma");
        artefatto.addModificatore(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_FISSA, 5, "Valore");
        // Then
        Collection<ModificatoreAttributoMD> modificatori = artefatto.getModificatori();
        assertEquals(2, modificatori.size());
        assertTrue(modificatori.contains(new ModificatoreAttributoMD(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "Carisma")));
        assertTrue(modificatori.contains(new ModificatoreAttributoMD(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_FISSA, 5, "Valore")));
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
        artefatto.addModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "Carisma");
        ModificatoreAttributoMD modificatore = new ModificatoreAttributoMD(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_FISSA, 5, "Valore");
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
        Collection<ModificatoreAttributoMD> modificatoriRicaricati = artefattoRicaricato.getModificatori();
        assertEquals(2, modificatoriRicaricati.size());
        assertTrue(modificatoriRicaricati.contains(new ModificatoreAttributoMD(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "Carisma")));
        assertTrue(modificatoriRicaricati.contains(new ModificatoreAttributoMD(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_FISSA, 5, "Valore")));
    }

    @Test
    void laDescrizioneAssenteRestaNullDopoIlSalvataggio() throws IOException {
        ArtefattoMD artefatto = new ArtefattoMD();
        artefatto.setTipo(TipoArtefatto.SPADA);
        artefatto.setNome("una spada");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        artefatto.salva(printWriter);
        printWriter.flush();
        ArtefattoMD ricaricato = new ArtefattoMD();
        ricaricato.leggi(new BufferedReader(new StringReader(stringWriter.toString())));
        assertNull(ricaricato.getDescrizione());
        assertEquals("una spada", ricaricato.getNomeCompleto());
    }

    @Test
    void salvaERicaricaTuttiITipiDiModificatore() throws IOException {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.ANELLO, "un Anello magico della Sirena", "che aumenta il Carisma");
        artefatto.addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "Forza");
        artefatto.addModificatore(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, 2.5, "Coraggio");
        artefatto.addModificatore(TipoAttributo.VELOCITA, TipoModificatore.QUANTITA_ASSOLUTA, -3, "Velocita");
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        Collection<ModificatoreAttributoMD> modificatori = ricaricato.getModificatori();
        assertEquals(3, modificatori.size());
        assertTrue(modificatori.contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 5, "Forza")));
        assertTrue(modificatori.contains(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, 2.5, "Coraggio")));
        assertTrue(modificatori.contains(new ModificatoreAttributoMD(TipoAttributo.VELOCITA, TipoModificatore.QUANTITA_ASSOLUTA, -3, "Velocita")));
    }

    @Test
    void salvaERicaricaModificatoreSenzaNote() throws IOException {
        // Given: il costruttore a tre argomenti (quello usato dal CostruttoreArtefatto e
        // dall'anello magico) mette come nota la stringa vuota
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.ANELLO, "un Anello magico del Grifone", "che aumenta il Valore");
        ModificatoreAttributoMD modificatore = new ModificatoreAttributoMD(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_FISSA, 5);
        artefatto.addModificatore(modificatore);
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        assertEquals(1, ricaricato.getModificatori().size());
        assertTrue(ricaricato.getModificatori().contains(modificatore));
    }

    @Test
    void salvaERicaricaArtefattoSenzaModificatoriNeIncantamenti() throws IOException {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.ELMO, "l'elmo di Gorgor", "che protegge poco");
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        assertEquals(TipoArtefatto.ELMO, ricaricato.getTipo());
        assertEquals("l'elmo di Gorgor", ricaricato.getNome());
        assertEquals("che protegge poco", ricaricato.getDescrizione());
        assertTrue(ricaricato.getModificatori().isEmpty());
        assertTrue(ricaricato.getIncantamenti().isEmpty());
    }

    @Test
    void salvaERicaricaModificatoriEIncantamenti() throws IOException {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la Spada della Morte alata", "che massacra i porci");
        artefatto.addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_PERCENTUALE, 5, "Forza");
        artefatto.addIncantamento("Il Peperoncino di Cayenna", TipoDanno.FUOCO, 10, 0.5);
        artefatto.addIncantamento("La serpe di Yalar", TipoDanno.VELENO, 4, 0.25);
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        assertEquals(1, ricaricato.getModificatori().size());
        assertTrue(ricaricato.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_PERCENTUALE, 5, "Forza")));
        List<IncantamentoMD> incantamenti = new ArrayList<>(ricaricato.getIncantamenti());
        assertEquals(2, incantamenti.size());
        verificaIncantamento(incantamenti, "Il Peperoncino di Cayenna", TipoDanno.FUOCO, 10, 0.5);
        verificaIncantamento(incantamenti, "La serpe di Yalar", TipoDanno.VELENO, 4, 0.25);
    }

    @Test
    void salvaERicaricaPiuArtefattiDiSeguito() throws IOException {
        // Given: come nei salvataggi veri (inventari, registro), più artefatti uno dopo
        // l'altro nello stesso stream; ognuno deve leggere solo le proprie righe
        ArtefattoMD primo = creaArtefatto(TipoArtefatto.SCUDO, "lo scudo fiscale", "che si fa fare sconti");
        primo.addModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_FISSA, 3, "Parata");
        primo.addIncantamento("La battuta del cavolo", TipoDanno.GELO, 10, 0.5);
        ArtefattoMD secondo = creaArtefatto(TipoArtefatto.TALISMANO, "il talismano di Yalar", "il cui potere è nella fortuna");
        secondo.addModificatore(TipoAttributo.FORTUNA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "Fortuna");
        secondo.addModificatore(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 1, "Carisma");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        primo.salva(printWriter);
        secondo.salva(printWriter);
        printWriter.flush();
        BufferedReader reader = new BufferedReader(new StringReader(stringWriter.toString()));
        // When
        ArtefattoMD primoRicaricato = new ArtefattoMD();
        primoRicaricato.leggi(reader);
        ArtefattoMD secondoRicaricato = new ArtefattoMD();
        secondoRicaricato.leggi(reader);
        // Then
        assertEquals("lo scudo fiscale", primoRicaricato.getNome());
        assertEquals(1, primoRicaricato.getModificatori().size());
        assertEquals(1, primoRicaricato.getIncantamenti().size());
        assertEquals("il talismano di Yalar", secondoRicaricato.getNome());
        assertEquals("il cui potere è nella fortuna", secondoRicaricato.getDescrizione());
        assertEquals(2, secondoRicaricato.getModificatori().size());
        assertTrue(secondoRicaricato.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "Fortuna")));
        assertTrue(secondoRicaricato.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 1, "Carisma")));
        assertTrue(secondoRicaricato.getIncantamenti().isEmpty());
        assertNull(reader.readLine());
    }

    @Test
    void salvaERicaricaNomeProprio() throws IOException {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "che brucia i nemici");
        artefatto.setNomeProprio("Diavolina");
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        assertEquals("Diavolina", ricaricato.getNomeProprio());
        assertEquals("la spada di fuoco", ricaricato.getNome());
        assertEquals("che brucia i nemici", ricaricato.getDescrizione());
    }

    @Test
    void salvaERicaricaSenzaNomeProprio() throws IOException {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "che brucia i nemici");
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        assertNull(ricaricato.getNomeProprio());
        assertEquals("la spada di fuoco", ricaricato.getNome());
    }

    @Test
    void salvaERicaricaRarita() throws IOException {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la Leggendaria Spada del Fulmine con Rinterzo", "che non perdona");
        artefatto.setRarita(TipoRaritaArtefatto.LEGGENDARIO);
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        assertEquals(TipoRaritaArtefatto.LEGGENDARIO, ricaricato.getRarita());
    }

    @Test
    void ilPipeSparisceDaiTesti() throws IOException {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada | di fuoco", "che | brucia");
        artefatto.setNomeProprio("Dia|volina");
        artefatto.addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1, "no|ta");
        artefatto.addIncantamento("Fiam|ma", TipoDanno.FUOCO, 5, 0.05);
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        assertEquals("Diavolina", ricaricato.getNomeProprio());
        assertEquals("la spada  di fuoco", ricaricato.getNome());
        assertEquals("che  brucia", ricaricato.getDescrizione());
        assertTrue(ricaricato.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1, "nota")));
        assertEquals("Fiamma", ricaricato.getIncantamenti().iterator().next().getNomeIncantamento());
        assertNull(ArtefattoMD.normalizzaNomeProprio("|"));
    }

    @Test
    void testiVuotiSiSalvanoESiRileggono() throws IOException {
        // Given: descrizione e nota vuote, nome vuoto che diventa "nessun nome"
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "", "");
        artefatto.addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1, "");
        artefatto.addIncantamento(" ", TipoDanno.FUOCO, 5, 0.05);
        assertEquals(ArtefattoMD.NESSUN_NOME, artefatto.getNome());
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then: i campi vuoti non spostano quelli che seguono
        assertEquals(ArtefattoMD.NESSUN_NOME, ricaricato.getNome());
        assertNull(ricaricato.getDescrizione());
        assertEquals(3, ricaricato.getLivello());
        assertEquals(7, ricaricato.getDanni());
        assertTrue(ricaricato.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1)));
        assertEquals(ArtefattoMD.NESSUN_NOME, ricaricato.getIncantamenti().iterator().next().getNomeIncantamento());
    }

    @Test
    void unArtefattoNuovoEComune() {
        assertEquals(TipoRaritaArtefatto.COMUNE, new ArtefattoMD().getRarita());
    }

    @Test
    void nomeProprioVuotoValeComeNessunNome() {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "che brucia i nemici");
        // When
        artefatto.setNomeProprio("   ");
        // Then
        assertNull(artefatto.getNomeProprio());
    }

    @Test
    void salvaERicaricaSlotEquipaggiamento() throws IOException {
        // Given: la spada di un Ladro nella mano secondaria
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada corta", "che punge");
        artefatto.setSlotEquipaggiamento(TipoSlotArtefatto.MANO_SECONDARIA);
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        assertEquals(TipoSlotArtefatto.MANO_SECONDARIA, ricaricato.getSlotEquipaggiamento());
    }

    @Test
    void salvaERicaricaArtefattoNonEquipaggiato() throws IOException {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADONE, "lo spadone del gigante", "che spacca le rocce");
        // When
        ArtefattoMD ricaricato = salvaERileggi(artefatto);
        // Then
        assertEquals(TipoArtefatto.SPADONE, ricaricato.getTipo());
        assertNull(ricaricato.getSlotEquipaggiamento());
        assertNull(ricaricato.getAspetto(), "l'aspetto è quello del tipo");
    }

    @Test
    void salvaERicaricaLAspetto() throws IOException {
        // Given: un libro magico con l'aspetto di un libro tecnico
        ArtefattoMD libro = creaArtefatto(TipoArtefatto.LIBRO_MAGICO, "il Manuale di Unix System V", "che spiega tutto, tranne come si esce dall'editor");
        libro.setAspetto(TipoOggetto.LIBRO_TECNICO);
        // When
        ArtefattoMD ricaricato = salvaERileggi(libro);
        // Then
        assertEquals(TipoOggetto.LIBRO_TECNICO, ricaricato.getAspetto());
    }

    @Test
    void salvaERicaricaPergamena() throws IOException {
        // Given: una pergamena che porta due incantamenti
        ArtefattoMD pergamena = creaArtefatto(TipoArtefatto.PERGAMENA, "una pergamena del fuoco", "che arde di magia");
        pergamena.setDanni(0);
        pergamena.addIncantamento("Fiamma", TipoDanno.FUOCO, 10, 0.1);
        pergamena.addIncantamento("Brina", TipoDanno.GELO, 0, 0.05);
        // When
        ArtefattoMD ricaricato = salvaERileggi(pergamena);
        // Then
        assertEquals(TipoArtefatto.PERGAMENA, ricaricato.getTipo());
        assertEquals(TipoSlotArtefatto.NUCLEO, ricaricato.getTipo().getSlotArtefatto());
        assertEquals("una pergamena del fuoco", ricaricato.getNome());
        assertTrue(ricaricato.getModificatori().isEmpty());
        List<IncantamentoMD> incantamenti = new ArrayList<>(ricaricato.getIncantamenti());
        assertEquals(2, incantamenti.size());
        verificaIncantamento(incantamenti, "Fiamma", TipoDanno.FUOCO, 10, 0.1);
        verificaIncantamento(incantamenti, "Brina", TipoDanno.GELO, 0, 0.05);
    }

    @Test
    void nomeCompletoConNomeProprio() {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "che brucia i nemici");
        artefatto.setNomeProprio("Diavolina");
        // Then
        assertEquals("Diavolina, la spada di fuoco, che brucia i nemici", artefatto.getNomeCompleto());
        assertEquals("Diavolina", artefatto.getNomeBreve());
        assertEquals("la spada di fuoco, che brucia i nemici", artefatto.getDescrizioneBreve());
    }

    @Test
    void nomeCompletoSenzaNomeProprio() {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "che brucia i nemici");
        // Then
        assertEquals("la spada di fuoco, che brucia i nemici", artefatto.getNomeCompleto());
        assertEquals("la spada di fuoco", artefatto.getNomeBreve());
        assertEquals("che brucia i nemici", artefatto.getDescrizioneBreve());
    }

    @Test
    void nomeCompletoSenzaDescrizione() {
        // Given
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "");
        artefatto.setNomeProprio("Diavolina");
        // Then
        assertEquals("Diavolina, la spada di fuoco", artefatto.getNomeCompleto());
    }

    @Test
    void normalizzaNomeProprioScelto() {
        assertEquals("Lama Del Drago", ArtefattoMD.normalizzaNomeProprio("lama del drago"));
        assertEquals("Lama Del Drago", ArtefattoMD.normalizzaNomeProprio("  lama   del drago "));
        assertNull(ArtefattoMD.normalizzaNomeProprio("  "));
        assertNull(ArtefattoMD.normalizzaNomeProprio(null));
    }

    static ArtefattoMD creaArtefatto(TipoArtefatto tipo, String nome, String descrizione) {
        ArtefattoMD artefatto = new ArtefattoMD();
        artefatto.setTipo(tipo);
        artefatto.setNome(nome);
        artefatto.setDescrizione(descrizione);
        artefatto.setLivello(3);
        artefatto.setDanni(7);
        artefatto.setCostoAcquisto(20);
        artefatto.setPeso(1.5);
        return artefatto;
    }

    private static ArtefattoMD salvaERileggi(ArtefattoMD artefatto) throws IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        artefatto.salva(printWriter);
        printWriter.flush();
        ArtefattoMD ricaricato = new ArtefattoMD();
        ricaricato.leggi(new BufferedReader(new StringReader(stringWriter.toString())));
        return ricaricato;
    }

    private static void verificaIncantamento(List<IncantamentoMD> incantamenti, String nome, TipoDanno tipoDanno,
                                             int dannoBonusFisso, double coefficienteScala) {
        IncantamentoMD incantamento = incantamenti.stream()
                .filter(i -> i.getNomeIncantamento().equals(nome))
                .findFirst()
                .orElseThrow(() -> new AssertionError("IncantamentoMD non riletto: " + nome));
        assertEquals(tipoDanno, incantamento.getTipoDannoElementale());
        assertEquals(dannoBonusFisso, incantamento.getDannoBonusFisso());
        assertEquals(coefficienteScala, incantamento.getCoefficienteScala(), 0.0001);
    }


    @Test
    void identificativoSetLeggendarioEFigliVisibili() {
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia");
        assertNotNull(artefatto.getUuid());
        assertNotEquals(artefatto.getUuid(), creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia").getUuid());
        artefatto.setPezzoDiSetLeggendario("SET_DEL_DRAGO", "LAMA");
        assertEquals("SET_DEL_DRAGO", artefatto.getSetLeggendario());
        assertEquals("LAMA", artefatto.getPezzoLeggendario());
        artefatto.setFigliVisibili(false);
        assertFalse(artefatto.isFigliVisibili());
        artefatto.setFigliVisibili(true);
        assertTrue(artefatto.isFigliVisibili());
    }

    @Test
    void nomiVuotiONulli() {
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia");
        artefatto.setNome(null);
        assertEquals(ArtefattoMD.NESSUN_NOME, artefatto.getNome());
        artefatto.setNome("   ");
        assertEquals(ArtefattoMD.NESSUN_NOME, artefatto.getNome());
        artefatto.setNomeProprio(null);
        assertNull(artefatto.getNomeProprio());
        artefatto.setNomeProprio("  ");
        assertNull(artefatto.getNomeProprio());
    }

    @Test
    void conIlNomeProprioLaDescrizioneBreveMetteIlNomeEPoiLaDescrizioneSeCe() {
        ArtefattoMD artefatto = creaArtefatto(TipoArtefatto.SPADA, "la spada di fuoco", "che brucia i nemici");
        assertEquals("che brucia i nemici", artefatto.getDescrizioneBreve(), "senza nome proprio, la sola descrizione");
        artefatto.setNomeProprio("Diavolina");
        assertEquals("la spada di fuoco, che brucia i nemici", artefatto.getDescrizioneBreve());
        artefatto.setDescrizione("");
        assertEquals("la spada di fuoco", artefatto.getDescrizioneBreve());
        artefatto.setDescrizione(null);
        assertEquals("la spada di fuoco", artefatto.getDescrizioneBreve());
    }

    @Test
    void unModificatoreSulBersaglioStaSoloSuArmiEIngredienti() {
        ModificatoreAttributoMD sulBersaglio = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0.0d,
                "", UnitaDurataModificatore.TURNI, 3, BersaglioModificatore.BERSAGLIO);
        creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia").addModificatore(sulBersaglio);
        creaArtefatto(TipoArtefatto.PERGAMENA, "la pergamena", "che porta l'effetto").addModificatore(sulBersaglio);
        ArtefattoMD elmo = creaArtefatto(TipoArtefatto.ELMO, "l'elmo", "che protegge");
        IllegalArgumentException errore = assertThrows(IllegalArgumentException.class, () -> elmo.addModificatore(sulBersaglio));
        assertTrue(errore.getMessage().contains("ELMO"), errore.getMessage());
        elmo.addModificatore(sulBersaglio.suPortatore());
    }

    @Test
    void modificatoriEIncantamentiSiTolgono() {
        ArtefattoMD spada = creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia");
        ModificatoreAttributoMD forza = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2.0d);
        spada.addModificatore(forza);
        assertTrue(spada.removeModificatore(forza));
        assertFalse(spada.removeModificatore(forza));
        IncantamentoMD fuoco = new IncantamentoMD("Fuoco", TipoDanno.FUOCO, 5, 0.1d);
        spada.addIncantamento(fuoco);
        assertTrue(spada.getIncantamenti().contains(fuoco));
        assertTrue(spada.removeIncantamento(fuoco));
        assertFalse(spada.removeIncantamento(fuoco));
    }
}
