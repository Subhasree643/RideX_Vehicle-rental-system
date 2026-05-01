// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// Global state
let currentUser = null;
let currentRole = null;
let authToken = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadVehicles();
    
    // Setup form submissions
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerCustomerForm').addEventListener('submit', handleCustomerRegister);
    document.getElementById('registerDriverForm').addEventListener('submit', handleDriverRegister);
    document.getElementById('rentForm').addEventListener('submit', handleRent);
    document.getElementById('addVehicleForm').addEventListener('submit', handleAddVehicle);
});

// Authentication
function checkAuth() {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    const role = localStorage.getItem('role');
    
    if (token && user) {
        currentUser = JSON.parse(user);
        currentRole = role;
        authToken = token;
        updateUIForLoggedInUser();
    }
}

function updateUIForLoggedInUser() {
    document.getElementById('loginBtn').style.display = 'none';
    document.getElementById('registerBtn').style.display = 'none';
    document.getElementById('logoutBtn').style.display = 'inline';
    
    if (currentRole === 'CUSTOMER') {
        document.getElementById('rentalsLink').style.display = 'inline';
    } else if (currentRole === 'DRIVER') {
        document.getElementById('driverTripsLink').style.display = 'inline';
        loadDriverTrips();
    } else if (currentRole === 'ADMIN') {
        document.getElementById('adminLink').style.display = 'inline';
        loadAdminDashboard();
    }
}

function logout() {
    localStorage.clear();
    currentUser = null;
    currentRole = null;
    authToken = null;
    
    document.getElementById('loginBtn').style.display = 'inline';
    document.getElementById('registerBtn').style.display = 'inline';
    document.getElementById('logoutBtn').style.display = 'none';
    document.getElementById('rentalsLink').style.display = 'none';
    document.getElementById('driverTripsLink').style.display = 'none';
    document.getElementById('adminLink').style.display = 'none';
    
    showPage('home');
    showToast('Logged out successfully', 'success');
}

async function handleLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    const role = document.getElementById('loginRole').value;
    
    if (role === 'ADMIN') {
        if (username === 'admin' && password === 'admin123') {
            currentUser = { username: 'admin', name: 'Administrator' };
            currentRole = 'ADMIN';
            authToken = 'admin-token';
            localStorage.setItem('token', authToken);
            localStorage.setItem('user', JSON.stringify(currentUser));
            localStorage.setItem('role', 'ADMIN');
            updateUIForLoggedInUser();
            showPage('admin');
            loadAdminDashboard();
            showToast('Admin login successful', 'success');
        } else {
            showToast('Invalid admin credentials', 'error');
        }
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, role })
        });
        
        if (response.ok) {
            const data = await response.json();
            currentUser = data.user;
            currentRole = data.role;
            authToken = data.token;
            
            localStorage.setItem('token', authToken);
            localStorage.setItem('user', JSON.stringify(currentUser));
            localStorage.setItem('role', currentRole);
            
            updateUIForLoggedInUser();
            showToast(`Welcome ${currentUser.name || currentUser.username}`, 'success');
            
            if (currentRole === 'CUSTOMER') showPage('vehicles');
            else if (currentRole === 'DRIVER') showPage('driverTrips');
            else if (currentRole === 'ADMIN') showPage('admin');
        } else {
            const error = await response.json();
            showToast(error.error || 'Login failed', 'error');
        }
    } catch (error) {
        showToast('Network error', 'error');
    }
}

async function handleCustomerRegister(e) {
    e.preventDefault();
    
    const customer = {
        name: document.getElementById('custName').value,
        username: document.getElementById('custUsername').value,
        password: document.getElementById('custPassword').value,
        phone: document.getElementById('custPhone').value,
        address: document.getElementById('custAddress').value
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/register/customer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(customer)
        });
        
        if (response.ok) {
            showToast('Registration successful! Please login.', 'success');
            showPage('login');
            document.getElementById('registerCustomerForm').reset();
        } else {
            const error = await response.json();
            showToast(error.error || 'Registration failed', 'error');
        }
    } catch (error) {
        showToast('Network error', 'error');
    }
}

async function handleDriverRegister(e) {
    e.preventDefault();
    
    const driver = {
        name: document.getElementById('driverName').value,
        username: document.getElementById('driverUsername').value,
        password: document.getElementById('driverPassword').value,
        licenseNumber: document.getElementById('driverLicense').value,
        phone: document.getElementById('driverPhone').value
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/register/driver`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(driver)
        });
        
        if (response.ok) {
            showToast('Registration successful! Please login.', 'success');
            showPage('login');
            document.getElementById('registerDriverForm').reset();
        } else {
            const error = await response.json();
            showToast(error.error || 'Registration failed', 'error');
        }
    } catch (error) {
        showToast('Network error', 'error');
    }
}

// Vehicle Management
async function loadVehicles() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/vehicles/available`);
        const vehicles = await response.json();
        displayVehicles(vehicles);
    } catch (error) {
        console.error('Error loading vehicles:', error);
    }
}

function displayVehicles(vehicles) {
    const container = document.getElementById('vehiclesList');
    if (!container) return;
    
    container.innerHTML = vehicles.map(vehicle => `
        <div class="vehicle-card" data-brand="${vehicle.brand.toLowerCase()}" data-model="${vehicle.model.toLowerCase()}" data-type="${vehicle.type}">
            <div class="vehicle-header">
                <i class="fas fa-${vehicle.type === 'Car' ? 'car' : 'motorcycle'}"></i>
                <h3>${vehicle.brand} ${vehicle.model}</h3>
            </div>
            <div class="vehicle-body">
                <div class="vehicle-info">
                    <p><strong>Type:</strong> ${vehicle.type}</p>
                    <p><strong>Category:</strong> ${vehicle.category}</p>
                    <p><strong>Reg No:</strong> ${vehicle.regNo}</p>
                </div>
                <div class="vehicle-price">₹${vehicle.rentPerDay}/day</div>
                <div class="vehicle-actions">
                    ${currentRole === 'CUSTOMER' ? 
                        `<button class="btn-rent" onclick="openRentModal(${vehicle.vehicleId})">Rent Now</button>` : 
                        (currentRole === 'ADMIN' ? 
                            `<button class="btn-delete" onclick="deleteVehicle(${vehicle.vehicleId})">Delete</button>` : 
                            '')
                    }
                </div>
            </div>
        </div>
    `).join('');
}

function filterVehicles() {
    const searchTerm = document.getElementById('vehicleSearch')?.value.toLowerCase() || '';
    const typeFilter = document.getElementById('vehicleTypeFilter')?.value || 'all';
    
    const cards = document.querySelectorAll('.vehicle-card');
    cards.forEach(card => {
        const brand = card.dataset.brand;
        const model = card.dataset.model;
        const type = card.dataset.type;
        
        const matchesSearch = brand.includes(searchTerm) || model.includes(searchTerm);
        const matchesType = typeFilter === 'all' || type === typeFilter;
        
        card.style.display = matchesSearch && matchesType ? 'block' : 'none';
    });
}

// Rent Vehicle
function openRentModal(vehicleId) {
    if (!currentUser || currentRole !== 'CUSTOMER') {
        showToast('Please login as customer to rent', 'error');
        showPage('login');
        return;
    }
    
    document.getElementById('rentVehicleId').value = vehicleId;
    document.getElementById('rentModal').style.display = 'block';
    loadAvailableDrivers();
}

function toggleRentalFields() {
    const type = document.getElementById('rentalType').value;
    document.getElementById('selfDriveFields').style.display = type === 'SELF_DRIVE' ? 'block' : 'none';
    document.getElementById('withDriverFields').style.display = type === 'WITH_DRIVER' ? 'block' : 'none';
}

async function loadAvailableDrivers() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/drivers/available`);
        const drivers = await response.json();
        const select = document.getElementById('driverSelect');
        select.innerHTML = '<option value="">Select Driver</option>' + 
            drivers.map(d => `<option value="${d.driverId}">${d.name} (Rating: ${d.rating})</option>`).join('');
    } catch (error) {
        console.error('Error loading drivers:', error);
    }
}

async function handleRent(e) {
    e.preventDefault();
    
    const vehicleId = document.getElementById('rentVehicleId').value;
    const rentalType = document.getElementById('rentalType').value;
    const paymentMode = document.getElementById('paymentMode').value;
    
    let requestBody = {
        customerId: currentUser.customerId,
        vehicleId: parseInt(vehicleId),
        paymentMode: paymentMode
    };
    
    if (rentalType === 'SELF_DRIVE') {
        requestBody.days = parseInt(document.getElementById('days').value);
        
        try {
            const response = await fetch(`${API_BASE_URL}/customer/rent/selfdrive`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${authToken}`
                },
                body: JSON.stringify(requestBody)
            });
            
            if (response.ok) {
                showToast('Vehicle rented successfully!', 'success');
                closeModal();
                loadVehicles();
                loadCustomerRentals();
            } else {
                const error = await response.json();
                showToast(error.error || 'Rental failed', 'error');
            }
        } catch (error) {
            showToast('Network error', 'error');
        }
    } else {
        requestBody.pickupLocation = document.getElementById('pickupLocation').value;
        requestBody.dropLocation = document.getElementById('dropLocation').value;
        requestBody.pickupDate = document.getElementById('pickupDate').value;
        requestBody.pickupTime = document.getElementById('pickupTime').value;
        requestBody.driverId = parseInt(document.getElementById('driverSelect').value);
        
        try {
            const response = await fetch(`${API_BASE_URL}/customer/rent/withdriver`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${authToken}`
                },
                body: JSON.stringify(requestBody)
            });
            
            if (response.ok) {
                showToast('Booking request sent to driver!', 'success');
                closeModal();
            } else {
                const error = await response.json();
                showToast(error.error || 'Booking failed', 'error');
            }
        } catch (error) {
            showToast('Network error', 'error');
        }
    }
}

// Customer Rentals
async function loadCustomerRentals() {
    if (!currentUser || currentRole !== 'CUSTOMER') return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/customer/rentals/${currentUser.customerId}`);
        const rentals = await response.json();
        displayRentals(rentals);
    } catch (error) {
        console.error('Error loading rentals:', error);
    }
}

function displayRentals(rentals) {
    const container = document.getElementById('rentalsList');
    if (!container) return;
    
    container.innerHTML = rentals.map(rental => `
        <div class="rental-item">
            <div class="rental-info">
                <h4>${rental.vehicle?.brand} ${rental.vehicle?.model}</h4>
                <p>Rental ID: ${rental.rentalId} | Date: ${rental.rentDate}</p>
                <p>Type: ${rental.rentalType === 'SELF_DRIVE' ? 'Self Drive' : 'With Driver'}</p>
                <p>Amount: ₹${rental.rentAmount}</p>
            </div>
            <div>
                <span class="rental-status status-${rental.status.toLowerCase()}">${rental.status}</span>
                ${rental.status === 'ACTIVE' && rental.rentalType === 'SELF_DRIVE' ? 
                    `<button onclick="returnVehicle(${rental.rentalId})" class="btn-primary" style="margin-top:10px;">Return Vehicle</button>` : ''}
                ${rental.status === 'COMPLETED' && !rental.paymentMode && rental.rentalType === 'WITH_DRIVER' ? 
                    `<button onclick="payForTrip(${rental.rentalId})" class="btn-primary" style="margin-top:10px;">Pay Now</button>` : ''}
            </div>
        </div>
    `).join('');
}

async function returnVehicle(rentalId) {
    const distance = prompt('Enter total distance traveled (km):');
    if (!distance) return;
    
    const dropLocation = prompt('Enter drop location:');
    const damage = confirm('Any damage to the vehicle?');
    const rating = prompt('Rate your experience (1-5):');
    
    try {
        const response = await fetch(`${API_BASE_URL}/customer/rentals/${rentalId}/return`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                distance: parseFloat(distance),
                dropLocation: dropLocation,
                damage: damage,
                paymentMode: 'Cash',
                rating: rating ? parseInt(rating) : null
            })
        });
        
        if (response.ok) {
            showToast('Vehicle returned successfully!', 'success');
            loadCustomerRentals();
            loadVehicles();
        } else {
            const error = await response.json();
            showToast(error.error || 'Return failed', 'error');
        }
    } catch (error) {
        showToast('Network error', 'error');
    }
}

// Driver Functions
async function loadDriverTrips() {
    if (!currentUser || currentRole !== 'DRIVER') return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/driver/rentals/${currentUser.driverId}`);
        const trips = await response.json();
        displayDriverTrips(trips);
    } catch (error) {
        console.error('Error loading trips:', error);
    }
}

function displayDriverTrips(trips) {
    const container = document.getElementById('driverTripsList');
    if (!container) return;
    
    container.innerHTML = trips.map(trip => `
        <div class="trip-item">
            <div class="trip-info">
                <h4>${trip.vehicle?.brand} ${trip.vehicle?.model}</h4>
                <p>Customer: ${trip.customer?.name}</p>
                <p>Pickup: ${trip.pickupLocation} | Drop: ${trip.dropLocation || 'Not set'}</p>
                <p>Status: ${trip.status}</p>
            </div>
            <div>
                ${trip.status === 'ACCEPTED' ? 
                    `<button onclick="startTrip(${trip.rentalId})" class="btn-primary">Start Trip</button>` : ''}
                ${trip.status === 'ONGOING' ? 
                    `<button onclick="completeTripDriver(${trip.rentalId})" class="btn-primary">Complete Trip</button>` : ''}
                <button onclick="updateAvailability(${!currentUser.available})" class="btn-secondary">
                    ${currentUser.available ? 'Mark Unavailable' : 'Mark Available'}
                </button>
            </div>
        </div>
    `).join('');
}

async function startTrip(rentalId) {
    try {
        const response = await fetch(`${API_BASE_URL}/driver/rentals/${rentalId}/start`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        
        if (response.ok) {
            showToast('Trip started!', 'success');
            loadDriverTrips();
        }
    } catch (error) {
        showToast('Error starting trip', 'error');
    }
}

async function completeTripDriver(rentalId) {
    const distance = prompt('Enter total distance (km):');
    if (!distance) return;
    
    const dropLocation = prompt('Enter drop location:');
    const damage = confirm('Any damage?');
    
    try {
        const response = await fetch(`${API_BASE_URL}/customer/rentals/${rentalId}/return`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                distance: parseFloat(distance),
                dropLocation: dropLocation,
                damage: damage,
                paymentMode: null
            })
        });
        
        if (response.ok) {
            showToast('Trip completed! Customer can now pay.', 'success');
            loadDriverTrips();
        }
    } catch (error) {
        showToast('Error completing trip', 'error');
    }
}

async function updateAvailability(available) {
    try {
        const response = await fetch(`${API_BASE_URL}/driver/status/${currentUser.driverId}`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({ available: available })
        });
        
        if (response.ok) {
            currentUser.available = available;
            localStorage.setItem('user', JSON.stringify(currentUser));
            showToast(`You are now ${available ? 'available' : 'unavailable'} for trips`, 'success');
            loadDriverTrips();
        }
    } catch (error) {
        showToast('Error updating status', 'error');
    }
}

// Admin Functions
async function loadAdminDashboard() {
    try {
        const statsResponse = await fetch(`${API_BASE_URL}/admin/dashboard`);
        const stats = await statsResponse.json();
        
        document.getElementById('adminStats').innerHTML = `
            <div class="stat-card"><i class="fas fa-car"></i><h3>${stats.totalVehicles}</h3><p>Total Vehicles</p></div>
            <div class="stat-card"><i class="fas fa-check-circle"></i><h3>${stats.availableVehicles}</h3><p>Available</p></div>
            <div class="stat-card"><i class="fas fa-users"></i><h3>${stats.totalCustomers}</h3><p>Customers</p></div>
            <div class="stat-card"><i class="fas fa-id-card"></i><h3>${stats.totalDrivers}</h3><p>Drivers</p></div>
            <div class="stat-card"><i class="fas fa-truck"></i><h3>${stats.activeRentals}</h3><p>Active Rentals</p></div>
        `;
        
        loadAllVehiclesAdmin();
        loadAllCustomers();
        loadAllDrivers();
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

async function loadAllVehiclesAdmin() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/vehicles`);
        const vehicles = await response.json();
        
        document.getElementById('adminVehiclesList').innerHTML = vehicles.map(vehicle => `
            <div class="vehicle-card">
                <div class="vehicle-header">
                    <i class="fas fa-${vehicle.type === 'Car' ? 'car' : 'motorcycle'}"></i>
                    <h3>${vehicle.brand} ${vehicle.model}</h3>
                </div>
                <div class="vehicle-body">
                    <div class="vehicle-info">
                        <p><strong>Type:</strong> ${vehicle.type}</p>
                        <p><strong>Category:</strong> ${vehicle.category}</p>
                        <p><strong>Reg No:</strong> ${vehicle.regNo}</p>
                        <p><strong>Status:</strong> ${vehicle.available ? 'Available' : 'Rented'}</p>
                    </div>
                    <div class="vehicle-price">₹${vehicle.rentPerDay}/day</div>
                    <div class="vehicle-actions">
                        <button class="btn-delete" onclick="deleteVehicle(${vehicle.vehicleId})">Delete</button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading vehicles:', error);
    }
}

async function loadAllCustomers() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/customers`);
        const customers = await response.json();
        
        document.getElementById('adminCustomersList').innerHTML = `
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>Username</th><th>Phone</th><th>Points</th><th>Rented</th></tr></thead>
                <tbody>
                    ${customers.map(c => `
                        <tr>
                            <td>${c.customerId}</td>
                            <td>${c.name}</td>
                            <td>${c.username}</td>
                            <td>${c.phone}</td>
                            <td>${c.loyaltyPoints}</td>
                            <td>${c.rented ? 'Yes' : 'No'}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        console.error('Error loading customers:', error);
    }
}

async function loadAllDrivers() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/drivers`);
        const drivers = await response.json();
        
        document.getElementById('adminDriversList').innerHTML = `
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>License</th><th>Phone</th><th>Rating</th><th>Available</th></tr></thead>
                <tbody>
                    ${drivers.map(d => `
                        <tr>
                            <td>${d.driverId}</td>
                            <td>${d.name}</td>
                            <td>${d.licenseNumber}</td>
                            <td>${d.phone}</td>
                            <td>${d.rating}</td>
                            <td>${d.available ? 'Yes' : 'No'}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        console.error('Error loading drivers:', error);
    }
}

async function deleteVehicle(vehicleId) {
    if (!confirm('Are you sure you want to delete this vehicle?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/vehicles/${vehicleId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        
        if (response.ok) {
            showToast('Vehicle deleted', 'success');
            loadAllVehiclesAdmin();
            loadVehicles();
        }
    } catch (error) {
        showToast('Error deleting vehicle', 'error');
    }
}

async function handleAddVehicle(e) {
    e.preventDefault();
    
    const vehicle = {
        type: document.getElementById('vehicleType').value,
        category: document.getElementById('vehicleCategory').value,
        brand: document.getElementById('vehicleBrand').value,
        model: document.getElementById('vehicleModel').value,
        regNo: document.getElementById('vehicleRegNo').value,
        rentPerDay: parseFloat(document.getElementById('vehicleRentPerDay').value),
        available: true
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/vehicles`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify(vehicle)
        });
        
        if (response.ok) {
            showToast('Vehicle added successfully!', 'success');
            closeAddVehicleModal();
            loadAllVehiclesAdmin();
            loadVehicles();
            document.getElementById('addVehicleForm').reset();
        } else {
            showToast('Error adding vehicle', 'error');
        }
    } catch (error) {
        showToast('Network error', 'error');
    }
}

async function generateRevenueReport() {
    const startDate = document.getElementById('reportStartDate').value;
    const endDate = document.getElementById('reportEndDate').value;
    
    if (!startDate || !endDate) {
        showToast('Please select both dates', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/revenue?start=${startDate}&end=${endDate}`);
        const data = await response.json();
        
        document.getElementById('reportResult').innerHTML = `
            <div class="report-card">
                <h3>Revenue Report</h3>
                <p><strong>Period:</strong> ${startDate} to ${endDate}</p>
                <p><strong>Total Trips:</strong> ${data.tripCount}</p>
                <p><strong>Total Revenue:</strong> ₹${data.totalRevenue}</p>
                <p><strong>Average per Trip:</strong> ₹${data.tripCount > 0 ? (data.totalRevenue / data.tripCount).toFixed(2) : 0}</p>
            </div>
        `;
    } catch (error) {
        showToast('Error generating report', 'error');
    }
}

// UI Helper Functions
function showPage(pageId) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    document.getElementById(`${pageId}Page`).classList.add('active');
    
    // Load data based on page
    if (pageId === 'vehicles') loadVehicles();
    else if (pageId === 'rentals' && currentRole === 'CUSTOMER') loadCustomerRentals();
    else if (pageId === 'driverTrips' && currentRole === 'DRIVER') loadDriverTrips();
    else if (pageId === 'admin' && currentRole === 'ADMIN') loadAdminDashboard();
}

function showRegisterTab(type) {
    document.querySelectorAll('.register-form').forEach(form => {
        form.classList.remove('active');
    });
    document.getElementById(`register${type.charAt(0).toUpperCase() + type.slice(1)}Form`).classList.add('active');
    
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
}

function showAdminTab(tab) {
    document.querySelectorAll('.admin-tab').forEach(t => {
        t.classList.remove('active');
    });
    document.getElementById(`admin${tab.charAt(0).toUpperCase() + tab.slice(1)}Tab`).classList.add('active');
    
    document.querySelectorAll('.admin-tabs .tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
}

function closeModal() {
    document.getElementById('rentModal').style.display = 'none';
}

function closeAddVehicleModal() {
    document.getElementById('addVehicleModal').style.display = 'none';
}

function showAddVehicleModal() {
    document.getElementById('addVehicleModal').style.display = 'block';
}

function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        padding: 12px 24px;
        background: ${type === 'success' ? '#48bb78' : '#f56565'};
        color: white;
        border-radius: 8px;
        z-index: 1000;
        animation: slideIn 0.3s ease;
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Add toast animation
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
`;
document.head.appendChild(style);