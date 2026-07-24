package com.boulangerie;


import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;


class BoulangerieModularityTest {


    @Test
    void verifierModularite() {

        ApplicationModules modules =
                ApplicationModules.of(BoulangerieApplication.class);


        modules.verify();


        assertThat(modules.stream())
                .isNotEmpty();

    }



    @Test
    void genererDocumentation() {


        ApplicationModules modules =
                ApplicationModules.of(BoulangerieApplication.class);


        new Documenter(modules)

                .writeModulesAsPlantUml()

                .writeIndividualModulesAsPlantUml()

                .writeModuleCanvases();

    }

}