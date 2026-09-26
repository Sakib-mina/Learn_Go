# 🎓 Learn Go - Client Application & Internal Technical Documentation

[![Access Status](https://img.shields.io/badge/Access-Private%20%26%20Confidential-red.svg?style=flat&logo=lock)](https://github.com/)
[![License](https://img.shields.io/badge/License-Proprietary-darkred.svg?style=flat)](https://github.com/)
[![Android SDK](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat&logo=android)](https://developer.android.com/about/versions/nougat)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-orange.svg?style=flat)](https://developer.android.com/topic/architecture)

> [!CAUTION]
> **PROPRIETARY & STRICTLY CONFIDENTIAL CLIENT PROJECT**
> 
> This repository contains proprietary code, database schema, design assets, and intellectual property developed exclusively for the Client (**Nova Mind Labs**). 
> 
> - **UNAUTHORIZED ACCESS IS STRICTLY PROHIBITED.**
> - **NO PUBLIC PERMISSION**: No part of this repository may be cloned, reproduced, redistributed, mirrored, sublicensed, or reverse-engineered in any form or by any means.
> - **NON-DISCLOSURE AGREEMENT (NDA)**: Access to this code base is strictly restricted to authorized developers and project administrators under NDA.

---

## 🔒 Confidentiality & Intellectual Property Notice

| Property Attribute | Details |
|---|---|
| **Project Name** | Learn Go (LearnGo) |
| **Package Identifier** | `com.novamindlabs.learngo` |
| **Ownership** | **Nova Mind Labs** (All Rights Reserved) |
| **Project Status** | Private Client Production Project |
| **Licensing** | **Strictly Proprietary** (Not Open Source) |

---

## 🌟 Application System Architecture

```mermaid
mindmap
  root((Learn Go System))
    Authentication
      Email & Password
      Google Credentials API
      Session Persistence
    Gamification Engine
      Daily Task Progress (0/5)
      Streak Counter
      Virtual Coin System
    Quiz Categories Matrix
      10 Free Categories
      9 Premium Unlockable Categories
      Quiz Timer Engine
    Wallet & Monetization
      In-App Purchases
      Permanent Category Unlocks
    Client Management
      User Profile
      Customer Support & FAQ
```

---

## 🚀 Internal Application Flow

```mermaid
graph TD
    A[Splash Screen] --> B{User Authenticated?}
    B -- No --> C[Onboarding / Auth Flow]
    C --> D[Login Fragment]
    C --> E[Sign Up Fragment]
    D --> F[Dashboard Activity]
    E --> F
    B -- Yes --> F

    subgraph Internal Dashboard Layout
        F --> G[Home Fragment]
        F --> H[All Quizzes Fragment]
        F --> I[Wallet Fragment]
        F --> J[Profile Fragment]
    end

    G -- Select Category --> K{Category Unlocked?}
    H -- Select Category --> K
    K -- Yes --> L[Quiz Test Fragment]
    K -- No --> M[20 Coin Unlock Modal]
    M -- Confirm --> N[Atomic Firebase Coin Deduction]
    N --> L
    L -- Finish Quiz --> O[Result Fragment]
    O --> G
```

---

## 📚 Configured Category Matrix (19 Categories)

| # | Category Name (বাংলা) | Internal Category ID | Question Count | Access Tier | Coin Cost |
|---|---|---|---|---|---|
| 1 | **ইসলামিক কুইজ** | `Islamic` | 20 Questions | `Free` | 0 Coins |
| 2 | **হাদিসের গল্প** | `Hadis_Quiz` | 20 Questions | `Premium` | 20 Coins |
| 3 | **নবীদের জীবনী** | `Prophets_Quiz` | 20 Questions | `Free` | 0 Coins |
| 4 | **কুরআন কুইজ** | `Quran_Quiz` | 20 Questions | `Premium` | 20 Coins |
| 5 | **নামাজ শিক্ষা** | `Namaj_Quiz` | 20 Questions | `Free` | 0 Coins |
| 6 | **সাধারণ জ্ঞান** | `GK` | 20 Questions | `Premium` | 20 Coins |
| 7 | **বিজ্ঞান ও প্রযুক্তি** | `Science` | 20 Questions | `Premium` | 20 Coins |
| 8 | **ইতিহাসের পাতা** | `History` | 20 Questions | `Free` | 0 Coins |
| 9 | **খেলাধুলা** | `Sports` | 20 Questions | `Free` | 0 Coins |
| 10 | **ভূগোল কুইজ** | `Geography` | 20 Questions | `Premium` | 20 Coins |
| 11 | **বাংলাদেশের ইতিহাস** | `BD_History` | 20 Questions | `Free` | 0 Coins |
| 12 | **বিশ্ব ও দেশ** | `World_Country` | 20 Questions | `Premium` | 20 Coins |
| 13 | **সাহাবীদের জীবন** | `Sahaba_Life` | 20 Questions | `Free` | 0 Coins |
| 14 | **ইসলামের ইতিহাস** | `Islamic_History` | 20 Questions | `Premium` | 20 Coins |
| 15 | **ধাঁধা ও বুদ্ধির খেলা** | `Riddles` | 20 Questions | `Free` | 0 Coins |
| 16 | **ইংরেজি ভাষা** | `English_Lang` | 20 Questions | `Premium` | 20 Coins |
| 17 | **কম্পিউটার ও আইটি** | `Computer_IT` | 20 Questions | `Free` | 0 Coins |
| 18 | **প্রাণীজগৎ** | `Animal_World` | 20 Questions | `Free` | 0 Coins |
| 19 | **আবিষ্কার ও আবিষ্কারক** | `Inventions` | 20 Questions | `Premium` | 20 Coins |

---

## 🛠️ Internal Tech Stack Specifications

```mermaid
graph LR
    subgraph UI Layer
        UI[ViewBinding / Material 3]
        VM[Shared ViewModel]
    end

    subgraph Business Logic Layer
        Repo[Auth & Quiz Repositories]
        Flows[Kotlin Coroutines / StateFlow]
    end

    subgraph Client Cloud Services
        FB_Auth[Firebase Auth]
        FB_Store[Firestore Cloud Database]
        Billing[Google Play In-App Billing]
    end

    UI <--> VM
    VM <--> Repo
    Repo <--> Flows
    Repo <--> FB_Auth
    Repo <--> FB_Store
    Repo <--> Billing
```

### Core Architecture Specifications
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Language**: Kotlin 100% (Coroutines, Flow, StateFlow, KTX)
- **Dependency Injection**: Hilt / Dagger
- **UI Framework**: ViewBinding, Material Components 3, Custom Glassmorphism Vectors, Intuit SDP/SSP Responsive Layouts
- **Cloud Backend**: Firebase Auth, Firebase Firestore, Firebase Storage
- **Monetization**: Google Play Billing Library v8.0.0
- **Optimization**: R8 Code Shrinking & Resource Shrinking Enabled

---

## 📁 Internal Source Code Organization

```
com.novamindlabs.learngo
├── core/
│   ├── Nodes.kt               # Protected Cloud Firestore Schema Nodes
│   └── Resource.kt            # Internal UI State Wrapper
├── data/
│   ├── model/
│   │   ├── QuizQuestion.kt    # Question & Option Schema
│   │   └── UserRegister.kt    # User Account Schema
│   └── repository/
│       └── AuthRepository.kt  # Cloud Auth & Database Transactions
├── di/
│   ├── AppModule.kt           # Hilt Dependency Modules
│   └── MyApp.kt               # Application Context
└── ui/
    ├── adapter/
    │   └── QuizAdapter.kt     # Custom Category Adapter
    ├── viewModel/
    │   └── AuthViewModel.kt   # Core State ViewModel
    └── views/
        ├── auth/              # Client Login & Registration Views
        ├── dashboard/         # Dashboard Screens
        ├── intro/             # Onboarding & Splash Screens
        └── support/           # Help & Customer Support
```

---

## ⚙️ R8 & ProGuard Production Security

> [!IMPORTANT]
> ProGuard code obfuscation and resource stripping are configured in `app/proguard-rules.pro` to protect client intellectual property and prevent reverse-engineering.

- **R8 Minification**: Active (`isMinifyEnabled = true`)
- **Resource Stripping**: Active (`isShrinkResources = true`)
- **Obfuscation**: Class & method name obfuscation enabled

---

## 🔐 Authorized Environment Setup (Authorized Maintainers Only)

> [!WARNING]
> Only personnel with authorized credentials and private client Firebase configuration files may build or deploy this application.

### Internal Build Instructions

1. **Verify Official Client Credentials**:
   - Ensure the private `google-services.json` provided by the client is placed in the `app/` root directory.

2. **Compile Release Production Bundle**:
   ```bash
   ./gradlew assembleRelease
   ```

---

## 📜 Copyright & Non-Disclosure Notice

```
CONFIDENTIAL AND PROPRIETARY PROPERTY OF NOVA MIND LABS.
ALL RIGHTS RESERVED.

UNAUTHORIZED COPYING, CLONING, DISTRIBUTION, OR USE OF THIS CODEBASE 
IS STRICTLY PROHIBITED AND SUBJECT TO LEGAL ACTION.
```
