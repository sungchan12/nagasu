import { useState, useRef } from 'react';
import './VideoUpload.css';

const API_BASE = '';

type Props = {
  onBack: () => void;
  onSuccess: () => void;
};

export function VideoUpload({ onBack, onSuccess }: Props) {
  const [title, setTitle] = useState('');
  const [artist, setArtist] = useState('');
  const [tags, setTags] = useState('');
  const [description, setDescription] = useState('');
  const [video, setVideo] = useState<File | null>(null);
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [subtitle, setSubtitle] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const videoInputRef = useRef<HTMLInputElement>(null);
  const thumbnailInputRef = useRef<HTMLInputElement>(null);
  const subtitleInputRef = useRef<HTMLInputElement>(null);

  const handleVideoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setVideo(e.target.files[0]);
    }
  };

  const handleThumbnailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setThumbnail(e.target.files[0]);
    }
  };

  const handleSubtitleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSubtitle(e.target.files[0]);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim()) {
      setError('제목을 입력해주세요');
      return;
    }
    if (!video) {
      setError('동영상을 선택해주세요');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const formData = new FormData();

      formData.append('title', title.trim());
      formData.append('artist', artist.trim());
      formData.append('description', description.trim());

      const tagList = tags.split(',').map((t) => t.trim()).filter((t) => t);
      tagList.forEach((tag) => {
        formData.append('tags', tag);
      });

      formData.append('video', video);

      if (thumbnail) {
        formData.append('thumbnail', thumbnail);
      }

      if (subtitle) {
        formData.append('subtitle', subtitle);
      }

      const response = await fetch(`${API_BASE}/api/videos`, {
        method: 'POST',
        body: formData,
        credentials: 'include',
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.error || 'Upload failed');
      }

      onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="video-upload-page">
      <div className="video-upload-header">
        <button className="back-button" onClick={onBack}>
          &larr; Back
        </button>
        <h1>New Video</h1>
      </div>

      <form className="video-upload-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="title">Title *</label>
          <input
            id="title"
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Video title"
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
            placeholder="Video description"
            rows={3}
          />
        </div>

        <div className="form-group">
          <label>Video *</label>
          <input
            ref={videoInputRef}
            type="file"
            accept="video/*"
            onChange={handleVideoChange}
          />
          {video && (
            <div className="file-info">{video.name}</div>
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
          <div className="file-hint">썸네일을 선택하지 않으면 자동 생성됩니다</div>
          {thumbnail && (
            <div className="file-info">{thumbnail.name}</div>
          )}
        </div>

        <div className="form-group">
          <label>Subtitle (optional)</label>
          <input
            ref={subtitleInputRef}
            type="file"
            accept=".srt,.vtt"
            onChange={handleSubtitleChange}
          />
          <div className="file-hint">자막 파일 (.srt, .vtt)</div>
          {subtitle && (
            <div className="file-info">{subtitle.name}</div>
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