package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoVenditaArtefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoNegozio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class NegoziTest {

    private static final CoordinateMD CITTA = new CoordinateMD(5, 6);

    private GruppoGiocatore gruppo;

    @BeforeEach
    void nuovoMondo() {
        ModelloDati.setIstanza(new ModelloDati());
        gruppo = new GruppoGiocatore();
        gruppo.subMonete(gruppo.getMonete());
    }

    @Test
    void ilVenditoreTrattaSoloPergameneLArmaioloTuttoIlResto() {
        assertTrue(TipoNegozio.VENDITORE_DI_PERGAMENE.tratta(TipoArtefatto.PERGAMENA));
        assertFalse(TipoNegozio.VENDITORE_DI_PERGAMENE.tratta(TipoArtefatto.SPADA));
        assertFalse(TipoNegozio.ARMAIOLO.tratta(TipoArtefatto.PERGAMENA));
        assertTrue(TipoNegozio.ARMAIOLO.tratta(TipoArtefatto.SPADA));
        assertTrue(TipoNegozio.ARMAIOLO.tratta(TipoArtefatto.ANELLO));
    }

    @Test
    void lArmaioloNonCompraPergamene() {
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 30);
        gruppo.addArtefatto(pergamena);
        ScambiatoreArtefatti armaiolo = RegistroArtefatti.getScambiatorePerNegozio(CITTA, TipoNegozio.ARMAIOLO);

        assertFalse(gruppo.vende(new InternoVenditaArtefatto(gruppo, armaiolo, pergamena, null)));

        assertEquals(0, gruppo.getMonete());
        assertTrue(nelGruppo(pergamena));
        assertTrue(armaiolo.getInventario().isEmpty());
    }

    @Test
    void ilVenditoreDiPergameneCompraSoloPergamene() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 20);
        Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA, 30);
        gruppo.addArtefatto(spada);
        gruppo.addArtefatto(pergamena);
        ScambiatoreArtefatti venditore = RegistroArtefatti.getScambiatorePerNegozio(CITTA, TipoNegozio.VENDITORE_DI_PERGAMENE);

        assertFalse(gruppo.vende(new InternoVenditaArtefatto(gruppo, venditore, spada, null)));
        assertTrue(gruppo.vende(new InternoVenditaArtefatto(gruppo, venditore, pergamena, null)));

        // Un gruppo senza nessuno che sappia trattare vende a metà del costo (vedi RegoleContrattazione)
        assertEquals(15, gruppo.getMonete());
        assertTrue(nelGruppo(spada));
        assertFalse(nelGruppo(pergamena));
        assertEquals(1, venditore.getInventario().size());
    }

    @Test
    void iMagazziniDellaCittaSiRiempionoOgnunoConLaSuaMerce() {
        RegistroArtefatti.riempiMagazzini(CITTA, GeneratoreArtefatti.istanza());

        Collection<Artefatto> armaiolo = RegistroArtefatti.getScambiatorePerNegozio(CITTA, TipoNegozio.ARMAIOLO).getInventario();
        Collection<Artefatto> venditore = RegistroArtefatti.getScambiatorePerNegozio(CITTA, TipoNegozio.VENDITORE_DI_PERGAMENE).getInventario();
        assertEquals(Costanti.MAGAZZINO_ARTEFATTI_ARMAIOLO, armaiolo.size());
        assertEquals(Costanti.MAGAZZINO_PERGAMENE, venditore.size());
        assertTrue(armaiolo.stream().allMatch(a -> TipoNegozio.ARMAIOLO.tratta(a.getTipo())));
        assertTrue(venditore.stream().allMatch(a -> a.getTipo().isIngrediente()));
        // Livelli a rotazione attorno al livello del mondo, che all'inizio e' 1: mai sotto 1
        Set<Integer> livelli = armaiolo.stream().map(a -> a.getModelloDati().getLivello()).collect(Collectors.toSet());
        Set<Integer> attesi = new HashSet<>();
        for (int livello = 1; livello <= 1 + Costanti.MAGAZZINO_DIVARIO_LIVELLO; livello++) {
            attesi.add(livello);
        }
        assertEquals(attesi, livelli);
        // Un'altra città ha i magazzini vuoti
        assertTrue(RegistroArtefatti.getScambiatorePerNegozio(new CoordinateMD(1, 1), TipoNegozio.ARMAIOLO).getInventario().isEmpty());
    }

    private boolean nelGruppo(Artefatto artefatto) {
        return gruppo.getInventario().stream().anyMatch(a -> a.getModelloDati() == artefatto.getModelloDati());
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int costo) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(1);
        md.setPeso(1);
        md.setCostoAcquisto(costo);
        return Artefatto.di(md);
    }
}
