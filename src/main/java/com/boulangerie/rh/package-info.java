
@ApplicationModule(
        allowedDependencies = {
                "shared::model",
                "shared::dto",
                "shared::exception",
                "shared::utils",
                "shared::specification",
                "comptabilite::api",
                "administration::api"
        }
)

package com.boulangerie.rh;

import org.springframework.modulith.ApplicationModule;
