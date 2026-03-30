import { useState, useEffect, useCallback } from 'react';
import { Sidebar } from './components/Sidebar';
import { CommandPalette } from './components/CommandPalette';
import { CollectionList } from './pages/CollectionList';
import { CollectionDetail } from './pages/CollectionDetail';
import { UploadCollection } from './pages/UploadCollection';
import { VideoList } from './pages/VideoList';
import { VideoDetail } from './pages/VideoDetail';
import { VideoUpload } from './pages/VideoUpload';
import { MediaBrowser } from './pages/MediaBrowser';
import './App.css';

type Page = 'list' | 'detail' | 'upload' | 'video-list' | 'video-detail' | 'video-upload' | 'most-viewed' | 'image-most-viewed' | 'private-video-list' | 'private-image-list' | 'media-browser';

function App() {
  const [page, setPage] = useState<Page>('video-list');
  const [selectedCollectionId, setSelectedCollectionId] = useState<string | null>(null);
  const [selectedVideoId, setSelectedVideoId] = useState<string | null>(null);
  const [privateMode, setPrivateMode] = useState(false);
  const [selectedIsPrivate, setSelectedIsPrivate] = useState(false);

  const navigateTo = useCallback((newPage: Page, state?: { collectionId?: string | null; videoId?: string | null; isPrivate?: boolean }) => {
    const s = {
      page: newPage,
      collectionId: state?.collectionId ?? null,
      videoId: state?.videoId ?? null,
      isPrivate: state?.isPrivate ?? false,
    };
    history.pushState(s, '', '');
    setPage(newPage);
    setSelectedCollectionId(s.collectionId);
    setSelectedVideoId(s.videoId);
    setSelectedIsPrivate(s.isPrivate);
  }, []);

  useEffect(() => {
    // Set initial state
    history.replaceState({ page, collectionId: selectedCollectionId, videoId: selectedVideoId, isPrivate: selectedIsPrivate }, '', '');

    const onPopState = (e: PopStateEvent) => {
      if (e.state) {
        setPage(e.state.page);
        setSelectedCollectionId(e.state.collectionId);
        setSelectedVideoId(e.state.videoId);
        setSelectedIsPrivate(e.state.isPrivate ?? false);
      }
    };
    window.addEventListener('popstate', onPopState);
    return () => window.removeEventListener('popstate', onPopState);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleSelectCollection = (id: string, isPrivate?: boolean) => {
    navigateTo('detail', { collectionId: id, isPrivate });
  };

  const handleSelectVideo = (id: string, isPrivate?: boolean) => {
    navigateTo('video-detail', { videoId: id, isPrivate });
  };

  const handleBack = () => {
    navigateTo('list');
  };

  const handleVideoListBack = () => {
    navigateTo('video-list');
  };

  const handleUploadSuccess = () => {
    navigateTo('list');
  };

  const handleVideoUploadSuccess = () => {
    navigateTo('video-list');
  };

  const handleNavigate = (target: Page) => {
    navigateTo(target);
  };

  const handleCommandNavigate = (page: string, id?: string) => {
    if (page === 'video-detail' && id) {
      navigateTo('video-detail', { videoId: id });
    } else if (page === 'detail' && id) {
      navigateTo('detail', { collectionId: id });
    }
  };

  return (
    <div className="app">
      <CommandPalette onNavigate={handleCommandNavigate} onPrivateModeChange={setPrivateMode} />
      <Sidebar currentPage={page} onNavigate={handleNavigate} privateMode={privateMode} />
      <main className="app-content">
        {page === 'media-browser' && (
          <MediaBrowser
            onSelectImage={handleSelectCollection}
            onSelectVideo={handleSelectVideo}
          />
        )}
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
