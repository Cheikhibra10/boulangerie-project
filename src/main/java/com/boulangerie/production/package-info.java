@ApplicationModule(
        allowedDependencies = {
                "stocks::service",
                "administration::repository",
                "administration::model",
                "administration::security",
                "administration::service",
                "administration::api",
                "comptabilite::repository",
                "comptabilite::model",
                "comptabilite::service",
                "comptabilite::api",
                "shared::exception",
                "shared::mapper",
                "shared::dto",
                "shared::utils",
                "shared::model"
        }
)
package com.boulangerie.production;

import org.springframework.modulith.ApplicationModule;