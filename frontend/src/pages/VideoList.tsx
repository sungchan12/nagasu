import { useState, useEffect } from 'react';
import { SearchBar } from '../components/SearchBar';
import { VideoCard } from '../components/VideoCard';
import type { VideoCollection } from '../types';
import './VideoList.css';

const API_BASE = '';

type Props = {
  onSelectVideo: (id: string, isPrivate?: boolean) => void;
  onBack: () => void;
  onUploadClick: () => void;
  sort?: string;
  order?: string;
  privateMode?: boolean;
};

export function VideoList({ onSelectVideo, onBack, onUploadClick, sort, order, privateMode }: Props) {
  const [videos, setVideos] = useState<VideoCollection[]>([]);
  const [filteredVideos, setFilteredVideos] = useState<VideoCollection[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchVideos = async () => {
      try {
        setLoading(true);
        const params = new URLSearchParams();
        if (sort) params.set('sort', sort);
        if (order) params.set('order', order);
        if (privateMode) params.set('private', 'true');
        const query = params.toString();
        const response = await fetch(`${API_BASE}/api/videos${query ? `?${query}` : ''}`, { credentials: 'include' });
        if (!response.ok) throw new Error('Failed to fetch videos');
        const data = await response.json();
        setVideos(data);
        setFilteredVideos(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    fetchVideos();
  }, [sort, order]);

  const handleSearch = (query: string) => {
    if (!query.trim()) {
      setFilteredVideos(videos);
      return;
    }

    const lowerQuery = query.toLowerCase();
    const filtered = videos.filter(
      (v) =>
        v.title.toLowerCase().includes(lowerQuery) ||
        v.artist.toLowerCase().includes(lowerQuery) ||
        v.tags.some((tag) => tag.toLowerCase().includes(lowerQuery))
    );
    setFilteredVideos(filtered);
  };

  const handleVideoClick = (video: VideoCollection) => {
    onSelectVideo(video.id, privateMode);
  };

  return (
    <div className="video-list-page">
      <div className="list-header">
        <button className="back-button" onClick={onBack}>Back</button>
        <SearchBar onSearch={handleSearch} />
        <button className="upload-button" onClick={onUploadClick}>Upload</button>
      </div>
      {loading && <div className="loading">Loading...</div>}
      {error && <div className="error">Error: {error}</div>}
      {!loading && !error && (
        <div className="video-list">
          {filteredVideos.map((video) => (
            <VideoCard
              key={video.id}
              video={video}
              onClick={handleVideoClick}
            />
          ))}
          {filteredVideos.length === 0 && (
            <div className="no-results">No videos found</div>
          )}
        </div>
      )}
    </div>
  );
}