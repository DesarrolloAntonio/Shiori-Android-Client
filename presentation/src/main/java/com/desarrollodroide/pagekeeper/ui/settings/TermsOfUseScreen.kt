package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.runtime.Composable

@Composable
fun TermsOfUseScreen(
    onBack: () -> Unit
) {
    LegalDocumentScreen(
        title = "Terms of Use",
        sections = TermsOfUseSections,
        onBack = onBack,
    )
}

private val TermsOfUseSections = listOf(
        LegalSection(
            heading = "1. Acceptance of Terms",
            body = "By accessing and using Shiori, you agree to be bound by these Terms of Use.",
        ),
        LegalSection(
            heading = "2. License for the App",
            body = "Shiori is provided under the Apache 2.0 License, allowing personal and commercial use, redistribution, and modification under the terms specified in the LICENSE file included with the source code.",
        ),
        LegalSection(
            heading = "3. Disclaimer",
            body = "Shiori is provided 'as is', without any warranties, expressed or implied, including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose.",
        ),
        LegalSection(
            heading = "4. Limitations of Liability",
            body = "In no event shall Shiori, or its contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.",
        ),
        LegalSection(
            heading = "5. Modifications to Terms",
            body = "We may revise these terms of use for Shiori at any time without notice. By using this app, you are agreeing to be bound by the then current version of these terms of use.",
        ),
        LegalSection(
            heading = "6. Governing Law",
            body = "Any claim relating to Shiori shall be governed by the laws of the app owner's jurisdiction without regard to its conflict of law provisions.",
        ),
)
