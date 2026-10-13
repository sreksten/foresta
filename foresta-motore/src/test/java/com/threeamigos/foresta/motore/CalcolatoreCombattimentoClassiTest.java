package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoModificatore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * I moltiplicatori di danno delle classi (Costanti.*_MOLTIPLICATORE_DANNI_FISICI e _MAGICI): a parità di statistiche
 * il guerriero fa più danno fisico, il mago più danno magico. Il Ladro fa da riferimento: i confronti sono in
 * rapporto ai suoi moltiplicatori.
 * Il critico è neutralizzato, così i danni sono deterministici.
 */
class CalcolatoreCombattimentoClassiTest {

    private Guerriero difensore;

    @BeforeEach
    void preparaDifensore() {
        ModelloDati.setIstanza(new ModelloDati());
        difensore = new Guerriero("Bersaglio", 5);
        difensore.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
    }

    @Test
    void ilGuerrieroFaPiuDannoFisico() {
        Arma spada = arma(TipoDanno.TAGLIENTE, Collections.emptyList());
        double ladro = danno(new Ladro("Riferimento", 5), spada);
        double guerriero = danno(new Guerriero("Pippo", 5), spada);
        assertEquals(ladro * Costanti.GUERRIERO_MOLTIPLICATORE_DANNI_FISICI / Costanti.LADRO_MOLTIPLICATORE_DANNI_FISICI, guerriero, 1.0);
    }

    @Test
    void ilMagoFaMenoDannoFisico() {
        Arma bastone = arma(TipoDanno.CONTUNDENTE, Collections.emptyList());
        double ladro = danno(new Ladro("Riferimento", 5), bastone);
        double mago = danno(new Mago("Merlino", 5), bastone);
        assertEquals(ladro * Costanti.MAGO_MOLTIPLICATORE_DANNI_FISICI / Costanti.LADRO_MOLTIPLICATORE_DANNI_FISICI, mago, 1.0);
    }

    @Test
    void ilMagoFaPiuDannoConGliIncantesimi() {
        IncantesimoMalefico aria = (IncantesimoMalefico) FabbricaIncantesimi.crea(ClasseIncantesimo.ARIA, 5);
        double ladro = danno(new Ladro("Riferimento", 5), aria);
        double mago = danno(new Mago("Merlino", 5), aria);
        double guerriero = danno(new Guerriero("Pippo", 5), aria);
        assertEquals(ladro * Costanti.MAGO_MOLTIPLICATORE_DANNI_MAGICI, mago, 1.0);
        assertEquals(ladro * Costanti.GUERRIERO_MOLTIPLICATORE_DANNI_MAGICI, guerriero, 1.0);
    }

    @Test
    void laParteFissaDiUnIncantamentoEUgualePerTutti() {
        IncantamentoMD soloFisso = new IncantamentoMD("Fuoco", TipoDanno.FUOCO, 10, 0.0);
        assertEquals(dannoIncantamento(new Ladro("Riferimento", 5), soloFisso),
                dannoIncantamento(new Mago("Merlino", 5), soloFisso), 1.0);
    }

    @Test
    void laPartePercentualeDiUnIncantamentoScalaColDannoMagico() {
        IncantamentoMD soloPercentuale = new IncantamentoMD("Fuoco", TipoDanno.FUOCO, 0, 1.0);
        double ladro = dannoIncantamento(new Ladro("Riferimento", 5), soloPercentuale);
        double mago = dannoIncantamento(new Mago("Merlino", 5), soloPercentuale);
        assertEquals(ladro * Costanti.MAGO_MOLTIPLICATORE_DANNI_MAGICI, mago, 1.0);
    }

    /**
     * Quanto aggiunge l'incantamento: il danno della stessa spada con e senza
     */
    private double dannoIncantamento(PersonaggioBase attaccante, IncantamentoMD incantamento) {
        double senza = danno(attaccante, arma(TipoDanno.TAGLIENTE, Collections.emptyList()));
        double con = danno(attaccante, arma(TipoDanno.TAGLIENTE, Collections.singletonList(incantamento)));
        return con - senza;
    }

    /**
     * Il danno di un attaccante con FORZA e INTELLIGENZA fissate a 20 e senza critici
     */
    private double danno(PersonaggioBase attaccante, Arma arma) {
        attaccante.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        attaccante.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.QUANTITA_ASSOLUTA, 20));
        attaccante.addModificatore(new ModificatoreAttributoMD(TipoAttributo.INTELLIGENZA, TipoModificatore.QUANTITA_ASSOLUTA, 20));
        return CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma).getDanno();
    }

    private static Arma arma(TipoDanno tipoDanno, Collection<IncantamentoMD> incantamenti) {
        return new Arma() {
            @Override
            public int getDanni() {
                return 10;
            }

            @Override
            public int getLivello() {
                return 5;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return tipoDanno;
            }

            @Override
            public boolean isIncantata() {
                return !incantamenti.isEmpty();
            }

            @Override
            public Collection<IncantamentoMD> getIncantamenti() {
                return incantamenti;
            }
        };
    }
}
