import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// Layouts
import CustomerLayout from './components/layout/CustomerLayout';
import AdminLayout from './components/layout/AdminLayout';

// Customer Pages
import HomePage from './pages/HomePage';
import VehiclesPage from './pages/VehiclesPage';
import CustomerVehicleDetail from './pages/CustomerVehicleDetail';

// Admin Pages
import Dashboard from './pages/Dashboard';
import FleetPage from './pages/FleetPage';
import VehicleDetailPage from './pages/VehicleDetailPage';
import PendingVehiclesPage from './pages/PendingVehiclesPage';
import CategoriesPage from './pages/CategoriesPage';
import MaintenancePage from './pages/MaintenancePage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* ===== Customer-Facing Routes ===== */}
        <Route element={<CustomerLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/vehicles" element={<VehiclesPage />} />
          <Route path="/vehicles/:id" element={<CustomerVehicleDetail />} />
        </Route>

        {/* ===== Admin Panel Routes ===== */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Dashboard />} />
          <Route path="fleet" element={<FleetPage />} />
          <Route path="vehicle/:id" element={<VehicleDetailPage />} />
          <Route path="pending" element={<PendingVehiclesPage />} />
          <Route path="categories" element={<CategoriesPage />} />
          <Route path="maintenance" element={<MaintenancePage />} />
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
  );
}
