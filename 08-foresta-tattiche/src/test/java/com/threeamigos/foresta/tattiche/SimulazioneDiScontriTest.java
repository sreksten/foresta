package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.CantiImparatiMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Chierico;
import com.threeamigos.foresta.personaggi.Druido;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdote;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scontri interi, con il dado dal seme fisso: ogni classe di mostro contro il gruppo del giocatore, tutti con la tattica base, finché una delle due
 * parti cade o passano i round. Passa da quasi tutte le strade dell'esecutore, del valutatore e delle tattiche base senza che nessuna si rompa.
 */
class SimulazioneDiScontriTest {

    private static final DestinazioneDiLog POZZO = new DestinazioneDiLog() {
        @Override
        public void scrivi(String messaggio) {
        }

        @Override
        public void scrivi(Exception e) {
        }
    };

    private static Gruppo gruppo(boolean delGiocatore, List<Personaggio> membri) {
        return new Gruppo() {
            {
                personaggi.addAll(membri);
                capo = membri.get(0);
            }

            @Override
            public boolean isGruppoGiocatore() {
                return delGiocatore;
            }
        };
    }

    @AfterEach
    void ripulisce() {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
        BusEventi.azzera();
        BusEventi.impostaConsegna(null);
        Dado.ripristina();
    }

    private static List<Personaggio> iMiei() {
        List<Personaggio> miei = new ArrayList<>(Arrays.asList(new Guerriero("Arsenio", 8), new Mago("Merlino", 8), new Chierico("Pio", 8),
                new Druido("Quercia", 8), new Sacerdote("Don", 8), new Bardo("Orfeo", 8), new Ladro("Zorro", 8), new Elfo("Legolas", 8)));
        for (Personaggio p : miei) {
            p.getModelloDati().setPng(false);
            p.addSalute(p.getSaluteMassima());
            p.addMagia(p.getMagiaMassima());
        }
        return miei;
    }

    @RepeatedTest(4)
    void ognClasseDiMostroControIlGruppo(RepetitionInfo info) {
        int giro = info.getCurrentRepetition();
        Logger.impostaDestinazione(giro % 2 == 0 ? POZZO : DestinazioneDiLog.NESSUNA);
        for (TipoPersonaggio classe : TatticheBase.classiDeiMostri()) {
            DatiDiProvaDelleTattiche.nuovaPartita();
            BusEventi.azzera();
            BusEventi.impostaConsegna(Runnable::run);
            CantiImparatiMD canti = new CantiImparatiMD();
            com.threeamigos.foresta.canti.CantiImparati.setFornitoreDiDati(() -> canti);
            for (TipoCanto canto : TipoCanto.values()) {
                com.threeamigos.foresta.canti.CantiImparati.impara(canto);
            }
            StatoDelCombattimento.azzeraIstanza();
            StatoDelCombattimento.getIstanza().reimposta();
            Dado.impostaSeme(1000L * giro + classe.ordinal());
            GruppoGiocatore giocatore = GruppoGiocatore.getIstanza();
            List<Personaggio> membri = iMiei();
            membri.forEach(giocatore::aggiungiPersonaggio);
            for (TipoIncantesimo incantesimo : TipoIncantesimo.values()) {
                giocatore.addIncantesimi(incantesimo, 3);
            }
            giocatore.addPozioniSalute(2);
            giocatore.addPozioniSaluteGrande(2);
            giocatore.addPozioniMagia(2);
            giocatore.addPozioniMagiaGrande(2);
            List<Personaggio> nemici = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Personaggio mostro = FabbricaPersonaggi.crea(classe, 4 + 4 * i);
                mostro.addMagia(mostro.getMagiaMassima());
                nemici.add(mostro);
            }
            Gruppo mostri = gruppo(false, nemici);
            EsecutoreTattiche esecutore = new EsecutoreTattiche(giocatore, mostri, StatoDelCombattimento.getIstanza(), (uccisore, vittima) -> { });
            for (int round = 0; round < 12 && !giocatore.getPersonaggiVivi().isEmpty() && !mostri.getPersonaggiVivi().isEmpty(); round++) {
                for (Personaggio p : new ArrayList<>(giocatore.getPersonaggiVivi())) {
                    TatticaMD tattica = TatticheBase.deiGiocabili(p.getClasse());
                    assertNotNull(tattica, p.getClasse().name());
                    esecutore.agisci(p, tattica);
                }
                for (Personaggio m : new ArrayList<>(mostri.getPersonaggiVivi())) {
                    esecutore.agisci(m, TatticheBase.deiMostri(classe));
                }
            }
        }
    }
}
