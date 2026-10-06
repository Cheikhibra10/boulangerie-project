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
                "shared::specification",
                "administration::model",
                "administration::repository",
                "administration::security",


        }
)
package com.boulangerie.achats;

import org.springframework.modulith.ApplicationModule;