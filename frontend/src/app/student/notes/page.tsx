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
  ChevronRight,
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
      setNotes(response.data?.notes || []);
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
      setStats(response.data);
    } catch (error) {
      console.error('Failed to load stats:', error);
    }
  };

  const loadSubjects = async () => {
    try {
      // For CBSE, we need to get the board first, then grades, then subjects
      const boardsResponse = await contentAPI.getBoards();
      const cbseBoard = boardsResponse.data?.find((b: any) => b.name === 'CBSE');
      if (cbseBoard) {
        const gradesResponse = await contentAPI.getGradesByBoard(cbseBoard.id);
        // Get subjects for grade 9-12 (common for CBSE)
        if (gradesResponse.data?.length > 0) {
          const subjectsResponse = await contentAPI.getSubjectsByGrade(gradesResponse.data[0].id);
          setSubjects(subjectsResponse.data || []);
        }
      }
    } catch (error) {
      console.error('Failed to load subjects:', error);
    }
  };

  const loadChapters = async (subjectId: number) => {
    try {
      const response = await contentAPI.getChaptersBySubject(subjectId);
      setChapters(response.data || []);
    } catch (error) {
      console.error('Failed to load chapters:', error);
    }
  };

  const loadTopics = async (chapterId: number) => {
    try {
      const response = await contentAPI.getTopicsByChapter(chapterId);
      setTopics(response.data || []);
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
      if (response.data?.note) {
        setSelectedNote(response.data.note);
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
      if (response.data?.note) {
        setSelectedNote(response.data.note);
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
            <Card key={i} className="animate-pulse">
              <CardContent className="p-6">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-8 bg-gray-200 rounded w-1/2"></div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6" data-testid="notes-page">
      {/* Header */}
      <div className="bg-gradient-to-r from-violet-600 to-purple-600 rounded-2xl p-8 text-white shadow-lg">
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
            className="bg-white text-purple-600 hover:bg-purple-50 font-semibold"
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
          <Card className="border-gray-100 shadow-sm">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-violet-100 rounded-lg flex items-center justify-center">
                <FileText className="h-5 w-5 text-violet-600" />
              </div>
              <div>
                <p className="text-2xl font-bold text-gray-900">{stats.totalNotes}</p>
                <p className="text-xs text-gray-500">Total Notes</p>
              </div>
            </CardContent>
          </Card>
          <Card className="border-gray-100 shadow-sm">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-blue-100 rounded-lg flex items-center justify-center">
                <Sparkles className="h-5 w-5 text-blue-600" />
              </div>
              <div>
                <p className="text-2xl font-bold text-gray-900">{stats.shortNotes}</p>
                <p className="text-xs text-gray-500">Quick Notes</p>
              </div>
            </CardContent>
          </Card>
          <Card className="border-gray-100 shadow-sm">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-purple-100 rounded-lg flex items-center justify-center">
                <FileEdit className="h-5 w-5 text-purple-600" />
              </div>
              <div>
                <p className="text-2xl font-bold text-gray-900">{stats.longNotes}</p>
                <p className="text-xs text-gray-500">Detailed</p>
              </div>
            </CardContent>
          </Card>
          <Card className="border-gray-100 shadow-sm">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-amber-100 rounded-lg flex items-center justify-center">
                <BookOpen className="h-5 w-5 text-amber-600" />
              </div>
              <div>
                <p className="text-2xl font-bold text-gray-900">{stats.revisionSheets}</p>
                <p className="text-xs text-gray-500">Revision Sheets</p>
              </div>
            </CardContent>
          </Card>
          <Card className="border-gray-100 shadow-sm">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-yellow-100 rounded-lg flex items-center justify-center">
                <Star className="h-5 w-5 text-yellow-600" />
              </div>
              <div>
                <p className="text-2xl font-bold text-gray-900">{stats.favoriteNotes}</p>
                <p className="text-xs text-gray-500">Favorites</p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Filters */}
      <Card className="border-gray-100 shadow-sm">
        <CardContent className="p-4">
          <div className="flex flex-wrap items-center gap-4">
            <div className="flex-1 min-w-[200px]">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  placeholder="Search notes..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Filter className="h-4 w-4 text-gray-500" />
              <Button
                variant={selectedFormat === null ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedFormat(null)}
              >
                All
              </Button>
              <Button
                variant={selectedFormat === 'SHORT' ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedFormat('SHORT')}
              >
                Quick
              </Button>
              <Button
                variant={selectedFormat === 'LONG' ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedFormat('LONG')}
              >
                Detailed
              </Button>
              <Button
                variant={selectedFormat === 'REVISION_SHEET' ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedFormat('REVISION_SHEET')}
              >
                Revision
              </Button>
            </div>
            <Button
              variant={showFavoritesOnly ? "default" : "outline"}
              size="sm"
              onClick={() => setShowFavoritesOnly(!showFavoritesOnly)}
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
            <Card className="border-gray-100 shadow-sm" data-testid="notes-empty-state">
              <CardContent className="p-8 text-center">
                <FileText className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                <p className="text-gray-500 mb-4">No notes yet</p>
                <Button onClick={handleOpenGenerateModal}>
                  <Plus className="h-4 w-4 mr-2" />
                  Generate Your First Notes
                </Button>
              </CardContent>
            </Card>
          ) : (
            notes.map((note) => (
              <Card 
                key={note.id} 
                className={`border-gray-100 shadow-sm cursor-pointer hover:shadow-md transition-all ${selectedNote?.id === note.id ? 'ring-2 ring-purple-500' : ''}`}
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
                          <StarOff className="h-4 w-4 text-gray-400 hover:text-yellow-500" />
                        )}
                      </button>
                      <button onClick={(e) => handleExportNote(note.id, e)}>
                        <Download className="h-4 w-4 text-gray-400 hover:text-gray-600" />
                      </button>
                      <button onClick={(e) => handleArchiveNote(note.id, e)}>
                        <Trash2 className="h-4 w-4 text-gray-400 hover:text-red-500" />
                      </button>
                    </div>
                  </div>
                  <h3 className="font-semibold text-gray-900 mb-1 line-clamp-2">{note.title}</h3>
                  <p className="text-sm text-gray-500 mb-2">{note.subjectName} • {note.topicTitle}</p>
                  {note.preview && (
                    <p className="text-xs text-gray-400 line-clamp-2">{note.preview}</p>
                  )}
                  <div className="flex items-center justify-between mt-3 text-xs text-gray-400">
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
            <Card className="border-gray-100 shadow-sm sticky top-4">
              <CardHeader className="border-b border-gray-100">
                <div className="flex items-start justify-between">
                  <div>
                    <Badge className={`${formatBadgeColor(selectedNote.format)} mb-2`}>
                      {formatLabel(selectedNote.format)}
                    </Badge>
                    <CardTitle className="text-xl">{selectedNote.title}</CardTitle>
                    <CardDescription>
                      {selectedNote.subjectName} • {selectedNote.topicTitle}
                    </CardDescription>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleRegenerateNote(selectedNote.id)}
                      disabled={isGenerating}
                    >
                      <RefreshCw className={`h-4 w-4 mr-1 ${isGenerating ? 'animate-spin' : ''}`} />
                      Regenerate
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setSelectedNote(null)}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="p-6 max-h-[70vh] overflow-y-auto">
                <div className="prose prose-sm max-w-none">
                  <ReactMarkdown>{selectedNote.contentMd || 'Loading...'}</ReactMarkdown>
                </div>
              </CardContent>
            </Card>
          ) : (
            <Card className="border-gray-100 shadow-sm">
              <CardContent className="p-12 text-center">
                <Eye className="h-16 w-16 mx-auto mb-4 text-gray-200" />
                <p className="text-gray-500">Select a note to preview</p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>

      {/* Generate Notes Modal */}
      {showGenerateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <Card className="w-full max-w-lg">
            <CardHeader className="border-b border-gray-100">
              <CardTitle className="flex items-center gap-2">
                <Sparkles className="h-5 w-5 text-purple-600" />
                Generate New Notes
              </CardTitle>
              <CardDescription>
                Choose a topic and format to generate AI-powered study notes
              </CardDescription>
            </CardHeader>
            <CardContent className="p-6 space-y-4">
              {/* Subject Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Subject</label>
                <select
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
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
                  <label className="block text-sm font-medium text-gray-700 mb-1">Chapter</label>
                  <select
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
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
                  <label className="block text-sm font-medium text-gray-700 mb-1">Topic</label>
                  <select
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
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
                <label className="block text-sm font-medium text-gray-700 mb-2">Notes Format</label>
                <div className="grid grid-cols-3 gap-2">
                  <button
                    className={`p-3 rounded-lg border text-center transition-all ${
                      generateFormat === 'SHORT' 
                        ? 'border-purple-500 bg-purple-50 text-purple-700' 
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                    onClick={() => setGenerateFormat('SHORT')}
                  >
                    <Sparkles className="h-5 w-5 mx-auto mb-1" />
                    <span className="text-sm font-medium">Quick</span>
                    <p className="text-xs text-gray-500">5 min read</p>
                  </button>
                  <button
                    className={`p-3 rounded-lg border text-center transition-all ${
                      generateFormat === 'LONG' 
                        ? 'border-purple-500 bg-purple-50 text-purple-700' 
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                    onClick={() => setGenerateFormat('LONG')}
                  >
                    <FileEdit className="h-5 w-5 mx-auto mb-1" />
                    <span className="text-sm font-medium">Detailed</span>
                    <p className="text-xs text-gray-500">Deep study</p>
                  </button>
                  <button
                    className={`p-3 rounded-lg border text-center transition-all ${
                      generateFormat === 'REVISION_SHEET' 
                        ? 'border-purple-500 bg-purple-50 text-purple-700' 
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                    onClick={() => setGenerateFormat('REVISION_SHEET')}
                  >
                    <BookOpen className="h-5 w-5 mx-auto mb-1" />
                    <span className="text-sm font-medium">Revision</span>
                    <p className="text-xs text-gray-500">Exam prep</p>
                  </button>
                </div>
              </div>

              {/* Language Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Language</label>
                <div className="flex gap-2">
                  <button
                    className={`flex-1 py-2 rounded-lg border transition-all ${
                      generateLanguage === 'en' 
                        ? 'border-purple-500 bg-purple-50 text-purple-700' 
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                    onClick={() => setGenerateLanguage('en')}
                  >
                    English
                  </button>
                  <button
                    className={`flex-1 py-2 rounded-lg border transition-all ${
                      generateLanguage === 'hi' 
                        ? 'border-purple-500 bg-purple-50 text-purple-700' 
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                    onClick={() => setGenerateLanguage('hi')}
                  >
                    हिंदी
                  </button>
                </div>
              </div>
            </CardContent>
            <div className="p-4 border-t border-gray-100 flex justify-end gap-2">
              <Button variant="outline" onClick={() => setShowGenerateModal(false)}>
                Cancel
              </Button>
              <Button 
                onClick={handleGenerateNotes}
                disabled={!selectedTopic || isGenerating}
                className="bg-purple-600 hover:bg-purple-700"
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

