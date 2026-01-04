import { useState } from 'react';
import { CollectionList } from './pages/CollectionList';
import { CollectionDetail } from './pages/CollectionDetail';
import './App.css';

function App() {
  const [selectedCollectionId, setSelectedCollectionId] = useState<string | null>(null);

  return (
    <div className="app">
      {selectedCollectionId ? (
        <CollectionDetail
          collectionId={selectedCollectionId}
          onBack={() => setSelectedCollectionId(null)}
        />
      ) : (
        <CollectionList onSelectCollection={setSelectedCollectionId} />
      )}
    </div>
  );
}

export default App;