@ApplicationModule(
        allowedDependencies = {
                "abonnements::repository",
                "stocks::repository",
                "ventes::repository",
                "ventes::service",
                "ventes::model",
                "livreurs::repository",
                "production::repository",
                "achats::repository",
                "achats::model",
                "comptabilite::model",
                "abonnements::api",
                "comptabilite::repository",
                "stocks::model",
                "administration::model",
                "administration::service",
                "shared::dto"

        }
)
package com.boulangerie.reporting;

import org.springframework.modulith.ApplicationModule;