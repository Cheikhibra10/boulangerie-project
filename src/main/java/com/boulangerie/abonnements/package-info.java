@ApplicationModule(
        allowedDependencies = {
                "shared::dto",
                "shared::exception",
                "shared::utils",
                "shared::mapper",
                "shared::model",
                "shared::specification",
                "comptabilite::service",
                "comptabilite::model",
                "comptabilite::mapper",
                "administration::model",
                "administration::repository",
                "administration::service",
                "administration::security",
                "production::api",
                "production::exception",
                "reporting::dto",
                "reporting::service"
        }
)
package com.boulangerie.abonnements;

import org.springframework.modulith.ApplicationModule;