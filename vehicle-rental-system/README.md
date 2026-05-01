# 🚗 DriveEase — Vehicle Rental System

A full-stack Vehicle Rental Management System built with:
- **Backend**: Spring Boot 3.2 + SQLite (via JPA/Hibernate)
- **Frontend**: Vanilla HTML/CSS/JS (no framework dependencies)
- **API Testing**: Postman Collection included

---

## 📁 Project Structure

```
vehicle-rental-system/
├── backend/                    ← Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/rental/
│       ├── VehicleRentalApplication.java
│       ├── model/              ← JPA Entities
│       │   ├── Vehicle.java
│       │   ├── Customer.java
│       │   ├── Driver.java
│       │   ├── Rental.java
│       │   └── TripRequest.java
│       ├── repository/         ← Spring Data JPA Repos
│       ├── service/            ← Business Logic
│       └── controller/         ← REST Controllers
├── frontend/                   ← Static UI
│   ├── index.html              ← Landing / Role selector
│   ├── css/style.css
│   ├── js/api.js
│   └── pages/
│       ├── customer.html       ← Customer Portal
│       ├── driver.html         ← Driver Dashboard
│       └── admin.html          ← Admin Panel
└── DriveEase_API_Postman.json  ← Postman Collection
```

---

## 🚀 Setup & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- Modern web browser

### Step 1 — Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts at **http://localhost:8080**

SQLite database (`vehicle_rental.db`) is auto-created in the `backend/` folder on first run.

### Step 2 — Open the Frontend

Open `frontend/index.html` in your browser directly (no server needed).

Or use a simple HTTP server:
```bash
cd frontend
python3 -m http.server 3000
# Open http://localhost:3000
```

### Step 3 — Seed Sample Data

When you open `index.html`, it automatically calls `POST /api/admin/seed` to load sample data.

Or manually in Postman: **POST** `http://localhost:8080/api/admin/seed`

---

## 🔑 Demo Credentials

| Role     | Username  | Password |
|----------|-----------|----------|
| Admin    | admin     | admin123 |
| Customer | alice     | pass123  |
| Customer | bob       | pass123  |
| Driver   | john123   | pass123  |
| Driver   | mike123   | pass123  |
| Driver   | sarah123  | pass123  |

---

## 🌐 API Endpoints

### Admin
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/admin/login` | Admin login |
| POST | `/api/admin/seed` | Load sample data |
| GET | `/api/admin/dashboard` | Revenue + stats |
| GET | `/api/admin/customers` | All customers |
| GET | `/api/admin/drivers` | All drivers |
| GET | `/api/admin/vehicles` | All vehicles |
| GET | `/api/admin/rentals` | All rentals |
| GET | `/api/admin/requests` | All requests |

### Vehicles
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/vehicles` | All vehicles |
| GET | `/api/vehicles/available` | Available only |
| GET | `/api/vehicles/rented` | Rented vehicles |
| POST | `/api/vehicles` | Add vehicle |
| PUT | `/api/vehicles/{id}` | Update vehicle |
| DELETE | `/api/vehicles/{id}` | Delete vehicle |

### Customers
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/customers/register` | Register |
| POST | `/api/customers/login` | Login |
| GET | `/api/customers/{id}` | Get customer |

### Drivers
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/drivers/register` | Register |
| POST | `/api/drivers/login` | Login |
| GET | `/api/drivers/available` | Available drivers |

### Rentals — Self Drive
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/rentals/self-drive` | Book self drive |
| POST | `/api/rentals/{id}/return` | Return vehicle |

### Rentals — With Driver (flow in order)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/rentals/with-driver/request` | 1. Customer books |
| POST | `/api/rentals/requests/{id}/respond` | 2. Driver accepts/rejects |
| POST | `/api/rentals/{id}/start` | 3. Driver starts trip |
| POST | `/api/rentals/{id}/complete` | 4. Driver completes |
| POST | `/api/rentals/{id}/pay` | 5. Customer pays |
| POST | `/api/rentals/{id}/rate` | 6. Customer rates driver |
| POST | `/api/rentals/{id}/cancel` | Cancel rental |

---

## 🧪 Postman Setup

1. Open Postman
2. Click **Import** → select `DriveEase_API_Postman.json`
3. The collection loads with all endpoints organized by category
4. Set variable `baseUrl = http://localhost:8080/api` (already preset)
5. Run **"Seed Sample Data"** first, then test other endpoints

### Full With-Driver Trip Flow in Postman:
1. **Seed Sample Data** → POST `/admin/seed`
2. **Request Trip** → POST `/rentals/with-driver/request`  
   Note the `requestId` from response
3. **Driver Responds** → POST `/rentals/requests/{requestId}/respond`  
   Note the `rentalId` from response
4. **Start Trip** → POST `/rentals/{rentalId}/start`
5. **Complete Trip** → POST `/rentals/{rentalId}/complete`
6. **Customer Pays** → POST `/rentals/{rentalId}/pay`
7. **Rate Driver** → POST `/rentals/{rentalId}/rate`

---

## 💡 Features

### Customer Portal
- Register / Login
- Browse all vehicles with filters
- **Self Drive**: Select vehicle → pay upfront → return on any date
- **With Driver**: Select vehicle + driver → send request → pay after trip
- View all rentals and request statuses
- Pay & rate completed with-driver trips
- Return self-drive vehicles (with late fee calculation)
- Loyalty points system (earn 10 per trip, redeem for discounts)

### Driver Dashboard
- Register / Login
- View and respond to pending trip requests (Accept/Reject)
- Start and complete trips with KM entry
- Track earnings (₹50/hr + ₹8/km)
- Performance analytics and rating history

### Admin Panel
- Login with admin credentials
- Fleet management (add/delete vehicles)
- View all customers, drivers, vehicles
- Full rental and request history
- Revenue dashboard with date range filter
- Platform-wide statistics

### Business Rules
- **Cancellation Fees**: Free within 2 min, ₹50 within 5 min, ₹150 after
- **Late Return**: 1.5× daily rate per extra day
- **Damage Fee**: ₹2,000 flat
- **GST**: 5% on all transactions
- **Loyalty Points**: 10 pts earned per completed trip; 10 pts = ₹10 discount

---

## 🗄️ Database

SQLite file: `backend/vehicle_rental.db`

Tables auto-created by Hibernate:
- `vehicles`
- `customers`  
- `drivers`
- `rentals`
- `trip_requests`

To reset data: delete `vehicle_rental.db` and restart the server.

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend Framework | Spring Boot 3.2 |
| ORM | Spring Data JPA + Hibernate |
| Database | SQLite (sqlite-jdbc 3.44) |
| REST | Spring MVC |
| CORS | Configured globally for all origins |
| Frontend | HTML5 + CSS3 + Vanilla JS |
| Fonts | Syne + DM Sans (Google Fonts) |
| API Testing | Postman |
