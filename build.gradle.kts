plugins {
    id("java")
    id("com.diffplug.spotless") version "6.25.0" // or latest
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

}

tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat() // or prettier() / eclipse() if preferred
        target("src/**/*.java")
    }
}
