'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { 
  BookOpen, 
  Search, 
  Plus, 
  Star,
  StarOff,
  FileText,
  Download,
  RefreshCw,
  Filter,
  Clock,
  Trash2,
  Sparkles,
  FileEdit,
  Eye,
  X
} from 'lucide-react';
import { studentAPI, contentAPI } from '@/lib/apiClient';
import { toast } from 'sonner';
import { StudentRoute } from '@/components/route-guard';
import ReactMarkdown from 'react-markdown';

interface Note {
  id: number;
  topicId: number;
  topicTitle: string;
  subjectId: number;
  subjectName: string;
  title: string;
  format: 'SHORT' | 'LONG' | 'REVISION_SHEET';
  language: string;
  contentMd?: string;
  isFavorite: boolean;
  versionCount: number;
  createdAt: string;
  updatedAt: string;
  preview?: string;
}

interface NotesStats {
  totalNotes: number;
  shortNotes: number;
  longNotes: number;
  revisionSheets: number;
  favoriteNotes: number;
  notesBySubject: { subjectId: number; subjectName: string; count: number }[];
}

interface Subject {
  id: number;
  name: string;
}

interface Topic {
  id: number;
  title: string;
}

interface Chapter {
  id: number;
  name: string;
}

export default function StudentNotesPage() {
  return (
    <StudentRoute>
      <NotesContent />
    </StudentRoute>
  );
}

function NotesContent() {
  const [notes, setNotes] = useState<Note[]>([]);
  const [stats, setStats] = useState<NotesStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedFormat, setSelectedFormat] = useState<string | null>(null);
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);
  const [selectedNote, setSelectedNote] = useState<Note | null>(null);
  const [showGenerateModal, setShowGenerateModal] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  
  // Generate modal state
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedSubject, setSelectedSubject] = useState<number | null>(null);
  const [selectedChapter, setSelectedChapter] = useState<number | null>(null);
  const [selectedTopic, setSelectedTopic] = useState<number | null>(null);
  const [generateFormat, setGenerateFormat] = useState<'SHORT' | 'LONG' | 'REVISION_SHEET'>('SHORT');
  const [generateLanguage, setGenerateLanguage] = useState<'en' | 'hi'>('en');

  useEffect(() => {
    loadNotes();
    loadStats();
  }, [searchTerm, selectedFormat, showFavoritesOnly]);

  const loadNotes = async () => {
    try {
      setIsLoading(true);
      const params: any = {};
      if (searchTerm) params.search = searchTerm;
      if (selectedFormat) params.format = selectedFormat;
      if (showFavoritesOnly) params.isFavorite = true;
      
      const response = await studentAPI.getNotes(params);
      setNotes(response?.notes || []);
    } catch (error) {
      console.error('Failed to load notes:', error);
      toast.error('Failed to load notes');
    } finally {
      setIsLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await studentAPI.getNotesStats();
      setStats(response);
    } catch (error) {
      console.error('Failed to load stats:', error);
    }
  };

  const loadSubjects = async () => {
    try {
      // For CBSE, we need to get the board first, then grades, then subjects
      const boardsResponse = await contentAPI.getBoards();
      const cbseBoard = boardsResponse?.find((b: any) => b.name === 'CBSE');
      if (cbseBoard) {
        const gradesResponse = await contentAPI.getGradesByBoard(cbseBoard.id);
        // Get subjects for grade 9-12 (common for CBSE)
        if (gradesResponse?.length > 0) {
          const subjectsResponse = await contentAPI.getSubjectsByGrade(gradesResponse[0].id);
          setSubjects(subjectsResponse || []);
        }
      }
    } catch (error) {
      console.error('Failed to load subjects:', error);
    }
  };

  const loadChapters = async (subjectId: number) => {
    try {
      const response = await contentAPI.getChaptersBySubject(subjectId);
      setChapters(response || []);
    } catch (error) {
      console.error('Failed to load chapters:', error);
    }
  };

  const loadTopics = async (chapterId: number) => {
    try {
      const response = await contentAPI.getTopicsByChapter(chapterId);
      setTopics(response || []);
    } catch (error) {
      console.error('Failed to load topics:', error);
    }
  };

  const handleOpenGenerateModal = () => {
    setShowGenerateModal(true);
    loadSubjects();
    setSelectedSubject(null);
    setSelectedChapter(null);
    setSelectedTopic(null);
    setChapters([]);
    setTopics([]);
  };

  const handleSubjectChange = (subjectId: number) => {
    setSelectedSubject(subjectId);
    setSelectedChapter(null);
    setSelectedTopic(null);
    setTopics([]);
    loadChapters(subjectId);
  };

  const handleChapterChange = (chapterId: number) => {
    setSelectedChapter(chapterId);
    setSelectedTopic(null);
    loadTopics(chapterId);
  };

  const handleGenerateNotes = async () => {
    if (!selectedTopic) {
      toast.error('Please select a topic');
      return;
    }

    try {
      setIsGenerating(true);
      const response = await studentAPI.generateNotes(selectedTopic, generateFormat, generateLanguage);
      toast.success('Notes generated successfully!');
      setShowGenerateModal(false);
      loadNotes();
      loadStats();
      // Open the newly generated note
      if (response?.note) {
        setSelectedNote(response.note);
      }
    } catch (error: any) {
      console.error('Failed to generate notes:', error);
      toast.error(error.response?.data?.message || 'Failed to generate notes');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleToggleFavorite = async (note: Note, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await studentAPI.toggleNoteFavorite(note.id);
      loadNotes();
      loadStats();
      toast.success(note.isFavorite ? 'Removed from favorites' : 'Added to favorites');
    } catch (error) {
      toast.error('Failed to update favorite');
    }
  };

  const handleArchiveNote = async (noteId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm('Are you sure you want to archive this note?')) return;
    
    try {
      await studentAPI.archiveNote(noteId);
      toast.success('Note archived');
      loadNotes();
      loadStats();
      if (selectedNote?.id === noteId) {
        setSelectedNote(null);
      }
    } catch (error) {
      toast.error('Failed to archive note');
    }
  };

  const handleExportNote = async (noteId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      const blob = await studentAPI.exportNote(noteId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `note-${noteId}.md`;
      a.click();
      window.URL.revokeObjectURL(url);
      toast.success('Note exported');
    } catch (error) {
      toast.error('Failed to export note');
    }
  };

  const handleRegenerateNote = async (noteId: number) => {
    try {
      setIsGenerating(true);
      const response = await studentAPI.regenerateNote(noteId);
      toast.success('Notes regenerated successfully!');
      if (response?.note) {
        setSelectedNote(response.note);
      }
      loadNotes();
    } catch (error) {
      toast.error('Failed to regenerate notes');
    } finally {
      setIsGenerating(false);
    }
  };

  const formatBadgeColor = (format: string) => {
    switch (format) {
      case 'SHORT': return 'bg-blue-100 text-blue-700 border-blue-200';
      case 'LONG': return 'bg-purple-100 text-purple-700 border-purple-200';
      case 'REVISION_SHEET': return 'bg-amber-100 text-amber-700 border-amber-200';
      default: return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  const formatLabel = (format: string) => {
    switch (format) {
      case 'SHORT': return 'Quick Notes';
      case 'LONG': return 'Detailed';
      case 'REVISION_SHEET': return 'Revision Sheet';
      default: return format;
    }
  };

  if (isLoading && notes.length === 0) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="glass-panel p-6">
              <div className="skeleton h-4 rounded w-3/4 mb-2"></div>
              <div className="skeleton h-8 rounded w-1/2"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6" data-testid="notes-page">
      {/* Header */}
      <div className="page-header">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold mb-2 flex items-center gap-3">
              <BookOpen className="h-8 w-8" />
              My Notes Notebook
            </h1>
            <p className="text-white/80">
              AI-generated study notes personalized for you
            </p>
          </div>
          <Button 
            onClick={handleOpenGenerateModal}
            className="btn-primary"
            data-testid="generate-notes-btn"
          >
            <Plus className="h-5 w-5 mr-2" />
            Generate New Notes
          </Button>
        </div>
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          <Card className="glass-panel">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-emerald-500/10 rounded-lg flex items-center justify-center">
                <FileText className="h-5 w-5 text-emerald-500" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.totalNotes}</p>
                <p className="text-xs text-slate-500 dark:text-slate-300">Total Notes</p>
              </div>
            </CardContent>
          </Card>
          <Card className="glass-panel">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-sky-500/10 rounded-lg flex items-center justify-center">
                <Sparkles className="h-5 w-5 text-sky-500" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.shortNotes}</p>
                <p className="text-xs text-slate-500 dark:text-slate-300">Quick Notes</p>
              </div>
            </CardContent>
          </Card>
          <Card className="glass-panel">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-indigo-500/10 rounded-lg flex items-center justify-center">
                <FileEdit className="h-5 w-5 text-indigo-500" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.longNotes}</p>
                <p className="text-xs text-slate-500 dark:text-slate-300">Detailed</p>
              </div>
            </CardContent>
          </Card>
          <Card className="glass-panel">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-amber-500/10 rounded-lg flex items-center justify-center">
                <BookOpen className="h-5 w-5 text-amber-500" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.revisionSheets}</p>
                <p className="text-xs text-slate-500 dark:text-slate-300">Revision Sheets</p>
              </div>
            </CardContent>
          </Card>
          <Card className="glass-panel">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-yellow-500/10 rounded-lg flex items-center justify-center">
                <Star className="h-5 w-5 text-yellow-500" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.favoriteNotes}</p>
                <p className="text-xs text-slate-500 dark:text-slate-300">Favorites</p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Filters */}
      <Card className="glass-panel border border-white/40">
        <CardContent className="p-4">
          <div className="flex flex-wrap items-center gap-4">
            <div className="flex-1 min-w-[200px]">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-slate-400" />
                <Input
                  placeholder="Search notes..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="input-modern pl-10"
                />
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Filter className="h-4 w-4 text-slate-500" />
              <Button
                size="sm"
                onClick={() => setSelectedFormat(null)}
                className={selectedFormat === null ? 'btn-primary h-9 px-4 text-sm' : 'btn-outline h-9 px-4 text-sm'}
              >
                All
              </Button>
              <Button
                size="sm"
                onClick={() => setSelectedFormat('SHORT')}
                className={selectedFormat === 'SHORT' ? 'btn-primary h-9 px-4 text-sm' : 'btn-outline h-9 px-4 text-sm'}
              >
                Quick
              </Button>
              <Button
                size="sm"
                onClick={() => setSelectedFormat('LONG')}
                className={selectedFormat === 'LONG' ? 'btn-primary h-9 px-4 text-sm' : 'btn-outline h-9 px-4 text-sm'}
              >
                Detailed
              </Button>
              <Button
                size="sm"
                onClick={() => setSelectedFormat('REVISION_SHEET')}
                className={selectedFormat === 'REVISION_SHEET' ? 'btn-primary h-9 px-4 text-sm' : 'btn-outline h-9 px-4 text-sm'}
              >
                Revision
              </Button>
            </div>
            <Button
              onClick={() => setShowFavoritesOnly(!showFavoritesOnly)}
              className={showFavoritesOnly ? 'btn-primary h-9 px-4 text-sm' : 'btn-outline h-9 px-4 text-sm'}
            >
              <Star className={`h-4 w-4 mr-1 ${showFavoritesOnly ? 'fill-current' : ''}`} />
              Favorites
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Notes Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Notes List */}
        <div className="lg:col-span-1 space-y-4">
          {notes.length === 0 ? (
            <Card className="glass-panel border border-white/40" data-testid="notes-empty-state">
              <CardContent className="p-8 text-center">
                <FileText className="h-12 w-12 mx-auto mb-4 text-slate-300" />
                <p className="text-slate-500 dark:text-slate-300 mb-4">No notes yet</p>
                <Button className="btn-primary" onClick={handleOpenGenerateModal}>
                  <Plus className="h-4 w-4 mr-2" />
                  Generate Your First Notes
                </Button>
              </CardContent>
            </Card>
          ) : (
            notes.map((note) => (
              <Card 
                key={note.id} 
                className={`glass-panel border border-white/40 cursor-pointer hover-lift transition-all ${selectedNote?.id === note.id ? 'ring-2 ring-emerald-400/70' : ''}`}
                onClick={() => setSelectedNote(note)}
                data-testid={`note-card-${note.id}`}
              >
                <CardContent className="p-4">
                  <div className="flex items-start justify-between mb-2">
                    <Badge className={`${formatBadgeColor(note.format)} text-xs`}>
                      {formatLabel(note.format)}
                    </Badge>
                    <div className="flex items-center gap-1">
                      <button onClick={(e) => handleToggleFavorite(note, e)}>
                        {note.isFavorite ? (
                          <Star className="h-4 w-4 text-yellow-500 fill-current" />
                        ) : (
                          <StarOff className="h-4 w-4 text-slate-400 hover:text-yellow-500" />
                        )}
                      </button>
                      <button onClick={(e) => handleExportNote(note.id, e)}>
                        <Download className="h-4 w-4 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200" />
                      </button>
                      <button onClick={(e) => handleArchiveNote(note.id, e)}>
                        <Trash2 className="h-4 w-4 text-slate-400 hover:text-red-500" />
                      </button>
                    </div>
                  </div>
                  <h3 className="font-semibold text-slate-900 dark:text-white mb-1 line-clamp-2">{note.title}</h3>
                  <p className="text-sm text-slate-500 dark:text-slate-300 mb-2">{note.subjectName} / {note.topicTitle}</p>
                  {note.preview && (
                    <p className="text-xs text-slate-400 dark:text-slate-300 line-clamp-2">{note.preview}</p>
                  )}
                  <div className="flex items-center justify-between mt-3 text-xs text-slate-400 dark:text-slate-300">
                    <span className="flex items-center gap-1">
                      <Clock className="h-3 w-3" />
                      {new Date(note.createdAt).toLocaleDateString()}
                    </span>
                    {note.versionCount > 1 && (
                      <span>v{note.versionCount}</span>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>

        {/* Note Preview */}
        <div className="lg:col-span-2">
          {selectedNote ? (
            <Card className="glass-panel border border-white/40 sticky top-4">
              <CardHeader className="border-b border-white/20">
                <div className="flex items-start justify-between">
                  <div>
                    <Badge className={`${formatBadgeColor(selectedNote.format)} mb-2`}>
                      {formatLabel(selectedNote.format)}
                    </Badge>
                    <CardTitle className="text-xl">{selectedNote.title}</CardTitle>
                    <CardDescription>
                      {selectedNote.subjectName} / {selectedNote.topicTitle}
                    </CardDescription>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleRegenerateNote(selectedNote.id)}
                      disabled={isGenerating}
                      className="btn-outline h-9 px-4 text-sm"
                    >
                      <RefreshCw className={`h-4 w-4 mr-1 ${isGenerating ? 'animate-spin' : ''}`} />
                      Regenerate
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setSelectedNote(null)}
                      className="text-slate-500 hover:text-slate-700 dark:text-slate-300 dark:hover:text-white"
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="p-6 max-h-[70vh] overflow-y-auto">
                <div className="prose prose-sm max-w-none dark:prose-invert">
                  <ReactMarkdown>{selectedNote.contentMd || 'Loading...'}</ReactMarkdown>
                </div>
              </CardContent>
            </Card>
          ) : (
            <Card className="glass-panel border border-white/40">
              <CardContent className="p-12 text-center">
                <Eye className="h-16 w-16 mx-auto mb-4 text-slate-300" />
                <p className="text-slate-500 dark:text-slate-300">Select a note to preview</p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>

      {/* Generate Notes Modal */}
      {showGenerateModal && (
        <div className="modal-overlay p-4">
          <Card className="modal-content">
            <CardHeader className="border-b border-white/20">
              <CardTitle className="flex items-center gap-2">
                <Sparkles className="h-5 w-5 text-emerald-500" />
                Generate New Notes
              </CardTitle>
              <CardDescription>
                Choose a topic and format to generate AI-powered study notes
              </CardDescription>
            </CardHeader>
            <CardContent className="p-6 space-y-4">
              {/* Subject Selection */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1">Subject</label>
                <select
                  className="input-modern"
                  value={selectedSubject || ''}
                  onChange={(e) => handleSubjectChange(Number(e.target.value))}
                >
                  <option value="">Select Subject</option>
                  {subjects.map((subject) => (
                    <option key={subject.id} value={subject.id}>{subject.name}</option>
                  ))}
                </select>
              </div>

              {/* Chapter Selection */}
              {selectedSubject && (
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1">Chapter</label>
                  <select
                    className="input-modern"
                    value={selectedChapter || ''}
                    onChange={(e) => handleChapterChange(Number(e.target.value))}
                  >
                    <option value="">Select Chapter</option>
                    {chapters.map((chapter) => (
                      <option key={chapter.id} value={chapter.id}>{chapter.name}</option>
                    ))}
                  </select>
                </div>
              )}

              {/* Topic Selection */}
              {selectedChapter && (
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1">Topic</label>
                  <select
                    className="input-modern"
                    value={selectedTopic || ''}
                    onChange={(e) => setSelectedTopic(Number(e.target.value))}
                  >
                    <option value="">Select Topic</option>
                    {topics.map((topic) => (
                      <option key={topic.id} value={topic.id}>{topic.title}</option>
                    ))}
                  </select>
                </div>
              )}

              {/* Format Selection */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">Notes Format</label>
                <div className="grid grid-cols-3 gap-2">
                  <button
                    className={`p-3 rounded-lg border text-center transition-all ${
                      generateFormat === 'SHORT' 
                        ? 'border-[#0F9D58] bg-white/80 text-[#0F9D58]' 
                        : 'border-white/30 bg-white/50 text-slate-700 dark:text-slate-100 hover:border-white/60'
                    }`}
                    onClick={() => setGenerateFormat('SHORT')}
                  >
                    <Sparkles className="h-5 w-5 mx-auto mb-1" />
                    <span className="text-sm font-medium">Quick</span>
                    <p className="text-xs text-slate-500 dark:text-slate-300">5 min read</p>
                  </button>
                  <button
                    className={`p-3 rounded-lg border text-center transition-all ${
                      generateFormat === 'LONG' 
                        ? 'border-[#0F9D58] bg-white/80 text-[#0F9D58]' 
                        : 'border-white/30 bg-white/50 text-slate-700 dark:text-slate-100 hover:border-white/60'
                    }`}
                    onClick={() => setGenerateFormat('LONG')}
                  >
                    <FileEdit className="h-5 w-5 mx-auto mb-1" />
                    <span className="text-sm font-medium">Detailed</span>
                    <p className="text-xs text-slate-500 dark:text-slate-300">Deep study</p>
                  </button>
                  <button
                    className={`p-3 rounded-lg border text-center transition-all ${
                      generateFormat === 'REVISION_SHEET' 
                        ? 'border-[#0F9D58] bg-white/80 text-[#0F9D58]' 
                        : 'border-white/30 bg-white/50 text-slate-700 dark:text-slate-100 hover:border-white/60'
                    }`}
                    onClick={() => setGenerateFormat('REVISION_SHEET')}
                  >
                    <BookOpen className="h-5 w-5 mx-auto mb-1" />
                    <span className="text-sm font-medium">Revision</span>
                    <p className="text-xs text-slate-500 dark:text-slate-300">Exam prep</p>
                  </button>
                </div>
              </div>

              {/* Language Selection */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">Language</label>
                <div className="flex gap-2">
                  <button
                    className={`flex-1 py-2 rounded-lg border transition-all ${
                      generateLanguage === 'en' 
                        ? 'border-[#0F9D58] bg-white/80 text-[#0F9D58]' 
                        : 'border-white/30 bg-white/50 text-slate-700 dark:text-slate-100 hover:border-white/60'
                    }`}
                    onClick={() => setGenerateLanguage('en')}
                  >
                    English
                  </button>
                  <button
                    className={`flex-1 py-2 rounded-lg border transition-all ${
                      generateLanguage === 'hi' 
                        ? 'border-[#0F9D58] bg-white/80 text-[#0F9D58]' 
                        : 'border-white/30 bg-white/50 text-slate-700 dark:text-slate-100 hover:border-white/60'
                    }`}
                    onClick={() => setGenerateLanguage('hi')}
                  >
                    Hindi
                  </button>
                </div>
              </div>
            </CardContent>
            <div className="p-4 border-t border-white/20 flex justify-end gap-2">
              <Button variant="outline" className="btn-outline" onClick={() => setShowGenerateModal(false)}>
                Cancel
              </Button>
              <Button 
                onClick={handleGenerateNotes}
                disabled={!selectedTopic || isGenerating}
                className="btn-primary"
              >
                {isGenerating ? (
                  <>
                    <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                    Generating...
                  </>
                ) : (
                  <>
                    <Sparkles className="h-4 w-4 mr-2" />
                    Generate Notes
                  </>
                )}
              </Button>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}

