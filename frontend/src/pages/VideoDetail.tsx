import { useState, useEffect } from 'react';
import type { VideoDetails } from '../types';
import './VideoDetail.css';

const API_BASE = '';

type Props = {
  videoId: string;
  onBack: () => void;
};

export function VideoDetail({ videoId, onBack }: Props) {
  const [details, setDetails] = useState<VideoDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    const fetchDetails = async () => {
      try {
        setLoading(true);
        const response = await fetch(`${API_BASE}/api/videos/${videoId}/details`, { credentials: 'include' });
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
  }, [videoId]);

  const handleDelete = async () => {
    try {
      setDeleting(true);
      const response = await fetch(`${API_BASE}/api/videos/${videoId}`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (!response.ok) throw new Error('Failed to delete video');

      onBack();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      setShowDeleteConfirm(false);
    } finally {
      setDeleting(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">Error: {error}</div>;
  if (!details) return <div className="error">Video not found</div>;

  return (
    <div className="video-detail-page">
      <button className="back-button" onClick={onBack}>
        &larr; Back
      </button>

      <div className="video-player-section">
        <video
          className="video-player"
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

      <div className="video-detail-info">
        <h1 className="detail-title">{details.title}</h1>
        <p className="detail-artist">{details.artist}</p>
        <div className="detail-tags">
          {details.tags.map((tag, index) => (
            <span key={index} className="tag">{tag}</span>
          ))}
        </div>
        {details.description && (
          <p className="detail-description">{details.description}</p>
        )}
        <button className="delete-button" onClick={() => setShowDeleteConfirm(true)}>
          Delete Video
        </button>
      </div>

      {showDeleteConfirm && (
        <div className="delete-confirm-overlay" onClick={() => setShowDeleteConfirm(false)}>
          <div className="delete-confirm-dialog" onClick={(e) => e.stopPropagation()}>
            <h2>Delete Video?</h2>
            <p>Are you sure you want to delete "{details.title}"?</p>
            <p className="warning">This action cannot be undone.</p>
            <div className="dialog-buttons">
              <button
                className="cancel-button"
                onClick={() => setShowDeleteConfirm(false)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                className="confirm-delete-button"
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