import { useState } from 'react';
import { CollectionList } from './pages/CollectionList';
import { CollectionDetail } from './pages/CollectionDetail';
import { UploadCollection } from './pages/UploadCollection';
import './App.css';

type Page = 'list' | 'detail' | 'upload';

function App() {
  const [page, setPage] = useState<Page>('list');
  const [selectedCollectionId, setSelectedCollectionId] = useState<string | null>(null);

  const handleSelectCollection = (id: string) => {
    setSelectedCollectionId(id);
    setPage('detail');
  };

  const handleBack = () => {
    setSelectedCollectionId(null);
    setPage('list');
  };

  const handleUploadSuccess = () => {
    setPage('list');
  };

  return (
    <div className="app">
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
      {page === 'list' && (
        <CollectionList
          onSelectCollection={handleSelectCollection}
          onUploadClick={() => setPage('upload')}
        />
      )}
    </div>
  );
}

export default App;