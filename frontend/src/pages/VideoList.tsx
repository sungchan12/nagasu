import { useState, useEffect } from 'react';
import { SearchBar } from '../components/SearchBar';
import { VideoCard } from '../components/VideoCard';
import type { VideoCollection } from '../types';
import './VideoList.css';

const API_BASE = 'http://localhost:8080';

type Props = {
  onSelectVideo: (id: string) => void;
  onBack: () => void;
  onUploadClick: () => void;
};

export function VideoList({ onSelectVideo, onBack, onUploadClick }: Props) {
  const [videos, setVideos] = useState<VideoCollection[]>([]);
  const [filteredVideos, setFilteredVideos] = useState<VideoCollection[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchVideos = async () => {
      try {
        setLoading(true);
        const response = await fetch(`${API_BASE}/api/videos`);
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
  }, []);

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
    onSelectVideo(video.id);
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">Error: {error}</div>;

  return (
    <div className="video-list-page">
      <div className="list-header">
        <button className="back-button" onClick={onBack}>
          &larr; Back
        </button>
        <SearchBar onSearch={handleSearch} />
        <button className="upload-button" onClick={onUploadClick}>
          + Upload
        </button>
      </div>
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
    </div>
  );
}