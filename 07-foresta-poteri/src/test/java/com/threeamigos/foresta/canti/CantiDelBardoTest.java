package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.CantiImparatiMD;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CostantiCanti;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I canti del Bardo e della Cantastorie: chi li conosce (l'Urlo della Banshee solo dopo averlo imparato), quando li si può intonare, e che
 * cosa fanno.
 */
class CantiDelBardoTest {

    private final List<String> frasi = new ArrayList<>();
    private Runnable iscrizione;
    private GruppoGiocatore gruppo;
    private GruppoAvversario avversari;
    private CantiImparatiMD imparati;

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        GruppoGiocatoreMD dati = new GruppoGiocatoreMD();
        GruppoGiocatore.setFornitoreDiDati(() -> dati);
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        StatoDelCombattimento.azzeraIstanza();
        imparati = new CantiImparatiMD();
        CantiImparati.setFornitoreDiDati(() -> imparati);
        Dado.impostaSeme(77L);
        iscrizione = BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio()));
        gruppo = GruppoGiocatore.getIstanza();
        avversari = GruppoAvversario.getIstanza();
    }

    @AfterEach
    void ripulisce() {
        iscrizione.run();
        BusEventi.impostaConsegna(null);
        StatoDelCombattimento.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        Dado.ripristina();
    }

    private Personaggio bardo() {
        Personaggio p = new Bardo("Samuel", 8);
        p.getModelloDati().setPng(false);
        p.addMagia(100000);
        p.addSalute(100000);
        gruppo.aggiungiPersonaggio(p);
        return p;
    }

    private Personaggio avversario(TipoPersonaggio tipo) {
        Personaggio p = FabbricaPersonaggi.crea(tipo, 5);
        avversari.aggiungiPersonaggio(p);
        return p;
    }

    private static void ferisci(Personaggio p) {
        p.subSalute(p.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }

    @Test
    void iDatiDeiCantiImparatiDevonoEsserci() {
        assertFalse(CantiImparati.haImparato(TipoCanto.URLO_DELLA_BANSHEE));
        CantiImparati.impara(TipoCanto.URLO_DELLA_BANSHEE);
        assertTrue(CantiImparati.haImparato(TipoCanto.URLO_DELLA_BANSHEE));
        assertTrue(CantoDelBardo.isUrloDellaBansheeAppreso());
        CantiImparati.setFornitoreDiDati(null);
        assertThrows(IllegalStateException.class, () -> CantiImparati.haImparato(TipoCanto.CURATIVO));
    }

    @Test
    void chiConosceQualeCanto() {
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            boolean bardo = classe == TipoPersonaggio.BARDO || classe == TipoPersonaggio.CANTASTORIE || classe == TipoPersonaggio.BARDO_DELLE_LOCANDE;
            assertEquals(bardo, CantoDelBardo.conosciutoDa(classe), classe.name());
            for (TipoCanto tipo : TipoCanto.values()) {
                if (!bardo) {
                    assertFalse(CantoDelBardo.conosciutoDa(classe, tipo));
                }
            }
        }
        assertTrue(CantoDelBardo.conosciutoDa(TipoPersonaggio.BARDO, TipoCanto.CURATIVO));
        assertFalse(CantoDelBardo.conosciutoDa(TipoPersonaggio.BARDO, TipoCanto.URLO_DELLA_BANSHEE));
        assertTrue(CantoDelBardo.conosciutoDa(TipoPersonaggio.BARDO_DELLE_LOCANDE, TipoCanto.URLO_DELLA_BANSHEE));
        CantiImparati.impara(TipoCanto.URLO_DELLA_BANSHEE);
        assertTrue(CantoDelBardo.conosciutoDa(TipoPersonaggio.CANTASTORIE, TipoCanto.URLO_DELLA_BANSHEE));
    }

    @Test
    void sipuoCantareSoloSeVivoNonSilenziatoConMagiaEFuoriRicarica() {
        Personaggio bardo = bardo();
        assertTrue(CantoDelBardo.puoCantare(bardo, TipoCanto.CURATIVO));
        assertFalse(CantoDelBardo.puoCantare(bardo, TipoCanto.URLO_DELLA_BANSHEE));
        bardo.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 2, 0);
        assertFalse(CantoDelBardo.puoCantare(bardo, TipoCanto.CURATIVO));
        bardo.rimuoviEffettoDiStato(TipoEffettoDiStato.SILENZIATO);
        CantoDelBardo.avviaRicarica(bardo, TipoCanto.CURATIVO);
        assertTrue(CantoDelBardo.isInRicarica(bardo, TipoCanto.CURATIVO));
        assertFalse(CantoDelBardo.puoCantare(bardo, TipoCanto.CURATIVO));
        bardo.setRicarica(TipoCanto.CURATIVO.name(), 0);
        bardo.subMagia(bardo.getMagia());
        assertFalse(CantoDelBardo.puoCantare(bardo, TipoCanto.CURATIVO));
        bardo.addMagia(100000);
        bardo.muore("prova");
        assertFalse(CantoDelBardo.puoCantare(bardo, TipoCanto.CURATIVO));
        assertTrue(CantoDelBardo.cantiInRicarica(bardo, gruppo, avversari).isEmpty());
    }

    @Test
    void iCantiUtiliEQuelliInRicaricaCheServirebbero() {
        Personaggio bardo = bardo();
        Personaggio ferito = new Guerriero("Ferito", 5);
        ferito.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(ferito);
        ferito.addSalute(100000);
        // Senza avversari non servono canti
        assertTrue(CantoDelBardo.cantiUtili(bardo, gruppo, avversari).isEmpty());
        assertTrue(CantoDelBardo.cantiInRicarica(bardo, gruppo, avversari).isEmpty());
        avversario(TipoPersonaggio.GOBLIN);
        List<TipoCanto> utili = CantoDelBardo.cantiUtili(bardo, gruppo, avversari);
        assertFalse(utili.contains(TipoCanto.CURATIVO), "nessun ferito");
        assertTrue(utili.contains(TipoCanto.IPNOTICO));
        ferisci(ferito);
        assertTrue(CantoDelBardo.cantiUtili(bardo, gruppo, avversari).contains(TipoCanto.CURATIVO));
        assertEquals(CantoDelBardo.cantiPossibili(bardo).size(), CantoDelBardo.cantiUtili(bardo, gruppo, avversari).size());
        // In ricarica: il curativo si vede solo se serve
        CantoDelBardo.avviaRicarica(bardo, TipoCanto.CURATIVO);
        CantoDelBardo.avviaRicarica(bardo, TipoCanto.IPNOTICO);
        assertEquals(2, CantoDelBardo.cantiInRicarica(bardo, gruppo, avversari).size());
        ferito.addSalute(100000);
        assertEquals(1, CantoDelBardo.cantiInRicarica(bardo, gruppo, avversari).size());
    }

    @Test
    void ilCantoCurativoCuraSoloChiNeHaBisogno() {
        Personaggio bardo = bardo();
        Personaggio ferito = new Guerriero("Ferito", 5);
        ferito.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(ferito);
        ferito.addSalute(100000);
        CantoCurativo canto = (CantoCurativo) FabbricaCanti.crea(TipoCanto.CURATIVO, bardo);
        assertEquals(TipoCanto.CURATIVO, canto.getTipo());
        assertEquals(TipoCanto.CURATIVO.getCostoLancio(), canto.getCostoLancio());
        assertTrue(canto.getCura() > 0);
        assertFalse(canto.canta(bardo, gruppo, avversari));
        assertTrue(frasi.get(frasi.size() - 1).contains("nessuno ha bisogno di cure"), frasi.toString());
        assertFalse(canto.cantaSu(bardo, ferito, gruppo, avversari));
        ferisci(ferito);
        int prima = ferito.getSalute();
        assertTrue(canto.canta(bardo, gruppo, avversari));
        assertTrue(ferito.getSalute() > prima);
        ferisci(ferito);
        assertTrue(canto.cantaSu(bardo, ferito, gruppo, avversari));
        ferito.muore("prova");
        assertFalse(canto.cantaSu(bardo, ferito, gruppo, avversari));
        assertFalse(canto.cantaSu(bardo, null, gruppo, avversari));
        // Un non morto del gruppo non si cura col canto
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 3);
        gruppo.aggiungiPersonaggio(scheletro);
        scheletro.subSalute(scheletro.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(canto.cantaSu(bardo, scheletro, gruppo, avversari));
    }

    @Test
    void ilCantoIpnoticoELUrloDelleBansheeFannoCedereOResistere() {
        Personaggio bardo = bardo();
        CantiImparati.impara(TipoCanto.URLO_DELLA_BANSHEE);
        Canto ipnotico = FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo);
        Canto urlo = FabbricaCanti.crea(TipoCanto.URLO_DELLA_BANSHEE, bardo);
        assertEquals(TipoCanto.IPNOTICO, ipnotico.getTipo());
        assertEquals(TipoCanto.URLO_DELLA_BANSHEE, urlo.getTipo());
        assertFalse(ipnotico.canta(bardo, gruppo, avversari));
        assertFalse(urlo.canta(bardo, gruppo, avversari));
        for (int i = 0; i < 6; i++) {
            avversario(TipoPersonaggio.GOBLIN);
        }
        int cedono = 0;
        for (int giro = 0; giro < 40; giro++) {
            bardo.addMagia(100000);
            assertTrue(ipnotico.canta(bardo, gruppo, avversari));
            assertTrue(urlo.canta(bardo, gruppo, avversari));
            assertTrue(ipnotico.cantaSu(bardo, null, gruppo, avversari));
        }
        for (Personaggio p : avversari.getPersonaggi()) {
            if (p.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO) || p.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO)) {
                cedono++;
            }
        }
        assertTrue(cedono > 0);
        assertTrue(frasi.stream().anyMatch(f -> f.endsWith("resiste al canto.")), "qualcuno resiste");
        assertTrue(frasi.stream().anyMatch(f -> f.endsWith("resiste all'urlo.")), "qualcuno resiste");
        // Un solo ascoltatore
        GruppoAvversario.azzeraIstanza();
        avversari = GruppoAvversario.getIstanza();
        avversario(TipoPersonaggio.GOBLIN);
        bardo.addMagia(100000);
        assertTrue(ipnotico.canta(bardo, gruppo, avversari));
        bardo.addMagia(100000);
        assertTrue(urlo.canta(bardo, gruppo, avversari));
        // Soggezione o coraggio a zero
        Personaggio muto = new Guerriero("Muto", 1);
        muto.addModificatore(new ModificatoreAttributoMD(TipoAttributo.SOGGEZIONE, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        Personaggio pavido = new Guerriero("Pavido", 1);
        pavido.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        assertEquals(0.0d, CantoIpnotico.probabilitaDiCedere(muto, pavido), 1e-9);
        assertEquals(0.0d, UrloDellaBanshee.probabilitaDiCedere(muto, pavido), 1e-9);
        assertTrue(CantoIpnotico.probabilitaDiCedere(bardo, pavido) > 99.0d);
        assertTrue(UrloDellaBanshee.probabilitaDiCedere(bardo, bardo) > CantoIpnotico.probabilitaDiCedere(bardo, bardo));
    }

    @Test
    void ilCantoDiValoreIspiraIlGruppoConUnIntensitaCheCresceEHaUnTetto() {
        Personaggio bardo = bardo();
        Personaggio altro = new Guerriero("Altro", 5);
        altro.getModelloDati().setPng(false);
        gruppo.aggiungiPersonaggio(altro);
        CantoDiValore canto = (CantoDiValore) FabbricaCanti.crea(TipoCanto.DI_VALORE, bardo);
        assertEquals(TipoCanto.DI_VALORE, canto.getTipo());
        assertTrue(canto.getIntensita() <= CostantiCanti.CANTO_DI_VALORE_PERCENTUALE_MASSIMA);
        Personaggio basso = new Bardo("Basso", 1);
        Personaggio altissimo = new Bardo("Altissimo", 30);
        double intensitaBassa = new CantoDiValore(basso).getIntensita();
        assertTrue(intensitaBassa < new CantoDiValore(altissimo).getIntensita() + 1e-9);
        assertEquals(CostantiCanti.CANTO_DI_VALORE_PERCENTUALE_MASSIMA, new CantoDiValore(altissimo).getIntensita(), 1e-9);
        assertTrue(canto.canta(bardo, gruppo, avversari));
        assertTrue(altro.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO));
        assertTrue(bardo.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO));
    }

    @Test
    void ilCantoCurativoSiRiconosceDalNomeDelTipo() {
        for (TipoCanto tipo : TipoCanto.values()) {
            assertNotNull(tipo.getTipoAzione());
            assertTrue(tipo.getCostoLancio() > 0);
            assertTrue(tipo.getRicarica() > 0);
            assertNotNull(tipo.getNome());
            assertNotNull(tipo.getDescrizione());
        }
    }
}
