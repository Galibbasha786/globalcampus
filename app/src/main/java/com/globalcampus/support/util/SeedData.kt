package com.globalcampus.support.util

import com.globalcampus.support.data.model.Article

object SeedData {

    val articles: List<Article> = listOf(
        Article(title = "How to Apply for a Student Visa Extension",
            category = "Visa",
            tags = listOf("visa", "immigration", "documents"),
            body = "1. Contact your university's international office at least 60 days before expiry.\n" +
                    "2. Prepare: passport, current visa, enrollment letter, proof of funds, insurance.\n" +
                    "3. Submit online through your country's immigration portal.\n" +
                    "4. Pay the fee and book a biometrics appointment if required.\n" +
                    "5. Keep the receipt — it proves legal status while processing."),

        Article(title = "Opening a Local Bank Account as an International Student",
            category = "Banking",
            tags = listOf("banking", "money", "finance"),
            body = "You'll typically need: passport, student ID, proof of address (lease/dorm letter), and enrollment letter. " +
                    "Most banks offer a free student account. Ask for: no monthly fee, free international transfers, and a debit card. " +
                    "Popular options: local credit unions, HSBC, Chase, Deutsche Bank (varies by country). " +
                    "Open it in the first week — many landlords and employers require a local account."),

        Article(title = "Finding Student Housing: On-Campus vs Off-Campus",
            category = "Housing",
            tags = listOf("housing", "accommodation", "rent"),
            body = "ON-CAMPUS: convenient, safe, pre-furnished, easy to meet people. Usually 20–40% more expensive.\n" +
                    "OFF-CAMPUS: cheaper, more independence, but you handle utilities and commute.\n" +
                    "TIPS: Never pay a deposit before viewing (or video tour). Use university-approved listings. " +
                    "Check commute time, not just distance. Read the lease — ask about early termination."),

        Article(title = "Registering with a Doctor / Health Insurance",
            category = "Health",
            tags = listOf("health", "insurance", "medical"),
            body = "Most countries require international students to have health insurance — often included in tuition. " +
                    "Steps:\n1. Confirm your university's insurance plan (or buy one).\n" +
                    "2. Register with a local GP/clinic near campus.\n" +
                    "3. Save the emergency number.\n" +
                    "4. Keep your insurance card with your student ID at all times."),

        Article(title = "Getting a Student ID: Everything You Need",
            category = "Campus",
            tags = listOf("campus", "id", "student"),
            body = "Your student ID unlocks: library access, gym, discounts, public transport passes, and building entry. " +
                    "Visit the student services office with your passport + enrollment letter + admission letter. " +
                    "Photos are usually taken on-site. Card prints in 5–10 minutes. Report lost IDs within 24 hours — " +
                    "replacement fee is typically $20–$50."),

        Article(title = "Navigating Public Transport with a Student Discount",
            category = "Transport",
            tags = listOf("transport", "discount", "bus", "metro"),
            body = "Most cities offer 30–70% off public transport for students. You usually need: " +
                    "student ID + enrollment proof + sometimes a passport photo. Apply at the transit authority " +
                    "or online. In many cities the student pass includes bus, tram, metro, and sometimes regional trains. " +
                    "Buy monthly, not daily — saves 40%+."),

        Article(title = "How to Get a Part-Time Job on a Student Visa",
            category = "Work",
            tags = listOf("work", "job", "visa"),
            body = "Most student visas allow 20 hours/week during term and full-time in holidays. CHECK your visa conditions first. " +
                    "On-campus jobs (library, café, admin) are easiest. Off-campus requires a work permit in some countries. " +
                    "You'll need: tax ID, bank account, and sometimes a police check. Start with the university career office — " +
                    "they know the legal limits."),

        Article(title = "Budgeting 101: Monthly Costs for International Students",
            category = "Finance",
            tags = listOf("budget", "money", "finance"),
            body = "Typical monthly breakdown (varies by city):\n• Rent: 40–55%\n• Food: 15–20%\n• Transport: 5–10%\n" +
                    "• Phone/internet: 2–5%\n• Books/supplies: 3–5%\n• Fun/misc: 10–15%\n" +
                    "Use a budgeting app (YNAB, Mint, or a spreadsheet). Track for one month — you'll be surprised where money goes. " +
                    "Cook at home 5 days/week = save $100–$200/month."),

        Article(title = "Making Friends as a New International Student",
            category = "Social",
            tags = listOf("social", "friends", "community"),
            body = "1. Join 2–3 clubs in the first 2 weeks — everyone is looking for friends.\n" +
                    "2. Attend international student orientation (mandatory but also social).\n" +
                    "3. Say yes to every invitation for the first month.\n" +
                    "4. Study groups form fast — join one.\n" +
                    "5. Don't only hang out with people from your country — mix.\n" +
                    "6. Homesickness is normal for 2–3 weeks; it passes."),

        Article(title = "Understanding Your University Email and Portal",
            category = "Campus",
            tags = listOf("campus", "email", "portal"),
            body = "Your university email is your official identity — check daily. " +
                    "The student portal handles: course registration, grades, tuition, library, and IT support. " +
                    "Set up forwarding to your personal email AND enable 2FA. " +
                    "Bookmark: portal, library, email, and the international office page."),

        Article(title = "Emergency Numbers Every International Student Should Save",
            category = "Safety",
            tags = listOf("safety", "emergency", "help"),
            body = "Save these in your phone:\n• Local emergency: 112 (EU), 911 (US/CA), 999 (UK)\n" +
                    "• University security (24/7): check your ID card\n" +
                    "• International office: get direct number\n" +
                    "• Your country's embassy in the host country\n" +
                    "• Health insurance emergency line\n" +
                    "• A trusted local friend\n" +
                    "In any crisis, call university security first — they know the local system best."),

        Article(title = "Cheap Eats: Grocery Shopping on a Student Budget",
            category = "Finance",
            tags = listOf("food", "budget", "shopping"),
            body = "Shop at discount chains (Aldi, Lidl, Trader Joe's equivalents). Buy: rice, pasta, eggs, " +
                    "frozen vegetables, beans, oats, bananas, chicken thighs. Cook 3 meals in one session (meal prep Sunday). " +
                    "Use student discount apps (UNiDAYS, Student Beans). Avoid campus cafés — 3× markup. " +
                    "Target: $40–$60/week for food."),

        Article(title = "How to Transfer Money Internationally Without Huge Fees",
            category = "Banking",
            tags = listOf("money", "banking", "transfer"),
            body = "AVOID: bank wires (3–5% + $25 flat fee).\nUSE: Wise (formerly TransferWise), Revolut, or Remitly. " +
                    "Fees typically 0.5–1%. Send larger amounts less often. " +
                    "Set up a local account first, then transfer from home country to local. " +
                    "Some universities accept international card payments for tuition — check before wiring."),

        Article(title = "Cultural Adjustment: What Nobody Tells You",
            category = "Social",
            tags = listOf("culture", "adjustment", "mental health"),
            body = "You'll go through 4 stages: honeymoon → frustration → adjustment → adaptation. " +
                    "Frustration is normal around weeks 3–8. Symptoms: irritability, missing home, feeling out of place. " +
                    "What helps: routine, exercise, one local friend, calling home weekly (not daily), and joining a club. " +
                    "If it lasts more than 6 weeks, talk to your university counseling service — it's free and confidential."),

        Article(title = "Setting Up a Local Phone Number: Prepaid vs Contract",
            category = "Transport",
            tags = listOf("phone", "sim", "mobile"),
            body = "PREPAID: no credit check, flexible, pay-as-you-go. Best for your first 3 months. " +
                    "CONTRACT: cheaper monthly, requires bank account + local ID. Wait 3 months before switching. " +
                    "You'll need: passport + student ID + proof of address. " +
                    "Popular: Vodafone, Orange, T-Mobile, Google Fi. " +
                    "Bring your old phone unlocked — most are fine."),

        Article(title = "Understanding Your Course Syllabus and Grading",
            category = "Campus",
            tags = listOf("campus", "academic", "grades"),
            body = "The syllabus is your contract — read it fully in week 1. Note: assignment weights, " +
                    "late penalty policy, attendance rules, exam format, office hours. " +
                    "Grades often use different scales: US GPA (A=4.0), UK (First/2:1/2:2), EU (1–5). " +
                    "Ask your professor: 'What does an A look like on this assignment?' — they'll tell you."),

        Article(title = "Part-Time Work Rules: Hours, Taxes, and Paperwork",
            category = "Work",
            tags = listOf("work", "tax", "visa"),
            body = "Before starting ANY job:\n1. Confirm visa work limit (usually 20 hrs/week).\n" +
                    "2. Get a tax ID number (called SSN, NIN, TFN depending on country).\n" +
                    "3. Open a local bank account for salary.\n" +
                    "4. Fill the university's work authorization form.\n" +
                    "5. Keep pay slips — needed for tax refunds.\n" +
                    "Tax refunds: usually file by April–June; students often get 100% back."),

        Article(title = "Mental Health Support: You're Not Alone",
            category = "Health",
            tags = listOf("mental health", "counseling", "support"),
            body = "Every university offers free counseling to students — use it. It's confidential and doesn't affect your record. " +
                    "Signs to reach out: persistent sadness >2 weeks, sleep changes, loss of interest, " +
                    "feeling overwhelmed, thoughts of self-harm. " +
                    "Other options: 24/7 crisis lines, international student support groups, apps like Calm/Headspace (student discount). " +
                    "Asking for help is a strength, not a weakness."),

        Article(title = "Traveling Home for the Holidays: Checklist",
            category = "Travel",
            tags = listOf("travel", "holiday", "visa"),
            body = "Before you book:\n1. Check visa re-entry rules — some single-entry visas need renewal.\n" +
                    "2. Get an enrollment letter from your university (proof you're returning).\n" +
                    "3. Book flights 6–8 weeks early.\n" +
                    "4. Arrange housing (sublet or keep lease — don't break it).\n" +
                    "5. Notify your landlord and university of travel dates.\n" +
                    "6. Pack documents in carry-on: passport, visa, enrollment letter, insurance."),

        Article(title = "Building a Professional Network While Studying",
            category = "Work",
            tags = listOf("career", "networking", "linkedin"),
            body = "1. Create a LinkedIn profile in week 1 — update it as you go.\n" +
                    "2. Attend 1 career event per month.\n" +
                    "3. Join student chapters of professional associations.\n" +
                    "4. Connect with alumni from your home country in your field.\n" +
                    "5. Volunteer for a role in a club — leadership looks great.\n" +
                    "6. Keep in touch: 1 message per quarter to your top 10 contacts.\n" +
                    "Your network will matter more than your GPA for your first job.")
    )
}