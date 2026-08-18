@ApplicationModule(
        allowedDependencies = {
                "shared::dto",
                "shared::exception",
                "shared::utils",
                "shared::mapper",
                "shared::model",
                "comptabilite::service",
                "comptabilite::model",
                "comptabilite::mapper",
                "administration::model",
                "administration::repository",
                "administration::service",
                "administration::security",
                "livreurs::repository",
                "production::api",
                "production::exception"
        }
)
package com.boulangerie.abonnements;

import org.springframework.modulith.ApplicationModule;