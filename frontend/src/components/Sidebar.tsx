import './Sidebar.css';

// 'most-viewed' 타입을 추가했습니다.
type Page = 'list' | 'detail' | 'upload' | 'video-list' | 'video-detail' | 'video-upload' | 'most-viewed' | 'image-most-viewed';

type Props = {
  currentPage: Page;
  onNavigate: (page: Page) => void;
};

export function Sidebar({ currentPage, onNavigate }: Props) {
  // 현재 어떤 페이지군에 속해있는지 확인 (아이콘 색상 활성화용)
  const isVideoPage = ['video-list', 'video-detail', 'video-upload', 'most-viewed'].includes(currentPage);
  const isImagePage = ['list', 'detail', 'upload', 'image-most-viewed'].includes(currentPage);

  return (
    <nav className="sidebar">
      <div className="sidebar-title">Nagasu</div>

      {/* Videos Section - 항상 노출 */}
      <div className="sidebar-section">
        <button
          className={`sidebar-section-header ${isVideoPage ? 'active' : ''}`}
          onClick={() => onNavigate('video-list')}
        >
          <svg className="sidebar-section-icon" width="20" height="20" viewBox="0 0 90 90" fill="currentColor">
            <path d="M45 0C20.147 0 0 20.147 0 45c0 24.853 20.147 45 45 45s45-20.147 45-45C90 20.147 69.853 0 45 0zM62.251 46.633L37.789 60.756c-1.258.726-2.829-.181-2.829-1.633V30.877c0-1.452 1.572-2.36 2.829-1.634l24.461 14.123C63.508 44.092 63.508 45.907 62.251 46.633z"/>
          </svg>
          Videos
        </button>
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
        </div>
      </div>

      {/* Images Section - 항상 노출 */}
      <div className="sidebar-section">
        <button
          className={`sidebar-section-header ${isImagePage ? 'active' : ''}`}
          onClick={() => onNavigate('list')}
        >
          <span className="sidebar-section-icon">
            <svg viewBox="0 0 122.88 98.27" width="20" height="20" fill="currentColor">
              <path d="M4.84,27.31H90.76a4.77,4.77,0,0,1,3.4,1.41,4.84,4.84,0,0,1,1.41,3.4V93.47a4.75,4.75,0,0,1-1.41,3.39,1.36,1.36,0,0,1-.25.22,4.67,4.67,0,0,1-3.18,1.19H4.81A4.81,4.81,0,0,1,0,93.47V32.12a4.77,4.77,0,0,1,1.41-3.4,4.83,4.83,0,0,1,3.4-1.41ZM32.15,0h85.92a4.77,4.77,0,0,1,3.4,1.41,4.84,4.84,0,0,1,1.41,3.4V66.16a4.75,4.75,0,0,1-1.41,3.39,1.09,1.09,0,0,1-.25.22A4.67,4.67,0,0,1,118,71h-5.38V65.22h4.51V5.71H33.06v4.2H27.31V4.81a4.77,4.77,0,0,1,1.41-3.4A4.84,4.84,0,0,1,32.12,0ZM18.5,13.66h85.92a4.75,4.75,0,0,1,3.39,1.41,4.8,4.8,0,0,1,1.41,3.39V79.81a4.77,4.77,0,0,1-1.41,3.4,1.4,1.4,0,0,1-.25.22,4.67,4.67,0,0,1-3.18,1.19H99V78.88h4.51V19.37H19.4v4.2H13.65V18.46a4.81,4.81,0,0,1,4.81-4.8ZM24.68,44a6.9,6.9,0,1,1-6.89,6.89A6.89,6.89,0,0,1,24.68,44Zm29,29.59L67.49,49.71,82.14,86.77H13.77V82.18l5.74-.29,5.75-14.08,2.87,10.06h8.62l7.47-19.25L53.7,73.56ZM89.86,33H5.75V92.53H89.86V33Z"/>
            </svg>
          </span>
          Images
        </button>
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
        </div>
      </div>

      <div className="sidebar-divider" />

      {/* Upload - 블루 배경 버튼으로 변경 */}
      <button
        className="sidebar-upload-btn"
        onClick={() => onNavigate(isVideoPage ? 'video-upload' : 'upload')}
      >
        <span>➕</span>
        Upload New Media
      </button>
    </nav>
  );
}