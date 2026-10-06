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
# 1. Clone / unzip the project
git clone <your-repo-url>
cd brewkery

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

## 📝 Assumptions & Decisions

1. **Item Detail uses menu list data** — the spec allows this; the `GET api/items/{id}.json` endpoint is wired in the Retrofit service and repository but the UI reads from the already-loaded menu to avoid a redundant network call.
2. **Sugar level key = full string** — `CartLineKey.sugarLevel` stores the complete sugar string (e.g. `"Light Wildflower Honey (+0.40)"`), which uniquely identifies the selection including its hidden price.
3. **Activity-scoped ViewModels** — both `MenuViewModel` and `CartViewModel` live at Activity scope so cart state is never lost across navigation.
4. **Cart identity** — `CartLineKey(itemId, sizeId, milkOptionId, sugarLevel)`. Adding the same configuration again merges quantities.
5. **Search** — real-time client-side filter on name + tagline; does not reset the selected category.
6. **No dark theme** — the prototype is light-only; dark theme was not implemented to stay within the 4-6 hour time-box.

---

## 🤖 How I Used AI

> *[Fill in as appropriate — e.g.: "Used AI assistance for initial scaffold and boilerplate generation. All architecture decisions, pricing logic, and UI polish were reviewed and validated manually."]*

---

## 🔮 Possible Improvements

- Add dark theme support
- Persist cart across sessions with DataStore
- Add skeleton loading placeholders (Shimmer effect)
- Image pre-fetching on the menu list
- Favourite items with local persistence
- Animated transitions between screens
- Proper error retry with exponential backoff
