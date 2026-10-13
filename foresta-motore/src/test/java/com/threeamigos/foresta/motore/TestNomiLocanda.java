package com.threeamigos.foresta.motore;

import java.util.List;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;

public class TestNomiLocanda extends TestGrammatiche {

       public static void main(String[] args) throws Exception {
           ProduttoreDiTestiCasuale.resetProduzioni();
            List<ProduttoreDiTestiCasuale.DatiLocanda> datiLocanda = ProduttoreDiTestiCasuale.getDatiLocanda(15);

            for (ProduttoreDiTestiCasuale.DatiLocanda datiLocandaCorrente : datiLocanda) {
            	System.out.println(datiLocandaCorrente.getNome());
                System.out.println(datiLocandaCorrente.getDialogo());
                System.out.println(datiLocandaCorrente.getRecensione());
                System.out.println("------------------------------");
            }
        }

}
