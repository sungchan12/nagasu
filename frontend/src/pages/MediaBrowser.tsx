import { useState, useEffect, useMemo } from 'react';
import type { ImageCollection, VideoCollection } from '../types';
import './MediaBrowser.css';

const API_BASE = '';

type MediaFilter = 'all' | 'images' | 'videos';

type MediaItem =
  | { type: 'image'; data: ImageCollection }
  | { type: 'video'; data: VideoCollection };

type Props = {
  onSelectImage: (id: string, isPrivate?: boolean) => void;
  onSelectVideo: (id: string, isPrivate?: boolean) => void;
  privateMode?: boolean;
};

export function MediaBrowser({ onSelectImage, onSelectVideo, privateMode = false }: Props) {
  const [images, setImages] = useState<ImageCollection[]>([]);
  const [videos, setVideos] = useState<VideoCollection[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<MediaFilter>('all');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      setError(null);
      try {
        const params = privateMode ? '?private=true' : '';
        const [imgRes, vidRes] = await Promise.all([
          fetch(`${API_BASE}/api/images${params}`, { credentials: 'include' }),
          fetch(`${API_BASE}/api/videos${params}`, { credentials: 'include' }),
        ]);
        if (!imgRes.ok || !vidRes.ok) throw new Error('Failed to fetch collections');
        const [imgData, vidData] = await Promise.all([imgRes.json(), vidRes.json()]);
        setImages(imgData);
        setVideos(vidData);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [privateMode]);

  const items = useMemo<MediaItem[]>(() => {
    let result: MediaItem[] = [];

    if (filter === 'all' || filter === 'images') {
      result.push(...images.map((img): MediaItem => ({ type: 'image', data: img })));
    }
    if (filter === 'all' || filter === 'videos') {
      result.push(...videos.map((vid): MediaItem => ({ type: 'video', data: vid })));
    }

    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter((item) => {
        const d = item.data;
        return (
          d.title.toLowerCase().includes(q) ||
          d.artist.toLowerCase().includes(q) ||
          d.tags.some((t) => t.toLowerCase().includes(q))
        );
      });
    }

    return result;
  }, [images, videos, filter, search]);

  const handleCardClick = (item: MediaItem) => {
    if (item.type === 'image') {
      onSelectImage(item.data.id, privateMode);
    } else {
      onSelectVideo(item.data.id, privateMode);
    }
  };

  return (
    <div className="media-browser">
      {/* Header */}
      <header className="mb-header">
        <div className="mb-overline">The Collection</div>
        <h1 className="mb-title">Curated Media</h1>
        <p className="mb-subtitle">
          A carefully arranged archive of visual works — browse, discover, and immerse.
        </p>
      </header>

      {/* Controls */}
      <div className="mb-controls">
        <div className="mb-filters">
          {(['all', 'images', 'videos'] as MediaFilter[]).map((f) => (
            <button
              key={f}
              className={`mb-filter-btn ${filter === f ? 'active' : ''}`}
              onClick={() => setFilter(f)}
            >
              {f === 'all' ? 'All' : f === 'images' ? 'Images' : 'Videos'}
            </button>
          ))}
        </div>
        <div className="mb-search">
          <input
            className="mb-search-input"
            type="text"
            placeholder="Search collections..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <svg className="mb-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
        </div>
      </div>

      {/* Divider */}
      <div className="mb-divider" />

      {/* Count */}
      {!loading && !error && (
        <div className="mb-count">
          <span className="mb-count-number">{items.length}</span>
          {' '}work{items.length !== 1 ? 's' : ''} in collection
        </div>
      )}

      {/* States */}
      {loading && (
        <div className="mb-loading">
          <div className="mb-loading-spinner" />
          <div className="mb-loading-text">Curating</div>
        </div>
      )}

      {error && <div className="mb-error">{error}</div>}

      {!loading && !error && items.length === 0 && (
        <div className="mb-empty">
          <div className="mb-empty-icon">&#9671;</div>
          <div className="mb-empty-title">Nothing here yet</div>
          <div className="mb-empty-text">
            {search ? 'No results match your search.' : 'Upload some media to begin curating.'}
          </div>
        </div>
      )}

      {/* Grid */}
      {!loading && !error && items.length > 0 && (
        <div className="mb-grid">
          {items.map((item, i) => {
            const d = item.data;
            const thumbUrl = d.thumbnailUrl
              ? `${API_BASE}${d.thumbnailUrl}`
              : null;
            const isVideo = item.type === 'video';
            const viewCount = isVideo ? (d as VideoCollection).viewCount : null;

            return (
              <article
                key={`${item.type}-${d.id}`}
                className="mb-card"
                style={{ animationDelay: `${0.5 + i * 0.06}s` }}
                onClick={() => handleCardClick(item)}
              >
                {/* Image */}
                <div className="mb-card-image-wrap">
                  {thumbUrl ? (
                    <img
                      className="mb-card-image"
                      src={thumbUrl}
                      alt={d.title}
                      loading={i < 5 ? 'eager' : 'lazy'}
                    />
                  ) : (
                    <div className="mb-card-no-thumb">
                      <span>{d.title.charAt(0)}</span>
                    </div>
                  )}

                  {/* Type badge */}
                  <div className="mb-card-type">
                    {isVideo ? 'Film' : 'Gallery'}
                  </div>

                  {/* Video play icon */}
                  {isVideo && (
                    <div className="mb-card-play">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="none">
                        <polygon points="6 3 20 12 6 21 6 3" />
                      </svg>
                    </div>
                  )}

                  {/* View count */}
                  {viewCount != null && viewCount > 0 && (
                    <div className="mb-card-views">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                      {viewCount.toLocaleString()}
                    </div>
                  )}
                </div>

                {/* Body */}
                <div className="mb-card-body">
                  <div className="mb-card-overline">{d.artist || 'Unknown'}</div>
                  <h3 className="mb-card-title">{d.title}</h3>
                  {d.tags.length > 0 && (
                    <div className="mb-card-tags">
                      {d.tags.slice(0, 3).map((tag) => (
                        <span key={tag} className="mb-tag">{tag}</span>
                      ))}
                      {d.tags.length > 3 && (
                        <span className="mb-tag">+{d.tags.length - 3}</span>
                      )}
                    </div>
                  )}
                </div>

                {/* Gold accent line */}
                <div className="mb-card-accent" />
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}