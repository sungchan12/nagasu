import { useState, useRef, useEffect, useCallback } from 'react';
import './CommandPalette.css';

type LogEntry = {
  text: string;
  type: 'input' | 'success' | 'error' | 'info' | 'help';
};

type Props = {
  onNavigate?: (page: string, id?: string) => void;
  onPrivateModeChange?: (active: boolean) => void;
};

const API_BASE = '/api';

const HELP_TEXT = `Available commands:
  repair video <id>     Repair video collection
  repair image <id>     Repair image collection
  delete video <id>     Delete video collection
  delete image <id>     Delete image collection
  goto video <id>       Navigate to video detail
  goto image <id>       Navigate to image detail
  list videos           List all video collections
  list images           List all image collections
  help                  Show this help
  clear                 Clear log
  exit / Esc            Close palette`;

export function CommandPalette({ onNavigate, onPrivateModeChange }: Props) {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [log, setLog] = useState<LogEntry[]>([]);
  const [history, setHistory] = useState<string[]>([]);
  const [historyIdx, setHistoryIdx] = useState(-1);
  const inputRef = useRef<HTMLInputElement>(null);
  const logRef = useRef<HTMLDivElement>(null);
  const tapTimestamps = useRef<number[]>([]);

  const addLog = useCallback((text: string, type: LogEntry['type']) => {
    setLog(prev => [...prev, { text, type }]);
  }, []);

  // Secret activation: backtick 3 times within 800ms
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (open && e.key === 'Escape') {
        setOpen(false);
        return;
      }

      if (e.key === '`' && !open) {
        // Don't trigger inside other inputs
        const target = e.target as HTMLElement;
        if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA') return;

        const now = Date.now();
        tapTimestamps.current.push(now);
        tapTimestamps.current = tapTimestamps.current.filter(t => now - t < 800);

        if (tapTimestamps.current.length >= 3) {
          e.preventDefault();
          tapTimestamps.current = [];
          setOpen(true);
        }
      }
    };

    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [open]);

  // Focus input when opened
  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [open]);

  // Auto-scroll log
  useEffect(() => {
    if (logRef.current) {
      logRef.current.scrollTop = logRef.current.scrollHeight;
    }
  }, [log]);

  const executeCommand = async (raw: string) => {
    const cmd = raw.trim().toLowerCase();
    if (!cmd) return;

    setHistory(prev => [...prev, raw]);
    setHistoryIdx(-1);
    addLog(`> ${raw}`, 'input');

    if (cmd === 'help') {
      addLog(HELP_TEXT, 'help');
      return;
    }

    if (cmd === 'clear') {
      setLog([]);
      return;
    }

    if (cmd === 'exit') {
      setOpen(false);
      return;
    }

    // repair video <id> | repair image <id>
    const repairMatch = cmd.match(/^repair\s+(video|image)\s+(.+)$/);
    if (repairMatch) {
      const [, type, id] = repairMatch;
      const endpoint = type === 'video'
        ? `${API_BASE}/videos/${id}/repair`
        : `${API_BASE}/images/${id}/repair`;
      try {
        addLog(`Repairing ${type} "${id}"...`, 'info');
        const res = await fetch(endpoint, { method: 'POST', credentials: 'include' });
        if (res.ok) {
          addLog(`${type} "${id}" repaired successfully.`, 'success');
        } else {
          const body = await res.text();
          addLog(`Failed (${res.status}): ${body}`, 'error');
        }
      } catch (err) {
        addLog(`Network error: ${err}`, 'error');
      }
      return;
    }

    // delete video <id> | delete image <id>
    const deleteMatch = cmd.match(/^delete\s+(video|image)\s+(.+)$/);
    if (deleteMatch) {
      const [, type, id] = deleteMatch;
      const endpoint = type === 'video'
        ? `${API_BASE}/videos/${id}`
        : `${API_BASE}/images/${id}`;
      try {
        addLog(`Deleting ${type} "${id}"...`, 'info');
        const res = await fetch(endpoint, { method: 'DELETE', credentials: 'include' });
        if (res.ok) {
          addLog(`${type} "${id}" deleted.`, 'success');
        } else {
          const body = await res.text();
          addLog(`Failed (${res.status}): ${body}`, 'error');
        }
      } catch (err) {
        addLog(`Network error: ${err}`, 'error');
      }
      return;
    }

    // goto video <id> | goto image <id>
    const gotoMatch = cmd.match(/^goto\s+(video|image)\s+(.+)$/);
    if (gotoMatch) {
      const [, type, id] = gotoMatch;
      if (onNavigate) {
        onNavigate(type === 'video' ? 'video-detail' : 'detail', id);
        addLog(`Navigating to ${type} "${id}".`, 'success');
        setOpen(false);
      } else {
        addLog('Navigation not available.', 'error');
      }
      return;
    }

    // list videos | list images
    const listMatch = cmd.match(/^list\s+(videos|images)$/);
    if (listMatch) {
      const [, type] = listMatch;
      const endpoint = type === 'videos'
        ? `${API_BASE}/videos`
        : `${API_BASE}/images`;
      try {
        addLog(`Fetching ${type}...`, 'info');
        const res = await fetch(endpoint, { credentials: 'include' });
        if (res.ok) {
          const data = await res.json();
          if (Array.isArray(data) && data.length === 0) {
            addLog('No collections found.', 'info');
          } else {
            const items = Array.isArray(data) ? data : [];
            items.forEach((item: { id?: string; title?: string }) => {
              addLog(`  ${item.id ?? '?'}  ${item.title ?? 'untitled'}`, 'info');
            });
            addLog(`Total: ${items.length}`, 'success');
          }
        } else {
          addLog(`Failed (${res.status})`, 'error');
        }
      } catch (err) {
        addLog(`Network error: ${err}`, 'error');
      }
      return;
    }

    // Send unrecognized commands to backend
    try {
      const res = await fetch(`${API_BASE}/toggle`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ command: raw.trim() }),
      });
      if (res.ok) {
        const data = await res.json();
        if ('privateMode' in data) {
          if (data.privateMode) {
            addLog('Mode changed.', 'success');
          } else {
            addLog('Mode restored.', 'info');
          }
          onPrivateModeChange?.(data.privateMode);
          return;
        }
      }
    } catch {
      // silently ignore
    }
    addLog(`Unknown command: "${cmd}". Type "help" for available commands.`, 'error');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      executeCommand(input);
      setInput('');
    } else if (e.key === 'Escape') {
      setOpen(false);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      if (history.length > 0) {
        const next = historyIdx === -1 ? history.length - 1 : Math.max(0, historyIdx - 1);
        setHistoryIdx(next);
        setInput(history[next]);
      }
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (historyIdx !== -1) {
        const next = historyIdx + 1;
        if (next >= history.length) {
          setHistoryIdx(-1);
          setInput('');
        } else {
          setHistoryIdx(next);
          setInput(history[next]);
        }
      }
    }
  };

  if (!open) return null;

  return (
    <div className="cmd-overlay" onClick={() => setOpen(false)}>
      <div className="cmd-palette" onClick={e => e.stopPropagation()}>
        <div className="cmd-input-row">
          <span className="cmd-prompt">&gt;</span>
          <input
            ref={inputRef}
            className="cmd-input"
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="type a command..."
            spellCheck={false}
            autoComplete="off"
          />
          <span className="cmd-hint">Esc to close</span>
        </div>
        <div className="cmd-log" ref={logRef}>
          {log.map((entry, i) => (
            <div
              key={i}
              className={`cmd-log-entry ${
                entry.type === 'input' ? 'cmd-input-echo' :
                entry.type === 'success' ? 'cmd-success' :
                entry.type === 'error' ? 'cmd-error' :
                entry.type === 'help' ? 'cmd-help' :
                'cmd-info'
              }`}
            >
              {entry.text}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}