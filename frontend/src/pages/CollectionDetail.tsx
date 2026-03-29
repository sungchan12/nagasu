import { useState, useEffect, useRef } from 'react';
import type { ImageDetails } from '../types';
import './CollectionDetail.css';

const API_BASE = '';

type Props = {
  collectionId: string;
  onBack: () => void;
  privateMode?: boolean;
};

export function CollectionDetail({ collectionId, onBack, privateMode }: Props) {
  const [details, setDetails] = useState<ImageDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [viewerIndex, setViewerIndex] = useState<number | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const [editing, setEditing] = useState(false);
  const [editTitle, setEditTitle] = useState('');
  const [editArtist, setEditArtist] = useState('');
  const [editTags, setEditTags] = useState('');
  const [editDescription, setEditDescription] = useState('');
  const [editThumbnail, setEditThumbnail] = useState<File | null>(null);
  const [thumbnailPreview, setThumbnailPreview] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const thumbnailInputRef = useRef<HTMLInputElement>(null);
  const addImagesInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    const fetchDetails = async () => {
      try {
        setLoading(true);
        const query = privateMode ? '?private=true' : '';
        const response = await fetch(`${API_BASE}/api/images/${collectionId}/details${query}`, { credentials: 'include' });
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

  const handleDelete = async () => {
    try {
      setDeleting(true);
      const response = await fetch(`${API_BASE}/api/images/${collectionId}`, {
        method: 'DELETE',
        credentials: 'include',
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

  const startEditing = () => {
    if (!details) return;
    setEditTitle(details.title);
    setEditArtist(details.artist);
    setEditTags(details.tags.join(', '));
    setEditDescription(details.description || '');
    setEditThumbnail(null);
    setThumbnailPreview(null);
    setEditing(true);
  };

  const cancelEditing = () => {
    setEditing(false);
    setEditThumbnail(null);
    if (thumbnailPreview) URL.revokeObjectURL(thumbnailPreview);
    setThumbnailPreview(null);
  };

  const handleThumbnailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setEditThumbnail(file);
    if (thumbnailPreview) URL.revokeObjectURL(thumbnailPreview);
    setThumbnailPreview(URL.createObjectURL(file));
  };

  const handleSave = async () => {
    if (!details) return;
    try {
      setSaving(true);
      const formData = new FormData();
      formData.append('title', editTitle);
      formData.append('artist', editArtist);
      editTags.split(',').map(t => t.trim()).filter(Boolean).forEach(tag => {
        formData.append('tags', tag);
      });
      formData.append('description', editDescription);
      if (editThumbnail) {
        formData.append('thumbnail', editThumbnail);
      }

      const response = await fetch(`${API_BASE}/api/images/${collectionId}`, {
        method: 'PATCH',
        body: formData,
        credentials: 'include',
      });
      if (!response.ok) throw new Error('Failed to update collection');

      // Refresh details
      const refreshRes = await fetch(`${API_BASE}/api/images/${collectionId}/details${privateMode ? '?private=true' : ''}`, { credentials: 'include' });
      if (refreshRes.ok) {
        setDetails(await refreshRes.json());
      }
      setEditing(false);
      if (thumbnailPreview) URL.revokeObjectURL(thumbnailPreview);
      setThumbnailPreview(null);
      setEditThumbnail(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setSaving(false);
    }
  };

  const handleAddImages = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;
    try {
      setUploading(true);
      const formData = new FormData();
      Array.from(files).forEach(file => formData.append('images', file));

      const response = await fetch(`${API_BASE}/api/images/${collectionId}/images`, {
        method: 'POST',
        body: formData,
        credentials: 'include',
      });
      if (!response.ok) throw new Error('Failed to add images');

      const refreshRes = await fetch(`${API_BASE}/api/images/${collectionId}/details${privateMode ? '?private=true' : ''}`, { credentials: 'include' });
      if (refreshRes.ok) {
        setDetails(await refreshRes.json());
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">Error: {error}</div>;
  if (!details) return <div className="error">Collection not found</div>;

  return (
    <div className="collection-detail-page">
      <button className="back-button" onClick={onBack}>
        &#8592; Back
      </button>

      <div className="detail-header">
        <div className="thumbnail-container">
          <img
            src={thumbnailPreview || `${API_BASE}${details.thumbnailUrl}`}
            alt={details.title}
            className="detail-thumbnail"
          />
          {editing && (
            <>
              <input
                ref={thumbnailInputRef}
                type="file"
                accept="image/*"
                onChange={handleThumbnailChange}
                style={{ display: 'none' }}
              />
              <button
                className="change-thumbnail-button"
                onClick={() => thumbnailInputRef.current?.click()}
              >
                Change Thumbnail
              </button>
            </>
          )}
          <button className="read-button" onClick={() => setViewerIndex(0)}>
            <span>View Gallery</span>
          </button>
        </div>

        <div className="detail-info">
          {editing ? (
            <>
              <input
                className="edit-input edit-title-input"
                value={editTitle}
                onChange={(e) => setEditTitle(e.target.value)}
                placeholder="Title"
              />
              <input
                className="edit-input edit-artist-input"
                value={editArtist}
                onChange={(e) => setEditArtist(e.target.value)}
                placeholder="Artist"
              />
              <input
                className="edit-input edit-tags-input"
                value={editTags}
                onChange={(e) => setEditTags(e.target.value)}
                placeholder="Tags (comma separated)"
              />
              <textarea
                className="edit-input edit-description-input"
                value={editDescription}
                onChange={(e) => setEditDescription(e.target.value)}
                placeholder="Description"
                rows={3}
              />
              <p className="detail-count"><strong>{details.fileCount}</strong> images in collection</p>
              <div className="edit-actions">
                <button className="save-button" onClick={handleSave} disabled={saving}>
                  {saving ? 'Saving...' : 'Save'}
                </button>
                <button className="cancel-edit-button" onClick={cancelEditing} disabled={saving}>
                  Cancel
                </button>
              </div>
            </>
          ) : (
            <>
              <div className="detail-overline">Gallery</div>
              <h1 className="detail-title">{details.title}</h1>
              <p className="detail-artist">{details.artist}</p>
              <div className="detail-divider" />
              <div className="detail-tags">
                {details.tags.map((tag, index) => (
                  <span key={index} className="tag">{tag}</span>
                ))}
              </div>
              {details.description && (
                <p className="detail-description">{details.description}</p>
              )}
              <p className="detail-count"><strong>{details.fileCount}</strong> images in collection</p>
              <div className="detail-actions">
                <button className="edit-button" onClick={startEditing}>
                  Edit
                </button>
                <button className="delete-button" onClick={() => setShowDeleteConfirm(true)}>
                  Delete
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      <div className="images-section">
        <div className="images-section-header">
          <span className="images-section-label">Works</span>
          <input
            ref={addImagesInputRef}
            type="file"
            accept="image/*"
            multiple
            onChange={handleAddImages}
            style={{ display: 'none' }}
          />
          <button
            className="add-images-button"
            onClick={() => addImagesInputRef.current?.click()}
            disabled={uploading}
          >
            {uploading ? 'Uploading...' : 'Add Images'}
          </button>
        </div>
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
                loading={index < 8 ? 'eager' : 'lazy'}
              />
              <span className="image-item-index">{String(index + 1).padStart(3, '0')}</span>
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
              &#215;
            </button>
            <button
              className="viewer-nav viewer-prev"
              onClick={() => setViewerIndex(viewerIndex > 0 ? viewerIndex - 1 : details.images.length - 1)}
            >
              &#8592;
            </button>
            <img src={`${API_BASE}${details.images[viewerIndex]}`} alt={`Image ${viewerIndex + 1}`} />
            <button
              className="viewer-nav viewer-next"
              onClick={() => setViewerIndex(viewerIndex < details.images.length - 1 ? viewerIndex + 1 : 0)}
            >
              &#8594;
            </button>
            <div className="viewer-counter">
              {String(viewerIndex + 1).padStart(3, '0')} / {String(details.images.length).padStart(3, '0')}
            </div>
          </div>
        </div>
      )}

      {showDeleteConfirm && (
        <div className="delete-confirm-overlay" onClick={() => setShowDeleteConfirm(false)}>
          <div className="delete-confirm-dialog" onClick={(e) => e.stopPropagation()}>
            <h2>Delete Collection?</h2>
            <p>Are you sure you want to delete &ldquo;{details.title}&rdquo;?</p>
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