plugins {
    //    application
    kotlin("jvm")
    id("org.flywaydb.flyway") version ("5.1.3")
}

dependencies {
    compile(kotlin("stdlib", embeddedKotlinVersion))
    compile("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "0.22.5")
    compile("org.postgresql:postgresql:42.2.2")
}

var ext2: ExtraPropertiesExtension = ext;
flyway {
    url = "${ext2["DB_URL"]}"
    user = "${ext2["DB_USER"]}"
    password = "${ext2["DB_PASSWORD"]}"
}