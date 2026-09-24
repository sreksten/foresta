package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.RaritaArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.oggetti.Artefatto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncantatoreTest {

    private GruppoGiocatore gruppo;

    @BeforeEach
    void nuovoGruppo() {
        ModelloDati.setIstanza(new ModelloDati());
        gruppo = new GruppoGiocatore();
        gruppo.subMonete(gruppo.getMonete());
        gruppo.addMonete(100);
    }

    // --- Cosa si può mettere sul banco

    @Test
    void gliAccessoriNonSiIncantano() {
        assertEquals(Optional.of(MotivoRifiutoIncantatura.NON_INCANTABILE),
                RegoleIncantatura.puoMettereSulBanco(Collections.emptyList(), artefatto(TipoArtefatto.ANELLO, 5)));
        assertEquals(Optional.of(MotivoRifiutoIncantatura.NON_INCANTABILE),
                RegoleIncantatura.puoMettereSulBanco(Collections.emptyList(), artefatto(TipoArtefatto.LIBRO_MAGICO, 5)));
    }

    @Test
    void unArtefattoAllaVolta() {
        List<Artefatto> banco = Collections.singletonList(artefatto(TipoArtefatto.SPADA, 5));
        assertEquals(Optional.of(MotivoRifiutoIncantatura.PIU_ARTEFATTI),
                RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.ELMO, 5)));
    }

    @Test
    void livelloTreConUnIncantamentoNeRiceveAlPiuUnAltro() {
        // Una spada comune di livello 3 ha 2 posti, uno già preso
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        spada.getModelloDati().addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        List<Artefatto> banco = new ArrayList<>(Collections.singletonList(spada));
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(banco, pergamena(1)));
        banco.add(pergamena(1));
        assertEquals(Optional.of(MotivoRifiutoIncantatura.LIMITE_SUPERATO), RegoleIncantatura.puoMettereSulBanco(banco, pergamena(1)));
        // Una pergamena con due effetti non ci sta
        assertEquals(Optional.of(MotivoRifiutoIncantatura.LIMITE_SUPERATO),
                RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(spada), pergamena(2)));
    }

    @Test
    void conLePergameneGiaSulBancoSiControllaLArtefattoCheArriva() {
        // Prima le pergamene (3 effetti), poi una spada comune di livello 3 (2 posti)
        List<Artefatto> banco = Arrays.asList(pergamena(2), pergamena(1));
        assertEquals(Optional.of(MotivoRifiutoIncantatura.LIMITE_SUPERATO),
                RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.SPADA, 3)));
        assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(banco, artefatto(TipoArtefatto.SPADA, 4)));
    }

    @Test
    void ilTettoDeiLeggendariECinque() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 20);
        spada.getModelloDati().setRarita(RaritaArtefatto.LEGGENDARIO);
        List<Artefatto> banco = Arrays.asList(spada, pergamena(3), pergamena(2));
        assertEquals(Optional.empty(), RegoleIncantatura.verifica(banco, 1000));
        assertEquals(Optional.of(MotivoRifiutoIncantatura.LIMITE_SUPERATO), RegoleIncantatura.puoMettereSulBanco(banco, pergamena(1)));
    }

    // --- Verifica della fusione

    @Test
    void rifiutiDellaFusione() {
        assertEquals(Optional.of(MotivoRifiutoIncantatura.NESSUN_ARTEFATTO),
                RegoleIncantatura.verifica(Collections.singletonList(pergamena(1)), 100));
        assertEquals(Optional.of(MotivoRifiutoIncantatura.NESSUNA_PERGAMENA),
                RegoleIncantatura.verifica(Collections.singletonList(artefatto(TipoArtefatto.SPADA, 4)), 100));
        assertEquals(Optional.of(MotivoRifiutoIncantatura.PIU_ARTEFATTI),
                RegoleIncantatura.verifica(Arrays.asList(artefatto(TipoArtefatto.SPADA, 4), artefatto(TipoArtefatto.ELMO, 4), pergamena(1)), 100));
        // 10 + 5 × 2 = 20 monete
        List<Artefatto> banco = Arrays.asList(artefatto(TipoArtefatto.SPADA, 4), pergamena(2));
        assertEquals(20, RegoleIncantatura.costo(banco));
        assertEquals(Optional.of(MotivoRifiutoIncantatura.MONETE_INSUFFICIENTI), RegoleIncantatura.verifica(banco, 19));
        assertEquals(Optional.empty(), RegoleIncantatura.verifica(banco, 20));
    }

    // --- Fusione

    @Test
    void fusioneRiuscita() {
        // Given: una spada di livello 4 (3 posti) e una pergamena con un incantamento e un modificatore
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 4);
        Artefatto pergamena = pergamena(0);
        pergamena.getModelloDati().addIncantamento("Gelo medio", TipoDanno.GELO, 10, 0.1);
        pergamena.getModelloDati().addModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 2, "");
        gruppo.addArtefatto(spada);
        gruppo.addArtefatto(pergamena);
        AutomaIncantatore incantatore = new AutomaIncantatore(gruppo, new BancoDiLavoro());
        incantatore.richiediSpostamentoSuParteRemota(spada);
        incantatore.richiediSpostamentoSuParteRemota(pergamena);
        assertTrue(gruppo.getInventario().isEmpty());
        // When
        Optional<MotivoRifiutoIncantatura> esito = gruppo.incanta(incantatore.getBanco(), "lama del drago");
        // Then
        assertEquals(Optional.empty(), esito);
        assertEquals(100 - 20, gruppo.getMonete());
        assertTrue(incantatore.getBanco().getInventario().isEmpty());
        assertEquals(1, gruppo.getInventario().size());
        Artefatto incantata = gruppo.getInventario().iterator().next();
        assertEquals(TipoArtefatto.SPADA, incantata.getTipo());
        assertEquals("Lama Del Drago", incantata.getNomeProprio().orElse(null));
        assertEquals(1, incantata.getIncantamenti().size());
        assertTrue(incantata.getModificatori().contains(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 2)));
    }

    @Test
    void conIlNomeVuotoLArtefattoNonHaNomeProprio() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 4);
        spada.getModelloDati().setNomeProprio("Diavolina");
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(spada);
        banco.addArtefatto(pergamena(1));
        assertEquals(Optional.empty(), gruppo.incanta(banco, ""));
        assertNull(gruppo.getInventario().iterator().next().getModelloDati().getNomeProprio());
    }

    @Test
    void unaFusioneRifiutataNonToccaNulla() {
        gruppo.subMonete(gruppo.getMonete());
        BancoDiLavoro banco = new BancoDiLavoro();
        banco.addArtefatto(artefatto(TipoArtefatto.SPADA, 4));
        banco.addArtefatto(pergamena(1));
        assertEquals(Optional.of(MotivoRifiutoIncantatura.MONETE_INSUFFICIENTI), gruppo.incanta(banco, "Diavolina"));
        assertEquals(2, banco.getInventario().size());
        assertTrue(gruppo.getInventario().isEmpty());
    }

    // --- Banco

    @Test
    void ilBancoRifiutaEUscendoTornaTuttoNelGruppo() {
        Artefatto anello = artefatto(TipoArtefatto.ANELLO, 3);
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 4);
        Artefatto pergamena = pergamena(1);
        gruppo.addArtefatto(anello);
        gruppo.addArtefatto(spada);
        gruppo.addArtefatto(pergamena);
        AutomaIncantatore incantatore = new AutomaIncantatore(gruppo, new BancoDiLavoro());
        // L'anello non va sul banco
        incantatore.richiediSpostamentoSuParteRemota(anello);
        assertEquals(3, gruppo.getInventario().size());
        incantatore.richiediSpostamentoSuParteRemota(spada);
        incantatore.richiediSpostamentoSuParteRemota(pergamena);
        assertEquals(2, incantatore.getBanco().getInventario().size());
        // Dal banco si torna al gruppo liberamente
        incantatore.richiediSpostamentoSuParteAttiva(pergamena);
        assertEquals(1, incantatore.getBanco().getInventario().size());
        // Uscendo, il banco si svuota nel gruppo
        incantatore.svuotaBanco();
        assertTrue(incantatore.getBanco().getInventario().isEmpty());
        assertEquals(3, gruppo.getInventario().size());
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(1);
        return Artefatto.di(md);
    }

    private static Artefatto pergamena(int effetti) {
        Artefatto pergamena = artefatto(TipoArtefatto.INCANTAMENTO, 1);
        for (int i = 0; i < effetti; i++) {
            pergamena.getModelloDati().addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        }
        return pergamena;
    }
}
