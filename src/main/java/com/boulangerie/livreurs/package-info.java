@ApplicationModule(
        allowedDependencies = {
                "administration::model",
                "administration::repository",
                "administration::security",
                "administration::service",
                "production::model",
                "production::api",
                "production::dto",
                "abonnements::api",
                "abonnements::model",
                "shared::dto",
                "shared::exception",
                "shared::utils",
                "shared::mapper",
                "shared::model",
                "comptabilite::model",
                "comptabilite::service",
                "comptabilite::api"
        }
)
package com.boulangerie.livreurs;

import org.springframework.modulith.ApplicationModule;