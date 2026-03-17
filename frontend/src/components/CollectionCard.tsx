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
        <h3 className="card-title">{collection.title}</h3>
        <div className="card-artist">{collection.artist}</div>
        {collection.tags.length > 0 && (
          <div className="card-tags">{collection.tags.join(', ')}</div>
        )}
        <div className="card-heart">&hearts;</div>
      </div>
    </div>
  );
}