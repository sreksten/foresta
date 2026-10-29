package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.abilita.DardoArcano;
import com.threeamigos.foresta.abilita.FabbricaAbilita;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.canti.FabbricaCanti;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Chierico;
import com.threeamigos.foresta.personaggi.Druido;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoAzione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le abilità innate e i canti dicono alla UI chi agisce, su chi e con quale azione <b>prima</b> di fare effetto, e solo se agiscono: con
 * il bersaglio se è uno solo (anche un compagno), senza se sono più di uno.
 */
class AnnunciDelleAzioniTest {

    private Gruppo compagni;
    private GruppoAvversario avversari;
    private final List<Object> eventi = new ArrayList<>();

    @BeforeEach
    void preparaGruppiEAscolta() {
        StatoDelMotore.azzera();
        GruppoAvversario.azzeraIstanza();
        avversari = GruppoAvversario.getIstanza();
        compagni = new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return true;
            }
        };
        BusEventi.azzera();
        BusEventi.iscriviti(InternoAzioneInCombattimento.class, eventi::add);
        BusEventi.iscriviti(NotificaTestoFrase.class, eventi::add);
    }

    @AfterEach
    void smettiDiAscoltare() {
        BusEventi.azzera();
    }

    private static <T extends PersonaggioBase> T pronto(T personaggio) {
        personaggio.addMagia(1000);
        personaggio.addSalute(10000);
        return personaggio;
    }

    private static void fissa(PersonaggioBase personaggio, TipoAttributo attributo, int valore) {
        personaggio.addModificatore(new ModificatoreAttributoMD(attributo, TipoModificatore.QUANTITA_ASSOLUTA, valore));
    }

    private InternoAzioneInCombattimento annuncio() {
        assertFalse(eventi.isEmpty(), "nessun evento");
        assertTrue(eventi.get(0) instanceof InternoAzioneInCombattimento, "l'annuncio viene prima del testo: " + eventi.get(0));
        assertEquals(1, eventi.stream().filter(e -> e instanceof InternoAzioneInCombattimento).count(), "un solo annuncio");
        return (InternoAzioneInCombattimento) eventi.get(0);
    }

    @Test
    void ilDardoAnnunciaPrimaEConIlBersaglio() {
        Mago mago = pronto(new Mago("Merlino", 5));
        compagni.aggiungiPersonaggio(mago);
        Goblin primo = new Goblin(5);
        Goblin secondo = new Goblin(5);
        avversari.aggiungiPersonaggio(primo);
        avversari.aggiungiPersonaggio(secondo);

        assertTrue(new DardoArcano(mago).usa(mago, compagni, avversari));

        InternoAzioneInCombattimento annuncio = annuncio();
        assertSame(mago, annuncio.getPersonaggio());
        assertSame(primo, annuncio.getBersaglio());
        assertEquals(TipoAzione.DARDO_ARCANO, annuncio.getAzione());
        assertTrue(eventi.get(1) instanceof NotificaTestoFrase);
        assertTrue(((NotificaTestoFrase) eventi.get(1)).getMessaggio().contains("dardo arcano contro"));
    }

    @Test
    void ilDardoColpisceIlBersaglioScelto() {
        Mago mago = pronto(new Mago("Merlino", 5));
        compagni.aggiungiPersonaggio(mago);
        Goblin primo = new Goblin(5);
        Goblin secondo = new Goblin(5);
        avversari.aggiungiPersonaggio(primo);
        avversari.aggiungiPersonaggio(secondo);

        assertTrue(new DardoArcano(mago).usaSu(mago, secondo, compagni, avversari));
        assertSame(secondo, annuncio().getBersaglio());
    }

    @Test
    void seIlBersagliSceltoEGiaCadutoIlDardoVaSulPrimoInPiedi() {
        Mago mago = pronto(new Mago("Merlino", 5));
        compagni.aggiungiPersonaggio(mago);
        Goblin primo = new Goblin(5);
        Goblin caduto = new Goblin(5);
        avversari.aggiungiPersonaggio(primo);
        avversari.aggiungiPersonaggio(caduto);
        caduto.muore("di prova");

        assertTrue(new DardoArcano(mago).usaSu(mago, caduto, compagni, avversari));
        assertSame(primo, annuncio().getBersaglio());
    }

    @Test
    void ilDardoCostaMenoAlMagoCheAllElfoEFaUnSoloBersaglio() {
        Mago mago = pronto(new Mago("Merlino", 5));
        fissa(mago, TipoAttributo.NUMERO_BERSAGLI, 3);
        compagni.aggiungiPersonaggio(mago);
        for (int i = 0; i < 3; i++) {
            avversari.aggiungiPersonaggio(new Goblin(5));
        }
        int magia = mago.getMagia();
        assertTrue(new DardoArcano(mago).usa(mago, compagni, avversari));
        assertNotNull(annuncio().getBersaglio(), "anche con molti bersagli possibili il dardo ne ha uno");
        assertEquals(magia - TipoAbilita.DARDO_ARCANO.getCostoLancio(), mago.getMagia());
    }

    @Test
    void conPiuBersagliLoSciameAnnunciaSenzaBersaglio() {
        Druido druido = pronto(new Druido("Quercia", 5));
        fissa(druido, TipoAttributo.NUMERO_BERSAGLI, 3);
        compagni.aggiungiPersonaggio(druido);
        avversari.aggiungiPersonaggio(new Goblin(5));
        avversari.aggiungiPersonaggio(new Goblin(5));

        assertTrue(FabbricaAbilita.crea(TipoAbilita.SCIAME, druido).usa(druido, compagni, avversari));

        InternoAzioneInCombattimento annuncio = annuncio();
        assertNull(annuncio.getBersaglio());
        assertEquals(TipoAzione.SCIAME, annuncio.getAzione());
    }

    @Test
    void conUnSoloAvversarioLoSciameLoAnnuncia() {
        Druido druido = pronto(new Druido("Quercia", 5));
        compagni.aggiungiPersonaggio(druido);
        Goblin goblin = new Goblin(5);
        avversari.aggiungiPersonaggio(goblin);

        assertTrue(FabbricaAbilita.crea(TipoAbilita.RADICI, druido).usa(druido, compagni, avversari));

        assertSame(goblin, annuncio().getBersaglio());
        assertEquals(TipoAzione.RADICI, annuncio().getAzione());
    }

    @Test
    void senzaAvversariUnAbilitaDiDannoNonAnnunciaNiente() {
        Druido druido = pronto(new Druido("Quercia", 5));
        compagni.aggiungiPersonaggio(druido);
        assertFalse(FabbricaAbilita.crea(TipoAbilita.SCIAME, druido).usa(druido, compagni, avversari));
        assertTrue(eventi.isEmpty());
    }

    @Test
    void limposizioneDelleManiAnnunciaIlCompagnoCheCura() {
        Chierico chierico = pronto(new Chierico("Ambrogio", 5));
        Guerriero ferito = pronto(new Guerriero("Wendy", 5));
        ferito.subSalute(ferito.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        compagni.aggiungiPersonaggio(chierico);
        compagni.aggiungiPersonaggio(ferito);
        avversari.aggiungiPersonaggio(new Goblin(5));

        assertTrue(FabbricaAbilita.crea(TipoAbilita.IMPOSIZIONE_DELLE_MANI, chierico).usa(chierico, compagni, avversari));

        InternoAzioneInCombattimento annuncio = annuncio();
        assertSame(ferito, annuncio.getBersaglio());
        assertEquals(TipoAzione.IMPOSIZIONE_DELLE_MANI, annuncio.getAzione());
    }

    @Test
    void senzaFeritiLimposizioneDelleManiNonAnnunciaNiente() {
        Chierico chierico = pronto(new Chierico("Ambrogio", 5));
        compagni.aggiungiPersonaggio(chierico);
        avversari.aggiungiPersonaggio(new Goblin(5));

        assertFalse(FabbricaAbilita.crea(TipoAbilita.IMPOSIZIONE_DELLE_MANI, chierico).usa(chierico, compagni, avversari));
        assertFalse(eventi.stream().anyMatch(e -> e instanceof InternoAzioneInCombattimento));
    }

    @Test
    void laPelleDiCortecciaAnnunciaSenzaBersaglio() {
        Druido druido = pronto(new Druido("Quercia", 5));
        compagni.aggiungiPersonaggio(druido);
        avversari.aggiungiPersonaggio(new Goblin(5));

        assertTrue(FabbricaAbilita.crea(TipoAbilita.PELLE_DI_CORTECCIA, druido).usa(druido, compagni, avversari));

        assertNull(annuncio().getBersaglio());
        assertEquals(TipoAzione.PELLE_DI_CORTECCIA, annuncio().getAzione());
    }

    @Test
    void ilCantoCurativoAnnunciaIlCompagnoCheCura() {
        Bardo bardo = pronto(new Bardo("Orfeo", 5));
        Guerriero ferito = pronto(new Guerriero("Wendy", 5));
        ferito.subSalute(ferito.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        compagni.aggiungiPersonaggio(bardo);
        compagni.aggiungiPersonaggio(ferito);
        avversari.aggiungiPersonaggio(new Goblin(5));

        assertTrue(FabbricaCanti.crea(TipoCanto.CURATIVO, bardo).canta(bardo, compagni, avversari));

        assertSame(ferito, annuncio().getBersaglio());
        assertEquals(TipoAzione.CANTO_CURATIVO, annuncio().getAzione());
    }

    @Test
    void ilCantoIpnoticoAGruppoAnnunciaSenzaBersaglio() {
        Bardo bardo = pronto(new Bardo("Orfeo", 5));
        fissa(bardo, TipoAttributo.NUMERO_BERSAGLI, 3);
        compagni.aggiungiPersonaggio(bardo);
        avversari.aggiungiPersonaggio(new Goblin(5));
        avversari.aggiungiPersonaggio(new Goblin(5));

        assertTrue(FabbricaCanti.crea(TipoCanto.IPNOTICO, bardo).canta(bardo, compagni, avversari));

        assertNull(annuncio().getBersaglio());
        assertEquals(TipoAzione.CANTO_IPNOTICO, annuncio().getAzione());
    }

    @Test
    void ilCantoDiValoreELurloDellaBansheeAnnunciano() {
        Bardo bardo = pronto(new Bardo("Orfeo", 5));
        compagni.aggiungiPersonaggio(bardo);
        Goblin goblin = new Goblin(5);
        avversari.aggiungiPersonaggio(goblin);

        assertTrue(FabbricaCanti.crea(TipoCanto.DI_VALORE, bardo).canta(bardo, compagni, avversari));
        assertNull(annuncio().getBersaglio());
        assertEquals(TipoAzione.CANTO_DI_VALORE, annuncio().getAzione());

        eventi.clear();
        assertTrue(FabbricaCanti.crea(TipoCanto.URLO_DELLA_BANSHEE, bardo).canta(bardo, compagni, avversari));
        assertSame(goblin, annuncio().getBersaglio());
        assertEquals(TipoAzione.URLO_DELLA_BANSHEE, annuncio().getAzione());
    }
}
