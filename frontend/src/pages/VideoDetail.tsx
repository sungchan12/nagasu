import { useState, useEffect } from 'react';
import type { VideoDetails } from '../types';
import './VideoDetail.css';

const API_BASE = '';

type Props = {
  videoId: string;
  onBack?: () => void;
  privateMode?: boolean;
};

export function VideoDetail({ videoId, onBack, privateMode }: Props) {
  const [details, setDetails] = useState<VideoDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [descExpanded, setDescExpanded] = useState(false);

  useEffect(() => {
    const fetchDetails = async () => {
      try {
        setLoading(true);
        const query = privateMode ? '?private=true' : '';
        const response = await fetch(`${API_BASE}/api/videos/${videoId}/details${query}`, { credentials: 'include' });
        if (!response.ok) throw new Error('Failed to fetch details');
        const data = await response.json();
        setDetails(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };
    fetchDetails();
  }, [videoId, privateMode]);

  const handleDelete = async () => {
    try {
      setDeleting(true);
      const response = await fetch(`${API_BASE}/api/videos/${videoId}`, {
        method: 'DELETE',
        credentials: 'include',
      });
      if (!response.ok) throw new Error('Failed to delete video');
      onBack?.();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      setShowDeleteConfirm(false);
    } finally {
      setDeleting(false);
    }
  };

  if (loading) {
    return (
      <div className="yt-loading">
        <div className="yt-spinner" />
        Loading...
      </div>
    );
  }

  if (error) return <div className="yt-error">{error}</div>;
  if (!details) return <div className="yt-error">Video not found</div>;

  const artistInitial = details.artist ? details.artist.charAt(0) : '?';
  const hasDescription = !!details.description?.trim();
  const hasTags = details.tags && details.tags.length > 0;

  return (
    <div className="video-detail-page">
      {/* Video Player */}
      <div className="yt-player-wrapper">
        <video
          src={`${API_BASE}${details.videoUrl}`}
          controls
          poster={`${API_BASE}${details.thumbnailUrl}`}
        >
          {details.videoSubtitleUrl && (
            <track
              kind="subtitles"
              src={`${API_BASE}${details.videoSubtitleUrl}`}
              default
            />
          )}
        </video>
      </div>

      {/* Title */}
      <div className="yt-title-section">
        <h1 className="yt-title">{details.title}</h1>
      </div>

      {/* Channel Row + Actions */}
      <div className="yt-channel-row">
        <div className="yt-channel-info">
          <div className="yt-channel-avatar">{artistInitial}</div>
          <span className="yt-channel-name">{details.artist}</span>
        </div>

        <div className="yt-actions">
          <button
            className="yt-action-btn"
            onClick={() => {
              if (navigator.share) {
                navigator.share({ title: details.title, url: window.location.href });
              } else {
                navigator.clipboard.writeText(window.location.href);
              }
            }}
          >
            <svg viewBox="0 0 24 24"><path d="M15 5.63L20.66 12 15 18.37V14h-1c-3.96 0-7.14 1-9.75 3.09 1.84-4.07 5.11-6.4 9.89-7.1l.86-.13V5.63M14 3v6C6.22 10.13 3.11 15.33 2 21c2.78-3.97 6.44-6 12-6v6l8-9-8-9z" /></svg>
            Share
          </button>

          <button
            className="yt-action-btn danger"
            onClick={() => setShowDeleteConfirm(true)}
          >
            <svg viewBox="0 0 24 24"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z" /></svg>
            Delete
          </button>
        </div>
      </div>

      <hr className="yt-divider" />

      {/* Description Card */}
      {(hasDescription || hasTags) && (
        <div
          className={`yt-description-card${descExpanded ? ' expanded' : ''}`}
          onClick={() => !descExpanded && setDescExpanded(true)}
        >
          {hasTags && (
            <div className="yt-desc-meta">
              {details.tags.slice(0, 3).map((tag, i) => (
                <span key={i}>
                  {i > 0 && <span className="yt-meta-separator"> &middot; </span>}
                  #{tag}
                </span>
              ))}
            </div>
          )}

          {hasDescription && !descExpanded && (
            <div className="yt-desc-preview">{details.description}</div>
          )}

          {hasDescription && descExpanded && (
            <div className="yt-desc-full">{details.description}</div>
          )}

          {hasTags && descExpanded && (
            <div className="yt-tags">
              {details.tags.map((tag, i) => (
                <span key={i} className="yt-tag">#{tag}</span>
              ))}
            </div>
          )}

          {(hasDescription || (hasTags && details.tags.length > 3)) && (
            <button
              className="yt-desc-toggle"
              onClick={(e) => {
                e.stopPropagation();
                setDescExpanded(!descExpanded);
              }}
            >
              {descExpanded ? 'Show less' : '...more'}
            </button>
          )}
        </div>
      )}

      {/* Delete Confirmation Dialog */}
      {showDeleteConfirm && (
        <div className="yt-overlay" onClick={() => setShowDeleteConfirm(false)}>
          <div className="yt-dialog" onClick={(e) => e.stopPropagation()}>
            <h3>Delete video?</h3>
            <p>"{details.title}" will be permanently deleted.</p>
            <p className="yt-dialog-warn">This action cannot be undone.</p>
            <div className="yt-dialog-actions">
              <button
                className="yt-dialog-btn secondary"
                onClick={() => setShowDeleteConfirm(false)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                className="yt-dialog-btn destructive"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
