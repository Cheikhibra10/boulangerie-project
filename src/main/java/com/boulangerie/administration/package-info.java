@ApplicationModule(
        allowedDependencies = {
                "shared::exception",
                "shared::mapper",
                "shared::dto",
                "shared::utils",
                "shared::controller",
                "shared::service",
                "shared::repository",
                "shared::model",
                "shared::service.impl",
                "stocks::model"
        }
)
package com.boulangerie.administration;

import org.springframework.modulith.ApplicationModule;