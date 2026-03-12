import { useState } from 'react';
import { Sidebar } from './components/Sidebar';
import { CollectionList } from './pages/CollectionList';
import { CollectionDetail } from './pages/CollectionDetail';
import { UploadCollection } from './pages/UploadCollection';
import { VideoList } from './pages/VideoList';
import { VideoDetail } from './pages/VideoDetail';
import { VideoUpload } from './pages/VideoUpload';
import './App.css';

type Page = 'list' | 'detail' | 'upload' | 'video-list' | 'video-detail' | 'video-upload' | 'most-viewed' | 'image-most-viewed';

function App() {
  const [page, setPage] = useState<Page>('video-list');
  const [selectedCollectionId, setSelectedCollectionId] = useState<string | null>(null);
  const [selectedVideoId, setSelectedVideoId] = useState<string | null>(null);

  const handleSelectCollection = (id: string) => {
    setSelectedCollectionId(id);
    setPage('detail');
  };

  const handleSelectVideo = (id: string) => {
    setSelectedVideoId(id);
    setPage('video-detail');
  };

  const handleBack = () => {
    setSelectedCollectionId(null);
    setSelectedVideoId(null);
    setPage('list');
  };

  const handleVideoListBack = () => {
    setSelectedVideoId(null);
    setPage('video-list');
  };

  const handleUploadSuccess = () => {
    setPage('list');
  };

  const handleVideoUploadSuccess = () => {
    setPage('video-list');
  };

  const handleNavigate = (target: Page) => {
    setSelectedCollectionId(null);
    setSelectedVideoId(null);
    setPage(target);
  };

  return (
    <div className="app">
      <Sidebar currentPage={page} onNavigate={handleNavigate} />
      <main className="app-content">
        {page === 'detail' && selectedCollectionId && (
          <CollectionDetail
            collectionId={selectedCollectionId}
            onBack={handleBack}
          />
        )}
        {page === 'upload' && (
          <UploadCollection
            onBack={handleBack}
            onSuccess={handleUploadSuccess}
          />
        )}
        {page === 'video-list' && (
          <VideoList
            onSelectVideo={handleSelectVideo}
            onBack={handleBack}
            onUploadClick={() => setPage('video-upload')}
          />
        )}
        {page === 'most-viewed' && (
          <VideoList
            onSelectVideo={handleSelectVideo}
            onBack={handleBack}
            onUploadClick={() => setPage('video-upload')}
            sort="viewCount"
            order="desc"
          />
        )}
        {page === 'video-upload' && (
          <VideoUpload
            onBack={handleVideoListBack}
            onSuccess={handleVideoUploadSuccess}
          />
        )}
        {page === 'video-detail' && selectedVideoId && (
          <VideoDetail
            videoId={selectedVideoId}
            onBack={handleVideoListBack}
          />
        )}
        {page === 'list' && (
          <CollectionList
            onSelectCollection={handleSelectCollection}
            onUploadClick={() => setPage('upload')}
            onVideoClick={() => setPage('video-list')}
          />
        )}
      </main>
    </div>
  );
}

export default App;
