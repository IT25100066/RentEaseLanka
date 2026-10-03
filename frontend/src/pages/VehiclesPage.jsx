import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import API, { BASE_URL } from '../api/axiosInstance';
import {
  HiOutlineSearch,
  HiOutlineFilter,
  HiOutlineArrowRight,
  HiOutlineLocationMarker,
  HiOutlineTruck,
  HiOutlineX,
} from 'react-icons/hi';
import StatusBadge from '../components/common/StatusBadge';
import './VehiclesPage.css';


export default function VehiclesPage() {
  const [vehicles, setVehicles] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [sortBy, setSortBy] = useState('default');
  const navigate = useNavigate();

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        const [vehiclesRes, catRes] = await Promise.all([
          API.get('/vehicles'),
          API.get('/category'),
        ]);
        const available = vehiclesRes.data.filter((v) => v.status != 'OUT_OF_SERVICE');
        setVehicles(available);
        setCategories(catRes.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  const filteredVehicles = vehicles
    .filter((v) => {
      const matchesSearch = v.model.toLowerCase().includes(searchQuery.toLowerCase()) ||
        v.licensePlate.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesCategory = !selectedCategory || v.categoryName === selectedCategory;
      return matchesSearch && matchesCategory;
    })
    .sort((a, b) => {
      if (sortBy === 'price-low') return (a.dailyRentalRate || 0) - (b.dailyRentalRate || 0);
      if (sortBy === 'price-high') return (b.dailyRentalRate || 0) - (a.dailyRentalRate || 0);
      if (sortBy === 'year-new') return (b.year || 0) - (a.year || 0);
      return 0;
    });

  const clearFilters = () => {
    setSearchQuery('');
    setSelectedCategory('');
    setSortBy('default');
  };

  const hasActiveFilters = searchQuery || selectedCategory || sortBy !== 'default';

  return (
    <div className="vehicles-page" id="vehicles-page">
      {/* Page Header */}
      <div className="vehicles-header">
        <div className="vehicles-header-content">
          <h1 className="vehicles-header-title">Our Vehicles</h1>
          <p className="vehicles-header-subtitle">
            Browse our collection of well-maintained vehicles, ready for your next adventure
          </p>
        </div>
      </div>

      <div className="vehicles-main">
        {/* Filters Bar */}
        <div className="vehicles-filters" id="vehicles-filters">
          <div className="filter-search">
            <HiOutlineSearch className="filter-search-icon" />
            <input
              type="text"
              placeholder="Search by model or plate..."
              className="filter-search-input"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              id="vehicle-search-input"
            />
          </div>

          <div className="filter-controls">
            <div className="filter-select-wrap">
              <HiOutlineFilter className="filter-select-icon" />
              <select
                className="filter-select-input"
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                id="category-filter-select"
              >
                <option value="">All Categories</option>
                {categories.map((cat) => (
                  <option key={cat.id} value={cat.name}>
                    {cat.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-select-wrap">
              <select
                className="filter-select-input"
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                id="sort-select"
              >
                <option value="default">Sort by</option>
                <option value="price-low">Price: Low to High</option>
                <option value="price-high">Price: High to Low</option>
                <option value="year-new">Newest First</option>
              </select>
            </div>

            {hasActiveFilters && (
              <button className="filter-clear-btn" onClick={clearFilters} id="clear-all-filters">
                <HiOutlineX />
                Clear
              </button>
            )}
          </div>

          <div className="filter-results-count">
            <span>{filteredVehicles.length} vehicle{filteredVehicles.length !== 1 ? 's' : ''} found</span>
          </div>
        </div>

        {/* Vehicle Grid */}
        {loading ? (
          <div className="vehicles-loading">
            <div className="loading-pulse"></div>
            <p>Loading vehicles...</p>
          </div>
        ) : filteredVehicles.length === 0 ? (
          <div className="vehicles-empty">
            <HiOutlineTruck className="vehicles-empty-icon" />
            <h3>No vehicles found</h3>
            <p>Try adjusting your filters or search query</p>
            {hasActiveFilters && (
              <button className="hero-btn hero-btn-primary" onClick={clearFilters}>
                Clear Filters
              </button>
            )}
          </div>
        ) : (
          <div className="vehicles-grid">
            {filteredVehicles.map((vehicle, index) => (
              <div
                key={vehicle.id}
                className="rental-card"
                style={{ animationDelay: `${index * 0.05}s` }}
                onClick={() => navigate(`/vehicles/${vehicle.id}`)}
                id={`rental-card-${vehicle.id}`}
              >
                <div className="rental-card-image">
                  {vehicle.imageUrls && vehicle.imageUrls.length > 0 ? (
                    <img
                      src={`${BASE_URL}${vehicle.imageUrls[0].startsWith('/') ? '' : '/'}${vehicle.imageUrls[0]}`}
                      alt={vehicle.model}
                      loading="lazy"
                      onError={(e) => {
                        e.target.style.display = 'none';
                        if (e.target.nextSibling) e.target.nextSibling.style.display = 'flex';
                      }}
                    />
                  ) : null}
                  <div className="rental-no-image" style={{ display: vehicle.imageUrls && vehicle.imageUrls.length > 0 ? 'none' : 'flex' }}>
                    <span>🚗</span>
                  </div>
                  <div className="rental-card-category">
                    {vehicle.categoryName || 'Vehicle'}
                  </div>
                  <div className="rental-card-status">
                    <StatusBadge status={vehicle.status} />
                  </div>
                </div>
                <div className="rental-card-body">
                  <div className="rental-card-top">
                    <h3 className="rental-card-model">{vehicle.model}</h3>
                    <span className="rental-card-year">{vehicle.year}</span>
                  </div>
                  <div className="rental-card-details">
                    <span className="rental-detail">
                      <HiOutlineLocationMarker />
                      {vehicle.licensePlate}
                    </span>
                    <span className="rental-detail">
                      {vehicle.currentOdometer?.toLocaleString()} km
                    </span>
                  </div>
                  <div className="rental-card-footer">
                    <div className="rental-price">
                      <span className="rental-price-amount">
                        LKR {vehicle.dailyRentalRate?.toLocaleString()}
                      </span>
                      <span className="rental-price-per">/day</span>
                    </div>
                    <button className="rental-btn" id={`rent-now-${vehicle.id}`}>
                      Rent Now
                      <HiOutlineArrowRight />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
