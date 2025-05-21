package com.demo.resumebuilder
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Build : AppCompatActivity() {
    // PDF page constants
    private val pageWidth = 8.5f * 72  // 8.5 inches in points (72 points per inch)
    private val pageHeight = 11f * 72  // 11 inches in points
    private val margin = 50f  // margin in points

    // Add this property to your class for notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
            Toast.makeText(
                this,
                "Notification permission denied. You won't receive notifications about generated PDFs.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Add this property for storage permission
//    private val requestStoragePermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            // Permission granted
//            Toast.makeText(this, "Storage permission granted. You can now generate PDFs.", Toast.LENGTH_SHORT).show()
//        } else {
//            // Permission denied
//            Toast.makeText(this, "Storage permission denied. Cannot save PDF files.", Toast.LENGTH_LONG).show()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_build)

        // Create notification channel
        createNotificationChannel()

        // Check permissions
        checkNotificationPermission()
        checkStoragePermission()

        val generateButton = findViewById<MaterialButton>(R.id.generateButton)
        val educationContainer = findViewById<LinearLayout>(R.id.educationContainer)
        val addEducationButton = findViewById<MaterialButton>(R.id.addEducationButton)
        val projectsContainer = findViewById<LinearLayout>(R.id.projectsContainer)
        val addProjectButton = findViewById<MaterialButton>(R.id.addProjectButton)
        val workExperienceContainer = findViewById<LinearLayout>(R.id.workExperienceContainer)
        val addWorkExperienceButton = findViewById<MaterialButton>(R.id.addWorkExperienceButton)
        val achievementsContainer = findViewById<LinearLayout>(R.id.achievementsContainer)
        val addAchievementsButton = findViewById<MaterialButton>(R.id.addAchievementButton)

        addEducationButton.setOnClickListener {
            val newEducationView = layoutInflater.inflate(R.layout.item_education, educationContainer, false)
            educationContainer.addView(newEducationView)
        }
        addProjectButton.setOnClickListener {
            val newProjectView = layoutInflater.inflate(R.layout.item_project, projectsContainer, false)
            projectsContainer.addView(newProjectView)
        }
        addWorkExperienceButton.setOnClickListener {
            val newWorkExperienceView = layoutInflater.inflate(R.layout.item_work_experience, workExperienceContainer, false)
            workExperienceContainer.addView(newWorkExperienceView)
        }
        addAchievementsButton.setOnClickListener {
            val newAchievementView = layoutInflater.inflate(R.layout.item_achievement, achievementsContainer, false)
            achievementsContainer.addView(newAchievementView)
        }

        generateButton.setOnClickListener {
            // First check if we have storage permission
            if (checkStoragePermission()) {
                try {
                    val name = findViewById<TextInputEditText>(R.id.nameInput).text.toString()
                    val phone = findViewById<TextInputEditText>(R.id.phoneInput).text.toString()
                    val email = findViewById<TextInputEditText>(R.id.emailInput).text.toString()

                    val linkedinUsername = findViewById<TextInputEditText>(R.id.linkedinUsernameInput).text.toString()
                    val linkedinUrl = findViewById<TextInputEditText>(R.id.linkedinUrlInput).text.toString()
                    val githubUsername = findViewById<TextInputEditText>(R.id.githubUsernameInput).text.toString()
                    val githubUrl = findViewById<TextInputEditText>(R.id.githubUrlInput).text.toString()
                    val twitterUsername = findViewById<TextInputEditText>(R.id.twitterUsernameInput).text.toString()
                    val twitterUrl = findViewById<TextInputEditText>(R.id.twitterUrlInput).text.toString()

                    val district = findViewById<TextInputEditText>(R.id.districtInput).text.toString()
                    val state = findViewById<TextInputEditText>(R.id.stateInput).text.toString()
                    val pincode = findViewById<TextInputEditText>(R.id.pincodeInput).text.toString()
                    val country = findViewById<TextInputEditText>(R.id.countryInput).text.toString()

                    val programmingLanguages = findViewById<TextInputEditText>(R.id.programmingLanguagesInput)?.text.toString()
                    val webTechnologies = findViewById<TextInputEditText>(R.id.webTechInput)?.text.toString()
                    val databaseSystems = findViewById<TextInputEditText>(R.id.databasesInput)?.text.toString()
                    val otherToolsAndTechnologies = findViewById<TextInputEditText>(R.id.otherToolsInput)?.text.toString()

                    if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                        Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val educationList = mutableListOf<Map<String, String>>()
                    for (i in 0 until educationContainer.childCount) {
                        val educationView = educationContainer.getChildAt(i) as? ViewGroup
                        if (educationView != null) {
                            val collegeName = educationView.findViewById<TextInputEditText>(R.id.undergradCollegeInput)?.text.toString()
                            val major = educationView.findViewById<TextInputEditText>(R.id.undergradMajorInput)?.text.toString()
                            val cgpa = educationView.findViewById<TextInputEditText>(R.id.undergradCgpaInput)?.text.toString()
                            val startYear = educationView.findViewById<TextInputEditText>(R.id.undergradStartYearInput)?.text.toString()
                            val endYear = educationView.findViewById<TextInputEditText>(R.id.undergradEndYearInput)?.text.toString()
                            val location = educationView.findViewById<TextInputEditText>(R.id.undergradLocationInput)?.text.toString() ?: ""
                            educationList.add(mapOf(
                                "collegeName" to collegeName,
                                "degree" to major,
                                "startYear" to startYear,
                                "endYear" to endYear,
                                "cgpa" to cgpa,
                                "location" to location
                            ))
                        }
                    }

                    val projectList = mutableListOf<Map<String, String>>()
                    for (i in 0 until projectsContainer.childCount) {
                        val projectView = projectsContainer.getChildAt(i) as? ViewGroup
                        if (projectView != null) {
                            val domain = projectView.findViewById<TextInputEditText>(R.id.projectDomainInput)?.text.toString()
                            val tools = projectView.findViewById<TextInputEditText>(R.id.projectToolsInput)?.text.toString()
                            val features = projectView.findViewById<TextInputEditText>(R.id.projectFeaturesInput)?.text.toString()
                            projectList.add(mapOf("domain" to domain, "tools" to tools, "features" to features))
                        }
                    }

                    val experienceList = mutableListOf<Map<String, String>>()
                    for (i in 0 until workExperienceContainer.childCount) {
                        val experienceView = workExperienceContainer.getChildAt(i) as? ViewGroup
                        if (experienceView != null) {
                            val companyName = experienceView.findViewById<TextInputEditText>(R.id.companyNameInput)?.text.toString()
                            val companyLink = experienceView.findViewById<TextInputEditText>(R.id.companyLinkInput)?.text.toString()
                            val location = experienceView.findViewById<TextInputEditText>(R.id.locationInput)?.text.toString()
                            val jobTitle = experienceView.findViewById<TextInputEditText>(R.id.positionInput)?.text.toString()
                            val startYear = experienceView.findViewById<TextInputEditText>(R.id.workExperienceStartYearInput)?.text.toString()
                            val endYear = experienceView.findViewById<TextInputEditText>(R.id.workExperienceEndYearInput)?.text.toString()
                            val responsibilities = experienceView.findViewById<TextInputEditText>(R.id.workExperienceDetailsInput)?.text.toString()
                            experienceList.add(mapOf(
                                "companyName" to companyName,
                                "companyLink" to companyLink,
                                "location" to location,
                                "jobTitle" to jobTitle,
                                "startYear" to startYear,
                                "endYear" to endYear,
                                "responsibilities" to responsibilities
                            ))
                        }
                    }

                    val achievementList = mutableListOf<Map<String, String>>()
                    for (i in 0 until achievementsContainer.childCount) {
                        val achievementView = achievementsContainer.getChildAt(i) as? ViewGroup
                        if (achievementView != null) {
                            val domain = achievementView.findViewById<TextInputEditText>(R.id.achievementDomainInput)?.text.toString()
                            val description = achievementView.findViewById<TextInputEditText>(R.id.achievementDescriptionInput)?.text.toString()
                            val proofLink = achievementView.findViewById<TextInputEditText>(R.id.achievementLinkInput)?.text.toString()
                            achievementList.add(mapOf("domain" to domain, "description" to description, "proofLink" to proofLink))
                        }
                    }

                    generatePdf(
                        name,
                        phone,
                        email,
                        linkedinUsername,
                        linkedinUrl,
                        githubUsername,
                        githubUrl,
                        twitterUsername,
                        twitterUrl,
                        district,
                        state,
                        pincode,
                        country,
                        programmingLanguages,
                        webTechnologies,
                        databaseSystems,
                        otherToolsAndTechnologies,
                        educationList,
                        projectList,
                        experienceList,
                        achievementList
                    )
                } catch (e: Exception) {
                    Toast.makeText(this, "Error generating resume: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

//    // Add this method to check and request storage permission
//    private fun checkStoragePermission(): Boolean {
//        return if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            true
//        } else {
//            requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            false
//        }
//    }
    private fun checkStoragePermission(): Boolean {
        return true // No need for storage permission in Android 10+
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "resume_builder_channel"
            val channelName = "Resume Builder"
            val descriptionText = "Notifications for generated resume PDFs"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show rationale if needed
                    Toast.makeText(
                        this,
                        "Notification permission is needed to alert you when your PDF is ready",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showPdfNotification(pdfFile: File) {
        val channelId = "resume_builder_channel"
        val notificationId = System.currentTimeMillis().toInt() // Use timestamp for unique notification ID

        // Create content URI for the PDF using FileProvider
        val pdfUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            pdfFile
        )

        // Create an intent to view the PDF
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Create pending intent for notification
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            viewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Make sure you have this icon in your drawable resources
            .setContentTitle("Resume PDF Generated")
            .setContentText("Tap to view your resume: ${pdfFile.name}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun generatePdf(
        name: String,
        phone: String,
        email: String,
        linkedinUsername: String,
        linkedinUrl: String,
        githubUsername: String,
        githubUrl: String,
        twitterUsername: String,
        twitterUrl: String,
        district: String,
        state: String,
        pincode: String,
        country: String,
        programmingLanguages: String,
        webTechnologies: String,
        databaseSystems: String,
        otherToolsAndTechnologies: String,
        educationList: List<Map<String, String>>,
        projectList: List<Map<String, String>>,
        experienceList: List<Map<String, String>>,
        achievementList: List<Map<String, String>>
    ) {
        // Create a PDF document
        val document = PdfDocument()

        // Start with page 1
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // For tracking current Y position on page
        var currentY = margin

        // Define text paints
        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headingPaint = TextPaint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subheadingPaint = TextPaint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val normalPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val smallPaint = TextPaint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(150, 150, 150)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // Function to check if we need a new page
        fun checkAndCreateNewPageIfNeeded(neededSpace: Float): Boolean {
            if (currentY + neededSpace > pageHeight - margin) {
                // Finish current page
                document.finishPage(page)

                // Create new page
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
                return true
            }
            return false
        }

        // Function to add text with specific paint
        fun addText(text: String, paint: TextPaint, x: Float, maxWidth: Float): Float {
            val textLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, maxWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()

            checkAndCreateNewPageIfNeeded(textLayout.height.toFloat())

            canvas.save()
            canvas.translate(x, currentY)
            textLayout.draw(canvas)
            canvas.restore()

            currentY += textLayout.height + 10 // Add some spacing
            return textLayout.height.toFloat()
        }

        // Function to add a section heading
        fun addSectionHeading(heading: String) {
            checkAndCreateNewPageIfNeeded(50f)
            addText(heading, headingPaint, margin, pageWidth - margin * 2)
            canvas.drawLine(margin, currentY - 5, pageWidth - margin, currentY - 5, linePaint)
            currentY += 10 // Add spacing after line
        }

        // NAME AND CONTACT INFO
        // ---------------------
        val nameWidth = titlePaint.measureText(name)
        canvas.drawText(name, (pageWidth - nameWidth) / 2, currentY + 24, titlePaint)
        currentY += 40 // Name + spacing

        // Contact info
        val contactInfo = "$phone | $email"
        val contactWidth = normalPaint.measureText(contactInfo)
        canvas.drawText(contactInfo, (pageWidth - contactWidth) / 2, currentY + 12, normalPaint)
        currentY += 20

        // Social links
        var socialLinks = ""
        if (linkedinUsername.isNotEmpty()) {
            socialLinks += "$linkedinUsername"
            if (githubUsername.isNotEmpty() || twitterUsername.isNotEmpty()) socialLinks += " | "
        }
        if (githubUsername.isNotEmpty()) {
            socialLinks += "$githubUsername"
            if (twitterUsername.isNotEmpty()) socialLinks += " | "
        }
        if (twitterUsername.isNotEmpty()) {
            socialLinks += "$twitterUsername"
        }

        if (socialLinks.isNotEmpty()) {
            val socialWidth = normalPaint.measureText(socialLinks)
            canvas.drawText(socialLinks, (pageWidth - socialWidth) / 2, currentY + 12, normalPaint)
            currentY += 20
        }

        // Address
        val address = "$district, $state - $pincode, $country"
        val addressWidth = normalPaint.measureText(address)
        canvas.drawText(address, (pageWidth - addressWidth) / 2, currentY + 12, normalPaint)
        currentY += 30 // Extra spacing after header

        // OBJECTIVE
        // ---------
        addSectionHeading("OBJECTIVE")
        addText(
            "Seeking a challenging position in Data Science / Software Engineering to leverage my expertise in those fields. " +
                    "Aiming to contribute to innovative projects at the intersection of Software Engineering and practical problem-solving in fields such as App/Web development and Data Science.",
            normalPaint,
            margin,
            pageWidth - margin * 2
        )
        currentY += 10 // Extra spacing after section

        // EXPERIENCE
        // ----------
        if (experienceList.isNotEmpty()) {
            addSectionHeading("EXPERIENCE")

            for (experience in experienceList) {
                checkAndCreateNewPageIfNeeded(100f) // Estimate space needed

                // Company name and position
                addText(experience["companyName"] ?: "", subheadingPaint, margin, pageWidth - margin * 2)

                // Job title, location, and date
                val jobInfo = "${experience["jobTitle"]} | ${experience["location"]} | ${experience["startYear"]} - ${experience["endYear"]}"
                addText(jobInfo, smallPaint, margin, pageWidth - margin * 2)

                // Responsibilities
                val responsibilities = experience["responsibilities"]?.split("\n") ?: emptyList()
                if (responsibilities.isNotEmpty()) {
                    currentY += 5 // Add a bit of space

                    for (responsibility in responsibilities) {
                        if (responsibility.isNotEmpty()) {
                            checkAndCreateNewPageIfNeeded(30f)

                            // Draw bullet
                            canvas.drawCircle(margin + 4, currentY + 4, 2f, normalPaint)

                            // Draw text after bullet
                            addText(responsibility, normalPaint, margin + 15, pageWidth - margin * 2 - 15)
                        }
                    }
                }

                currentY += 15 // Extra spacing between experiences
            }

            currentY += 10 // Extra spacing after section
        }

        // EDUCATION
        // ---------
        if (educationList.isNotEmpty()) {
            addSectionHeading("EDUCATION")

            for (education in educationList) {
                checkAndCreateNewPageIfNeeded(80f) // Estimate space needed

                // School name
                addText(education["collegeName"] ?: "", subheadingPaint, margin, pageWidth - margin * 2)

                // Degree and date
                val eduInfo = "${education["degree"]} | ${education["startYear"]} - ${education["endYear"]}"
                addText(eduInfo, smallPaint, margin, pageWidth - margin * 2)

                // Location and CGPA
                var extraInfo = ""
                if (education["location"]?.isNotEmpty() == true) {
                    extraInfo += education["location"]
                }
                if (education["cgpa"]?.isNotEmpty() == true) {
                    if (extraInfo.isNotEmpty()) extraInfo += " | "
                    extraInfo += "CGPA: ${education["cgpa"]}"
                }

                if (extraInfo.isNotEmpty()) {
                    addText(extraInfo, normalPaint, margin, pageWidth - margin * 2)
                }

                currentY += 15 // Extra spacing between education entries
            }

            currentY += 10 // Extra spacing after section
        }

        // PROJECTS
        // --------
        if (projectList.isNotEmpty()) {
            addSectionHeading("PROJECTS")

            for (project in projectList) {
                checkAndCreateNewPageIfNeeded(80f) // Estimate space needed

                // Project name
                addText(project["domain"] ?: "", subheadingPaint, margin, pageWidth - margin * 2)

                // Technologies
                addText("Technologies: ${project["tools"]}", smallPaint, margin, pageWidth - margin * 2)

                // Features/Description
                addText(project["features"] ?: "", normalPaint, margin, pageWidth - margin * 2)

                currentY += 15 // Extra spacing between projects
            }

            currentY += 10 // Extra spacing after section
        }

        // SKILLS
        // ------
        addSectionHeading("SKILLS")

        if (programmingLanguages.isNotEmpty()) {
            addText("Programming Languages: $programmingLanguages", normalPaint, margin, pageWidth - margin * 2)
        }

        if (webTechnologies.isNotEmpty()) {
            addText("Web Technologies: $webTechnologies", normalPaint, margin, pageWidth - margin * 2)
        }

        if (databaseSystems.isNotEmpty()) {
            addText("Database Systems: $databaseSystems", normalPaint, margin, pageWidth - margin * 2)
        }

        if (otherToolsAndTechnologies.isNotEmpty()) {
            addText("Other Tools & Technologies: $otherToolsAndTechnologies", normalPaint, margin, pageWidth - margin * 2)
        }

        currentY += 10 // Extra spacing after section

        // ACHIEVEMENTS
        // ------------
        if (achievementList.isNotEmpty()) {
            addSectionHeading("ACHIEVEMENTS")

            for (achievement in achievementList) {
                checkAndCreateNewPageIfNeeded(50f) // Estimate space needed

                // Achievement name
                addText(achievement["domain"] ?: "", subheadingPaint, margin, pageWidth - margin * 2)

                // Description
                addText(achievement["description"] ?: "", normalPaint, margin, pageWidth - margin * 2)

                currentY += 15 // Extra spacing between achievements
            }
        }

        // Finish the last page
        document.finishPage(page)

        // Write the PDF file
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            // Format the current date and time for the filename
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val dateTime = dateFormat.format(Date())

            // Create a filename with the date and user's name
            val sanitizedName = name.replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
            val fileName = "resume_${sanitizedName}_$dateTime.pdf"
            val pdfFile = File(downloadDir, fileName)

            FileOutputStream(pdfFile).use { output ->
                document.writeTo(output)
            }

            // Show notification with the PDF
            showPdfNotification(pdfFile)

            Toast.makeText(this, "PDF saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            document.close()
        }
    }
}