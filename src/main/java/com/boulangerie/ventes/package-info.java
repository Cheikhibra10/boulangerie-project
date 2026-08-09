@ApplicationModule(
        allowedDependencies = {
                "stocks::service",
                "stocks::dto",
                "administration::service",
                "administration::model",
                "administration::security",
                "shared::exception",
                "shared::mapper",
                "shared::dto",
                "shared::utils",
                "shared::model",
                "comptabilite::api",
                "comptabilite::dto",
                "comptabilite::model",
                "comptabilite::event",
                "production::api"
        }
)
package com.boulangerie.ventes;

import org.springframework.modulith.ApplicationModule;