# ☕ Brewkery — Native Android Ordering App

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10.01-green.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-1.3.0-orange.svg?style=flat)](https://m3.material.io)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue.svg?style=flat)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue.svg?style=flat)](https://developer.android.com)
[![Tests](https://img.shields.io/badge/Unit%20Tests-11%20Passed-brightgreen.svg?style=flat)](app/src/test/java/com/brewkery/app/PriceAndCartTests.kt)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg?style=flat)](LICENSE)

> A modern, production-grade native Android coffee & artisan bakery ordering application built with **100% Kotlin**, **Jetpack Compose (Material 3)**, and **Clean Architecture (MVVM + Repository)**. Engineered for the **Clickretina Android Developer Assessment** based on the official [Live Prototype & Brief](https://vivekshah138.github.io/Brewkery/).

---

## 📌 Table of Contents
1. [App Flow & Screens](#-app-flow--screens)
2. [Architecture & Design Principles](#-architecture--design-principles)
3. [🤖 AI-First Engineering & Workflow](#-ai-first-engineering--workflow)
   - [AI Tools Ecosystem](#1-ai-tools-ecosystem)
   - [Production-Grade Prompts Used](#2-production-grade-prompts-used)
   - [What AI Got Right](#3-what-ai-got-right)
   - [What AI Got Wrong & How It Was Fixed (Deep-Dive)](#4-what-ai-got-wrong--how-it-was-fixed-deep-dive)
4. [Tech Stack & Libraries](#-tech-stack--libraries)
5. [Financial Precision & Pricing Engine](#-financial-precision--pricing-engine)
6. [Design System & UI Fidelity](#-design-system--ui-fidelity)
7. [Unit Testing & Verification](#-unit-testing--verification)
8. [Build & Installation Guide](#-build--installation-guide)

---

## 📱 App Flow & Screens

```
┌─────────────────┐      tap item       ┌──────────────────────┐
│ Screen 1: Menu  │ ──────────────────> │ Screen 2: Detail     │
│ Catalog & Banner│ <────────────────── │ Dynamic Customization│
└────────┬────────┘      back/add       └──────────┬───────────┘
         │                                         │
         │ tap cart bar                            │ add to cart
         ▼                                         ▼
┌─────────────────┐     place order     ┌──────────────────────┐
│ Screen 3: Cart  │ ──────────────────> │ Screen 4: Status     │
│ Live Bill Breakdown                   │ Ticket #BK-XXXXX     │
└─────────────────┘                     └──────────────────────┘
```

1. **Screen 1 — Home / Menu Catalog**:
   - Header with `BK` branding, store status, and dynamic cart bubble.
   - Store info banner with delivery estimate & fee; dynamically transitions into a real-time **Active Order Tracking Card** with green pulsing dot and `"Track"` CTA once an order is placed.
   - Interactive search bar filtering items in real-time across titles, taglines, and categories.
   - Sticky category selection pills (`All Items`, `☕ Hot Coffee`, `🧊 Cold Brews`, `🥐 Artisan Bakery`).
   - Catalog cards with 56dp thumbnails, `bg-amber-100` badge pills, star ratings, review counts, base prices, and `+ Customize` CTA.
   - Sticky bottom checkout bar (`View Your Cart • $X.XX`) with safe insets.

2. **Screen 2 — Item Customizer & Detail**:
   - Full-bleed 160dp hero image with badge pill and interactive favorite toggle (`❤️`).
   - Description in MutedBrown (`#786457`) and Key Ingredients pill tags.
   - **Size Selection**: 3-column equal-width grid with terracotta selection tint (`#FAEEE5`), border stroke, and price delta sub-labels.
   - **Milk Options / Spreads**: Full-width rows with selection borders and `+$X.XX` pricing.
   - **Sugar Levels**: Rounded-lg chips with solid Espresso fill (`#140B07`) and white text when selected.
   - Live recalculating bottom action bar with `[ − qty + ]` stepper and `gradient-terracotta` `"Add to Cart • $X.XX"` CTA.

3. **Screen 3 — Cart & Checkout**:
   - Uppercase `"YOUR CART"` header with `"Clear Cart"` action.
   - Clean itemized list displaying item name, selected options (`Size • Milk • Sugar`), and line total (no unnecessary thumbnails, matching prototype).
   - Cream-container stepper `[ − 1 + ]` for instantaneous quantity adjustments.
   - Live financial summary (Subtotal, \$2.50 flat delivery, 8.0% tax calculated via `BigDecimal HALF_UP`, and bold Total Payable).
   - `"🔒 Place Order Now • $X.XX"` full-width gradient button with tactile press feedback.

4. **Screen 4 — Order Dispatched & Tracking**:
   - Cream circular coffee badge (`☕`) with terracotta border ring.
   - Monospace ticket ID (`#BK-XXXXX`) generated via `TicketIdGenerator`.
   - Pulsating amber `"PREPARING"` chip, estimated wait time in terracotta, and emerald barista status.
   - Full-width Espresso `"Back to Menu"` navigation button.

---

## 🏗 Architecture & Design Principles

The application strictly implements **Unidirectional Data Flow (UDF)** under the **MVVM + Repository** architectural pattern:

```
                    ┌─────────────────────────┐
                    │   Retrofit REST API     │
                    │  (data.json / items.json)│
                    └────────────┬────────────┘
                                 │ DTOs (Gson)
                                 ▼
                    ┌─────────────────────────┐
                    │    DtoMappers.kt        │  (Converts Double to Long cents)
                    └────────────┬────────────┘
                                 │ Pure Domain Models
                                 ▼
                    ┌─────────────────────────┐
                    │   Menu & Cart Repos     │
                    └────────────┬────────────┘
                                 │ StateFlow<UiState>
                                 ▼
                    ┌─────────────────────────┐
                    │  Menu & Cart ViewModels │  (Activity-scoped, survives config changes)
                    └────────────┬────────────┘
                                 │ Immutable State
                                 ▼
                    ┌─────────────────────────┐
                    │  Jetpack Compose Screens│  (Declarative UI, zero side-effects)
                    └─────────────────────────┘
```

- **Clean Domain Separation**: Zero Android dependencies in `domain/` models. All calculations (`PriceCalculator`) are pure Kotlin functions, making them 100% testable without mocking Android frameworks or Robolectric.
- **AppContainer DI**: Pragmatic, lightweight manual Dependency Injection via `AppContainer` (singleton initialized in `BrewkeryApp`). Avoids unnecessary annotation-processing overhead (Hilt/KAPT) while retaining testability and clean decoupling.
- **Activity-Scoped State**: `MenuViewModel` and `CartViewModel` are scoped to `MainActivity`, ensuring in-memory cart items and active orders persist seamlessly during navigation between screens without premature state destruction.

---

## 🤖 AI-First Engineering & Workflow

As an engineer on an AI-first team, I leveraged AI not just for code autocompletion, but as an **interactive senior pair-programmer and systems architect** across all stages of development: requirements synthesis, financial math edge-case discovery, pixel-perfect Compose rendering, and edge-to-edge window inset engineering.

### 1. AI Tools Ecosystem
- **Google Antigravity / Gemini & Claude**: Primary agentic pair-programmer used for architectural planning, Jetpack Compose screen implementation, prototype reverse-engineering, and deep debugging.
- **Cursor / GitHub Copilot**: Fast contextual synthesis, boilerplate generation, and rapid test suite scaffolding.

---

### 2. Production-Grade Prompts Used

Below are 3 actual, high-signal prompts used during the development lifecycle:

#### 💬 Prompt 1: Architecture, DI & Financial Boundary Design
> *"Design a clean native Android coffee & bakery ordering app in Kotlin using Jetpack Compose (Material 3), Navigation Compose, and Retrofit. Follow MVVM + Repository pattern with manual AppContainer DI (no Hilt/KAPT overhead). At the network boundary (`DtoMappers.kt`), convert all incoming Double prices to `Long` cents so that no floating-point arithmetic touches the domain or UI layers. Keep the domain layer 100% pure Kotlin for frictionless JUnit 4 unit testing."*

#### 💬 Prompt 2: API Schema Anomaly & Pricing Precision Engine
> *"Inspect the Brewkery `data.json` API endpoint. Notice that while `sizes` and `milk_options` have explicit `extra_price` numeric fields, `sugar_levels` contains unstructured strings with embedded price surcharges like `'Light Wildflower Honey (+0.40)'`. Write a pure Kotlin `PriceCalculator` that: (1) uses regex to extract hidden surcharges from sugar strings, defaulting to 0 for standard options; (2) calculates 8% tax using `BigDecimal.setScale(0, RoundingMode.HALF_UP)` on the subtotal only; and (3) matches the exact reference test bill: Subtotal \$9.40 → Tax \$0.75 → Total \$12.65. Generate a comprehensive JUnit test suite covering all edge cases."*

#### 💬 Prompt 3: Prototype Pixel-Match & Edge-to-Edge Inset Engineering
> *"Analyze the live prototype at https://vivekshah138.github.io/Brewkery/ and the 5 provided screen captures. Match the visual hierarchy 1:1: exact color tokens (`#D9532F` Terracotta, `#140B07` Espresso, `#FDFAF7` Crema, `#F59E0B` Amber), 56dp catalog thumbnails, `bg-amber-100` badges, 3-column size grid with terracotta selected tint, and gradient CTAs (`linear-gradient(135deg, #d9532f, #b84121, #8c2b12)`). On edge-to-edge Android devices, ensure custom bottom bars draw a solid full-bleed background to the physical bottom edge while placing `navigationBarsPadding()` on inner content so that underlying scrolling lists never leak through the system navigation bar."*

---

### 3. What AI Got Right

1. **Unidirectional Architecture & State Modeling**:
   - AI generated a clean, idiomatic Kotlin `StateFlow` architecture with sealed UI state interfaces (`Loading`, `Error`, `Success`).
   - The separation between DTOs (`MenuDtos.kt`), Domain Models (`Models.kt`), and UI State enabled zero regression during rapid UI refactoring passes.
2. **Deterministic Financial Math**:
   - By enforcing `Long` cents across all internal structures, floating-point rounding bugs (e.g., `$0.10 + $0.20 = 0.30000000000000004`) were completely eliminated.
   - The tax calculation accurately matched the prototype’s exact rounded bill (\$9.40 → \$0.75 tax → \$12.65 total) on the first execution.
3. **Automated Test Scaffolding**:
   - Rapidly scaffolded 11 unit tests in `PriceAndCartTests.kt` verifying pricing boundaries, cart item identity collisions, and quantity merging.

---

### 4. What AI Got Wrong & How It Was Fixed (Deep-Dive)

Working effectively with AI requires active oversight, verification against real devices, and deep understanding of the platform. Here are the major bugs identified and resolved:

#### ❌ Bug 1: Schema Hallucination on Sugar Surcharges
- **The Issue**: AI initially assumed that `sugar_levels` in `data.json` had structured objects like `sizes` (e.g., `{ "id": "...", "extra_price": 0.40 }`). In reality, the API returns a raw array of strings: `["0% Unsweetened", "Light Wildflower Honey (+0.40)"]`. Standard deserialization would have caused a JSON crash or dropped the \$0.40 surcharge entirely.
- **The Fix**: I caught this during API payload inspection and directed the creation of a specialized regex parser:
  ```kotlin
  private val SUGAR_EXTRA_REGEX = Regex("""\(\+(\d+\.\d+)\)""")

  fun parseSugarExtraCents(sugarLevel: String): Long {
      val match = SUGAR_EXTRA_REGEX.find(sugarLevel) ?: return 0L
      val dollarsStr = match.groupValues[1]
      return dollarsToCents(dollarsStr.toDoubleOrNull() ?: 0.0)
  }
  ```
  This cleanly extracts the surcharge at runtime and stores it directly in the cart line identity.

#### ❌ Bug 2: Edge-to-Edge Navigation Bar Inset Leak (Visual Defect)
- **The Issue**: On modern Android devices with gesture navigation and edge-to-edge display enabled (`enableEdgeToEdge()`), AI placed `Modifier.navigationBarsPadding()` directly on the outer `Surface` of the bottom action bar. This caused the white `Surface` to terminate *above* the navigation bar, leaving the bottom 24–48dp of the screen transparent. As a result, items from the scrollable `LazyColumn` (specifically, the dark Sugar Levels card) peeked out and leaked through underneath the "Add to Cart" button.
- **The Fix**: I diagnosed the root cause from a physical device screenshot and refactored the inset structure:
  ```kotlin
  // Surface draws full-bleed solid background to the physical bottom edge
  Surface(
      modifier        = Modifier.fillMaxWidth(),
      color           = Color.White,
      shadowElevation = 8.dp,
  ) {
      // navigationBarsPadding applied to inner Row so controls sit safely above gesture pill
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
              .padding(horizontal = 16.dp, vertical = 12.dp),
          ...
      ) { ... }
  }
  ```
  Additionally, I added `innerPadding.calculateBottomPadding() + 24.dp` to the `LazyColumn` content padding, ensuring the user can scroll the lowest card cleanly into view with comfortable breathing room.

#### ❌ Bug 3: Compose API Compilation Mismatch
- **The Issue**: AI attempted to call `Modifier.padding(horizontal = 16.dp, top = 14.dp, bottom = 8.dp)`, which is an invalid overload in Jetpack Compose and failed to compile.
- **The Fix**: Refactored the call to use standard named parameters `start = 16.dp, top = 14.dp, end = 16.dp, bottom = 8.dp`, keeping the build clean and error-free.

---

## 🛠 Tech Stack & Libraries

| Technology | Purpose | Rationale |
|---|---|---|
| **Kotlin 2.0.21** | Primary Language | Null-safety, coroutines, modern language features. |
| **Jetpack Compose + Material 3** | UI Toolkit | Declarative, reactive UI without XML boilerplate. |
| **Navigation Compose** | In-App Routing | Single-Activity architecture with type-safe route parameters. |
| **Kotlin Coroutines + StateFlow** | Reactive State Management | Native lifecycle-aware concurrency and reactive state streams. |
| **Retrofit 2.11.0 + OkHttp 4.12.0** | Networking | Industry-standard HTTP client with connection pooling and logging. |
| **Gson 2.11.0** | JSON Parsing | Robust deserialization matching static GitHub JSON endpoints. |
| **Coil 2.7.0 (`coil-compose`)** | Image Loading | Lightweight, memory-efficient Compose-first image caching. |
| **JUnit 4** | Unit Testing | Fast, headless unit test execution without emulator overhead. |

---

## 💰 Financial Precision & Pricing Engine

To ensure enterprise-grade reliability, all financial arithmetic follows strict financial accounting standards:

$$\text{Unit Price} = \text{Base Price} + \text{Size Extra} + \text{Milk Extra} + \text{Sugar Extra}$$
$$\text{Line Total} = \text{Unit Price} \times \text{Quantity}$$
$$\text{Subtotal} = \sum \text{Line Totals}$$
$$\text{Tax} = \text{round}_{\text{HALF\_UP}}(\text{Subtotal} \times \text{Tax Rate})$$
$$\text{Total Payable} = \text{Subtotal} + \text{Delivery Fee} + \text{Tax}$$

- **Zero Float/Double Arithmetic**: All prices are modeled as `Long` cents internally (e.g., \$4.85 = `485L`).
- **Rounding Rule**: Taxes are calculated via `BigDecimal(subtotalCents).multiply(BigDecimal(0.08)).setScale(0, RoundingMode.HALF_UP).toLong()`.
- **Verified Benchmark**:
  - Subtotal: **\$9.40** (940 cents)
  - Delivery Fee: **\$2.50** (250 cents)
  - Est. Tax (8.0%): **\$0.75** (75 cents)
  - Total Payable: **\$12.65** (1265 cents) ✅ *(Unit test verified)*

---

## 🎨 Design System & UI Fidelity

The app faithfully implements the design system defined in the assignment prototype:

| Token Name | Hex Code | Compose Color | Applied Element |
|---|---|---|---|
| **Terracotta** | `#D9532F` | `Color(0xFFD9532F)` | Primary CTA buttons, pricing highlights, active borders |
| **Espresso** | `#140B07` | `Color(0xFF140B07)` | Brand circle (`BK`), selected sugar chips, Back to Menu button |
| **Crema** | `#FDFAF7` | `Color(0xFFFDFAF7)` | Scaffold app background, clean card contrast |
| **Amber** | `#F59E0B` | `Color(0xFFF59E0B)` | "Proceed to Checkout" CTA, preparing order badges |
| **Border** | `#EBD8CB` | `Color(0xFFEBD8CB)` | Structural card borders, dividers, unselected chip strokes |
| **MutedBrown** | `#786457` | `Color(0xFF786457)` | Product descriptions, review counts, secondary captions |
| **Emerald** | `#047857` | `Color(0xFF047857)` | Active order live status dot, barista confirmation pill |

---

## 🧪 Unit Testing & Verification

Unit tests are located at [PriceAndCartTests.kt](app/src/test/java/com/brewkery/app/PriceAndCartTests.kt) and can be executed via Gradle:

```bash
./gradlew test
```

### Test Suite Coverage (11/11 Passing):
- `dollarsToCents_roundsCorrectly()`: Validates clean floating-point to integer cents conversion.
- `formatPrice_formatsCorrectly()`: Validates currency formatting string generation.
- `parseSugarExtraCents_extractsPositiveSurcharge()`: Validates regex extraction of `(+0.40)` and `(+0.75)`.
- `parseSugarExtraCents_defaultsToZeroForStandard()`: Validates zero-extra options like `"0% Unsweetened"`.
- `unitPriceCents_sumsAllAdditions()`: Validates item + size + milk + sugar addition.
- `taxCents_calculatesHalfUpRounding()`: Verifies `HALF_UP` tax rounding on arbitrary subtotals.
- `totalCents_matchesPrototypeReferenceCalculation()`: **Validates \$9.40 subtotal + \$2.50 fee + \$0.75 tax = \$12.65 total.**
- `cartLineKey_sameCustomizationProducesSameKey()`: Validates identity hashing for line merging.
- `cartLineKey_differentCustomizationProducesDifferentKey()`: Validates separate cart line creation for distinct configurations.
- `cartLine_totalCents_multipliesByQuantity()`: Validates unitPrice × quantity scaling.
- `emptyCart_subtotalIsZero()`: Validates boundary state when cart is cleared.

---

## 🚀 Build & Installation Guide

### Prerequisites
- **Android Studio Ladybug (2024.2.x)** or newer
- **JDK 17** (configured as Gradle JDK)
- Android physical device or emulator running **API 24+**

### 1. Clone the Project
```bash
git clone https://github.com/MahalaxmiDhanai/CLICKRETINA.git
cd CLICKRETINA
```

### 2. Run All Unit Tests
```bash
./gradlew test
```

### 3. Build Debug APK
```bash
./gradlew assembleDebug
```
The compiled, installable APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 4. Install Directly to Device / Emulator
```bash
./gradlew installDebug
```

---

## 👤 Author & Submission Details

- **Candidate**: Mahalaxmi
- **Role**: Android Developer
- **Target Company**: Clickretina
- **Repository**: [https://github.com/MahalaxmiDhanai/CLICKRETINA](https://github.com/MahalaxmiDhanai/CLICKRETINA)
- **Submission Email**: `Hr@clickretina.co.in`
- **Assessment Brief**: [https://vivekshah138.github.io/Brewkery/](https://vivekshah138.github.io/Brewkery/)
