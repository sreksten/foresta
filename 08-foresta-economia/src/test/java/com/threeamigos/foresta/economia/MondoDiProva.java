package com.threeamigos.foresta.economia;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CassaMD;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.modellodati.MagazziniMD;
import com.threeamigos.foresta.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.modellodati.StatisticheMD;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.Random;

/**
 * Il mondo delle prove di questo modulo: i dati di ogni parte, nuovi a ogni prova, e una Foresta di boschi in cui il gruppo sta al centro.
 */
public final class MondoDiProva {

    public static final CoordinateMD QUI = new CoordinateMD(5, 4);

    public static CassaMD cassa;
    public static MagazziniMD magazzini;
    public static StatisticheMD statistiche;

    private MondoDiProva() {
    }

    public static void nuovo(int larghezza, int altezza) {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        Dado.impostaSeme(11);
        GruppoGiocatoreMD datiGruppo = new GruppoGiocatoreMD();
        GruppoGiocatore.setFornitoreDiDati(() -> datiGruppo);
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        cassa = new CassaMD();
        Cassa.setFornitoreDiDati(() -> cassa);
        magazzini = new MagazziniMD();
        MagazziniDeiNegozi.setFornitoreDiDati(() -> magazzini);
        statistiche = new StatisticheMD();
        Statistiche.setFornitoreDiDati(() -> statistiche);
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        RegistroArtefatti.setFornitoreDiDati(() -> registro);
        ForestaMD foresta = new ForestaMD();
        Foresta.setFornitoreDiDati(() -> foresta);
        Foresta.azzera(larghezza, altezza);
        for (int x = 0; x < larghezza; x++) {
            for (int y = 0; y < altezza; y++) {
                Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.BOSCO);
            }
        }
        Foresta.setPosizioneDelGruppo(QUI);
    }

    public static void finito() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(null);
        Dado.ripristina();
    }

    public static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        return new GeneratoreArtefattiTabelle(new Random(5)).generaArtefatto(tipo, livello);
    }
}
