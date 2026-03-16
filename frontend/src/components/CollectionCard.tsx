import type { ImageCollection } from '../types';
import './CollectionCard.css';

const API_BASE = '';

interface CollectionCardProps {
  collection: ImageCollection;
  onClick?: (collection: ImageCollection) => void;
}

export function CollectionCard({ collection, onClick }: CollectionCardProps) {
  const handleClick = () => {
    onClick?.(collection);
  };

  const thumbnailUrl = `${API_BASE}${collection.thumbnailUrl}`;

  return (
    <div className="collection-card" onClick={handleClick}>
      <div className="card-thumbnail">
        <img src={thumbnailUrl} alt={collection.title} />
      </div>
      <div className="card-info">
        <div className="card-header">
          <h3 className="card-title">{collection.title}</h3>
        </div>
        <div className="card-artist">{collection.artist}</div>
        <div className="card-meta">
          <div className="card-meta-row">
            <span className="card-meta-label">Artist</span>
            <span className="card-meta-value">{collection.artist}</span>
          </div>
        </div>
        <div className="card-tags">
          {collection.tags.map((tag, index) => (
            <span key={index} className="tag">{tag}</span>
          ))}
        </div>
      </div>
    </div>
  );
}