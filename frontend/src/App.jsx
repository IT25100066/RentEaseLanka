import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// Auth
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';

// Layouts
import CustomerLayout from './components/layout/CustomerLayout';
import AdminLayout from './components/layout/AdminLayout';
import ManagerLayout from './components/layout/ManagerLayout';
import StaffLayout from './components/layout/StaffLayout';
import DriverLayout from './components/layout/DriverLayout';

// Public / Customer Pages
import HomePage from './pages/HomePage';
import VehiclesPage from './pages/VehiclesPage';
import CustomerVehicleDetail from './pages/CustomerVehicleDetail';
import UnauthorizedPage from './pages/UnauthorizedPage';

// Auth Pages
import SignIn from './pages/Auth/SignIn';
import SignUp from './pages/Auth/SignUp';

// Customer Protected Pages
import MyBookingsPage from './pages/Customer/MyBookingsPage';
import NicUploadPage from './pages/Customer/NicUploadPage';

// Admin Pages
import AdminDashboard from './pages/Admin/AdminDashboard';
import CreateManagerPage from './pages/Admin/CreateManagerPage';

// Manager & Staff Shared Pages
import FleetPage from './pages/FleetPage';
import CategoriesPage from './pages/CategoriesPage';
import PendingVehiclesPage from './pages/PendingVehiclesPage';
import BookingsPage from './pages/BookingsPage';
import MaintenancePage from './pages/MaintenancePage';
import VehicleDetailPage from './pages/VehicleDetailPage';
import FinancialPage from './pages/FinancialPage';

// Manager Pages
import ManagerDashboard from './pages/Manager/ManagerDashboard';
import CreateStaffPage from './pages/Manager/CreateStaffPage';
import CreateDriverPage from './pages/Manager/CreateDriverPage';

// Staff Pages
import StaffDashboard from './pages/Staff/StaffDashboard';
import PendingCustomersPage from './pages/Staff/PendingCustomersPage';
import HandoverPage from './pages/Staff/HandoverPage';
import VehicleReturnPage from './pages/Staff/VehicleReturnPage';

// Driver Pages
import DriverDashboardPage from './pages/Driver/DriverDashboardPage';
import DriverTripsPage from './pages/Driver/DriverTripsPage';
import DriverProfilePage from './pages/Driver/DriverProfilePage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* ===== Auth Routes ===== */}
          <Route path="/signin" element={<SignIn />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/* ===== Public Customer-Facing Routes ===== */}
          <Route element={<CustomerLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/vehicles" element={<VehiclesPage />} />
            <Route path="/vehicles/:id" element={<CustomerVehicleDetail />} />
          </Route>

          {/* ===== Customer Protected Routes ===== */}
          <Route element={<ProtectedRoute allowedRoles={['CUSTOMER']} />}>
            <Route element={<CustomerLayout />}>
              <Route path="/my/bookings" element={<MyBookingsPage />} />
              <Route path="/my/nic-upload" element={<NicUploadPage />} />
            </Route>
          </Route>

          {/* ===== Admin Routes (ADMIN only) ===== */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route element={<AdminLayout />}>
              <Route path="/admin" element={<AdminDashboard />} />
              <Route path="/admin/fleet" element={<FleetPage />} />
              <Route path="/admin/bookings" element={<BookingsPage />} />
              <Route path="/admin/pending-vehicles" element={<PendingVehiclesPage />} />
              <Route path="/admin/categories" element={<CategoriesPage />} />
              <Route path="/admin/maintenance" element={<MaintenancePage />} />
              <Route path="/admin/financial" element={<FinancialPage />} />
              <Route path="/admin/handovers" element={<HandoverPage />} />
              <Route path="/admin/returns" element={<VehicleReturnPage />} />
              <Route path="/admin/managers" element={<CreateManagerPage />} />
              <Route path="/admin/vehicle/:id" element={<VehicleDetailPage />} />
            </Route>
          </Route>

          {/* ===== Manager Routes (MANAGER only) ===== */}
          <Route element={<ProtectedRoute allowedRoles={['MANAGER']} />}>
            <Route element={<ManagerLayout />}>
              <Route path="/manager" element={<ManagerDashboard />} />
              <Route path="/manager/fleet" element={<FleetPage />} />
              <Route path="/manager/pending-vehicles" element={<PendingVehiclesPage />} />
              <Route path="/manager/bookings" element={<BookingsPage />} />
              <Route path="/manager/categories" element={<CategoriesPage />} />
              <Route path="/manager/handovers" element={<HandoverPage />} />
              <Route path="/manager/returns" element={<VehicleReturnPage />} />
              <Route path="/manager/financial" element={<FinancialPage />} />
              <Route path="/manager/staff" element={<CreateStaffPage />} />
              <Route path="/manager/drivers" element={<CreateDriverPage />} />
              <Route path="/manager/vehicle/:id" element={<VehicleDetailPage />} />
            </Route>
          </Route>

          {/* ===== Staff Routes (STAFF only) ===== */}
          <Route element={<ProtectedRoute allowedRoles={['STAFF']} />}>
            <Route element={<StaffLayout />}>
              <Route path="/staff" element={<StaffDashboard />} />
              <Route path="/staff/fleet" element={<FleetPage />} />
              <Route path="/staff/bookings" element={<BookingsPage />} />
              <Route path="/staff/handovers" element={<HandoverPage />} />
              <Route path="/staff/returns" element={<VehicleReturnPage />} />
              <Route path="/staff/pending-customers" element={<PendingCustomersPage />} />
              <Route path="/staff/maintenance" element={<MaintenancePage />} />
              <Route path="/staff/vehicle/:id" element={<VehicleDetailPage />} />
            </Route>
          </Route>

          {/* ===== Driver Routes (DRIVER only) ===== */}
          <Route element={<ProtectedRoute allowedRoles={['DRIVER']} />}>
            <Route element={<DriverLayout />}>
              <Route path="/driver" element={<DriverDashboardPage />} />
              <Route path="/driver/trips" element={<DriverTripsPage />} />
              <Route path="/driver/profile" element={<DriverProfilePage />} />
            </Route>
          </Route>

        </Routes>

        <ToastContainer
          position="top-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop
          closeOnClick
          pauseOnFocusLoss
          draggable
          pauseOnHover
          theme="light"
        />
      </BrowserRouter>
    </AuthProvider>
  );
}
