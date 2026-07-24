@ApplicationModule(
        allowedDependencies = {
                "stocks::api",
                "stocks::dto",
                "shared::exception",
                "shared::mapper",
                "shared::dto",
                "shared::utils",
                "shared::repository",
                "shared::model",
                "administration::model",
                "administration::repository",
                "administration::security",


        }
)
package com.boulangerie.achats;

import org.springframework.modulith.ApplicationModule;