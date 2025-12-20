package com.example.tramut.userInterface.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(){

    val scrollState = rememberScrollState()

    Column(modifier = Modifier
        .verticalScroll(scrollState)
        .fillMaxWidth()) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pursuant and subject to the Malaysian Personal Data Protection Act, 2010 (PDPA, 2010), this privacy policy provides for the Tunku Abdul Rahman University of Management and Technology (TAR UMT) uses and secure any personally identifiable information that you (the User) may have given to the TAR UMT during your visit of our website herein.\n" +
                    "\n" +
                    "The TAR UMT is committed to ensure the personal data security of all Users as required under the laws in Malaysia generally and the PDPA, 2010 specifically.\n" +
                    "\n" +
                    "The Internet, being an open environment, necessitates that the TAR UMT shall not be able to guarantee and warrant that all data collected shall not be accessed, copied, disclosed, altered or otherwise tampered with.\n" +
                    "\n" +
                    "This Privacy Policy outlines the TAR UMT commitment to the safeguard and treatment of the users’ personal data pursuant to the PDPA, 2010.",
            modifier = Modifier
                .padding(horizontal = 20.dp)
        )
        Text(
            text = "\nTypes of data that may be collected\n",
            modifier = Modifier
                .padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
        Text(
            text = "Types of data collected may include (but not limited to):",
            modifier = Modifier
                .padding(horizontal = 20.dp),
            )
        Text(
            text = "\n       - Name, title, honorific, education background and " +
                    "\n         other related information\n" +
                    "       - Contact information such as mobile and phone " +
                    "\n         numbers, house address, email address and other " +
                    "\n         contact details.\n" +
                    "       - Demographic information such as race, religion, " +
                    "\n         gender, preferences and interests\n" +
                    "       - Any other information relevant to surveys and/or " +
                    "\n         those related to our products and services inquiries\n",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )
        Text(
            text = "Treatment of collected data\n",
            modifier = Modifier
                .padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
        Text(
            text = "By visiting our website, the User consents that the data collected by TAR UMT will be utilised to understand the needs and provide the User with better and more accurate service(s). The data collected will be utilized in the following manner:",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )

        Text(
            text = "\n       - Internal record keeping.\n" +
                    "       - For products and services offer and/or improvement\n" +
                    "       - TAR UMT may from time to time send promotional " +
                    "\n         materialism new or existing products & services, " +
                    "\n         special offers, events or other information that the " +
                    "\n         TAR UMT is of the view that you may find interesting " +
                    "\n         using the contact and other details which you have " +
                    "\n         provided.\n" +
                    "       - TAR UMT may also use your information to contact " +
                    "\n         you for market research and surveys purposes.\n" +
                    "       - Any other marketing and promotional activities that " +
                    "\n         the TAR UMT is of the view that you may find " +
                    "\n         interesting using the details which you have " +
                    "\n         provided.\n" +
                    "       - The TAR UMT shall not sell, distribute, lease or " +
                    "\n         otherwise disclose any data collected herein to any " +
                    "\n         other third parties save and except as outlined above " +
                    "\n         or permitted by the User or is required by law to do " +
                    "\n         so.\n",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )
        Text(
            text = "The User’s personal information may be utilised by TAR UMT to send promotional information about third parties which TAR UMT is of the view that User may find interesting if the User allows for the same.\n" +
                    "\n" +
                    "Under no circumstances will the data collected be sold or manipulated in any way and for any other commercial reasons except as disclosed herein above.",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )
        Text(
            text = "\nSecurity\n",
            modifier = Modifier
                .padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
        Text(
            text = "The TAR UMT is committed to ensuring that any personal information collected is secured. In order to prevent unauthorised access or disclosure,the TAR UMT has put in place, as the present resources and knowledge so permit, suitable and reasonable physical, electronic and processes to safeguard and secure the said information.",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )
        Text(
            text = "\nCookies\n",
            modifier = Modifier
                .padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
        Text(
            text = "The TAR UMT use traffic log cookies to identify which pages are being used. This allows for data analysis about web page traffic in order to improve the website to tailor it to market needs. The information is for statistical analysis only and shall be removed from the system thereafter. A cookie shall in no way be used to access the User’s computer or any information about the User save and except the data the User volunteered to share.",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )
        Text(
                text = "\nThe User may accept or decline cookies. Most web browsers automatically accept cookies, but may be modified to decline selected cookies. This may, however, prevent the User from taking full advantage of the website.",
        modifier = Modifier
            .padding(horizontal = 20.dp),
        )
        Text(
            text = "\nLinks to other websites\n",
            modifier = Modifier
                .padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
        Text(
            text = "The TAR UMT website may contain links to other websites of interest. Please note that the TAR UMT do not have any control over that other Third Party website. Therefore, The TAR UMT cannot be responsible for the protection and privacy of any information which the User provided whilst visiting such sites and such sites are not governed by this privacy statement.",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )
        Text(
            text = "\nControlling of personal information\n",
            modifier = Modifier
                .padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
        Text(
            text = "The User may choose to restrict the collection or use of your personal information in the following ways:\n" +
                    "\n" +
                    "           - whenever the User is asked to fill in a form on the " +
                    "\n             website, check the box provided to disallow " +
                    "\n             consent for the information to be provided to be " +
                    "\n             used by anybody for direct marketing purposes\n" +
                    "\n           - if the User has previously agreed to TAR UMT " +
                    "\n             using the personal information collected, the User " +
                    "\n             may change his mind at any time by writing to or " +
                    "\n             emailing the same to pdpa@tarc.edu.my\n" +
                    "\nThe User may request details of personal information which the TAR UMT holds about the User under the PDPA, 2010 by sending such request via email to pdpa@tarc.edu.my An administrative fee shall be payable. In the event the User believes that any information of the User envisaged herein in the TAR UMT's possession is incorrect, inaccurate and or incomplete, please write to or email the TAR UMT as soon as possible, at the above address or pdpa@tarc.edu.my for the TAR UMT's immediate and prompt action.",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )
        Text(
            text = "\nPrivacy Notice\n",
            modifier = Modifier
                .padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
        Text(
            text = "The Tunku Abdul Rahman University of Management and Technology (TAR UMT) and its Holding Company the TARC Education Foundation (collectively herein referred to as TAR UMT) is committed to ensure your personally identifiable information (personal data) be treated in accordance with the present legal and regulatory demands in force in Malaysia.\n" +
                    "\n" +
                    "Whilst the TAR UMT already has in place a privacy policy before the Malaysian Personal Data Protection Act, 2010 (PDPA, 2010) came into force; the TAR UMT, nevertheless is taking further steps to ensure compliance with the PDPA, 2010.\n" +
                    "\n" +
                    "This serves as a notice to you informing how the TAR UMT may be processing and treating your personal data. The TAR UMT processes your personal data such as name, identity card information, passport information, title, honorific, education background, records & performances and skills; contact information, mobile & phone numbers, house address, email address, mobile contacts like whatsapp, twitter, google+, viber, skype, wechat, facebook and/or linkedin; demographic information such as race, religion, gender, preferences and interests and financial background AND any other information relevant to you being a student, graduate, alumnus, staff, vendor, contractor, agent, partner or affiliate (whichever is applicable) which you have in the past provided and consented to provide and/or which personal data derived and arising out of your contract, contact/ relation with TAR UMT.\n" +
                    "\n" +
                    "Please be informed that the personal data will be processed by the TAR UMT for the following purposes (Purposes):",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )

        Text(
                text = "\n      i. Internal record keeping and maintenance. Such " +
                        "\n         process may include but not limited to updating and " +
                        "\n         managing the accuracy of the TAR UMT records.\n" +
                        "\n      ii. For the TAR UMT products and services offer and/or " +
                        "\n          improvement. TAR UMT may from time to time send " +
                        "\n          promotional materials on new or existing products & " +
                        "\n          services, special offers, events or other information " +
                        "\n          that the TAR UMT is of the view that you may find " +
                        "\n          interesting using the personal data which you have " +
                        "\n          provided.\n" +
                        "\n      iii. TAR UMT may use your personal data to " +
                        "\n            communicate with you.\n" +
                        "\n      iv. TAR UMT may also use your personal data to " +
                        "\n           contact you (for academic or market research and " +
                        "\n           surveys purposes subject always to your express " +
                        "\n           consent to participate in the same).\n" +
                        "\n      v. Any other marketing and promotional activities that " +
                        "\n           the TAR UMT is of the view that you may find " +
                        "\n           interesting using the personal data and details " +
                        "\n           which you have provided.\n" +
                        "\n      vi. Prevention, detection and prosecution of Offences " +
                        "\n            or Crimes, and compliance with legal, statutory, " +
                        "\n            regulatory and contractual obligations.\n" +
                        "\n      vii. Maintain your academic, scholastic and " +
                        "\n            disciplinary background (applicable to students, " +
                        "\n            graduates and Alumnus only).\n" +
                        "\n      viii. Maintain your academic, scholastic, employment " +
                        "\n             and disciplinary records (applicable to staff and " +
                        "\n             former staff only ).\n" +
                        "\n      ix. Protecting TAR UMT's interest and/or other " +
                        "\n            ancillary and related purposes.",
        modifier = Modifier
            .padding(horizontal = 20.dp),
        )
        Text(
            text = "\nFurther, your personal data may be disclosed to the TAR UMT's strategic partners, professional advisers such as lawyers, accountants and auditors, governmental agencies and or vendors whether within or outside Malaysia directly or via their agents, representatives or servants for the Purposes.\n" +
                    "\n" +
                    "In certain circumstances, you may have provided the TAR UMT personal data relating to others (such as spouse, parent(s), guardian(s) and/or sibling(s) for the Purposes. For thesepersonal data, you expressly warrant and represent to the TAR UMT that you have permission, consent or assent from the same to provide such personal data to the TAR UMT.\n" +
                    "\n" +
                    "We trust that you agree and consent to the above about how your personal data is processed by the TAR UMT.\n" +
                    "\n" +
                    "You may request details of your personal data which the TAR UMT holds about you under the PDPA, 2010 by sending such request via email to pdpa@tarc.edu.my and may also request for the correction, deletion and update of your personal data in the TAR UMT's possession, which is incorrect, inaccurate and or incomplete provided always that your personal data under items (vii) and (viii) above shall not be applicable to this statement herein.\n" +
                    "\n" +
                    "An administrative fee shall be payable.\n" +
                    "\n" +
                    "The TAR UMT reserves the absolute right and discretion to alter, change, add, subtract or otherwise modify this notice and/or the privacy policy.",
            modifier = Modifier
                .padding(horizontal = 20.dp),
        )
        Text(
            text = "\n\n.",
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .align (Alignment.CenterHorizontally),
        )
    }
}


@Preview (showBackground = true, showSystemUi = true)
@Composable
fun Pr() {
    PrivacyPolicyScreen()
}
