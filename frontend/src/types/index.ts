export type ImageCollection = {
  id: string;
  name: string;
  title: string;
  artist: string;
  tags: string[];
  thumbnailUrl: string;
};

export type ImageDetails = {
  id: string;
  name: string;
  title: string;
  artist: string;
  tags: string[];
  description: string;
  thumbnailUrl: string;
  fileCount: number;
  images: string[];
};