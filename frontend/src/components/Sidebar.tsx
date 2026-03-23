import { useState } from 'react';
import './Sidebar.css';

type Page = 'list' | 'detail' | 'upload' | 'video-list' | 'video-detail' | 'video-upload' | 'most-viewed' | 'image-most-viewed' | 'private-video-list' | 'private-image-list';

type Props = {
  currentPage: Page;
  onNavigate: (page: Page) => void;
  privateMode: boolean;
};

export function Sidebar({ currentPage, onNavigate, privateMode }: Props) {
  const [collapsed, setCollapsed] = useState(false);
  const [expandedSection, setExpandedSection] = useState<'videos' | 'images' | null>(() => {
    if (['video-list', 'video-detail', 'video-upload', 'most-viewed', 'private-video-list'].includes(currentPage)) return 'videos';
    if (['list', 'detail', 'upload', 'image-most-viewed', 'private-image-list'].includes(currentPage)) return 'images';
    return null;
  });

  const isVideoPage = ['video-list', 'video-detail', 'video-upload', 'most-viewed', 'private-video-list'].includes(currentPage);
  const isImagePage = ['list', 'detail', 'upload', 'image-most-viewed', 'private-image-list'].includes(currentPage);

  const toggleSection = (section: 'videos' | 'images') => {
    if (collapsed) {
      setCollapsed(false);
      setExpandedSection(section);
      return;
    }
    setExpandedSection(prev => prev === section ? null : section);
  };

  return (
    <nav className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      {/* Header */}
      <div className="sidebar-header">
        {!collapsed && <div className="sidebar-title">Nagasu</div>}
        <button className="sidebar-toggle" onClick={() => setCollapsed(c => !c)}>
          {collapsed ? '›' : '‹'}
        </button>
      </div>

      {/* MEDIA Section */}
      <div className="sidebar-section-label">{collapsed ? '' : 'MEDIA'}</div>

      {/* Videos */}
      <div className="sidebar-section">
        <button
          className={`sidebar-item ${isVideoPage ? 'active' : ''}`}
          onClick={() => toggleSection('videos')}
          title={collapsed ? 'Videos' : undefined}
        >
          <svg className="sidebar-icon" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="5 3 19 12 5 21 5 3" />
          </svg>
          {!collapsed && (
            <>
              <span className="sidebar-item-text">Videos</span>
              <svg className={`sidebar-chevron ${expandedSection === 'videos' ? 'open' : ''}`} width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </>
          )}
          {collapsed && <span className="sidebar-tooltip">Videos</span>}
        </button>
        {!collapsed && expandedSection === 'videos' && (
          <div className="sidebar-sub-items">
            <button
              className={`sidebar-sub-item ${currentPage === 'video-list' ? 'active' : ''}`}
              onClick={() => onNavigate('video-list')}
            >
              Recently Upload
            </button>
            <button
              className={`sidebar-sub-item ${currentPage === 'most-viewed' ? 'active' : ''}`}
              onClick={() => onNavigate('most-viewed')}
            >
              Most Viewed
            </button>
            {privateMode && (
              <button
                className={`sidebar-sub-item ${currentPage === 'private-video-list' ? 'active' : ''}`}
                onClick={() => onNavigate('private-video-list')}
              >
                Private
              </button>
            )}
          </div>
        )}
      </div>

      {/* Images */}
      <div className="sidebar-section">
        <button
          className={`sidebar-item ${isImagePage ? 'active' : ''}`}
          onClick={() => toggleSection('images')}
          title={collapsed ? 'Images' : undefined}
        >
          <svg className="sidebar-icon" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            <circle cx="8.5" cy="8.5" r="1.5" />
            <polyline points="21 15 16 10 5 21" />
          </svg>
          {!collapsed && (
            <>
              <span className="sidebar-item-text">Images</span>
              <svg className={`sidebar-chevron ${expandedSection === 'images' ? 'open' : ''}`} width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </>
          )}
          {collapsed && <span className="sidebar-tooltip">Images</span>}
        </button>
        {!collapsed && expandedSection === 'images' && (
          <div className="sidebar-sub-items">
            <button
              className={`sidebar-sub-item ${currentPage === 'list' ? 'active' : ''}`}
              onClick={() => onNavigate('list')}
            >
              Recently Upload
            </button>
            <button
              className={`sidebar-sub-item ${currentPage === 'image-most-viewed' ? 'active' : ''}`}
              onClick={() => onNavigate('image-most-viewed')}
            >
              Most Viewed
            </button>
            {privateMode && (
              <button
                className={`sidebar-sub-item ${currentPage === 'private-image-list' ? 'active' : ''}`}
                onClick={() => onNavigate('private-image-list')}
              >
                Private
              </button>
            )}
          </div>
        )}
      </div>

      <div className="sidebar-divider" />

      {/* UPLOAD Section */}
      <div className="sidebar-section-label">{collapsed ? '' : 'UPLOAD'}</div>

      <button
        className="sidebar-item upload-item"
        onClick={() => onNavigate(isVideoPage ? 'video-upload' : 'upload')}
        title={collapsed ? 'Upload' : undefined}
      >
        <svg className="sidebar-icon" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
        {!collapsed && <span className="sidebar-item-text">Upload Media</span>}
        {collapsed && <span className="sidebar-tooltip">Upload</span>}
      </button>
    </nav>
  );
}