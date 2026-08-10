import { HiOutlineInbox } from 'react-icons/hi';
import './EmptyState.css';

export default function EmptyState({ title = 'No data found', message = 'There are no records to display.', icon }) {
  return (
    <div className="empty-state" id="empty-state">
      <div className="empty-icon">
        {icon || <HiOutlineInbox />}
      </div>
      <h3 className="empty-title">{title}</h3>
      <p className="empty-message">{message}</p>
    </div>
  );
}
