# Brewkery — Android Take-Home Assessment

Built by **Mahalaxmi** for the **Clickretina Android Developer** take-home assessment.

This is a native Android coffee & bakery ordering app written in Kotlin using Jetpack Compose, Retrofit, and Coroutines. It follows the exact 4-screen flow and UI design from the [Live Prototype](https://vivekshah138.github.io/Brewkery/).

---

## 📱 App Walkthrough

1. **Screen 1 — Home / Menu Catalog**:
   - Header with `BK` branding, store status, and live cart item count.
   - Store info delivery banner ($2.50 fee, 20-30 mins). Once an order is placed, this dynamically changes into an **Active Order Tracking Card** with a green pulsing dot and a "Track" button.
   - Real-time search bar that filters items across names, taglines, and categories.
   - Filter pills for categories (`All Items`, `☕ Hot Coffee`, `🧊 Cold Brews`, `🥐 Artisan Bakery`).
   - Catalog cards with 56dp thumbnails, `BESTSELLER` / `SEASONAL` badges, star ratings, review counts, base price, and a `+ Customize` button.
   - Sticky bottom bar (`View Your Cart • $X.XX`) that shows up whenever items are in the cart.

2. **Screen 2 — Item Detail / Customizer**:
   - 160dp hero image with badge overlay and interactive favorite heart toggle.
   - Item title, base price in terracotta, description, and Key Ingredients tags.
   - **Size Selection**: 3-column grid of cards with selected cream tint and border (`Tall`, `Grande`, `Venti`).
   - **Milk Options**: Full-width selectable rows with price deltas (`+$0.50`, etc.).
   - **Sugar Levels**: Rounded chips with dark Espresso fill when selected.
   - Bottom bar with `[ − qty + ]` stepper and live-updating `Add to Cart • $X.XX` button.

3. **Screen 3 — Cart & Checkout**:
   - Header with `YOUR CART` and a `Clear Cart` option.
   - Itemized list with item name, customized options (`Size • Milk • Sugar`), line price, and `[ − qty + ]` stepper.
   - Bill breakdown card: Subtotal, $2.50 flat delivery fee, 8.0% tax, and Total Payable.
   - Full-width `🔒 Place Order Now • $X.XX` gradient CTA.

4. **Screen 4 — Order Status**:
   - Coffee mug badge with terracotta border ring.
   - Generated ticket ID (e.g. `#BK-84920`) and amber `PREPARING` badge.
   - Estimated wait time, items ordered count, and barista status.
   - `Back to Menu` button that clears the screen and returns to the catalog.

---

## 🤖 How I Used AI (Prompts, Wins & Fixes)

At Clickretina you emphasize being AI-first. Throughout this project, I used AI tools (Gemini, Claude, Cursor) as my pair-programmer for planning, coding, and debugging. Here is an honest breakdown of how I used them.

### Tools I Used
- **Google Antigravity & Claude**: Used for initial architecture planning, drafting the Jetpack Compose screens, and debugging Compose window inset issues.
- **Cursor / GitHub Copilot**: Used inside the IDE for fast autocompletion, writing DTOs, and scaffolding unit tests.

### High-Signal Prompts I Used (How I Directed the AI)

Instead of asking generic questions like *"make an app"*, I treated AI as a senior engineering partner by giving explicit constraints, design tokens, non-functional requirements, and architectural boundaries. Here are the 4 key prompts I used:

#### 1. Architecture, State Scoping & Money Representation
> **Why this prompt**: Sets up a solid foundational architecture upfront, prevents floating-point currency errors at the network boundary, and avoids state loss during navigation without over-engineering with heavy frameworks.
> 
> *"Let's architect a native Android coffee ordering app in Kotlin following Clean MVVM. Here are my requirements: (1) Single-Activity with Navigation Compose; (2) Manual DI via an AppContainer singleton — no heavy Hilt/Dagger setup needed for this scope; (3) Retrofit 2 + Gson fetching from raw JSON endpoints; (4) Scope CartViewModel to the Activity so the in-memory cart and active order never get destroyed when navigating between screens; (5) Convert incoming Double prices into Long cents at DtoMappers.kt so zero floating-point arithmetic touches the domain or UI. Scaffold the core layers, Repositories, and ViewModels."*

#### 2. Reverse-Engineering the Prototype UI in Jetpack Compose
> **Why this prompt**: Provides the exact visual specs, Tailwind styles, and component constraints extracted directly from the live prototype so the generated Compose code matches pixel-for-pixel.
> 
> *"I'm inspecting the Brewkery web prototype (https://vivekshah138.github.io/Brewkery/). I need to match the design 1:1 in Jetpack Compose Material 3: (1) Define our color tokens: Terracotta (#D9532F), Espresso (#140B07), Crema (#FDFAF7), Amber (#F59E0B), Border (#EBD8CB), MutedBrown (#786457); (2) Catalog cards must have 56dp rounded thumbnails, amber-100 badges, star ratings, and a terracotta '+ Customize' button; (3) Item Detail needs a 3-column equal-width size selection grid with cream selected tint, full-width milk rows, and dark Espresso rounded chips for sugar levels; (4) The bottom CTA must be a gradient-terracotta button side-by-side with a bordered quantity stepper. Write modular composables for these screens."*

#### 3. API Edge Cases & Accounting-Grade Financial Math
> **Why this prompt**: Identifies a hidden data quirk in the API response and enforces strict financial rounding rules verified against the reference test bill.
> 
> *"Check the data.json API response closely: 'sizes' and 'milk_options' have numeric extra_price fields, but 'sugar_levels' are unstructured plain strings with embedded price strings like 'Light Wildflower Honey (+0.40)'. Write a pure Kotlin PriceCalculator that: (1) Uses regex to pull the extra price from sugar strings, defaulting to 0 for standard options; (2) Keeps all calculations strictly in Long cents; (3) Computes 8% tax on the subtotal using BigDecimal HALF_UP rounding to match the prototype's reference bill ($9.40 subtotal + $2.50 delivery + $0.75 tax = $12.65 total); (4) Write JUnit 4 unit tests covering every calculation and edge case."*

#### 4. Real-Device Edge-to-Edge Window Inset Engineering
> **Why this prompt**: Diagnoses a subtle visual defect discovered during physical phone testing and directs the AI toward the architectural root cause rather than a quick hack.
> 
> *"On physical device testing with enableEdgeToEdge(), the bottom 'Add to Cart' bar has a visual bug: the outer Surface has navigationBarsPadding(), which makes it stop above the system gesture bar, leaving the bottom 30dp transparent so the scrolled LazyColumn list peeks through underneath! How do we structure Scaffold and Compose insets so the Surface background paints solid full-bleed all the way to the physical screen edge, while keeping the interactive buttons and steppers safely padded above the gesture bar? Also ensure LazyColumn bottom padding accommodates the bar so the last item is fully scrollable."*

### What AI Got Right
- **Clean MVVM Architecture**: It set up a very clean unidirectional data flow with Kotlin `StateFlow`. Having the ViewModels scoped to the Activity meant the cart state and active order stayed intact when switching between screens.
- **Handling Money in Cents**: It followed the suggestion to map everything to `Long` cents in `DtoMappers.kt`. This completely prevented floating-point math issues like `$0.10 + $0.20 = 0.30000000000000004`.
- **Unit Tests**: It quickly generated 11 unit test cases for `PriceCalculator`, which made validating calculations very fast.

### What AI Got Wrong & How I Fixed It
1. **Schema Assumption on Sugar Levels**:
   - *What happened*: AI initially wrote DTOs assuming `sugar_levels` would be objects with an `extra_price` property, just like `sizes` and `milk_options`.
   - *How I caught & fixed it*: When checking the raw `data.json`, I saw that `sugar_levels` is just a list of strings: `["0% Unsweetened", "Light Wildflower Honey (+0.40)"]`. I wrote a regex extractor in `PriceCalculator.kt`:
     ```kotlin
     private val SUGAR_EXTRA_REGEX = Regex("""\(\+(\d+\.\d+)\)""")
     fun parseSugarExtraCents(sugarLevel: String): Long {
         val match = SUGAR_EXTRA_REGEX.find(sugarLevel) ?: return 0L
         return dollarsToCents(match.groupValues[1].toDoubleOrNull() ?: 0.0)
     }
     ```
     This safely pulls the extra cost directly from the string and defaults to 0 if there's no surcharge.

2. **Bottom Bar Layout Leak on Real Devices**:
   - *What happened*: AI put `Modifier.navigationBarsPadding()` directly on the outer `Surface` of the bottom action bar. On real phones with gesture navigation, this caused the white `Surface` to stop above the gesture bar, leaving the bottom 30dp transparent. As a result, the scrollable list underneath (the dark sugar level chips) peeked out through the bottom of the screen under the Add to Cart button.
   - *How I caught & fixed it*: When testing on my phone, I noticed the visual glitch immediately. I fixed it by keeping the `Surface` full-bleed (so it draws solid white all the way to the screen edge) and moving `navigationBarsPadding()` to the inner `Row`:
     ```kotlin
     Surface(
         modifier = Modifier.fillMaxWidth(),
         color    = Color.White,
     ) {
         Row(
             modifier = Modifier
                 .fillMaxWidth()
                 .navigationBarsPadding() // pads inner buttons safely above gesture pill
                 .padding(horizontal = 16.dp, vertical = 12.dp)
         ) { ... }
     }
     ```
     I also increased the bottom padding of the `LazyColumn` so the lowest card can be scrolled comfortably into view.

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin 2.0.21
- **UI**: Jetpack Compose + Material 3 (Single Activity with Navigation Compose)
- **Networking**: Retrofit 2 + Gson + OkHttp
- **Async & State**: Coroutines + `StateFlow`
- **Images**: Coil (`AsyncImage`)
- **Architecture**: MVVM + Repository pattern
- **Dependency Injection**: Manual via `AppContainer` singleton (simple, readable, and doesn't add Hilt/KAPT build overhead)
- **Local Database**: None needed (in-memory cart resets when app closes, matching the assignment spec)

---

## 💡 Practical Engineering Decisions

1. **All Money in Long Cents**: All arithmetic happens in `Long` cents internally. Conversion from `Double` happens once at the DTO mapping boundary (`DtoMappers.kt`), and formatting to `$X.XX` happens only when displaying text.
2. **Tax Rounding (`BigDecimal HALF_UP`)**: The prototype showed this exact reference bill: Subtotal `$9.40` $\rightarrow$ Tax `$0.75` $\rightarrow$ Total `$12.65`. Standard float math can round unpredictably, so I used `BigDecimal` with `HALF_UP` on the subtotal:
   ```kotlin
   BigDecimal(subtotalCents).multiply(BigDecimal(0.08)).setScale(0, RoundingMode.HALF_UP).toLong()
   ```
3. **Cart Item Merging**: Cart items use a composite key `CartLineKey(itemId, sizeId, milkOptionId, sugarLevel)`. If a user adds the exact same item with the same options twice, it increases the quantity rather than creating duplicate lines.

---

## 🧪 Unit Tests

Run the test suite with:
```bash
./gradlew test
```

All 11 unit tests in `PriceAndCartTests.kt` pass:
- Price conversions (`dollarsToCents`, `formatPrice`)
- Regex extraction for sugar surcharge strings
- Additions of base price + size + milk + sugar
- 8% tax calculation with `HALF_UP` rounding
- Full bill reference check: **$9.40 subtotal + $2.50 delivery + $0.75 tax = $12.65 total**
- Cart key hashing and line item quantity merging

---

## 🚀 How to Build & Run

### 1. Clone the Repository
```bash
git clone https://github.com/MahalaxmiDhanai/CLICKRETINA.git
cd CLICKRETINA
```

### 2. Run Tests
```bash
./gradlew test
```

### 3. Build the Debug APK
```bash
./gradlew assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 4. Install Directly to Device / Emulator
```bash
./gradlew installDebug
```

---

## 👤 Submission Details
- **Candidate**: Mahalaxmi
- **GitHub Repository**: [https://github.com/MahalaxmiDhanai/CLICKRETINA](https://github.com/MahalaxmiDhanai/CLICKRETINA)
- **Email**: `Hr@clickretina.co.in`
