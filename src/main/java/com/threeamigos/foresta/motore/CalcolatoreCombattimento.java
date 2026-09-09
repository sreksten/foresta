package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.SupertipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class CalcolatoreCombattimento {

    public static int calcolaProbabilitaDiColpire(Personaggio attaccante, Personaggio difensore, SupertipoDanno tipoDanno) {

        // 0. CONTROLLO EFFETTI DI STATO CHE DETERMINANO AUTOMATICAMENTE LA RIUSCITA
        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
            Logger.log("L'attaccante è STORDITO e non può colpire.");
            return 0;
        }
        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
            Logger.log("Il difensore è ATTERRATO/CONGELATO/STORDITO e viene colpito automaticamente.");
            return 100;
        }

        double attaccoTotale = 0.0d;
        double difesaTotale = 0.0d;

        // BIVIO LOGICO DI BILANCIAMENTO IN BASE AL TIPO DI DANNO
        if (tipoDanno == SupertipoDanno.FISICO) {
            // --- CALCOLO ASSE FISICO ---
            // Attaccante usa la coordinazione occhio-mano (Precisione + Destrezza)
            attaccoTotale = attaccante.getPrecisione() + attaccante.getDestrezza();
            Logger.log(String.format("[FISICO] Attacco totale (PRECISIONE %d + DESTREZZA %d): %f",
                    attaccante.getPrecisione(), attaccante.getDestrezza(), attaccoTotale));

            // Il difensore contrasta fisicamente (Velocità + Destrezza) modificata dalla PARATA
            difesaTotale = difensore.getVelocita() + difensore.getDestrezza() + (difensore.getParata() * 0.5d);
            Logger.log(String.format("[FISICO] Difesa totale (VELOCITA %d + DESTREZZA %d + 50%% PARATA %d): %f",
                    difensore.getVelocita(), difensore.getDestrezza(), difensore.getParata(), difesaTotale));

        } else {
            // --- CALCOLO ASSE MAGICO/ELEMENTALE ---
            // Chi lancia magie si affida al controllo mentale e alla potenza magica (Intelligenza + Precisione)
            attaccoTotale = attaccante.getPrecisione() + attaccante.getIntelligenza();
            Logger.log(String.format("[MAGICO] Attacco totale (PRECISIONE %d + INTELLIGENZA %d): %f",
                    attaccante.getPrecisione(), attaccante.getIntelligenza(), attaccoTotale));

            // Chi subisce magie si affida al filtro difensivo mistico e alla stabilità mentale (Resistenza Magica + Saggezza)
            difesaTotale = difensore.getResistenzaMagica() + difensore.getSaggezza();
            Logger.log(String.format("[MAGICO] Difesa totale (RESISTENZA_MAGICA %d + SAGGEZZA %d): %f",
                    difensore.getResistenzaMagica(), difensore.getSaggezza(), difesaTotale));
        }

        // 3. CONTROLLO EFFETTI DI STATO SULL'ATTACCANTE (CONFUSO / ACCECATO / STANCHEZZA)
        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO)) {
            attaccoTotale = attaccoTotale * (8.0d + Math.min(2, attaccante.getSaggezza() / 20.0d)) / 10.0d;
            Logger.log(String.format("Attacco dopo effetto CONFUSO (mitigato da SAGGEZZA %d): %f", attaccante.getSaggezza(), attaccoTotale));
        }

        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.ACCECATO)) {
            if (tipoDanno == SupertipoDanno.FISICO) {
                // La cecità devasta la mira fisica
                double penalitaAccecato = (attaccoTotale / 2.0d) * (1.0d - Math.min(1.0d, attaccante.getPercezione() / 100.0d));
                attaccoTotale -= penalitaAccecato;
            } else {
                // Per le magie, la cecità influisce meno perché il mago si guida con la Percezione dei flussi magici
                double penalitaAccecatoMagico = (attaccoTotale / 4.0d) * (1.0d - Math.min(1.0d, attaccante.getPercezione() / 100.0d));
                attaccoTotale -= penalitaAccecatoMagico;
            }
            Logger.log(String.format("Attacco dopo effetto ACCECATO (mitigato da PERCEZIONE %d): %f", attaccante.getPercezione(), attaccoTotale));
        }

        // Penalità di STANCHEZZA sull'attaccante
        attaccoTotale -= attaccante.getStanchezza() * 2.0d;
        Logger.log(String.format("Attacco totale dopo stanchezza (STANCHEZZA %d): %f", attaccante.getStanchezza(), attaccoTotale));


        // 4. APPLICAZIONE DEI MODIFICATORI DI STATO AL DIFENSORE
        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.RALLENTATO)) {
            if (tipoDanno == SupertipoDanno.FISICO) {
                difesaTotale = difesaTotale / 2.0d; // Rallentato distrugge la schivata fisica
            } else {
                // Rallentato influisce pochissimo sulla barriera mistica passiva (Resistenza Magica)
                difesaTotale = difesaTotale * 0.9d;
            }
            Logger.log("Difesa totale dopo effetto RALLENTATO: " + difesaTotale);
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)) {
            difesaTotale = (difesaTotale * (9.0d + Math.min(1, difensore.getSaggezza() / 20.0d) + Math.min(1.0d, difensore.getCoraggio() / 100.0d)) / 10.0d);
            Logger.log(String.format("Difesa totale dopo effetto SPAVENTATO (mitigato da SAGGEZZA %d e CORAGGIO %d): %f", difensore.getSaggezza(), difensore.getCoraggio(), difesaTotale));
        }

        // Penalità di STANCHEZZA sul difensore (La fatica logora sia i riflessi fisici che la concentrazione magica)
        difesaTotale -= difensore.getStanchezza() * 2.0d;
        Logger.log(String.format("Difesa totale dopo stanchezza (STANCHEZZA %d): %f", difensore.getStanchezza(), difesaTotale));

        // 5. CALCOLO DELLA PROBABILITÀ FINALE DI COLPIRE
        int probabilitaFinale = 75 + (int)((attaccoTotale - difesaTotale) * 2.0d);
        Logger.log("Probabilità finale di colpire: " + probabilitaFinale);

        // Limiti minimi e massimi (Cap) per il bilanciamento
        if (probabilitaFinale < 5) {
            probabilitaFinale = 5;
        } else if (probabilitaFinale > 95) {
            probabilitaFinale = 95;
        }
        Logger.log("Probabilità finale di colpire dopo applicazione dei limiti: " + probabilitaFinale);
        return probabilitaFinale;
    }

    public static boolean colpisce(Personaggio attaccante, Personaggio difensore, SupertipoDanno tipoDanno) {

        int probabilitaFinale = calcolaProbabilitaDiColpire(attaccante, difensore, tipoDanno);

        // 6. TIRO DEL DADO (Generazione numero casuale da 1 a 100)
        int tiroDado = Dado.tira(100);
        Logger.log("Risultato sul COLPIRE (tiro del dado = " + tiroDado + "): " + (tiroDado <= probabilitaFinale));

        return tiroDado <= probabilitaFinale;
    }

    public static RisultatoCombattimento calcolaDannoFinale(Personaggio attaccante, Personaggio difensore, Arma arma) {

        RisultatoCombattimento risultatoCombattimento = new RisultatoCombattimento(attaccante, difensore);
        TipoDanno tipoDanno = arma.getTipoDanno();
        risultatoCombattimento.setTipoDanno(tipoDanno);

        // 1. CALCOLO STATISTICHE EFFETTIVE
        // Determina se l'attacco scala su FORZA (Fisico) o INTELLIGENZA (Magico/Elementale)
        int statOffensiva;
        boolean dannoNonFisico = tipoDanno.getSuperTipo() == SupertipoDanno.ELEMENTALE || tipoDanno.getSuperTipo() == SupertipoDanno.MAGICO;
        Logger.log("tipoDanno = " + tipoDanno + ", dannoNonFisico = " + dannoNonFisico);
        if (dannoNonFisico) {
            statOffensiva = attaccante.getIntelligenza();
            Logger.log("statOffensiva (INTELLIGENZA) = " + statOffensiva);
            // La SAGGEZZA potenzia i danni SACRO
            if (tipoDanno == TipoDanno.SACRO) {
                statOffensiva += attaccante.getSaggezza() / 2;
                Logger.log(String.format("statOffensiva modificata da SACRO (%d) = %d", attaccante.getSaggezza(), statOffensiva));
            }
        } else {
            statOffensiva = attaccante.getForza();
            Logger.log("statOffensiva (FORZA) = " + statOffensiva);
        }

        // Determina la difesa del bersaglio (COSTITUZIONE + PARATA per Fisico, RESISTENZA_MAGICA per Magico/Elementale)
        double statDifensiva;
        if (dannoNonFisico) {
            statDifensiva = difensore.getResistenzaMagica();
            Logger.log("statDifensiva (RESISTENZA_MAGICA) = " + statDifensiva);
        } else {
            statDifensiva = difensore.getCostituzione() + difensore.getParata();
            Logger.log(String.format("statDifensiva (COSTITUZIONE %d + PARATA %d) = %f", difensore.getCostituzione(), difensore.getParata(), statDifensiva));
        }

        // 2. MATEMATICA DI BASE DEL DANNO (Con fattore di scala livello arma)
        int dannoBaseArma = arma.getDanni() * arma.getLivello();

        Logger.log(String.format("dannoBaseArma = danniBase %d + livello arma %d = %d", arma.getDanni(), arma.getLivello(), dannoBaseArma));

        // Rapporto di Efficacia dell'Arma per evitare exploit di armi liv. 1 su campioni liv. 20
        double rapportoEfficacia = (double) arma.getLivello() / (double) attaccante.getLivello();
        if (rapportoEfficacia > 1.0d) {
            rapportoEfficacia = 1.0d;
        }
        Logger.log(String.format("rapportoEfficacia (livello arma %d / livello attaccante %d) = %f", arma.getLivello(), attaccante.getLivello(), rapportoEfficacia));

        double contributoEroe = (double)(statOffensiva * attaccante.getLivello()) / 5.0d;
        Logger.log(String.format("contributoEroe (statOffensiva %d * livello attaccante %d / 5 = %f", statOffensiva, attaccante.getLivello(), contributoEroe));
        double dannoOffensivoGrezzo = dannoBaseArma + Math.floor(contributoEroe * rapportoEfficacia);
        Logger.log("dannoOffensivoGrezzo = " + dannoOffensivoGrezzo);

        // 2.5 APPLICAZIONE DEL BONUS BERSERK (Esclusivo ai Guerrieri con FURIA)
        if (dannoNonFisico && attaccante.hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
            // Il danno fisico scala con la salute persa: più è ferito, più forte colpisce
            double percentualeSalutePerduta = 1.0d - ((double)attaccante.getSalute() / (double)attaccante.getSaluteMassima());
            dannoOffensivoGrezzo = dannoOffensivoGrezzo * (1.0d + percentualeSalutePerduta * 0.5d);
            Logger.log("dannoOffensivoGrezzo dopo BERSERK = " + dannoOffensivoGrezzo);
        }

        // 3. APPLICAZIONE INTERAZIONI ELEMENTALI E STATI DEL DIFENSORE
        double moltiplicatoreDannoStato = 1.0d;
        boolean criticoAutomatico = false;

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO)) {
            Logger.log("difensore ha stato BAGNATO");
            if (tipoDanno == TipoDanno.FULMINE) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.ELETTROCUZIONE);
            } else if (tipoDanno == TipoDanno.GELO) {
                risultatoCombattimento.rimuoviEffettoDiStato(TipoEffettoDiStato.BAGNATO);
                risultatoCombattimento.addEffettoDiStato(TipoEffettoDiStato.CONGELATO, 1);
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.CONGELAMENTO);
            } else if (tipoDanno == TipoDanno.FUOCO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 0.5d;
                risultatoCombattimento.rimuoviEffettoDiStato(TipoEffettoDiStato.BAGNATO);
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.VAPORIZZAZIONE);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.BRUCIATO)) {
            Logger.log("difensore ha stato BRUCIATO");
            if (tipoDanno == TipoDanno.ACQUA) {
                risultatoCombattimento.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.ESTINZIONE);
            } else if (tipoDanno == TipoDanno.GELO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                risultatoCombattimento.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                risultatoCombattimento.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 1);
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.SCIOGLIMENTO_TERMICO);
            } else if (tipoDanno == TipoDanno.VELENO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.3d; // Esplosione di gas
                risultatoCombattimento.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                risultatoCombattimento.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 1);
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.ESPLOSIONE_DI_GAS);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO)) {
            Logger.log("difensore ha stato CONGELATO");
            if (tipoDanno == TipoDanno.CONTUNDENTE) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 2.0d;
                risultatoCombattimento.rimuoviEffettoDiStato(TipoEffettoDiStato.CONGELATO);
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.FRANTUMAZIONE_DEL_GHIACCO);
            } else if (tipoDanno == TipoDanno.FUOCO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                risultatoCombattimento.rimuoviEffettoDiStato(TipoEffettoDiStato.CONGELATO);
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.DISGELO_VIOLENTO);
            } else if (tipoDanno.getSuperTipo() == SupertipoDanno.FISICO) {
                statDifensiva = statDifensiva * 1.5d; // Il guscio di ghiaccio fa da scudo ai colpi di lama/punta
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.MALEDETTO)) {
            Logger.log("difensore ha stato MALEDETTO");
            if (tipoDanno == TipoDanno.NECROTICO) {
                // La SAGGEZZA del difensore riduce l'efficacia dei danni NECROTICO su un bersaglio MALEDETTO
                double moltiplicatoreMaledetto = Math.max(1.0d, 2.0d - (difensore.getSaggezza() / 100.0));
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * moltiplicatoreMaledetto;
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.MIETITURA);
            } else if (tipoDanno == TipoDanno.SACRO) {
                risultatoCombattimento.rimuoviEffettoDiStato(TipoEffettoDiStato.MALEDETTO);
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.RIGETTO);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.INFETTATO)) {
            Logger.log("difensore ha stato INFETTATO");
            if (tipoDanno == TipoDanno.SACRO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                risultatoCombattimento.addInterazioneElementale(TipoInterazioneElementale.PURIFICAZIONE);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.STORDITO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO)) {
            Logger.log("difensore ha stato STORDITO o ATTERRATO");
            if (tipoDanno == TipoDanno.TAGLIENTE || tipoDanno == TipoDanno.PERFORANTE) {
                Logger.log("tipo danno TAGLIENTE o PERFORANTE, critico automatico");
                criticoAutomatico = true;
                risultatoCombattimento.setColpoDiGrazia(true);
            }
        }

        // 4. MITIGAZIONE DELLA DIFESA DEL DIFENSORE (Formula Diminishing Returns)
        double fattoreMitigazione = 100.0d / (100.0d + statDifensiva);
        Logger.log("fattoreMitigazione: " + fattoreMitigazione + ", moltiplicatoreDannoStato = " + moltiplicatoreDannoStato);
        double dannoMitigato = Math.floor(dannoOffensivoGrezzo * fattoreMitigazione * moltiplicatoreDannoStato);
        Logger.log("dannoMitigato: " + dannoMitigato);

        // 5. DETERMINAZIONE DEL COLPO CRITICO (Come capire se il colpo raddoppia)
        // Formula di base: 5% fisso + 1% per ogni punto statistica CRITICO dell'attaccante,
        // contrastata dalla FORTUNA del difensore.

        int intuitoCritico = attaccante.getCritico();
        int contromisuraCritico = difensore.getFortuna();

        double probabilitaCritico = Math.max(0.0d, 5.0d + intuitoCritico - contromisuraCritico);
        Logger.log(String.format("probabilitaCritico: %f = MAX(0, 5 + CRITICO attaccante %d - FORTUNA difensore %d)", probabilitaCritico, intuitoCritico, contromisuraCritico));

        double tiroDadoCritico = Dado.tira(100);
        Logger.log("tiroDadoCritico: " + tiroDadoCritico);
        if (tiroDadoCritico <= probabilitaCritico || criticoAutomatico) {
            // Il colpo critico raddoppia il danno finale calcolato
            dannoMitigato = dannoMitigato * 2.0d;
            Logger.log("dannoMitigato raddoppiato per CRITICO: " + dannoMitigato);
        }

        // 6. APPLICAZIONE DEI NUOVI STATI NATIVI DEL TIPO DI DANNO (Proc Rate)
        // Più danno si fa rispetto alla vita del difensore, più è facile infliggere lo stato (es. Veleno o Sanguinamento)
        if (dannoMitigato > 0.0d || tipoDanno.hasEffettiDiStato()) {
            Logger.log("Applicazione stati nativi");
            int statAusiliariaProc;

            if (dannoNonFisico) {
                statAusiliariaProc = attaccante.getMagia();
            } else {
                statAusiliariaProc = attaccante.getFuria();
            }
            Logger.log("statAusiliariaProc: " + statAusiliariaProc);

            double probabilitaApplicareStato = ((dannoMitigato * 100.0d) / difensore.getForza()) + (statAusiliariaProc * 2.0d);
            Logger.log("probabilitaApplicareStato: " + probabilitaApplicareStato);
            double tiroDadoStato = Dado.tira(100);
            Logger.log("tiroDadoStato: " + tiroDadoStato);

            if (tiroDadoStato <= probabilitaApplicareStato) {
                Logger.log("Possibilità di applicare stato nativo");
                TipoEffettoDiStato effettoDiStato = tipoDanno.getTipoEffettoDiStatoCasuale();
                Logger.log("Stato nativo applicato: " + effettoDiStato);

                if (!difensore.hasEffettoDiStato(effettoDiStato)) {
                    risultatoCombattimento.addEffettoDiStato(effettoDiStato, 1);
                }
            } else {
                Logger.log("Stato nativo non applicato");
            }

            int dannoFinale = Math.max(1, (int)dannoMitigato);
            Logger.log("dannoFinale: " + dannoFinale);
            risultatoCombattimento.setDanno(dannoFinale);
        }

        return risultatoCombattimento;
    }
}
