@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "administration::model",
                "administration::repository",
                "administration::security",
                "shared::exception",
                "shared::mapper",
                "shared::dto",
                "shared::utils",
                "shared::model",
                "shared::specification",
                "administration::service",
                "comptabilite::service"
        }
)
package com.boulangerie.stocks;