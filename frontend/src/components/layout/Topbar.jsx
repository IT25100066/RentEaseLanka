import { useLocation } from 'react-router-dom';
import { HiOutlineSearch } from 'react-icons/hi';
import './Topbar.css';

const pageTitles = {
  '/admin': 'Dashboard',
  '/admin/fleet': 'Fleet Management',
  '/admin/pending': 'Pending Approvals',
  '/admin/categories': 'Category Management',
  '/admin/maintenance': 'Maintenance Management',
};

export default function Topbar() {
  const location = useLocation();

  const getTitle = () => {
    if (location.pathname.startsWith('/admin/vehicle/')) return 'Vehicle Details';
    return pageTitles[location.pathname] || 'RentEaseLanka';
  };

  return (
    <header className="topbar" id="topbar">
      <div className="topbar-left">
        <h2 className="topbar-title">{getTitle()}</h2>
      </div>
      <div className="topbar-right">
        <div className="topbar-search">
          <HiOutlineSearch className="search-icon" />
          <input
            type="text"
            placeholder="Search vehicles..."
            className="search-input"
            id="global-search"
          />
        </div>
        <div className="topbar-avatar" id="user-avatar">
          <span>A</span>
        </div>
      </div>
    </header>
  );
}
