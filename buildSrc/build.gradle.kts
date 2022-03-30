plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
dependencies {
    // https://mvnrepository.com/artifact/org.tmatesoft.svnkit/svnkit
    // https://svnkit.com/download.php
    implementation(group="org.tmatesoft.svnkit", name="svnkit", version="1.10.4")

    testImplementation("junit:junit:4.13")
}