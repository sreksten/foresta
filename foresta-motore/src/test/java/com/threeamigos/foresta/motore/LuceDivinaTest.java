package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.abilita.AbilitaInnate;
import com.threeamigos.foresta.abilita.FabbricaAbilita;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Gargoyle;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdote;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La Luce Divina: l'abilità sacra innata di Sacerdote e Sacerdotessa, che brucia i non morti e non risparmia lo Scheletro che combatte con il
 * gruppo.
 */
class LuceDivinaTest {

    private static final String FRASE_SULLO_SCHELETRO = "La luce divina investe anche lo Scheletro.";

    @Test
    void ECaratterizzataComeIlFulmineMaSacraEAPiuBersagli() {
        assertEquals(Comando.LUCE_DIVINA, ComandiAssociati.di(TipoAbilita.LUCE_DIVINA));
        assertSame(TipoAbilita.LUCE_DIVINA, ComandiAssociati.abilita(Comando.LUCE_DIVINA));
        assertEquals(CostantiIncantesimi.INCANTESIMO_FULMINE_DANNI, CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_DANNI);
        assertEquals(CostantiIncantesimi.INCANTESIMO_FULMINE_COSTO_LANCIO, TipoAbilita.LUCE_DIVINA.getCostoLancio());
        Personaggio sacerdote = FabbricaPersonaggi.crea(TipoPersonaggio.SACERDOTE, 5);
        assertEquals(TipoDanno.SACRO, ((com.threeamigos.foresta.abilita.AbilitaDiDanno) FabbricaAbilita.crea(TipoAbilita.LUCE_DIVINA, sacerdote)).getTipoDanno());
    }

    @Test
    void ELInnataSoloDeiSacerdotiConLaMagiaSufficiente() {
        Personaggio sacerdote = FabbricaPersonaggi.crea(TipoPersonaggio.SACERDOTE, 5);
        Personaggio sacerdotessa = FabbricaPersonaggi.crea(TipoPersonaggio.SACERDOTESSA, 5);
        Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 5);
        for (Personaggio p : new Personaggio[]{sacerdote, sacerdotessa, guerriero}) {
            p.getModelloDati().set(TipoAttributo.MAGIA, CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_COSTO_LANCIO);
        }
        assertTrue(AbilitaInnate.puoUsare(sacerdote, TipoAbilita.LUCE_DIVINA));
        assertTrue(AbilitaInnate.puoUsare(sacerdotessa, TipoAbilita.LUCE_DIVINA));
        assertFalse(AbilitaInnate.puoUsare(guerriero, TipoAbilita.LUCE_DIVINA), "gli altri non la conoscono");

        sacerdote.getModelloDati().set(TipoAttributo.MAGIA, CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_COSTO_LANCIO - 1);
        assertFalse(AbilitaInnate.puoUsare(sacerdote, TipoAbilita.LUCE_DIVINA));
        sacerdote.getModelloDati().set(TipoAttributo.MAGIA, CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_COSTO_LANCIO);
        sacerdote.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertFalse(AbilitaInnate.puoUsare(sacerdote, TipoAbilita.LUCE_DIVINA), "il silenzio impedisce di invocarla");
    }

    private static Personaggio scheletroAlleato(PartitaDiTest partita) {
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        scheletro.setMotivoNelGruppo(com.threeamigos.foresta.tipi.TipoMotivoNelGruppo.MIETITURA);
        partita.gruppo().aggiungiPersonaggio(scheletro);
        return scheletro;
    }

    private static List<String> frasi(PartitaDiTest partita) {
        return partita.eventi().tutti(NotificaTestoFrase.class).stream().map(NotificaTestoFrase::getMessaggio)
                .collect(Collectors.toList());
    }

    @Test
    void ColpisceAncheLoScheletroAlleatoDiChiLaInvoca() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(301)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio sacerdote = new Sacerdote("Fra Bartolomeo", 5);
            partita.gruppo().aggiungiPersonaggio(sacerdote);
            scheletroAlleato(partita);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            avversari.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 5));
            partita.eventi().ascolta(NotificaTestoFrase.class);
            int magiaPrima = sacerdote.getMagia();

            assertTrue(FabbricaAbilita.crea(TipoAbilita.LUCE_DIVINA, sacerdote).usa(sacerdote, partita.gruppo(), avversari));

            assertTrue(frasi(partita).contains(FRASE_SULLO_SCHELETRO), frasi(partita).toString());
            assertEquals(magiaPrima - CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_COSTO_LANCIO, sacerdote.getMagia());
        }
    }

    @Test
    void ChiNonEDelGruppoDelGiocatoreNonColpisceLoScheletroAlleato() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(302)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            scheletroAlleato(partita);
            Personaggio sacerdoteAvversario = new Sacerdote("Fra Cattivo", 5);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            avversari.aggiungiPersonaggio(sacerdoteAvversario);
            partita.eventi().ascolta(NotificaTestoFrase.class);

            FabbricaAbilita.crea(TipoAbilita.LUCE_DIVINA, sacerdoteAvversario).usa(sacerdoteAvversario, avversari, partita.gruppo());

            assertFalse(frasi(partita).contains(FRASE_SULLO_SCHELETRO), frasi(partita).toString());
        }
    }

    @Test
    void ColpisceDaPrimoIlBersaglioScelto() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(303)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio sacerdote = new Sacerdote("Fra Bartolomeo", 5);
            sacerdote.addModificatore(new ModificatoreAttributoMD(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.QUANTITA_ASSOLUTA, 1));
            partita.gruppo().aggiungiPersonaggio(sacerdote);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            Personaggio goblin = new Goblin(5);
            Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 5);
            avversari.aggiungiPersonaggio(goblin);
            avversari.aggiungiPersonaggio(scheletro);
            int saluteGoblin = goblin.getSalute();

            assertTrue(FabbricaAbilita.crea(TipoAbilita.LUCE_DIVINA, sacerdote).usaSu(sacerdote, scheletro, partita.gruppo(), avversari));

            assertEquals(saluteGoblin, goblin.getSalute(), "con un solo bersaglio colpisce quello scelto, non il primo");
        }
    }

    @Test
    void IlGargoyleDiPietraNonSenteNessunDannoNonFisico() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(304)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio sacerdote = new Sacerdote("Fra Bartolomeo", 5);
            Personaggio mago = new Mago("Merlino", 5);
            partita.gruppo().aggiungiPersonaggio(sacerdote);
            partita.gruppo().aggiungiPersonaggio(mago);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            Personaggio gargoyle = new Gargoyle(5);
            avversari.aggiungiPersonaggio(gargoyle);
            int salute = gargoyle.getSalute();

            for (int i = 0; i < 20; i++) {
                // La magia massima è poca: si riempie a ogni giro
                sacerdote.addMagia(1000);
                mago.addMagia(1000);
                FabbricaAbilita.crea(TipoAbilita.LUCE_DIVINA, sacerdote).usa(sacerdote, partita.gruppo(), avversari);
                FabbricaAbilita.crea(TipoAbilita.DARDO_ARCANO, mago).usa(mago, partita.gruppo(), avversari);
            }
            assertEquals(salute, gargoyle.getSalute());
        }
    }

    @Test
    void IMostriNonLaConoscono() {
        for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.LICH, TipoPersonaggio.STREGA, TipoPersonaggio.DRAGO}) {
            assertFalse(AbilitaInnate.conosciutaDa(classe, TipoAbilita.LUCE_DIVINA), classe.name());
        }
    }

    @Test
    void IlSacroFaIlDoppioAiNonMortiDelGruppoDiChiLoSubisce() {
        // La regola dei non morti (deboli al sacro) è quella di sempre: qui si controlla che valga per la Luce Divina
        assertEquals(2.0d, TipoPersonaggio.SCHELETRO.getMoltiplicatoreDannoSubito(TipoDanno.SACRO));
        assertEquals(2.0d, TipoPersonaggio.LICH.getMoltiplicatoreDannoSubito(TipoDanno.SACRO));
        assertEquals(1.0d, TipoPersonaggio.GOBLIN.getMoltiplicatoreDannoSubito(TipoDanno.SACRO));
    }
}
