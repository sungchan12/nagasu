import { useState } from 'react';
import './SearchBar.css';

interface SearchBarProps {
  onSearch: (query: string) => void;
}

export function SearchBar({ onSearch }: SearchBarProps) {
  const [query, setQuery] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch(query);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setQuery(value);
    onSearch(value);
  };

  return (
    <form className="search-bar" onSubmit={handleSubmit}>
      <div className="search-input-wrapper">
        <svg className="search-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 90 90" width="18" height="18">
          <path d="M 80.496 9.503 c -12.65 -12.65 -33.236 -12.65 -45.887 0 C 22.795 21.319 22.018 40.051 32.27 52.78 L 1.025 84.025 c -1.367 1.366 -1.367 3.583 0 4.949 C 1.708 89.658 2.604 90 3.5 90 s 1.792 -0.342 2.475 -1.025 L 37.219 57.73 c 5.914 4.764 13.123 7.149 20.333 7.149 c 8.309 0 16.618 -3.162 22.943 -9.488 C 93.147 42.739 93.147 22.155 80.496 9.503 z M 75.547 50.441 c -9.922 9.921 -26.066 9.921 -35.988 0 c -9.922 -9.922 -9.922 -26.066 0 -35.988 c 9.921 -9.922 26.066 -9.922 35.988 0 S 85.469 40.52 75.547 50.441 z" fill="#999"/>
        </svg>
        <input
          type="text"
          placeholder="Search..."
          value={query}
          onChange={handleChange}
          className="search-input"
        />
      </div>
    </form>
  );
}