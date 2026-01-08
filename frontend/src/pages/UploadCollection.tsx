import { useState, useRef } from 'react';
import './UploadCollection.css';

const API_BASE = 'http://localhost:8080';

type Props = {
  onBack: () => void;
  onSuccess: () => void;
};

export function UploadCollection({ onBack, onSuccess }: Props) {
  const [title, setTitle] = useState('');
  const [artist, setArtist] = useState('');
  const [tags, setTags] = useState('');
  const [description, setDescription] = useState('');
  const [images, setImages] = useState<File[]>([]);
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const imageInputRef = useRef<HTMLInputElement>(null);
  const thumbnailInputRef = useRef<HTMLInputElement>(null);

  const handleImagesChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setImages(Array.from(e.target.files));
    }
  };

  const handleThumbnailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setThumbnail(e.target.files[0]);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim()) {
      setError('제목을 입력해주세요');
      return;
    }
    if (images.length === 0) {
      setError('이미지를 선택해주세요');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const formData = new FormData();

      // 평면적 FormData 구조로 전송
      formData.append('title', title.trim());
      formData.append('artist', artist.trim());
      formData.append('description', description.trim());

      // tags는 동일한 키로 여러 번 추가
      const tagList = tags.split(',').map((t) => t.trim()).filter((t) => t);
      tagList.forEach((tag) => {
        formData.append('tags', tag);
      });

      // images는 동일한 키로 여러 파일 추가
      images.forEach((image) => {
        formData.append('images', image);
      });

      if (thumbnail) {
        formData.append('thumbnail', thumbnail);
      }

      const response = await fetch(`${API_BASE}/api/images`, {
        method: 'POST',
        body: formData,
      });

      const result = await response.json();

      if (!response.ok || !result.status) {
        throw new Error(result.message || 'Upload failed');
      }

      onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upload-page">
      <div className="upload-header">
        <button className="back-button" onClick={onBack}>
          &larr; Back
        </button>
        <h1>New Collection</h1>
      </div>

      <form className="upload-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="title">Title *</label>
          <input
            id="title"
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Collection title"
          />
        </div>

        <div className="form-group">
          <label htmlFor="artist">Artist</label>
          <input
            id="artist"
            type="text"
            value={artist}
            onChange={(e) => setArtist(e.target.value)}
            placeholder="Artist name"
          />
        </div>

        <div className="form-group">
          <label htmlFor="tags">Tags</label>
          <input
            id="tags"
            type="text"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            placeholder="tag1, tag2, tag3"
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Collection description"
            rows={3}
          />
        </div>

        <div className="form-group">
          <label>Images *</label>
          <input
            ref={imageInputRef}
            type="file"
            accept="image/*"
            multiple
            onChange={handleImagesChange}
          />
          {images.length > 0 && (
            <div className="file-info">{images.length} files selected</div>
          )}
        </div>

        <div className="form-group">
          <label>Thumbnail (optional)</label>
          <input
            ref={thumbnailInputRef}
            type="file"
            accept="image/*"
            onChange={handleThumbnailChange}
          />
          {thumbnail && (
            <div className="file-info">{thumbnail.name}</div>
          )}
        </div>

        {error && <div className="error-message">{error}</div>}

        <button type="submit" className="submit-button" disabled={loading}>
          {loading ? 'Uploading...' : 'Upload'}
        </button>
      </form>
    </div>
  );
}