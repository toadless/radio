import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging

plugins {
    java
    idea
    application

    id ("com.github.johnrengelman.shadow") version ("7.1.2")
    id ("nu.studer.jooq") version ("7.1.1")
}

val mainClassName = "net.toadless.radio.Main"

val RADIO_DB_USER: String by project
val RADIO_DB_PASSWORD: String by project
val RADIO_DB_URL: String by project

project.setProperty("mainClassName", mainClassName)

java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

repositories {
    mavenCentral()
    maven { setUrl ("https://m2.dv8tion.net/releases") }
    maven { setUrl ("https://jitpack.io") }
}

dependencies {

    //Misc / Util
    implementation ("info.debatty:java-string-similarity:2.0.0")
    implementation ("io.github.classgraph:classgraph:4.8.143")
    implementation ("ch.qos.logback:logback-classic:1.2.11")
    implementation ("net.jodah:expiringmap:0.5.10")
    implementation ("se.michaelthelin.spotify:spotify-web-api-java:7.0.1")
    implementation ("me.carleslc.Simple-YAML:Simple-Yaml:1.8")

    //Web
    implementation ("org.jsoup:jsoup:1.14.3")
    implementation ("io.javalin:javalin:4.4.0")

    //Eval
    implementation ("org.codehaus.groovy:groovy-jsr223:3.0.10")

    //Discord
    implementation ("net.dv8tion:JDA:4.4.0_352")
    implementation ("com.github.walkyst:lavaplayer-fork:1.3.97")

    //Database
    implementation ("org.postgresql:postgresql:42.3.4")
    jooqGenerator ("org.postgresql:postgresql:42.3.3")

    implementation ("com.zaxxer:HikariCP:5.0.1")
    implementation ("org.jooq:jooq:3.16.5")
}

jooq {
    version.set("3.16.4")
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                logging = Logging.WARN
                jdbc.apply {
                    url = RADIO_DB_URL
                    user = RADIO_DB_USER
                    password = RADIO_DB_PASSWORD
                    driver = "org.postgresql.Driver"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        forcedTypes.addAll(listOf(
                                ForcedType().apply {
                                    name = "varchar"
                                    includeExpression = ".*"
                                    includeTypes = "JSONB?"
                                },
                                ForcedType().apply {
                                    name = "varchar"
                                    includeExpression = ".*"
                                    includeTypes = "INET"
                                }
                        ))
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "net.toadless.radio.jooq"
                        directory = "src/main/jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}