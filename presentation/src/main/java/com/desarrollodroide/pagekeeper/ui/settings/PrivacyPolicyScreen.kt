package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.runtime.Composable

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    LegalDocumentScreen(
        title = "Privacy policy",
        sections = PrivacyPolicySections,
        onBack = onBack,
    )
}

private val PrivacyPolicySections = listOf(
        LegalSection(
            heading = "Privacy Policy of Shiori",
            body = "This Privacy Policy outlines our policies and procedures on the collection, use, and disclosure of personal information. Shiori values your privacy and is committed to protecting it through our compliance with this policy. Our app is designed not to collect or store any personal data from our users, ensuring your privacy and security.",
        ),
        LegalSection(
            heading = "Data Collection and Use",
            body = "As a commitment to your privacy, Shiori does not gather, store, or process any personal data. This includes but is not limited to personal identifiers, contact details, usage data, and location information.",
        ),
        LegalSection(
            heading = "Third-Party Services",
            body = "Shiori does not share any personal data with third parties as no personal data is collected. However, users should be aware that third-party services used by the app may collect their own data.",
        ),
        LegalSection(
            heading = "Security",
            body = "The security of your personal information is important to us. As we do not collect personal data, there is no risk of your personal information being accessed from our app.",
        ),
        LegalSection(
            heading = "Changes to This Privacy Policy",
            body = "We may update our Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the 'Effective as of' date at the top of this policy. We may also provide notice to you in other ways in our discretion, such as through contact information you have provided.",
        ),
        LegalSection(
            heading = "Contact Us",
            body = "For questions or concerns about this Privacy Policy, please contact us via email at desarrollodroide@gmail.com or through other communication channels as provided in our app.",
        ),
)
