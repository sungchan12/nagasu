import { useState, useEffect } from 'react';
import { SearchBar } from '../components/SearchBar';
import { CollectionCard } from '../components/CollectionCard';
import type { ImageCollection } from '../types';
import './CollectionList.css';

const API_BASE = '';

type Props = {
  onSelectCollection: (id: string, isPrivate?: boolean) => void;
  onUploadClick: () => void;
  onVideoClick: () => void;
  privateMode?: boolean;
};

export function CollectionList({ onSelectCollection, onUploadClick, onVideoClick, privateMode }: Props) {
  const [collections, setCollections] = useState<ImageCollection[]>([]);
  const [filteredCollections, setFilteredCollections] = useState<ImageCollection[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCollections = async () => {
      try {
        setLoading(true);
        const params = new URLSearchParams();
        if (privateMode) params.set('private', 'true');
        const query = params.toString();
        const response = await fetch(`${API_BASE}/api/images${query ? `?${query}` : ''}`, { credentials: 'include' });
        if (!response.ok) throw new Error('Failed to fetch collections');
        const data = await response.json();
        setCollections(data);
        setFilteredCollections(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    fetchCollections();
  }, []);

  const handleSearch = (query: string) => {
    if (!query.trim()) {
      setFilteredCollections(collections);
      return;
    }

    const lowerQuery = query.toLowerCase();
    const filtered = collections.filter(
      (c) =>
        c.title.toLowerCase().includes(lowerQuery) ||
        c.artist.toLowerCase().includes(lowerQuery) ||
        c.tags.some((tag) => tag.toLowerCase().includes(lowerQuery))
    );
    setFilteredCollections(filtered);
  };

  const handleCollectionClick = (collection: ImageCollection) => {
    onSelectCollection(collection.id, privateMode);
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">Error: {error}</div>;

  return (
    <div className="collection-list-page">
      <div className="list-header">
        <SearchBar onSearch={handleSearch} />
      </div>
      <div className="collection-list">
        {filteredCollections.map((collection) => (
          <CollectionCard
            key={collection.id}
            collection={collection}
            onClick={handleCollectionClick}
          />
        ))}
        {filteredCollections.length === 0 && (
          <div className="no-results">No collections found</div>
        )}
      </div>
    </div>
  );
}