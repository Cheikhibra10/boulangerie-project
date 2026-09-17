@ApplicationModule(
        allowedDependencies = {
                "stocks::repository",
                "ventes::repository",
                "ventes::service",
                "ventes::model",
                "livreurs::repository",
                "production::repository",
                "production::dto",
                "achats::repository",
                "achats::model",
                "comptabilite::model",
                "abonnements::api",
                "abonnements::projection",
                "abonnements::service",
                "abonnements::model",
                "abonnements::dto",
                "comptabilite::repository",
                "stocks::model",
                "administration::model",
                "administration::service",
                "shared::dto"

        }
)
package com.boulangerie.reporting;

import org.springframework.modulith.ApplicationModule;