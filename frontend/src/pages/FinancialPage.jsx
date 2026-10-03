import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import {
  HiOutlineCalendar,
  HiOutlineCheckCircle,
  HiOutlineClipboardList,
  HiOutlineCog,
  HiOutlineRefresh,
} from 'react-icons/hi';
import { getIncomeSummary } from '../api/financial';
import { extractErrorMessage } from '../api/axiosInstance';
import LoadingSpinner from '../components/common/LoadingSpinner';
import './FinancialPage.css';

const bookingStatuses = ['COMPLETED', 'PENDING', 'CONFIRMED', 'ACTIVE', 'CANCELLED'];

const toInputDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const getCurrentMonthRange = () => {
  const today = new Date();
  return {
    from: toInputDate(new Date(today.getFullYear(), today.getMonth(), 1)),
    to: toInputDate(today),
  };
};

const getLastThirtyDaysRange = () => {
  const today = new Date();
  const from = new Date(today);
  from.setDate(today.getDate() - 29);
  return { from: toInputDate(from), to: toInputDate(today) };
};

const getPreviousMonthRange = () => {
  const today = new Date();
  const firstOfCurrentMonth = new Date(today.getFullYear(), today.getMonth(), 1);
  const lastOfPreviousMonth = new Date(firstOfCurrentMonth);
  lastOfPreviousMonth.setDate(0);
  return {
    from: toInputDate(new Date(lastOfPreviousMonth.getFullYear(), lastOfPreviousMonth.getMonth(), 1)),
    to: toInputDate(lastOfPreviousMonth),
  };
};

const formatCurrency = (value) => new Intl.NumberFormat('en-LK', {
  style: 'currency',
  currency: 'LKR',
  minimumFractionDigits: 2,
}).format(Number(value || 0));

const formatDate = (value) => {
  if (!value) return '—';
  return new Date(`${value}T00:00:00`).toLocaleDateString('en-LK', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
};

export default function FinancialPage() {
  const [filters, setFilters] = useState({ ...getCurrentMonthRange(), status: 'COMPLETED' });
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadSummary = useCallback(async (nextFilters) => {
    setLoading(true);
    try {
      const data = await getIncomeSummary(nextFilters);
      setSummary(data);
    } catch (error) {
      toast.error(extractErrorMessage(error, 'Unable to load financial report.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSummary(filters);
  }, [loadSummary]);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  };

  const applyFilters = (event) => {
    event.preventDefault();
    if (filters.from > filters.to) {
      toast.error('The start date cannot be after the end date.');
      return;
    }
    loadSummary(filters);
  };

  const applyPreset = (range) => {
    const nextFilters = { ...filters, ...range };
    setFilters(nextFilters);
    loadSummary(nextFilters);
  };

  const resetFilters = () => {
    const nextFilters = { ...getCurrentMonthRange(), status: 'COMPLETED' };
    setFilters(nextFilters);
    loadSummary(nextFilters);
  };

  return (
    <div className="page-container financial-page" id="financial-page">
      <div className="page-header financial-page-header">
        <div>
          <h1 className="page-title">Financial Reports</h1>
          <p className="page-subtitle">Track booking revenue, maintenance expenses, and net income.</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={() => loadSummary(filters)} disabled={loading} id="financial-refresh-btn">
          <HiOutlineRefresh /> Refresh
        </button>
      </div>

      <form className="financial-filters" onSubmit={applyFilters}>
        <div className="financial-filter-field">
          <label htmlFor="financial-from">From</label>
          <input id="financial-from" name="from" type="date" value={filters.from} onChange={handleFilterChange} required />
        </div>
        <div className="financial-filter-field">
          <label htmlFor="financial-to">To</label>
          <input id="financial-to" name="to" type="date" value={filters.to} onChange={handleFilterChange} required />
        </div>
        <div className="financial-filter-field">
          <label htmlFor="financial-status">Booking status</label>
          <select id="financial-status" name="status" value={filters.status} onChange={handleFilterChange}>
            {bookingStatuses.map((status) => (
              <option value={status} key={status}>{status.replace(/_/g, ' ')}</option>
            ))}
          </select>
        </div>
        <div className="financial-filter-actions">
          <button className="btn btn-primary btn-sm" type="submit" disabled={loading}>Apply filters</button>
          <button className="btn btn-ghost btn-sm" type="button" onClick={resetFilters}>Reset</button>
        </div>
        <div className="financial-presets" aria-label="Report period presets">
          <span>Quick range</span>
          <button type="button" onClick={() => applyPreset(getCurrentMonthRange())}>This month</button>
          <button type="button" onClick={() => applyPreset(getLastThirtyDaysRange())}>Last 30 days</button>
          <button type="button" onClick={() => applyPreset(getPreviousMonthRange())}>Previous month</button>
        </div>
      </form>

      {loading && !summary ? (
        <LoadingSpinner message="Loading financial report..." />
      ) : (
        <>
          <div className="financial-period" role="status">
            <HiOutlineCalendar />
            <span>
              {summary?.status?.replace(/_/g, ' ') || filters.status.replace(/_/g, ' ')} bookings
              <strong>{formatDate(summary?.from || filters.from)} - {formatDate(summary?.to || filters.to)}</strong>
            </span>
          </div>

          <div className="financial-summary-grid">
            <div className="financial-summary-card financial-summary-card--gross">
              <div className="financial-card-icon"><HiOutlineClipboardList /></div>
              <div><span>Gross revenue</span><strong>{formatCurrency(summary?.grossRevenue)}</strong><small>Booking income before costs</small></div>
            </div>
            <div className="financial-summary-card financial-summary-card--cost">
              <div className="financial-card-icon"><HiOutlineCog /></div>
              <div><span>Maintenance cost</span><strong>-{formatCurrency(summary?.maintenanceCost)}</strong><small>Completed maintenance in period</small></div>
            </div>
            <div className="financial-summary-card financial-summary-card--net">
              <div className="financial-card-icon"><HiOutlineCheckCircle /></div>
              <div><span>Net revenue</span><strong>{formatCurrency(summary?.netRevenue)}</strong><small>Gross revenue less maintenance</small></div>
            </div>
            <div className="financial-summary-card financial-summary-card--count">
              <div className="financial-card-icon"><HiOutlineCalendar /></div>
              <div><span>Bookings included</span><strong>{summary?.bookingCount || 0}</strong><small>Matching the selected status</small></div>
            </div>
          </div>

          {summary?.bookingCount === 0 && (
            <div className="financial-empty-state">
              No bookings found for this period and status. The report totals are currently zero.
            </div>
          )}
        </>
      )}
    </div>
  );
}
