package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Stefano Reksten
 */
public class TestGenerazionePersonaggi {

    /**
     * Costruisce un CSV con i valori medi delle classi dei personaggi
     * @throws IOException
     */
    @Test
    public void testValoriMedi() throws IOException {
        final int NUMERO_ISTANZE = 10000;
        final String CSV_FILE = "PERSONAGGI_VALORI_MEDI.csv";

        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            writer.append("TIPO,FORZA,FORZA_MEDIA,DESTREZZA,DESTREZZA_MEDIA,COSTITUZIONE,COSTITUZIONE_MEDIA,");
            writer.append("INTELLIGENZA,INTELLIGENZA_MEDIA,SAGGEZZA,SAGGEZZA_MEDIA,CARISMA,CARISMA_MEDIA,");
            writer.append("FORTUNA,FORTUNA_MEDIA,CRITICO_MEDIA,CARICO_MASSIMO_MEDIA,PRECISIONE_MEDIA,");
            writer.append("VELOCITA_MEDIA,FURTIVITA_MEDIA,PARATA_MEDIA,RESISTENZA_MAGICA_MEDIA,");
            writer.append("PERCEZIONE_MEDIA,SOGGEZIONE_MEDIA,FURIA_MEDIA,CORAGGIO_MEDIA,VALORE_MEDIA,CONTRATTAZIONE_MEDIA\n");

            for (ClassePersonaggio classe : ClassePersonaggio.values()) {
                Personaggio pPerMax = classe.getIstanza(1);
                PersonaggioBase pbPerMax = (PersonaggioBase) pPerMax;

                long sommaForza = 0, sommaDestrezza = 0, sommaCostituzione = 0, sommaIntelligenza = 0;
                long sommaSaggezza = 0, sommaCarisma = 0, sommaFortuna = 0;
                long sommaCritico = 0, sommaCarico = 0, sommaPrecisione = 0, sommaVelocita = 0;
                long sommaFurtivita = 0, sommaParata = 0, sommaResistenzaMagica = 0, sommaPercezione = 0;
                long sommaSoggezione = 0, sommaFuria = 0, sommaCoraggio = 0, sommaValore = 0;
                long sommaContrattazione = 0;

                for (int i = 0; i < NUMERO_ISTANZE; i++) {
                    Personaggio p = classe.getIstanza(1);
                    assertNotNull(p);
                    sommaForza += p.getForza();
                    sommaDestrezza += p.getDestrezza();
                    sommaCostituzione += p.getCostituzione();
                    sommaIntelligenza += p.getIntelligenza();
                    sommaSaggezza += p.getSaggezza();
                    sommaCarisma += p.getCarisma();
                    sommaFortuna += p.getFortuna();
                    sommaCritico += p.getCritico();
                    sommaCarico += p.getCaricoMassimo();
                    sommaPrecisione += p.getPrecisione();
                    sommaVelocita += p.getVelocita();
                    sommaFurtivita += p.getFurtivita();
                    sommaParata += p.getParata();
                    sommaResistenzaMagica += p.getResistenzaMagica();
                    sommaPercezione += p.getPercezione();
                    sommaSoggezione += p.getSoggezione();
                    sommaFuria += p.getFuria();
                    sommaCoraggio += p.getCoraggio();
                    sommaValore += p.getValore();
                    sommaContrattazione += p.getContrattazione();
                }

                double mediaForza = sommaForza / (double) NUMERO_ISTANZE;
                double mediaDestrezza = sommaDestrezza / (double) NUMERO_ISTANZE;
                double mediaCostituzione = sommaCostituzione / (double) NUMERO_ISTANZE;
                double mediaIntelligenza = sommaIntelligenza / (double) NUMERO_ISTANZE;
                double mediaSaggezza = sommaSaggezza / (double) NUMERO_ISTANZE;
                double mediaCarisma = sommaCarisma / (double) NUMERO_ISTANZE;
                double mediaFortuna = sommaFortuna / (double) NUMERO_ISTANZE;
                double mediaCritico = sommaCritico / (double) NUMERO_ISTANZE;
                double mediaCarico = sommaCarico / (double) NUMERO_ISTANZE;
                double mediaPrecisione = sommaPrecisione / (double) NUMERO_ISTANZE;
                double mediaVelocita = sommaVelocita / (double) NUMERO_ISTANZE;
                double mediaFurtivita = sommaFurtivita / (double) NUMERO_ISTANZE;
                double mediaParata = sommaParata / (double) NUMERO_ISTANZE;
                double mediaResistenzaMagica = sommaResistenzaMagica / (double) NUMERO_ISTANZE;
                double mediaPercezione = sommaPercezione / (double) NUMERO_ISTANZE;
                double mediaSoggezione = sommaSoggezione / (double) NUMERO_ISTANZE;
                double mediaFuria = sommaFuria / (double) NUMERO_ISTANZE;
                double mediaCoraggio = sommaCoraggio / (double) NUMERO_ISTANZE;
                double mediaValore = sommaValore / (double) NUMERO_ISTANZE;
                double mediaContrattazione = sommaContrattazione / (double) NUMERO_ISTANZE;

                writer.append(classe.toString()).append(",");
                writer.append(String.valueOf(pbPerMax.getMaxStatistica(TipoAttributo.FORZA))).append(",");
                writer.append(String.format("%.2f", mediaForza)).append(",");
                writer.append(String.valueOf(pbPerMax.getMaxStatistica(TipoAttributo.DESTREZZA))).append(",");
                writer.append(String.format("%.2f", mediaDestrezza)).append(",");
                writer.append(String.valueOf(pbPerMax.getMaxStatistica(TipoAttributo.COSTITUZIONE))).append(",");
                writer.append(String.format("%.2f", mediaCostituzione)).append(",");
                writer.append(String.valueOf(pbPerMax.getMaxStatistica(TipoAttributo.INTELLIGENZA))).append(",");
                writer.append(String.format("%.2f", mediaIntelligenza)).append(",");
                writer.append(String.valueOf(pbPerMax.getMaxStatistica(TipoAttributo.SAGGEZZA))).append(",");
                writer.append(String.format("%.2f", mediaSaggezza)).append(",");
                writer.append(String.valueOf(pbPerMax.getMaxStatistica(TipoAttributo.CARISMA))).append(",");
                writer.append(String.format("%.2f", mediaCarisma)).append(",");
                writer.append(String.valueOf(pbPerMax.getMaxStatistica(TipoAttributo.FORTUNA))).append(",");
                writer.append(String.format("%.2f", mediaFortuna)).append(",");
                writer.append(String.format("%.2f", mediaCritico)).append(",");
                writer.append(String.format("%.2f", mediaCarico)).append(",");
                writer.append(String.format("%.2f", mediaPrecisione)).append(",");
                writer.append(String.format("%.2f", mediaVelocita)).append(",");
                writer.append(String.format("%.2f", mediaFurtivita)).append(",");
                writer.append(String.format("%.2f", mediaParata)).append(",");
                writer.append(String.format("%.2f", mediaResistenzaMagica)).append(",");
                writer.append(String.format("%.2f", mediaPercezione)).append(",");
                writer.append(String.format("%.2f", mediaSoggezione)).append(",");
                writer.append(String.format("%.2f", mediaFuria)).append(",");
                writer.append(String.format("%.2f", mediaCoraggio)).append(",");
                writer.append(String.format("%.2f", mediaValore)).append(",");
                writer.append(String.format("%.2f", mediaContrattazione)).append("\n");
            }
        }
        System.out.println("File CSV generato: " + CSV_FILE);
    }

    @Test
    public void testValoriMassimiCoerenti() {
        for (ClassePersonaggio classe : ClassePersonaggio.values()) {
            Personaggio p = classe.getIstanza(1);
            PersonaggioBase pb = (PersonaggioBase) p;

            if (pb.isParteConValoriMassimi()) {
                assertEquals(p.getForza(), pb.getMaxStatistica(TipoAttributo.FORZA),
                        p.getNomeSingolare() + " ha isParteConValoriMassimi=true ma FORZA=" + p.getForza());
                assertEquals(p.getDestrezza(), pb.getMaxStatistica(TipoAttributo.DESTREZZA),
                        p.getNomeSingolare() + " ha isParteConValoriMassimi=true ma DESTREZZA=" + p.getDestrezza());
                assertEquals(p.getCostituzione(), pb.getMaxStatistica(TipoAttributo.COSTITUZIONE),
                        p.getNomeSingolare() + " ha isParteConValoriMassimi=true ma COSTITUZIONE=" + p.getCostituzione());
                assertEquals(p.getIntelligenza(), pb.getMaxStatistica(TipoAttributo.INTELLIGENZA),
                        p.getNomeSingolare() + " ha isParteConValoriMassimi=true ma INTELLIGENZA=" + p.getIntelligenza());
                assertEquals(p.getSaggezza(), pb.getMaxStatistica(TipoAttributo.SAGGEZZA),
                        p.getNomeSingolare() + " ha isParteConValoriMassimi=true ma SAGGEZZA=" + p.getSaggezza());
                assertEquals(p.getCarisma(), pb.getMaxStatistica(TipoAttributo.CARISMA),
                        p.getNomeSingolare() + " ha isParteConValoriMassimi=true ma CARISMA=" + p.getCarisma());
                assertEquals(p.getFortuna(), pb.getMaxStatistica(TipoAttributo.FORTUNA),
                        p.getNomeSingolare() + " ha isParteConValoriMassimi=true ma FORTUNA=" + p.getFortuna());
            }
        }
    }

}
