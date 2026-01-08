import { useState, useEffect } from 'react';
import type { ImageDetails } from '../types';
import './CollectionDetail.css';

const API_BASE = 'http://localhost:8080';

type Props = {
  collectionId: string;
  onBack: () => void;
};

export function CollectionDetail({ collectionId, onBack }: Props) {
  const [details, setDetails] = useState<ImageDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [viewerIndex, setViewerIndex] = useState<number | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    fetchDetails();
  }, [collectionId]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (viewerIndex === null || !details) return;
      if (e.key === 'ArrowLeft') {
        setViewerIndex((prev) => (prev! > 0 ? prev! - 1 : details.images.length - 1));
      } else if (e.key === 'ArrowRight') {
        setViewerIndex((prev) => (prev! < details.images.length - 1 ? prev! + 1 : 0));
      } else if (e.key === 'Escape') {
        setViewerIndex(null);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [viewerIndex, details]);

  const fetchDetails = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE}/api/images/${collectionId}/details`);
      if (!response.ok) throw new Error('Failed to fetch details');
      const data = await response.json();
      setDetails(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    try {
      setDeleting(true);
      const response = await fetch(`${API_BASE}/api/images/${collectionId}`, {
        method: 'DELETE',
      });

      if (!response.ok) throw new Error('Failed to delete collection');

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
  if (!details) return <div className="error">Collection not found</div>;

  return (
    <div className="collection-detail-page">
      <button className="back-button" onClick={onBack}>
        &larr; Back
      </button>

      <div className="detail-header">
        <div className="thumbnail-container">
          <img
            src={`${API_BASE}${details.thumbnailUrl}`}
            alt={details.title}
            className="detail-thumbnail"
          />
          <button className="read-button" onClick={() => setViewerIndex(0)}>READ</button>
        </div>

        <div className="detail-info">
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
          <p className="detail-count">{details.fileCount} images</p>
          <button className="delete-button" onClick={() => setShowDeleteConfirm(true)}>
            Delete Collection
          </button>
        </div>
      </div>

      <div className="images-section">
        <div className="images-grid">
          {details.images.map((imageUrl, index) => (
            <div
              key={index}
              className="image-item"
              onClick={() => setViewerIndex(index)}
            >
              <img
                src={`${API_BASE}${imageUrl}`}
                alt={`Image ${index + 1}`}
                loading="lazy"
              />
            </div>
          ))}
        </div>
      </div>

      {viewerIndex !== null && (
        <div className="image-viewer" onClick={() => setViewerIndex(null)}>
          <div className="viewer-content" onClick={(e) => e.stopPropagation()}>
            <button className="gallery-info-button" onClick={() => setViewerIndex(null)}>
              Gallery Info
            </button>
            <button className="viewer-close" onClick={() => setViewerIndex(null)}>
              &times;
            </button>
            <button
              className="viewer-nav viewer-prev"
              onClick={() => setViewerIndex(viewerIndex > 0 ? viewerIndex - 1 : details.images.length - 1)}
            >
              &larr;
            </button>
            <img src={`${API_BASE}${details.images[viewerIndex]}`} alt={`Image ${viewerIndex + 1}`} />
            <button
              className="viewer-nav viewer-next"
              onClick={() => setViewerIndex(viewerIndex < details.images.length - 1 ? viewerIndex + 1 : 0)}
            >
              &rarr;
            </button>
            <div className="viewer-counter">
              {viewerIndex + 1} / {details.images.length}
            </div>
          </div>
        </div>
      )}

      {showDeleteConfirm && (
        <div className="delete-confirm-overlay" onClick={() => setShowDeleteConfirm(false)}>
          <div className="delete-confirm-dialog" onClick={(e) => e.stopPropagation()}>
            <h2>Delete Collection?</h2>
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