@ApplicationModule(
        allowedDependencies = {
                "shared::exception",
                "shared::mapper",
                "shared::dto",
                "shared::utils",
                "shared::repository",
                "shared::model",
                "shared::service",
                "stocks::model",
                "stocks::mapper",
                "stocks::dto",
                "stocks::api",
                "achats::exception",
                "administration::repository",
                "abonnements::repository",
                "abonnements::api",
                "abonnements::event",
                "administration::model",
                "administration::security",
                "administration::service",
                "administration::api",
                "livreurs::api",
                "livreurs::event",
                "ventes::model",
                "ventes::api",
                "ventes::event",
                "production::api",
                "ventes::repository",
                "livreurs::repository",
                "livreurs::model"
        }
)
package com.boulangerie.comptabilite;

import org.springframework.modulith.ApplicationModule;