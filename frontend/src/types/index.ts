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

export type VideoCollection = {
  id: string;
  title: string;
  artist: string;
  tags: string[];
  thumbnailUrl: string | null;
  viewCount: number;
};

export type VideoDetails = {
  id: string;
  name: string;
  title: string;
  artist: string;
  tags: string[];
  description: string;
  thumbnailUrl: string;
  videoUrl: string;
  videoSubtitleUrl: string | null;
};