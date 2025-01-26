dependencies {
    implementation(platform(libs.google.firebase.bom))

    // Thư viện Firebase Authentication
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.database)

    // Authentication with Credential Manager
    implementation(libs.play.services.auth)

    // UI homeScreen
    implementation(libs.circleimageview)
    implementation(libs.glide)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
