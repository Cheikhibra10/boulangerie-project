@ApplicationModule(
        allowedDependencies = {
                "shared::exception",
                "shared::mapper",
                "shared::dto",
                "shared::utils",
                "shared::repository",
                "shared::model",
                "stocks::model",
                "stocks::mapper",
                "stocks::dto",
                "stocks::api",
                "achats::exception",
                "administration::repository",
                "abonnements::repository",
                "abonnements::api",
                "administration::model",
                "administration::security",
                "administration::service",
                "administration::api",
                "livreurs::api",
                "ventes::model",
                "ventes::api",
                "ventes::event",
                "production::api"
        }
)
package com.boulangerie.comptabilite;

import org.springframework.modulith.ApplicationModule;