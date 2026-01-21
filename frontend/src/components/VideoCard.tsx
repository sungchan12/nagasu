import type { VideoCollection } from '../types';
import './VideoCard.css';

const API_BASE = 'http://localhost:8080';

interface VideoCardProps {
  video: VideoCollection;
  onClick?: (video: VideoCollection) => void;
}

export function VideoCard({ video, onClick }: VideoCardProps) {
  const handleClick = () => {
    onClick?.(video);
  };

  const thumbnailUrl = `${API_BASE}${video.thumbnailUrl}`;

  return (
    <div className="video-card" onClick={handleClick}>
      <div className="video-thumbnail">
        <img src={thumbnailUrl} alt={video.title} />
        <div className="play-icon">&#9658;</div>
      </div>
      <div className="video-info">
        <h3 className="video-title">{video.title}</h3>
        <p className="video-artist">{video.artist}</p>
        <div className="video-tags">
          {video.tags.map((tag, index) => (
            <span key={index} className="tag">{tag}</span>
          ))}
        </div>
      </div>
    </div>
  );
}