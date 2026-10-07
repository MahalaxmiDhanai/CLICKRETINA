# Brewkery — Android Take-Home Assessment

> A native Android coffee & bakery ordering app built for the Clickretina Android Developer take-home assessment.

---

## 📱 App Flow

```
Menu Catalog → Item Detail → Cart & Checkout → Order Status
```

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (single Activity) |
| Architecture | MVVM + Repository |
| State | Kotlin StateFlow / coroutines |
| Networking | Retrofit 2 + OkHttp 4 + Gson |
| Images | Coil 2 (coil-compose) |
| DI | Manual wiring via `AppContainer` (no Hilt/Dagger) |
| Persistence | None — cart is in-memory |
| Min SDK | 24 |
| Compile/Target SDK | 35 |

---

## 🏗 Project Structure

```
com.brewkery.app
├── AppContainer.kt              Manual dependency wiring (singleton)
├── BrewkeryApp.kt               Application class
├── MainActivity.kt              Single Activity
│
├── data/
│   ├── remote/
│   │   ├── BrewkeryApi.kt       Retrofit service (getMenu, getItem)
│   │   ├── MenuDtos.kt          @SerializedName DTOs
│   │   └── DtoMappers.kt        DTO → domain (Double → Long cents boundary)
│   └── repository/
│       ├── MenuRepository.kt    Network calls + NetworkResult<T>
│       └── CartRepository.kt    In-memory cart + active order (StateFlow)
│
├── domain/
│   ├── Models.kt                Pure domain models (CartLine, Order, etc.)
│   ├── PriceCalculator.kt       Pure Kotlin pricing logic (BigDecimal)
│   └── TicketIdGenerator.kt     #BK-XXXXX random ticket IDs
│
└── ui/
    ├── theme/BrewkeryTheme.kt   Exact prototype colour palette
    ├── navigation/NavGraph.kt   4-screen nav graph
    ├── menu/                    MenuScreen + MenuViewModel
    ├── detail/                  ItemDetailScreen
    ├── cart/                    CartScreen + CartViewModel
    └── order/                   OrderStatusScreen
```

---

## 🚀 Build & Run

### Prerequisites
- Android Studio Ladybug (2024.2.x) or newer
- JDK 17
- Android emulator / device with API 24+

### Steps
```bash
# 1. Clone the project
git clone https://github.com/MahalaxmiDhanai/CLICKRETINA.git
cd CLICKRETINA

# 2. Open in Android Studio → let Gradle sync

# 3. Run on emulator
./gradlew :app:installDebug
```

### Build APK
```bash
# Debug APK (for submission)
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

Or via Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

---

## 🌐 API

Base URL: `https://raw.githubusercontent.com/VivekShah138/Brewkery/main/`

| Endpoint | Description |
|---|---|
| `GET data.json` | Full menu — meta, categories, 6 items |
| `GET api/items/{id}.json` | Single item detail (ids 1–6) |

> **Note:** `raw.githubusercontent.com` returns `text/plain` — Gson parses by body content, not Content-Type, so no custom converter is needed.

---

## 💰 Pricing Rules

All money is stored as **`Long` cents** internally. Doubles are converted at the DTO boundary in `DtoMappers.kt`. Formatting is done only at display time via `PriceCalculator.formatPrice()`.

```
unitPrice  = base_price + size.extra + milk.extra + sugarExtra
sugarExtra = parsed from "(+X.XX)" suffix in sugar level strings (API quirk)
lineTotal  = unitPrice × quantity
subtotal   = Σ lineTotals
tax        = subtotal × tax_rate_percent / 100  (rounded HALF_UP, on subtotal only)
total      = subtotal + delivery_fee + tax
```

**Reference check:** subtotal \$9.40 → tax \$0.75 → total \$12.65 ✅ (covered by unit tests)

---

## 🎨 Design

UI matches the live prototype at **https://vivekshah138.github.io/Brewkery/** exactly:

| Token | Hex | Used for |
|---|---|---|
| Terracotta | `#D9532F` | Primary buttons, badges, prices |
| Espresso | `#140B07` | Dark chip fill, TopBar text, Order button |
| Crema | `#FDFAF7` | App background |
| Amber | `#F59E0B` | "Proceed to Checkout" accent |
| Border | `#EBD8CB` | Card borders, dividers, chip outlines |
| MutedBrown | `#786457` | Secondary text, captions |

---

## 🧪 Tests

```bash
./gradlew :app:test
```

11 unit tests in `PriceAndCartTests.kt` covering:
- `parseSugarExtraCents` (regex quirk)
- `unitPriceCents`, `lineTotalCents`, `subtotalCents`
- `taxCents` (HALF_UP rounding)
- `totalCents` (reference check: \$9.40 → \$12.65)
- `CartLineKey` identity / merge behaviour

---

## 🤖 How I Used AI

### AI Tools Used
- **Google Antigravity / Gemini & Claude**: Architecture design, Compose UI development, and debugging.
- **Cursor / GitHub Copilot**: Rapid autocompletion and unit test generation.

### Actual Prompts Used
1. *"Create a clean native Android coffee & bakery ordering app in Kotlin using Jetpack Compose (Material 3), Navigation Compose, and Retrofit. Structure it with MVVM + Repository and manual AppContainer DI without heavy frameworks."*
2. *"Parse the Brewkery data.json API. Note that sugar levels are plain strings like 'Light Wildflower Honey (+0.40)'. Write pure Kotlin pricing logic converting Double to Long cents, extracting sugar extra price using regex, and calculating tax with BigDecimal HALF_UP to match $9.40 subtotal → $0.75 tax → $12.65 total. Write JUnit tests for all edge cases."*
3. *"Check the live prototype at https://vivekshah138.github.io/Brewkery/ and the 5 screenshots. Match the exact color palette (#D9532F Terracotta, #140B07 Espresso, #FDFAF7 Crema), size 3-column grid, full-width milk options, espresso dark sugar chips, and order dispatched ticket card with PREPARING badge."*

### What AI Got Right
- **Architecture & Precision Calculations**: Produced clean, decoupled MVVM architecture with unidirectional data flow (UDF) using Kotlin `StateFlow`. Implemented monetary values as `Long` cents at the DTO mapping boundary, correctly handling `BigDecimal(0.08)` rounding for tax calculation which matched the prototype reference bill ($9.40 → $12.65) on the very first run.
- **Comprehensive Unit Testing**: Automatically generated 11 targeted unit tests covering PriceCalculator and CartLineKey merge logic.

### What AI Got Wrong & How It Was Fixed
- **What AI got wrong:**
  1. *Hidden Sugar Surcharge in API*: AI initially assumed `sugar_levels` followed the same DTO structure as `milk_options` (with an explicit `extra_price` field), rather than being plain strings containing `(+0.40)`.
  2. *Initial Layout Mismatch*: In the first draft of the UI, AI rendered Size Selection as standard Compose chips (`FlowRow`) and included image thumbnails in Cart lines.
- **How I fixed it:**
  1. Implemented a regex extractor `Regex("""\(\+(\d+\.\d+)\)""")` inside `PriceCalculator.parseSugarExtraCents` to reliably parse positive increments from strings, defaulting to 0 for standard options.
  2. Inspected the prototype HTML/CSS directly and updated `ItemDetailScreen` to use a 3-column grid (`Row` with `weight(1f)` cards) for sizes, full-width rows for milk options, dark Espresso pills for sugar selection, and removed thumbnails from the Cart to mirror the prototype.

---

## 📝 Assumptions & Decisions

1. **Item Detail uses menu list data** — the spec allows this; the `GET api/items/{id}.json` endpoint is wired in the Retrofit service and repository but the UI reads from the already-loaded menu to avoid a redundant network call.
2. **Sugar level key = full string** — `CartLineKey.sugarLevel` stores the complete sugar string (e.g. `"Light Wildflower Honey (+0.40)"`), which uniquely identifies the selection including its hidden price.
3. **Activity-scoped ViewModels** — both `MenuViewModel` and `CartViewModel` live at Activity scope so cart state is never lost across navigation.
4. **Cart identity** — `CartLineKey(itemId, sizeId, milkOptionId, sugarLevel)`. Adding the same configuration again merges quantities.
5. **Search** — real-time client-side filter on name + tagline; does not reset the selected category.
6. **No dark theme** — the prototype is light-only; dark theme was not implemented to stay within the 4-6 hour time-box.

---

## 🔮 Possible Improvements

- Add dark theme support
- Persist cart across sessions with DataStore
- Add skeleton loading placeholders (Shimmer effect)
- Image pre-fetching on the menu list
- Favourite items with local persistence
- Animated transitions between screens
- Proper error retry with exponential backoff
