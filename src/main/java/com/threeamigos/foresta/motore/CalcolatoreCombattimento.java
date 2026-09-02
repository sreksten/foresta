package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.SupertipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class CalcolatoreCombattimento {

    public static boolean colpisce(Personaggio attaccante, Personaggio difensore) {

        // 0. CONTROLLO EFFETTI DI STATO CHE DETERMINANO AUTOMATICAMENTE LA RIUSCITA
        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
            return false;
        }
        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
            return true;
        }

        // 1. CALCOLO DELLA PRECISIONE TOTALE DELL'ATTACCANTE
        int precisioneTotale = attaccante.getPrecisione() + attaccante.getDestrezza();

        // 2. CALCOLO DELLA VELOCITÀ TOTALE DEL DIFENSORE
        int velocitaTotale = difensore.getVelocita() + difensore.getDestrezza();

        // 3. CONTROLLO EFFETTI DI STATO
        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO)) {
            // La SAGGEZZA aiuta a mantenere la lucidità nonostante la confusione
            precisioneTotale = precisioneTotale * (8 + Math.min(2, attaccante.getSaggezza() / 20)) / 10;
        }
        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.ACCECATO)) {
            // La PERCEZIONE aiuta a compensare la cecità trovando il bersaglio
            double penalitaAccecato = (precisioneTotale / 2.0) * (1.0 - Math.min(1.0, attaccante.getPercezione() / 100.0));
            precisioneTotale -= (int)penalitaAccecato;
        }

        // Penalità di STANCHEZZA sull'attaccante
        precisioneTotale -= attaccante.getStanchezza() * 2;

        // 4. APPLICAZIONE DEI MODIFICATORI DI STATO AL DIFENSORE
        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
            return true;
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.RALLENTATO)) {
            // Chi è rallentato fatica a schivare
            velocitaTotale = velocitaTotale / 2;
        }
        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)) {
            // La paura blocca le gambe e riduce i riflessi; la SAGGEZZA e il CORAGGIO aiutano a resistervi
            velocitaTotale = (int)(velocitaTotale * (9 + Math.min(1, difensore.getSaggezza() / 20) + Math.min(1, difensore.getCoraggio() / 100.0)) / 10);
        }

        // Penalità di STANCHEZZA sul difensore
        velocitaTotale -= difensore.getStanchezza() * 2;

        // 5. CALCOLO DELLA PROBABILITÀ FINALE DI COLPIRE (Formula GDR base: 75% +/- scarto)
        int probabilitaFinale = 75 + (precisioneTotale - velocitaTotale) * 2;

        // Applica i limiti minimi e massimi (Cap) per mantenere il bilanciamento
        if (probabilitaFinale < 5) {
            probabilitaFinale = 5;
        } else if (probabilitaFinale > 95) {
            probabilitaFinale = 95;
        }

        // 6. TIRO DEL DADO (Generazione numero casuale da 1 a 100)
        int tiroDado = Dado.tira(100);

        return tiroDado <= probabilitaFinale;

    }

    public static RisultatoDanno calcolaDannoFinale(Personaggio attaccante, Personaggio difensore, TipoDanno tipoDanno,
                                             Artefatto arma) {

        RisultatoDanno risultatoDanno = new RisultatoDanno();

        // 1. CALCOLO STATISTICHE EFFETTIVE
        // Determina se l'attacco scala su FORZA (Fisico) o INTELLIGENZA (Magico/Elementale)
        int statOffensiva;
        if (tipoDanno.getSuperTipo() == SupertipoDanno.ELEMENTALE || tipoDanno.getSuperTipo() == SupertipoDanno.MAGICO) {
            statOffensiva = attaccante.getIntelligenza();
            // La SAGGEZZA potenzia i danni SACRO
            if (tipoDanno == TipoDanno.SACRO) {
                statOffensiva += attaccante.getSaggezza() / 2;
            }
        } else {
            statOffensiva = attaccante.getForza();
        }

        // Determina la difesa del bersaglio (COSTITUZIONE + PARATA per Fisico, RESISTENZA_MAGICA per Magico/Elementale)
        double statDifensiva;
        if (tipoDanno.getSuperTipo() == SupertipoDanno.ELEMENTALE || tipoDanno.getSuperTipo() == SupertipoDanno.MAGICO) {
            statDifensiva = difensore.getResistenzaMagica();
        } else {
            statDifensiva = difensore.getCostituzione() + difensore.getParata();
        }

        int intuitoCritico = attaccante.getCritico();
        int contromisuraCritico = difensore.getFortuna();

        // 2. MATEMATICA DI BASE DEL DANNO (Con fattore di scala livello arma)
        int dannoBaseArma = arma.getDanni() * arma.getLivello();

        // Rapporto di Efficacia dell'Arma per evitare exploit di armi liv. 1 su campioni liv. 20
        double rapportoEfficacia = (double) arma.getLivello() / (double) attaccante.getLivello();
        if (rapportoEfficacia > 1.0d) {
            rapportoEfficacia = 1.0d;
        }

        double contributoEroe = (double)(statOffensiva * attaccante.getLivello()) / 5;
        double dannoOffensivoGrezzo = dannoBaseArma + Math.floor(contributoEroe * rapportoEfficacia);

        // 2.5 APPLICAZIONE DEL BONUS BERSERK (Esclusivo ai Guerrieri con FURIA)
        if (tipoDanno.getSuperTipo() != SupertipoDanno.ELEMENTALE && tipoDanno.getSuperTipo() != SupertipoDanno.MAGICO &&
                attaccante.hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
            // Il danno fisico scala con la salute persa: più è ferito, più forte colpisce
            double percentualeSalutePerduta = 1.0d - ((double)attaccante.getSalute() / (double)attaccante.getSaluteMassima());
            dannoOffensivoGrezzo = dannoOffensivoGrezzo * (1.0d + percentualeSalutePerduta * 0.5d);
        }

        // 3. APPLICAZIONE INTERAZIONI ELEMENTALI E STATI DEL DIFENSORE
        double moltiplicatoreDannoStato = 1.0d;
        boolean criticoAutomatico = false;

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO)) {
            if (tipoDanno == TipoDanno.FULMINE) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.ELETTROCUZIONE);
            } else if (tipoDanno == TipoDanno.GELO) {
                risultatoDanno.rimuoviEffettoDiStato(TipoEffettoDiStato.BAGNATO);
                risultatoDanno.addEffettoDiStato(TipoEffettoDiStato.CONGELATO, 1);
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.CONGELAMENTO);
            } else if (tipoDanno == TipoDanno.FUOCO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 0.5d;
                risultatoDanno.rimuoviEffettoDiStato(TipoEffettoDiStato.BAGNATO);
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.VAPORIZZAZIONE);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.BRUCIATO)) {
            if (tipoDanno == TipoDanno.ACQUA) {
                risultatoDanno.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.ESTINZIONE);
            } else if (tipoDanno == TipoDanno.GELO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                risultatoDanno.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                risultatoDanno.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 1);
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.SCIOGLIMENTO_TERMICO);
            } else if (tipoDanno == TipoDanno.VELENO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.3d; // Esplosione di gas
                risultatoDanno.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                risultatoDanno.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 1);
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.ESPLOSIONE_DI_GAS);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO)) {
            if (tipoDanno == TipoDanno.CONTUNDENTE) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 2.0d;
                risultatoDanno.rimuoviEffettoDiStato(TipoEffettoDiStato.CONGELATO);
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.FRANTUMAZIONE_DEL_GHIACCO);
            } else if (tipoDanno == TipoDanno.FUOCO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                risultatoDanno.rimuoviEffettoDiStato(TipoEffettoDiStato.CONGELATO);
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.DISGELO_VIOLENTO);
            } else if (tipoDanno.getSuperTipo() == SupertipoDanno.FISICO) {
                statDifensiva = statDifensiva * 1.5d; // Il guscio di ghiaccio fa da scudo ai colpi di lama/punta
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.MALEDETTO)) {
            if (tipoDanno == TipoDanno.NECROTICO) {
                // La SAGGEZZA del difensore riduce l'efficacia dei danni NECROTICO su un bersaglio MALEDETTO
                double moltiplicatoreMaledetto = Math.max(1.0d, 2.0d - (difensore.getSaggezza() / 100.0));
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * moltiplicatoreMaledetto;
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.MIETITURA);
            } else if (tipoDanno == TipoDanno.SACRO) {
                risultatoDanno.rimuoviEffettoDiStato(TipoEffettoDiStato.MALEDETTO);
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.RIGETTO);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.INFETTATO)) {
            if (tipoDanno == TipoDanno.SACRO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                risultatoDanno.addInterazioneElementale(TipoInterazioneElementale.PURIFICAZIONE);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.STORDITO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO)) {
            if (tipoDanno == TipoDanno.TAGLIENTE || tipoDanno == TipoDanno.PERFORANTE) {
                criticoAutomatico = true;
                risultatoDanno.setColpoDiGrazia(true);
            }
        }

        // 4. MITIGAZIONE DELLA DIFESA DEL DIFENSORE (Formula Diminishing Returns)
        double fattoreMitigazione = 100.0d / (100.0d + statDifensiva);
        double dannoMitigato = Math.floor(dannoOffensivoGrezzo * fattoreMitigazione * moltiplicatoreDannoStato);

        // 5. DETERMINAZIONE DEL COLPO CRITICO (Come capire se il colpo raddoppia)
        // Formula di base: 5% fisso + 1% per ogni punto statistica CRITICO dell'attaccante,
        // contrastata dalla FORTUNA del difensore.
        double probabilitaCritico = Math.max(0.0d, 5.0d + intuitoCritico - contromisuraCritico);

        double tiroDadoCritico = Dado.tira(100);
        if (tiroDadoCritico <= probabilitaCritico || criticoAutomatico) {
            // Il colpo critico raddoppia il danno finale calcolato
            dannoMitigato = dannoMitigato * 2.0d;
        }

        // 6. APPLICAZIONE DEI NUOVI STATI NATIVI DEL TIPO DI DANNO (Proc Rate)
        // Più danno si fa rispetto alla vita del difensore, più è facile infliggere lo stato (es. Veleno o Sanguinamento)
        if (dannoMitigato > 0.0d || tipoDanno.hasEffettiDiStato()) {
            int statAusiliariaProc;

            if (tipoDanno.getSuperTipo() == SupertipoDanno.MAGICO || tipoDanno.getSuperTipo() == SupertipoDanno.ELEMENTALE) {
                statAusiliariaProc = attaccante.getMagia();
            } else {
                statAusiliariaProc = attaccante.getFuria();
            }

            double probabilitaApplicareStato = ((dannoMitigato * 100.0d) / difensore.getForza()) + (statAusiliariaProc * 2.0d);
            double tiroDadoStato = Dado.tira(100);

            if (tiroDadoStato <= probabilitaApplicareStato) {
                TipoEffettoDiStato effettoDiStato = tipoDanno.getTipoEffettoDiStatoCasuale();

                if (!difensore.hasEffettoDiStato(effettoDiStato)) {
                    risultatoDanno.addEffettoDiStato(effettoDiStato, 1);
                }
            }

            risultatoDanno.addDanno(Math.max(1, (int)dannoMitigato));
        }

        return risultatoDanno;
    }
}
