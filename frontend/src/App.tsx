import { useState } from 'react';
import { Sidebar } from './components/Sidebar';
import { CommandPalette } from './components/CommandPalette';
import { CollectionList } from './pages/CollectionList';
import { CollectionDetail } from './pages/CollectionDetail';
import { UploadCollection } from './pages/UploadCollection';
import { VideoList } from './pages/VideoList';
import { VideoDetail } from './pages/VideoDetail';
import { VideoUpload } from './pages/VideoUpload';
import './App.css';

type Page = 'list' | 'detail' | 'upload' | 'video-list' | 'video-detail' | 'video-upload' | 'most-viewed' | 'image-most-viewed' | 'private-video-list' | 'private-image-list';

function App() {
  const [page, setPage] = useState<Page>('video-list');
  const [selectedCollectionId, setSelectedCollectionId] = useState<string | null>(null);
  const [selectedVideoId, setSelectedVideoId] = useState<string | null>(null);
  const [privateMode, setPrivateMode] = useState(false);
  const [selectedIsPrivate, setSelectedIsPrivate] = useState(false);

  const handleSelectCollection = (id: string, isPrivate?: boolean) => {
    setSelectedCollectionId(id);
    setSelectedIsPrivate(isPrivate ?? false);
    setPage('detail');
  };

  const handleSelectVideo = (id: string, isPrivate?: boolean) => {
    setSelectedVideoId(id);
    setSelectedIsPrivate(isPrivate ?? false);
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

  const handleCommandNavigate = (page: string, id?: string) => {
    if (page === 'video-detail' && id) {
      setSelectedVideoId(id);
      setPage('video-detail');
    } else if (page === 'detail' && id) {
      setSelectedCollectionId(id);
      setPage('detail');
    }
  };

  return (
    <div className="app">
      <CommandPalette onNavigate={handleCommandNavigate} onPrivateModeChange={setPrivateMode} />
      <Sidebar currentPage={page} onNavigate={handleNavigate} privateMode={privateMode} />
      <main className="app-content">
        {page === 'detail' && selectedCollectionId && (
          <CollectionDetail
            collectionId={selectedCollectionId}
            onBack={handleBack}
            privateMode={selectedIsPrivate}
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
            privateMode={selectedIsPrivate}
          />
        )}
        {page === 'list' && (
          <CollectionList
            onSelectCollection={handleSelectCollection}
            onUploadClick={() => setPage('upload')}
            onVideoClick={() => setPage('video-list')}
          />
        )}
        {page === 'image-most-viewed' && (
          <CollectionList
            onSelectCollection={handleSelectCollection}
            onUploadClick={() => setPage('upload')}
            onVideoClick={() => setPage('video-list')}
            sort="viewCount"
            order="desc"
          />
        )}
        {page === 'private-image-list' && (
          <CollectionList
            onSelectCollection={handleSelectCollection}
            onUploadClick={() => setPage('upload')}
            onVideoClick={() => setPage('video-list')}
            privateMode
          />
        )}
        {page === 'private-video-list' && (
          <VideoList
            onSelectVideo={handleSelectVideo}
            onBack={handleBack}
            onUploadClick={() => setPage('video-upload')}
            privateMode
          />
        )}
      </main>
    </div>
  );
}

export default App;
