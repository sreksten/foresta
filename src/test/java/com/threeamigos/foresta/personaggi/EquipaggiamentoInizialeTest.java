package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.RegistroPersonaggi;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La dotazione di base delle classi giocanti e i compagni reclutati, allineati al livello del mondo.
 */
class EquipaggiamentoInizialeTest {

    @BeforeEach
    void mondoNuovo() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    @Test
    void ogniClasseGiocanteRiceveLaSuaDotazione() {
        for (ClassePersonaggio classe : new ClassePersonaggio[]{ClassePersonaggio.GUERRIERO, ClassePersonaggio.LADRA,
                ClassePersonaggio.ELFO, ClassePersonaggio.CANTASTORIE, ClassePersonaggio.MAGA}) {
            Personaggio personaggio = classe.getIstanza(1);
            EquipaggiamentoIniziale.equipaggia(personaggio);
            Collection<TipoArtefatto> tipi = personaggio.getModelloDati().getArtefatti().stream().map(ArtefattoMD::getTipo)
                    .collect(Collectors.toList());
            assertEquals(EquipaggiamentoIniziale.perClasse(classe).size(), tipi.size(), classe + ": " + tipi);
            assertTrue(tipi.containsAll(EquipaggiamentoIniziale.perClasse(classe)), classe + ": " + tipi);
        }
    }

    @Test
    void ilCompagnoArrivaVicinoAlLivelloDelMondoConPezziChePuoUsare() {
        ModelloDati.getIstanza().getStatisticheMD().setLivello(5);
        for (int i = 0; i < 50; i++) {
            Personaggio compagno = RegistroPersonaggi.preparaCompagno(new Guerriero("Reginald", 1));
            assertTrue(compagno.getLivello() >= 3 && compagno.getLivello() <= 5, "livello " + compagno.getLivello());
            assertEquals("Reginald", compagno.getNomeProprio().orElse(null));
            assertEquals(2, compagno.getModelloDati().getArtefatti().size());
            for (ArtefattoMD artefatto : compagno.getModelloDati().getArtefatti()) {
                assertTrue(artefatto.getLivello() >= 3 && artefatto.getLivello() <= compagno.getLivello(),
                        "artefatto di livello " + artefatto.getLivello() + " a un compagno di livello " + compagno.getLivello());
            }
        }
    }
}
