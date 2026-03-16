import type { VideoCollection } from '../types';
import './VideoCard.css';

const API_BASE = '';

interface VideoCardProps {
  video: VideoCollection;
  onClick?: (video: VideoCollection) => void;
}

export function VideoCard({ video, onClick }: VideoCardProps) {
  const handleClick = () => {
    onClick?.(video);
  };

  const thumbnailUrl = video.thumbnailUrl
    ? `${API_BASE}${video.thumbnailUrl}`
    : '/placeholder.jpg';

  return (
    <div className="video-card" onClick={handleClick}>
      <div className="video-thumbnail">
        <img src={thumbnailUrl} alt={video.title} />
        <div className="play-icon">&#9658;</div>
      </div>
      <div className="video-info">
        <div className="video-header">
          <h3 className="video-title">{video.title}</h3>
        </div>
        <div className="video-artist">{video.artist}</div>
        <div className="video-meta">
          <div className="video-meta-row">
            <span className="video-meta-label">Artist</span>
            <span className="video-meta-value">{video.artist}</span>
          </div>
          <div className="video-meta-row">
            <span className="video-meta-label">Views</span>
            <span className="video-meta-value">{video.viewCount.toLocaleString()}</span>
          </div>
        </div>
        <div className="video-tags">
          {video.tags.map((tag, index) => (
            <span key={index} className="tag">{tag}</span>
          ))}
        </div>
      </div>
    </div>
  );
}